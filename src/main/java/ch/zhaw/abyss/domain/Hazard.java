package ch.zhaw.abyss.domain;

/** Periodische Raumgefahr mit klarer Vorwarnung und schadensfreier Eintrittsphase. */
public final class Hazard {
    public enum Kind {
        STEAM,
        ELECTRIC
    }

    private final double x;
    private final Kind kind;
    private double time = -3;

    Hazard(double x, Kind kind) {
        this.x = x;
        this.kind = kind;
    }

    void update(double dt) {
        time += dt;
    }

    public double x() {
        return x;
    }

    public Kind kind() {
        return kind;
    }

    public boolean warning() {
        return time >= 0 && time % 6.5 < 1.15;
    }

    public boolean active() {
        return time >= 0 && time % 6.5 >= 1.15 && time % 6.5 < 2.6;
    }

    public double phase() {
        return Math.max(0, time % 6.5);
    }

    public Bounds bounds() {
        return new Bounds(
                x - 60,
                GameRun.FLOOR - (kind == Kind.STEAM ? 145 : 35),
                120,
                kind == Kind.STEAM ? 145 : 35);
    }
}
