package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.domain.SupplyCrate;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.HashMap;
import java.util.Map;

/**
 * Pixelgrafiken für interaktive Objekte: Kisten, Bergungskapsel, Händlerin, Kapelle, Werkbank,
 * Ausgangsschott, Beute und Geschosse. Einmal gemalt und zwischengespeichert.
 */
public final class PropArt {
    private static final Map<String, Sprite> CACHE = new HashMap<>();

    private PropArt() {}

    /**
     * @param kind Kisteninhalt
     * @return Kistengrafik, Anker unten Mitte
     */
    public static Sprite crate(SupplyCrate.Kind kind) {
        return cached("crate-" + kind, () -> paintCrate(kind));
    }

    /**
     * @param open geöffnet
     * @param frame Animationsbild 0 bis 3
     * @return Bergungskapsel
     */
    public static Sprite pod(boolean open, int frame) {
        return cached("pod-" + open + frame, () -> paintPod(open, frame));
    }

    /**
     * @param frame Animationsbild 0 bis 3
     * @return Händlerin mit Laterne
     */
    public static Sprite merchant(int frame) {
        return cached("merchant-" + frame, () -> paintMerchant(frame));
    }

    /**
     * @param frame Animationsbild 0 bis 3
     * @param spent bereits gehandelt
     * @return Altar der Druckkapelle
     */
    public static Sprite shrine(int frame, boolean spent) {
        return cached("shrine-" + frame + spent, () -> paintShrine(frame, spent));
    }

    /**
     * @param frame Animationsbild 0 bis 3
     * @return Werkbank mit Roboterarm
     */
    public static Sprite workbench(int frame) {
        return cached("bench-" + frame, () -> paintBench(frame));
    }

    /**
     * @param open Öffnungsgrad 0 bis 4
     * @return Ausgangsschott
     */
    public static Sprite door(int open) {
        return cached("door-" + open, () -> paintDoor(open));
    }

    /**
     * @param name {@code scrap0}, {@code scrap1}, {@code health}, {@code energy}, {@code core},
     *     {@code harpoon}, {@code eharpoon}, {@code torpedo}, {@code mine}, {@code cryo}, {@code
     *     drone0}, {@code drone1}
     * @return kleine Grafik, Anker in der Mitte
     */
    public static Sprite small(String name) {
        return cached("small-" + name, () -> paintSmall(name));
    }

    private static synchronized Sprite cached(
            String key, java.util.function.Supplier<Sprite> painter) {
        var sprite = CACHE.get(key);
        if (sprite == null) {
            sprite = painter.get();
            CACHE.put(key, sprite);
        }
        return sprite;
    }

    private static Sprite paintCrate(SupplyCrate.Kind kind) {
        var p = new Painter(20, 22);
        if (kind == SupplyCrate.Kind.EXPLOSIVE) {
            int[] c = {Pal.RED_1, Pal.RED_2, Pal.RED_3};
            p.rect(4, 3, 12, 17, c[1]);
            p.rect(5, 3, 3, 17, c[2]);
            p.rect(14, 3, 2, 17, c[0]);
            p.rect(4, 2, 12, 1, Pal.RED_4);
            p.rect(4, 8, 12, 1, c[0]);
            p.rect(4, 15, 12, 1, c[0]);
            for (int x = 6; x < 14; x++) p.px(x, 11, (x % 2 == 0) ? Pal.RUST_6 : Pal.INK);
            p.light(9, 5, Pal.RUST_6);
        } else {
            int[] c = {Pal.RUST_1, Pal.RUST_2, Pal.RUST_3};
            p.box(2, 5, 16, 15, c);
            p.rect(2, 11, 16, 2, Pal.STEEL_2);
            p.rect(9, 5, 2, 15, Pal.STEEL_2);
            int color =
                    switch (kind) {
                        case REPAIR -> Pal.GREEN_4;
                        case ENERGY -> Pal.TEAL_5;
                        default -> Pal.RUST_6;
                    };
            p.rect(4, 7, 4, 3, Pal.INK);
            p.light(5, 8, color);
            p.light(6, 8, color);
            p.px(3, 6, Pal.RUST_4);
        }
        p.outline(Pal.OUTLINE);
        return p.sprite(10, 20);
    }

    private static Sprite paintPod(boolean open, int frame) {
        var p = new Painter(30, 30);
        p.box(3, 22, 24, 6, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.rect(6, 27, 18, 1, Pal.STEEL_0);
        if (open) {
            p.box(5, 16, 20, 6, new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5});
            p.poly(new double[] {5, 25, 22, 8}, new double[] {16, 16, 6, 6}, Pal.STEEL_3);
            p.lightRect(8, 17, 14, 3, Pal.TEAL_2);
        } else {
            p.sphere(15, 16, 11, 8, new int[] {Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5, Pal.STEEL_7});
            p.rect(4, 17, 22, 2, Pal.STEEL_2);
            int glow = frame % 2 == 0 ? Pal.TEAL_5 : Pal.TEAL_6;
            p.rect(11, 11, 8, 4, Pal.INK);
            p.lightRect(12, 12, 6, 2, glow);
            for (int x = 7; x < 24; x += 4)
                p.light(x, 18, frame % 4 == (x / 4) % 4 ? Pal.RUST_6 : Pal.RUST_3);
        }
        p.outline(Pal.OUTLINE);
        return p.sprite(15, 28);
    }

    private static Sprite paintMerchant(int frame) {
        var p = new Painter(40, 50);
        int bob = frame >= 2 ? 1 : 0;
        // Kapuzenmantel
        p.poly(new double[] {12, 26, 30, 8}, new double[] {18 + bob, 18 + bob, 48, 48}, 0xFF3A2A40);
        p.poly(
                new double[] {14, 20, 18, 10},
                new double[] {20 + bob, 20 + bob, 48, 48},
                0xFF4E3A56);
        p.sphere(19, 14 + bob, 7, 7, new int[] {0xFF241A2A, 0xFF3A2A40, 0xFF4E3A56, 0xFF6A527A});
        p.ellipse(21, 15 + bob, 4, 4.5, Pal.INK);
        p.light(20, 14 + bob, Pal.RUST_6);
        p.light(23, 14 + bob, Pal.RUST_6);
        // Rucksack voller Kram
        p.box(4, 20 + bob, 10, 16, new int[] {Pal.RUST_1, Pal.RUST_2, Pal.RUST_3});
        p.rect(5, 17 + bob, 3, 4, Pal.STEEL_4);
        p.rect(9, 16 + bob, 2, 5, Pal.RED_2);
        // Laterne an einem Stab
        p.line(28, 20 + bob, 32, 6, Pal.STEEL_2);
        p.rect(31, 6, 1, 4, Pal.STEEL_2);
        p.box(29, 10, 6, 7, new int[] {Pal.RUST_2, Pal.RUST_3, Pal.RUST_4});
        int flame = frame % 2 == 0 ? 0xFFFFD27A : 0xFFFFB050;
        p.lightRect(30, 12, 4, 3, flame);
        p.light(26, 26 + bob, Pal.STEEL_5);
        p.outline(Pal.OUTLINE);
        return p.sprite(20, 48);
    }

    private static Sprite paintShrine(int frame, boolean spent) {
        var p = new Painter(44, 56);
        p.box(8, 44, 28, 10, new int[] {0xFF1C1420, 0xFF2A1E2E, 0xFF3C2C40});
        p.box(12, 30, 20, 14, new int[] {0xFF1C1420, 0xFF2A1E2E, 0xFF3C2C40});
        p.rect(14, 33, 16, 1, Pal.VIOLET_2);
        for (int i = 0; i < 5; i++) p.px(15 + i * 3, 40, Pal.RUST_4);
        double hover = Math.sin(frame / 4.0 * Math.PI * 2) * 1.5;
        if (!spent) {
            int cy = (int) (18 + hover);
            p.poly(
                    new double[] {22, 28, 22, 16},
                    new double[] {cy - 8, cy, cy + 8, cy},
                    Pal.VIOLET_2);
            p.poly(
                    new double[] {22, 25, 22, 19},
                    new double[] {cy - 5, cy, cy + 5, cy},
                    Pal.VIOLET_4);
            p.light(22, cy, Pal.WHITE);
            p.light(22, cy - 1, Pal.VIOLET_5);
            p.light(21, cy, Pal.VIOLET_5);
        } else {
            p.rect(19, 27, 6, 3, Pal.VIOLET_1);
        }
        for (int s = -1; s <= 1; s += 2) {
            p.rect(22 + s * 17 - 1, 28, 3, 26, 0xFF2A1E2E);
            p.light(22 + s * 17, 26, frame % 2 == 0 ? 0xFFFFD27A : Pal.RUST_6);
        }
        p.outline(Pal.OUTLINE);
        return p.sprite(22, 54);
    }

    private static Sprite paintBench(int frame) {
        var p = new Painter(54, 50);
        p.box(4, 30, 46, 5, new int[] {Pal.RUST_1, Pal.RUST_2, Pal.RUST_4});
        p.rect(6, 35, 4, 13, Pal.STEEL_2);
        p.rect(44, 35, 4, 13, Pal.STEEL_2);
        p.rect(10, 40, 34, 2, Pal.STEEL_2);
        // Amboss
        p.box(14, 24, 16, 6, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_5});
        p.rect(12, 24, 20, 2, Pal.STEEL_4);
        // Roboterarm
        double a = frame * .25;
        p.box(38, 22, 8, 8, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        double ex = 36 - Math.cos(a) * 3, ey = 12 + Math.sin(a) * 2;
        p.thick(42, 22, ex, ey, 3, Pal.RUST_4);
        p.thick(ex, ey, 26, 18 + Math.sin(a) * 2, 3, Pal.RUST_4);
        p.rect(24, 18 + (int) (Math.sin(a) * 2), 4, 3, Pal.STEEL_4);
        if (frame % 2 == 0) {
            p.light(25, 23, Pal.WHITE);
            p.light(24, 22, 0xFFBFE8FF);
            p.light(27, 22, 0xFFBFE8FF);
        }
        p.outline(Pal.OUTLINE);
        return p.sprite(27, 48);
    }

    private static Sprite paintDoor(int open) {
        var p = new Painter(30, 72);
        int lift = open * 14;
        p.box(0, 0, 30, 6, new int[] {Pal.STEEL_1, Pal.STEEL_3, Pal.STEEL_4});
        p.rect(0, 6, 4, 66, Pal.STEEL_2);
        p.rect(26, 6, 4, 66, Pal.STEEL_1);
        int doorTop = 6, doorBottom = 70 - lift;
        if (doorBottom > doorTop + 2) {
            p.box(
                    4,
                    doorTop,
                    22,
                    doorBottom - doorTop,
                    new int[] {Pal.STEEL_2, Pal.STEEL_3, Pal.STEEL_5});
            for (int y = doorTop + 4; y < doorBottom - 2; y += 10) p.rect(6, y, 18, 1, Pal.STEEL_2);
            for (int y = Math.max(doorTop, doorBottom - 6); y < doorBottom; y++)
                for (int x = 4; x < 26; x++)
                    p.px(x, y, ((x + y) / 2) % 2 == 0 ? Pal.RUST_5 : Pal.INK);
        }
        int light = open == 0 ? Pal.RED_4 : Pal.GREEN_4;
        p.light(14, 2, light);
        p.light(15, 2, light);
        p.outline(Pal.OUTLINE);
        return p.sprite(15, 71);
    }

    private static Sprite paintSmall(String name) {
        var p = new Painter(20, 12);
        switch (name) {
            case "scrap0", "scrap1" -> {
                boolean turn = name.endsWith("1");
                p.ellipse(10, 6, 3, 3, Pal.RUST_4);
                if (turn) {
                    p.px(10, 2, Pal.RUST_5);
                    p.px(10, 10, Pal.RUST_3);
                    p.px(6, 6, Pal.RUST_3);
                    p.px(14, 6, Pal.RUST_5);
                } else {
                    p.px(7, 3, Pal.RUST_5);
                    p.px(13, 3, Pal.RUST_5);
                    p.px(7, 9, Pal.RUST_3);
                    p.px(13, 9, Pal.RUST_3);
                }
                p.px(10, 6, Pal.INK);
                p.light(9, 5, Pal.RUST_7);
            }
            case "health" -> {
                p.lightRect(9, 3, 2, 6, Pal.GREEN_5);
                p.lightRect(7, 5, 6, 2, Pal.GREEN_5);
                p.px(10, 4, Pal.WHITE);
            }
            case "energy" -> {
                int[][] pts = {
                    {11, 2}, {10, 3}, {9, 4}, {8, 5}, {10, 5}, {11, 5}, {10, 6}, {9, 7}, {8, 8}
                };
                for (int[] pt : pts) p.light(pt[0], pt[1], Pal.TEAL_5);
            }
            case "core" -> {
                p.poly(new double[] {10, 14, 10, 6}, new double[] {1, 6, 11, 6}, Pal.TEAL_3);
                p.light(10, 4, Pal.TEAL_6);
                p.light(10, 5, Pal.WHITE);
                p.light(9, 6, Pal.TEAL_5);
            }
            case "harpoon" -> {
                p.rect(2, 5, 13, 1, Pal.STEEL_5);
                p.rect(2, 6, 13, 1, Pal.STEEL_3);
                p.poly(new double[] {14, 19, 14}, new double[] {3, 6, 9}, Pal.STEEL_7);
                p.rect(1, 4, 2, 4, Pal.RUST_4);
            }
            case "eharpoon" -> {
                p.rect(1, 5, 14, 2, Pal.RUST_2);
                p.poly(new double[] {14, 19, 14}, new double[] {2, 6, 10}, Pal.RED_3);
                p.light(17, 6, Pal.RED_4);
            }
            case "torpedo" -> {
                p.rect(4, 4, 11, 4, Pal.STEEL_5);
                p.rect(4, 4, 11, 1, Pal.STEEL_7);
                p.ellipse(15, 6, 2.5, 2, Pal.RED_3);
                p.rect(2, 3, 2, 6, Pal.STEEL_3);
                p.light(16, 5, Pal.RED_4);
            }
            case "mine" -> {
                p.sphere(
                        10,
                        7,
                        4,
                        3.5,
                        new int[] {Pal.STEEL_1, Pal.STEEL_2, Pal.STEEL_4, Pal.STEEL_5});
                p.px(6, 4, Pal.STEEL_4);
                p.px(14, 4, Pal.STEEL_4);
                p.px(10, 2, Pal.STEEL_4);
            }
            case "cryo" -> {
                p.sphere(
                        10, 6, 3.5, 3.5, new int[] {0xFF2A5A8A, 0xFF4A8ACF, 0xFF8AC0F0, Pal.WHITE});
                p.light(10, 6, Pal.TEAL_6);
            }
            case "drone0", "drone1" -> {
                boolean wide = name.endsWith("0");
                p.rect(wide ? 4 : 7, 1, wide ? 12 : 6, 1, Pal.STEEL_6);
                p.rect(9, 2, 2, 2, Pal.STEEL_3);
                p.sphere(10, 7, 4, 3.5, new int[] {Pal.RUST_2, Pal.RUST_4, Pal.RUST_5, Pal.RUST_7});
                p.light(12, 7, Pal.TEAL_5);
                p.light(13, 7, Pal.TEAL_6);
            }
            default -> p.rect(8, 4, 4, 4, Pal.WHITE);
        }
        p.outline(Pal.OUTLINE);
        return p.sprite(10, 6);
    }
}
