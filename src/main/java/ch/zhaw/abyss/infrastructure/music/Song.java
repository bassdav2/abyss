package ch.zhaw.abyss.infrastructure.music;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Musikstück: Tempo, Länge, Harmonie, Stimmen und Raumklang. Die Hilfsmethoden erzeugen
 * Begleitungen aus der Harmonie, damit die Partituren kurz und lesbar bleiben.
 */
final class Song {
    static final double BAR = 4;

    final String id, title;
    final double bpm;
    final int bars;
    final List<Part> parts = new ArrayList<>();
    double swing, delayBeats = .75, delayFeedback = .38;
    double reverbSize = 1.2, reverbSeconds = 3.2, reverbBrightness = .45, gain = 1;
    private List<Notation.Span> harmony = List.of();

    Song(String id, String title, double bpm, int bars) {
        this.id = id;
        this.title = title;
        this.bpm = bpm;
        this.bars = bars;
    }

    double beats() {
        return bars * BAR;
    }

    List<Notation.Span> harmony() {
        return harmony;
    }

    Song harmony(String text) {
        harmony = Notation.harmony(text, BAR);
        double length = harmony.getLast().beat() + harmony.getLast().length();
        if (Math.abs(length - beats()) > 1e-6)
            throw new IllegalArgumentException(
                    id + ": Harmonie dauert " + length / BAR + " statt " + bars + " Takte");
        return this;
    }

    Part synth(String name, Patch patch) {
        var part = new Part(name, patch, null, BAR);
        parts.add(part);
        return part;
    }

    Part drum(String name, DrumVoice.Kind kind) {
        var part = new Part(name, null, kind, BAR);
        parts.add(part);
        return part;
    }

    /** Akkordflächen um die Tonhöhe {@code center}. */
    List<Note> pad(double center) {
        return Notation.pad(harmony, center, .8);
    }

    /** Grundtöne ab {@code base}. */
    List<Note> roots(int base) {
        return Notation.roots(harmony, base, .85);
    }

    /** Bassfigur in Sechzehnteln, taktweise wiederholt. */
    List<Note> bass(int base, String figure) {
        return bass(base, figure, 16);
    }

    List<Note> bass(int base, String figure, int res) {
        return Notation.follow(
                harmony, figure, res, BAR, beats(), (t, c) -> Notation.bassStep(t, c, base));
    }

    /** Arpeggio über die Akkordtöne ab {@code center}. */
    List<Note> arp(int center, String figure) {
        return arp(center, figure, 16);
    }

    List<Note> arp(int center, String figure, int res) {
        return Notation.follow(
                harmony, figure, res, BAR, beats(), (t, c) -> Notation.arpStep(t, c, center));
    }

    /** Akkordschläge im Rhythmus {@code x}/{@code X}, mit Stimmführung. */
    List<Note> stabs(double center, String rhythm) {
        return stabs(center, rhythm, 16);
    }

    List<Note> stabs(double center, String rhythm, int res) {
        int[][] previous = {null};
        return Notation.follow(
                harmony,
                rhythm.replace("X", "s!").replace("x", "s"),
                res,
                BAR,
                beats(),
                (t, c) -> {
                    int[] voicing = Notation.voicing(c, center, previous[0]);
                    previous[0] = voicing;
                    double[] pitches = new double[voicing.length];
                    for (int i = 0; i < voicing.length; i++) pitches[i] = voicing[i];
                    return pitches;
                });
    }

    /** Melodie ab Takt {@code fromBar} (ab 1) in Sechzehnteln. */
    List<Note> melody(int fromBar, String text) {
        return melody(fromBar, text, 16);
    }

    List<Note> melody(int fromBar, String text, int res) {
        return Notation.melody(text, res, BAR).stream()
                .map(note -> note.shifted((fromBar - 1) * BAR))
                .toList();
    }

    /** Wiederholt ein Muster von {@code bars} Takten bis zum Stückende. */
    List<Note> loop(int bars, List<Note> pattern) {
        return Notation.tile(pattern, bars * BAR, beats());
    }

    /** Je eine taktlange Note in den angegebenen Takten, etwa für Rauschanstiege. */
    List<Note> swells(String bars) {
        var notes = new ArrayList<Note>();
        for (int bar = 1; bar <= this.bars; bar++)
            notes.add(new Note((bar - 1) * BAR, BAR, 60, .9));
        return Notation.only(notes, bars, BAR);
    }

    /** Schlagzeugmuster in Sechzehnteln, bis zum Stückende wiederholt. */
    List<Note> hits(String text) {
        return hits(text, 16);
    }

    List<Note> hits(String text, int res) {
        var pattern = Notation.hits(text, res, BAR);
        double length = Notation.tokens(text, res).size() / (double) res * BAR;
        return Notation.tile(pattern, length, beats());
    }

    /** Muster nur in bestimmten Takten. */
    List<Note> hits(String bars, String text) {
        return Notation.only(hits(text), bars, BAR);
    }
}
