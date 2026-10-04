package ch.zhaw.abyss.domain;

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
        return p.stats.attackSpeed() * (p.overdriveTime > 0 ? 1.4 : 1) * (1 + frenzyBonus(p));
    }

    /**
     * @param p Figur
     * @return Angriffstempo-Bonus des Blutrauschs
     */
    static double frenzyBonus(Player p) {
        int stacks = p.stacks(Item.BLOODRUSH);
        if (stacks == 0 || p.frenzy == 0) return 0;
        return Math.min(.25 + .1 * stacks, p.frenzy * (.015 + .005 * stacks));
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
        p.waveFired = false;
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
            if (!p.waveFired && p.stacks(Item.BLADE_WAVE) > 0) {
                p.waveFired = true;
                bladeWaves(swing, p.facing);
            }
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

    /**
     * Druckklingen der Klingenwelle: mit dem Mehrfachlader fächern mehrere Klingen auf.
     *
     * @param swing aktueller Schlag
     * @param direction Flugrichtung
     */
    private void bladeWaves(Swing swing, int direction) {
        var p = run.player;
        int stacks = p.stacks(Item.BLADE_WAVE);
        boolean tempest = p.stacks(Item.TEMPEST) > 0;
        // Klingenorkan: ein breiter Fächer aus durchschlagenden Klingen.
        int count = 1 + p.stats.extraProjectiles() + (tempest ? 4 : 0);
        double area = p.stats.area();
        for (int i = 0; i < count; i++) {
            double spread = (i - (count - 1) / 2.0) * (tempest ? .2 : .14);
            var blade =
                    run.shoot(
                            Projectile.Kind.BLADE,
                            true,
                            p.x + direction * 50,
                            p.y - 62,
                            direction * Math.cos(spread) * 980,
                            Math.sin(spread) * 980,
                            swing.damage() * (.4 + .1 * (stacks - 1)),
                            26 * area,
                            .5 + .06 * stacks);
            blade.pierce = tempest ? Integer.MAX_VALUE : 2 + stacks;
            blade.knockback = swing.knockback() * .4;
        }
    }

    private void meleeHits(Swing swing, boolean slam) {
        var p = run.player;
        double area = p.stats.area();
        double reach = swing.reach() * p.stats.reach() * area;
        double height = swing.height() * Math.sqrt(area);
        Bounds zone;
        if (slam) zone = new Bounds(p.x - 60 * area, p.y - 80, 120 * area, 110);
        else if (swing.style() == Swing.Style.AIR_SLASH)
            zone =
                    new Bounds(
                            p.facing > 0 ? p.x - 30 : p.x - reach,
                            p.y - height + 40,
                            reach + 30,
                            height);
        else if (p.weapon == Weapon.SCYTHE && p.swingIndex == 2)
            // Sensenschwung rundherum: trifft vor und hinter der Figur
            zone = new Bounds(p.x - reach, p.y - height - 4, reach * 2, height);
        else
            zone =
                    new Bounds(
                            p.facing > 0 ? p.x - 16 : p.x - reach + 16,
                            p.y - height - 4,
                            reach,
                            height);
        double damage = swing.damage() * (slam ? .5 : 1);
        for (var crate : run.crates)
            if (crate.intact()
                    && !p.swingCrates.contains(crate)
                    && zone.intersects(crate.bounds())) {
                p.swingCrates.add(crate);
                if (crate.hit(damage * p.stats.damage())) run.loot.breakCrate(crate);
            }
        var candidates =
                run.grid()
                        .query(
                                zone.x(),
                                zone.y(),
                                zone.x() + zone.width(),
                                zone.y() + zone.height());
        for (var e : candidates) {
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
                run.combat.chain(e, 15 * p.stacks(Item.ARC_COIL), 300, p.stacks(Item.ARC_COIL));
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
        double radius = 170 * p.stats.area();
        run.emit(new GameEvent(GameEvent.Type.SLAM, p.x, p.y, radius, "player"));
        if (run.phase() != GameRun.Phase.RUNNING) return;
        if (p.stacks(Item.BLADE_WAVE) > 0) {
            bladeWaves(swing, -1);
            bladeWaves(swing, 1);
        }
        for (var e : run.grid().around(p.x, p.y - 60, radius + 120)) {
            if (e.alive()
                    && !e.untargetable()
                    && Math.abs(e.x - p.x) < radius + e.width / 2
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
        double aim = aimAssist(originX, originY);
        int count = 1 + p.stats.extraProjectiles();
        if (p.weapon == Weapon.PLASMA) {
            firePlasma(swing, originX, originY, aim, count);
            return;
        }
        for (int i = 0; i < count; i++) {
            double angle = aim + (i - (count - 1) / 2.0) * .12;
            var harpoon =
                    run.shoot(
                            Projectile.Kind.HARPOON,
                            true,
                            originX,
                            originY,
                            p.facing * Math.cos(angle) * 1150,
                            Math.sin(angle) * 1150,
                            swing.damage() * (1 + .2 * p.stacks(Item.HOMING)),
                            12,
                            1.1);
            harpoon.pierce = (p.swingIndex == 2 ? 3 : 1) + p.stacks(Item.BLADE_WAVE);
            harpoon.knockback = swing.knockback();
            harpoon.homing = p.stacks(Item.HOMING) > 0;
        }
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

    /** Plasmakugeln: explodieren beim Aufprall, die dritte Ladung besonders gross. */
    private void firePlasma(Swing swing, double originX, double originY, double aim, int count) {
        var p = run.player;
        boolean heavy = p.swingIndex == 2;
        for (int i = 0; i < count; i++) {
            double angle = aim + (i - (count - 1) / 2.0) * .12;
            var plasma =
                    run.shoot(
                            Projectile.Kind.PLASMA,
                            true,
                            originX,
                            originY,
                            p.facing * Math.cos(angle) * 880,
                            Math.sin(angle) * 880,
                            swing.damage(),
                            heavy ? 18 : 12,
                            1.2);
            plasma.explosionRadius = (heavy ? 150 : 85) * p.stats.area();
            plasma.homing = p.stacks(Item.HOMING) > 0;
        }
        p.lungeVelocity = p.facing * swing.lunge() * 11;
        run.emit(GameEvent.at(GameEvent.Type.SHOT, originX, originY));
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
                int count = 1 + p.stats.extraProjectiles();
                for (int i = 0; i < count; i++) {
                    var torpedo =
                            run.shoot(
                                    Projectile.Kind.TORPEDO,
                                    true,
                                    p.x + p.facing * 40,
                                    p.y - 60 - i * 26,
                                    p.facing * 540,
                                    (i - (count - 1) / 2.0) * 120,
                                    60,
                                    16,
                                    3);
                    torpedo.homing = true;
                    torpedo.explosionRadius = 185 * p.stats.area();
                }
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
                grenade.explosionRadius = 175 * p.stats.area();
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
        double radius = 300 * p.stats.area();
        run.emit(new GameEvent(GameEvent.Type.PULSE, p.x, p.centerY(), radius - 10, ""));
        for (var e : run.grid().around(p.x, p.centerY(), radius + 100)) {
            if (!e.alive() || Math.hypot(e.x - p.x, e.centerY() - p.centerY()) > radius) continue;
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
        var targets = run.grid().nearest(p.x, p.centerY(), 720, 1 + p.stats.extraProjectiles());
        for (var e : targets) {
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
        }
    }

    // --- Horden-Werkzeuge: Blutrausch, Kreiselmesser, Teslafeld -------------------------------

    /** Umlaufgeschwindigkeit der Kreiselmesser in Radiant pro Sekunde. */
    static final double ORBIT_SPEED = 3.4;

    /**
     * @param p Figur
     * @return Bahnradius der Kreiselmesser
     */
    static double orbitRadius(Player p) {
        return (95 + 10 * Math.min(12, orbitalCount(p))) * p.stats.area();
    }

    /**
     * @param p Figur
     * @return Kreiselmesser aus Modulen und Laufbahn
     */
    static int orbitalCount(Player p) {
        return p.stacks(Item.ORBITAL) + p.meta.orbitals() + (storm(p) ? 6 : 0);
    }

    private static boolean storm(Player p) {
        return p.stacks(Item.STORM_BLADES) > 0;
    }

    /**
     * Positionen der Kreiselmesser als Paare (x, y).
     *
     * @param time Tauchzeit
     * @return Koordinaten, leer ohne Kreiselmesser
     */
    double[] orbitals(double time) {
        var p = run.player;
        int count = orbitalCount(p);
        var result = new double[count * 2];
        double radius = orbitRadius(p);
        // Klingensturm: ein zweiter, weiterer Ring dreht gegenläufig.
        int inner = storm(p) ? (count + 1) / 2 : count;
        for (int i = 0; i < count; i++) {
            boolean outer = i >= inner;
            int ring = outer ? count - inner : inner;
            int slot = outer ? i - inner : i;
            double angle =
                    time * ORBIT_SPEED * (outer ? -1.25 : 1)
                            + slot * Math.PI * 2 / Math.max(1, ring);
            double r = radius * (outer ? 1.6 : 1);
            result[i * 2] = p.x + Math.cos(angle) * r;
            result[i * 2 + 1] = p.centerY() + Math.sin(angle) * r * .7;
        }
        return result;
    }

    /**
     * Wirkt die dauerhaften Horden-Werkzeuge der Figur.
     *
     * @param dt Schrittweite
     */
    void updateHordeWeapons(double dt) {
        var p = run.player;
        if (p.frenzy > 0) {
            p.frenzyTime -= dt;
            if (p.frenzyTime <= 0) {
                p.frenzy = Math.max(0, p.frenzy - 3);
                p.frenzyTime = .5;
            }
        }
        int orbitals = orbitalCount(p);
        if (orbitals > 0) {
            var blades = orbitals(run.elapsed());
            double reach = 34 * p.stats.area();
            for (int i = 0; i < blades.length; i += 2) {
                double bx = blades[i], by = blades[i + 1];
                for (var e : run.grid().around(bx, by, reach + 100)) {
                    if (!e.alive() || e.untargetable()) continue;
                    if (Math.abs(e.x - bx) > reach + e.width / 2
                            || Math.abs(e.centerY() - by) > reach + e.height / 2) continue;
                    double last = p.orbitHits.getOrDefault(e.id, -9.0);
                    if (run.elapsed() - last < .35) continue;
                    p.orbitHits.put(e.id, run.elapsed());
                    run.combat.hitEnemy(e, storm(p) ? 36 : 12, Combat.Source.ORBIT, 140, p.x);
                }
            }
            if (p.orbitHits.size() > 2400) p.orbitHits.clear();
            if (storm(p)) {
                p.stormTime -= dt;
                if (p.stormTime <= 0) {
                    p.stormTime = .9;
                    for (int i = 0; i < blades.length; i += 4) fling(blades[i], blades[i + 1]);
                }
            }
        }
        if (p.stacks(Item.MISSILE_SWARM) > 0) {
            p.missileTime -= dt;
            if (p.missileTime <= 0) {
                p.missileTime = 1.2;
                missiles(p);
            }
        }
        if (p.stacks(Item.ABSOLUTE_ZERO) > 0) {
            p.frostTime -= dt;
            if (p.frostTime <= 0) {
                p.frostTime = 3;
                double radius = 420 * p.stats.area();
                run.combat.explode(
                        p.x,
                        p.centerY(),
                        radius,
                        40,
                        false,
                        true,
                        Status.FREEZE,
                        Combat.Source.ABILITY);
            }
        }
        int tesla = p.stacks(Item.TESLA_FIELD);
        if (tesla > 0) {
            boolean thunder = p.stacks(Item.THUNDERHEAD) > 0;
            p.teslaTime -= dt;
            if (p.teslaTime <= 0) {
                p.teslaTime = thunder ? .4 : 1.2 * Math.pow(.9, tesla - 1);
                double range = 340 * p.stats.area() * (thunder ? 1.5 : 1);
                var targets = run.grid().nearest(p.x, p.centerY(), range, thunder ? 8 : 1 + tesla);
                double damage = (14 + 3 * tesla) * (thunder ? 2.5 : 1);
                for (var e : targets) {
                    // Gewitterkern: der Blitz fällt von der Decke und springt weiter.
                    double fromX = thunder ? e.x + (e.id % 7 - 3) * 12 : p.x;
                    double fromY = thunder ? 30 : p.centerY() - 20;
                    run.emit(GameEvent.link(GameEvent.Type.CHAIN, fromX, fromY, e.x, e.centerY()));
                    run.combat.hitEnemy(e, damage, Combat.Source.CHAIN, 40, p.x);
                    if (thunder && e.alive()) run.combat.chain(e, damage * .6, 300, 3);
                }
            }
        }
    }

    /** Klingensturm: eine Kreiselklinge löst sich und fliegt nach aussen. */
    private void fling(double bx, double by) {
        var p = run.player;
        double angle = Math.atan2(by - p.centerY(), bx - p.x);
        var blade =
                run.shoot(
                        Projectile.Kind.BLADE,
                        true,
                        bx,
                        by,
                        Math.cos(angle) * 760,
                        Math.sin(angle) * 760,
                        20,
                        22 * p.stats.area(),
                        .55);
        blade.pierce = 4;
        blade.knockback = 120;
    }

    /** Raketenschwarm: zielsuchende Minitorpedos fächern über der Figur auf. */
    private void missiles(Player p) {
        int count = 6 + p.stats.extraProjectiles();
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 + (i - (count - 1) / 2.0) * .32;
            var missile =
                    run.shoot(
                            Projectile.Kind.MISSILE,
                            true,
                            p.x,
                            p.y - 90,
                            Math.cos(angle) * 520,
                            Math.sin(angle) * 520,
                            26,
                            9,
                            2.4);
            missile.homing = true;
            missile.steerStart = .15;
            missile.explosionRadius = 90 * p.stats.area();
        }
        run.emit(GameEvent.at(GameEvent.Type.TORPEDO, p.x, p.y - 90));
    }
}
