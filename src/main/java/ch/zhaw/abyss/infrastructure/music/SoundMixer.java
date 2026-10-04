package ch.zhaw.abyss.infrastructure.music;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Effektmischer im Audio-Thread: feste Zahl an Stimmen, höchstens drei gleichzeitige Kopien
 * desselben Klangs, eine begrenzte Warteschlange zwischen Spiel- und Audio-Thread. Wer mehr
 * anfordert, als gespielt werden kann, verdrängt die ältesten Stimmen oder wird verworfen. So
 * bleiben Kosten und Lautstärke auch bei tausenden Ereignissen pro Sekunde konstant.
 */
final class SoundMixer {
    static final int VOICES = 24, PER_SOUND = 3, QUEUE = 128;

    private final int[] queueIds = new int[QUEUE];
    private final float[] queueGains = new float[QUEUE];
    private final AtomicLong head = new AtomicLong(), tail = new AtomicLong();
    private final int[] sound = new int[VOICES], position = new int[VOICES];
    private final float[] gain = new float[VOICES];
    private final long[] started = new long[VOICES];
    private volatile double ambienceTarget;
    private double ambienceLevel;
    private int ambiencePosition;
    private long counter, dropped;
    private SoundBank bank;

    SoundMixer() {
        java.util.Arrays.fill(sound, -1);
    }

    void bank(SoundBank value) {
        bank = value;
    }

    /**
     * Fordert einen Klang an (aus dem Spiel-Thread). Ist die Warteschlange voll, verfällt die
     * Anforderung: ein verspäteter Klang wäre schlimmer als ein fehlender.
     *
     * @return {@code true}, wenn die Anforderung angenommen wurde
     */
    boolean trigger(int id, double level) {
        if (id < 0) return false;
        long t = tail.get();
        if (t - head.get() >= QUEUE) {
            dropped++;
            return false;
        }
        int slot = (int) (t % QUEUE);
        queueIds[slot] = id;
        queueGains[slot] = (float) level;
        tail.lazySet(t + 1);
        return true;
    }

    /** Lautstärke der Unterwasser-Atmosphäre (Endlosschleife), 0 schaltet sie ab. */
    void ambience(double level) {
        ambienceTarget = level;
    }

    /** Wartende Anforderungen (für Tests). */
    int pending() {
        return (int) (tail.get() - head.get());
    }

    /** Spielende Stimmen (für Tests). */
    int active() {
        int count = 0;
        for (int s : sound) if (s >= 0) count++;
        return count;
    }

    /** Spielende Kopien eines Klangs (für Tests). */
    int active(int id) {
        int count = 0;
        for (int s : sound) if (s == id) count++;
        return count;
    }

    /** Verworfene Anforderungen seit dem Start. */
    long dropped() {
        return dropped;
    }

    /** Addiert alle Stimmen in die Puffer. */
    void render(double[] left, double[] right, int n) {
        for (long h = head.get(), t = tail.get(); h < t; h++) {
            int slot = (int) (h % QUEUE);
            if (bank != null) start(queueIds[slot], queueGains[slot]);
            head.lazySet(h + 1);
        }
        if (bank == null) return;
        for (int v = 0; v < VOICES; v++) {
            if (sound[v] < 0) continue;
            float[] data = bank.samples(sound[v]);
            if (data == null) {
                sound[v] = -1;
                continue;
            }
            int frames = data.length / 2, p = position[v];
            int count = Math.min(n, frames - p);
            float g = gain[v];
            for (int s = 0; s < count; s++) {
                left[s] += data[(p + s) * 2] * g;
                right[s] += data[(p + s) * 2 + 1] * g;
            }
            position[v] = p + count;
            if (position[v] >= frames) sound[v] = -1;
        }
        ambience(left, right, n);
    }

    private void ambience(double[] left, double[] right, int n) {
        float[] data = bank.samples(SoundBank.id("ambience"));
        double target = ambienceTarget;
        if (data == null || data.length == 0 || target <= 0 && ambienceLevel <= 1e-4) return;
        int frames = data.length / 2;
        double step = (target - ambienceLevel) / n;
        for (int s = 0; s < n; s++) {
            ambienceLevel += step;
            left[s] += data[ambiencePosition * 2] * ambienceLevel;
            right[s] += data[ambiencePosition * 2 + 1] * ambienceLevel;
            if (++ambiencePosition >= frames) ambiencePosition = 0;
        }
    }

    private void start(int id, float level) {
        int same = 0, oldestSame = -1, free = -1, oldest = 0;
        for (int v = 0; v < VOICES; v++) {
            if (sound[v] < 0) {
                if (free < 0) free = v;
                continue;
            }
            if (sound[v] == id) {
                same++;
                if (oldestSame < 0 || started[v] < started[oldestSame]) oldestSame = v;
            }
            if (started[v] < started[oldest] || sound[oldest] < 0) oldest = v;
        }
        int voice = same >= PER_SOUND ? oldestSame : free >= 0 ? free : oldest;
        sound[voice] = id;
        position[voice] = 0;
        gain[voice] = level;
        started[voice] = counter++;
    }
}
