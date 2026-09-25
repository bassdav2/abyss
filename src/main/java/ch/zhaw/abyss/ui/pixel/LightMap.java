package ch.zhaw.abyss.ui.pixel;

import java.util.Arrays;

/**
 * Lichtkarte in halber Pixelauflösung. Punkt- und Kegellichter addieren Farbe; beim Anwenden wird
 * das Licht in Stufen quantisiert und mit einer Bayer-Matrix gerastert. Das ergibt die typischen
 * gestuften Lichtkegel von Pixelspielen statt weicher Verläufe.
 */
public final class LightMap {
    private static final int[] BAYER = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};
    private static final double STEPS = 9;
    private final int width, height, scale;
    private final float[] red, green, blue;

    /**
     * @param frameWidth Breite des Ziel-Frames
     * @param frameHeight Höhe des Ziel-Frames
     * @param scale Verkleinerungsfaktor der Lichtkarte
     */
    public LightMap(int frameWidth, int frameHeight, int scale) {
        this.scale = scale;
        width = (frameWidth + scale - 1) / scale;
        height = (frameHeight + scale - 1) / scale;
        red = new float[width * height];
        green = new float[width * height];
        blue = new float[width * height];
    }

    /**
     * Setzt das Grundlicht.
     *
     * @param rgb Farbe
     * @param intensity Helligkeit 0 bis 1
     */
    public void ambient(int rgb, double intensity) {
        Arrays.fill(red, (float) (((rgb >> 16) & 255) / 255.0 * intensity));
        Arrays.fill(green, (float) (((rgb >> 8) & 255) / 255.0 * intensity));
        Arrays.fill(blue, (float) ((rgb & 255) / 255.0 * intensity));
    }

    /**
     * Fügt ein weiches Punktlicht hinzu.
     *
     * @param x Mitte in Frame-Pixeln
     * @param y Mitte in Frame-Pixeln
     * @param radius Radius in Frame-Pixeln
     * @param rgb Farbe
     * @param intensity Helligkeit in der Mitte
     */
    public void point(double x, double y, double radius, int rgb, double intensity) {
        if (radius <= 1 || intensity <= 0) return;
        double cx = x / scale, cy = y / scale, r = radius / scale, r2 = r * r;
        float cr = (float) (((rgb >> 16) & 255) / 255.0 * intensity);
        float cg = (float) (((rgb >> 8) & 255) / 255.0 * intensity);
        float cb = (float) ((rgb & 255) / 255.0 * intensity);
        int x0 = Math.max(0, (int) (cx - r)), x1 = Math.min(width - 1, (int) (cx + r));
        int y0 = Math.max(0, (int) (cy - r)), y1 = Math.min(height - 1, (int) (cy + r));
        for (int yy = y0; yy <= y1; yy++) {
            double dy = yy + .5 - cy;
            for (int xx = x0; xx <= x1; xx++) {
                double dx = xx + .5 - cx, d = dx * dx + dy * dy;
                if (d >= r2) continue;
                float f = (float) (1 - d / r2);
                f *= f;
                int i = yy * width + xx;
                red[i] += cr * f;
                green[i] += cg * f;
                blue[i] += cb * f;
            }
        }
    }

    /**
     * Fügt einen Lichtkegel hinzu, etwa die Stirnlampe.
     *
     * @param x Ursprung
     * @param y Ursprung
     * @param angle Richtung in Radiant
     * @param spread halber Öffnungswinkel
     * @param length Reichweite in Frame-Pixeln
     * @param rgb Farbe
     * @param intensity Helligkeit
     */
    public void cone(
            double x,
            double y,
            double angle,
            double spread,
            double length,
            int rgb,
            double intensity) {
        double cx = x / scale, cy = y / scale, r = length / scale, r2 = r * r;
        float cr = (float) (((rgb >> 16) & 255) / 255.0 * intensity);
        float cg = (float) (((rgb >> 8) & 255) / 255.0 * intensity);
        float cb = (float) ((rgb & 255) / 255.0 * intensity);
        int x0 = Math.max(0, (int) (cx - r)), x1 = Math.min(width - 1, (int) (cx + r));
        int y0 = Math.max(0, (int) (cy - r)), y1 = Math.min(height - 1, (int) (cy + r));
        double cos = Math.cos(angle), sin = Math.sin(angle);
        for (int yy = y0; yy <= y1; yy++)
            for (int xx = x0; xx <= x1; xx++) {
                double dx = xx + .5 - cx, dy = yy + .5 - cy, d = dx * dx + dy * dy;
                if (d >= r2 || d < .01) continue;
                double along = (dx * cos + dy * sin) / Math.sqrt(d);
                double edge = Math.cos(spread);
                if (along <= edge) continue;
                float f = (float) ((1 - d / r2) * Math.min(1, (along - edge) / (1 - edge) * 2.5));
                int i = yy * width + xx;
                red[i] += cr * f;
                green[i] += cg * f;
                blue[i] += cb * f;
            }
    }

    /**
     * Fügt einen senkrechten Lichtschacht hinzu, etwa Licht durch ein Bullauge.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param rgb Farbe
     * @param intensity Helligkeit oben; nach unten schwächer
     */
    public void shaft(double x, double y, double w, double h, int rgb, double intensity) {
        float cr = (float) (((rgb >> 16) & 255) / 255.0 * intensity);
        float cg = (float) (((rgb >> 8) & 255) / 255.0 * intensity);
        float cb = (float) ((rgb & 255) / 255.0 * intensity);
        int x0 = Math.max(0, (int) (x / scale)), x1 = Math.min(width, (int) ((x + w) / scale));
        int y0 = Math.max(0, (int) (y / scale)), y1 = Math.min(height, (int) ((y + h) / scale));
        for (int yy = y0; yy < y1; yy++) {
            float f = 1 - (float) (yy - y0) / Math.max(1, y1 - y0);
            for (int xx = x0; xx < x1; xx++) {
                float edge = Math.min(1, Math.min(xx - x0 + 1, x1 - xx) / 2f);
                int i = yy * width + xx;
                red[i] += cr * f * edge;
                green[i] += cg * f * edge;
                blue[i] += cb * f * edge;
            }
        }
    }

    /**
     * Multipliziert das Albedo mit dem gestuften Licht und addiert die Leuchtebene.
     *
     * @param albedo Farbbild, wird überschrieben
     * @param emissive Leuchtebene oder {@code null}
     * @param top erste Zeile, die beleuchtet wird; darüber bleibt das Bild unverändert
     */
    public void apply(Frame albedo, Frame emissive, int top) {
        int fw = albedo.width, fh = albedo.height;
        int[] px = albedo.pixels;
        int[] glow = emissive == null ? null : emissive.pixels;
        for (int y = Math.max(0, top); y < fh; y++) {
            int ly = Math.min(height - 1, y / scale);
            for (int x = 0; x < fw; x++) {
                int li = ly * width + Math.min(width - 1, x / scale);
                double threshold = (BAYER[(y & 3) * 4 + (x & 3)] + .5) / 16.0;
                double lr = quantize(red[li], threshold);
                double lg = quantize(green[li], threshold);
                double lb = quantize(blue[li], threshold);
                int i = y * fw + x, c = px[i];
                int r = (int) (((c >> 16) & 255) * lr);
                int g = (int) (((c >> 8) & 255) * lg);
                int b = (int) ((c & 255) * lb);
                if (glow != null) {
                    int e = glow[i];
                    r += (e >> 16) & 255;
                    g += (e >> 8) & 255;
                    b += e & 255;
                }
                px[i] =
                        0xFF000000
                                | Math.min(255, r) << 16
                                | Math.min(255, g) << 8
                                | Math.min(255, b);
            }
        }
    }

    private static double quantize(float value, double threshold) {
        double v = Math.min(1.6, value) * STEPS;
        return Math.floor(v + threshold) / STEPS;
    }
}
