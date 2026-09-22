package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RepairKitTest {
    @Test
    void repairKitConsumesOnlyWhenItCanHealAndStopsAtMaximum() {
        var run = new GameRun(4, ActiveModule.PULSE, false);
        assertEquals(1, run.player.repairKits());
        assertFalse(run.useRepairKit());
        run.player.health = 90;
        assertTrue(run.useRepairKit());
        assertEquals(100, run.player.health());
        assertEquals(0, run.player.repairKits());
        run.player.health = 50;
        assertFalse(run.useRepairKit());
        assertEquals(50, run.player.health());
    }

    @Test
    void qInputUsesOneKitAndCannotReviveDefeatedPlayer() {
        var run = new GameRun(4, ActiveModule.PULSE, false);
        run.player.health = 40;
        run.update(1.0 / 120, new InputFrame(false, false, false, false, false, false, 0, true));
        assertEquals(75, run.player.health());
        assertEquals(0, run.player.repairKits());
        run.player.health = 0;
        run.player.repairKits = 1;
        assertFalse(run.useRepairKit());
    }

    @Test
    void workshopPurchaseEnforcesCostCapacityAndPersistsAtNextRoom() {
        var run = new GameRun(4, ActiveModule.PULSE, false);
        assertFalse(run.buyRepairKit());
        for (int depth = 0; depth < 5; depth++) {
            while (run.phase() == GameRun.Phase.RUNNING) {
                run.enemies.clear();
                run.update(1.0 / 30, InputFrame.NONE);
            }
            assertTrue(run.chooseNextRoom(0));
        }
        run.player.salvage = 40;
        assertTrue(run.buyRepairKit());
        assertTrue(run.buyRepairKit());
        assertEquals(0, run.player.salvage());
        assertEquals(3, run.player.repairKits());
        run.player.salvage = 100;
        assertFalse(run.buyRepairKit());
        assertEquals(100, run.player.salvage());
        assertTrue(run.chooseNextRoom(0));
        assertEquals(3, GameRun.restore(run.checkpoint()).player().repairKits());
    }
}
