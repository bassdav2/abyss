package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Upgrade;
import ch.zhaw.abyss.infrastructure.AudioSystem;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;

/** Native Bildschirmsteuerung. Der Controller übersetzt nur Eingaben in Anwendungsfälle. */
public final class GameWindow implements AutoCloseable {
    private enum Screen {
        TITLE,
        LOADOUT,
        PLAY,
        PAUSE,
        ROUTE,
        REWARD,
        SETTINGS,
        ARCHIVE,
        HELP,
        INVENTORY,
        MAP,
        OUTCOME
    }

    private static final double STEP = 1.0 / 120;
    private final Stage stage;
    private final GameService service;
    private final AssetCatalog assets = new AssetCatalog();
    private final Canvas canvas = new Canvas(1600, 900);
    private final Pane overlay = new Pane();
    private final Pane world = new Pane(canvas, overlay);
    private final GameRenderer renderer = new GameRenderer(canvas, assets);
    private final InputController input = new InputController();
    private final AudioSystem audio = new AudioSystem();
    private final Map<String, String> arguments;
    private final Scene scene;
    private Screen screen = Screen.TITLE;
    private GameRun run;
    private AnimationTimer loop;
    private long previous;
    private double accumulator;
    private ActiveModule selected = ActiveModule.PULSE;
    private double lastDt;
    private int frames;
    private boolean modalFromGame;
    private double outcomeDelay;

    public GameWindow(Stage stage, GameService service, Map<String, String> arguments) {
        this.stage = stage;
        this.service = service;
        this.arguments = arguments;
        world.setMinSize(1600, 900);
        world.setPrefSize(1600, 900);
        world.setMaxSize(1600, 900);
        overlay.setPrefSize(1600, 900);
        overlay.setPickOnBounds(false);
        var group = new Group(world);
        var root = new StackPane(group);
        root.setStyle("-fx-background-color: #020a10;");
        var scale =
                Bindings.min(root.widthProperty().divide(1600), root.heightProperty().divide(900));
        world.scaleXProperty().bind(scale);
        world.scaleYProperty().bind(scale);
        scene = new Scene(root, 1280, 720, Color.web("#020a10"));
        var stylesheet = GameWindow.class.getResource("/ui.css");
        if (stylesheet != null) scene.getStylesheets().add(stylesheet.toExternalForm());
        root.setStyle(root.getStyle() + "-fx-font-family: '" + assets.textFamily() + "';");
        stage.setScene(scene);
        stage.setTitle("ABYSS · Vom Heck bis zur Brücke");
        stage.setMinWidth(960);
        stage.setMinHeight(580);
        stage.setFullScreenExitHint("");
        stage.focusedProperty()
                .addListener(
                        (observable, oldValue, focused) -> {
                            if (!focused
                                    && screen == Screen.PLAY
                                    && !arguments.containsKey("capture")) showPause();
                        });
        installInput();
        audio.settings(service.profile().settings());
    }

    public void show() {
        showTitle();
        stage.show();
        stage.setFullScreen(service.profile().settings().fullscreen());
        audio.start();
        loop =
                new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        if (previous == 0) previous = now;
                        double dt = Math.min(.1, (now - previous) / 1_000_000_000.0);
                        previous = now;
                        lastDt = dt;
                        frames++;
                        if (screen == Screen.PLAY && run != null) {
                            accumulator += dt;
                            while (accumulator >= STEP) {
                                run.update(STEP, input.frame(run.player().x()));
                                accumulator -= STEP;
                            }
                            for (var event : run.drainEvents()) {
                                renderer.event(event, service.profile().settings());
                                audio.event(event);
                            }
                            if (run.phase() == GameRun.Phase.DEFEAT
                                    || run.phase() == GameRun.Phase.VICTORY) {
                                service.recordOutcome();
                                outcomeDelay += dt;
                                if (outcomeDelay
                                        > (run.phase() == GameRun.Phase.VICTORY ? 1.5 : .8))
                                    showOutcome();
                            }
                        } else accumulator = 0;
                        audio.context(
                                run,
                                screen == Screen.TITLE
                                        || screen == Screen.LOADOUT
                                        || screen == Screen.ARCHIVE);
                        renderer.update(dt);
                        renderer.render(
                                run,
                                service.profile().settings(),
                                screen == Screen.TITLE
                                        || screen == Screen.LOADOUT
                                        || screen == Screen.ARCHIVE
                                        || run == null,
                                screen == Screen.TITLE || screen == Screen.PLAY);
                        String message = service.takeStorageMessage();
                        if (!message.isBlank()) renderer.toast(message);
                    }
                };
        loop.start();
        if (arguments.containsKey("start"))
            startRun(73419, selected, service.profile().settings().explorer());
        if (arguments.containsKey("resume")) resume();
        if (arguments.containsKey("qa-screen")) {
            switch (arguments.get("qa-screen")) {
                case "loadout" -> showLoadout();
                case "archive" -> showArchive();
                case "settings" -> showSettings(run != null);
                case "help" -> showHelp(run != null);
                case "inventory" -> {
                    if (run != null) showInventory();
                }
                case "map" -> {
                    if (run != null) showMap();
                }
                case "pause" -> {
                    if (run != null) showPause();
                }
                case "route" -> {
                    if (run != null && run.phase() == GameRun.Phase.ROOM_CLEARED) showRoute();
                }
                case "reward" -> {
                    if (run != null && run.phase() == GameRun.Phase.ROOM_CLEARED) showReward();
                }
                default -> {}
            }
        }
        if (arguments.containsKey("capture")) {
            var delay =
                    new PauseTransition(
                            Duration.seconds(
                                    Double.parseDouble(arguments.getOrDefault("after", "2"))));
            delay.setOnFinished(
                    event -> {
                        try {
                            capture(Path.of(arguments.get("capture")));
                        } catch (IOException error) {
                            error.printStackTrace();
                        }
                        if (arguments.containsKey("exit")) Platform.exit();
                    });
            delay.play();
        }
    }

    private void installInput() {
        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == KeyCode.F11) {
                        stage.setFullScreen(!stage.isFullScreen());
                        event.consume();
                        return;
                    }
                    if (event.getCode() == KeyCode.F12) {
                        try {
                            capture(
                                    Path.of(
                                            System.getProperty("user.home"),
                                            "Desktop",
                                            "Abyss-" + System.currentTimeMillis() + ".png"));
                        } catch (IOException e) {
                            renderer.toast("Screenshot konnte nicht gespeichert werden.");
                        }
                        event.consume();
                        return;
                    }
                    if (event.getCode() == KeyCode.ESCAPE) {
                        if (screen == Screen.PLAY) showPause();
                        else if (screen == Screen.PAUSE
                                || screen == Screen.ROUTE
                                || screen == Screen.REWARD
                                || screen == Screen.INVENTORY
                                || screen == Screen.MAP) play();
                        else if (screen == Screen.SETTINGS || screen == Screen.HELP) {
                            if (modalFromGame) showPause();
                            else showTitle();
                        } else if (screen != Screen.OUTCOME) showTitle();
                        event.consume();
                        return;
                    }
                    if (screen == Screen.PLAY) {
                        if (event.getCode() == KeyCode.I || event.getCode() == KeyCode.TAB)
                            showInventory();
                        else if (event.getCode() == KeyCode.M) showMap();
                        else if (event.getCode() == KeyCode.E) interact();
                        else input.down(event.getCode());
                        event.consume();
                    } else if (screen == Screen.ROUTE && event.getCode().isDigitKey()) {
                        int choice =
                                event.getText().equals("1")
                                        ? 0
                                        : event.getText().equals("2") ? 1 : -1;
                        if (choice >= 0) chooseRoute(choice);
                    } else if (screen == Screen.REWARD && event.getCode().isDigitKey()) {
                        try {
                            chooseReward(Integer.parseInt(event.getText()) - 1);
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (screen == Screen.OUTCOME
                            && event.getCode() == KeyCode.R
                            && run.phase() == GameRun.Phase.DEFEAT) {
                        startRun(
                                System.nanoTime(),
                                run.player().module(),
                                service.profile().settings().explorer());
                    }
                });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> input.up(event.getCode()));
        scene.addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                event -> {
                    if (screen != Screen.PLAY || event.getTarget() instanceof Button) return;
                    input.pointer(world.sceneToLocal(event.getSceneX(), event.getSceneY()).getX());
                    if (event.getButton() == MouseButton.PRIMARY) input.mouseAttack(true);
                    if (event.getButton() == MouseButton.SECONDARY) input.mouseAbility(true);
                });
        scene.addEventFilter(
                MouseEvent.MOUSE_MOVED,
                event -> {
                    if (screen == Screen.PLAY)
                        input.pointer(
                                world.sceneToLocal(event.getSceneX(), event.getSceneY()).getX());
                });
        scene.addEventFilter(
                MouseEvent.MOUSE_DRAGGED,
                event -> {
                    if (screen == Screen.PLAY)
                        input.pointer(
                                world.sceneToLocal(event.getSceneX(), event.getSceneY()).getX());
                });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> input.mouseAttack(false));
    }

    private void setScreen(Screen next, boolean dim) {
        screen = next;
        input.clear();
        overlay.getChildren().clear();
        scene.setCursor(next == Screen.PLAY ? Cursor.CROSSHAIR : Cursor.DEFAULT);
        if (dim) {
            var shade = new Pane();
            shade.setPrefSize(1600, 900);
            shade.setStyle("-fx-background-color: rgba(2,12,20,0.88);");
            overlay.getChildren().add(shade);
        }
    }

    private void showTitle() {
        setScreen(Screen.TITLE, false);
        double x = 72;
        if (service.saved().isPresent()) {
            button("FORTSETZEN  →", x, 752, 290, 65, "primary", this::resume);
            x += 310;
        }
        button(
                "NEUER TAUCHGANG",
                x,
                752,
                300,
                65,
                service.saved().isEmpty() ? "primary" : "secondary",
                this::showLoadout);
        x += 320;
        button("BAUPLÄNE", x, 752, 190, 65, "secondary", this::showArchive);
        x += 210;
        button("OPTIONEN", x, 752, 190, 65, "secondary", () -> showSettings(false));
        x += 210;
        button("STEUERUNG", x, 752, 190, 65, "quiet", () -> showHelp(false));
        button("BEENDEN", 1388, 680, 140, 38, "quiet", Platform::exit);
    }

    private void showLoadout() {
        setScreen(Screen.LOADOUT, true);
        heading(
                "VORBEREITUNG",
                "Jeder Tauchgang beginnt im Heck.",
                "Wähle ein Startmodul. Gefundene Baupläne bleiben für spätere Versuche erhalten.");
        int i = 0;
        for (ActiveModule module : ActiveModule.values()) {
            double x = 130 + i * 450;
            boolean unlocked = service.profile().unlocked().contains(module);
            pane(x, 285, 430, 280, "card" + (selected == module ? " selected" : ""));
            label("0" + (i + 1) + "  /  STARTMODUL", x + 28, 311, 375, 25, 17, "eyebrow");
            label(module.title(), x + 28, 355, 375, 50, 37, "title");
            label(module.description(), x + 28, 417, 375, 70, 23, "muted");
            button(
                            unlocked
                                    ? (selected == module ? "AUSGEWÄHLT" : "AUSWÄHLEN")
                                    : "BAUPLAN FEHLT",
                            x + 28,
                            499,
                            375,
                            44,
                            unlocked && selected == module ? "primary" : "secondary",
                            () -> {
                                selected = module;
                                showLoadout();
                            })
                    .setDisable(!unlocked);
            i++;
        }
        var explorer = new CheckBox("Entdecker · mehr Integrität und etwas mehr Schaden");
        explorer.setSelected(service.profile().settings().explorer());
        place(explorer, 130, 603, 780, 40);
        var seed = new TextField();
        seed.setPromptText("Seed (optional, ganze Zahl)");
        place(seed, 1050, 603, 410, 44);
        label(
                "Die Route wird pro Run neu kombiniert. Gesichert wird automatisch beim"
                        + " Raumwechsel.",
                130,
                657,
                1270,
                35,
                22,
                "muted");
        button(
                "TAUCHGANG STARTEN  →",
                1010,
                744,
                450,
                67,
                "primary",
                () -> {
                    try {
                        long value =
                                seed.getText().isBlank()
                                        ? System.nanoTime()
                                        : Long.parseLong(seed.getText().trim());
                        startRun(value, selected, explorer.isSelected());
                    } catch (NumberFormatException error) {
                        seed.setStyle("-fx-border-color: #f0886b;");
                        seed.setPromptText("Bitte eine ganze Zahl eingeben");
                    }
                });
        button("← ZURÜCK", 130, 744, 220, 67, "quiet", this::showTitle);
    }

    private void startRun(long seed, ActiveModule module, boolean explorer) {
        outcomeDelay = 0;
        run = service.start(seed, module, explorer);
        renderer.clearEffects();
        play();
        renderer.toast("Finde deinen Weg zur Brücke. Das rechte Schott führt weiter.");
    }

    private void resume() {
        outcomeDelay = 0;
        service.resume()
                .ifPresent(
                        value -> {
                            run = value;
                            renderer.clearEffects();
                            play();
                            renderer.toast("Raum-Sicherung geladen");
                        });
    }

    private void play() {
        setScreen(Screen.PLAY, false);
        button("I", 1397, 126, 46, 41, "quiet", this::showInventory).setFocusTraversable(false);
        button("M", 1456, 126, 46, 41, "quiet", this::showMap).setFocusTraversable(false);
        button("Ⅱ", 1515, 126, 46, 41, "quiet", this::showPause).setFocusTraversable(false);
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
    }

    private void interact() {
        if (run.phase() != GameRun.Phase.ROOM_CLEARED) {
            renderer.toast("Sichere zuerst den Raum.");
            return;
        }
        if (run.player().x() > 1300) {
            showRoute();
            return;
        }
        if (Math.abs(run.player().x() - 800) < 165) {
            if (run.room().kind() == RoomPlan.Kind.WORKSHOP) {
                run.repair();
                showReward();
            } else if (run.rewardAvailable()) showReward();
            else renderer.toast("Alles geborgen. Weiter zum rechten Schott.");
        } else renderer.toast("E am Modulbehälter oder am rechten Schott.");
    }

    private void showRoute() {
        setScreen(Screen.ROUTE, true);
        heading(
                "NAVIGATION",
                "Dein Weg nach vorn.",
                "Das Schott schliesst sich hinter dir. Wähle, welches Risiko du als Nächstes"
                        + " eingehst.");
        drawRouteLine();
        var choices = run.nextRooms();
        for (int i = 0; i < choices.size(); i++) {
            var room = choices.get(i);
            final int index = i;
            double x = choices.size() == 1 ? 480 : 155 + i * 670;
            pane(x, 353, 620, 330, "card");
            var picture = new ImageView(assets.room(room));
            picture.setViewport(new Rectangle2D(0, 220, picture.getImage().getWidth(), 310));
            picture.setFitWidth(618);
            picture.setFitHeight(111);
            picture.setLayoutX(x + 1);
            picture.setLayoutY(354);
            overlay.getChildren().add(picture);
            label(
                    "0" + (i + 1) + "   /   " + room.typeName().toUpperCase(),
                    x + 30,
                    475,
                    560,
                    30,
                    18,
                    "eyebrow");
            label(room.title(), x + 30, 507, 560, 48, 36, "title");
            label(room.description(), x + 30, 554, 540, 60, 21, "muted");
            button(
                    "DIESEN WEG NEHMEN  →",
                    x + 30,
                    625,
                    560,
                    44,
                    "primary",
                    () -> chooseRoute(index));
        }
        label(
                "RAUM "
                        + String.format("%02d", run.room().depth() + 2)
                        + " / 18   ·   SEED "
                        + run.seed(),
                155,
                705,
                1250,
                30,
                18,
                "muted");
        button("← NOCH IM RAUM BLEIBEN", 155, 765, 365, 55, "quiet", this::play);
    }

    private void drawRouteLine() {
        for (int i = 0; i < RoomGenerator.ROOM_COUNT; i++) {
            double x = 171 + i * 74;
            if (i < RoomGenerator.ROOM_COUNT - 1) {
                var line = new Line(x, 290, x + 74, 290);
                line.setStroke(Color.web(i < run.room().depth() ? "#eaba72" : "#29424d"));
                line.setStrokeWidth(2);
                overlay.getChildren().add(line);
            }
            var node =
                    new Circle(
                            x,
                            290,
                            i == run.room().depth() ? 10 : RoomGenerator.bossDepth(i) ? 8 : 5,
                            Color.web(i <= run.room().depth() ? "#eaba72" : "#284550"));
            overlay.getChildren().add(node);
            label(
                    RoomGenerator.bossDepth(i) ? "◆" : "" + (i + 1),
                    x - 14,
                    307,
                    35,
                    24,
                    16,
                    RoomGenerator.bossDepth(i) ? "accent" : "muted");
        }
    }

    private void showInventory() {
        setScreen(Screen.INVENTORY, true);
        heading(
                "AUSRÜSTUNG",
                "Dein Build. Deine Spielweise.",
                "Jedes Fundstück bleibt für diesen Tauchgang und weitere Zyklen. Maximal drei"
                        + " Stufen je Modul.");
        var p = run.player();
        label(
                String.format(
                        java.util.Locale.ROOT,
                        "WERKZEUG %.0f    /    REICHWEITE %.0f    /    MODUL %.0f    /   "
                                + " INTEGRITÄT %.0f",
                        p.attackDamage(),
                        p.attackReach(),
                        p.abilityDamage(),
                        p.maxHealth()),
                130,
                745,
                1320,
                30,
                20,
                "accent");
        int i = 0;
        for (Upgrade u : Upgrade.values()) {
            int level = p.stacks(u);
            double x = 130 + (i % 4) * 340, y = 291 + (i / 4) * 153;
            Pane card = pane(x, y, 322, 137, "card" + (level > 0 ? " selected" : ""));
            card.setOpacity(level > 0 ? 1 : .54);
            var icon = ItemGlyph.node(u, 43);
            place(icon, x + 16, y + 17, 43, 43);
            label(u.title(), x + 73, y + 14, 235, 32, 23, "title");
            label(
                    level == 0 ? "NICHT GEFUNDEN" : "STUFE " + level + " / 3",
                    x + 73,
                    y + 47,
                    230,
                    22,
                    13,
                    level == 0 ? "muted" : "eyebrow");
            label(u.effect(), x + 16, y + 78, 290, 49, 17, "muted").setAlignment(Pos.TOP_LEFT);
            i++;
        }
        button("WEITERSPIELEN  →", 1050, 789, 380, 58, "primary", this::play);
        label(
                "DIE WERTE AUF DEN KARTEN GELTEN PRO STUFE.  ·  I / TAB ÖFFNET DAS INVENTAR.",
                130,
                801,
                880,
                30,
                15,
                "muted");
    }

    private void showMap() {
        setScreen(Screen.MAP, true);
        heading(
                "BOOTSKARTE",
                "Achtzehn Schotts. Ein Ziel.",
                "Grundroute durch drei Sektionen. Alternative Abzweige wählst du am Schott. Das"
                        + " Spiel ist pausiert.");
        var generator = new RoomGenerator(run.seed(), run.cycle());
        for (int sector = 0; sector < 3; sector++) {
            double y = 326 + sector * 158;
            label(
                    switch (sector) {
                        case 0 -> "01 / HECKSEKTION";
                        case 1 -> "02 / MASCHINENDECK";
                        default -> "03 / KOMMANDODECK";
                    },
                    130,
                    y - 32,
                    800,
                    28,
                    17,
                    "eyebrow");
            for (int n = 0; n < 6; n++) {
                int depth = sector * 6 + n;
                int branch = depth < run.route().size() ? run.route().get(depth) : 0;
                var room = generator.room(depth, branch);
                double x = 130 + n * 224;
                Pane card =
                        pane(
                                x,
                                y,
                                211,
                                111,
                                "card" + (depth == run.room().depth() ? " selected" : ""));
                card.setOpacity(depth > run.room().depth() ? .62 : 1);
                label(
                        String.format("%02d", depth + 1)
                                + " / "
                                + (depth < run.room().depth()
                                        ? "PASSIERT"
                                        : depth == run.room().depth()
                                                ? "DU BIST HIER"
                                                : RoomGenerator.bossDepth(depth)
                                                        ? "BOSS"
                                                        : "SIGNAL"),
                        x + 12,
                        y + 10,
                        191,
                        21,
                        12,
                        RoomGenerator.bossDepth(depth) ? "accent" : "eyebrow");
                label(room.title(), x + 12, y + 37, 187, 43, 23, "title");
                label(room.typeName(), x + 12, y + 84, 187, 19, 13, "muted");
            }
        }
        label(
                "ZYKLUS "
                        + (run.cycle() + 1L)
                        + "    /    SEED "
                        + run.seed()
                        + "    /    SPIEL PAUSIERT",
                130,
                797,
                870,
                32,
                17,
                "muted");
        button("WEITERSPIELEN  →", 1050, 789, 380, 58, "primary", this::play);
    }

    private void chooseRoute(int index) {
        if (run.chooseNextRoom(index)) {
            service.saveRoom();
            renderer.clearEffects();
            play();
        }
    }

    private void showReward() {
        if (run.rewardOffers().isEmpty() && run.room().kind() != RoomPlan.Kind.WORKSHOP) {
            run.claimSupplies();
            play();
            return;
        }
        setScreen(Screen.REWARD, true);
        boolean workshop = run.room().kind() == RoomPlan.Kind.WORKSHOP;
        heading(
                workshop ? "WERKSTATT" : "BERGUNG",
                workshop ? "Ein Moment zum Durchatmen." : "Mach diesen Run zu deinem.",
                workshop
                        ? "Reparatur abgeschlossen. Ein Modul kostet 15 Schrott. Verfügbar: "
                                + run.player().salvage()
                        : run.room().rewardRanks() == 2
                                ? "Seltene Bergung: Das gewählte Modul erhält zwei Stufen (bis"
                                        + " Stufe 3)."
                                : "Wähle ein Modul. Die anderen Teile bleiben zurück.");
        int i = 0;
        for (var upgrade : run.rewardOffers()) {
            final int index = i;
            double x = 130 + i * 450;
            pane(x, 315, 430, 375, "card");
            var glyph = ItemGlyph.node(upgrade, 62);
            place(glyph, x + 338, 337, 62, 62);
            label(
                    "0"
                            + (i + 1)
                            + "   /   STUFE "
                            + run.player().stacks(upgrade)
                            + " → "
                            + Math.min(3, run.player().stacks(upgrade) + run.room().rewardRanks()),
                    x + 28,
                    342,
                    292,
                    27,
                    17,
                    "eyebrow");
            label(upgrade.title(), x + 28, 407, 375, 50, 34, "title");
            label(upgrade.effect(), x + 28, 465, 375, 79, 24, "accent").setAlignment(Pos.TOP_LEFT);
            label(upgrade.description(), x + 28, 547, 375, 60, 20, "muted");
            button(
                            workshop ? "INSTALLIEREN · 15 SCHROTT" : "INSTALLIEREN",
                            x + 28,
                            623,
                            375,
                            42,
                            "primary",
                            () -> chooseReward(index))
                    .setDisable(!run.rewardAvailable() || workshop && run.player().salvage() < 15);
            i++;
        }
        if (workshop) {
            button(
                            "REPARATURSET KAUFEN · 20 SCHROTT",
                            535,
                            744,
                            560,
                            60,
                            "secondary",
                            () -> {
                                if (run.buyRepairKit()) showReward();
                            })
                    .setDisable(run.player().salvage() < 20 || run.player().repairKits() >= 3);
            label(
                    "SETS " + run.player().repairKits() + " / 3   ·   Q: +35 INTEGRITÄT",
                    535,
                    813,
                    570,
                    30,
                    16,
                    "muted");
        }
        button("SPÄTER ENTSCHEIDEN", 130, 744, 330, 60, "quiet", this::play);
    }

    private void chooseReward(int index) {
        if (index >= 0
                && index < run.rewardOffers().size()
                && run.claimReward(run.rewardOffers().get(index))) play();
    }

    private void showPause() {
        setScreen(Screen.PAUSE, true);
        heading("PAUSE", "Das Boot wartet.", "Dein aktueller Raum ist angehalten.");
        button("WEITERSPIELEN  →", 500, 300, 600, 69, "primary", this::play);
        button("OPTIONEN", 500, 389, 600, 60, "secondary", () -> showSettings(true));
        button("STEUERUNG", 500, 469, 600, 60, "secondary", () -> showHelp(true));
        button("ZUM HAUPTMENÜ", 500, 549, 600, 60, "quiet", this::showTitle);
        label(
                "Nach dem Verlassen des Tauchgangs startest du beim letzten Raumeingang."
                        + " Weiterspielen setzt diese Pause direkt fort.",
                400,
                670,
                800,
                90,
                23,
                "muted");
        label(
                "SEED " + run.seed() + "   /   " + run.room().sectorName(),
                400,
                780,
                800,
                30,
                17,
                "eyebrow");
    }

    private void showSettings(boolean inGame) {
        modalFromGame = inGame;
        setScreen(Screen.SETTINGS, true);
        heading(
                "OPTIONEN",
                "Dein Tauchgang. Dein Tempo.",
                "Lautstärke, Darstellung und ein zugänglicherer Einstieg.");
        var settings = service.profile().settings();
        label("Gesamtlautstärke", 350, 301, 420, 40, 28, "title");
        Slider volume = new Slider(0, 1, settings.masterVolume());
        place(volume, 850, 305, 390, 42);
        label("Musik", 350, 377, 420, 40, 28, "title");
        Slider music = new Slider(0, 1, settings.musicVolume());
        place(music, 850, 381, 390, 42);
        CheckBox reduced =
                new CheckBox("Ruhige Darstellung · weniger Partikel, kein Kamerawackeln");
        reduced.setSelected(settings.reducedMotion());
        place(reduced, 350, 466, 900, 40);
        CheckBox fullscreen = new CheckBox("Vollbild (auch mit F11 umschaltbar)");
        fullscreen.setSelected(stage.isFullScreen());
        place(fullscreen, 350, 532, 900, 40);
        CheckBox explorer = new CheckBox("Entdecker als Vorgabe für neue Runs");
        explorer.setSelected(settings.explorer());
        place(explorer, 350, 598, 900, 40);
        button(
                "ÜBERNEHMEN",
                900,
                731,
                340,
                63,
                "primary",
                () -> {
                    var next =
                            new Settings(
                                    volume.getValue(),
                                    music.getValue(),
                                    reduced.isSelected(),
                                    fullscreen.isSelected(),
                                    explorer.isSelected());
                    service.settings(next);
                    audio.settings(next);
                    stage.setFullScreen(next.fullscreen());
                    if (inGame) showPause();
                    else showTitle();
                });
        button(
                "← ZURÜCK",
                350,
                731,
                270,
                63,
                "quiet",
                () -> {
                    if (inGame) showPause();
                    else showTitle();
                });
    }

    private void showArchive() {
        setScreen(Screen.ARCHIVE, true);
        var profile = service.profile();
        heading(
                "BAUPLANARCHIV",
                "Was du findest, bleibt.",
                "Alternative Startmodule öffnen neue Spielweisen. Dein Run-Build beginnt jedes Mal"
                        + " neu.");
        int i = 0;
        for (ActiveModule module : ActiveModule.values()) {
            double x = 130 + i * 450;
            boolean unlocked = profile.unlocked().contains(module);
            pane(x, 323, 430, 260, "card");
            label(
                    unlocked ? "BAUPLAN GESICHERT" : "SIGNAL NOCH UNBEKANNT",
                    x + 28,
                    348,
                    375,
                    28,
                    17,
                    "eyebrow");
            label(module.title(), x + 28, 394, 375, 50, 36, "title");
            label(
                    unlocked
                            ? module.description()
                            : module == ActiveModule.ARC
                                    ? "Erreiche die erste Werkstatt (Raum 6)."
                                    : "Erreiche die zweite Werkstatt (Raum 12).",
                    x + 28,
                    458,
                    375,
                    83,
                    23,
                    "muted");
            i++;
        }
        label(
                profile.runs()
                        + " TAUCHGÄNGE     /     "
                        + profile.wins()
                        + " BRÜCKEN EROBERT     /     BESTER RAUM "
                        + profile.bestRoom()
                        + " / 18",
                130,
                655,
                1330,
                44,
                27,
                "accent");
        button("← ZURÜCK", 130, 760, 260, 60, "quiet", this::showTitle);
    }

    private void showHelp(boolean inGame) {
        modalFromGame = inGame;
        setScreen(Screen.HELP, true);
        heading(
                "STEUERUNG",
                "Lies den Raum. Dann handle.",
                "Gegner kündigen ihre Angriffe an. Ausweichen schützt kurz, Springen hilft gegen"
                        + " Bodenwellen.");
        String[][] controls = {
            {"A / D oder ← / →", "Bewegen"},
            {"LEER oder W", "Springen"},
            {"J oder linke Maustaste", "Werkzeugangriff · halten für weitere Schläge"},
            {"SHIFT", "Ausweichen · kurz unverwundbar"},
            {"K oder rechte Maustaste", "Aktives Modul · verbraucht Energie"},
            {"E", "Modul bergen, Werkstatt nutzen, Schott öffnen"},
            {"Q / I / M", "Reparaturset / Inventar / Bootskarte"},
            {"ESC / F11 / F12", "Pause / Vollbild / Screenshot auf dem Schreibtisch"}
        };
        for (int i = 0; i < controls.length; i++) {
            label(controls[i][0], 240, 267 + i * 58, 490, 44, 26, "accent");
            label(controls[i][1], 735, 267 + i * 58, 665, 44, 24, "muted");
        }
        button(
                "VERSTANDEN",
                1010,
                770,
                360,
                59,
                "primary",
                () -> {
                    if (inGame) showPause();
                    else showTitle();
                });
    }

    private void showOutcome() {
        setScreen(Screen.OUTCOME, true);
        boolean won = run.phase() == GameRun.Phase.VICTORY;
        heading(
                won ? "BRÜCKE EROBERT" : "SIGNAL VERLOREN",
                won ? "Du bestimmst den Kurs." : "Der nächste Versuch wartet.",
                won
                        ? "Behalte deinen Build und wage einen schwereren Tauchzyklus. Oder kehre"
                                + " mit diesem Sieg zurück."
                        : "Dein Run endet hier. Entdeckte Baupläne bleiben im Archiv.");
        pane(220, 332, 1160, 230, "card");
        String[] values = {
            String.format("%02d / 18", run.room().depth() + 1),
            "" + run.kills(),
            formatTime(run.elapsed()),
            "" + (run.cycle() + 1L)
        };
        String[] names = {"ERREICHTER RAUM", "GEGNER BESIEGT", "TAUCHZEIT", "ZYKLUS"};
        for (int i = 0; i < 4; i++) {
            label(values[i], 255 + i * 282, 371, 255, 75, 65, "title");
            label(names[i], 255 + i * 282, 471, 255, 32, 18, "eyebrow");
        }
        label(
                "BAUPLÄNE GESICHERT: " + service.profile().unlocked().size() + " / 3",
                220,
                609,
                1120,
                35,
                22,
                "accent");
        if (won)
            button(
                    "NÄCHSTER ZYKLUS  →",
                    850,
                    725,
                    530,
                    69,
                    "primary",
                    () -> {
                        if (service.nextCycle()) {
                            outcomeDelay = 0;
                            renderer.clearEffects();
                            play();
                        }
                    });
        else
            button(
                    "ERNEUT VERSUCHEN  [R]",
                    850,
                    725,
                    530,
                    69,
                    "primary",
                    () ->
                            startRun(
                                    System.nanoTime(),
                                    run.player().module(),
                                    service.profile().settings().explorer()));
        button("ZUM HAUPTMENÜ", 220, 725, 420, 69, "secondary", this::showTitle);
    }

    private void heading(String kicker, String title, String description) {
        label(kicker, 130, 105, 1340, 31, 19, "eyebrow");
        label(title, 130, 152, 1340, 80, 64, "title");
        label(description, 130, 235, 1340, 50, 24, "muted");
    }

    private Pane pane(double x, double y, double w, double h, String classes) {
        var pane = new Pane();
        pane.getStyleClass().addAll(classes.split(" "));
        place(pane, x, y, w, h);
        return pane;
    }

    private Label label(
            String text, double x, double y, double w, double h, double size, String style) {
        var label = new Label(text);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setFont(style.equals("title") ? assets.display(size) : assets.text(size));
        label.getStyleClass().add(style);
        label.setStyle(
                "-fx-font-size: "
                        + size
                        + "px; -fx-font-family: '"
                        + (style.equals("title") ? assets.displayFamily() : assets.textFamily())
                        + "';");
        place(label, x, y, w, h);
        label.setMouseTransparent(true);
        return label;
    }

    private Button button(
            String text, double x, double y, double w, double h, String style, Runnable action) {
        var button = new Button(text);
        button.getStyleClass().add(style);
        button.setFont(assets.text(h < 50 ? 18 : 23));
        button.setStyle(
                "-fx-font-size: "
                        + (h < 50 ? 18 : 23)
                        + "px; -fx-font-family: '"
                        + assets.textFamily()
                        + "';");
        button.setOnAction(
                event -> {
                    audio.play("click");
                    action.run();
                });
        if (w < 70) button.setStyle(button.getStyle() + "-fx-padding:0;");
        place(button, x, y, w, h);
        return button;
    }

    private void place(Node node, double x, double y, double w, double h) {
        node.setLayoutX(x);
        node.setLayoutY(y);
        if (node instanceof javafx.scene.layout.Region region) {
            region.setPrefSize(w, h);
            region.setMinSize(w, h);
            region.setMaxSize(w, h);
        }
        overlay.getChildren().add(node);
    }

    private static String formatTime(double seconds) {
        return String.format("%02d:%02d", (int) seconds / 60, (int) seconds % 60);
    }

    public void capture(Path file) throws IOException {
        world.applyCss();
        world.layout();
        var snapshot = world.snapshot(null, null);
        int w = (int) snapshot.getWidth(), h = (int) snapshot.getHeight();
        int[] pixels = new int[w * h];
        snapshot.getPixelReader()
                .getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);
        var buffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        buffered.setRGB(0, 0, w, h, pixels, 0, w);
        if (file.toAbsolutePath().getParent() != null)
            Files.createDirectories(file.toAbsolutePath().getParent());
        ImageIO.write(buffered, "png", file.toFile());
        System.out.println("ABYSS_CAPTURE " + file + " frames=" + frames + " dt=" + lastDt);
    }

    @Override
    public void close() {
        if (loop != null) loop.stop();
        audio.close();
    }
}
