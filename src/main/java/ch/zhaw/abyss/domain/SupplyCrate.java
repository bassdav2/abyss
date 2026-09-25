package ch.zhaw.abyss.domain;

/** Zerstörbarer Vorrat, deterministisch pro Raum verteilt. Keine zufällige Beute beim Laden. */
public final class SupplyCrate {
    /** Inhalt einer Kiste. */
    public enum Kind {
        REPAIR,
        ENERGY,
        SALVAGE,
        EXPLOSIVE
    }

    private final double x, y;
    private final Kind kind;
    private double health = 30;
    double hitTime;

    SupplyCrate(double x, double y, Kind kind) {
        this.x = x;
        this.y = y;
        this.kind = kind;
    }

    /**
     * @return horizontale Mitte
     */
    public double x() {
        return x;
    }

    /**
     * @return Standhöhe
     */
    public double y() {
        return y;
    }

    /**
     * @return Inhalt
     */
    public Kind kind() {
        return kind;
    }

    /**
     * @return {@code true}, solange die Kiste steht
     */
    public boolean intact() {
        return health > 0;
    }

    /**
     * @return verbleibende Wackelzeit nach einem Treffer
     */
    public double hitTime() {
        return hitTime;
    }

    /**
     * @param damage Schaden
     * @return {@code true}, wenn dieser Treffer die Kiste öffnet
     */
    boolean hit(double damage) {
        if (!intact()) return false;
        health = Math.max(0, health - damage);
        hitTime = .15;
        return !intact();
    }

    /**
     * @return Trefferzone
     */
    public Bounds bounds() {
        double h = kind == Kind.EXPLOSIVE ? 58 : 48;
        return new Bounds(x - 26, y - h, 52, h);
    }
}
