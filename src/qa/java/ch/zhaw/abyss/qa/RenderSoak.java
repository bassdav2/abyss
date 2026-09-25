package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameEvent;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.render.SpriteBank;
import ch.zhaw.abyss.ui.render.WorldRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;

/**
 * Render-Probelauf ohne Fenster: Der Testspieler spielt fortlaufend Tauchgänge aller Klassen, jedes
 * Bild wird vollständig gezeichnet (Licht, Bloom, HUD). Gemessen werden Zeichenkosten pro Bild und
 * Fehler. Das Ergebnis landet als JSON unter {@code docs/qa/render-soak.json}.
 */
public final class RenderSoak {
    private RenderSoak() {}

    /**
     * @param args {@code --seconds=N} reale Laufzeit, {@code --output=Pfad}
     * @throws IOException bei Schreibfehlern
     */
    public static void main(String[] args) throws IOException {
        double seconds = 60;
        Path output = Path.of("docs/qa/render-soak.json");
        for (String arg : args) {
            if (arg.startsWith("--seconds=")) seconds = Double.parseDouble(arg.substring(10));
            if (arg.startsWith("--output=")) output = Path.of(arg.substring(9));
        }
        var renderer = new WorldRenderer(PixelFont.load(), new SpriteBank());
        var settings = Settings.DEFAULT;
        var costs = new ArrayList<Double>();
        var events = EnumSet.noneOf(GameEvent.Type.class);
        var rooms = new HashSet<String>();
        int runs = 0, wins = 0, losses = 0, frames = 0, errors = 0;
        long end = System.nanoTime() + (long) (seconds * 1e9);
        var run = new GameRun(RunSetup.standard(0, DiverClass.MECHANIC));
        while (System.nanoTime() < end) {
            try {
                for (int step = 0; step < 2; step++) {
                    if (run.phase() == GameRun.Phase.ROOM_CLEARED)
                        CampaignPilot.advance(run, runs % 2);
                    else if (run.phase() == GameRun.Phase.VICTORY
                            || run.phase() == GameRun.Phase.DEFEAT) {
                        if (run.phase() == GameRun.Phase.VICTORY) wins++;
                        else losses++;
                        runs++;
                        var diver = DiverClass.values()[runs % DiverClass.values().length];
                        run = new GameRun(RunSetup.standard(runs * 7_919L, diver));
                        renderer.clearEffects();
                    }
                    run.update(1.0 / 120, CampaignPilot.input(run));
                }
                for (var event : run.drainEvents()) {
                    events.add(event.type());
                    renderer.event(event, run, settings);
                }
                rooms.add(run.room().theme() + ":" + run.room().kind());
                long started = System.nanoTime();
                renderer.update(1.0 / 60, run, settings);
                renderer.render(run, settings, true);
                costs.add((System.nanoTime() - started) / 1e6);
                frames++;
            } catch (RuntimeException error) {
                errors++;
                error.printStackTrace();
                run = new GameRun(RunSetup.standard(++runs, DiverClass.MECHANIC));
            }
        }
        Collections.sort(costs);
        double average = costs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double p95 = costs.isEmpty() ? 0 : costs.get((int) (costs.size() * .95));
        double worst = costs.isEmpty() ? 0 : costs.getLast();
        String json =
                String.format(
                        Locale.ROOT,
                        "{%n  \"seconds\": %.0f,%n  \"frames\": %d,%n  \"errors\": %d,%n  \"runs\":"
                            + " %d,%n  \"wins\": %d,%n  \"losses\": %d,%n  \"averageRenderMs\":"
                            + " %.3f,%n  \"p95RenderMs\": %.3f,%n  \"worstRenderMs\": %.3f,%n "
                            + " \"roomThemesSeen\": %d,%n  \"eventTypesSeen\": %d,%n  \"note\":"
                            + " \"Headless-Software-Rendering mit Testspieler, zwei"
                            + " Simulationsschritte pro Bild. Kein menschlicher Spieltest.\"%n}%n",
                        seconds,
                        frames,
                        errors,
                        runs,
                        wins,
                        losses,
                        average,
                        p95,
                        worst,
                        rooms.size(),
                        events.size());
        if (output.toAbsolutePath().getParent() != null)
            Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, json);
        System.out.print(json);
        if (errors > 0) System.exit(1);
    }
}
