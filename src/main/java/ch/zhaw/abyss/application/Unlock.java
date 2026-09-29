package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Dauerhafte Freischaltung im Archiv, bezahlt mit Datenkernen.
 *
 * @param id stabile Kennung für den Spielstand
 * @param category Kategorie für die Anzeige
 * @param title angezeigter Name
 * @param description Wirkung
 * @param cost Preis in Datenkernen
 * @param requires Kennung einer vorausgesetzten Freischaltung oder {@code null}
 */
public record Unlock(
        String id, Category category, String title, String description, int cost, String requires) {
    /** Kategorien im Archiv. */
    public enum Category {
        DIVER("Taucher"),
        WEAPON("Waffen"),
        MODULE("Module"),
        ITEM("Baupläne"),
        PERK("Tiefenbaum"),
        COSMETIC("Garderobe");

        private final String title;

        Category(String title) {
            this.title = title;
        }

        /**
         * @return angezeigter Name
         */
        public String title() {
            return title;
        }
    }

    private static final List<Unlock> CATALOG = build();

    /**
     * @return vollständiger, stabil sortierter Katalog
     */
    public static List<Unlock> catalog() {
        return CATALOG;
    }

    /**
     * @param id Kennung
     * @return passende Freischaltung
     */
    public static Optional<Unlock> find(String id) {
        return CATALOG.stream().filter(unlock -> unlock.id.equals(id)).findFirst();
    }

    /**
     * @param diver Klasse
     * @return Kennung der Klasse
     */
    public static String of(DiverClass diver) {
        return "diver:" + diver.name();
    }

    /**
     * @param weapon Waffe
     * @return Kennung der Waffe
     */
    public static String of(Weapon weapon) {
        return "weapon:" + weapon.name();
    }

    /**
     * @param module Modul
     * @return Kennung des Moduls
     */
    public static String of(ActiveModule module) {
        return "module:" + module.name();
    }

    /**
     * @param item Bauplan
     * @return Kennung des Bauplans
     */
    public static String of(Item item) {
        return "item:" + item.name();
    }

    /** Legt eine Kette aufeinander aufbauender Stufen im Tiefenbaum an. */
    private static void tree(
            List<Unlock> list, String id, String title, String effect, int[] costs, String root) {
        String[] numerals = {"I", "II", "III", "IV", "V"};
        for (int i = 0; i < costs.length; i++)
            list.add(
                    new Unlock(
                            id + (i + 1),
                            Category.PERK,
                            title + " " + numerals[i],
                            effect + (i == 0 ? " in jedem Tauchgang." : ", weitere Stufe."),
                            costs[i],
                            i == 0 ? root : id + i));
    }

    private static List<Unlock> build() {
        var list = new ArrayList<Unlock>();
        for (var diver : DiverClass.values())
            if (diver.unlockCost() > 0)
                list.add(
                        new Unlock(
                                of(diver),
                                Category.DIVER,
                                diver.title(),
                                diver.description(),
                                diver.unlockCost(),
                                null));
        for (var weapon : Weapon.values())
            if (!weapon.startsUnlocked())
                list.add(
                        new Unlock(
                                of(weapon),
                                Category.WEAPON,
                                weapon.title(),
                                weapon.description(),
                                weapon.unlockCost(),
                                null));
        for (var module : ActiveModule.values())
            if (!module.startsUnlocked())
                list.add(
                        new Unlock(
                                of(module),
                                Category.MODULE,
                                module.title(),
                                module.description(),
                                module.unlockCost(),
                                null));
        Arrays.stream(Item.values())
                .filter(item -> !item.startsUnlocked() && !item.cursed())
                .forEach(
                        item ->
                                list.add(
                                        new Unlock(
                                                of(item),
                                                Category.ITEM,
                                                item.title(),
                                                item.effect().replace('\n', ' '),
                                                item.unlockCost(),
                                                null)));
        list.add(
                new Unlock(
                        "perk:hull1",
                        Category.PERK,
                        "Verstärkter Anzug I",
                        "+10 maximale Integrität zu Beginn jedes Tauchgangs.",
                        6,
                        null));
        list.add(
                new Unlock(
                        "perk:hull2",
                        Category.PERK,
                        "Verstärkter Anzug II",
                        "Weitere +10 maximale Integrität.",
                        10,
                        "perk:hull1"));
        list.add(
                new Unlock(
                        "perk:hull3",
                        Category.PERK,
                        "Verstärkter Anzug III",
                        "Weitere +10 maximale Integrität.",
                        15,
                        "perk:hull2"));
        list.add(
                new Unlock(
                        "perk:kit",
                        Category.PERK,
                        "Notfallvorrat",
                        "Jeder Tauchgang beginnt mit einem zusätzlichen Reparaturset.",
                        10,
                        null));
        list.add(
                new Unlock(
                        "perk:scrap",
                        Category.PERK,
                        "Schrottsucher",
                        "Jeder Tauchgang beginnt mit 25 Schrott.",
                        8,
                        null));
        tree(
                list,
                "perk:dmg",
                "Schneidwerk",
                "+8 % Werkzeug- und Modulschaden",
                new int[] {6, 9, 13, 18, 24},
                null);
        tree(
                list,
                "perk:spd",
                "Taktgeber",
                "+6 % Angriffstempo",
                new int[] {8, 13, 19},
                "perk:dmg1");
        tree(
                list,
                "perk:area",
                "Druckverstärker",
                "+8 % Wirkungsbereich",
                new int[] {8, 13, 19},
                "perk:dmg1");
        tree(
                list,
                "perk:xp",
                "Datenlink",
                "+12 % Überladung aus Splittern",
                new int[] {6, 10, 15},
                null);
        tree(list, "perk:mag", "Magnetspule", "+25 % Sammelradius", new int[] {5, 9}, "perk:xp1");
        tree(
                list,
                "perk:crit",
                "Zielsystem",
                "+4 % kritische Trefferchance",
                new int[] {9, 15},
                "perk:spd1");
        list.add(
                new Unlock(
                        "perk:choice",
                        Category.PERK,
                        "Erweiterte Auswahl",
                        "Jeder Levelaufstieg bietet eine zusätzliche Karte.",
                        25,
                        "perk:xp2"));
        String[][] cosmetics = {
            {"suit", "Anzugfarbe"},
            {"helmet", "Helmform"},
            {"visor", "Visierfarbe"},
            {"trim", "Metallton"}
        };
        int[] counts = {Cosmetics.SUITS, Cosmetics.HELMETS, Cosmetics.VISORS, Cosmetics.TRIMS};
        for (int c = 0; c < cosmetics.length; c++)
            for (int i = 2; i < counts[c]; i++)
                list.add(
                        new Unlock(
                                Cosmetics.unlockId(cosmetics[c][0], i),
                                Category.COSMETIC,
                                cosmetics[c][1] + " " + (i + 1),
                                "Neue Option in der Garderobe.",
                                2 + i / 2,
                                null));
        return List.copyOf(list);
    }
}
