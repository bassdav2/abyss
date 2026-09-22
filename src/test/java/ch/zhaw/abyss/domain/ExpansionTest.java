package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class ExpansionTest {
    @Test
    void everyRouteContainsThreeDifferentBossesAndTwoSafeWorkshops() {
        for (long seed = 0; seed < 100; seed++) {
            var generator = new RoomGenerator(seed, 0);
            assertEquals(List.of(EnemyKind.WARDEN), generator.room(4, 0).enemies());
            assertEquals(List.of(EnemyKind.REACTOR), generator.room(10, 0).enemies());
            assertEquals(List.of(EnemyKind.CAPTAIN), generator.room(17, 0).enemies());
            for (int depth : new int[] {4, 5, 10, 11, 17})
                assertEquals(1, generator.choices(depth).size());
            for (int depth : new int[] {5, 11})
                assertEquals(RoomPlan.Kind.WORKSHOP, generator.room(depth, 0).kind());
        }
    }

    @Test
    void supplyCrateBreaksWithNormalAttacksAndCannotBeFarmed() {
        var run = new GameRun(0, ActiveModule.PULSE, false);
        run.enemies.clear();
        run.crates.clear();
        var crate = new SupplyCrate(300, SupplyCrate.Kind.SALVAGE);
        run.crates.add(crate);
        run.player.x = 230;
        run.player.salvage = 0;
        for (int tick = 0; tick < 80; tick++)
            run.update(1.0 / 120, new InputFrame(false, false, false, false, true, false));
        assertFalse(crate.intact());
        int salvage = run.player.salvage();
        assertEquals(14, salvage); // 6 Raumabschluss + 8 Kiste
        for (int tick = 0; tick < 160; tick++)
            run.update(1.0 / 120, new InputFrame(false, false, false, false, true, false));
        assertEquals(salvage, run.player.salvage());
    }

    @Test
    void extendedReachAndAttackTempoActuallyAffectCombat() {
        var run = new GameRun(0, ActiveModule.PULSE, false);
        run.enemies.clear();
        run.crates.clear();
        var enemy = new Enemy(90, EnemyKind.SENTINEL, run.player.x + 225, 1, 100);
        run.enemies.add(enemy);
        run.update(1.0 / 120, new InputFrame(false, false, false, false, true, false));
        assertEquals(enemy.maxHealth(), enemy.health());
        run.player.upgrade(Upgrade.LANCE);
        run.player.upgrade(Upgrade.LANCE);
        run.player.upgrade(Upgrade.OVERCLOCK);
        run.player.attackCooldown = 0;
        run.update(1.0 / 120, new InputFrame(false, false, false, false, true, false));
        assertTrue(enemy.health() < enemy.maxHealth());
        assertTrue(run.player.attackCooldown < .36);
    }

    @Test
    void thirdMeleeStrikeChainsToASecondEnemyOutsideReach() {
        var run = new GameRun(0, ActiveModule.PULSE, false);
        run.enemies.clear();
        run.crates.clear();
        run.player.upgrade(Upgrade.ARC_COIL);
        var front = new Enemy(90, EnemyKind.SENTINEL, 260, 5, 100);
        var far = new Enemy(91, EnemyKind.SENTINEL, 475, 5, 100);
        run.enemies.add(front);
        run.enemies.add(far);
        for (int hit = 0; hit < 3; hit++) {
            run.player.attackCooldown = 0;
            run.update(1.0 / 120, new InputFrame(false, false, false, false, true, false));
        }
        assertEquals(far.maxHealth() - 15, far.health(), .001);
    }

    @Test
    void movementSiphonAndRepairSwarmHaveDistinctEffects() {
        var run = new GameRun(0, ActiveModule.PULSE, false);
        run.player.upgrade(Upgrade.THRUSTER);
        run.player.upgrade(Upgrade.SIPHON);
        run.player.upgrade(Upgrade.REGEN);
        run.player.energy = 0;
        run.player.health = 40;
        double x = run.player.x;
        run.update(1.0 / 120, new InputFrame(false, true, false, false, false, false));
        assertEquals(330.0 / 120, run.player.x - x, .001);
        assertEquals(6.5 / 120, run.player.energy, .001);
        run.enemies.clear();
        run.update(1.0 / 120, InputFrame.NONE);
        assertEquals(47, run.player.health, .001);
        run.update(1.0 / 120, InputFrame.NONE);
        assertEquals(47, run.player.health, .001);
    }

    @Test
    void bossPatternsDifferAndAlwaysExposeARecoveryWindow() {
        for (EnemyKind kind :
                new EnemyKind[] {EnemyKind.WARDEN, EnemyKind.REACTOR, EnemyKind.CAPTAIN}) {
            var run = new GameRun(1, ActiveModule.PULSE, false);
            run.enemies.clear();
            var boss = new Enemy(90, kind, 400, 1, 0);
            run.enemies.add(boss);
            run.player.x = 300;
            EnemyAi.update(boss, run, .01);
            assertEquals(Enemy.State.WINDUP, boss.state());
            assertEquals(kind == EnemyKind.WARDEN ? 2 : 0, boss.attackPattern());
            for (int step = 0; step < 200 && boss.state() != Enemy.State.RECOVER; step++)
                EnemyAi.update(boss, run, .01);
            assertEquals(Enemy.State.RECOVER, boss.state());
            assertFalse(boss.armored());
        }
    }
}
