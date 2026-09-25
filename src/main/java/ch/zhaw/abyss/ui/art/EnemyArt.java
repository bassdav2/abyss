package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Prozedural gemalte Gegner. Jede Art besteht aus wenigen schattierten Grundformen mit leuchtenden
 * Augen; die Animation entsteht aus verschobenen Gliedmassen je Bild.
 */
public final class EnemyArt {
    /** Animationen der Gegner. */
    public enum Anim {
        IDLE(4),
        MOVE(6),
        WINDUP(2),
        STRIKE(2),
        RECOVER(2),
        HURT(1);

        private final int frames;

        Anim(int frames) {
            this.frames = frames;
        }

        /**
         * @return Anzahl Bilder
         */
        public int frames() {
            return frames;
        }
    }

    private static final int[] RUST = {Pal.RUST_1, Pal.RUST_3, Pal.RUST_4, Pal.RUST_6};
    private static final int[] STEEL = {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5, Pal.STEEL_7};
    private static final int[] DARK = {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_6};
    private static final int[] BLUE = {0xFF1B2A44, 0xFF2D4A72, 0xFF4A6E9E, 0xFF8FB2D8};

    private EnemyArt() {}

    /**
     * Malt alle Animationen einer Gegnerart.
     *
     * @param kind Art
     * @return Einzelbilder je Animation
     */
    public static Map<Anim, List<Sprite>> build(EnemyKind kind) {
        var result = new EnumMap<Anim, List<Sprite>>(Anim.class);
        for (var anim : Anim.values()) {
            var frames = new ArrayList<Sprite>();
            for (int f = 0; f < anim.frames(); f++) frames.add(paint(kind, anim, f));
            result.put(anim, List.copyOf(frames));
        }
        return result;
    }

    private static Sprite paint(EnemyKind kind, Anim anim, int f) {
        if (kind.boss()) return BossArt.paint(kind, anim, f);
        int[] size = size(kind);
        var p = new Painter(size[0], size[1]);
        switch (kind) {
            case SCUTTLER -> scuttler(p, anim, f);
            case DRONE -> drone(p, anim, f);
            case SENTINEL -> sentinel(p, anim, f);
            case BOMBER -> bomber(p, anim, f);
            case WELDER -> welder(p, anim, f);
            case TURRET -> turret(p, anim, f);
            case MINELAYER -> minelayer(p, anim, f);
            case JELLY -> jelly(p, anim, f);
            case EEL -> eel(p, anim, f);
            case SHIELDBEARER -> shieldbearer(p, anim, f);
            case ENFORCER -> enforcer(p, anim, f);
            case SEEKER -> seeker(p, anim, f);
            case SMUGGLER -> smuggler(p, anim, f);
            default -> {}
        }
        p.outline(Pal.OUTLINE);
        return p.sprite(size[0] / 2, size[2]);
    }

    private static int[] size(EnemyKind kind) {
        return switch (kind) {
            case SCUTTLER -> new int[] {36, 26, 23};
            case DRONE -> new int[] {32, 30, 27};
            case SENTINEL -> new int[] {52, 50, 47};
            case BOMBER -> new int[] {26, 24, 21};
            case WELDER -> new int[] {44, 38, 35};
            case TURRET -> new int[] {34, 44, 41};
            case MINELAYER -> new int[] {46, 34, 31};
            case JELLY -> new int[] {36, 44, 32};
            case EEL -> new int[] {54, 28, 25};
            case SHIELDBEARER -> new int[] {52, 48, 45};
            case ENFORCER -> new int[] {50, 50, 47};
            case SEEKER -> new int[] {34, 30, 25};
            case SMUGGLER -> new int[] {38, 34, 30};
            default -> new int[] {32, 32, 30};
        };
    }

    private static int bob(Anim anim, int f) {
        return switch (anim) {
            case IDLE -> f >= 2 ? 1 : 0;
            case MOVE -> f % 3 == 1 ? 1 : 0;
            case WINDUP -> 1;
            case RECOVER -> 1;
            default -> 0;
        };
    }

    // --- Schrottläufer: rostige Kuppel auf vier Beinen ------------------------------------------

    private static void scuttler(Painter p, Anim anim, int f) {
        int bob = bob(anim, f);
        double lunge = anim == Anim.STRIKE ? 3 : anim == Anim.WINDUP ? -2 : 0;
        double cx = 17 + lunge, cy = 13 + bob + (anim == Anim.WINDUP ? 2 : 0);
        double[] attach = {cx - 6, cx - 2.5, cx + 2.5, cx + 6};
        for (int i = 0; i < 4; i++) {
            double phase = anim == Anim.MOVE ? Math.sin((f / 6.0 + i * .5) * Math.PI * 2) : 0;
            double side = i < 2 ? -1 : 1;
            double footX = attach[i] + side * 5 + phase * 2.5;
            double footY = 23 - Math.max(0, phase) * 2;
            double kneeX = attach[i] + side * 4, kneeY = cy - 3 - Math.abs(phase);
            int color = i % 2 == 0 ? Pal.STEEL_2 : Pal.STEEL_3;
            p.thick(attach[i], cy + 2, kneeX, kneeY, 2, color);
            p.thick(kneeX, kneeY, footX, footY, 2, color);
            p.px((int) kneeX, (int) kneeY, Pal.STEEL_5);
        }
        p.sphere(cx, cy, 8.5, 5.5, RUST);
        p.rect((int) cx - 7, (int) cy + 2, 15, 2, Pal.RUST_2);
        p.rect((int) cx - 5, (int) cy - 1, 1, 3, Pal.RUST_2);
        p.rect((int) cx + 1, (int) cy - 3, 1, 3, Pal.RUST_2);
        p.line((int) cx - 3, (int) cy - 5, (int) cx - 6, (int) cy - 10, Pal.STEEL_4);
        p.light((int) cx - 6, (int) cy - 11, Pal.RED_4);
        // Scheren
        double clawX = cx + 9 + (anim == Anim.STRIKE ? 4 : 0);
        double clawY = cy + (anim == Anim.WINDUP ? -4 : 1);
        p.thick(cx + 6, cy + 1, clawX, clawY, 2, Pal.STEEL_3);
        p.rect((int) clawX, (int) clawY - 2, 3, 1, Pal.STEEL_5);
        p.rect((int) clawX, (int) clawY + 1, 3, 1, Pal.STEEL_5);
        p.light((int) cx + 6, (int) cy - 1, anim == Anim.WINDUP ? Pal.RED_5 : Pal.RED_4);
        p.light((int) cx + 7, (int) cy - 1, Pal.RED_3);
        if (anim == Anim.HURT) p.light((int) cx + 6, (int) cy - 1, Pal.WHITE);
    }

    // --- Wachdrohne: Kugel mit Rotor und Linse ---------------------------------------------------

    private static void drone(Painter p, Anim anim, int f) {
        int bob = anim == Anim.IDLE || anim == Anim.MOVE ? (f % 2) : 0;
        double cx = 16, cy = 17 + bob;
        boolean wide = f % 2 == 0;
        p.rect(15, (int) cy - 10, 2, 3, Pal.STEEL_3);
        p.rect(wide ? 6 : 10, (int) cy - 11, wide ? 20 : 12, 1, Pal.STEEL_5);
        p.rect(wide ? 9 : 12, (int) cy - 12, wide ? 14 : 8, 1, Pal.STEEL_6);
        p.rect(7, (int) cy - 1, 3, 4, Pal.STEEL_3);
        p.sphere(cx, cy, 7, 6.5, STEEL);
        p.rect((int) cx - 6, (int) cy + 1, 13, 1, Pal.STEEL_3);
        p.rect((int) cx + 2, (int) cy + 5, 7, 2, Pal.STEEL_2);
        boolean charged = anim == Anim.WINDUP || anim == Anim.STRIKE;
        p.ellipse(cx + 3.5, cy - 1, 2.8, 2.8, Pal.INK);
        p.light((int) cx + 3, (int) cy - 2, charged ? Pal.RED_5 : Pal.RED_4);
        p.light((int) cx + 4, (int) cy - 2, charged ? Pal.WHITE : Pal.RED_4);
        p.light((int) cx + 4, (int) cy - 1, Pal.RED_3);
        p.light((int) cx + 3, (int) cy - 1, Pal.RED_3);
        if (charged) p.light((int) cx + 9, (int) cy + 5, Pal.RED_5);
        if (anim == Anim.HURT) p.light((int) cx - 2, (int) cy - 3, Pal.WHITE);
    }

    // --- Schottwächter: schwer gepanzerter Wächter mit Druckhammer ------------------------------

    private static void sentinel(Painter p, Anim anim, int f) {
        int bob = bob(anim, f);
        double hipX = 24, hipY = 33 + bob;
        double step = anim == Anim.MOVE ? Math.sin(f / 6.0 * Math.PI * 2) * 3 : 0;
        // Beine
        p.thick(hipX - 3, hipY, hipX - 4 - step, 45, 5, Pal.STEEL_2);
        p.rect((int) (hipX - 8 - step), 44, 8, 3, Pal.STEEL_1);
        p.thick(hipX + 3, hipY, hipX + 4 + step, 45, 5, Pal.STEEL_3);
        p.rect((int) (hipX + step), 44, 8, 3, Pal.STEEL_2);
        p.rect((int) (hipX + step) + 6, 45, 2, 2, Pal.RED_2);
        // Hammer hinter dem Körper oder erhoben
        double handX, handY, angle;
        switch (anim) {
            case WINDUP -> {
                handX = hipX - 2;
                handY = hipY - 22;
                angle = -110;
            }
            case STRIKE -> {
                handX = hipX + 12;
                handY = hipY - 10;
                angle = 35;
            }
            case RECOVER -> {
                handX = hipX + 11;
                handY = hipY - 2;
                angle = 80;
            }
            default -> {
                handX = hipX + 9;
                handY = hipY - 6;
                angle = -40 + bob * 3;
            }
        }
        hammer(p, handX, handY, Math.toRadians(angle));
        // Rumpf
        p.box(
                (int) hipX - 8,
                (int) hipY - 16,
                16,
                17,
                new int[] {Pal.STEEL_2, Pal.STEEL_3, Pal.STEEL_5});
        p.rect((int) hipX - 6, (int) hipY - 13, 12, 5, Pal.STEEL_4);
        p.rect((int) hipX - 7, (int) hipY - 6, 14, 2, Pal.RED_2);
        p.px((int) hipX - 5, (int) hipY - 12, Pal.STEEL_7);
        p.px((int) hipX + 4, (int) hipY - 12, Pal.STEEL_6);
        p.rect((int) hipX - 8, (int) hipY - 1, 16, 2, Pal.STEEL_1);
        // Schulterplatte
        p.sphere(hipX - 6, hipY - 15, 5, 4, STEEL);
        // Helm mit rotem Schlitz
        p.box(
                (int) hipX - 4,
                (int) hipY - 26,
                11,
                10,
                new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_6});
        p.rect((int) hipX - 4, (int) hipY - 27, 10, 1, Pal.STEEL_5);
        p.rect((int) hipX, (int) hipY - 22, 8, 2, Pal.INK);
        boolean alert = anim == Anim.WINDUP || anim == Anim.STRIKE;
        for (int x = 1; x < 7; x++)
            p.light((int) hipX + x, (int) hipY - 22, alert ? Pal.RED_5 : Pal.RED_4);
        // Arm zur Hand
        p.thick(hipX + 4, hipY - 14, handX, handY, 4, Pal.STEEL_4);
        p.ellipse(handX, handY, 2.4, 2.4, Pal.STEEL_2);
    }

    private static void hammer(Painter p, double x, double y, double angle) {
        p.stamp(
                new String[] {
                    "..............kkkk",
                    "..............kkkk",
                    ".ddddddddddddddkkkr",
                    ".ddddddddddddddkkkr",
                    "..............kkkk",
                    "..............kkkk"
                },
                new Object[] {"d", Pal.STEEL_2, "k", Pal.STEEL_4, "r", Pal.RED_3},
                3,
                2.5,
                x,
                y,
                angle,
                false);
    }

    // --- Sicherheitsautomat: schlanker Wachroboter mit Schockstab ------------------------------

    private static void enforcer(Painter p, Anim anim, int f) {
        int bob = bob(anim, f);
        double lean = anim == Anim.STRIKE ? 4 : anim == Anim.WINDUP ? -2 : 0;
        double hipX = 22 + lean * .5, hipY = 31 + bob + (anim == Anim.WINDUP ? 2 : 0);
        double step = anim == Anim.MOVE ? Math.sin(f / 6.0 * Math.PI * 2) * 4 : 0;
        if (anim == Anim.STRIKE) step = 5;
        // Beine mit Kniegelenk
        p.thick(hipX - 2, hipY, hipX - 4 - step * .5, hipY + 8, 3, BLUE[0]);
        p.thick(hipX - 4 - step * .5, hipY + 8, hipX - 3 - step, 45, 3, BLUE[0]);
        p.rect((int) (hipX - 6 - step), 44, 6, 3, Pal.STEEL_1);
        p.thick(hipX + 2, hipY, hipX + 4 + step * .5, hipY + 8, 3, BLUE[1]);
        p.thick(hipX + 4 + step * .5, hipY + 8, hipX + 3 + step, 45, 3, BLUE[1]);
        p.rect((int) (hipX + 1 + step), 44, 6, 3, Pal.STEEL_2);
        p.px((int) (hipX + 4 + step * .5), (int) hipY + 8, BLUE[3]);
        // Schockstab
        double handX, handY, angle;
        switch (anim) {
            case WINDUP -> {
                handX = hipX - 7;
                handY = hipY - 16;
                angle = -150;
            }
            case STRIKE -> {
                handX = hipX + 14 + lean;
                handY = hipY - 12;
                angle = 0;
            }
            case RECOVER -> {
                handX = hipX + 9;
                handY = hipY - 5;
                angle = 50;
            }
            default -> {
                handX = hipX + 7;
                handY = hipY - 8;
                angle = 70 + bob * 4;
            }
        }
        double tipX = handX + Math.cos(Math.toRadians(angle)) * 13;
        double tipY = handY + Math.sin(Math.toRadians(angle)) * 13;
        p.thick(handX, handY, tipX, tipY, 2, Pal.STEEL_3);
        boolean charged = anim == Anim.WINDUP || anim == Anim.STRIKE;
        p.light((int) tipX, (int) tipY, charged ? Pal.WHITE : Pal.TEAL_5);
        p.light((int) tipX + 1, (int) tipY, Pal.TEAL_5);
        p.light((int) tipX, (int) tipY + 1, Pal.TEAL_4);
        if (charged)
            for (int k = 0; k < 3; k++)
                p.light(
                        (int) tipX + (k - 1) * 2 + (f % 2),
                        (int) tipY - 2 + ((k + f) % 3),
                        Pal.TEAL_6);
        // Rumpf mit Brustpanzer und Warnstreifen
        p.box(
                (int) (hipX - 7 + lean * .5),
                (int) hipY - 17,
                14,
                18,
                new int[] {BLUE[0], BLUE[1], BLUE[2]});
        p.rect((int) (hipX - 5 + lean * .5), (int) hipY - 14, 10, 6, BLUE[2]);
        p.rect((int) (hipX - 5 + lean * .5), (int) hipY - 14, 10, 1, BLUE[3]);
        for (int x = 0; x < 12; x += 3)
            p.rect((int) (hipX - 6 + lean * .5) + x, (int) hipY - 3, 2, 2, Pal.RUST_6);
        p.light((int) (hipX - 3 + lean * .5), (int) hipY - 11, Pal.TEAL_4);
        // Schulter und Arm
        p.sphere(hipX + 3 + lean * .5, hipY - 16, 4, 3.2, BLUE);
        p.thick(hipX + 3 + lean * .5, hipY - 14, handX, handY, 3, BLUE[1]);
        p.ellipse(handX, handY, 2, 2, Pal.STEEL_2);
        // Kopf mit Visierband
        double headX = hipX + lean, headY = hipY - 24;
        p.box((int) headX - 5, (int) headY - 5, 11, 9, new int[] {BLUE[0], BLUE[2], BLUE[3]});
        p.rect((int) headX - 5, (int) headY - 1, 11, 2, Pal.INK);
        for (int x = -3; x < 6; x++)
            p.light((int) headX + x, (int) headY - 1, charged ? Pal.RUST_7 : Pal.RUST_6);
        p.line((int) headX - 2, (int) headY - 6, (int) headX - 3, (int) headY - 10, Pal.STEEL_4);
        p.light((int) headX - 3, (int) headY - 11, Pal.RED_4);
        if (anim == Anim.HURT) p.light((int) headX, (int) headY - 3, Pal.WHITE);
    }

    // --- Suchlichtsonde: flache Scheibe mit grossem Scheinwerfer --------------------------------

    private static void seeker(Painter p, Anim anim, int f) {
        int bob = anim == Anim.IDLE || anim == Anim.MOVE ? (f % 2) : 0;
        double cx = 16, cy = 14 + bob;
        boolean charged = anim == Anim.WINDUP || anim == Anim.STRIKE;
        // Antenne und Stabilisatoren
        p.line((int) cx - 4, (int) cy - 6, (int) cx - 7, (int) cy - 12, Pal.STEEL_4);
        p.light((int) cx - 7, (int) cy - 13, f % 2 == 0 ? Pal.RED_4 : Pal.RED_3);
        p.rect((int) cx - 13, (int) cy - 1, 4, 2, Pal.STEEL_3);
        p.rect((int) cx - 14, (int) cy - 3, 2, 6, Pal.STEEL_2);
        // Körper
        p.sphere(cx, cy, 10, 5.5, STEEL);
        p.rect((int) cx - 9, (int) cy + 2, 19, 1, Pal.STEEL_2);
        p.rect((int) cx - 6, (int) cy - 5, 9, 1, Pal.STEEL_6);
        // Scheinwerfer vorn
        p.ellipse(cx + 7, cy, 4, 4, Pal.STEEL_1);
        p.ellipse(cx + 7.5, cy, 3, 3, Pal.INK);
        int lens = charged ? Pal.WHITE : 0xFFFFF0B8;
        for (int y = -2; y <= 2; y++)
            for (int x = -2; x <= 2; x++)
                if (x * x + y * y <= 5) p.light((int) cx + 8 + x, (int) cy + y, lens);
        if (charged) p.light((int) cx + 11, (int) cy, Pal.RED_5);
        // Düsen unten
        int jet = f % 2 == 0 ? Pal.TEAL_5 : Pal.TEAL_4;
        p.light((int) cx - 4, (int) cy + 5, jet);
        p.light((int) cx + 2, (int) cy + 5, jet);
        if (anim == Anim.MOVE) {
            p.light((int) cx - 4, (int) cy + 6, Pal.TEAL_3);
            p.light((int) cx + 2, (int) cy + 6, Pal.TEAL_3);
        }
        if (anim == Anim.HURT) p.light((int) cx - 3, (int) cy - 2, Pal.WHITE);
    }

    // --- Schmugglerdrohne: Rotoren, Frachtkiste, Goldlicht --------------------------------------

    private static void smuggler(Painter p, Anim anim, int f) {
        int bob = f % 2;
        double cx = 19, cy = 12 + bob;
        boolean wide = f % 2 == 0;
        for (int side = -1; side <= 1; side += 2) {
            int rx = (int) cx + side * 10;
            p.rect(rx - 1, (int) cy - 6, 2, 4, Pal.STEEL_3);
            p.rect(rx - (wide ? 6 : 3), (int) cy - 7, wide ? 12 : 6, 1, Pal.STEEL_6);
        }
        p.sphere(cx, cy, 8, 5, STEEL);
        p.rect((int) cx - 7, (int) cy + 2, 15, 1, Pal.STEEL_2);
        p.light((int) cx + 4, (int) cy - 1, Pal.RUST_6);
        p.light((int) cx + 5, (int) cy - 1, f % 2 == 0 ? Pal.RUST_7 : Pal.RUST_5);
        // Frachtkiste an Seilen
        p.line((int) cx - 4, (int) cy + 3, (int) cx - 5, (int) cy + 9, Pal.STEEL_4);
        p.line((int) cx + 4, (int) cy + 3, (int) cx + 5, (int) cy + 9, Pal.STEEL_4);
        p.box((int) cx - 7, (int) cy + 9, 14, 9, new int[] {Pal.RUST_2, Pal.RUST_3, Pal.RUST_5});
        p.rect((int) cx - 7, (int) cy + 12, 14, 1, Pal.RUST_1);
        p.light((int) cx, (int) cy + 14, Pal.RUST_6);
        if (anim == Anim.HURT) p.light((int) cx - 3, (int) cy - 2, Pal.WHITE);
    }

    // --- Kugelbombe ------------------------------------------------------------------------------

    private static void bomber(Painter p, Anim anim, int f) {
        double cx = 13, cy = 13;
        p.sphere(cx, cy, 7.5, 7.5, DARK);
        double phase = anim == Anim.MOVE ? f / 6.0 : f / 4.0 * .2;
        for (int k = 0; k < 2; k++) {
            double a = (phase + k * .5) * Math.PI * 2;
            double sx = Math.sin(a);
            if (Math.cos(a) < 0) continue;
            int x = (int) Math.round(cx + sx * 6);
            for (int y = -5; y <= 5; y++)
                if (Math.hypot(x - cx, y) < 7) p.light(x, (int) cy + y, Pal.RUST_5);
        }
        p.rect((int) cx - 1, (int) cy - 10, 2, 3, Pal.STEEL_3);
        boolean blink = anim == Anim.WINDUP && f % 2 == 0 || anim == Anim.STRIKE;
        p.light((int) cx, (int) cy - 11, blink ? Pal.WHITE : Pal.RED_4);
        p.light((int) cx + 3, (int) cy - 2, blink ? Pal.RED_5 : Pal.RED_3);
        p.light((int) cx + 4, (int) cy - 2, blink ? Pal.RED_5 : Pal.RED_3);
        if (blink)
            for (int y = -6; y <= 6; y += 3)
                for (int x = -6; x <= 6; x += 3)
                    if (Math.hypot(x, y) < 6.5) p.light((int) cx + x, (int) cy + y, Pal.RED_3);
    }

    // --- Schweissroboter -----------------------------------------------------------------------

    private static void welder(Painter p, Anim anim, int f) {
        int bob = bob(anim, f);
        p.box(9, 28, 24, 7, new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_3});
        for (int i = 0; i < 4; i++) {
            int wx = 12 + i * 6 + (anim == Anim.MOVE ? f % 2 : 0);
            p.ellipse(wx, 31.5, 2, 2, Pal.STEEL_4);
            p.px(wx, 31, Pal.STEEL_1);
        }
        p.box(12, 15 + bob, 18, 13, new int[] {Pal.RUST_2, Pal.RUST_4, Pal.RUST_5});
        for (int x = 0; x < 18; x += 4) {
            p.rect(12 + x, 24 + bob, 2, 3, Pal.INK);
        }
        p.rect(12, 23 + bob, 18, 1, Pal.RUST_2);
        p.sphere(19, 12 + bob, 5.5, 4.5, STEEL);
        p.rect(19, 11 + bob, 6, 2, Pal.INK);
        for (int x = 19; x < 25; x++) p.light(x, 11 + bob, Pal.GREEN_4);
        p.rect(8, 16 + bob, 4, 8, Pal.STEEL_3);
        p.rect(7, 15 + bob, 2, 3, Pal.RED_2);
        double reach = anim == Anim.STRIKE ? 13 : anim == Anim.WINDUP ? 7 : 9;
        p.thick(27, 19 + bob, 27 + reach * .6, 21 + bob, 3, Pal.STEEL_4);
        p.thick(27 + reach * .6, 21 + bob, 29 + reach, 20 + bob, 3, Pal.STEEL_3);
        int nx = (int) (30 + reach);
        p.rect(nx - 1, 19 + bob, 3, 3, Pal.STEEL_5);
        boolean hot = anim == Anim.WINDUP || anim == Anim.STRIKE;
        p.light(nx + 2, 20 + bob, hot ? 0xFFBFE8FF : Pal.RUST_5);
        if (hot) p.light(nx + 3, 20 + bob, Pal.WHITE);
    }

    // --- Geschützturm an der Decke ---------------------------------------------------------------

    private static void turret(Painter p, Anim anim, int f) {
        p.box(8, 0, 18, 4, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.rect(15, 4, 4, 16, Pal.STEEL_2);
        p.rect(16, 4, 1, 16, Pal.STEEL_4);
        p.rect(13, 12, 8, 3, Pal.STEEL_3);
        double cx = 17, cy = 30;
        double recoil = anim == Anim.STRIKE && f == 0 ? -2 : 0;
        p.thick(cx + 3, cy + 2, cx + 13 + recoil, cy + 8, 3, Pal.STEEL_2);
        p.rect((int) (cx + 12 + recoil), (int) cy + 7, 3, 3, Pal.STEEL_4);
        p.sphere(cx, cy, 8, 7, STEEL);
        p.rect((int) cx - 8, (int) cy - 1, 17, 2, Pal.STEEL_3);
        boolean charged = anim == Anim.WINDUP || anim == Anim.STRIKE;
        p.ellipse(cx + 3, cy + 2, 2.6, 2.6, Pal.INK);
        p.light((int) cx + 3, (int) cy + 1, charged ? Pal.WHITE : Pal.RED_4);
        p.light((int) cx + 4, (int) cy + 2, Pal.RED_4);
        p.light((int) cx + 2, (int) cy + 2, Pal.RED_3);
        if (anim == Anim.STRIKE && f == 0) {
            p.light((int) (cx + 16), (int) cy + 8, Pal.RUST_7);
            p.light((int) (cx + 17), (int) cy + 9, Pal.RUST_6);
        }
    }

    // --- Minenleger ----------------------------------------------------------------------------

    private static void minelayer(Painter p, Anim anim, int f) {
        int bob = bob(anim, f);
        p.box(5, 24, 36, 7, new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_3});
        for (int i = 0; i < 6; i++)
            p.ellipse(9 + i * 6 + (anim == Anim.MOVE ? f % 2 : 0), 27.5, 2, 2, Pal.STEEL_4);
        p.box(8, 13 + bob, 28, 11, new int[] {0xFF2F3B2A, 0xFF4C5E3C, 0xFF6F8757});
        p.rect(8, 19 + bob, 28, 1, 0xFF2F3B2A);
        // Minen auf dem Heck
        for (int i = 0; i < 3; i++) {
            p.sphere(12 + i * 5, 10 + bob, 2.4, 2.4, DARK);
            p.light(12 + i * 5, 9 + bob, Pal.RED_4);
        }
        double angle = anim == Anim.STRIKE ? -.7 : anim == Anim.WINDUP ? -1.0 : -.85;
        double bx = 30, by = 13 + bob;
        p.thick(bx, by, bx + Math.cos(angle) * 11, by + Math.sin(angle) * 11, 4, Pal.STEEL_3);
        p.px((int) (bx + Math.cos(angle) * 11), (int) (by + Math.sin(angle) * 11), Pal.INK);
        p.rect(30, 16 + bob, 6, 3, Pal.INK);
        for (int x = 31; x < 36; x++)
            p.light(x, 17 + bob, anim == Anim.WINDUP ? Pal.RUST_7 : Pal.RUST_5);
    }

    // --- Leuchtqualle --------------------------------------------------------------------------

    private static void jelly(Painter p, Anim anim, int f) {
        double pulse = anim == Anim.IDLE || anim == Anim.MOVE ? Math.sin(f / 4.0 * Math.PI * 2) : 0;
        double cx = 18, cy = 12 - pulse;
        boolean charged = anim == Anim.WINDUP || anim == Anim.STRIKE;
        for (int t = 0; t < 5; t++) {
            double x0 = cx - 7 + t * 3.5;
            double prevX = x0, prevY = cy + 5;
            for (int s = 1; s <= 8; s++) {
                double y = cy + 5 + s * 2.6;
                double x = x0 + Math.sin(s * .8 + f * 1.3 + t) * (1 + s * .25);
                p.line(
                        (int) prevX,
                        (int) prevY,
                        (int) x,
                        (int) y,
                        t % 2 == 0 ? Pal.VIOLET_2 : Pal.VIOLET_3);
                prevX = x;
                prevY = y;
            }
            p.light((int) prevX, (int) prevY, charged ? Pal.TEAL_6 : Pal.TEAL_5);
        }
        int[] bell =
                charged
                        ? new int[] {Pal.VIOLET_2, Pal.VIOLET_3, Pal.VIOLET_4, Pal.VIOLET_5}
                        : new int[] {Pal.VIOLET_1, Pal.VIOLET_2, Pal.VIOLET_3, Pal.VIOLET_4};
        p.sphere(cx, cy, 10 + pulse * .6, 7.5, bell);
        for (int x = (int) cx - 10; x <= cx + 10; x++) p.px(x, (int) cy + 5, 0);
        for (int x = (int) cx - 9; x <= cx + 9; x += 2) p.px(x, (int) cy + 4, Pal.VIOLET_4);
        p.ellipse(cx, cy, 4, 3, Pal.VIOLET_1);
        p.light((int) cx, (int) cy, charged ? Pal.WHITE : Pal.TEAL_6);
        p.light((int) cx - 1, (int) cy, Pal.TEAL_5);
        p.light((int) cx + 1, (int) cy - 1, Pal.TEAL_5);
        int[][] spots = {{-6, -2}, {5, -3}, {-3, -5}, {7, 1}, {-8, 2}};
        for (int[] s : spots)
            p.light((int) cx + s[0], (int) cy + s[1], charged ? Pal.TEAL_6 : Pal.TEAL_4);
    }

    // --- Tiefseeaal ------------------------------------------------------------------------------

    private static void eel(Painter p, Anim anim, int f) {
        double phase = f * .9 + (anim == Anim.STRIKE ? 1.5 : 0);
        double headX = 42, headY = 15;
        double[] xs = new double[12], ys = new double[12];
        for (int i = 0; i < 12; i++) {
            xs[i] = headX - i * 3.2;
            ys[i] =
                    headY
                            + Math.sin(phase + i * .6)
                                    * (anim == Anim.RECOVER ? 4 : 2.2)
                                    * (i / 11.0 + .2);
        }
        for (int i = 11; i >= 0; i--) {
            double r = 1.5 + (11 - i) * .38;
            p.sphere(
                    xs[i], ys[i], r, r, new int[] {Pal.TEAL_0, Pal.TEAL_1, Pal.TEAL_2, Pal.TEAL_3});
        }
        p.thick(xs[11], ys[11], xs[11] - 5, ys[11] - 3, 2, Pal.TEAL_1);
        p.thick(xs[11], ys[11], xs[11] - 5, ys[11] + 3, 2, Pal.TEAL_1);
        for (int i = 2; i < 11; i += 2) p.light((int) xs[i], (int) ys[i] + 2, Pal.GREEN_4);
        boolean open = anim == Anim.WINDUP || anim == Anim.STRIKE;
        p.sphere(
                headX + 2,
                headY,
                5.5,
                4.5,
                new int[] {Pal.TEAL_0, Pal.TEAL_1, Pal.TEAL_2, Pal.TEAL_4});
        if (open) {
            p.poly(
                    new double[] {headX + 2, headX + 9, headX + 9},
                    new double[] {headY, headY - 4, headY + 4},
                    0);
            p.rect((int) headX + 3, (int) headY - 1, 5, 2, Pal.RED_1);
            p.px((int) headX + 5, (int) headY - 2, Pal.BONE);
            p.px((int) headX + 7, (int) headY - 3, Pal.BONE);
            p.px((int) headX + 5, (int) headY + 2, Pal.BONE);
            p.px((int) headX + 7, (int) headY + 3, Pal.BONE);
        } else p.rect((int) headX + 4, (int) headY + 1, 4, 1, Pal.INK);
        p.light((int) headX + 3, (int) headY - 2, Pal.RUST_6);
        p.light((int) headX + 3, (int) headY + 5, Pal.GREEN_5);
    }

    // --- Schildträger ----------------------------------------------------------------------------

    private static void shieldbearer(Painter p, Anim anim, int f) {
        int bob = bob(anim, f);
        double hipX = 22, hipY = 31 + bob;
        double step = anim == Anim.MOVE ? Math.sin(f / 6.0 * Math.PI * 2) * 3 : 0;
        p.thick(hipX - 3, hipY, hipX - 4 - step, 43, 5, 0xFF3A3024);
        p.rect((int) (hipX - 8 - step), 42, 8, 3, Pal.STEEL_1);
        p.thick(hipX + 3, hipY, hipX + 4 + step, 43, 5, 0xFF4E4130);
        p.rect((int) (hipX + step), 42, 8, 3, Pal.STEEL_2);
        p.box(
                (int) hipX - 7,
                (int) hipY - 15,
                14,
                16,
                new int[] {0xFF3A3024, 0xFF5C4B34, 0xFF7E6848});
        p.rect((int) hipX - 7, (int) hipY - 4, 14, 2, Pal.RUST_2);
        p.box(
                (int) hipX - 4,
                (int) hipY - 25,
                10,
                10,
                new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_6});
        p.rect((int) hipX + 1, (int) hipY - 21, 5, 2, Pal.INK);
        p.light((int) hipX + 2, (int) hipY - 21, Pal.RUST_6);
        p.light((int) hipX + 4, (int) hipY - 21, Pal.RUST_6);
        double shieldX = hipX + 8 + (anim == Anim.STRIKE ? 5 : anim == Anim.WINDUP ? -2 : 0);
        boolean lowered = anim == Anim.RECOVER;
        int top = (int) hipY - (lowered ? 14 : 26);
        p.box((int) shieldX, top, 9, 30, new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_6});
        for (int y = top + 3; y < top + 28; y += 6) {
            p.px((int) shieldX + 2, y, Pal.STEEL_7);
            p.px((int) shieldX + 6, y, Pal.STEEL_7);
        }
        for (int y = top + 20; y < top + 28; y += 2)
            p.rect((int) shieldX + 1, y, 7, 1, (y / 2) % 2 == 0 ? Pal.RUST_5 : Pal.INK);
        p.rect((int) shieldX + 3, top + 7, 3, 4, Pal.TEAL_1);
        p.light((int) shieldX + 4, top + 8, Pal.TEAL_5);
        p.thick(hipX + 3, hipY - 12, shieldX + 1, hipY - 10, 4, 0xFF5C4B34);
    }
}
