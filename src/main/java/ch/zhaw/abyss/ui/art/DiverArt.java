package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Swing;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Erzeugt die animierte Taucherfigur als Pixelgrafik aus einem kleinen Skelett: Posen legen Hüfte,
 * Hände und Füsse fest, Knie und Ellbogen folgen per Zweigelenk-Kinematik. Aussehen und Waffe sind
 * frei kombinierbar, weil jede Kombination beim Start neu gemalt wird.
 */
public final class DiverArt {
    /** Kantenlänge eines Einzelbilds. */
    public static final int SIZE = 48;

    /** Ankerpunkt: Fussmitte. */
    public static final int ANCHOR_X = 24, ANCHOR_Y = 44;

    /** Anzugfarben der Garderobe. */
    public static final int[] SUITS = {
        0xFFE0A526,
        0xFFE06A2A,
        0xFFB8322E,
        0xFF2E9C94,
        0xFF2F4F8C,
        0xFF5E7A34,
        0xFFD8D2C2,
        0xFF43345E
    };

    /** Namen der Anzugfarben. */
    public static final String[] SUIT_NAMES = {
        "Signalgelb",
        "Tieforange",
        "Rettungsrot",
        "Lagunentürkis",
        "Marineblau",
        "Seetangoliv",
        "Polarweiss",
        "Tintenviolett"
    };

    /** Visierfarben. */
    public static final int[] VISORS = {
        0xFF7FF5E6, 0xFFFFC857, 0xFF9CFF6E, 0xFFFF5E57, 0xFFD18CFF, 0xFFF4F7FF
    };

    /** Namen der Visierfarben. */
    public static final String[] VISOR_NAMES = {
        "Cyan", "Bernstein", "Algengrün", "Alarmrot", "Violett", "Weiss"
    };

    /** Namen der Helmformen. */
    public static final String[] HELMET_NAMES = {
        "Rundhelm", "Glaskuppel", "Panzerhelm", "Laternenhelm"
    };

    /** Namen der Metalltöne. */
    public static final String[] TRIM_NAMES = {"Messing", "Stahl", "Kupfer", "Brüniert"};

    private static final int[][] TRIMS = {
        {Pal.RUST_3, Pal.RUST_5, Pal.RUST_6, Pal.RUST_7},
        {Pal.STEEL_3, Pal.STEEL_5, Pal.STEEL_6, Pal.STEEL_7},
        {Pal.RUST_2, 0xFFB5582E, 0xFFDA8352, 0xFFF6B889},
        {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5}
    };

    /** Animationsarten der Figur. */
    public enum Anim {
        IDLE,
        RUN,
        JUMP,
        FALL,
        DASH,
        HURT,
        DOWN,
        SLASH,
        OVERHEAD,
        THRUST,
        PUNCH,
        UPPERCUT,
        SHOOT,
        AIR_SLASH,
        SLAM
    }

    private record Pose(
            double lean,
            int bob,
            double frontFootX,
            double frontFootY,
            double backFootX,
            double backFootY,
            double frontHandX,
            double frontHandY,
            double backHandX,
            double backHandY,
            double weaponAngle,
            double tilt) {}

    private DiverArt() {}

    /**
     * Malt alle Animationen einer Kombination.
     *
     * @param look Aussehen
     * @param weapon Waffe in der Hand
     * @param diver Klasse, bestimmt die Statur
     * @return Einzelbilder je Animation
     */
    public static Map<Anim, List<Sprite>> build(Cosmetics look, Weapon weapon, DiverClass diver) {
        var result = new EnumMap<Anim, List<Sprite>>(Anim.class);
        for (var anim : Anim.values()) {
            var frames = new ArrayList<Sprite>();
            for (var pose : poses(anim)) frames.add(paint(look, weapon, diver, pose, anim));
            result.put(anim, List.copyOf(frames));
        }
        return result;
    }

    /**
     * @param style Angriffsart aus der Domäne
     * @return passende Animation
     */
    public static Anim of(Swing.Style style) {
        return switch (style) {
            case SLASH -> Anim.SLASH;
            case OVERHEAD -> Anim.OVERHEAD;
            case THRUST -> Anim.THRUST;
            case PUNCH -> Anim.PUNCH;
            case UPPERCUT -> Anim.UPPERCUT;
            case SHOOT -> Anim.SHOOT;
            case AIR_SLASH -> Anim.AIR_SLASH;
            case SLAM -> Anim.SLAM;
        };
    }

    private static List<Pose> poses(Anim anim) {
        var list = new ArrayList<Pose>();
        switch (anim) {
            case IDLE -> {
                int[] bobs = {0, 0, 1, 1};
                for (int i = 0; i < 4; i++) {
                    int b = bobs[i];
                    list.add(new Pose(0, b, 27, 44, 21, 44, 29, 31 + b, 20, 31 + b, 55, 0));
                }
            }
            case RUN -> {
                for (int i = 0; i < 8; i++) {
                    double t = i / 8.0 * Math.PI * 2;
                    double ff = Math.cos(t), bf = Math.cos(t + Math.PI);
                    int bob = (i % 4 == 1 || i % 4 == 2) ? 1 : 0;
                    list.add(
                            new Pose(
                                    1,
                                    -bob + 1,
                                    24 + 6 * ff,
                                    44 - Math.max(0, 3.2 * Math.sin(t)),
                                    24 + 6 * bf,
                                    44 - Math.max(0, 3.2 * Math.sin(t + Math.PI)),
                                    27 - 4 * ff,
                                    30 + Math.abs(ff) * -1,
                                    22 + 4 * ff,
                                    30,
                                    35 - 25 * ff,
                                    .05));
                }
            }
            case JUMP -> list.add(new Pose(0, -1, 28, 40, 20, 41, 30, 26, 19, 27, 20, 0));
            case FALL -> list.add(new Pose(0, 0, 27, 44, 20, 43, 31, 24, 17, 25, -10, 0));
            case DASH -> {
                list.add(new Pose(3, 1, 27, 43, 15, 42, 33, 29, 17, 28, 10, .25));
                list.add(new Pose(3, 1, 28, 44, 14, 43, 33, 30, 16, 29, 10, .25));
            }
            case HURT -> {
                list.add(new Pose(-2, 0, 27, 44, 20, 44, 26, 24, 17, 25, -40, -.2));
                list.add(new Pose(-1, 0, 27, 44, 20, 44, 27, 27, 18, 27, -10, -.1));
            }
            case DOWN -> list.add(new Pose(0, 9, 32, 44, 27, 44, 33, 42, 19, 42, 90, .0));
            case SLASH -> {
                list.add(new Pose(-1, 0, 28, 44, 20, 44, 20, 22, 19, 29, -125, -.1));
                list.add(new Pose(2, 1, 30, 44, 19, 44, 34, 28, 22, 30, 5, .15));
                list.add(new Pose(1, 1, 29, 44, 20, 44, 31, 34, 21, 30, 55, .08));
            }
            case OVERHEAD -> {
                list.add(new Pose(-1, 0, 27, 44, 20, 44, 24, 15, 21, 20, -100, -.15));
                list.add(new Pose(2, 1, 30, 44, 19, 44, 34, 25, 25, 28, -15, .15));
                list.add(new Pose(2, 2, 30, 44, 19, 44, 32, 35, 24, 33, 70, .2));
            }
            case THRUST -> {
                list.add(new Pose(-1, 0, 27, 44, 20, 44, 21, 28, 19, 29, 0, -.05));
                list.add(new Pose(3, 1, 31, 44, 18, 44, 37, 28, 26, 29, 0, .15));
                list.add(new Pose(1, 0, 29, 44, 20, 44, 33, 29, 22, 30, 4, .05));
            }
            case PUNCH -> {
                list.add(new Pose(-1, 0, 27, 44, 20, 44, 23, 27, 25, 26, 0, -.05));
                list.add(new Pose(3, 1, 31, 44, 19, 44, 37, 26, 21, 28, 0, .15));
                list.add(new Pose(1, 0, 29, 44, 20, 44, 32, 28, 22, 28, 0, .05));
            }
            case UPPERCUT -> {
                list.add(new Pose(0, 1, 28, 44, 20, 44, 27, 34, 21, 27, 60, 0));
                list.add(new Pose(2, -1, 29, 44, 20, 44, 33, 17, 22, 27, -80, .1));
                list.add(new Pose(1, 0, 29, 44, 20, 44, 31, 20, 21, 28, -70, .05));
            }
            case SHOOT -> {
                list.add(new Pose(0, 0, 28, 44, 20, 44, 32, 27, 27, 28, 0, 0));
                list.add(new Pose(-2, 0, 28, 44, 20, 44, 29, 27, 24, 28, -8, -.1));
                list.add(new Pose(-1, 0, 28, 44, 20, 44, 31, 27, 26, 28, -3, -.05));
            }
            case AIR_SLASH -> {
                list.add(new Pose(-1, -1, 28, 40, 20, 41, 21, 20, 19, 26, -140, -.1));
                list.add(new Pose(2, -1, 29, 41, 20, 41, 34, 31, 22, 27, 55, .15));
                list.add(new Pose(1, -1, 28, 41, 20, 41, 29, 36, 21, 28, 110, .1));
            }
            case SLAM -> {
                list.add(new Pose(0, -1, 28, 40, 20, 40, 24, 13, 22, 16, -90, 0));
                list.add(new Pose(2, 2, 30, 44, 19, 44, 32, 38, 25, 35, 85, .25));
            }
        }
        return list;
    }

    private static Sprite paint(
            Cosmetics look, Weapon weapon, DiverClass diver, Pose pose, Anim anim) {
        var p = new Painter(SIZE, SIZE);
        int[] suit = Pal.ramp(SUITS[look.suit()]);
        int[] trim = TRIMS[look.trim()];
        int visor = VISORS[look.visor()];
        boolean heavy = diver == DiverClass.TITAN;
        double hipX = 24 + pose.lean, hipY = 31 + pose.bob;
        if (anim == Anim.DOWN) return down(p, suit, trim, visor);
        double shoulderY = hipY - 9;
        double frontShoulderX = hipX + 3 + pose.tilt * 6, backShoulderX = hipX - 3 + pose.tilt * 6;
        int legWidth = heavy ? 5 : 4;

        // Hinteres Bein und hinterer Arm liegen im Schatten.
        leg(
                p,
                hipX - 1.5,
                hipY,
                pose.backFootX,
                pose.backFootY,
                legWidth,
                Pal.shade(suit[0], .9),
                Pal.STEEL_1,
                trim[0]);
        arm(
                p,
                backShoulderX,
                shoulderY,
                pose.backHandX,
                pose.backHandY,
                suit[0],
                Pal.STEEL_1,
                heavy);

        // Atemflaschen auf dem Rücken.
        double tankX = hipX - 9 + pose.tilt * 5;
        tanks(p, tankX, hipY - 13, trim, heavy);

        if (diver == DiverClass.SPARK) antenna(p, tankX + 2, hipY - 14, anim);
        if (diver == DiverClass.HARPOONER) scarf(p, hipX - 4 + pose.tilt * 6, hipY - 11, pose);

        // Rumpf mit Brustplatte und Gürtel.
        torso(p, hipX, hipY, suit, trim, pose.tilt, heavy);
        accessory(p, diver, hipX + pose.tilt * 3, hipY, trim);

        leg(
                p,
                hipX + 1.5,
                hipY,
                pose.frontFootX,
                pose.frontFootY,
                legWidth,
                suit[1],
                Pal.STEEL_2,
                trim[1]);

        // Helm sitzt leicht vor der Körpermitte.
        double helmetX = hipX + 1 + pose.tilt * 9, helmetY = hipY - 16;
        helmet(p, look.helmet(), helmetX, helmetY, trim, visor, suit);

        // Waffe hinter der vorderen Hand, danach Arm und Handschuh.
        weapon(p, weapon, pose.frontHandX, pose.frontHandY, Math.toRadians(pose.weaponAngle), anim);
        arm(
                p,
                frontShoulderX,
                shoulderY,
                pose.frontHandX,
                pose.frontHandY,
                suit[1],
                Pal.STEEL_2,
                heavy);
        if (weapon == Weapon.TESLA) teslaGlove(p, pose.frontHandX, pose.frontHandY);
        else glove(p, pose.frontHandX, pose.frontHandY);

        p.outline(Pal.OUTLINE);
        return p.sprite(ANCHOR_X, ANCHOR_Y);
    }

    private static void leg(
            Painter p,
            double hipX,
            double hipY,
            double footX,
            double footY,
            int width,
            int color,
            int boot,
            int trim) {
        double ankleX = footX, ankleY = footY - 3;
        double[] knee = joint(hipX, hipY, ankleX, ankleY, 6.6, 1);
        p.thick(hipX, hipY, knee[0], knee[1], width, color);
        p.thick(knee[0], knee[1], ankleX, ankleY, width - 1, color);
        // Taucherstiefel mit schwerer Sohle und Messingkappe.
        int bx = (int) Math.round(footX - 3), by = (int) Math.round(footY - 4);
        p.rect(bx, by, 6, 3, boot);
        p.rect(bx, by + 3, 7, 1, Pal.STEEL_0);
        p.rect(bx + 5, by + 1, 2, 2, trim);
        p.px(bx + 1, by, Pal.shade(boot, 1.6));
    }

    private static void arm(
            Painter p,
            double shoulderX,
            double shoulderY,
            double handX,
            double handY,
            int color,
            int cuff,
            boolean heavy) {
        double[] elbow = joint(shoulderX, shoulderY, handX, handY, 5.2, -1);
        p.thick(shoulderX, shoulderY, elbow[0], elbow[1], heavy ? 4 : 3, color);
        p.thick(elbow[0], elbow[1], handX, handY, heavy ? 4 : 3, color);
        p.thick(
                handX - (handX - elbow[0]) * .25,
                handY - (handY - elbow[1]) * .25,
                handX,
                handY,
                3,
                cuff);
    }

    private static void glove(Painter p, double x, double y) {
        p.ellipse(x, y, 1.8, 1.8, Pal.STEEL_2);
        p.px((int) Math.floor(x) - 1, (int) Math.floor(y) - 1, Pal.STEEL_4);
    }

    private static void teslaGlove(Painter p, double x, double y) {
        p.ellipse(x, y, 2.6, 2.4, Pal.STEEL_3);
        p.rect((int) x - 2, (int) y - 3, 5, 1, Pal.RUST_5);
        p.light((int) x + 2, (int) y - 1, Pal.TEAL_6);
        p.light((int) x + 3, (int) y + 1, Pal.TEAL_5);
        p.light((int) x, (int) y - 4, Pal.TEAL_5);
    }

    private static double[] joint(
            double ax, double ay, double bx, double by, double segment, int bend) {
        double dx = bx - ax, dy = by - ay, d = Math.hypot(dx, dy);
        double mx = (ax + bx) / 2, my = (ay + by) / 2;
        if (d >= segment * 2 || d < .01) return new double[] {mx, my};
        double h = Math.sqrt(segment * segment - (d / 2) * (d / 2));
        double nx = -dy / d, ny = dx / d;
        if (nx * bend < 0) {
            nx = -nx;
            ny = -ny;
        }
        return new double[] {mx + nx * h, my + ny * h};
    }

    private static void tanks(Painter p, double x, double y, int[] trim, boolean heavy) {
        int tx = (int) Math.round(x), ty = (int) Math.round(y);
        int w = heavy ? 5 : 4;
        p.box(tx, ty + 1, w, 11, new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5});
        p.box(tx + 1, ty, w, 11, new int[] {Pal.STEEL_3, Pal.STEEL_5, Pal.STEEL_6});
        p.rect(tx + 1, ty - 1, 2, 1, trim[1]);
        p.px(tx + 2, ty - 2, Pal.RED_3);
        p.rect(tx, ty + 5, w + 1, 1, trim[0]);
    }

    private static void torso(
            Painter p,
            double hipX,
            double hipY,
            int[] suit,
            int[] trim,
            double tilt,
            boolean heavy) {
        int w = heavy ? 13 : 11;
        int x = (int) Math.round(hipX - w / 2.0 + tilt * 3), y = (int) Math.round(hipY - 11);
        p.rect(x + 1, y, w - 2, 12, suit[1]);
        p.rect(x, y + 1, w, 10, suit[1]);
        p.rect(x + 1, y + 1, 2, 8, suit[2]);
        p.rect(x + w - 2, y + 2, 1, 9, suit[0]);
        // Brustplatte mit Nieten.
        p.box(x + 3, y + 2, w - 5, 5, trim);
        p.px(x + 4, y + 3, trim[3]);
        p.px(x + w - 4, y + 3, trim[0]);
        if (heavy) p.rect(x + 2, y + 7, w - 4, 1, trim[0]);
        // Gürtel mit Schnalle.
        p.rect(x, y + 10, w, 2, Pal.RUST_1);
        p.rect(x + w / 2, y + 10, 2, 2, trim[2]);
    }

    private static void antenna(Painter p, double x, double y, Anim anim) {
        int ax = (int) Math.round(x), ay = (int) Math.round(y);
        p.line(ax, ay, ax - 2, ay - 11, Pal.STEEL_4);
        p.light(ax - 2, ay - 12, Pal.RED_4);
        p.px(ax - 1, ay - 6, Pal.STEEL_5);
        p.px(ax - 3, ay - 8, Pal.STEEL_5);
    }

    private static void scarf(Painter p, double x, double y, Pose pose) {
        double wave = pose.bob * .8 + pose.lean * .5;
        for (int i = 0; i < 9; i++) {
            double sx = x - i * 1.1 - Math.max(0, pose.lean) * i * .25;
            double sy = y + i * .55 + Math.sin(i * .9 + wave) * .8;
            p.rect(
                    (int) Math.round(sx),
                    (int) Math.round(sy),
                    2,
                    2,
                    i % 3 == 0 ? Pal.RED_3 : Pal.RED_2);
        }
    }

    private static void accessory(
            Painter p, DiverClass diver, double hipX, double hipY, int[] trim) {
        int x = (int) Math.round(hipX), y = (int) Math.round(hipY);
        switch (diver) {
            case MECHANIC -> {
                p.rect(x - 6, y - 2, 3, 3, Pal.RUST_2);
                p.rect(x + 3, y - 2, 3, 3, Pal.RUST_2);
                p.px(x - 5, y - 2, Pal.RUST_3);
            }
            case WELDER -> {
                p.rect(x - 3, y - 7, 7, 9, Pal.RUST_1);
                p.rect(x - 3, y - 7, 7, 1, Pal.RUST_3);
                p.px(x, y - 4, Pal.RUST_5);
                p.px(x + 1, y - 3, Pal.RUST_4);
            }
            case TITAN -> {
                p.sphere(x - 5, y - 10, 3.5, 3, trim);
                p.sphere(x + 5, y - 10, 3.5, 3, trim);
            }
            case HARPOONER -> p.rect(x - 5, y - 9, 11, 1, Pal.RUST_2);
            case SPARK -> {
                p.rect(x + 2, y - 6, 3, 3, Pal.STEEL_2);
                p.light(x + 3, y - 5, Pal.TEAL_5);
            }
        }
    }

    private static void helmet(
            Painter p, int style, double cx, double cy, int[] trim, int visor, int[] suit) {
        int visorDark = Pal.mix(visor, Pal.INK, .45);
        switch (style) {
            case 1 -> {
                // Glaskuppel: dunkles Glas mit Glanzlicht, darin das leuchtende Gesichtsfeld.
                p.rect((int) cx - 6, (int) cy + 4, 13, 3, trim[1]);
                p.rect((int) cx - 6, (int) cy + 6, 13, 1, trim[0]);
                p.sphere(
                        cx,
                        cy - 1,
                        7.5,
                        7,
                        new int[] {Pal.TEAL_0, Pal.TEAL_1, Pal.TEAL_2, Pal.TEAL_5});
                p.ellipse(cx + 1.5, cy, 3.5, 3, visorDark);
                p.light((int) cx + 2, (int) cy - 1, visor);
                p.light((int) cx + 3, (int) cy - 1, visor);
                p.light((int) cx + 2, (int) cy, Pal.mix(visor, Pal.WHITE, .4));
                p.px((int) cx - 3, (int) cy - 5, Pal.TEAL_6);
                p.px((int) cx - 4, (int) cy - 4, Pal.TEAL_5);
                p.px((int) cx - 2, (int) cy - 6, Pal.TEAL_5);
                lamp(p, cx + 1, cy - 8);
            }
            case 2 -> {
                // Panzerhelm: kantig mit durchgehendem Sehschlitz.
                double[] xs = {cx - 7, cx + 5, cx + 8, cx + 8, cx - 7};
                double[] ys = {cy - 6, cy - 7, cy - 3, cy + 6, cy + 6};
                p.poly(xs, ys, trim[1]);
                p.rect((int) cx - 7, (int) cy - 6, 12, 1, trim[2]);
                p.rect((int) cx - 7, (int) cy + 4, 15, 2, trim[0]);
                p.rect((int) cx - 1, (int) cy - 2, 9, 3, Pal.INK);
                for (int x = 0; x < 8; x++)
                    p.light(
                            (int) cx + x,
                            (int) cy - 1,
                            x > 5 ? Pal.mix(visor, Pal.WHITE, .4) : visor);
                p.px((int) cx - 5, (int) cy - 3, trim[3]);
                p.px((int) cx - 5, (int) cy + 1, trim[3]);
                lamp(p, cx - 2, cy - 8);
            }
            case 3 -> {
                // Laternenhelm: kleiner Rundhelm mit Köderlampe an einer Antenne.
                p.rect((int) cx - 5, (int) cy + 4, 11, 2, trim[0]);
                p.sphere(cx, cy, 6.5, 6.5, trim);
                p.ellipse(cx + 3, cy, 2.6, 2.6, trim[0]);
                p.ellipse(cx + 3, cy, 1.7, 1.7, visorDark);
                p.light((int) cx + 3, (int) cy - 1, visor);
                p.light((int) cx + 4, (int) cy, visor);
                p.line((int) cx - 1, (int) cy - 6, (int) cx + 2, (int) cy - 11, trim[0]);
                p.line((int) cx + 2, (int) cy - 11, (int) cx + 7, (int) cy - 10, trim[0]);
                p.light((int) cx + 8, (int) cy - 9, Pal.RUST_7);
                p.light((int) cx + 8, (int) cy - 8, Pal.RUST_6);
                p.light((int) cx + 9, (int) cy - 9, Pal.RUST_6);
                p.light((int) cx + 7, (int) cy - 9, Pal.RUST_6);
            }
            default -> {
                // Klassischer Rundhelm mit Frontbullauge, Nieten und Halsring.
                p.rect((int) cx - 6, (int) cy + 4, 13, 3, trim[0]);
                p.rect((int) cx - 5, (int) cy + 4, 11, 1, trim[1]);
                p.sphere(cx, cy - .5, 7.5, 7.5, trim);
                p.ellipse(cx + 3.5, cy, 3.4, 3.4, trim[0]);
                p.ellipse(cx + 3.5, cy, 2.4, 2.4, visorDark);
                p.light((int) cx + 3, (int) cy - 1, visor);
                p.light((int) cx + 4, (int) cy - 1, visor);
                p.light((int) cx + 4, (int) cy, Pal.mix(visor, Pal.WHITE, .5));
                p.light((int) cx + 3, (int) cy, visor);
                p.px((int) cx - 4, (int) cy + 1, trim[0]);
                p.ellipse(cx - 4, cy - 1, 1.5, 1.8, trim[0]);
                p.px((int) cx - 4, (int) cy - 2, Pal.mix(visor, Pal.INK, .3));
                p.px((int) cx, (int) cy - 6, trim[3]);
                p.px((int) cx - 3, (int) cy - 5, trim[3]);
                lamp(p, cx + 2, cy - 8);
            }
        }
    }

    private static void lamp(Painter p, double x, double y) {
        int lx = (int) Math.round(x), ly = (int) Math.round(y);
        p.rect(lx - 1, ly, 3, 2, Pal.STEEL_3);
        p.light(lx + 1, ly, Pal.RUST_7);
        p.light(lx + 2, ly, Pal.BONE);
        p.light(lx + 2, ly + 1, Pal.RUST_7);
    }

    private static final Object[] WEAPON_COLORS = {
        "r",
        Pal.RED_2,
        "d",
        Pal.STEEL_2,
        "m",
        Pal.STEEL_4,
        "k",
        Pal.STEEL_6,
        "w",
        Pal.STEEL_7,
        "b",
        Pal.RUST_4,
        "B",
        Pal.RUST_6,
        "Y",
        0xFFFFE9A8,
        "O",
        Pal.RUST_5,
        "C",
        Pal.TEAL_5,
        "t",
        Pal.RUST_2,
        "g",
        Pal.STEEL_3
    };

    private static void weapon(
            Painter p, Weapon weapon, double x, double y, double angle, Anim anim) {
        switch (weapon) {
            case WRENCH ->
                    p.stamp(
                            new String[] {
                                "..........kkk", ".rrmmmmmmmkk.", ".rrmmmmmmmkk.", "..........kkk"
                            },
                            WEAPON_COLORS,
                            2,
                            2,
                            x,
                            y,
                            angle,
                            false);
            case KNIVES ->
                    p.stamp(
                            new String[] {".rrgwwww.", ".rrgwwwww"},
                            WEAPON_COLORS,
                            2,
                            1,
                            x,
                            y,
                            angle,
                            false);
            case TORCH_LANCE ->
                    p.stamp(
                            new String[] {
                                ".rrmmmmmmmmmmmmbBY.",
                                ".rrmmmmmmmmmmmmbBYY",
                                ".....................",
                            },
                            WEAPON_COLORS,
                            2,
                            1,
                            x,
                            y,
                            angle,
                            false);
            case ANCHOR ->
                    p.stamp(
                            new String[] {
                                "...........kk..",
                                "...........kkk.",
                                ".ttmmmmmmmmkkkk",
                                ".ttmmmmmmmmkkkk",
                                "...........kkk.",
                                "...........kk.."
                            },
                            WEAPON_COLORS,
                            2,
                            2.5,
                            x,
                            y,
                            angle,
                            false);
            case HARPOON ->
                    p.stamp(
                            new String[] {
                                ".....ggggggggkw.",
                                "ddddddddddddd...",
                                "..tt............",
                                "..tt............"
                            },
                            WEAPON_COLORS,
                            4,
                            1.5,
                            x,
                            y,
                            angle,
                            false);
            case TESLA -> {}
            case GRAPPLE ->
                    p.stamp(
                            new String[] {
                                "............kk.",
                                ".rrmmmmmmmmmk.k",
                                ".rrmmmmmmmmmkk.",
                                "............kkk"
                            },
                            WEAPON_COLORS,
                            2,
                            1.5,
                            x,
                            y,
                            angle,
                            false);
        }
    }

    private static Sprite down(Painter p, int[] suit, int[] trim, int visor) {
        // Zusammengesunkene Figur am Boden für die Niederlage.
        p.rect(12, 38, 18, 5, suit[1]);
        p.rect(12, 38, 18, 1, suit[2]);
        p.rect(28, 39, 7, 4, Pal.STEEL_2);
        p.rect(8, 37, 6, 6, Pal.STEEL_4);
        p.sphere(34, 37, 6, 5.5, trim);
        p.light(37, 36, Pal.mix(visor, Pal.INK, .5));
        p.outline(Pal.OUTLINE);
        return p.sprite(ANCHOR_X, ANCHOR_Y);
    }
}
