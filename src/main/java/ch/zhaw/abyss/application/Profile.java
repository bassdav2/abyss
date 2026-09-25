package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.domain.Weapon;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Dauerhafter Fortschritt über alle Tauchgänge: Statistik, Datenkerne, Freischaltungen, entdeckte
 * Module, Aussehen, letzte Vorbereitung und Einstellungen. Unveränderlich; Änderungen erzeugen
 * Kopien.
 *
 * @param runs begonnene Tauchgänge
 * @param wins eroberte Brücken
 * @param bestRoom weiteste erreichte Raumnummer ab 1
 * @param bestCycle höchster erreichter Zyklus
 * @param totalKills Abschüsse insgesamt
 * @param cores verfügbare Datenkerne
 * @param maxPressure höchste wählbare Druckstufe
 * @param unlocks Kennungen gekaufter Freischaltungen
 * @param discovered mindestens einmal installierte Module
 * @param achievements erreichte Logbuch-Einträge
 * @param cosmetics gewähltes Aussehen
 * @param loadout letzte Vorbereitung
 * @param settings Einstellungen
 */
public record Profile(
        int runs,
        int wins,
        int bestRoom,
        int bestCycle,
        int totalKills,
        int cores,
        int maxPressure,
        Set<String> unlocks,
        Set<Item> discovered,
        Set<Achievement> achievements,
        Cosmetics cosmetics,
        Loadout loadout,
        Settings settings) {

    /** Prüft Wertebereiche und kopiert Mengen. */
    public Profile {
        unlocks = Set.copyOf(unlocks);
        discovered = discovered.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(discovered));
        achievements = achievements.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(achievements));
        if (runs < 0
                || wins < 0
                || bestRoom < 0
                || bestRoom > RoomGenerator.ROOM_COUNT
                || bestCycle < 0
                || totalKills < 0
                || cores < 0
                || maxPressure < 0
                || maxPressure > RunSetup.MAX_PRESSURE
                || cosmetics == null
                || loadout == null
                || settings == null) throw new IllegalArgumentException("Ungültiges Profil");
    }

    /**
     * @return leeres Profil für den ersten Start
     */
    public static Profile fresh() {
        return new Profile(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                Set.of(),
                Set.of(),
                Set.of(),
                Cosmetics.DEFAULT,
                Loadout.DEFAULT,
                Settings.DEFAULT);
    }

    /**
     * @param id Kennung aus {@link Unlock}
     * @return {@code true}, wenn gekauft oder von Beginn an frei
     */
    public boolean owns(String id) {
        return id == null || unlocks.contains(id);
    }

    /**
     * @param diver Klasse
     * @return {@code true}, wenn spielbar
     */
    public boolean owns(DiverClass diver) {
        return diver.unlockCost() == 0 || unlocks.contains(Unlock.of(diver));
    }

    /**
     * @param weapon Waffe
     * @return {@code true}, wenn als Startwaffe und in Waffenkisten verfügbar
     */
    public boolean owns(Weapon weapon) {
        return weapon.startsUnlocked() || unlocks.contains(Unlock.of(weapon));
    }

    /**
     * @param module Modul
     * @return {@code true}, wenn wählbar
     */
    public boolean owns(ActiveModule module) {
        return module.startsUnlocked() || unlocks.contains(Unlock.of(module));
    }

    /**
     * @param item Modul
     * @return {@code true}, wenn im Bergungspool
     */
    public boolean owns(Item item) {
        return !item.cursed() && (item.startsUnlocked() || unlocks.contains(Unlock.of(item)));
    }

    /**
     * Prüft, ob eine Freischaltung jetzt gekauft werden kann.
     *
     * @param unlock Freischaltung
     * @return {@code true} bei genug Kernen, erfüllter Voraussetzung und noch nicht gekauft
     */
    public boolean canBuy(Unlock unlock) {
        return !unlocks.contains(unlock.id())
                && cores >= unlock.cost()
                && (unlock.requires() == null || unlocks.contains(unlock.requires()));
    }

    /**
     * @param diver Klasse, deren Startwaffe immer verfügbar ist
     * @return Waffen für Waffenkisten
     */
    public Set<Weapon> weaponPool(DiverClass diver) {
        var weapons = EnumSet.noneOf(Weapon.class);
        for (var weapon : Weapon.values()) if (owns(weapon)) weapons.add(weapon);
        weapons.add(diver.weapon());
        return weapons;
    }

    /**
     * @return Module im Bergungspool
     */
    public Set<Item> itemPool() {
        var items = EnumSet.noneOf(Item.class);
        for (var item : Item.values()) if (owns(item)) items.add(item);
        return items;
    }

    /**
     * @return Bonusintegrität aus gekauften Anzugverstärkungen
     */
    public double bonusHealth() {
        int levels = 0;
        for (String id : new String[] {"perk:hull1", "perk:hull2", "perk:hull3"})
            if (unlocks.contains(id)) levels++;
        return 10 * levels;
    }

    /**
     * @return zusätzliche Reparatursets beim Start
     */
    public int bonusKits() {
        return unlocks.contains("perk:kit") ? 1 : 0;
    }

    /**
     * @return Startschrott
     */
    public int startSalvage() {
        return unlocks.contains("perk:scrap") ? 25 : 0;
    }

    /**
     * @return veränderliche Kopie für Änderungen
     */
    Builder edit() {
        return new Builder(this);
    }

    /**
     * @param next neue Einstellungen
     * @return Kopie
     */
    public Profile withSettings(Settings next) {
        return edit().settings(next).build();
    }

    /** Veränderlicher Baukasten für Profiländerungen innerhalb der Anwendungsschicht. */
    static final class Builder {
        int runs, wins, bestRoom, bestCycle, totalKills, cores, maxPressure;
        final Set<String> unlocks;
        final Set<Item> discovered;
        final Set<Achievement> achievements;
        Cosmetics cosmetics;
        Loadout loadout;
        Settings settings;

        private Builder(Profile p) {
            runs = p.runs;
            wins = p.wins;
            bestRoom = p.bestRoom;
            bestCycle = p.bestCycle;
            totalKills = p.totalKills;
            cores = p.cores;
            maxPressure = p.maxPressure;
            unlocks = new HashSet<>(p.unlocks);
            discovered = EnumSet.noneOf(Item.class);
            discovered.addAll(p.discovered);
            achievements = EnumSet.noneOf(Achievement.class);
            achievements.addAll(p.achievements);
            cosmetics = p.cosmetics;
            loadout = p.loadout;
            settings = p.settings;
        }

        Builder settings(Settings value) {
            settings = value;
            return this;
        }

        Profile build() {
            return new Profile(
                    runs,
                    wins,
                    Math.min(RoomGenerator.ROOM_COUNT, bestRoom),
                    bestCycle,
                    totalKills,
                    Math.min(99_999, cores),
                    Math.min(RunSetup.MAX_PRESSURE, maxPressure),
                    unlocks,
                    discovered,
                    achievements,
                    cosmetics,
                    loadout,
                    settings);
        }
    }
}
