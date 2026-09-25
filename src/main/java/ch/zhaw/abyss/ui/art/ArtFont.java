package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.ui.pixel.PixelFont;

/** Schablonenschrift für Beschriftungen, die direkt in Raumgrafiken gemalt werden. */
final class ArtFont {
    private static final ArtFont INSTANCE = new ArtFont(PixelFont.load());
    private final PixelFont font;

    private ArtFont(PixelFont font) {
        this.font = font;
    }

    static ArtFont get() {
        return INSTANCE;
    }

    /**
     * Malt Text als Schablonenschrift mit kleinen Stegen, wie aufgesprüht.
     *
     * @param p Zeichenfläche
     * @param text Text
     * @param x linke Kante
     * @param y Oberkante
     * @param color Farbe
     */
    void stencil(Painter p, String text, int x, int y, int color) {
        font.render(
                text,
                x,
                y,
                1,
                (px, py) -> {
                    if ((py - y) != 3) p.px(px, py, color);
                });
    }
}
