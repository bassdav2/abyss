package ch.zhaw.abyss.domain;

/** Seltenheit eines Moduls. Bestimmt Angebotsgewicht und Händlerpreis. */
public enum Rarity {
    COMMON("Standard", 30, 70),
    RARE("Selten", 55, 24),
    LEGENDARY("Legendär", 90, 6),
    CURSED("Verflucht", 0, 0);

    private final String title;
    private final int price;
    private final int weight;

    Rarity(String title, int price, int weight) {
        this.title = title;
        this.price = price;
        this.weight = weight;
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Grundpreis beim Schwarzmarkt in Schrott
     */
    public int price() {
        return price;
    }

    /**
     * @return relatives Gewicht bei normalen Bergungen
     */
    public int weight() {
        return weight;
    }
}
