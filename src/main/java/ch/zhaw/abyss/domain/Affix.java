package ch.zhaw.abyss.domain;

/** Elite-Eigenschaften. Elitegegner haben mehr Integrität und geben einen Datenkern. */
public enum Affix {
    NONE("", ""),
    ARMORED("Gepanzert", "Erleidet 40 % weniger Schaden."),
    VOLATILE("Instabil", "Explodiert beim Zerstören."),
    SWIFT("Flink", "Bewegt sich und greift deutlich schneller an."),
    REGENERATING("Selbstreparatur", "Stellt ohne Treffer Integrität wieder her."),
    SHIELDED("Schildgenerator", "Ein Schild fängt Schaden ab und lädt sich wieder auf.");
    private final String title, description;

    Affix(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Wirkung
     */
    public String description() {
        return description;
    }

    /**
     * @return {@code true} für alle Elitegegner
     */
    public boolean elite() {
        return this != NONE;
    }
}
