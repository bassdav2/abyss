package ch.zhaw.abyss.domain;

import java.util.List;

/**
 * Unveränderlicher Bauplan eines Raums: Art, Thema, Geometrie, Gegnerwellen, Gefahren und Kisten.
 * Derselbe Seed erzeugt immer denselben Plan.
 *
 * @param depth Position auf der Route ab 0
 * @param branch gewählter Abzweig 0 oder 1
 * @param sector Sektor 0 bis 3
 * @param variant Darstellungsvariante
 * @param kind Raumart
 * @param theme fachliches Raumthema
 * @param description Kurzbeschreibung für die Routenwahl
 * @param waves Gegnerwellen in Reihenfolge
 * @param layout begehbare Geometrie
 * @param hazards dauerhafte Gefahren
 * @param crates Vorratskisten
 * @param salvageReward Schrott beim Sichern
 * @param condition besonderer Raumzustand
 * @param fixtures eingebaute Raumtechnik
 * @param hordes Schwarmkontingente je Welle
 * @param threat Bedrohung, die einen bestimmten Build verlangt
 */
public record RoomPlan(
        int depth,
        int branch,
        int sector,
        int variant,
        Kind kind,
        RoomTheme theme,
        String description,
        List<List<Spawn>> waves,
        RoomLayout layout,
        List<HazardSlot> hazards,
        List<CrateSlot> crates,
        int salvageReward,
        RoomCondition condition,
        List<FixtureSlot> fixtures,
        List<List<Horde>> hordes,
        Threat threat) {
    /** Raumarten. */
    public enum Kind {
        COMBAT,
        ELITE,
        WORKSHOP,
        CACHE,
        MERCHANT,
        SHRINE,
        BOSS,
        BRIDGE
    }

    /**
     * Startposition eines Gegners.
     *
     * @param kind Art
     * @param affix Elite-Eigenschaft
     * @param x horizontale Position
     * @param y Fusshöhe
     */
    public record Spawn(EnemyKind kind, Affix affix, double x, double y) {}

    /**
     * Lage einer dauerhaften Gefahr.
     *
     * @param x horizontale Mitte
     * @param kind Art
     */
    public record HazardSlot(double x, Hazard.Kind kind) {}

    /**
     * Lage einer Vorratskiste.
     *
     * @param x horizontale Mitte
     * @param y Standhöhe
     * @param kind Inhalt
     */
    public record CrateSlot(double x, double y, SupplyCrate.Kind kind) {}

    /**
     * Lage einer eingebauten Anlage.
     *
     * @param kind Art
     * @param x horizontale Mitte
     * @param direction Schubrichtung, -1 oder 1
     * @param start Versatz im Takt
     */
    public record FixtureSlot(Fixture.Kind kind, double x, int direction, double start) {}

    /**
     * Schwarmkontingent einer Welle: so viele Gegner dieser Art strömen nach, bis alle besiegt
     * sind.
     *
     * @param kind Schwarmart
     * @param count Anzahl
     */
    public record Horde(EnemyKind kind, int count) {
        /** Prüft die Werte. */
        public Horde {
            if (kind == null || count < 0) throw new IllegalArgumentException("Ungültige Horde");
        }
    }

    /** Kopiert Listen unveränderlich. */
    public RoomPlan {
        fixtures = List.copyOf(fixtures);
        waves = waves.stream().map(List::copyOf).toList();
        hordes = hordes.stream().map(List::copyOf).toList();
        if (hordes.size() != waves.size())
            throw new IllegalArgumentException("Horden passen nicht zu den Wellen");
        hazards = List.copyOf(hazards);
        crates = List.copyOf(crates);
        if (threat == null) threat = Threat.NONE;
    }

    /**
     * @return angezeigter Raumname
     */
    public String title() {
        return theme.title();
    }

    /**
     * @return Anzahl der Gegnerwellen
     */
    public int waveCount() {
        return waves.size();
    }

    /**
     * @return alle Schwarmgegner über alle Wellen
     */
    public int hordeTotal() {
        return hordes.stream().flatMap(List::stream).mapToInt(Horde::count).sum();
    }

    /**
     * @return alle Gegner über alle Wellen inklusive Schwärmen
     */
    public int enemyTotal() {
        return waves.stream().mapToInt(List::size).sum() + hordeTotal();
    }

    /**
     * @return alle Gegner der ersten Welle, für Vorschauen
     */
    public List<EnemyKind> enemies() {
        return waves.isEmpty() ? List.of() : waves.getFirst().stream().map(Spawn::kind).toList();
    }

    /**
     * @return {@code true} für Räume mit Kampf
     */
    public boolean hostile() {
        return !waves.isEmpty();
    }

    /**
     * @return Sektorname in Grossbuchstaben
     */
    public String sectorName() {
        return sectorName(sector);
    }

    /**
     * @param sector Sektorindex
     * @return Sektorname in Grossbuchstaben
     */
    public static String sectorName(int sector) {
        return switch (sector) {
            case 0 -> "HECKSEKTION";
            case 1 -> "MASCHINENDECK";
            case 2 -> "FORSCHUNGSDECK";
            default -> "KOMMANDODECK";
        };
    }

    /**
     * @return Raumart als Text
     */
    public String typeName() {
        return switch (kind) {
            case COMBAT -> "Patrouille";
            case ELITE -> "Schwer bewacht";
            case WORKSHOP -> "Werkstatt";
            case CACHE -> "Versorgung";
            case MERCHANT -> "Schwarzmarkt";
            case SHRINE -> "Druckkapelle";
            case BOSS -> "Sektorwächter";
            case BRIDGE -> "Brücke";
        };
    }
}
