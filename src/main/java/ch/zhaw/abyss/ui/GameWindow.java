package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.application.Achievement;
import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Interaction;
import ch.zhaw.abyss.domain.Offer;
import ch.zhaw.abyss.infrastructure.AudioSystem;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.gui.Gui;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.render.SpriteBank;
import ch.zhaw.abyss.ui.render.WorldRenderer;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Hauptfenster: Spielschleife mit festem Simulationsschritt, Eingabeübersetzung und Wechsel der
 * Bildschirme. Enthält keine Spielregeln; Interaktionen fragt es bei der Domäne ab ({@link
 * GameRun#interaction()}) und ruft dann Anwendungsfälle auf.
 */
public final class GameWindow implements AutoCloseable, Navigator {
    private enum Screen {
        TITLE,
        LOADOUT,
        WARDROBE,
        ARCHIVE,
        SETTINGS,
        HELP,
        PLAY,
        PAUSE,
        REWARD,
        SHRINE,
        ROUTE,
        INVENTORY,
        MAP,
        OUTCOME
    }

    private static final double STEP = 1.0 / 120;
    private final Stage stage;
    private final GameService service;
    private final Map<String, String> arguments;
    private final PixelFont font = PixelFont.load();
    private final SpriteBank bank = new SpriteBank();
    private final WorldRenderer renderer = new WorldRenderer(font, bank);
    private final Canvas canvas = new Canvas(1280, 720);
    private final PixelView view = new PixelView(canvas, WorldRenderer.W, WorldRenderer.H);
    private final Gui gui = new Gui(font);
    private final InputController input = new InputController();
    private final AudioSystem audio = new AudioSystem();
    private final MenuScreens menus;
    private final RunScreens runScreens;
    private final Scene scene;
    private Screen screen = Screen.TITLE;
    private GameRun run;
    private AnimationTimer loop;
    private long previous;
    private double accumulator, hitStop, outcomeDelay, lastDt;
    // Sekunden in der Siegesszene; negativ, solange sie nicht läuft.
    private double ending = -1;
    // Sekunden im Auftakt vor dem ersten Tauchgang; negativ, solange er nicht läuft.
    private double intro = -1;
    private boolean endingSeen;
    private int frames;
    private boolean inGameModal;

    /**
     * @param stage Fenster
     * @param service Anwendungsdienst
     * @param arguments benannte Startparameter, etwa für automatische Bildschirmfotos
     */
    public GameWindow(Stage stage, GameService service, Map<String, String> arguments) {
        this.stage = stage;
        this.service = service;
        this.arguments = arguments;
        this.menus = new MenuScreens(this);
        this.runScreens = new RunScreens(this);
        gui.onConfirm(() -> audio.play("click"));
        var root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #020409;");
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        canvas.setManaged(false);
        scene = new Scene(root, 1280, 720, Color.web("#020409"));
        stage.setScene(scene);
        stage.setTitle("ABYSS · Vom Heck bis zur Brücke");
        stage.setMinWidth(960);
        stage.setMinHeight(560);
        stage.setFullScreenExitHint("");
        stage.focusedProperty()
                .addListener(
                        (o, was, focused) -> {
                            if (!focused
                                    && screen == Screen.PLAY
                                    && !arguments.containsKey("capture")) pause();
                        });
        renderer.look(service.profile().cosmetics());
        installInput();
        audio.settings(service.profile().settings());
        bank.warmUp();
    }

    /** Zeigt das Fenster und startet die Spielschleife. */
    public void show() {
        title();
        stage.show();
        stage.setFullScreen(service.profile().settings().fullscreen());
        audio.start();
        loop =
                new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        tick(now);
                    }
                };
        loop.start();
        automation();
    }

    private void tick(long now) {
        if (previous == 0) previous = now;
        double dt = Math.min(.1, (now - previous) / 1e9);
        previous = now;
        lastDt = dt;
        frames++;
        var settings = service.profile().settings();
        if (screen == Screen.PLAY && run != null && intro >= 0) {
            intro += dt;
            renderer.update(dt, null, settings);
            renderer.renderIntro(intro, settings);
            if (intro > 9) skipIntro();
        } else if (screen == Screen.PLAY && run != null) {
            double sim = dt * renderer.timeScale();
            if (hitStop > 0) {
                hitStop -= dt;
                sim = 0;
            }
            accumulator += sim;
            while (accumulator >= STEP) {
                run.update(STEP, input.frame(renderer.screenX(run.player().x())));
                accumulator -= STEP;
            }
            for (var event : run.drainEvents()) {
                renderer.event(event, run, settings);
                audio.event(event);
            }
            hitStop = Math.max(hitStop, renderer.takeHitStop());
            if (run.phase() == GameRun.Phase.DEFEAT || run.phase() == GameRun.Phase.VICTORY) {
                service.recordOutcome();
                outcomeDelay += dt;
                if (outcomeDelay > (run.phase() == GameRun.Phase.VICTORY ? 2.2 : 1.6)) outcome();
            }
            renderer.update(dt, run, settings);
            renderer.render(run, settings, true);
        } else if (run != null && inGameScreen()) {
            if (screen == Screen.OUTCOME && ending >= 0) {
                ending += dt;
                renderer.update(dt, null, settings);
                renderer.renderEnding(ending, settings);
                if (ending > 10) finishEnding();
            } else if (screen == Screen.MAP) {
                renderer.update(dt, null, settings);
                renderer.renderMap(run);
                drawScreen(dt);
            } else {
                renderer.update(dt, run, settings);
                renderer.render(run, settings, false);
                renderer.dim(screen == Screen.OUTCOME ? .78 : .6);
                drawScreen(dt);
            }
        } else {
            accumulator = 0;
            renderer.update(dt, null, settings);
            renderer.renderTitle(settings, screen == Screen.TITLE);
            if (screen != Screen.TITLE) renderer.dim(.62);
            drawScreen(dt);
        }
        audio.context(run, !inGameScreen() && screen != Screen.PLAY);
        view.present(renderer.frame(), settings.retroFilter());
        String message = service.takeStorageMessage();
        if (!message.isBlank()) toast(message);
        for (var achievement : service.takeAchievements()) {
            renderer.hud()
                    .toast(
                            "LOGBUCH: "
                                    + achievement.title()
                                    + " · +"
                                    + achievement.reward()
                                    + " KERNE",
                            Pal.RUST_6);
            audio.play("upgrade");
        }
    }

    /** Zeichnet das Bordsystem des aktuellen Bildschirms über das fertige Weltbild. */
    private void drawScreen(double dt) {
        gui.begin(renderer.frame(), dt);
        switch (screen) {
            case TITLE -> menus.title(gui);
            case LOADOUT -> menus.loadout(gui);
            case WARDROBE -> menus.wardrobe(gui);
            case ARCHIVE -> menus.archive(gui);
            case SETTINGS -> menus.settings(gui, inGameModal);
            case HELP -> menus.help(gui, inGameModal);
            case PAUSE -> runScreens.pause(gui);
            case REWARD -> runScreens.reward(gui);
            case SHRINE -> runScreens.shrine(gui);
            case ROUTE -> runScreens.route(gui);
            case INVENTORY -> runScreens.inventory(gui);
            case MAP -> runScreens.map(gui);
            case OUTCOME -> runScreens.outcome(gui);
            case PLAY -> {}
        }
        if (screen == Screen.TITLE) renderer.hud().drawMessagesOnly(renderer.frame());
        else renderer.hud().drawMessagesCompact(renderer.frame());
        gui.end();
    }

    private boolean inGameScreen() {
        return switch (screen) {
            case PAUSE, REWARD, SHRINE, ROUTE, INVENTORY, MAP, OUTCOME -> true;
            case SETTINGS, HELP -> inGameModal;
            default -> false;
        };
    }

    // ---------------------------------------------------------------------------------------------
    // Eingabe
    // ---------------------------------------------------------------------------------------------

    private void installInput() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> input.up(event.getCode()));
        scene.addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                event -> {
                    pointer(event);
                    if (screen != Screen.PLAY) {
                        if (event.getButton() == MouseButton.PRIMARY) gui.mouseButton(true);
                        return;
                    }
                    if (event.getButton() == MouseButton.PRIMARY) input.mouseAttack(true);
                    if (event.getButton() == MouseButton.SECONDARY) input.mouseAbility(true);
                });
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, this::pointer);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::pointer);
        scene.addEventFilter(
                MouseEvent.MOUSE_RELEASED,
                event -> {
                    input.mouseAttack(false);
                    gui.mouseButton(false);
                });
    }

    private void pointer(MouseEvent event) {
        input.pointer(view.toPixelX(event.getSceneX()));
        gui.mouse(view.toPixelX(event.getSceneX()), view.toPixelY(event.getSceneY()));
    }

    private void keyPressed(KeyEvent event) {
        var code = event.getCode();
        if (code == KeyCode.F11) {
            stage.setFullScreen(!stage.isFullScreen());
            event.consume();
            return;
        }
        if (code == KeyCode.F12) {
            try {
                capture(
                        Path.of(
                                System.getProperty("user.home"),
                                "Desktop",
                                "Abyss-" + System.currentTimeMillis() + ".png"));
                toast("Bildschirmfoto gespeichert");
            } catch (IOException e) {
                toast("Bildschirmfoto fehlgeschlagen");
            }
            event.consume();
            return;
        }
        if (code == KeyCode.ESCAPE) {
            switch (screen) {
                case PLAY -> {
                    if (intro >= 0) skipIntro();
                    else pause();
                }
                case PAUSE, REWARD, SHRINE, ROUTE, INVENTORY, MAP -> play();
                case SETTINGS, HELP -> {
                    if (inGameModal) pause();
                    else title();
                }
                case OUTCOME -> {
                    if (ending >= 0) finishEnding();
                }
                default -> title();
            }
            event.consume();
            return;
        }
        int digit = digit(event);
        if (screen != Screen.PLAY && !(screen == Screen.OUTCOME && ending >= 0)) menuKey(event);
        switch (screen) {
            case PLAY -> {
                if (intro >= 0) skipIntro();
                else if (code == KeyCode.I || code == KeyCode.TAB) inventory();
                else if (code == KeyCode.M) map();
                else if (code == KeyCode.E) interact();
                else input.down(code);
                event.consume();
            }
            case REWARD -> {
                if (digit >= 0) runScreens.choose(digit);
            }
            case SHRINE -> {
                if (digit >= 0) runScreens.acceptDeal(digit);
            }
            case ROUTE -> {
                if (digit >= 0) runScreens.chooseRoute(digit);
            }
            case INVENTORY -> {
                if (code == KeyCode.I || code == KeyCode.TAB) play();
                event.consume();
            }
            case MAP -> {
                if (code == KeyCode.M) play();
            }
            case OUTCOME -> {
                if (ending >= 0
                        && (code == KeyCode.SPACE || code == KeyCode.ENTER || code == KeyCode.E))
                    finishEnding();
                else if (code == KeyCode.R && run != null && run.phase() == GameRun.Phase.DEFEAT)
                    startRun(service.profile().loadout(), System.nanoTime());
            }
            default -> {}
        }
    }

    /** Übersetzt Tasten in Eingaben für das Bordsystem. */
    private void menuKey(KeyEvent event) {
        switch (event.getCode()) {
            case UP, W -> gui.nav(Gui.Nav.UP);
            case DOWN, S -> gui.nav(Gui.Nav.DOWN);
            case LEFT, A -> gui.nav(Gui.Nav.LEFT);
            case RIGHT, D -> gui.nav(Gui.Nav.RIGHT);
            case ENTER, SPACE, E -> gui.confirm();
            case BACK_SPACE, DELETE -> gui.backspace();
            default -> gui.type(event.getText());
        }
        event.consume();
    }

    private static int digit(KeyEvent event) {
        String text = event.getText();
        if (text != null && text.length() == 1 && Character.isDigit(text.charAt(0)))
            return text.charAt(0) - '1';
        return switch (event.getCode()) {
            case DIGIT1, NUMPAD1 -> 0;
            case DIGIT2, NUMPAD2 -> 1;
            case DIGIT3, NUMPAD3 -> 2;
            case DIGIT4, NUMPAD4 -> 3;
            case DIGIT5, NUMPAD5 -> 4;
            case DIGIT6, NUMPAD6 -> 5;
            default -> -1;
        };
    }

    private void interact() {
        var interaction = run.interaction();
        switch (interaction) {
            case REWARD -> {
                var offers = run.offers();
                if (offers.size() == 1 && offers.getFirst().type() == Offer.Type.SUPPLIES)
                    run.take(offers.getFirst());
                else reward();
            }
            case WORKSHOP -> {
                run.repair();
                reward();
            }
            case MERCHANT -> reward();
            case SHRINE -> shrine();
            case CONSOLE -> run.useConsole();
            case EXIT -> route();
            case LOCKED -> toast("Verriegelt · Sichere zuerst den Raum");
            case NONE -> {
                if (run.phase() == GameRun.Phase.ROOM_CLEARED)
                    toast("E an der Bergung oder am rechten Schott");
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------------------------

    private void setScreen(Screen next) {
        screen = next;
        input.clear();
        gui.reset();
        gui.cursor(next != Screen.PLAY);
        scene.setCursor(next == Screen.PLAY ? Cursor.CROSSHAIR : Cursor.NONE);
    }

    @Override
    public void title() {
        setScreen(Screen.TITLE);
        inGameModal = false;
        run = null;
        renderer.clearEffects();
    }

    @Override
    public void loadout() {
        menus.openLoadout();
        setScreen(Screen.LOADOUT);
    }

    @Override
    public void wardrobe() {
        menus.openWardrobe();
        setScreen(Screen.WARDROBE);
    }

    @Override
    public void archive(int tab) {
        menus.openArchive(tab);
        setScreen(Screen.ARCHIVE);
        run = null;
    }

    @Override
    public void settings(boolean inGame) {
        inGameModal = inGame;
        menus.openSettings();
        setScreen(Screen.SETTINGS);
    }

    @Override
    public void help(boolean inGame) {
        inGameModal = inGame;
        setScreen(Screen.HELP);
    }

    @Override
    public void play() {
        if (run == null) return;
        setScreen(Screen.PLAY);
        inGameModal = false;
        canvas.requestFocus();
    }

    @Override
    public void pause() {
        if (run == null) return;
        setScreen(Screen.PAUSE);
    }

    @Override
    public void route() {
        setScreen(Screen.ROUTE);
    }

    @Override
    public void reward() {
        setScreen(Screen.REWARD);
    }

    @Override
    public void shrine() {
        setScreen(Screen.SHRINE);
    }

    @Override
    public void inventory() {
        setScreen(Screen.INVENTORY);
    }

    @Override
    public void map() {
        setScreen(Screen.MAP);
    }

    @Override
    public void outcome() {
        setScreen(Screen.OUTCOME);
        if (run != null && run.phase() == GameRun.Phase.VICTORY && !endingSeen) {
            // Erst die Siegesszene zeigen, danach die Auswertung.
            endingSeen = true;
            ending = 0;
            return;
        }
        ending = -1;
    }

    private void finishEnding() {
        ending = -1;
        gui.reset();
    }

    /**
     * @return {@code true}, solange die Siegesszene läuft
     */
    public boolean endingPlaying() {
        return ending >= 0;
    }

    /** Überspringt die Siegesszene wie die Leertaste. */
    public void skipEnding() {
        if (ending >= 0) finishEnding();
    }

    @Override
    public void startRun(Loadout loadout, long seed) {
        try {
            run = service.start(loadout, seed);
        } catch (IllegalArgumentException e) {
            toast(e.getMessage());
            return;
        }
        begin();
        toast("Kämpf dich zur Brücke vor. Das rechte Schott führt weiter.");
        // Auftakt, bis zum ersten Mal ein Raum gesichert wurde (auch nach Übernahme alter Stände).
        if (!service.profile().achievements().contains(Achievement.FIRST_ROOM)) intro = 0;
    }

    /**
     * @return {@code true}, solange der Auftakt vor dem ersten Tauchgang läuft
     */
    public boolean introPlaying() {
        return intro >= 0;
    }

    /** Beendet den Auftakt und zeigt die Raumkarte. */
    public void skipIntro() {
        if (intro < 0) return;
        intro = -1;
        input.clear();
        renderer.hud().roomCard(run.room());
    }

    @Override
    public void resume() {
        service.resume()
                .ifPresentOrElse(
                        value -> {
                            run = value;
                            begin();
                            toast("Raum-Sicherung geladen");
                        },
                        () -> toast("Keine gültige Raum-Sicherung"));
    }

    private void begin() {
        intro = -1;
        outcomeDelay = 0;
        ending = -1;
        endingSeen = false;
        hitStop = 0;
        renderer.look(service.profile().cosmetics());
        renderer.clearEffects();
        renderer.hud().roomCard(run.room());
        play();
    }

    @Override
    public void nextCycle() {
        if (service.nextCycle()) {
            outcomeDelay = 0;
            ending = -1;
            endingSeen = false;
            renderer.clearEffects();
            renderer.hud()
                    .banner(
                            "ZYKLUS " + (run.cycle() + 1),
                            "Das Boot taucht tiefer",
                            Pal.RUST_6,
                            2.5);
            play();
        }
    }

    @Override
    public void applySettings(Settings settings) {
        service.settings(settings);
        audio.settings(settings);
        stage.setFullScreen(settings.fullscreen());
    }

    @Override
    public void toast(String text) {
        renderer.hud().toast(text, Pal.TEAL_5);
    }

    @Override
    public void quit() {
        Platform.exit();
    }

    @Override
    public GameService service() {
        return service;
    }

    @Override
    public GameRun run() {
        return run;
    }

    @Override
    public SpriteBank bank() {
        return bank;
    }

    // ---------------------------------------------------------------------------------------------
    // Automatisierung für QA-Bildschirmfotos
    // ---------------------------------------------------------------------------------------------

    private void automation() {
        if (arguments.containsKey("start"))
            startRun(
                    service.profile().loadout(),
                    Long.parseLong(arguments.getOrDefault("seed", "73419")));
        if (arguments.containsKey("resume")) resume();
        if (arguments.containsKey("qa-screen")) {
            switch (arguments.get("qa-screen")) {
                case "loadout" -> loadout();
                case "wardrobe" -> wardrobe();
                case "archive" -> archive(Integer.parseInt(arguments.getOrDefault("tab", "0")));
                case "settings" -> settings(false);
                case "help" -> help(false);
                case "inventory" -> {
                    if (run != null) inventory();
                }
                case "map" -> {
                    if (run != null) map();
                }
                case "pause" -> {
                    if (run != null) pause();
                }
                case "route" -> {
                    if (run != null && run.phase() == GameRun.Phase.ROOM_CLEARED) route();
                }
                case "reward" -> {
                    if (run != null && run.phase() == GameRun.Phase.ROOM_CLEARED) reward();
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

    /**
     * Speichert den aktuellen Fensterinhalt als PNG.
     *
     * @param file Ziel
     * @throws IOException bei Schreibfehlern
     */
    public void capture(Path file) throws IOException {
        var snapshot = scene.getRoot().snapshot(null, null);
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

    /** Hält die Spielschleife an und gibt Audio frei. */
    @Override
    public void close() {
        if (loop != null) loop.stop();
        audio.close();
    }

    /**
     * @return aktuell angezeigter Bildschirm, für Komponententests
     */
    public String screenName() {
        return screen.name();
    }

    /**
     * @return laufender Tauchgang, für Komponententests
     */
    public GameRun currentRun() {
        return run;
    }

    /**
     * @return {@code true}, wenn die Interaktionstaste gerade etwas auslösen würde
     */
    public boolean interactionAvailable() {
        return run != null && run.interaction() != Interaction.NONE;
    }
}
