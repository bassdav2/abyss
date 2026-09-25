package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.qa.BalanceReport;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Systemnahe Kampagnensimulation: Der Testspieler spielt vollständige Tauchgänge mit regulären
 * Eingaben. Geprüft wird, dass jede Klasse spielbar bleibt, kein Raum hängen bleibt und Siege
 * möglich sind. Bot-Ergebnisse sind keine menschlichen Spieltests.
 */
class CampaignSimulationTest {

    @ParameterizedTest
    @EnumSource(DiverClass.class)
    void everyDiverCanReachTheBridgeWithoutStalling(DiverClass diver) {
        int wins = 0;
        for (long seed = 1; seed <= 8; seed++) {
            var run = BalanceReport.play(RunSetup.standard(seed * 7_919L, diver), 3600);
            assertNotEquals(
                    GameRun.Phase.RUNNING,
                    run.phase(),
                    diver + " hängt in Raum " + run.room().depth());
            assertNotEquals(
                    GameRun.Phase.ROOM_CLEARED,
                    run.phase(),
                    diver + " hängt nach Raum " + run.room().depth());
            if (run.phase() == GameRun.Phase.VICTORY) {
                wins++;
                assertEquals(RoomGenerator.ROOM_COUNT - 1, run.room().depth());
                assertTrue(run.kills() > 20);
            }
        }
        assertTrue(wins >= 3, diver + " gewinnt zu selten: " + wins + "/8");
    }

    @ParameterizedTest
    @EnumSource(DiverClass.class)
    void higherPressureIsHarderButStillPlayable(DiverClass diver) {
        var setup = RunSetup.standard(31337, diver);
        var hard =
                new RunSetup(
                        31337,
                        diver,
                        diver.weapon(),
                        diver.module(),
                        false,
                        5,
                        setup.itemPool(),
                        setup.weaponPool(),
                        0,
                        0,
                        0);
        var run = BalanceReport.play(hard, 3600);
        assertTrue(run.phase() == GameRun.Phase.VICTORY || run.phase() == GameRun.Phase.DEFEAT);
    }
}
