package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.domain.Fixture;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.art.RoomArt;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.LightMap;

import java.util.Random;

/**
 * Zeichnet die Raumtechnik: Förderbänder, Dampfdüsen, Turbinenwind, Hydraulikpressen, Lasergitter
 * und Notschalter. Liest nur den Zustand der {@link Fixture}s.
 */
final class MachinePainter {
    private static final double PX = RoomArt.PX;
    private static final int CEILING = 18;

    private final Frame frame, emissive;
    private final Random random = new Random(7);

    MachinePainter(Frame frame, Frame emissive) {
        this.frame = frame;
        this.emissive = emissive;
    }

    private static int px(double units) {
        return (int) Math.round(units * PX);
    }

    /** Teile hinter den Figuren: Bänder, Düsen, Gehäuse, Emitter, Konsolen. */
    void drawBack(GameRun run, int camX, int camY, double time) {
        int floor = RoomArt.FLOOR + camY;
        for (var m : run.fixtures()) {
            int x = px(m.x()) - camX, half = px(m.width() / 2);
            if (x + half < -20 || x - half > WorldRenderer.W + 20) continue;
            switch (m.kind()) {
                case CONVEYOR -> conveyor(m, x, half, floor, time);
                case VENT_PAD -> vent(m, x, half, floor, time);
                case FAN -> fan(m, x, half, floor, time);
                case PRESS -> pressHousing(m, x, half, floor, time, camY);
                case LASER -> laserEmitters(m, x, floor, camY);
                case CONSOLE -> console(m, x, floor, time);
            }
        }
    }

    /** Teile vor den Figuren: Pressenstempel und Laserstrahlen. */
    void drawFront(GameRun run, int camX, int camY, double time) {
        int floor = RoomArt.FLOOR + camY;
        for (var m : run.fixtures()) {
            int x = px(m.x()) - camX, half = px(m.width() / 2);
            if (x + half < -20 || x - half > WorldRenderer.W + 20) continue;
            switch (m.kind()) {
                case PRESS -> pressBlock(m, x, half, floor, time, camY);
                case LASER -> laserBeam(m, x, floor, time, camY);
                default -> {}
            }
        }
    }

    /** Lichtquellen der Anlagen. */
    void light(GameRun run, LightMap lights, int camX) {
        for (var m : run.fixtures()) {
            int x = px(m.x()) - camX;
            if (x < -60 || x > WorldRenderer.W + 60) continue;
            switch (m.kind()) {
                case LASER -> {
                    if (m.active()) lights.point(x, 110, 60, 0xFFFF3060, .55);
                }
                case CONSOLE ->
                        lights.point(
                                x, RoomArt.FLOOR - 18, 28, consoleColor(m), m.active() ? .6 : .2);
                case VENT_PAD -> {
                    if (m.active()) lights.point(x, RoomArt.FLOOR - 2, 22, Pal.TEAL_5, .45);
                }
                case PRESS -> {
                    if (m.warning()) lights.point(x, CEILING + 12, 26, Pal.RED_4, .7);
                }
                default -> {}
            }
        }
    }

    /** Laufende Partikel: Dampf über Düsen, Windstreifen, Funken an Emittern. */
    void particles(GameRun run, Effects fx, double dt) {
        for (var m : run.fixtures()) {
            double x = m.x() * PX;
            switch (m.kind()) {
                case VENT_PAD -> {
                    if (m.active() && random.nextDouble() < dt * 8)
                        fx.burst(
                                Effects.Kind.STEAM,
                                x + (random.nextDouble() - .5) * 12,
                                RoomArt.FLOOR - 2,
                                1,
                                30,
                                -Math.PI / 2,
                                .4,
                                0xFFCCE8F0);
                }
                case FAN -> {
                    if (m.active() && random.nextDouble() < dt * 30) {
                        double start = x - m.direction() * m.width() * PX / 2;
                        fx.burst(
                                Effects.Kind.DUST,
                                start,
                                RoomArt.FLOOR - 8 - random.nextDouble() * 100,
                                1,
                                260,
                                m.direction() > 0 ? 0 : Math.PI,
                                .08,
                                0xFFB8D0DC);
                    }
                }
                case LASER -> {
                    if (m.active() && random.nextDouble() < dt * 10)
                        fx.burst(
                                Effects.Kind.SPARK,
                                x,
                                RoomArt.FLOOR - 4,
                                1,
                                50,
                                -Math.PI / 2,
                                1.5,
                                0xFFFF6080);
                }
                default -> {}
            }
        }
    }

    // --- Einzelne Anlagen ------------------------------------------------------------------------

    private void conveyor(Fixture m, int x, int half, int floor, double time) {
        int left = x - half, width = half * 2, top = floor - 5;
        frame.fill(left - 1, top - 1, width + 2, 8, Pal.OUTLINE);
        frame.fill(left, top, width, 6, 0xFF252B33);
        frame.fill(left, top, width, 1, 0xFF6A7480);
        frame.fill(left, top + 5, width, 1, 0xFF0C0F13);
        int shift = (int) Math.floor(time * 38 * m.direction());
        for (int i = 0; i < width + 10; i += 10) {
            int cx = left + Math.floorMod(i + shift, width + 10) - 5;
            if (cx < left + 1 || cx > left + width - 6) continue;
            for (int k = 0; k < 3; k++) {
                int tx = m.direction() > 0 ? cx + k : cx + 2 - k;
                frame.pixel(tx, top + 1 + k, 0xFFC8CED4);
                frame.pixel(tx, top + 4 - k, 0xFFC8CED4);
            }
        }
        for (int side = 0; side < 2; side++) {
            int px = side == 0 ? left - 5 : left + width + 1;
            frame.fill(px, top - 8, 5, 14, Pal.OUTLINE);
            for (int k = 0; k < 12; k++)
                frame.fill(px + 1, top - 7 + k, 3, 1, (k / 3) % 2 == 0 ? Pal.RUST_6 : Pal.INK);
            boolean blink = ((int) (time * 4) + side) % 2 == 0;
            frame.fill(px + 1, top - 10, 3, 2, blink ? Pal.RUST_6 : Pal.RUST_2);
            if (blink) emissive.add(px + 2, top - 10, Pal.RUST_6, .9);
        }
    }

    private void vent(Fixture m, int x, int half, int floor, double time) {
        int left = x - half, width = half * 2;
        frame.fill(left - 2, floor - 4, width + 4, 5, Pal.OUTLINE);
        frame.fill(left - 1, floor - 3, width + 2, 4, Pal.STEEL_4);
        frame.fill(left - 1, floor - 3, width + 2, 1, Pal.STEEL_6);
        boolean ready = m.active();
        if (ready) emissive.glow(x, floor - 3, half + 3, Pal.TEAL_4, .25);
        double pulse = .5 + .5 * Math.sin(time * 6);
        for (int k = left + 2; k < left + width - 2; k += 3) {
            frame.fill(k, floor - 2, 1, 2, ready ? Pal.TEAL_4 : 0xFF0C1015);
            if (ready) emissive.add(k, floor - 2, Pal.TEAL_5, .5 + .4 * pulse);
        }
        frame.fill(left, floor - 3, width, 1, ready ? Pal.TEAL_5 : Pal.STEEL_4);
    }

    private void fan(Fixture m, int x, int half, int floor, double time) {
        int fx = m.direction() > 0 ? x - half : x + half;
        int cy = floor - 42;
        frame.fill(fx - 3, cy + 14, 6, floor - cy - 14, Pal.STEEL_2);
        frame.disc(fx, cy, 18, Pal.OUTLINE);
        frame.disc(fx, cy, 17, Pal.STEEL_4);
        frame.circle(fx, cy, 16, Pal.STEEL_6);
        frame.disc(fx, cy, 13, 0xFF0B0F14);
        double speed = m.active() ? 26 : m.warning() ? 10 : 1.5;
        double angle = time * speed;
        for (int b = 0; b < 4; b++) {
            double a = angle + b * Math.PI / 2;
            int ex = fx + (int) Math.round(Math.cos(a) * 12);
            int ey = cy + (int) Math.round(Math.sin(a) * 12);
            frame.line(fx, cy, ex, ey, Pal.STEEL_7);
            frame.line(fx + 1, cy, ex + 1, ey, Pal.STEEL_5);
        }
        frame.disc(fx, cy, 3, Pal.STEEL_6);
        boolean lamp = m.active() || m.warning() && ((int) (time * 6)) % 2 == 0;
        frame.fill(fx - 2, cy - 21, 4, 3, lamp ? Pal.RUST_6 : Pal.RUST_1);
        if (lamp) emissive.add(fx, cy - 20, Pal.RUST_6, .9);
        if (m.warning()) {
            int from = Math.min(fx, x + (m.direction() > 0 ? half : -half));
            int to = Math.max(fx, x + (m.direction() > 0 ? half : -half));
            for (int k = from; k < to; k += 12) frame.fill(k, floor - 1, 6, 1, 0x80F5C45E);
        }
    }

    private void pressHousing(Fixture m, int x, int half, int floor, double time, int camY) {
        int top = CEILING + camY;
        frame.fill(x - half - 4, top, half * 2 + 8, 11, Pal.OUTLINE);
        frame.fill(x - half - 3, top + 1, half * 2 + 6, 9, Pal.STEEL_4);
        frame.fill(x - half - 3, top + 1, half * 2 + 6, 1, Pal.STEEL_6);
        for (int k = 0; k < half * 2 + 6; k += 4)
            frame.fill(x - half - 3 + k, top + 8, 2, 2, (k / 4) % 2 == 0 ? Pal.RUST_5 : Pal.INK);
        boolean lamp = m.warning() && ((int) (time * 8)) % 2 == 0;
        frame.fill(x - 2, top + 3, 4, 3, lamp ? Pal.RED_4 : Pal.RED_1);
        if (lamp) emissive.add(x, top + 4, Pal.RED_4, 1);
        if (m.warning()) {
            frame.fill(x - half, floor - 2, half * 2, 2, 0x70FF4030);
            for (int k = x - half; k < x + half; k += 6) frame.fill(k, floor - 1, 3, 1, Pal.RED_4);
        }
    }

    private void pressBlock(Fixture m, int x, int half, int floor, double time, int camY) {
        int top = CEILING + camY + 11;
        int bottom = floor - (int) Math.round(m.pressHeight() * 118);
        int shake = m.warning() ? (int) Math.round(Math.sin(time * 70)) : 0;
        int blockTop = bottom - 14;
        frame.fill(x - 4 + shake, top, 8, Math.max(0, blockTop - top), Pal.OUTLINE);
        frame.fill(x - 3 + shake, top, 6, Math.max(0, blockTop - top), Pal.STEEL_5);
        frame.fill(x - 3 + shake, top, 2, Math.max(0, blockTop - top), Pal.STEEL_7);
        frame.fill(x - half + shake, blockTop, half * 2, 14, Pal.OUTLINE);
        frame.fill(x - half + 1 + shake, blockTop + 1, half * 2 - 2, 12, Pal.STEEL_4);
        frame.fill(x - half + 1 + shake, blockTop + 1, half * 2 - 2, 2, Pal.STEEL_6);
        for (int k = 0; k < half * 2 - 2; k += 5)
            frame.fill(
                    x - half + 1 + k + shake,
                    bottom - 4,
                    3,
                    3,
                    (k / 5) % 2 == 0 ? Pal.RUST_5 : Pal.INK);
    }

    private void laserEmitters(Fixture m, int x, int floor, int camY) {
        int top = CEILING + camY;
        boolean on = m.disabled() <= 0;
        int color = on ? Pal.RED_3 : Pal.STEEL_3;
        frame.fill(x - 4, top, 9, 7, Pal.OUTLINE);
        frame.fill(x - 3, top + 1, 7, 5, Pal.STEEL_4);
        frame.fill(x - 1, top + 5, 3, 2, color);
        frame.fill(x - 4, floor - 6, 9, 6, Pal.OUTLINE);
        frame.fill(x - 3, floor - 5, 7, 5, Pal.STEEL_4);
        frame.fill(x - 1, floor - 6, 3, 2, color);
        if (on) {
            emissive.add(x, top + 6, Pal.RED_4, .8);
            emissive.add(x, floor - 6, Pal.RED_4, .8);
        }
    }

    private void laserBeam(Fixture m, int x, int floor, double time, int camY) {
        int top = CEILING + camY + 7, bottom = floor - 6;
        if (m.active()) {
            double flicker = .85 + .15 * Math.sin(time * 90);
            for (int y = top; y < bottom; y++) {
                frame.fill(x - 1, y, 3, 1, Pal.mix(0xFFFF4070, Pal.WHITE, .3 * flicker));
                frame.pixel(x, y, Pal.WHITE);
                emissive.add(x - 1, y, 0xFFFF3060, .6 * flicker);
                emissive.add(x, y, 0xFFFF6090, 1);
                emissive.add(x + 1, y, 0xFFFF3060, .6 * flicker);
            }
        } else if (m.warning()) {
            boolean blink = ((int) (time * 14)) % 2 == 0;
            if (blink) for (int y = top; y < bottom; y += 3) frame.pixel(x, y, 0xC0FF5070);
        }
    }

    private void console(Fixture m, int x, int floor, double time) {
        int top = floor - 28;
        int color = consoleColor(m);
        frame.fill(x - 10, top, 20, 28, Pal.OUTLINE);
        frame.fill(x - 9, top + 1, 18, 27, Pal.STEEL_4);
        frame.fill(x - 9, top + 1, 18, 1, Pal.STEEL_6);
        frame.fill(x - 9, top + 1, 1, 27, Pal.STEEL_5);
        frame.fill(x - 7, top + 3, 14, 9, 0xFF040A0E);
        if (m.active()) {
            boolean blink = ((int) (time * 3)) % 2 == 0;
            frame.fill(x - 6, top + 4, 12, 7, Frame.alpha(color, blink ? .9 : .65));
            emissive.addRect(x - 6, top + 4, 12, 7, color, .9);
            emissive.glow(x, top + 7, 12, color, .25);
        } else {
            int filled = (int) Math.round(12 * m.charge());
            frame.fill(x - 6, top + 9, filled, 2, color);
            emissive.addRect(x - 6, top + 9, filled, 2, color, .7);
        }
        frame.fill(x - 7, top + 15, 14, 4, Pal.STEEL_2);
        frame.fill(x - 2, top + 14, 4, 4, m.active() ? Pal.RED_3 : Pal.RED_1);
        if (m.active()) emissive.add(x, top + 15, Pal.RED_4, .8);
        for (int k = 0; k < 3; k++) frame.fill(x - 7 + k * 5, top + 22, 3, 2, Pal.STEEL_2);
        frame.fill(x - 1, top - 5, 2, 5, Pal.STEEL_5);
        boolean lamp = m.active() && ((int) (time * 2)) % 2 == 0;
        frame.fill(x - 2, top - 7, 4, 2, lamp ? color : Pal.STEEL_3);
        if (lamp) emissive.add(x, top - 6, color, 1);
    }

    static int consoleColor(Fixture m) {
        return switch (m.sector()) {
            case 0 -> Pal.RUST_6;
            case 1 -> 0xFFE8F4F8;
            case 2 -> Pal.TEAL_5;
            default -> Pal.RED_4;
        };
    }
}
