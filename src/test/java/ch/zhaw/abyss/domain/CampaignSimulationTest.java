package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.qa.CampaignPilot;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/** Vollständige Runs mit regulären Eingaben, ohne Unverwundbarkeit oder direktes Töten. */
class CampaignSimulationTest {
    static Stream<Arguments> configurations() {
        return Stream.of(ActiveModule.values())
                .flatMap(
                        module ->
                                Stream.of(false, true)
                                        .flatMap(
                                                explorer ->
                                                        Stream.of(0, 1)
                                                                .map(
                                                                        branch ->
                                                                                Arguments.of(
                                                                                        module,
                                                                                        explorer,
                                                                                        branch))));
    }

    @ParameterizedTest(name = "{0}, explorer={1}, preferred branch={2}")
    @MethodSource("configurations")
    void fullCampaignIsReachableWithEveryLoadoutAndRoute(
            ActiveModule module, boolean explorer, int branch) {
        int wins = 0;
        for (int seed = 0; seed < 12; seed++) {
            var run = new GameRun(seed, module, explorer);
            for (int tick = 0; tick < 120 * 900; tick++) {
                if (run.phase() == GameRun.Phase.DEFEAT || run.phase() == GameRun.Phase.VICTORY)
                    break;
                if (run.phase() == GameRun.Phase.ROOM_CLEARED)
                    assertTrue(CampaignPilot.advance(run, branch));
                else run.update(1.0 / 120, CampaignPilot.input(run));
                var p = run.player();
                assertTrue(Double.isFinite(p.x()) && Double.isFinite(p.y()));
                assertTrue(p.health() >= 0 && p.health() <= p.maxHealth());
                assertTrue(p.energy() >= 0 && p.energy() <= p.maxEnergy());
                run.drainEvents();
            }
            assertTrue(
                    run.phase() == GameRun.Phase.VICTORY || run.phase() == GameRun.Phase.DEFEAT,
                    "Kein Run darf blockieren.");
            boolean won = run.phase() == GameRun.Phase.VICTORY;
            if (won) wins++;
            System.out.printf(
                    "CAMPAIGN seed=%d module=%s explorer=%s branch=%d won=%s room=%d kills=%d"
                            + " seconds=%.1f health=%.1f%n",
                    seed,
                    module,
                    explorer,
                    branch,
                    won,
                    run.room().depth() + 1,
                    run.kills(),
                    run.elapsed(),
                    run.player().health());
        }
        assertTrue(
                wins > 0,
                "Mindestens ein vollständiger Run muss pro Konfiguration erreichbar sein.");
    }
}
