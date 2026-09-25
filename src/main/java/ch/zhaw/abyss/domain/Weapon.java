package ch.zhaw.abyss.domain;

import ch.zhaw.abyss.domain.Swing.Style;

import java.util.List;

/**
 * Primärwaffen. Jede Waffe ist eine austauschbare Angriffsstrategie aus Kombinationsschlägen,
 * Luftangriff und Sondereigenschaften.
 */
public enum Weapon {
    WRENCH(
            "Rohrzange",
            "Ausgewogene Dreierkombination. Der dritte Schlag schleudert Gegner zurück.",
            List.of(
                    new Swing(Style.SLASH, .07, .10, .16, 165, 140, 20, 180, 70),
                    new Swing(Style.SLASH, .06, .10, .16, 165, 140, 20, 180, 70),
                    new Swing(Style.OVERHEAD, .12, .12, .24, 185, 175, 31, 430, 95)),
            new Swing(Style.AIR_SLASH, .05, .14, .14, 165, 175, 19, 160, 0),
            0,
            0,
            true),
    KNIVES(
            "Bergungsmesser",
            "Vier blitzschnelle Schnitte. Deutlich höhere kritische Trefferchance.",
            List.of(
                    new Swing(Style.SLASH, .03, .07, .09, 135, 120, 10, 70, 50),
                    new Swing(Style.SLASH, .03, .07, .09, 135, 120, 10, 70, 50),
                    new Swing(Style.SLASH, .03, .07, .09, 135, 120, 11, 70, 50),
                    new Swing(Style.THRUST, .06, .09, .16, 175, 120, 18, 240, 120)),
            new Swing(Style.AIR_SLASH, .03, .10, .10, 140, 150, 11, 80, 0),
            .15,
            0,
            true),
    TORCH_LANCE(
            "Schweisslanze",
            "Lange Plasmastösse. Treffer entzünden Gegner häufig.",
            List.of(
                    new Swing(Style.THRUST, .08, .09, .18, 245, 120, 16, 120, 80),
                    new Swing(Style.THRUST, .08, .09, .18, 245, 120, 16, 120, 80),
                    new Swing(Style.THRUST, .14, .12, .24, 275, 125, 26, 280, 130)),
            new Swing(Style.THRUST, .06, .12, .14, 225, 130, 16, 120, 0),
            0,
            .3,
            false),
    ANCHOR(
            "Ankerhammer",
            "Langsam und verheerend. Der zweite Schlag erzeugt Bodenwellen, in der Luft stampfst du"
                    + " auf.",
            List.of(
                    new Swing(Style.OVERHEAD, .17, .12, .26, 200, 190, 44, 520, 55),
                    new Swing(Style.OVERHEAD, .21, .14, .32, 220, 200, 60, 700, 75)),
            new Swing(Style.SLAM, .04, .9, .18, 170, 160, 45, 420, 0),
            0,
            0,
            false),
    HARPOON(
            "Harpunenwerfer",
            "Durchschlagende Harpunen auf Distanz. Jeder dritte Schuss trifft mehrere Gegner.",
            List.of(
                    new Swing(Style.SHOOT, .10, .04, .26, 0, 0, 24, 150, -45),
                    new Swing(Style.SHOOT, .10, .04, .26, 0, 0, 24, 150, -45),
                    new Swing(Style.SHOOT, .16, .04, .32, 0, 0, 34, 260, -80)),
            new Swing(Style.SHOOT, .08, .04, .22, 0, 0, 20, 120, 0),
            .05,
            0,
            false),
    TESLA(
            "Tesla-Handschuh",
            "Schnelle Schläge. Jeder Treffer springt als Blitz auf einen weiteren Gegner über.",
            List.of(
                    new Swing(Style.PUNCH, .04, .07, .11, 125, 110, 12, 90, 45),
                    new Swing(Style.PUNCH, .04, .07, .11, 125, 110, 12, 90, 45),
                    new Swing(Style.UPPERCUT, .07, .09, .18, 130, 160, 18, 260, 55)),
            new Swing(Style.PUNCH, .04, .08, .12, 125, 130, 12, 90, 0),
            0,
            0,
            false),
    GRAPPLE(
            "Enterhaken",
            "Weite Hakenstösse. Der dritte Stoss zieht getroffene Gegner zu dir heran.",
            List.of(
                    new Swing(Style.THRUST, .08, .09, .19, 265, 115, 15, 140, 40),
                    new Swing(Style.THRUST, .08, .09, .19, 265, 115, 15, 140, 40),
                    new Swing(Style.THRUST, .12, .11, .24, 330, 125, 22, -1, -20)),
            new Swing(Style.THRUST, .06, .12, .14, 240, 130, 14, 110, 0),
            .03,
            0,
            false);

    /** Höchste Werkstattstufe einer Waffe. */
    public static final int MAX_LEVEL = 3;

    private final String title, description;
    private final List<Swing> combo;
    private final Swing air;
    private final double critBonus, burnChance;
    private final boolean startsUnlocked;

    Weapon(
            String title,
            String description,
            List<Swing> combo,
            Swing air,
            double critBonus,
            double burnChance,
            boolean startsUnlocked) {
        this.title = title;
        this.description = description;
        this.combo = List.copyOf(combo);
        this.air = air;
        this.critBonus = critBonus;
        this.burnChance = burnChance;
        this.startsUnlocked = startsUnlocked;
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Beschreibung für Auswahl und Inventar
     */
    public String description() {
        return description;
    }

    /**
     * @return Bodenkombination in Ausführungsreihenfolge
     */
    public List<Swing> combo() {
        return combo;
    }

    /**
     * @return Angriff in der Luft
     */
    public Swing air() {
        return air;
    }

    /**
     * @return zusätzliche kritische Trefferchance
     */
    public double critBonus() {
        return critBonus;
    }

    /**
     * @return Grundchance, Gegner zu entzünden
     */
    public double burnChance() {
        return burnChance;
    }

    /**
     * @return {@code true}, wenn die Waffe ohne Freischaltung im Pool liegt
     */
    public boolean startsUnlocked() {
        return startsUnlocked;
    }

    /**
     * @return {@code true}, wenn jeder Treffer einen Kettenblitz auslöst
     */
    public boolean chains() {
        return this == TESLA;
    }

    /**
     * @return Kosten in Datenkernen für die dauerhafte Freischaltung
     */
    public int unlockCost() {
        return 10;
    }

    /**
     * @param level Werkstattstufe
     * @return Schadensfaktor der Stufe
     */
    public static double levelMultiplier(int level) {
        return 1 + .15 * Math.max(0, Math.min(MAX_LEVEL, level));
    }

    /**
     * @param level aktuelle Stufe
     * @return Schrottpreis für die nächste Stufe
     */
    public static int upgradePrice(int level) {
        return 30 + 20 * level;
    }
}
