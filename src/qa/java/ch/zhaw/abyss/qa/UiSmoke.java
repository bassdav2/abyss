package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.*;
import ch.zhaw.abyss.domain.*;
import ch.zhaw.abyss.infrastructure.FileGameRepository;
import ch.zhaw.abyss.ui.GameWindow;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Komponenten-/Controller-Test innerhalb JavaFX. Kein Betriebssystem- oder Usability-Test. */
public final class UiSmoke extends Application {
    private Stage stage;
    private GameWindow window;
    private GameService service;
    private FileGameRepository repository;
    private final List<String> checks = new ArrayList<>();
    private Path output;

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("abyss.silent", "true");
        this.stage = stage;
        output = Path.of("docs/qa/ui-smoke.txt");
        Path save = Files.createTempDirectory(Path.of("build"), "ui-smoke-");
        repository = new FileGameRepository(save);
        service = new GameService(repository);
        window =
                new GameWindow(
                        stage, service, Map.of("capture", "qa-focus-suppression", "after", "9999"));
        window.show();
        later(
                .3,
                () -> {
                    button("NEUER TAUCHGANG").fire();
                    check(button("TAUCHGANG STARTEN  →") != null, "Titel -> Vorbereitung");
                    var seed = (TextField) stage.getScene().lookup(".text-field");
                    seed.setText("invalid");
                    button("TAUCHGANG STARTEN  →").fire();
                    check(service.run() == null, "Ungültiger Seed startet keinen Run");
                    seed.setText("8123");
                    button("TAUCHGANG STARTEN  →").fire();
                    check(service.run().seed() == 8123, "Seed-Eingabe und Start-Button");
                    key(KeyCode.I, true);
                    check(button("WEITERSPIELEN  →") != null, "I öffnet Build-Inventar");
                    check(
                            stage.getScene().getRoot().lookupAll(".card").size() == 12,
                            "Inventar enthält zwölf Item-Karten");
                    key(KeyCode.ESCAPE, true);
                    key(KeyCode.M, true);
                    check(
                            stage.getScene().getRoot().lookupAll(".card").size() == 18,
                            "Bootskarte enthält achtzehn Räume");
                    button("WEITERSPIELEN  →").fire();
                    key(KeyCode.D, true);
                    later(
                            .5,
                            () -> {
                                key(KeyCode.D, false);
                                check(
                                        service.run().player().x() > 230,
                                        "JavaFX-KeyEvent bewegt Spielfigur");
                                key(KeyCode.ESCAPE, true);
                                double pausedX = service.run().player().x();
                                double pausedTime = service.run().elapsed();
                                later(
                                        .35,
                                        () -> {
                                            check(
                                                    service.run().elapsed() == pausedTime
                                                            && service.run().player().x()
                                                                    == pausedX,
                                                    "Pause stoppt Simulation");
                                            button("STEUERUNG").fire();
                                            check(button("VERSTANDEN") != null, "Hilfe aus Pause");
                                            button("VERSTANDEN").fire();
                                            button("OPTIONEN").fire();
                                            for (var node :
                                                    stage.getScene().getRoot().lookupAll(".slider"))
                                                ((Slider) node).setValue(0);
                                            button("ÜBERNEHMEN").fire();
                                            check(
                                                    service.profile().settings().masterVolume()
                                                            == 0,
                                                    "Optionen gespeichert");
                                            button("WEITERSPIELEN  →").fire();
                                            later(
                                                    .3,
                                                    () -> {
                                                        check(
                                                                service.run().elapsed()
                                                                        > pausedTime,
                                                                "Weiter nach Pause");
                                                        installWorkshop();
                                                    });
                                        });
                            });
                });
    }

    private void installWorkshop() throws Exception {
        window.close();
        repository.saveCheckpoint(
                new RunCheckpoint(
                        73419,
                        0,
                        5,
                        0,
                        ActiveModule.PULSE,
                        false,
                        60,
                        80,
                        45,
                        Map.of(),
                        5,
                        30,
                        List.of(0, 0, 0, 0, 0, 0)));
        service = new GameService(repository);
        window =
                new GameWindow(
                        stage, service, Map.of("capture", "qa-focus-suppression", "after", "9999"));
        window.show();
        button("FORTSETZEN  →").fire();
        check(
                service.run().room().kind() == RoomPlan.Kind.WORKSHOP,
                "Fortsetzen rekonstruiert gespeicherten Raum");
        key(KeyCode.D, true);
        until(
                () -> service.run().player().x() >= 790,
                () -> {
                    key(KeyCode.D, false);
                    key(KeyCode.E, true);
                    check(
                            service.run().player().health() == 100,
                            "Werkstatt-Interaktion repariert");
                    check(
                            button("INSTALLIEREN · 15 SCHROTT") != null,
                            "Werkstatt öffnet Modulauswahl");
                    button("REPARATURSET KAUFEN · 20 SCHROTT").fire();
                    check(
                            service.run().player().repairKits() == 2,
                            "Werkstatt kauft ein mitnehmbares Reparaturset");
                    check(
                            service.run().player().salvage() == 31,
                            "Reparaturset kostet genau 20 Schrott");
                    button("INSTALLIEREN · 15 SCHROTT").fire();
                    check(
                            service.run().player().upgrades().size() == 1,
                            "Modulkarte verändert Build");
                    check(
                            service.run().player().salvage() == 16,
                            "Werkstatt zieht genau 15 Schrott ab (inkl. Raumlohn)");
                    key(KeyCode.D, true);
                    until(
                            () -> service.run().player().x() >= 1340,
                            () -> {
                                key(KeyCode.D, false);
                                key(KeyCode.E, true);
                                check(
                                        button("DIESEN WEG NEHMEN  →") != null,
                                        "Rechtes Schott öffnet Navigation");
                                button("DIESEN WEG NEHMEN  →").fire();
                                check(
                                        service.run().room().depth() == 6,
                                        "Routenkarte wechselt Raum");
                                check(
                                        repository.loadCheckpoint().orElseThrow().depth() == 6,
                                        "Raumwechsel persistiert Sicherung");
                                key(KeyCode.ESCAPE, true);
                                button("ZUM HAUPTMENÜ").fire();
                                button("FORTSETZEN  →").fire();
                                check(
                                        service.run().room().depth() == 6
                                                && service.run().player().upgrades().size() == 1
                                                && service.run().player().repairKits() == 2,
                                        "Hauptmenü -> Fortsetzen erhält Build");
                                key(KeyCode.ESCAPE, true);
                                stage.setWidth(960);
                                stage.setHeight(580);
                                later(
                                        .3,
                                        () -> {
                                            window.capture(
                                                    Path.of("docs/qa/screens/pause-minimum.png"));
                                            check(
                                                    stage.getScene()
                                                                    .getRoot()
                                                                    .getBoundsInLocal()
                                                                    .getWidth()
                                                            <= stage.getScene().getWidth() + 2,
                                                    "Layout bei minimaler Fensterbreite");
                                            Files.createDirectories(output.getParent());
                                            Files.writeString(
                                                    output, String.join("\n", checks) + "\nPASS\n");
                                            System.out.println(
                                                    "UI_SMOKE_PASS checks=" + checks.size());
                                            window.close();
                                            Platform.exit();
                                        });
                            });
                });
    }

    private Button button(String text) {
        stage.getScene().getRoot().applyCss();
        return stage.getScene().getRoot().lookupAll(".button").stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().equals(text))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Button missing: " + text));
    }

    private void key(KeyCode code, boolean down) {
        Event.fireEvent(
                stage.getScene(),
                new KeyEvent(
                        down ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED,
                        "",
                        code.getName(),
                        code,
                        false,
                        false,
                        false,
                        false));
    }

    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks.add("PASS " + message);
        System.out.println("PASS " + message);
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    private void later(double seconds, CheckedAction action) {
        var delay = new PauseTransition(Duration.seconds(seconds));
        delay.setOnFinished(
                event -> {
                    try {
                        action.run();
                    } catch (Throwable error) {
                        error.printStackTrace();
                        window.close();
                        Platform.exit();
                        System.exit(1);
                    }
                });
        delay.play();
    }

    private void until(BooleanSupplier condition, CheckedAction action) {
        poll(condition, action, System.nanoTime());
    }

    private void poll(BooleanSupplier condition, CheckedAction action, long start) {
        later(
                .02,
                () -> {
                    if (condition.getAsBoolean()) action.run();
                    else if (System.nanoTime() - start > 10_000_000_000L)
                        throw new AssertionError("UI movement timeout");
                    else poll(condition, action, start);
                });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
