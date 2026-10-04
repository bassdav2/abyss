package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.ui.art.Pal;

/**
 * Kurzlebige Bildwirkung nach Ereignissen: Bildblitz, chromatische Aberration, Trefferpause und
 * Zeitlupe. Reine Präsentation.
 */
final class ScreenFeel {
    double flash, aberration, hitStop, slowMotion, dashGhost;

    /** Laufende Zeit für schillernde Farben. */
    double time;

    int flashColor = Pal.WHITE;

    /**
     * Lässt die Wirkungen abklingen; die Trefferpause wird vom Fenster verbraucht.
     *
     * @param dt Sekunden
     */
    void update(double dt) {
        time += dt;
        flash = Math.max(0, flash - dt * 2.2);
        aberration = Math.max(0, aberration - dt);
        slowMotion = Math.max(0, slowMotion - dt);
    }

    /** Setzt alle Wirkungen zurück. */
    void clear() {
        flash = 0;
        aberration = 0;
        slowMotion = 0;
        hitStop = 0;
    }
}
