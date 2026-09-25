package ch.zhaw.abyss.application;

/**
 * Aussehen der Taucherfigur. Die Werte sind Indizes in die Farb- und Formkataloge der Darstellung;
 * Spielregeln hängen nicht davon ab.
 *
 * @param suit Anzugfarbe
 * @param helmet Helmform
 * @param visor Visierfarbe
 * @param trim Metallton von Helm und Beschlägen
 */
public record Cosmetics(int suit, int helmet, int visor, int trim) {
    /** Anzahl Anzugfarben. */
    public static final int SUITS = 8;

    /** Anzahl Helmformen. */
    public static final int HELMETS = 4;

    /** Anzahl Visierfarben. */
    public static final int VISORS = 6;

    /** Anzahl Metalltöne. */
    public static final int TRIMS = 4;

    /** Vorgabe: gelber Anzug, runder Stahlhelm, cyanfarbenes Visier. */
    public static final Cosmetics DEFAULT = new Cosmetics(0, 0, 0, 1);

    /** Prüft die Indizes. */
    public Cosmetics {
        if (suit < 0
                || suit >= SUITS
                || helmet < 0
                || helmet >= HELMETS
                || visor < 0
                || visor >= VISORS
                || trim < 0
                || trim >= TRIMS) throw new IllegalArgumentException("Ungültiges Aussehen");
    }

    /**
     * Liefert die Freischalt-Kennung einer Option; die jeweils erste Option jeder Kategorie ist
     * frei.
     *
     * @param category {@code suit}, {@code helmet}, {@code visor} oder {@code trim}
     * @param index Index der Option
     * @return Kennung im Freischaltkatalog oder {@code null} für freie Optionen
     */
    public static String unlockId(String category, int index) {
        return index < 2 ? null : "cosmetic:" + category + ":" + index;
    }
}
