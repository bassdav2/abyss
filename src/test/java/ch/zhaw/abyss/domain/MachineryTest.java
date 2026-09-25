package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

/** Raumtechnik: Schub, Dampfdüsen, Pressen, Laser, Notschalter und ihre Platzierung. */
class MachineryTest {

    private static GameRun emptyRun() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.fixtures.clear();
        return run;
    }

    @Test
    void conveyorCarriesStandingActors() {
        var run = emptyRun();
        var enemy = TestRuns.place(run, EnemyKind.SCUTTLER, run.player.x + 60);
        enemy.state = Enemy.State.STUNNED;
        enemy.stateTime = 5;
        run.fixtures.add(new Fixture(Fixture.Kind.CONVEYOR, run.player.x, 1, 0, 0));
        double start = run.player.x, enemyStart = enemy.x;
        TestRuns.seconds(run, InputFrame.NONE, .5);
        assertEquals(GameRun.Phase.RUNNING, run.phase());
        assertEquals(
                start + 115 * .5, run.player.x, 3, "Band schiebt mit 115 Einheiten pro Sekunde");
        assertTrue(enemy.x > enemyStart + 40, "auch Gegner am Boden werden mitgenommen");
    }

    @Test
    void ventPadLaunchesThePlayerAndRecharges() {
        var run = emptyRun();
        run.fixtures.add(new Fixture(Fixture.Kind.VENT_PAD, run.player.x, 1, 1, 0));
        run.drainEvents();
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertFalse(run.player.grounded);
        assertTrue(run.player.vy < -1000, "Düse schleudert höher als ein Sprung");
        assertTrue(
                run.drainEvents().stream()
                        .anyMatch(
                                e ->
                                        e.type() == GameEvent.Type.MACHINE
                                                && e.text().equals("VENT")));
        assertFalse(run.fixtures.getFirst().active(), "kurze Abklingzeit nach dem Start");
        run.player.x += 400;
        TestRuns.seconds(run, InputFrame.NONE, 1.2);
        assertTrue(run.fixtures.getFirst().active(), "Düse lädt nach");
    }

    @Test
    void pressStrikesOncePerCycleAndCrushesEnemiesUnderneath() {
        var run = emptyRun();
        var press = new Fixture(Fixture.Kind.PRESS, 900, 1, 1, 0);
        run.fixtures.add(press);
        var enemy = TestRuns.place(run, EnemyKind.SENTINEL, 900);
        enemy.state = Enemy.State.STUNNED;
        enemy.stateTime = 20;
        TestRuns.seconds(run, InputFrame.NONE, 3.2);
        assertTrue(press.warning(), "Vorwarnung vor dem Schlag");
        assertEquals(enemy.maxHealth(), enemy.health(), "noch kein Treffer während der Warnung");
        TestRuns.seconds(run, InputFrame.NONE, .2);
        double afterStrike = enemy.health();
        assertTrue(afterStrike < enemy.maxHealth(), "Presse trifft den Gegner darunter");
        TestRuns.seconds(run, InputFrame.NONE, .5);
        assertEquals(afterStrike, enemy.health(), 1e-9, "ein Schlag pro Takt");
        assertEquals(run.player.maxHealth, run.player.health, 1e-9, "Figur stand nicht darunter");
    }

    @Test
    void laserHurtsOncePerActivationAndCanBeDisabled() {
        var run = emptyRun();
        var laser = new Fixture(Fixture.Kind.LASER, run.player.x, 1, 3, 2.6);
        run.fixtures.add(laser);
        run.player.invulnerableTime = 0;
        run.update(TestRuns.STEP, InputFrame.NONE);
        double hurt = run.player.health;
        assertTrue(hurt < run.player.maxHealth, "aktiver Laser trifft");
        run.player.invulnerableTime = 0;
        TestRuns.seconds(run, InputFrame.NONE, .5);
        assertEquals(hurt, run.player.health, 1e-9, "nur einmal pro Aktivierung");
        laser.disabled = 5;
        assertFalse(laser.active());
        assertFalse(laser.warning());
    }

    @Test
    void consolesTriggerTheirSectorEffectAndCoolDown() {
        for (int sector = 0; sector < 4; sector++) {
            var run = emptyRun();
            var console = new Fixture(Fixture.Kind.CONSOLE, run.player.x + 20, 1, sector, 0);
            run.fixtures.add(console);
            var laser = new Fixture(Fixture.Kind.LASER, 1500, 1, sector, 2.6);
            run.fixtures.add(laser);
            var enemy = TestRuns.place(run, EnemyKind.SENTINEL, run.player.x + 400);
            assertEquals(Interaction.CONSOLE, run.interaction());
            assertTrue(run.useConsole(), "Sektor " + sector);
            assertFalse(run.useConsole(), "Abklingzeit verhindert sofortige Wiederholung");
            assertNotEquals(Interaction.CONSOLE, run.interaction());
            switch (sector) {
                case 0 ->
                        assertTrue(
                                run.projectiles.stream()
                                        .anyMatch(
                                                q ->
                                                        q.friendly()
                                                                && q.kind()
                                                                        == Projectile.Kind
                                                                                .TORPEDO));
                case 1 -> assertTrue(enemy.health() < enemy.maxHealth(), "Dampf trifft am Boden");
                case 2 -> assertTrue(enemy.statuses.active(Status.FREEZE));
                default -> {
                    assertTrue(enemy.statuses.active(Status.MARK));
                    assertEquals(Enemy.State.STUNNED, enemy.state);
                    assertTrue(laser.disabled() > 0, "Überlast schaltet Lasergitter ab");
                }
            }
        }
    }

    @Test
    void machinesPierceBossArmorAndExposeTheCore() {
        var run = emptyRun();
        var boss = TestRuns.place(run, EnemyKind.WARDEN, 900);
        boss.state = Enemy.State.APPROACH;
        assertTrue(boss.armored());
        double before = boss.health();
        run.combat.hitEnemy(boss, 70, Combat.Source.MACHINE, 0, 900);
        assertEquals(before - 70, boss.health(), 1e-9, "Anlagen ignorieren die Panzerung");
        var press = new Fixture(Fixture.Kind.PRESS, boss.x, 1, 0, 3.29);
        run.fixtures.add(press);
        run.update(TestRuns.STEP, InputFrame.NONE);
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertEquals(Enemy.State.RECOVER, boss.state, "Presse legt den Kern frei");
        assertFalse(boss.armored());
    }

    @Test
    void listingShipPushesEveryoneAndDropsDebris() {
        var run =
                reach(
                        room ->
                                room.condition() == RoomCondition.LIST
                                        && room.kind() == RoomPlan.Kind.COMBAT);
        run.player.invulnerableTime = 1e9;
        int hazards = run.hazards.size();
        boolean pushed = false;
        for (int i = 0; i < 120 * 11 && run.phase() == GameRun.Phase.RUNNING; i++) {
            double x = run.player.x;
            run.update(TestRuns.STEP, InputFrame.NONE);
            if (Math.abs(run.listTilt()) > .5 && Math.abs(run.player.x - x) > 1) pushed = true;
            if (run.hazards.size() > hazards + 2) hazards = -100;
        }
        assertTrue(pushed || run.phase() != GameRun.Phase.RUNNING, "Krängung schiebt die Figur");
        assertTrue(hazards < 0 || run.phase() != GameRun.Phase.RUNNING, "Trümmer fallen");
    }

    private static GameRun reach(java.util.function.Predicate<RoomPlan> target) {
        for (long seed = 1; seed < 800; seed++) {
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
    void dangerousMachinesRestOnceTheRoomIsSecured() {
        var run = TestRuns.standard();
        TestRuns.clear(run);
        assertEquals(GameRun.Phase.ROOM_CLEARED, run.phase());
        run.fixtures.clear();
        var press = new Fixture(Fixture.Kind.PRESS, run.player.x, 1, 1, 3.2);
        run.fixtures.add(press);
        run.player.invulnerableTime = 0;
        double health = run.player.health;
        TestRuns.seconds(run, InputFrame.NONE, 1);
        assertEquals(health, run.player.health, 1e-9);
        assertFalse(press.active());
    }

    @Test
    void generatorPlacesSectorMachinesInsideCombatRoomsWithoutOverlap() {
        var seen = EnumSet.noneOf(Fixture.Kind.class);
        for (long seed = 0; seed < 200; seed++) {
            var generator = new RoomGenerator(seed, 0);
            for (int depth = 0; depth < RoomGenerator.ROOM_COUNT; depth++)
                for (var room : generator.choices(depth)) {
                    var slots = room.fixtures();
                    boolean arena =
                            room.kind() == RoomPlan.Kind.BOSS
                                    || room.kind() == RoomPlan.Kind.BRIDGE;
                    if (arena) assertFalse(slots.isEmpty(), "Bossarena mit Anlagen");
                    else if (depth == 0
                            || room.kind() != RoomPlan.Kind.COMBAT
                                    && room.kind() != RoomPlan.Kind.ELITE)
                        assertTrue(slots.isEmpty(), "keine Technik in " + room.kind());
                    for (int i = 0; i < slots.size(); i++) {
                        var a = slots.get(i);
                        seen.add(a.kind());
                        assertTrue(a.x() - a.kind().width() / 2 > RoomLayout.WALL);
                        assertTrue(a.x() + a.kind().width() / 2 < room.layout().width());
                        assertTrue(a.x() >= 560, "Startbereich bleibt frei");
                        for (int j = i + 1; j < slots.size(); j++) {
                            var b = slots.get(j);
                            assertTrue(
                                    Math.abs(a.x() - b.x())
                                            >= (a.kind().width() + b.kind().width()) / 2 + 60);
                        }
                    }
                    assertTrue(
                            slots.stream().filter(s -> s.kind() == Fixture.Kind.CONSOLE).count()
                                    <= 1);
                }
            assertEquals(generator.room(8, 0), new RoomGenerator(seed, 0).room(8, 0));
        }
        assertEquals(EnumSet.allOf(Fixture.Kind.class), seen);
    }
}
