package ch.zhaw.abyss.ui.render;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameEvent;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.qa.CampaignPilot;
import ch.zhaw.abyss.ui.pixel.PixelFont;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

/**
 * Komponententest der Pixel-Darstellung ohne Fenster: Ein Testspieler spielt durch alle Sektionen,
 * der Renderer zeichnet jedes Bild samt Ereignissen. Geprüft wird Fehlerfreiheit und dass sinnvolle
 * Bilder entstehen.
 */
class RenderSmokeTest {
    @Test
    void renderingEveryFrameOfAPlaythroughProducesVariedImagesWithoutErrors() {
        var font = PixelFont.load();
        var bank = new SpriteBank();
        var renderer = new WorldRenderer(font, bank);
        var settings = Settings.DEFAULT;
        var run = new GameRun(RunSetup.standard(2024, DiverClass.MECHANIC));
        var sectors = new HashSet<Integer>();
        int frames = 0;
        for (int tick = 0;
                tick < 120 * 400
                        && run.phase() != GameRun.Phase.VICTORY
                        && run.phase() != GameRun.Phase.DEFEAT;
                tick++) {
            if (run.phase() == GameRun.Phase.ROOM_CLEARED) CampaignPilot.advance(run, 1);
            else run.update(1.0 / 120, CampaignPilot.input(run));
            for (var event : run.drainEvents()) renderer.event(event, run, settings);
            if (tick % 8 == 0) {
                renderer.update(1.0 / 15, run, settings);
                renderer.render(run, settings, true);
                frames++;
                sectors.add(run.room().sector());
            }
        }
        assertTrue(frames > 100);
        assertEquals(4, sectors.size(), "alle vier Sektionen wurden gezeichnet");
        var pixels = renderer.frame().pixels();
        var colors = new HashSet<Integer>();
        for (int i = 0; i < pixels.length; i += 37) colors.add(pixels[i]);
        assertTrue(colors.size() > 40, "das Bild ist nicht einfarbig");
    }

    @Test
    void hitStopsPunchButNeverFreezeTheSwarmForLong() {
        var renderer = new WorldRenderer(PixelFont.load(), new SpriteBank());
        var settings = Settings.DEFAULT;
        var run = new GameRun(RunSetup.standard(7, DiverClass.MECHANIC));
        renderer.update(1.0 / 60, run, settings);
        renderer.event(new GameEvent(GameEvent.Type.CRIT, 600, 500, 40, "#2"), run, settings);
        assertTrue(renderer.takeHitStop() > 0, "der erste Volltreffer sitzt");
        double frozen = 0;
        for (int frame = 0; frame < 60 * 60; frame++) {
            // Endgame: in jedem Bild mehrere Volltreffer, Abschüsse und Explosionen.
            for (int i = 0; i < 6; i++) {
                renderer.event(
                        new GameEvent(GameEvent.Type.CRIT, 600, 500, 40, "#3"), run, settings);
                renderer.event(
                        new GameEvent(GameEvent.Type.ELITE_DOWN, 640, 500, 2, "X"), run, settings);
            }
            frozen += renderer.takeHitStop();
            renderer.update(1.0 / 60, run, settings);
        }
        assertTrue(frozen / 60 <= .045, "Stillstand " + Math.round(frozen / .6) + " %");
    }

    @Test
    void theInterfaceLayerDoublesTheWorldAndDrawsTheHudOnTop() {
        var renderer = new WorldRenderer(PixelFont.load(), new SpriteBank());
        var settings = Settings.DEFAULT;
        var run = new GameRun(RunSetup.standard(7, DiverClass.MECHANIC));
        renderer.update(1.0 / 30, run, settings);
        renderer.render(run, settings, false);
        var world = renderer.frame();
        var ui = renderer.ui();
        assertEquals(WorldRenderer.W * 2, ui.width());
        assertEquals(WorldRenderer.H * 2, ui.height());
        // Ohne HUD ist jedes Pixel der Welt ein 2 × 2-Block der Oberflächenebene.
        int x = 300, y = 200;
        int color = world.pixels()[y * WorldRenderer.W + x] | 0xFF000000;
        for (int dy = 0; dy < 2; dy++)
            for (int dx = 0; dx < 2; dx++)
                assertEquals(color, ui.pixels()[(y * 2 + dy) * ui.width() + x * 2 + dx]);
        int before = ui.pixels()[30 * ui.width() + 100];
        renderer.render(run, settings, true);
        assertNotEquals(before, ui.pixels()[30 * ui.width() + 100], "HUD oben links");
    }

    @Test
    void titleAndMapScenesRender() {
        var renderer = new WorldRenderer(PixelFont.load(), new SpriteBank());
        var settings = Settings.DEFAULT;
        for (int i = 0; i < 30; i++) {
            renderer.update(1.0 / 30, null, settings);
            renderer.renderTitle(settings, true);
        }
        var run = new GameRun(RunSetup.standard(1, DiverClass.SPARK));
        assertDoesNotThrow(() -> renderer.renderMap(run));
        assertNotEquals(renderer.frame().pixels()[0], renderer.frame().pixels()[480 * 150 + 240]);
    }
}
