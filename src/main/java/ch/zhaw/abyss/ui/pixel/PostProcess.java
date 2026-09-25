package ch.zhaw.abyss.ui.pixel;

/**
 * Nachbearbeitung des fertigen Pixelbilds: Bloom aus der Leuchtebene, Farbstimmung, Vignette,
 * chromatische Aberration und Bildblitze. Reine Pixelarithmetik ohne JavaFX.
 */
public final class PostProcess {
    private final int width, height, bw, bh;
    private final int[] bloom, temp;
    private final float[] vignette;
    private final int[] lutR = new int[256], lutG = new int[256], lutB = new int[256];
    private int gradeKey = Integer.MIN_VALUE;

    /**
     * @param width Frame-Breite
     * @param height Frame-Höhe
     */
    public PostProcess(int width, int height) {
        this.width = width;
        this.height = height;
        bw = width / 4;
        bh = height / 4;
        bloom = new int[bw * bh];
        temp = new int[bw * bh];
        vignette = new float[width * height];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                double dx = (x - width / 2.0) / (width / 2.0),
                        dy = (y - height / 2.0) / (height / 2.0);
                double d = Math.sqrt(dx * dx * .8 + dy * dy);
                vignette[y * width + x] = (float) Math.max(0, Math.min(1, (d - .55) / .75));
            }
    }

    /**
     * Addiert einen weichen Schein um helle Leuchtpixel.
     *
     * @param frame Ziel
     * @param emissive Leuchtebene
     * @param strength Stärke
     */
    public void bloom(Frame frame, Frame emissive, double strength) {
        int[] e = emissive.pixels;
        for (int y = 0; y < bh; y++)
            for (int x = 0; x < bw; x++) {
                int r = 0, g = 0, b = 0;
                for (int oy = 0; oy < 4; oy++)
                    for (int ox = 0; ox < 4; ox++) {
                        int c = e[(y * 4 + oy) * width + x * 4 + ox];
                        r += (c >> 16) & 255;
                        g += (c >> 8) & 255;
                        b += c & 255;
                    }
                bloom[y * bw + x] = (r / 16) << 16 | (g / 16) << 8 | (b / 16);
            }
        blur(bloom, temp, true);
        blur(temp, bloom, false);
        blur(bloom, temp, true);
        blur(temp, bloom, false);
        int[] px = frame.pixels;
        for (int y = 0; y < height; y++) {
            double fy = y / 4.0 - .5;
            int y0 = Math.max(0, Math.min(bh - 1, (int) Math.floor(fy)));
            int y1 = Math.min(bh - 1, y0 + 1);
            double ty = Math.max(0, Math.min(1, fy - y0));
            for (int x = 0; x < width; x++) {
                double fx = x / 4.0 - .5;
                int x0 = Math.max(0, Math.min(bw - 1, (int) Math.floor(fx)));
                int x1 = Math.min(bw - 1, x0 + 1);
                double tx = Math.max(0, Math.min(1, fx - x0));
                int a = bloom[y0 * bw + x0], b = bloom[y0 * bw + x1];
                int c = bloom[y1 * bw + x0], d = bloom[y1 * bw + x1];
                double r =
                        lerp2(a >> 16 & 255, b >> 16 & 255, c >> 16 & 255, d >> 16 & 255, tx, ty);
                double g = lerp2(a >> 8 & 255, b >> 8 & 255, c >> 8 & 255, d >> 8 & 255, tx, ty);
                double bl = lerp2(a & 255, b & 255, c & 255, d & 255, tx, ty);
                int i = y * width + x, p = px[i];
                int nr = Math.min(255, (p >> 16 & 255) + (int) (r * strength));
                int ng = Math.min(255, (p >> 8 & 255) + (int) (g * strength));
                int nb = Math.min(255, (p & 255) + (int) (bl * strength));
                px[i] = 0xFF000000 | nr << 16 | ng << 8 | nb;
            }
        }
    }

    private static double lerp2(int a, int b, int c, int d, double tx, double ty) {
        double top = a + (b - a) * tx, bottom = c + (d - c) * tx;
        return top + (bottom - top) * ty;
    }

    private void blur(int[] src, int[] dst, boolean horizontal) {
        for (int y = 0; y < bh; y++)
            for (int x = 0; x < bw; x++) {
                int r = 0, g = 0, b = 0, n = 0;
                for (int k = -2; k <= 2; k++) {
                    int sx = horizontal ? x + k : x, sy = horizontal ? y : y + k;
                    if (sx < 0 || sy < 0 || sx >= bw || sy >= bh) continue;
                    int c = src[sy * bw + sx];
                    int w = k == 0 ? 2 : 1;
                    r += (c >> 16 & 255) * w;
                    g += (c >> 8 & 255) * w;
                    b += (c & 255) * w;
                    n += w;
                }
                dst[y * bw + x] = (r / n) << 16 | (g / n) << 8 | (b / n);
            }
    }

    /**
     * Wendet eine Farbstimmung an: Schatten werden in Richtung {@code shadow}, Lichter in Richtung
     * {@code highlight} verschoben, dazu leicht erhöhter Kontrast.
     *
     * @param frame Ziel
     * @param shadow Farbe der Schatten
     * @param highlight Farbe der Lichter
     * @param amount Stärke 0 bis 1
     * @param top erste betroffene Zeile
     */
    public void grade(Frame frame, int shadow, int highlight, double amount, int top) {
        int key = shadow * 31 + highlight * 17 + (int) (amount * 1000);
        if (key != gradeKey) {
            gradeKey = key;
            buildLut(lutR, shadow >> 16 & 255, highlight >> 16 & 255, amount);
            buildLut(lutG, shadow >> 8 & 255, highlight >> 8 & 255, amount);
            buildLut(lutB, shadow & 255, highlight & 255, amount);
        }
        int[] px = frame.pixels;
        for (int i = Math.max(0, top) * width; i < px.length; i++) {
            int c = px[i];
            px[i] =
                    0xFF000000
                            | lutR[c >> 16 & 255] << 16
                            | lutG[c >> 8 & 255] << 8
                            | lutB[c & 255];
        }
    }

    private static void buildLut(int[] lut, int shadow, int highlight, double amount) {
        for (int v = 0; v < 256; v++) {
            double t = v / 255.0;
            double curve = t < .5 ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
            double contrast = t + (curve - t) * .35;
            double tinted =
                    contrast
                            + ((shadow / 255.0) * (1 - t) * .35
                                            + (highlight / 255.0 - .5) * t * .25)
                                    * amount;
            lut[v] = (int) Math.max(0, Math.min(255, Math.round(tinted * 255)));
        }
    }

    /**
     * Dunkelt die Bildränder ab, optional mit Farbe (etwa Rot bei niedriger Integrität).
     *
     * @param frame Ziel
     * @param strength Stärke 0 bis 1
     * @param rgb Farbe am Rand
     */
    public void vignette(Frame frame, double strength, int rgb) {
        int[] px = frame.pixels;
        int vr = rgb >> 16 & 255, vg = rgb >> 8 & 255, vb = rgb & 255;
        for (int i = 0; i < px.length; i++) {
            double v = vignette[i] * strength;
            if (v <= 0) continue;
            int c = px[i];
            int r = (int) ((c >> 16 & 255) * (1 - v) + vr * v);
            int g = (int) ((c >> 8 & 255) * (1 - v) + vg * v);
            int b = (int) ((c & 255) * (1 - v) + vb * v);
            px[i] = 0xFF000000 | r << 16 | g << 8 | b;
        }
    }

    /**
     * Verschiebt Rot- und Blaukanal gegeneinander.
     *
     * @param frame Ziel
     * @param offset Verschiebung in Pixeln
     */
    public void aberration(Frame frame, int offset) {
        if (offset <= 0) return;
        int[] px = frame.pixels;
        int[] row = new int[width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(px, y * width, row, 0, width);
            for (int x = 0; x < width; x++) {
                int r = row[Math.min(width - 1, x + offset)] >> 16 & 255;
                int b = row[Math.max(0, x - offset)] & 255;
                int c = row[x];
                px[y * width + x] = 0xFF000000 | r << 16 | (c & 0xFF00) | b;
            }
        }
    }

    /**
     * Mischt das ganze Bild mit einer Farbe.
     *
     * @param frame Ziel
     * @param rgb Farbe
     * @param amount Anteil 0 bis 1
     */
    public void flash(Frame frame, int rgb, double amount) {
        if (amount <= 0) return;
        int a = (int) Math.round(Math.min(1, amount) * 255);
        int[] px = frame.pixels;
        for (int i = 0; i < px.length; i++) px[i] = Frame.blend(px[i], rgb, a);
    }

    /**
     * Horizontale Wellenverzerrung, etwa bei Druckwellen oder Wassereinbruch.
     *
     * @param frame Ziel
     * @param time Zeit in Sekunden
     * @param amplitude Amplitude in Pixeln
     */
    public void ripple(Frame frame, double time, double amplitude) {
        if (amplitude < .5) return;
        int[] px = frame.pixels;
        int[] row = new int[width];
        for (int y = 0; y < height; y++) {
            int shift = (int) Math.round(Math.sin(y * .18 + time * 9) * amplitude);
            if (shift == 0) continue;
            System.arraycopy(px, y * width, row, 0, width);
            for (int x = 0; x < width; x++)
                px[y * width + x] = row[Math.max(0, Math.min(width - 1, x - shift))];
        }
    }
}
