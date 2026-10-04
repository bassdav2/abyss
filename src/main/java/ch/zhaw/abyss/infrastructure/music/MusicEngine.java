package ch.zhaw.abyss.infrastructure.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

/**
 * Bordaudio: erzeugt den Soundtrack zur Laufzeit aus den Partituren in {@link Scores} und mischt
 * die Spielklänge dazu. Stücke werden überblendet, Schichten folgen der Intensität, ein
 * Unterwasserfilter dämpft die Musik in Menüs und bei wenig Leben, ein Begrenzer schützt vor
 * Übersteuerung. Klänge laufen über einen eigenen Mischer mit fester Stimmenzahl ({@link
 * SoundMixer}), damit auch tausende Ereignisse pro Sekunde weder Verzögerungen noch Ruckler
 * erzeugen. Die Ausgabe läuft in einem eigenen Thread über Java Sound; fällt das Ausgabegerät weg,
 * versucht der Thread es erneut.
 */
public final class MusicEngine implements AutoCloseable {
    private static final int BLOCK = 512;
    private static final double CEILING = .89;

    private final Map<String, Song> songs;
    private final List<SongPlayer> fading = new ArrayList<>(4);
    private final double[] left = new double[BLOCK], right = new double[BLOCK];
    private final double[] effectsL = new double[BLOCK], effectsR = new double[BLOCK];
    private final SoundMixer effects = new SoundMixer();
    private final Svf highL = new Svf(), highR = new Svf(), lowL = new Svf(), lowR = new Svf();
    private volatile String cue = "title";
    private volatile double intensityTarget = .5, muffleTarget, tempoTarget = 1, volumeTarget = .3;
    private volatile boolean running, ready;
    private SongPlayer current;
    private double intensity = -1, muffle, tempo = 1, volume = -1, limiter = 1, cutoff = 20000;
    private Thread thread;

    private MusicEngine(Map<String, Song> songs) {
        this.songs = songs;
        highL.set(28, .7);
        highR.set(28, .7);
    }

    /**
     * Motor ohne Tonausgabe, etwa für Tests und Aufnahmen.
     *
     * @return Motor, der nur über {@link #render(double[], double[])} klingt
     */
    public static MusicEngine offline() {
        var engine = new MusicEngine(Scores.all());
        engine.effects.bank(SoundBank.load());
        return engine;
    }

    /**
     * Startet die Tonausgabe in einem Hintergrund-Thread. Die Partituren werden dort aufgebaut, das
     * Spiel wartet nicht darauf.
     *
     * @return laufender Motor
     */
    public static MusicEngine start() {
        var engine = new MusicEngine(new java.util.concurrent.ConcurrentHashMap<>());
        engine.running = true;
        engine.thread = new Thread(engine::loop, "abyss-music");
        engine.thread.setDaemon(true);
        engine.thread.setPriority(Thread.MAX_PRIORITY);
        engine.thread.start();
        return engine;
    }

    /**
     * @return Kennungen aller Stücke in Spielreihenfolge
     */
    public static List<String> songIds() {
        return List.copyOf(Scores.all().keySet());
    }

    /**
     * @return Anzahl lesbarer Klangdateien (für die Oberflächenprüfung)
     */
    public static int soundCount() {
        return SoundBank.load().loaded();
    }

    /**
     * @param id Stückkennung; unbekannte Kennungen werden ignoriert
     */
    public void cue(String id) {
        if (id != null) cue = id;
    }

    /**
     * @param value Intensität 0 (Ruhe) bis 1 (volle Besetzung)
     */
    public void intensity(double value) {
        intensityTarget = Math.max(0, Math.min(1, value));
    }

    /**
     * @param value Unterwasserdämpfung 0 (klar) bis 1 (stark gedämpft)
     */
    public void muffle(double value) {
        muffleTarget = Math.max(0, Math.min(1, value));
    }

    /**
     * @param factor Tempofaktor, 1 ist das notierte Tempo
     */
    public void tempo(double factor) {
        tempoTarget = Math.max(.5, Math.min(1.5, factor));
    }

    /**
     * @param value Lautstärke 0 bis 1
     */
    public void volume(double value) {
        volumeTarget = Math.max(0, Math.min(1, value));
    }

    /**
     * Spielt einen Klang über den Effektmischer. Unabhängig von Musiklautstärke und Dämpfung.
     *
     * @param name Klangname wie {@code "hit"}
     * @param level Lautstärke 0 bis 1
     * @return {@code false}, wenn der Name unbekannt ist
     */
    public boolean sound(String name, double level) {
        int id = SoundBank.id(name);
        if (id < 0) return false;
        effects.trigger(id, Math.max(0, Math.min(1, level)));
        return true;
    }

    /**
     * @param level Lautstärke der Unterwasser-Atmosphäre, 0 schaltet sie ab
     */
    public void ambience(double level) {
        effects.ambience(Math.max(0, Math.min(1, level)));
    }

    /**
     * @return {@code true}, solange die Tonausgabe läuft
     */
    public boolean ready() {
        return ready;
    }

    /**
     * @return gerade gespieltes Stück oder {@code null}
     */
    public String playing() {
        var player = current;
        return player == null ? null : player.song.id;
    }

    /**
     * Rendert so viele Stereo-Abtastwerte, wie die Puffer lang sind (44,1 kHz).
     *
     * @param outL linker Kanal, wird überschrieben
     * @param outR rechter Kanal, wird überschrieben
     */
    public void render(double[] outL, double[] outR) {
        for (int offset = 0; offset < outL.length; offset += BLOCK) {
            int n = Math.min(BLOCK, outL.length - offset);
            block(n);
            System.arraycopy(left, 0, outL, offset, n);
            System.arraycopy(right, 0, outR, offset, n);
        }
    }

    private void block(int n) {
        java.util.Arrays.fill(left, 0, n, 0);
        java.util.Arrays.fill(right, 0, n, 0);
        java.util.Arrays.fill(effectsL, 0, n, 0);
        java.util.Arrays.fill(effectsR, 0, n, 0);
        double seconds = n * Dsp.DT;
        String wanted = cue;
        var song = songs.get(wanted);
        if (song != null && (current == null || !current.song.id.equals(wanted))) {
            boolean first = current == null;
            if (current != null) {
                current.fade(0, 2.2);
                if (fading.size() >= 3) fading.removeFirst();
                fading.add(current);
            }
            current = new SongPlayer(song, BLOCK);
            current.fade(1, first ? 0 : 1.4);
        }
        double target = intensityTarget;
        if (intensity < 0) intensity = target;
        double tau = target > intensity ? .5 : 3.5;
        intensity += (target - intensity) * (1 - Math.exp(-seconds / tau));
        tempo += (tempoTarget - tempo) * (1 - Math.exp(-seconds / 1.5));
        muffle += (muffleTarget - muffle) * (1 - Math.exp(-seconds / .25));
        if (volume < 0) volume = volumeTarget;
        volume += (volumeTarget - volume) * (1 - Math.exp(-seconds / .15));
        // Stumm geschaltete Musik kostet nichts; die Klänge laufen trotzdem weiter.
        if (volumeTarget > 0 || volume > 1e-4) {
            if (current != null) {
                current.tempo = tempo;
                current.render(left, right, n, intensity);
            }
            for (int i = fading.size() - 1; i >= 0; i--) {
                var player = fading.get(i);
                player.render(left, right, n, intensity);
                if (player.silent()) fading.remove(i);
            }
        }
        effects.render(effectsL, effectsR, n);
        master(n);
    }

    private void master(int n) {
        double wanted = 20000 * Math.pow(500 / 20000.0, muffle);
        cutoff += (wanted - cutoff) * .3;
        boolean filtered = cutoff < 17000;
        if (filtered) {
            lowL.set(cutoff, .8);
            lowR.set(cutoff, .8);
        }
        double gain = volume * (1 - .3 * muffle);
        double attack = Dsp.smoothing(.0015, 1), release = Dsp.smoothing(.25, 1);
        for (int s = 0; s < n; s++) {
            highL.process(left[s]);
            highR.process(right[s]);
            double l = highL.high, r = highR.high;
            if (filtered) {
                l = lowL.process(l);
                r = lowR.process(r);
            }
            l = l * gain + effectsL[s];
            r = r * gain + effectsR[s];
            double peak = Math.max(Math.abs(l), Math.abs(r));
            double wantedGain = peak * limiter > CEILING ? CEILING / peak : 1;
            limiter += (wantedGain - limiter) * (wantedGain < limiter ? attack : release);
            left[s] = clip(l * limiter);
            right[s] = clip(r * limiter);
        }
    }

    private static double clip(double x) {
        double a = Math.abs(x);
        if (a <= .8) return x;
        return Math.signum(x) * (.8 + .2 * Math.tanh((a - .8) / .2));
    }

    private void loop() {
        SoundBank bank;
        try {
            songs.putAll(Scores.all());
            bank = SoundBank.load();
            effects.bank(bank);
        } catch (RuntimeException error) {
            System.err.println("Bordaudio nicht verfügbar: " + error.getMessage());
            running = false;
            return;
        }
        System.out.println(
                "ABYSS_AUDIO_READY " + songs.size() + " Stücke, " + bank.loaded() + " Klänge");
        var format = new AudioFormat(Dsp.RATE, 16, 2, true, false);
        byte[] bytes = new byte[BLOCK * 4];
        boolean reported = false;
        while (running) {
            SourceDataLine line = null;
            try {
                line = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
                line.open(format, BLOCK * 4 * 12);
                line.start();
                ready = true;
                System.out.println("ABYSS_MUSIC_READY Tonausgabe offen");
                while (running) {
                    block(BLOCK);
                    for (int s = 0, b = 0; s < BLOCK; s++) {
                        int l = (int) Math.round(left[s] * 32767);
                        int r = (int) Math.round(right[s] * 32767);
                        bytes[b++] = (byte) l;
                        bytes[b++] = (byte) (l >> 8);
                        bytes[b++] = (byte) r;
                        bytes[b++] = (byte) (r >> 8);
                    }
                    line.write(bytes, 0, bytes.length);
                }
            } catch (Exception | LinkageError error) {
                if (!reported) System.err.println("Tonausgabe unterbrochen: " + error.getMessage());
                reported = true;
            } finally {
                ready = false;
                if (line != null) {
                    line.stop();
                    line.close();
                }
            }
            // Ausgabegerät weg (etwa Kopfhörer gezogen): nach einer Pause neu versuchen.
            for (int wait = 0; wait < 20 && running; wait++)
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
        }
    }

    /** Hält die Tonausgabe an und gibt das Ausgabegerät frei. */
    @Override
    public void close() {
        running = false;
        if (thread != null)
            try {
                thread.join(400);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
    }
}
