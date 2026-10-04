package ch.zhaw.abyss.infrastructure.music;

/**
 * Synthetisches Schlagzeug ohne Samples: Bassdrum, 808-Bass, Snare, Clap, metallische Hi-Hats nach
 * Art der TR-808, Becken, Toms, Rimshot, Shaker, Industrie-Schlag, Rauschanstieg und Einschlag.
 */
final class DrumVoice {
    enum Kind {
        KICK,
        BOOM,
        SNARE,
        CLAP,
        HAT,
        OPEN_HAT,
        RIDE,
        CRASH,
        TOM,
        RIM,
        SHAKER,
        CLANK,
        RISER,
        IMPACT
    }

    private static final double[] METAL = {205.3, 304.4, 369.6, 522.7, 540.0, 800.0};

    private final Kind kind;
    private final double tune;
    private final Svf band = new Svf(), high = new Svf(), tone = new Svf();
    private final double[] metal = new double[METAL.length];
    private double t, velocity, length, phase, phase2, phase3, phase4;
    private boolean active;
    private long seed;

    DrumVoice(Kind kind, double tune, long seed) {
        this.kind = kind;
        this.tune = tune;
        this.seed = seed | 1;
    }

    boolean active() {
        return active;
    }

    void stop() {
        active = false;
    }

    /**
     * @param vel Anschlagstärke
     * @param seconds Notenlänge (nur für den Rauschanstieg von Bedeutung)
     */
    void trigger(double vel, double seconds) {
        t = 0;
        velocity = vel;
        length = Math.max(.05, seconds);
        phase = phase2 = phase3 = phase4 = 0;
        band.reset();
        high.reset();
        tone.reset();
        active = true;
        switch (kind) {
            case SNARE -> {
                band.set(3200 * tune, .8);
                high.set(1400, .7);
            }
            case CLAP -> band.set(1150 * tune, 1.4);
            case HAT, OPEN_HAT -> {
                band.set(9500 * tune, 1.2);
                high.set(7000 * tune, .7);
            }
            case RIDE -> {
                band.set(7200 * tune, 2.5);
                high.set(5200, .7);
            }
            case CRASH -> {
                band.set(6000 * tune, .6);
                high.set(3500, .7);
            }
            case RIM -> band.set(2600 * tune, 3);
            case SHAKER -> {
                band.set(7800 * tune, 1);
                high.set(6000, .7);
            }
            case CLANK -> band.set(1900 * tune, 2.2);
            case IMPACT -> tone.set(380, .7);
            case TOM -> tone.set(2400, .7);
            default -> {}
        }
    }

    /** Rendert additiv in einen Monopuffer. */
    void render(double[] out, int offset, int n) {
        for (int s = 0; s < n && active; s++) {
            out[offset + s] += sample() * velocity;
            t += Dsp.DT;
        }
    }

    private double noise() {
        seed = Dsp.next(seed);
        return Dsp.noise(seed);
    }

    private double metal() {
        double sum = 0;
        for (int i = 0; i < METAL.length; i++) {
            metal[i] += METAL[i] * tune * 1.55 * Dsp.DT;
            if (metal[i] >= 1) metal[i] -= 1;
            sum += metal[i] < .5 ? 1 : -1;
        }
        return sum / METAL.length;
    }

    private double done(double limit) {
        if (t > limit) active = false;
        return 0;
    }

    private double sample() {
        switch (kind) {
            case KICK:
                {
                    done(.55);
                    double f = 46 * tune + 150 * tune * Math.exp(-t / .032);
                    phase += f * Dsp.DT;
                    double body = Dsp.sin(phase) * Math.exp(-t / .2);
                    double click = noise() * Math.exp(-t / .0025) * .3;
                    return Dsp.soft((body + click) * 1.25);
                }
            case BOOM:
                {
                    done(2.2);
                    double f = 41 * tune + 70 * tune * Math.exp(-t / .05);
                    phase += f * Dsp.DT;
                    return Dsp.soft(Dsp.sin(phase) * Math.exp(-t / .75) * 2.4) * .9;
                }
            case SNARE:
                {
                    done(.45);
                    phase += 182 * tune * Dsp.DT;
                    phase2 += 331 * tune * Dsp.DT;
                    double body = (Dsp.sin(phase) + .55 * Dsp.sin(phase2)) * Math.exp(-t / .045);
                    double n = noise();
                    band.process(n);
                    high.process(n);
                    double rattle = (band.band * 1.4 + high.high * .6) * Math.exp(-t / .14);
                    return body * .55 + rattle * .9;
                }
            case CLAP:
                {
                    done(.55);
                    double env = 0;
                    for (int k = 0; k < 3; k++) {
                        double dt = t - k * .011;
                        if (dt >= 0) env += Math.exp(-dt / .0035);
                    }
                    if (t >= .03) env += .55 * Math.exp(-(t - .03) / .13);
                    band.process(noise());
                    return band.band * env * 1.8;
                }
            case HAT:
            case OPEN_HAT:
                {
                    double decay = kind == Kind.HAT ? .032 : .24;
                    done(decay * 6);
                    double x = metal() * .7 + noise() * .45;
                    band.process(x);
                    high.process(band.band);
                    return high.high * Math.exp(-t / decay) * 1.6;
                }
            case RIDE:
                {
                    done(2.2);
                    double x = metal() * .8 + noise() * .2;
                    band.process(x);
                    high.process(band.band);
                    phase += 4100 * tune * Dsp.DT;
                    return high.high * Math.exp(-t / .45) * 1.3
                            + Dsp.sin(phase) * Math.exp(-t / .3) * .06;
                }
            case CRASH:
                {
                    done(3.2);
                    double x = noise() * .8 + metal() * .4;
                    band.process(x);
                    high.process(band.low + band.band);
                    double attack = Math.min(1, t / .004);
                    return high.high * attack * Math.exp(-t / .85) * 1.1;
                }
            case TOM:
                {
                    done(.9);
                    double f = 96 * tune * (1 + .75 * Math.exp(-t / .045));
                    phase += f * Dsp.DT;
                    double skin = tone.process(noise()) * Math.exp(-t / .02) * .3;
                    return Dsp.soft((Dsp.sin(phase) * Math.exp(-t / .26) + skin) * 1.3);
                }
            case RIM:
                {
                    done(.12);
                    phase += 1720 * tune * Dsp.DT;
                    band.process(noise());
                    return (Dsp.sin(phase) * .5 + band.band * 1.5) * Math.exp(-t / .011);
                }
            case SHAKER:
                {
                    done(.22);
                    band.process(noise());
                    high.process(band.band);
                    double env = Math.min(1, t / .012) * Math.exp(-t / .045);
                    return high.high * env * 1.3;
                }
            case CLANK:
                {
                    done(1.4);
                    double f = 268 * tune;
                    phase2 += f * 1.414 * Dsp.DT;
                    double index = 5.5 * Math.exp(-t / .028) + .6;
                    phase += f * Dsp.DT;
                    double fm = Dsp.sin(phase + index / (2 * Math.PI) * Dsp.sin(phase2));
                    phase3 += f * 2.76 * Dsp.DT;
                    phase4 += f * 5.4 * Dsp.DT;
                    double partials =
                            Dsp.sin(phase3) * Math.exp(-t / .22) * .5
                                    + Dsp.sin(phase4) * Math.exp(-t / .12) * .3;
                    band.process(noise());
                    double hit = band.band * Math.exp(-t / .01) * .8;
                    return (fm * Math.exp(-t / .35) * .7 + partials + hit) * .9;
                }
            case RISER:
                {
                    done(length);
                    double progress = Math.min(1, t / length);
                    band.set(250 * Math.pow(36, progress), 2.2);
                    band.process(noise());
                    return band.band * progress * progress * 1.5;
                }
            case IMPACT:
                {
                    done(3.0);
                    phase += (36 + 30 * Math.exp(-t / .08)) * Dsp.DT;
                    double boom = Dsp.sin(phase) * Math.exp(-t / 1.1);
                    double rumble = tone.process(noise()) * Math.exp(-t / .35) * 1.4;
                    return Dsp.soft((boom + rumble) * 1.8) * .9;
                }
            default:
                active = false;
                return 0;
        }
    }
}
