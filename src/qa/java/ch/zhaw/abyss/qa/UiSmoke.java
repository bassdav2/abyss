package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.RunCheckpoint;
import ch.zhaw.abyss.domain.StatSheet;
import ch.zhaw.abyss.domain.Threat;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.infrastructure.FileGameRepository;
import ch.zhaw.abyss.infrastructure.music.MusicEngine;
import ch.zhaw.abyss.ui.GameWindow;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JavaFX-Komponentenprüfung im echten Fenster mit temporärem Speicherort: alle Bildschirme öffnen,
 * einen Tauchgang starten, Räume sichern, Bergung, Händler, Route, Ausrüstung, Karte und Ergebnis
 * anzeigen. Mit {@code --capture=Verzeichnis} entsteht zu jedem Schritt ein Bildschirmfoto. Keine
 * Betriebssystem-Eingaben und kein menschlicher Spieltest.
 */
public final class UiSmoke extends Application {
    private record Step(String name, String expected, Action action, double delay) {}

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }

    private final List<String> results = new ArrayList<>();
    private final ArrayDeque<Step> steps = new ArrayDeque<>();
    private int failures;
    private GameWindow window;
    private GameService service;
    private Path capture;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        var directory = Files.createTempDirectory("abyss-ui-smoke");
        String target = getParameters().getNamed().get("capture");
        capture = target == null ? null : Path.of(target);
        service = new GameService(new FileGameRepository(directory));
        // Automatischer Lauf: kein Pausieren, wenn das Betriebssystem den Fokus wegnimmt. Das
        // dabei entstehende Startbild landet im temporären Ordner.
        window = new GameWindow(stage, service, auto(directory));
        window.show();
        plan();
        next();
    }

    private void plan() {
        add("title", "TITLE", () -> window.title());
        add("loadout", "LOADOUT", () -> window.loadout());
        add("wardrobe", "WARDROBE", () -> window.wardrobe());
        add("archive-divers", "ARCHIVE", () -> window.archive(0));
        add("archive-blueprints", "ARCHIVE", () -> window.archive(3));
        add("archive-codex", "ARCHIVE", () -> window.archive(6));
        add("archive-logbook", "ARCHIVE", () -> window.archive(7));
        add("archive-resonances", "ARCHIVE", () -> window.archive(8));
        add("settings", "SETTINGS", () -> window.settings(false));
        add("help", "HELP", () -> window.help(false));
        add(
                "intro",
                "PLAY",
                () -> {
                    window.startRun(Loadout.DEFAULT, 4242);
                    expect("Auftakt beim ersten Tauchgang", window.introPlaying());
                },
                3.5);
        add(
                "play-start",
                "PLAY",
                () -> {
                    window.skipIntro();
                    expect("Auftakt übersprungen", !window.introPlaying());
                });
        add("play-fight", "PLAY", () -> simulate(1.2));
        add(
                "reward",
                "REWARD",
                () -> {
                    clearRoom();
                    expect("Raum gesichert", run().phase() == GameRun.Phase.ROOM_CLEARED);
                    window.reward();
                });
        add(
                "inventory",
                "INVENTORY",
                () -> {
                    run().take(run().offers().getFirst());
                    window.inventory();
                });
        add("map", "MAP", () -> window.map());
        add("pause", "PAUSE", () -> window.pause());
        add("route", "ROUTE", () -> window.route());
        add(
                "merchant",
                "REWARD",
                () -> {
                    travelTo(RoomPlan.Kind.MERCHANT);
                    run().player().statuses();
                    window.reward();
                });
        add(
                "boss",
                "PLAY",
                () -> {
                    travelTo(RoomPlan.Kind.BOSS);
                    window.play();
                    simulate(3.2);
                });
        add(
                "shrine-or-workshop",
                "REWARD",
                () -> {
                    travelTo(RoomPlan.Kind.WORKSHOP);
                    run().repair();
                    window.reward();
                });
        add(
                "sector2",
                "PLAY",
                () -> {
                    travelTo(RoomPlan.Kind.COMBAT);
                    window.play();
                    simulate(2.5);
                });
        add(
                "level-up",
                "LEVEL_UP",
                () -> {
                    var run = run();
                    CampaignPilot.autoLevel = false;
                    for (int guard = 0; guard < 30 && run.pendingLevelUps() == 0; guard++) {
                        if (run.phase() == GameRun.Phase.RUNNING) clearRoom();
                        if (run.pendingLevelUps() > 0 || run.phase() != GameRun.Phase.ROOM_CLEARED)
                            break;
                        CampaignPilot.advance(run, 0);
                    }
                    CampaignPilot.autoLevel = true;
                    expect("Levelaufstieg offen", run.pendingLevelUps() > 0);
                    expect("Auswahl angeboten", !run.levelOffers().isEmpty());
                    window.levelUp();
                });
        add(
                "level-up-chosen",
                "PLAY",
                () -> {
                    var run = run();
                    for (int guard = 0; guard < 20 && run.pendingLevelUps() > 0; guard++)
                        window.chooseLevel(0);
                    expect("alle Levelaufstiege gewählt", run.pendingLevelUps() == 0);
                    window.play();
                });
        add(
                "route-condition",
                "ROUTE",
                () -> {
                    var run = run();
                    for (int guard = 0; guard < 30 && !conditionAhead(); guard++) {
                        if (run.phase() == GameRun.Phase.RUNNING) clearRoom();
                        if (run.phase() != GameRun.Phase.ROOM_CLEARED || conditionAhead()) break;
                        CampaignPilot.advance(run, 0);
                        service.saveRoom();
                    }
                    expect("Raumzustand in der Routenwahl", conditionAhead());
                    window.route();
                });
        add(
                "audio",
                "PLAY",
                () -> {
                    window.play();
                    int clips = MusicEngine.soundCount();
                    expect("Klänge geladen (" + clips + ")", clips == 29);
                    var music = MusicEngine.start();
                    music.volume(1e-4);
                    music.cue("boss");
                    long until = System.nanoTime() + 2_000_000_000L;
                    while (!music.ready() && System.nanoTime() < until) Thread.onSpinWait();
                    boolean device = javax.sound.sampled.AudioSystem.getMixerInfo().length > 0;
                    expect("Bordsynthesizer spielt", music.ready() || !device);
                    music.close();
                });
        add(
                "ending",
                "OUTCOME",
                () -> {
                    travelTo(RoomPlan.Kind.BRIDGE);
                    clearRoom();
                    expect("Brücke erobert", run().phase() == GameRun.Phase.VICTORY);
                    service.recordOutcome();
                    window.outcome();
                    expect("Siegesszene läuft", window.endingPlaying());
                },
                4.5);
        add(
                "victory",
                "OUTCOME",
                () -> {
                    window.skipEnding();
                    expect("Auswertung nach der Szene", !window.endingPlaying());
                });
        add(
                "outcome",
                "OUTCOME",
                () -> {
                    window.startRun(Loadout.DEFAULT, 77);
                    killPlayer();
                    window.outcome();
                    expect("Niederlage ohne Siegesszene", !window.endingPlaying());
                });
        add(
                "career",
                "CAREER",
                () -> {
                    window.career();
                    expect("Laufbahn-Erfahrung verbucht", service.profile().career().xp() > 0);
                });
        add("career-depth", "CAREER", () -> window.career(0));
        add("career-weapons", "CAREER", () -> window.career(8));
        add("title-again", "TITLE", () -> window.title());
        add(
                "endgame-swarm",
                "PLAY",
                () -> {
                    endgame(
                            1,
                            9,
                            Map.of(
                                    Item.SERVO,
                                    6,
                                    Item.PLATING,
                                    6,
                                    Item.ORBITAL,
                                    6,
                                    Item.AREA,
                                    4,
                                    Item.NANITES,
                                    4,
                                    Item.MEDICAL,
                                    6),
                            0);
                    expect("Eskalation im zweiten Zyklus", run().escalation() > 10);
                    simulate(7);
                    expect(
                            "Schwarm mit über hundert Gegnern",
                            run().enemies().stream().filter(e -> e.alive()).count() >= 100);
                });
        add(
                "route-threat",
                "ROUTE",
                () -> {
                    // Werkstätten sind sofort gesichert; davor liegt eine Wahl mit Bedrohung.
                    var generator = new ch.zhaw.abyss.domain.RoomGenerator(4242, 1, 0);
                    int workshop = 5;
                    for (int depth : new int[] {5, 11, 17})
                        if (generator.choices(depth + 1).stream()
                                .anyMatch(r -> r.threat() != Threat.NONE)) {
                            workshop = depth;
                            break;
                        }
                    endgame(1, workshop, Map.of(Item.SERVO, 4, Item.LENS, 6, Item.MEDICAL, 4), 0);
                    expect(
                            "Bedrohung in der Routenwahl",
                            run().nextRooms().stream().anyMatch(r -> r.threat() != Threat.NONE));
                    window.route();
                });
        add(
                "evolution",
                "LEVEL_UP",
                () -> {
                    endgame(
                            1,
                            2,
                            Map.of(Item.ORBITAL, 6, Item.AREA, 2, Item.SERVO, 4, Item.MEDICAL, 4),
                            1);
                    expect(
                            "Entfesselung obenauf",
                            !run().levelOffers().isEmpty()
                                    && run().levelOffers().getFirst().item() == Item.STORM_BLADES);
                    window.levelUp();
                });
        add(
                "evolution-taken",
                "PLAY",
                () -> {
                    window.chooseLevel(0);
                    expect(
                            "Klingensturm installiert",
                            run().player().stacks(Item.STORM_BLADES) == 1);
                    simulate(2);
                });
        add(
                "empress",
                "PLAY",
                () -> {
                    endgame(
                            1,
                            23,
                            Map.of(
                                    Item.SERVO,
                                    8,
                                    Item.PLATING,
                                    8,
                                    Item.MEDICAL,
                                    8,
                                    Item.NANITES,
                                    5,
                                    Item.SHIELD_CELL,
                                    5,
                                    Item.LENS,
                                    6),
                            0);
                    expect(
                            "Prismenkaiserin auf der Brücke",
                            run().boss() != null && run().boss().kind() == EnemyKind.EMPRESS);
                    simulate(9);
                },
                1.2);
        add("title-final", "TITLE", () -> window.title());
    }

    /**
     * Setzt einen vorbereiteten Raumeingang im Endgame in einem frischen Fenster fort. Der
     * Spielstand liegt in einem eigenen temporären Ordner.
     */
    private void endgame(int cycle, int depth, Map<Item, Integer> items, int pendingLevels)
            throws Exception {
        var directory = Files.createTempDirectory("abyss-ui-endgame");
        var repository = new FileGameRepository(directory);
        var route = new ArrayList<Integer>();
        for (int i = 0; i <= depth; i++) route.add(0);
        int level = Math.min(Weapon.MAX_LEVEL, 2 + cycle * 2);
        double bonus = 150 + 150 * cycle;
        double health =
                StatSheet.compute(DiverClass.MECHANIC, Weapon.WRENCH, level, items, bonus)
                        .maxHealth();
        repository.saveCheckpoint(
                new RunCheckpoint(
                        4242,
                        cycle,
                        0,
                        depth,
                        0,
                        DiverClass.MECHANIC,
                        Weapon.WRENCH,
                        level,
                        ActiveModule.PULSE,
                        false,
                        health,
                        60,
                        120,
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
                        20 + pendingLevels,
                        0,
                        pendingLevels));
        window.close();
        service = new GameService(repository);
        window = new GameWindow(stage, service, auto(directory));
        window.show();
        window.resume();
    }

    private static Map<String, String> auto(Path directory) {
        return Map.of("capture", directory.resolve("auto-capture.png").toString());
    }

    private void add(String name, String expected, Action action) {
        add(name, expected, action, .7);
    }

    private void add(String name, String expected, Action action, double delay) {
        steps.add(new Step(name, expected, action, delay));
    }

    private GameRun run() {
        return window.currentRun();
    }

    private void simulate(double seconds) {
        var run = run();
        for (int i = 0; i < seconds * 120 && run.phase() == GameRun.Phase.RUNNING; i++)
            run.update(1.0 / 120, CampaignPilot.input(run));
    }

    private void clearRoom() {
        var run = run();
        for (int i = 0; i < 120 * 90 && run.phase() == GameRun.Phase.RUNNING; i++)
            run.update(1.0 / 120, CampaignPilot.input(run));
    }

    private void travelTo(RoomPlan.Kind kind) {
        var run = run();
        for (int guard = 0; guard < 40; guard++) {
            if (run.phase() == GameRun.Phase.RUNNING) clearRoom();
            if (run.phase() != GameRun.Phase.ROOM_CLEARED) return;
            var choices = run.nextRooms();
            int branch = 0;
            for (int i = 0; i < choices.size(); i++) if (choices.get(i).kind() == kind) branch = i;
            boolean found = choices.get(branch).kind() == kind;
            CampaignPilot.advance(run, branch);
            service.saveRoom();
            if (found) return;
        }
    }

    private boolean conditionAhead() {
        var run = run();
        return run.phase() == GameRun.Phase.ROOM_CLEARED
                && run.nextRooms().stream()
                        .anyMatch(room -> room.condition() != RoomCondition.NONE);
    }

    private void killPlayer() {
        var run = run();
        for (int i = 0; i < 120 * 600 && run.phase() == GameRun.Phase.RUNNING; i++)
            run.update(1.0 / 120, ch.zhaw.abyss.domain.InputFrame.NONE);
        service.recordOutcome();
    }

    private void next() {
        var step = steps.poll();
        if (step == null) {
            finish();
            return;
        }
        try {
            step.action().run();
            boolean match = step.expected().equals(window.screenName());
            expect(
                    step.name()
                            + " → "
                            + step.expected()
                            + (match ? "" : " (war " + window.screenName() + ")"),
                    match);
        } catch (Exception | AssertionError error) {
            failures++;
            results.add("FEHLER " + step.name() + ": " + error);
            error.printStackTrace();
        }
        var delay = new PauseTransition(Duration.seconds(capture == null ? .05 : step.delay()));
        delay.setOnFinished(
                event -> {
                    if (capture != null) {
                        try {
                            window.capture(capture.resolve(step.name() + ".png"));
                        } catch (Exception error) {
                            results.add("FEHLER Bildschirmfoto " + step.name());
                        }
                    }
                    next();
                });
        delay.play();
    }

    private void finish() {
        results.forEach(System.out::println);
        System.out.println(
                "UI_SMOKE " + (results.size() - failures) + "/" + results.size() + " bestanden");
        window.close();
        Platform.exit();
        if (failures > 0) System.exit(1);
    }

    private void expect(String name, boolean condition) {
        results.add((condition ? "OK     " : "FEHLER ") + name);
        if (!condition) failures++;
    }

    /**
     * @param args JavaFX-Argumente, optional {@code --capture=Verzeichnis}
     */
    public static void main(String[] args) {
        launch(UiSmoke.class, args);
    }
}
