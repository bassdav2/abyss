package ch.zhaw.abyss.infrastructure.music;

/**
 * Spielt ein Stück in Endlosschleife: Sequenzer, Stimmenverwaltung, Schichten nach Intensität,
 * Sidechain-Pumpen, Echo und Hall. Schichten wechseln nur auf Taktgrenzen, damit Einsätze
 * musikalisch klingen. Nach dem Aufbau teilt der Abspieler keinen Speicher mehr zu.
 */
final class SongPlayer {
    private static final class Track {
        final Part part;
        final Note[] notes;
        final SynthVoice[] voices;
        final DrumVoice[] drums;
        final double panL, panR;
        int cursor;
        long counter;
        double level, target, applied;

        Track(Part part, Note[] notes, long seed) {
            this.part = part;
            this.notes = notes;
            if (part.patch != null) {
                voices = new SynthVoice[Math.max(1, part.patch.polyphony)];
                for (int i = 0; i < voices.length; i++)
                    voices[i] = new SynthVoice(part.patch, seed + i * 7919L);
                drums = new DrumVoice[0];
            } else {
                voices = new SynthVoice[0];
                drums = new DrumVoice[4];
                for (int i = 0; i < drums.length; i++)
                    drums[i] = new DrumVoice(part.drum, part.tune, seed + i * 104729L);
            }
            double angle = (Math.max(-1, Math.min(1, part.pan)) + 1) * Math.PI / 4;
            panL = Math.cos(angle) * Math.sqrt(2);
            panR = Math.sin(angle) * Math.sqrt(2);
        }

        boolean sounding() {
            for (var voice : voices) if (voice.active()) return true;
            for (var drum : drums) if (drum.active()) return true;
            return false;
        }
    }

    final Song song;
    private final Track[] tracks;
    private final PingPong delay;
    private final Reverb reverb;
    private final double[] busL = new double[Dsp.CONTROL], busR = new double[Dsp.CONTROL];
    private final double[] dryL, dryR, delayL, delayR, reverbL, reverbR;
    private double position, clock, duck, duckHold;
    private boolean primed;
    private double gain, gainTarget, gainStep;
    double tempo = 1;

    SongPlayer(Song song, int capacity) {
        this.song = song;
        tracks = new Track[song.parts.size()];
        for (int i = 0; i < tracks.length; i++) {
            var part = song.parts.get(i);
            tracks[i] = new Track(part, part.sorted(song.swing), 0x5EED + 977L * i);
        }
        delay = new PingPong(song.delayBeats * 60 / song.bpm, song.delayFeedback);
        reverb = new Reverb(song.reverbSize, song.reverbSeconds, song.reverbBrightness);
        dryL = new double[capacity];
        dryR = new double[capacity];
        delayL = new double[capacity];
        delayR = new double[capacity];
        reverbL = new double[capacity];
        reverbR = new double[capacity];
    }

    /** Blendet über {@code seconds} auf {@code value}. */
    void fade(double value, double seconds) {
        gainTarget = value;
        gainStep = Math.abs(value - gain) / Math.max(1, seconds * Dsp.RATE);
        if (seconds <= 0) gain = value;
    }

    boolean silent() {
        return gain <= 0 && gainTarget <= 0;
    }

    /** Schlagposition innerhalb der Schleife (für Tests). */
    double position() {
        return position;
    }

    /** Rendert n Abtastwerte (höchstens die Kapazität) additiv in die Ausgänge. */
    void render(double[] outL, double[] outR, int n, double intensity) {
        java.util.Arrays.fill(dryL, 0, n, 0);
        java.util.Arrays.fill(dryR, 0, n, 0);
        java.util.Arrays.fill(delayL, 0, n, 0);
        java.util.Arrays.fill(delayR, 0, n, 0);
        java.util.Arrays.fill(reverbL, 0, n, 0);
        java.util.Arrays.fill(reverbR, 0, n, 0);
        double beatsPerSample = song.bpm * tempo / 60 * Dsp.DT;
        double duckRelease = Math.exp(-Dsp.CONTROL * Dsp.DT / (.24 * 60 / song.bpm));
        double duckAttack = Dsp.smoothing(.004, Dsp.CONTROL);
        for (int offset = 0; offset < n; offset += Dsp.CONTROL) {
            int m = Math.min(Dsp.CONTROL, n - offset);
            double beats = m * beatsPerSample;
            boolean barLine =
                    !primed
                            || Math.floor(position / Song.BAR)
                                    != Math.floor((position + beats) / Song.BAR)
                            || position + beats >= song.beats();
            if (barLine) layers(intensity);
            primed = true;
            sequence(beats);
            release();
            duck += (duckHold - duck) * duckAttack;
            duckHold *= duckRelease;
            for (var track : tracks) mix(track, offset, m, beats);
        }
        delay.process(delayL, delayR, dryL, dryR, n);
        reverb.process(reverbL, reverbR, dryL, dryR, n);
        double g = song.gain;
        for (int s = 0; s < n; s++) {
            if (gain < gainTarget) gain = Math.min(gainTarget, gain + gainStep);
            else if (gain > gainTarget) gain = Math.max(gainTarget, gain - gainStep);
            outL[s] += dryL[s] * gain * g;
            outR[s] += dryR[s] * gain * g;
        }
    }

    private void layers(double intensity) {
        for (var track : tracks) {
            boolean on =
                    track.target > 0
                            ? intensity >= track.part.layer - .05
                            : intensity >= track.part.layer;
            track.target = on ? 1 : 0;
            if (!primed) track.level = track.applied = track.target;
        }
    }

    private void sequence(double beats) {
        double end = position + beats;
        double loop = song.beats();
        trigger(position, Math.min(end, loop));
        if (end >= loop) {
            for (var track : tracks) track.cursor = 0;
            end -= loop;
            trigger(0, end);
        }
        position = end;
        clock += beats;
    }

    private void trigger(double from, double to) {
        for (var track : tracks) {
            var notes = track.notes;
            while (track.cursor < notes.length && notes[track.cursor].beat() < to) {
                var note = notes[track.cursor++];
                if (note.beat() < from - 1e-9) continue;
                if (track.target <= 0 && track.level <= 0) continue;
                play(track, note);
            }
        }
    }

    private void play(Track track, Note note) {
        if (track.drums.length > 0) {
            var drum = track.drums[(int) (track.counter++ % track.drums.length)];
            drum.trigger(note.velocity(), note.length() * 60 / (song.bpm * tempo));
            if (track.part.sidechain)
                duckHold =
                        Math.max(duckHold, note.velocity() * Math.max(track.level, track.target));
            return;
        }
        var patch = track.part.patch;
        SynthVoice voice;
        boolean legato = false;
        if (patch.mono) {
            voice = track.voices[0];
            legato =
                    patch.glide > 0
                            && voice.held
                            && voice.endBeat >= clock - 1e-6
                            && voice.active();
        } else voice = free(track);
        voice.start(note.pitch(), note.velocity(), legato);
        voice.endBeat = clock + note.length() - 1e-6;
        voice.order = track.counter++;
    }

    private static SynthVoice free(Track track) {
        SynthVoice oldest = null, oldestReleased = null;
        for (var voice : track.voices) {
            if (!voice.active()) return voice;
            if (!voice.held && (oldestReleased == null || voice.order < oldestReleased.order))
                oldestReleased = voice;
            if (oldest == null || voice.order < oldest.order) oldest = voice;
        }
        return oldestReleased != null ? oldestReleased : oldest;
    }

    private void release() {
        for (var track : tracks)
            for (var voice : track.voices)
                if (voice.held && voice.endBeat <= clock) voice.release();
    }

    private void mix(Track track, int offset, int m, double beats) {
        double fadeIn = beats / .25, fadeOut = beats / 1.5;
        if (track.level < track.target) track.level = Math.min(track.target, track.level + fadeIn);
        else if (track.level > track.target)
            track.level = Math.max(track.target, track.level - fadeOut);
        if (track.level <= 0 && track.applied <= 0) {
            if (track.sounding()) {
                for (var voice : track.voices) voice.kill();
                for (var drum : track.drums) drum.stop();
            }
            return;
        }
        java.util.Arrays.fill(busL, 0, m, 0);
        java.util.Arrays.fill(busR, 0, m, 0);
        boolean any = false;
        for (var voice : track.voices)
            if (voice.active()) {
                voice.render(busL, busR, 0, m);
                any = true;
            }
        for (var drum : track.drums)
            if (drum.active()) {
                drum.render(busL, 0, m);
                any = true;
            }
        var part = track.part;
        double next = part.gain * track.level * (1 - part.duck * duck);
        double previous = track.applied;
        track.applied = next;
        if (!any) return;
        boolean mono = track.drums.length > 0;
        double step = (next - previous) / m;
        for (int s = 0; s < m; s++) {
            double g = previous + step * (s + 1);
            double l = busL[s] * g * track.panL;
            double r = (mono ? busL[s] : busR[s]) * g * track.panR;
            int i = offset + s;
            dryL[i] += l;
            dryR[i] += r;
            delayL[i] += l * part.delay;
            delayR[i] += r * part.delay;
            reverbL[i] += l * part.reverb;
            reverbR[i] += r * part.reverb;
        }
    }
}
