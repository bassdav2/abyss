package ch.zhaw.abyss.infrastructure;

import ch.zhaw.abyss.application.Achievement;
import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.application.Profile;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.application.Unlock;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RunCheckpoint;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ports.GameRepository;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kleine versionierte Textdateien; kein Datenbank- oder Persistenzframework nötig. Liest die
 * Formate 1 bis 3, schreibt immer Format 3 und sichert unlesbare oder migrierte Originale vor dem
 * ersten Überschreiben als {@code .bak}.
 */
public final class FileGameRepository implements GameRepository {
    /** Aktuelle Formatversion. */
    public static final String VERSION = "3";

    private static final long MAX_FILE_BYTES = 64 * 1024;
    private final Path directory;
    private final Set<Path> preserveBeforeReplace = new HashSet<>();

    /**
     * @param directory Verzeichnis für Profil und Raum-Sicherung
     */
    public FileGameRepository(Path directory) {
        this.directory = directory;
    }

    /**
     * @return Speicherverzeichnis
     */
    public Path directory() {
        return directory;
    }

    /**
     * @return plattformübliches Verzeichnis oder die Systemeigenschaft {@code abyss.saveDir}
     */
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
            String version = p.getProperty("version");
            var unlocks = new HashSet<String>();
            int bestRoom = integer(p, "bestRoom", 0);
            int cores = integer(p, "cores", 0);
            if (!VERSION.equals(version)) {
                preserveBeforeReplace.add(path);
                int oldRooms = "1".equals(version) ? 12 : 18;
                if (bestRoom < 0 || bestRoom > oldRooms)
                    throw new IllegalArgumentException("Ungültiger alter Fortschritt");
                bestRoom =
                        (int) Math.round(bestRoom * (double) RoomGenerator.ROOM_COUNT / oldRooms);
                for (String key : p.getProperty("unlocked", "PULSE").split(",")) {
                    var module = ActiveModule.valueOf(key.trim());
                    if (!module.startsUnlocked()) unlocks.add(Unlock.of(module));
                }
                // Dankeschön für bisherigen Fortschritt: ein Kern pro zwei Tauchgänge, höchstens
                // 20.
                cores = Math.min(20, integer(p, "runs", 0) / 2);
            } else {
                for (String id : list(p, "unlocks"))
                    if (Unlock.find(id).isPresent()) unlocks.add(id);
            }
            var discovered = EnumSet.noneOf(Item.class);
            for (String name : list(p, "discovered")) {
                var item = Item.parse(name);
                if (item != null) discovered.add(item);
            }
            var achievements = EnumSet.noneOf(Achievement.class);
            for (String name : list(p, "achievements"))
                for (var achievement : Achievement.values())
                    if (achievement.name().equals(name)) achievements.add(achievement);
            var settings =
                    new Settings(
                            number(p, "volume", .65),
                            number(p, "music", .45),
                            flag(p, "reducedMotion", false),
                            flag(p, "fullscreen", false),
                            flag(p, "explorer", false),
                            number(p, "screenShake", .8),
                            flag(p, "retroFilter", true),
                            flag(p, "damageNumbers", true));
            var cosmetics =
                    new Cosmetics(
                            integer(p, "suit", 0),
                            integer(p, "helmet", 0),
                            integer(p, "visor", 0),
                            integer(p, "trim", 0));
            var loadout = Loadout.DEFAULT;
            if (p.getProperty("loadout.diver") != null)
                loadout =
                        new Loadout(
                                DiverClass.valueOf(p.getProperty("loadout.diver")),
                                Weapon.valueOf(p.getProperty("loadout.weapon")),
                                ActiveModule.valueOf(p.getProperty("loadout.module")),
                                integer(p, "loadout.pressure", 0),
                                flag(p, "loadout.explorer", false));
            return new Profile(
                    integer(p, "runs", 0),
                    integer(p, "wins", 0),
                    bestRoom,
                    integer(p, "bestCycle", 0),
                    integer(p, "kills", 0),
                    cores,
                    integer(p, "maxPressure", 0),
                    unlocks,
                    discovered,
                    achievements,
                    cosmetics,
                    loadout,
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
            if (!VERSION.equals(p.getProperty("version"))) {
                migrateCheckpoint(p);
                preserveBeforeReplace.add(path);
            }
            var items = new EnumMap<Item, Integer>(Item.class);
            for (Item item : Item.values()) {
                int count = integer(p, "item." + item.name(), 0);
                if (count != 0) items.put(item, count);
            }
            var route =
                    Arrays.stream(p.getProperty("route", "0").split(","))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .toList();
            return Optional.of(
                    new RunCheckpoint(
                            Long.parseLong(p.getProperty("seed")),
                            integer(p, "cycle", 0),
                            integer(p, "pressure", 0),
                            integer(p, "depth", 0),
                            integer(p, "branch", 0),
                            DiverClass.valueOf(p.getProperty("diver", "MECHANIC")),
                            Weapon.valueOf(p.getProperty("weapon", "WRENCH")),
                            integer(p, "weaponLevel", 0),
                            ActiveModule.valueOf(p.getProperty("module", "PULSE")),
                            flag(p, "explorer", false),
                            number(p, "health", 100),
                            number(p, "energy", 100),
                            integer(p, "salvage", 0),
                            integer(p, "repairKits", 1),
                            items,
                            integer(p, "kills", 0),
                            number(p, "elapsed", 0),
                            route,
                            integer(p, "cores", 0),
                            flag(p, "reviveUsed", false),
                            integer(p, "rushStacks", 0),
                            number(p, "healthPenalty", 0),
                            number(p, "bonusHealth", 0)));
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
        p.setProperty("cores", "" + profile.cores());
        p.setProperty("maxPressure", "" + profile.maxPressure());
        p.setProperty(
                "unlocks", profile.unlocks().stream().sorted().collect(Collectors.joining(",")));
        p.setProperty(
                "discovered",
                profile.discovered().stream()
                        .map(Enum::name)
                        .sorted()
                        .collect(Collectors.joining(",")));
        p.setProperty(
                "achievements",
                profile.achievements().stream()
                        .map(Enum::name)
                        .sorted()
                        .collect(Collectors.joining(",")));
        var c = profile.cosmetics();
        p.setProperty("suit", "" + c.suit());
        p.setProperty("helmet", "" + c.helmet());
        p.setProperty("visor", "" + c.visor());
        p.setProperty("trim", "" + c.trim());
        var l = profile.loadout();
        p.setProperty("loadout.diver", l.diver().name());
        p.setProperty("loadout.weapon", l.weapon().name());
        p.setProperty("loadout.module", l.module().name());
        p.setProperty("loadout.pressure", "" + l.pressure());
        p.setProperty("loadout.explorer", "" + l.explorer());
        var s = profile.settings();
        p.setProperty("volume", "" + s.masterVolume());
        p.setProperty("music", "" + s.musicVolume());
        p.setProperty("reducedMotion", "" + s.reducedMotion());
        p.setProperty("fullscreen", "" + s.fullscreen());
        p.setProperty("explorer", "" + s.explorer());
        p.setProperty("screenShake", "" + s.screenShake());
        p.setProperty("retroFilter", "" + s.retroFilter());
        p.setProperty("damageNumbers", "" + s.damageNumbers());
        write(directory.resolve("profile.properties"), p);
    }

    @Override
    public void saveCheckpoint(RunCheckpoint c) throws IOException {
        var p = new Properties();
        p.setProperty("seed", "" + c.seed());
        p.setProperty("cycle", "" + c.cycle());
        p.setProperty("pressure", "" + c.pressure());
        p.setProperty("depth", "" + c.depth());
        p.setProperty("branch", "" + c.branch());
        p.setProperty("diver", c.diver().name());
        p.setProperty("weapon", c.weapon().name());
        p.setProperty("weaponLevel", "" + c.weaponLevel());
        p.setProperty("module", c.module().name());
        p.setProperty("explorer", "" + c.explorer());
        p.setProperty("health", "" + c.health());
        p.setProperty("energy", "" + c.energy());
        p.setProperty("salvage", "" + c.salvage());
        p.setProperty("repairKits", "" + c.repairKits());
        p.setProperty("kills", "" + c.kills());
        p.setProperty("elapsed", "" + c.elapsed());
        p.setProperty("cores", "" + c.cores());
        p.setProperty("reviveUsed", "" + c.reviveUsed());
        p.setProperty("rushStacks", "" + c.rushStacks());
        p.setProperty("healthPenalty", "" + c.healthPenalty());
        p.setProperty("bonusHealth", "" + c.bonusHealth());
        p.setProperty(
                "route", c.route().stream().map(String::valueOf).collect(Collectors.joining(",")));
        c.items().forEach((item, count) -> p.setProperty("item." + item.name(), "" + count));
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
        String version = p.getProperty("version");
        if (!"1".equals(version) && !"2".equals(version) && !VERSION.equals(version))
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
        p.setProperty("version", VERSION);
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

    /**
     * Überträgt eine Raum-Sicherung der Formate 1 und 2 bestmöglich: gleiche Sektion, gültige
     * Abzweige, bekannte Module. Die Mechanikerin mit Rohrzange übernimmt den alten Build.
     */
    private static void migrateCheckpoint(Properties p) {
        boolean first = "1".equals(p.getProperty("version"));
        int[] legacy = {0, 1, 2, 5, 6, 7, 8, 11, 12, 13, 16, 17};
        int oldDepth = integer(p, "depth", 0);
        var oldRoute =
                Arrays.stream(p.getProperty("route", "0").split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .toList();
        if (oldRoute.size() != oldDepth + 1)
            throw new IllegalArgumentException("Ungültige alte Route");
        int depth = first ? legacy[oldDepth] : oldDepth;
        depth = Math.min(depth, 15);
        var route = new ArrayList<Integer>();
        for (int i = 0; i <= depth; i++) {
            int value = i < oldRoute.size() && !first ? oldRoute.get(i) : 0;
            route.add(RoomGenerator.fixed(i) ? 0 : Math.max(0, Math.min(1, value)));
        }
        p.setProperty("depth", "" + depth);
        p.setProperty("branch", "" + route.getLast());
        p.setProperty(
                "route", route.stream().map(String::valueOf).collect(Collectors.joining(",")));
        p.setProperty("diver", DiverClass.MECHANIC.name());
        p.setProperty("weapon", Weapon.WRENCH.name());
        for (String key : List.copyOf(p.stringPropertyNames())) {
            if (!key.startsWith("upgrade.")) continue;
            var item = Item.parse(key.substring("upgrade.".length()));
            if (item != null)
                p.setProperty(
                        "item." + item.name(), "" + Math.min(item.maxStacks(), integer(p, key, 0)));
            p.remove(key);
        }
        double health = number(p, "health", 100);
        p.setProperty("health", "" + Math.max(1, Math.min(health, 100)));
        p.setProperty("energy", "" + Math.min(number(p, "energy", 100), 100));
        p.setProperty("repairKits", "" + Math.min(3, integer(p, "repairKits", 1)));
    }

    private static List<String> list(Properties p, String key) {
        String value = p.getProperty(key, "");
        if (value.isBlank()) return List.of();
        return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static int integer(Properties p, String key, int fallback) {
        return Integer.parseInt(p.getProperty(key, "" + fallback).trim());
    }

    private static double number(Properties p, String key, double fallback) {
        return Double.parseDouble(p.getProperty(key, "" + fallback).trim());
    }

    private static boolean flag(Properties p, String key, boolean fallback) {
        return Boolean.parseBoolean(p.getProperty(key, "" + fallback).trim());
    }
}
