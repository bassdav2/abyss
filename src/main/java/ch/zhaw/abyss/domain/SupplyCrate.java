package ch.zhaw.abyss.domain;

/** Zerstörbarer Vorrat, deterministisch pro Raum verteilt. Keine zufällige Beute beim Laden. */
public final class SupplyCrate {
    public enum Kind {
        REPAIR,
        ENERGY,
        SALVAGE
    }

    private final double x;
    private final Kind kind;
    private double health = 30;

    SupplyCrate(double x, Kind kind) {
        this.x = x;
        this.kind = kind;
    }

    public double x() {
        return x;
    }

    public Kind kind() {
        return kind;
    }

    public boolean intact() {
        return health > 0;
    }

    boolean hit(double damage) {
        if (!intact()) return false;
        health = Math.max(0, health - damage);
        return !intact();
    }

    public Bounds bounds() {
        return new Bounds(x - 26, GameRun.FLOOR - 48, 52, 48);
    }
}
