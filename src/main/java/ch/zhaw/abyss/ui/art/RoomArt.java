package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Platform;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.RoomTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Malt einen Raum als Pixelstreifen: Deckenrohre, Rumpfrippen, Wandplatten, Bullaugen mit Blick
 * aufs Meer, themenspezifische Requisiten, Laufstege und Bodengitter. Dazu liefert es Lichtquellen,
 * animierte Elemente und Vordergrundsilhouetten. Alles ist aus dem Raum-Seed reproduzierbar.
 */
public final class RoomArt {
    /** Pixel pro Welteinheit. */
    public static final double PX = .3;

    /** Höhe des Streifens in Pixeln. */
    public static final int HEIGHT = 270;

    /** Bodenhöhe in Pixeln. */
    public static final int FLOOR = (int) Math.round(GameRun.FLOOR * PX);

    /**
     * Statische Lichtquelle.
     *
     * @param x Position im Streifen
     * @param y Position
     * @param radius Radius in Pixeln
     * @param color Farbe
     * @param intensity Helligkeit
     * @param flicker 0 ruhig, 1 flackernd, 2 pulsierend, 3 defekt
     */
    public record Light(int x, int y, int radius, int color, double intensity, int flicker) {}

    /** Arten animierter Elemente. */
    public enum PropKind {
        FAN,
        SCREEN,
        BLINK,
        CORE,
        TANK,
        BEACON,
        WATER,
        CANDLE,
        STEAM,
        DRIP
    }

    /**
     * Animiertes Element.
     *
     * @param kind Art
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param color Hauptfarbe
     */
    public record Prop(PropKind kind, int x, int y, int w, int h, int color) {}

    /**
     * Vordergrundsilhouette mit eigener Parallaxe.
     *
     * @param kind 0 hängendes Kabel, 1 Rohr senkrecht, 2 Strebe, 3 Maschine unten, 4 Kette
     * @param x Position
     * @param size Grösse
     */
    public record Foreground(int kind, int x, int size) {}

    /**
     * Fenster zum Meer, für Lichtschächte.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     */
    public record Window(int x, int y, int w, int h) {}

    /** Farbstimmung einer Sektion. */
    private record Scheme(
            int[] wall,
            int[] rib,
            int[] pipe,
            int[] floor,
            int lamp,
            int accent,
            int grime,
            int ambient,
            double ambientLevel) {}

    private final int width;
    private final int[] pixels;
    private final List<Light> lights = new ArrayList<>();
    private final List<Prop> props = new ArrayList<>();
    private final List<Foreground> foreground = new ArrayList<>();
    private final List<Window> windows = new ArrayList<>();
    private final Scheme scheme;

    private RoomArt(int width, Scheme scheme) {
        this.width = width;
        this.pixels = new int[width * HEIGHT];
        this.scheme = scheme;
    }

    /**
     * @return Breite in Pixeln
     */
    public int width() {
        return width;
    }

    /**
     * @return Pixel des Streifens; transparente Pixel zeigen das Meer
     */
    public int[] pixels() {
        return pixels;
    }

    /**
     * @return statische Lichter
     */
    public List<Light> lights() {
        return lights;
    }

    /**
     * @return animierte Elemente
     */
    public List<Prop> props() {
        return props;
    }

    /**
     * @return Vordergrundsilhouetten
     */
    public List<Foreground> foreground() {
        return foreground;
    }

    /**
     * @return Fenster
     */
    public List<Window> windows() {
        return windows;
    }

    /**
     * @return Grundlichtfarbe
     */
    public int ambient() {
        return scheme.ambient;
    }

    /**
     * @return Grundlichtstärke
     */
    public double ambientLevel() {
        return scheme.ambientLevel;
    }

    /**
     * @return Akzentfarbe der Sektion
     */
    public int accent() {
        return scheme.accent;
    }

    /**
     * @return Lampenfarbe der Sektion
     */
    public int lamp() {
        return scheme.lamp;
    }

    /**
     * Malt einen Raum.
     *
     * @param plan Raumplan
     * @param seed Routen-Seed für Variation
     * @return fertiger Streifen samt Lichtern und Animationen
     */
    public static RoomArt build(RoomPlan plan, long seed) {
        int width = (int) Math.round(plan.layout().width() * PX);
        var art = new RoomArt(width, scheme(plan));
        var random = new Random(seed * 1_000_003L + plan.depth() * 97L + plan.branch() * 13L);
        art.paint(plan, random);
        return art;
    }

    private static Scheme scheme(RoomPlan plan) {
        int sector = plan.theme().sector() >= 0 ? plan.theme().sector() : plan.sector();
        if (plan.theme() == RoomTheme.SHRINE)
            return new Scheme(
                    ramp(0xFF2A1E2E),
                    ramp(0xFF1C1420),
                    ramp(0xFF5A3A2A),
                    ramp(0xFF3C3238),
                    0xFFFFB35C,
                    Pal.VIOLET_4,
                    0xFF140C14,
                    0xFF6A4A8A,
                    .26);
        if (plan.theme() == RoomTheme.MARKET)
            return new Scheme(
                    ramp(0xFF2B2A28),
                    ramp(0xFF1D1C1B),
                    ramp(0xFF6B4A2A),
                    ramp(0xFF3E3A34),
                    0xFFFF9E4A,
                    Pal.RUST_6,
                    0xFF100E0C,
                    0xFF7A5A40,
                    .3);
        return switch (sector) {
            case 0 ->
                    new Scheme(
                            ramp(0xFF3A2E27),
                            ramp(0xFF261C17),
                            ramp(0xFF6B4A33),
                            ramp(0xFF453A33),
                            0xFFFFC27A,
                            Pal.RUST_5,
                            0xFF1A100A,
                            0xFF7C5A48,
                            .33);
            case 1 ->
                    new Scheme(
                            ramp(0xFF28342F),
                            ramp(0xFF1A2320),
                            ramp(0xFF4F6B3A),
                            ramp(0xFF34403A),
                            0xFFB8FF9A,
                            Pal.GREEN_4,
                            0xFF0C1410,
                            0xFF4E7A62,
                            .31);
            case 2 ->
                    new Scheme(
                            ramp(0xFF3C4250),
                            ramp(0xFF272B38),
                            ramp(0xFF5A6A80),
                            ramp(0xFF3E4452),
                            0xFFD4B8FF,
                            Pal.VIOLET_4,
                            0xFF10121C,
                            0xFF6A5A9A,
                            .31);
            default ->
                    new Scheme(
                            ramp(0xFF26344A),
                            ramp(0xFF182233),
                            ramp(0xFF4A5E7A),
                            ramp(0xFF2E3A50),
                            0xFF9AD8FF,
                            Pal.TEAL_5,
                            0xFF0A0E18,
                            0xFF4A6A9A,
                            .31);
        };
    }

    private static int[] ramp(int base) {
        return new int[] {
            Pal.shade(base, .55),
            Pal.shade(base, .78),
            base,
            Pal.shade(base, 1.25),
            Pal.shade(base, 1.55)
        };
    }

    // ---------------------------------------------------------------------------------------------

    private void paint(RoomPlan plan, Random r) {
        var p = new Painter(width, HEIGHT);
        var theme = plan.theme();
        boolean bigWindows =
                theme == RoomTheme.OBSERVATORY
                        || theme == RoomTheme.AQUARIUM
                        || theme == RoomTheme.BRIDGE
                        || theme == RoomTheme.HATCHERY
                        || theme == RoomTheme.GARDEN;
        wall(p, r);
        int ribSpacing = 88 + r.nextInt(3) * 8;
        var ribs = new ArrayList<Integer>();
        for (int x = 40 + r.nextInt(30); x < width - 40; x += ribSpacing + r.nextInt(12) - 6)
            ribs.add(x);
        windowsBetween(p, r, ribs, bigWindows, theme);
        for (int x : ribs) rib(p, x, r);
        ceiling(p, r);
        wainscot(p, r);
        props(p, r, plan);
        signage(p, r, plan);
        floor(p, r);
        for (Platform platform : plan.layout().platforms()) catwalk(p, platform, r);
        leftDoor(p);
        rightFrame(p);
        grime(p, r, plan);
        System.arraycopy(p.color, 0, pixels, 0, pixels.length);
        foregroundLayer(r, plan);
    }

    private void wall(Painter p, Random r) {
        int[] w = scheme.wall;
        for (int y = 0; y < FLOOR; y++) {
            double t = y / (double) FLOOR;
            double shade = 1 - Math.abs(t - .45) * .9;
            for (int x = 0; x < width; x++) {
                int bayer = ((x & 1) + (y & 1) * 2);
                double v = shade * 3 + (bayer - 1.5) * .18;
                int c = v > 2.35 ? w[3] : v > 1.6 ? w[2] : v > 1.0 ? w[1] : w[0];
                p.px(x, y, c);
            }
        }
        int panelW = 34 + r.nextInt(3) * 6, panelH = 24 + r.nextInt(2) * 4;
        for (int py = 40; py < 150; py += panelH)
            for (int px = (py / panelH % 2) * panelW / 2 - panelW; px < width; px += panelW) {
                p.rect(px, py, panelW, 1, w[0]);
                p.rect(px, py, 1, panelH, w[0]);
                p.rect(px + 1, py + 1, panelW - 1, 1, w[3]);
                p.rect(px + 1, py + 1, 1, panelH - 1, w[3]);
                int variant = r.nextInt(12);
                if (variant == 0)
                    for (int s = 0; s < 4; s++)
                        p.rect(px + 6, py + 6 + s * 3, panelW - 12, 1, w[0]);
                else if (variant == 1) {
                    for (int s = 3; s < panelW - 2; s += 5) {
                        p.px(px + s, py + 3, w[4]);
                        p.px(px + s, py + panelH - 3, w[0]);
                    }
                } else if (variant == 2) p.rect(px + 3, py + 3, panelW - 5, panelH - 5, w[1]);
            }
    }

    private void windowsBetween(
            Painter p, Random r, List<Integer> ribs, boolean big, RoomTheme theme) {
        for (int i = 0; i + 1 < ribs.size(); i++) {
            int left = ribs.get(i) + 8, right = ribs.get(i + 1);
            int span = right - left;
            if (big && r.nextInt(4) != 0) {
                int wx = left + 6, wy = 46, ww = span - 12, wh = 88;
                p.rect(wx - 4, wy - 4, ww + 8, wh + 8, scheme.rib[1]);
                p.rect(wx - 3, wy - 3, ww + 6, 1, scheme.pipe[3]);
                p.rect(wx - 3, wy + wh + 2, ww + 6, 1, scheme.rib[0]);
                p.rect(wx, wy, ww, wh, 0);
                for (int m = wx + ww / 3; m < wx + ww - 4; m += ww / 3)
                    p.rect(m, wy, 2, wh, scheme.rib[1]);
                p.rect(wx, wy + wh / 2, ww, 2, scheme.rib[1]);
                for (int c = 0; c < 4; c++) {
                    p.px(wx + (c % 2 == 0 ? 0 : ww - 1), wy + (c < 2 ? 0 : wh - 1), scheme.rib[1]);
                }
                windows.add(new Window(wx, wy, ww, wh));
            } else if (r.nextInt(3) != 0 || big) {
                int cx = left + span / 2, cy = 88 + r.nextInt(12);
                int radius = 10 + r.nextInt(3);
                p.ellipse(cx, cy, radius + 3.5, radius + 3.5, scheme.pipe[1]);
                p.ellipse(cx - .5, cy - .5, radius + 2.5, radius + 2.5, scheme.pipe[3]);
                p.ellipse(cx + .5, cy + .5, radius + 2.5, radius + 2.5, scheme.pipe[0]);
                p.ellipse(cx, cy, radius + 2, radius + 2, scheme.pipe[2]);
                for (int k = 0; k < 8; k++) {
                    double a = k * Math.PI / 4;
                    p.px(
                            (int) Math.round(cx + Math.cos(a) * (radius + 2)),
                            (int) Math.round(cy + Math.sin(a) * (radius + 2)),
                            scheme.pipe[4]);
                }
                p.ellipse(cx, cy, radius, radius, 0);
                windows.add(new Window(cx - radius, cy - radius, radius * 2, radius * 2));
            } else if (r.nextBoolean()) {
                // Schaltkasten mit blinkenden Kontrollleuchten
                int bx = left + span / 2 - 10, by = 80;
                p.box(bx, by, 20, 26, new int[] {scheme.rib[0], scheme.pipe[1], scheme.pipe[3]});
                p.rect(bx + 3, by + 4, 14, 8, Pal.INK);
                props.add(new Prop(PropKind.SCREEN, bx + 3, by + 4, 14, 8, scheme.accent));
                props.add(new Prop(PropKind.BLINK, bx + 4, by + 16, 12, 6, Pal.RED_4));
            } else {
                // Druckanzeige mit Zeiger
                int cx = left + span / 2, cy = 96;
                p.ellipse(cx, cy, 7, 7, scheme.pipe[1]);
                p.ellipse(cx, cy, 5.5, 5.5, Pal.PAPER);
                p.line(cx, cy, cx + 3, cy - 3, Pal.RED_2);
                p.px(cx, cy, Pal.INK);
            }
        }
    }

    private void rib(Painter p, int x, Random r) {
        int[] c = scheme.rib;
        p.rect(x, 26, 9, FLOOR - 26, c[2]);
        p.rect(x, 26, 1, FLOOR - 26, c[4]);
        p.rect(x + 1, 26, 1, FLOOR - 26, c[3]);
        p.rect(x + 7, 26, 2, FLOOR - 26, c[0]);
        for (int y = 36; y < FLOOR - 4; y += 9) {
            p.px(x + 3, y, c[4]);
            p.px(x + 5, y + 4, c[0]);
        }
        // Knotenbleche zur Decke
        for (int k = 0; k < 10; k++) {
            p.rect(x - 10 + k, 30 + k, 10 - k, 1, c[1]);
            p.rect(x + 9, 30 + k, 10 - k, 1, c[1]);
        }
        p.line(x - 10, 30, x, 40, c[3]);
        if (r.nextInt(3) == 0) {
            // Kabelbündel an der Rippe
            p.rect(x + 3, 44, 2, 100, Pal.INK);
            p.rect(x + 4, 44, 1, 100, scheme.pipe[0]);
        }
    }

    private void ceiling(Painter p, Random r) {
        int[] c = scheme.rib;
        p.rect(0, 0, width, 26, c[0]);
        for (int x = 0; x < width; x += 22) p.rect(x, 0, 1, 26, Pal.OUTLINE);
        p.rect(0, 25, width, 1, c[3]);
        pipe(p, 0, width, 9, 5, scheme.pipe, r, true);
        pipe(
                p,
                0,
                width,
                19,
                3,
                ramp(scheme.pipe[2] == 0 ? Pal.STEEL_4 : Pal.mix(scheme.pipe[2], Pal.STEEL_4, .6)),
                r,
                false);
        // Durchhängende Kabel
        for (int x = r.nextInt(40); x < width - 40; x += 60 + r.nextInt(50)) {
            int span = 30 + r.nextInt(40), sag = 5 + r.nextInt(8);
            for (int i = 0; i <= span; i++) {
                double t = i / (double) span;
                int y = 26 + (int) Math.round(4 * sag * t * (1 - t));
                p.px(x + i, y, Pal.INK);
            }
        }
        // Deckenlampen
        for (int x = 70 + r.nextInt(40); x < width - 40; x += 110 + r.nextInt(40)) {
            p.rect(x - 1, 26, 2, 6, Pal.STEEL_1);
            p.box(x - 6, 31, 12, 4, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
            p.lightRect(x - 4, 35, 8, 2, Pal.mix(scheme.lamp, Pal.WHITE, .3));
            p.px(x - 5, 35, Pal.STEEL_2);
            p.px(x + 4, 35, Pal.STEEL_2);
            int flicker = r.nextInt(11) == 0 ? 3 : r.nextInt(4) == 0 ? 1 : 0;
            lights.add(new Light(x, 38, 95, scheme.lamp, .95, flicker));
        }
    }

    private void pipe(
            Painter p, int x0, int x1, int y, int radius, int[] ramp, Random r, boolean valves) {
        for (int yy = -radius; yy <= radius; yy++) {
            double t = (yy + radius) / (2.0 * radius);
            int c = t < .2 ? ramp[3] : t < .45 ? ramp[2] : t < .8 ? ramp[1] : ramp[0];
            p.rect(x0, y + yy, x1 - x0, 1, c);
        }
        for (int x = x0 + r.nextInt(30); x < x1; x += 48 + r.nextInt(30)) {
            p.rect(x, y - radius - 1, 3, radius * 2 + 3, ramp[0]);
            p.rect(x, y - radius - 1, 1, radius * 2 + 3, ramp[3]);
            if (valves && r.nextInt(4) == 0) {
                p.rect(x + 8, y + radius, 2, 4, Pal.STEEL_2);
                p.ellipse(x + 9, y + radius + 5, 3.5, 1.5, Pal.RED_2);
                p.px(x + 9, y + radius + 5, Pal.RED_4);
            }
        }
    }

    private void wainscot(Painter p, Random r) {
        int[] c = scheme.wall;
        p.rect(0, 150, width, FLOOR - 150, c[1]);
        p.rect(0, 150, width, 1, c[4]);
        p.rect(0, 151, width, 1, c[3]);
        for (int x = 0; x < width; x += 26) {
            p.rect(x, 152, 1, FLOOR - 152, c[0]);
            p.px(x + 3, 155, c[4]);
            p.px(x + 3, FLOOR - 4, c[0]);
        }
        pipe(p, 0, width, 160, 2, scheme.pipe, r, false);
        for (int y = FLOOR - 6; y < FLOOR; y++)
            for (int x = (y & 1); x < width; x += 2) p.px(x, y, c[0]);
    }

    private void floor(Painter p, Random r) {
        int[] f = scheme.floor;
        p.rect(0, FLOOR, width, 2, f[4]);
        p.rect(0, FLOOR + 2, width, 1, f[3]);
        p.rect(0, FLOOR + 3, width, 8, f[2]);
        for (int x = 0; x < width; x += 4) {
            p.rect(x + 1, FLOOR + 4, 2, 5, f[0]);
        }
        p.rect(0, FLOOR + 10, width, 1, f[1]);
        p.rect(0, FLOOR + 11, width, HEIGHT - FLOOR - 11, Pal.INK);
        for (int x = r.nextInt(30); x < width; x += 40 + r.nextInt(20)) {
            p.rect(x, FLOOR + 11, 6, HEIGHT - FLOOR - 11, f[0]);
            p.rect(x, FLOOR + 11, 1, HEIGHT - FLOOR - 11, f[1]);
            for (int k = 0; k < 6; k++) p.px(x + k, FLOOR + 12 + k, f[1]);
        }
        pipe(p, 0, width, FLOOR + 30, 4, ramp(Pal.shade(scheme.pipe[2], .6)), r, false);
        pipe(p, 0, width, FLOOR + 48, 2, ramp(Pal.shade(scheme.pipe[2], .5)), r, false);
        // Bilge: trübes Wasser im Unterdeck, nach unten dunkler
        int bilge = FLOOR + 60;
        for (int y = bilge; y < HEIGHT; y++) {
            double t = (y - bilge) / (double) (HEIGHT - bilge);
            int c = Pal.mix(Pal.mix(scheme.accent, Pal.INK, .78), Pal.INK, Math.min(1, t * 1.4));
            p.rect(0, y, width, 1, c);
        }
        for (int x = r.nextInt(20); x < width; x += 14 + r.nextInt(18))
            p.rect(
                    x,
                    bilge + 3 + r.nextInt(8),
                    4 + r.nextInt(8),
                    1,
                    Pal.mix(scheme.accent, Pal.INK, .6));
        // Unterflurlampen machen Rohre und Streben sichtbar
        for (int x = 60 + r.nextInt(60); x < width - 20; x += 150 + r.nextInt(70)) {
            p.rect(x - 3, FLOOR + 12, 7, 2, Pal.STEEL_2);
            p.rect(x - 2, FLOOR + 14, 5, 3, Pal.STEEL_1);
            p.light(x, FLOOR + 15, scheme.accent);
            p.light(x - 1, FLOOR + 15, Pal.mix(scheme.accent, Pal.INK, .3));
            lights.add(new Light(x, FLOOR + 18, 48, scheme.accent, .42, 2));
        }
        for (int x = r.nextInt(80); x < width; x += 90 + r.nextInt(80))
            props.add(new Prop(PropKind.WATER, x, bilge, 40 + r.nextInt(40), 6, scheme.accent));
    }

    private void catwalk(Painter p, Platform platform, Random r) {
        int x0 = (int) Math.round(platform.x() * PX), x1 = (int) Math.round(platform.right() * PX);
        int y = (int) Math.round(platform.y() * PX);
        int[] f = scheme.floor;
        // Geländer liegt hinter der Spielebene
        p.rect(x0, y - 10, x1 - x0, 1, scheme.pipe[3]);
        p.rect(x0, y - 9, x1 - x0, 1, scheme.pipe[0]);
        p.rect(x0, y - 5, x1 - x0, 1, scheme.pipe[1]);
        for (int x = x0 + 2; x < x1; x += 12) p.rect(x, y - 10, 2, 10, scheme.pipe[1]);
        p.rect(x0, y, x1 - x0, 1, f[4]);
        p.rect(x0, y + 1, x1 - x0, 3, f[2]);
        for (int x = x0 + 1; x < x1 - 1; x += 3) p.px(x, y + 2, f[0]);
        p.rect(x0, y + 4, x1 - x0, 1, f[0]);
        // Stützen zum Boden oder Ketten zur Decke
        if (y < FLOOR - 70) {
            for (int x : new int[] {x0 + 4, x1 - 6}) {
                for (int yy = 26; yy < y; yy += 3) {
                    p.px(x, yy, Pal.STEEL_3);
                    p.px(x + 1, yy + 1, Pal.STEEL_2);
                }
            }
        } else {
            for (int x : new int[] {x0 + 6, x1 - 9}) {
                p.rect(x, y + 5, 3, FLOOR - y - 5, scheme.rib[1]);
                p.rect(x, y + 5, 1, FLOOR - y - 5, scheme.rib[3]);
            }
            for (int k = 0; k < Math.min(20, FLOOR - y - 6); k++)
                p.px(x0 + 9 + k, y + 5 + k, scheme.rib[0]);
        }
    }

    private void leftDoor(Painter p) {
        int[] c = scheme.rib;
        p.rect(0, 40, 22, FLOOR - 40, c[1]);
        p.rect(20, 40, 2, FLOOR - 40, c[0]);
        p.box(2, 118, 16, FLOOR - 118, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.ellipse(10, 146, 4, 4, Pal.RUST_3);
        p.ellipse(10, 146, 2.5, 2.5, Pal.STEEL_1);
        for (int y = 112; y < 118; y++)
            for (int x = 0; x < 22; x++) p.px(x, y, ((x + y) / 3) % 2 == 0 ? Pal.RUST_5 : Pal.INK);
    }

    private void rightFrame(Painter p) {
        int[] c = scheme.rib;
        int x = width - 30;
        p.rect(x, 40, 30, 72, c[1]);
        p.rect(x, 40, 2, FLOOR - 40, c[3]);
        p.rect(x + 28, 40, 2, FLOOR - 40, c[0]);
        for (int y = 112; y < 118; y++)
            for (int xx = x; xx < width; xx++)
                p.px(xx, y, ((xx + y) / 3) % 2 == 0 ? Pal.RUST_5 : Pal.INK);
        p.rect(x + 4, 118, 24, FLOOR - 118, Pal.INK);
        lights.add(new Light(width - 16, 104, 40, Pal.RED_4, .6, 2));
    }

    private void signage(Painter p, Random r, RoomPlan plan) {
        var font = ArtFont.get();
        String code = String.format("%s-%02d", sectorCode(plan.sector()), plan.depth() + 1);
        int x = 40 + r.nextInt(40);
        font.stencil(p, code, x, 52, Pal.mix(scheme.wall[1], Pal.BONE, .35));
        if (r.nextBoolean()) {
            for (int i = 0; i < 18; i++)
                for (int y = 0; y < 5; y++)
                    p.px(
                            x + i,
                            62 + y,
                            ((i + y) / 3) % 2 == 0
                                    ? Pal.mix(scheme.accent, scheme.wall[1], .5)
                                    : scheme.wall[0]);
        }
        String label =
                switch (plan.theme()) {
                    case TORPEDO -> "TORPEDO";
                    case REACTOR -> "GEFAHR";
                    case MEDBAY -> "SANITÄT";
                    case BRIDGE -> "BRÜCKE";
                    case WORKSHOP -> "WERKSTATT";
                    case LAB -> "LABOR";
                    case SECURITY -> "ZUTRITT";
                    case BALLAST -> "BALLAST";
                    case GALLEY -> "KOMBÜSE";
                    case BOILER -> "KESSEL";
                    case GENERATOR -> "HOCHSPANNUNG";
                    case ARCHIVE -> "ARCHIV";
                    case CRYO -> "KRYO";
                    case ARMORY -> "WAFFEN";
                    default -> null;
                };
        if (label != null)
            font.stencil(
                    p,
                    label,
                    width / 2 - label.length() * 3 + r.nextInt(60) - 30,
                    42,
                    Pal.mix(scheme.accent, scheme.wall[1], .45));
    }

    private static String sectorCode(int sector) {
        return switch (sector) {
            case 0 -> "HK";
            case 1 -> "MD";
            case 2 -> "FD";
            default -> "KD";
        };
    }

    private void grime(Painter p, Random r, RoomPlan plan) {
        int count = width * 3;
        for (int i = 0; i < count; i++) {
            int x = r.nextInt(width), y = 26 + r.nextInt(FLOOR - 26);
            int c = p.get(x, y);
            if (c >>> 24 == 0) continue;
            p.px(x, y, Pal.shade(c, .82));
        }
        if (plan.sector() == 0 || plan.theme() == RoomTheme.BALLAST) {
            for (int i = 0; i < width / 10; i++) {
                int x = r.nextInt(width), y = 40 + r.nextInt(100), len = 6 + r.nextInt(24);
                for (int k = 0; k < len; k++) {
                    int c = p.get(x, y + k);
                    if (c >>> 24 != 0)
                        p.px(x, y + k, Pal.mix(c, Pal.RUST_2, .45 * (1 - k / (double) len)));
                }
            }
        }
        if (plan.sector() == 2) {
            for (int i = 0; i < width / 14; i++) {
                int x = r.nextInt(width), y = 140 + r.nextInt(46);
                for (int k = 0; k < 5; k++)
                    p.px(
                            x + r.nextInt(5),
                            y - r.nextInt(8),
                            r.nextBoolean() ? Pal.GREEN_2 : Pal.GREEN_1);
            }
        }
    }

    private void foregroundLayer(Random r, RoomPlan plan) {
        int w = (int) (width * 1.3) + 200;
        for (int x = r.nextInt(120); x < w; x += 160 + r.nextInt(180)) {
            int kind = r.nextInt(9);
            if (kind > 4) kind = kind == 5 ? 0 : kind == 6 ? 4 : kind == 7 ? 2 : 0;
            foreground.add(new Foreground(kind, x, 10 + r.nextInt(20)));
        }
        if (plan.theme() == RoomTheme.ENGINE || plan.theme() == RoomTheme.PUMPS)
            for (int x = 60; x < w; x += 220) foreground.add(new Foreground(3, x, 30));
    }

    // --- Requisiten ------------------------------------------------------------------------------

    private void props(Painter p, Random r, RoomPlan plan) {
        int reward = (int) Math.round(plan.layout().rewardX() * PX);
        int x = 36 + r.nextInt(20);
        while (x < width - 70) {
            if (Math.abs(x - reward) < 34) {
                x += 40;
                continue;
            }
            int used = prop(p, r, plan.theme(), x);
            x += used + 14 + r.nextInt(40);
        }
        switch (plan.theme()) {
            case REACTOR -> reactorCore(p, width / 2 - 20);
            case HATCHERY -> brokenTank(p, width / 2 - 36);
            case BULKHEAD -> {
                beacon(p, 60, 42);
                beacon(p, width - 70, 42);
            }
            case BRIDGE -> helm(p, width / 2 + 60);
            default -> {}
        }
    }

    private int prop(Painter p, Random r, RoomTheme theme, int x) {
        int choice = r.nextInt(4);
        return switch (theme) {
            case QUARTERS, BRIG ->
                    choice < 2 ? bunk(p, x) : choice == 2 ? locker(p, x, r) : table(p, x);
            case CARGO, STORAGE ->
                    choice < 2 ? crates(p, x, r) : choice == 2 ? barrels(p, x, r) : shelf(p, x, r);
            case MESS -> choice < 3 ? table(p, x) : shelf(p, x, r);
            case TORPEDO, BULKHEAD -> choice < 2 ? torpedoRack(p, x) : crates(p, x, r);
            case ENGINE, PUMPS ->
                    choice < 2 ? turbine(p, x, r) : choice == 2 ? pump(p, x) : barrels(p, x, r);
            case COOLING, BALLAST -> choice < 2 ? tank(p, x, Pal.TEAL_4, false) : pump(p, x);
            case REACTOR -> choice < 2 ? pump(p, x) : console(p, x, Pal.GREEN_4);
            case LAB ->
                    choice < 2
                            ? labBench(p, x, r)
                            : choice == 2
                                    ? console(p, x, Pal.VIOLET_4)
                                    : tank(p, x, Pal.VIOLET_4, true);
            case GARDEN -> choice < 3 ? planter(p, x, r) : tank(p, x, Pal.GREEN_4, true);
            case MEDBAY ->
                    choice < 2
                            ? medBed(p, x)
                            : choice == 2 ? locker(p, x, r) : console(p, x, Pal.TEAL_5);
            case AQUARIUM, HATCHERY ->
                    choice < 3 ? tank(p, x, Pal.TEAL_5, true) : labBench(p, x, r);
            case SECURITY -> choice < 2 ? locker(p, x, r) : console(p, x, Pal.RED_4);
            case OBSERVATORY, SIGNAL -> choice < 3 ? console(p, x, Pal.TEAL_5) : crates(p, x, r);
            case COMMAND_HALL, BRIDGE -> choice < 2 ? console(p, x, Pal.TEAL_5) : mapTable(p, x);
            case WORKSHOP ->
                    choice < 2 ? workbench(p, x) : choice == 2 ? crates(p, x, r) : shelf(p, x, r);
            case MARKET -> choice < 2 ? crates(p, x, r) : lantern(p, x);
            case SHRINE -> choice < 2 ? candles(p, x, r) : pew(p, x);
            case GALLEY -> choice < 2 ? stove(p, x) : choice == 2 ? table(p, x) : shelf(p, x, r);
            case LAUNDRY -> choice < 3 ? washer(p, x) : barrels(p, x, r);
            case BOILER -> choice < 2 ? boiler(p, x) : pump(p, x);
            case GENERATOR -> choice < 2 ? generator(p, x) : console(p, x, Pal.RUST_6);
            case ARCHIVE -> choice < 3 ? serverRack(p, x, r) : console(p, x, Pal.TEAL_5);
            case CRYO -> choice < 3 ? cryoPod(p, x, r) : labBench(p, x, r);
            case ARMORY -> choice < 2 ? weaponRack(p, x, r) : locker(p, x, r);
            case MAPROOM -> choice < 2 ? mapTable(p, x) : console(p, x, Pal.GREEN_4);
        };
    }

    // --- Requisiten der neuen Abteilungen --------------------------------------------------------

    private int stove(Painter p, int x) {
        p.box(x, FLOOR - 18, 36, 18, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_5});
        p.rect(x + 2, FLOOR - 12, 32, 8, Pal.STEEL_2);
        p.rect(x + 4, FLOOR - 10, 12, 5, Pal.INK);
        props.add(new Prop(PropKind.BLINK, x + 5, FLOOR - 8, 9, 1, Pal.RUST_5));
        for (int k = 0; k < 2; k++) {
            int px = x + 5 + k * 16;
            p.rect(px, FLOOR - 26, 12, 8, k == 0 ? Pal.STEEL_4 : Pal.RUST_3);
            p.rect(px - 1, FLOOR - 27, 14, 2, k == 0 ? Pal.STEEL_6 : Pal.RUST_4);
            props.add(new Prop(PropKind.STEAM, px + 4, FLOOR - 44, 4, 16, Pal.STEEL_6));
        }
        p.rect(x - 2, FLOOR - 62, 40, 6, Pal.STEEL_3);
        p.rect(x + 14, FLOOR - 80, 8, 18, Pal.STEEL_2);
        lights.add(new Light(x + 18, FLOOR - 14, 26, Pal.RUST_5, .4, 1));
        return 38;
    }

    private int washer(Painter p, int x) {
        int[] shell = {0xFF6E7278, 0xFF9CA2A8, 0xFFC4C8CC};
        p.box(x, FLOOR - 28, 26, 28, shell);
        p.rect(x + 2, FLOOR - 26, 22, 3, 0xFF5A5E64);
        p.light(x + 20, FLOOR - 25, Pal.GREEN_4);
        p.ellipse(x + 13, FLOOR - 12, 9, 9, Pal.STEEL_2);
        p.ellipse(x + 13, FLOOR - 12, 7, 7, 0xFF16303A);
        props.add(new Prop(PropKind.FAN, x + 7, FLOOR - 18, 12, 12, 0xFF6FB8D0));
        return 28;
    }

    private int boiler(Painter p, int x) {
        int w = 34, h = 58;
        p.rect(x, FLOOR - h, w, h, Pal.RUST_2);
        for (int y = FLOOR - h; y < FLOOR; y++) {
            p.px(x + 2, y, Pal.RUST_4);
            p.px(x + 3, y, Pal.RUST_3);
            p.px(x + w - 3, y, Pal.RUST_1);
        }
        for (int y = FLOOR - h + 6; y < FLOOR; y += 12)
            for (int k = 3; k < w - 2; k += 5) p.px(x + k, y, Pal.RUST_5);
        p.ellipse(x + w / 2.0, FLOOR - h + 16, 6, 6, Pal.PAPER);
        p.line(x + w / 2, FLOOR - h + 16, x + w / 2 + 3, FLOOR - h + 13, Pal.RED_2);
        p.rect(x + 9, FLOOR - 14, 16, 10, Pal.INK);
        p.light(x + 12, FLOOR - 8, Pal.RUST_6);
        p.light(x + 17, FLOOR - 7, Pal.RUST_5);
        p.light(x + 21, FLOOR - 8, Pal.RUST_6);
        p.rect(x + w / 2 - 3, 26, 6, FLOOR - h - 26, scheme.pipe[1]);
        props.add(new Prop(PropKind.STEAM, x + w - 6, FLOOR - h - 16, 4, 30, Pal.STEEL_6));
        lights.add(new Light(x + w / 2, FLOOR - 10, 36, Pal.RUST_5, .6, 2));
        return w + 2;
    }

    private int generator(Painter p, int x) {
        p.box(x, FLOOR - 14, 42, 14, new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4});
        for (int k = 0; k < 2; k++) {
            int cx = x + 5 + k * 18;
            p.rect(cx, FLOOR - 46, 14, 32, Pal.RUST_2);
            for (int y = FLOOR - 44; y < FLOOR - 16; y += 2)
                p.rect(cx + 1, y, 12, 1, y % 4 == 0 ? Pal.RUST_4 : Pal.RUST_5);
            p.rect(cx + 5, FLOOR - 52, 4, 6, Pal.STEEL_5);
        }
        props.add(new Prop(PropKind.BLINK, x + 3, FLOOR - 8, 36, 1, Pal.TEAL_5));
        lights.add(new Light(x + 21, FLOOR - 50, 40, Pal.TEAL_5, .5, 3));
        return 44;
    }

    private int serverRack(Painter p, int x, Random r) {
        int n = 1 + r.nextInt(3);
        for (int i = 0; i < n; i++) {
            int rx = x + i * 21;
            p.box(rx, FLOOR - 62, 20, 62, new int[] {0xFF0C1016, 0xFF1A2230, 0xFF2E3A4C});
            for (int y = FLOOR - 58; y < FLOOR - 4; y += 6) {
                p.rect(rx + 2, y, 16, 4, 0xFF10161E);
                props.add(
                        new Prop(
                                PropKind.BLINK,
                                rx + 3,
                                y + 1,
                                12,
                                1,
                                r.nextInt(4) == 0 ? Pal.RUST_6 : Pal.TEAL_5));
            }
        }
        lights.add(new Light(x + n * 10, FLOOR - 30, 34, Pal.TEAL_4, .35, 1));
        return n * 21;
    }

    private int cryoPod(Painter p, int x, Random r) {
        int w = 22, h = 52;
        p.box(x - 1, FLOOR - 6, w + 2, 6, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_5});
        p.rect(x, FLOOR - h, w, h - 6, 0xFF1E3A48);
        for (int y = FLOOR - h; y < FLOOR - 6; y++) {
            p.px(x + 1, y, 0xFFB4E8F4);
            p.px(x + w - 2, y, 0xFF2A5A6A);
        }
        p.ellipse(x + w / 2.0, FLOOR - h / 2.0 - 2, 5, 12, 0xFF2E4A58);
        p.light(x + w / 2 + 1, FLOOR - h / 2 - 10, Pal.TEAL_6);
        for (int k = 0; k < 8; k++) p.px(x + r.nextInt(w), FLOOR - 7 - r.nextInt(8), 0xFFDCF4FA);
        p.rect(x - 1, FLOOR - h - 4, w + 2, 4, Pal.STEEL_4);
        props.add(new Prop(PropKind.TANK, x, FLOOR - h, w, h - 8, 0xFFB4E8F4));
        lights.add(new Light(x + w / 2, FLOOR - h / 2, 40, Pal.TEAL_6, .5, 2));
        return w + 2;
    }

    private int weaponRack(Painter p, int x, Random r) {
        p.box(x, FLOOR - 44, 42, 44, new int[] {Pal.STEEL_0, Pal.STEEL_1, Pal.STEEL_3});
        for (int k = 0; k < 5; k++) {
            int wx = x + 5 + k * 7;
            int len = 26 + r.nextInt(8);
            p.rect(wx, FLOOR - 40, 2, len, Pal.STEEL_5);
            p.rect(wx - 1, FLOOR - 40, 4, 4, k % 2 == 0 ? Pal.RUST_4 : Pal.STEEL_6);
        }
        p.rect(x + 2, FLOOR - 8, 38, 2, Pal.STEEL_4);
        props.add(new Prop(PropKind.BLINK, x + 36, FLOOR - 42, 3, 1, Pal.RED_4));
        return 44;
    }

    private int crates(Painter p, int x, Random r) {
        int n = 1 + r.nextInt(3);
        int cx = x;
        for (int i = 0; i < n; i++) {
            int s = 14 + r.nextInt(6);
            int stack = 1 + r.nextInt(2);
            for (int k = 0; k < stack; k++) {
                int y = FLOOR - s * (k + 1);
                int[] c =
                        r.nextBoolean()
                                ? new int[] {Pal.RUST_1, Pal.RUST_2, Pal.RUST_3}
                                : new int[] {0xFF2E3A2A, 0xFF46583E, 0xFF5E7452};
                p.box(cx, y, s, s, c);
                p.line(cx + 1, y + 1, cx + s - 2, y + s - 2, c[0]);
                p.rect(cx + 2, y + s / 2, s - 4, 1, c[0]);
            }
            cx += s + 1;
        }
        return cx - x;
    }

    private int barrels(Painter p, int x, Random r) {
        int n = 1 + r.nextInt(3);
        for (int i = 0; i < n; i++) {
            int bx = x + i * 11;
            int color = r.nextInt(3) == 0 ? 0xFF8A2A22 : r.nextBoolean() ? 0xFF3A5A7A : 0xFF6A6A2A;
            int[] c = Pal.ramp(color);
            p.rect(bx, FLOOR - 18, 10, 18, c[1]);
            p.rect(bx + 1, FLOOR - 18, 2, 18, c[2]);
            p.rect(bx + 8, FLOOR - 18, 2, 18, c[0]);
            p.rect(bx, FLOOR - 13, 10, 1, c[0]);
            p.rect(bx, FLOOR - 5, 10, 1, c[0]);
            p.rect(bx + 1, FLOOR - 19, 8, 1, c[3]);
        }
        return n * 11;
    }

    private int shelf(Painter p, int x, Random r) {
        int w = 28, h = 46;
        p.rect(x, FLOOR - h, 2, h, Pal.STEEL_2);
        p.rect(x + w - 2, FLOOR - h, 2, h, Pal.STEEL_2);
        for (int y = FLOOR - h + 4; y < FLOOR; y += 14) {
            p.rect(x, y, w, 2, Pal.STEEL_3);
            for (int i = x + 3; i < x + w - 4; i += 5 + r.nextInt(3)) {
                int bh = 5 + r.nextInt(6);
                int color =
                        new int[] {Pal.RUST_3, Pal.TEAL_2, Pal.STEEL_4, Pal.RED_2, Pal.GREEN_2}
                                [r.nextInt(5)];
                p.rect(i, y - bh, 4, bh, color);
                p.px(i, y - bh, Pal.shade(color, 1.4));
            }
        }
        return w;
    }

    private int locker(Painter p, int x, Random r) {
        int n = 2 + r.nextInt(2);
        for (int i = 0; i < n; i++) {
            int lx = x + i * 11;
            p.box(lx, FLOOR - 40, 10, 40, new int[] {Pal.STEEL_2, Pal.STEEL_3, Pal.STEEL_4});
            for (int k = 0; k < 3; k++) p.rect(lx + 2, FLOOR - 36 + k * 2, 6, 1, Pal.STEEL_1);
            p.px(lx + 7, FLOOR - 22, Pal.STEEL_6);
        }
        return n * 11;
    }

    private int bunk(Painter p, int x) {
        int w = 44;
        p.rect(x, FLOOR - 44, 2, 44, Pal.STEEL_2);
        p.rect(x + w - 2, FLOOR - 44, 2, 44, Pal.STEEL_2);
        for (int y : new int[] {FLOOR - 40, FLOOR - 16}) {
            p.rect(x, y, w, 3, Pal.STEEL_3);
            p.rect(x + 2, y - 4, w - 4, 4, 0xFF5A5A6A);
            p.rect(x + 2, y - 4, w - 4, 1, 0xFF7A7A8A);
            p.rect(x + 3, y - 6, 9, 3, Pal.PAPER);
        }
        return w;
    }

    private int table(Painter p, int x) {
        int w = 40;
        p.rect(x, FLOOR - 16, w, 3, Pal.RUST_2);
        p.rect(x, FLOOR - 16, w, 1, Pal.RUST_3);
        p.rect(x + 3, FLOOR - 13, 2, 13, Pal.STEEL_2);
        p.rect(x + w - 5, FLOOR - 13, 2, 13, Pal.STEEL_2);
        p.rect(x + 8, FLOOR - 19, 4, 3, Pal.STEEL_5);
        p.rect(x + 24, FLOOR - 18, 3, 2, Pal.PAPER);
        p.rect(x - 4, FLOOR - 8, 8, 2, Pal.RUST_1);
        p.rect(x + w - 4, FLOOR - 8, 8, 2, Pal.RUST_1);
        return w + 4;
    }

    private int torpedoRack(Painter p, int x) {
        int w = 70;
        for (int k = 0; k < 3; k++) {
            int y = FLOOR - 12 - k * 13;
            p.rect(x, y + 8, w, 2, Pal.STEEL_2);
            p.rect(x + 4, y, w - 14, 8, Pal.STEEL_4);
            p.rect(x + 4, y, w - 14, 2, Pal.STEEL_5);
            p.rect(x + 4, y + 6, w - 14, 2, Pal.STEEL_3);
            p.ellipse(x + w - 10, y + 4, 4, 4, Pal.RED_2);
            p.px(x + w - 9, y + 2, Pal.RED_4);
            for (int f = 0; f < 3; f++) p.rect(x + 1, y + f * 3, 4, 2, Pal.STEEL_3);
        }
        return w;
    }

    private int turbine(Painter p, int x, Random r) {
        int radius = 18 + r.nextInt(6);
        int cx = x + radius + 2, cy = FLOOR - radius - 6;
        p.rect(cx - radius, FLOOR - 8, radius * 2, 8, Pal.STEEL_2);
        p.ellipse(cx, cy, radius + 3, radius + 3, scheme.pipe[1]);
        p.ellipse(cx, cy, radius + 1, radius + 1, scheme.pipe[0]);
        p.ellipse(cx, cy, radius, radius, Pal.INK);
        props.add(
                new Prop(
                        PropKind.FAN,
                        cx - radius,
                        cy - radius,
                        radius * 2,
                        radius * 2,
                        scheme.pipe[3]));
        return radius * 2 + 4;
    }

    private int pump(Painter p, int x) {
        p.box(x, FLOOR - 26, 22, 26, new int[] {scheme.pipe[0], scheme.pipe[1], scheme.pipe[3]});
        p.rect(x + 4, FLOOR - 40, 5, 14, scheme.pipe[1]);
        p.rect(x + 4, FLOOR - 40, 1, 14, scheme.pipe[3]);
        p.ellipse(x + 15, FLOOR - 16, 4, 4, Pal.PAPER);
        p.line(x + 15, FLOOR - 16, x + 17, FLOOR - 19, Pal.RED_2);
        props.add(new Prop(PropKind.BLINK, x + 3, FLOOR - 8, 6, 2, Pal.GREEN_4));
        props.add(new Prop(PropKind.STEAM, x + 6, FLOOR - 44, 4, 30, Pal.STEEL_6));
        return 24;
    }

    private int tank(Painter p, int x, int liquid, boolean creature) {
        int w = 26, h = 58;
        p.box(x - 2, FLOOR - 8, w + 4, 8, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.box(x - 2, FLOOR - h - 6, w + 4, 6, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.rect(x, FLOOR - h, w, h - 8, Pal.mix(liquid, Pal.INK, .72));
        for (int y = FLOOR - h; y < FLOOR - 8; y++) {
            p.px(x + 2, y, Pal.mix(liquid, Pal.WHITE, .2));
            p.px(x + w - 3, y, Pal.mix(liquid, Pal.INK, .5));
        }
        if (creature) {
            p.ellipse(x + w / 2.0, FLOOR - h / 2.0 - 4, 6, 8, Pal.mix(liquid, Pal.INK, .45));
            p.light(x + w / 2 + 2, FLOOR - h / 2 - 7, Pal.mix(liquid, Pal.WHITE, .5));
        }
        props.add(new Prop(PropKind.TANK, x, FLOOR - h, w, h - 8, liquid));
        lights.add(new Light(x + w / 2, FLOOR - h / 2, 50, liquid, .55, 2));
        return w + 4;
    }

    private int console(Painter p, int x, int screen) {
        p.box(x, FLOOR - 22, 30, 22, new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4});
        p.poly(
                new double[] {x, x + 30, x + 26, x + 4},
                new double[] {FLOOR - 22, FLOOR - 22, FLOOR - 26, FLOOR - 26},
                Pal.STEEL_3);
        p.rect(x + 3, FLOOR - 44, 24, 16, Pal.STEEL_2);
        p.rect(x + 5, FLOOR - 42, 20, 12, Pal.INK);
        props.add(new Prop(PropKind.SCREEN, x + 5, FLOOR - 42, 20, 12, screen));
        props.add(new Prop(PropKind.BLINK, x + 4, FLOOR - 20, 22, 3, screen));
        lights.add(new Light(x + 15, FLOOR - 36, 34, screen, .5, 1));
        return 30;
    }

    private int labBench(Painter p, int x, Random r) {
        p.rect(x, FLOOR - 18, 42, 3, Pal.PAPER);
        p.rect(x, FLOOR - 15, 42, 15, Pal.STEEL_3);
        p.rect(x + 20, FLOOR - 15, 1, 15, Pal.STEEL_2);
        for (int i = 0; i < 4; i++) {
            int vx = x + 3 + i * 9;
            int color = new int[] {Pal.GREEN_4, Pal.VIOLET_4, Pal.TEAL_5, Pal.RUST_6}[r.nextInt(4)];
            p.rect(vx, FLOOR - 24, 3, 6, Pal.STEEL_6);
            p.light(vx + 1, FLOOR - 21, color);
            p.light(vx + 1, FLOOR - 20, color);
        }
        return 42;
    }

    private int planter(Painter p, int x, Random r) {
        int w = 40;
        p.box(x, FLOOR - 10, w, 10, new int[] {Pal.RUST_1, Pal.RUST_2, Pal.RUST_3});
        p.rect(x + 1, FLOOR - 11, w - 2, 2, 0xFF2A1A10);
        for (int i = 0; i < 9; i++) {
            int sx = x + 3 + i * 4, h = 10 + r.nextInt(16);
            for (int k = 0; k < h; k++) {
                int px = sx + (int) Math.round(Math.sin(k * .5 + i) * 1.2);
                p.px(px, FLOOR - 11 - k, k % 3 == 0 ? Pal.GREEN_4 : Pal.GREEN_3);
                if (k % 4 == 2) p.px(px + 1, FLOOR - 11 - k, Pal.GREEN_2);
            }
            if (r.nextInt(3) == 0) p.light(sx, FLOOR - 11 - h, Pal.VIOLET_4);
        }
        p.rect(x + 2, FLOOR - 58, w - 4, 3, Pal.STEEL_2);
        p.lightRect(x + 4, FLOOR - 55, w - 8, 1, Pal.VIOLET_4);
        lights.add(new Light(x + w / 2, FLOOR - 50, 50, Pal.VIOLET_4, .7, 0));
        return w;
    }

    private int medBed(Painter p, int x) {
        p.rect(x, FLOOR - 14, 40, 3, Pal.STEEL_5);
        p.rect(x + 2, FLOOR - 17, 36, 3, Pal.BONE);
        p.rect(x + 2, FLOOR - 19, 8, 2, Pal.PAPER);
        p.rect(x + 3, FLOOR - 11, 2, 11, Pal.STEEL_3);
        p.rect(x + 35, FLOOR - 11, 2, 11, Pal.STEEL_3);
        p.rect(x + 44, FLOOR - 40, 1, 40, Pal.STEEL_4);
        p.rect(x + 41, FLOOR - 40, 7, 6, 0xFFAA2A2A);
        p.rect(x + 43, FLOOR - 39, 3, 1, Pal.BONE);
        p.rect(x + 44, FLOOR - 40, 1, 3, Pal.BONE);
        return 50;
    }

    private int mapTable(Painter p, int x) {
        p.rect(x, FLOOR - 20, 48, 4, Pal.RUST_2);
        p.rect(x + 2, FLOOR - 21, 44, 2, Pal.TEAL_1);
        for (int i = 0; i < 6; i++)
            p.light(x + 6 + i * 7, FLOOR - 21, i % 2 == 0 ? Pal.TEAL_5 : Pal.TEAL_3);
        p.rect(x + 8, FLOOR - 16, 4, 16, Pal.STEEL_2);
        p.rect(x + 36, FLOOR - 16, 4, 16, Pal.STEEL_2);
        lights.add(new Light(x + 24, FLOOR - 26, 40, Pal.TEAL_5, .6, 2));
        return 48;
    }

    private int workbench(Painter p, int x) {
        p.rect(x, FLOOR - 18, 50, 4, Pal.RUST_2);
        p.rect(x, FLOOR - 18, 50, 1, Pal.RUST_3);
        p.rect(x + 2, FLOOR - 14, 3, 14, Pal.STEEL_2);
        p.rect(x + 45, FLOOR - 14, 3, 14, Pal.STEEL_2);
        p.rect(x, FLOOR - 64, 50, 32, 0xFF3A2A1E);
        for (int y = FLOOR - 62; y < FLOOR - 34; y += 4)
            for (int xx = x + 2; xx < x + 48; xx += 4) p.px(xx, y, 0xFF2A1C14);
        p.line(x + 6, FLOOR - 58, x + 6, FLOOR - 44, Pal.STEEL_5);
        p.rect(x + 4, FLOOR - 58, 5, 2, Pal.STEEL_5);
        p.line(x + 16, FLOOR - 60, x + 22, FLOOR - 42, Pal.STEEL_6);
        p.rect(x + 30, FLOOR - 56, 12, 3, Pal.RED_2);
        p.rect(x + 36, FLOOR - 53, 2, 10, Pal.STEEL_4);
        p.rect(x + 8, FLOOR - 24, 12, 6, Pal.STEEL_4);
        return 50;
    }

    private int lantern(Painter p, int x) {
        p.rect(x + 4, 26, 1, 70, Pal.STEEL_2);
        p.rect(x, 96, 9, 10, Pal.RUST_2);
        p.lightRect(x + 2, 98, 5, 6, 0xFFFFB050);
        lights.add(new Light(x + 4, 100, 60, 0xFFFFA040, .9, 1));
        return 12;
    }

    private int candles(Painter p, int x, Random r) {
        int n = 3 + r.nextInt(4);
        for (int i = 0; i < n; i++) {
            int cx = x + i * 5, h = 4 + r.nextInt(8);
            p.rect(cx, FLOOR - h, 3, h, Pal.PAPER);
            p.light(cx + 1, FLOOR - h - 2, 0xFFFFD27A);
            p.light(cx + 1, FLOOR - h - 1, Pal.RUST_6);
            props.add(new Prop(PropKind.CANDLE, cx + 1, FLOOR - h - 2, 1, 2, 0xFFFFC060));
        }
        lights.add(new Light(x + n * 2, FLOOR - 10, 45, 0xFFFFB050, .7, 1));
        return n * 5;
    }

    private int pew(Painter p, int x) {
        p.rect(x, FLOOR - 12, 36, 3, Pal.RUST_1);
        p.rect(x, FLOOR - 24, 3, 24, Pal.RUST_1);
        p.rect(x + 3, FLOOR - 9, 2, 9, Pal.RUST_0);
        p.rect(x + 31, FLOOR - 9, 2, 9, Pal.RUST_0);
        return 36;
    }

    private void reactorCore(Painter p, int x) {
        p.box(x - 6, FLOOR - 20, 52, 20, new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4});
        p.box(x - 6, 36, 52, 16, new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4});
        p.rect(x, 52, 40, FLOOR - 72, Pal.GREEN_0);
        for (int y = 52; y < FLOOR - 20; y += 10) p.rect(x, y, 40, 2, Pal.STEEL_2);
        props.add(new Prop(PropKind.CORE, x, 52, 40, FLOOR - 72, Pal.GREEN_4));
        lights.add(new Light(x + 20, 110, 150, Pal.GREEN_4, 1.1, 2));
    }

    private void brokenTank(Painter p, int x) {
        int w = 72, h = 120;
        p.box(x - 4, FLOOR - 10, w + 8, 10, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.box(x - 4, FLOOR - h - 10, w + 8, 10, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.rect(x, FLOOR - h, w, h - 10, 0);
        for (int y = FLOOR - h; y < FLOOR - 10; y++) {
            p.px(x, y, Pal.TEAL_3);
            p.px(x + w - 1, y, Pal.TEAL_2);
        }
        int[][] shards = {{8, 30}, {20, 18}, {44, 40}, {60, 22}};
        for (int[] s : shards)
            p.line(x + s[0], FLOOR - h + s[1], x + s[0] + 6, FLOOR - h + s[1] + 14, Pal.TEAL_5);
        windows.add(new Window(x, FLOOR - h, w, h - 10));
    }

    private void beacon(Painter p, int x, int y) {
        p.rect(x - 3, y - 2, 7, 3, Pal.STEEL_2);
        p.ellipse(x + .5, y + 3, 3, 3, Pal.RED_1);
        props.add(new Prop(PropKind.BEACON, x - 2, y, 5, 6, Pal.RED_4));
        lights.add(new Light(x, y + 3, 70, Pal.RED_4, .9, 2));
    }

    private void helm(Painter p, int x) {
        p.rect(x - 2, FLOOR - 30, 5, 30, Pal.RUST_2);
        for (int k = 0; k < 48; k++) {
            double a = k * Math.PI / 24;
            p.px(
                    (int) Math.round(x + Math.cos(a) * 9),
                    (int) Math.round(FLOOR - 34 + Math.sin(a) * 9),
                    Pal.RUST_3);
        }
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            p.line(
                    x,
                    FLOOR - 34,
                    (int) Math.round(x + Math.cos(a) * 12),
                    (int) Math.round(FLOOR - 34 + Math.sin(a) * 12),
                    Pal.RUST_4);
        }
        p.ellipse(x, FLOOR - 34, 2, 2, Pal.RUST_5);
    }
}
