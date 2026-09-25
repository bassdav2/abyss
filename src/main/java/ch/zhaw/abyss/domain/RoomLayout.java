package ch.zhaw.abyss.domain;

import java.util.List;

/**
 * Begehbare Geometrie eines Raums: Breite, Boden und Laufstege. Hintergrundgrafik ist keine
 * Kollisionsgeometrie; nur diese Daten entscheiden über Stehen, Fallen und Landen.
 *
 * @param width Raumbreite in Welteinheiten; ein Bildschirm entspricht 1600 Einheiten
 * @param platforms einseitig begehbare Laufstege
 * @param rewardX Position des Bergungsbehälters beziehungsweise der Händlerin
 */
public record RoomLayout(double width, List<Platform> platforms, double rewardX) {
    /** Innerer Abstand zu den Schotten, den keine Figur überschreitet. */
    public static final double WALL = 44;

    /** Prüft Breite und Plattformlage. */
    public RoomLayout {
        platforms = List.copyOf(platforms);
        if (width < GameRun.VIEW_WIDTH || width > 4 * GameRun.VIEW_WIDTH)
            throw new IllegalArgumentException("Ungültige Raumbreite");
        if (rewardX < WALL || rewardX > width - WALL)
            throw new IllegalArgumentException("Bergung liegt ausserhalb des Raums");
        for (Platform platform : platforms)
            if (platform.x() < 0 || platform.right() > width || platform.y() >= GameRun.FLOOR)
                throw new IllegalArgumentException("Plattform liegt ausserhalb des Raums");
    }

    /**
     * @return x-Position des rechten Ausgangsschotts
     */
    public double exitX() {
        return width - 90;
    }

    /**
     * Liefert die Standhöhe direkt unter einer Position.
     *
     * @param x horizontale Position
     * @param fromY aktuelle Fusshöhe; nur tiefere Flächen zählen
     * @return nächste Fläche unter der Position, mindestens der Boden
     */
    public double surfaceBelow(double x, double fromY) {
        double best = GameRun.FLOOR;
        for (Platform platform : platforms)
            if (platform.covers(x) && platform.y() >= fromY - .5 && platform.y() < best)
                best = platform.y();
        return best;
    }
}
