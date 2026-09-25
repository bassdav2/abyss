package ch.zhaw.abyss.domain;

/**
 * Fachliche Identität eines Raums im Boot. Das Thema bestimmt Namen und Sektor; die Darstellung
 * wählt daraus Requisiten und Licht, ohne Spielregeln abzuleiten.
 */
public enum RoomTheme {
    QUARTERS("Mannschaftsquartier", 0),
    CARGO("Frachtraum", 0),
    MESS("Verlassene Messe", 0),
    TORPEDO("Torpedomagazin", 0),
    BRIG("Arrestzellen", 0),
    BULKHEAD("Das verriegelte Schott", 0),
    GALLEY("Kombüse", 0),
    LAUNDRY("Wäscherei", 0),
    ENGINE("Turbinenhalle", 1),
    COOLING("Kühlkreislauf", 1),
    BALLAST("Ballastkammer", 1),
    PUMPS("Pumpenraum", 1),
    REACTOR("Reaktorkammer", 1),
    BOILER("Kesselraum", 1),
    GENERATOR("Generatorraum", 1),
    LAB("Forschungslabor", 2),
    GARDEN("Sauerstoffgarten", 2),
    MEDBAY("Krankenstation", 2),
    AQUARIUM("Probenbecken", 2),
    HATCHERY("Die Brutkammer", 2),
    ARCHIVE("Datenarchiv", 2),
    CRYO("Kryolabor", 2),
    SECURITY("Sicherheitszentrale", 3),
    OBSERVATORY("Beobachtungsdeck", 3),
    COMMAND_HALL("Kommandohalle", 3),
    SIGNAL("Signalzentrale", 3),
    BRIDGE("Die Brücke", 3),
    ARMORY("Waffenkammer", 3),
    MAPROOM("Kartenraum", 3),
    WORKSHOP("Werkstatt", -1),
    MARKET("Schwarzmarkt", -1),
    SHRINE("Die Druckkapelle", -1),
    STORAGE("Vergessenes Depot", -1);

    private final String title;
    private final int sector;

    RoomTheme(String title, int sector) {
        this.title = title;
        this.sector = sector;
    }

    /**
     * @return angezeigter Raumname
     */
    public String title() {
        return title;
    }

    /**
     * @return Sektor, zu dem das Thema gehört, oder -1 für sektorübergreifende Räume
     */
    public int sector() {
        return sector;
    }

    /**
     * @param sector Sektorindex 0 bis 3
     * @return reguläre Kampfthemen des Sektors ohne Boss- und Sonderräume
     */
    public static RoomTheme[] combatThemes(int sector) {
        return switch (sector) {
            case 0 -> new RoomTheme[] {QUARTERS, CARGO, MESS, TORPEDO, BRIG, GALLEY, LAUNDRY};
            case 1 -> new RoomTheme[] {ENGINE, COOLING, BALLAST, PUMPS, BOILER, GENERATOR};
            case 2 -> new RoomTheme[] {LAB, GARDEN, MEDBAY, AQUARIUM, ARCHIVE, CRYO};
            default ->
                    new RoomTheme[] {SECURITY, OBSERVATORY, COMMAND_HALL, SIGNAL, ARMORY, MAPROOM};
        };
    }
}
