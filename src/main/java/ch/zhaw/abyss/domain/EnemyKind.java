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

    /** Schwarm: winzige Rostmilbe, die in Horden aus Lüftungen und Schotts quillt. */
    MITE("Rostmilbe", 10, 36, 28, 235, 0, 0, 0),

    /** Schwarm: leuchtender Glimmfisch, der in Schulen durch geflutete Decks schiesst. */
    GLOWFISH("Glimmfisch", 8, 34, 24, 250, 0, 2, 0),

    /** Schwarm: Nanodrohne, kreist um die Figur und stürzt sich im Pulk herab. */
    NANODRONE("Nanodrohne", 12, 30, 28, 230, 0, 1, 0),

    /** Schwarm: Säurespucker, bleibt auf Abstand und wirft Säureklumpen im Bogen. */
    SPITTER("Säurespucker", 14, 34, 26, 150, 0, 1, 0),

    /** Schwarm: Zündmilbe, rennt heran und sprengt sich; besiegt reisst sie Nachbarn mit. */
    FUSE("Zündmilbe", 9, 28, 22, 280, 0, 1, 0),

    /** Schwarm: Prismaqualle, schwebt und entlässt Ringe aus Prismageschossen. */
    PRISM("Prismaqualle", 22, 36, 40, 95, 0, 3, 0),

    /** Schwarm: Speerfisch, zielt sichtbar und schiesst geradlinig durch den Raum. */
    LANCER("Speerfisch", 16, 48, 20, 210, 0, 2, 0),

    /** Schwarm: Panzerkrabbe, deren Schale Treffer von vorn fast ganz abfängt. */
    CRAB("Panzerkrabbe", 30, 44, 30, 140, 0, 2, 0),

    /** Brutnest: speit Milben, bis es zerstört ist; fällt das Nest, stirbt seine Brut. */
    HIVE("Brutnest", 170, 58, 62, 0, 2, 1, 3),

    /** Brutei der Brutmutter: schlüpft nach kurzer Zeit zu drei Rostmilben. */
    EGG("Brutei", 28, 30, 30, 0, 0, 2, 0),
    WARDEN("Der Schottmeister", 600, 150, 170, 120, 0, 0, 35),
    REACTOR("Der Reaktorkern", 720, 170, 180, 60, 0, 1, 40),
    BROOD("Die Brutmutter", 820, 200, 150, 150, 0, 2, 45),
    CAPTAIN("Der Lotse", 1100, 110, 160, 125, 0, 3, 60),

    /** Endgegnerin ab dem zweiten Zyklus: Lichtlanzen, Prismenbolzen und Sonnentanz. */
    EMPRESS("Die Prismenkaiserin", 450, 150, 170, 140, 0, 3, 80);
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
        return this == WARDEN
                || this == REACTOR
                || this == BROOD
                || this == CAPTAIN
                || this == EMPRESS;
    }

    /**
     * @return {@code true} für Schwarmgegner, die in Horden nachströmen
     */
    public boolean swarm() {
        return switch (this) {
            case MITE, GLOWFISH, NANODRONE, SPITTER, FUSE, PRISM, LANCER, CRAB -> true;
            default -> false;
        };
    }

    /**
     * @return {@code true} für ortsfeste Arten, die weder Rückstoss noch Abstossung bewegt
     */
    public boolean stationary() {
        return this == TURRET || this == HIVE || this == EGG;
    }

    /**
     * @return {@code true}, wenn die Art keine Schwerkraft hat
     */
    public boolean flying() {
        return this == DRONE
                || this == GLOWFISH
                || this == NANODRONE
                || this == JELLY
                || this == TURRET
                || this == BROOD
                || this == SEEKER
                || this == SMUGGLER
                || this == PRISM
                || this == LANCER
                || this == EMPRESS;
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
            case EMPRESS -> .35;
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
            case MITE -> Behaviors.MITE;
            case GLOWFISH -> Behaviors.GLOWFISH;
            case NANODRONE -> Behaviors.NANODRONE;
            case SPITTER -> Behaviors.SPITTER;
            case FUSE -> Behaviors.FUSE;
            case PRISM -> Behaviors.PRISM;
            case LANCER -> Behaviors.LANCER;
            case CRAB -> Behaviors.CRAB;
            case HIVE -> Behaviors.HIVE;
            case EGG -> Behaviors.EGG;
            case WARDEN -> Behaviors.WARDEN;
            case REACTOR -> Behaviors.REACTOR;
            case BROOD -> Behaviors.BROOD;
            case CAPTAIN -> Behaviors.CAPTAIN;
            case EMPRESS -> Behaviors.EMPRESS;
        };
    }
}
