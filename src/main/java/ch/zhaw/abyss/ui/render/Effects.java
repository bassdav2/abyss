package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.art.RoomArt;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.LightMap;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rein visuelle Effekte in Welt-Pixelkoordinaten: Partikel, Ringe, Blitze, Explosionen, Nachbilder,
 * Brandflecken, Blitzlichter und schwebende Zahlen. Nichts davon wirkt auf die Spielregeln zurück.
 */
public final class Effects {
    /** Partikelarten mit eigener Physik und Darstellung. */
    public enum Kind {
        SPARK,
        EMBER,
        SMOKE,
        DUST,
        BUBBLE,
        DEBRIS,
        GOO,
        ICE,
        PLUS,
        STAR,
        STEAM,
        FLAME
    }

    private static final class Particle {
        Kind kind;
        double x, y, vx, vy, life, max, size, gravity;
        int color;
        boolean glow;
    }

    private record Ring(
            double x, double y, double r0, double r1, double max, int color, double[] age) {}

    private record Bolt(
            double x1, double y1, double x2, double y2, double max, int color, double[] age) {}

    private record Text(
            String text, double x, double y, double max, int color, int scale, double[] age) {}

    private record Ghost(
            Sprite sprite, int x, int y, boolean flip, int color, double max, double[] age) {}

    private record Blast(
            double x, double y, double radius, double max, boolean cold, double[] age) {}

    private record Flash(
            double x,
            double y,
            double radius,
            int color,
            double intensity,
            double max,
            double[] age) {}

    private record Decal(double x, double y, double width, int color, double[] age) {}

    private final List<Particle> particles = new ArrayList<>();
    private final List<Ring> rings = new ArrayList<>();
    private final List<Bolt> bolts = new ArrayList<>();
    private final List<Text> texts = new ArrayList<>();
    private final List<Ghost> ghosts = new ArrayList<>();
    private final List<Blast> blasts = new ArrayList<>();
    private final List<Flash> flashes = new ArrayList<>();
    private final List<Decal> decals = new ArrayList<>();
    private final Random random = new Random(4711);
    private double budget = 1;

    /**
     * @param factor Partikelbudget 0 bis 1, reduziert bei ruhiger Darstellung
     */
    public void budget(double factor) {
        budget = factor;
    }

    /** Entfernt alle Effekte, etwa beim Raumwechsel. */
    public void clear() {
        particles.clear();
        rings.clear();
        bolts.clear();
        texts.clear();
        ghosts.clear();
        blasts.clear();
        flashes.clear();
        decals.clear();
    }

    /**
     * @return Zufallsquelle für Effektvariation
     */
    public Random random() {
        return random;
    }

    /**
     * Streut Partikel.
     *
     * @param kind Art
     * @param x Ursprung
     * @param y Ursprung
     * @param count Anzahl vor Budget
     * @param speed Grundtempo in Pixeln pro Sekunde
     * @param angle Hauptrichtung in Radiant
     * @param spread Streuung in Radiant
     * @param color Farbe
     */
    public void burst(
            Kind kind,
            double x,
            double y,
            int count,
            double speed,
            double angle,
            double spread,
            int color) {
        int n = (int) Math.ceil(count * budget);
        for (int i = 0; i < n && particles.size() < 900; i++) {
            var p = new Particle();
            p.kind = kind;
            p.x = x + (random.nextDouble() - .5) * 2;
            p.y = y + (random.nextDouble() - .5) * 2;
            double a = angle + (random.nextDouble() - .5) * spread;
            double s = speed * (.35 + random.nextDouble() * .9);
            p.vx = Math.cos(a) * s;
            p.vy = Math.sin(a) * s;
            p.color = color;
            p.size = 1;
            switch (kind) {
                case SPARK -> {
                    p.life = .18 + random.nextDouble() * .25;
                    p.gravity = 260;
                    p.glow = true;
                }
                case EMBER -> {
                    p.life = .6 + random.nextDouble() * .8;
                    p.gravity = -30;
                    p.glow = true;
                }
                case SMOKE -> {
                    p.life = .6 + random.nextDouble() * .7;
                    p.gravity = -25;
                    p.size = 2 + random.nextInt(3);
                }
                case DUST -> {
                    p.life = .3 + random.nextDouble() * .3;
                    p.gravity = 40;
                    p.size = 1 + random.nextInt(2);
                }
                case BUBBLE -> {
                    p.life = .8 + random.nextDouble() * 1.2;
                    p.gravity = -60;
                    p.size = random.nextInt(3) == 0 ? 2 : 1;
                }
                case DEBRIS -> {
                    p.life = 1.4 + random.nextDouble();
                    p.gravity = 520;
                    p.size = 1 + random.nextInt(2);
                }
                case GOO -> {
                    p.life = 1.0 + random.nextDouble() * .6;
                    p.gravity = 420;
                    p.glow = true;
                    p.size = 1 + random.nextInt(2);
                }
                case ICE -> {
                    p.life = .4 + random.nextDouble() * .4;
                    p.gravity = 300;
                    p.glow = true;
                }
                case PLUS, STAR -> {
                    p.life = .7 + random.nextDouble() * .4;
                    p.gravity = -20;
                    p.glow = true;
                }
                case STEAM -> {
                    p.life = .5 + random.nextDouble() * .5;
                    p.gravity = -80;
                    p.size = 2 + random.nextInt(3);
                }
                case FLAME -> {
                    p.life = .25 + random.nextDouble() * .3;
                    p.gravity = -120;
                    p.glow = true;
                    p.size = 1 + random.nextInt(2);
                }
            }
            p.max = p.life;
            particles.add(p);
        }
    }

    /**
     * @param x Mitte
     * @param y Mitte
     * @param from Startradius
     * @param to Endradius
     * @param seconds Dauer
     * @param color Farbe
     */
    public void ring(double x, double y, double from, double to, double seconds, int color) {
        rings.add(new Ring(x, y, from, to, seconds, color, new double[1]));
    }

    /**
     * @param x1 Start
     * @param y1 Start
     * @param x2 Ende
     * @param y2 Ende
     * @param seconds Dauer
     * @param color Farbe
     */
    public void bolt(double x1, double y1, double x2, double y2, double seconds, int color) {
        bolts.add(new Bolt(x1, y1, x2, y2, seconds, color, new double[1]));
    }

    /**
     * Schwebender Text, etwa Schadenszahlen.
     *
     * @param text Text
     * @param x Mitte
     * @param y Oberkante
     * @param seconds Dauer
     * @param color Farbe
     * @param scale Vergrösserung
     */
    public void text(String text, double x, double y, double seconds, int color, int scale) {
        if (texts.size() > 40) texts.removeFirst();
        texts.add(new Text(text, x, y, seconds, color, scale, new double[1]));
    }

    /**
     * Nachbild eines Sprites, etwa beim Ausweichen.
     *
     * @param sprite Sprite
     * @param x Anker
     * @param y Anker
     * @param flip gespiegelt
     * @param color Tönung
     * @param seconds Dauer
     */
    public void ghost(Sprite sprite, int x, int y, boolean flip, int color, double seconds) {
        if (sprite != null)
            ghosts.add(new Ghost(sprite, x, y, flip, color, seconds, new double[1]));
    }

    /**
     * Explosion mit Feuerball, Rauch, Funken, Licht und Brandfleck.
     *
     * @param x Mitte
     * @param y Mitte
     * @param radius Radius in Pixeln
     * @param cold Kälteexplosion statt Feuer
     */
    public void explosion(double x, double y, double radius, boolean cold) {
        blasts.add(new Blast(x, y, radius, .45 + radius / 140, cold, new double[1]));
        flash(x, y, radius * 3.2, cold ? Pal.TEAL_5 : 0xFFFFB060, 1.6, .35);
        burst(
                cold ? Kind.ICE : Kind.SPARK,
                x,
                y,
                (int) (radius * 1.1),
                radius * 7,
                0,
                Math.PI * 2,
                cold ? Pal.TEAL_6 : Pal.RUST_6);
        burst(
                Kind.SMOKE,
                x,
                y,
                (int) (radius * .5),
                radius * 1.6,
                -Math.PI / 2,
                Math.PI * 1.6,
                cold ? 0xFF8AB8D0 : 0xFF3A3434);
        if (!cold)
            burst(
                    Kind.EMBER,
                    x,
                    y,
                    (int) (radius * .4),
                    radius * 3,
                    -Math.PI / 2,
                    Math.PI,
                    Pal.RUST_5);
        if (y > RoomArt.FLOOR - radius)
            decal(x, RoomArt.FLOOR, radius * 1.4, cold ? 0xFF9ACBE0 : 0xFF140E0C);
    }

    /**
     * Kurzes Lichtereignis für die Lichtkarte.
     *
     * @param x Mitte
     * @param y Mitte
     * @param radius Radius
     * @param color Farbe
     * @param intensity Helligkeit
     * @param seconds Dauer
     */
    public void flash(
            double x, double y, double radius, int color, double intensity, double seconds) {
        flashes.add(new Flash(x, y, radius, color, intensity, seconds, new double[1]));
    }

    /**
     * Fleck auf dem Boden.
     *
     * @param x Mitte
     * @param y Bodenhöhe
     * @param width Breite
     * @param color Farbe
     */
    public void decal(double x, double y, double width, int color) {
        if (decals.size() > 30) decals.removeFirst();
        decals.add(new Decal(x, y, width, color, new double[1]));
    }

    /**
     * Lässt Zeit vergehen.
     *
     * @param dt Sekunden
     */
    public void update(double dt) {
        for (var p : particles) {
            p.life -= dt;
            p.vy += p.gravity * dt;
            if (p.kind == Kind.SMOKE || p.kind == Kind.STEAM || p.kind == Kind.BUBBLE) {
                p.vx *= Math.exp(-2 * dt);
                if (p.kind == Kind.BUBBLE) p.vx += Math.sin(p.life * 9) * 6 * dt;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if ((p.kind == Kind.DEBRIS || p.kind == Kind.GOO || p.kind == Kind.SPARK)
                    && p.y > RoomArt.FLOOR) {
                p.y = RoomArt.FLOOR;
                if (p.kind == Kind.GOO) {
                    decal(p.x, RoomArt.FLOOR, 3 + p.size, p.color);
                    p.life = 0;
                } else {
                    p.vy = -Math.abs(p.vy) * .35;
                    p.vx *= .6;
                }
            }
            if (p.kind == Kind.BUBBLE && p.y < 30) p.life = 0;
        }
        particles.removeIf(p -> p.life <= 0);
        age(rings, dt);
        age(bolts, dt);
        age(texts, dt);
        age(ghosts, dt);
        age(blasts, dt);
        age(flashes, dt);
        for (var d : decals) d.age[0] += dt;
        decals.removeIf(d -> d.age[0] > 18);
    }

    private static <T extends Record> void age(List<T> list, double dt) {
        list.removeIf(
                item -> {
                    double[] age;
                    double max;
                    switch (item) {
                        case Ring r -> {
                            age = r.age;
                            max = r.max;
                        }
                        case Bolt b -> {
                            age = b.age;
                            max = b.max;
                        }
                        case Text t -> {
                            age = t.age;
                            max = t.max;
                        }
                        case Ghost g -> {
                            age = g.age;
                            max = g.max;
                        }
                        case Blast b -> {
                            age = b.age;
                            max = b.max;
                        }
                        case Flash f -> {
                            age = f.age;
                            max = f.max;
                        }
                        default -> {
                            return true;
                        }
                    }
                    age[0] += dt;
                    return age[0] >= max;
                });
    }

    /**
     * Zeichnet Bodenflecken unter allen anderen Effekten.
     *
     * @param frame Ziel
     * @param camX Kameraversatz
     * @param camY vertikaler Versatz
     */
    public void drawDecals(Frame frame, int camX, int camY) {
        for (var d : decals) {
            double fade = Math.max(0, 1 - d.age[0] / 18);
            int half = (int) (d.width / 2);
            for (int i = -half; i <= half; i++) {
                double edge = 1 - Math.abs(i) / (double) Math.max(1, half);
                if ((i * 7 + (int) d.x) % 3 == 0 && edge < .5) continue;
                frame.pixel(
                        (int) d.x + i - camX,
                        (int) d.y + camY,
                        Frame.alpha(d.color, .55 * fade * edge + .1 * fade));
                if (edge > .4)
                    frame.pixel(
                            (int) d.x + i - camX,
                            (int) d.y + 1 + camY,
                            Frame.alpha(d.color, .4 * fade));
            }
        }
    }

    /**
     * Zeichnet Partikel, Ringe, Blitze, Explosionen und Nachbilder.
     *
     * @param frame Farbebene
     * @param emissive Leuchtebene
     * @param camX Kameraversatz
     * @param camY vertikaler Versatz
     */
    public void draw(Frame frame, Frame emissive, int camX, int camY) {
        for (var g : ghosts) {
            double a = 1 - g.age[0] / g.max;
            frame.draw(g.sprite, g.x - camX, g.y + camY, g.flip, g.color, .75, a * .55);
            emissive.drawAdd(g.sprite, g.x - camX, g.y + camY, g.flip, a * .12);
        }
        for (var b : blasts) drawBlast(frame, emissive, b, camX, camY);
        for (var p : particles) {
            int x = (int) Math.round(p.x) - camX, y = (int) Math.round(p.y) + camY;
            double t = p.life / p.max;
            switch (p.kind) {
                case SPARK, ICE -> {
                    int tx = (int) Math.round(p.x - p.vx * .02) - camX,
                            ty = (int) Math.round(p.y - p.vy * .02) + camY;
                    frame.line(tx, ty, x, y, p.color);
                    emissive.lineAdd(tx, ty, x, y, p.color, .8 * t);
                }
                case EMBER, FLAME -> {
                    int c = p.kind == Kind.FLAME ? Frame.mix(Pal.RED_3, Pal.RUST_7, t) : p.color;
                    frame.fill(x, y, (int) p.size, (int) p.size, c);
                    emissive.addRect(x, y, (int) p.size, (int) p.size, c, .7 * t + .2);
                }
                case SMOKE, STEAM -> {
                    int r = (int) Math.round(p.size + (1 - t) * 3);
                    int c = Frame.alpha(p.color, (p.kind == Kind.STEAM ? .35 : .55) * t);
                    for (int yy = -r; yy <= r; yy++)
                        for (int xx = -r; xx <= r; xx++)
                            if (xx * xx + yy * yy <= r * r && ((xx + yy + x + y) & 1) == 0)
                                frame.pixel(x + xx, y + yy, c);
                }
                case DUST -> frame.fill(x, y, (int) p.size, 1, Frame.alpha(p.color, .8 * t));
                case BUBBLE -> {
                    if (p.size > 1) {
                        frame.pixel(x, y - 1, Frame.alpha(p.color, .8));
                        frame.pixel(x - 1, y, Frame.alpha(p.color, .6));
                        frame.pixel(x + 1, y, Frame.alpha(p.color, .6));
                        frame.pixel(x, y + 1, Frame.alpha(p.color, .4));
                    } else frame.pixel(x, y, Frame.alpha(p.color, .7));
                    emissive.add(x, y, p.color, .15);
                }
                case DEBRIS -> {
                    double a = Math.min(1, t * 3);
                    frame.fill(x, y, (int) p.size + 1, (int) p.size, Frame.alpha(p.color, a));
                    frame.pixel(x, y, Frame.alpha(Pal.shade(p.color, 1.5), a));
                }
                case GOO -> {
                    frame.fill(x, y, (int) p.size, (int) p.size, p.color);
                    emissive.addRect(x, y, (int) p.size, (int) p.size, p.color, .6);
                }
                case PLUS -> {
                    frame.pixel(x, y, p.color);
                    frame.pixel(x - 1, y, p.color);
                    frame.pixel(x + 1, y, p.color);
                    frame.pixel(x, y - 1, p.color);
                    frame.pixel(x, y + 1, p.color);
                    emissive.add(x, y, p.color, t);
                }
                case STAR -> {
                    boolean big = t > .5;
                    emissive.add(x, y, p.color, t);
                    frame.pixel(x, y, Pal.WHITE);
                    if (big) {
                        frame.pixel(x - 1, y, p.color);
                        frame.pixel(x + 1, y, p.color);
                        frame.pixel(x, y - 1, p.color);
                        frame.pixel(x, y + 1, p.color);
                    }
                }
            }
        }
        for (var r : rings) {
            double t = r.age[0] / r.max;
            double radius = r.r0 + (r.r1 - r.r0) * (1 - (1 - t) * (1 - t));
            int cx = (int) Math.round(r.x) - camX, cy = (int) Math.round(r.y) + camY;
            int color = r.color;
            frame.circle(cx, cy, (int) radius, Frame.alpha(color, 1 - t));
            if (t < .6) frame.circle(cx, cy, (int) radius - 1, Frame.alpha(color, .5 * (1 - t)));
            ringGlow(emissive, cx, cy, (int) radius, color, 1 - t);
        }
        for (var b : bolts) drawBolt(frame, emissive, b, camX, camY);
    }

    private static void ringGlow(
            Frame emissive, int cx, int cy, int r, int color, double intensity) {
        int steps = Math.max(12, r * 4);
        for (int i = 0; i < steps; i++) {
            double a = i * Math.PI * 2 / steps;
            emissive.add(
                    (int) Math.round(cx + Math.cos(a) * r),
                    (int) Math.round(cy + Math.sin(a) * r),
                    color,
                    intensity * .7);
        }
    }

    private void drawBolt(Frame frame, Frame emissive, Bolt b, int camX, int camY) {
        double t = b.age[0] / b.max;
        double x = b.x1, y = b.y1;
        int segments = Math.max(3, (int) (Math.hypot(b.x2 - b.x1, b.y2 - b.y1) / 7));
        for (int i = 1; i <= segments; i++) {
            double f = i / (double) segments;
            double nx =
                    b.x1 + (b.x2 - b.x1) * f + (i == segments ? 0 : (random.nextDouble() - .5) * 8);
            double ny =
                    b.y1 + (b.y2 - b.y1) * f + (i == segments ? 0 : (random.nextDouble() - .5) * 8);
            int x0 = (int) x - camX,
                    y0 = (int) y + camY,
                    x1 = (int) nx - camX,
                    y1 = (int) ny + camY;
            frame.line(x0, y0, x1, y1, Pal.WHITE);
            emissive.lineAdd(x0, y0, x1, y1, b.color, 1 - t);
            emissive.lineAdd(x0 + 1, y0, x1 + 1, y1, b.color, .4 * (1 - t));
            x = nx;
            y = ny;
        }
    }

    private void drawBlast(Frame frame, Frame emissive, Blast b, int camX, int camY) {
        double t = b.age[0] / b.max;
        int cx = (int) Math.round(b.x) - camX, cy = (int) Math.round(b.y) + camY;
        double r = b.radius * (.35 + .75 * Math.sqrt(t));
        int[] hot =
                b.cold
                        ? new int[] {Pal.WHITE, Pal.TEAL_6, Pal.TEAL_5, Pal.TEAL_3}
                        : new int[] {Pal.WHITE, Pal.RUST_7, Pal.RUST_5, Pal.RED_3};
        int ri = (int) Math.ceil(r);
        for (int y = -ri; y <= ri; y++)
            for (int x = -ri; x <= ri; x++) {
                double d = Math.hypot(x, y) / r;
                if (d > 1) continue;
                double heat = (1 - d) * (1.25 - t * 1.4) + (((x + y) & 1) == 0 ? .06 : -.06);
                if (heat <= 0) {
                    if (t > .4 && d > .6 && ((x ^ y) & 1) == 0)
                        frame.pixel(cx + x, cy + y, Frame.alpha(0xFF2A2424, .5 * (1 - t)));
                    continue;
                }
                int c = heat > .75 ? hot[0] : heat > .5 ? hot[1] : heat > .25 ? hot[2] : hot[3];
                frame.pixel(cx + x, cy + y, c);
                emissive.add(cx + x, cy + y, c, Math.min(1, heat + .2));
            }
    }

    /**
     * Zeichnet schwebende Texte nach der Nachbearbeitung, damit sie scharf bleiben.
     *
     * @param frame Ziel
     * @param font Schrift
     * @param camX Kameraversatz
     * @param camY vertikaler Versatz
     */
    public void drawTexts(Frame frame, PixelFont font, int camX, int camY) {
        for (var t : texts) {
            double k = t.age[0] / t.max;
            int y = (int) Math.round(t.y - k * 16 - (k < .15 ? (.15 - k) * 20 : 0)) + camY;
            int x = (int) Math.round(t.x) - camX - font.width(t.text, t.scale) / 2;
            int color = k > .7 ? Frame.alpha(t.color, 1 - (k - .7) / .3) : t.color;
            font.drawOutlined(
                    frame,
                    t.text,
                    x,
                    y,
                    color,
                    Frame.alpha(Pal.OUTLINE, k > .7 ? 1 - (k - .7) / .3 : 1),
                    t.scale);
        }
    }

    /**
     * Fügt Blitzlichter und leuchtende Partikel der Lichtkarte hinzu.
     *
     * @param lights Lichtkarte
     * @param camX Kameraversatz
     */
    public void light(LightMap lights, int camX) {
        for (var f : flashes) {
            double t = f.age[0] / f.max;
            lights.point(f.x - camX, f.y, f.radius, f.color, f.intensity * (1 - t) * (1 - t));
        }
        for (var b : blasts) {
            double t = b.age[0] / b.max;
            lights.point(
                    b.x - camX,
                    b.y,
                    b.radius * 2.4,
                    b.cold ? Pal.TEAL_5 : 0xFFFF9A40,
                    1.2 * (1 - t));
        }
        int budget = 0;
        for (var p : particles)
            if (p.glow && (p.kind == Kind.EMBER || p.kind == Kind.FLAME) && budget++ < 30)
                lights.point(p.x - camX, p.y, 14, p.color, .25 * p.life / p.max);
    }
}
