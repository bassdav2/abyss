package ch.zhaw.abyss.application;

import ch.zhaw.abyss.application.SkillTree.Perk;
import ch.zhaw.abyss.application.SkillTree.SkillNode;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.MetaBonus;
import ch.zhaw.abyss.domain.Weapon;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dauerhafte Laufbahn: Erfahrung für den Laufbahnrang und je Klasse, Waffenmeisterschaft und
 * gekaufte Knoten der Skill-Bäume. Jeder Rang bringt einen Punkt; Kontobäume teilen sich die Punkte
 * des Laufbahnrangs, Klassenbäume nutzen den Rang ihrer Klasse.
 *
 * @param xp Laufbahnerfahrung
 * @param diverXp Erfahrung je Klasse
 * @param weaponXp Meisterschaftserfahrung je Waffe
 * @param nodes gekaufte Knoten
 */
public record Career(
        long xp, Map<DiverClass, Long> diverXp, Map<Weapon, Long> weaponXp, Set<String> nodes) {
    /** Leere Laufbahn. */
    public static final Career NEW = new Career(0, Map.of(), Map.of(), Set.of());

    /** Kumulierte Meisterschaftserfahrung je Stufe 1 bis 10. */
    private static final long[] MASTERY = {
        150, 400, 800, 1400, 2200, 3200, 4500, 6000, 8000, 10500
    };

    /** Prüft und kopiert die Werte; unbekannte Knoten werden verworfen. */
    public Career {
        if (xp < 0) throw new IllegalArgumentException("Ungültige Laufbahn");
        var divers = new EnumMap<DiverClass, Long>(DiverClass.class);
        if (diverXp != null)
            diverXp.forEach(
                    (diver, value) -> {
                        if (value > 0) divers.put(diver, value);
                    });
        var weapons = new EnumMap<Weapon, Long>(Weapon.class);
        if (weaponXp != null)
            weaponXp.forEach(
                    (weapon, value) -> {
                        if (value > 0) weapons.put(weapon, value);
                    });
        diverXp = Map.copyOf(divers);
        weaponXp = Map.copyOf(weapons);
        var known = new HashSet<String>();
        if (nodes != null)
            for (String id : nodes) if (SkillNode.find(id).isPresent()) known.add(id);
        nodes = Set.copyOf(known);
    }

    /**
     * @param rank aktueller Rang
     * @return Erfahrung vom Rang zum nächsten Rang
     */
    public static long xpForRank(int rank) {
        return 400 + 150L * rank;
    }

    /**
     * @param xp gesammelte Erfahrung
     * @return erreichter Rang
     */
    public static int rankFor(long xp) {
        int rank = 0;
        long need = xpForRank(0);
        while (xp >= need && rank < 9999) {
            xp -= need;
            rank++;
            need = xpForRank(rank);
        }
        return rank;
    }

    /**
     * @param xp gesammelte Erfahrung
     * @return Anteil 0 bis 1 auf dem Weg zum nächsten Rang
     */
    public static double rankProgress(long xp) {
        int rank = 0;
        long need = xpForRank(0);
        while (xp >= need && rank < 9999) {
            xp -= need;
            rank++;
            need = xpForRank(rank);
        }
        return xp / (double) need;
    }

    /**
     * @return Laufbahnrang
     */
    public int rank() {
        return rankFor(xp);
    }

    /**
     * @param diver Klasse
     * @return Rang der Klasse
     */
    public int rank(DiverClass diver) {
        return rankFor(diverXp.getOrDefault(diver, 0L));
    }

    /**
     * @param diver Klasse
     * @return Erfahrung der Klasse
     */
    public long xp(DiverClass diver) {
        return diverXp.getOrDefault(diver, 0L);
    }

    /**
     * @param weapon Waffe
     * @return Meisterschaftsstufe 0 bis 10
     */
    public int mastery(Weapon weapon) {
        long value = weaponXp.getOrDefault(weapon, 0L);
        int level = 0;
        while (level < MASTERY.length && value >= MASTERY[level]) level++;
        return level;
    }

    /**
     * @param weapon Waffe
     * @return Anteil 0 bis 1 zur nächsten Meisterschaftsstufe, 1 auf der Höchststufe
     */
    public double masteryProgress(Weapon weapon) {
        int level = mastery(weapon);
        if (level >= MASTERY.length) return 1;
        long value = weaponXp.getOrDefault(weapon, 0L);
        long from = level == 0 ? 0 : MASTERY[level - 1];
        return (value - from) / (double) (MASTERY[level] - from);
    }

    /**
     * @param tree Baum
     * @return gekaufte Knoten dieses Baums
     */
    public int owned(SkillTree tree) {
        int count = 0;
        for (String id : nodes)
            if (SkillNode.find(id).map(node -> node.tree() == tree).orElse(false)) count++;
        return count;
    }

    private int spent(SkillTree tree) {
        int sum = 0;
        for (String id : nodes) {
            var node = SkillNode.find(id).orElseThrow();
            boolean same = tree.classTree() ? node.tree() == tree : !node.tree().classTree();
            if (same) sum += node.cost();
        }
        return sum;
    }

    /**
     * Verfügbare Punkte für einen Baum: Kontobäume teilen sich den Laufbahnrang, Klassenbäume
     * nutzen den Rang ihrer Klasse.
     *
     * @param tree Baum
     * @return freie Punkte
     */
    public int points(SkillTree tree) {
        int earned = tree.classTree() ? rank(tree.diver()) : rank();
        return Math.max(0, earned - spent(tree));
    }

    /**
     * @param node Knoten
     * @return {@code true}, wenn alle Voraussetzungen gekauft sind
     */
    public boolean reachable(SkillNode node) {
        return nodes.containsAll(node.requires());
    }

    /**
     * @param node Knoten
     * @param profile Profil für die Öffnung des Baums
     * @return {@code true}, wenn der Knoten jetzt gekauft werden kann
     */
    public boolean canBuy(SkillNode node, Profile profile) {
        return !nodes.contains(node.id())
                && node.tree().open(profile)
                && reachable(node)
                && points(node.tree()) >= node.cost();
    }

    /**
     * @param node Knoten
     * @return Laufbahn mit dem gekauften Knoten
     */
    public Career with(SkillNode node) {
        var copy = new HashSet<>(nodes);
        copy.add(node.id());
        return new Career(xp, diverXp, weaponXp, copy);
    }

    /**
     * Verbucht Erfahrung eines Tauchgangs.
     *
     * @param gained Erfahrung für Laufbahn und Klasse
     * @param diver gespielte Klasse
     * @param weaponGains Meisterschaftserfahrung je Waffe vor Boni
     * @return neue Laufbahn
     */
    public Career gain(long gained, DiverClass diver, Map<Weapon, Integer> weaponGains) {
        var divers = new EnumMap<DiverClass, Long>(DiverClass.class);
        divers.putAll(diverXp);
        divers.merge(diver, Math.max(0, gained), Long::sum);
        var weapons = new EnumMap<Weapon, Long>(Weapon.class);
        weapons.putAll(weaponXp);
        double factor = 1 + sum(Perk.MASTERY_XP, null);
        weaponGains.forEach(
                (weapon, kills) -> weapons.merge(weapon, Math.round(kills * factor), Long::sum));
        return new Career(xp + Math.max(0, gained), divers, weapons, nodes);
    }

    /**
     * Summe einer Wirkung über gekaufte Knoten der Kontobäume und des Klassenbaums.
     *
     * @param perk Wirkung
     * @param diver tauchende Klasse oder {@code null} nur für Kontobäume
     * @return Summe der Stärken
     */
    public double sum(Perk perk, DiverClass diver) {
        double total = 0;
        for (String id : nodes) {
            var node = SkillNode.find(id).orElseThrow();
            if (node.perk() != perk) continue;
            if (node.tree().classTree() && node.tree().diver() != diver) continue;
            total += node.amount();
        }
        return total;
    }

    /**
     * Rechnet die Laufbahn in Verstärkungen für einen Tauchgang um.
     *
     * @param diver tauchende Klasse
     * @return Verstärkungen
     */
    public MetaBonus bonus(DiverClass diver) {
        var mastery = new EnumMap<Weapon, Integer>(Weapon.class);
        for (var weapon : Weapon.values()) mastery.put(weapon, mastery(weapon));
        return new MetaBonus(
                1 + sum(Perk.DAMAGE, diver),
                1 + sum(Perk.ATTACK_SPEED, diver),
                1 + sum(Perk.AREA, diver),
                1 + sum(Perk.XP, diver),
                1 + sum(Perk.PICKUP, diver),
                sum(Perk.CRIT, diver),
                (int) sum(Perk.CHOICE, diver),
                sum(Perk.HEALTH, diver),
                Math.max(.3, 1 - sum(Perk.ARMOR, diver)),
                1 + sum(Perk.MOVE, diver),
                sum(Perk.ENERGY, diver),
                Math.max(.3, 1 - sum(Perk.COOLDOWN, diver)),
                1 + sum(Perk.ABILITY, diver),
                sum(Perk.CRIT_DAMAGE, diver),
                sum(Perk.BURN, diver),
                (int) sum(Perk.PROJECTILE, diver),
                (int) sum(Perk.ORBITAL, diver),
                (int) sum(Perk.KITS, diver),
                (int) sum(Perk.START_LEVEL, diver),
                (int) sum(Perk.START_WEAPON, diver),
                (int) sum(Perk.START_RARE, diver),
                1 + sum(Perk.HORDES, diver),
                mastery);
    }
}
