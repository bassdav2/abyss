package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.*;
import ch.zhaw.abyss.ui.*;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Offline-Aufnahme des echten Renderers mit regulärem Testspieler und gekennzeichneten
 * QA-Einstiegen.
 */
public final class RenderDemo extends Application {
    private static final int FPS = 30, TOTAL = 82 * FPS;
    private int frame, chapter = -1;
    private GameRun run;
    private double clearedTime;
    private final Settings settings = new Settings(0, 0, false, false, false);
    private Process encoder;
    private OutputStream pipe;

    @Override
    public void start(Stage stage) throws Exception {
        Path out = Path.of("output/video/Abyss_Gameplay_Demo.mp4");
        Files.createDirectories(out.getParent());
        encoder =
                new ProcessBuilder(
                                "ffmpeg",
                                "-y",
                                "-f",
                                "rawvideo",
                                "-pix_fmt",
                                "bgra",
                                "-s",
                                "1600x900",
                                "-r",
                                "30",
                                "-i",
                                "pipe:0",
                                "-stream_loop",
                                "-1",
                                "-i",
                                "src/main/resources/audio/music.wav",
                                "-c:v",
                                "libx264",
                                "-preset",
                                "fast",
                                "-crf",
                                "21",
                                "-pix_fmt",
                                "yuv420p",
                                "-c:a",
                                "aac",
                                "-b:a",
                                "128k",
                                "-af",
                                "volume=0.35",
                                "-shortest",
                                "-movflags",
                                "+faststart",
                                out.toString())
                        .redirectError(Path.of("docs/qa/demo-encode.log").toFile())
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start();
        pipe = new BufferedOutputStream(encoder.getOutputStream(), 4 * 1024 * 1024);
        var canvas = new Canvas(1600, 900);
        var assets = new AssetCatalog();
        var renderer = new GameRenderer(canvas, assets);
        var reward = new Image(Path.of("docs/qa/screens/reward.png").toUri().toString());
        var route = new Image(Path.of("docs/qa/screens/route.png").toUri().toString());
        var scene = new Scene(new StackPane(canvas), 1280, 720);
        stage.setScene(scene);
        stage.setTitle("ABYSS · Offline-Demo-Export");
        stage.show();
        var pixels = new byte[1600 * 900 * 4];
        var snapshot = new WritableImage(1600, 900);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int batch = 0; batch < 2 && frame < TOTAL; batch++, frame++) {
                        double seconds = frame / (double) FPS;
                        int next =
                                seconds < 3
                                        ? 0
                                        : seconds < 21
                                                ? 1
                                                : seconds < 26
                                                        ? 2
                                                        : seconds < 44
                                                                ? 3
                                                                : seconds < 49
                                                                        ? 4
                                                                        : seconds < 79 ? 5 : 6;
                        if (next != chapter) {
                            chapter = next;
                            clearedTime = 0;
                            renderer.clearEffects();
                            if (chapter == 1) run = new GameRun(0, ActiveModule.PULSE, false);
                            if (chapter == 3) run = fixture(6, ActiveModule.ARC);
                            if (chapter == 5) run = fixture(11, ActiveModule.PULSE);
                            System.out.println("DEMO_CHAPTER " + chapter + " at " + seconds);
                        }
                        if (chapter == 1 || chapter == 3 || chapter == 5) {
                            for (int step = 0; step < 4; step++) {
                                if (run.phase() == GameRun.Phase.ROOM_CLEARED) {
                                    clearedTime += 1.0 / 120;
                                    if (clearedTime > 2) {
                                        CampaignPilot.advance(run, 0);
                                        clearedTime = 0;
                                        renderer.clearEffects();
                                    }
                                } else run.update(1.0 / 120, CampaignPilot.input(run));
                                for (var event : run.drainEvents()) renderer.event(event, settings);
                            }
                        }
                        renderer.update(1.0 / FPS);
                        renderer.render(run, settings, chapter == 0 || chapter == 6, true);
                        var g = canvas.getGraphicsContext2D();
                        if (chapter == 2) g.drawImage(reward, 0, 0, 1600, 900);
                        if (chapter == 4) g.drawImage(route, 0, 0, 1600, 900);
                        if (chapter == 5 && run.phase() == GameRun.Phase.VICTORY) {
                            g.setFill(Color.rgb(3, 14, 22, .82));
                            g.fillRoundRect(380, 290, 840, 160, 12, 12);
                            g.setTextAlign(TextAlignment.CENTER);
                            g.setFill(GameRenderer.AMBER);
                            g.setFont(assets.display(62));
                            g.fillText("BRÜCKE EROBERT", 800, 363);
                            g.setFill(GameRenderer.TEXT);
                            g.setFont(assets.text(25));
                            g.fillText("Ein nächster, schwererer Zyklus ist möglich.", 800, 414);
                        }
                        if (chapter == 6) {
                            g.setTextAlign(TextAlignment.CENTER);
                            g.setFill(GameRenderer.AMBER);
                            g.setFont(assets.display(48));
                            g.fillText("SPIELBARER ENTWICKLUNGSSTAND", 800, 728);
                        }
                        String label =
                                switch (chapter) {
                                    case 0 -> "ABYSS · VOM HECK BIS ZUR BRÜCKE";
                                    case 1 -> "01 / HECK · BEWEGUNG, AUSWEICHEN, KAMPF";
                                    case 2 -> "02 / WERKSTATT · DEIN BUILD ENTSTEHT IM RUN";
                                    case 3 -> "03 / MASCHINENDECK · LICHTBOGEN UND GEFAHREN";
                                    case 4 -> "04 / NAVIGATION · SICHERHEIT ODER MEHR RISIKO";
                                    case 5 -> "05 / DIE BRÜCKE · DER LOTSE";
                                    default -> "JAVA + JAVAFX · LOKALE MAC-APP";
                                };
                        g.setGlobalAlpha(1);
                        g.setFill(Color.rgb(2, 10, 16, .88));
                        g.fillRect(0, 766, 1600, 48);
                        g.setTextAlign(TextAlignment.LEFT);
                        g.setFont(assets.text(19));
                        g.setFill(GameRenderer.AMBER);
                        g.fillText(label, 32, 797);
                        g.setTextAlign(TextAlignment.RIGHT);
                        g.setFont(assets.text(15));
                        g.setFill(GameRenderer.TEXT);
                        g.fillText("AUTOMATISIERTE DEMO · VORBEREITETE SPIELSTÄNDE", 1568, 797);
                        canvas.snapshot(null, snapshot);
                        snapshot.getPixelReader()
                                .getPixels(
                                        0,
                                        0,
                                        1600,
                                        900,
                                        PixelFormat.getByteBgraInstance(),
                                        pixels,
                                        0,
                                        1600 * 4);
                        pipe.write(pixels);
                    }
                    if (frame >= TOTAL) {
                        stop();
                        pipe.close();
                        int result = encoder.waitFor();
                        if (result != 0) throw new IOException("ffmpeg exit=" + result);
                        System.out.println("DEMO_READY " + out + " frames=" + frame);
                        Platform.exit();
                    }
                } catch (Exception error) {
                    error.printStackTrace();
                    encoder.destroy();
                    Platform.exit();
                    System.exit(1);
                }
            }
        }.start();
    }

    private static GameRun fixture(int depth, ActiveModule module) {
        return GameRun.restore(
                new RunCheckpoint(
                        73419,
                        0,
                        depth,
                        0,
                        module,
                        false,
                        140,
                        140,
                        45,
                        Map.of(
                                Upgrade.SERVO,
                                2,
                                Upgrade.MEDICAL,
                                2,
                                Upgrade.RECOVERY,
                                1,
                                Upgrade.PLATING,
                                2,
                                Upgrade.CAPACITOR,
                                2,
                                Upgrade.COOLANT,
                                1),
                        28,
                        420,
                        Collections.nCopies(depth + 1, 0)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
