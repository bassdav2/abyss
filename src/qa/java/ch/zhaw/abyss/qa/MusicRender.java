package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.infrastructure.music.MusicEngine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/**
 * Nimmt den Soundtrack ohne Ausgabegerät auf: jedes Stück als WAV-Datei, kampfbezogene Stücke mit
 * steigender Intensität (Ruhe, Kampf, volle Wucht). Misst Spitzenpegel, RMS und Rechenzeit.
 */
public final class MusicRender {
    private static final int RATE = 44100;
    private static final Set<String> STEADY = Set.of("title", "haven", "ending");

    private MusicRender() {}

    /**
     * @param args Zielverzeichnis (Vorgabe {@code build/music}), Sekunden pro Stück (Vorgabe 60)
     * @throws IOException bei Schreibfehlern
     */
    public static void main(String[] args) throws IOException {
        var out = Path.of(args.length > 0 ? args[0] : "build/music");
        double seconds = args.length > 1 ? Double.parseDouble(args[1]) : 60;
        Files.createDirectories(out);
        var report = new StringBuilder("{\n  \"songs\": [\n");
        var ids = MusicEngine.songIds();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            var engine = MusicEngine.offline();
            engine.volume(1);
            engine.cue(id);
            int frames = (int) (seconds * RATE);
            double[] left = new double[frames], right = new double[frames];
            int block = RATE / 4;
            long start = System.nanoTime();
            for (int offset = 0; offset < frames; offset += block) {
                double t = offset / (double) frames;
                engine.intensity(STEADY.contains(id) ? 1 : t < .25 ? .3 : t < .5 ? .55 : 1);
                int n = Math.min(block, frames - offset);
                double[] l = new double[n], r = new double[n];
                engine.render(l, r);
                System.arraycopy(l, 0, left, offset, n);
                System.arraycopy(r, 0, right, offset, n);
            }
            double cpu = (System.nanoTime() - start) / 1e9;
            double peak = 0, sum = 0, loud = 0;
            int loudFrames = 0;
            for (int s = 0; s < frames; s++) {
                peak = Math.max(peak, Math.max(Math.abs(left[s]), Math.abs(right[s])));
                double e = (left[s] * left[s] + right[s] * right[s]) / 2;
                sum += e;
                if (s > frames / 2) {
                    loud += e;
                    loudFrames++;
                }
            }
            double rms = 20 * Math.log10(Math.sqrt(sum / frames) + 1e-12);
            double rmsFull = 20 * Math.log10(Math.sqrt(loud / Math.max(1, loudFrames)) + 1e-12);
            write(out.resolve(id + ".wav"), left, right);
            String line =
                    String.format(
                            Locale.ROOT,
                            "    {\"id\": \"%s\", \"peak\": %.3f, \"rmsDb\": %.1f, \"rmsFullDb\":"
                                    + " %.1f, \"realtimeFactor\": %.1f}",
                            id,
                            peak,
                            rms,
                            rmsFull,
                            seconds / cpu);
            System.out.println(line.trim());
            report.append(line).append(i + 1 < ids.size() ? ",\n" : "\n");
        }
        report.append("  ]\n}\n");
        Files.writeString(out.resolve("music-report.json"), report);
    }

    private static void write(Path file, double[] left, double[] right) throws IOException {
        int frames = left.length;
        var buffer = ByteBuffer.allocate(44 + frames * 4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put("RIFF".getBytes()).putInt(36 + frames * 4).put("WAVE".getBytes());
        buffer.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 2);
        buffer.putInt(RATE).putInt(RATE * 4).putShort((short) 4).putShort((short) 16);
        buffer.put("data".getBytes()).putInt(frames * 4);
        for (int s = 0; s < frames; s++) {
            buffer.putShort((short) Math.round(Math.max(-1, Math.min(1, left[s])) * 32767));
            buffer.putShort((short) Math.round(Math.max(-1, Math.min(1, right[s])) * 32767));
        }
        Files.write(file, buffer.array());
    }
}
