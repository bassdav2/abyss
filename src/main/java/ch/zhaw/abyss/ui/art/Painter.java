package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.Arrays;

/**
 * Kleine Zeichenfläche zum prozeduralen Malen von Pixelgrafiken: Formen, schattierte Körper,
 * gedrehte Stempel und eine automatische Kontur. Eine zweite Ebene nimmt leuchtende Pixel auf.
 */
public final class Painter {
    final int width, height;
    final int[] color;
    final int[] glow;

    /**
     * @param width Breite
     * @param height Höhe
     */
    public Painter(int width, int height) {
        this.width = width;
        this.height = height;
        color = new int[width * height];
        glow = new int[width * height];
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
     * @param x Spalte
     * @param y Zeile
     * @param c Farbe; Alpha 0 löscht
     */
    public void px(int x, int y, int c) {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        color[y * width + x] = c;
    }

    /**
     * @param x Spalte
     * @param y Zeile
     * @return Farbe oder 0
     */
    public int get(int x, int y) {
        return x < 0 || y < 0 || x >= width || y >= height ? 0 : color[y * width + x];
    }

    /**
     * Setzt ein leuchtendes Pixel; es erscheint auch in der Farbebene.
     *
     * @param x Spalte
     * @param y Zeile
     * @param c Farbe
     */
    public void light(int x, int y, int c) {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        color[y * width + x] = c;
        glow[y * width + x] = c;
    }

    /**
     * Setzt nur die Leuchtebene, etwa für einen weichen Schein.
     *
     * @param x Spalte
     * @param y Zeile
     * @param c Farbe
     */
    public void glowOnly(int x, int y, int c) {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        glow[y * width + x] = c;
    }

    /**
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param c Farbe
     */
    public void rect(int x, int y, int w, int h, int c) {
        for (int yy = y; yy < y + h; yy++) for (int xx = x; xx < x + w; xx++) px(xx, yy, c);
    }

    /**
     * Leuchtendes Rechteck.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param c Farbe
     */
    public void lightRect(int x, int y, int w, int h, int c) {
        for (int yy = y; yy < y + h; yy++) for (int xx = x; xx < x + w; xx++) light(xx, yy, c);
    }

    /**
     * Rechteck mit Lichtkante oben/links und Schattenkante unten/rechts.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param ramp Farbrampe dunkel, mittel, hell
     */
    public void box(int x, int y, int w, int h, int[] ramp) {
        rect(x, y, w, h, ramp[1]);
        rect(x, y, w, 1, ramp[2]);
        rect(x, y, 1, h, ramp[2]);
        rect(x, y + h - 1, w, 1, ramp[0]);
        rect(x + w - 1, y, 1, h, ramp[0]);
    }

    /**
     * Gefüllte Ellipse.
     *
     * @param cx Mitte
     * @param cy Mitte
     * @param rx Radius x
     * @param ry Radius y
     * @param c Farbe
     */
    public void ellipse(double cx, double cy, double rx, double ry, int c) {
        for (int y = (int) Math.floor(cy - ry); y <= (int) Math.ceil(cy + ry); y++)
            for (int x = (int) Math.floor(cx - rx); x <= (int) Math.ceil(cx + rx); x++) {
                double dx = (x + .5 - cx) / rx, dy = (y + .5 - cy) / ry;
                if (dx * dx + dy * dy <= 1.0) px(x, y, c);
            }
    }

    /**
     * Ellipse mit Kugelschattierung: Glanz oben links, Schatten unten rechts.
     *
     * @param cx Mitte
     * @param cy Mitte
     * @param rx Radius x
     * @param ry Radius y
     * @param ramp Farbrampe dunkel, mittel, hell, Glanz
     */
    public void sphere(double cx, double cy, double rx, double ry, int[] ramp) {
        for (int y = (int) Math.floor(cy - ry); y <= (int) Math.ceil(cy + ry); y++)
            for (int x = (int) Math.floor(cx - rx); x <= (int) Math.ceil(cx + rx); x++) {
                double dx = (x + .5 - cx) / rx, dy = (y + .5 - cy) / ry;
                double d = dx * dx + dy * dy;
                if (d > 1.0) continue;
                double light = -dx * .55 - dy * .75 + (1 - d) * .35;
                int c =
                        light > .62 && ramp.length > 3
                                ? ramp[3]
                                : light > .18 ? ramp[2] : light > -.35 ? ramp[1] : ramp[0];
                px(x, y, c);
            }
    }

    /**
     * Linie mit Pinselbreite (Kapsel).
     *
     * @param x0 Start
     * @param y0 Start
     * @param x1 Ende
     * @param y1 Ende
     * @param thickness Breite in Pixeln
     * @param c Farbe
     */
    public void thick(double x0, double y0, double x1, double y1, double thickness, int c) {
        double r = thickness / 2;
        int minX = (int) Math.floor(Math.min(x0, x1) - r - 1);
        int maxX = (int) Math.ceil(Math.max(x0, x1) + r + 1);
        int minY = (int) Math.floor(Math.min(y0, y1) - r - 1);
        int maxY = (int) Math.ceil(Math.max(y0, y1) + r + 1);
        double dx = x1 - x0, dy = y1 - y0, len2 = Math.max(1e-6, dx * dx + dy * dy);
        for (int y = minY; y <= maxY; y++)
            for (int x = minX; x <= maxX; x++) {
                double px = x + .5, py = y + .5;
                double t = Math.max(0, Math.min(1, ((px - x0) * dx + (py - y0) * dy) / len2));
                double qx = x0 + dx * t - px, qy = y0 + dy * t - py;
                if (qx * qx + qy * qy <= r * r + .1) px(x, y, c);
            }
    }

    /**
     * Einpixelige Linie.
     *
     * @param x0 Start
     * @param y0 Start
     * @param x1 Ende
     * @param y1 Ende
     * @param c Farbe
     */
    public void line(int x0, int y0, int x1, int y1, int c) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        for (int guard = 0; guard < 2048; guard++) {
            px(x0, y0, c);
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
     * Gefülltes Polygon.
     *
     * @param xs x-Koordinaten
     * @param ys y-Koordinaten
     * @param c Farbe
     */
    public void poly(double[] xs, double[] ys, int c) {
        double minY = Arrays.stream(ys).min().orElse(0), maxY = Arrays.stream(ys).max().orElse(0);
        for (int y = (int) Math.floor(minY); y <= (int) Math.ceil(maxY); y++) {
            double py = y + .5;
            for (int x = 0; x < width; x++) {
                double px = x + .5;
                boolean inside = false;
                for (int i = 0, j = xs.length - 1; i < xs.length; j = i++) {
                    if ((ys[i] > py) != (ys[j] > py)
                            && px < (xs[j] - xs[i]) * (py - ys[i]) / (ys[j] - ys[i]) + xs[i])
                        inside = !inside;
                }
                if (inside) px(x, y, c);
            }
        }
    }

    /**
     * Stempelt eine kleine Bitmap gedreht um einen Drehpunkt (inverse Abbildung, lückenlos).
     *
     * @param rows Zeilen; Zeichen werden über {@code colors} aufgelöst, {@code .} ist leer
     * @param colors Zuordnung Zeichen zu Farbe als Paare {@code "a", 0xFF...}
     * @param pivotX Drehpunkt in der Bitmap
     * @param pivotY Drehpunkt in der Bitmap
     * @param x Zielposition des Drehpunkts
     * @param y Zielposition des Drehpunkts
     * @param angle Drehung in Radiant
     * @param flip horizontal gespiegelt
     */
    public void stamp(
            String[] rows,
            Object[] colors,
            double pivotX,
            double pivotY,
            double x,
            double y,
            double angle,
            boolean flip) {
        int h = rows.length, w = 0;
        for (String row : rows) w = Math.max(w, row.length());
        double reach = Math.hypot(Math.max(pivotX, w - pivotX), Math.max(pivotY, h - pivotY)) + 1;
        double cos = Math.cos(-angle), sin = Math.sin(-angle);
        for (int ty = (int) (y - reach); ty <= (int) (y + reach); ty++)
            for (int tx = (int) (x - reach); tx <= (int) (x + reach); tx++) {
                double dx = tx + .5 - x, dy = ty + .5 - y;
                double lx = dx * cos - dy * sin, ly = dx * sin + dy * cos;
                if (flip) lx = -lx;
                int sx = (int) Math.floor(lx + pivotX), sy = (int) Math.floor(ly + pivotY);
                if (sy < 0 || sy >= h || sx < 0 || sx >= rows[sy].length()) continue;
                char ch = rows[sy].charAt(sx);
                if (ch == '.' || ch == ' ') continue;
                for (int i = 0; i < colors.length; i += 2)
                    if (((String) colors[i]).charAt(0) == ch) {
                        int c = (Integer) colors[i + 1];
                        if (Character.isUpperCase(ch) && ch != 'O') light(tx, ty, c);
                        else px(tx, ty, c);
                        break;
                    }
            }
    }

    /**
     * Zeichnet eine Bitmap ungedreht.
     *
     * @param rows Zeilen
     * @param colors Zuordnung Zeichen zu Farbe; Grossbuchstaben leuchten
     * @param x linke Kante
     * @param y obere Kante
     */
    public void bitmap(String[] rows, Object[] colors, int x, int y) {
        for (int yy = 0; yy < rows.length; yy++)
            for (int xx = 0; xx < rows[yy].length(); xx++) {
                char ch = rows[yy].charAt(xx);
                if (ch == '.' || ch == ' ') continue;
                for (int i = 0; i < colors.length; i += 2)
                    if (((String) colors[i]).charAt(0) == ch) {
                        int c = (Integer) colors[i + 1];
                        if (Character.isUpperCase(ch) && ch != 'O') light(x + xx, y + yy, c);
                        else px(x + xx, y + yy, c);
                        break;
                    }
            }
    }

    /**
     * Legt eine einpixelige Kontur um alle deckenden Pixel.
     *
     * @param c Konturfarbe
     */
    public void outline(int c) {
        var copy = color.clone();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if (copy[y * width + x] >>> 24 != 0) continue;
                boolean edge =
                        opaque(copy, x - 1, y)
                                || opaque(copy, x + 1, y)
                                || opaque(copy, x, y - 1)
                                || opaque(copy, x, y + 1);
                if (edge) color[y * width + x] = c;
            }
    }

    /**
     * Setzt eine innere Kante in der Farbe {@code c} auf Pixel, die an Transparenz grenzen und eine
     * der angegebenen Richtungen haben. Nützlich für Randlicht.
     *
     * @param c Farbe
     * @param dx Richtung x (-1 links, 1 rechts)
     * @param dy Richtung y (-1 oben, 1 unten)
     */
    public void rim(int c, int dx, int dy) {
        var copy = color.clone();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int v = copy[y * width + x];
                if (v >>> 24 == 0 || v == Pal.OUTLINE || glow[y * width + x] != 0) continue;
                if (!opaque(copy, x + dx, y + dy)) color[y * width + x] = c;
            }
    }

    private boolean opaque(int[] data, int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && data[y * width + x] >>> 24 != 0;
    }

    /**
     * Ersetzt eine Farbe.
     *
     * @param from alte Farbe
     * @param to neue Farbe
     */
    public void replace(int from, int to) {
        for (int i = 0; i < color.length; i++) if (color[i] == from) color[i] = to;
    }

    /**
     * Erzeugt das Sprite samt Leuchtebene.
     *
     * @param anchorX Ankerpunkt
     * @param anchorY Ankerpunkt
     * @return Sprite
     */
    public Sprite sprite(int anchorX, int anchorY) {
        var sprite = new Sprite(color.clone(), width, height, anchorX, anchorY);
        boolean any = false;
        for (int value : glow)
            if (value != 0) {
                any = true;
                break;
            }
        if (any) sprite.withGlow(new Sprite(glow.clone(), width, height, anchorX, anchorY));
        return sprite;
    }

    /** Löscht beide Ebenen. */
    public void clear() {
        Arrays.fill(color, 0);
        Arrays.fill(glow, 0);
    }
}
