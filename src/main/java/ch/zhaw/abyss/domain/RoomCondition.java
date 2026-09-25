package ch.zhaw.abyss.domain;

/**
 * Besonderer Zustand eines Kampfraums. Er verändert Regeln und Belohnung und wird bei der
 * Routenwahl angekündigt, damit Risiko und Gewinn abwägbar sind.
 */
public enum RoomCondition {
    /** Normaler Raum. */
    NONE("", ""),
    /** Das Licht ist ausgefallen; mehr Schrott als Ausgleich. */
    BLACKOUT("Stromausfall", "Nur Stirnlampe und Gegneraugen leuchten. +60 % Schrott."),
    /** Jede Welle bringt einen zusätzlichen Gegner; die Bergung ist selten oder besser. */
    ALARM("Alarmstufe Rot", "Ein zusätzlicher Gegner je Welle. Seltene Bergung."),
    /** Zusätzliche Dampfaustritte; dafür zwei weitere Vorratskisten. */
    LEAK("Druckleck", "Zwei zusätzliche Dampfaustritte. Zwei weitere Vorratskisten."),
    /** Überlebe, bis das Leck abgedichtet ist; Gegner strömen nach. Seltene Bergung. */
    BREACH("Hüllenbruch", "Halte 40 Sekunden durch, bis das Leck dicht ist. Seltene Bergung."),
    /** Das Boot krängt in Abständen; alles rutscht, Trümmer fallen. Mehr Schrott. */
    LIST("Schlagseite", "Das Boot krängt: alles rutscht, Trümmer fallen. +40 % Schrott.");

    private final String title;
    private final String description;

    RoomCondition(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * @return Anzeigename, leer für {@link #NONE}
     */
    public String title() {
        return title;
    }

    /**
     * @return Regel und Belohnung in einem Satz
     */
    public String description() {
        return description;
    }
}
