package ch.zhaw.abyss.infrastructure;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.GameEvent;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.infrastructure.music.MusicDirector;
import ch.zhaw.abyss.infrastructure.music.MusicEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Übersetzt Spielereignisse in Klänge und den Spielzustand in Musik. Beides spielt das Bordaudio
 * ({@link MusicEngine}) in einem eigenen Thread mit fester Stimmenzahl. Bis 1.6 liefen Klänge über
 * JavaFX-{@code AudioClip}s; deren unbegrenzte Warteschlange liess im Endgame den Ton verstummen
 * und den Spiel-Thread ruckeln. Audio-Ausfall darf den Spielstart nicht verhindern.
 */
public final class AudioSystem implements AutoCloseable {
    private final Map<String, Long> lastPlayed = new HashMap<>();
    private Settings settings = Settings.DEFAULT;
    private boolean enabled = !Boolean.getBoolean("abyss.silent");
    private MusicEngine music;

    /**
     * @param value aktuelle Einstellungen (Lautstärken)
     */
    public void settings(Settings value) {
        settings = value;
        if (music != null) {
            music.volume(musicLevel());
            music.ambience(settings.masterVolume() * .3);
        }
    }

    private double musicLevel() {
        return settings.masterVolume() * settings.musicVolume();
    }

    /** Startet das Bordaudio (einmalig). */
    public void start() {
        if (!enabled || music != null) return;
        try {
            music = MusicEngine.start();
            settings(settings);
        } catch (RuntimeException error) {
            enabled = false;
            System.err.println("Audio nicht verfügbar: " + error.getMessage());
        }
    }

    /**
     * Übergibt der Musikregie den aktuellen Spielzustand; wird jedes Bild aufgerufen und ist
     * billig.
     *
     * @param run laufender Tauchgang oder {@code null}
     * @param title Titel und Menüs ausserhalb eines Tauchgangs
     * @param overlay Dämpfung durch einen Bildschirm über dem Spiel (0 ohne)
     */
    public void context(GameRun run, boolean title, double overlay) {
        if (!enabled || music == null) return;
        var cue = MusicDirector.cue(run, title, overlay);
        music.cue(cue.song());
        music.intensity(cue.intensity());
        music.muffle(cue.muffle());
        music.tempo(cue.tempo());
    }

    private static String fallback(String name) {
        return switch (name) {
            case "crit", "block", "crate", "land" -> "hit";
            case "zap", "freeze", "spawn", "core", "curse" -> "pulse";
            case "harpoon" -> "shot";
            case "explosion", "roar", "door" -> "down";
            case "pickup" -> "click";
            default -> "click";
        };
    }

    /**
     * Spielt einen Klang, höchstens alle 60 ms pro Name.
     *
     * @param name Dateiname ohne Endung
     */
    public void play(String name) {
        if (!enabled || music == null || settings.masterVolume() <= 0) return;
        long now = System.nanoTime();
        if (now - lastPlayed.getOrDefault(name, 0L) < 60_000_000L) return;
        lastPlayed.put(name, now);
        double level =
                settings.masterVolume()
                        * (name.equals("warning") || name.equals("pickup") ? .45 : .7);
        if (!music.sound(name, level)) music.sound(fallback(name), level);
    }

    /**
     * Spielt den passenden Klang zu einem Domänenereignis.
     *
     * @param event Ereignis
     */
    public void event(GameEvent event) {
        String name =
                switch (event.type()) {
                    case SWING -> "swing";
                    case HIT -> "BURN".equals(event.text()) ? null : "hit";
                    case CRIT -> "crit";
                    case PLAYER_HIT -> "hurt";
                    case SHIELD_HIT, BLOCK -> "block";
                    case ENEMY_DOWN -> "down";
                    case ELITE_DOWN, BOSS_DOWN -> "explosion";
                    case DASH -> "dash";
                    case JUMP, AIR_JUMP -> "jump";
                    case LAND -> "land";
                    case PULSE, SHIELD, SONAR, OVERDRIVE -> "pulse";
                    case ARC, CHAIN, SHOCK -> "zap";
                    case SHOT, TORPEDO, FLAME -> "shot";
                    case HARPOON -> "harpoon";
                    case EXPLOSION, SLAM -> "explosion";
                    case FREEZE -> "freeze";
                    case DRONE -> "spawn";
                    case SPAWN -> "swarm".equals(event.text()) ? null : "spawn";
                    case LEVEL_UP -> "upgrade";
                    case NOVA -> "explosion";
                    case TELEGRAPH, REINFORCEMENTS -> "warning";
                    case ROOM_CLEAR -> "clear";
                    case UPGRADE, WEAPON, HEAL, SUPPLY, PURCHASE, SYNERGY -> "upgrade";
                    case CURSE -> "curse";
                    case DEFEAT -> "defeat";
                    case VICTORY -> "victory";
                    case REVIVE, BOSS_PHASE -> "roar";
                    case BOSS_INTRO -> "roar";
                    case CRATE_BREAK -> "crate";
                    case PICKUP -> "pickup";
                    case CORE -> "core";
                    case DOOR -> "door";
                    case THREAT, ESCALATION -> "warning";
                    case EVOLUTION -> "victory";
                    case CONDITION -> "ALARM".equals(event.text()) ? "warning" : "down";
                    case MACHINE ->
                            switch (event.text()) {
                                case "VENT" -> "jump";
                                case "LASER" -> "zap";
                                case "STEAM" -> "explosion";
                                case "ESCAPE", "FLEE" -> "dash";
                                case "CAUGHT" -> "core";
                                case "SEALED" -> "clear";
                                case "LIST_WARN" -> "warning";
                                case "LIST" -> "down";
                                case "EXPOSED", "VENTED" -> "crit";
                                case "MELTDOWN", "FURY", "INK", "BOARDING" -> "roar";
                                default -> "pulse";
                            };
                };
        if (name != null) play(name);
    }

    @Override
    public void close() {
        if (music != null) music.close();
    }
}
