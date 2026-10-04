package ch.zhaw.abyss.infrastructure.music;

/**
 * Zustandsvariablenfilter in Trapez-Topologie (nach Zavalishin und Simper). Stabil auch bei schnell
 * modulierter Grenzfrequenz; liefert Tief-, Band- und Hochpass aus einem Durchlauf.
 */
final class Svf {
    private double ic1, ic2, a1, a2, a3, k = 1.4;
    double low, band, high;

    /**
     * @param cutoff Grenzfrequenz in Hertz
     * @param q Güte (0.5 weich, 0.707 neutral, über 4 stark resonant)
     */
    void set(double cutoff, double q) {
        double fc = Math.max(16, Math.min(cutoff, Dsp.RATE * .45));
        double g = Math.tan(Math.PI * fc / Dsp.RATE);
        k = 1 / Math.max(.3, q);
        a1 = 1 / (1 + g * (g + k));
        a2 = g * a1;
        a3 = g * a2;
    }

    /**
     * Verarbeitet einen Abtastwert und gibt den Tiefpass zurück; Band und Hoch liegen in Feldern.
     */
    double process(double v0) {
        double v3 = v0 - ic2;
        double v1 = a1 * ic1 + a2 * v3;
        double v2 = ic2 + a2 * ic1 + a3 * v3;
        ic1 = 2 * v1 - ic1;
        ic2 = 2 * v2 - ic2;
        low = v2;
        band = v1;
        high = v0 - k * v1 - v2;
        return v2;
    }

    void reset() {
        ic1 = ic2 = low = band = high = 0;
    }
}
