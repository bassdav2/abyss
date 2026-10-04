package ch.zhaw.abyss.domain;

/**
 * Lichtlanze mit Warnlinie. Sie erscheint zuerst als dünne Linie und schiesst nach Ablauf der
 * Vorwarnzeit geradlinig los. Bossmuster bauen daraus Reihen, Gitter und Kreuzfeuer mit Lücken, die
 * sich lernen lassen.
 */
public final class Lance {
    /** Herkunft und Farbe für die Darstellung. */
    public enum Style {
        /** Schillernde Lanze der Prismenkaiserin. */
        PRISM,
        /** Harpunengeschütz des Lotsen. */
        STEEL,
        /** Fischzug der Brutmutter. */
        FISH,
        /** Strahl des Reaktorkerns. */
        CORE
    }

    final double x, y, angle, speed, damage, warning;
    final Style style;
    final double hue;
    double delay;

    /**
     * @param x Startpunkt
     * @param y Startpunkt
     * @param angle Flugrichtung im Bogenmass
     * @param speed Fluggeschwindigkeit
     * @param damage bereits skalierter Schaden
     * @param warning Vorwarnzeit in Sekunden
     * @param style Darstellung
     * @param hue Farbton 0 bis 1 für schillernde Lanzen
     */
    Lance(
            double x,
            double y,
            double angle,
            double speed,
            double damage,
            double warning,
            Style style,
            double hue) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.damage = damage;
        this.warning = Math.max(.05, warning);
        this.delay = this.warning;
        this.style = style;
        this.hue = hue;
    }

    /**
     * @return Startpunkt x
     */
    public double x() {
        return x;
    }

    /**
     * @return Startpunkt y
     */
    public double y() {
        return y;
    }

    /**
     * @return Flugrichtung im Bogenmass
     */
    public double angle() {
        return angle;
    }

    /**
     * @return Darstellung
     */
    public Style style() {
        return style;
    }

    /**
     * @return Farbton 0 bis 1
     */
    public double hue() {
        return hue;
    }

    /**
     * @return Anteil 0 bis 1 der abgelaufenen Vorwarnzeit
     */
    public double progress() {
        return 1 - Math.max(0, delay) / warning;
    }
}
