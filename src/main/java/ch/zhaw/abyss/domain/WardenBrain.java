package ch.zhaw.abyss.domain;

/**
 * Der Schottmeister, Wächter der Hecksektion: Ansturm gegen die Wand, Bodenstampfer, später
 * Verstärkung und Sprungangriffe. Nach einem Ansturm in die Wand ist sein Kern lange offen.
 */
final class WardenBrain extends Brain {
    static final int CHARGE = 0, SLAM = 1, SUMMON = 2, LEAP = 3;
    private static final int[][] CYCLES = {
        {CHARGE, SLAM, CHARGE, SLAM},
        {CHARGE, SLAM, SUMMON, LEAP},
        {LEAP, CHARGE, SLAM, SUMMON, LEAP}
    };

    @Override
    void approach(Enemy e, GameRun run, double dt) {
        close(e, run, 280);
    }

    @Override
    boolean ready(Enemy e, GameRun run) {
        return true;
    }

    @Override
    void choose(Enemy e, GameRun run) {
        var cycle = CYCLES[e.phase];
        e.pattern = cycle[e.attacks % cycle.length];
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case CHARGE -> e.enraged ? .8 : .95;
            case SLAM -> .8;
            case SUMMON -> .7;
            default -> .6;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        double width = run.layout().width();
        return switch (e.pattern) {
            case CHARGE ->
                    new Telegraph(
                            Telegraph.Shape.FLOOR, e.x, e.y, e.facing > 0 ? width - 50 : 50, e.y);
            case SLAM -> new Telegraph(Telegraph.Shape.FLOOR, 60, GameRun.FLOOR, width - 60, 0);
            case LEAP ->
                    new Telegraph(Telegraph.Shape.CIRCLE, e.targetX, GameRun.FLOOR - 20, 170, 0);
            default -> null;
        };
    }

    @Override
    double strikeTime(Enemy e) {
        return switch (e.pattern) {
            case CHARGE -> 3;
            case LEAP -> 2.5;
            default -> .3;
        };
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        switch (e.pattern) {
            case SLAM -> slam(e, run, 17);
            case SUMMON -> {
                if (minions(e, run) < 3) {
                    run.summon(EnemyKind.SCUTTLER, 120, GameRun.FLOOR, e);
                    run.summon(EnemyKind.SCUTTLER, run.layout().width() - 120, GameRun.FLOOR, e);
                }
            }
            case LEAP -> {
                e.vy = -1050;
                e.grounded = false;
                e.homeX = GameRun.clamp((e.targetX - e.x) / .95, -700, 700);
            }
            default -> {}
        }
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        switch (e.pattern) {
            case CHARGE -> {
                e.vx = e.facing * (e.enraged ? 720 : 640);
                melee(e, run, 90, 150, 20);
                if (atWall(e, run)) {
                    run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 120, ""));
                    e.stateTime = 0;
                }
            }
            case LEAP -> {
                e.vx = e.homeX;
                if (e.grounded && e.stateTime < 2.3) {
                    slam(e, run, 20);
                    e.stateTime = 0;
                }
            }
            default -> {}
        }
    }

    private static void slam(Enemy e, GameRun run, double damage) {
        run.shockwaves(e.x, damage * run.enemyDamage(), e.enraged ? 520 : 430);
        run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 170, ""));
        if (Math.abs(dx(e, run)) < 170 && run.player.grounded)
            run.combat.hurtPlayer(18 * run.enemyDamage(), e.x, e, true);
    }

    @Override
    double recoverTime(Enemy e) {
        return switch (e.pattern) {
            case CHARGE -> 2.2;
            case SLAM -> 1.5;
            case SUMMON -> .9;
            default -> 1.6;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .4;
    }
}
