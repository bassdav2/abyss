package ch.zhaw.abyss.infrastructure.music;

import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.CLANK;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.CLAP;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.CRASH;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.HAT;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.IMPACT;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.KICK;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.OPEN_HAT;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.RIDE;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.RIM;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.RISER;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.SHAKER;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.SNARE;
import static ch.zhaw.abyss.infrastructure.music.DrumVoice.Kind.TOM;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Die Partituren des Soundtracks. Alle Stücke teilen ein Leitmotiv (das frühere Titelthema
 * D–F–E–C–D–A–G–E über B♭maj7–C–Dm–A7), jede Sektion spielt es in ihrer Tonart und ihrem Stil. Die
 * Schichten (layer) bestimmen, ab welcher Intensität eine Stimme mitspielt: 0 Grundierung, 0.3 Bass
 * und Becken, 0.5 Schlagzeug, 0.75 Melodie, 0.9 volle Wucht.
 */
final class Scores {
    /** Leitmotiv in d-Moll, acht Takte. */
    static final String THEME =
            "d5:12 f5:4 | e5:8 c5:8 | d5:12 a5:4 | g5:8 e5:8 | f5:4 g5:4 a5:8 | g5:4 e5:4 c5:8"
                    + " | bb4:4 d5:4 g5:4 f5:4 | e5:8 c#5:4 a4:4";

    private static final String THEME_CHORDS = "Bbmaj7 C Dm A7 Bbmaj7 C Gm A7";
    private static final String FOUR = "X . . . X . . . X . . . X . . .";
    private static final String BACKBEAT = ". . . . X . . . . . . . X . . .";
    private static final String OFFBEAT = ". . x . . . x . . . x . . . x .";
    private static final String EIGHTHS = "x o x o x o x o x o x o x o x o";
    private static final String ONE = "X . . . . . . . . . . . . . . .";
    private static final String ROLL = ". . . . x . x . x x x x X X X X";
    private static final String FILL = ". . . . . . . . . . x . x x X X";
    private static final String BREAK =
            "X . . . . . . . . . X . . . . . | X . . . . . . . . . X . . X . .";

    private Scores() {}

    /** Alle Stücke in Spielreihenfolge. */
    static Map<String, Song> all() {
        var songs = new LinkedHashMap<String, Song>();
        for (var song :
                new Song[] {
                    title(),
                    hold(),
                    engine(),
                    research(),
                    command(),
                    boss(),
                    empress(),
                    abyss(),
                    haven(),
                    ending()
                }) songs.put(song.id, song);
        return songs;
    }

    /** Titel: weite Flächen, Sonar, das Leitmotiv zum ersten Mal. */
    static Song title() {
        var s = new Song("title", "Abyss", 84, 32);
        s.gain = .76;
        s.reverbSize = 1.6;
        s.reverbSeconds = 5;
        s.delayFeedback = .45;
        s.harmony(THEME_CHORDS + " " + THEME_CHORDS + " Gm Bb Dm C Gm Bb A A " + THEME_CHORDS);
        s.synth("pad", Instruments.pad()).add(s.pad(62)).gain(.134).sends(0, .5);
        s.synth("sub", Instruments.sub()).add(s.roots(26)).bars("5-32").gain(.131);
        s.synth("sonar", Instruments.sonar())
                .add(s.loop(2, s.melody(1, "d6:4 .:12 | .:16")))
                .gain(.16)
                .sends(.5, .6);
        s.synth("bells", Instruments.bell())
                .add(s.arp(62, "0 . 1 . 2 . 3 . 4 . 3 . 2 . 1 ."))
                .bars("1-8,17-24")
                .gain(.128)
                .sends(.35, .35);
        s.synth("arp", Instruments.pluck())
                .add(s.arp(57, "0 1 2 3 4 3 2 1 0 1 2 3 4 5 4 3"))
                .bars("25-32")
                .gain(.14)
                .sends(.3, .25)
                .duck(.4);
        s.synth("lead", Instruments.lead())
                .add(s.melody(9, THEME))
                .add(
                        s.melody(
                                17,
                                "bb4:4 a4:4 g4:8 | f4:8 d5:8 | a4:16 | g4:4 e4:4 g4:4 c5:4"
                                        + " | bb4:4 a4:4 g4:4 d5:4 | f5:8 d5:4 bb4:4 | c#5:8 e5:8"
                                        + " | a4:12 .:4"))
                .add(s.melody(25, THEME))
                .gain(.312)
                .sends(.35, .35);
        s.synth("echo", Instruments.bell())
                .add(s.melody(25, THEME))
                .transpose(12)
                .gain(.06)
                .sends(.4, .4);
        s.synth("choir", Instruments.choir()).add(s.pad(60)).bars("17-24").gain(.2).sends(0, .5);
        s.synth("bass", Instruments.pulseBass())
                .add(s.bass(38, "x . x . x . x . x . x . x . x ."))
                .bars("17-32")
                .gain(.493)
                .duck(.5);
        s.drum("kick", KICK)
                .add(s.hits("9-32", "X . . . . . . . . . x . . . . ."))
                .gain(.385)
                .sidechain();
        s.drum("snare", SNARE)
                .add(s.hits("17-32", ". . . . . . . . X . . . . . . ."))
                .gain(.383)
                .sends(0, .3);
        s.drum("hat", HAT).add(s.hits("17-32", "x . o . x . o . x . o . x . o .")).gain(.896);
        s.drum("crash", CRASH).add(s.hits("17,25", ONE)).gain(.28).sends(0, .3);
        s.drum("riser", RISER).add(s.swells("16")).gain(.22).sends(.2, .3);
        s.drum("tom", TOM).add(s.hits("32", FILL)).gain(.228).sends(0, .25);
        return s;
    }

    /** Hecksektion: Rost, Metallschläge, pulsierender Bass. */
    static Song hold() {
        var s = new Song("hold", "Rost im Heck", 100, 32);
        s.gain = .72;
        s.reverbSize = 1.3;
        s.reverbSeconds = 3.5;
        s.reverbBrightness = .35;
        s.delayFeedback = .4;
        s.harmony(
                "Dm Dm Bb C Dm Dm Bb A Gm Gm Dm Dm Bb C Dm A "
                        + THEME_CHORDS
                        + " Dm Dm Bb C Dm Dm Bb A");
        String riffA =
                "a4:2 d5:2 e5:2 f5:4 e5:2 d5:2 c5:2 | d5:8 .:2 a4:2 c5:2 d5:2"
                        + " | f5:4 e5:2 d5:4 c5:2 d5:2 e5:2 | c5:8 g4:4 c5:2 e5:2"
                        + " | a4:2 d5:2 e5:2 f5:4 e5:2 d5:2 c5:2 | d5:8 .:2 f5:2 e5:2 d5:2"
                        + " | d5:4 c5:2 bb4:4 a4:2 g4:2 f4:2 | e4:8 c#5:4 e5:4";
        String riffB =
                "g4:2 bb4:2 d5:2 g5:4 f5:2 d5:2 bb4:2 | d5:8 .:4 bb4:2 d5:2"
                        + " | a4:2 d5:2 f5:2 a5:4 g5:2 f5:2 d5:2 | f5:8 .:4 e5:2 d5:2"
                        + " | d5:4 f5:4 bb5:4 a5:2 g5:2 | g5:8 e5:4 c5:4 | f5:4 e5:4 d5:4 a4:4"
                        + " | c#5:8 e5:4 a5:4";
        s.synth("pad", Instruments.darkPad()).add(s.pad(57)).gain(.135).sends(0, .4);
        s.synth("sub", Instruments.sub()).add(s.roots(26)).gain(.125);
        s.drum("clank", CLANK)
                .add(s.hits(ONE + " | . . . . . . . . . . o . . . . ."))
                .gain(.3)
                .sends(.3, .5);
        s.synth("sonar", Instruments.sonar())
                .add(s.loop(4, s.melody(1, "a5:4 .:12 | .:16 | .:16 | .:16")))
                .gain(.12)
                .sends(.5, .6);
        s.synth("bass", Instruments.pulseBass())
                .add(s.bass(38, "x . x x . x . x x . x . x x . x"))
                .layer(.3)
                .gain(.551)
                .duck(.45);
        s.drum("hat", HAT).add(s.hits(OFFBEAT)).layer(.3).gain(1.12);
        s.drum("shaker", SHAKER)
                .add(s.hits("o o x o o o x o o o x o o o x o"))
                .layer(.3)
                .gain(.39)
                .pan(.3);
        s.drum("kick", KICK)
                .add(s.hits("X . . . . . x . X . . . . . . . | X . . . . . x . X . . x . . . ."))
                .layer(.5)
                .gain(.44)
                .sidechain();
        s.drum("snare", SNARE).add(s.hits(BACKBEAT)).layer(.5).gain(.425).sends(0, .25);
        s.drum("clang", CLANK)
                .tune(1.6)
                .add(s.hits(". . . . x . . . . . . . x . . ."))
                .layer(.5)
                .gain(.16)
                .sends(.2, .3);
        s.synth("arp", Instruments.pluck())
                .add(s.arp(62, "0 1 2 1 3 2 1 2 0 1 2 1 3 2 4 2"))
                .layer(.5)
                .gain(.16)
                .sends(.35, .2)
                .duck(.4);
        s.synth("bells", Instruments.bell())
                .add(s.melody(17, THEME))
                .layer(.5)
                .gain(.085)
                .sends(.3, .3);
        s.synth("riff", Instruments.sawLead())
                .add(s.melody(1, riffA))
                .add(s.melody(9, riffB))
                .add(s.melody(25, riffA))
                .layer(.75)
                .gain(.336)
                .sends(.3, .25);
        s.synth("theme", Instruments.lead())
                .add(s.melody(17, THEME))
                .layer(.75)
                .gain(.39)
                .sends(.35, .3);
        s.drum("tom", TOM).add(s.hits("8,16,24,32", FILL)).layer(.75).gain(.21).sends(0, .25);
        s.drum("crash", CRASH).add(s.hits("1,9,17,25", ONE)).layer(.75).gain(.22);
        return s;
    }

    /** Maschinendeck: Turbinen, Säurebass, gerader Takt. */
    static Song engine() {
        var s = new Song("engine", "Turbinenherz", 118, 32);
        s.gain = .77;
        s.reverbSeconds = 2.8;
        s.harmony(
                "Em C G D Em C Am B Cmaj7 D Em B7 Cmaj7 D Am B7"
                        + " Am Am C C Em Em D B Em C G D Em C Am B");
        String riffA =
                "e5:3 b4:3 e5:2 g5:3 f#5:3 e5:2 | e5:8 .:2 c5:2 d5:2 e5:2"
                        + " | d5:3 b4:3 d5:2 g5:3 a5:3 g5:2 | f#5:8 .:2 d5:2 e5:2 f#5:2"
                        + " | e5:3 b4:3 e5:2 g5:3 f#5:3 e5:2 | g5:8 e5:4 c5:4 | c5:4 e5:4 a5:4 g5:4"
                        + " | f#5:8 d#5:4 b4:4";
        String riffC =
                "a4:3 c5:3 e5:2 a5:4 g5:4 | e5:12 .:4 | g4:3 c5:3 e5:2 g5:4 e5:4 | c5:12 .:4"
                        + " | b4:3 e5:3 g5:2 b5:4 a5:4 | g5:12 .:4 | f#5:4 a5:4 d6:4 c6:4"
                        + " | b5:8 d#5:4 f#5:4";
        s.synth("pad", Instruments.pad()).add(s.pad(64)).gain(.109).sends(0, .4).duck(.55);
        s.synth("turbine", Instruments.turbine())
                .add(s.loop(4, s.melody(1, "e3:64")))
                .gain(.36)
                .sends(0, .3);
        s.synth("sub", Instruments.sub()).add(s.roots(28)).gain(.119).duck(.5);
        s.synth("acid", Instruments.acid())
                .add(s.bass(40, "X x o x x . 5 x O x . x 5 o x x"))
                .layer(.3)
                .gain(.345)
                .duck(.4)
                .sends(.15, .05);
        s.drum("hat", HAT).add(s.hits(EIGHTHS)).layer(.3).gain(.672).pan(-.2);
        s.drum("kick", KICK).add(s.hits(FOUR)).layer(.5).gain(.468).sidechain();
        s.drum("clap", CLAP).add(s.hits(BACKBEAT)).layer(.5).gain(.6).sends(0, .3);
        s.drum("open", OPEN_HAT).add(s.hits(OFFBEAT)).layer(.5).gain(.39).pan(.2);
        s.synth("arp", Instruments.pluck())
                .add(s.arp(64, "0 2 1 3 2 4 3 5 4 3 2 1 2 3 1 2"))
                .layer(.5)
                .gain(.14)
                .sends(.3, .2)
                .duck(.5);
        s.synth("riff", Instruments.sawLead())
                .add(s.melody(1, riffA))
                .add(s.melody(17, riffC))
                .add(s.melody(25, riffA))
                .layer(.75)
                .gain(.308)
                .sends(.3, .25);
        s.synth("theme", Instruments.lead())
                .add(s.melody(9, THEME))
                .transpose(2)
                .layer(.75)
                .gain(.39)
                .sends(.35, .3);
        s.drum("crash", CRASH).add(s.hits("1,9,17,25", ONE)).layer(.75).gain(.22);
        s.drum("riser", RISER).add(s.swells("8,16,24,32")).layer(.75).gain(.2).sends(.2, .3);
        return s;
    }

    /** Forschungsdeck: gläserne Glocken, gebrochener Rhythmus, phrygische Farben. */
    static Song research() {
        var s = new Song("research", "Glasgarten", 108, 32);
        s.gain = .83;
        s.reverbSize = 1.5;
        s.reverbSeconds = 4.2;
        s.reverbBrightness = .6;
        s.delayBeats = 1.5;
        s.delayFeedback = .45;
        s.harmony(
                "Fm Fm Dbmaj7 Dbmaj7 Bbm Bbm C C Dbmaj7 Eb Fm C7 Dbmaj7 Eb Bbm C7"
                        + " Gbmaj7 Fm Gbmaj7 Fm Ebm Dbmaj7 C C Fm Fm Dbmaj7 Dbmaj7 Bbm Bbm C C");
        String riffA =
                "c5:3 f5:3 ab5:2 g5:4 f5:4 | c5:12 .:4 | c5:3 f5:3 ab5:2 c6:4 bb5:4 | ab5:12 .:4"
                        + " | bb4:3 db5:3 f5:2 ab5:4 f5:4 | db5:12 .:4 | c5:3 e5:3 g5:2 bb5:4 g5:4"
                        + " | e5:12 .:4";
        String riffC =
                "bb4:8 db5:4 f5:4 | eb5:8 c5:8 | bb4:8 db5:4 gb5:4 | f5:12 .:4 | gb5:4 f5:4 eb5:4"
                        + " db5:4 | f5:8 c5:4 ab4:4 | g4:8 c5:4 e5:4 | g5:12 .:4";
        s.synth("pad", Instruments.glassPad()).add(s.pad(65)).gain(.126).sends(0, .5);
        s.synth("bells", Instruments.glass())
                .add(s.arp(65, "0 . . 1 . . 2 . 3 . . 2 . . 4 ."))
                .gain(.126)
                .sends(.45, .4);
        s.synth("sonar", Instruments.sonar())
                .add(s.loop(4, s.melody(1, "c6:4 .:12 | .:16 | .:16 | .:16")))
                .gain(.1)
                .sends(.5, .6);
        s.synth("sub", Instruments.sub()).add(s.roots(29)).gain(.119);
        s.drum("rim", RIM)
                .add(s.hits(". . . . x . . x . . . . x . . ."))
                .layer(.3)
                .gain(.26)
                .sends(.25, .2)
                .pan(.25);
        s.drum("shaker", SHAKER)
                .add(s.hits("x o o x o o x o x o o x o o x o"))
                .layer(.3)
                .gain(.325)
                .pan(-.3);
        s.drum("kick", KICK).add(s.hits(BREAK)).layer(.5).gain(.429).sidechain();
        s.drum("snare", SNARE)
                .add(s.hits(". . . . X . . o . o . . X . . o"))
                .layer(.5)
                .gain(.383)
                .sends(0, .3);
        s.drum("hat", HAT).add(s.hits("x o x o x o x x x o x o x o x x")).layer(.5).gain(.672);
        s.synth("arp", Instruments.pluck())
                .add(s.arp(60, "0 1 2 0 1 2 0 1 2 3 4 3 2 1 2 3"))
                .layer(.5)
                .gain(.13)
                .sends(.35, .25)
                .duck(.35);
        s.synth("riff", Instruments.glassLead())
                .add(s.melody(1, riffA))
                .add(s.melody(17, riffC))
                .add(s.melody(25, riffA))
                .layer(.75)
                .gain(.21)
                .sends(.4, .35);
        s.synth("theme", Instruments.glassLead())
                .add(s.melody(9, THEME))
                .transpose(3)
                .layer(.75)
                .gain(.21)
                .sends(.4, .35);
        s.synth("choir", Instruments.choir())
                .add(s.pad(60))
                .bars("17-24")
                .layer(.75)
                .gain(.18)
                .sends(0, .5);
        s.drum("crash", CRASH).add(s.hits("9,25", ONE)).layer(.75).gain(.2);
        s.drum("riser", RISER).add(s.swells("8,24")).layer(.75).gain(.2).sends(.2, .3);
        return s;
    }

    /** Kommandodeck: Marschtrommel, Streicher-Ostinato, Bläser, das Motiv heroisch. */
    static Song command() {
        var s = new Song("command", "Kommandobrücke", 126, 32);
        s.gain = .68;
        s.reverbSeconds = 3.4;
        s.reverbSize = 1.3;
        s.delayFeedback = .35;
        s.harmony(
                "Gm Eb Bb F Gm Eb Cm D Ebmaj7 F Gm D7 Ebmaj7 F Cm D7"
                        + " Cm Cm Eb Eb Gm Gm D D Ebmaj7 F Gm D7 Ebmaj7 F Cm D7");
        String riffA =
                "g4:4 bb4:4 d5:6 c5:2 | bb4:8 g4:8 | f4:4 bb4:4 d5:6 f5:2 | c5:8 a4:8"
                        + " | g4:4 bb4:4 d5:4 g5:4 | f5:4 eb5:4 d5:4 bb4:4 | c5:8 eb5:4 g5:4"
                        + " | f#5:12 d5:4";
        String riffC =
                "g5:12 eb5:4 | c5:16 | bb4:8 eb5:4 g5:4 | f5:16 | d5:8 g5:4 bb5:4 | a5:8 g5:4 f5:4"
                        + " | f#5:16 | a5:12 .:4";
        s.synth("strings", Instruments.strings())
                .add(s.arp(55, "0 0 1 0 2 0 1 0", 8))
                .gain(.2)
                .sends(0, .3)
                .duck(.3);
        s.synth("pad", Instruments.darkPad()).add(s.pad(60)).gain(.099).sends(0, .4);
        s.synth("sub", Instruments.sub()).add(s.roots(31)).gain(.119);
        s.synth("bass", Instruments.pulseBass())
                .add(s.bass(31, "x o x o x o x o", 8))
                .layer(.3)
                .gain(.493)
                .duck(.4);
        s.drum("timpani", TOM)
                .tune(.7)
                .add(s.hits("X . . . . . . . x . . . . . . ."))
                .layer(.3)
                .gain(.228)
                .sends(0, .35);
        s.drum("kick", KICK).add(s.hits(FOUR)).layer(.5).gain(.44).sidechain();
        s.drum("snare", SNARE)
                .add(
                        s.hits(
                                ". . . . X . . o . o . . X . o o | . . . . X . . o . o . . X . X o"
                                        + " | . . . . X . . o . o . . X . o o"
                                        + " | . . o . X . o o X o X o X X X X"))
                .layer(.5)
                .gain(.357)
                .sends(0, .25);
        s.synth("brass", Instruments.brass())
                .add(s.stabs(60, "X . . . . . . . . . . . . . x ."))
                .layer(.5)
                .gain(.176)
                .sends(.2, .3);
        s.drum("hat", HAT).add(s.hits("x . x . x . x . x . x . x . x .")).layer(.5).gain(.672);
        s.synth("riff", Instruments.sawLead())
                .add(s.melody(1, riffA))
                .add(s.melody(17, riffC))
                .layer(.75)
                .gain(.336)
                .sends(.3, .25);
        s.synth("theme", Instruments.lead())
                .add(s.melody(9, THEME))
                .add(s.melody(25, THEME))
                .transpose(5)
                .layer(.75)
                .gain(.312)
                .sends(.35, .3);
        s.synth("choir", Instruments.choir())
                .add(s.pad(62))
                .bars("17-32")
                .layer(.75)
                .gain(.16)
                .sends(0, .5);
        s.drum("crash", CRASH).add(s.hits("1,9,17,25", ONE)).layer(.75).gain(.22);
        return s;
    }

    /** Sektorwächter: Reese-Bass, Galopp, phrygische Wendung, volle Wucht in der letzten Phase. */
    static Song boss() {
        var s = new Song("boss", "Wächter", 140, 32);
        s.gain = .68;
        s.reverbSize = 1.1;
        s.reverbSeconds = 2.8;
        s.reverbBrightness = .4;
        s.delayFeedback = .3;
        s.harmony(
                "Cm Cm Db Db Cm Cm Ab G Abmaj7 Bb Cm G7 Abmaj7 Bb Fm G7"
                        + " Fm Fm Db Db Cm Cm G G Cm Cm Db Db Ab Bb G G");
        String opening =
                "c5:2 c5:2 eb5:2 c5:2 g5:4 f5:2 eb5:2 | d5:4 c5:4 .:8"
                        + " | db5:2 db5:2 f5:2 db5:2 ab5:4 g5:2 f5:2 | eb5:4 db5:4 .:8";
        String riffA =
                opening
                        + " | c5:2 c5:2 eb5:2 c5:2 g5:4 f5:2 eb5:2 | eb5:4 g5:4 c6:4 bb5:4"
                        + " | ab5:8 g5:4 eb5:4 | d5:8 b4:4 g4:4";
        String riffC =
                "f5:6 ab5:6 c6:4 | bb5:8 ab5:4 g5:4 | f5:6 ab5:6 db6:4 | c6:8 bb5:4 ab5:4"
                        + " | g5:6 c6:6 eb6:4 | d6:8 c6:4 bb5:4 | b5:16 | d6:8 b5:4 g5:4";
        String riffD =
                opening
                        + " | c6:4 bb5:4 ab5:4 eb5:4 | d6:4 c6:4 bb5:4 f5:4 | g5:8 b5:4 d6:4"
                        + " | g6:8 f6:4 d6:4";
        s.synth("reese", Instruments.reese()).add(s.roots(36)).gain(.22);
        s.synth("sub", Instruments.sub()).add(s.roots(28)).gain(.109);
        s.synth("choir", Instruments.choir()).add(s.pad(60)).gain(.18).sends(0, .45);
        s.drum("toms", TOM)
                .tune(.8)
                .add(s.hits("X . . . . . . . . . x . . . . ."))
                .gain(.21)
                .sends(0, .3);
        s.synth("gallop", Instruments.pulseBass())
                .add(s.bass(36, "x . x x x . x x x . x x x . x x"))
                .layer(.3)
                .gain(.435)
                .duck(.35);
        s.drum("hat", HAT).add(s.hits("X x o x X x o x X x o x X x o x")).layer(.3).gain(.56);
        s.drum("kick", KICK)
                .add(s.hits("X . . X . . X . . . X . . X . . | X . . X . . X . . . X . . . X X"))
                .layer(.5)
                .gain(.328)
                .sidechain();
        s.drum("snare", SNARE).add(s.hits(BACKBEAT)).layer(.5).gain(.425).sends(0, .2);
        s.drum("clap", CLAP).add(s.hits(BACKBEAT)).layer(.5).gain(.375).sends(0, .25);
        s.synth("riff", Instruments.sawLead())
                .add(s.melody(1, riffA))
                .add(s.melody(17, riffC))
                .add(s.melody(25, riffD))
                .layer(.72)
                .gain(.336)
                .sends(.25, .2);
        s.synth("theme", Instruments.lead())
                .add(s.melody(9, THEME))
                .transpose(-2)
                .layer(.72)
                .gain(.39)
                .sends(.3, .25);
        s.synth("arp", Instruments.arpSaw())
                .add(s.arp(72, "0 1 2 3 2 1 0 1 2 3 4 3 2 1 2 3"))
                .layer(.9)
                .gain(.11)
                .duck(.4)
                .sends(.2, .15);
        s.synth("stabs", Instruments.choir())
                .add(s.stabs(64, "X . . . . . . . X . . . . . . ."))
                .layer(.9)
                .gain(.49)
                .sends(0, .4);
        s.drum("crash", CRASH).add(s.hits("1,5,9,13,17,21,25,29", ONE)).layer(.9).gain(.22);
        s.drum("impact", IMPACT).add(s.hits("1,17", ONE)).layer(.9).gain(.4).sends(0, .3);
        return s;
    }

    /** Prismenkaiserin: euphorisch, schnell, hell; das Motiv als Hymne. */
    static Song empress() {
        var s = new Song("empress", "Prismenkaiserin", 156, 32);
        s.gain = .68;
        s.reverbSize = 1.4;
        s.reverbSeconds = 3;
        s.reverbBrightness = .7;
        s.delayFeedback = .42;
        s.harmony(
                "F G Em Am F G Am Am Fmaj7 G Am E7 Fmaj7 G Dm E7"
                        + " Am F C G Am F G E Fmaj7 G Am E7 Fmaj7 G Dm E7");
        String hymnA =
                "a5:2 c6:2 a5:2 g5:2 f5:2 g5:2 a5:2 c6:2 | b5:4 g5:4 d6:4 b5:4"
                        + " | b5:2 g5:2 e5:2 g5:2 b5:4 e6:4 | c6:8 a5:8"
                        + " | a5:2 c6:2 a5:2 g5:2 f5:2 g5:2 a5:2 c6:2 | d6:4 b5:4 g5:4 d6:4"
                        + " | e6:8 c6:4 a5:4 | e5:4 g5:4 a5:4 b5:4";
        String hymnC =
                "e5:4 a5:4 c6:4 b5:2 a5:2 | c6:8 a5:4 f5:4 | g5:4 c6:4 e6:4 d6:2 c6:2"
                        + " | d6:8 b5:4 g5:4 | a5:4 c6:4 e6:4 d6:2 c6:2 | c6:8 a5:4 c6:4"
                        + " | b5:4 d6:4 g6:4 f6:2 d6:2 | e6:8 g#5:4 b5:4";
        s.synth("pad", Instruments.brightPad()).add(s.pad(64)).gain(.11).sends(0, .4).duck(.6);
        s.synth("bells", Instruments.bell())
                .add(s.arp(69, "0 1 2 3 2 1 2 3 0 1 2 3 4 3 2 1"))
                .gain(.102)
                .sends(.3, .3);
        s.synth("sub", Instruments.sub()).add(s.roots(33)).gain(.112).duck(.55);
        s.synth("bass", Instruments.pulseBass())
                .add(s.bass(45, OFFBEAT))
                .layer(.3)
                .gain(.464)
                .duck(.3);
        s.drum("hat", HAT).add(s.hits(EIGHTHS)).layer(.3).gain(.56);
        s.drum("kick", KICK).add(s.hits(FOUR)).layer(.5).gain(.468).sidechain();
        s.drum("clap", CLAP).add(s.hits(BACKBEAT)).layer(.5).gain(.57).sends(0, .25);
        s.drum("open", OPEN_HAT).add(s.hits(OFFBEAT)).layer(.5).gain(.364);
        s.synth("stab", Instruments.stab())
                .add(s.stabs(64, "X . . X . . X . . . X . . X . ."))
                .layer(.5)
                .gain(.16)
                .duck(.45)
                .sends(.2, .25);
        s.synth("hymn", Instruments.trance())
                .add(s.melody(1, hymnA))
                .add(s.melody(17, hymnC))
                .layer(.75)
                .gain(.299)
                .sends(.3, .3);
        s.synth("theme", Instruments.trance())
                .add(s.melody(9, THEME))
                .add(s.melody(25, THEME))
                .transpose(7)
                .layer(.75)
                .gain(.299)
                .sends(.3, .3);
        s.synth("low", Instruments.pluck())
                .add(s.melody(25, THEME))
                .transpose(-5)
                .layer(.75)
                .gain(.21)
                .sends(.3, .2);
        s.synth("high", Instruments.arpSaw())
                .add(s.arp(81, "0 1 2 1 0 1 2 1 0 1 2 1 0 1 2 3"))
                .layer(.9)
                .gain(.09)
                .duck(.4)
                .sends(.2, .2);
        s.synth("choir", Instruments.choir()).add(s.pad(64)).layer(.9).gain(.16).sends(0, .45);
        s.drum("crash", CRASH).add(s.hits("1,5,9,13,17,21,25,29", ONE)).layer(.9).gain(.22);
        s.drum("roll", SNARE).add(s.hits("8,16,24,32", ROLL)).layer(.9).gain(.297);
        s.drum("riser", RISER).add(s.swells("8,16,24,32")).layer(.9).gain(.2).sends(.2, .3);
        return s;
    }

    /** Abgrund (Eskalation): Drum and Bass, Reese, das Motiv verzerrt über tausend Gegnern. */
    static Song abyss() {
        var s = new Song("abyss", "Abgrund", 174, 32);
        s.gain = .64;
        s.reverbSize = 1.5;
        s.reverbSeconds = 3.6;
        s.reverbBrightness = .35;
        s.delayFeedback = .4;
        s.harmony(
                "Dm Dm Bb Bb Gm Gm A A " + THEME_CHORDS + " Dm Dm Bb Bb Gm Gm A A " + THEME_CHORDS);
        s.synth("pad", Instruments.darkPad()).add(s.pad(57)).gain(.117).sends(0, .45);
        s.synth("drone", Instruments.reese()).add(s.roots(38)).gain(.16);
        s.synth("sub", Instruments.sub()).add(s.roots(26)).gain(.094).duck(.4);
        s.synth("sonar", Instruments.sonar())
                .add(s.loop(4, s.melody(1, "d6:4 .:12 | .:16 | a5:4 .:12 | .:16")))
                .gain(.1)
                .sends(.5, .6);
        s.synth("reese", Instruments.reese())
                .add(s.bass(38, "x - - - - - x - - - x - - - - -"))
                .layer(.3)
                .gain(.26)
                .duck(.3);
        s.drum("hat", HAT).add(s.hits("x . x x x . x . x . x x x . x .")).layer(.3).gain(.56);
        s.drum("kick", KICK).add(s.hits(BREAK)).layer(.5).gain(.468).sidechain();
        s.drum("snare", SNARE)
                .add(s.hits(". . . . X . . o . o . . X . . . | . . . . X . . o . o . . X . . o"))
                .layer(.5)
                .gain(.442)
                .sends(0, .2);
        s.drum("ride", RIDE)
                .add(s.hits("x . x . x . x . x . x . x . x ."))
                .layer(.5)
                .gain(.12)
                .pan(.3);
        s.synth("theme", Instruments.lead())
                .add(s.melody(9, THEME))
                .add(s.melody(25, THEME))
                .layer(.75)
                .gain(.39)
                .sends(.3, .3);
        s.synth("stab", Instruments.stab())
                .add(s.stabs(62, "X . . X . . X . . . X . . X . ."))
                .bars("1-8,17-24")
                .layer(.75)
                .gain(.14)
                .duck(.4)
                .sends(.2, .25);
        s.synth("arp", Instruments.arpSaw())
                .add(s.arp(62, "0 1 2 3 4 3 2 1 0 1 2 3 4 5 4 3"))
                .bars("1-8,17-24")
                .layer(.75)
                .gain(.1)
                .duck(.4)
                .sends(.2, .15);
        s.synth("choir", Instruments.choir()).add(s.pad(62)).layer(.9).gain(.16).sends(0, .45);
        s.drum("shaker", SHAKER).add(s.hits(EIGHTHS)).layer(.9).gain(.26).pan(-.3);
        s.drum("crash", CRASH).add(s.hits("1,9,17,25", ONE)).layer(.9).gain(.22);
        s.drum("impact", IMPACT).add(s.hits("1,17", ONE)).layer(.9).gain(.35).sends(0, .3);
        s.drum("roll", SNARE).add(s.hits("8,16,24,32", ROLL)).layer(.9).gain(.272);
        return s;
    }

    /** Atempause: Versorgung, Händlerin, Werkstatt, Kapelle; das Motiv in Dur als Ruhepunkt. */
    static Song haven() {
        var s = new Song("haven", "Atempause", 76, 16);
        s.gain = 1.08;
        s.swing = .07;
        s.reverbSize = 1.3;
        s.reverbSeconds = 3.8;
        s.reverbBrightness = .4;
        s.delayFeedback = .35;
        s.harmony("Bbmaj7 Am7 Gm7 C7sus4 Bbmaj7 Am7 Dm7 C7 Bbmaj7 Am7 Gm7 C7sus4 Fmaj7 Dm7 Gm7 C7");
        s.synth("keys", Instruments.epiano())
                .add(s.stabs(62, "X - - - - - x - - - - - - - - -"))
                .gain(.14)
                .sends(.2, .35);
        s.synth("bass", Instruments.softBass())
                .add(s.bass(41, "x - - - - - - 5 - - x - - - - -"))
                .gain(.21);
        s.drum("kick", KICK).tune(.9).add(s.hits("X . . . . . . . . . x . . . . .")).gain(.248);
        s.drum("snare", SNARE)
                .tune(.8)
                .add(s.hits(". . . . x . . . . . . . x . . ."))
                .gain(.204)
                .sends(0, .3);
        s.drum("hat", HAT).add(s.hits("x . o . x . o . x . o . x . o .")).gain(.56);
        s.synth("melody", Instruments.bell())
                .add(s.melody(1, "d5:12 f5:4 | e5:8 c5:8 | d5:8 a4:8 | g4:12 .:4"))
                .add(s.melody(9, "f5:12 a5:4 | g5:8 e5:8 | f5:8 d5:8 | c5:12 .:4"))
                .gain(.153)
                .sends(.35, .35);
        s.synth("sonar", Instruments.sonar())
                .add(s.loop(4, s.melody(1, "c6:4 .:12 | .:16 | .:16 | .:16")))
                .gain(.08)
                .sends(.5, .6);
        s.synth("pad", Instruments.glassPad()).add(s.pad(60)).gain(.063).sends(0, .4);
        return s;
    }

    /** Auftauchen: das Leitmotiv in D-Dur, das Boot steigt ins Licht. */
    static Song ending() {
        var s = new Song("ending", "Auftauchen", 96, 24);
        s.gain = .93;
        s.reverbSize = 1.5;
        s.reverbSeconds = 4;
        s.reverbBrightness = .6;
        s.delayFeedback = .4;
        s.harmony("G A D D G A Bm A G A D A7 G A Em A7 Bb C D D Bb C D D");
        s.synth("pad", Instruments.brightPad()).add(s.pad(64)).gain(.12).sends(0, .4).duck(.4);
        s.synth("bells", Instruments.bell())
                .add(s.arp(66, "0 1 2 3 4 3 2 1 0 1 2 3 4 5 4 3"))
                .gain(.085)
                .sends(.3, .3);
        s.synth("sub", Instruments.sub()).add(s.roots(26)).gain(.109);
        s.synth("bass", Instruments.pulseBass())
                .add(s.bass(38, "x . x . x . x . x . x . x . x ."))
                .bars("9-24")
                .gain(.377)
                .duck(.4);
        s.drum("kick", KICK).add(s.hits("9-24", FOUR)).gain(.385).sidechain();
        s.drum("clap", CLAP).add(s.hits("17-24", BACKBEAT)).gain(.45).sends(0, .3);
        s.drum("hat", HAT).add(s.hits("9-24", OFFBEAT)).gain(.672);
        s.synth("intro", Instruments.bell())
                .add(
                        s.melody(
                                1,
                                "b4:8 d5:8 | c#5:8 e5:8 | f#5:16 | a5:16 | b5:8 a5:8 | e5:8 c#5:8"
                                        + " | d5:8 f#5:8 | e5:16"))
                .gain(.136)
                .sends(.35, .4);
        s.synth("lead", Instruments.trance())
                .add(
                        s.melody(
                                9,
                                "d5:12 f#5:4 | e5:8 c#5:8 | d5:12 a5:4 | g5:8 e5:8"
                                        + " | f#5:4 g5:4 a5:8 | g5:4 e5:4 c#5:8"
                                        + " | b4:4 d5:4 g5:4 f#5:4 | e5:8 c#5:4 a4:4"))
                .add(
                        s.melody(
                                17,
                                "f5:8 d5:4 f5:4 | g5:8 e5:4 g5:4 | a5:16 | f#5:8 a5:4 d6:4"
                                        + " | d6:8 bb5:4 f5:4 | e6:8 c6:4 g5:4 | d6:16"
                                        + " | a5:8 f#5:4 d5:4"))
                .gain(.322)
                .sends(.35, .35);
        s.synth("choir", Instruments.choir()).add(s.pad(62)).bars("17-24").gain(.16).sends(0, .5);
        s.drum("crash", CRASH).add(s.hits("9,17", ONE)).gain(.25);
        s.drum("riser", RISER).add(s.swells("8,16")).gain(.2).sends(.2, .3);
        return s;
    }
}
