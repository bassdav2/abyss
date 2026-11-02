package ch.zhaw.abyss.domain;

/**
 * Gemeinsame Basis aller Figuren. Weltkoordinaten: {@code x} ist die horizontale Mitte, {@code y}
 * die Fusshöhe; kleinere y-Werte liegen weiter oben.
 */
public abstract class Actor extends Body {
    double health, maxHealth;
    int facing = 1;
    double hurtTime, animationTime;
    double width, height;
    boolean grounded = true;
    Platform platform;
    double dropTime;
    final StatusSet statuses = new StatusSet();

    /**
     * @param id eindeutige Kennung innerhalb eines Raums
     * @param x horizontale Mitte
     * @param y Fusshöhe
     * @param width Breite der Trefferzone
     * @param height Höhe der Trefferzone
     * @param health Start- und Maximalintegrität
     */
    protected Actor(long id, double x, double y, double width, double height, double health) {
        super(id, x, y, 0, 0);
        this.width = width;
        this.height = height;
        this.health = health;
        this.maxHealth = health;
    }

    /**
     * @return {@code true} für schwebende Figuren ohne Schwerkraft
     */
    boolean flying() {
        return false;
    }

    /** Schwerkraft für Figuren in Einheiten pro Sekunde². */
    static final double GRAVITY = 2100;

    /** Höchste Fallgeschwindigkeit einer Figur. */
    static final double MAX_FALL = 1500;

    /**
     * Bewegt die Figur einen Schritt mit Schwerkraft, Boden und einseitigen Laufstegen. Schwebende
     * Figuren bewegen sich frei, bleiben aber innerhalb des Raums. Früher eine statische Methode in
     * {@code Physics}; die Figur bewegt sich jetzt selbst (Information Expert).
     *
     * @param layout Raumgeometrie
     * @param dt Sekunden
     */
    void move(RoomLayout layout, double dt) {
        double previousY = y;
        double min = RoomLayout.WALL + width / 2, max = layout.width() - min;
        x = GameRun.clamp(x + vx * dt, min, max);
        if (flying()) {
            y = GameRun.clamp(y + vy * dt, height + 30, GameRun.FLOOR);
            grounded = y >= GameRun.FLOOR;
            return;
        }
        dropTime = Math.max(0, dropTime - dt);
        fall(GRAVITY, MAX_FALL, dt);
        y += vy * dt;
        grounded = false;
        platform = null;
        if (y >= GameRun.FLOOR) {
            y = GameRun.FLOOR;
            vy = 0;
            grounded = true;
            return;
        }
        if (vy < 0 || dropTime > 0) return;
        for (Platform candidate : layout.platforms()) {
            if (previousY <= candidate.y() + 1 && y >= candidate.y() && candidate.covers(x)) {
                y = candidate.y();
                vy = 0;
                grounded = true;
                platform = candidate;
                return;
            }
        }
    }

    /**
     * @return aktuelle Integrität
     */
    public double health() {
        return health;
    }

    /**
     * @return maximale Integrität
     */
    public double maxHealth() {
        return maxHealth;
    }

    /**
     * @return Breite der Trefferzone
     */
    public double width() {
        return width;
    }

    /**
     * @return Höhe der Trefferzone
     */
    public double height() {
        return height;
    }

    /**
     * @return Blickrichtung: 1 rechts, -1 links
     */
    public int facing() {
        return facing;
    }

    /**
     * @return verbleibende Zeit der Trefferreaktion
     */
    public double hurtTime() {
        return hurtTime;
    }

    /**
     * @return fortlaufende Animationszeit
     */
    public double animationTime() {
        return animationTime;
    }

    /**
     * @return {@code true}, wenn die Figur auf Boden oder Steg steht
     */
    public boolean grounded() {
        return grounded;
    }

    /**
     * @return aktive Zustände
     */
    public StatusSet statuses() {
        return statuses;
    }

    /**
     * @return {@code true}, solange Integrität übrig ist
     */
    public boolean alive() {
        return health > 0;
    }

    /**
     * @return aktuelle Trefferzone
     */
    public Bounds bounds() {
        return new Bounds(x - width / 2, y - height, width, height);
    }

    /**
     * @return vertikale Mitte der Trefferzone
     */
    public double centerY() {
        return y - height / 2;
    }
}
