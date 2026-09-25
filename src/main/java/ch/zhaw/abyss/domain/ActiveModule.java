package ch.zhaw.abyss.domain;

/** Aktive Fähigkeiten. Sie verbrauchen Energie und haben eine Abklingzeit. */
public enum ActiveModule {
    PULSE(
            "Schildimpuls",
            "Ein Druckimpuls trifft, betäubt und stösst Gegner in deiner Nähe weg.",
            35,
            6.5,
            true),
    ARC(
            "Lichtbogen",
            "Ein elektrisches Geschoss durchschlägt alle Gegner in einer Linie.",
            30,
            4.5,
            false),
    AEGIS(
            "Druckschild",
            "Vier Sekunden Schutz: 75 % weniger Schaden und Rückstoss für Angreifer.",
            40,
            9.0,
            false),
    TORPEDO(
            "Minitorpedo",
            "Ein suchender Torpedo explodiert in einem grossen Radius.",
            45,
            7.0,
            false),
    SONAR(
            "Sonarpuls",
            "Markiert alle Gegner: Sie erleiden sechs Sekunden lang 50 % mehr Schaden.",
            30,
            10.0,
            false),
    CRYO(
            "Kryogranate",
            "Eine Granate friert Gegner im Umkreis für zwei Sekunden ein.",
            40,
            8.0,
            false),
    DRONE(
            "Wartungsdrohne",
            "Eine Begleitdrohne feuert zwölf Sekunden lang auf nahe Gegner.",
            50,
            16.0,
            false),
    OVERDRIVE(
            "Überlastung",
            "Sechs Sekunden: +40 % Angriffs- und Lauftempo, Treffer laden Energie.",
            45,
            14.0,
            false);

    private final String title;
    private final String description;
    private final int cost;
    private final double cooldown;
    private final boolean startsUnlocked;

    ActiveModule(
            String title, String description, int cost, double cooldown, boolean startsUnlocked) {
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.cooldown = cooldown;
        this.startsUnlocked = startsUnlocked;
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Wirkung für Auswahl und HUD
     */
    public String description() {
        return description;
    }

    /**
     * @return Energiekosten pro Einsatz
     */
    public int cost() {
        return cost;
    }

    /**
     * @return Abklingzeit in Sekunden vor Modulen
     */
    public double cooldown() {
        return cooldown;
    }

    /**
     * @return {@code true}, wenn das Modul von Beginn an verfügbar ist
     */
    public boolean startsUnlocked() {
        return startsUnlocked;
    }

    /**
     * @return Kosten in Datenkernen für die dauerhafte Freischaltung
     */
    public int unlockCost() {
        return switch (this) {
            case PULSE -> 0;
            case ARC, SONAR -> 6;
            case AEGIS, CRYO -> 8;
            case TORPEDO, OVERDRIVE -> 10;
            case DRONE -> 14;
        };
    }
}
