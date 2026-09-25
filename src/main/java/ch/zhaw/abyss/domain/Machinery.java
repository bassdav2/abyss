package ch.zhaw.abyss.domain;

/**
 * Wirkung der Raumtechnik auf Figur und Gegner. Mitarbeiterklasse von {@link GameRun}: Schub durch
 * Band und Wind, Start von Dampfdüsen, Treffer von Pressen und Lasern sowie die Effekte der
 * Notschalter.
 */
final class Machinery {
    /** Reichweite, in der die Figur einen Notschalter bedienen kann. */
    static final double CONSOLE_REACH = 70;

    private final GameRun run;

    Machinery(GameRun run) {
        this.run = run;
    }

    /**
     * Horizontaler Schub, den Band und Wind auf eine Figur ausüben.
     *
     * @param a Figur
     * @return zusätzliche Geschwindigkeit in Einheiten pro Sekunde
     */
    double push(Actor a) {
        double push = 0;
        double tilt = run.listTilt();
        if (Math.abs(tilt) > .3 && !a.flying() && a.grounded) push += tilt * 300;
        for (var m : run.fixtures)
            switch (m.kind()) {
                case CONVEYOR -> {
                    if (!a.flying() && a.grounded && a.platform == null && m.covers(a.x))
                        push += m.direction() * 115;
                }
                case FAN -> {
                    if (m.active() && m.covers(a.x))
                        push += m.direction() * (a.flying() ? 140 : 240);
                }
                default -> {}
            }
        return push;
    }

    void update(double dt) {
        boolean running = run.phase() == GameRun.Phase.RUNNING;
        for (var m : run.fixtures) {
            m.update(dt);
            if (!running
                    && (m.kind() == Fixture.Kind.PRESS
                            || m.kind() == Fixture.Kind.LASER
                            || m.kind() == Fixture.Kind.FAN)) m.disabled = Math.max(m.disabled, .5);
            switch (m.kind()) {
                case VENT_PAD -> vent(m);
                case PRESS -> press(m);
                case LASER -> laser(m);
                default -> {}
            }
        }
    }

    private void vent(Fixture m) {
        if (!m.active()) return;
        var p = run.player;
        if (p.grounded && p.platform == null && m.covers(p.x) && p.vy >= 0) {
            p.vy = -1320;
            p.grounded = false;
            p.slamming = false;
            m.cooldown = .9;
            run.emit(new GameEvent(GameEvent.Type.MACHINE, m.x(), GameRun.FLOOR, 1, "VENT"));
            return;
        }
        for (var e : run.enemies)
            if (e.alive() && !e.flying() && e.grounded && e.platform == null && m.covers(e.x)) {
                e.vy = -950;
                e.grounded = false;
                m.cooldown = .9;
                run.emit(new GameEvent(GameEvent.Type.MACHINE, m.x(), GameRun.FLOOR, 0, "VENT"));
                return;
            }
    }

    private void press(Fixture m) {
        if (!m.active()) {
            m.struck = false;
            return;
        }
        if (m.struck) return;
        m.struck = true;
        run.emit(new GameEvent(GameEvent.Type.SLAM, m.x(), GameRun.FLOOR, 70, "press"));
        var zone = m.strikeZone();
        if (zone.intersects(run.player.bounds()))
            run.combat.hurtPlayer(22 * run.enemyDamage(), m.x(), null, false);
        for (var e : run.enemies)
            if (e.alive() && !e.flying() && zone.intersects(e.bounds())) {
                run.combat.hitEnemy(e, 70, Combat.Source.MACHINE, 0, m.x());
                if (e.alive() && !e.kind.boss()) {
                    e.state = Enemy.State.STUNNED;
                    e.stateTime = .6;
                }
                expose(e);
            }
    }

    private void laser(Fixture m) {
        if (!m.active()) {
            m.struck = false;
            m.hitEnemies.clear();
            return;
        }
        var zone = m.strikeZone();
        if (!m.struck
                && zone.intersects(run.player.bounds())
                && run.combat.hurtPlayer(14 * run.enemyDamage(), m.x(), null, false)) {
            m.struck = true;
            run.emit(
                    new GameEvent(GameEvent.Type.MACHINE, m.x(), run.player.centerY(), 0, "LASER"));
        }
        for (var e : run.enemies)
            if (e.alive() && zone.intersects(e.bounds()) && m.hitEnemies.add(e.id)) {
                run.combat.hitEnemy(e, 35, Combat.Source.MACHINE, 0, m.x());
                expose(e);
                run.emit(new GameEvent(GameEvent.Type.MACHINE, m.x(), e.centerY(), 0, "LASER"));
            }
    }

    /** Ein Treffer der Raumtechnik legt den Kern eines Wächters kurz frei. */
    private void expose(Enemy e) {
        if (!e.alive() || !e.kind.boss() || e.state == Enemy.State.SPAWNING) return;
        e.state = Enemy.State.RECOVER;
        e.stateTime = 1.6;
        e.telegraph = null;
        run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "EXPOSED"));
    }

    /**
     * @return bereiter Notschalter in Reichweite der Figur oder {@code null}
     */
    Fixture console() {
        for (var m : run.fixtures)
            if (m.kind() == Fixture.Kind.CONSOLE
                    && Math.abs(m.x() - run.player.x) <= CONSOLE_REACH
                    && run.player.grounded) return m;
        return null;
    }

    /**
     * Bedient den nahen Notschalter.
     *
     * @return {@code true}, wenn ein Effekt ausgelöst wurde
     */
    boolean use() {
        var m = console();
        if (m == null || !m.active() || run.phase() != GameRun.Phase.RUNNING) return false;
        m.cooldown = Fixture.CONSOLE_COOLDOWN;
        switch (m.sector()) {
            case 0 -> torpedo(m);
            case 1 -> steam(m);
            case 2 -> cold();
            default -> overload();
        }
        run.emit(
                new GameEvent(GameEvent.Type.MACHINE, m.x(), GameRun.FLOOR, m.sector(), "CONSOLE"));
        return true;
    }

    private void torpedo(Fixture m) {
        int direction = 1;
        double best = Double.MAX_VALUE;
        for (var e : run.enemies)
            if (e.alive() && Math.abs(e.x - m.x()) < best) {
                best = Math.abs(e.x - m.x());
                direction = e.x >= m.x() ? 1 : -1;
            }
        var shot =
                run.shoot(
                        Projectile.Kind.TORPEDO,
                        true,
                        m.x() + direction * 30,
                        GameRun.FLOOR - 60,
                        direction * 620,
                        0,
                        90,
                        16,
                        4);
        shot.homing = true;
        shot.explosionRadius = 220;
        run.emit(GameEvent.at(GameEvent.Type.TORPEDO, m.x(), GameRun.FLOOR - 60));
    }

    private void steam(Fixture m) {
        for (var e : run.enemies)
            if (e.alive() && !e.flying() && e.grounded) {
                run.combat.hitEnemy(e, 40, Combat.Source.EXPLOSION, 280, m.x());
                if (e.alive() && !e.kind.boss()) e.vy = -760;
            }
        run.emit(
                new GameEvent(
                        GameEvent.Type.MACHINE,
                        m.x(),
                        GameRun.FLOOR,
                        run.layout().width(),
                        "STEAM"));
    }

    private void cold() {
        for (var e : run.enemies)
            if (e.alive()) {
                run.combat.applyStatus(e, Status.FREEZE, 2.6);
                run.emit(GameEvent.at(GameEvent.Type.FREEZE, e.x, e.centerY()));
            }
    }

    private void overload() {
        for (var e : run.enemies)
            if (e.alive()) {
                e.statuses.apply(Status.MARK, 6);
                if (!e.kind.boss()) {
                    e.state = Enemy.State.STUNNED;
                    e.stateTime = 1.4;
                    e.telegraph = null;
                }
                run.emit(new GameEvent(GameEvent.Type.SHOCK, e.x, e.centerY(), 50, ""));
            }
        for (var m : run.fixtures) if (m.kind() == Fixture.Kind.LASER) m.disabled = 8;
    }
}
