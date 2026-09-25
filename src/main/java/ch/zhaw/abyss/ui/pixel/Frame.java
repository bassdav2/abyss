package ch.zhaw.abyss.ui.pixel;

import java.util.Arrays;

/**
 * Software-Framebuffer für die Pixeldarstellung. Alle Zeichenoperationen arbeiten auf ganzzahligen
 * Pixelkoordinaten, beachten ein Clipping-Rechteck und mischen Alpha selbst. Die Klasse kennt
 * JavaFX nicht und ist damit ohne Fenster testbar.
 */
public final class Frame {
    final int width, height;
    final int[] pixels;
    private int clipX0, clipY0, clipX1, clipY1;

    /**
     * @param width Breite in Pixeln
     * @param height Höhe in Pixeln
     */
    public Frame(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        resetClip();
    }

    /**
     * @return Breite
     */
    public int width() {
        return width;
    }

    /**
     * @return Höhe
     */
    public int height() {
        return height;
    }

    /**
     * @return direkter Pixelspeicher für Nachbearbeitung und Hochladen
     */
    public int[] pixels() {
        return pixels;
    }

    /**
     * Begrenzt alle weiteren Zeichenoperationen.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     */
    public void clip(int x, int y, int w, int h) {
        clipX0 = Math.max(0, x);
        clipY0 = Math.max(0, y);
        clipX1 = Math.min(width, x + w);
        clipY1 = Math.min(height, y + h);
    }

    /** Hebt das Clipping auf. */
    public void resetClip() {
        clipX0 = 0;
        clipY0 = 0;
        clipX1 = width;
        clipY1 = height;
    }

    /**
     * @param argb Farbe für die gesamte Fläche
     */
    public void clear(int argb) {
        Arrays.fill(pixels, argb | 0xFF000000);
    }

    /**
     * Mischt ein Pixel mit Alpha.
     *
     * @param x Spalte
     * @param y Zeile
     * @param argb Farbe mit Alpha
     */
    public void pixel(int x, int y, int argb) {
        if (x < clipX0 || y < clipY0 || x >= clipX1 || y >= clipY1) return;
        int a = argb >>> 24;
        if (a == 0) return;
        int i = y * width + x;
        pixels[i] = a == 255 ? argb : blend(pixels[i], argb, a);
    }

    /**
     * Addiert Licht auf ein Pixel.
     *
     * @param x Spalte
     * @param y Zeile
     * @param rgb Farbe
     * @param intensity Faktor 0 bis 1
     */
    public void add(int x, int y, int rgb, double intensity) {
        if (x < clipX0 || y < clipY0 || x >= clipX1 || y >= clipY1 || intensity <= 0) return;
        int i = y * width + x;
        pixels[i] = addColor(pixels[i], rgb, intensity);
    }

    /**
     * Füllt ein Rechteck.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param argb Farbe mit Alpha
     */
    public void fill(int x, int y, int w, int h, int argb) {
        int x0 = Math.max(clipX0, x), y0 = Math.max(clipY0, y);
        int x1 = Math.min(clipX1, x + w), y1 = Math.min(clipY1, y + h);
        int a = argb >>> 24;
        if (a == 0 || x0 >= x1 || y0 >= y1) return;
        for (int yy = y0; yy < y1; yy++) {
            int row = yy * width;
            if (a == 255) Arrays.fill(pixels, row + x0, row + x1, argb);
            else
                for (int xx = x0; xx < x1; xx++)
                    pixels[row + xx] = blend(pixels[row + xx], argb, a);
        }
    }

    /**
     * Addiert Licht auf ein Rechteck.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param rgb Farbe
     * @param intensity Faktor
     */
    public void addRect(int x, int y, int w, int h, int rgb, double intensity) {
        int x0 = Math.max(clipX0, x), y0 = Math.max(clipY0, y);
        int x1 = Math.min(clipX1, x + w), y1 = Math.min(clipY1, y + h);
        for (int yy = y0; yy < y1; yy++)
            for (int xx = x0; xx < x1; xx++) {
                int i = yy * width + xx;
                pixels[i] = addColor(pixels[i], rgb, intensity);
            }
    }

    /**
     * Zeichnet einen Rahmen.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param argb Farbe
     */
    public void rect(int x, int y, int w, int h, int argb) {
        fill(x, y, w, 1, argb);
        fill(x, y + h - 1, w, 1, argb);
        fill(x, y + 1, 1, h - 2, argb);
        fill(x + w - 1, y + 1, 1, h - 2, argb);
    }

    /**
     * Bresenham-Linie.
     *
     * @param x0 Start x
     * @param y0 Start y
     * @param x1 Ende x
     * @param y1 Ende y
     * @param argb Farbe
     */
    public void line(int x0, int y0, int x1, int y1, int argb) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        for (int guard = 0; guard < 4096; guard++) {
            pixel(x0, y0, argb);
            if (x0 == x1 && y0 == y1) return;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    /**
     * Additive Linie, etwa für Blitze.
     *
     * @param x0 Start x
     * @param y0 Start y
     * @param x1 Ende x
     * @param y1 Ende y
     * @param rgb Farbe
     * @param intensity Faktor
     */
    public void lineAdd(int x0, int y0, int x1, int y1, int rgb, double intensity) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        for (int guard = 0; guard < 4096; guard++) {
            add(x0, y0, rgb, intensity);
            if (x0 == x1 && y0 == y1) return;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    /**
     * Gefüllter Kreis.
     *
     * @param cx Mitte x
     * @param cy Mitte y
     * @param r Radius
     * @param argb Farbe
     */
    public void disc(int cx, int cy, int r, int argb) {
        for (int y = -r; y <= r; y++) {
            int span = (int) Math.sqrt(r * r - y * y + r * .8);
            fill(cx - span, cy + y, span * 2 + 1, 1, argb);
        }
    }

    /**
     * Additiver, weich auslaufender Kreis.
     *
     * @param cx Mitte x
     * @param cy Mitte y
     * @param r Radius
     * @param rgb Farbe
     * @param intensity Faktor in der Mitte
     */
    public void glow(int cx, int cy, int r, int rgb, double intensity) {
        if (r <= 0 || intensity <= 0) return;
        int r2 = r * r;
        for (int y = -r; y <= r; y++)
            for (int x = -r; x <= r; x++) {
                int d = x * x + y * y;
                if (d >= r2) continue;
                double f = 1 - (double) d / r2;
                add(cx + x, cy + y, rgb, intensity * f * f);
            }
    }

    /**
     * Kreislinie.
     *
     * @param cx Mitte x
     * @param cy Mitte y
     * @param r Radius
     * @param argb Farbe
     */
    public void circle(int cx, int cy, int r, int argb) {
        int x = r, y = 0, err = 1 - r;
        while (x >= y) {
            pixel(cx + x, cy + y, argb);
            pixel(cx + y, cy + x, argb);
            pixel(cx - y, cy + x, argb);
            pixel(cx - x, cy + y, argb);
            pixel(cx - x, cy - y, argb);
            pixel(cx - y, cy - x, argb);
            pixel(cx + y, cy - x, argb);
            pixel(cx + x, cy - y, argb);
            y++;
            if (err < 0) err += 2 * y + 1;
            else {
                x--;
                err += 2 * (y - x) + 1;
            }
        }
    }

    /**
     * Zeichnet ein Sprite an seinem Ankerpunkt.
     *
     * @param s Sprite
     * @param x Zielposition des Ankers
     * @param y Zielposition des Ankers
     * @param flip horizontal gespiegelt
     */
    public void draw(Sprite s, int x, int y, boolean flip) {
        draw(s, x, y, flip, 0, 0, 1);
    }

    /**
     * Zeichnet ein Sprite mit Einfärbung und Transparenz.
     *
     * @param s Sprite
     * @param x Zielposition des Ankers
     * @param y Zielposition des Ankers
     * @param flip horizontal gespiegelt
     * @param tint Mischfarbe, etwa Weiss für Trefferblitze
     * @param tintAmount Anteil der Mischfarbe 0 bis 1
     * @param alpha Deckkraft 0 bis 1
     */
    public void draw(
            Sprite s, int x, int y, boolean flip, int tint, double tintAmount, double alpha) {
        if (s == null || alpha <= 0) return;
        int left = flip ? x - (s.width - 1 - s.anchorX) : x - s.anchorX;
        int top = y - s.anchorY;
        int x0 = Math.max(clipX0, left), y0 = Math.max(clipY0, top);
        int x1 = Math.min(clipX1, left + s.width), y1 = Math.min(clipY1, top + s.height);
        int ta = (int) Math.round(Math.max(0, Math.min(1, tintAmount)) * 255);
        int globalAlpha = (int) Math.round(Math.min(1, alpha) * 255);
        for (int yy = y0; yy < y1; yy++) {
            int srcRow = (yy - top) * s.width, dstRow = yy * width;
            for (int xx = x0; xx < x1; xx++) {
                int sx = xx - left;
                int c = s.pixels[srcRow + (flip ? s.width - 1 - sx : sx)];
                int a = c >>> 24;
                if (a == 0) continue;
                if (ta > 0) c = blend(c, tint, ta);
                a = a * globalAlpha / 255;
                int i = dstRow + xx;
                pixels[i] = a >= 255 ? c | 0xFF000000 : blend(pixels[i], c, a);
            }
        }
    }

    /**
     * Zeichnet ein Sprite ganzzahlig vergrössert mit der linken oberen Ecke an (x, y), etwa für
     * Symbole und Vorschaubilder in Menüs.
     *
     * @param s Sprite
     * @param x linke Kante
     * @param y obere Kante
     * @param scale ganzzahliger Faktor ab 1
     * @param alpha Deckkraft 0 bis 1
     */
    public void drawScaled(Sprite s, int x, int y, int scale, double alpha) {
        if (s == null || alpha <= 0 || scale < 1) return;
        int globalAlpha = (int) Math.round(Math.min(1, alpha) * 255);
        for (int sy = 0; sy < s.height; sy++)
            for (int sx = 0; sx < s.width; sx++) {
                int c = s.pixels[sy * s.width + sx];
                int a = (c >>> 24) * globalAlpha / 255;
                if (a == 0) continue;
                fill(x + sx * scale, y + sy * scale, scale, scale, (a << 24) | (c & 0xFFFFFF));
            }
    }

    /**
     * Zeichnet ein Sprite additiv, etwa Leuchtebenen oder Effekte.
     *
     * @param s Sprite
     * @param x Zielposition des Ankers
     * @param y Zielposition des Ankers
     * @param flip gespiegelt
     * @param intensity Faktor
     */
    public void drawAdd(Sprite s, int x, int y, boolean flip, double intensity) {
        if (s == null || intensity <= 0) return;
        int left = flip ? x - (s.width - 1 - s.anchorX) : x - s.anchorX;
        int top = y - s.anchorY;
        int x0 = Math.max(clipX0, left), y0 = Math.max(clipY0, top);
        int x1 = Math.min(clipX1, left + s.width), y1 = Math.min(clipY1, top + s.height);
        for (int yy = y0; yy < y1; yy++) {
            int srcRow = (yy - top) * s.width, dstRow = yy * width;
            for (int xx = x0; xx < x1; xx++) {
                int sx = xx - left;
                int c = s.pixels[srcRow + (flip ? s.width - 1 - sx : sx)];
                int a = c >>> 24;
                if (a == 0) continue;
                pixels[dstRow + xx] = addColor(pixels[dstRow + xx], c, intensity * a / 255.0);
            }
        }
    }

    /**
     * Zeichnet ein Sprite einfarbig, etwa als Silhouette oder Umriss.
     *
     * @param s Sprite
     * @param x Zielposition des Ankers
     * @param y Zielposition des Ankers
     * @param flip gespiegelt
     * @param argb Farbe mit Alpha
     */
    public void silhouette(Sprite s, int x, int y, boolean flip, int argb) {
        if (s == null) return;
        int left = flip ? x - (s.width - 1 - s.anchorX) : x - s.anchorX;
        int top = y - s.anchorY;
        for (int yy = 0; yy < s.height; yy++)
            for (int xx = 0; xx < s.width; xx++) {
                int c = s.pixels[yy * s.width + (flip ? s.width - 1 - xx : xx)];
                if (c >>> 24 != 0) pixel(left + xx, top + yy, argb);
            }
    }

    /**
     * Kopiert einen Ausschnitt eines anderen Puffers deckend in diesen Frame.
     *
     * @param source Quellpuffer
     * @param sourceWidth Breite der Quelle
     * @param sourceX linke Kante in der Quelle
     * @param destY Zielzeile
     * @param rows Anzahl Zeilen
     */
    public void copyStrip(int[] source, int sourceWidth, int sourceX, int destY, int rows) {
        int sourceHeight = source.length / sourceWidth;
        for (int y = 0; y < rows && y + destY < height && y < sourceHeight; y++) {
            int dy = y + destY;
            if (dy < 0) continue;
            for (int x = 0; x < width; x++) {
                int sx = sourceX + x;
                int c = sx < 0 || sx >= sourceWidth ? 0xFF000000 : source[y * sourceWidth + sx];
                int a = c >>> 24;
                if (a == 255) pixels[dy * width + x] = c;
                else if (a > 0) pixels[dy * width + x] = blend(pixels[dy * width + x], c, a);
            }
        }
    }

    /**
     * Mischt zwei Farben.
     *
     * @param dst Hintergrund
     * @param src Vordergrund
     * @param a Alpha des Vordergrunds 0 bis 255
     * @return deckende Mischfarbe
     */
    public static int blend(int dst, int src, int a) {
        int ia = 255 - a;
        int r = (((src >> 16) & 255) * a + ((dst >> 16) & 255) * ia) / 255;
        int g = (((src >> 8) & 255) * a + ((dst >> 8) & 255) * ia) / 255;
        int b = ((src & 255) * a + (dst & 255) * ia) / 255;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /**
     * Addiert eine Farbe mit Sättigung.
     *
     * @param dst Ausgangsfarbe
     * @param rgb addierte Farbe
     * @param intensity Faktor
     * @return Ergebnis
     */
    public static int addColor(int dst, int rgb, double intensity) {
        int r = Math.min(255, ((dst >> 16) & 255) + (int) (((rgb >> 16) & 255) * intensity));
        int g = Math.min(255, ((dst >> 8) & 255) + (int) (((rgb >> 8) & 255) * intensity));
        int b = Math.min(255, (dst & 255) + (int) ((rgb & 255) * intensity));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /**
     * Interpoliert linear zwischen zwei Farben.
     *
     * @param a erste Farbe
     * @param b zweite Farbe
     * @param t Anteil der zweiten Farbe
     * @return Mischfarbe mit voller Deckkraft
     */
    public static int mix(int a, int b, double t) {
        return blend(a, b, (int) Math.round(Math.max(0, Math.min(1, t)) * 255));
    }

    /**
     * @param argb Farbe
     * @param alpha neue Deckkraft 0 bis 1
     * @return Farbe mit gesetztem Alpha
     */
    public static int alpha(int argb, double alpha) {
        return ((int) Math.round(Math.max(0, Math.min(1, alpha)) * 255) << 24) | (argb & 0xFFFFFF);
    }
}
