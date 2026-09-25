package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.pixel.Frame;

import java.util.Random;

/**
 * Tiefsee hinter den Bullaugen: Farbverlauf mit Raster, Lichtschächte, Felsnadeln, Tang,
 * Meeresschnee, Fischschwärme, ferne Leuchtwesen und gelegentlich ein vorbeiziehender Leviathan.
 * Alles mit eigener Parallaxe, damit das Boot sich zu bewegen scheint.
 */
public final class Ocean {
    private static final int[][] SECTOR_COLORS = {
        {0xFF12303A, 0xFF0A1C26, 0xFF040B12},
        {0xFF0E3432, 0xFF08201F, 0xFF030C0C},
        {0xFF1C2448, 0xFF10142C, 0xFF060816},
        {0xFF0E2A4A, 0xFF08172C, 0xFF030912}
    };
    private final int width, height;
    private final int[] gradient;
    private final double[] snowX, snowY, snowSpeed;
    private final int[] rocks;
    private int sector = -1;
    private double time;

    /**
     * @param width Bildbreite
     * @param height Bildhöhe
     */
    public Ocean(int width, int height) {
        this.width = width;
        this.height = height;
        gradient = new int[width * height];
        var random = new Random(99);
        snowX = new double[90];
        snowY = new double[90];
        snowSpeed = new double[90];
        for (int i = 0; i < snowX.length; i++) {
            snowX[i] = random.nextDouble() * width;
            snowY[i] = random.nextDouble() * height;
            snowSpeed[i] = .3 + random.nextDouble();
        }
        rocks = new int[width * 3];
        double h = 40;
        for (int x = 0; x < rocks.length; x++) {
            h += (random.nextDouble() - .5) * 6;
            h = Math.max(10, Math.min(90, h));
            rocks[x] = (int) (h + Math.max(0, Math.sin(x * .02) * 40) + (x % 97 < 6 ? 30 : 0));
        }
    }

    /**
     * @param dt Sekunden
     */
    public void update(double dt) {
        time += dt;
    }

    /**
     * Zeichnet das Meer bildschirmfüllend.
     *
     * @param frame Farbebene
     * @param emissive Leuchtebene
     * @param camX Kameraposition für die Parallaxe
     * @param sector Sektor für die Farbstimmung
     * @param calm ruhige Darstellung ohne bewegte Elemente
     */
    public void draw(Frame frame, Frame emissive, double camX, int sector, boolean calm) {
        if (sector != this.sector) buildGradient(sector);
        System.arraycopy(gradient, 0, frame.pixels(), 0, gradient.length);
        double t = calm ? 0 : time;
        // Lichtschächte von oben
        for (int s = 0; s < 4; s++) {
            double base =
                    ((s * 173 - camX * .08 + Math.sin(t * .2 + s) * 20) % (width + 160)
                                            + width
                                            + 160)
                                    % (width + 160)
                            - 80;
            for (int y = 0; y < height * .75; y++) {
                double fade = 1 - y / (height * .75);
                int x0 = (int) (base + y * .35);
                for (int w = 0; w < 18 + s * 5; w++)
                    if (((x0 + w + y) & 3) != 0) frame.add(x0 + w, y, 0xFF6FB8C8, .06 * fade);
            }
        }
        // Ferne Felsnadeln
        int far = SECTOR_COLORS[Math.max(0, Math.min(3, sector))][2];
        int rockColor = Pal.mix(far, 0xFF000000, .3);
        for (int x = 0; x < width; x++) {
            int index = (int) ((x + camX * .15) % rocks.length);
            if (index < 0) index += rocks.length;
            int top = height - rocks[index];
            frame.fill(x, top, 1, height - top, rockColor);
        }
        // Tang
        for (int k = 0; k < 14; k++) {
            double x = ((k * 91 - camX * .32) % (width + 60) + width + 60) % (width + 60) - 30;
            int len = 50 + (k * 37) % 70;
            for (int y = 0; y < len; y++) {
                int px = (int) (x + Math.sin(y * .08 + t * .8 + k) * (y * .12));
                frame.pixel(px, height - y, Pal.mix(Pal.GREEN_0, far, .35));
                if (y % 9 == 4) frame.pixel(px + 1, height - y, Pal.GREEN_1);
            }
        }
        // Leviathan zieht alle 45 Sekunden vorbei
        double cycle = t % 45;
        if (!calm && cycle < 14) {
            double lx = width + 120 - cycle / 14 * (width + 400);
            int ly = (int) (height * .38 + Math.sin(cycle * .5) * 8);
            int dark = Pal.mix(far, 0xFF000000, .55);
            for (int i = 0; i < 180; i++) {
                double r = 22 * Math.sin(Math.PI * Math.min(1, i / 170.0)) + 3;
                int cx = (int) (lx + i);
                int cy = (int) (ly + Math.sin(i * .05 + cycle) * 6);
                frame.fill(cx, (int) (cy - r * .6), 1, (int) (r * 1.2), dark);
            }
            emissive.add((int) lx + 14, ly - 4, Pal.RUST_6, .9);
            emissive.add((int) lx + 15, ly - 4, Pal.RUST_5, .6);
        }
        // Fischschwarm
        for (int k = 0; k < 3; k++) {
            double sx =
                    ((t * (12 + k * 5) + k * 200 - camX * .4) % (width + 200) + width + 200)
                                    % (width + 200)
                            - 100;
            int sy = 60 + k * 45;
            for (int f = 0; f < 9; f++) {
                int fx = (int) (sx + (f % 3) * 7 + Math.sin(t * 2 + f) * 2);
                int fy = (int) (sy + (f / 3) * 5 + Math.cos(t * 1.7 + f) * 2);
                frame.fill(fx, fy, 3, 1, Pal.mix(far, Pal.TIDE, .6));
                frame.pixel(fx - 1, fy, Pal.mix(far, Pal.TIDE, .4));
            }
        }
        // Ferne Leuchtwesen
        for (int k = 0; k < 8; k++) {
            double x = ((k * 67 + Math.sin(t * .3 + k) * 20 - camX * .25) % width + width) % width;
            double y = 40 + (k * 53) % 150 + Math.sin(t * .5 + k * 2) * 6;
            double pulse = .5 + .5 * Math.sin(t * 1.5 + k);
            int color = k % 3 == 0 ? Pal.VIOLET_4 : Pal.TEAL_5;
            emissive.add((int) x, (int) y, color, .35 * pulse);
            frame.pixel((int) x, (int) y, Pal.mix(far, color, .5));
        }
        // Meeresschnee driftet nach hinten
        for (int i = 0; i < snowX.length; i++) {
            double x =
                    ((snowX[i] - t * 14 * snowSpeed[i] - camX * .5 * snowSpeed[i]) % width + width)
                            % width;
            double y = ((snowY[i] - t * 3 * snowSpeed[i]) % height + height) % height;
            frame.pixel((int) x, (int) y, snowSpeed[i] > 1 ? 0xFF9FC4CC : 0xFF5E8490);
        }
    }

    private void buildGradient(int sector) {
        this.sector = sector;
        int[] c = SECTOR_COLORS[Math.max(0, Math.min(3, sector))];
        int[] bayer = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};
        for (int y = 0; y < height; y++) {
            double t = y / (double) height;
            for (int x = 0; x < width; x++) {
                double v = t * 6 + (bayer[(y & 3) * 4 + (x & 3)] / 16.0 - .5);
                int step = (int) Math.max(0, Math.min(6, Math.floor(v)));
                int color =
                        step < 3
                                ? Pal.mix(c[0], c[1], step / 3.0)
                                : Pal.mix(c[1], c[2], (step - 3) / 3.0);
                gradient[y * width + x] = color;
            }
        }
    }
}
