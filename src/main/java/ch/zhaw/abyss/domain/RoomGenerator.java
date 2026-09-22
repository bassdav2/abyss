package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Ein Seed erzeugt einen reproduzierbaren, stets bis zur Brücke führenden Schichtgraphen. */
public final class RoomGenerator {
    public static final int ROOM_COUNT = 12;
    private static final String[][] TITLES = {
        {"Die letzte Koje", "Versorgungsleitung", "Verlassene Messe", "Druckschleuse"},
        {"Turbinenhalle", "Kühlkreislauf", "Generatorraum", "Instandhaltung"},
        {"Beobachtungsdeck", "Signalzentrale", "Vor der Brücke", "Die Brücke"}
    };
    private final long seed;
    private final int cycle;

    public RoomGenerator(long seed, int cycle) {
        this.seed = seed;
        this.cycle = cycle;
    }

    public List<RoomPlan> choices(int depth) {
        if (depth < 0 || depth >= ROOM_COUNT)
            throw new IllegalArgumentException("Ungültige Raumtiefe");
        int count = depth == 0 || depth == 3 || depth == 7 || depth == 11 ? 1 : 2;
        var rooms = new ArrayList<RoomPlan>();
        for (int branch = 0; branch < count; branch++) rooms.add(room(depth, branch));
        return List.copyOf(rooms);
    }

    public RoomPlan room(int depth, int branch) {
        if (depth < 0
                || depth >= ROOM_COUNT
                || branch < 0
                || branch > 1
                || branch == 1 && (depth == 0 || depth == 3 || depth == 7 || depth == 11))
            throw new IllegalArgumentException("Ungültiger Raum");
        var random =
                new Random(
                        seed
                                ^ (0x9E3779B97F4A7C15L * (depth + 1))
                                ^ (branch * 8191L)
                                ^ (cycle * 31337L));
        int sector = depth / 4;
        RoomPlan.Kind kind =
                depth == 11
                        ? RoomPlan.Kind.BRIDGE
                        : depth == 3 || depth == 7
                                ? RoomPlan.Kind.WORKSHOP
                                : branch == 1 && depth % 3 == 2
                                        ? RoomPlan.Kind.CACHE
                                        : branch == 1 || depth == 10
                                                ? RoomPlan.Kind.ELITE
                                                : RoomPlan.Kind.COMBAT;
        var enemies = new ArrayList<EnemyKind>();
        if (kind == RoomPlan.Kind.BRIDGE) enemies.add(EnemyKind.CAPTAIN);
        else if (kind == RoomPlan.Kind.COMBAT || kind == RoomPlan.Kind.ELITE) {
            int count = depth == 0 ? 1 : 2 + sector + (kind == RoomPlan.Kind.ELITE ? 1 : 0);
            for (int i = 0; i < count; i++) {
                EnemyKind enemy =
                        depth == 0
                                ? EnemyKind.SCUTTLER
                                : i == count - 1 && (sector > 0 || kind == RoomPlan.Kind.ELITE)
                                        ? EnemyKind.SENTINEL
                                        : random.nextDouble() < .4
                                                ? EnemyKind.DRONE
                                                : EnemyKind.SCUTTLER;
                enemies.add(enemy);
            }
        }
        String title =
                kind == RoomPlan.Kind.WORKSHOP
                        ? "Die stille Werkstatt"
                        : kind == RoomPlan.Kind.CACHE
                                ? "Vergessenes Depot"
                                : TITLES[sector][depth % 4];
        String description =
                switch (kind) {
                    case COMBAT -> "Sichere den Raum. Ein Modul als Belohnung.";
                    case ELITE -> "Drei Patrouillen. Ein Modul mit zwei Stufen als Belohnung.";
                    case WORKSHOP -> "Durchatmen. Reparieren. Den Build verstärken.";
                    case CACHE -> "Sichere Vorräte: Heilung und Schrott, aber kein Modul.";
                    case BRIDGE -> "Übernimm die Brücke. Der Lotse wartet.";
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
                kind == RoomPlan.Kind.ELITE ? 16 : kind == RoomPlan.Kind.CACHE ? 8 : 6);
    }
}
