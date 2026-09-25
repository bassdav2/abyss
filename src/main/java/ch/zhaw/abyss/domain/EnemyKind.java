package ch.zhaw.abyss.domain;

/**
 * Gegnerarten mit Grundwerten. Das Verhalten liefert {@link #behavior()} als austauschbare
 * Strategie; die Werte hier sind reine Daten.
 */
public enum EnemyKind {
    SCUTTLER("Schrottläufer", 44, 62, 58, 160, 1, 0, 2),
    DRONE("Wachdrohne", 34, 56, 52, 110, 1, 0, 2),
    SENTINEL("Schottwächter", 95, 78, 124, 95, 3, 0, 5),
    BOMBER("Kugelbombe", 26, 50, 50, 235, 1, 0, 2),
    WELDER("Schweissroboter", 75, 80, 96, 90, 2, 1, 4),
    TURRET("Geschützturm", 60, 70, 56, 0, 2, 1, 3),
    MINELAYER("Minenleger", 80, 96, 78, 65, 2, 1, 4),
    JELLY("Leuchtqualle", 42, 64, 72, 70, 2, 2, 3),
    EEL("Tiefseeaal", 58, 120, 46, 0, 2, 2, 3),
    SHIELDBEARER("Schildträger", 110, 84, 118, 80, 3, 2, 6),
    ENFORCER("Sicherheitsautomat", 120, 70, 128, 115, 3, 3, 6),
    SEEKER("Suchlichtsonde", 50, 58, 50, 125, 2, 3, 3),
    SMUGGLER("Schmugglerdrohne", 70, 56, 44, 250, 0, 0, 26),
    WARDEN("Der Schottmeister", 600, 150, 170, 120, 0, 0, 35),
    REACTOR("Der Reaktorkern", 720, 170, 180, 60, 0, 1, 40),
    BROOD("Die Brutmutter", 820, 200, 150, 150, 0, 2, 45),
    CAPTAIN("Der Lotse", 1100, 110, 160, 125, 0, 3, 60);
    final double health, width, height, speed;
    private final String title;
    private final int threat, minSector, salvage;

    EnemyKind(
            String title,
            double health,
            double width,
            double height,
            double speed,
            int threat,
            int minSector,
            int salvage) {
        this.title = title;
        this.health = health;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.threat = threat;
        this.minSector = minSector;
        this.salvage = salvage;
    }

    /**
     * @return {@code true} für Sektorwächter und den Lotsen
     */
    public boolean boss() {
        return this == WARDEN || this == REACTOR || this == BROOD || this == CAPTAIN;
    }

    /**
     * @return {@code true}, wenn die Art keine Schwerkraft hat
     */
    public boolean flying() {
        return this == DRONE
                || this == JELLY
                || this == TURRET
                || this == BROOD
                || this == SEEKER
                || this == SMUGGLER;
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Bedrohungspunkte für die Wellenzusammenstellung
     */
    public int threat() {
        return threat;
    }

    /**
     * @return frühester Sektor, in dem die Art auftaucht
     */
    public int minSector() {
        return minSector;
    }

    /**
     * @return Schrott beim Abschuss
     */
    public int salvage() {
        return salvage;
    }

    /**
     * @return Grundintegrität
     */
    public double baseHealth() {
        return health;
    }

    /**
     * @return Breite der Trefferzone
     */
    public double baseWidth() {
        return width;
    }

    /**
     * @return Höhe der Trefferzone
     */
    public double baseHeight() {
        return height;
    }

    /**
     * @return Panzerfaktor ausserhalb der Erholung; 1 bedeutet ungepanzert
     */
    public double armor() {
        return switch (this) {
            case WARDEN -> .45;
            case REACTOR -> .38;
            case BROOD -> .5;
            case CAPTAIN -> .3;
            default -> 1;
        };
    }

    /**
     * Fabrikmethode für das Verhalten. Jede Instanz ist zustandslos; Zustand liegt am Gegner.
     *
     * @return Verhaltensstrategie
     */
    EnemyBehavior behavior() {
        return switch (this) {
            case SCUTTLER -> Behaviors.SCUTTLER;
            case DRONE -> Behaviors.DRONE;
            case SENTINEL -> Behaviors.SENTINEL;
            case BOMBER -> Behaviors.BOMBER;
            case WELDER -> Behaviors.WELDER;
            case TURRET -> Behaviors.TURRET;
            case MINELAYER -> Behaviors.MINELAYER;
            case JELLY -> Behaviors.JELLY;
            case EEL -> Behaviors.EEL;
            case SHIELDBEARER -> Behaviors.SHIELDBEARER;
            case ENFORCER -> Behaviors.ENFORCER;
            case SEEKER -> Behaviors.SEEKER;
            case SMUGGLER -> Behaviors.SMUGGLER;
            case WARDEN -> Behaviors.WARDEN;
            case REACTOR -> Behaviors.REACTOR;
            case BROOD -> Behaviors.BROOD;
            case CAPTAIN -> Behaviors.CAPTAIN;
        };
    }
}
