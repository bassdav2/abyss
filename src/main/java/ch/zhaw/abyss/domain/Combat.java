package ch.zhaw.abyss.domain;

import java.util.ArrayDeque;

/**
 * Schadensregeln des Tauchgangs: Treffer auf Gegner mit Kritik, Panzerung, Zuständen und
 * Modul-Auslösern; Treffer auf die Figur mit Schild, Barriere und Wiederbelebung; Explosionen und
 * Kettenblitze. Wird ausschliesslich von {@link GameRun} und den Gegnerstrategien benutzt.
 */
final class Combat {
    /** Herkunft eines Treffers. Bestimmt, welche Faktoren und Auslöser gelten. */
    enum Source {
        MELEE,
        PROJECTILE,
        ABILITY,
        CHAIN,
        EXPLOSION,
        BURN,
        DASH,
        DRONE,
        /** Raumtechnik: ohne Werte der Figur, durchschlägt Panzerung. */
        MACHINE,
        /** Kreiselmesser: Werkzeugschaden, aber ohne Kritik und Lebensraub. */
        ORBIT
    }

    /** Wartende Explosion; Kettenreaktionen laufen iterativ statt rekursiv. */
    private record Blast(
            double x,
            double y,
            double radius,
            double damage,
            boolean hurtsPlayer,
            boolean hurtsEnemies,
            Status status,
            Source source) {}

    /** Höchstzahl ausgewerteter Explosionen pro Simulationsschritt; der Rest folgt danach. */
    static final int BLASTS_PER_STEP = 160;

    private final GameRun run;
    private final ArrayDeque<Blast> blasts = new ArrayDeque<>();
    private int blastDepth;

    Combat(GameRun run) {
        this.run = run;
    }

    /**
     * Fügt einem Gegner Schaden der spielenden Figur zu.
     *
     * @param e Ziel
     * @param base Grundschaden vor Faktoren
     * @param source Herkunft
     * @param knockback Rückstoss in Einheiten pro Sekunde
     * @param sourceX Herkunft des Treffers für Richtung und Frontalblock
     * @return tatsächlich zugefügter Schaden
     */
    double hitEnemy(Enemy e, double base, Source source, double knockback, double sourceX) {
        if (!e.alive() || e.untargetable() || base <= 0) return 0;
        var p = run.player;
        var s = p.stats;
        boolean direct = source == Source.MELEE || source == Source.PROJECTILE;
        double damage = base;
        switch (source) {
            case MELEE, PROJECTILE, CHAIN, DASH, ORBIT ->
                    damage *= s.damage() * (p.explorer ? 1.2 : 1);
            case ABILITY, EXPLOSION, DRONE -> damage *= s.abilityDamage();
            case BURN, MACHINE -> {}
        }
        int tier =
                direct || source == Source.DRONE
                        ? StatSheet.critTier(s.critChance(), run.rng.nextDouble())
                        : 0;
        boolean crit = tier > 0;
        if (crit) {
            // Überkritik: jede weitere Stufe legt 60 % des kritischen Schadens drauf.
            damage *= s.critDamage() * (1 + .6 * (tier - 1));
            if (tier > 1 && p.stacks(Item.DEATH_EYE) > 0) damage *= 1.5;
        }
        if (direct && p.afterburner) {
            damage *= 1.6;
            p.afterburner = false;
        }
        if (source != Source.BURN) damage *= situational(e);
        if (e.statuses.active(Status.MARK)) damage *= 1.5;
        if (source != Source.BURN && source != Source.MACHINE && blocksFront(e, sourceX)) {
            damage *= .15;
            run.emit(GameEvent.at(GameEvent.Type.BLOCK, e.x, e.centerY()));
        }
        if (e.armored() && source != Source.MACHINE) damage *= e.kind.armor();
        boolean plain =
                !crit
                        && (source == Source.MELEE
                                || source == Source.PROJECTILE
                                || source == Source.ORBIT
                                || source == Source.DASH
                                || source == Source.DRONE);
        if (e.plated && plain) damage *= .35;
        // Im Panzerschwarm ersetzt der Panzer die Schale, sonst wären Krabben fast unverwundbar.
        if (e.kind == EnemyKind.CRAB && plain && !e.plated && shellFront(e, sourceX)) {
            damage *= .2;
            if (run.rng.nextInt(4) == 0)
                run.emit(GameEvent.at(GameEvent.Type.BLOCK, e.x, e.centerY()));
        }
        if (p.stacks(Item.ABSOLUTE_ZERO) > 0 && e.statuses.active(Status.FREEZE)) damage *= 2;
        if (e.affix == Affix.ARMORED) damage *= .6;
        if (e.eliteShield > 0) {
            double absorbed = Math.min(e.eliteShield, damage);
            e.eliteShield -= absorbed;
            damage -= absorbed;
        }
        e.health = Math.max(0, e.health - damage);
        e.regenDelay = 4;
        if (source != Source.BURN) e.hurtTime = .14;
        run.emit(
                new GameEvent(
                        crit ? GameEvent.Type.CRIT : GameEvent.Type.HIT,
                        e.x,
                        e.y - e.height * .6,
                        damage,
                        tier > 1 ? source.name() + "#" + tier : source.name()));
        applyKnockback(e, knockback, sourceX, direct);
        if (direct) applyStatuses(e, base);
        if (direct && s.lifesteal() > 0) p.heal(damage * s.lifesteal());
        if (direct && p.overdriveTime > 0) p.addEnergy(2);
        if (crit && p.stacks(Item.LEVIATHAN_TOOTH) > 0) {
            p.heal(2);
            chain(e, 15, 300);
        }
        if (crit && p.stacks(Item.DEATH_EYE) > 0 && p.deathEyeTime <= 0) {
            p.deathEyeTime = .08;
            // Ohne erneute Faktoren: der kritische Schaden ist schon eingerechnet, Panzer wirkt.
            explode(e.x, e.centerY(), 70 * s.area(), damage * .3, false, true, null, Source.BURN);
        }
        if (!e.alive()) killed(e);
        return damage;
    }

    private double situational(Enemy e) {
        var p = run.player;
        double factor = 1 + .03 * p.rushStacks;
        if (p.health < p.maxHealth * .35) factor *= 1 + .3 * p.stacks(Item.ADRENALINE);
        boolean behind = e.facing == (int) Math.signum(e.x - p.x);
        if (behind) factor *= 1 + .35 * p.stacks(Item.AMBUSH);
        boolean lit =
                (e.x - p.x) * p.facing > 0
                        && Math.abs(e.x - p.x) < 340
                        && Math.abs(e.centerY() - (p.y - 100)) < 130;
        if (lit) factor *= 1 + .2 * p.stacks(Item.LANTERN);
        return factor;
    }

    /** Die Schale der Panzerkrabbe schützt vorn, solange sie nicht ausholt. */
    private static boolean shellFront(Enemy e, double sourceX) {
        return e.state != Enemy.State.STRIKE
                && e.state != Enemy.State.STUNNED
                && Math.signum(sourceX - e.x) == e.facing;
    }

    private static boolean blocksFront(Enemy e, double sourceX) {
        return e.kind == EnemyKind.SHIELDBEARER
                && e.state != Enemy.State.RECOVER
                && e.state != Enemy.State.STUNNED
                && Math.signum(sourceX - e.x) == e.facing;
    }

    /**
     * Negativer Rückstoss zieht den Gegner vor die Figur. Der Rückstoss klingt mit {@code e^-9t}
     * ab, daher legt ein Anfangstempo von 9 · d ungefähr die Strecke d zurück.
     */
    private void pull(Enemy e, double sourceX) {
        if (e.kind == EnemyKind.TURRET || e.state == Enemy.State.HIDDEN) return;
        double factor = e.kind.boss() ? .1 : e.affix.elite() ? .6 : 1;
        double gap = Math.abs(e.x - sourceX) - e.width / 2 - 50;
        if (gap <= 0) return;
        e.knockVx = -Math.signum(e.x - sourceX) * 9 * gap * factor;
        if (e.kind.boss()) return;
        e.state = Enemy.State.STUNNED;
        e.stateTime = .35;
    }

    private void applyKnockback(Enemy e, double knockback, double sourceX, boolean direct) {
        if (knockback < 0) {
            pull(e, sourceX);
            return;
        }
        if (knockback <= 0 || e.kind == EnemyKind.TURRET || e.state == Enemy.State.HIDDEN) return;
        double direction = e.x == sourceX ? run.player.facing : Math.signum(e.x - sourceX);
        double factor = e.kind.boss() ? .12 : e.affix.elite() ? .5 : 1;
        e.knockVx = direction * knockback * run.player.stats.knockback() * factor;
        if (e.kind.boss()) return;
        if (e.state == Enemy.State.APPROACH || e.state == Enemy.State.RECOVER) {
            e.state = Enemy.State.STUNNED;
            e.stateTime = .2;
        } else if (direct
                && e.state == Enemy.State.WINDUP
                && run.rng.nextDouble() < .25 * run.player.stacks(Item.BALLAST)) {
            e.state = Enemy.State.STUNNED;
            e.stateTime = .4;
        }
    }

    private void applyStatuses(Enemy e, double base) {
        var s = run.player.stats;
        if (s.burnChance() > 0 && run.rng.nextDouble() < s.burnChance())
            e.statuses.ignite(3, (5 + base * .15) * s.burnPower());
        if (s.chillChance() > 0 && run.rng.nextDouble() < s.chillChance()) {
            e.statuses.apply(Status.CHILL, 2.5);
            run.emit(GameEvent.at(GameEvent.Type.FREEZE, e.x, e.centerY()));
        }
    }

    /**
     * Wendet einen Zustand an; Bosse werden statt eingefroren nur verlangsamt.
     *
     * @param e Ziel
     * @param status Zustand
     * @param seconds Dauer
     */
    void applyStatus(Enemy e, Status status, double seconds) {
        if (status == Status.FREEZE && e.kind.boss()) e.statuses.apply(Status.CHILL, seconds * 1.5);
        else e.statuses.apply(status, seconds);
    }

    private void killed(Enemy e) {
        var p = run.player;
        run.countKill();
        run.weaponKills.merge(p.weapon, 1, Integer::sum);
        boolean summoned = e.parentId != 0;
        int salvage = summoned ? 1 : e.kind.salvage() + (e.affix.elite() ? 8 : 0);
        if (salvage > 0) run.dropScrap(e.x, e.centerY(), salvage);
        double shard =
                e.kind.boss()
                        ? 30
                        : e.kind.swarm() || summoned
                                ? 1
                                : 1 + e.kind.threat() + (e.affix.elite() ? 8 : 0);
        run.drop(Pickup.Kind.SHARD, shard, e.x, e.centerY());
        if (e.affix.elite() || e.kind == EnemyKind.SMUGGLER)
            run.drop(Pickup.Kind.CORE, 1, e.x, e.centerY());
        if (e.kind == EnemyKind.HIVE)
            // Fällt ein Nest, stirbt seine Brut mit.
            for (var minion : run.enemies)
                if (e.children.contains(minion.id) && minion.alive()) {
                    minion.health = 0;
                    run.emit(
                            new GameEvent(
                                    GameEvent.Type.ENEMY_DOWN,
                                    minion.x,
                                    minion.centerY(),
                                    minion.kind.ordinal(),
                                    ""));
                }
        if (e.kind == EnemyKind.SMUGGLER) {
            run.smugglersCaught++;
            run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "CAUGHT"));
        }
        if (e.kind.boss()) {
            run.cancelHordes();
            run.drop(Pickup.Kind.CORE, e.kind == EnemyKind.CAPTAIN ? 5 : 3, e.x, e.centerY());
            for (var minion : run.enemies)
                if (e.children.contains(minion.id) && minion.alive()) {
                    minion.health = 0;
                    run.emit(GameEvent.at(GameEvent.Type.ENEMY_DOWN, minion.x, minion.centerY()));
                }
        }
        p.addEnergy(6 + 4 * p.stacks(Item.SIPHON));
        p.heal(3 * p.stacks(Item.RECOVERY));
        if (p.stacks(Item.BLOOD_PACT) > 0) p.heal(p.maxHealth * .01);
        if (p.stacks(Item.PHASE_STORM) > 0 && p.dashTime > 0) p.dashCooldown = 0;
        if (p.stacks(Item.INFERNO) > 0 && e.statuses.active(Status.BURN)) spread(e);
        if (p.stacks(Item.DEPTH_RUSH) > 0 && ++p.killsTowardRush >= 10) {
            p.killsTowardRush = 0;
            p.rushStacks += p.stacks(Item.DEPTH_RUSH);
        }
        if (p.stacks(Item.BLOODRUSH) > 0) {
            p.frenzy = Math.min(20, p.frenzy + 1);
            p.frenzyTime = 4;
        }
        boolean supernova = p.stacks(Item.SUPERNOVA) > 0;
        int novaEvery = supernova ? 5 : 14 - 3 * p.stacks(Item.NOVA);
        if (p.stacks(Item.NOVA) > 0 && ++p.novaKills >= novaEvery) {
            p.novaKills = 0;
            double radius =
                    (260 + 50 * p.stacks(Item.NOVA)) * p.stats.area() * (supernova ? 1.5 : 1);
            run.emit(new GameEvent(GameEvent.Type.NOVA, p.x, p.centerY(), radius, ""));
            explode(
                    p.x,
                    p.centerY(),
                    radius,
                    (30 + 25 * p.stacks(Item.NOVA)) * (supernova ? 2 : 1),
                    false,
                    true,
                    null);
        }
        if (p.stacks(Item.CHAIN_REACTION) > 0)
            explode(
                    e.x,
                    e.centerY(),
                    (100 + 15 * p.stacks(Item.CHAIN_REACTION))
                            * p.stats.area()
                            * (supernova ? 2 : 1),
                    20 * p.stacks(Item.CHAIN_REACTION) * (supernova ? 2 : 1),
                    false,
                    true,
                    null);
        if (e.kind == EnemyKind.FUSE)
            // Eine erwischte Zündmilbe zündet gegen ihre Nachbarn.
            explode(e.x, e.centerY(), 80 * p.stats.area(), 12, false, true, null);
        if (e.kind == EnemyKind.BOMBER) explode(e.x, e.centerY(), 130, 30, false, true, null);
        if (e.affix == Affix.VOLATILE)
            explode(e.x, e.centerY(), 125, 16 * run.enemyDamage(), true, false, null);
        run.emit(
                new GameEvent(
                        e.kind.boss()
                                ? GameEvent.Type.BOSS_DOWN
                                : e.affix.elite()
                                        ? GameEvent.Type.ELITE_DOWN
                                        : GameEvent.Type.ENEMY_DOWN,
                        e.x,
                        e.y - e.height * .5,
                        e.kind.ordinal(),
                        e.kind.title()));
    }

    /** Höllenglut: ein brennender Gegner steckt beim Tod seine Nachbarn an. */
    private void spread(Enemy e) {
        var s = run.player.stats;
        int lit = 0;
        for (var other : run.grid().around(e.x, e.centerY(), 170)) {
            if (other == e || !other.alive() || lit >= 6) continue;
            if (Math.hypot(other.x - e.x, other.centerY() - e.centerY()) > 170) continue;
            other.statuses.ignite(3, 8 * s.burnPower());
            lit++;
        }
        if (lit > 0) run.emit(GameEvent.at(GameEvent.Type.FLAME, e.x, e.centerY()));
    }

    /**
     * Fügt der spielenden Figur Schaden zu.
     *
     * @param amount bereits skalierter Schaden
     * @param sourceX Herkunft für den Rückstoss
     * @param source angreifender Gegner oder {@code null}
     * @param melee {@code true} für Nahkampf, relevant für Dornenpanzer und Druckschild
     * @return {@code true}, wenn der Treffer angenommen wurde
     */
    boolean hurtPlayer(double amount, double sourceX, Enemy source, boolean melee) {
        var p = run.player;
        boolean swarm = source != null && source.kind.swarm();
        if (!p.alive() || p.invulnerableTime > 0 || run.phase() != GameRun.Phase.RUNNING)
            return false;
        if (p.barrierCharges > 0) {
            p.barrierCharges--;
            p.invulnerableTime = .6;
            run.emit(GameEvent.at(GameEvent.Type.BLOCK, p.x, p.centerY()));
            return false;
        }
        double damage = amount * p.stats.damageTaken() * (p.shieldTime > 0 ? .25 : 1);
        if (p.shield > 0) {
            double absorbed = Math.min(p.shield, damage);
            p.shield -= absorbed;
            damage -= absorbed;
            run.emit(new GameEvent(GameEvent.Type.SHIELD_HIT, p.x, p.centerY(), absorbed, ""));
            if (p.shield <= 0 && p.stacks(Item.BASTION) > 0 && p.bastionTime <= 0) {
                // Bollwerk: der brechende Schild schlägt zurück und schützt kurz.
                p.bastionTime = 6;
                damage = 0;
                p.invulnerableTime = 1.2;
                double radius = 320 * p.stats.area();
                run.emit(new GameEvent(GameEvent.Type.NOVA, p.x, p.centerY(), radius, "bastion"));
                explode(p.x, p.centerY(), radius, 60, false, true, null);
                return true;
            }
        }
        p.health = Math.max(0, p.health - damage);
        p.hurtTime = .25;
        p.invulnerableTime = swarm ? .5 : .8;
        p.lastHitTime = 0;
        p.shieldRegenDelay = 4;
        double direction = p.x == sourceX ? -p.facing : Math.signum(p.x - sourceX);
        if (!swarm) {
            // Schwarmbisse unterbrechen keine Angriffe, sonst wäre man in Horden wehrlos.
            p.swing = null;
            p.slamming = false;
            p.lungeVelocity = direction * 420;
            if (p.grounded) p.vy = -260;
        }
        run.emit(new GameEvent(GameEvent.Type.PLAYER_HIT, p.x, p.y - 70, damage, ""));
        if (melee && source != null && source.alive()) {
            if (p.stacks(Item.SPIKES) > 0)
                hitEnemy(source, 14 * p.stacks(Item.SPIKES), Source.BURN, 0, p.x);
            if (p.shieldTime > 0) source.knockVx = -direction * 520;
        }
        if (p.stacks(Item.VALVE) > 0)
            explode(p.x, p.centerY(), 160, 25 * p.stacks(Item.VALVE), false, true, null);
        if (p.health <= 0 && p.stacks(Item.SECOND_HEART) > 0 && !p.reviveUsed) {
            p.reviveUsed = true;
            p.health = p.maxHealth * .5;
            p.invulnerableTime = 2.2;
            run.emit(GameEvent.at(GameEvent.Type.REVIVE, p.x, p.centerY()));
            explode(p.x, p.centerY(), 260, 40, false, true, null);
        }
        return true;
    }

    /**
     * Löst eine Explosion aus.
     *
     * @param x Mittelpunkt
     * @param y Mittelpunkt
     * @param radius Radius
     * @param damage Schaden
     * @param hurtsPlayer trifft die spielende Figur
     * @param hurtsEnemies trifft Gegner und Kisten
     * @param status optionaler Zustand für getroffene Gegner
     */
    void explode(
            double x,
            double y,
            double radius,
            double damage,
            boolean hurtsPlayer,
            boolean hurtsEnemies,
            Status status) {
        explode(x, y, radius, damage, hurtsPlayer, hurtsEnemies, status, Source.EXPLOSION);
    }

    /**
     * Explosion mit wählbarer Schadensherkunft, etwa Werkzeugschaden für Plasmakugeln.
     *
     * @param x Mittelpunkt
     * @param y Mittelpunkt
     * @param radius Radius
     * @param damage Grundschaden
     * @param hurtsPlayer trifft die Figur
     * @param hurtsEnemies trifft Gegner
     * @param status Zustand für getroffene Gegner oder {@code null}
     * @param source Herkunft der Treffer auf Gegner
     */
    void explode(
            double x,
            double y,
            double radius,
            double damage,
            boolean hurtsPlayer,
            boolean hurtsEnemies,
            Status status,
            Source source) {
        blasts.add(new Blast(x, y, radius, damage, hurtsPlayer, hurtsEnemies, status, source));
        if (blastDepth == 0) flush();
    }

    /**
     * Wertet wartende Explosionen aus. Explosionen, die währenddessen durch Abschüsse entstehen,
     * werden angehängt statt verschachtelt ausgeführt; mehr als {@value #BLASTS_PER_STEP} pro
     * Schritt werden auf den nächsten Schritt verschoben.
     */
    void flush() {
        if (blastDepth > 0) return;
        blastDepth++;
        try {
            int budget = BLASTS_PER_STEP;
            while (!blasts.isEmpty() && budget-- > 0) detonate(blasts.poll());
        } finally {
            blastDepth--;
        }
    }

    private void detonate(Blast b) {
        double x = b.x(), y = b.y(), radius = b.radius(), damage = b.damage();
        boolean hurtsPlayer = b.hurtsPlayer(), hurtsEnemies = b.hurtsEnemies();
        Status status = b.status();
        run.emit(
                new GameEvent(
                        status == Status.FREEZE ? GameEvent.Type.FREEZE : GameEvent.Type.EXPLOSION,
                        x,
                        y,
                        radius,
                        hurtsPlayer ? "hostile" : "friendly"));
        if (hurtsEnemies) {
            for (var e : run.grid().around(x, y, radius + 120)) {
                if (!e.alive() || e.untargetable()) continue;
                if (Math.hypot(e.x - x, e.centerY() - y) > radius + e.width / 2) continue;
                if (status != null) applyStatus(e, status, status == Status.FREEZE ? 2.2 : 3);
                hitEnemy(e, damage, b.source(), 260, x);
            }
            for (var crate : run.crates)
                if (crate.intact()
                        && Math.hypot(crate.x() - x, crate.y() - 24 - y) < radius + 26
                        && crate.hit(999)) run.breakCrate(crate);
        }
        if (hurtsPlayer
                && Math.hypot(run.player.x - x, run.player.centerY() - y)
                        < radius + run.player.width / 2) hurtPlayer(damage, x, null, false);
    }

    /**
     * Springt vom Ursprung auf den nächsten weiteren Gegner über.
     *
     * @param origin Ausgangsgegner
     * @param damage Grundschaden
     * @param range maximale Reichweite
     */
    void chain(Enemy origin, double damage, double range) {
        chain(origin, damage, range, 1);
    }

    /**
     * Kettenblitz über mehrere Sprünge; jeder Gegner wird höchstens einmal getroffen.
     *
     * @param origin Ausgangsgegner
     * @param damage Schaden je Sprung
     * @param range Sprungweite
     * @param jumps Anzahl Sprünge
     */
    void chain(Enemy origin, double damage, double range, int jumps) {
        var visited = new java.util.HashSet<Long>();
        visited.add(origin.id);
        var from = origin;
        for (int jump = 0; jump < jumps; jump++) {
            Enemy target = null;
            double best = range;
            for (var e : run.grid().around(from.x, from.centerY(), range)) {
                if (visited.contains(e.id) || !e.alive() || e.untargetable()) continue;
                double d = Math.hypot(e.x - from.x, e.centerY() - from.centerY());
                if (d < best) {
                    best = d;
                    target = e;
                }
            }
            if (target == null) return;
            visited.add(target.id);
            link(from, target, damage);
            from = target;
        }
    }

    /**
     * Zeichnet einen Blitz von Gegner zu Gegner und trifft das Ziel.
     *
     * @param origin Ausgangspunkt
     * @param target Ziel
     * @param damage Schaden
     */
    private void link(Enemy origin, Enemy target, double damage) {
        run.emit(
                GameEvent.link(
                        GameEvent.Type.CHAIN,
                        origin.x,
                        origin.centerY(),
                        target.x,
                        target.centerY()));
        hitEnemy(target, damage, Source.CHAIN, 60, origin.x);
    }
}
