package ch.zhaw.abyss.domain;

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
        MACHINE
    }

    private final GameRun run;

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
            case MELEE, PROJECTILE, CHAIN, DASH -> damage *= s.damage() * (p.explorer ? 1.2 : 1);
            case ABILITY, EXPLOSION, DRONE -> damage *= s.abilityDamage();
            case BURN, MACHINE -> {}
        }
        boolean crit = (direct || source == Source.DRONE) && run.rng.nextDouble() < s.critChance();
        if (crit) damage *= s.critDamage();
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
                        source.name()));
        applyKnockback(e, knockback, sourceX, direct);
        if (direct) applyStatuses(e, base);
        if (direct && s.lifesteal() > 0) p.heal(damage * s.lifesteal());
        if (direct && p.overdriveTime > 0) p.addEnergy(2);
        if (crit && p.stacks(Item.LEVIATHAN_TOOTH) > 0) {
            p.heal(2);
            chain(e, 15, 300);
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
        boolean summoned = run.enemies.stream().anyMatch(o -> o.children.contains(e.id));
        int salvage = summoned ? 1 : e.kind.salvage() + (e.affix.elite() ? 8 : 0);
        run.dropScrap(e.x, e.centerY(), salvage);
        if (e.affix.elite() || e.kind == EnemyKind.SMUGGLER)
            run.drop(Pickup.Kind.CORE, 1, e.x, e.centerY());
        if (e.kind == EnemyKind.SMUGGLER) {
            run.smugglersCaught++;
            run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "CAUGHT"));
        }
        if (e.kind.boss()) {
            run.drop(Pickup.Kind.CORE, e.kind == EnemyKind.CAPTAIN ? 5 : 3, e.x, e.centerY());
            for (var minion : run.enemies)
                if (e.children.contains(minion.id) && minion.alive()) {
                    minion.health = 0;
                    run.emit(GameEvent.at(GameEvent.Type.ENEMY_DOWN, minion.x, minion.centerY()));
                }
        }
        p.addEnergy(6 + 4 * p.stacks(Item.SIPHON));
        p.heal(3 * p.stacks(Item.RECOVERY));
        if (p.stacks(Item.DEPTH_RUSH) > 0 && ++p.killsTowardRush >= 10) {
            p.killsTowardRush = 0;
            p.rushStacks += p.stacks(Item.DEPTH_RUSH);
        }
        if (p.stacks(Item.CHAIN_REACTION) > 0)
            explode(e.x, e.centerY(), 115, 20 * p.stacks(Item.CHAIN_REACTION), false, true, null);
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
        }
        p.health = Math.max(0, p.health - damage);
        p.hurtTime = .25;
        p.invulnerableTime = .8;
        p.lastHitTime = 0;
        p.shieldRegenDelay = 4;
        p.swing = null;
        p.slamming = false;
        double direction = p.x == sourceX ? -p.facing : Math.signum(p.x - sourceX);
        p.lungeVelocity = direction * 420;
        if (p.grounded) p.vy = -260;
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
        run.emit(
                new GameEvent(
                        status == Status.FREEZE ? GameEvent.Type.FREEZE : GameEvent.Type.EXPLOSION,
                        x,
                        y,
                        radius,
                        hurtsPlayer ? "hostile" : "friendly"));
        if (hurtsEnemies) {
            for (int i = 0; i < run.enemies.size(); i++) {
                var e = run.enemies.get(i);
                if (!e.alive() || e.untargetable()) continue;
                if (Math.hypot(e.x - x, e.centerY() - y) > radius + e.width / 2) continue;
                if (status != null) applyStatus(e, status, status == Status.FREEZE ? 2.2 : 3);
                hitEnemy(e, damage, Source.EXPLOSION, 260, x);
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
        Enemy target = null;
        double best = range;
        for (var e : run.enemies) {
            if (e == origin || !e.alive() || e.untargetable()) continue;
            double d = Math.hypot(e.x - origin.x, e.centerY() - origin.centerY());
            if (d < best) {
                best = d;
                target = e;
            }
        }
        if (target == null) return;
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
