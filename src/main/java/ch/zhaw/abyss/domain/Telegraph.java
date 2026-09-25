package ch.zhaw.abyss.domain;

/**
 * Sichtbare Vorwarnung eines Angriffs. Die Domäne beschreibt nur Form und Lage; die Darstellung
 * entscheidet über Farben und Animation.
 *
 * @param shape Form der Warnung
 * @param x1 Startpunkt oder linke Kante
 * @param y1 Startpunkt oder obere Kante
 * @param x2 Zielpunkt oder rechte Kante
 * @param y2 Zielpunkt oder untere Kante
 */
public record Telegraph(Shape shape, double x1, double y1, double x2, double y2) {
    /** Formen von Vorwarnungen. */
    public enum Shape {
        /** Zielstrahl von Schütze zu Ziel. */
        AIM,
        /** Bodenzone zwischen x1 und x2. */
        FLOOR,
        /** Kreisfläche um (x1, y1) mit Radius x2. */
        CIRCLE,
        /** Horizontaler Strahl über die gesamte Raumbreite auf Höhe y1. */
        BEAM,
        /** Senkrechte Einschlagsäule bei x1. */
        COLUMN
    }
}
