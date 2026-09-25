package ch.zhaw.abyss.domain;

/**
 * Bewegung der spielenden Figur: Zeitgeber, Energie- und Schildregeneration, Blickrichtung, Sprünge
 * mit Koyotezeit und Sprungpuffer, Luftsprünge, Abtauchen durch Laufstege und Ausweichen.
 */
final class PlayerMotor {
    private final GameRun run;

    PlayerMotor(GameRun run) {
        this.run = run;
    }

    /**
     * Lässt Zeitgeber ablaufen und regeneriert Energie und Schild.
     *
     * @param dt Sekunden
     */
    void tick(double dt) {
        var p = run.player;
        var s = p.stats;
        p.dashTime = Math.max(0, p.dashTime - dt);
        p.dashCooldown = Math.max(0, p.dashCooldown - dt);
        p.invulnerableTime = Math.max(0, p.invulnerableTime - dt);
        p.abilityCooldown = Math.max(0, p.abilityCooldown - dt);
        p.shieldTime = Math.max(0, p.shieldTime - dt);
        p.overdriveTime = Math.max(0, p.overdriveTime - dt);
        p.hurtTime = Math.max(0, p.hurtTime - dt);
        p.comboTimer = Math.max(0, p.comboTimer - dt);
        p.shieldRegenDelay = Math.max(0, p.shieldRegenDelay - dt);
        p.lastHitTime += dt;
        p.addEnergy(s.energyRegen() * dt);
        if (p.shieldRegenDelay <= 0 && p.shield < s.maxShield())
            p.shield = Math.min(s.maxShield(), p.shield + 6 * dt);
    }

    /**
     * Setzt Eingaben in Bewegung um und integriert die Figur.
     *
     * @param dt Sekunden
     * @param input Eingaben
     */
    void move(double dt, InputFrame input) {
        var p = run.player;
        var s = p.stats;
        double speed =
                s.moveSpeed()
                        * (p.overdriveTime > 0 ? 1.4 : 1)
                        * p.statuses.slowFactor()
                        * (p.statuses.active(Status.SHOCK) ? .55 : 1);
        int direction = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
        boolean committed = committed(p);
        if (direction != 0 && p.dashTime <= 0 && !committed) p.facing = direction;
        if (input.aimDirection() != 0
                && p.dashTime <= 0
                && !committed
                && (input.attack() || input.ability())) p.facing = input.aimDirection();
        jump(dt, input);
        if (input.dash() && p.dashCooldown <= 0 && !p.slamming && (p.grounded || !p.airDashUsed))
            startDash();
        if (p.dashTime > 0) {
            p.vx = p.facing * 820 * speed;
            p.vy = 0;
        } else {
            double factor = committed && p.grounded ? .3 : 1;
            p.vx = direction * GameRun.RUN * speed * factor + p.lungeVelocity;
        }
        if (p.dashTime <= 0) p.vx += run.machinery.push(p);
        p.lungeVelocity *= Math.exp(-11 * dt);
        if (Math.abs(p.lungeVelocity) < 4) p.lungeVelocity = 0;
        boolean wasGrounded = p.grounded;
        double fallSpeed = p.vy;
        Physics.move(p, run.layout(), dt);
        if (!wasGrounded && p.grounded) {
            if (p.slamming) run.arsenal.slamImpact();
            else if (fallSpeed > 380)
                run.emit(new GameEvent(GameEvent.Type.LAND, p.x, p.y, fallSpeed, ""));
        }
        if (p.dashTime > 0 && p.stacks(Item.DASH_BLADE) > 0) dashBlade(p);
    }

    /**
     * @return {@code true}, solange die Figur ausholt oder trifft und sich nicht drehen darf
     */
    static boolean committed(Player p) {
        return p.swing != null && p.swingTime < p.swing.windup() + p.swing.active();
    }

    private void jump(double dt, InputFrame input) {
        var p = run.player;
        if (p.grounded) {
            p.coyoteTime = .1;
            p.airJumps = 0;
            p.airDashUsed = false;
        } else p.coyoteTime = Math.max(0, p.coyoteTime - dt);
        p.jumpBuffer = input.jump() ? .13 : Math.max(0, p.jumpBuffer - dt);
        if (p.jumpBuffer > 0 && p.dashTime <= 0 && !p.slamming) {
            if (input.down() && p.grounded && p.platform != null) {
                p.dropTime = .28;
                p.grounded = false;
                p.y += 2;
                p.jumpBuffer = 0;
            } else if (p.coyoteTime > 0) {
                p.vy = -GameRun.JUMP;
                p.coyoteTime = 0;
                p.jumpBuffer = 0;
                p.grounded = false;
                run.emit(GameEvent.at(GameEvent.Type.JUMP, p.x, p.y));
            } else if (p.airJumps < p.stats.extraJumps()) {
                p.vy = -GameRun.JUMP * .92;
                p.airJumps++;
                p.jumpBuffer = 0;
                run.emit(GameEvent.at(GameEvent.Type.AIR_JUMP, p.x, p.y));
            }
        }
        if (!input.jumpHeld() && p.vy < -400 && !p.slamming) p.vy = -400;
    }

    private void startDash() {
        var p = run.player;
        p.dashTime = .18;
        p.dashCooldown = .9 * p.stats.cooldown();
        p.invulnerableTime = Math.max(.26, p.invulnerableTime);
        if (!p.grounded) p.airDashUsed = true;
        p.swing = null;
        p.dashHits.clear();
        if (p.stacks(Item.AFTERBURNER) > 0) p.afterburner = true;
        if (p.stacks(Item.PHASE_CORE) > 0 && run.phase() == GameRun.Phase.RUNNING) {
            var image =
                    run.shoot(
                            Projectile.Kind.AFTERIMAGE, true, p.x, p.centerY(), 0, 0, 34, 30, .55);
            image.explosionRadius = 150;
        }
        run.emit(GameEvent.at(GameEvent.Type.DASH, p.x, p.y));
    }

    private void dashBlade(Player p) {
        if (run.phase() != GameRun.Phase.RUNNING) return;
        for (int i = 0; i < run.enemies.size(); i++) {
            var e = run.enemies.get(i);
            if (e.alive() && !p.dashHits.contains(e.id) && e.bounds().intersects(p.bounds())) {
                p.dashHits.add(e.id);
                run.combat.hitEnemy(
                        e, 18 * p.stacks(Item.DASH_BLADE), Combat.Source.DASH, 120, p.x);
            }
        }
    }
}
