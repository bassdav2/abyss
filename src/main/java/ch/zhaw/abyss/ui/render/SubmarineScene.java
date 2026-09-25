package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.LightMap;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.pixel.PostProcess;

/**
 * Das Boot im Längsschnitt: für den Titelbildschirm langsam durch die Tiefe gleitend, für die
 * Bootskarte mit markierter Position. Vom Heck (links) bis zur Brücke (rechts) liegen 24
 * beleuchtete Räume in vier farbig gekennzeichneten Sektionen.
 */
public final class SubmarineScene {
    private static final int[] SECTOR_LIGHT = {0xFFFFB866, 0xFF9CFF8A, 0xFFC8A4FF, 0xFF8AD4FF};
    private static final int[] HULL_RAMP = {
        0xFF0C1118,
        0xFF141C26,
        0xFF1C2834,
        0xFF263646,
        0xFF324658,
        0xFF42586C,
        0xFF587088,
        0xFF7890A6
    };
    private static final int[] BAYER = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};
    private final Frame frame, emissive;
    private final LightMap lights;
    private final PostProcess post;
    private final Ocean ocean;
    private final PixelFont font;

    SubmarineScene(
            Frame frame,
            Frame emissive,
            LightMap lights,
            PostProcess post,
            Ocean ocean,
            PixelFont font) {
        this.frame = frame;
        this.emissive = emissive;
        this.lights = lights;
        this.post = post;
        this.ocean = ocean;
        this.font = font;
    }

    /**
     * Titelbild mit Schriftzug.
     *
     * @param time Zeit in Sekunden
     * @param calm ruhige Darstellung
     * @param showTitle Schriftzug zeichnen
     */
    void title(double time, boolean calm, boolean showTitle) {
        emissive.clear(0);
        double drift = calm ? 0 : time * 6;
        ocean.draw(frame, emissive, drift, 3, calm);
        // Lichtschächte von der Oberfläche
        for (int k = 0; k < 6; k++) {
            double sway = calm ? 0 : Math.sin(time * .3 + k * 1.3) * 6;
            int sx = 30 + k * 84 + (int) sway;
            int width = 14 + (k * 7) % 12;
            for (int yy = 0; yy < 230; yy++) {
                double fade = (1 - yy / 230.0) * (.05 + .02 * ((k * 5) % 3));
                frame.addRect(sx + yy * 2 / 5, yy, width, 1, 0xFF9CC8E8, fade);
            }
        }
        int bob = calm ? 0 : (int) Math.round(Math.sin(time * .6) * 2);
        lights.ambient(0xFF7896B4, .92);
        hull(24, 150 + bob, 432, 64, time, -1, -1, calm);
        lights.point(440, 150 + bob, 120, 0xFFBFE8FF, .6);
        lights.cone(456, 150 + bob, 0, .3, 260, 0xFFBFE8FF, .8);
        lights.apply(frame, emissive, 0);
        post.bloom(frame, emissive, .9);
        post.grade(frame, 0xFF0A1A36, 0xFFB8E0FF, .7, 0);
        post.vignette(frame, .55, Pal.INK);
        if (showTitle) {
            int w = font.width("ABYSS", 7);
            int x = 240 - w / 2, y = 30;
            font.drawOutlined(frame, "ABYSS", x, y, Pal.BONE, Pal.OUTLINE, 7);
            for (int yy = y + 28; yy < y + 49; yy++)
                for (int xx = x - 2; xx < x + w + 2; xx++) {
                    int c = frame.pixels()[yy * frame.width() + xx];
                    if (c == Pal.BONE)
                        frame.pixels()[yy * frame.width() + xx] =
                                Pal.mix(Pal.BONE, Pal.TEAL_5, (yy - y - 28) / 24.0);
                }
            font.drawCentered(
                    frame, "VOM HECK BIS ZUR BRÜCKE", 240, y + 58, Pal.RUST_6, Pal.OUTLINE, 1);
        }
    }

    /**
     * Kurzer Auftakt vor dem ersten Tauchgang: Das Boot treibt durch die Tiefe, drei Zeilen
     * erzählen die Ausgangslage.
     *
     * @param t Sekunden seit Beginn der Szene
     * @param calm ruhige Darstellung
     */
    void intro(double t, boolean calm) {
        emissive.clear(0);
        ocean.draw(frame, emissive, calm ? 0 : t * 10, 0, calm);
        lights.ambient(0xFF6A7A8A, .6);
        int boatX = (int) Math.round(-150 + t * 24);
        hull(boatX, 158, 280, 42, t, -1, -1, calm);
        lights.cone(boatX + 274, 160, 0, .28, 230, 0xFFBFE8FF, .8);
        lights.apply(frame, emissive, 0);
        post.bloom(frame, emissive, .8);
        post.vignette(frame, .7, Pal.INK);
        String[] lines = {
            "U-24. SEIT JAHREN TAUCHT DAS BOOT DURCH DIE TIEFE.",
            "VORN AUF DER BRÜCKE BESTIMMT DER LOTSE DEN KURS.",
            "HINTEN IM HECK HAST DU NICHTS MEHR ZU VERLIEREN."
        };
        double out = Math.max(0, Math.min(1, (8.6 - t) * 2));
        for (int i = 0; i < lines.length; i++) {
            double a = Math.max(0, Math.min(1, (t - .6 - i * 1.6) * 1.5)) * out;
            if (a > 0)
                font.drawCentered(
                        frame,
                        lines[i],
                        240,
                        44 + i * 16,
                        Frame.alpha(Pal.BONE, a),
                        Frame.alpha(Pal.OUTLINE, a),
                        1);
        }
        double title = Math.max(0, Math.min(1, (t - 5.4) * 1.5)) * out;
        if (title > 0) {
            String text = "VOM HECK BIS ZUR BRÜCKE";
            font.drawOutlined(
                    frame,
                    text,
                    240 - font.width(text, 2) / 2,
                    214,
                    Frame.alpha(Pal.RUST_6, title),
                    Frame.alpha(Pal.OUTLINE, title),
                    2);
        }
        String skip = "TASTE · ÜBERSPRINGEN";
        font.drawShadow(frame, skip, 474 - font.width(skip, 1), 258, 0x90C8D4DC, Pal.OUTLINE, 1);
        if (t > 8.2) post.flash(frame, Pal.INK, Math.min(1, (t - 8.2) / .8));
    }

    /**
     * Abschluss nach dem Sieg: Das Boot steigt aus der Tiefe zur Oberfläche auf.
     *
     * @param t Sekunden seit Beginn der Szene
     * @param calm ruhige Darstellung
     */
    void ending(double t, boolean calm) {
        emissive.clear(0);
        double rise = Math.min(1, t / 8.0);
        double ease = rise * rise * (3 - 2 * rise);
        int surface = (int) Math.round(-30 + ease * 118);
        for (int y = 0; y < frame.height(); y++) {
            int color;
            if (y < surface)
                color = Pal.mix(0xFF6FA6DA, 0xFFFFC08A, y / (double) Math.max(1, surface));
            else {
                double depth = Math.min(1, (y - surface) / 210.0);
                color =
                        Pal.mix(
                                Pal.mix(0xFF0E2A48, 0xFF4AA2C4, ease),
                                0xFF050E1C,
                                Math.pow(depth, .7));
            }
            for (int x = 0; x < frame.width(); x++) {
                int dither = BAYER[(y & 3) * 4 + (x & 3)];
                frame.pixel(x, y, dither > 13 ? Pal.mix(color, 0xFF000000, .06) : color);
            }
        }
        if (surface > 12) {
            int sunY = surface - 20;
            frame.disc(424, sunY, 11, 0xFFFFF2CC);
            emissive.glow(424, sunY, 28, 0xFFFFE0A0, .9);
        }
        for (int k = 0; k < 7; k++) {
            double sway = calm ? 0 : Math.sin(t * .5 + k) * 5;
            int sx = 20 + k * 70 + (int) sway;
            for (int y = Math.max(0, surface); y < frame.height(); y++)
                frame.addRect(
                        sx + (y - surface) / 3,
                        y,
                        10 + k % 3 * 4,
                        1,
                        0xFFBFE8FF,
                        .09 * ease * (1 - (y - surface) / 270.0));
        }
        int subX = (int) Math.round(30 + ease * 60), subY = (int) Math.round(236 - ease * 124);
        lights.ambient(0xFFC0DCF0, .9 + .15 * ease);
        hull(subX, subY, 330, 50, t, -1, -1, calm);
        for (int x = 0; x < frame.width(); x++) {
            int wy = surface + (int) Math.round(Math.sin(x * .09 + t * 2.2) * 1.5);
            frame.fill(x, wy, 1, 2, 0xFFE8F8FF);
        }
        if (ease > .98 && !calm)
            for (int k = 0; k < 16; k++) {
                int fx = subX + 200 + (int) (Math.sin(t * 3 + k * 1.7) * 30);
                frame.pixel(fx, surface - 1 - (k % 3), 0xFFFFFFFF);
            }
        lights.apply(frame, emissive, 0);
        post.bloom(frame, emissive, .9);
        post.vignette(frame, .3, Pal.INK);
        double title = Math.max(0, Math.min(1, (t - 1.2) * 1.5));
        if (title > 0) {
            String text = "BRÜCKE EROBERT";
            font.drawOutlined(
                    frame,
                    text,
                    240 - font.width(text, 3) / 2,
                    22,
                    Frame.alpha(Pal.BONE, title),
                    Frame.alpha(Pal.OUTLINE, title),
                    3);
        }
        double sub = Math.max(0, Math.min(1, (t - 2.8) * 1.5));
        if (sub > 0)
            font.drawCentered(
                    frame,
                    "DAS BOOT STEIGT AUF. DU BESTIMMST DEN KURS.",
                    240,
                    52,
                    Frame.alpha(Pal.RUST_7, sub),
                    Frame.alpha(Pal.OUTLINE, sub),
                    1);
        if (t > 2 && ((int) (t * 2)) % 2 == 0)
            font.drawCentered(frame, "LEERTASTE · WEITER", 240, 256, Pal.BONE, Pal.OUTLINE, 1);
    }

    /**
     * Bootskarte mit Position und Route.
     *
     * @param time Zeit
     * @param current aktuelle Raumtiefe
     * @param bossDepths ob Bosspositionen markiert werden
     */
    void map(double time, int current, boolean bossDepths) {
        emissive.clear(0);
        ocean.draw(frame, emissive, time * 4, 3, true);
        lights.ambient(0xFF7896B4, .9);
        hull(20, 138, 440, 76, time, current, bossDepths ? 1 : 0, false);
        lights.apply(frame, emissive, 0);
        post.bloom(frame, emissive, .8);
        post.vignette(frame, .5, Pal.INK);
    }

    /**
     * Zeichnet den Rumpf mit 24 Räumen.
     *
     * @param x linke Kante
     * @param cy vertikale Mitte
     * @param w Länge
     * @param h Höhe
     * @param time Zeit
     * @param current markierte Raumtiefe oder -1
     * @param marks 1 markiert Bosse
     * @param calm ruhige Darstellung
     */
    private void hull(
            int x, int cy, int w, int h, double time, int current, int marks, boolean calm) {
        int top = cy - h / 2, bottom = cy + h / 2;
        int stern = 70, bow = 44;
        // Propeller mit Welle
        double spin = calm ? 0 : time * 8;
        frame.fill(x - 12, cy - 1, 14, 3, 0xFF1A2028);
        for (int b = 0; b < 3; b++) {
            double a = spin + b * Math.PI * 2 / 3;
            int len = (int) Math.round(Math.cos(a) * 13);
            int shade = Math.sin(a) > 0 ? 0xFF6A5A40 : 0xFF3A3024;
            frame.fill(x - 11, cy - Math.max(0, -len), 4, Math.abs(len), shade);
            frame.fill(x - 11, cy, 4, Math.max(0, len), shade);
        }
        frame.fill(x - 13, cy - 2, 3, 5, 0xFF8A7A58);
        // Leitwerk am Heck: Ruder oben und unten, Tiefenruder seitlich
        for (int i = 0; i < 22; i++) {
            int fin = Math.max(0, 22 - (int) Math.round(i * 1.15));
            int fx = x + 4 + i;
            int r = radius(fx - x, w, h, stern, bow);
            int upper = cy - r - fin, lower = cy + r + fin;
            for (int yy = upper; yy < cy - r + 2; yy++)
                frame.pixel(fx, yy, yy == upper ? 0xFF8AA0B4 : HULL_RAMP[4]);
            for (int yy = cy + r - 2; yy <= lower; yy++)
                frame.pixel(fx, yy, yy == lower ? 0xFF2E5A74 : HULL_RAMP[2]);
        }
        // Rumpf: zylindrisch schattiert mit Bayer-Raster
        for (int xx = x; xx < x + w; xx++) {
            int r = radius(xx - x, w, h, stern, bow);
            for (int yy = cy - r; yy <= cy + r; yy++) {
                double t = (yy - cy) / (double) Math.max(1, r);
                double v = Math.pow((1 - t) / 2, 1.25);
                v += .22 * Math.exp(-Math.pow((t + .58) / .14, 2));
                if (xx > x + w - bow) v *= .8 + .2 * (x + w - xx) / (double) bow;
                double level = Math.max(0, Math.min(.999, v)) * (HULL_RAMP.length - 1);
                int idx = (int) (level + BAYER[(yy & 3) * 4 + (xx & 3)] / 16.0 - .25);
                idx = Math.max(0, Math.min(HULL_RAMP.length - 1, idx));
                frame.pixel(xx, yy, HULL_RAMP[idx]);
            }
            frame.pixel(xx, cy - r - 1, Pal.OUTLINE);
            frame.pixel(xx, cy + r + 1, Pal.OUTLINE);
            if (r > h / 2 - 3) frame.pixel(xx, cy - r, 0xFF9AB2C6);
            frame.pixel(xx, cy + r, 0xFF2E5A74);
        }
        // Plattennähte, Nieten, Ballastschlitze
        for (int xx = x + stern - 8; xx < x + w - bow; xx += 22) {
            int r = radius(xx - x, w, h, stern, bow);
            for (int yy = cy - r + 2; yy < cy + r - 1; yy++) frame.pixel(xx, yy, 0x60000000);
            for (int yy = cy - r + 4; yy < cy + r - 3; yy += 5) {
                frame.pixel(xx + 2, yy, yy < cy ? 0xC0A8BCCC : 0x80586878);
                frame.pixel(xx - 2, yy, yy < cy ? 0xC0A8BCCC : 0x80586878);
            }
        }
        for (int xx = x + stern; xx < x + w - bow; xx++) {
            frame.pixel(xx, cy - h / 2 + h * 3 / 10, 0x50000000);
            frame.pixel(xx, cy + h / 2 - h / 5, 0x60000000);
            if ((xx - x) % 7 < 3) frame.pixel(xx, cy + h / 2 - h / 8, 0xFF0A0E14);
        }
        // Rostspuren unter den Nähten
        for (int k = 0; k < 18; k++) {
            int rx = x + stern + (k * 97 + 31) % (w - stern - bow);
            int len = 4 + (k * 13) % 11;
            int sy = cy + 11 + (k % 3) * 3;
            for (int j = 0; j < len; j++)
                frame.pixel(rx, sy + j, Frame.alpha(0xFF7A4020, .55 * (1 - j / (double) len)));
        }
        // Turm mit Tiefenrudern, Periskop und Antenne
        int towerX = x + (int) (w * .64), towerW = 52, towerH = 26;
        for (int row = 0; row < towerH; row++) {
            double u = row / (double) towerH;
            int left = towerX + (int) Math.round((1 - u) * 12), right = towerX + towerW - 2;
            int c = HULL_RAMP[Math.min(HULL_RAMP.length - 1, 5 - (int) (u * 3))];
            frame.fill(left, top - towerH + row, right - left, 1, c);
            frame.pixel(left - 1, top - towerH + row, Pal.OUTLINE);
            frame.pixel(right, top - towerH + row, Pal.OUTLINE);
        }
        frame.fill(towerX + 12, top - towerH - 1, towerW - 14, 1, Pal.OUTLINE);
        frame.fill(towerX + 13, top - towerH, towerW - 16, 1, 0xFFA8BED0);
        frame.fill(towerX + 4, top - 14, towerW + 14, 3, Pal.OUTLINE);
        frame.fill(towerX + 5, top - 13, towerW + 12, 1, HULL_RAMP[4]);
        frame.fill(towerX + 34, top - towerH - 18, 3, 18, 0xFF222C38);
        frame.fill(towerX + 34, top - towerH - 18, 9, 3, 0xFF222C38);
        frame.pixel(towerX + 42, top - towerH - 17, 0xFFBFE8FF);
        frame.fill(towerX + 20, top - towerH - 12, 1, 12, 0xFF3A4A5A);
        boolean beacon = calm || ((int) (time * 1.5)) % 2 == 0;
        if (beacon) {
            frame.pixel(towerX + 20, top - towerH - 13, Pal.RED_4);
            emissive.glow(towerX + 20, top - towerH - 13, 3, Pal.RED_4, .9);
        }
        for (int k = 0; k < 3; k++) {
            int wx = towerX + 22 + k * 8, wy = top - towerH + 7;
            frame.fill(wx - 1, wy - 1, 5, 4, Pal.OUTLINE);
            frame.fill(wx, wy, 3, 2, Pal.RUST_6);
            emissive.addRect(wx, wy, 3, 2, Pal.RUST_6, .9);
        }
        // Bugruder und Kennung
        frame.fill(x + w - bow - 24, cy + 4, 20, 3, Pal.OUTLINE);
        frame.fill(x + w - bow - 23, cy + 4, 18, 1, HULL_RAMP[4]);
        font.draw(frame, "U-24", x + w - bow - 34, cy + h / 2 - 17, 0x90C8D4DC, 1);
        // Räume als Fensterreihe
        int rooms = RoomGenerator.ROOM_COUNT;
        int roomW = (w - 110) / rooms;
        for (int i = 0; i < rooms; i++) {
            int rx = x + 40 + i * roomW, ry = cy - 10;
            int sector = i / RoomGenerator.SECTOR_ROOMS;
            int light = SECTOR_LIGHT[sector];
            boolean boss = RoomGenerator.bossDepth(i);
            double flicker = calm ? 1 : .75 + .25 * Math.sin(time * 2 + i * 1.7);
            if (!calm && (i * 7 + (int) (time * 3)) % 23 == 0) flicker = .3;
            boolean passed = current >= 0 && i < current;
            boolean here = i == current;
            int fill = passed ? Pal.mix(light, 0xFF000000, .5) : light;
            frame.rect(rx, ry - 1, roomW, 20, Pal.OUTLINE);
            frame.fill(rx + 1, ry - 1, roomW - 2, 1, 0xFF8A7A58);
            frame.fill(rx + 1, ry, roomW - 2, 18, 0xFF0A0E14);
            for (int yy = 1; yy < 17; yy++)
                for (int xx = 2; xx < roomW - 3; xx++) {
                    double v = (1 - yy / 18.0) * flicker;
                    int c = Pal.mix(0xFF0A0E14, fill, .45 + .5 * v);
                    frame.pixel(rx + xx, ry + yy, c);
                    emissive.add(rx + xx, ry + yy, fill, .28 * v);
                }
            if (boss && marks > 0) {
                frame.fill(rx + roomW / 2 - 1, ry + 5, 3, 3, Pal.RED_4);
                emissive.add(rx + roomW / 2, ry + 6, Pal.RED_4, 1);
            }
            if (here) {
                double pulse = .5 + .5 * Math.sin(time * 6);
                frame.rect(rx, ry - 1, roomW, 20, Pal.WHITE);
                emissive.glow(rx + roomW / 2, ry + 9, 12, Pal.WHITE, .5 * pulse);
                frame.fill(rx + roomW / 2 - 1, ry + 12, 3, 5, Pal.RUST_6);
                frame.fill(rx + roomW / 2 - 2, ry + 9, 5, 3, Pal.RUST_6);
            }
            for (int k = 0; k < 4; k++)
                frame.fill(
                        rx + 2,
                        ry + 19 + k,
                        roomW - 4,
                        1,
                        Frame.alpha(fill, .22 * flicker * (1 - k / 4.0)));
            lights.point(rx + roomW / 2.0, ry + 8, 24, light, .35 * flicker);
        }
        // Scheinwerfer am Bug
        emissive.glow(x + w - 12, cy, 5, Pal.WHITE, 1);
        // Blasen am Heck
        if (!calm) {
            for (int k = 0; k < 14; k++) {
                double age = (time * .8 + k * .37) % 3;
                int bx = (int) (x - 14 - age * 30 + Math.sin(age * 5 + k) * 3);
                int by = (int) (cy + Math.sin(k) * 8 - age * 22);
                frame.pixel(bx, by, 0xFF8FB8C8);
                if (k % 3 == 0) frame.circle(bx, by, 1, 0xFF6F98A8);
            }
        }
    }

    private static int radius(int offset, int w, int h, int stern, int bow) {
        double half = h / 2.0;
        if (offset < stern) {
            double u = offset / (double) stern;
            return (int) Math.round(half * (.25 + .75 * (1 - (1 - u) * (1 - u))));
        }
        if (offset > w - bow) {
            double u = (w - offset) / (double) bow;
            return (int) Math.round(half * Math.sqrt(Math.max(0, 1 - (1 - u) * (1 - u))));
        }
        return (int) half;
    }
}
