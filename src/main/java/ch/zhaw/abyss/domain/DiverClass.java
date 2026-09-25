package ch.zhaw.abyss.domain;

/**
 * Spielbare Taucherinnen und Taucher. Die Klasse legt Grundwerte, Startausrüstung und eine
 * dauerhafte Eigenheit fest; alles Weitere entsteht im Run.
 */
public enum DiverClass {
    MECHANIC(
            "Die Mechanikerin",
            "Ausgewogen. Werkstätten sind für sie 25 % günstiger, sie trägt ein Reparaturset mehr.",
            100,
            1.0,
            Weapon.WRENCH,
            ActiveModule.PULSE,
            0),
    HARPOONER(
            "Der Harpunier",
            "Kämpft auf Distanz. +10 % kritische Trefferchance, aber weniger Integrität.",
            85,
            1.05,
            Weapon.HARPOON,
            ActiveModule.SONAR,
            12),
    WELDER(
            "Die Schweisserin",
            "Setzt alles in Brand. +20 % Brandchance, Brand wirkt doppelt so stark.",
            95,
            1.0,
            Weapon.TORCH_LANCE,
            ActiveModule.AEGIS,
            16),
    TITAN(
            "Der Koloss",
            "Schwerer Panzeranzug: 150 Integrität und 20 % Schadensreduktion, aber langsamer.",
            150,
            .92,
            Weapon.ANCHOR,
            ActiveModule.TORPEDO,
            22),
    SPARK(
            "Die Funkerin",
            "Energiequelle auf zwei Beinen: +40 Energie, doppelte Energierückgewinnung.",
            90,
            1.08,
            Weapon.TESLA,
            ActiveModule.DRONE,
            28);
    private final String title, description;
    private final double health, speed;
    private final Weapon weapon;
    private final ActiveModule module;
    private final int unlockCost;

    DiverClass(
            String title,
            String description,
            double health,
            double speed,
            Weapon weapon,
            ActiveModule module,
            int unlockCost) {
        this.title = title;
        this.description = description;
        this.health = health;
        this.speed = speed;
        this.weapon = weapon;
        this.module = module;
        this.unlockCost = unlockCost;
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Beschreibung der Eigenheit
     */
    public String description() {
        return description;
    }

    /**
     * @return Grundintegrität
     */
    public double health() {
        return health;
    }

    /**
     * @return Faktor für das Lauftempo
     */
    public double speed() {
        return speed;
    }

    /**
     * @return Startwaffe
     */
    public Weapon weapon() {
        return weapon;
    }

    /**
     * @return empfohlenes Startmodul
     */
    public ActiveModule module() {
        return module;
    }

    /**
     * @return Kosten in Datenkernen; 0 bedeutet von Beginn an spielbar
     */
    public int unlockCost() {
        return unlockCost;
    }
}
