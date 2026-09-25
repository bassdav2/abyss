package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.infrastructure.FileGameRepository;
import ch.zhaw.abyss.ui.GameWindow;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public void start(Stage stage) throws Exception {
        var directory = Files.createTempDirectory("abyss-ui-smoke");
        String target = getParameters().getNamed().get("capture");
        capture = target == null ? null : Path.of(target);
        service = new GameService(new FileGameRepository(directory));
        window = new GameWindow(stage, service, Map.of());
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
                    int clips = 0;
                    for (String name :
                            new String[] {
                                "hit",
                                "hurt",
                                "swing",
                                "dash",
                                "jump",
                                "shot",
                                "down",
                                "click",
                                "upgrade",
                                "clear",
                                "victory",
                                "defeat",
                                "pulse",
                                "warning",
                                "ambience",
                                "music"
                            }) {
                        var clip =
                                new AudioClip(
                                        Objects.requireNonNull(
                                                        UiSmoke.class.getResource(
                                                                "/audio/" + name + ".wav"))
                                                .toExternalForm());
                        clip.setVolume(0);
                        clips++;
                    }
                    expect("Audioclips geladen (" + clips + ")", clips == 16);
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
        add("title-again", "TITLE", () -> window.title());
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
            expect(
                    step.name() + " → " + step.expected(),
                    step.expected().equals(window.screenName()));
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
