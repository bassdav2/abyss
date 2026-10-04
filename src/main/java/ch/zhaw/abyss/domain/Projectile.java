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
        AFTERIMAGE,
        /** Druckklinge der Klingenwelle: fliegt flach und durchschlägt mehrere Gegner. */
        BLADE,
        /** Plasmakugel des Plasmawerfers: explodiert beim Aufprall mit Werkzeugschaden. */
        PLASMA,
        /** Prismageschoss: schillerndes Geschoss der Prismaquallen und der Prismenkaiserin. */
        PRISM,
        /** Lichtlanze: sehr schnelles, langes Geschoss nach einer Warnlinie. */
        LANCE,
        /** Säureklumpen der Säurespucker: fliegt im Bogen und zerplatzt am Boden. */
        GLOB,
        /** Minitorpedo des Raketenschwarms: sucht Ziele und explodiert. */
        MISSILE
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

    /** Schütze, etwa für Ankerwürfe und Torpedos, die den Boss treffen; sonst 0. */
    long owner;

    /** Anker: zieht die getroffene Figur zum Schützen. */
    boolean hook;

    /** Feindlicher Torpedo, der auch Gegner trifft und Bosse betäubt. */
    boolean wreck;

    /** Geschwindigkeitsfaktor pro Sekunde, 1 für gleichförmige Bewegung. */
    double accel = 1;

    /** Zielsuche nur zwischen diesen Altern; ausserhalb fliegt das Geschoss geradeaus. */
    double steerStart, steerEnd = Double.MAX_VALUE;

    /** Farbton 0 bis 1 für Prismageschosse. */
    double hue;

    /**
     * Alter, in dem ein schwebendes Geschoss auf die Figur zuschnellt, sonst negativ; danach fliegt
     * es mit {@link #surgeSpeed} geradeaus.
     */
    double surgeAt = -1, surgeSpeed;

    /** Darstellung einer Lichtlanze, sonst {@code null}. */
    Lance.Style style;

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
     * @return Farbton 0 bis 1, für Prismageschosse
     */
    public double hue() {
        return hue;
    }

    /**
     * @return Darstellung einer Lichtlanze oder {@code null}
     */
    public Lance.Style style() {
        return style;
    }

    /**
     * @return verbleibende Lebenszeit
     */
    public double life() {
        return life;
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
