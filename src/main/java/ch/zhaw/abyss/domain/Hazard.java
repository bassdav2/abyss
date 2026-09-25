package ch.zhaw.abyss.domain;

/**
 * Raumgefahr mit Vorwarnung. Dauerhafte Gefahren pulsieren periodisch; temporäre Gefahren wie
 * Feuer, Säure oder ein Sperrfeuer-Einschlag verschwinden nach ihrer Lebenszeit.
 */
public final class Hazard {
    /** Gefahrenarten. */
    public enum Kind {
        STEAM,
        ELECTRIC,
        FIRE,
        ACID,
        BARRAGE
    }

    private final double x, width;
    private final Kind kind;
    private double time;
    private double life;

    /**
     * @param x horizontale Mitte
     * @param width Breite
     * @param kind Art
     * @param start Startzeit; negative Werte verzögern den ersten Zyklus
     * @param life Lebenszeit in Sekunden, {@link Double#POSITIVE_INFINITY} für dauerhafte Gefahren
     */
    Hazard(double x, double width, Kind kind, double start, double life) {
        this.x = x;
        this.width = width;
        this.kind = kind;
        this.time = start;
        this.life = life;
    }

    /**
     * Erzeugt eine dauerhafte, periodische Gefahr.
     *
     * @param x horizontale Mitte
     * @param kind Dampf oder Strom
     * @return Gefahr
     */
    static Hazard periodic(double x, Kind kind) {
        return new Hazard(x, 120, kind, -3, Double.POSITIVE_INFINITY);
    }

    void update(double dt) {
        time += dt;
        life -= dt;
    }

    /**
     * @return horizontale Mitte
     */
    public double x() {
        return x;
    }

    /**
     * @return Breite
     */
    public double width() {
        return width;
    }

    /**
     * @return Art
     */
    public Kind kind() {
        return kind;
    }

    /**
     * @return {@code true}, wenn die Gefahr entfernt werden kann
     */
    boolean expired() {
        return life <= 0;
    }

    /**
     * @return {@code true}, während die Gefahr sichtbar warnt, aber noch nicht schadet
     */
    public boolean warning() {
        return switch (kind) {
            case STEAM, ELECTRIC -> time >= 0 && time % 6.5 < 1.15;
            case BARRAGE -> time < 0;
            default -> false;
        };
    }

    /**
     * @return {@code true}, während die Gefahr Schaden verursacht
     */
    public boolean active() {
        return switch (kind) {
            case STEAM, ELECTRIC -> time >= 0 && time % 6.5 >= 1.15 && time % 6.5 < 2.6;
            case BARRAGE -> time >= 0 && time < .3;
            case FIRE, ACID -> life > 0;
        };
    }

    /**
     * @return Zeit im aktuellen Zyklus
     */
    public double phase() {
        return kind == Kind.STEAM || kind == Kind.ELECTRIC ? Math.max(0, time % 6.5) : time;
    }

    /**
     * @return verbleibende Lebenszeit
     */
    public double life() {
        return life;
    }

    /**
     * @return Schaden pro Treffer vor Skalierung
     */
    double damage() {
        return switch (kind) {
            case STEAM, ELECTRIC -> 6;
            case FIRE, ACID -> 4;
            case BARRAGE -> 16;
        };
    }

    /**
     * @return Trefferzone
     */
    public Bounds bounds() {
        double h =
                switch (kind) {
                    case STEAM -> 150;
                    case BARRAGE -> GameRun.FLOOR;
                    case FIRE -> 60;
                    default -> 34;
                };
        return new Bounds(x - width / 2, GameRun.FLOOR - h, width, h);
    }
}
