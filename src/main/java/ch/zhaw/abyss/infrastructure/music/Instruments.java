package ch.zhaw.abyss.infrastructure.music;

import static ch.zhaw.abyss.infrastructure.music.Patch.Wave.NOISE;
import static ch.zhaw.abyss.infrastructure.music.Patch.Wave.SAW;
import static ch.zhaw.abyss.infrastructure.music.Patch.Wave.SINE;
import static ch.zhaw.abyss.infrastructure.music.Patch.Wave.SQUARE;
import static ch.zhaw.abyss.infrastructure.music.Patch.Wave.TRIANGLE;

/** Die Klangpalette des Soundtracks: Bässe, Flächen, Leads, Glocken und Effektklänge. */
final class Instruments {
    private Instruments() {}

    /** Tiefer Sinusbass als Fundament. */
    static Patch sub() {
        return Patch.of(SINE).osc2(TRIANGLE, .25, 0).lowpass(500, .7).env(.004, .3, 1, .12).mono(0);
    }

    /** Verstimmter Sägezahnbass, langsam atmendes Filter. */
    static Patch reese() {
        return Patch.of(SAW)
                .unison(2, .32)
                .width(.25)
                .osc2(SAW, .7, -.11)
                .sub(.3)
                .lowpass(420, .9)
                .lfo(.18, .55, 0)
                .drive(.35)
                .env(.01, .4, 1, .25)
                .mono(.03);
    }

    /** Pulsierender Bass mit kurzer Filterhüllkurve. */
    static Patch pulseBass() {
        return Patch.of(SAW)
                .osc2(SQUARE, .45, -12)
                .sub(.3)
                .lowpass(240, 1.3)
                .filterEnv(3.1, .001, .13, 0, .1)
                .drive(.25)
                .env(.002, .2, .3, .06)
                .mono(0);
    }

    /** Resonanter Säurebass, Akzente öffnen das Filter. */
    static Patch acid() {
        return Patch.of(SAW)
                .lowpass(230, 5.5)
                .filterEnv(3.5, .001, .19, 0, .1)
                .velocityCutoff(1.7)
                .drive(.5)
                .env(.002, .25, .5, .05)
                .mono(0);
    }

    /** Weicher Bass für ruhige Räume. */
    static Patch softBass() {
        return Patch.of(TRIANGLE).osc2(SINE, .6, -12).lowpass(900, .7).env(.01, .5, .7, .2).mono(0);
    }

    /** Gezupfter Synthesizer für Arpeggien. */
    static Patch pluck() {
        return Patch.of(SAW)
                .osc2(SQUARE, .45, .08)
                .pulse(.3, 0)
                .unison(2, .1)
                .width(.5)
                .lowpass(520, 1.1)
                .filterEnv(3.8, .001, .17, 0, .15)
                .keyTrack(.4)
                .env(.001, .4, 0, .25)
                .poly(8);
    }

    /** Heller, kurzer Sägezahn für hohe Läufe. */
    static Patch arpSaw() {
        return Patch.of(SAW)
                .osc2(SAW, .5, 12)
                .lowpass(1800, 1.2)
                .filterEnv(2.5, .001, .1, 0, .1)
                .env(.001, .18, 0, .1)
                .poly(6);
    }

    /** FM-Glocke wie ein Sonar aus Glas. */
    static Patch bell() {
        return Patch.of(SINE)
                .fm(3.5, 3, .35, .08)
                .osc2(SINE, .2, 12)
                .env(.002, 1.8, 0, 1.2)
                .poly(10);
    }

    /** Gläserner FM-Anschlag. */
    static Patch glass() {
        return Patch.of(SINE).fm(7, 2.2, .07, 0).osc2(SINE, .22, 19).env(.001, 1, 0, .8).poly(8);
    }

    /** Elektrisches Klavier aus zwei FM-Operatoren. */
    static Patch epiano() {
        return Patch.of(SINE)
                .fm(1, 1.8, .5, .12)
                .osc2(SINE, .1, 24)
                .env(.003, 2.2, .1, .5)
                .poly(10);
    }

    /** Breite Sägezahnfläche. */
    static Patch pad() {
        return Patch.of(SAW)
                .unison(7, .32)
                .width(.9)
                .lowpass(2600, .7)
                .lfo(.11, .45, 0)
                .env(.9, 1.5, .85, 1.8)
                .poly(12);
    }

    /** Dunkle Fläche mit Rechteck eine Oktave tiefer. */
    static Patch darkPad() {
        return Patch.of(SAW)
                .unison(5, .22)
                .width(.8)
                .osc2(SQUARE, .35, -12)
                .lowpass(1050, .9)
                .lfo(.07, .5, 0)
                .env(1.2, 1.5, .9, 2.2)
                .poly(10);
    }

    /** Leuchtende, offene Fläche. */
    static Patch brightPad() {
        return Patch.of(SAW)
                .unison(7, .38)
                .width(1)
                .lowpass(4200, .7)
                .lfo(.13, .3, 0)
                .env(.35, 1, .85, 1.4)
                .poly(12);
    }

    /** Gläserne Fläche aus Dreiecken. */
    static Patch glassPad() {
        return Patch.of(TRIANGLE)
                .unison(5, .18)
                .width(.9)
                .osc2(SINE, .3, 12)
                .lowpass(3200, .7)
                .env(1.6, 2, .9, 2.6)
                .poly(10);
    }

    /** Synthetischer Chor über zwei Formantfilter (Vokal „a“). */
    static Patch choir() {
        return Patch.of(SAW)
                .unison(5, .2)
                .width(.8)
                .formants(730, 1090)
                .vibrato(.12, 5, .2)
                .env(.6, 1, .9, 1.6)
                .poly(10);
    }

    /** Singender Lead mit Pulsbreitenmodulation und Glide. */
    static Patch lead() {
        return Patch.of(SQUARE)
                .pulse(.38, .25)
                .osc2(SAW, .6, .08)
                .lowpass(2900, 1)
                .filterEnv(1, .005, .35, .4, .3)
                .lfo(.4, 0, 0)
                .vibrato(.18, 5.5, .35)
                .drive(.2)
                .env(.008, .3, .85, .3)
                .mono(.045);
    }

    /** Harter Sägezahn-Lead mit Anschlag auf jedem Ton. */
    static Patch sawLead() {
        return Patch.of(SAW)
                .unison(3, .14)
                .width(.5)
                .lowpass(4200, .9)
                .filterEnv(.8, .005, .3, .5, .3)
                .vibrato(.2, 5.8, .3)
                .drive(.45)
                .env(.005, .3, .85, .25)
                .mono(0);
    }

    /** Hymnischer Supersaw-Lead. */
    static Patch trance() {
        return Patch.of(SAW)
                .unison(5, .25)
                .width(.7)
                .lowpass(5000, .8)
                .vibrato(.15, 5.5, .3)
                .env(.005, .3, .9, .3)
                .mono(.03);
    }

    /** Gläserner FM-Lead. */
    static Patch glassLead() {
        return Patch.of(SINE)
                .fm(2, 1.2, .4, .3)
                .osc2(TRIANGLE, .3, 12)
                .vibrato(.15, 5, .3)
                .env(.01, .5, .7, .4)
                .mono(.04);
    }

    /** Bläsersatz mit anschwellendem Filter. */
    static Patch brass() {
        return Patch.of(SAW)
                .unison(3, .12)
                .width(.6)
                .lowpass(700, 1)
                .filterEnv(2.2, .07, .45, .45, .3)
                .vibrato(.1, 5, .5)
                .env(.03, .35, .8, .3)
                .poly(8);
    }

    /** Kurze Streicher für Ostinati. */
    static Patch strings() {
        return Patch.of(SAW)
                .unison(4, .16)
                .width(.8)
                .lowpass(2400, .7)
                .env(.008, .2, .25, .14)
                .poly(10);
    }

    /** Akkordschlag für treibende Rhythmen. */
    static Patch stab() {
        return Patch.of(SAW)
                .unison(5, .28)
                .width(.9)
                .lowpass(1300, 1)
                .filterEnv(2.4, .001, .22, .15, .2)
                .env(.002, .3, .25, .2)
                .poly(12);
    }

    /** Sonarimpuls. */
    static Patch sonar() {
        return Patch.of(SINE).osc2(SINE, .2, 12.02).env(.002, 2.2, 0, 1.5).poly(3);
    }

    /** Turbinenrauschen mit wanderndem Bandpass. */
    static Patch turbine() {
        return Patch.of(NOISE)
                .filter(Patch.Mode.BAND, 700, 3)
                .lfo(.15, 1.3, 0)
                .env(2.5, 1, 1, 3)
                .poly(2);
    }
}
