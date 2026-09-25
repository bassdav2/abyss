package ch.zhaw.abyss.application;

/**
 * Unveränderliche Benutzereinstellungen.
 *
 * @param masterVolume Gesamtlautstärke 0 bis 1
 * @param musicVolume Musiklautstärke 0 bis 1
 * @param reducedMotion ruhige Darstellung ohne Wackeln und mit weniger Partikeln
 * @param fullscreen Vollbild
 * @param explorer Entdeckermodus als Vorgabe
 * @param screenShake Stärke des Kamerawackelns 0 bis 1
 * @param retroFilter Röhrenbildschirm-Filter mit Scanlines
 * @param damageNumbers Schadenszahlen anzeigen
 */
public record Settings(
        double masterVolume,
        double musicVolume,
        boolean reducedMotion,
        boolean fullscreen,
        boolean explorer,
        double screenShake,
        boolean retroFilter,
        boolean damageNumbers) {
    /** Vorgabe für neue Profile. */
    public static final Settings DEFAULT =
            new Settings(.65, .45, false, false, false, .8, true, true);

    /** Prüft Wertebereiche. */
    public Settings {
        if (!Double.isFinite(masterVolume)
                || !Double.isFinite(musicVolume)
                || !Double.isFinite(screenShake)
                || masterVolume < 0
                || masterVolume > 1
                || musicVolume < 0
                || musicVolume > 1
                || screenShake < 0
                || screenShake > 1) throw new IllegalArgumentException("Ungültige Einstellung");
    }

    /**
     * @param value neue Entdecker-Vorgabe
     * @return Kopie mit geänderter Vorgabe
     */
    public Settings withExplorer(boolean value) {
        return new Settings(
                masterVolume,
                musicVolume,
                reducedMotion,
                fullscreen,
                value,
                screenShake,
                retroFilter,
                damageNumbers);
    }
}
