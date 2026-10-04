package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.infrastructure.music.MusicDirector;
import ch.zhaw.abyss.infrastructure.music.MusicEngine;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.render.SpriteBank;
import ch.zhaw.abyss.ui.render.WorldRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Erzeugt ein gekennzeichnetes Gameplay-Video ohne Fenster: Titel, dann Ausschnitte aus allen vier
 * Sektionen und Bosskämpfen, gespielt vom Testspieler, zum Schluss die Siegesszene. Die Pixelbilder
 * werden roh an ffmpeg übergeben und mit Nachbarpixel-Skalierung auf 1920 x 1080 kodiert; die
 * Musikregie spielt dabei mit, der Bordsynthesizer liefert die Tonspur.
 */
public final class RenderDemo {
    private static final int FPS = 30;

    private RenderDemo() {}

    /**
     * @param args Zieldatei, Vorgabe {@code output/video/Abyss_Gameplay_Demo.mp4}
     * @throws Exception bei Kodierfehlern
     */
    public static void main(String[] args) throws Exception {
        var target = Path.of(args.length > 0 ? args[0] : "output/video/Abyss_Gameplay_Demo.mp4");
        Files.createDirectories(target.toAbsolutePath().getParent());
        var video = Files.createTempFile("abyss-demo", ".mp4");
        var soundtrack = Files.createTempFile("abyss-demo", ".wav");
        var process =
                new ProcessBuilder(
                                "ffmpeg",
                                "-y",
                                "-loglevel",
                                "error",
                                "-f",
                                "rawvideo",
                                "-pix_fmt",
                                "bgra",
                                "-s",
                                "960x540",
                                "-r",
                                "" + FPS,
                                "-i",
                                "-",
                                "-vf",
                                "scale=1920:1080:flags=neighbor",
                                "-c:v",
                                "libx264",
                                "-preset",
                                "medium",
                                "-crf",
                                "18",
                                "-pix_fmt",
                                "yuv420p",
                                video.toString())
                        .redirectErrorStream(true)
                        .start();
        var font = PixelFont.load();
        var renderer = new WorldRenderer(font, new SpriteBank());
        var settings = Settings.DEFAULT;
        var music = new Soundtrack();
        try (var out = process.getOutputStream()) {
            for (int i = 0; i < FPS * 5; i++) {
                renderer.update(1.0 / FPS, null, settings);
                renderer.renderTitle(settings, true);
                write(out, renderer.ui());
                music.frame(MusicDirector.cue(null, true, 0));
            }
            int[][] segments = {
                {0, 7}, {2, 6}, {4, 10}, {7, 7}, {10, 10}, {13, 7}, {16, 10}, {20, 7}, {23, 12}
            };
            var divers = DiverClass.values();
            for (int s = 0; s < segments.length; s++) {
                var run = new GameRun(RunSetup.standard(4000 + s * 17L, divers[s % divers.length]));
                fastForward(run, segments[s][0]);
                renderer.clearEffects();
                for (int frame = 0; frame < FPS * segments[s][1]; frame++) {
                    for (int step = 0; step < 120 / FPS; step++) {
                        if (run.phase() == GameRun.Phase.ROOM_CLEARED)
                            CampaignPilot.advance(run, 0);
                        else run.update(1.0 / 120, CampaignPilot.input(run));
                    }
                    for (var event : run.drainEvents()) renderer.event(event, run, settings);
                    renderer.update(1.0 / FPS, run, settings);
                    renderer.render(run, settings, true);
                    label(font, renderer.ui());
                    write(out, renderer.ui());
                    music.frame(MusicDirector.cue(run, false, 0));
                }
            }
            for (int frame = 0; frame < FPS * 8; frame++) {
                renderer.update(1.0 / FPS, null, settings);
                renderer.renderEnding(frame / (double) FPS, settings);
                label(font, renderer.ui(), 12);
                write(out, renderer.ui());
                music.frame(new MusicDirector.Cue("ending", 1, 0, 1));
            }
        }
        int code = process.waitFor();
        System.out.println(new String(process.getInputStream().readAllBytes()));
        if (code != 0) throw new IOException("ffmpeg beendet mit " + code);
        music.write(soundtrack);
        var mux =
                new ProcessBuilder(
                                "ffmpeg",
                                "-y",
                                "-loglevel",
                                "error",
                                "-i",
                                video.toString(),
                                "-i",
                                soundtrack.toString(),
                                "-c:v",
                                "copy",
                                "-c:a",
                                "aac",
                                "-b:a",
                                "160k",
                                "-shortest",
                                target.toString())
                        .redirectErrorStream(true)
                        .start();
        int muxed = mux.waitFor();
        System.out.println(new String(mux.getInputStream().readAllBytes()));
        Files.deleteIfExists(video);
        Files.deleteIfExists(soundtrack);
        if (muxed != 0) throw new IOException("ffmpeg (Ton) beendet mit " + muxed);
        System.out.println("ABYSS_DEMO " + target);
    }

    /** Spielt die Musikregie Bild für Bild mit und sammelt den Ton für das Video. */
    private static final class Soundtrack {
        private final MusicEngine engine = MusicEngine.offline();
        private final java.io.ByteArrayOutputStream pcm = new java.io.ByteArrayOutputStream();
        private final double[] left = new double[44100 / FPS], right = new double[44100 / FPS];

        Soundtrack() {
            engine.volume(.8);
        }

        void frame(MusicDirector.Cue cue) {
            engine.cue(cue.song());
            engine.intensity(cue.intensity());
            engine.muffle(cue.muffle());
            engine.tempo(cue.tempo());
            engine.render(left, right);
            for (int i = 0; i < left.length; i++) {
                int l = (int) Math.round(left[i] * 32767), r = (int) Math.round(right[i] * 32767);
                pcm.write(l);
                pcm.write(l >> 8);
                pcm.write(r);
                pcm.write(r >> 8);
            }
        }

        void write(Path file) throws IOException {
            byte[] data = pcm.toByteArray();
            var header = java.nio.ByteBuffer.allocate(44).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            header.put("RIFF".getBytes()).putInt(36 + data.length).put("WAVE".getBytes());
            header.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 2);
            header.putInt(44100).putInt(44100 * 4).putShort((short) 4).putShort((short) 16);
            header.put("data".getBytes()).putInt(data.length);
            try (var out = Files.newOutputStream(file)) {
                out.write(header.array());
                out.write(data);
            }
        }
    }

    private static void label(PixelFont font, Frame frame) {
        label(font, frame, 44);
    }

    private static void label(PixelFont font, Frame frame, int y) {
        var text = "AUTOMATISCHE DEMO · TESTSPIELER";
        font.drawOutlined(
                frame,
                text,
                (frame.width() - font.width(text, 1)) / 2,
                y,
                0x90FFFFFF,
                0xA0000000,
                1);
    }

    private static void fastForward(GameRun run, int depth) {
        for (int guard = 0; guard < 120 * 60 * 30 && run.room().depth() < depth; guard++) {
            if (run.phase() == GameRun.Phase.ROOM_CLEARED) CampaignPilot.advance(run, guard % 2);
            else if (run.phase() == GameRun.Phase.DEFEAT) return;
            else run.update(1.0 / 120, CampaignPilot.input(run));
            run.drainEvents();
        }
    }

    private static void write(OutputStream out, Frame frame) throws IOException {
        var pixels = frame.pixels();
        var bytes = new byte[pixels.length * 4];
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            bytes[i * 4] = (byte) c;
            bytes[i * 4 + 1] = (byte) (c >> 8);
            bytes[i * 4 + 2] = (byte) (c >> 16);
            bytes[i * 4 + 3] = (byte) 255;
        }
        out.write(bytes);
    }
}
