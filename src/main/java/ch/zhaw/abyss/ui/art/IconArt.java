package ch.zhaw.abyss.ui.art;

import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.Rarity;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.EnumMap;
import java.util.Map;

/**
 * Eigene 16x16-Pixelsymbole für Module, Waffen, aktive Fähigkeiten und Ressourcen. Keine
 * heruntergeladenen Icons: jedes Symbol wird aus Grundformen gemalt.
 */
public final class IconArt {
    /** Kantenlänge eines Symbols. */
    public static final int SIZE = 16;

    private static final Map<Item, Sprite> ITEMS = new EnumMap<>(Item.class);
    private static final Map<Weapon, Sprite> WEAPONS = new EnumMap<>(Weapon.class);
    private static final Map<ActiveModule, Sprite> MODULES = new EnumMap<>(ActiveModule.class);
    private static final Map<String, Sprite> MISC = new java.util.HashMap<>();

    private IconArt() {}

    /**
     * @param item Modul
     * @return Symbol
     */
    public static synchronized Sprite item(Item item) {
        return ITEMS.computeIfAbsent(item, IconArt::paintItem);
    }

    /**
     * @param weapon Waffe
     * @return Symbol
     */
    public static synchronized Sprite weapon(Weapon weapon) {
        return WEAPONS.computeIfAbsent(weapon, IconArt::paintWeapon);
    }

    /**
     * @param module aktives Modul
     * @return Symbol
     */
    public static synchronized Sprite module(ActiveModule module) {
        return MODULES.computeIfAbsent(module, IconArt::paintModule);
    }

    /**
     * @param name {@code scrap}, {@code core}, {@code kit}, {@code heart}, {@code energy}, {@code
     *     dash}, {@code lock}, {@code skull}, {@code chest}, {@code shop}, {@code shrine}, {@code
     *     workshop}, {@code cache}, {@code elite}, {@code boss}, {@code combat}
     * @return Symbol
     */
    public static synchronized Sprite misc(String name) {
        return MISC.computeIfAbsent(name, IconArt::paintMisc);
    }

    /**
     * @param rarity Seltenheit
     * @return Rahmenfarbe
     */
    public static int rarityColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFF9DB3BF;
            case RARE -> 0xFF5FD4FF;
            case LEGENDARY -> 0xFFFFC24A;
            case CURSED -> 0xFFC063FF;
        };
    }

    private static Sprite finish(Painter p) {
        p.outline(Pal.OUTLINE);
        return p.sprite(SIZE / 2, SIZE / 2);
    }

    private static int[] r(int c) {
        return Pal.ramp(c);
    }

    private static Sprite paintItem(Item item) {
        var p = new Painter(SIZE, SIZE);
        switch (item) {
            case SERVO -> {
                gear(p, 8, 8, 5.5, r(Pal.RUST_4));
                p.ellipse(8, 8, 2, 2, Pal.INK);
            }
            case PLATING -> shield(p, r(Pal.STEEL_5), Pal.STEEL_7);
            case CAPACITOR -> {
                p.box(4, 3, 8, 11, r(Pal.TEAL_3));
                p.rect(6, 1, 4, 2, Pal.STEEL_5);
                bolt(p, 8, 5, Pal.TEAL_6);
            }
            case MEDICAL -> cross(p, Pal.RED_3, Pal.RED_5);
            case COOLANT -> {
                p.box(3, 3, 10, 10, r(0xFF3A7ABF));
                snowflake(p, 8, 8, Pal.TEAL_6);
            }
            case RECOVERY -> {
                p.sphere(8, 8, 5.5, 5.5, r(Pal.GREEN_3));
                p.rect(7, 5, 2, 6, Pal.BONE);
                p.rect(5, 7, 6, 2, Pal.BONE);
            }
            case LANCE -> {
                p.thick(2, 14, 13, 3, 2, Pal.STEEL_5);
                p.poly(new double[] {11, 15, 13}, new double[] {1, 1, 5}, Pal.STEEL_7);
            }
            case OVERCLOCK -> {
                p.ellipse(8, 8, 6, 6, Pal.RED_2);
                p.ellipse(8, 8, 4.5, 4.5, Pal.INK);
                p.line(8, 8, 11, 5, Pal.RED_4);
                p.px(8, 8, Pal.BONE);
            }
            case THRUSTER -> {
                p.poly(new double[] {3, 11, 11}, new double[] {8, 4, 12}, Pal.STEEL_4);
                p.rect(11, 5, 3, 7, Pal.STEEL_5);
                p.light(1, 7, Pal.RUST_6);
                p.light(2, 8, Pal.RUST_7);
                p.light(1, 9, Pal.RUST_5);
            }
            case SIPHON -> {
                p.thick(3, 3, 8, 8, 2, Pal.TEAL_3);
                p.thick(8, 8, 13, 13, 2, Pal.TEAL_4);
                p.ellipse(8, 8, 3, 3, Pal.TEAL_2);
                p.light(8, 8, Pal.TEAL_6);
            }
            case REGEN -> {
                for (int i = 0; i < 3; i++) {
                    p.ellipse(4 + i * 4, 6 + (i % 2) * 4, 2, 1.5, Pal.STEEL_5);
                    p.light(4 + i * 4, 6 + (i % 2) * 4, Pal.GREEN_4);
                }
            }
            case MAGNET -> {
                p.thick(4, 3, 4, 10, 3, Pal.RED_3);
                p.thick(12, 3, 12, 10, 3, Pal.RED_3);
                p.thick(4, 10, 12, 10, 3, Pal.RED_3);
                p.rect(3, 2, 3, 2, Pal.STEEL_6);
                p.rect(11, 2, 3, 2, Pal.STEEL_6);
            }
            case LENS -> {
                p.ellipse(7, 7, 5, 5, Pal.STEEL_4);
                p.ellipse(7, 7, 3.5, 3.5, Pal.TEAL_2);
                p.light(6, 6, Pal.TEAL_6);
                p.thick(10, 10, 14, 14, 2, Pal.RUST_3);
            }
            case BALLAST -> {
                p.box(3, 6, 10, 8, r(Pal.STEEL_3));
                p.rect(6, 3, 4, 3, Pal.STEEL_4);
                p.rect(5, 9, 6, 2, Pal.STEEL_1);
            }
            case IGNITER -> flame(p, 8, 13, 1);
            case CRYO_COIL -> {
                for (int i = 0; i < 4; i++)
                    p.thick(3, 3 + i * 3, 13, 4 + i * 3, 1.5, i % 2 == 0 ? Pal.TEAL_4 : Pal.TEAL_5);
                snowflake(p, 8, 8, Pal.WHITE);
            }
            case TOOLBELT -> {
                p.rect(1, 7, 14, 3, Pal.RUST_2);
                p.rect(3, 9, 4, 5, Pal.RUST_3);
                p.rect(9, 9, 4, 5, Pal.RUST_3);
                p.rect(7, 7, 2, 3, Pal.RUST_6);
            }
            case SPIKES -> {
                shield(p, r(Pal.STEEL_3), Pal.STEEL_5);
                for (int i = 0; i < 4; i++) p.px(3 + i * 3, 1, Pal.STEEL_7);
                p.px(1, 6, Pal.STEEL_7);
                p.px(14, 6, Pal.STEEL_7);
            }
            case ARC_COIL -> {
                for (int i = 0; i < 5; i++) p.rect(5, 3 + i * 2, 6, 1, Pal.RUST_4);
                p.rect(7, 2, 2, 11, Pal.STEEL_3);
                bolt(p, 11, 2, Pal.TEAL_6);
            }
            case JETPACK -> {
                p.box(4, 3, 4, 10, r(Pal.STEEL_4));
                p.box(9, 3, 4, 10, r(Pal.STEEL_4));
                p.light(5, 14, Pal.RUST_6);
                p.light(11, 14, Pal.RUST_6);
                p.light(6, 15, Pal.RUST_5);
                p.light(10, 15, Pal.RUST_5);
            }
            case ADRENALINE -> {
                p.rect(7, 1, 2, 4, Pal.STEEL_5);
                p.box(5, 5, 6, 8, r(Pal.RED_2));
                p.lightRect(6, 7, 4, 4, Pal.RED_4);
                p.rect(7, 13, 2, 2, Pal.STEEL_6);
            }
            case AMBUSH -> {
                p.sphere(8, 8, 6, 6, r(Pal.STEEL_2));
                p.rect(3, 7, 10, 2, Pal.INK);
                p.light(10, 7, Pal.RED_4);
                p.light(11, 7, Pal.RED_4);
            }
            case AFTERBURNER -> {
                for (int i = 0; i < 3; i++)
                    p.thick(2 + i, 4 + i * 3, 9, 4 + i * 3, 1.5, Pal.RUST_4);
                flame(p, 12, 12, .7);
            }
            case DASH_BLADE -> {
                p.poly(new double[] {2, 14, 10}, new double[] {12, 4, 12}, Pal.STEEL_6);
                p.line(3, 12, 13, 5, Pal.WHITE);
            }
            case VALVE -> {
                p.ellipse(8, 8, 6, 6, Pal.RED_2);
                p.ellipse(8, 8, 4, 4, Pal.INK);
                for (int k = 0; k < 4; k++) {
                    double a = k * Math.PI / 2 + .4;
                    p.line(
                            8,
                            8,
                            (int) Math.round(8 + Math.cos(a) * 5),
                            (int) Math.round(8 + Math.sin(a) * 5),
                            Pal.RED_4);
                }
            }
            case CHAIN_REACTION -> {
                p.sphere(8, 9, 5, 5, r(Pal.STEEL_2));
                p.light(8, 3, Pal.RUST_6);
                p.light(9, 2, Pal.WHITE);
                p.light(6, 9, Pal.RUST_5);
            }
            case NANITES -> {
                for (int i = 0; i < 7; i++) {
                    int x = 3 + (i * 5) % 10, y = 3 + (i * 7) % 10;
                    p.rect(x, y, 2, 2, Pal.STEEL_5);
                    p.light(x, y, Pal.GREEN_4);
                }
            }
            case BARRIER -> {
                for (int k = 0; k < 6; k++) {
                    double a = k * Math.PI / 3;
                    p.line(
                            (int) Math.round(8 + Math.cos(a) * 6),
                            (int) Math.round(8 + Math.sin(a) * 6),
                            (int) Math.round(8 + Math.cos(a + Math.PI / 3) * 6),
                            (int) Math.round(8 + Math.sin(a + Math.PI / 3) * 6),
                            Pal.TEAL_4);
                }
                p.light(8, 8, Pal.TEAL_6);
            }
            case LANTERN -> {
                p.rect(7, 1, 2, 2, Pal.STEEL_4);
                p.box(5, 3, 6, 10, r(Pal.RUST_3));
                p.lightRect(6, 5, 4, 6, Pal.RUST_7);
            }
            case DEPTH_RUSH -> {
                p.thick(8, 2, 8, 12, 2, Pal.TEAL_3);
                p.poly(new double[] {3, 13, 8}, new double[] {10, 10, 15}, Pal.TEAL_4);
                p.light(8, 13, Pal.TEAL_6);
            }
            case SHIELD_CELL -> {
                shield(p, r(Pal.TEAL_2), Pal.TEAL_4);
                p.lightRect(7, 5, 2, 6, Pal.TEAL_6);
            }
            case HOMING -> {
                p.ellipse(8, 8, 6, 6, Pal.RED_2);
                p.ellipse(8, 8, 4, 4, Pal.INK);
                p.ellipse(8, 8, 2, 2, Pal.RED_2);
                p.light(8, 8, Pal.RED_4);
            }
            case CRIT_DAMAGE -> {
                p.poly(new double[] {8, 12, 8, 4}, new double[] {1, 8, 15, 8}, Pal.RUST_4);
                p.poly(new double[] {8, 10, 8, 6}, new double[] {4, 8, 12, 8}, Pal.RUST_6);
            }
            case COMPASS -> {
                p.ellipse(8, 8, 6.5, 6.5, Pal.RUST_4);
                p.ellipse(8, 8, 5, 5, Pal.PAPER);
                p.poly(new double[] {8, 10, 8, 6}, new double[] {2, 8, 14, 8}, Pal.RED_3);
                p.poly(new double[] {8, 10, 8, 6}, new double[] {8, 8, 14, 8}, Pal.STEEL_4);
            }
            case SECOND_HEART -> {
                heart(p, Pal.RUST_5, Pal.RUST_7);
                p.light(6, 6, Pal.WHITE);
            }
            case PHASE_CORE -> {
                p.sphere(
                        8,
                        8,
                        6,
                        6,
                        new int[] {Pal.VIOLET_1, Pal.VIOLET_2, Pal.VIOLET_3, Pal.VIOLET_5});
                p.light(8, 8, Pal.WHITE);
                p.light(5, 11, Pal.VIOLET_4);
            }
            case SINGULARITY -> {
                p.ellipse(8, 8, 6, 6, Pal.INK);
                for (int k = 0; k < 12; k++) {
                    double a = k * Math.PI / 6;
                    p.light(
                            (int) Math.round(8 + Math.cos(a) * 6),
                            (int) Math.round(8 + Math.sin(a) * 4),
                            Pal.VIOLET_4);
                }
                p.light(8, 8, Pal.WHITE);
            }
            case LEVIATHAN_TOOTH -> {
                p.poly(new double[] {4, 12, 8}, new double[] {2, 2, 15}, Pal.BONE);
                p.line(8, 3, 8, 12, Pal.PAPER);
                p.light(8, 5, Pal.TEAL_5);
            }
            case GLASS_HULL -> {
                heart(p, Pal.TEAL_2, Pal.TEAL_5);
                p.line(5, 4, 10, 11, Pal.WHITE);
            }
            case GREED -> {
                for (int i = 0; i < 3; i++) {
                    p.ellipse(8, 12 - i * 3, 5, 2, Pal.RUST_4);
                    p.rect(4, 11 - i * 3, 8, 1, Pal.RUST_6);
                }
                p.light(13, 3, Pal.VIOLET_4);
            }
            case FEVER -> {
                p.rect(7, 2, 3, 10, Pal.BONE);
                p.ellipse(8.5, 12, 3, 3, Pal.RED_3);
                p.lightRect(8, 5, 1, 7, Pal.RED_4);
            }
        }
        return finish(p);
    }

    private static Sprite paintWeapon(Weapon weapon) {
        var p = new Painter(SIZE, SIZE);
        switch (weapon) {
            case WRENCH -> {
                p.thick(3, 13, 10, 6, 2, Pal.STEEL_4);
                p.thick(2, 14, 4, 12, 2.5, Pal.RED_2);
                p.rect(9, 2, 5, 3, Pal.STEEL_6);
                p.rect(12, 5, 2, 3, Pal.STEEL_6);
            }
            case KNIVES -> {
                p.thick(3, 13, 12, 4, 2, Pal.STEEL_7);
                p.thick(2, 14, 5, 11, 2.5, Pal.RED_2);
                p.thick(6, 13, 13, 7, 1.5, Pal.STEEL_6);
            }
            case TORCH_LANCE -> {
                p.thick(2, 14, 12, 4, 2, Pal.STEEL_4);
                p.light(13, 3, Pal.WHITE);
                p.light(14, 2, 0xFFFFE9A8);
                p.light(12, 3, Pal.RUST_6);
            }
            case ANCHOR -> {
                p.thick(3, 13, 9, 7, 2, Pal.RUST_2);
                p.poly(new double[] {7, 14, 14, 11}, new double[] {3, 3, 10, 7}, Pal.STEEL_5);
                p.rect(10, 2, 4, 2, Pal.STEEL_7);
            }
            case HARPOON -> {
                p.rect(1, 8, 11, 3, Pal.STEEL_3);
                p.rect(3, 11, 3, 3, Pal.RUST_2);
                p.thick(4, 7, 15, 7, 1, Pal.STEEL_6);
                p.px(15, 6, Pal.STEEL_7);
                p.px(15, 8, Pal.STEEL_7);
            }
            case TESLA -> {
                p.sphere(8, 9, 5, 5, r(Pal.STEEL_3));
                p.rect(4, 4, 8, 2, Pal.RUST_4);
                bolt(p, 12, 2, Pal.TEAL_6);
            }
            case GRAPPLE -> {
                p.thick(2, 14, 11, 5, 1.5, Pal.STEEL_4);
                p.thick(2, 14, 4, 12, 2.5, Pal.RED_2);
                p.thick(11, 5, 14, 2, 1.5, Pal.STEEL_6);
                p.thick(11, 5, 14, 7, 1.5, Pal.STEEL_6);
                p.px(14, 1, Pal.STEEL_7);
            }
        }
        return finish(p);
    }

    private static Sprite paintModule(ActiveModule module) {
        var p = new Painter(SIZE, SIZE);
        switch (module) {
            case PULSE -> {
                p.ellipse(8, 8, 6.5, 6.5, Pal.TEAL_2);
                p.ellipse(8, 8, 5, 5, Pal.INK);
                p.ellipse(8, 8, 3, 3, Pal.TEAL_3);
                p.light(8, 8, Pal.TEAL_6);
            }
            case ARC -> {
                bolt(p, 8, 1, Pal.TEAL_6);
                bolt(p, 4, 4, Pal.TEAL_4);
            }
            case AEGIS -> {
                shield(p, r(Pal.TEAL_3), Pal.TEAL_5);
                p.light(8, 7, Pal.WHITE);
            }
            case TORPEDO -> {
                p.rect(2, 6, 10, 4, Pal.STEEL_5);
                p.rect(2, 6, 10, 1, Pal.STEEL_7);
                p.ellipse(12, 8, 2.5, 2, Pal.RED_3);
                p.rect(1, 4, 2, 8, Pal.STEEL_3);
            }
            case SONAR -> {
                for (int rr = 2; rr <= 6; rr += 2)
                    for (int k = -4; k <= 4; k++) {
                        double a = k * .18;
                        p.light(
                                (int) Math.round(3 + Math.cos(a) * rr * 1.8),
                                (int) Math.round(8 + Math.sin(a) * rr * 1.8),
                                rr == 6 ? Pal.TEAL_4 : Pal.TEAL_6);
                    }
                p.rect(1, 7, 3, 3, Pal.STEEL_5);
            }
            case CRYO -> {
                p.sphere(8, 9, 5, 5, r(0xFF4A8ACF));
                p.rect(7, 2, 2, 3, Pal.STEEL_5);
                snowflake(p, 8, 9, Pal.WHITE);
            }
            case DRONE -> {
                p.sphere(8, 9, 4.5, 4, r(Pal.STEEL_5));
                p.rect(2, 3, 12, 1, Pal.STEEL_6);
                p.rect(7, 4, 2, 2, Pal.STEEL_3);
                p.light(10, 9, Pal.TEAL_5);
            }
            case OVERDRIVE -> {
                p.poly(
                        new double[] {9, 3, 8, 6, 13, 8},
                        new double[] {1, 9, 9, 15, 7, 7},
                        Pal.RUST_5);
                p.light(8, 8, Pal.RUST_7);
            }
        }
        return finish(p);
    }

    private static Sprite paintMisc(String name) {
        var p = new Painter(SIZE, SIZE);
        switch (name) {
            case "scrap" -> {
                gear(p, 8, 8, 5, r(Pal.RUST_4));
                p.ellipse(8, 8, 1.5, 1.5, Pal.INK);
            }
            case "core" -> {
                p.poly(new double[] {8, 13, 8, 3}, new double[] {2, 8, 14, 8}, Pal.TEAL_3);
                p.poly(new double[] {8, 11, 8, 5}, new double[] {4, 8, 12, 8}, Pal.TEAL_5);
                p.light(8, 7, Pal.WHITE);
            }
            case "kit" -> {
                p.box(3, 4, 10, 9, r(Pal.RED_2));
                p.rect(6, 2, 4, 2, Pal.STEEL_4);
                p.rect(7, 6, 2, 5, Pal.BONE);
                p.rect(5, 7, 6, 2, Pal.BONE);
            }
            case "heart" -> heart(p, Pal.RED_3, Pal.RED_5);
            case "energy" -> bolt(p, 8, 2, Pal.TEAL_5);
            case "dash" -> {
                for (int i = 0; i < 3; i++)
                    p.thick(2 + i * 2, 5 + i * 3, 10 + i, 5 + i * 3, 1.5, Pal.STEEL_6);
                p.poly(new double[] {10, 15, 10}, new double[] {3, 8, 13}, Pal.TEAL_4);
            }
            case "lock" -> {
                p.box(3, 7, 10, 8, r(Pal.STEEL_4));
                p.thick(5, 7, 5, 3, 2, Pal.STEEL_5);
                p.thick(11, 7, 11, 3, 2, Pal.STEEL_5);
                p.thick(5, 3, 11, 3, 2, Pal.STEEL_5);
                p.rect(7, 10, 2, 3, Pal.INK);
            }
            case "skull" -> {
                p.sphere(8, 7, 5.5, 5, r(Pal.BONE));
                p.rect(5, 11, 6, 3, Pal.PAPER);
                p.rect(5, 6, 2, 2, Pal.INK);
                p.rect(9, 6, 2, 2, Pal.INK);
            }
            case "chest", "combat" -> {
                p.box(2, 6, 12, 8, r(Pal.RUST_3));
                p.box(2, 3, 12, 4, r(Pal.RUST_4));
                p.rect(7, 6, 2, 3, Pal.RUST_6);
            }
            case "elite" -> {
                p.poly(
                        new double[] {2, 5, 8, 11, 14, 12, 4},
                        new double[] {5, 9, 3, 9, 5, 13, 13},
                        Pal.RUST_5);
                p.light(8, 5, Pal.RED_4);
            }
            case "boss" -> {
                p.sphere(8, 8, 6, 6, r(Pal.RED_2));
                p.light(6, 7, Pal.RUST_7);
                p.light(10, 7, Pal.RUST_7);
                p.rect(5, 11, 6, 1, Pal.INK);
            }
            case "shop" -> {
                gear(p, 8, 8, 5, r(Pal.RUST_5));
                p.rect(7, 4, 2, 8, Pal.INK);
            }
            case "shrine" -> {
                p.rect(7, 2, 2, 12, Pal.VIOLET_3);
                p.rect(4, 5, 8, 2, Pal.VIOLET_3);
                p.light(8, 5, Pal.VIOLET_5);
            }
            case "workshop" -> {
                p.thick(3, 13, 11, 5, 2, Pal.STEEL_5);
                p.rect(9, 2, 5, 3, Pal.STEEL_6);
                p.thick(3, 4, 12, 13, 2, Pal.RUST_4);
            }
            case "cache" -> {
                p.box(3, 4, 10, 10, r(Pal.GREEN_2));
                p.rect(7, 6, 2, 6, Pal.BONE);
                p.rect(5, 8, 6, 2, Pal.BONE);
            }
            default -> p.rect(4, 4, 8, 8, Pal.STEEL_4);
        }
        return finish(p);
    }

    private static void gear(Painter p, double cx, double cy, double radius, int[] ramp) {
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            p.thick(
                    cx,
                    cy,
                    cx + Math.cos(a) * (radius + 1.5),
                    cy + Math.sin(a) * (radius + 1.5),
                    2.2,
                    ramp[1]);
        }
        p.sphere(cx, cy, radius, radius, ramp);
    }

    private static void shield(Painter p, int[] ramp, int light) {
        p.poly(new double[] {2, 14, 14, 8, 2}, new double[] {2, 2, 8, 15, 8}, ramp[1]);
        p.poly(new double[] {3, 8, 8, 3}, new double[] {3, 3, 13, 8}, ramp[2]);
        p.line(8, 3, 8, 13, light);
    }

    private static void cross(Painter p, int base, int light) {
        p.rect(6, 2, 4, 12, base);
        p.rect(2, 6, 12, 4, base);
        p.rect(6, 2, 1, 12, light);
        p.rect(2, 6, 12, 1, light);
    }

    private static void heart(Painter p, int base, int light) {
        p.ellipse(5, 6, 3.2, 3.2, base);
        p.ellipse(11, 6, 3.2, 3.2, base);
        p.poly(new double[] {2, 14, 8}, new double[] {7, 7, 14}, base);
        p.px(4, 4, light);
        p.px(5, 4, light);
    }

    private static void bolt(Painter p, int x, int y, int color) {
        int[][] pts = {
            {0, 0}, {-1, 1}, {-2, 2}, {-3, 3}, {-1, 3}, {0, 3}, {-1, 4}, {-2, 5}, {-3, 6}, {-4, 7}
        };
        for (int[] pt : pts) {
            p.light(x + pt[0], y + pt[1], color);
            p.light(x + pt[0] + 1, y + pt[1], Pal.mix(color, Pal.WHITE, .4));
        }
    }

    private static void snowflake(Painter p, int cx, int cy, int color) {
        for (int k = -3; k <= 3; k++) {
            p.px(cx + k, cy, color);
            p.px(cx, cy + k, color);
            if (Math.abs(k) <= 2) {
                p.px(cx + k, cy + k, color);
                p.px(cx + k, cy - k, color);
            }
        }
    }

    private static void flame(Painter p, int cx, int bottom, double scale) {
        p.poly(
                new double[] {cx - 5 * scale, cx, cx + 5 * scale},
                new double[] {bottom, bottom - 12 * scale, bottom},
                Pal.RUST_4);
        for (int y = 0; y < 8 * scale; y++)
            p.light(cx, bottom - 2 - y, y < 4 ? Pal.RUST_7 : Pal.RUST_6);
        p.light(cx - 1, bottom - 3, Pal.RUST_6);
        p.light(cx + 1, bottom - 3, Pal.RUST_6);
    }
}
