package ch.zhaw.abyss.infrastructure;

import ch.zhaw.abyss.application.Profile;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunCheckpoint;
import ch.zhaw.abyss.domain.Upgrade;
import ch.zhaw.abyss.ports.GameRepository;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/** Kleine versionierte Textdateien; kein Datenbank-/Persistenzframework nötig. */
public final class FileGameRepository implements GameRepository {
    private static final long MAX_FILE_BYTES = 64 * 1024;
    private final Path directory;
    private final Set<Path> preserveBeforeReplace = new HashSet<>();

    public FileGameRepository(Path directory) {
        this.directory = directory;
    }

    public Path directory() {
        return directory;
    }

    public static Path defaultDirectory() {
        String override = System.getProperty("abyss.saveDir");
        if (override != null && !override.isBlank()) return Path.of(override);
        var home = Path.of(System.getProperty("user.home"));
        if (System.getProperty("os.name").toLowerCase().contains("mac"))
            return home.resolve("Library/Application Support/Abyss");
        return home.resolve(".abyss");
    }

    @Override
    public Profile loadProfile() throws IOException {
        var path = directory.resolve("profile.properties");
        if (!Files.exists(path)) return Profile.fresh();
        try {
            var p = read(path);
            if ("1".equals(p.getProperty("version"))) {
                preserveBeforeReplace.add(path);
                int old = integer(p, "bestRoom", 0);
                if (old < 0 || old > 12)
                    throw new IllegalArgumentException("Ungültiger alter Fortschritt");
                p.setProperty("bestRoom", "" + (old == 0 ? 0 : legacyDepth(old - 1) + 1));
            }
            var unlocked = EnumSet.of(ActiveModule.PULSE);
            for (String key : p.getProperty("unlocked", "PULSE").split(","))
                unlocked.add(ActiveModule.valueOf(key));
            var settings =
                    new Settings(
                            number(p, "volume", .65),
                            number(p, "music", .45),
                            Boolean.parseBoolean(p.getProperty("reducedMotion", "false")),
                            Boolean.parseBoolean(p.getProperty("fullscreen", "false")),
                            Boolean.parseBoolean(p.getProperty("explorer", "false")));
            return new Profile(
                    integer(p, "runs", 0),
                    integer(p, "wins", 0),
                    integer(p, "bestRoom", 0),
                    integer(p, "bestCycle", 0),
                    integer(p, "kills", 0),
                    unlocked,
                    settings);
        } catch (IllegalArgumentException | IOException e) {
            preserveBeforeReplace.add(path);
            throw new IOException(
                    "Profil nicht lesbar. Das Original wird vor einem neuen Save gesichert.", e);
        }
    }

    @Override
    public Optional<RunCheckpoint> loadCheckpoint() throws IOException {
        var path = directory.resolve("checkpoint.properties");
        if (!Files.exists(path)) return Optional.empty();
        try {
            var p = read(path);
            if ("1".equals(p.getProperty("version"))) {
                migrateCheckpoint(p);
                preserveBeforeReplace.add(path);
            }
            var upgrades = new EnumMap<Upgrade, Integer>(Upgrade.class);
            for (Upgrade upgrade : Upgrade.values()) {
                int count = integer(p, "upgrade." + upgrade.name(), 0);
                if (count != 0) upgrades.put(upgrade, count);
            }
            var route =
                    Arrays.stream(p.getProperty("route", "0").split(","))
                            .map(Integer::parseInt)
                            .toList();
            var checkpoint =
                    new RunCheckpoint(
                            Long.parseLong(p.getProperty("seed")),
                            integer(p, "cycle", 0),
                            integer(p, "depth", 0),
                            integer(p, "branch", 0),
                            ActiveModule.valueOf(p.getProperty("module", "PULSE")),
                            Boolean.parseBoolean(p.getProperty("explorer", "false")),
                            number(p, "health", 100),
                            number(p, "energy", 100),
                            integer(p, "salvage", 0),
                            upgrades,
                            integer(p, "kills", 0),
                            number(p, "elapsed", 0),
                            route,
                            integer(p, "repairKits", 1));
            GameRun.restore(checkpoint);
            return Optional.of(checkpoint);
        } catch (IllegalArgumentException | NullPointerException | IOException e) {
            preserveBeforeReplace.add(path);
            throw new IOException(
                    "Raum-Sicherung nicht lesbar. Das Original wird vor einem neuen Save"
                            + " gesichert.",
                    e);
        }
    }

    @Override
    public void saveProfile(Profile profile) throws IOException {
        var p = new Properties();
        p.setProperty("runs", "" + profile.runs());
        p.setProperty("wins", "" + profile.wins());
        p.setProperty("bestRoom", "" + profile.bestRoom());
        p.setProperty("bestCycle", "" + profile.bestCycle());
        p.setProperty("kills", "" + profile.totalKills());
        p.setProperty(
                "unlocked",
                profile.unlocked().stream()
                        .map(Enum::name)
                        .sorted()
                        .collect(Collectors.joining(",")));
        var s = profile.settings();
        p.setProperty("volume", "" + s.masterVolume());
        p.setProperty("music", "" + s.musicVolume());
        p.setProperty("reducedMotion", "" + s.reducedMotion());
        p.setProperty("fullscreen", "" + s.fullscreen());
        p.setProperty("explorer", "" + s.explorer());
        write(directory.resolve("profile.properties"), p);
    }

    @Override
    public void saveCheckpoint(RunCheckpoint c) throws IOException {
        var p = new Properties();
        p.setProperty("seed", "" + c.seed());
        p.setProperty("cycle", "" + c.cycle());
        p.setProperty("depth", "" + c.depth());
        p.setProperty("branch", "" + c.branch());
        p.setProperty("module", c.module().name());
        p.setProperty("explorer", "" + c.explorer());
        p.setProperty("health", "" + c.health());
        p.setProperty("energy", "" + c.energy());
        p.setProperty("salvage", "" + c.salvage());
        p.setProperty("repairKits", "" + c.repairKits());
        p.setProperty("kills", "" + c.kills());
        p.setProperty("elapsed", "" + c.elapsed());
        p.setProperty(
                "route", c.route().stream().map(String::valueOf).collect(Collectors.joining(",")));
        c.upgrades()
                .forEach(
                        (upgrade, count) -> p.setProperty("upgrade." + upgrade.name(), "" + count));
        write(directory.resolve("checkpoint.properties"), p);
    }

    @Override
    public void clearCheckpoint() throws IOException {
        Files.deleteIfExists(directory.resolve("checkpoint.properties"));
    }

    private Properties read(Path path) throws IOException {
        if (Files.size(path) > MAX_FILE_BYTES)
            throw new IOException("Spielstand ist unerwartet gross.");
        var p = new Properties();
        p.load(new StringReader(Files.readString(path, StandardCharsets.UTF_8)));
        if (!"1".equals(p.getProperty("version")) && !"2".equals(p.getProperty("version")))
            throw new IOException("Unbekannte Spielstand-Version.");
        return p;
    }

    private void write(Path target, Properties p) throws IOException {
        Files.createDirectories(directory);
        if (preserveBeforeReplace.contains(target) && Files.exists(target)) {
            Path backup =
                    Files.createTempFile(directory, target.getFileName() + ".recovered-", ".bak");
            try {
                Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException error) {
                Files.deleteIfExists(backup);
                throw error;
            }
            preserveBeforeReplace.remove(target);
        }
        p.setProperty("version", "2");
        var text = new StringWriter();
        p.store(text, "ABYSS local save");
        Path temporary = Files.createTempFile(directory, ".abyss-", ".tmp");
        try {
            Files.writeString(temporary, text.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static int legacyDepth(int depth) {
        int[] mapping = {0, 1, 2, 5, 6, 7, 8, 11, 12, 13, 16, 17};
        if (depth < 0 || depth >= mapping.length)
            throw new IllegalArgumentException("Ungültige alte Raumtiefe");
        return mapping[depth];
    }

    private static void migrateCheckpoint(Properties p) {
        int oldDepth = integer(p, "depth", 0), branch = integer(p, "branch", 0);
        var oldRoute =
                Arrays.stream(p.getProperty("route", "0").split(","))
                        .map(Integer::parseInt)
                        .toList();
        if (oldRoute.size() != oldDepth + 1 || oldRoute.getLast() != branch)
            throw new IllegalArgumentException("Ungültige alte Route");
        var route =
                new java.util.ArrayList<Integer>(
                        java.util.Collections.nCopies(legacyDepth(oldDepth) + 1, 0));
        for (int i = 0; i < oldRoute.size(); i++) {
            int value = oldRoute.get(i);
            if (value < 0 || value > 1 || value == 1 && (i == 0 || i == 3 || i == 7 || i == 11))
                throw new IllegalArgumentException("Ungültiger alter Abzweig");
            route.set(legacyDepth(i), value);
        }
        p.setProperty("depth", "" + legacyDepth(oldDepth));
        p.setProperty(
                "route", route.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    private static int integer(Properties p, String key, int fallback) {
        return Integer.parseInt(p.getProperty(key, "" + fallback));
    }

    private static double number(Properties p, String key, double fallback) {
        return Double.parseDouble(p.getProperty(key, "" + fallback));
    }
}
