package ch.zhaw.abyss.infrastructure.music;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Eine Stimme der Partitur: Instrument, Noten und Mischung. {@code layer} ist die Intensität, ab
 * der die Stimme einsetzt; so baut sich jedes Stück mit dem Kampfgeschehen auf.
 */
final class Part {
    final String name;
    final Patch patch;
    final DrumVoice.Kind drum;
    private final double barBeats;
    private final List<Note> notes = new ArrayList<>();
    double tune = 1, layer, gain = .6, pan, delay, reverb, duck;
    boolean sidechain;

    Part(String name, Patch patch, DrumVoice.Kind drum, double barBeats) {
        this.name = name;
        this.patch = patch;
        this.drum = drum;
        this.barBeats = barBeats;
    }

    Part add(List<Note> more) {
        notes.addAll(more);
        return this;
    }

    /** Beschränkt die bisher eingetragenen Noten auf Takte wie {@code "9-16,25-32"}. */
    Part bars(String spec) {
        var kept = Notation.only(notes, spec, barBeats);
        notes.clear();
        notes.addAll(kept);
        return this;
    }

    Part layer(double minimumIntensity) {
        layer = minimumIntensity;
        return this;
    }

    Part gain(double value) {
        gain = value;
        return this;
    }

    Part pan(double value) {
        pan = value;
        return this;
    }

    /** Anteile für Echo und Hall. */
    Part sends(double delaySend, double reverbSend) {
        delay = delaySend;
        reverb = reverbSend;
        return this;
    }

    /** Wird von der Bassdrum weggedrückt (Sidechain-Pumpen). */
    Part duck(double amount) {
        duck = amount;
        return this;
    }

    /** Diese Stimme löst das Pumpen der anderen aus. */
    Part sidechain() {
        sidechain = true;
        return this;
    }

    Part tune(double factor) {
        tune = factor;
        return this;
    }

    Part transpose(double semitones) {
        notes.replaceAll(note -> note.transposed(semitones));
        return this;
    }

    Note[] sorted(double swing) {
        return notes.stream()
                .map(
                        note -> {
                            double sixteenth = note.beat() * 4;
                            boolean offbeat =
                                    Math.abs(sixteenth - Math.rint(sixteenth)) < 1e-6
                                            && Math.floorMod((long) Math.rint(sixteenth), 2) == 1;
                            return swing > 0 && offbeat ? note.shifted(swing) : note;
                        })
                .sorted(Comparator.comparingDouble(Note::beat))
                .toArray(Note[]::new);
    }

    int size() {
        return notes.size();
    }
}
