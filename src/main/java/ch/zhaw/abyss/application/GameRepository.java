package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.RunCheckpoint;

import java.io.IOException;
import java.util.Optional;

/**
 * Persistenzgrenze (Port): Die Anwendungslogik kennt weder Pfade noch Dateiformate. Das Interface
 * gehört zur Schicht, die es benutzt; {@code infrastructure.FileGameRepository} implementiert es
 * von aussen (Dependency Inversion). So hat {@code application} keine Abhängigkeit nach aussen.
 */
public interface GameRepository {
    Profile loadProfile() throws IOException;

    Optional<RunCheckpoint> loadCheckpoint() throws IOException;

    void saveProfile(Profile profile) throws IOException;

    void saveCheckpoint(RunCheckpoint checkpoint) throws IOException;

    void clearCheckpoint() throws IOException;
}
