package ch.zhaw.abyss.infrastructure.music;

import ch.zhaw.abyss.domain.Enemy;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Threat;

/**
 * Musikregie: wählt aus dem Spielzustand Stück, Intensität, Dämpfung und Tempo. Rein lesend und
 * ohne Ton, daher ohne Ausgabegerät testbar.
 */
public final class MusicDirector {
    /**
     * Musikalische Anweisung für einen Bildschirmmoment.
     *
     * @param song Stückkennung
     * @param intensity 0 Ruhe bis 1 volle Besetzung
     * @param muffle Unterwasserdämpfung 0 bis 1
     * @param tempo Tempofaktor
     */
    public record Cue(String song, double intensity, double muffle, double tempo) {}

    private static final String[] SECTORS = {"hold", "engine", "research", "command"};

    private MusicDirector() {}

    /**
     * @param run laufender Tauchgang oder {@code null}
     * @param title Titel und Menüs ausserhalb eines Tauchgangs
     * @param overlay Dämpfung durch einen Bordsystem-Bildschirm über dem Spiel, 0 bis 1
     * @return passende Anweisung
     */
    public static Cue cue(GameRun run, boolean title, double overlay) {
        if (title || run == null) return new Cue("title", 1, 0, 1);
        var phase = run.phase();
        if (phase == GameRun.Phase.VICTORY) return new Cue("ending", 1, 0, 1);
        var room = run.room();
        String sector = SECTORS[Math.max(0, Math.min(SECTORS.length - 1, room.sector()))];
        if (phase == GameRun.Phase.DEFEAT) return new Cue(song(run, sector), 0, .85, .97);
        double muffle = Math.max(0, Math.min(1, overlay));
        var player = run.player();
        double health = player.maxHealth() > 0 ? player.health() / player.maxHealth() : 1;
        if (health < .35) muffle = Math.max(muffle, (.35 - health) / .35 * .5);
        String song = song(run, sector);
        double intensity;
        double tempo = 1;
        var boss = run.boss();
        if (boss != null && phase == GameRun.Phase.RUNNING) {
            intensity =
                    boss.furious() ? 1 : new double[] {.62, .82, .97}[Math.min(2, boss.phase())];
            if (boss.kind() == EnemyKind.EMPRESS && boss.furious()) tempo = 1.06;
        } else
            switch (room.kind()) {
                case CACHE, MERCHANT, SHRINE, WORKSHOP -> intensity = 1;
                default -> intensity = combat(run, phase);
            }
        return new Cue(song, intensity, muffle, tempo);
    }

    private static String song(GameRun run, String sector) {
        var room = run.room();
        var boss = run.boss();
        if (boss != null && run.phase() == GameRun.Phase.RUNNING)
            return boss.kind() == EnemyKind.EMPRESS ? "empress" : "boss";
        return switch (room.kind()) {
            case CACHE, MERCHANT, SHRINE, WORKSHOP -> "haven";
            case COMBAT, ELITE -> run.escalation() > 0 ? "abyss" : sector;
            default -> sector;
        };
    }

    /** Ruhe nach dem Raum, sonst wächst die Intensität logarithmisch mit dem Schwarm. */
    private static double combat(GameRun run, GameRun.Phase phase) {
        if (phase != GameRun.Phase.RUNNING) return .3;
        int alive = 0;
        for (Enemy enemy : run.enemies()) if (enemy.alive() && !enemy.kind().boss()) alive++;
        if (alive == 0) return .45;
        double full = run.escalation() > 0 ? 500 : 120;
        double value = .5 + .5 * Math.min(1, Math.log1p(alive) / Math.log1p(full));
        if (run.room().threat() != Threat.NONE || run.room().kind() == RoomPlan.Kind.ELITE)
            value += .08;
        return Math.min(1, value);
    }
}
