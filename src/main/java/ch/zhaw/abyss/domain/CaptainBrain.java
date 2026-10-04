package ch.zhaw.abyss.domain;

/**
 * Der Lotse auf der Brücke: Harpunenkanone, Ansturm, Bodenwellen und ein Torpedofächer. Die
 * Torpedos suchen die Figur, explodieren aber auch am Lotsen selbst: Wer sie in ihn lenkt, betäubt
 * ihn und durchschlägt seine Panzerung. Ab der zweiten Phase Kreuzfeuer von beiden Wänden auf zwei
 * Höhen und Drohnen, in der letzten Phase ein Sperrfeuer im Schachbrettmuster und Verstärkung durch
 * Sicherheitsautomaten.
 */
final class CaptainBrain extends Brain {
    static final int HARPOON = 0, CHARGE = 1, WAVE = 2, DRONES = 3, BARRAGE = 4, TORPEDO = 5;
    static final int CROSSFIRE = 6, BOARDING = 7;
    private static final int[][] CYCLES = {
        {HARPOON, CHARGE, TORPEDO, HARPOON, WAVE},
        {CROSSFIRE, HARPOON, DRONES, CHARGE, TORPEDO, WAVE},
        {BARRAGE, CROSSFIRE, CHARGE, BOARDING, TORPEDO, HARPOON, BARRAGE}
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
        switch (e.pattern) {
            case BARRAGE -> barrage(e, run);
            case CROSSFIRE -> crossfire(run);
            default -> {}
        }
    }

    /**
     * Sperrfeuer: In der letzten Phase zwei versetzte Reihen im Schachbrettmuster. Wer nach der
     * ersten Reihe in deren Einschlagstellen wechselt, steht sicher.
     */
    private static void barrage(Enemy e, GameRun run) {
        double width = run.layout().width();
        if (e.phase < 2) {
            for (int k = -2; k <= 2; k++) {
                double x = GameRun.clamp(run.player.x + k * 190, 90, width - 90);
                run.addHazard(
                        new Hazard(x, 110, Hazard.Kind.BARRAGE, -1.05 - Math.abs(k) * .08, 1.6));
            }
            return;
        }
        double offset = run.rng.nextInt(2) * 160;
        for (int round = 0; round < 2; round++)
            for (double x = 100 + offset + round * 160; x < width - 60; x += 320)
                run.addHazard(
                        new Hazard(x, 140, Hazard.Kind.BARRAGE, -1.05 - round * .85, 1.6 + round));
    }

    /**
     * Kreuzfeuer: Harpunengeschütze an beiden Wänden zielen zuerst auf Kopfhöhe der Figur, dann in
     * Sprunghöhe. Erst ducken bleiben, dann springen.
     */
    private static void crossfire(GameRun run) {
        double width = run.layout().width();
        double low = Math.min(GameRun.FLOOR - 60, run.player.centerY());
        double high = GameRun.FLOOR - 210;
        for (int side : new int[] {-1, 1}) {
            double x = side < 0 ? 30 : width - 30;
            double angle = side < 0 ? 0 : Math.PI;
            run.lance(
                    new Lance(
                            x,
                            low,
                            angle,
                            1500,
                            14 * run.enemyDamage(),
                            1.0,
                            Lance.Style.STEEL,
                            0));
            run.lance(
                    new Lance(
                            x,
                            high,
                            angle,
                            1500,
                            14 * run.enemyDamage(),
                            1.9,
                            Lance.Style.STEEL,
                            0));
        }
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case HARPOON -> e.enraged ? .6 : .75;
            case CHARGE -> .9;
            case WAVE -> .8;
            case DRONES, BOARDING -> .7;
            case TORPEDO -> .8;
            case CROSSFIRE -> .7;
            default -> 1.0;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        double width = run.layout().width();
        return switch (e.pattern) {
            case HARPOON, TORPEDO ->
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
                int shots = e.phase >= 1 ? 3 : 1;
                for (int i = 0; i < shots; i++)
                    run.shoot(
                            Projectile.Kind.ENEMY_HARPOON,
                            false,
                            e.x + e.facing * 40,
                            y,
                            Math.cos(angle + (i - (shots - 1) / 2.0) * .18) * 1150,
                            Math.sin(angle + (i - (shots - 1) / 2.0) * .18) * 1150,
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
            case BOARDING -> {
                if (minions(e, run) < 3) {
                    run.summon(EnemyKind.ENFORCER, 140, GameRun.FLOOR, e);
                    run.summon(EnemyKind.ENFORCER, run.layout().width() - 140, GameRun.FLOOR, e);
                    run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.y, 0, "BOARDING"));
                }
            }
            case TORPEDO -> torpedoes(e, run);
            default -> {}
        }
    }

    /** Torpedofächer: langsame, zielsuchende Torpedos, die auch den Lotsen selbst treffen. */
    private static void torpedoes(Enemy e, GameRun run) {
        int count = 2 + e.phase;
        double y = e.y - e.height * .6;
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 + (i - (count - 1) / 2.0) * .55;
            var torpedo =
                    run.shoot(
                            Projectile.Kind.TORPEDO,
                            false,
                            e.x,
                            y,
                            Math.cos(angle) * 300 + e.facing * 80,
                            Math.sin(angle) * 300,
                            15 * run.enemyDamage(),
                            16,
                            7);
            torpedo.homing = true;
            torpedo.steerStart = .35;
            torpedo.explosionRadius = 110;
            torpedo.wreck = true;
            torpedo.owner = e.id;
        }
        run.emit(GameEvent.at(GameEvent.Type.TORPEDO, e.x, y));
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
            case DRONES, BOARDING -> .8;
            case TORPEDO -> 1.4;
            case CROSSFIRE -> 1.2;
            default -> .9;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .35;
    }
}
