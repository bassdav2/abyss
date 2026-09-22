package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Reproduzierbare Route mit drei Sektionen, eigenen Bossen und sicheren Werkstätten. */
public final class RoomGenerator {
    public static final int ROOM_COUNT = 18;
    private static final String[][] TITLES = {
        {
            "Die letzte Koje",
            "Versorgungsleitung",
            "Verlassene Messe",
            "Torpedomagazin",
            "Das verriegelte Schott",
            "Die stille Werkstatt"
        },
        {
            "Turbinenhalle",
            "Kühlkreislauf",
            "Ballastkammer",
            "Reaktorzugang",
            "Das Herz des Bootes",
            "Die zweite Werkstatt"
        },
        {
            "Beobachtungsdeck",
            "Der grüne Garten",
            "Quarantänestation",
            "Signalzentrale",
            "Vor der Brücke",
            "Die Brücke"
        }
    };
    private final long seed;
    private final int cycle;

    public RoomGenerator(long seed, int cycle) {
        this.seed = seed;
        this.cycle = cycle;
    }

    public static boolean fixed(int depth) {
        return depth == 0 || depth == 4 || depth == 5 || depth == 10 || depth == 11 || depth == 17;
    }

    public static boolean bossDepth(int depth) {
        return depth == 4 || depth == 10 || depth == 17;
    }

    public List<RoomPlan> choices(int depth) {
        if (depth < 0 || depth >= ROOM_COUNT)
            throw new IllegalArgumentException("Ungültige Raumtiefe");
        var rooms = new ArrayList<RoomPlan>();
        for (int branch = 0; branch < (fixed(depth) ? 1 : 2); branch++)
            rooms.add(room(depth, branch));
        return List.copyOf(rooms);
    }

    public RoomPlan room(int depth, int branch) {
        if (depth < 0
                || depth >= ROOM_COUNT
                || branch < 0
                || branch > 1
                || branch == 1 && fixed(depth))
            throw new IllegalArgumentException("Ungültiger Raum");
        var random =
                new Random(
                        seed
                                ^ (0x9E3779B97F4A7C15L * (depth + 1))
                                ^ (branch * 8191L)
                                ^ (cycle * 31337L));
        int sector = depth / 6;
        RoomPlan.Kind kind =
                depth == 17
                        ? RoomPlan.Kind.BRIDGE
                        : bossDepth(depth)
                                ? RoomPlan.Kind.BOSS
                                : depth == 5 || depth == 11
                                        ? RoomPlan.Kind.WORKSHOP
                                        : branch == 1 && (depth % 3 == 2 || depth == 13)
                                                ? RoomPlan.Kind.CACHE
                                                : branch == 1 || depth == 16
                                                        ? RoomPlan.Kind.ELITE
                                                        : RoomPlan.Kind.COMBAT;
        var enemies = new ArrayList<EnemyKind>();
        if (bossDepth(depth))
            enemies.add(
                    depth == 4
                            ? EnemyKind.WARDEN
                            : depth == 10 ? EnemyKind.REACTOR : EnemyKind.CAPTAIN);
        else if (kind == RoomPlan.Kind.COMBAT || kind == RoomPlan.Kind.ELITE) {
            int count = depth == 0 ? 1 : 2 + sector + (kind == RoomPlan.Kind.ELITE ? 1 : 0);
            for (int i = 0; i < count; i++)
                enemies.add(
                        depth == 0
                                ? EnemyKind.SCUTTLER
                                : i == count - 1 && (sector > 0 || kind == RoomPlan.Kind.ELITE)
                                        ? EnemyKind.SENTINEL
                                        : random.nextDouble() < .4
                                                ? EnemyKind.DRONE
                                                : EnemyKind.SCUTTLER);
        }
        String title =
                kind == RoomPlan.Kind.CACHE
                        ? (depth == 13 ? "Der Sauerstoffgarten" : "Vergessenes Depot")
                        : TITLES[sector][depth % 6];
        if (branch == 1 && kind == RoomPlan.Kind.ELITE)
            title =
                    switch (depth % 4) {
                        case 0 -> "Gesicherte Krankenstation";
                        case 1 -> "Rüstkammer";
                        case 2 -> "Druckkammer";
                        default -> "Torpedowerkstatt";
                    };
        String description =
                switch (kind) {
                    case COMBAT ->
                            "Sichere den Raum. Berge ein Modul und suche nach Vorratskisten.";
                    case ELITE -> "Drei Patrouillen. Seltene Bergung mit zwei Modulstufen.";
                    case WORKSHOP -> "Kostenlose Reparatur. Ein Modul für 15 Schrott.";
                    case CACHE -> "Sicherer Umweg: Integrität, Energie und Schrott.";
                    case BOSS ->
                            depth == 4
                                    ? "Der Schottmeister: Weiche seinem Ansturm aus, dann nutze die"
                                            + " Pause."
                                    : "Der Reaktorkern: Springe über Wellen und durchbrich sein"
                                            + " Sperrfeuer.";
                    case BRIDGE -> "Die letzte Begegnung. Der Lotse erwartet dich.";
                };
        return new RoomPlan(
                depth,
                branch,
                sector,
                random.nextInt(4),
                kind,
                title,
                description,
                enemies,
                kind == RoomPlan.Kind.BOSS
                        ? 30
                        : kind == RoomPlan.Kind.ELITE ? 16 : kind == RoomPlan.Kind.CACHE ? 8 : 6);
    }
}
