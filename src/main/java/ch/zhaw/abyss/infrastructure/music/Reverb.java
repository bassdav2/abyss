package ch.zhaw.abyss.infrastructure.music;

/**
 * Hall aus einem Feedback-Delay-Netz mit acht Leitungen und Hadamard-Mischung: Vorverzögerung, zwei
 * Diffusoren, frequenzabhängige Dämpfung pro Leitung. Klingt wie ein grosser Stahlraum.
 */
final class Reverb {
    private static final int[] LENGTHS = {1557, 1617, 1491, 1422, 1277, 1356, 1188, 1116};
    private static final int[] DIFFUSERS = {556, 441, 341, 225};
    private static final double ANTI_DENORMAL = 1e-20;

    private final float[][] lines = new float[8][];
    private final int[] positions = new int[8];
    private final double[] feedback = new double[8], damped = new double[8];
    private final float[][] diffusers = new float[DIFFUSERS.length][];
    private final int[] diffuserPositions = new int[DIFFUSERS.length];
    private final float[] predelay;
    private final double damping;
    private int predelayPosition;

    /**
     * @param size Raumgrösse 0.5 bis 2
     * @param seconds Nachhallzeit (RT60)
     * @param brightness 0 dunkel bis 1 hell
     */
    Reverb(double size, double seconds, double brightness) {
        for (int i = 0; i < 8; i++) {
            int length = (int) Math.round(LENGTHS[i] * size);
            lines[i] = new float[length];
            feedback[i] = Math.pow(10, -3.0 * length / (Dsp.RATE * Math.max(.2, seconds)));
        }
        for (int i = 0; i < DIFFUSERS.length; i++)
            diffusers[i] = new float[(int) Math.round(DIFFUSERS[i] * Math.max(.6, size))];
        predelay = new float[(int) (Dsp.RATE * .022)];
        double cutoff = 1800 + 9000 * brightness;
        damping = 1 - Math.exp(-2 * Math.PI * cutoff / Dsp.RATE);
    }

    /** Liest die Sendepuffer und addiert den Hall in die Ausgänge. */
    void process(double[] inL, double[] inR, double[] outL, double[] outR, int n) {
        final double scale = 1 / Math.sqrt(8);
        for (int s = 0; s < n; s++) {
            double input = (inL[s] + inR[s]) * .5;
            float delayed = predelay[predelayPosition];
            predelay[predelayPosition] = (float) input;
            predelayPosition = (predelayPosition + 1) % predelay.length;
            double x = delayed;
            for (int d = 0; d < diffusers.length; d++) {
                float[] buffer = diffusers[d];
                int position = diffuserPositions[d];
                double stored = buffer[position];
                double v = x + stored * .5;
                buffer[position] = (float) v;
                x = stored - v * .5;
                diffuserPositions[d] = (position + 1) % buffer.length;
            }
            double a0 = tap(0), a1 = tap(1), a2 = tap(2), a3 = tap(3);
            double a4 = tap(4), a5 = tap(5), a6 = tap(6), a7 = tap(7);
            double b0 = a0 + a1, b1 = a0 - a1, b2 = a2 + a3, b3 = a2 - a3;
            double b4 = a4 + a5, b5 = a4 - a5, b6 = a6 + a7, b7 = a6 - a7;
            double c0 = b0 + b2, c2 = b0 - b2, c1 = b1 + b3, c3 = b1 - b3;
            double c4 = b4 + b6, c6 = b4 - b6, c5 = b5 + b7, c7 = b5 - b7;
            write(0, (c0 + c4) * scale + x);
            write(1, (c1 + c5) * scale - x);
            write(2, (c2 + c6) * scale + x);
            write(3, (c3 + c7) * scale - x);
            write(4, (c0 - c4) * scale + x);
            write(5, (c1 - c5) * scale - x);
            write(6, (c2 - c6) * scale + x);
            write(7, (c3 - c7) * scale - x);
            outL[s] += (a0 - a2 + a4 - a6) * .3;
            outR[s] += (a1 - a3 + a5 - a7) * .3;
        }
    }

    private double tap(int i) {
        double raw = lines[i][positions[i]];
        damped[i] += damping * (raw - damped[i]) + ANTI_DENORMAL;
        return damped[i] * feedback[i];
    }

    private void write(int i, double value) {
        lines[i][positions[i]] = (float) value;
        positions[i] = (positions[i] + 1) % lines[i].length;
    }
}
