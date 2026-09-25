package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resonanzen: Paare passiver Module, die zusammen einen zusätzlichen Bonus freischalten. Die Werte
 * wirken über {@link StatSheet}; die Resonanz selbst ist reine Bedingung.
 */
public enum Synergy {
    /** Brand und Angriffstempo. */
    FIRESTORM("Feuersturm", Item.IGNITER, Item.OVERCLOCK, "Brand wirkt 50 % stärker."),
    /** Kälte und Kettenblitz. */
    PERMAFROST("Permafrost", Item.CRYO_COIL, Item.ARC_COIL, "+15 % Kältechance."),
    /** Kritische Chance und kritischer Schaden. */
    MARKSMAN("Scharfschütze", Item.LENS, Item.CRIT_DAMAGE, "+8 % kritische Trefferchance."),
    /** Panzerung und Schild. */
    FORTRESS("Festung", Item.PLATING, Item.SHIELD_CELL, "+20 Schild."),
    /** Lebensraub und Heilung pro Abschuss. */
    BLOODLINE("Blutkreislauf", Item.NANITES, Item.RECOVERY, "+3 % Lebensraub."),
    /** Energie speichern und zurückgewinnen. */
    POWER_LOOP(
            "Energiekreislauf",
            Item.CAPACITOR,
            Item.SIPHON,
            "+20 Energie und +2 Energie pro Sekunde."),
    /** Tempo und Abklingzeiten. */
    SLIPSTREAM("Sturmläufer", Item.THRUSTER, Item.COOLANT, "−10 % Abklingzeiten, +5 % Tempo."),
    /** Schrott und Vorräte. */
    SCRAPPER(
            "Schrotthändler",
            Item.MAGNET,
            Item.TOOLBELT,
            "+20 % Schrott und +1 Reparaturset-Kapazität."),
    /** Rückstoss und Schlagkraft. */
    HEAVYWEIGHT("Schwergewicht", Item.BALLAST, Item.SERVO, "+10 % Schaden.");

    private final String title;
    private final Item first, second;
    private final String effect;

    Synergy(String title, Item first, Item second, String effect) {
        this.title = title;
        this.first = first;
        this.second = second;
        this.effect = effect;
    }

    /**
     * @return Anzeigename
     */
    public String title() {
        return title;
    }

    /**
     * @return erstes beteiligtes Modul
     */
    public Item first() {
        return first;
    }

    /**
     * @return zweites beteiligtes Modul
     */
    public Item second() {
        return second;
    }

    /**
     * @return Bonus in einem Satz
     */
    public String effect() {
        return effect;
    }

    /**
     * @param items installierte Module mit Stufen
     * @return {@code true}, wenn beide Module installiert sind
     */
    public boolean active(Map<Item, Integer> items) {
        return items.getOrDefault(first, 0) > 0 && items.getOrDefault(second, 0) > 0;
    }

    /**
     * @param items installierte Module mit Stufen
     * @return alle aktiven Resonanzen
     */
    public static List<Synergy> activeIn(Map<Item, Integer> items) {
        var result = new ArrayList<Synergy>();
        for (var synergy : values()) if (synergy.active(items)) result.add(synergy);
        return List.copyOf(result);
    }

    /**
     * Resonanzen, die ein zusätzliches Modul neu vervollständigen würde.
     *
     * @param items installierte Module mit Stufen
     * @param candidate angebotenes Modul
     * @return neu entstehende Resonanzen
     */
    public static List<Synergy> completedBy(Map<Item, Integer> items, Item candidate) {
        var result = new ArrayList<Synergy>();
        for (var synergy : values())
            if (!synergy.active(items)
                    && (synergy.first == candidate && items.getOrDefault(synergy.second, 0) > 0
                            || synergy.second == candidate
                                    && items.getOrDefault(synergy.first, 0) > 0))
                result.add(synergy);
        return List.copyOf(result);
    }
}
