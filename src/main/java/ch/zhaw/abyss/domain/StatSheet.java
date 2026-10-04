package ch.zhaw.abyss.domain;

import java.util.Map;

/**
 * Unveränderliche, abgeleitete Kampfwerte. Wird nach jeder Moduländerung aus Klasse, Waffenstufe
 * und Modulen neu berechnet, damit keine Werte schleichend auseinanderlaufen.
 *
 * @param maxHealth maximale Integrität
 * @param maxEnergy maximale Energie
 * @param damage Schadensfaktor für Werkzeug und Geschosse
 * @param abilityDamage Schadensfaktor für aktive Module
 * @param attackSpeed Tempofaktor; 1.2 bedeutet 20 % schneller
 * @param reach Reichweitenfaktor
 * @param moveSpeed Lauftempofaktor
 * @param cooldown Faktor für Abklingzeiten; kleiner ist besser
 * @param moduleCooldown zusätzlicher Faktor nur für das aktive Modul
 * @param damageTaken Faktor für erlittenen Schaden
 * @param critChance kritische Trefferchance; über 1 bedeutet Überkritik
 * @param critDamage kritischer Schadensfaktor
 * @param knockback Rückstossfaktor
 * @param lifesteal Anteil des Schadens, der heilt
 * @param extraJumps zusätzliche Sprünge in der Luft
 * @param energyRegen Energie pro Sekunde
 * @param scrapGain Schrottfaktor
 * @param burnChance Brandchance pro Treffer
 * @param chillChance Kältechance pro Treffer
 * @param burnPower Faktor für Brandschaden
 * @param maxShield Energieschild aus Schildzellen
 * @param maxRepairKits Kapazität für Reparatursets
 * @param magnetRadius Sammelradius für Schrott
 */
public record StatSheet(
        double maxHealth,
        double maxEnergy,
        double damage,
        double abilityDamage,
        double attackSpeed,
        double reach,
        double moveSpeed,
        double cooldown,
        double moduleCooldown,
        double damageTaken,
        double critChance,
        double critDamage,
        double knockback,
        double lifesteal,
        int extraJumps,
        double energyRegen,
        double scrapGain,
        double burnChance,
        double chillChance,
        double burnPower,
        double maxShield,
        int maxRepairKits,
        double magnetRadius,
        double area,
        double xpGain,
        int extraProjectiles) {

    /**
     * Berechnet die Werte einer Figur.
     *
     * @param diver gewählte Klasse
     * @param weapon aktuelle Waffe
     * @param weaponLevel Werkstattstufe der Waffe
     * @param items installierte Module mit Stufen
     * @param bonusHealth dauerhafte Bonusintegrität aus dem Archiv
     * @return abgeleitete Werte
     */
    public static StatSheet compute(
            DiverClass diver,
            Weapon weapon,
            int weaponLevel,
            Map<Item, Integer> items,
            double bonusHealth) {
        return compute(diver, weapon, weaponLevel, items, bonusHealth, MetaBonus.NONE);
    }

    /**
     * Berechnet die Werte einschliesslich dauerhafter Verstärkungen. Gleichartige Module stapeln
     * multiplikativ, damit sich ein spezialisierter Build deutlich stärker anfühlt.
     *
     * @param diver Klasse
     * @param weapon Waffe
     * @param weaponLevel Werkstattstufe
     * @param items installierte Module mit Stufen
     * @param bonusHealth zusätzliche Integrität
     * @param meta Verstärkungen aus dem Tiefenbaum
     * @return Werte
     */
    public static StatSheet compute(
            DiverClass diver,
            Weapon weapon,
            int weaponLevel,
            Map<Item, Integer> items,
            double bonusHealth,
            MetaBonus meta) {
        var s = new Stacks(items);
        var resonance = Synergy.activeIn(items);
        double health =
                (diver.health()
                                + bonusHealth
                                + meta.health()
                                + 20 * s.of(Item.MEDICAL)
                                + 6 * s.of(Item.OVERCHARGE))
                        * (s.has(Item.GLASS_HULL) ? .7 : 1);
        double energy =
                100
                        + meta.energy()
                        + 20 * s.of(Item.CAPACITOR)
                        + (diver == DiverClass.SPARK ? 40 : 0);
        double damage =
                Math.pow(1.15, s.of(Item.SERVO))
                        * (s.has(Item.GLASS_HULL) ? 1.4 : 1)
                        * Weapon.levelMultiplier(weaponLevel)
                        * meta.damage()
                        * meta.masteryDamage(weapon)
                        * Math.pow(1.06, s.of(Item.OVERCHARGE));
        double ability =
                Math.pow(1.15, s.of(Item.CAPACITOR))
                        * (s.has(Item.SINGULARITY) ? 1 + .25 * s.of(Item.SINGULARITY) : 1)
                        * Weapon.levelMultiplier(weaponLevel / 2)
                        * meta.damage()
                        * meta.abilityDamage();
        double attackSpeed =
                Math.pow(1.1, s.of(Item.OVERCLOCK))
                        * (s.has(Item.FEVER) ? 1.25 : 1)
                        * meta.attackSpeed()
                        * Math.pow(1.06, s.of(Item.OVERCHARGE));
        double reach = 1 + .15 * s.of(Item.LANCE);
        double move = diver.speed() * (1 + .10 * s.of(Item.THRUSTER)) * meta.move();
        double cooldown = Math.pow(.88, s.of(Item.COOLANT)) * meta.cooldown();
        double moduleCooldown = Math.pow(.5, s.of(Item.SINGULARITY));
        double taken =
                Math.pow(.9, s.of(Item.PLATING))
                        * (diver == DiverClass.TITAN ? .8 : 1)
                        * meta.armor();
        double crit =
                .05
                        + .07 * s.of(Item.LENS)
                        + weapon.critBonus()
                        + (diver == DiverClass.HARPOONER ? .10 : 0)
                        + meta.crit()
                        + meta.masteryCrit(weapon);
        if (s.has(Item.DEATH_EYE)) crit += .25;
        double critDamage = 2 + .4 * s.of(Item.CRIT_DAMAGE) + meta.critDamage();
        double knockback = 1 + .35 * s.of(Item.BALLAST);
        double lifesteal = .04 * s.of(Item.NANITES) + (s.has(Item.BLOOD_PACT) ? .08 : 0);
        double regen =
                (5.5 + s.of(Item.SIPHON)) * (diver == DiverClass.SPARK ? 2 : 1)
                        - (s.has(Item.FEVER) ? 1 : 0);
        double scrap = 1 + .3 * s.of(Item.MAGNET) + (s.has(Item.GREED) ? .6 : 0);
        double burn =
                weapon.burnChance()
                        + .15 * s.of(Item.IGNITER)
                        + (diver == DiverClass.WELDER ? .20 : 0)
                        + meta.burn();
        double chill = .15 * s.of(Item.CRYO_COIL);
        if (s.has(Item.INFERNO)) burn += .4;
        double burnPower = (diver == DiverClass.WELDER ? 2 : 1) * (s.has(Item.INFERNO) ? 3 : 1);
        int kits = 3 + s.of(Item.TOOLBELT) + (diver == DiverClass.MECHANIC ? 1 : 0) + meta.kits();
        double shield = 20 * s.of(Item.SHIELD_CELL) + (s.has(Item.BASTION) ? 120 : 0);
        for (var synergy : resonance)
            switch (synergy) {
                case FIRESTORM -> burnPower *= 1.5;
                case PERMAFROST -> chill += .15;
                case MARKSMAN -> crit += .08;
                case FORTRESS -> shield += 20;
                case BLOODLINE -> lifesteal += .03;
                case POWER_LOOP -> {
                    energy += 20;
                    regen += 2;
                }
                case SLIPSTREAM -> {
                    cooldown *= .9;
                    move *= 1.05;
                }
                case SCRAPPER -> {
                    scrap += .2;
                    kits += 1;
                }
                case HEAVYWEIGHT -> damage *= 1.1;
            }
        return new StatSheet(
                health,
                energy,
                damage,
                ability,
                Math.min(MAX_ATTACK_SPEED, attackSpeed),
                reach,
                move,
                cooldown,
                moduleCooldown,
                taken,
                // Über 100 % hinaus wird Kritik zur Überkritik mit bis zu drei Stufen.
                Math.min(MAX_CRIT, crit),
                critDamage,
                knockback,
                Math.min(s.has(Item.BLOOD_PACT) ? .4 : .25, lifesteal),
                s.of(Item.JETPACK),
                Math.max(0, regen),
                scrap,
                Math.min(.9, burn),
                Math.min(.9, chill),
                burnPower,
                shield,
                kits,
                (180 + 90 * s.of(Item.MAGNET)) * meta.pickup(),
                Math.min(MAX_AREA, Math.pow(1.12, s.of(Item.AREA)) * meta.area()),
                (1 + .15 * s.of(Item.CHARGER)) * meta.xpGain(),
                s.of(Item.MULTISHOT) + meta.projectiles());
    }

    /** Höchste kritische Chance: drei volle Stufen Überkritik und ein halber Schritt darüber. */
    public static final double MAX_CRIT = 3.5;

    /** Obergrenze des Wirkungsbereichs, damit Novae nicht über mehrere Räume reichen. */
    public static final double MAX_AREA = 6;

    /** Obergrenze des Angriffstempos; schneller werden Schläge nicht mehr lesbar. */
    public static final double MAX_ATTACK_SPEED = 4;

    /**
     * Würfelt die Stufe eines kritischen Treffers. Jede volle 100 % garantieren eine Stufe, der
     * Rest ist die Chance auf die nächste.
     *
     * @param chance kritische Chance, auch über 1
     * @param roll Zufallszahl 0 bis 1
     * @return Stufe 0 (kein Kritischer) bis 3
     */
    public static int critTier(double chance, double roll) {
        int tier = (int) Math.floor(chance);
        if (roll < chance - tier) tier++;
        return Math.max(0, Math.min(3, tier));
    }

    private record Stacks(Map<Item, Integer> items) {
        int of(Item item) {
            return items.getOrDefault(item, 0);
        }

        boolean has(Item item) {
            return of(item) > 0;
        }
    }
}
