package ch.zhaw.abyss.domain;

/**
 * Handel in der Druckkapelle: ein Opfer gegen eine Belohnung. Die Regeln der Auszahlung liegen in
 * {@link GameRun#acceptDeal(ShrineDeal)}.
 */
public enum ShrineDeal {
    BLOOD_FOR_POWER("Leben opfern", "−25 % max. Leben dauerhaft", "Ein legendäres Upgrade"),
    GLASS("Gläserner Rumpf", "Verfluchtes Upgrade: −30 % Leben", "+40 % Schaden"),
    GREED(
            "Gier der Tiefe",
            "Verfluchtes Upgrade: Gegner +15 % Schaden",
            "+60 % Schrott, 40 Schrott sofort"),
    FEVER("Druckfieber", "Verfluchtes Upgrade: −1 Energie/s", "+25 % Angriffstempo"),
    SCRAP_FOR_HEALTH("Schrott opfern", "−40 Schrott", "Volles Leben und ein Medkit"),
    KITS_FOR_ITEM("Vorräte opfern", "Alle Medkits", "Zwei seltene Upgrades");
    private final String title, cost, reward;

    ShrineDeal(String title, String cost, String reward) {
        this.title = title;
        this.cost = cost;
        this.reward = reward;
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Beschreibung des Opfers
     */
    public String cost() {
        return cost;
    }

    /**
     * @return Beschreibung der Belohnung
     */
    public String reward() {
        return reward;
    }
}
