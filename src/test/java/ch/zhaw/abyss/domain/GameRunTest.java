package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameRunTest {
    private GameRun run() {
        return new GameRun(73419, ActiveModule.PULSE, false);
    }

    private void clear(GameRun run) {
        for (int tick = 0; tick < 1000 && run.phase() == GameRun.Phase.RUNNING; tick++) {
            for (Enemy enemy : run.enemies()) run.damageEnemy(enemy, 100_000);
            run.update(1.0 / 120, InputFrame.NONE);
        }
        assertNotEquals(GameRun.Phase.RUNNING, run.phase());
    }

    @Test
    void routesAreDeterministicBoundedAndAlwaysReachTheBridge() {
        for (long seed = 0; seed < 1000; seed++) {
            var first = new RoomGenerator(seed, 0);
            var second = new RoomGenerator(seed, 0);
            for (int depth = 0; depth < RoomGenerator.ROOM_COUNT; depth++) {
                var choices = first.choices(depth);
                assertEquals(choices, second.choices(depth));
                assertFalse(choices.isEmpty());
                for (var choice : choices) {
                    assertEquals(depth, choice.depth());
                    assertEquals(depth / 6, choice.sector());
                    assertTrue(choice.enemies().size() <= 5);
                }
            }
            assertEquals(RoomPlan.Kind.BRIDGE, first.choices(17).getFirst().kind());
        }
    }

    @Test
    void unclearedRoomCannotBeSkipped() {
        var run = run();
        assertFalse(run.chooseNextRoom(0));
        assertEquals(0, run.room().depth());
    }

    @Test
    void meleeHitsOnlyInFrontAndOnlyOncePerCooldown() {
        var run = run();
        run.enemies.clear();
        var front = new Enemy(10, EnemyKind.SENTINEL, run.player.x + 90, 1, 10);
        var behind = new Enemy(11, EnemyKind.SENTINEL, run.player.x - 90, 1, 10);
        run.enemies.add(front);
        run.enemies.add(behind);
        var attack = new InputFrame(false, false, false, false, true, false);
        run.update(1.0 / 120, attack);
        double hitHealth = front.health();
        assertTrue(hitHealth < front.maxHealth());
        assertEquals(behind.maxHealth(), behind.health());
        run.update(1.0 / 120, attack);
        assertEquals(hitHealth, front.health());
    }

    @Test
    void mouseDirectionCanAimWhileMovingBackwards() {
        var run = run();
        run.enemies.clear();
        var enemy = new Enemy(10, EnemyKind.SENTINEL, run.player.x + 95, 1, 10);
        run.enemies.add(enemy);
        double x = run.player.x;
        run.update(1.0 / 120, new InputFrame(true, false, false, false, true, false, 1));
        assertTrue(run.player.x < x);
        assertEquals(1, run.player.facing());
        assertTrue(enemy.health() < enemy.maxHealth());
    }

    @Test
    void dodgePreventsDamageAndCannotBeRepeatedImmediately() {
        var run = run();
        run.player.invulnerableTime = 0;
        run.update(1.0 / 120, new InputFrame(false, true, false, true, false, false));
        double health = run.player.health();
        run.damagePlayer(20, 0);
        assertEquals(health, run.player.health());
        double cooldown = run.player.dashCooldown();
        run.update(1.0 / 120, new InputFrame(false, true, false, true, false, false));
        assertTrue(run.player.dashCooldown() < cooldown);
    }

    @Test
    void damageGracePeriodPreventsMultipleSimultaneousHits() {
        var run = run();
        run.player.invulnerableTime = 0;
        run.damagePlayer(10, 0);
        run.damagePlayer(20, 0);
        assertEquals(90, run.player.health());
    }

    @Test
    void jumpClearsFloorShockwave() {
        var run = run();
        run.player.invulnerableTime = 0;
        run.update(1.0 / 120, new InputFrame(false, false, true, false, false, false));
        for (int i = 0; i < 20; i++) run.update(1.0 / 120, InputFrame.NONE);
        assertTrue(run.player.y() < GameRun.FLOOR - 70);
        run.addProjectile(
                Projectile.Kind.SHOCKWAVE,
                false,
                run.player.x,
                GameRun.FLOOR - 18,
                0,
                0,
                20,
                22,
                1);
        run.update(1.0 / 120, InputFrame.NONE);
        assertEquals(100, run.player.health());
    }

    @Test
    void insufficientEnergyCannotCastModule() {
        var run = run();
        run.player.energy = 5;
        run.update(1.0 / 120, new InputFrame(false, false, false, false, false, true));
        assertEquals(0, run.player.abilityCooldown());
        assertTrue(run.player.energy() >= 5);
    }

    @Test
    void rewardIsOneChoiceAndCheckpointIsIndependentOfLaterMutations() {
        var run = run();
        clear(run);
        var offers = run.rewardOffers();
        assertEquals(3, offers.size());
        assertTrue(run.claimReward(offers.getFirst()));
        assertFalse(run.claimReward(offers.get(1)));
        assertTrue(run.chooseNextRoom(0));
        var snapshot = run.checkpoint();
        run.player.salvage += 100;
        run.player.health -= 20;
        var resumed = GameRun.restore(snapshot);
        assertEquals(snapshot.health(), resumed.player.health());
        assertEquals(snapshot.salvage(), resumed.player.salvage());
        assertEquals(snapshot.upgrades(), resumed.player.upgrades());
        assertEquals(run.room(), resumed.room());
    }

    @Test
    void defeatEndsSimulationAndDoesNotAllowLeaving() {
        var run = run();
        run.player.invulnerableTime = 0;
        run.damagePlayer(1000, 0);
        run.update(1.0 / 120, InputFrame.NONE);
        assertEquals(GameRun.Phase.DEFEAT, run.phase());
        double x = run.player.x();
        run.update(1.0 / 120, new InputFrame(false, true, false, true, true, true));
        assertEquals(x, run.player.x());
        assertFalse(run.chooseNextRoom(0));
    }

    @Test
    void allEighteenRoomsEndInVictoryAndNextCyclePreservesBuild() {
        var run = run();
        for (int depth = 0; depth < RoomGenerator.ROOM_COUNT; depth++) {
            clear(run);
            if (depth < RoomGenerator.ROOM_COUNT - 1) {
                if (run.rewardAvailable() && !run.rewardOffers().isEmpty())
                    run.claimReward(run.rewardOffers().getFirst());
                assertTrue(run.chooseNextRoom(0));
            }
        }
        assertEquals(GameRun.Phase.VICTORY, run.phase());
        var upgrades = run.player.upgrades();
        assertTrue(run.nextCycle());
        assertEquals(1, run.cycle());
        assertEquals(0, run.room().depth());
        assertEquals(upgrades, run.player.upgrades());
        assertTrue(run.enemies().getFirst().maxHealth() > EnemyKind.SCUTTLER.health);
    }

    @Test
    void upgradeCapsAndRepairRulesPreventUnlimitedFarming() {
        var run = run();
        for (int i = 0; i < 5; i++) {
            clear(run);
            assertTrue(run.chooseNextRoom(0));
        }
        assertEquals(RoomPlan.Kind.WORKSHOP, run.room().kind());
        run.player.health = 10;
        assertTrue(run.repair());
        assertEquals(50, run.player.health());
        assertFalse(run.repair());
        for (int i = 0; i < 3; i++) run.player.upgrade(Upgrade.PLATING);
        assertThrows(IllegalStateException.class, () -> run.player.upgrade(Upgrade.PLATING));
    }

    @Test
    void invalidCheckpointAndOversizedTimestepsAreRejected() {
        var checkpoint = run().checkpoint();
        var bad =
                new RunCheckpoint(
                        checkpoint.seed(),
                        0,
                        0,
                        0,
                        ActiveModule.PULSE,
                        false,
                        Double.NaN,
                        100,
                        0,
                        checkpoint.upgrades(),
                        0,
                        0,
                        checkpoint.route());
        assertThrows(IllegalArgumentException.class, () -> GameRun.restore(bad));
        assertThrows(IllegalArgumentException.class, () -> run().update(1, InputFrame.NONE));
    }

    @Test
    void hazardsHaveAnEntryGracePeriodAndWarningBeforeDamage() {
        var hazard = new Hazard(500, Hazard.Kind.STEAM);
        assertFalse(hazard.warning());
        assertFalse(hazard.active());
        hazard.update(3.1);
        assertTrue(hazard.warning());
        assertFalse(hazard.active());
        hazard.update(1.1);
        assertTrue(hazard.active());
        hazard.update(1.5);
        assertFalse(hazard.active());
    }

    @Test
    void eliteRewardGrantsTwoRanksButCacheOnlyGrantsSupplies() {
        var run = run();
        clear(run);
        assertTrue(run.chooseNextRoom(1));
        clear(run);
        var reward = run.rewardOffers().getFirst();
        assertTrue(run.claimReward(reward));
        assertEquals(2, run.player().stacks(reward));
        assertTrue(run.chooseNextRoom(1));
        assertEquals(RoomPlan.Kind.CACHE, run.room().kind());
        assertTrue(run.rewardOffers().isEmpty());
        run.player.health = 20;
        assertTrue(run.claimSupplies());
        assertEquals(45, run.player.health());
        assertFalse(run.claimSupplies());
    }

    @Test
    void bossArmorRewardsAttackingDuringRecovery() {
        var run = run();
        var boss = new Enemy(99, EnemyKind.CAPTAIN, 1000, 1, 0);
        run.damageEnemy(boss, 100);
        assertEquals(boss.maxHealth() - 22, boss.health(), .001);
        boss.state = Enemy.State.RECOVER;
        run.damageEnemy(boss, 100);
        assertEquals(boss.maxHealth() - 122, boss.health(), .001);
    }

    @Test
    void restoredRoomMatchesFreshEntryBeforeTheSameInputs() {
        var original = run();
        for (int depth = 0; depth < 5; depth++) {
            clear(original);
            assertTrue(original.chooseNextRoom(0));
        }
        var restored = GameRun.restore(original.checkpoint());
        for (int tick = 0; tick < 500; tick++) {
            var frame =
                    new InputFrame(
                            tick % 240 > 120,
                            tick % 240 <= 120,
                            tick % 100 == 0,
                            tick % 150 == 0,
                            true,
                            tick % 120 == 0);
            original.update(1.0 / 120, frame);
            restored.update(1.0 / 120, frame);
            assertEquals(original.player().health(), restored.player().health());
            assertEquals(original.player().x(), restored.player().x());
            assertEquals(original.player().energy(), restored.player().energy());
            assertEquals(original.enemies().size(), restored.enemies().size());
            for (int enemy = 0; enemy < original.enemies().size(); enemy++) {
                assertEquals(original.enemies().get(enemy).x(), restored.enemies().get(enemy).x());
                assertEquals(original.enemies().get(enemy).y(), restored.enemies().get(enemy).y());
                assertEquals(
                        original.enemies().get(enemy).health(),
                        restored.enemies().get(enemy).health());
            }
            original.drainEvents();
            restored.drainEvents();
        }
    }

    @Test
    void impossibleHistoricalRouteIsRejected() {
        var checkpoint = run().checkpoint();
        var impossible =
                new RunCheckpoint(
                        checkpoint.seed(),
                        0,
                        1,
                        0,
                        ActiveModule.PULSE,
                        false,
                        100,
                        100,
                        0,
                        java.util.Map.of(),
                        0,
                        0,
                        java.util.List.of(1, 0));
        assertThrows(IllegalArgumentException.class, () -> GameRun.restore(impossible));
        assertThrows(IllegalArgumentException.class, () -> new RoomGenerator(1, 0).room(5, 1));
    }
}
