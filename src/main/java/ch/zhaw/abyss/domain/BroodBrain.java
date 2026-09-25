package ch.zhaw.abyss.domain;

/**
 * Die Brutmutter, Wächterin des Forschungsdecks: ein schwimmendes Tiefseewesen mit Leuchtköder.
 * Stösst auf die Figur herab, spuckt Säure, ruft Quallen und stürzt sich später von oben herab.
 */
final class BroodBrain extends Brain {
    static final int LUNGE = 0, SPIT = 1, SPAWN = 2, DIVE = 3;
    static final double HOVER = GameRun.FLOOR - 230;
    private static final int[][] CYCLES = {
        {SPIT, LUNGE, SPIT, LUNGE},
        {SPIT, LUNGE, SPAWN, LUNGE},
        {DIVE, SPIT, LUNGE, SPAWN, DIVE, LUNGE}
    };

    @Override
    void idle(Enemy e, GameRun run, double dt) {
        flyTo(e, e.x, HOVER + Math.sin(run.elapsed() * 1.3) * 20, 90);
    }

    @Override
    void approach(Enemy e, GameRun run, double dt) {
        face(e, run);
        double side = e.x < run.player.x ? -270 : 270;
        flyTo(e, run.player.x + side, HOVER + Math.sin(run.elapsed() * 1.3) * 20, speed(e));
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
            case LUNGE -> .9;
            case SPIT -> .7;
            case SPAWN -> .9;
            default -> 1.1;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        return switch (e.pattern) {
            case LUNGE, SPIT ->
                    new Telegraph(Telegraph.Shape.AIM, e.x, e.centerY(), e.targetX, e.targetY);
            case DIVE -> new Telegraph(Telegraph.Shape.COLUMN, e.targetX, 0, 190, 0);
            default -> null;
        };
    }

    @Override
    void windup(Enemy e, GameRun run, double dt) {
        if (e.pattern == DIVE) flyTo(e, e.targetX, e.height + 40, 520);
        else idle(e, run, dt);
    }

    @Override
    double strikeTime(Enemy e) {
        return switch (e.pattern) {
            case LUNGE -> .6;
            case DIVE -> 2;
            default -> .25;
        };
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        switch (e.pattern) {
            case LUNGE -> {
                double ddx = e.targetX - e.x, ddy = e.targetY - e.centerY();
                double length = Math.max(1, Math.hypot(ddx, ddy));
                e.vx = ddx / length * 900;
                e.vy = ddy / length * 900;
            }
            case SPIT -> {
                for (int i = -1; i <= 1; i++) {
                    var acid =
                            run.shoot(
                                    Projectile.Kind.ACID,
                                    false,
                                    e.x + e.facing * 70,
                                    e.centerY(),
                                    (e.targetX - e.x) / .9 + i * 140,
                                    -480,
                                    10 * run.enemyDamage(),
                                    14,
                                    5);
                    acid.gravity = 1500;
                }
                run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, e.centerY()));
            }
            case SPAWN -> {
                if (minions(e, run) < 3) {
                    run.summon(EnemyKind.JELLY, e.x - 160, GameRun.FLOOR - 160, e);
                    run.summon(EnemyKind.JELLY, e.x + 160, GameRun.FLOOR - 160, e);
                }
            }
            default -> {
                e.x = e.targetX;
                e.vx = 0;
                e.vy = 1500;
            }
        }
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        switch (e.pattern) {
            case LUNGE -> {
                contact(e, run, 20);
                if (e.y >= GameRun.FLOOR - 1) {
                    e.vy = 0;
                    e.vx *= .8;
                }
            }
            case DIVE -> {
                e.vy = 1500;
                if (e.y >= GameRun.FLOOR - 1) {
                    e.vy = 0;
                    run.shockwaves(e.x, 18 * run.enemyDamage(), 480);
                    run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, GameRun.FLOOR, 190, ""));
                    if (Math.abs(dx(e, run)) < 190)
                        run.combat.hurtPlayer(20 * run.enemyDamage(), e.x, e, true);
                    e.stateTime = 0;
                }
            }
            default -> {}
        }
    }

    @Override
    void strikeEnd(Enemy e, GameRun run) {
        e.vx = 0;
        e.vy = 0;
    }

    @Override
    void recover(Enemy e, GameRun run, double dt) {
        if (e.pattern == LUNGE || e.pattern == DIVE) flyTo(e, e.x, GameRun.FLOOR, 220);
        else idle(e, run, dt);
    }

    @Override
    double recoverTime(Enemy e) {
        return switch (e.pattern) {
            case LUNGE -> 1.6;
            case DIVE -> 1.8;
            default -> .8;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .4;
    }
}
