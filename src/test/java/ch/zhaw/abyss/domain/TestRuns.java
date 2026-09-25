package ch.zhaw.abyss.domain;

import java.util.EnumSet;

/** Gemeinsame Hilfen für Domänentests: Standardläufe, leere Räume und Zeitraffer. */
final class TestRuns {
    static final double STEP = 1.0 / 120;

    private TestRuns() {}

    static GameRun standard() {
        return new GameRun(RunSetup.standard(73419, DiverClass.MECHANIC));
    }

    static GameRun of(DiverClass diver) {
        return new GameRun(RunSetup.standard(73419, diver));
    }

    /** Lauf mit allen Modulen im Pool, damit legendäre Angebote prüfbar sind. */
    static GameRun allItems() {
        var items = EnumSet.noneOf(Item.class);
        for (var item : Item.lootable()) items.add(item);
        return new GameRun(
                new RunSetup(
                        99,
                        DiverClass.MECHANIC,
                        Weapon.WRENCH,
                        ActiveModule.PULSE,
                        false,
                        0,
                        items,
                        EnumSet.allOf(Weapon.class),
                        0,
                        0,
                        0));
    }

    /** Entfernt alle Gegner und Gefahren, ohne den Raum zu sichern. */
    static void empty(GameRun run) {
        run.enemies.clear();
        run.projectiles.clear();
        run.hazards.clear();
        run.crates.clear();
        run.player.invulnerableTime = 0;
    }

    static Enemy place(GameRun run, EnemyKind kind, double x) {
        var enemy =
                new Enemy(
                        900 + run.enemies.size(),
                        kind,
                        Affix.NONE,
                        x,
                        kind.flying() ? GameRun.FLOOR - 120 : GameRun.FLOOR,
                        1,
                        99);
        enemy.state = Enemy.State.APPROACH;
        run.enemies.add(enemy);
        return enemy;
    }

    static void tick(GameRun run, InputFrame input, int steps) {
        for (int i = 0; i < steps; i++) run.update(STEP, input);
    }

    static void seconds(GameRun run, InputFrame input, double seconds) {
        tick(run, input, (int) Math.round(seconds * 120));
    }

    /** Besiegt alle Wellen des aktuellen Raums ohne Eingaben. */
    static void clear(GameRun run) {
        for (int tick = 0; tick < 120 * 30 && run.phase() == GameRun.Phase.RUNNING; tick++) {
            for (var enemy : run.enemies) enemy.health = 0;
            run.update(STEP, InputFrame.NONE);
        }
    }
}
