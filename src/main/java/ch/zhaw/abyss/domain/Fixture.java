package ch.zhaw.abyss.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Raumtechnik: fest eingebaute Anlagen, die den Kampf verändern. Förderbänder und Turbinenwind
 * schieben, Dampfdüsen schleudern nach oben, Pressen und Lasergitter treffen Figur und Gegner
 * gleichermassen, und Notschalter lösen auf Tastendruck einen Effekt der Sektion aus.
 *
 * <p>Die Anlage kennt nur ihren Takt. Die Wirkung auf Figuren berechnet {@link Machinery}.
 */
public final class Fixture {
    /** Arten der Raumtechnik. */
    public enum Kind {
        /** Förderband am Boden, schiebt stehende Figuren. */
        CONVEYOR("Förderband", 260),
        /** Dampfdüse im Boden, schleudert die Figur hoch hinauf. */
        VENT_PAD("Dampfdüse", 64),
        /** Turbinenwind, der in Abständen alles in eine Richtung drückt. */
        FAN("Turbinenwind", 560),
        /** Hydraulikpresse, die periodisch auf den Boden stampft. */
        PRESS("Hydraulikpresse", 96),
        /** Senkrechtes Lasergitter mit Takt. */
        LASER("Lasergitter", 18),
        /** Notschalter, der einen Effekt der Sektion auslöst. */
        CONSOLE("Notschalter", 60);

        private final String title;
        private final double width;

        Kind(String title, double width) {
            this.title = title;
            this.width = width;
        }

        /**
         * @return Anzeigename
         */
        public String title() {
            return title;
        }

        /**
         * @return Breite der Wirkzone
         */
        public double width() {
            return width;
        }
    }

    /** Taktlänge der Presse in Sekunden. */
    static final double PRESS_CYCLE = 4.4;

    /** Taktlänge von Wind und Laser in Sekunden. */
    static final double FAN_CYCLE = 7, LASER_CYCLE = 5;

    /** Abklingzeit eines Notschalters in Sekunden. */
    static final double CONSOLE_COOLDOWN = 14;

    private final Kind kind;
    private final double x;
    private final int direction, sector;
    double time, cooldown, disabled;
    boolean struck;
    final Set<Long> hitEnemies = new HashSet<>();

    /**
     * @param kind Art
     * @param x horizontale Mitte
     * @param direction Schubrichtung für Band und Wind, -1 oder 1
     * @param sector Sektion, bestimmt den Effekt eines Notschalters
     * @param start Anfangsversatz im Takt, damit gleiche Anlagen nicht synchron laufen
     */
    Fixture(Kind kind, double x, int direction, int sector, double start) {
        this.kind = kind;
        this.x = x;
        this.direction = direction >= 0 ? 1 : -1;
        this.sector = sector;
        this.time = start;
    }

    void update(double dt) {
        time += dt;
        cooldown = Math.max(0, cooldown - dt);
        disabled = Math.max(0, disabled - dt);
    }

    /**
     * @return Art
     */
    public Kind kind() {
        return kind;
    }

    /**
     * @return horizontale Mitte
     */
    public double x() {
        return x;
    }

    /**
     * @return Breite der Wirkzone
     */
    public double width() {
        return kind.width();
    }

    /**
     * @return Schubrichtung, -1 oder 1
     */
    public int direction() {
        return direction;
    }

    /**
     * @return Sektion der Anlage
     */
    public int sector() {
        return sector;
    }

    /**
     * @return Zeit im aktuellen Takt
     */
    public double phase() {
        return switch (kind) {
            case PRESS -> Math.floorMod((long) (time * 1000), (long) (PRESS_CYCLE * 1000)) / 1000.0;
            case FAN -> Math.floorMod((long) (time * 1000), (long) (FAN_CYCLE * 1000)) / 1000.0;
            case LASER -> Math.floorMod((long) (time * 1000), (long) (LASER_CYCLE * 1000)) / 1000.0;
            default -> time;
        };
    }

    /**
     * @return {@code true}, während die Anlage sichtbar vorwarnt
     */
    public boolean warning() {
        if (disabled > 0) return false;
        double t = phase();
        return switch (kind) {
            case PRESS -> t >= 2.5 && t < 3.3;
            case FAN -> t >= 3 && t < 4;
            case LASER -> t >= 2 && t < 2.6;
            default -> false;
        };
    }

    /**
     * @return {@code true}, während die Anlage wirkt
     */
    public boolean active() {
        if (disabled > 0) return false;
        double t = phase();
        return switch (kind) {
            case CONVEYOR -> true;
            case VENT_PAD -> cooldown <= 0;
            case PRESS -> t >= 3.3 && t < 3.55;
            case FAN -> t >= 4;
            case LASER -> t >= 2.6;
            case CONSOLE -> cooldown <= 0;
        };
    }

    /**
     * @return Höhe des Pressenstempels über dem Boden als Anteil 0 (unten) bis 1 (oben)
     */
    public double pressHeight() {
        double t = phase();
        if (t < 3.3) return 1;
        if (t < 3.55) return 1 - (t - 3.3) / .25;
        if (t < 3.9) return 0;
        return Math.min(1, (t - 3.9) / .5);
    }

    /**
     * @return Bereitschaft eines Notschalters, 0 gerade benutzt bis 1 bereit
     */
    public double charge() {
        return kind == Kind.CONSOLE ? 1 - cooldown / CONSOLE_COOLDOWN : 1;
    }

    /**
     * @return verbleibende Sekunden, in denen die Anlage abgeschaltet ist
     */
    public double disabled() {
        return disabled;
    }

    /**
     * @return Trefferzone von Presse und Laser
     */
    Bounds strikeZone() {
        return switch (kind) {
            case PRESS -> new Bounds(x - width() / 2, GameRun.FLOOR - 170, width(), 170);
            case LASER -> new Bounds(x - width() / 2, 60, width(), GameRun.FLOOR - 60);
            default -> new Bounds(x - width() / 2, GameRun.FLOOR - 40, width(), 40);
        };
    }

    /**
     * @param actorX Position einer Figur
     * @return {@code true}, wenn die Figur in der Wirkzone steht
     */
    boolean covers(double actorX) {
        return Math.abs(actorX - x) <= width() / 2;
    }

    /**
     * @return Name des Sektionseffekts eines Notschalters
     */
    public String consoleEffect() {
        return switch (sector) {
            case 0 -> "TORPEDOROHR";
            case 1 -> "DAMPFVENTIL";
            case 2 -> "KÄLTEKAMMER";
            default -> "ÜBERLAST";
        };
    }
}
