package ch.zhaw.abyss.infrastructure.music;

/**
 * Klangeinstellung eines Synthesizer-Instruments: Oszillatoren, Filter, Hüllkurven, Modulation. Die
 * Setter geben die Einstellung zurück, damit sich Klänge in den Partituren knapp beschreiben
 * lassen.
 */
final class Patch {
    enum Wave {
        NONE,
        SAW,
        SQUARE,
        TRIANGLE,
        SINE,
        NOISE
    }

    enum Mode {
        LOW,
        BAND,
        HIGH
    }

    static final int MAX_UNISON = 7;

    Wave wave1 = Wave.SAW, wave2 = Wave.NONE;
    double mix2, detune2, sub, noise, pulseWidth = .5;
    int unison = 1;
    double spread, width = .6;
    double fmRatio, fmIndex, fmDecay = .3, fmSustain;
    Mode mode = Mode.LOW;
    double cutoff = 16000, resonance = .707, envAmount, keyTrack, velocityToCutoff;
    double filterAttack = .001, filterDecay = .3, filterSustain, filterRelease = .3;
    double lfoRate, lfoToCutoff, lfoToPitch, pwm;
    double formant1, formant2;
    double attack = .005, decay = .2, sustain = .8, release = .2;
    double glide, vibrato, vibratoRate = 5.2, vibratoDelay = .35;
    double drive, pitchDrop, pitchDropTime = .04, gain = 1;
    boolean mono;
    int polyphony = 8;

    static Patch of(Wave wave) {
        var patch = new Patch();
        patch.wave1 = wave;
        return patch;
    }

    Patch osc2(Wave wave, double mix, double detuneSemitones) {
        wave2 = wave;
        mix2 = mix;
        detune2 = detuneSemitones;
        return this;
    }

    Patch unison(int voices, double spreadSemitones) {
        unison = Math.max(1, Math.min(MAX_UNISON, voices));
        spread = spreadSemitones;
        return this;
    }

    Patch width(double value) {
        width = value;
        return this;
    }

    Patch sub(double level) {
        sub = level;
        return this;
    }

    Patch noise(double level) {
        noise = level;
        return this;
    }

    Patch pulse(double pw, double pwmDepth) {
        pulseWidth = pw;
        pwm = pwmDepth;
        return this;
    }

    Patch fm(double ratio, double index, double decaySeconds, double sustainLevel) {
        fmRatio = ratio;
        fmIndex = index;
        fmDecay = decaySeconds;
        fmSustain = sustainLevel;
        return this;
    }

    Patch filter(Mode filterMode, double hz, double q) {
        mode = filterMode;
        cutoff = hz;
        resonance = q;
        return this;
    }

    Patch lowpass(double hz, double q) {
        return filter(Mode.LOW, hz, q);
    }

    /** Filterhüllkurve; {@code octaves} ist der Hub über der Grenzfrequenz. */
    Patch filterEnv(double octaves, double a, double d, double s, double r) {
        envAmount = octaves;
        filterAttack = a;
        filterDecay = d;
        filterSustain = s;
        filterRelease = r;
        return this;
    }

    Patch keyTrack(double amount) {
        keyTrack = amount;
        return this;
    }

    Patch velocityCutoff(double octaves) {
        velocityToCutoff = octaves;
        return this;
    }

    Patch lfo(double hz, double cutoffOctaves, double pitchSemitones) {
        lfoRate = hz;
        lfoToCutoff = cutoffOctaves;
        lfoToPitch = pitchSemitones;
        return this;
    }

    Patch formants(double first, double second) {
        formant1 = first;
        formant2 = second;
        return this;
    }

    Patch env(double a, double d, double s, double r) {
        attack = a;
        decay = d;
        sustain = s;
        release = r;
        return this;
    }

    Patch mono(double glideSeconds) {
        mono = true;
        glide = glideSeconds;
        polyphony = 1;
        return this;
    }

    Patch vibrato(double semitones, double hz, double delaySeconds) {
        vibrato = semitones;
        vibratoRate = hz;
        vibratoDelay = delaySeconds;
        return this;
    }

    Patch drive(double amount) {
        drive = amount;
        return this;
    }

    Patch drop(double semitones, double seconds) {
        pitchDrop = semitones;
        pitchDropTime = seconds;
        return this;
    }

    Patch poly(int voices) {
        polyphony = voices;
        return this;
    }

    Patch gain(double value) {
        gain = value;
        return this;
    }
}
