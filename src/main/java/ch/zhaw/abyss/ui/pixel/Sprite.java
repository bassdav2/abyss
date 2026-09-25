package ch.zhaw.abyss.ui.pixel;

/**
 * Unveränderliches Pixelbild mit Ankerpunkt und optionaler Leuchtebene. Pixel sind ARGB ohne
 * Vormultiplikation; Transparenz ist in der Regel binär.
 */
public final class Sprite {
    final int[] pixels;
    final int width, height;
    final int anchorX, anchorY;
    private Sprite glow;

    /**
     * @param pixels ARGB-Pixel zeilenweise
     * @param width Breite
     * @param height Höhe
     * @param anchorX Ankerpunkt x, relativ zur linken Kante
     * @param anchorY Ankerpunkt y, relativ zur oberen Kante
     */
    public Sprite(int[] pixels, int width, int height, int anchorX, int anchorY) {
        if (pixels.length != width * height) throw new IllegalArgumentException("Pixelanzahl");
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    /**
     * @return Breite in Pixeln
     */
    public int width() {
        return width;
    }

    /**
     * @return Höhe in Pixeln
     */
    public int height() {
        return height;
    }

    /**
     * @return Ankerpunkt x
     */
    public int anchorX() {
        return anchorX;
    }

    /**
     * @return Ankerpunkt y
     */
    public int anchorY() {
        return anchorY;
    }

    /**
     * @param x Spalte
     * @param y Zeile
     * @return ARGB-Pixel oder 0 ausserhalb
     */
    public int get(int x, int y) {
        return x < 0 || y < 0 || x >= width || y >= height ? 0 : pixels[y * width + x];
    }

    /**
     * @return Leuchtebene oder {@code null}
     */
    public Sprite glow() {
        return glow;
    }

    /**
     * @param value Leuchtebene gleicher Grösse
     * @return dieses Sprite
     */
    public Sprite withGlow(Sprite value) {
        glow = value;
        return this;
    }

    /**
     * @return Kopie der Pixel
     */
    public int[] pixels() {
        return pixels.clone();
    }
}
