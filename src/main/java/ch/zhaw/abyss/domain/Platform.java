package ch.zhaw.abyss.domain;

/**
 * Einseitig begehbarer Laufsteg. Figuren landen von oben darauf; von unten springen sie hindurch
 * und mit "Abtauchen" (unten + Sprung) lassen sie sich fallen.
 *
 * @param x linke Kante in Welteinheiten
 * @param y Oberkante (Fusshöhe einer darauf stehenden Figur)
 * @param width Breite in Welteinheiten
 */
public record Platform(double x, double y, double width) {
    /** Prüft die Geometrie, damit keine unerreichbaren oder negativen Stege entstehen. */
    public Platform {
        if (!Double.isFinite(x) || !Double.isFinite(y) || width <= 0 || y <= 0)
            throw new IllegalArgumentException("Ungültige Plattform");
    }

    /**
     * @param px horizontale Position
     * @return {@code true}, wenn die Position über dem Steg liegt
     */
    public boolean covers(double px) {
        return px >= x && px <= x + width;
    }

    /**
     * @return rechte Kante in Welteinheiten
     */
    public double right() {
        return x + width;
    }

    /**
     * @return horizontale Mitte des Stegs
     */
    public double centerX() {
        return x + width / 2;
    }
}
