package ch.zhaw.abyss.domain;

/**
 * Die Prismenkaiserin, Endgegnerin am Grund des Abgrunds ab dem zweiten Zyklus. Ein Kampf wie ein
 * Lichtgewitter: schwebende Prismenbolzen, die auf die Figur zuschnellen, Reihen von Lichtlanzen
 * mit Lücken, ein Lanzenregen, der Ewige Regenbogen, Lichtstürze quer durch den Saal und der
 * Sonnentanz, bei dem sich Strahlen um sie drehen. Jedes Muster hat eine erlernbare Lösung; bei
 * jedem Phasenwechsel entrückt sie kurz und lässt einen Sternenbruch los. Nach jedem Muster sinkt
 * sie erschöpft herab und ist verwundbar. Wer zu lange braucht, erlebt sie rasend.
 */
final class EmpressBrain extends Brain {
    static final int BOLTS = 0, LANCES = 1, SUN_DANCE = 2, DASH = 3, RAINBOW = 4, RAIN = 5;
    static final int BROOD = 6, STARBURST = 7, ASCEND = 8;

    /** Sekunden bis zur Raserei. */
    static final double FURY = 240;

    /** Schwebehöhe: mit einem Sprung oder von den Stegen aus erreichbar. */
    static final double HOVER = GameRun.FLOOR - 250;

    /** Erschöpft sinkt sie nach jedem Muster tief herab: das Fenster zum Zuschlagen. */
    static final double LOW = GameRun.FLOOR - 120;

    private static final int[][] CYCLES = {
        {BOLTS, LANCES, DASH, RAINBOW, LANCES, SUN_DANCE},
        {SUN_DANCE, BOLTS, RAIN, DASH, BROOD, RAINBOW, LANCES},
        {STARBURST, DASH, SUN_DANCE, RAIN, BOLTS, DASH, RAINBOW, LANCES}
    };

    @Override
    void idle(Enemy e, GameRun run, double dt) {
        flyTo(e, e.x, HOVER + Math.sin(run.elapsed() * 1.1) * 26, 160);
    }

    @Override
    void approach(Enemy e, GameRun run, double dt) {
        face(e, run);
        double center = run.layout().width() / 2;
        double tx = center + Math.sin(run.elapsed() * .45) * 430;
        flyTo(e, tx, HOVER + Math.sin(run.elapsed() * 1.1) * 26, speed(e));
        if (!e.furious && e.animationTime > FURY) {
            e.furious = true;
            run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "FURY"));
        }
    }

    @Override
    boolean ready(Enemy e, GameRun run) {
        return true;
    }

    @Override
    void choose(Enemy e, GameRun run) {
        if (e.step < e.phase) {
            // Phasenwechsel: sie entrückt und bereitet den Sternenbruch vor.
            e.step = e.phase;
            e.pattern = ASCEND;
            e.veiled = true;
            return;
        }
        var cycle = CYCLES[e.phase];
        e.pattern = cycle[e.attacks % cycle.length];
        e.aux = (e.attacks / cycle.length) % 2 == 0 ? 1 : -1;
        switch (e.pattern) {
            case LANCES -> lanceRows(e, run);
            case RAIN -> lanceRain(e, run);
            case DASH -> {
                double width = run.layout().width();
                e.homeX = run.player.x < width / 2 ? width - 110 : 110;
                e.targetY =
                        GameRun.clamp(
                                run.player.centerY(), GameRun.FLOOR - 430, GameRun.FLOOR - 60);
            }
            default -> {}
        }
    }

    private double power(Enemy e, GameRun run) {
        return run.enemyDamage() * (e.furious ? 1.5 : 1);
    }

    /**
     * Lichtlanzen: zehn Reihen von einer Seite, drei benachbarte Reihen bleiben frei. Die zweite
     * Welle kommt von der anderen Seite mit verschobener Lücke. In der ersten Phase liegt die Lücke
     * immer am Boden.
     */
    private void lanceRows(Enemy e, GameRun run) {
        double width = run.layout().width();
        int waves = e.phase >= 1 ? 3 : 2;
        int gap = e.phase == 0 ? 0 : run.rng.nextInt(3);
        boolean fromLeft = run.player.x > width / 2;
        for (int wave = 0; wave < waves; wave++) {
            for (int row = 0; row < 10; row++) {
                if (row >= gap && row < gap + 3) continue;
                double y = GameRun.FLOOR - 25 - row * 55;
                run.lance(
                        new Lance(
                                fromLeft ? -30 : width + 30,
                                y,
                                fromLeft ? 0 : Math.PI,
                                1700,
                                13 * power(e, run),
                                1.0 + wave * .95,
                                Lance.Style.PRISM,
                                row / 10.0));
            }
            fromLeft = !fromLeft;
            gap = e.phase == 0 ? (gap == 0 ? 1 : 0) : (gap + 1 + run.rng.nextInt(2)) % 4;
        }
    }

    /**
     * Lanzenregen: drei Salven senkrechter Lanzen um die Figur, jede um eine halbe Lücke versetzt.
     * Wer nach jeder Salve einen halben Schritt zur Seite macht, steht in der nächsten Lücke.
     */
    private void lanceRain(Enemy e, GameRun run) {
        double width = run.layout().width();
        double center = run.player.x;
        for (int volley = 0; volley < 3; volley++)
            for (int k = -4; k <= 4; k++) {
                double x = center + k * 150 + (volley % 2) * 75;
                if (x < 40 || x > width - 40) continue;
                run.lance(
                        new Lance(
                                x,
                                -30,
                                Math.PI / 2,
                                1750,
                                13 * power(e, run),
                                .85 + volley * .6,
                                Lance.Style.PRISM,
                                (x / width + volley * .3) % 1));
            }
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case BOLTS -> .7;
            case LANCES -> .5;
            case SUN_DANCE -> 1.3;
            case DASH -> .9;
            case RAINBOW -> .7;
            case RAIN -> .4;
            case BROOD -> .8;
            case STARBURST -> .9;
            default -> 2.2;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        return switch (e.pattern) {
            case DASH ->
                    new Telegraph(
                            Telegraph.Shape.AIM,
                            e.homeX,
                            e.targetY,
                            run.layout().width() - e.homeX,
                            e.targetY);
            case STARBURST, ASCEND ->
                    new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.centerY(), 180, 0);
            default -> null;
        };
    }

    @Override
    void windup(Enemy e, GameRun run, double dt) {
        double center = run.layout().width() / 2;
        switch (e.pattern) {
            case SUN_DANCE -> {
                flyTo(e, center, GameRun.FLOOR - 380 + e.height / 2, 900);
                sunBeams(e, run, 0, false);
            }
            case DASH -> flyTo(e, e.homeX, e.targetY + e.height / 2, 1500);
            case ASCEND -> flyTo(e, center, GameRun.FLOOR - 420 + e.height / 2, 700);
            default -> idle(e, run, dt);
        }
    }

    @Override
    double strikeTime(Enemy e) {
        return switch (e.pattern) {
            case BOLTS -> 1.3;
            case SUN_DANCE -> e.phase >= 2 ? 4 : 3.4;
            case DASH -> 1.4;
            case RAINBOW -> e.phase >= 1 ? 3.4 : 2.8;
            case STARBURST -> 1.6;
            case ASCEND -> 1.2;
            default -> .3;
        };
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        e.clock = 0;
        switch (e.pattern) {
            case DASH -> {
                e.facing = e.homeX < run.layout().width() / 2 ? 1 : -1;
                e.vy = 0;
            }
            case BROOD -> {
                if (minions(e, run) < 4)
                    for (int i = 0; i < 3; i++)
                        run.summon(
                                EnemyKind.PRISM,
                                e.x + (i - 1) * 220,
                                GameRun.FLOOR - 260 - (i % 2) * 80,
                                e);
            }
            case ASCEND -> {
                e.veiled = false;
                ring(e, run, 32, 300, Double.NaN);
                run.emit(new GameEvent(GameEvent.Type.PULSE, e.x, e.centerY(), -500, ""));
            }
            default -> {}
        }
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        double before = e.clock;
        e.clock += dt;
        switch (e.pattern) {
            case BOLTS -> {
                for (int burst = 0; burst < 3; burst++)
                    if (before <= burst * .4 && e.clock > burst * .4) bolts(e, run, burst);
            }
            case SUN_DANCE -> {
                e.vx *= .9;
                e.vy *= .9;
                sunBeams(e, run, e.clock, true);
            }
            case DASH -> {
                e.vx = e.facing * 1700;
                e.vy = (e.targetY + e.height / 2 - e.y) * 4;
                contact(e, run, 22 * (e.furious ? 2 : 1));
                if (Math.floor(before / .06) != Math.floor(e.clock / .06)) trail(e, run);
                if (atWall(e, run) && e.clock > .2) e.stateTime = 0;
            }
            case RAINBOW -> rainbow(e, run, before);
            case STARBURST -> {
                for (int k = 0; k < 3; k++)
                    if (before <= k * .5 && e.clock > k * .5)
                        ring(
                                e,
                                run,
                                22,
                                230 + k * 30,
                                Math.atan2(run.player.centerY() - e.centerY(), run.player.x - e.x));
            }
            default -> idle(e, run, dt);
        }
    }

    /**
     * Prismenbolzen: ein Fächer aus den Flügeln, der kurz in der Luft hängt und dann auf die
     * Position der Figur zuschnellt. Seitwärts laufen genügt, stehen bleiben nicht.
     */
    private void bolts(Enemy e, GameRun run, int burst) {
        int count = 10 + e.phase * 2;
        for (int i = 0; i < count; i++) {
            double angle = Math.PI + (i + .5) * Math.PI / count + (burst - 1) * .08;
            var bolt =
                    run.shoot(
                            Projectile.Kind.PRISM,
                            false,
                            e.x + Math.cos(angle) * 70,
                            e.centerY() + Math.sin(angle) * 50,
                            Math.cos(angle) * 380,
                            Math.sin(angle) * 380,
                            9 * power(e, run),
                            9,
                            5);
            bolt.accel = .15;
            bolt.surgeAt = .9 + i * .035;
            bolt.surgeSpeed = (e.furious ? 950 : 760);
            bolt.hue = (i / (double) count + burst * .2) % 1;
        }
        run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, e.centerY()));
    }

    /**
     * Sonnentanz: Strahlen drehen sich um die Kaiserin. Unter ihr sind die Lücken am langsamsten;
     * wer mit der Drehung läuft, bleibt heil.
     */
    private void sunBeams(Enemy e, GameRun run, double time, boolean live) {
        int count = 6 + e.phase;
        double speed = (.55 + .15 * e.phase) * (e.furious ? 1.3 : 1);
        double start = live ? Math.max(0, time - .3) : 0;
        double base = e.aux * (start * speed + start * start * .04) + Math.PI / 2 / count;
        double length = 1250, cx = e.x, cy = e.centerY();
        var player = run.player;
        for (int k = 0; k < count; k++) {
            double angle = base + k * Math.PI * 2 / count;
            var beam =
                    new Beam(
                            cx,
                            cy,
                            cx + Math.cos(angle) * length,
                            cy + Math.sin(angle) * length,
                            live ? 26 : 6,
                            live && time > .3,
                            (k / (double) count + time * .1) % 1);
            run.beams.add(beam);
            if (beam.live()
                    && (beam.touches(player.x, player.centerY(), 18)
                            || beam.touches(player.x, player.y - 20, 16)
                            || beam.touches(player.x, player.y - player.height + 16, 16)))
                run.combat.hurtPlayer(16 * power(e, run), cx, e, false);
        }
    }

    /** Lichtsturz: hinter der Kaiserin bleibt eine Spur langsam fallender Funken. */
    private void trail(Enemy e, GameRun run) {
        var spark =
                run.shoot(
                        Projectile.Kind.PRISM,
                        false,
                        e.x,
                        e.centerY(),
                        0,
                        60,
                        8 * power(e, run),
                        8,
                        3.2);
        spark.gravity = 160;
        spark.hue = (e.clock * 2) % 1;
    }

    /** Ewiger Regenbogen: drei bis vier Spiralarme aus Prismageschossen. */
    private void rainbow(Enemy e, GameRun run, double before) {
        double interval = .075;
        if (Math.floor(before / interval) == Math.floor(e.clock / interval)) return;
        int arms = e.phase >= 2 ? 4 : 3;
        double turn = e.clock * 1.9 * e.aux;
        for (int k = 0; k < arms; k++) {
            double angle = turn + k * Math.PI * 2 / arms;
            var bolt =
                    run.shoot(
                            Projectile.Kind.PRISM,
                            false,
                            e.x + Math.cos(angle) * 50,
                            e.centerY() + Math.sin(angle) * 40,
                            Math.cos(angle) * 250,
                            Math.sin(angle) * 250,
                            8 * power(e, run),
                            8,
                            5);
            bolt.hue = (angle / (Math.PI * 2) + 10) % 1;
        }
    }

    /**
     * Sternenbruch: ein Ring aus Prismageschossen mit einer Lücke in Richtung {@code gapAngle}. Mit
     * {@link Double#NaN} schliesst sich der Ring vollständig.
     */
    private void ring(Enemy e, GameRun run, int count, double speed, double gapAngle) {
        for (int k = 0; k < count; k++) {
            double angle = k * Math.PI * 2 / count + e.clock;
            if (!Double.isNaN(gapAngle)
                    && Math.abs(Math.atan2(Math.sin(angle - gapAngle), Math.cos(angle - gapAngle)))
                            < .32) continue;
            var bolt =
                    run.shoot(
                            Projectile.Kind.PRISM,
                            false,
                            e.x,
                            e.centerY(),
                            Math.cos(angle) * speed,
                            Math.sin(angle) * speed,
                            9 * power(e, run),
                            9,
                            5);
            bolt.hue = k / (double) count;
        }
        run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, e.centerY()));
    }

    @Override
    void recover(Enemy e, GameRun run, double dt) {
        flyTo(e, e.x, LOW, 260);
    }

    @Override
    void strikeEnd(Enemy e, GameRun run) {
        e.veiled = false;
        if (e.pattern == DASH) {
            e.vx = 0;
            e.vy = 0;
        }
    }

    @Override
    double recoverTime(Enemy e) {
        return switch (e.pattern) {
            case DASH -> 1.2;
            case SUN_DANCE -> 1.5;
            case RAINBOW -> 1.1;
            case LANCES, RAIN -> 1.6;
            case ASCEND -> 1;
            default -> .9;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return e.furious ? .1 : .3;
    }
}
