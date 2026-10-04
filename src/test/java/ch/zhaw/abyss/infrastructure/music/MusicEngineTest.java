package ch.zhaw.abyss.infrastructure.music;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/** Bordsynthesizer: Notenschrift, Partituren und Klangerzeugung ohne Ausgabegerät. */
class MusicEngineTest {
    private static final int RATE = 44100;

    @Test
    void noteNamesAndChordSymbolsFollowTheUsualSpelling() {
        assertEquals(60, Notation.pitch("c4"));
        assertEquals(58, Notation.pitch("bb3"));
        assertEquals(61, Notation.pitch("c#4"));
        assertEquals(71, Notation.pitch("b4"));
        assertArrayEquals(new int[] {10, 2, 5, 9}, Notation.chord("Bbmaj7").pitchClasses());
        assertArrayEquals(new int[] {9, 1, 4, 7}, Notation.chord("A7").pitchClasses());
        assertEquals(4, Notation.chord("C/E").bass());
        assertThrows(IllegalArgumentException.class, () -> Notation.chord("Hmaj7"));
    }

    @Test
    void melodiesHoldRestAndValidateEveryBar() {
        var notes = Notation.melody("d5:12 f5:4 | e5 - . c5!+e5 .:12", 16, 4);
        assertEquals(5, notes.size());
        assertEquals(3, notes.get(0).length(), 1e-9);
        assertEquals(.5, notes.get(2).length(), 1e-9);
        assertEquals(1, notes.get(3).velocity(), 1e-9);
        assertEquals(4 + .75, notes.get(4).beat(), 1e-9);
        assertThrows(
                IllegalArgumentException.class, () -> Notation.melody("d5:12 f5:3 | e5:16", 16, 4));
    }

    @Test
    void padsUseCloseVoicingsThatMoveLittle() {
        var harmony = Notation.harmony("C Am F G", 4);
        var pads = Notation.pad(harmony, 64, .8);
        assertEquals(12, pads.size());
        for (int chord = 1; chord < 4; chord++) {
            int moved = 0;
            for (int voice = 0; voice < 3; voice++)
                moved +=
                        (int)
                                Math.abs(
                                        pads.get(chord * 3 + voice).pitch()
                                                - pads.get((chord - 1) * 3 + voice).pitch());
            assertTrue(moved <= 6, "Stimmführung springt um " + moved + " Halbtöne");
        }
    }

    @Test
    void everyScoreBuildsWithAHarmonyThatFillsTheSong() {
        var songs = Scores.all();
        assertEquals(10, songs.size());
        assertEquals(MusicEngine.songIds(), songs.keySet().stream().toList());
        for (var song : songs.values()) {
            assertTrue(song.parts.size() >= 8, song.id);
            for (var part : song.parts) assertTrue(part.size() > 0, song.id + "/" + part.name);
            double seconds = song.beats() * 60 / song.bpm;
            assertTrue(seconds > 40 && seconds < 100, song.id + " dauert " + seconds + " s");
        }
    }

    @Test
    void longMelodyNotesNeverClashWithTheHarmony() {
        var melodic = java.util.Set.of("riff", "theme", "lead", "hymn", "melody", "intro", "low");
        for (var song : Scores.all().values())
            for (var part : song.parts) {
                if (!melodic.contains(part.name)) continue;
                for (var note : part.sorted(0)) {
                    if (note.length() < 1) continue;
                    var chord = Notation.chordAt(song.harmony(), note.beat());
                    int interval = Math.floorMod((int) note.pitch() - chord.root(), 12);
                    boolean major = chord.contains((chord.root() + 4) % 12);
                    boolean minor = chord.contains((chord.root() + 3) % 12);
                    boolean clash =
                            interval == 1
                                    || interval == 6 && !chord.contains((chord.root() + 6) % 12)
                                    || major && interval == 3
                                    || minor && interval == 4;
                    assertFalse(
                            clash, song.id + "/" + part.name + " Takt " + (note.beat() / 4 + 1));
                }
            }
    }

    @Test
    void everySongRendersCleanAudioBelowFullScale() {
        for (String id : MusicEngine.songIds()) {
            var engine = MusicEngine.offline();
            engine.volume(1);
            engine.cue(id);
            engine.intensity(1);
            double[][] audio = render(engine, 4);
            double peak = 0, energy = 0;
            for (double[] channel : audio)
                for (double v : channel) {
                    assertTrue(Double.isFinite(v), id);
                    peak = Math.max(peak, Math.abs(v));
                    energy += v * v;
                }
            double rms = 10 * Math.log10(energy / (2.0 * audio[0].length));
            assertTrue(peak <= 1, id + " übersteuert: " + peak);
            assertTrue(rms > -40, id + " ist zu leise: " + rms + " dB");
        }
    }

    @Test
    void renderingIsDeterministic() {
        var first = MusicEngine.offline();
        var second = MusicEngine.offline();
        for (var engine : new MusicEngine[] {first, second}) {
            engine.cue("boss");
            engine.intensity(1);
        }
        assertTrue(Arrays.equals(render(first, 2)[0], render(second, 2)[0]));
    }

    @Test
    void higherIntensityAddsLayers() {
        double calm = rms(levelled("engine", .2, 0)), full = rms(levelled("engine", 1, 0));
        assertTrue(full > calm + 4, "Kampf " + full + " dB gegen Ruhe " + calm + " dB");
    }

    @Test
    void theUnderwaterFilterTakesAwayTheHighs() {
        double clear = brightness(levelled("empress", 1, 0));
        double muffled = brightness(levelled("empress", 1, 1));
        assertTrue(muffled < clear * .3, "gedämpft " + muffled + " gegen klar " + clear);
    }

    @Test
    void changingTheCueCrossfadesToTheNewSong() {
        var engine = MusicEngine.offline();
        engine.cue("hold");
        render(engine, 1);
        assertEquals("hold", engine.playing());
        engine.cue("boss");
        var audio = render(engine, 3);
        assertEquals("boss", engine.playing());
        for (double v : audio[0]) assertTrue(Double.isFinite(v) && Math.abs(v) <= 1);
        engine.cue("gibt-es-nicht");
        render(engine, .2);
        assertEquals("boss", engine.playing());
    }

    @Test
    void everySoundFileIsKnownAndLoads() throws Exception {
        var bank = SoundBank.load();
        assertEquals(SoundBank.NAMES.size(), bank.loaded());
        var folder = java.nio.file.Path.of(SoundBank.class.getResource("/audio").toURI());
        try (var files = java.nio.file.Files.list(folder)) {
            files.map(f -> f.getFileName().toString().replace(".wav", ""))
                    .forEach(name -> assertTrue(SoundBank.id(name) >= 0, name + " fehlt"));
        }
    }

    @Test
    void aFloodOfSoundsStaysBoundedAndNeverQueuesUp() {
        var mixer = new SoundMixer();
        mixer.bank(SoundBank.load());
        double[] left = new double[512], right = new double[512];
        int accepted = 0;
        for (int block = 0; block < 200; block++) {
            for (int i = 0; i < 400; i++)
                if (mixer.trigger(i % SoundBank.NAMES.size(), .7)) accepted++;
            assertTrue(mixer.pending() <= SoundMixer.QUEUE);
            mixer.render(left, right, 512);
            assertEquals(0, mixer.pending(), "nichts wartet nach einem Block");
            assertTrue(mixer.active() <= SoundMixer.VOICES);
            for (int id = 0; id < SoundBank.NAMES.size(); id++)
                assertTrue(mixer.active(id) <= SoundMixer.PER_SOUND);
        }
        assertTrue(accepted <= 200 * SoundMixer.QUEUE);
        assertTrue(mixer.dropped() > 0, "Überlast wird verworfen statt gestaut");
    }

    @Test
    void soundsPlayEvenWhenTheMusicIsMuted() {
        var engine = MusicEngine.offline();
        engine.volume(0);
        assertTrue(engine.sound("explosion", .7));
        assertFalse(engine.sound("gibt-es-nicht", .7));
        double level = rms(render(engine, .5)[0]);
        assertTrue(level > -40, "Explosion hörbar: " + level + " dB");
        double[][] audio = render(engine, 4);
        for (double v : audio[0]) assertTrue(Math.abs(v) <= 1);
    }

    @Test
    void theAmbienceLoopsWithoutEnd() {
        var engine = MusicEngine.offline();
        engine.volume(0);
        engine.ambience(.3);
        double first = rms(render(engine, 2)[0]), later = rms(render(engine, 20)[0]);
        assertTrue(first > -50 && later > -50, first + " / " + later);
    }

    @Test
    void theSynthesizerRunsFarFasterThanRealTime() {
        var engine = MusicEngine.offline();
        engine.cue("empress");
        engine.intensity(1);
        render(engine, 1);
        long start = System.nanoTime();
        render(engine, 10);
        double seconds = (System.nanoTime() - start) / 1e9;
        assertTrue(seconds < 2.5, "10 s Musik brauchten " + seconds + " s Rechenzeit");
    }

    private static double[] levelled(String id, double intensity, double muffle) {
        var engine = MusicEngine.offline();
        engine.volume(.5);
        engine.cue(id);
        engine.intensity(intensity);
        engine.muffle(muffle);
        render(engine, 2);
        return render(engine, 6)[0];
    }

    private static double rms(double[] samples) {
        double sum = 0;
        for (double v : samples) sum += v * v;
        return 10 * Math.log10(sum / samples.length + 1e-20);
    }

    /** Energie der Differenzen relativ zur Gesamtenergie: grobes Mass für Höhen. */
    private static double brightness(double[] samples) {
        double diff = 0, sum = 0;
        for (int i = 1; i < samples.length; i++) {
            double d = samples[i] - samples[i - 1];
            diff += d * d;
            sum += samples[i] * samples[i];
        }
        return diff / (sum + 1e-20);
    }

    private static double[][] render(MusicEngine engine, double seconds) {
        int frames = (int) (seconds * RATE);
        double[] left = new double[frames], right = new double[frames];
        engine.render(left, right);
        return new double[][] {left, right};
    }
}
