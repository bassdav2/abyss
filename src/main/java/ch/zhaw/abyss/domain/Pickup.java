package ch.zhaw.abyss.domain;

/**
 * Aufsammelbare Beute. Fällt mit Schwerkraft, springt kurz und wird danach vom Sammelradius der
 * Figur angezogen. Der Wert wird genau einmal gutgeschrieben.
 */
public final class Pickup extends Body {
    /** Beutearten. */
    public enum Kind {
        SCRAP,
        HEALTH,
        ENERGY,
        CORE,
        /** Energiesplitter: füllt die Überladung. */
        SHARD
    }

    final Kind kind;
    double value;
    double age;

    /** Schwerkraft für fallende Beute; leichter als bei Figuren, damit sie sichtbar springt. */
    static final double GRAVITY = 1700;

    boolean collected;

    Pickup(long id, Kind kind, double value, double x, double y, double vx, double vy) {
        super(id, x, y, vx, vy);
        this.kind = kind;
        this.value = value;
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
     * @return Alter in Sekunden
     */
    public double age() {
        return age;
    }
}
