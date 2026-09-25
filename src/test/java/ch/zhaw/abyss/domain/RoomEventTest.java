package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

/** Raumereignisse: Hüllenbruch mit Countdown und fliehende Schmugglerdrohnen. */
class RoomEventTest {

    private static GameRun reach(Predicate<RoomPlan> target) {
        for (long seed = 1; seed < 600; seed++) {
            var run = new GameRun(RunSetup.standard(seed, DiverClass.MECHANIC).withSeed(seed));
            for (int guard = 0; guard < RoomGenerator.ROOM_COUNT - 1; guard++) {
                if (target.test(run.room())) return run;
                TestRuns.clear(run);
                var choices = run.nextRooms();
                int branch = 0;
                for (int i = 0; i < choices.size(); i++)
                    if (target.test(choices.get(i))) branch = i;
                run.chooseNextRoom(branch);
            }
        }
        throw new AssertionError("Raum nicht gefunden");
    }

    @Test
    void breachRoomsSendReinforcementsAndSealAfterTheCountdown() {
        var run = reach(room -> room.condition() == RoomCondition.BREACH);
        assertEquals(GameRun.BREACH_TIME, run.breachRemaining(), 1e-9);
        run.player.invulnerableTime = 1e9;
        int reinforcements = 0;
        for (int i = 0; i < 120 * 39 && run.phase() == GameRun.Phase.RUNNING; i++) {
            run.update(TestRuns.STEP, InputFrame.NONE);
            reinforcements +=
                    (int)
                            run.drainEvents().stream()
                                    .filter(e -> e.type() == GameEvent.Type.REINFORCEMENTS)
                                    .count();
        }
        assertEquals(GameRun.Phase.RUNNING, run.phase(), "Gegner besiegen allein sichert nicht");
        assertTrue(reinforcements >= 3, "Nachschub durch das Leck: " + reinforcements);
        assertTrue(run.breachLevel() > .9, "Wasser steigt mit der Zeit");
        TestRuns.seconds(run, InputFrame.NONE, 1.5);
        assertEquals(GameRun.Phase.ROOM_CLEARED, run.phase(), "nach 40 Sekunden dicht");
        assertTrue(run.enemies().stream().noneMatch(Enemy::alive), "Übrige Gegner fliehen");
        assertTrue(run.clearedConditions().contains(RoomCondition.BREACH));
        for (var offer : run.offers())
            if (offer.type() == Offer.Type.ITEM)
                assertTrue(offer.item().rarity().ordinal() >= Rarity.RARE.ordinal());
        TestRuns.seconds(run, InputFrame.NONE, 3.5);
        assertEquals(0, run.breachLevel(), 1e-9, "abgepumpt");
    }

    @Test
    void smugglerDronesFleeAndEscapeUnlessShotDown() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var drone = TestRuns.place(run, EnemyKind.SMUGGLER, run.player.x + 300);
        var guard = TestRuns.place(run, EnemyKind.SENTINEL, run.player.x + 900);
        guard.state = Enemy.State.STUNNED;
        guard.stateTime = 60;
        double start = drone.x;
        TestRuns.seconds(run, InputFrame.NONE, 3);
        assertTrue(drone.x > start, "flieht von der Figur weg");
        assertEquals(drone.maxHealth(), drone.health(), "greift nie an und wird nicht getroffen");
        TestRuns.seconds(run, InputFrame.NONE, 13);
        assertTrue(drone.escaped());
        assertFalse(drone.alive());
        assertEquals(0, run.smugglersCaught());

        var second = TestRuns.standard();
        TestRuns.empty(second);
        var target = TestRuns.place(second, EnemyKind.SMUGGLER, second.player.x + 300);
        TestRuns.place(second, EnemyKind.SENTINEL, second.player.x + 900).state =
                Enemy.State.STUNNED;
        second.combat.hitEnemy(target, 1000, Combat.Source.PROJECTILE, 0, second.player.x);
        assertFalse(target.alive());
        assertEquals(1, second.smugglersCaught());
        assertTrue(
                second.pickups.stream().anyMatch(p -> p.kind() == Pickup.Kind.CORE),
                "Beute: ein Datenkern");
    }
}
