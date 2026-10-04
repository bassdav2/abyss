package ch.zhaw.abyss.domain;

/**
 * Der Schottmeister, Wächter der Hecksektion. Ansturm gegen die Wand, Bodenstampfer und Ankerwurf;
 * später Finten, Verstärkung, Sprungangriffe und der Schottfall, bei dem Schotts in einer Welle
 * durch die Arena krachen. Ab dem zweiten Zyklus entfesselt: Nietenringe und doppelte Druckwellen
 * auf zwei Höhen. Nach einem Ansturm in die Wand ist sein Kern lange offen.
 */
final class WardenBrain extends Brain {
    static final int CHARGE = 0, SLAM = 1, SUMMON = 2, LEAP = 3, FEINT = 4, ANCHOR = 5, RAIN = 6;
    static final int RIVETS = 7;

    /** Schrittmarke: der Anker hat getroffen, der nächste Angriff ist ein Stampfer. */
    static final int HOOKED = 1;

    private static final int[][] CYCLES = {
        {CHARGE, SLAM, ANCHOR, CHARGE, SLAM},
        {FEINT, SLAM, SUMMON, LEAP, ANCHOR, RAIN},
        {RAIN, FEINT, LEAP, SLAM, CHARGE, SUMMON, LEAP}
    };
    private static final int[][] UNLEASHED = {
        {CHARGE, RIVETS, ANCHOR, FEINT, SLAM},
        {FEINT, RIVETS, SUMMON, LEAP, ANCHOR, RAIN, RIVETS},
        {RAIN, FEINT, RIVETS, LEAP, SLAM, CHARGE, RIVETS, SUMMON, LEAP}
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
        if (e.step == HOOKED) {
            e.step = 0;
            e.pattern = SLAM;
            e.aux = 1;
            return;
        }
        e.aux = 0;
        var cycle = (run.cycle() > 0 ? UNLEASHED : CYCLES)[e.phase];
        e.pattern = cycle[e.attacks % cycle.length];
        if (e.pattern == RAIN) rain(e, run);
    }

    /**
     * Schottfall: sieben Schotts krachen nacheinander von der Seite der Figur aus durch die Arena.
     * Eine Lücke bleibt; wer mit der Welle läuft oder durch die Lücke ausweicht, bleibt heil.
     */
    private static void rain(Enemy e, GameRun run) {
        double width = run.layout().width();
        int columns = 7;
        double span = (width - 180) / columns;
        boolean fromLeft = run.player.x < width / 2;
        int gap = 1 + run.rng.nextInt(columns - 2);
        for (int i = 0; i < columns; i++) {
            if (i == gap) continue;
            int slot = fromLeft ? i : columns - 1 - i;
            double x = 90 + span * (slot + .5);
            run.addHazard(
                    new Hazard(
                            x, span * .92, Hazard.Kind.BARRAGE, -(1.0 + i * .26), 1.4 + i * .26));
        }
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case CHARGE, FEINT -> e.enraged ? .8 : .95;
            case SLAM -> e.aux > 0 ? .35 : .8;
            case SUMMON -> .7;
            case ANCHOR -> .7;
            case RAIN -> 1.0;
            case RIVETS -> .85;
            default -> .6;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        double width = run.layout().width();
        return switch (e.pattern) {
            case CHARGE, FEINT ->
                    new Telegraph(
                            Telegraph.Shape.FLOOR, e.x, e.y, e.facing > 0 ? width - 50 : 50, e.y);
            case SLAM, RIVETS ->
                    new Telegraph(Telegraph.Shape.FLOOR, 60, GameRun.FLOOR, width - 60, 0);
            case LEAP ->
                    new Telegraph(Telegraph.Shape.CIRCLE, e.targetX, GameRun.FLOOR - 20, 170, 0);
            case ANCHOR ->
                    new Telegraph(
                            Telegraph.Shape.AIM, e.x, e.y - e.height * .6, e.targetX, e.targetY);
            default -> null;
        };
    }

    @Override
    double strikeTime(Enemy e) {
        return switch (e.pattern) {
            case CHARGE -> 3;
            case FEINT -> 3.4;
            case LEAP -> 2.5;
            case RIVETS -> .7;
            default -> .3;
        };
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        e.clock = 0;
        switch (e.pattern) {
            case SLAM -> slam(e, run, 17);
            case SUMMON -> {
                if (minions(e, run) < 3) {
                    var kind = run.cycle() > 0 ? EnemyKind.FUSE : EnemyKind.SCUTTLER;
                    run.summon(kind, 120, GameRun.FLOOR, e);
                    run.summon(kind, run.layout().width() - 120, GameRun.FLOOR, e);
                    if (run.cycle() > 0) run.summon(EnemyKind.CRAB, e.x, GameRun.FLOOR, e);
                }
            }
            case LEAP -> {
                e.vy = -1050;
                e.grounded = false;
                e.homeX = GameRun.clamp((e.targetX - e.x) / .95, -700, 700);
            }
            case ANCHOR -> {
                double y = e.y - e.height * .6;
                double angle = Math.atan2(e.targetY - y, e.targetX - e.x);
                var anchor =
                        run.shoot(
                                Projectile.Kind.ENEMY_HARPOON,
                                false,
                                e.x + e.facing * 50,
                                y,
                                Math.cos(angle) * 980,
                                Math.sin(angle) * 980,
                                12 * run.enemyDamage(),
                                16,
                                2);
                anchor.hook = true;
                anchor.owner = e.id;
                run.emit(GameEvent.at(GameEvent.Type.HARPOON, e.x, y));
            }
            case RAIN -> run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 200, ""));
            case RIVETS -> {
                slam(e, run, 15);
                rivets(e, run);
            }
            default -> {}
        }
    }

    /** Nietenring: Bolzen fliegen sternförmig aus dem Stampfer, mit einer Lücke über der Figur. */
    private static void rivets(Enemy e, GameRun run) {
        int count = e.phase >= 2 ? 16 : 12;
        double toward = Math.atan2(run.player.centerY() - e.centerY(), run.player.x - e.x);
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI + i * Math.PI / (count - 1);
            if (Math.abs(Math.atan2(Math.sin(angle - toward), Math.cos(angle - toward))) < .2)
                continue;
            run.shoot(
                    Projectile.Kind.BOLT,
                    false,
                    e.x,
                    e.centerY() - 20,
                    Math.cos(angle) * 360,
                    Math.sin(angle) * 360,
                    9 * run.enemyDamage(),
                    9,
                    4);
        }
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        e.clock += dt;
        switch (e.pattern) {
            case CHARGE -> {
                e.vx = e.facing * (e.enraged ? 720 : 640);
                melee(e, run, 90, 150, 20);
                if (atWall(e, run)) {
                    run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 120, ""));
                    e.stateTime = 0;
                }
            }
            case FEINT -> feint(e, run);
            case LEAP -> {
                e.vx = e.homeX;
                if (e.grounded && e.stateTime < 2.3) {
                    slam(e, run, 20);
                    e.stateTime = 0;
                }
            }
            case RIVETS -> {
                // Zweite Druckwelle in Sprunghöhe: erst springen, dann stehen bleiben.
                if (e.step == 0 && e.clock > .45) {
                    e.step = 2;
                    for (int side : new int[] {-1, 1})
                        run.shoot(
                                Projectile.Kind.SHOCKWAVE,
                                false,
                                e.x + side * 70,
                                GameRun.FLOOR - 150,
                                side * 470,
                                0,
                                15 * run.enemyDamage(),
                                22,
                                4);
                }
            }
            default -> {}
        }
    }

    /**
     * Finte: stürmt los, bremst nach der halben Strecke, dreht sich zur Figur und stürmt schneller
     * zurück. Wer zu früh ausweicht, läuft in den zweiten Ansturm.
     */
    private static void feint(Enemy e, GameRun run) {
        if (e.step == 0) {
            e.vx = e.facing * 640;
            melee(e, run, 90, 150, 20);
            if (e.clock > .55 || atWall(e, run)) {
                e.step = 3;
                e.clock = 0;
                e.vx = 0;
                run.emit(GameEvent.at(GameEvent.Type.TELEGRAPH, e.x, e.y - e.height));
            }
        } else if (e.step == 3) {
            e.vx = 0;
            if (e.clock > .3) {
                face(e, run);
                e.step = 4;
                e.strikeHit = false;
            }
        } else {
            e.vx = e.facing * 880;
            melee(e, run, 90, 150, 22);
            if (atWall(e, run)) {
                run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 140, ""));
                e.stateTime = 0;
            }
        }
    }

    private static void slam(Enemy e, GameRun run, double damage) {
        run.shockwaves(e.x, damage * run.enemyDamage(), e.enraged ? 520 : 430);
        run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 170, ""));
        if (Math.abs(dx(e, run)) < 170 && run.player.grounded)
            run.combat.hurtPlayer(18 * run.enemyDamage(), e.x, e, true);
    }

    @Override
    void strikeEnd(Enemy e, GameRun run) {
        if (e.step != HOOKED) e.step = 0;
    }

    @Override
    double recoverTime(Enemy e) {
        return switch (e.pattern) {
            case CHARGE -> 2.2;
            case FEINT -> 2.4;
            case SLAM -> 1.5;
            case SUMMON -> .9;
            case RAIN -> 1.8;
            case ANCHOR -> .8;
            case RIVETS -> 1.3;
            default -> 1.6;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .4;
    }
}
