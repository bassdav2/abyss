package ch.zhaw.abyss.ports;

import ch.zhaw.abyss.application.Profile;
import ch.zhaw.abyss.domain.RunCheckpoint;

import java.io.IOException;
import java.util.Optional;

/** Persistenzgrenze: Die Anwendungslogik kennt weder Pfade noch Dateiformate. */
public interface GameRepository {
    Profile loadProfile() throws IOException;

    Optional<RunCheckpoint> loadCheckpoint() throws IOException;

    void saveProfile(Profile profile) throws IOException;

    void saveCheckpoint(RunCheckpoint checkpoint) throws IOException;

    void clearCheckpoint() throws IOException;
}
