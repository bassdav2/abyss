package ch.zhaw.abyss.domain;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Unveränderliche Startbedingungen eines Tauchgangs, zusammengestellt aus Vorbereitung und
 * dauerhaftem Profil.
 *
 * @param seed Routen-Seed
 * @param diver gewählte Klasse
 * @param weapon Startwaffe
 * @param module aktives Modul
 * @param explorer zugänglicherer Entdeckermodus
 * @param pressure Druckstufe ab 0
 * @param itemPool freigeschaltete Module für Bergungen
 * @param weaponPool freigeschaltete Waffen für Waffenkisten
 * @param bonusHealth dauerhafte Bonusintegrität
 * @param bonusKits zusätzliche Reparatursets beim Start
 * @param startSalvage Schrott beim Start
 */
public record RunSetup(
        long seed,
        DiverClass diver,
        Weapon weapon,
        ActiveModule module,
        boolean explorer,
        int pressure,
        Set<Item> itemPool,
        Set<Weapon> weaponPool,
        double bonusHealth,
        int bonusKits,
        int startSalvage) {
    /** Höchste wählbare Druckstufe. */
    public static final int MAX_PRESSURE = 5;

    /** Prüft Werte und kopiert Mengen unveränderlich. */
    public RunSetup {
        if (diver == null || weapon == null || module == null)
            throw new IllegalArgumentException("Unvollständige Ausrüstung");
        if (pressure < 0
                || pressure > MAX_PRESSURE
                || bonusHealth < 0
                || bonusKits < 0
                || startSalvage < 0) throw new IllegalArgumentException("Ungültige Startwerte");
        itemPool = itemPool.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(itemPool));
        weaponPool = weaponPool.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(weaponPool));
    }

    /**
     * Standardstart mit allen anfänglich verfügbaren Inhalten, etwa für Tests und Demo.
     *
     * @param seed Routen-Seed
     * @param diver Klasse
     * @return Startbedingungen
     */
    public static RunSetup standard(long seed, DiverClass diver) {
        return new RunSetup(
                seed,
                diver,
                diver.weapon(),
                diver.module(),
                false,
                0,
                defaultItems(),
                defaultWeapons(),
                0,
                0,
                0);
    }

    /**
     * @return Module, die ohne Freischaltung im Pool liegen
     */
    public static Set<Item> defaultItems() {
        var items = EnumSet.noneOf(Item.class);
        Arrays.stream(Item.values())
                .filter(item -> item.startsUnlocked() && !item.cursed())
                .forEach(items::add);
        return items;
    }

    /**
     * @return Waffen, die ohne Freischaltung verfügbar sind
     */
    public static Set<Weapon> defaultWeapons() {
        var weapons = EnumSet.noneOf(Weapon.class);
        Arrays.stream(Weapon.values()).filter(Weapon::startsUnlocked).forEach(weapons::add);
        return weapons;
    }

    /**
     * @param seed neuer Seed
     * @return gleiche Bedingungen mit anderem Seed
     */
    public RunSetup withSeed(long seed) {
        return new RunSetup(
                seed,
                diver,
                weapon,
                module,
                explorer,
                pressure,
                itemPool,
                weaponPool,
                bonusHealth,
                bonusKits,
                startSalvage);
    }
}
