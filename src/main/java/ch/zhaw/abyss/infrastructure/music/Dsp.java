package ch.zhaw.abyss.infrastructure.music;

/** Gemeinsame Grundbausteine der Klangerzeugung: Abtastrate, Sinustabelle, Antialiasing. */
final class Dsp {
    static final int RATE = 44100;
    static final double DT = 1.0 / RATE;

    /** Steuerblock: Filter, Hüllkurven der Filter und Sequenzer laufen in diesem Takt. */
    static final int CONTROL = 32;

    private static final int TABLE = 4096;
    private static final float[] SINE = new float[TABLE + 1];

    static {
        for (int i = 0; i <= TABLE; i++) SINE[i] = (float) Math.sin(2 * Math.PI * i / TABLE);
    }

    private Dsp() {}

    /** Sinus über eine Phase in Umdrehungen (beliebiger Wert, auch negativ). */
    static double sin(double phase) {
        double p = (phase - Math.floor(phase)) * TABLE;
        int i = (int) p;
        return SINE[i] + (SINE[i + 1] - SINE[i]) * (p - i);
    }

    /** MIDI-Tonhöhe in Hertz (69 = A4 = 440 Hz). */
    static double freq(double midi) {
        return 440 * Math.pow(2, (midi - 69) / 12);
    }

    /** PolyBLEP-Korrektur an einer Sprungstelle; t ist die Phase, dt der Phasenschritt. */
    static double blep(double t, double dt) {
        if (t < dt) {
            t /= dt;
            return t + t - t * t - 1;
        }
        if (t > 1 - dt) {
            t = (t - 1) / dt;
            return t * t + t + t + 1;
        }
        return 0;
    }

    /** Weiche Sättigung (rationale tanh-Näherung), begrenzt auf ±1. */
    static double soft(double x) {
        if (x > 3) return 1;
        if (x < -3) return -1;
        double x2 = x * x;
        return x * (27 + x2) / (27 + 9 * x2);
    }

    /** Koeffizient eines Einpolfilters für eine Zeitkonstante in Sekunden bei n Abtastwerten. */
    static double smoothing(double seconds, int samples) {
        return seconds <= 0 ? 1 : 1 - Math.exp(-samples * DT / seconds);
    }

    /** Deterministisches Rauschen (xorshift); Zustand liegt beim Aufrufer. */
    static long next(long seed) {
        seed ^= seed << 13;
        seed ^= seed >>> 7;
        seed ^= seed << 17;
        return seed;
    }

    /** Rauschwert in [-1, 1) aus einem Zustand von {@link #next(long)}. */
    static double noise(long seed) {
        return (seed >>> 11) * 0x1.0p-52 - 1;
    }
}
