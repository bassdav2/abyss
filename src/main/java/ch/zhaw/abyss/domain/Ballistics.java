package ch.zhaw.abyss.domain;

import java.util.Comparator;
import java.util.List;

/**
 * Flugbahnen und Treffer aller Geschosse: Schwerkraft, Zielsuche, Landung von Minen, Säure und
 * Granaten, Durchschlag und Explosionen beim Aufprall oder Ablauf.
 */
final class Ballistics {
    private final GameRun run;

    Ballistics(GameRun run) {
        this.run = run;
    }

    /**
     * Bewegt alle Geschosse und wertet Treffer aus.
     *
     * @param dt Sekunden
     */
    void update(double dt) {
        var projectiles = run.projectiles;
        for (int i = 0; i < projectiles.size(); i++) {
            var q = projectiles.get(i);
            q.age += dt;
            q.life -= dt;
            if (q.homing && q.age >= q.steerStart && q.age <= q.steerEnd) steer(q, dt);
            if (q.accel != 1) {
                double factor = Math.pow(q.accel, dt);
                q.vx *= factor;
                q.vy *= factor;
            }
            if (q.surgeAt >= 0 && q.age >= q.surgeAt) surge(q);
            if (q.kind == Projectile.Kind.MINE && q.landed) {
                q.armTime -= dt;
                if (q.armed()
                        && Math.hypot(run.player.x - q.x, run.player.centerY() - q.y) < 80
                        && run.phase() == GameRun.Phase.RUNNING) detonate(q);
                continue;
            }
            q.vy += q.gravity * dt;
            q.x += q.vx * dt;
            q.y += q.vy * dt;
            if (q.gravity > 0 && q.vy > 0) {
                double surface = run.layout().surfaceBelow(q.x, q.y - q.radius - q.vy * dt);
                if (q.y + q.radius >= surface) land(q, surface);
            }
            if (q.life <= 0) continue;
            if (q.friendly) collideFriendly(q);
            else if (run.phase() == GameRun.Phase.RUNNING) collideHostile(q);
        }
        for (var q : List.copyOf(projectiles))
            if (q.life <= 0 && q.explosionRadius > 0) detonate(q);
        projectiles.removeIf(
                q ->
                        q.life <= 0
                                || q.x < -120
                                || q.x > run.layout().width() + 120
                                || q.y < -200
                                || q.y > GameRun.FLOOR + 80);
        int hostile = 0;
        for (var q : projectiles) if (!q.friendly) hostile++;
        run.hostileShots = hostile;
    }

    /** Ein schwebendes Prismageschoss schnellt auf die aktuelle Position der Figur zu. */
    private void surge(Projectile q) {
        q.surgeAt = -1;
        q.accel = 1;
        var p = run.player;
        double angle = Math.atan2(p.centerY() - q.y, p.x - q.x);
        q.vx = Math.cos(angle) * q.surgeSpeed;
        q.vy = Math.sin(angle) * q.surgeSpeed;
    }

    private void steer(Projectile q, double dt) {
        double speed = Math.hypot(q.vx, q.vy);
        if (speed < 1) return;
        Actor target =
                q.friendly
                        ? run.grid().around(q.x, q.y, 700).stream()
                                .filter(e -> e.alive() && !e.untargetable())
                                .filter(e -> (e.x - q.x) * Math.signum(q.vx) > -40)
                                .min(Comparator.comparingDouble(e -> Math.abs(e.x - q.x)))
                                .orElse(null)
                        : run.player;
        if (target == null) return;
        double desired = Math.atan2(target.centerY() - q.y, target.x - q.x);
        double current = Math.atan2(q.vy, q.vx);
        double delta = Math.atan2(Math.sin(desired - current), Math.cos(desired - current));
        double turn = Math.max(-4 * dt, Math.min(4 * dt, delta));
        q.vx = Math.cos(current + turn) * speed;
        q.vy = Math.sin(current + turn) * speed;
    }

    private void land(Projectile q, double surface) {
        switch (q.kind) {
            case MINE -> {
                q.landed = true;
                q.y = surface - q.radius;
                q.vx = 0;
                q.vy = 0;
            }
            case ACID -> {
                run.hazards.add(new Hazard(q.x, 110, Hazard.Kind.ACID, 0, 4));
                run.emit(new GameEvent(GameEvent.Type.EXPLOSION, q.x, surface - 10, 40, "acid"));
                q.life = 0;
            }
            case CRYO -> {
                q.y = surface - q.radius;
                q.life = 0;
            }
            case GLOB -> {
                run.emit(new GameEvent(GameEvent.Type.EXPLOSION, q.x, surface - 6, 22, "acid"));
                q.life = 0;
            }
            default -> q.life = 0;
        }
    }

    private void collideFriendly(Projectile q) {
        var zone = q.bounds();
        var candidates =
                run.grid()
                        .query(
                                zone.x(),
                                zone.y(),
                                zone.x() + zone.width(),
                                zone.y() + zone.height());
        for (int i = 0; i < candidates.size() && q.life > 0; i++) {
            var e = candidates.get(i);
            if (!e.alive() || e.untargetable() || q.hitActors.contains(e.id)) continue;
            if (!q.bounds().intersects(e.bounds())) continue;
            q.hitActors.add(e.id);
            if (q.explosionRadius > 0) {
                q.life = 0;
                return;
            }
            var source =
                    switch (q.kind) {
                        case HARPOON, SHOCKWAVE, BLADE -> Combat.Source.PROJECTILE;
                        case DRONE_SHOT -> Combat.Source.DRONE;
                        default -> Combat.Source.ABILITY;
                    };
            run.combat.hitEnemy(e, q.damage, source, q.knockback, q.x - Math.signum(q.vx) * 10);
            if (q.pierce != Integer.MAX_VALUE && q.pierce-- <= 0) q.life = 0;
        }
        for (var crate : run.crates)
            if (crate.intact() && q.bounds().intersects(crate.bounds()) && crate.hit(q.damage)) {
                run.loot.breakCrate(crate);
                if (q.kind == Projectile.Kind.HARPOON || q.kind == Projectile.Kind.DRONE_SHOT)
                    q.life = 0;
            }
    }

    private void collideHostile(Projectile q) {
        var player = run.player;
        if (q.wreck && q.age > .6 && wreck(q)) return;
        if (q.kind == Projectile.Kind.MINE || q.hitActors.contains(player.id)) return;
        if (!q.bounds().intersects(player.bounds())) return;
        if (q.explosionRadius > 0) {
            q.life = 0;
            return;
        }
        q.hitActors.add(player.id);
        boolean hit = run.combat.hurtPlayer(q.damage, q.x, null, false);
        if (hit && q.hook) hook(q);
        if (q.kind != Projectile.Kind.SHOCKWAVE && q.kind != Projectile.Kind.LANCE) q.life = 0;
    }

    /**
     * Feindlicher Torpedo, den die Figur in einen Gegner lenkt: er explodiert dort, durchschlägt
     * die Panzerung und betäubt einen Wächter. So lässt sich der Lotse mit seinen eigenen Waffen
     * schlagen.
     */
    private boolean wreck(Projectile q) {
        for (var e : run.grid().around(q.x, q.y, q.radius + 120)) {
            if (!e.alive() || e.untargetable() || e.kind.swarm()) continue;
            if (!q.bounds().intersects(e.bounds())) continue;
            q.life = 0;
            q.explosionRadius = 0;
            double damage = e.kind.boss() ? e.maxHealth * .045 : e.maxHealth * .5;
            run.combat.hitEnemy(e, damage, Combat.Source.MACHINE, 0, q.x);
            if (e.alive() && e.kind.boss()) {
                e.state = Enemy.State.STUNNED;
                e.stateTime = 1.8;
                e.telegraph = null;
                run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.centerY(), 0, "EXPOSED"));
            }
            run.combat.explode(q.x, q.y, 120, q.damage, true, false, null);
            return true;
        }
        return false;
    }

    /** Anker: zieht die Figur zum Schützen, der sofort mit einem Stampfer nachsetzt. */
    private void hook(Projectile q) {
        var p = run.player;
        for (var e : run.enemies)
            if (e.id == q.owner && e.alive() && e.pattern == WardenBrain.ANCHOR) {
                double gap = Math.abs(e.x - p.x) - 90;
                if (gap > 0) p.lungeVelocity = Math.signum(e.x - p.x) * Math.min(1500, gap * 11);
                e.step = WardenBrain.HOOKED;
                run.emit(GameEvent.at(GameEvent.Type.HARPOON, p.x, p.centerY()));
            }
    }

    private void detonate(Projectile q) {
        if (q.explosionRadius <= 0) return;
        double radius = q.explosionRadius;
        q.explosionRadius = 0;
        q.life = 0;
        var source =
                q.kind == Projectile.Kind.PLASMA ? Combat.Source.CHAIN : Combat.Source.EXPLOSION;
        run.combat.explode(q.x, q.y, radius, q.damage, !q.friendly, q.friendly, q.status, source);
    }
}
