package ch.zhaw.abyss.domain;

public enum ActiveModule {
    PULSE("Schildimpuls", "Ein Druckimpuls trifft und betäubt Gegner in deiner Nähe.", 35, 6.5),
    ARC("Lichtbogen", "Ein elektrisches Geschoss durchschlägt mehrere Gegner.", 30, 4.5),
    AEGIS("Druckschild", "Vier Sekunden Schutz. Eingehender Schaden sinkt deutlich.", 40, 9.0);

    private final String title;
    private final String description;
    private final int cost;
    private final double cooldown;

    ActiveModule(String title, String description, int cost, double cooldown) {
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.cooldown = cooldown;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public int cost() {
        return cost;
    }

    public double cooldown() {
        return cooldown;
    }
}
