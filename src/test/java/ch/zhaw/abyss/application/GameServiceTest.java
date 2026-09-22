package ch.zhaw.abyss.application;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.domain.*;
import ch.zhaw.abyss.ports.GameRepository;
import ch.zhaw.abyss.qa.CampaignPilot;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

class GameServiceTest {
    private static final class MemoryRepository implements GameRepository {
        Profile profile = Profile.fresh();
        Optional<RunCheckpoint> saved = Optional.empty();
        boolean failWrites;

        public Profile loadProfile() {
            return profile;
        }

        public Optional<RunCheckpoint> loadCheckpoint() {
            return saved;
        }

        public void saveProfile(Profile next) throws IOException {
            if (failWrites) throw new IOException("test disk full");
            profile = next;
        }

        public void saveCheckpoint(RunCheckpoint next) throws IOException {
            if (failWrites) throw new IOException("test disk full");
            saved = Optional.of(next);
        }

        public void clearCheckpoint() {
            saved = Optional.empty();
        }
    }

    @Test
    void fullApplicationFlowUnlocksModulesRecordsVictoryOnceAndResumesNextCycle() {
        var repository = new MemoryRepository();
        var service = new GameService(repository);
        var run = service.start(0, ActiveModule.PULSE, true);
        for (int tick = 0;
                tick < 120 * 900
                        && run.phase() != GameRun.Phase.VICTORY
                        && run.phase() != GameRun.Phase.DEFEAT;
                tick++) {
            if (run.phase() == GameRun.Phase.ROOM_CLEARED) {
                assertTrue(CampaignPilot.advance(run, 0));
                service.saveRoom();
                if (run.room().depth() >= 5)
                    assertTrue(service.profile().unlocked().contains(ActiveModule.ARC));
                if (run.room().depth() >= 11)
                    assertTrue(service.profile().unlocked().contains(ActiveModule.AEGIS));
            } else run.update(1.0 / 120, CampaignPilot.input(run));
            run.drainEvents();
        }
        assertEquals(GameRun.Phase.VICTORY, run.phase());
        for (int duplicateFrame = 0; duplicateFrame < 100; duplicateFrame++)
            service.recordOutcome();
        assertEquals(1, service.profile().wins());
        assertEquals(run.kills(), service.profile().totalKills());
        assertTrue(repository.saved.isEmpty());
        var build = run.player().upgrades();
        assertTrue(service.nextCycle());
        assertEquals(1, run.cycle());
        assertEquals(build, run.player().upgrades());
        var reopened = new GameService(repository);
        var resumed = reopened.resume().orElseThrow();
        assertEquals(1, resumed.cycle());
        assertEquals(build, resumed.player().upgrades());
        assertEquals(service.profile(), reopened.profile());
        assertEquals(1, reopened.profile().runs());
    }

    @Test
    void failedStorageDoesNotEndCurrentPlayAndProducesConsumableMessage() {
        var repository = new MemoryRepository();
        repository.failWrites = true;
        var service = new GameService(repository);
        var run = assertDoesNotThrow(() -> service.start(3, ActiveModule.PULSE, false));
        assertEquals(GameRun.Phase.RUNNING, run.phase());
        assertFalse(service.takeStorageMessage().isBlank());
        assertTrue(service.takeStorageMessage().isBlank());
        assertTrue(repository.saved.isEmpty());
    }
}
