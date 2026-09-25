package ch.zhaw.abyss.domain;

/**
 * Bewegung mit Schwerkraft, Boden und einseitigen Laufstegen. Schwebende Figuren bewegen sich frei,
 * bleiben aber innerhalb des Raums.
 */
final class Physics {
    /** Schwerkraft in Einheiten pro Sekunde². */
    static final double GRAVITY = 2100;

    /** Höchste Fallgeschwindigkeit. */
    static final double MAX_FALL = 1500;

    private Physics() {}

    /**
     * Integriert eine Figur um einen Schritt.
     *
     * @param a Figur
     * @param layout Raumgeometrie
     * @param dt Sekunden
     */
    static void move(Actor a, RoomLayout layout, double dt) {
        double previousY = a.y;
        double min = RoomLayout.WALL + a.width / 2, max = layout.width() - min;
        a.x = GameRun.clamp(a.x + a.vx * dt, min, max);
        if (a.flying()) {
            a.y = GameRun.clamp(a.y + a.vy * dt, a.height + 30, GameRun.FLOOR);
            a.grounded = a.y >= GameRun.FLOOR;
            return;
        }
        a.dropTime = Math.max(0, a.dropTime - dt);
        a.vy = Math.min(MAX_FALL, a.vy + GRAVITY * dt);
        a.y += a.vy * dt;
        a.grounded = false;
        a.platform = null;
        if (a.y >= GameRun.FLOOR) {
            a.y = GameRun.FLOOR;
            a.vy = 0;
            a.grounded = true;
            return;
        }
        if (a.vy < 0 || a.dropTime > 0) return;
        for (Platform platform : layout.platforms()) {
            if (previousY <= platform.y() + 1 && a.y >= platform.y() && platform.covers(a.x)) {
                a.y = platform.y();
                a.vy = 0;
                a.grounded = true;
                a.platform = platform;
                return;
            }
        }
    }
}
