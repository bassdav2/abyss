package ch.zhaw.abyss.domain;

/**
 * Aufsammelbare Beute. Fällt mit Schwerkraft, springt kurz und wird danach vom Sammelradius der
 * Figur angezogen. Der Wert wird genau einmal gutgeschrieben.
 */
public final class Pickup {
    /** Beutearten. */
    public enum Kind {
        SCRAP,
        HEALTH,
        ENERGY,
        CORE
    }

    final long id;
    final Kind kind;
    final double value;
    double x, y, vx, vy, age;
    boolean collected;

    Pickup(long id, Kind kind, double value, double x, double y, double vx, double vy) {
        this.id = id;
        this.kind = kind;
        this.value = value;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
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
     * @return Wert
     */
    public double value() {
        return value;
    }

    /**
     * @return horizontale Position
     */
    public double x() {
        return x;
    }

    /**
     * @return vertikale Position
     */
    public double y() {
        return y;
    }

    /**
     * @return Alter in Sekunden
     */
    public double age() {
        return age;
    }
}
