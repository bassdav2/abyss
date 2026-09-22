package ch.zhaw.abyss.domain;

import java.util.HashSet;
import java.util.Set;

public final class Projectile {
    public enum Kind {
        BOLT,
        ARC,
        SHOCKWAVE
    }

    final long id;
    final Kind kind;
    final boolean friendly;
    double x, y, vx, vy, life;
    final double damage, radius;
    final Set<Long> hitActors = new HashSet<>();

    Projectile(
            long id,
            Kind kind,
            boolean friendly,
            double x,
            double y,
            double vx,
            double vy,
            double damage,
            double radius,
            double life) {
        this.id = id;
        this.kind = kind;
        this.friendly = friendly;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.radius = radius;
        this.life = life;
    }

    public long id() {
        return id;
    }

    public Kind kind() {
        return kind;
    }

    public boolean friendly() {
        return friendly;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double radius() {
        return radius;
    }

    public double vx() {
        return vx;
    }

    public Bounds bounds() {
        return new Bounds(x - radius, y - radius, radius * 2, radius * 2);
    }
}
