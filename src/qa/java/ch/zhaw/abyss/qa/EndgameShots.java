package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Enemy;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RunCheckpoint;
import ch.zhaw.abyss.domain.Threat;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.render.SpriteBank;
import ch.zhaw.abyss.ui.render.WorldRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Bildschirmfotos des Endgames ohne Fenster: Ein gespeicherter Raumeingang mit starkem Build wird
 * fortgesetzt, der Testspieler spielt, und sobald eine Mechanik sichtbar ist, entsteht ein Bild.
 * Zeigt Schwärme mit über tausend Gegnern, Bedrohungen, die neuen Bossmuster und die
 * Prismenkaiserin.
 */
public final class EndgameShots {
    private static final Settings SETTINGS = Settings.DEFAULT;
    private final PixelFont font = PixelFont.load();
    private final SpriteBank bank = new SpriteBank();
    private final Path out;

    private EndgameShots(Path out) {
        this.out = out;
    }

    /**
     * @param args Zielverzeichnis, Vorgabe {@code build/endgame}
     * @throws IOException bei Schreibfehlern
     */
    public static void main(String[] args) throws IOException {
        var out = Path.of(args.length > 0 ? args[0] : "build/endgame");
        Files.createDirectories(out);
        new EndgameShots(out).all();
    }

    private void all() throws IOException {
        shoot("swarm-cycle3", 2, 21, build(3), 60, run -> alive(run) >= 1100);
        for (var threat : new Threat[] {Threat.ARMORED, Threat.NESTS, Threat.SKY, Threat.BARRAGE}) {
            int depth = threatDepth(threat);
            if (depth >= 0)
                shoot(
                        "threat-" + threat.name().toLowerCase(),
                        1,
                        depth,
                        build(2),
                        14,
                        run -> run.roomTime() > 7);
        }
        shoot(
                "boss-reactor-meltdown",
                0,
                10,
                starter(),
                120,
                run -> pattern(run, 6, Enemy.State.WINDUP, 2));
        shoot(
                "boss-reactor-spiral",
                1,
                10,
                build(2),
                90,
                run -> pattern(run, 4, Enemy.State.STRIKE, 1.2));
        shoot("boss-brood-ink", 0, 16, starter(), 120, run -> run.ink() > .9 && run.roomTime() > 3);
        shoot(
                "boss-brood-school",
                1,
                16,
                build(2),
                120,
                run -> !run.lances().isEmpty() && run.roomTime() > 3);
        shoot("boss-captain-crossfire", 0, 23, starter(), 120, run -> !run.lances().isEmpty());
        shoot("empress-lances", 1, 23, build(2), 90, run -> run.lances().size() > 10);
        shoot(
                "empress-sundance",
                1,
                23,
                build(2),
                120,
                run ->
                        run.beams().stream().anyMatch(b -> b.live())
                                && pattern(run, 2, Enemy.State.STRIKE, 1.2));
        shoot(
                "empress-rainbow",
                1,
                23,
                build(2),
                120,
                run -> pattern(run, 4, Enemy.State.STRIKE, 1.6));
        shoot(
                "empress-bolts",
                1,
                23,
                build(2),
                120,
                run -> pattern(run, 0, Enemy.State.STRIKE, .6));
    }

    private static long alive(GameRun run) {
        return run.enemies().stream().filter(Enemy::alive).count();
    }

    /** Boss im gewünschten Muster und Zustand, seit mindestens {@code after} Sekunden darin. */
    private static boolean pattern(GameRun run, int pattern, Enemy.State state, double after) {
        var boss = run.boss();
        if (boss == null || boss.pattern() != pattern || boss.state() != state) {
            WAIT[0] = 0;
            return false;
        }
        WAIT[0] += 1 / 60.0;
        return WAIT[0] >= after;
    }

    private static final double[] WAIT = {0};

    private int threatDepth(Threat threat) {
        var generator = new RoomGenerator(SEED, 1, 0);
        for (int depth = 1; depth < 23; depth++) {
            if (RoomGenerator.fixed(depth)) continue;
            if (generator.room(depth, 0).threat() == threat) return depth;
        }
        return -1;
    }

    private static final long SEED = 4242;

    /** Schmaler Build für die Wächter des ersten Zyklus, damit alle Phasen zu sehen sind. */
    private static Map<Item, Integer> starter() {
        return Map.of(Item.PLATING, 3, Item.MEDICAL, 4, Item.NANITES, 2, Item.SHIELD_CELL, 2);
    }

    /** Starker Build passend zum Zyklus, damit der Testspieler lange genug überlebt. */
    private static Map<Item, Integer> build(int tier) {
        var items = new EnumMap<Item, Integer>(Item.class);
        int common = Math.min(8, 2 + tier * 2), rare = Math.min(5, 1 + tier * 2);
        for (var item :
                new Item[] {
                    Item.SERVO, Item.PLATING, Item.MEDICAL, Item.OVERCLOCK, Item.AREA, Item.LENS
                }) items.put(item, common);
        for (var item :
                new Item[] {
                    Item.NANITES, Item.ORBITAL, Item.TESLA_FIELD, Item.SHIELD_CELL, Item.BLADE_WAVE
                }) items.put(item, rare);
        if (tier >= 2) {
            items.put(Item.ORBITAL, 6);
            items.put(Item.STORM_BLADES, 1);
            items.put(Item.TESLA_FIELD, 5);
            items.put(Item.ARC_COIL, 2);
            items.put(Item.THUNDERHEAD, 1);
            items.put(Item.SECOND_HEART, 1);
        }
        if (tier >= 3) {
            items.put(Item.BLADE_WAVE, 5);
            items.put(Item.MULTISHOT, 3);
            items.put(Item.TEMPEST, 1);
            items.put(Item.LIMIT_BREAK, 2);
            items.put(Item.SERVO, 12);
        }
        return items;
    }

    private void shoot(
            String name,
            int cycle,
            int depth,
            Map<Item, Integer> items,
            double seconds,
            Predicate<GameRun> moment)
            throws IOException {
        var route = new ArrayList<Integer>();
        for (int i = 0; i <= depth; i++) route.add(0);
        int level = Math.min(Weapon.MAX_LEVEL, cycle * 3);
        double bonus = 200 + 300 * cycle;
        double health =
                ch.zhaw.abyss.domain.StatSheet.compute(
                                DiverClass.MECHANIC, Weapon.WRENCH, level, items, bonus + 50)
                        .maxHealth();
        var checkpoint =
                new RunCheckpoint(
                        SEED,
                        cycle,
                        0,
                        depth,
                        0,
                        DiverClass.MECHANIC,
                        Weapon.WRENCH,
                        level,
                        ActiveModule.PULSE,
                        true,
                        health,
                        50,
                        0,
                        2,
                        items,
                        0,
                        0,
                        route,
                        0,
                        false,
                        0,
                        0,
                        bonus,
                        20 + cycle * 30,
                        0,
                        0);
        var pool = EnumSet.noneOf(Item.class);
        pool.addAll(Item.lootable());
        var run = GameRun.restore(checkpoint, pool, EnumSet.allOf(Weapon.class));
        var renderer = new WorldRenderer(font, bank);
        WAIT[0] = 0;
        boolean hit = false;
        for (int frame = 0; frame < seconds * 60 && !hit; frame++) {
            if (run.phase() != GameRun.Phase.RUNNING) break;
            for (int s = 0; s < 2; s++) run.update(1.0 / 120, CampaignPilot.input(run));
            for (var event : run.drainEvents()) renderer.event(event, run, SETTINGS);
            renderer.update(1.0 / 60, run, SETTINGS);
            hit = frame > 30 && moment.test(run);
        }
        renderer.render(run, SETTINGS, true);
        SceneShot.save(renderer, out.resolve(name + ".png"));
        var boss = run.boss();
        System.out.printf(
                "SHOT %-24s %s alive %4d time %5.1fs %s%n",
                name,
                hit ? "OK  " : "TIME",
                alive(run),
                run.roomTime(),
                boss == null
                        ? ""
                        : boss.kind() + " pattern " + boss.pattern() + " " + boss.state());
        if (run.enemies().stream().anyMatch(e -> e.kind() == EnemyKind.EMPRESS && !e.alive()))
            System.out.println("  Kaiserin besiegt");
    }
}
