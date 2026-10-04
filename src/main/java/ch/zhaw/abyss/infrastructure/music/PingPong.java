package ch.zhaw.abyss.infrastructure.music;

/**
 * Tempo-synchrones Stereo-Echo, das zwischen links und rechts springt; Filter in der Rückkopplung.
 */
final class PingPong {
    private final float[] left, right;
    private final double feedback, lowCoef, highCoef;
    private int position;
    private double lowL, lowR, highL, highR;

    /**
     * @param seconds Verzögerung
     * @param feedback Rückkopplung 0 bis 0.8
     */
    PingPong(double seconds, double feedback) {
        int length = Math.max(64, (int) Math.round(seconds * Dsp.RATE));
        left = new float[length];
        right = new float[length];
        this.feedback = feedback;
        lowCoef = 1 - Math.exp(-2 * Math.PI * 3800 / Dsp.RATE);
        highCoef = 1 - Math.exp(-2 * Math.PI * 180 / Dsp.RATE);
    }

    void process(double[] inL, double[] inR, double[] outL, double[] outR, int n) {
        for (int s = 0; s < n; s++) {
            double dl = left[position], dr = right[position];
            outL[s] += dl;
            outR[s] += dr;
            lowL += lowCoef * (dr - lowL) + 1e-20;
            lowR += lowCoef * (dl - lowR) + 1e-20;
            highL += highCoef * (lowL - highL);
            highR += highCoef * (lowR - highR);
            left[position] = (float) ((inL[s] + inR[s]) * .5 + (lowL - highL) * feedback);
            right[position] = (float) ((lowR - highR) * feedback);
            position = (position + 1) % left.length;
        }
    }
}
