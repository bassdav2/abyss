package ch.zhaw.abyss.domain;

/**
 * Dauerhafte Verstärkungen aus dem Tiefenbaum des Archivs. Sie gelten für jeden Tauchgang und
 * wirken als Faktoren auf die Werte der Figur.
 *
 * @param damage Faktor auf Werkzeug- und Modulschaden
 * @param attackSpeed Faktor auf das Angriffstempo
 * @param area Faktor auf den Wirkungsbereich von Schlägen und Explosionen
 * @param xpGain Faktor auf gesammelte Überladung
 * @param pickup Faktor auf den Sammelradius
 * @param crit zusätzliche kritische Trefferchance
 * @param choices zusätzliche Auswahl bei jedem Levelaufstieg
 */
public record MetaBonus(
        double damage,
        double attackSpeed,
        double area,
        double xpGain,
        double pickup,
        double crit,
        int choices) {
    /** Keine Verstärkung. */
    public static final MetaBonus NONE = new MetaBonus(1, 1, 1, 1, 1, 0, 0);

    /** Prüft die Werte. */
    public MetaBonus {
        if (damage <= 0 || attackSpeed <= 0 || area <= 0 || xpGain <= 0 || pickup <= 0)
            throw new IllegalArgumentException("Ungültige Verstärkung");
        if (crit < 0 || choices < 0) throw new IllegalArgumentException("Ungültige Verstärkung");
    }
}
