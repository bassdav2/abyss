package ch.zhaw.abyss.domain;

import java.util.Comparator;

/**
 * Angriffe der spielenden Figur: Waffenkombinationen mit Aushol-, Treffer- und Nachlauffenster,
 * Luftangriffe, Bodenstampfer, Harpunen mit Zielhilfe, aktive Module und die Begleitdrohne.
 */
final class Arsenal {
    private final GameRun run;

    Arsenal(GameRun run) {
        this.run = run;
    }

    /**
     * Führt laufende Angriffe fort und startet neue Angriffe oder Module.
     *
     * @param dt Sekunden
     * @param input Eingaben
     */
    void update(double dt, InputFrame input) {
        var p = run.player;
        updateSwing(dt);
        if (input.attack() && canAttack()) startSwing();
        if (input.ability()
                && run.phase() == GameRun.Phase.RUNNING
                && p.abilityCooldown <= 0
                && p.energy >= p.module.cost()) useAbility();
    }

    private boolean canAttack() {
        var p = run.player;
        if (p.dashTime > 0 || p.slamming) return false;
        return p.swing == null || p.swingTime >= p.swing.cancelTime();
    }

    private double attackSpeed() {
        var p = run.player;
        return p.stats.attackSpeed() * (p.overdriveTime > 0 ? 1.4 : 1);
    }

    private void startSwing() {
        var p = run.player;
        boolean air = !p.grounded;
        var combo = p.weapon.combo();
        Swing swing;
        if (air) {
            swing = p.weapon.air();
            p.swingIndex = -1;
        } else {
            if (p.comboTimer <= 0) p.comboStep = 0;
            p.swingIndex = p.comboStep % combo.size();
            swing = combo.get(p.swingIndex);
            p.comboStep = (p.swingIndex + 1) % combo.size();
        }
        p.swing = swing;
        p.swingTime = 0;
        p.swingHits.clear();
        p.swingCrates.clear();
        p.swingFired = false;
        p.chainedThisSwing = false;
        p.comboTimer = swing.duration() / attackSpeed() + .4;
        if (!air && !swing.ranged()) p.lungeVelocity = p.facing * swing.lunge() * 11;
        if (swing.style() == Swing.Style.SLAM) {
            p.slamming = true;
            p.vy = 1650;
        }
        run.emit(
                new GameEvent(
                        GameEvent.Type.SWING,
                        p.x + p.facing * 50,
                        p.y - 60,
                        p.swingIndex,
                        swing.style().name()));
    }

    private void updateSwing(double dt) {
        var p = run.player;
        var swing = p.swing;
        if (swing == null) return;
        p.swingTime += dt * attackSpeed();
        if (swing.style() == Swing.Style.SLAM) {
            if (p.slamming)
                p.swingTime = Math.min(p.swingTime, swing.windup() + swing.active() * .5);
            if (p.slamming && run.phase() == GameRun.Phase.RUNNING) meleeHits(swing, true);
        } else if (swing.ranged()) {
            if (!p.swingFired && p.swingTime >= swing.windup()) {
                p.swingFired = true;
                fireHarpoon(swing);
            }
        } else if (p.swingTime >= swing.windup()
                && p.swingTime <= swing.windup() + swing.active()) {
            meleeHits(swing, false);
            if (p.weapon == Weapon.ANCHOR && p.swingIndex == 1 && !p.swingFired) anchorWaves(swing);
        }
        if (p.swingTime >= swing.duration() && !p.slamming) p.swing = null;
    }

    private void anchorWaves(Swing swing) {
        var p = run.player;
        p.swingFired = true;
        for (int side : new int[] {-1, 1}) {
            var wave =
                    run.shoot(
                            Projectile.Kind.SHOCKWAVE,
                            true,
                            p.x + side * 60,
                            p.y - 18,
                            side * 620,
                            0,
                            swing.damage() * .6,
                            24,
                            .7);
            wave.knockback = 300;
        }
        run.emit(new GameEvent(GameEvent.Type.SLAM, p.x + p.facing * 80, p.y, 120, "player"));
    }

    private void meleeHits(Swing swing, boolean slam) {
        var p = run.player;
        double reach = swing.reach() * p.stats.reach();
        Bounds zone;
        if (slam) zone = new Bounds(p.x - 60, p.y - 80, 120, 110);
        else if (swing.style() == Swing.Style.AIR_SLASH)
            zone =
                    new Bounds(
                            p.facing > 0 ? p.x - 30 : p.x - reach,
                            p.y - swing.height() + 40,
                            reach + 30,
                            swing.height());
        else
            zone =
                    new Bounds(
                            p.facing > 0 ? p.x - 16 : p.x - reach + 16,
                            p.y - swing.height() - 4,
                            reach,
                            swing.height());
        double damage = swing.damage() * (slam ? .5 : 1);
        for (var crate : run.crates)
            if (crate.intact()
                    && !p.swingCrates.contains(crate)
                    && zone.intersects(crate.bounds())) {
                p.swingCrates.add(crate);
                if (crate.hit(damage * p.stats.damage())) run.loot.breakCrate(crate);
            }
        for (int i = 0; i < run.enemies.size(); i++) {
            var e = run.enemies.get(i);
            if (!e.alive() || e.untargetable() || p.swingHits.contains(e.id)) continue;
            if (!zone.intersects(e.bounds())) continue;
            p.swingHits.add(e.id);
            run.combat.hitEnemy(e, damage, Combat.Source.MELEE, swing.knockback(), p.x);
            if (p.chainedThisSwing) continue;
            if (p.weapon.chains()) {
                p.chainedThisSwing = true;
                run.combat.chain(e, damage * .5, 260);
            }
            if (p.swingIndex == 2 && p.stacks(Item.ARC_COIL) > 0) {
                p.chainedThisSwing = true;
                run.combat.chain(e, 15 * p.stacks(Item.ARC_COIL), 300);
            }
        }
    }

    /** Beendet einen Bodenstampfer bei der Landung mit Flächenschaden. */
    void slamImpact() {
        var p = run.player;
        var swing = p.swing;
        p.slamming = false;
        if (swing == null) return;
        p.swingTime = swing.windup() + swing.active();
        run.emit(new GameEvent(GameEvent.Type.SLAM, p.x, p.y, 170, "player"));
        if (run.phase() != GameRun.Phase.RUNNING) return;
        for (int i = 0; i < run.enemies.size(); i++) {
            var e = run.enemies.get(i);
            if (e.alive()
                    && !e.untargetable()
                    && Math.abs(e.x - p.x) < 170 + e.width / 2
                    && Math.abs(e.y - p.y) < 140)
                run.combat.hitEnemy(e, swing.damage(), Combat.Source.MELEE, swing.knockback(), p.x);
        }
        for (var crate : run.crates)
            if (crate.intact() && Math.abs(crate.x() - p.x) < 170 && crate.hit(999))
                run.loot.breakCrate(crate);
    }

    private void fireHarpoon(Swing swing) {
        var p = run.player;
        double originX = p.x + p.facing * 40, originY = p.y - 62;
        double angle = aimAssist(originX, originY);
        var harpoon =
                run.shoot(
                        Projectile.Kind.HARPOON,
                        true,
                        originX,
                        originY,
                        p.facing * Math.cos(angle) * 1150,
                        Math.sin(angle) * 1150,
                        swing.damage() * (p.stacks(Item.HOMING) > 0 ? 1.2 : 1),
                        12,
                        1.1);
        harpoon.pierce = p.swingIndex == 2 ? 3 : 1;
        harpoon.knockback = swing.knockback();
        harpoon.homing = p.stacks(Item.HOMING) > 0;
        p.lungeVelocity = p.facing * swing.lunge() * 11;
        run.emit(GameEvent.at(GameEvent.Type.HARPOON, originX, originY));
    }

    /**
     * Zielhilfe für Fernwaffen: neigt den Schuss bis 35 Grad zum nächsten Gegner in Blickrichtung.
     *
     * @return vertikaler Winkel in Radiant, positiv nach unten
     */
    private double aimAssist(double originX, double originY) {
        var p = run.player;
        Enemy best = null;
        double bestDistance = 900;
        for (var e : run.enemies) {
            if (!e.alive() || e.untargetable()) continue;
            double dx = (e.x - originX) * p.facing;
            if (dx < 20 || dx > 900) continue;
            double dy = e.centerY() - originY;
            if (Math.abs(Math.atan2(dy, dx)) > .62) continue;
            if (dx < bestDistance) {
                bestDistance = dx;
                best = e;
            }
        }
        if (best == null) return 0;
        return Math.atan2(best.centerY() - originY, (best.x - originX) * p.facing);
    }

    private void useAbility() {
        var p = run.player;
        p.energy -= p.module.cost();
        p.abilityCooldown = p.abilityCooldownTotal();
        switch (p.module) {
            case PULSE -> pulse(p);
            case ARC -> {
                run.shoot(
                        Projectile.Kind.ARC,
                        true,
                        p.x + p.facing * 45,
                        p.y - 55,
                        p.facing * 960,
                        0,
                        39,
                        20,
                        2);
                run.emit(GameEvent.at(GameEvent.Type.ARC, p.x, p.y - 70));
            }
            case AEGIS -> {
                p.shieldTime = 4;
                run.emit(GameEvent.at(GameEvent.Type.SHIELD, p.x, p.centerY()));
            }
            case TORPEDO -> {
                var torpedo =
                        run.shoot(
                                Projectile.Kind.TORPEDO,
                                true,
                                p.x + p.facing * 40,
                                p.y - 60,
                                p.facing * 540,
                                0,
                                60,
                                16,
                                3);
                torpedo.homing = true;
                torpedo.explosionRadius = 185;
                run.emit(GameEvent.at(GameEvent.Type.TORPEDO, p.x, p.y - 60));
            }
            case SONAR -> {
                for (var e : run.enemies) if (e.alive()) e.statuses.apply(Status.MARK, 6);
                run.emit(new GameEvent(GameEvent.Type.SONAR, p.x, p.centerY(), 1200, ""));
            }
            case CRYO -> {
                var grenade =
                        run.shoot(
                                Projectile.Kind.CRYO,
                                true,
                                p.x + p.facing * 30,
                                p.y - 90,
                                p.facing * 620,
                                -520,
                                20,
                                12,
                                3);
                grenade.gravity = 1500;
                grenade.explosionRadius = 175;
                grenade.status = Status.FREEZE;
                run.emit(GameEvent.at(GameEvent.Type.SHOT, p.x, p.y - 90));
            }
            case DRONE -> {
                p.droneTime = 12;
                p.droneCooldown = .3;
                run.emit(GameEvent.at(GameEvent.Type.DRONE, p.x, p.y - 150));
            }
            case OVERDRIVE -> {
                p.overdriveTime = 6;
                run.emit(GameEvent.at(GameEvent.Type.OVERDRIVE, p.x, p.centerY()));
            }
        }
    }

    private void pulse(Player p) {
        run.emit(new GameEvent(GameEvent.Type.PULSE, p.x, p.centerY(), 290, ""));
        for (int i = 0; i < run.enemies.size(); i++) {
            var e = run.enemies.get(i);
            if (!e.alive() || Math.hypot(e.x - p.x, e.centerY() - p.centerY()) > 300) continue;
            run.combat.hitEnemy(e, 34, Combat.Source.ABILITY, 380, p.x);
            if (!e.kind.boss() && e.alive() && e.state != Enemy.State.HIDDEN) {
                e.state = Enemy.State.STUNNED;
                e.stateTime = 1.1;
            }
        }
        run.projectiles.removeIf(
                q -> !q.friendly && Math.hypot(q.x - p.x, q.y - p.centerY()) < 300);
    }

    /**
     * Lässt die Begleitdrohne auf den nächsten Gegner feuern.
     *
     * @param dt Sekunden
     */
    void updateDrone(double dt) {
        var p = run.player;
        if (p.droneTime <= 0) return;
        p.droneTime = Math.max(0, p.droneTime - dt);
        p.droneCooldown -= dt;
        if (p.droneCooldown > 0 || run.phase() != GameRun.Phase.RUNNING) return;
        double originX = p.x - p.facing * 40, originY = p.y - 150;
        run.enemies.stream()
                .filter(e -> e.alive() && !e.untargetable())
                .filter(e -> Math.abs(e.x - p.x) < 720)
                .min(Comparator.comparingDouble(e -> Math.abs(e.x - p.x)))
                .ifPresent(
                        e -> {
                            double angle = Math.atan2(e.centerY() - originY, e.x - originX);
                            run.shoot(
                                    Projectile.Kind.DRONE_SHOT,
                                    true,
                                    originX,
                                    originY,
                                    Math.cos(angle) * 900,
                                    Math.sin(angle) * 900,
                                    9,
                                    7,
                                    1.2);
                            p.droneCooldown = .42;
                        });
    }
}
