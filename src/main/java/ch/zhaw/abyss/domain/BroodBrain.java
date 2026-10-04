package ch.zhaw.abyss.domain;

/**
 * Die Brutmutter, Wächterin des Forschungsdecks: ein schwimmendes Tiefseewesen mit Leuchtköder.
 * Stösst herab, spuckt Säure und legt Eier, die zu Milben schlüpfen, wenn man sie nicht rechtzeitig
 * zerschlägt. Später hüllt sie die Arena in Tinte, in der nur ihr Köder leuchtet, zieht die Figur
 * mit einem Sog heran und stürzt sich von oben herab. Ab dem zweiten Zyklus entfesselt:
 * Leuchtsporenringe und Fischzüge auf drei Höhen.
 */
final class BroodBrain extends Brain {
    static final int LUNGE = 0, SPIT = 1, SPAWN = 2, DIVE = 3, EGGS = 4, INK = 5, WHIRL = 6;
    static final int SPORES = 7, SCHOOL = 8;
    static final double HOVER = GameRun.FLOOR - 230;

    /** Dauer der Tintenwolke. */
    static final double INK_TIME = 6.5;

    private static final int[][] CYCLES = {
        {SPIT, LUNGE, EGGS, SPIT, LUNGE},
        {INK, LUNGE, LUNGE, SPAWN, WHIRL, SPIT, EGGS},
        {DIVE, WHIRL, INK, LUNGE, LUNGE, DIVE, EGGS, SPIT}
    };
    private static final int[][] UNLEASHED = {
        {SPIT, SPORES, LUNGE, EGGS, SCHOOL},
        {INK, LUNGE, SPORES, LUNGE, WHIRL, SCHOOL, EGGS},
        {DIVE, SCHOOL, WHIRL, SPORES, INK, LUNGE, LUNGE, DIVE, EGGS}
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
        var cycle = (run.cycle() > 0 ? UNLEASHED : CYCLES)[e.phase];
        e.pattern = cycle[e.attacks % cycle.length];
        if (e.pattern == INK) run.ink = INK_TIME;
        if (e.pattern == SCHOOL) school(e, run);
    }

    /**
     * Fischzug: Reihen leuchtender Fische schiessen auf drei Höhen durch die Arena. Eine Höhe
     * bleibt frei; die nächste Reihe kommt von der anderen Seite mit einer anderen Lücke.
     */
    private static void school(Enemy e, GameRun run) {
        double width = run.layout().width();
        double[] heights = {GameRun.FLOOR - 40, GameRun.FLOOR - 190, GameRun.FLOOR - 340};
        int first = run.rng.nextInt(3);
        int second = (first + 1 + run.rng.nextInt(2)) % 3;
        for (int round = 0; round < 2; round++) {
            boolean fromLeft = (round == 0) == (run.player.x > width / 2);
            int gap = round == 0 ? first : second;
            for (int row = 0; row < 3; row++) {
                if (row == gap) continue;
                for (int k = 0; k < 3; k++)
                    run.lance(
                            new Lance(
                                    fromLeft ? -40 - k * 70 : width + 40 + k * 70,
                                    heights[row] + (k - 1) * 22,
                                    fromLeft ? 0 : Math.PI,
                                    1150,
                                    12 * run.enemyDamage(),
                                    1.1 + round * 1.1,
                                    Lance.Style.FISH,
                                    .5));
            }
        }
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case LUNGE -> e.phase >= 1 ? .65 : .9;
            case SPIT -> .7;
            case SPAWN, EGGS -> .9;
            case INK -> .8;
            case WHIRL -> .7;
            case SPORES -> .8;
            case SCHOOL -> .6;
            default -> 1.1;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        return switch (e.pattern) {
            case LUNGE, SPIT ->
                    new Telegraph(Telegraph.Shape.AIM, e.x, e.centerY(), e.targetX, e.targetY);
            case DIVE -> new Telegraph(Telegraph.Shape.COLUMN, e.targetX, 0, 190, 0);
            case WHIRL -> new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.centerY(), 260, 0);
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
            case WHIRL -> 2.4;
            case SPORES -> 1.4;
            default -> .25;
        };
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        e.clock = 0;
        switch (e.pattern) {
            case LUNGE -> {
                double ddx = e.targetX - e.x, ddy = e.targetY - e.centerY();
                double length = Math.max(1, Math.hypot(ddx, ddy));
                e.vx = ddx / length * 900;
                e.vy = ddy / length * 900;
            }
            case SPIT -> spit(e, run, e.phase >= 1 ? 2 : 1);
            case SPAWN -> {
                if (minions(e, run) < 3) {
                    var kind = run.cycle() > 0 ? EnemyKind.PRISM : EnemyKind.JELLY;
                    run.summon(kind, e.x - 160, GameRun.FLOOR - 160, e);
                    run.summon(kind, e.x + 160, GameRun.FLOOR - 160, e);
                }
            }
            case EGGS -> {
                // Eierregen: die Eier fallen aus dem Körper und schlüpfen nach kurzer Zeit.
                int count = 3 + e.phase;
                for (int i = 0; i < count; i++) {
                    double x = e.x + (i - (count - 1) / 2.0) * 120;
                    var egg = run.summon(EnemyKind.EGG, x, e.centerY(), e);
                    egg.vy = -200;
                    egg.grounded = false;
                }
                run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, e.centerY()));
            }
            case INK -> run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "INK"));
            case WHIRL -> run.pull(e.x, 210, strikeTime(e));
            case SPORES -> {}
            case SCHOOL -> {}
            default -> {
                e.x = e.targetX;
                e.vx = 0;
                e.vy = 1500;
            }
        }
    }

    /** Säurefontäne: Klumpen im Bogen, in späteren Phasen eine zweite, weitere Salve. */
    private static void spit(Enemy e, GameRun run, int volleys) {
        for (int v = 0; v < volleys; v++)
            for (int i = -1; i <= 1; i++) {
                var acid =
                        run.shoot(
                                Projectile.Kind.ACID,
                                false,
                                e.x + e.facing * 70,
                                e.centerY(),
                                (e.targetX - e.x) / (.9 + v * .25) + i * 140,
                                -480 - v * 140,
                                10 * run.enemyDamage(),
                                14,
                                5);
                acid.gravity = 1500;
            }
        run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, e.centerY()));
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        double before = e.clock;
        e.clock += dt;
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
            case WHIRL -> {
                // Sog mit Säureregen: wer sich nicht dagegenstemmt, landet im Maul.
                if (Math.floor(before / .55) != Math.floor(e.clock / .55)) {
                    e.targetX = run.player.x;
                    spit(e, run, 1);
                }
                if (Math.abs(dx(e, run)) < 80 && Math.abs(run.player.centerY() - e.centerY()) < 140)
                    contact(e, run, 22);
            }
            case SPORES -> {
                if (Math.floor(before / .45) != Math.floor(e.clock / .45)) spores(e, run);
            }
            default -> {}
        }
    }

    /** Leuchtsporen: Ringe aus Prismageschossen aus dem Köder, jeder Ring etwas verdreht. */
    private static void spores(Enemy e, GameRun run) {
        int count = 14;
        double offset = e.clock * 1.7 + e.attacks;
        double lureX = e.x + e.facing * 60, lureY = e.y - e.height - 10;
        for (int k = 0; k < count; k++) {
            if (k % 7 == 3) continue;
            double angle = offset + k * Math.PI * 2 / count;
            var spore =
                    run.shoot(
                            Projectile.Kind.PRISM,
                            false,
                            lureX,
                            lureY,
                            Math.cos(angle) * 210,
                            Math.sin(angle) * 210,
                            8 * run.enemyDamage(),
                            8,
                            5);
            spore.hue = .45 + .15 * Math.sin(angle);
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
            case LUNGE -> quick(e) ? .7 : 1.6;
            case DIVE -> 1.8;
            case EGGS -> 1.1;
            case WHIRL -> 1.2;
            default -> .8;
        };
    }

    /** Ab der zweiten Phase folgen Stösse paarweise dicht aufeinander. */
    private static boolean quick(Enemy e) {
        return e.phase >= 1 && e.attacks % 2 == 1;
    }

    @Override
    double cooldown(Enemy e) {
        return .4;
    }
}
