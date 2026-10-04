package ch.zhaw.abyss.domain;

/**
 * Bedrohung eines Raums: eine Prüfung, die einen bestimmten Build verlangt. Wer sich auf keine
 * Stärke festlegt, gerät in solchen Räumen in Bedrängnis. Die Routenwahl zeigt die Bedrohung und ob
 * der aktuelle Build darauf vorbereitet ist. Bestandene Prüfungen bringen eine seltenere Bergung
 * und einen Datenkern.
 */
public enum Threat {
    /** Keine besondere Bedrohung. */
    NONE("", "", ""),
    /** Panzerschwarm: normale Treffer wirken nur zu 35 Prozent. */
    ARMORED(
            "Panzerschwarm",
            "Alle Gegner tragen Panzer. Normale Treffer wirken nur zu 35 %.",
            "Kritische Treffer, Brand, Explosionen, Blitze"),
    /** Flutwelle: dreimal so viele, halb so zähe Schwarmgegner. */
    FLOOD(
            "Flutwelle",
            "Dreimal so viele Schwarmgegner mit halber Integrität.",
            "Flächenschaden, Kreiselmesser, Teslafeld, Kettenreaktion"),
    /** Kolosse: wenige Elitegegner mit vierfacher Integrität. */
    COLOSSUS(
            "Kolosse",
            "Wenige Elitegegner, riesig und vierfach so zäh.",
            "Einzelschaden, Kritik, Werkstattstufe"),
    /** Luftschlag: ausschliesslich fliegende Gegner. */
    SKY(
            "Luftschlag",
            "Nur fliegende Gegner, hoch über dem Boden.",
            "Geschosse, Zielsucher, Teslafeld, Sprünge"),
    /** Brutnester: Nester speien Milben, bis sie zerstört sind. */
    NESTS(
            "Brutnester",
            "Nester speien Milben. Fällt ein Nest, stirbt seine Brut.",
            "Hoher Einzelschaden, schnelles Vorrücken"),
    /** Sperrfeuer: Schützen und Prismaquallen überziehen den Raum mit Geschossen. */
    BARRAGE(
            "Sperrfeuer",
            "Spucker und Prismaquallen füllen den Raum mit Geschossen, dazu Einschläge.",
            "Ausweichen, Schild, Barriere, Tempo"),
    /** Regeneration: Gegner heilen schnell, solange sie nicht brennen oder frieren. */
    REGEN(
            "Regeneration",
            "Gegner heilen sich rasch, solange sie weder brennen noch frieren.",
            "Brand, Kälte, Schadensspitzen");
    private final String title, description, counter;

    Threat(String title, String description, String counter) {
        this.title = title;
        this.description = description;
        this.counter = counter;
    }

    /**
     * @return Anzeigename
     */
    public String title() {
        return title;
    }

    /**
     * @return Wirkung in einem Satz
     */
    public String description() {
        return description;
    }

    /**
     * @return was gegen diese Bedrohung hilft
     */
    public String counter() {
        return counter;
    }

    /**
     * Prüft, ob ein Build auf die Bedrohung vorbereitet ist. Die Schwellen sind bewusst so gewählt,
     * dass ein breiter Allround-Build sie selten erfüllt.
     *
     * @param p Figur mit aktuellem Build
     * @return {@code true}, wenn der Build die passende Stärke mitbringt
     */
    public boolean prepared(Player p) {
        var s = p.stats();
        return switch (this) {
            case NONE -> true;
            case ARMORED ->
                    s.critChance() >= .35
                            || s.burnChance() >= .3
                            || p.stacks(Item.CHAIN_REACTION) >= 2
                            || p.stacks(Item.ARC_COIL) >= 3;
            case FLOOD ->
                    p.stacks(Item.ORBITAL)
                                    + p.stacks(Item.TESLA_FIELD)
                                    + p.stacks(Item.CHAIN_REACTION)
                                    + p.stacks(Item.NOVA)
                                    + p.stacks(Item.BLADE_WAVE)
                                    + p.stacks(Item.AREA)
                                    + 3 * evolved(p)
                            >= 5;
            case COLOSSUS -> s.damage() * (1 + s.critChance() * (s.critDamage() - 1)) >= 4;
            case SKY ->
                    p.weapon() == Weapon.HARPOON
                            || p.weapon() == Weapon.PLASMA
                            || p.stacks(Item.MULTISHOT)
                                            + p.stacks(Item.HOMING)
                                            + p.stacks(Item.TESLA_FIELD)
                                            + p.stacks(Item.ORBITAL)
                                            + p.stacks(Item.JETPACK)
                                    >= 4;
            case NESTS -> s.damage() >= 3 || p.weaponLevel() >= 5;
            case BARRAGE ->
                    p.stacks(Item.SHIELD_CELL)
                                            + p.stacks(Item.BARRIER)
                                            + p.stacks(Item.PLATING)
                                            + p.stacks(Item.THRUSTER)
                                            + p.stacks(Item.COOLANT)
                                    >= 5
                            || p.module() == ActiveModule.PULSE
                            || p.module() == ActiveModule.AEGIS;
            case REGEN -> s.burnChance() >= .3 || s.chillChance() >= .3;
        };
    }

    private static int evolved(Player p) {
        int count = 0;
        for (var item : Item.values()) if (item.evolution() && p.stacks(item) > 0) count++;
        return count;
    }
}
