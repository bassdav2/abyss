package ch.zhaw.abyss.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Geschosse, Wellen und Minen. Eigenschaften wie Schwerkraft, Durchschlag und Explosion werden bei
 * der Erzeugung gesetzt; die Bewegung übernimmt {@link GameRun}.
 */
public final class Projectile {
    /** Geschossarten. */
    public enum Kind {
        BOLT,
        ARC,
        SHOCKWAVE,
        HARPOON,
        TORPEDO,
        MINE,
        ACID,
        CRYO,
        DRONE_SHOT,
        ENEMY_HARPOON,
        AFTERIMAGE
    }

    final long id;
    final Kind kind;
    final boolean friendly;
    double x, y, vx, vy, life, age;
    final double damage, radius;
    double gravity, explosionRadius, armTime, knockback;
    int pierce;
    boolean homing, landed;
    Status status;
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
        this.pierce = kind == Kind.ARC || kind == Kind.SHOCKWAVE ? Integer.MAX_VALUE : 0;
    }

    /**
     * @return eindeutige Kennung
     */
    public long id() {
        return id;
    }

    /**
     * @return Art
     */
    public Kind kind() {
        return kind;
    }

    /**
     * @return {@code true} für Geschosse der spielenden Figur
     */
    public boolean friendly() {
        return friendly;
    }

    /**
     * @return horizontale Mitte
     */
    public double x() {
        return x;
    }

    /**
     * @return vertikale Mitte
     */
    public double y() {
        return y;
    }

    /**
     * @return Radius der Trefferzone
     */
    public double radius() {
        return radius;
    }

    /**
     * @return horizontale Geschwindigkeit
     */
    public double vx() {
        return vx;
    }

    /**
     * @return vertikale Geschwindigkeit
     */
    public double vy() {
        return vy;
    }

    /**
     * @return Alter in Sekunden
     */
    public double age() {
        return age;
    }

    /**
     * @return {@code true}, sobald eine Mine scharf ist
     */
    public boolean armed() {
        return kind == Kind.MINE && landed && armTime <= 0;
    }

    /**
     * @return Trefferzone
     */
    public Bounds bounds() {
        return new Bounds(x - radius, y - radius, radius * 2, radius * 2);
    }
}
