package ch.zhaw.abyss.infrastructure.music;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Alle Spielklänge als Abtastwerte im Speicher (16-Bit-WAV aus {@code /audio}). Jeder Name hat eine
 * feste Nummer, damit der Spiel-Thread Klänge anfordern kann, bevor die Daten geladen sind.
 */
final class SoundBank {
    /** Bekannte Klänge in fester Reihenfolge; die Position ist die Nummer. */
    static final List<String> NAMES =
            List.of(
                    "hit",
                    "hurt",
                    "swing",
                    "dash",
                    "jump",
                    "shot",
                    "down",
                    "click",
                    "upgrade",
                    "clear",
                    "victory",
                    "defeat",
                    "pulse",
                    "warning",
                    "ambience",
                    "crit",
                    "block",
                    "explosion",
                    "zap",
                    "freeze",
                    "spawn",
                    "harpoon",
                    "land",
                    "crate",
                    "pickup",
                    "core",
                    "door",
                    "roar",
                    "curse");

    /** Stereo, verschränkt (links, rechts, links, …); {@code null}, wenn die Datei fehlt. */
    private final float[][] samples = new float[NAMES.size()][];

    private SoundBank() {}

    /**
     * Lädt alle Klänge; fehlende oder defekte Dateien bleiben leer.
     *
     * @return geladene Klangbank
     */
    static SoundBank load() {
        var bank = new SoundBank();
        for (int i = 0; i < NAMES.size(); i++) bank.samples[i] = read(NAMES.get(i));
        return bank;
    }

    /** Nummer eines Klangs oder -1. */
    static int id(String name) {
        return NAMES.indexOf(name);
    }

    float[] samples(int id) {
        return id >= 0 && id < samples.length ? samples[id] : null;
    }

    int loaded() {
        int count = 0;
        for (float[] data : samples) if (data != null && data.length > 0) count++;
        return count;
    }

    private static float[] read(String name) {
        var url = SoundBank.class.getResource("/audio/" + name + ".wav");
        if (url == null) return null;
        try (var in =
                javax.sound.sampled.AudioSystem.getAudioInputStream(
                        new BufferedInputStream(url.openStream()))) {
            return decode(in);
        } catch (IOException | UnsupportedAudioFileException error) {
            System.err.println("Klang nicht lesbar: " + name + " (" + error.getMessage() + ")");
            return null;
        }
    }

    private static float[] decode(AudioInputStream in) throws IOException {
        AudioFormat format = in.getFormat();
        if (format.getSampleSizeInBits() != 16
                || format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
            throw new IOException("nur 16-Bit-PCM wird unterstützt");
        byte[] bytes = in.readAllBytes();
        int channels = format.getChannels();
        boolean big = format.isBigEndian();
        int frames = bytes.length / (2 * channels);
        double step = format.getSampleRate() / Dsp.RATE;
        int length = (int) (frames / step);
        float[] out = new float[length * 2];
        for (int i = 0; i < length; i++) {
            int frame = Math.min(frames - 1, (int) (i * step));
            for (int c = 0; c < 2; c++) {
                int index = (frame * channels + Math.min(c, channels - 1)) * 2;
                int lo = bytes[index + (big ? 1 : 0)] & 0xFF, hi = bytes[index + (big ? 0 : 1)];
                out[i * 2 + c] = (short) (hi << 8 | lo) / 32768f;
            }
        }
        return out;
    }
}
