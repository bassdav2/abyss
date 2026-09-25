package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunSetup;
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
 * werden roh an ffmpeg übergeben und mit Nachbarpixel-Skalierung auf 1920 x 1080 kodiert.
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
        var music = Path.of("src/main/resources/audio/music.wav");
        var command =
                new java.util.ArrayList<>(
                        java.util.List.of(
                                "ffmpeg",
                                "-y",
                                "-loglevel",
                                "error",
                                "-f",
                                "rawvideo",
                                "-pix_fmt",
                                "bgra",
                                "-s",
                                "480x270",
                                "-r",
                                "" + FPS,
                                "-i",
                                "-"));
        if (Files.exists(music))
            command.addAll(
                    java.util.List.of(
                            "-stream_loop",
                            "-1",
                            "-i",
                            music.toString(),
                            "-shortest",
                            "-c:a",
                            "aac",
                            "-b:a",
                            "128k"));
        command.addAll(
                java.util.List.of(
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
                        target.toString()));
        var process = new ProcessBuilder(command).redirectErrorStream(true).start();
        var font = PixelFont.load();
        var renderer = new WorldRenderer(font, new SpriteBank());
        var settings = Settings.DEFAULT;
        try (var out = process.getOutputStream()) {
            for (int i = 0; i < FPS * 5; i++) {
                renderer.update(1.0 / FPS, null, settings);
                renderer.renderTitle(settings, true);
                write(out, renderer.frame());
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
                    label(font, renderer.frame());
                    write(out, renderer.frame());
                }
            }
            for (int frame = 0; frame < FPS * 8; frame++) {
                renderer.update(1.0 / FPS, null, settings);
                renderer.renderEnding(frame / (double) FPS, settings);
                label(font, renderer.frame(), 6);
                write(out, renderer.frame());
            }
        }
        int code = process.waitFor();
        System.out.println(new String(process.getInputStream().readAllBytes()));
        if (code != 0) throw new IOException("ffmpeg beendet mit " + code);
        System.out.println("ABYSS_DEMO " + target);
    }

    private static void label(PixelFont font, Frame frame) {
        label(font, frame, 22);
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
