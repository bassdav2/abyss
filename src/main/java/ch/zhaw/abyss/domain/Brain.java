package ch.zhaw.abyss.domain;

import ch.zhaw.abyss.domain.Enemy.State;

/**
 * Schablonenmethode für die gemeinsame Kampf-Zustandsmaschine: Annähern, Ausholen mit Vorwarnung,
 * Angriff, Erholung. Unterklassen überschreiben nur die Stellen, an denen sich Arten unterscheiden.
 */
abstract class Brain implements EnemyBehavior {

    @Override
    public final void update(Enemy e, GameRun run, double dt) {
        e.actionCooldown = Math.max(0, e.actionCooldown - dt);
        if (!e.flying()) e.vx = 0;
        if (e.kind.boss() && e.state != State.SPAWNING) checkPhase(e, run);
        switch (e.state) {
            case SPAWNING -> {
                e.stateTime -= dt;
                idle(e, run, dt);
                if (e.stateTime <= 0) {
                    e.state = State.APPROACH;
                    spawned(e, run);
                }
            }
            case STUNNED -> {
                e.stateTime -= dt;
                idle(e, run, dt);
                if (e.stateTime <= 0) e.state = State.APPROACH;
            }
            case RECOVER -> {
                e.stateTime -= dt;
                recover(e, run, dt);
                if (e.stateTime <= 0) e.state = State.APPROACH;
            }
            case HIDDEN -> hidden(e, run, dt);
            case WINDUP -> {
                e.stateTime -= dt;
                windup(e, run, dt);
                if (e.stateTime <= 0) {
                    e.state = State.STRIKE;
                    e.stateTime = strikeTime(e);
                    e.strikeHit = false;
                    e.fired = false;
                    e.burst = 0;
                    strikeStart(e, run);
                }
            }
            case STRIKE -> {
                strike(e, run, dt);
                e.stateTime -= dt;
                if (e.stateTime <= 0 && e.state == State.STRIKE) finishStrike(e, run);
            }
            case APPROACH -> {
                approach(e, run, dt);
                if (e.state == State.APPROACH && e.actionCooldown <= 0 && ready(e, run))
                    beginWindup(e, run);
            }
        }
    }

    /** Startet das Ausholen mit Zielwahl und Vorwarnung. */
    final void beginWindup(Enemy e, GameRun run) {
        face(e, run);
        e.targetX = run.player.x;
        e.targetY = run.player.centerY();
        choose(e, run);
        e.state = State.WINDUP;
        e.stateTime = windupTime(e) * (e.affix == Affix.SWIFT ? .7 : 1);
        e.telegraph = telegraph(e, run);
        run.emit(GameEvent.at(GameEvent.Type.TELEGRAPH, e.x, e.y - e.height));
    }

    private void finishStrike(Enemy e, GameRun run) {
        e.state = State.RECOVER;
        e.stateTime = recoverTime(e) * (e.enraged ? .85 : 1);
        e.actionCooldown = cooldown(e) * (e.affix == Affix.SWIFT ? .7 : 1);
        e.attacks++;
        e.telegraph = null;
        strikeEnd(e, run);
    }

    private static void checkPhase(Enemy e, GameRun run) {
        int next = e.healthRatio() < 1 / 3.0 ? 2 : e.healthRatio() < 2 / 3.0 ? 1 : 0;
        if (next > e.phase) {
            e.phase = next;
            e.enraged = true;
            run.emit(
                    new GameEvent(
                            GameEvent.Type.BOSS_PHASE, e.x, e.y - e.height, next, e.kind.title()));
        }
    }

    // --- Einstiegspunkte der Schablone ---------------------------------------------------------

    void idle(Enemy e, GameRun run, double dt) {
        if (e.flying()) {
            e.vx *= .9;
            e.vy *= .9;
        }
    }

    void spawned(Enemy e, GameRun run) {}

    abstract void approach(Enemy e, GameRun run, double dt);

    abstract boolean ready(Enemy e, GameRun run);

    void choose(Enemy e, GameRun run) {
        e.pattern = 0;
    }

    abstract double windupTime(Enemy e);

    Telegraph telegraph(Enemy e, GameRun run) {
        return null;
    }

    void windup(Enemy e, GameRun run, double dt) {
        idle(e, run, dt);
    }

    double strikeTime(Enemy e) {
        return .16;
    }

    void strikeStart(Enemy e, GameRun run) {}

    void strike(Enemy e, GameRun run, double dt) {}

    void strikeEnd(Enemy e, GameRun run) {}

    double recoverTime(Enemy e) {
        return .8;
    }

    double cooldown(Enemy e) {
        return .9;
    }

    void recover(Enemy e, GameRun run, double dt) {
        idle(e, run, dt);
    }

    void hidden(Enemy e, GameRun run, double dt) {
        e.state = State.APPROACH;
    }

    // --- Hilfsfunktionen -----------------------------------------------------------------------

    static double speed(Enemy e) {
        return e.kind.speed * (e.affix == Affix.SWIFT ? 1.4 : 1) * (e.enraged ? 1.15 : 1);
    }

    static double dx(Enemy e, GameRun run) {
        return run.player.x - e.x;
    }

    static double dy(Enemy e, GameRun run) {
        return run.player.y - e.y;
    }

    static void face(Enemy e, GameRun run) {
        e.facing = run.player.x >= e.x ? 1 : -1;
    }

    /** Läuft auf die Figur zu, bis der gewünschte Abstand erreicht ist. */
    static void close(Enemy e, GameRun run, double distance) {
        face(e, run);
        followDown(e, run);
        if (Math.abs(dx(e, run)) > distance) e.vx = e.facing * speed(e);
    }

    /** Lässt einen Bodengegner durch den Steg fallen, wenn die Figur deutlich tiefer steht. */
    static void followDown(Enemy e, GameRun run) {
        if (!e.flying() && e.platform != null && e.grounded && dy(e, run) > 90) e.dropTime = .3;
    }

    /** Hält einen Abstandskorridor zur Figur. */
    static void keepDistance(Enemy e, GameRun run, double min, double max) {
        face(e, run);
        followDown(e, run);
        double d = Math.abs(dx(e, run));
        if (d > max) e.vx = e.facing * speed(e);
        else if (d < min) e.vx = -e.facing * speed(e) * .8;
    }

    /** Fliegt weich auf eine Zielposition zu. */
    static void flyTo(Enemy e, double x, double y, double speed) {
        double ddx = x - e.x, ddy = y - e.y;
        double length = Math.max(1, Math.hypot(ddx, ddy));
        double scale = Math.min(1, length / 60);
        e.vx += (ddx / length * speed * scale - e.vx) * .08;
        e.vy += (ddy / length * speed * scale - e.vy) * .08;
    }

    /**
     * Prüft einen einmaligen Nahkampftreffer vor dem Gegner.
     *
     * @return {@code true}, wenn getroffen wurde
     */
    static boolean melee(Enemy e, GameRun run, double reach, double height, double damage) {
        if (e.strikeHit) return false;
        double left = e.facing > 0 ? e.x - 30 : e.x - reach;
        var zone = new Bounds(left, e.y - height, reach + 30, height);
        if (!zone.intersects(run.player.bounds())) return false;
        e.strikeHit = true;
        run.combat.hurtPlayer(damage * run.enemyDamage(), e.x, e, true);
        return true;
    }

    /** Prüft einmaligen Schaden bei Körperkontakt. */
    static boolean contact(Enemy e, GameRun run, double damage) {
        if (e.strikeHit || !e.bounds().intersects(run.player.bounds())) return false;
        e.strikeHit = true;
        run.combat.hurtPlayer(damage * run.enemyDamage(), e.x, e, true);
        return true;
    }

    /** Feuert ein gezieltes Energiegeschoss. */
    static void shootAt(
            Enemy e, GameRun run, double offset, double speed, double damage, double x, double y) {
        double originY = e.y - e.height * .55;
        double angle = Math.atan2(y - originY, x - e.x) + offset;
        run.shoot(
                Projectile.Kind.BOLT,
                false,
                e.x + e.facing * 26,
                originY,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                damage * run.enemyDamage(),
                9,
                4);
        run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, originY));
    }

    /**
     * @return {@code true}, wenn der Gegner an eine Raumwand stösst
     */
    static boolean atWall(Enemy e, GameRun run) {
        double min = RoomLayout.WALL + e.width / 2, max = run.layout().width() - min;
        return e.x <= min + 2 && e.facing < 0 || e.x >= max - 2 && e.facing > 0;
    }

    /**
     * @return Anzahl lebender Begleiter dieses Gegners
     */
    static long minions(Enemy e, GameRun run) {
        return run.enemies.stream().filter(m -> m.alive() && e.children.contains(m.id)).count();
    }
}
