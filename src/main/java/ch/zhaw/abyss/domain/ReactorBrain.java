package ch.zhaw.abyss.domain;

/**
 * Der Reaktorkern, Wächter des Maschinendecks: Fächerfeuer, Bodenwellen, später abwechselnd hohe
 * und tiefe Strahlen sowie ein Strahlungsimpuls, dem man nur mit Abstand oder Ausweichen entgeht.
 */
final class ReactorBrain extends Brain {
    static final int FAN = 0, WAVE = 1, BEAM = 2, PULSE = 3;
    static final double PULSE_RADIUS = 430;
    private static final int[][] CYCLES = {
        {FAN, WAVE, FAN, WAVE}, {FAN, BEAM, WAVE, BEAM}, {BEAM, PULSE, FAN, BEAM, WAVE, PULSE}
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
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case FAN -> e.enraged ? .8 : 1.0;
            case WAVE -> .9;
            case BEAM -> 1.1;
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
            default -> new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.centerY(), PULSE_RADIUS, 0);
        };
    }

    @Override
    double strikeTime(Enemy e) {
        return e.pattern == BEAM ? .45 : .2;
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        switch (e.pattern) {
            case FAN -> {
                int spread = e.phase == 0 ? 2 : 3;
                for (int i = -spread; i <= spread; i++)
                    shootAt(e, run, i * .16, 380, 10, e.targetX, e.targetY);
            }
            case WAVE -> run.shockwaves(e.x, 16 * run.enemyDamage(), 440);
            case BEAM -> run.emit(new GameEvent(GameEvent.Type.ARC, e.x, e.targetY, -1, "beam"));
            default -> {
                run.emit(new GameEvent(GameEvent.Type.PULSE, e.x, e.centerY(), -PULSE_RADIUS, ""));
                if (Math.hypot(dx(e, run), run.player.centerY() - e.centerY()) < PULSE_RADIUS)
                    run.combat.hurtPlayer(16 * run.enemyDamage(), e.x, e, false);
            }
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
    }

    @Override
    double recoverTime(Enemy e) {
        return switch (e.pattern) {
            case BEAM -> 1.1;
            case PULSE -> 1.6;
            default -> 1.3;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .35;
    }
}
