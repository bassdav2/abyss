package ch.zhaw.abyss.domain;

/**
 * Der Reaktorkern, Wächter des Maschinendecks. Fächerfeuer, Bodenwellen und eine rotierende
 * Kernspirale; später Gitterstrahlen von der Decke, hohe und tiefe Strahlen und ein
 * Strahlungsimpuls. In der letzten Phase droht die Kernschmelze: Wer rechtzeitig den Notschalter
 * bedient, kühlt den Kern und legt ihn lange frei, wer ihn vorher verschwendet hat, muss im
 * richtigen Moment ausweichen. Ab dem zweiten Zyklus wird die Spirale zum Prismensturm.
 */
final class ReactorBrain extends Brain {
    static final int FAN = 0, WAVE = 1, BEAM = 2, PULSE = 3, SPIRAL = 4, GRID = 5, MELTDOWN = 6;
    static final double PULSE_RADIUS = 430;

    /** Dauer der Vorwarnung einer Kernschmelze. */
    static final double MELTDOWN_TIME = 4.2;

    private static final int[][] CYCLES = {
        {FAN, WAVE, SPIRAL, FAN, WAVE},
        {GRID, FAN, BEAM, SPIRAL, WAVE, BEAM},
        {MELTDOWN, SPIRAL, BEAM, GRID, PULSE, FAN, BEAM}
    };

    @Override
    void approach(Enemy e, GameRun run, double dt) {
        face(e, run);
        double target = run.layout().width() / 2 + Math.sin(run.elapsed() * .3) * 260;
        if (Math.abs(target - e.x) > 30) e.vx = Math.signum(target - e.x) * speed(e);
    }

    @Override
    boolean ready(Enemy e, GameRun run) {
        return true;
    }

    @Override
    void choose(Enemy e, GameRun run) {
        var cycle = CYCLES[e.phase];
        e.pattern = cycle[e.attacks % cycle.length];
        if (e.pattern == BEAM) {
            e.burst = (e.attacks / 2) % 2;
            e.targetY = e.burst == 0 ? GameRun.FLOOR - 32 : GameRun.FLOOR - 205;
        }
        if (e.pattern == GRID) grid(e, run);
    }

    /**
     * Gitterstrahl: zwei Reihen senkrechter Strahlen schiessen nacheinander von der Decke. Die
     * zweite Reihe trifft genau die Lücken der ersten.
     */
    private static void grid(Enemy e, GameRun run) {
        double width = run.layout().width();
        double offset = run.rng.nextInt(2) * 150;
        for (int round = 0; round < 2; round++)
            for (double x = 120 + offset + round * 150; x < width - 80; x += 300)
                run.lance(
                        new Lance(
                                x,
                                -20,
                                Math.PI / 2,
                                1900,
                                15 * run.enemyDamage(),
                                1.0 + round * .8,
                                Lance.Style.CORE,
                                .33));
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case FAN -> e.enraged ? .8 : 1.0;
            case WAVE -> .9;
            case BEAM -> 1.1;
            case SPIRAL -> .8;
            case GRID -> .9;
            case MELTDOWN -> MELTDOWN_TIME;
            default -> 1.2;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        return switch (e.pattern) {
            case FAN ->
                    new Telegraph(
                            Telegraph.Shape.AIM, e.x, e.y - e.height * .55, e.targetX, e.targetY);
            case WAVE ->
                    new Telegraph(
                            Telegraph.Shape.FLOOR, 60, GameRun.FLOOR, run.layout().width() - 60, 0);
            case BEAM ->
                    new Telegraph(Telegraph.Shape.BEAM, 0, e.targetY, run.layout().width(), 36);
            case SPIRAL -> new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.centerY(), 120, 0);
            case GRID -> null;
            case MELTDOWN ->
                    new Telegraph(
                            Telegraph.Shape.CIRCLE, e.x, e.centerY(), run.layout().width(), 0);
            default -> new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.centerY(), PULSE_RADIUS, 0);
        };
    }

    @Override
    void windup(Enemy e, GameRun run, double dt) {
        idle(e, run, dt);
        if (e.pattern == MELTDOWN && e.stateTime > MELTDOWN_TIME - dt * 1.5)
            run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "MELTDOWN"));
    }

    @Override
    double strikeTime(Enemy e) {
        return switch (e.pattern) {
            case BEAM -> .45;
            case SPIRAL -> 2.4;
            default -> .2;
        };
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        e.clock = 0;
        e.aux = run.rng.nextBoolean() ? 1 : -1;
        switch (e.pattern) {
            case FAN -> {
                int spread = e.phase == 0 ? 2 : 3;
                for (int i = -spread; i <= spread; i++)
                    shootAt(e, run, i * .16, 380, 10, e.targetX, e.targetY);
                if (e.phase >= 1)
                    for (int i = -spread; i < spread; i++)
                        shootAt(e, run, (i + .5) * .16, 300, 10, e.targetX, e.targetY);
            }
            case WAVE -> run.shockwaves(e.x, 16 * run.enemyDamage(), 440);
            case BEAM -> run.emit(new GameEvent(GameEvent.Type.ARC, e.x, e.targetY, -1, "beam"));
            case MELTDOWN -> {
                run.emit(
                        new GameEvent(
                                GameEvent.Type.PULSE, e.x, e.centerY(), -run.layout().width(), ""));
                run.combat.hurtPlayer(42 * run.enemyDamage(), e.x, e, false);
                run.shockwaves(e.x, 16 * run.enemyDamage(), 520);
            }
            case PULSE -> {
                run.emit(new GameEvent(GameEvent.Type.PULSE, e.x, e.centerY(), -PULSE_RADIUS, ""));
                if (Math.hypot(dx(e, run), run.player.centerY() - e.centerY()) < PULSE_RADIUS)
                    run.combat.hurtPlayer(16 * run.enemyDamage(), e.x, e, false);
            }
            default -> {}
        }
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        if (e.pattern == BEAM && !e.strikeHit) {
            var beam = new Bounds(0, e.targetY - 18, run.layout().width(), 36);
            if (beam.intersects(run.player.bounds())
                    && run.combat.hurtPlayer(18 * run.enemyDamage(), e.x, e, false))
                e.strikeHit = true;
        }
        if (e.pattern == SPIRAL) spiral(e, run, dt);
    }

    /**
     * Kernspirale: zwei Arme aus Bolzen drehen sich um den Kern; wer im Abstand mitläuft, bleibt in
     * der Lücke. Entfesselt werden daraus vier gegenläufige Arme aus Prismageschossen.
     */
    private static void spiral(Enemy e, GameRun run, double dt) {
        double before = e.clock;
        e.clock += dt;
        double interval = .09;
        if (Math.floor(before / interval) == Math.floor(e.clock / interval)) return;
        boolean prism = run.cycle() > 0;
        int arms = prism ? 4 : 2 + (e.phase >= 2 ? 1 : 0);
        double turn = e.clock * (e.phase >= 1 ? 2.6 : 2.1) * e.aux;
        for (int k = 0; k < arms; k++) {
            double direction = prism && k % 2 == 1 ? -1 : 1;
            double angle = turn * direction + k * Math.PI * 2 / arms;
            var bolt =
                    run.shoot(
                            prism ? Projectile.Kind.PRISM : Projectile.Kind.BOLT,
                            false,
                            e.x + Math.cos(angle) * 60,
                            e.centerY() + Math.sin(angle) * 60,
                            Math.cos(angle) * 290,
                            Math.sin(angle) * 290,
                            9 * run.enemyDamage(),
                            9,
                            4);
            bolt.hue = (k / (double) arms + e.clock * .2) % 1;
        }
    }

    /**
     * Kühlt eine laufende Kernschmelze über den Notschalter: der Kern bricht ein und bleibt lange
     * ungepanzert.
     *
     * @param run Tauchgang
     * @return {@code true}, wenn eine Kernschmelze gestoppt wurde
     */
    static boolean vent(GameRun run) {
        for (var e : run.enemies)
            if (e.alive()
                    && e.kind == EnemyKind.REACTOR
                    && e.state == Enemy.State.WINDUP
                    && e.pattern == MELTDOWN) {
                e.state = Enemy.State.STUNNED;
                e.stateTime = 4.5;
                e.telegraph = null;
                e.attacks++;
                run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "VENTED"));
                return true;
            }
        return false;
    }

    @Override
    double recoverTime(Enemy e) {
        return switch (e.pattern) {
            case BEAM -> 1.1;
            case PULSE -> 1.6;
            case SPIRAL -> 1.2;
            case GRID -> 1.4;
            case MELTDOWN -> 2.4;
            default -> 1.3;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .35;
    }
}
