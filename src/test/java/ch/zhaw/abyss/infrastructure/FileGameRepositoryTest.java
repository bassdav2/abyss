package ch.zhaw.abyss.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Profile;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.GameRun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

class FileGameRepositoryTest {
    @TempDir Path directory;

    @Test
    void profileAndCheckpointSurviveNewRepositoryInstance() throws Exception {
        var repository = new FileGameRepository(directory);
        var profile =
                new Profile(
                        3,
                        1,
                        12,
                        1,
                        29,
                        Set.of(ActiveModule.PULSE, ActiveModule.ARC),
                        new Settings(.4, .2, true, false, true));
        var run = new GameRun(123456, ActiveModule.ARC, true);
        repository.saveProfile(profile);
        repository.saveCheckpoint(run.checkpoint());
        var reopened = new FileGameRepository(directory);
        assertEquals(profile, reopened.loadProfile());
        assertEquals(run.checkpoint(), reopened.loadCheckpoint().orElseThrow());
        assertDoesNotThrow(() -> GameRun.restore(reopened.loadCheckpoint().orElseThrow()));
        try (var files = Files.list(directory)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void unknownVersionFailsWithoutErasingOriginal() throws Exception {
        Path file = directory.resolve("profile.properties");
        String original = "version=999\nruns=100";
        Files.writeString(file, original);
        assertThrows(IOException.class, () -> new FileGameRepository(directory).loadProfile());
        assertEquals(original, Files.readString(file));
    }

    @Test
    void corruptedStateShowsRecoverableMessageAndTitleCanStillLoad() throws Exception {
        Files.writeString(
                directory.resolve("checkpoint.properties"), "version=1\nseed=not-a-number\n");
        var service = new GameService(new FileGameRepository(directory));
        assertTrue(service.saved().isEmpty());
        assertFalse(service.takeStorageMessage().isBlank());
        assertDoesNotThrow(() -> service.start(12, ActiveModule.PULSE, false));
    }

    @Test
    void missingFilesCreateFreshProfileAndStartingRunIsResumable() throws Exception {
        var service = new GameService(new FileGameRepository(directory));
        assertEquals(Profile.fresh(), service.profile());
        assertTrue(service.resume().isEmpty());
        service.start(88, ActiveModule.PULSE, false);
        var reopened = new GameService(new FileGameRepository(directory));
        assertEquals(1, reopened.profile().runs());
        assertEquals(88, reopened.resume().orElseThrow().seed());
    }

    @Test
    void lockedBlueprintCannotBeSelectedViaApplicationService() {
        var service = new GameService(new FileGameRepository(directory));
        assertThrows(
                IllegalArgumentException.class, () -> service.start(1, ActiveModule.ARC, false));
    }

    @Test
    void replacingUnreadableFilesKeepsExactOriginalBackups() throws Exception {
        String profile = "version=999\nruns=100";
        String checkpoint = "version=1\nseed=not-a-number\n";
        Files.writeString(directory.resolve("profile.properties"), profile);
        Files.writeString(directory.resolve("checkpoint.properties"), checkpoint);
        var service = new GameService(new FileGameRepository(directory));
        service.start(42, ActiveModule.PULSE, false);
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
}
