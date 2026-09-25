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
 * @param critChance kritische Trefferchance 0 bis 1
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
        double magnetRadius) {

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
        var s = new Stacks(items);
        var resonance = Synergy.activeIn(items);
        double health =
                (diver.health() + bonusHealth + 20 * s.of(Item.MEDICAL))
                        * (s.has(Item.GLASS_HULL) ? .7 : 1);
        double energy = 100 + 20 * s.of(Item.CAPACITOR) + (diver == DiverClass.SPARK ? 40 : 0);
        double damage =
                (1 + .15 * s.of(Item.SERVO) + (s.has(Item.GLASS_HULL) ? .4 : 0))
                        * Weapon.levelMultiplier(weaponLevel);
        double ability =
                (1 + .15 * s.of(Item.CAPACITOR) + (s.has(Item.SINGULARITY) ? .25 : 0))
                        * Weapon.levelMultiplier(weaponLevel / 2);
        double attackSpeed = 1 + .10 * s.of(Item.OVERCLOCK) + (s.has(Item.FEVER) ? .25 : 0);
        double reach = 1 + .15 * s.of(Item.LANCE);
        double move = diver.speed() * (1 + .10 * s.of(Item.THRUSTER));
        double cooldown = Math.pow(.88, s.of(Item.COOLANT));
        double moduleCooldown = s.has(Item.SINGULARITY) ? .5 : 1;
        double taken = Math.pow(.9, s.of(Item.PLATING)) * (diver == DiverClass.TITAN ? .8 : 1);
        double crit =
                .05
                        + .07 * s.of(Item.LENS)
                        + weapon.critBonus()
                        + (diver == DiverClass.HARPOONER ? .10 : 0);
        double critDamage = 2 + .4 * s.of(Item.CRIT_DAMAGE);
        double knockback = 1 + .35 * s.of(Item.BALLAST);
        double lifesteal = .04 * s.of(Item.NANITES);
        double regen =
                (5.5 + s.of(Item.SIPHON)) * (diver == DiverClass.SPARK ? 2 : 1)
                        - (s.has(Item.FEVER) ? 1 : 0);
        double scrap = 1 + .3 * s.of(Item.MAGNET) + (s.has(Item.GREED) ? .6 : 0);
        double burn =
                weapon.burnChance()
                        + .15 * s.of(Item.IGNITER)
                        + (diver == DiverClass.WELDER ? .20 : 0);
        double chill = .15 * s.of(Item.CRYO_COIL);
        double burnPower = diver == DiverClass.WELDER ? 2 : 1;
        int kits = 3 + s.of(Item.TOOLBELT) + (diver == DiverClass.MECHANIC ? 1 : 0);
        double shield = 20 * s.of(Item.SHIELD_CELL);
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
                attackSpeed,
                reach,
                move,
                cooldown,
                moduleCooldown,
                taken,
                Math.min(.75, crit),
                critDamage,
                knockback,
                lifesteal,
                s.of(Item.JETPACK),
                Math.max(0, regen),
                scrap,
                Math.min(.9, burn),
                Math.min(.9, chill),
                burnPower,
                shield,
                kits,
                180 + 90 * s.of(Item.MAGNET));
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
