package ch.zhaw.abyss.domain;

/**
 * Bewegliches Spielobjekt mit Kennung, Position und Geschwindigkeit. Basisklasse für Figuren
 * ({@link Actor}), Geschosse ({@link Projectile}) und Beute ({@link Pickup}): Die Bewegung unter
 * Schwerkraft ist damit so weit oben im Generalisierungsbaum zugeordnet wie möglich und nicht mehr
 * in jeder Klasse einzeln programmiert. Die Klasse kennt keine ABYSS-Begriffe und ist damit ein
 * erster Baustein für eine wiederverwendbare 2D-Physik.
 */
public abstract class Body {
    final long id;

    /**
     * Position; welcher Punkt des Objekts gemeint ist (Mitte oder Fuss), legt die Unterklasse fest.
     */
    double x, y;

    /** Geschwindigkeit in Einheiten pro Sekunde; negatives {@code vy} bedeutet aufwärts. */
    double vx, vy;

    protected Body(long id, double x, double y, double vx, double vy) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Beschleunigt nach unten und begrenzt die Fallgeschwindigkeit.
     *
     * @param gravity Schwerkraft in Einheiten pro Sekunde²
     * @param maxFall höchste Fallgeschwindigkeit
     * @param dt Sekunden
     */
    final void fall(double gravity, double maxFall, double dt) {
        vy = Math.min(maxFall, vy + gravity * dt);
    }

    /**
     * Bewegt das Objekt einen Schritt entlang seiner Geschwindigkeit.
     *
     * @param dt Sekunden
     */
    final void advance(double dt) {
        x += vx * dt;
        y += vy * dt;
    }

    /**
     * @return eindeutige Kennung
     */
    public long id() {
        return id;
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
}
