package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.ui.art.EnemyArt.Anim;
import ch.zhaw.abyss.ui.pixel.Sprite;

/**
 * Pixelgrafiken der vier Sektorwächter. Jeder Boss hat eine klar lesbare Silhouette und einen
 * leuchtenden Schwachpunkt, der in der Erholung sichtbar offen liegt.
 */
final class BossArt {
    private BossArt() {}

    static Sprite paint(EnemyKind kind, Anim anim, int f) {
        return switch (kind) {
            case WARDEN -> warden(anim, f);
            case REACTOR -> reactor(anim, f);
            case BROOD -> brood(anim, f);
            default -> captain(anim, f);
        };
    }

    // --- Der Schottmeister: Kettenfahrzeug mit Schottschild und Druckhammer -------------------

    private static Sprite warden(Anim anim, int f) {
        var p = new Painter(90, 70);
        int bob = anim == Anim.IDLE ? (f >= 2 ? 1 : 0) : anim == Anim.MOVE ? f % 2 : 0;
        boolean open = anim == Anim.RECOVER;
        int tilt = open ? 3 : 0;
        // Ketten
        p.box(14, 55, 54, 11, new int[] {Pal.STEEL_0, Pal.STEEL_1, Pal.STEEL_3});
        for (int i = 0; i < 7; i++) {
            int x = 19 + i * 7;
            p.ellipse(x, 60.5, 3, 3, Pal.STEEL_3);
            p.px(x, 60, Pal.STEEL_1);
        }
        for (int x = 15 + (anim == Anim.MOVE ? (f * 2) % 4 : 0); x < 67; x += 4)
            p.px(x, 55, Pal.STEEL_4);
        // Auspuffrohre
        p.rect(20, 12 + bob, 4, 18, Pal.STEEL_2);
        p.rect(26, 16 + bob, 3, 14, Pal.STEEL_3);
        p.rect(19, 11 + bob, 6, 2, Pal.STEEL_4);
        // Rumpf
        int top = 27 + bob + tilt;
        p.box(20, top, 40, 28 - tilt, new int[] {Pal.RUST_1, Pal.RUST_3, Pal.RUST_4});
        p.rect(22, top + 2, 3, 22, Pal.RUST_5);
        for (int x = 20; x < 60; x += 6) p.rect(x, top + 20 - tilt, 3, 4, Pal.INK);
        p.rect(20, top + 19 - tilt, 40, 1, Pal.RUST_2);
        for (int x = 23; x < 58; x += 5) p.px(x, top + 1, Pal.RUST_6);
        // Kern hinter dem Gitter
        int coreX = 42, coreY = top + 11;
        p.ellipse(coreX, coreY, 6, 6, Pal.STEEL_1);
        int core = open ? Pal.RUST_7 : Pal.RUST_5;
        for (int y = -4; y <= 4; y++)
            for (int x = -4; x <= 4; x++)
                if (x * x + y * y <= 17) {
                    boolean bar = !open && (x == -2 || x == 1 || x == 4);
                    if (bar) p.px(coreX + x, coreY + y, Pal.STEEL_3);
                    else p.light(coreX + x, coreY + y, x * x + y * y < 5 ? Pal.WHITE : core);
                }
        // Kanzel
        p.box(34, top - 11, 16, 12, new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5});
        p.rect(38, top - 7, 11, 3, Pal.INK);
        boolean angry = anim == Anim.WINDUP || anim == Anim.STRIKE;
        for (int x = 39; x < 48; x++) p.light(x, top - 6, angry ? Pal.RED_5 : Pal.RED_4);
        p.px(35, top - 10, Pal.STEEL_6);
        // Hammerarm
        double sx = 30, sy = top + 3, hx, hy;
        switch (anim) {
            case WINDUP -> {
                hx = 34;
                hy = top - 22;
            }
            case STRIKE -> {
                hx = 76;
                hy = 58;
            }
            case RECOVER -> {
                hx = 62;
                hy = 60;
            }
            default -> {
                hx = 42 + bob;
                hy = top - 12;
            }
        }
        p.thick(sx, sy, (sx + hx) / 2, Math.min(sy, hy) - 4, 5, Pal.STEEL_3);
        p.thick((sx + hx) / 2, Math.min(sy, hy) - 4, hx, hy, 4, Pal.STEEL_4);
        p.box(
                (int) hx - 5,
                (int) hy - 5,
                11,
                10,
                new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_5});
        p.rect((int) hx - 5, (int) hy - 1, 11, 2, Pal.RED_2);
        // Schottschild vorne
        int shieldX = anim == Anim.STRIKE ? 60 : open ? 57 : 58 + (anim == Anim.WINDUP ? 3 : 0);
        int shieldTop = top - 10 + (open ? 8 : 0);
        p.box(shieldX, shieldTop, 13, 38, new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_6});
        p.rect(shieldX + 1, shieldTop + 1, 11, 1, Pal.STEEL_7);
        for (int y = shieldTop + 3; y < shieldTop + 36; y += 5) {
            p.px(shieldX + 2, y, Pal.STEEL_6);
            p.px(shieldX + 10, y, Pal.STEEL_6);
        }
        p.ellipse(shieldX + 6.5, shieldTop + 10, 3.5, 3.5, Pal.STEEL_1);
        p.ellipse(shieldX + 6.5, shieldTop + 10, 2.5, 2.5, Pal.TEAL_1);
        p.light(shieldX + 6, shieldTop + 9, Pal.TEAL_4);
        p.ellipse(shieldX + 6.5, shieldTop + 24, 4, 4, Pal.RUST_3);
        p.ellipse(shieldX + 6.5, shieldTop + 24, 2.5, 2.5, Pal.STEEL_2);
        p.outline(Pal.OUTLINE);
        return p.sprite(42, 65);
    }

    // --- Der Reaktorkern: sechsbeiniger Läufer mit offenem Energiekern ------------------------

    private static Sprite reactor(Anim anim, int f) {
        var p = new Painter(96, 78);
        int bob = anim == Anim.IDLE ? (f >= 2 ? 1 : 0) : anim == Anim.MOVE ? f % 2 : 0;
        boolean open = anim == Anim.RECOVER;
        double cx = 48, cy = 34 + bob + (open ? 4 : 0);
        // Beine: drei hinten (dunkel), drei vorne
        for (int layer = 0; layer < 2; layer++)
            for (int i = 0; i < 3; i++) {
                double side = i - 1;
                double phase =
                        anim == Anim.MOVE
                                ? Math.sin((f / 6.0 + i / 3.0 + layer * .5) * Math.PI * 2)
                                : 0;
                double hipX = cx + side * 8, hipY = cy + 6;
                double kneeX = cx + side * 24 + (layer == 0 ? -3 : 3),
                        kneeY = cy - 8 - Math.max(0, phase) * 3;
                double footX = cx + side * 34 + phase * 3 + (layer == 0 ? -4 : 4), footY = 75;
                int color = layer == 0 ? Pal.STEEL_1 : Pal.STEEL_3;
                p.thick(hipX, hipY, kneeX, kneeY, layer == 0 ? 4 : 5, color);
                p.thick(kneeX, kneeY, footX, footY, layer == 0 ? 3 : 4, color);
                p.ellipse(kneeX, kneeY, 2.5, 2.5, layer == 0 ? Pal.STEEL_2 : Pal.STEEL_5);
                p.rect((int) footX - 3, 74, 6, 2, Pal.STEEL_2);
                if (layer == 1) {
                    p.light((int) kneeX, (int) kneeY - 1, Pal.GREEN_4);
                }
            }
        // Käfig und Kern
        p.sphere(cx, cy, 19, 17, new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5});
        boolean charged = anim == Anim.WINDUP || anim == Anim.STRIKE;
        int[] glow =
                charged
                        ? new int[] {Pal.GREEN_4, Pal.GREEN_5, 0xFFEFFFD0, Pal.WHITE}
                        : open
                                ? new int[] {Pal.TEAL_3, Pal.TEAL_4, Pal.TEAL_5, Pal.TEAL_6}
                                : new int[] {Pal.GREEN_3, Pal.GREEN_4, Pal.GREEN_5, 0xFFEFFFD0};
        double r = open ? 12 : 10.5;
        for (int y = (int) -r; y <= r; y++)
            for (int x = (int) -r; x <= r; x++) {
                double d = Math.hypot(x, y) / r;
                if (d > 1) continue;
                int c = d < .35 ? glow[3] : d < .6 ? glow[2] : d < .85 ? glow[1] : glow[0];
                p.light((int) cx + x, (int) cy + y, c);
            }
        if (!open)
            for (int bar = -12; bar <= 12; bar += 6)
                for (int y = -13; y <= 13; y++) {
                    double x = bar * Math.sqrt(Math.max(0, 1 - (y / 14.0) * (y / 14.0)));
                    p.px((int) Math.round(cx + x), (int) cy + y, Pal.STEEL_3);
                }
        p.rect((int) cx - 12, (int) cy - 1, 25, 2, Pal.STEEL_4);
        // Kamin und Warnlichter
        p.box((int) cx - 4, (int) cy - 26, 9, 9, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.rect((int) cx - 5, (int) cy - 27, 11, 2, Pal.STEEL_5);
        p.light((int) cx - 3, (int) cy - 22, (f % 2 == 0) ? Pal.RED_4 : Pal.RED_2);
        p.light((int) cx + 3, (int) cy - 22, (f % 2 == 1) ? Pal.RED_4 : Pal.RED_2);
        p.outline(Pal.OUTLINE);
        return p.sprite(48, 75);
    }

    // --- Die Brutmutter: Anglerfisch mit Leuchtköder ------------------------------------------

    private static Sprite brood(Anim anim, int f) {
        var p = new Painter(110, 84);
        double wave = Math.sin(f / (anim == Anim.MOVE ? 6.0 : 4.0) * Math.PI * 2);
        boolean open = anim == Anim.WINDUP || anim == Anim.STRIKE;
        boolean resting = anim == Anim.RECOVER;
        double cx = 52, cy = 44 + (resting ? 6 : wave);
        int[] body = {Pal.VIOLET_0, 0xFF2A1640, Pal.VIOLET_1, Pal.VIOLET_2};
        // Schwanzflosse
        double tailY = cy + wave * 4;
        p.poly(
                new double[] {cx - 26, cx - 44, cx - 40, cx - 44},
                new double[] {cy, tailY - 14, tailY, tailY + 14},
                0xFF2A1640);
        p.poly(
                new double[] {cx - 28, cx - 40, cx - 38},
                new double[] {cy, tailY - 9, tailY},
                Pal.VIOLET_1);
        // Rückenstacheln
        for (int i = 0; i < 5; i++) {
            double x = cx - 16 + i * 7;
            p.thick(x, cy - 17, x - 3, cy - 26 + Math.abs(i - 2), 2, Pal.VIOLET_1);
        }
        // Körper
        p.sphere(cx, cy, 30, 20, body);
        p.sphere(
                cx - 4,
                cy + 8,
                22,
                9,
                new int[] {0xFF2A1640, Pal.VIOLET_1, Pal.VIOLET_2, Pal.VIOLET_3});
        // Maul
        double jaw = open ? 12 : resting ? 3 : 6;
        p.poly(
                new double[] {cx + 14, cx + 33, cx + 32, cx + 14},
                new double[] {cy - 2, cy - 7, cy + jaw, cy + 6},
                Pal.RED_0);
        p.poly(
                new double[] {cx + 12, cx + 34, cx + 30, cx + 10},
                new double[] {cy + 6 + jaw * .4, cy + jaw + 1, cy + jaw + 6, cy + 14},
                0xFF2A1640);
        for (int i = 0; i < 5; i++) {
            int tx = (int) (cx + 16 + i * 3.5);
            p.px(tx, (int) (cy - 5 + i * -.3), Pal.BONE);
            p.px(tx, (int) (cy - 4 + i * -.3), Pal.BONE);
            p.px(tx + 1, (int) (cy + jaw - 1), Pal.BONE);
            p.px(tx + 1, (int) (cy + jaw - 2), Pal.PAPER);
        }
        // Auge
        p.ellipse(cx + 12, cy - 9, 3, 3, Pal.INK);
        p.light((int) cx + 13, (int) cy - 10, open ? Pal.RED_5 : Pal.RUST_6);
        p.light((int) cx + 12, (int) cy - 10, Pal.RUST_5);
        // Brustflosse
        double fin = wave * 5;
        p.poly(
                new double[] {cx - 2, cx - 16, cx - 8},
                new double[] {cy + 4, cy + 14 + fin, cy + 16 + fin},
                Pal.VIOLET_2);
        // Leuchtpunkte
        int[][] spots = {{-18, -6}, {-10, -12}, {-2, -15}, {6, -14}, {-20, 4}, {-12, 12}, {0, 13}};
        for (int[] s : spots) p.light((int) cx + s[0], (int) cy + s[1], Pal.TEAL_5);
        // Köder an der Angel
        double stalkX = cx + 8, stalkY = cy - 19;
        double lureX = cx + 34 + wave * 2, lureY = cy - 34 + (resting ? 16 : 0);
        double midX = cx + 22, midY = cy - 42 + (resting ? 12 : 0);
        for (int i = 0; i <= 12; i++) {
            double t = i / 12.0;
            double x = (1 - t) * (1 - t) * stalkX + 2 * (1 - t) * t * midX + t * t * lureX;
            double y = (1 - t) * (1 - t) * stalkY + 2 * (1 - t) * t * midY + t * t * lureY;
            p.px((int) x, (int) y, Pal.VIOLET_2);
            p.px((int) x, (int) y + 1, Pal.VIOLET_1);
        }
        int lure = resting ? Pal.TEAL_3 : open ? Pal.WHITE : Pal.TEAL_6;
        p.ellipse(lureX, lureY + 2, 3.2, 3.2, Pal.TEAL_2);
        for (int y = -2; y <= 2; y++)
            for (int x = -2; x <= 2; x++)
                if (x * x + y * y <= 5)
                    p.light(
                            (int) lureX + x,
                            (int) lureY + 2 + y,
                            x * x + y * y <= 1 ? Pal.WHITE : lure);
        p.outline(Pal.OUTLINE);
        return p.sprite(52, 66);
    }

    // --- Der Lotse: Kapitän mit Mantel, Mützenhelm und Harpunenkanone -------------------------

    private static Sprite captain(Anim anim, int f) {
        var p = new Painter(76, 70);
        int bob =
                anim == Anim.IDLE ? (f >= 2 ? 1 : 0) : anim == Anim.MOVE ? (f % 3 == 1 ? 1 : 0) : 0;
        boolean kneel = anim == Anim.RECOVER;
        double hipX = 36 + (anim == Anim.STRIKE ? 3 : 0), hipY = 43 + bob + (kneel ? 6 : 0);
        double step = anim == Anim.MOVE ? Math.sin(f / 6.0 * Math.PI * 2) * 4 : 0;
        int coat = 0xFF1B2944, coatLight = 0xFF2E4468, coatDark = 0xFF0F172A;
        // Beine
        p.thick(hipX - 3, hipY, hipX - 5 - step, kneel ? 62 : 64, 5, coatDark);
        p.rect((int) (hipX - 10 - step), 63, 8, 3, Pal.STEEL_1);
        if (kneel) p.thick(hipX + 3, hipY, hipX + 10, 62, 5, coatDark);
        else p.thick(hipX + 3, hipY, hipX + 5 + step, 64, 5, coat);
        p.rect((int) (hipX + (kneel ? 7 : 1 + step)), 63, 8, 3, Pal.STEEL_2);
        // Mantel
        p.poly(
                new double[] {hipX - 9, hipX + 9, hipX + 12, hipX - 13},
                new double[] {hipY - 18, hipY - 18, hipY + 8, hipY + 8},
                coat);
        p.rect((int) hipX - 12, (int) hipY + 6, 24, 2, coatDark);
        p.rect((int) hipX - 1, (int) hipY - 17, 2, 24, coatDark);
        for (int y = -14; y < 6; y += 5) p.px((int) hipX + 2, (int) hipY + y, Pal.RUST_6);
        p.rect((int) hipX - 8, (int) hipY - 18, 5, 18, coatLight);
        p.rect((int) hipX - 9, (int) hipY - 6, 19, 2, Pal.RUST_2);
        // Schulterstücke
        p.rect((int) hipX - 11, (int) hipY - 19, 7, 3, Pal.RUST_5);
        p.rect((int) hipX + 5, (int) hipY - 19, 7, 3, Pal.RUST_5);
        for (int x = -11; x < -4; x += 2) p.px((int) hipX + x, (int) hipY - 16, Pal.RUST_6);
        // Helm mit Mützenschirm
        double hx = hipX + 1, hy = hipY - 27;
        p.rect((int) hx - 6, (int) hy + 5, 13, 3, Pal.RUST_3);
        p.sphere(hx, hy, 7.5, 7, new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5, Pal.STEEL_7});
        p.rect((int) hx - 8, (int) hy - 6, 16, 3, coat);
        p.rect((int) hx - 7, (int) hy - 8, 13, 2, coat);
        p.rect((int) hx + 2, (int) hy - 3, 9, 2, Pal.INK);
        p.light((int) hx - 1, (int) hy - 7, Pal.RUST_6);
        p.rect((int) hx + 1, (int) hy - 1, 6, 3, Pal.INK);
        boolean angry = anim == Anim.WINDUP || anim == Anim.STRIKE;
        for (int x = 1; x < 7; x++) p.light((int) hx + x, (int) hy, angry ? Pal.RED_5 : Pal.RUST_6);
        // hinterer Arm
        p.thick(hipX - 7, hipY - 16, hipX - 11, hipY - 4, 4, coatDark);
        // Harpunenkanone am vorderen Arm
        double aim = anim == Anim.WINDUP ? -.15 : anim == Anim.STRIKE ? -.05 : kneel ? .9 : .35;
        double sx = hipX + 7, sy = hipY - 15;
        double ex = sx + Math.cos(aim) * 7, ey = sy + Math.sin(aim) * 7;
        p.thick(sx, sy, ex, ey, 4, coat);
        double gx = ex + Math.cos(aim) * 14, gy = ey + Math.sin(aim) * 14;
        p.thick(ex, ey, gx, gy, 6, Pal.STEEL_2);
        p.thick(ex, ey - 1, gx, gy - 1, 2, Pal.STEEL_4);
        p.thick(gx, gy, gx + Math.cos(aim) * 5, gy + Math.sin(aim) * 5, 2, Pal.STEEL_6);
        p.px((int) (gx + Math.cos(aim) * 6), (int) (gy + Math.sin(aim) * 6), Pal.STEEL_7);
        p.rect((int) ex - 1, (int) ey + 2, 4, 2, Pal.RUST_4);
        if (anim == Anim.STRIKE && f == 0) {
            p.light((int) (gx + Math.cos(aim) * 8), (int) gy, Pal.RUST_7);
            p.light((int) (gx + Math.cos(aim) * 9), (int) gy - 1, Pal.WHITE);
        }
        p.outline(Pal.OUTLINE);
        return p.sprite(36, 65);
    }
}
