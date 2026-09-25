package ch.zhaw.abyss.domain;

/**
 * Der Lotse auf der Brücke: Harpunenkanone, Ansturm und Bodenwellen; ruft ab der zweiten Phase
 * Drohnen und lässt in der letzten Phase ein Sperrfeuer auf markierte Säulen niedergehen.
 */
final class CaptainBrain extends Brain {
    static final int HARPOON = 0, CHARGE = 1, WAVE = 2, DRONES = 3, BARRAGE = 4;
    private static final int[][] CYCLES = {
        {HARPOON, CHARGE, HARPOON, WAVE},
        {HARPOON, DRONES, CHARGE, WAVE, HARPOON},
        {BARRAGE, CHARGE, HARPOON, WAVE, BARRAGE, DRONES}
    };

    @Override
    void approach(Enemy e, GameRun run, double dt) {
        keepDistance(e, run, 300, 430);
    }

    @Override
    boolean ready(Enemy e, GameRun run) {
        return true;
    }

    @Override
    void choose(Enemy e, GameRun run) {
        var cycle = CYCLES[e.phase];
        e.pattern = cycle[e.attacks % cycle.length];
        if (e.pattern == BARRAGE)
            for (int k = -2; k <= 2; k++) {
                double x = GameRun.clamp(run.player.x + k * 190, 90, run.layout().width() - 90);
                run.addHazard(
                        new Hazard(x, 110, Hazard.Kind.BARRAGE, -1.05 - Math.abs(k) * .08, 1.6));
            }
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case HARPOON -> e.enraged ? .6 : .75;
            case CHARGE -> .9;
            case WAVE -> .8;
            case DRONES -> .7;
            default -> 1.0;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        double width = run.layout().width();
        return switch (e.pattern) {
            case HARPOON ->
                    new Telegraph(
                            Telegraph.Shape.AIM, e.x, e.y - e.height * .6, e.targetX, e.targetY);
            case CHARGE ->
                    new Telegraph(
                            Telegraph.Shape.FLOOR, e.x, e.y, e.facing > 0 ? width - 50 : 50, e.y);
            case WAVE -> new Telegraph(Telegraph.Shape.FLOOR, 60, GameRun.FLOOR, width - 60, 0);
            default -> null;
        };
    }

    @Override
    double strikeTime(Enemy e) {
        return e.pattern == CHARGE ? 3 : .2;
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        switch (e.pattern) {
            case HARPOON -> {
                double y = e.y - e.height * .6;
                double angle = Math.atan2(e.targetY - y, e.targetX - e.x);
                run.shoot(
                        Projectile.Kind.ENEMY_HARPOON,
                        false,
                        e.x + e.facing * 40,
                        y,
                        Math.cos(angle) * 1150,
                        Math.sin(angle) * 1150,
                        16 * run.enemyDamage(),
                        12,
                        3);
                run.emit(GameEvent.at(GameEvent.Type.HARPOON, e.x, y));
            }
            case WAVE -> {
                run.shockwaves(e.x, 17 * run.enemyDamage(), 500);
                run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 140, ""));
            }
            case DRONES -> {
                if (minions(e, run) < 3) {
                    run.summon(EnemyKind.DRONE, e.x - 200, GameRun.FLOOR - 220, e);
                    run.summon(EnemyKind.DRONE, e.x + 200, GameRun.FLOOR - 220, e);
                }
            }
            default -> {}
        }
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        if (e.pattern != CHARGE) return;
        e.vx = e.facing * 740;
        melee(e, run, 90, 150, 20);
        if (atWall(e, run) || (e.x - e.targetX) * e.facing > 350) e.stateTime = 0;
    }

    @Override
    double recoverTime(Enemy e) {
        return switch (e.pattern) {
            case HARPOON -> .7;
            case CHARGE -> 1.5;
            case WAVE -> 1.2;
            case DRONES -> .8;
            default -> .9;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .35;
    }
}
