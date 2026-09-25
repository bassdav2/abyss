package ch.zhaw.abyss.application;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.domain.*;
import ch.zhaw.abyss.ports.GameRepository;
import ch.zhaw.abyss.qa.CampaignPilot;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;

/** Anwendungsfälle mit einem Speicher im Arbeitsspeicher (Integrationstest ohne Dateisystem). */
class GameServiceTest {
    static final class MemoryRepository implements GameRepository {
        Profile profile = Profile.fresh();
        Optional<RunCheckpoint> saved = Optional.empty();
        boolean failWrites;

        @Override
        public Profile loadProfile() {
            return profile;
        }

        @Override
        public Optional<RunCheckpoint> loadCheckpoint() {
            return saved;
        }

        @Override
        public void saveProfile(Profile next) throws IOException {
            if (failWrites) throw new IOException("test disk full");
            profile = next;
        }

        @Override
        public void saveCheckpoint(RunCheckpoint next) throws IOException {
            if (failWrites) throw new IOException("test disk full");
            saved = Optional.of(next);
        }

        @Override
        public void clearCheckpoint() {
            saved = Optional.empty();
        }
    }

    private static GameRun playOut(GameService service, GameRun run) {
        for (int tick = 0;
                tick < 120 * 1200
                        && run.phase() != GameRun.Phase.VICTORY
                        && run.phase() != GameRun.Phase.DEFEAT;
                tick++) {
            if (run.phase() == GameRun.Phase.ROOM_CLEARED) {
                assertTrue(CampaignPilot.advance(run, 0));
                service.saveRoom();
            } else run.update(1.0 / 120, CampaignPilot.input(run));
            run.drainEvents();
        }
        return run;
    }

    @Test
    void dailySeedIsTheSameForEveryoneOnTheSameDay() {
        assertEquals(20260925L, GameService.dailySeed(java.time.LocalDate.of(2026, 9, 25)));
        assertNotEquals(
                GameService.dailySeed(java.time.LocalDate.of(2026, 9, 25)),
                GameService.dailySeed(java.time.LocalDate.of(2026, 9, 26)));
    }

    @Test
    void fullFlowRecordsVictoryOnceAwardsCoresAndResumesNextCycle() {
        var repository = new MemoryRepository();
        var service = new GameService(repository);
        var run = playOut(service, service.start(Loadout.DEFAULT, 5));
        assertEquals(GameRun.Phase.VICTORY, run.phase());
        for (int i = 0; i < 50; i++) service.recordOutcome();
        var profile = service.profile();
        assertEquals(1, profile.wins());
        assertEquals(run.kills(), profile.totalKills());
        assertEquals(1, profile.maxPressure(), "Sieg schaltet Druckstufe 1 frei");
        assertTrue(profile.cores() >= run.player().cores() + 6);
        assertTrue(profile.discovered().containsAll(run.player().items().keySet()));
        assertTrue(repository.saved.isEmpty());
        var build = run.player().items();
        assertTrue(service.nextCycle());
        var reopened = new GameService(repository);
        var resumed = reopened.resume().orElseThrow();
        assertEquals(1, resumed.cycle());
        assertEquals(build, resumed.player().items());
        assertEquals(service.profile(), reopened.profile());
    }

    @Test
    void victoryFillsTheLogbookOnceAndPaysItsCores() {
        var service = new GameService(new MemoryRepository());
        var run = playOut(service, service.start(Loadout.DEFAULT, 5));
        int before = service.profile().cores();
        service.recordOutcome();
        var profile = service.profile();
        var expected =
                EnumSet.of(
                        Achievement.FIRST_ROOM,
                        Achievement.WARDEN,
                        Achievement.REACTOR,
                        Achievement.BROOD,
                        Achievement.BRIDGE,
                        Achievement.WIN_MECHANIC);
        assertTrue(profile.achievements().containsAll(expected));
        assertFalse(profile.achievements().contains(Achievement.ALL_CLASSES));
        var shown = service.takeAchievements();
        assertTrue(shown.containsAll(expected));
        assertTrue(service.takeAchievements().isEmpty(), "Meldungen nur einmal");
        int rewards = profile.achievements().stream().mapToInt(Achievement::reward).sum();
        assertTrue(profile.cores() - before >= Achievement.BRIDGE.reward());
        assertTrue(profile.cores() >= rewards);
        service.recordOutcome();
        assertEquals(profile, service.profile(), "Sieg und Einträge nicht doppelt verbucht");
        assertEquals(GameRun.Phase.VICTORY, run.phase());
    }

    @Test
    void lockedChoicesAreRejectedUntilPurchased() {
        var service = new GameService(new MemoryRepository());
        var harpooner = Loadout.DEFAULT.withDiver(DiverClass.HARPOONER);
        assertThrows(IllegalArgumentException.class, () -> service.start(harpooner, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.start(Loadout.DEFAULT.withPressure(2), 1));
        var unlock = Unlock.find(Unlock.of(DiverClass.HARPOONER)).orElseThrow();
        assertFalse(service.purchase(unlock), "ohne Kerne kein Kauf");
        var rich = withCores(50);
        assertTrue(rich.purchase(unlock));
        assertEquals(50 - unlock.cost(), rich.profile().cores());
        assertFalse(rich.purchase(unlock), "kein Doppelkauf");
        assertDoesNotThrow(() -> rich.start(harpooner, 1));
    }

    private static GameService withCores(int cores) {
        var repository = new MemoryRepository();
        var edit = Profile.fresh().edit();
        edit.cores = cores;
        repository.profile = edit.build();
        return new GameService(repository);
    }

    @Test
    void perksRequireTheirPredecessorAndAffectNewRuns() {
        var service = withCores(100);
        var second = Unlock.find("perk:hull2").orElseThrow();
        assertFalse(service.purchase(second));
        assertTrue(service.purchase(Unlock.find("perk:hull1").orElseThrow()));
        assertTrue(service.purchase(second));
        assertTrue(service.purchase(Unlock.find("perk:scrap").orElseThrow()));
        var run = service.start(Loadout.DEFAULT, 3);
        assertEquals(120, run.player().maxHealth(), 1e-6);
        assertEquals(25, run.player().salvage());
    }

    @Test
    void cosmeticsNeedUnlockedOptions() {
        var service = new GameService(new MemoryRepository());
        assertTrue(service.cosmetics(new Cosmetics(1, 1, 1, 0)));
        assertFalse(service.cosmetics(new Cosmetics(5, 0, 0, 0)));
        service = withCores(10);
        assertTrue(service.purchase(Unlock.find(Cosmetics.unlockId("suit", 5)).orElseThrow()));
        assertTrue(service.cosmetics(new Cosmetics(5, 0, 0, 0)));
        assertEquals(5, service.profile().cosmetics().suit());
    }

    @Test
    void defeatStillBanksCollectedCores() {
        var repository = new MemoryRepository();
        var service = new GameService(repository);
        var run = service.start(Loadout.DEFAULT, 9);
        run.player().statuses();
        for (int i = 0; i < 5 && run.phase() == GameRun.Phase.RUNNING; i++)
            run.update(1.0 / 120, InputFrame.NONE);
        while (run.phase() == GameRun.Phase.RUNNING) {
            run.update(1.0 / 120, InputFrame.NONE);
            if (run.elapsed() > 600) break;
        }
        assertEquals(GameRun.Phase.DEFEAT, run.phase(), "ohne Eingaben unterliegt die Figur");
        service.recordOutcome();
        assertEquals(0, service.profile().wins());
        assertTrue(service.saved().isEmpty());
        assertEquals(1, service.profile().runs());
    }

    @Test
    void failedStorageDoesNotEndCurrentPlayAndProducesConsumableMessage() {
        var repository = new MemoryRepository();
        repository.failWrites = true;
        var service = new GameService(repository);
        var run = assertDoesNotThrow(() -> service.start(Loadout.DEFAULT, 3));
        assertEquals(GameRun.Phase.RUNNING, run.phase());
        assertFalse(service.takeStorageMessage().isBlank());
        assertTrue(service.takeStorageMessage().isBlank());
        assertTrue(repository.saved.isEmpty());
    }
}
