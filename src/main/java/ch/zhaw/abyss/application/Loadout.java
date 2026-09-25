package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.domain.Weapon;

/**
 * Auswahl in der Vorbereitung: wer taucht, mit welcher Waffe, welchem Modul und unter welchem
 * Druck.
 *
 * @param diver Klasse
 * @param weapon Startwaffe
 * @param module aktives Modul
 * @param pressure Druckstufe
 * @param explorer Entdeckermodus
 */
public record Loadout(
        DiverClass diver, Weapon weapon, ActiveModule module, int pressure, boolean explorer) {
    /** Vorgabe für neue Profile. */
    public static final Loadout DEFAULT =
            new Loadout(DiverClass.MECHANIC, Weapon.WRENCH, ActiveModule.PULSE, 0, false);

    /** Prüft Vollständigkeit und Druckstufe. */
    public Loadout {
        if (diver == null || weapon == null || module == null)
            throw new IllegalArgumentException("Unvollständige Auswahl");
        if (pressure < 0 || pressure > RunSetup.MAX_PRESSURE)
            throw new IllegalArgumentException("Ungültige Druckstufe");
    }

    /**
     * @param value neue Klasse; Waffe und Modul springen auf deren Startausrüstung
     * @return geänderte Auswahl
     */
    public Loadout withDiver(DiverClass value) {
        return new Loadout(value, value.weapon(), value.module(), pressure, explorer);
    }

    /**
     * @param value neue Waffe
     * @return geänderte Auswahl
     */
    public Loadout withWeapon(Weapon value) {
        return new Loadout(diver, value, module, pressure, explorer);
    }

    /**
     * @param value neues Modul
     * @return geänderte Auswahl
     */
    public Loadout withModule(ActiveModule value) {
        return new Loadout(diver, weapon, value, pressure, explorer);
    }

    /**
     * @param value neue Druckstufe
     * @return geänderte Auswahl
     */
    public Loadout withPressure(int value) {
        return new Loadout(diver, weapon, module, value, explorer);
    }

    /**
     * @param value Entdeckermodus
     * @return geänderte Auswahl
     */
    public Loadout withExplorer(boolean value) {
        return new Loadout(diver, weapon, module, pressure, value);
    }
}
