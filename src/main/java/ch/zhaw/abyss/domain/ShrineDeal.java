package ch.zhaw.abyss.domain;

/**
 * Handel in der Druckkapelle: ein Opfer gegen eine Belohnung. Die Regeln der Auszahlung liegen in
 * {@link GameRun#acceptDeal(ShrineDeal)}.
 */
public enum ShrineDeal {
    BLOOD_FOR_POWER("Integrität opfern", "−25 % max. Integrität dauerhaft", "Ein legendäres Modul"),
    GLASS("Gläserner Rumpf", "Verfluchtes Modul: −30 % Integrität", "+40 % Schaden"),
    GREED(
            "Gier der Tiefe",
            "Verfluchtes Modul: Gegner +15 % Schaden",
            "+60 % Schrott, 40 Schrott sofort"),
    FEVER("Druckfieber", "Verfluchtes Modul: −1 Energie/s", "+25 % Angriffstempo"),
    SCRAP_FOR_HEALTH("Schrott opfern", "−40 Schrott", "Volle Integrität und ein Reparaturset"),
    KITS_FOR_ITEM("Vorräte opfern", "Alle Reparatursets", "Zwei seltene Module");
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
