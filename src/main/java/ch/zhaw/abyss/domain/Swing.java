package ch.zhaw.abyss.domain;

/**
 * Ein einzelner Angriff einer Waffenkombination. Treffer entstehen nur im aktiven Fenster, damit
 * Ausholen, Treffer und Nachlauf fachlich getrennt und testbar sind.
 *
 * @param style Bewegungsart, auch für die Darstellung
 * @param windup Ausholzeit in Sekunden
 * @param active Dauer des Trefferfensters
 * @param recovery Nachlauf; die zweite Hälfte kann durch den nächsten Angriff abgebrochen werden
 * @param reach Reichweite vor der Figur in Welteinheiten
 * @param height Höhe der Trefferzone
 * @param damage Grundschaden vor Modulen
 * @param knockback Rückstoss in Einheiten pro Sekunde
 * @param lunge Vorwärtsbewegung während des Angriffs; negativ bedeutet Rückstoss der Waffe
 */
public record Swing(
        Style style,
        double windup,
        double active,
        double recovery,
        double reach,
        double height,
        double damage,
        double knockback,
        double lunge) {
    /** Bewegungsarten, die Kampfregeln und Animation gemeinsam benennen. */
    public enum Style {
        SLASH,
        OVERHEAD,
        THRUST,
        PUNCH,
        UPPERCUT,
        SHOOT,
        AIR_SLASH,
        SLAM
    }

    /** Validiert die Zeitfenster. */
    public Swing {
        if (windup < 0 || active <= 0 || recovery < 0 || damage < 0)
            throw new IllegalArgumentException("Ungültiger Angriff");
    }

    /**
     * @return Gesamtdauer in Sekunden bei normalem Angriffstempo
     */
    public double duration() {
        return windup + active + recovery;
    }

    /**
     * @return frühester Zeitpunkt, ab dem der nächste Angriff den Nachlauf abbricht
     */
    public double cancelTime() {
        return windup + active + recovery * .5;
    }

    /**
     * @return {@code true}, wenn der Angriff ein Geschoss statt einer Trefferzone erzeugt
     */
    public boolean ranged() {
        return style == Style.SHOOT;
    }
}
