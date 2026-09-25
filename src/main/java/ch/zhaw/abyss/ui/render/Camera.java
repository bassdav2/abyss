package ch.zhaw.abyss.ui.render;

/**
 * Seitlich folgende Kamera in Pixelkoordinaten mit Vorausblick und Wackeln nach dem Trauma-Modell.
 * Rein darstellend; die Kamera beeinflusst keine Spielregeln.
 */
public final class Camera {
    private double x, target, trauma, time;
    private double shakeX, shakeY;

    /**
     * Folgt einer Zielposition weich.
     *
     * @param focusX Fokus in Pixeln (etwa die Spielfigur)
     * @param facing Blickrichtung für den Vorausblick
     * @param roomWidth Raumbreite in Pixeln
     * @param viewWidth Bildbreite in Pixeln
     * @param dt Sekunden
     * @param shakeScale Faktor aus den Einstellungen, 0 schaltet Wackeln ab
     */
    public void follow(
            double focusX, int facing, int roomWidth, int viewWidth, double dt, double shakeScale) {
        time += dt;
        target =
                clamp(
                        focusX - viewWidth / 2.0 + facing * 34,
                        0,
                        Math.max(0, roomWidth - viewWidth));
        x += (target - x) * (1 - Math.exp(-5.5 * dt));
        x = clamp(x, 0, Math.max(0, roomWidth - viewWidth));
        trauma = Math.max(0, trauma - dt * 1.6);
        double amount = trauma * trauma * 7 * shakeScale;
        shakeX = amount * (Math.sin(time * 91) * .6 + Math.sin(time * 37) * .4);
        shakeY = amount * (Math.cos(time * 83) * .6 + Math.sin(time * 29) * .4) * .7;
    }

    /**
     * Springt ohne Übergang auf eine Position, etwa beim Raumwechsel.
     *
     * @param focusX Fokus in Pixeln
     * @param roomWidth Raumbreite
     * @param viewWidth Bildbreite
     */
    public void snap(double focusX, int roomWidth, int viewWidth) {
        x = clamp(focusX - viewWidth / 2.0, 0, Math.max(0, roomWidth - viewWidth));
        target = x;
        trauma = 0;
    }

    /**
     * @param amount zusätzliches Trauma 0 bis 1
     */
    public void shake(double amount) {
        trauma = Math.min(1, trauma + amount);
    }

    /**
     * @return linke Kante inklusive Wackeln, gerundet
     */
    public int left() {
        return (int) Math.round(x + shakeX);
    }

    /**
     * @return vertikaler Wackelversatz
     */
    public int offsetY() {
        return (int) Math.round(shakeY);
    }

    /**
     * @return ungerundete linke Kante ohne Wackeln
     */
    public double exact() {
        return x;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
