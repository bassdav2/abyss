package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

/** Reproduzierbare Route: feste Wächter und Werkstätten, gültige Geometrie, begrenzte Wellen. */
class RoomGeneratorTest {

    @Test
    void routesAreDeterministicAndStructurallyValidForManySeeds() {
        for (long seed = 0; seed < 600; seed++) {
            var first = new RoomGenerator(seed, 0);
            var second = new RoomGenerator(seed, 0);
            var specials = EnumSet.noneOf(RoomPlan.Kind.class);
            for (int depth = 0; depth < RoomGenerator.ROOM_COUNT; depth++) {
                var choices = first.choices(depth);
                assertEquals(choices, second.choices(depth));
                assertEquals(RoomGenerator.fixed(depth) ? 1 : 2, choices.size());
                for (var room : choices) {
                    assertEquals(depth, room.depth());
                    assertEquals(depth / RoomGenerator.SECTOR_ROOMS, room.sector());
                    assertTrue(room.waveCount() <= 3);
                    room.waves().forEach(wave -> assertTrue(wave.size() <= 6 && !wave.isEmpty()));
                    for (var wave : room.waves())
                        for (var spawn : wave) {
                            assertTrue(spawn.x() > 0 && spawn.x() < room.layout().width());
                            assertTrue(
                                    spawn.kind().minSector() <= room.sector()
                                            || spawn.kind() == EnemyKind.SCUTTLER);
                        }
                    specials.add(room.kind());
                }
            }
            assertEquals(RoomPlan.Kind.BRIDGE, first.room(23, 0).kind());
            assertTrue(
                    specials.containsAll(
                            EnumSet.of(
                                    RoomPlan.Kind.MERCHANT,
                                    RoomPlan.Kind.SHRINE,
                                    RoomPlan.Kind.CACHE,
                                    RoomPlan.Kind.ELITE)));
        }
    }

    @Test
    void roomConditionsAppearOnlyInCombatRoomsAndChangeTheRules() {
        var seen = EnumSet.noneOf(RoomCondition.class);
        for (long seed = 0; seed < 300; seed++) {
            var generator = new RoomGenerator(seed, 0);
            for (int depth = 0; depth < RoomGenerator.ROOM_COUNT; depth++)
                for (var room : generator.choices(depth)) {
                    var condition = room.condition();
                    seen.add(condition);
                    if (condition == RoomCondition.NONE) continue;
                    assertTrue(depth >= 2, "keine Zustände in den ersten Räumen");
                    assertTrue(
                            room.kind() == RoomPlan.Kind.COMBAT
                                    || room.kind() == RoomPlan.Kind.ELITE);
                    switch (condition) {
                        case BLACKOUT -> {
                            int base =
                                    room.kind() == RoomPlan.Kind.COMBAT
                                            ? 6 + 2 * room.sector()
                                            : 14 + 3 * room.sector();
                            assertEquals(Math.round(base * 1.6), room.salvageReward());
                        }
                        case ALARM -> {
                            assertEquals(RoomPlan.Kind.COMBAT, room.kind());
                            room.waves().forEach(wave -> assertTrue(wave.size() >= 2));
                        }
                        case LIST -> {
                            int base =
                                    room.kind() == RoomPlan.Kind.COMBAT
                                            ? 6 + 2 * room.sector()
                                            : 14 + 3 * room.sector();
                            assertEquals(Math.round(base * 1.4), room.salvageReward());
                            assertTrue(room.sector() >= 1);
                        }
                        case BREACH -> {
                            assertEquals(RoomPlan.Kind.COMBAT, room.kind());
                            assertTrue(room.sector() >= 1, "Hüllenbruch erst ab dem Maschinendeck");
                        }
                        case LEAK -> {
                            assertTrue(
                                    room.hazards().stream()
                                                    .filter(h -> h.kind() == Hazard.Kind.STEAM)
                                                    .count()
                                            >= 2);
                            assertTrue(room.crates().size() >= 3);
                        }
                        default -> {}
                    }
                }
            assertEquals(generator.room(9, 0), new RoomGenerator(seed, 0).room(9, 0));
        }
        assertEquals(EnumSet.allOf(RoomCondition.class), seen);
    }

    @Test
    void wardensGuardEachSectorAndWorkshopsFollowThem() {
        var generator = new RoomGenerator(7, 0);
        assertEquals(EnemyKind.WARDEN, generator.room(4, 0).enemies().getFirst());
        assertEquals(EnemyKind.REACTOR, generator.room(10, 0).enemies().getFirst());
        assertEquals(EnemyKind.BROOD, generator.room(16, 0).enemies().getFirst());
        assertEquals(EnemyKind.CAPTAIN, generator.room(23, 0).enemies().getFirst());
        for (int depth : new int[] {5, 11, 17, 22})
            assertEquals(RoomPlan.Kind.WORKSHOP, generator.room(depth, 0).kind());
        assertTrue(
                RoomGenerator.bossDepth(4)
                        && RoomGenerator.bossDepth(23)
                        && !RoomGenerator.bossDepth(22));
    }

    @Test
    void laterCyclesAndPressureProduceLargerWaves() {
        int base = 0, harder = 0;
        for (long seed = 0; seed < 50; seed++) {
            base +=
                    new RoomGenerator(seed, 0, 0)
                            .room(19, 0).waves().stream().mapToInt(java.util.List::size).sum();
            harder +=
                    new RoomGenerator(seed, 3, 5)
                            .room(19, 0).waves().stream().mapToInt(java.util.List::size).sum();
        }
        assertTrue(harder > base);
    }

    @Test
    void invalidRoomsAreRejected() {
        var generator = new RoomGenerator(1, 0);
        assertThrows(IllegalArgumentException.class, () -> generator.room(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> generator.room(24, 0));
        assertThrows(IllegalArgumentException.class, () -> generator.room(4, 1));
        assertThrows(IllegalArgumentException.class, () -> generator.choices(99));
    }

    @Test
    void layoutsKeepPlatformsInsideTheRoomAndReachable() {
        for (long seed = 0; seed < 200; seed++) {
            var generator = new RoomGenerator(seed, 0);
            for (int depth = 0; depth < RoomGenerator.ROOM_COUNT; depth++)
                for (var room : generator.choices(depth))
                    for (var platform : room.layout().platforms()) {
                        assertTrue(platform.y() >= 300 && platform.y() < GameRun.FLOOR);
                        assertTrue(platform.width() >= 60, "Stege sind begehbar breit");
                    }
        }
    }
}
