package ch.zhaw.abyss.infrastructure.music;

/** ADSR-Hüllkurve: linearer Anstieg, exponentielles Abklingen und Ausklingen. */
final class Envelope {
    private static final int IDLE = 0, ATTACK = 1, DECAY = 2, SUSTAIN = 3, RELEASE = 4;
    private double attackStep = 1, decayCoef, releaseCoef, sustain = 1;
    private int stage;
    double value;

    /**
     * @param attack Anstieg in Sekunden
     * @param decay Zeit bis nahe am Haltepegel
     * @param sustain Haltepegel 0 bis 1
     * @param release Ausklingzeit nach dem Loslassen
     */
    void set(double attack, double decay, double sustain, double release) {
        attackStep = attack <= 0 ? 1 : Dsp.DT / attack;
        decayCoef = Math.exp(-Dsp.DT / Math.max(1e-4, decay / 4.6));
        releaseCoef = Math.exp(-Dsp.DT / Math.max(1e-4, release / 4.6));
        this.sustain = sustain;
    }

    /** Anschlag (Wert läuft vom aktuellen Pegel an, daher ohne Knacken) oder Loslassen. */
    void gate(boolean on) {
        if (on) stage = ATTACK;
        else if (stage != IDLE) stage = RELEASE;
    }

    double next() {
        switch (stage) {
            case ATTACK -> {
                value += attackStep;
                if (value >= 1) {
                    value = 1;
                    stage = DECAY;
                }
            }
            case DECAY -> {
                value = sustain + (value - sustain) * decayCoef;
                if (sustain <= 0 && value < 1e-4) {
                    value = 0;
                    stage = IDLE;
                } else if (value - sustain < 1e-4) stage = SUSTAIN;
            }
            case SUSTAIN -> value = sustain;
            case RELEASE -> {
                value *= releaseCoef;
                if (value < 1e-4) {
                    value = 0;
                    stage = IDLE;
                }
            }
            default -> {}
        }
        return value;
    }

    boolean idle() {
        return stage == IDLE;
    }

    void reset() {
        stage = IDLE;
        value = 0;
    }
}
