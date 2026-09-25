package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Reproduzierbare Route durch vier Sektionen mit je einem Wächter. Seed, Zyklus und Druckstufe
 * bestimmen Raumarten, Geometrie, Gegnerwellen und Kisten; der Weg nach vorn existiert immer.
 */
public final class RoomGenerator {
    /** Anzahl Sektionen des Bootes. */
    public static final int SECTORS = 4;

    /** Räume pro Sektion. */
    public static final int SECTOR_ROOMS = 6;

    /** Gesamtzahl der Raumpositionen vom Heck bis zur Brücke. */
    public static final int ROOM_COUNT = SECTORS * SECTOR_ROOMS;

    private static final double LOW = 470, HIGH = 335;
    private final long seed;
    private final int cycle, pressure;

    /**
     * @param seed Routen-Seed
     * @param cycle Zyklus ab 0
     * @param pressure Druckstufe ab 0
     */
    public RoomGenerator(long seed, int cycle, int pressure) {
        this.seed = seed;
        this.cycle = cycle;
        this.pressure = pressure;
    }

    /**
     * @param seed Routen-Seed
     * @param cycle Zyklus ab 0
     */
    public RoomGenerator(long seed, int cycle) {
        this(seed, cycle, 0);
    }

    /**
     * @param depth Raumposition
     * @return {@code true}, wenn es an dieser Position keine Wahl gibt
     */
    public static boolean fixed(int depth) {
        return depth == 0 || depth % SECTOR_ROOMS >= 4;
    }

    /**
     * @param depth Raumposition
     * @return {@code true} für Sektorwächter und Brücke
     */
    public static boolean bossDepth(int depth) {
        return depth == ROOM_COUNT - 1 || depth % SECTOR_ROOMS == 4 && depth / SECTOR_ROOMS < 3;
    }

    /**
     * @param depth Raumposition
     * @return {@code true} für Werkstätten
     */
    public static boolean workshopDepth(int depth) {
        return depth == ROOM_COUNT - 2 || depth % SECTOR_ROOMS == 5 && depth / SECTOR_ROOMS < 3;
    }

    /**
     * @param depth Raumposition
     * @return wählbare Räume, einer oder zwei
     */
    public List<RoomPlan> choices(int depth) {
        if (depth < 0 || depth >= ROOM_COUNT)
            throw new IllegalArgumentException("Ungültige Raumtiefe");
        var rooms = new ArrayList<RoomPlan>();
        for (int branch = 0; branch < (fixed(depth) ? 1 : 2); branch++)
            rooms.add(room(depth, branch));
        return List.copyOf(rooms);
    }

    /**
     * Erzeugt einen Raum deterministisch.
     *
     * @param depth Raumposition
     * @param branch Abzweig
     * @return Raumplan
     */
    public RoomPlan room(int depth, int branch) {
        if (depth < 0
                || depth >= ROOM_COUNT
                || branch < 0
                || branch > 1
                || branch == 1 && fixed(depth))
            throw new IllegalArgumentException("Ungültiger Raum");
        var random = random(depth, branch);
        int sector = depth / SECTOR_ROOMS;
        var kind = kind(depth, branch);
        var theme = theme(kind, sector, random);
        var layout = layout(kind, sector, random);
        var waves = waves(kind, depth, sector, layout, random);
        var hazards = new ArrayList<RoomPlan.HazardSlot>();
        if (sector > 0 && (kind == RoomPlan.Kind.COMBAT || kind == RoomPlan.Kind.ELITE)) {
            int count = layout.width() > 2000 ? 2 : 1;
            for (int i = 0; i < count; i++)
                hazards.add(
                        new RoomPlan.HazardSlot(
                                520
                                        + (layout.width() - 900)
                                                * (i + random.nextDouble() * .6)
                                                / count,
                                random.nextBoolean() ? Hazard.Kind.STEAM : Hazard.Kind.ELECTRIC));
        }
        var crates = crates(kind, layout, random);
        int salvage =
                switch (kind) {
                    case COMBAT -> 6 + 2 * sector;
                    case ELITE -> 14 + 3 * sector;
                    case BOSS, BRIDGE -> 30;
                    case CACHE -> 10;
                    default -> 0;
                };
        // Eigener Zufallsstrom, damit Raumzustände die übrigen Raumdaten nicht verschieben.
        var special = new Random(random(depth, branch).nextLong() ^ 0x5DEECE66DL);
        var condition = condition(kind, depth, sector, special);
        // Gelegentlich flieht eine Schmugglerdrohne mit Beute durch den Raum.
        var smuggler = new Random(random(depth, branch).nextLong() ^ 0x5A5A5A5AL);
        if (kind == RoomPlan.Kind.COMBAT
                && depth >= 2
                && condition != RoomCondition.BREACH
                && smuggler.nextInt(100) < 14
                && !waves.isEmpty()
                && waves.getFirst().size() < 6) {
            var first = new ArrayList<>(waves.getFirst());
            first.add(
                    new RoomPlan.Spawn(
                            EnemyKind.SMUGGLER,
                            Affix.NONE,
                            layout.width() * (.55 + smuggler.nextDouble() * .3),
                            GameRun.FLOOR - 190));
            var copy = new ArrayList<List<RoomPlan.Spawn>>(waves);
            copy.set(0, first);
            waves = copy;
        }
        switch (condition) {
            case BLACKOUT -> salvage = (int) Math.round(salvage * 1.6);
            case LIST -> salvage = (int) Math.round(salvage * 1.4);
            case ALARM -> {
                var reinforced = new ArrayList<List<RoomPlan.Spawn>>();
                for (var wave : waves) {
                    var spawns = new ArrayList<>(wave);
                    if (spawns.size() < 6)
                        spawns.add(
                                place(
                                        pick(sector, depth, special),
                                        Affix.NONE,
                                        spawns.size(),
                                        layout,
                                        special));
                    reinforced.add(spawns);
                }
                waves = reinforced;
            }
            case LEAK -> {
                for (int i = 0; i < 2; i++) {
                    hazards.add(
                            new RoomPlan.HazardSlot(
                                    420
                                            + (layout.width() - 840)
                                                    * (i + .25 + special.nextDouble() * .5)
                                                    / 2,
                                    Hazard.Kind.STEAM));
                    crates.add(
                            new RoomPlan.CrateSlot(
                                    300 + (layout.width() - 600) * (i + .5) / 2,
                                    GameRun.FLOOR,
                                    i == 0 ? SupplyCrate.Kind.REPAIR : SupplyCrate.Kind.ENERGY));
                }
            }
            default -> {}
        }
        return new RoomPlan(
                depth,
                branch,
                sector,
                random.nextInt(4),
                kind,
                theme,
                description(kind, sector),
                waves,
                layout,
                hazards,
                crates,
                salvage,
                condition,
                fixtures(kind, depth, sector, layout, hazards, random(depth, branch).nextLong()));
    }

    /**
     * Verteilt Raumtechnik passend zur Sektion. Ein eigener Zufallsstrom lässt die übrigen
     * Raumdaten unverändert.
     */
    private static List<RoomPlan.FixtureSlot> fixtures(
            RoomPlan.Kind kind,
            int depth,
            int sector,
            RoomLayout layout,
            List<RoomPlan.HazardSlot> hazards,
            long stream) {
        var result = new ArrayList<RoomPlan.FixtureSlot>();
        if (kind == RoomPlan.Kind.BOSS || kind == RoomPlan.Kind.BRIDGE) {
            // Bossarenen: feste Anlagen, die sich gegen den Wächter einsetzen lassen.
            double w = layout.width();
            switch (sector) {
                case 0 -> {
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.PRESS, w * .38, 1, -1));
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.PRESS, w * .66, 1, -2.2));
                }
                case 1 -> {
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.VENT_PAD, w * .38, 1, 0));
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.CONSOLE, w * .55, 1, 0));
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.VENT_PAD, w * .72, 1, 0));
                }
                case 2 -> {
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.VENT_PAD, w * .38, 1, 0));
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.CONSOLE, w * .52, 1, 0));
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.VENT_PAD, w * .66, 1, 0));
                }
                default -> {
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.CONSOLE, w * .36, 1, 0));
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.LASER, w * .5, 1, -1));
                    result.add(new RoomPlan.FixtureSlot(Fixture.Kind.LASER, w * .72, 1, -3));
                }
            }
            return result;
        }
        if (depth < 1 || kind != RoomPlan.Kind.COMBAT && kind != RoomPlan.Kind.ELITE) return result;
        var random = new Random(stream ^ 0xC0FFEE11L);
        Fixture.Kind[] pool;
        int[] weights;
        switch (sector) {
            case 0 -> {
                pool =
                        new Fixture.Kind[] {
                            Fixture.Kind.CONVEYOR, Fixture.Kind.CONSOLE, Fixture.Kind.PRESS
                        };
                weights = new int[] {3, 3, 1};
            }
            case 1 -> {
                pool =
                        new Fixture.Kind[] {
                            Fixture.Kind.VENT_PAD,
                            Fixture.Kind.FAN,
                            Fixture.Kind.PRESS,
                            Fixture.Kind.CONSOLE
                        };
                weights = new int[] {3, 2, 3, 2};
            }
            case 2 -> {
                pool =
                        new Fixture.Kind[] {
                            Fixture.Kind.VENT_PAD,
                            Fixture.Kind.CONSOLE,
                            Fixture.Kind.PRESS,
                            Fixture.Kind.CONVEYOR
                        };
                weights = new int[] {2, 3, 1, 1};
            }
            default -> {
                pool =
                        new Fixture.Kind[] {
                            Fixture.Kind.LASER,
                            Fixture.Kind.CONSOLE,
                            Fixture.Kind.FAN,
                            Fixture.Kind.VENT_PAD
                        };
                weights = new int[] {4, 2, 2, 1};
            }
        }
        int count = sector == 0 ? 1 + (random.nextInt(3) == 0 ? 1 : 0) : 1 + random.nextInt(3);
        for (int attempt = 0; attempt < 30 && result.size() < count; attempt++) {
            int total = 0;
            for (int w : weights) total += w;
            int roll = random.nextInt(total);
            var chosen = pool[0];
            for (int i = 0; i < pool.length; i++) {
                roll -= weights[i];
                if (roll < 0) {
                    chosen = pool[i];
                    break;
                }
            }
            final var candidate = chosen;
            if ((candidate == Fixture.Kind.CONSOLE || candidate == Fixture.Kind.FAN)
                    && result.stream().anyMatch(f -> f.kind() == candidate)) continue;
            double half = candidate.width() / 2;
            double min = Math.max(560, RoomLayout.WALL + half + 20);
            double max = layout.width() - Math.max(420, half + RoomLayout.WALL + 20);
            if (max <= min) continue;
            double x = min + random.nextDouble() * (max - min);
            boolean clear = true;
            for (var other : result)
                if (Math.abs(other.x() - x) < half + other.kind().width() / 2 + 60) clear = false;
            for (var hazard : hazards) if (Math.abs(hazard.x() - x) < half + 90) clear = false;
            if (candidate != Fixture.Kind.FAN
                    && candidate != Fixture.Kind.CONVEYOR
                    && Math.abs(layout.rewardX() - x) < half + 60) clear = false;
            if (!clear) continue;
            result.add(
                    new RoomPlan.FixtureSlot(
                            candidate, x, random.nextBoolean() ? 1 : -1, -random.nextDouble() * 3));
        }
        return result;
    }

    private RoomCondition condition(RoomPlan.Kind kind, int depth, int sector, Random random) {
        if (depth < 2 || kind != RoomPlan.Kind.COMBAT && kind != RoomPlan.Kind.ELITE)
            return RoomCondition.NONE;
        int chance = 18 + 6 * sector + 3 * pressure + Math.min(9, 3 * cycle);
        if (random.nextInt(100) >= chance) return RoomCondition.NONE;
        var options =
                kind == RoomPlan.Kind.ELITE
                        ? sector == 0
                                ? List.of(RoomCondition.BLACKOUT, RoomCondition.LEAK)
                                : List.of(
                                        RoomCondition.BLACKOUT,
                                        RoomCondition.LEAK,
                                        RoomCondition.LIST)
                        : sector == 0
                                ? List.of(
                                        RoomCondition.BLACKOUT,
                                        RoomCondition.ALARM,
                                        RoomCondition.LEAK)
                                : List.of(
                                        RoomCondition.BLACKOUT,
                                        RoomCondition.ALARM,
                                        RoomCondition.LEAK,
                                        RoomCondition.BREACH,
                                        RoomCondition.LIST);
        return options.get(random.nextInt(options.size()));
    }

    private Random random(int depth, int branch) {
        return new Random(
                seed
                        ^ 0x9E3779B97F4A7C15L * (depth + 1)
                        ^ branch * 8191L
                        ^ cycle * 31337L
                        ^ pressure * 7_777_777L);
    }

    private RoomPlan.Kind kind(int depth, int branch) {
        if (depth == ROOM_COUNT - 1) return RoomPlan.Kind.BRIDGE;
        if (bossDepth(depth)) return RoomPlan.Kind.BOSS;
        if (workshopDepth(depth)) return RoomPlan.Kind.WORKSHOP;
        if (branch == 0) return RoomPlan.Kind.COMBAT;
        int sector = depth / SECTOR_ROOMS;
        var specials =
                new ArrayList<>(
                        List.of(
                                RoomPlan.Kind.ELITE,
                                RoomPlan.Kind.MERCHANT,
                                RoomPlan.Kind.SHRINE,
                                RoomPlan.Kind.CACHE));
        Collections.shuffle(specials, new Random(seed ^ sector * 104_729L ^ cycle * 17L));
        if (sector == 0) {
            // Die erste Sektion hat nur drei Abzweige; der Schwarzmarkt soll früh erreichbar sein.
            specials.remove(RoomPlan.Kind.MERCHANT);
            specials.add(0, RoomPlan.Kind.MERCHANT);
            Collections.swap(specials, 0, 1 + (int) Math.floorMod(seed, 2L));
            return specials.get(depth % SECTOR_ROOMS - 1);
        }
        return specials.get(depth % SECTOR_ROOMS);
    }

    private static RoomTheme theme(RoomPlan.Kind kind, int sector, Random random) {
        return switch (kind) {
            case WORKSHOP -> RoomTheme.WORKSHOP;
            case MERCHANT -> RoomTheme.MARKET;
            case SHRINE -> RoomTheme.SHRINE;
            case CACHE -> RoomTheme.STORAGE;
            case BRIDGE -> RoomTheme.BRIDGE;
            case BOSS ->
                    switch (sector) {
                        case 0 -> RoomTheme.BULKHEAD;
                        case 1 -> RoomTheme.REACTOR;
                        default -> RoomTheme.HATCHERY;
                    };
            case COMBAT, ELITE -> {
                var themes = RoomTheme.combatThemes(sector);
                yield themes[random.nextInt(themes.length)];
            }
        };
    }

    private static RoomLayout layout(RoomPlan.Kind kind, int sector, Random random) {
        double width =
                switch (kind) {
                    case COMBAT -> 1600 + 400 * random.nextInt(3);
                    case ELITE -> 2400 + 400 * random.nextInt(2);
                    default -> 1600;
                };
        var platforms = new ArrayList<Platform>();
        switch (kind) {
            case COMBAT, ELITE -> {
                for (double start = 250; start < width - 700; start += 1250) {
                    double span = Math.min(1150, width - 250 - start);
                    if (span < 550) break;
                    addTemplate(platforms, random.nextInt(5), start, span);
                }
            }
            case BOSS, BRIDGE -> {
                double y = sector == 1 ? 440 : LOW;
                platforms.add(new Platform(150, y, 230));
                platforms.add(new Platform(1220, y, 230));
                if (sector == 2) platforms.add(new Platform(690, HIGH, 220));
            }
            case MERCHANT, SHRINE -> platforms.add(new Platform(1080, LOW, 300));
            default -> {}
        }
        double reward =
                switch (kind) {
                    case COMBAT, ELITE -> Math.min(width - 420, width * .62);
                    case BOSS, BRIDGE -> 800;
                    default -> 800;
                };
        return new RoomLayout(width, platforms, reward);
    }

    private static void addTemplate(List<Platform> out, int template, double x, double span) {
        switch (template) {
            case 1 -> out.add(new Platform(x + span * .35, LOW, span * .3));
            case 2 -> {
                out.add(new Platform(x + span * .15, LOW, span * .2));
                out.add(new Platform(x + span * .62, LOW, span * .2));
            }
            case 3 -> {
                out.add(new Platform(x + span * .22, LOW, span * .22));
                out.add(new Platform(x + span * .5, HIGH, span * .2));
            }
            case 4 -> {
                out.add(new Platform(x + span * .1, LOW, span * .17));
                out.add(new Platform(x + span * .4, HIGH + 25, span * .2));
                out.add(new Platform(x + span * .72, LOW, span * .17));
            }
            default -> {}
        }
    }

    private List<List<RoomPlan.Spawn>> waves(
            RoomPlan.Kind kind, int depth, int sector, RoomLayout layout, Random random) {
        var waves = new ArrayList<List<RoomPlan.Spawn>>();
        switch (kind) {
            case BOSS, BRIDGE ->
                    waves.add(
                            List.of(
                                    new RoomPlan.Spawn(
                                            boss(sector),
                                            Affix.NONE,
                                            1180,
                                            boss(sector) == EnemyKind.BROOD
                                                    ? GameRun.FLOOR - 200
                                                    : GameRun.FLOOR)));
            case COMBAT, ELITE -> {
                int count =
                        kind == RoomPlan.Kind.ELITE
                                ? 3
                                : depth < 2 ? 1 : sector == 0 && depth < 3 ? 1 : 2;
                for (int wave = 0; wave < count; wave++) {
                    boolean elite =
                            kind == RoomPlan.Kind.ELITE && wave == count - 1
                                    || sector >= 2 && wave == count - 1 && random.nextInt(4) == 0;
                    waves.add(wave(depth, sector, wave, layout, random, elite));
                }
            }
            default -> {}
        }
        return waves;
    }

    private List<RoomPlan.Spawn> wave(
            int depth, int sector, int wave, RoomLayout layout, Random random, boolean elite) {
        var spawns = new ArrayList<RoomPlan.Spawn>();
        if (depth == 0) {
            spawns.add(new RoomPlan.Spawn(EnemyKind.SCUTTLER, Affix.NONE, 980, GameRun.FLOOR));
            spawns.add(new RoomPlan.Spawn(EnemyKind.SCUTTLER, Affix.NONE, 1320, GameRun.FLOOR));
            return spawns;
        }
        double budget =
                2
                        + sector * 1.6
                        + (depth % SECTOR_ROOMS) * .5
                        + wave * .5
                        + Math.min(6, cycle * 1.2)
                        + pressure * .6;
        int eliteIndex = elite ? 0 : -1;
        while (budget >= 1 && spawns.size() < 6) {
            var kind = pick(sector, depth, random);
            if (kind.threat() > budget + .5 && spawns.size() > 0) {
                kind = EnemyKind.SCUTTLER;
            }
            budget -= kind.threat();
            var affix =
                    spawns.size() == eliteIndex
                            ? Affix.values()[1 + random.nextInt(Affix.values().length - 1)]
                            : Affix.NONE;
            spawns.add(place(kind, affix, spawns.size(), layout, random));
        }
        return spawns;
    }

    private static EnemyKind pick(int sector, int depth, Random random) {
        EnemyKind[] pool;
        int[] weights;
        switch (sector) {
            case 0 -> {
                pool =
                        new EnemyKind[] {
                            EnemyKind.SCUTTLER,
                            EnemyKind.DRONE,
                            EnemyKind.SENTINEL,
                            EnemyKind.BOMBER
                        };
                weights = new int[] {5, 3, depth >= 2 ? 2 : 0, depth >= 2 ? 2 : 0};
            }
            case 1 -> {
                pool =
                        new EnemyKind[] {
                            EnemyKind.SCUTTLER,
                            EnemyKind.DRONE,
                            EnemyKind.BOMBER,
                            EnemyKind.WELDER,
                            EnemyKind.TURRET,
                            EnemyKind.MINELAYER,
                            EnemyKind.SENTINEL
                        };
                weights = new int[] {3, 3, 2, 3, 2, 2, 2};
            }
            case 2 -> {
                pool =
                        new EnemyKind[] {
                            EnemyKind.JELLY,
                            EnemyKind.EEL,
                            EnemyKind.DRONE,
                            EnemyKind.SCUTTLER,
                            EnemyKind.SHIELDBEARER,
                            EnemyKind.TURRET,
                            EnemyKind.WELDER
                        };
                weights = new int[] {4, 3, 2, 2, 2, 1, 1};
            }
            default -> {
                pool =
                        new EnemyKind[] {
                            EnemyKind.ENFORCER,
                            EnemyKind.SEEKER,
                            EnemyKind.SENTINEL,
                            EnemyKind.SHIELDBEARER,
                            EnemyKind.DRONE,
                            EnemyKind.TURRET,
                            EnemyKind.MINELAYER,
                            EnemyKind.BOMBER,
                            EnemyKind.WELDER
                        };
                weights = new int[] {4, 4, 2, 2, 1, 2, 1, 2, 1};
            }
        }
        int total = 0;
        for (int weight : weights) total += weight;
        int roll = random.nextInt(total);
        for (int i = 0; i < pool.length; i++) {
            roll -= weights[i];
            if (roll < 0) return pool[i];
        }
        return pool[0];
    }

    private static RoomPlan.Spawn place(
            EnemyKind kind, Affix affix, int index, RoomLayout layout, Random random) {
        double min = 760, max = layout.width() - 160;
        double x = min + (max - min) * ((index * .37 + random.nextDouble() * .3) % 1);
        double y = GameRun.FLOOR;
        switch (kind) {
            case DRONE -> y = GameRun.FLOOR - 110 - random.nextInt(40);
            case SEEKER -> y = GameRun.FLOOR - 190 - random.nextInt(40);
            case JELLY -> y = GameRun.FLOOR - 110 - random.nextInt(90);
            case TURRET -> y = 330;
            default -> {
                if (kind != EnemyKind.EEL
                        && !layout.platforms().isEmpty()
                        && random.nextInt(3) == 0) {
                    var platform =
                            layout.platforms().get(random.nextInt(layout.platforms().size()));
                    if (platform.centerX() > 600) {
                        x = platform.centerX();
                        y = platform.y();
                    }
                }
            }
        }
        return new RoomPlan.Spawn(kind, affix, x, y);
    }

    private static List<RoomPlan.CrateSlot> crates(
            RoomPlan.Kind kind, RoomLayout layout, Random random) {
        var crates = new ArrayList<RoomPlan.CrateSlot>();
        int count =
                switch (kind) {
                    case COMBAT -> 1 + random.nextInt(2);
                    case ELITE -> 2 + random.nextInt(2);
                    case CACHE -> 3;
                    default -> 0;
                };
        for (int i = 0; i < count; i++) {
            double x = 340 + (layout.width() - 700) * (i + .2 + random.nextDouble() * .5) / count;
            double y = GameRun.FLOOR;
            if (!layout.platforms().isEmpty() && random.nextInt(3) == 0) {
                var platform = layout.platforms().get(random.nextInt(layout.platforms().size()));
                x = platform.x() + 40 + random.nextDouble() * Math.max(1, platform.width() - 80);
                y = platform.y();
            }
            var content =
                    kind == RoomPlan.Kind.CACHE && i == 0
                            ? SupplyCrate.Kind.REPAIR
                            : SupplyCrate.Kind.values()[random.nextInt(4)];
            crates.add(new RoomPlan.CrateSlot(x, y, content));
        }
        return crates;
    }

    /**
     * @param sector Sektorindex
     * @return Wächter des Sektors
     */
    public static EnemyKind boss(int sector) {
        return switch (sector) {
            case 0 -> EnemyKind.WARDEN;
            case 1 -> EnemyKind.REACTOR;
            case 2 -> EnemyKind.BROOD;
            default -> EnemyKind.CAPTAIN;
        };
    }

    private static String description(RoomPlan.Kind kind, int sector) {
        return switch (kind) {
            case COMBAT -> "Sichere den Raum. Berge ein Modul und zerschlage Vorratskisten.";
            case ELITE ->
                    "Drei Patrouillen und ein Elitegegner. Seltene Bergung und ein Datenkern.";
            case WORKSHOP -> "Kostenlose Reparatur, Modul, Reparatursets und Waffenverbesserung.";
            case CACHE -> "Sicherer Umweg: Vorräte, Reparaturset und Schrott.";
            case MERCHANT -> "Eine Händlerin im Dunkeln. Module gegen Schrott.";
            case SHRINE -> "Eine vergessene Kapelle. Macht verlangt ein Opfer.";
            case BOSS ->
                    switch (sector) {
                        case 0 ->
                                "Der Schottmeister: Weiche seinem Ansturm aus, dann nutze die"
                                        + " Pause.";
                        case 1 -> "Der Reaktorkern: Springe über Wellen, lies seine Strahlen.";
                        default -> "Die Brutmutter: Etwas Grosses schwimmt hinter dem Glas.";
                    };
            case BRIDGE -> "Die letzte Begegnung. Der Lotse erwartet dich.";
        };
    }
}
