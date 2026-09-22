package ch.zhaw.abyss.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.domain.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;

class SaveMigrationTest {
    @TempDir Path directory;

    @Test
    void oldTwelveRoomCheckpointMapsToSameSectionAndPreservesOriginal() throws Exception {
        String old =
                "version=1\n"
                        + "seed=12\n"
                        + "depth=7\n"
                        + "branch=0\n"
                        + "health=80\n"
                        + "energy=90\n"
                        + "salvage=25\n"
                        + "route=0,1,0,0,1,0,0,0\n"
                        + "upgrade.SERVO=2\n";
        Path file = directory.resolve("checkpoint.properties");
        Files.writeString(file, old);
        var repo = new FileGameRepository(directory);
        var c = repo.loadCheckpoint().orElseThrow();
        assertEquals(11, c.depth());
        assertEquals(12, c.route().size());
        assertEquals(2, c.upgrades().get(Upgrade.SERVO));
        assertEquals(RoomPlan.Kind.WORKSHOP, GameRun.restore(c).room().kind());
        repo.saveCheckpoint(c);
        assertTrue(Files.readString(file).contains("version=2"));
        try (var files = Files.list(directory)) {
            var backup = files.filter(p -> p.toString().endsWith(".bak")).findFirst().orElseThrow();
            assertEquals(old, Files.readString(backup));
        }
        assertEquals(c, new FileGameRepository(directory).loadCheckpoint().orElseThrow());
    }

    @Test
    void oldCompletedProfileMapsToEighteenRoomsWithoutLosingWins() throws Exception {
        Files.writeString(
                directory.resolve("profile.properties"),
                "version=1\nruns=3\nwins=1\nbestRoom=12\nunlocked=PULSE,ARC,AEGIS\n");
        var repo = new FileGameRepository(directory);
        var p = repo.loadProfile();
        assertEquals(18, p.bestRoom());
        assertEquals(1, p.wins());
        repo.saveProfile(p);
        assertEquals(p, new FileGameRepository(directory).loadProfile());
    }
}
