package ch.zhaw.abyss.infrastructure;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.GameEvent;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RoomPlan;

import javafx.scene.media.AudioClip;

import java.util.HashMap;
import java.util.Map;

/** Audio-Ausfall darf den Spielstart nicht verhindern (z.B. kein Ausgabegerät). */
public final class AudioSystem implements AutoCloseable {
    private final Map<String, AudioClip> clips = new HashMap<>();
    private final Map<String, Long> lastPlayed = new HashMap<>();
    private Settings settings = Settings.DEFAULT;
    private boolean enabled = true;
    private AudioClip ambience, music;
    private String musicKey = "music";

    public AudioSystem() {
        if (Boolean.getBoolean("abyss.silent")) {
            enabled = false;
            return;
        }
        try {
            ambience = load("ambience");
            music = load("music");
            if (ambience != null) ambience.setCycleCount(AudioClip.INDEFINITE);
            if (music != null) music.setCycleCount(AudioClip.INDEFINITE);
            System.out.println("ABYSS_AUDIO_READY native clips loaded");
        } catch (RuntimeException error) {
            enabled = false;
            System.err.println("Audio unavailable: " + error.getMessage());
        }
    }

    public void settings(Settings value) {
        settings = value;
        if (ambience != null) ambience.setVolume(settings.masterVolume() * .45);
        if (music != null) music.setVolume(settings.masterVolume() * settings.musicVolume() * .6);
    }

    public void start() {
        if (!enabled) return;
        try {
            if (ambience != null && !ambience.isPlaying())
                ambience.play(settings.masterVolume() * .45);
            if (music != null && !music.isPlaying())
                music.play(settings.masterVolume() * settings.musicVolume() * .6);
        } catch (RuntimeException error) {
            enabled = false;
        }
    }

    /** Wechsel erfolgt nur beim Kontextwechsel, nie bei jedem Renderframe. */
    public void context(GameRun run, boolean title) {
        if (!enabled) return;
        String next =
                title || run == null
                        ? "music"
                        : (run.room().kind() == RoomPlan.Kind.BOSS
                                                || run.room().kind() == RoomPlan.Kind.BRIDGE)
                                        && run.phase() == GameRun.Phase.RUNNING
                                ? "music_boss"
                                : run.room().sector() == 1
                                        ? "music_engine"
                                        : run.room().sector() == 2 ? "music_command" : "music";
        if (next.equals(musicKey)) return;
        try {
            if (music != null) music.stop();
            music = load(next);
            musicKey = next;
            if (music != null) {
                music.setCycleCount(AudioClip.INDEFINITE);
                music.play(settings.masterVolume() * settings.musicVolume() * .6);
            }
        } catch (RuntimeException error) {
            System.err.println("Music unavailable: " + next);
        }
    }

    private AudioClip load(String name) {
        return clips.computeIfAbsent(
                name,
                key -> {
                    var url = AudioSystem.class.getResource("/audio/" + key + ".wav");
                    return url == null ? null : new AudioClip(url.toExternalForm());
                });
    }

    public void play(String name) {
        if (!enabled || settings.masterVolume() <= 0) return;
        long now = System.nanoTime();
        if (now - lastPlayed.getOrDefault(name, 0L) < 60_000_000L) return;
        lastPlayed.put(name, now);
        try {
            var clip = load(name);
            if (clip != null)
                clip.play(settings.masterVolume() * (name.equals("warning") ? .55 : .7));
        } catch (RuntimeException error) {
            System.err.println("Sound unavailable: " + name);
        }
    }

    public void event(GameEvent event) {
        String name =
                switch (event.type()) {
                    case SWING -> "swing";
                    case HIT -> "hit";
                    case PLAYER_HIT -> "hurt";
                    case ENEMY_DOWN -> "down";
                    case DASH -> "dash";
                    case JUMP -> "jump";
                    case PULSE, SHIELD, ARC -> "pulse";
                    case SHOT -> "shot";
                    case TELEGRAPH -> "warning";
                    case ROOM_CLEAR -> "clear";
                    case UPGRADE, HEAL, SUPPLY -> "upgrade";
                    case DEFEAT -> "defeat";
                    case VICTORY -> "victory";
                    case BOSS_PHASE -> "pulse";
                    case REINFORCEMENTS -> "warning";
                };
        play(name);
    }

    @Override
    public void close() {
        clips.values().forEach(AudioClip::stop);
    }
}
