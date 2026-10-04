package ch.zhaw.abyss.infrastructure.music;

/**
 * Eine Stimme des subtraktiven Synthesizers mit optionaler FM: bis zu sieben verstimmte
 * Oszillatoren, zweiter Oszillator, Suboszillator, Rauschen, Filter mit Hüllkurve und LFO,
 * Formantfilter für Chorklänge, Glide und Vibrato. Arbeitet ohne Speicherzuteilung.
 */
final class SynthVoice {
    private final Patch p;
    private final boolean stereo, formant;
    private final double[] phase = new double[Patch.MAX_UNISON];
    private final double[] ratio = new double[Patch.MAX_UNISON];
    private final double[] gainL = new double[Patch.MAX_UNISON];
    private final double[] gainR = new double[Patch.MAX_UNISON];
    private final double ratio2, driveGain, driveMakeup;
    private final Envelope amp = new Envelope(), filterEnv = new Envelope();
    private final Svf left = new Svf(), right = new Svf(), left2 = new Svf(), right2 = new Svf();
    private double phase2, phaseSub, phaseFm, lfoPhase, vibratoPhase;
    private double pitch, target, velocity, age;
    private long seed;

    /** Absoluter Schlag, an dem der Sequenzer die Stimme loslässt. */
    double endBeat;

    /** Anschlagreihenfolge für das Stehlen der ältesten Stimme. */
    long order;

    boolean held;

    SynthVoice(Patch patch, long seed) {
        p = patch;
        this.seed = seed | 1;
        stereo = patch.unison > 1;
        formant = patch.formant1 > 0;
        amp.set(patch.attack, patch.decay, patch.sustain, patch.release);
        filterEnv.set(
                patch.filterAttack, patch.filterDecay, patch.filterSustain, patch.filterRelease);
        int n = patch.unison;
        double norm = Math.sqrt(2) / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            double position = n == 1 ? 0 : i / (n - 1.0) - .5;
            ratio[i] = Math.pow(2, position * patch.spread / 12);
            double pan =
                    n == 1 ? 0 : (i % 2 == 0 ? -1 : 1) * patch.width * (.35 + Math.abs(position));
            pan = Math.max(-1, Math.min(1, pan));
            gainL[i] = Math.cos((pan + 1) * Math.PI / 4) * norm;
            gainR[i] = Math.sin((pan + 1) * Math.PI / 4) * norm;
            this.seed = Dsp.next(this.seed);
            phase[i] = (Dsp.noise(this.seed) + 1) / 2;
        }
        ratio2 = Math.pow(2, patch.detune2 / 12);
        driveGain = 1 + patch.drive * 5;
        driveMakeup = patch.drive > 0 ? 1 / Dsp.soft(driveGain * .5) * .5 : 1;
    }

    boolean active() {
        return !amp.idle();
    }

    /**
     * @param midi Tonhöhe
     * @param vel Anschlagstärke 0 bis 1
     * @param legato bei monophonen Klängen: gleitet ohne neuen Anschlag
     */
    void start(double midi, double vel, boolean legato) {
        target = midi;
        velocity = vel;
        held = true;
        if (legato && active()) {
            if (p.glide <= 0) pitch = midi;
            return;
        }
        if (!active()) {
            left.reset();
            right.reset();
            left2.reset();
            right2.reset();
        }
        pitch = midi;
        age = 0;
        phaseFm = 0;
        vibratoPhase = 0;
        amp.gate(true);
        filterEnv.gate(true);
    }

    void release() {
        held = false;
        amp.gate(false);
        filterEnv.gate(false);
    }

    void kill() {
        held = false;
        amp.reset();
        filterEnv.reset();
    }

    /** Rendert höchstens {@link Dsp#CONTROL} Abtastwerte additiv in die Puffer. */
    void render(double[] outL, double[] outR, int offset, int n) {
        double seconds = n * Dsp.DT;
        if (p.glide > 0) pitch += (target - pitch) * Dsp.smoothing(p.glide, n);
        else pitch = target;
        lfoPhase += p.lfoRate * seconds;
        double lfo = p.lfoRate > 0 ? Dsp.sin(lfoPhase) : 0;
        double midi = pitch + lfo * p.lfoToPitch;
        if (p.vibrato > 0 && age > p.vibratoDelay) {
            vibratoPhase += p.vibratoRate * seconds;
            midi += p.vibrato * Math.min(1, (age - p.vibratoDelay) / .4) * Dsp.sin(vibratoPhase);
        }
        if (p.pitchDrop != 0) midi += p.pitchDrop * Math.exp(-age / p.pitchDropTime);
        double inc = Dsp.freq(midi) * Dsp.DT;
        double octaves =
                p.envAmount * filterEnv.value
                        + p.lfoToCutoff * lfo
                        + p.keyTrack * (midi - 60) / 12
                        + p.velocityToCutoff * (velocity - .8);
        double cutoff = p.cutoff * Math.pow(2, octaves);
        if (formant) {
            left.set(p.formant1, 5);
            left2.set(p.formant2, 6);
            if (stereo) {
                right.set(p.formant1, 5);
                right2.set(p.formant2, 6);
            }
        } else {
            left.set(cutoff, p.resonance);
            if (stereo) right.set(cutoff, p.resonance);
        }
        double fm =
                p.fmIndex > 0
                        ? p.fmIndex
                                * (p.fmSustain + (1 - p.fmSustain) * Math.exp(-age / p.fmDecay))
                                / (2 * Math.PI)
                        : 0;
        double pw = Math.max(.05, Math.min(.95, p.pulseWidth + p.pwm * lfo * .4));
        age += seconds;
        double level = velocity * p.gain;
        int voices = p.unison;
        for (int s = 0; s < n; s++) {
            double env = amp.next();
            filterEnv.next();
            double modulation = 0;
            if (fm > 0) {
                phaseFm += inc * p.fmRatio;
                if (phaseFm >= 1) phaseFm -= Math.floor(phaseFm);
                modulation = fm * Dsp.sin(phaseFm);
            }
            double l = 0, r = 0;
            for (int i = 0; i < voices; i++) {
                double step = inc * ratio[i];
                double ph = phase[i] + step;
                if (ph >= 1) ph -= 1;
                phase[i] = ph;
                double v = wave(p.wave1, ph, step, pw, modulation);
                l += v * gainL[i];
                r += v * gainR[i];
            }
            double center = 0;
            if (p.wave2 != Patch.Wave.NONE) {
                double step = inc * ratio2;
                phase2 += step;
                if (phase2 >= 1) phase2 -= Math.floor(phase2);
                center += p.mix2 * wave(p.wave2, phase2, step, pw, 0);
            }
            if (p.sub > 0) {
                double step = inc * .5;
                phaseSub += step;
                if (phaseSub >= 1) phaseSub -= 1;
                center += p.sub * wave(Patch.Wave.SQUARE, phaseSub, step, .5, 0);
            }
            if (p.noise > 0) {
                seed = Dsp.next(seed);
                center += p.noise * Dsp.noise(seed);
            }
            double yl, yr;
            if (stereo) {
                yl = filter(left, left2, l + center);
                yr = filter(right, right2, r + center);
            } else {
                yl = yr = filter(left, left2, l + center);
            }
            if (p.drive > 0) {
                yl = Dsp.soft(yl * driveGain) * driveMakeup;
                yr = stereo ? Dsp.soft(yr * driveGain) * driveMakeup : yl;
            }
            double g = env * level;
            outL[offset + s] += yl * g;
            outR[offset + s] += yr * g;
        }
    }

    private double filter(Svf first, Svf second, double x) {
        if (formant) {
            first.process(x);
            second.process(x);
            return first.band * (1.6 / 5) + second.band * (1.1 / 6);
        }
        double low = first.process(x);
        return switch (p.mode) {
            case LOW -> low;
            case BAND -> first.band / Math.max(1, p.resonance);
            case HIGH -> first.high;
        };
    }

    private double wave(Patch.Wave type, double ph, double dt, double pw, double modulation) {
        switch (type) {
            case SAW:
                return 2 * ph - 1 - Dsp.blep(ph, dt);
            case SQUARE:
                double t2 = ph - pw;
                if (t2 < 0) t2 += 1;
                return (ph < pw ? 1 : -1) + Dsp.blep(ph, dt) - Dsp.blep(t2, dt);
            case TRIANGLE:
                return 4 * Math.abs(ph - .5) - 1;
            case SINE:
                return Dsp.sin(ph + modulation);
            case NOISE:
                seed = Dsp.next(seed);
                return Dsp.noise(seed);
            default:
                return 0;
        }
    }
}
