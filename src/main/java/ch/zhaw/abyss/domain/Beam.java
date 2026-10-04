package ch.zhaw.abyss.domain;

/**
 * Ein Lichtstrahl als Strecke, etwa der Sonnentanz der Prismenkaiserin. Die Domäne legt Lage und
 * Zustand fest; Treffer prüft die Bossstrategie, die Darstellung zeichnet ihn.
 *
 * @param x1 Ursprung x
 * @param y1 Ursprung y
 * @param x2 Ende x
 * @param y2 Ende y
 * @param width Breite der Trefferzone
 * @param live {@code false} während der Vorwarnung, {@code true} sobald er schadet
 * @param hue Farbton 0 bis 1
 */
public record Beam(
        double x1, double y1, double x2, double y2, double width, boolean live, double hue) {
    /**
     * @param px Punkt x
     * @param py Punkt y
     * @param radius Radius um den Punkt
     * @return {@code true}, wenn der Kreis den Strahl berührt
     */
    public boolean touches(double px, double py, double radius) {
        double dx = x2 - x1, dy = y2 - y1;
        double length = dx * dx + dy * dy;
        double t = length <= 0 ? 0 : ((px - x1) * dx + (py - y1) * dy) / length;
        t = Math.max(0, Math.min(1, t));
        double cx = x1 + dx * t, cy = y1 + dy * t;
        return Math.hypot(px - cx, py - cy) <= radius + width / 2;
    }
}
