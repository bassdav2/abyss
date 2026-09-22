package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.*;
import ch.zhaw.abyss.infrastructure.AudioSystem;
import ch.zhaw.abyss.ui.*;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import java.nio.file.*;
import java.util.*;

/** Reale JavaFX-Frames mit Testspieler. Keine OS-Eingaben und kein menschlicher Spieltest. */
public final class RenderSoak extends Application {
    private final List<Double> frameCosts = new ArrayList<>(), intervals = new ArrayList<>();
    private final EnumSet<GameEvent.Type> eventsSeen = EnumSet.noneOf(GameEvent.Type.class);
    private final Set<String> roomsSeen = new HashSet<>();
    private final Set<ActiveModule> modulesSeen = EnumSet.noneOf(ActiveModule.class);
    private int runs, wins, losses, frames, errors;
    private GameRun run;
    private AudioSystem audio;
    private long begin, previous;
    private double accumulator;

    @Override
    public void start(Stage stage) throws Exception {
        double duration =
                Double.parseDouble(getParameters().getNamed().getOrDefault("seconds", "180"));
        double speed = Double.parseDouble(getParameters().getNamed().getOrDefault("speed", "4"));
        Path output =
                Path.of(
                        getParameters()
                                .getNamed()
                                .getOrDefault("output", "docs/qa/render-soak.json"));
        var canvas = new Canvas(1600, 900);
        var renderer = new GameRenderer(canvas, new AssetCatalog());
        var settings = new Settings(0, 0, false, false, false);
        audio = new AudioSystem();
        audio.settings(settings);
        audio.start();
        int nativeClips = 0;
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
                                            getClass().getResource("/audio/" + name + ".wav"))
                                    .toExternalForm());
            clip.setVolume(0);
            clip.play(0);
            clip.stop();
            nativeClips++;
        }
        System.out.println("SOAK_NATIVE_AUDIO_CLIPS=" + nativeClips + " volume=0");
        stage.setScene(new Scene(new StackPane(canvas), 1280, 720));
        stage.setTitle("ABYSS · automated render soak");
        stage.show();
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> {
                    errors++;
                    error.printStackTrace();
                });
        run = new GameRun(0, ActiveModule.PULSE, false);
        begin = System.nanoTime();
        previous = begin;
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsed = (now - begin) / 1e9, dt = Math.min(.1, (now - previous) / 1e9);
                previous = now;
                if (frames > 120) intervals.add(dt * 1000);
                long started = System.nanoTime();
                accumulator += dt * speed;
                while (accumulator >= 1.0 / 120) {
                    if (run.phase() == GameRun.Phase.ROOM_CLEARED)
                        CampaignPilot.advance(run, runs % 2);
                    else if (run.phase() == GameRun.Phase.VICTORY
                            || run.phase() == GameRun.Phase.DEFEAT) {
                        if (run.phase() == GameRun.Phase.VICTORY) wins++;
                        else losses++;
                        runs++;
                        run = new GameRun(runs, ActiveModule.values()[runs % 3], runs % 2 == 1);
                        renderer.clearEffects();
                    }
                    run.update(1.0 / 120, CampaignPilot.input(run));
                    accumulator -= 1.0 / 120;
                    for (var event : run.drainEvents()) {
                        eventsSeen.add(event.type());
                        renderer.event(event, settings);
                        audio.event(event);
                    }
                }
                roomsSeen.add(run.room().depth() + ":" + run.room().branch());
                modulesSeen.add(run.player().module());
                renderer.update(dt);
                renderer.render(run, settings, false, true);
                double cost = (System.nanoTime() - started) / 1e6;
                if (frames > 120) frameCosts.add(cost);
                frames++;
                if (elapsed >= duration) {
                    stop();
                    audio.close();
                    try {
                        Files.createDirectories(output.toAbsolutePath().getParent());
                        String report =
                                String.format(
                                        Locale.ROOT,
                                        """
                                        {
                                          "kind": "automated JavaFX render test; no human input",
                                          "seconds": %.2f, "simulationSpeed": %.1f,
                                          "frames": %d, "completedRuns": %d, "wins": %d, "losses": %d,
                                          "distinctRoomBranches": %d, "modules": %d, "eventTypes": %d,
                                          "nativeAudioClipsLoadedAndInvokedAtZeroVolume": 16,
                                          "updateAndDrawCpuMsP50": %.3f, "updateAndDrawCpuMsP95": %.3f,
                                          "frameIntervalMsP50": %.3f, "frameIntervalMsP95": %.3f,
                                          "uncaughtErrors": %d,
                                          "renderTimingNote": "CPU command submission; not GPU completion. Locked desktop can throttle frames."
                                        }
                                        """,
                                        elapsed,
                                        speed,
                                        frames,
                                        runs,
                                        wins,
                                        losses,
                                        roomsSeen.size(),
                                        modulesSeen.size(),
                                        eventsSeen.size(),
                                        percentile(frameCosts, .5),
                                        percentile(frameCosts, .95),
                                        percentile(intervals, .5),
                                        percentile(intervals, .95),
                                        errors);
                        Files.writeString(output, report);
                        System.out.println(report);
                    } catch (Exception error) {
                        error.printStackTrace();
                        System.exit(1);
                    }
                    Platform.exit();
                }
            }
        }.start();
    }

    private static double percentile(List<Double> values, double fraction) {
        if (values.isEmpty()) return 0;
        var sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        return sorted.get(Math.min(sorted.size() - 1, (int) (sorted.size() * fraction)));
    }

    @Override
    public void stop() {
        if (audio != null) audio.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
