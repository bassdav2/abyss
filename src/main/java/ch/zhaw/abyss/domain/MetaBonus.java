package ch.zhaw.abyss.domain;

import java.util.EnumMap;
import java.util.Map;

/**
 * Dauerhafte Verstärkungen aus der Laufbahn: Knoten der Skill-Bäume und Waffenmeisterschaft. Sie
 * gelten für jeden Tauchgang und wirken als Faktoren oder Zuschläge auf die Werte der Figur.
 *
 * @param damage Faktor auf Werkzeugschaden
 * @param attackSpeed Faktor auf das Angriffstempo
 * @param area Faktor auf den Wirkungsbereich von Schlägen und Explosionen
 * @param xpGain Faktor auf gesammelte Überladung
 * @param pickup Faktor auf den Sammelradius
 * @param crit zusätzliche kritische Trefferchance
 * @param choices zusätzliche Karten bei jedem Levelaufstieg
 * @param health zusätzliche maximale Integrität
 * @param armor Faktor auf erlittenen Schaden (kleiner ist besser)
 * @param move Faktor auf das Lauftempo
 * @param energy zusätzliche maximale Energie
 * @param cooldown Faktor auf Abklingzeiten (kleiner ist besser)
 * @param abilityDamage Faktor auf Modul- und Explosionsschaden
 * @param critDamage zusätzlicher kritischer Schaden
 * @param burn zusätzliche Brandchance
 * @param projectiles zusätzliche Geschosse
 * @param orbitals zusätzliche Kreiselmesser
 * @param kits zusätzliche Reparatursets beim Start
 * @param startLevel Levelaufstiege, die direkt zu Beginn gewählt werden
 * @param startWeaponLevel Werkstattstufe der Waffe zu Beginn
 * @param startRare Anzahl seltener Module zu Beginn
 * @param hordes Faktor auf die Grösse der Schwärme
 * @param mastery Meisterschaftsstufe je Waffe
 */
public record MetaBonus(
        double damage,
        double attackSpeed,
        double area,
        double xpGain,
        double pickup,
        double crit,
        int choices,
        double health,
        double armor,
        double move,
        double energy,
        double cooldown,
        double abilityDamage,
        double critDamage,
        double burn,
        int projectiles,
        int orbitals,
        int kits,
        int startLevel,
        int startWeaponLevel,
        int startRare,
        double hordes,
        Map<Weapon, Integer> mastery) {
    /** Keine Verstärkung. */
    public static final MetaBonus NONE =
            new MetaBonus(
                    1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, Map.of());

    /** Höchste Meisterschaftsstufe einer Waffe. */
    public static final int MAX_MASTERY = 10;

    /** Prüft die Werte und kopiert die Meisterschaften. */
    public MetaBonus {
        if (damage <= 0
                || attackSpeed <= 0
                || area <= 0
                || xpGain <= 0
                || pickup <= 0
                || armor <= 0
                || move <= 0
                || cooldown <= 0
                || abilityDamage <= 0
                || hordes <= 0) throw new IllegalArgumentException("Ungültige Verstärkung");
        if (crit < 0
                || choices < 0
                || health < 0
                || energy < 0
                || critDamage < 0
                || burn < 0
                || projectiles < 0
                || orbitals < 0
                || kits < 0
                || startLevel < 0
                || startWeaponLevel < 0
                || startRare < 0) throw new IllegalArgumentException("Ungültige Verstärkung");
        var copy = new EnumMap<Weapon, Integer>(Weapon.class);
        if (mastery != null)
            mastery.forEach(
                    (weapon, level) -> copy.put(weapon, Math.max(0, Math.min(MAX_MASTERY, level))));
        mastery = Map.copyOf(copy);
    }

    /**
     * Kurzform mit den Grundwerten des Tiefenbaums; alle übrigen Verstärkungen sind neutral.
     *
     * @param damage Faktor auf Werkzeugschaden
     * @param attackSpeed Faktor auf das Angriffstempo
     * @param area Faktor auf den Wirkungsbereich
     * @param xpGain Faktor auf Überladung
     * @param pickup Faktor auf den Sammelradius
     * @param crit zusätzliche kritische Trefferchance
     * @param choices zusätzliche Karten
     */
    public MetaBonus(
            double damage,
            double attackSpeed,
            double area,
            double xpGain,
            double pickup,
            double crit,
            int choices) {
        this(
                damage,
                attackSpeed,
                area,
                xpGain,
                pickup,
                crit,
                choices,
                0,
                1,
                1,
                0,
                1,
                1,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                1,
                Map.of());
    }

    /**
     * @param weapon Waffe
     * @return Meisterschaftsstufe 0 bis {@value #MAX_MASTERY}
     */
    public int mastery(Weapon weapon) {
        return mastery.getOrDefault(weapon, 0);
    }

    /**
     * @param weapon Waffe
     * @return Schadensfaktor der Meisterschaft: +4 % je Stufe
     */
    public double masteryDamage(Weapon weapon) {
        return 1 + .04 * mastery(weapon);
    }

    /**
     * @param weapon Waffe
     * @return zusätzliche kritische Trefferchance der Meisterschaft ab Stufe 5 und 10
     */
    public double masteryCrit(Weapon weapon) {
        int level = mastery(weapon);
        return (level >= 5 ? .05 : 0) + (level >= 10 ? .05 : 0);
    }
}
