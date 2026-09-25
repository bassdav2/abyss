package ch.zhaw.abyss.domain;

/** Was die Interaktionstaste an der aktuellen Position der Figur auslöst. */
public enum Interaction {
    NONE("", ""),
    REWARD("E", "BERGEN"),
    WORKSHOP("E", "WERKSTATT"),
    MERCHANT("E", "HANDELN"),
    SHRINE("E", "KAPELLE"),
    EXIT("E", "WEITER"),
    CONSOLE("E", "SCHALTER"),
    LOCKED("", "VERRIEGELT");
    private final String key, label;

    Interaction(String key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * @return Taste für die Anzeige
     */
    public String key() {
        return key;
    }

    /**
     * @return Kurzbeschreibung für die Anzeige
     */
    public String label() {
        return label;
    }
}
