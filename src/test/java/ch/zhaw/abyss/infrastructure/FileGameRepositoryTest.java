package ch.zhaw.abyss.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.application.Achievement;
import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.application.Profile;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.application.Unlock;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.domain.Weapon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Versioniertes Dateiformat: Rundreise, Fehlerfälle, Sicherungskopien und Migration alter Stände.
 */
class FileGameRepositoryTest {
    @TempDir Path directory;

    @Test
    void profileAndCheckpointSurviveANewRepositoryInstance() throws Exception {
        var repository = new FileGameRepository(directory);
        var profile =
                new Profile(
                        3,
                        1,
                        12,
                        1,
                        29,
                        17,
                        2,
                        Set.of(Unlock.of(ActiveModule.ARC), "perk:kit"),
                        Set.of(Item.SERVO, Item.LENS),
                        Set.of(Achievement.WARDEN, Achievement.DARKNESS),
                        new Cosmetics(3, 2, 1, 0),
                        new Loadout(DiverClass.MECHANIC, Weapon.WRENCH, ActiveModule.ARC, 1, true),
                        new Settings(.4, .2, true, false, true, .5, false, true));
        var run = new GameRun(RunSetup.standard(123456, DiverClass.TITAN));
        repository.saveProfile(profile);
        repository.saveCheckpoint(run.checkpoint());
        var reopened = new FileGameRepository(directory);
        assertEquals(profile, reopened.loadProfile());
        assertEquals(run.checkpoint(), reopened.loadCheckpoint().orElseThrow());
        try (var files = Files.list(directory)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void unknownVersionFailsWithoutErasingTheOriginal() throws Exception {
        Path file = directory.resolve("profile.properties");
        String original = "version=999\nruns=100";
        Files.writeString(file, original);
        assertThrows(IOException.class, () -> new FileGameRepository(directory).loadProfile());
        assertEquals(original, Files.readString(file));
    }

    @Test
    void corruptedStateShowsARecoverableMessageAndANewRunCanStart() throws Exception {
        Files.writeString(
                directory.resolve("checkpoint.properties"), "version=3\nseed=not-a-number\n");
        var service = new GameService(new FileGameRepository(directory));
        assertTrue(service.saved().isEmpty());
        assertFalse(service.takeStorageMessage().isBlank());
        assertDoesNotThrow(() -> service.start(Loadout.DEFAULT, 12));
    }

    @Test
    void missingFilesCreateAFreshProfileAndStartedRunsAreResumable() {
        var service = new GameService(new FileGameRepository(directory));
        assertEquals(Profile.fresh(), service.profile());
        assertTrue(service.resume().isEmpty());
        service.start(Loadout.DEFAULT, 88);
        var reopened = new GameService(new FileGameRepository(directory));
        assertEquals(1, reopened.profile().runs());
        assertEquals(88, reopened.resume().orElseThrow().seed());
    }

    @Test
    void replacingUnreadableFilesKeepsExactOriginalBackups() throws Exception {
        String profile = "version=999\nruns=100";
        String checkpoint = "version=3\nseed=not-a-number\n";
        Files.writeString(directory.resolve("profile.properties"), profile);
        Files.writeString(directory.resolve("checkpoint.properties"), checkpoint);
        var service = new GameService(new FileGameRepository(directory));
        service.start(Loadout.DEFAULT, 42);
        try (var files = Files.list(directory)) {
            var backups = files.filter(path -> path.toString().endsWith(".bak")).toList();
            assertEquals(2, backups.size());
            var contents = new java.util.HashSet<String>();
            for (Path backup : backups) contents.add(Files.readString(backup));
            assertEquals(Set.of(profile, checkpoint), contents);
        }
        assertEquals(
                42,
                new GameService(new FileGameRepository(directory)).resume().orElseThrow().seed());
    }

    @Test
    void version2ProfileKeepsProgressAndUnlockedModules() throws Exception {
        Files.writeString(
                directory.resolve("profile.properties"),
                "version=2\n"
                        + "runs=7\n"
                        + "wins=1\n"
                        + "bestRoom=18\n"
                        + "bestCycle=1\n"
                        + "kills=124\n"
                        + "unlocked=PULSE,ARC,AEGIS\n"
                        + "volume=0.3\n"
                        + "music=0.2\n");
        var profile = new FileGameRepository(directory).loadProfile();
        assertEquals(7, profile.runs());
        assertEquals(24, profile.bestRoom());
        assertEquals(124, profile.totalKills());
        assertTrue(profile.owns(ActiveModule.ARC) && profile.owns(ActiveModule.AEGIS));
        assertEquals(3, profile.cores());
        assertEquals(.3, profile.settings().masterVolume(), 1e-9);
    }

    @Test
    void version2CheckpointMigratesToAPlayableRoomAndKeepsTheOriginal() throws Exception {
        String original =
                "version=2\nseed=73419\ncycle=0\ndepth=8\nbranch=0\nmodule=PULSE\nexplorer=false\n"
                        + "health=90\nenergy=80\nsalvage=45\nkills=23\nelapsed=420\nrepairKits=2\n"
                        + "upgrade.SERVO=2\nupgrade.LANCE=1\nroute=0,0,1,0,0,0,1,0,0\n";
        Files.writeString(directory.resolve("checkpoint.properties"), original);
        var repository = new FileGameRepository(directory);
        var checkpoint = repository.loadCheckpoint().orElseThrow();
        assertEquals(8, checkpoint.depth());
        assertEquals(2, checkpoint.items().get(Item.SERVO));
        assertEquals(DiverClass.MECHANIC, checkpoint.diver());
        var run =
                assertDoesNotThrow(
                        () ->
                                GameRun.restore(
                                        checkpoint,
                                        RunSetup.defaultItems(),
                                        RunSetup.defaultWeapons()));
        assertEquals(8, run.room().depth());
        repository.saveCheckpoint(run.checkpoint());
        try (var files = Files.list(directory)) {
            assertTrue(
                    files.anyMatch(
                            path -> {
                                try {
                                    return path.toString().endsWith(".bak")
                                            && Files.readString(path).equals(original);
                                } catch (IOException e) {
                                    return false;
                                }
                            }));
        }
    }

    @Test
    void clearingTheCheckpointRemovesOnlyTheRoomSave() throws Exception {
        var repository = new FileGameRepository(directory);
        repository.saveProfile(Profile.fresh());
        repository.saveCheckpoint(
                new GameRun(RunSetup.standard(1, DiverClass.MECHANIC)).checkpoint());
        repository.clearCheckpoint();
        assertTrue(repository.loadCheckpoint().isEmpty());
        assertEquals(Profile.fresh(), repository.loadProfile());
    }
}
