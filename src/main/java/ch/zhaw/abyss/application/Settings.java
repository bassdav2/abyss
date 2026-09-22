package ch.zhaw.abyss.application;

public record Settings(
        double masterVolume,
        double musicVolume,
        boolean reducedMotion,
        boolean fullscreen,
        boolean explorer) {
    public static final Settings DEFAULT = new Settings(.65, .45, false, false, false);

    public Settings {
        if (!Double.isFinite(masterVolume)
                || !Double.isFinite(musicVolume)
                || masterVolume < 0
                || masterVolume > 1
                || musicVolume < 0
                || musicVolume > 1) throw new IllegalArgumentException("Ungültige Lautstärke");
    }
}
