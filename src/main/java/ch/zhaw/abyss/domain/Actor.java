package ch.zhaw.abyss.domain;

/**
 * Gemeinsame Basis aller Figuren. Weltkoordinaten: {@code x} ist die horizontale Mitte, {@code y}
 * die Fusshöhe; kleinere y-Werte liegen weiter oben.
 */
public abstract class Actor {
    final long id;
    double x, y, vx, vy;
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
        this.id = id;
        this.x = x;
        this.y = y;
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

    /**
     * @return eindeutige Kennung
     */
    public long id() {
        return id;
    }

    /**
     * @return horizontale Mitte
     */
    public double x() {
        return x;
    }

    /**
     * @return Fusshöhe
     */
    public double y() {
        return y;
    }

    /**
     * @return horizontale Geschwindigkeit
     */
    public double vx() {
        return vx;
    }

    /**
     * @return vertikale Geschwindigkeit; negativ bedeutet aufwärts
     */
    public double vy() {
        return vy;
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
