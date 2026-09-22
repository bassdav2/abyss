package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunCheckpoint;
import ch.zhaw.abyss.ports.GameRepository;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;

/** Anwendungsfälle koordinieren Domäne und Repository, aber enthalten keine UI-Widgets. */
public final class GameService {
    private final GameRepository repository;
    private Profile profile;
    private Optional<RunCheckpoint> saved = Optional.empty();
    private GameRun run;
    private int accountedKills;
    private boolean outcomeRecorded;
    private String storageMessage = "";

    public GameService(GameRepository repository) {
        this.repository = repository;
        try {
            profile = repository.loadProfile();
        } catch (IOException e) {
            profile = Profile.fresh();
            storageMessage = e.getMessage();
        }
        try {
            saved = repository.loadCheckpoint();
            if (saved.isPresent()) GameRun.restore(saved.get());
        } catch (IOException | IllegalArgumentException e) {
            saved = Optional.empty();
            storageMessage = e.getMessage();
        }
    }

    public GameRun start(long seed, ActiveModule module, boolean explorer) {
        if (!profile.unlocked().contains(module))
            throw new IllegalArgumentException("Bauplan noch nicht entdeckt");
        run = new GameRun(seed, module, explorer);
        accountedKills = 0;
        outcomeRecorded = false;
        profile =
                new Profile(
                        profile.runs() + 1,
                        profile.wins(),
                        profile.bestRoom(),
                        profile.bestCycle(),
                        profile.totalKills(),
                        profile.unlocked(),
                        new Settings(
                                profile.settings().masterVolume(),
                                profile.settings().musicVolume(),
                                profile.settings().reducedMotion(),
                                profile.settings().fullscreen(),
                                explorer));
        persistProfile();
        saveRoom();
        return run;
    }

    public Optional<GameRun> resume() {
        if (saved.isEmpty()) return Optional.empty();
        try {
            run = GameRun.restore(saved.get());
            accountedKills = run.kills();
            outcomeRecorded = false;
            return Optional.of(run);
        } catch (IllegalArgumentException e) {
            storageMessage = e.getMessage();
            return Optional.empty();
        }
    }

    public void saveRoom() {
        if (run == null) return;
        updateProgress(false);
        saved = Optional.of(run.checkpoint());
        try {
            repository.saveCheckpoint(run.checkpoint());
        } catch (IOException e) {
            storageMessage = "Raum konnte nicht gesichert werden: " + e.getMessage();
        }
    }

    public void recordOutcome() {
        if (run == null
                || outcomeRecorded
                || run.phase() != GameRun.Phase.DEFEAT && run.phase() != GameRun.Phase.VICTORY)
            return;
        outcomeRecorded = true;
        updateProgress(run.phase() == GameRun.Phase.VICTORY);
        saved = Optional.empty();
        try {
            repository.clearCheckpoint();
        } catch (IOException e) {
            storageMessage = "Alte Raum-Sicherung konnte nicht entfernt werden.";
        }
    }

    public boolean nextCycle() {
        if (run == null || !run.nextCycle()) return false;
        outcomeRecorded = false;
        saveRoom();
        return true;
    }

    private void updateProgress(boolean win) {
        int reached =
                run.phase() == GameRun.Phase.DEFEAT ? run.room().depth() : run.room().depth() + 1;
        var unlocked = EnumSet.copyOf(profile.unlocked());
        if (reached >= 6) unlocked.add(ActiveModule.ARC);
        if (reached >= 12) unlocked.add(ActiveModule.AEGIS);
        int newKills = Math.max(0, run.kills() - accountedKills);
        accountedKills = run.kills();
        profile =
                new Profile(
                        profile.runs(),
                        profile.wins() + (win ? 1 : 0),
                        Math.max(profile.bestRoom(), reached),
                        Math.max(profile.bestCycle(), run.cycle()),
                        profile.totalKills() + newKills,
                        unlocked,
                        profile.settings());
        persistProfile();
    }

    public void settings(Settings settings) {
        profile = profile.withSettings(settings);
        persistProfile();
    }

    private void persistProfile() {
        try {
            repository.saveProfile(profile);
        } catch (IOException e) {
            storageMessage = "Profil konnte nicht gespeichert werden: " + e.getMessage();
        }
    }

    public Profile profile() {
        return profile;
    }

    public Optional<RunCheckpoint> saved() {
        return saved;
    }

    public GameRun run() {
        return run;
    }

    public String takeStorageMessage() {
        var message = storageMessage;
        storageMessage = "";
        return message;
    }
}
