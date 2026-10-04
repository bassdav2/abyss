package ch.zhaw.abyss.infrastructure.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Kleine Notenschrift für die Partituren, angelehnt an Tracker:
 *
 * <ul>
 *   <li>Melodie: {@code "d5 - - f5 | e5 . c5!"}: Ton, {@code -} hält, {@code .} Pause, {@code !}
 *       Akzent, {@code ?} leise, {@code +} verbindet Akkordtöne. Jeder Taktstrich trennt genau
 *       {@code res} Schritte.
 *   <li>Schlagzeug: {@code x} normal, {@code X} Akzent, {@code o} Geisternote.
 *   <li>Harmonie: ein Akkordsymbol pro Takt, {@code Dm*4} wiederholt, {@code Dm,C} teilt den Takt.
 * </ul>
 *
 * Daraus erzeugen Begleitmuster (Flächen, Bass, Arpeggio, Akkordschläge) die Noten passend zur
 * Harmonie, mit Stimmführung zum nächstgelegenen Akkord.
 */
final class Notation {
    /** Akkord mit Grundton (0 = C), Intervallen und optionalem Basston (-1 ohne). */
    record Chord(int root, int[] intervals, int bass) {
        int[] pitchClasses() {
            return Arrays.stream(intervals).map(i -> (root + i) % 12).distinct().toArray();
        }

        boolean contains(int pitchClass) {
            for (int pc : pitchClasses()) if (pc == pitchClass) return true;
            return false;
        }
    }

    /** Ein Harmonieabschnitt. */
    record Span(double beat, double length, Chord chord) {}

    private static final Pattern NOTE = Pattern.compile("([a-g])([#b]?)(-?\\d)");
    private static final Pattern SYMBOL =
            Pattern.compile("([A-G])([#b]?)([^/]*)(?:/([A-G][#b]?))?");
    private static final int[] NATURAL = {9, 11, 0, 2, 4, 5, 7};
    private static final Map<String, int[]> QUALITIES =
            Map.ofEntries(
                    Map.entry("", new int[] {0, 4, 7}),
                    Map.entry("m", new int[] {0, 3, 7}),
                    Map.entry("5", new int[] {0, 7}),
                    Map.entry("7", new int[] {0, 4, 7, 10}),
                    Map.entry("maj7", new int[] {0, 4, 7, 11}),
                    Map.entry("m7", new int[] {0, 3, 7, 10}),
                    Map.entry("6", new int[] {0, 4, 7, 9}),
                    Map.entry("m6", new int[] {0, 3, 7, 9}),
                    Map.entry("sus2", new int[] {0, 2, 7}),
                    Map.entry("sus4", new int[] {0, 5, 7}),
                    Map.entry("7sus4", new int[] {0, 5, 7, 10}),
                    Map.entry("add9", new int[] {0, 4, 7, 14}),
                    Map.entry("madd9", new int[] {0, 3, 7, 14}),
                    Map.entry("m9", new int[] {0, 3, 7, 10, 14}),
                    Map.entry("maj9", new int[] {0, 4, 7, 11, 14}),
                    Map.entry("dim", new int[] {0, 3, 6}),
                    Map.entry("aug", new int[] {0, 4, 8}));

    private Notation() {}

    /** Tonname wie {@code c#4} oder {@code bb2} als MIDI-Tonhöhe (c4 = 60). */
    static double pitch(String name) {
        var m = NOTE.matcher(name);
        if (!m.matches()) throw new IllegalArgumentException("Unbekannter Ton: " + name);
        int pc = NATURAL[m.group(1).charAt(0) - 'a'];
        if (m.group(2).equals("#")) pc++;
        if (m.group(2).equals("b")) pc--;
        return 12 * (Integer.parseInt(m.group(3)) + 1) + pc;
    }

    /** Akkordsymbol wie {@code Bbmaj7}, {@code F#m}, {@code C/E}. */
    static Chord chord(String symbol) {
        var m = SYMBOL.matcher(symbol);
        if (!m.matches()) throw new IllegalArgumentException("Unbekannter Akkord: " + symbol);
        int[] intervals = QUALITIES.get(m.group(3));
        if (intervals == null)
            throw new IllegalArgumentException("Unbekannte Akkordart: " + symbol);
        int bass = m.group(4) == null ? -1 : pitchClass(m.group(4));
        return new Chord(pitchClass(m.group(1) + m.group(2)), intervals, bass);
    }

    private static int pitchClass(String name) {
        int pc = NATURAL[Character.toLowerCase(name.charAt(0)) - 'a'];
        if (name.length() > 1) pc += name.charAt(1) == '#' ? 1 : -1;
        return Math.floorMod(pc, 12);
    }

    /** Harmoniefolge, ein Symbol pro Takt. */
    static List<Span> harmony(String text, double barBeats) {
        var spans = new ArrayList<Span>();
        double beat = 0;
        for (String token : text.trim().split("\\s+")) {
            if (token.equals("|")) continue;
            int repeat = 1;
            int star = token.indexOf('*');
            if (star > 0) {
                repeat = Integer.parseInt(token.substring(star + 1));
                token = token.substring(0, star);
            }
            String[] parts = token.split(",");
            for (int r = 0; r < repeat; r++)
                for (String part : parts) {
                    double length = barBeats / parts.length;
                    spans.add(new Span(beat, length, chord(part)));
                    beat += length;
                }
        }
        return spans;
    }

    static Chord chordAt(List<Span> harmony, double beat) {
        double total = harmony.getLast().beat() + harmony.getLast().length();
        double b = beat % total;
        for (var span : harmony)
            if (b >= span.beat() - 1e-9 && b < span.beat() + span.length() - 1e-9)
                return span.chord();
        return harmony.getLast().chord();
    }

    /** Schritte eines Musters; prüft, dass jeder Takt genau {@code res} Schritte hat. */
    static List<String> tokens(String text, int res) {
        var result = new ArrayList<String>();
        boolean bars = text.contains("|");
        for (String bar : text.split("\\|")) {
            if (bar.isBlank()) continue;
            var tokens = expand(bar.trim().split("\\s+"));
            if (bars && tokens.size() != res)
                throw new IllegalArgumentException(
                        "Takt mit " + tokens.size() + " statt " + res + " Schritten: " + bar);
            result.addAll(tokens);
        }
        if (result.size() % res != 0)
            throw new IllegalArgumentException(
                    result.size() + " Schritte sind kein Vielfaches von " + res + ": " + text);
        return result;
    }

    /** {@code d5:4} steht für {@code d5 - - -}, {@code .:4} für vier Pausen. */
    private static List<String> expand(String[] raw) {
        var tokens = new ArrayList<String>();
        for (String token : raw) {
            int colon = token.lastIndexOf(':');
            if (colon <= 0) {
                tokens.add(token);
                continue;
            }
            String head = token.substring(0, colon);
            int count = Integer.parseInt(token.substring(colon + 1));
            tokens.add(head);
            for (int i = 1; i < count; i++) tokens.add(head.equals(".") ? "." : "-");
        }
        return tokens;
    }

    /** Melodie mit festen Tönen. */
    static List<Note> melody(String text, int res, double barBeats) {
        double step = barBeats / res;
        var notes = new ArrayList<Note>();
        var open = new ArrayList<double[]>();
        int index = 0;
        for (String token : tokens(text, res)) {
            double beat = index++ * step;
            if (token.equals("-")) {
                for (double[] o : open) o[1] += step;
                continue;
            }
            close(open, notes);
            if (token.equals(".")) continue;
            double velocity = velocity(token);
            for (String name : token.replaceAll("[!?]", "").split("\\+"))
                open.add(new double[] {beat, step, pitch(name), velocity});
        }
        close(open, notes);
        return notes;
    }

    /** Schlagzeugmuster. */
    static List<Note> hits(String text, int res, double barBeats) {
        double step = barBeats / res;
        var notes = new ArrayList<Note>();
        int index = 0;
        for (String token : tokens(text, res)) {
            double beat = index++ * step;
            switch (token) {
                case "x" -> notes.add(new Note(beat, step, 60, .8));
                case "X" -> notes.add(new Note(beat, step, 60, 1));
                case "o" -> notes.add(new Note(beat, step, 60, .42));
                case ".", "-" -> {}
                default -> throw new IllegalArgumentException("Unbekannter Schlag: " + token);
            }
        }
        return notes;
    }

    private static double velocity(String token) {
        if (token.contains("!") || token.equals("X") || token.equals("O")) return 1;
        if (token.contains("?")) return .5;
        return .8;
    }

    private static void close(List<double[]> open, List<Note> notes) {
        for (double[] o : open) notes.add(new Note(o[0], o[1], o[2], o[3]));
        open.clear();
    }

    /** Liefert für einen Schritt die Töne zum aktuellen Akkord. */
    @FunctionalInterface
    interface StepMapper {
        double[] pitches(String token, Chord chord);
    }

    /** Wendet ein taktweise wiederholtes Muster auf die Harmonie über das ganze Stück an. */
    static List<Note> follow(
            List<Span> harmony,
            String figure,
            int res,
            double barBeats,
            double totalBeats,
            StepMapper mapper) {
        var tokens = tokens(figure, res);
        double step = barBeats / res;
        int steps = (int) Math.round(totalBeats / step);
        var notes = new ArrayList<Note>();
        var open = new ArrayList<double[]>();
        for (int i = 0; i < steps; i++) {
            String token = tokens.get(i % tokens.size());
            double beat = i * step;
            if (token.equals("-")) {
                for (double[] o : open) o[1] += step;
                continue;
            }
            close(open, notes);
            if (token.equals(".")) continue;
            double velocity = velocity(token);
            String core = token.replaceAll("[!?]", "");
            for (double pitch : mapper.pitches(core, chordAt(harmony, beat)))
                open.add(new double[] {beat, step, pitch, velocity});
        }
        close(open, notes);
        return notes;
    }

    /** Engste Lage um {@code center}, möglichst nah an der vorherigen Lage. */
    static int[] voicing(Chord chord, double center, int[] previous) {
        int[] pcs = chord.pitchClasses();
        int[] best = null;
        double bestScore = Double.MAX_VALUE;
        for (int start = (int) center - 9; start <= (int) center + 3; start++) {
            if (!chord.contains(Math.floorMod(start, 12))) continue;
            int[] candidate = new int[pcs.length];
            int count = 0;
            for (int p = start; count < pcs.length && p < start + 12; p++)
                if (chord.contains(Math.floorMod(p, 12))) candidate[count++] = p;
            double mean = Arrays.stream(candidate).average().orElse(center);
            double score = .5 * Math.abs(mean - center);
            if (previous != null && previous.length == candidate.length)
                for (int i = 0; i < candidate.length; i++)
                    score += Math.abs(candidate[i] - previous[i]);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    /** Tiefster Ton ab {@code base} mit dem Grund- oder Basston des Akkords. */
    static int bassNote(Chord chord, int base) {
        int pc = chord.bass() >= 0 ? chord.bass() : chord.root();
        return base + Math.floorMod(pc - base, 12);
    }

    /** Liegende Akkordflächen, ein Akkord pro Harmonieabschnitt. */
    static List<Note> pad(List<Span> harmony, double center, double velocity) {
        var notes = new ArrayList<Note>();
        int[] previous = null;
        for (var span : harmony) {
            int[] voicing = voicing(span.chord(), center, previous);
            for (int pitch : voicing)
                notes.add(new Note(span.beat(), span.length(), pitch, velocity));
            previous = voicing;
        }
        return notes;
    }

    /** Grundtöne als liegende Noten. */
    static List<Note> roots(List<Span> harmony, int base, double velocity) {
        var notes = new ArrayList<Note>();
        for (var span : harmony)
            notes.add(new Note(span.beat(), span.length(), bassNote(span.chord(), base), velocity));
        return notes;
    }

    /**
     * Bassfigur: {@code x} Grundton, {@code o} Oktave, {@code 5} Quinte, {@code 3} Terz, {@code 7}
     * Septime; Grossbuchstaben sind Akzente.
     */
    static double[] bassStep(String token, Chord chord, int base) {
        int root = bassNote(chord, base);
        int[] iv = chord.intervals();
        int value =
                switch (token.toLowerCase()) {
                    case "x" -> root;
                    case "o" -> root + 12;
                    case "5" -> root + (iv.length > 2 ? iv[2] : 7);
                    case "3" -> root + (iv.length > 1 ? iv[1] : 4);
                    case "7" -> root + (iv.length > 3 ? iv[3] : 10);
                    default -> throw new IllegalArgumentException("Unbekannter Basston: " + token);
                };
        return new double[] {value};
    }

    /** Arpeggio: Ziffern zählen die Akkordtöne ab {@code center} aufwärts. */
    static double[] arpStep(String token, Chord chord, int center) {
        int index = Integer.parseInt(token);
        int count = 0;
        for (int p = center; p < center + 48; p++)
            if (chord.contains(Math.floorMod(p, 12)) && count++ == index) return new double[] {p};
        return new double[] {center};
    }

    /** Wiederholt ein Muster bis zur Stücklänge. */
    static List<Note> tile(List<Note> pattern, double patternBeats, double totalBeats) {
        var notes = new ArrayList<Note>();
        for (double offset = 0; offset < totalBeats - 1e-9; offset += patternBeats)
            for (var note : pattern)
                if (note.beat() + offset < totalBeats - 1e-9) notes.add(note.shifted(offset));
        return notes;
    }

    /** Behält nur Noten in den angegebenen Takten, zum Beispiel {@code "9-16,25-32"} (ab 1). */
    static List<Note> only(List<Note> notes, String bars, double barBeats) {
        var ranges = new ArrayList<int[]>();
        for (String range : bars.split(",")) {
            String[] ends = range.trim().split("-");
            int from = Integer.parseInt(ends[0]);
            int to = ends.length > 1 ? Integer.parseInt(ends[1]) : from;
            ranges.add(new int[] {from, to});
        }
        var result = new ArrayList<Note>();
        for (var note : notes) {
            int bar = (int) Math.floor(note.beat() / barBeats + 1e-9) + 1;
            for (int[] r : ranges)
                if (bar >= r[0] && bar <= r[1]) {
                    result.add(note);
                    break;
                }
        }
        return result;
    }
}
