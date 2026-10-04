package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Skill-Bäume der Laufbahn. Der Tiefenbaum steht allen offen; weitere Bäume öffnen sich durch
 * Fortschritt. Kontobäume werden mit Punkten aus dem Laufbahnrang bezahlt, Klassenbäume mit Punkten
 * aus dem Rang der jeweiligen Klasse und wirken nur, wenn diese Klasse taucht.
 */
public enum SkillTree {
    /** Grundlagen für jede Taucherin: Kraft, Überladung, Rumpf. */
    DEPTH("TIEFE", "Tiefenbaum", null),
    /** Waffen und Geschosse, öffnet sich nach fünf Knoten im Tiefenbaum. */
    ARSENAL("ARSENAL", "Arsenal", null),
    /** Endgame-Verstärkungen, öffnet sich nach dem ersten Sieg. */
    ABYSS("ABGRUND", "Abgrund", null),
    /** Klassenbaum der Mechanikerin. */
    MECHANIC("MECH", "Mechanikerin", DiverClass.MECHANIC),
    /** Klassenbaum des Harpuniers. */
    HARPOONER("HARP", "Harpunier", DiverClass.HARPOONER),
    /** Klassenbaum der Schweisserin. */
    WELDER("SCHW", "Schweisserin", DiverClass.WELDER),
    /** Klassenbaum des Koloss. */
    TITAN("KOLOSS", "Koloss", DiverClass.TITAN),
    /** Klassenbaum der Funkerin. */
    SPARK("FUNK", "Funkerin", DiverClass.SPARK);

    /** Knoten im Tiefenbaum, ab denen sich das Arsenal öffnet. */
    public static final int ARSENAL_GATE = 5;

    private final String tab, title;
    private final DiverClass diver;

    SkillTree(String tab, String title, DiverClass diver) {
        this.tab = tab;
        this.title = title;
        this.diver = diver;
    }

    /**
     * @return kurze Beschriftung des Reiters
     */
    public String tab() {
        return tab;
    }

    /**
     * @return Name des Baums
     */
    public String title() {
        return title;
    }

    /**
     * @return Klasse eines Klassenbaums, sonst {@code null}
     */
    public DiverClass diver() {
        return diver;
    }

    /**
     * @return {@code true} für Klassenbäume
     */
    public boolean classTree() {
        return diver != null;
    }

    /**
     * @param profile Profil
     * @return {@code true}, wenn der Baum geöffnet ist
     */
    public boolean open(Profile profile) {
        return switch (this) {
            case DEPTH -> true;
            case ARSENAL -> profile.career().owned(DEPTH) >= ARSENAL_GATE;
            case ABYSS -> profile.wins() >= 1;
            default -> profile.owns(diver);
        };
    }

    /**
     * @return Bedingung, unter der sich der Baum öffnet
     */
    public String requirement() {
        return switch (this) {
            case DEPTH -> "Immer offen.";
            case ARSENAL -> ARSENAL_GATE + " Knoten im Tiefenbaum freischalten.";
            case ABYSS -> "Einmal die Brücke erobern.";
            default -> diver.title() + " im Archiv freischalten.";
        };
    }

    /**
     * @return Knoten dieses Baums
     */
    public List<SkillNode> nodes() {
        return SkillNode.catalog().stream().filter(node -> node.tree() == this).toList();
    }

    /** Wirkung eines Knotens. */
    public enum Perk {
        /** Werkzeugschaden. */
        DAMAGE,
        /** Angriffstempo. */
        ATTACK_SPEED,
        /** Wirkungsbereich. */
        AREA,
        /** Überladung aus Splittern. */
        XP,
        /** Sammelradius. */
        PICKUP,
        /** Kritische Trefferchance. */
        CRIT,
        /** Zusätzliche Karte bei Levelaufstiegen. */
        CHOICE,
        /** Maximale Integrität. */
        HEALTH,
        /** Weniger erlittener Schaden. */
        ARMOR,
        /** Lauftempo. */
        MOVE,
        /** Maximale Energie. */
        ENERGY,
        /** Kürzere Abklingzeiten. */
        COOLDOWN,
        /** Modul- und Explosionsschaden. */
        ABILITY,
        /** Kritischer Schaden. */
        CRIT_DAMAGE,
        /** Brandchance. */
        BURN,
        /** Zusätzliche Geschosse. */
        PROJECTILE,
        /** Zusätzliche Kreiselmesser. */
        ORBITAL,
        /** Zusätzliche Reparatursets. */
        KITS,
        /** Levelaufstiege zu Beginn. */
        START_LEVEL,
        /** Werkstattstufe zu Beginn. */
        START_WEAPON,
        /** Seltene Module zu Beginn. */
        START_RARE,
        /** Grössere Schwärme. */
        HORDES,
        /** Mehr Datenkerne nach jedem Tauchgang. */
        CORES,
        /** Mehr Waffenmeisterschaft. */
        MASTERY_XP,
        /** Schaltet eine Waffe frei. */
        WEAPON
    }

    /**
     * Knoten eines Skill-Baums.
     *
     * @param id eindeutige Kennung
     * @param tree Baum
     * @param title Name
     * @param effect Wirkung in einem Satz
     * @param cost Punkte
     * @param requires Kennungen, die vorher gekauft sein müssen
     * @param column Spalte in der Darstellung
     * @param row Zeile in der Darstellung
     * @param perk Art der Wirkung
     * @param amount Stärke der Wirkung
     * @param weapon freigeschaltete Waffe oder {@code null}
     */
    public record SkillNode(
            String id,
            SkillTree tree,
            String title,
            String effect,
            int cost,
            List<String> requires,
            int column,
            int row,
            Perk perk,
            double amount,
            Weapon weapon) {
        private static final List<SkillNode> CATALOG = build();

        /** Prüft und kopiert die Werte. */
        public SkillNode {
            requires = List.copyOf(requires);
            if (cost <= 0 || column < 0 || row < 0)
                throw new IllegalArgumentException("Ungültiger Knoten");
            if ((perk == Perk.WEAPON) != (weapon != null))
                throw new IllegalArgumentException("Waffenknoten ohne Waffe");
        }

        /**
         * @return alle Knoten aller Bäume
         */
        public static List<SkillNode> catalog() {
            return CATALOG;
        }

        /**
         * @param id Kennung
         * @return Knoten oder leer
         */
        public static Optional<SkillNode> find(String id) {
            return CATALOG.stream().filter(node -> node.id.equals(id)).findFirst();
        }

        private static SkillNode node(
                String id,
                SkillTree tree,
                String title,
                String effect,
                int cost,
                int column,
                int row,
                Perk perk,
                double amount,
                String... requires) {
            return new SkillNode(
                    id,
                    tree,
                    title,
                    effect,
                    cost,
                    List.of(requires),
                    column,
                    row,
                    perk,
                    amount,
                    null);
        }

        private static SkillNode weapon(
                String id, SkillTree tree, Weapon weapon, int cost, String... requires) {
            return new SkillNode(
                    id,
                    tree,
                    weapon.title(),
                    "Neue Waffe: " + weapon.description(),
                    cost,
                    List.of(requires),
                    3,
                    1,
                    Perk.WEAPON,
                    1,
                    weapon);
        }

        private static List<SkillNode> build() {
            var list = new ArrayList<SkillNode>();
            var d = DEPTH;
            list.add(
                    node(
                            "d.dmg1",
                            d,
                            "Schneidwerk I",
                            "+8 % Werkzeugschaden",
                            1,
                            0,
                            0,
                            Perk.DAMAGE,
                            .08));
            list.add(
                    node(
                            "d.dmg2",
                            d,
                            "Schneidwerk II",
                            "+8 % Werkzeugschaden",
                            1,
                            1,
                            0,
                            Perk.DAMAGE,
                            .08,
                            "d.dmg1"));
            list.add(
                    node(
                            "d.speed",
                            d,
                            "Taktgeber",
                            "+8 % Angriffstempo",
                            2,
                            2,
                            0,
                            Perk.ATTACK_SPEED,
                            .08,
                            "d.dmg2"));
            list.add(
                    node(
                            "d.crit",
                            d,
                            "Zielsystem",
                            "+5 % kritische Trefferchance",
                            2,
                            3,
                            0,
                            Perk.CRIT,
                            .05,
                            "d.speed"));
            list.add(
                    node(
                            "d.dmg3",
                            d,
                            "Schneidwerk III",
                            "+12 % Werkzeugschaden",
                            3,
                            4,
                            0,
                            Perk.DAMAGE,
                            .12,
                            "d.crit"));
            list.add(node("d.xp1", d, "Datenlink I", "+12 % Überladung", 1, 0, 1, Perk.XP, .12));
            list.add(
                    node(
                            "d.magnet",
                            d,
                            "Magnetspule",
                            "+30 % Sammelradius",
                            1,
                            1,
                            1,
                            Perk.PICKUP,
                            .3,
                            "d.xp1"));
            list.add(
                    node(
                            "d.xp2",
                            d,
                            "Datenlink II",
                            "+15 % Überladung",
                            2,
                            2,
                            1,
                            Perk.XP,
                            .15,
                            "d.magnet"));
            list.add(
                    node(
                            "d.choice",
                            d,
                            "Erweiterte Auswahl",
                            "+1 Karte bei jedem Levelaufstieg",
                            3,
                            3,
                            1,
                            Perk.CHOICE,
                            1,
                            "d.xp2"));
            list.add(
                    node(
                            "d.start",
                            d,
                            "Frühstart",
                            "2 Levelaufstiege direkt zu Beginn",
                            3,
                            4,
                            1,
                            Perk.START_LEVEL,
                            2,
                            "d.choice"));
            list.add(
                    node(
                            "d.hp1",
                            d,
                            "Verstärkter Anzug",
                            "+15 maximale Integrität",
                            1,
                            0,
                            2,
                            Perk.HEALTH,
                            15));
            list.add(
                    node(
                            "d.armor1",
                            d,
                            "Druckplatten",
                            "−6 % erlittener Schaden",
                            1,
                            1,
                            2,
                            Perk.ARMOR,
                            .06,
                            "d.hp1"));
            list.add(
                    node(
                            "d.area",
                            d,
                            "Druckfeld",
                            "+10 % Wirkungsbereich",
                            2,
                            2,
                            2,
                            Perk.AREA,
                            .1,
                            "d.armor1"));
            list.add(
                    node(
                            "d.hp2",
                            d,
                            "Titanrippen",
                            "+25 maximale Integrität",
                            2,
                            3,
                            2,
                            Perk.HEALTH,
                            25,
                            "d.area"));
            list.add(
                    node(
                            "d.armor2",
                            d,
                            "Tiefenpanzer",
                            "−10 % erlittener Schaden",
                            3,
                            4,
                            2,
                            Perk.ARMOR,
                            .1,
                            "d.hp2"));
            list.add(
                    node(
                            "d.eye",
                            d,
                            "Abgrundblick",
                            "+1 Geschoss für alle Schusswerkzeuge",
                            4,
                            5,
                            1,
                            Perk.PROJECTILE,
                            1,
                            "d.dmg3",
                            "d.start",
                            "d.armor2"));

            var a = ARSENAL;
            list.add(
                    node(
                            "a.forge1",
                            a,
                            "Schmiede I",
                            "Waffe beginnt auf Werkstattstufe 1",
                            1,
                            0,
                            0,
                            Perk.START_WEAPON,
                            1));
            list.add(
                    node(
                            "a.forge2",
                            a,
                            "Schmiede II",
                            "+1 Werkstattstufe zu Beginn",
                            2,
                            1,
                            0,
                            Perk.START_WEAPON,
                            1,
                            "a.forge1"));
            list.add(
                    node(
                            "a.forge3",
                            a,
                            "Schmiede III",
                            "+1 Werkstattstufe zu Beginn",
                            3,
                            2,
                            0,
                            Perk.START_WEAPON,
                            1,
                            "a.forge2"));
            list.add(
                    node(
                            "a.master",
                            a,
                            "Waffenkenner",
                            "+50 % Waffenmeisterschaft",
                            1,
                            0,
                            1,
                            Perk.MASTERY_XP,
                            .5));
            list.add(
                    node(
                            "a.hollow",
                            a,
                            "Hohlschliff",
                            "+40 % kritischer Schaden",
                            2,
                            1,
                            1,
                            Perk.CRIT_DAMAGE,
                            .4,
                            "a.master"));
            list.add(
                    node(
                            "a.loot",
                            a,
                            "Beutekiste",
                            "1 seltenes Modul zu Beginn",
                            3,
                            2,
                            1,
                            Perk.START_RARE,
                            1,
                            "a.hollow"));
            list.add(
                    node(
                            "a.rotor",
                            a,
                            "Kreiselwerk",
                            "+1 Kreiselmesser dauerhaft",
                            2,
                            0,
                            2,
                            Perk.ORBITAL,
                            1));
            list.add(
                    node(
                            "a.blast",
                            a,
                            "Sprengkunst",
                            "+12 % Wirkungsbereich",
                            2,
                            1,
                            2,
                            Perk.AREA,
                            .12,
                            "a.rotor"));
            list.add(
                    node(
                            "a.double",
                            a,
                            "Doppellauf",
                            "+1 Geschoss für alle Schusswerkzeuge",
                            4,
                            2,
                            2,
                            Perk.PROJECTILE,
                            1,
                            "a.blast"));
            list.add(
                    node(
                            "a.armory",
                            a,
                            "Volles Arsenal",
                            "+1 seltenes Modul zu Beginn",
                            5,
                            3,
                            1,
                            Perk.START_RARE,
                            1,
                            "a.forge3",
                            "a.loot",
                            "a.double"));

            var b = ABYSS;
            list.add(
                    node(
                            "b.cores1",
                            b,
                            "Kernsammler I",
                            "+25 % Datenkerne nach jedem Tauchgang",
                            2,
                            0,
                            0,
                            Perk.CORES,
                            .25));
            list.add(
                    node(
                            "b.cores2",
                            b,
                            "Kernsammler II",
                            "+25 % Datenkerne",
                            3,
                            1,
                            0,
                            Perk.CORES,
                            .25,
                            "b.cores1"));
            list.add(
                    node(
                            "b.cores3",
                            b,
                            "Kernsammler III",
                            "+50 % Datenkerne",
                            4,
                            2,
                            0,
                            Perk.CORES,
                            .5,
                            "b.cores2"));
            list.add(
                    node(
                            "b.bait",
                            b,
                            "Schwarmköder",
                            "+25 % grössere Schwärme",
                            2,
                            0,
                            1,
                            Perk.HORDES,
                            .25));
            list.add(
                    node(
                            "b.hunger",
                            b,
                            "Schwarmgier",
                            "+25 % Überladung",
                            3,
                            1,
                            1,
                            Perk.XP,
                            .25,
                            "b.bait"));
            list.add(
                    node(
                            "b.rush",
                            b,
                            "Tiefenrausch",
                            "+12 % Angriffstempo",
                            3,
                            2,
                            1,
                            Perk.ATTACK_SPEED,
                            .12,
                            "b.hunger"));
            list.add(
                    node(
                            "b.press",
                            b,
                            "Überdruck",
                            "+15 % Werkzeugschaden",
                            3,
                            0,
                            2,
                            Perk.DAMAGE,
                            .15));
            list.add(
                    node(
                            "b.cards",
                            b,
                            "Endlose Wahl",
                            "+1 Karte bei jedem Levelaufstieg",
                            4,
                            1,
                            2,
                            Perk.CHOICE,
                            1,
                            "b.press"));
            list.add(
                    node(
                            "b.rotor",
                            b,
                            "Abgrundrotor",
                            "+1 Kreiselmesser dauerhaft",
                            4,
                            2,
                            2,
                            Perk.ORBITAL,
                            1,
                            "b.cards"));
            list.add(
                    node(
                            "b.leviathan",
                            b,
                            "Leviathan",
                            "+1 Geschoss für alle Schusswerkzeuge",
                            6,
                            3,
                            1,
                            Perk.PROJECTILE,
                            1,
                            "b.cores3",
                            "b.rush",
                            "b.rotor"));

            classTree(
                    list,
                    MECHANIC,
                    "m",
                    new String[][] {
                        {"Werkzeugsatz", "+1 Reparaturset", "KITS", "1"},
                        {"Servorotor", "+1 Kreiselmesser dauerhaft", "ORBITAL", "1"},
                        {"Meisterzange", "+10 % Werkzeugschaden", "DAMAGE", ".1"},
                        {"Drehmoment", "+10 % Werkzeugschaden", "DAMAGE", ".1"},
                        {"Takt der Maschine", "+8 % Angriffstempo", "ATTACK_SPEED", ".08"},
                        {"Werkanzug", "+20 maximale Integrität", "HEALTH", "20"},
                        {"Notreparatur", "−8 % erlittener Schaden", "ARMOR", ".08"}
                    },
                    Weapon.DRILL);
            classTree(
                    list,
                    HARPOONER,
                    "h",
                    new String[][] {
                        {"Ruhige Hand", "+5 % kritische Trefferchance", "CRIT", ".05"},
                        {"Widerhaken", "+30 % kritischer Schaden", "CRIT_DAMAGE", ".3"},
                        {"Doppelschuss", "+1 Geschoss für alle Schusswerkzeuge", "PROJECTILE", "1"},
                        {"Schnelles Nachladen", "+10 % Angriffstempo", "ATTACK_SPEED", ".1"},
                        {"Weitsicht", "+30 % Sammelradius", "PICKUP", ".3"},
                        {"Flossen", "+8 % Lauftempo", "MOVE", ".08"},
                        {"Hakenschlag", "−10 % Abklingzeiten", "COOLDOWN", ".1"}
                    },
                    null);
            classTree(
                    list,
                    WELDER,
                    "w",
                    new String[][] {
                        {"Brennstoff", "+10 % Brandchance", "BURN", ".1"},
                        {"Weissglut", "+15 % Modul- und Explosionsschaden", "ABILITY", ".15"},
                        {"Flammenkegel", "+10 % Wirkungsbereich", "AREA", ".1"},
                        {"Schutzmaske", "−6 % erlittener Schaden", "ARMOR", ".06"},
                        {"Ruhige Naht", "+10 % Werkzeugschaden", "DAMAGE", ".1"},
                        {"Hitzekern", "+20 maximale Integrität", "HEALTH", "20"},
                        {"Aufladung", "+30 maximale Energie", "ENERGY", "30"}
                    },
                    Weapon.PLASMA);
            classTree(
                    list,
                    TITAN,
                    "t",
                    new String[][] {
                        {"Dicke Platten", "+30 maximale Integrität", "HEALTH", "30"},
                        {"Standfest", "−8 % erlittener Schaden", "ARMOR", ".08"},
                        {"Wucht", "+12 % Werkzeugschaden", "DAMAGE", ".12"},
                        {"Stampfer", "+12 % Wirkungsbereich", "AREA", ".12"},
                        {"Marschtritt", "+6 % Lauftempo", "MOVE", ".06"},
                        {"Eisenwille", "+1 Reparaturset", "KITS", "1"},
                        {"Zorn", "+8 % Angriffstempo", "ATTACK_SPEED", ".08"}
                    },
                    Weapon.SCYTHE);
            classTree(
                    list,
                    SPARK,
                    "s",
                    new String[][] {
                        {"Zusatzzellen", "+30 maximale Energie", "ENERGY", "30"},
                        {"Energiefluss", "−10 % Abklingzeiten", "COOLDOWN", ".1"},
                        {"Fokus", "+20 % Modul- und Explosionsschaden", "ABILITY", ".2"},
                        {"Statik", "+4 % kritische Trefferchance", "CRIT", ".04"},
                        {"Datenstrom", "+15 % Überladung", "XP", ".15"},
                        {"Relais", "+1 Karte bei jedem Levelaufstieg", "CHOICE", "1"},
                        {"Feldschild", "−6 % erlittener Schaden", "ARMOR", ".06"}
                    },
                    null);
            return List.copyOf(list);
        }

        /**
         * Klassenbaum: drei Äste mit 3, 2 und 2 Knoten und ein Schlussknoten, der eine Waffe
         * freischaltet oder eine starke Wirkung gibt.
         */
        private static void classTree(
                List<SkillNode> list,
                SkillTree tree,
                String prefix,
                String[][] rows,
                Weapon capstone) {
            int[][] layout = {{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {0, 2}, {1, 2}};
            int[] costs = {1, 2, 3, 1, 2, 1, 2};
            String[] ids = new String[rows.length];
            for (int i = 0; i < rows.length; i++) {
                ids[i] = prefix + "." + (i + 1);
                boolean chained = i != 0 && i != 3 && i != 5;
                list.add(
                        node(
                                ids[i],
                                tree,
                                rows[i][0],
                                rows[i][1],
                                costs[i],
                                layout[i][0],
                                layout[i][1],
                                Perk.valueOf(rows[i][2]),
                                Double.parseDouble(rows[i][3]),
                                chained ? new String[] {ids[i - 1]} : new String[0]));
            }
            String[] gate = {ids[2], ids[4], ids[6]};
            if (capstone != null) list.add(weapon(prefix + ".cap", tree, capstone, 4, gate));
            else if (tree == HARPOONER)
                list.add(
                        node(
                                prefix + ".cap",
                                tree,
                                "Sturmharpune",
                                "+1 Geschoss für alle Schusswerkzeuge",
                                4,
                                3,
                                1,
                                Perk.PROJECTILE,
                                1,
                                gate));
            else
                list.add(
                        node(
                                prefix + ".cap",
                                tree,
                                "Funkenkrone",
                                "+2 Kreiselmesser dauerhaft",
                                4,
                                3,
                                1,
                                Perk.ORBITAL,
                                2,
                                gate));
        }
    }
}
