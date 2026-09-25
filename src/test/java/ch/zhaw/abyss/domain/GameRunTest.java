package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/** Regeln des Tauchgang-Aggregats: Phasen, Räume, Wellen, Sicherung und Wiederherstellung. */
class GameRunTest {

    @Test
    void newRunStartsInTheSternWithTwoScuttlersAndACheckpoint() {
        var run = TestRuns.standard();
        assertEquals(0, run.room().depth());
        assertEquals(GameRun.Phase.RUNNING, run.phase());
        assertEquals(2, run.enemies().size());
        assertTrue(run.enemies().stream().allMatch(e -> e.kind() == EnemyKind.SCUTTLER));
        assertEquals(List.of(0), run.checkpoint().route());
        assertEquals(DiverClass.MECHANIC, run.checkpoint().diver());
    }

    @Test
    void unclearedRoomCannotBeLeftAndExitIsLocked() {
        var run = TestRuns.standard();
        assertFalse(run.chooseNextRoom(0));
        run.player.x = run.layout().exitX();
        assertEquals(Interaction.LOCKED, run.interaction());
        assertEquals(0, run.room().depth());
    }

    @Test
    void clearingTheLastWaveOpensRewardAndExit() {
        var run = TestRuns.standard();
        TestRuns.clear(run);
        assertEquals(GameRun.Phase.ROOM_CLEARED, run.phase());
        assertTrue(run.rewardAvailable());
        assertFalse(run.offers().isEmpty());
        run.player.x = run.layout().rewardX();
        assertEquals(Interaction.REWARD, run.interaction());
        run.player.x = run.layout().exitX();
        assertEquals(Interaction.EXIT, run.interaction());
        assertTrue(run.chooseNextRoom(0));
        assertEquals(1, run.room().depth());
    }

    @Test
    void multiWaveRoomsAnnounceReinforcementsBeforeSpawning() {
        var run = TestRuns.standard();
        for (int depth = 0; depth < 7; depth++) {
            TestRuns.clear(run);
            run.chooseNextRoom(0);
        }
        assertTrue(run.room().waveCount() >= 2, "Sektor 2 hat mehrere Wellen");
        run.drainEvents();
        for (var enemy : run.enemies) enemy.health = 0;
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertTrue(
                run.drainEvents().stream()
                        .anyMatch(e -> e.type() == GameEvent.Type.REINFORCEMENTS));
        assertEquals(0, run.wave());
        TestRuns.seconds(run, InputFrame.NONE, 2);
        assertEquals(1, run.wave());
        assertFalse(run.enemies().isEmpty());
    }

    @Test
    void checkpointRestoresTheRoomEntranceIncludingBuild() {
        var run = TestRuns.standard();
        TestRuns.clear(run);
        run.take(run.offers().getFirst());
        run.chooseNextRoom(0);
        var saved = run.checkpoint();
        var restored = GameRun.restore(saved, run.itemPool(), RunSetup.defaultWeapons());
        assertEquals(saved, restored.checkpoint());
        assertEquals(run.player().items(), restored.player().items());
        assertEquals(run.room().depth(), restored.room().depth());
        assertEquals(run.room().title(), restored.room().title());
    }

    @Test
    void manipulatedCheckpointsAreRejected() {
        var saved = TestRuns.standard().checkpoint();
        var badHealth = copy(saved, 99999, saved.items(), saved.route());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        GameRun.restore(
                                badHealth, RunSetup.defaultItems(), RunSetup.defaultWeapons()));
        var badStacks = copy(saved, saved.health(), Map.of(Item.SERVO, 9), saved.route());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        GameRun.restore(
                                badStacks, RunSetup.defaultItems(), RunSetup.defaultWeapons()));
        var badRoute = copy(saved, saved.health(), saved.items(), List.of(1));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        GameRun.restore(
                                badRoute, RunSetup.defaultItems(), RunSetup.defaultWeapons()));
        assertThrows(
                IllegalArgumentException.class,
                () -> GameRun.restore(null, RunSetup.defaultItems(), RunSetup.defaultWeapons()));
    }

    private static RunCheckpoint copy(
            RunCheckpoint c, double health, Map<Item, Integer> items, List<Integer> route) {
        return new RunCheckpoint(
                c.seed(),
                c.cycle(),
                c.pressure(),
                c.depth(),
                c.branch(),
                c.diver(),
                c.weapon(),
                c.weaponLevel(),
                c.module(),
                c.explorer(),
                health,
                c.energy(),
                c.salvage(),
                c.repairKits(),
                items,
                c.kills(),
                c.elapsed(),
                route,
                c.cores(),
                c.reviveUsed(),
                c.rushStacks(),
                c.healthPenalty(),
                c.bonusHealth());
    }

    @Test
    void defeatEndsTheRunAndFurtherUpdatesChangeNothing() {
        var run = TestRuns.standard();
        run.player.health = 0;
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertEquals(GameRun.Phase.DEFEAT, run.phase());
        double elapsed = run.elapsed();
        run.update(TestRuns.STEP, InputFrame.builder().attack().build());
        assertEquals(elapsed, run.elapsed());
        assertFalse(run.chooseNextRoom(0));
    }

    @Test
    void simulationStepIsBounded() {
        var run = TestRuns.standard();
        assertThrows(IllegalArgumentException.class, () -> run.update(.1, InputFrame.NONE));
        run.update(-1, InputFrame.NONE);
        run.update(Double.NaN, InputFrame.NONE);
        assertEquals(0, run.elapsed());
    }

    @Test
    void bridgeVictoryAllowsAHarderCycleWithTheSameBuild() {
        var run = TestRuns.standard();
        while (run.room().kind() != RoomPlan.Kind.BRIDGE) {
            TestRuns.clear(run);
            run.chooseNextRoom(0);
        }
        run.player.install(Item.SERVO);
        TestRuns.clear(run);
        assertEquals(GameRun.Phase.VICTORY, run.phase());
        var build = run.player().items();
        double firstStrength = run.enemyHealth();
        assertTrue(run.nextCycle());
        assertEquals(1, run.cycle());
        assertEquals(0, run.room().depth());
        assertEquals(build, run.player().items());
        assertTrue(run.enemyHealth() > firstStrength * .6);
        assertFalse(run.nextCycle());
    }

    @Test
    void eventsAreDrainedExactlyOnce() {
        var run = TestRuns.standard();
        TestRuns.clear(run);
        var events = run.drainEvents();
        assertTrue(events.stream().anyMatch(e -> e.type() == GameEvent.Type.ROOM_CLEAR));
        assertTrue(run.drainEvents().isEmpty());
    }

    @Test
    void killingEnemiesCountsKillsAndDropsCollectableScrap() {
        var run = TestRuns.standard();
        var enemy = run.enemies.getFirst();
        run.combat.hitEnemy(enemy, 10_000, Combat.Source.ABILITY, 0, run.player.x);
        assertEquals(1, run.kills());
        assertFalse(run.pickups().isEmpty());
        int before = run.player.salvage;
        run.player.x = run.pickups().getFirst().x();
        TestRuns.seconds(run, InputFrame.NONE, 1.5);
        assertTrue(run.player.salvage > before);
    }
}
