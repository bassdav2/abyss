package ch.zhaw.abyss.ui.render;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.DiverClass;
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
