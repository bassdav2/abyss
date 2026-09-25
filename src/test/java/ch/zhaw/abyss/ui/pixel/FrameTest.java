package ch.zhaw.abyss.ui.pixel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Pixelarithmetik des Software-Framebuffers, der Lichtkarte und der Schrift. */
class FrameTest {
    @Test
    void blendingMixesColorsAndOpaquePixelsReplace() {
        assertEquals(0xFF808080, Frame.blend(0xFF000000, 0xFFFFFFFF, 128));
        var frame = new Frame(4, 4);
        frame.clear(0xFF000000);
        frame.pixel(1, 1, 0xFFFF0000);
        assertEquals(0xFFFF0000, frame.pixels()[5]);
        frame.pixel(2, 2, 0x80FFFFFF);
        int c = frame.pixels()[10];
        assertTrue((c & 0xFF) > 100 && (c & 0xFF) < 140);
    }

    @Test
    void drawingIsClippedToTheFrameAndClipRectangle() {
        var frame = new Frame(8, 8);
        frame.clear(0xFF000000);
        assertDoesNotThrow(() -> frame.fill(-5, -5, 30, 30, 0xFF112233));
        frame.clear(0xFF000000);
        frame.clip(2, 2, 2, 2);
        frame.fill(0, 0, 8, 8, 0xFFFFFFFF);
        int white = 0;
        for (int p : frame.pixels()) if (p == 0xFFFFFFFF) white++;
        assertEquals(4, white);
    }

    @Test
    void spritesAreAnchoredAndMirrored() {
        var sprite = new Sprite(new int[] {0xFFFF0000, 0, 0, 0xFF00FF00}, 2, 2, 0, 1);
        var frame = new Frame(4, 4);
        frame.clear(0xFF000000);
        frame.draw(sprite, 1, 2, false);
        assertEquals(0xFFFF0000, frame.pixels()[1 * 4 + 1]);
        assertEquals(0xFF00FF00, frame.pixels()[2 * 4 + 2]);
        frame.clear(0xFF000000);
        frame.draw(sprite, 1, 2, true);
        assertEquals(0xFFFF0000, frame.pixels()[1 * 4 + 1]);
        assertEquals(0xFF00FF00, frame.pixels()[2 * 4]);
    }

    @Test
    void lightMapDarkensAndBrightensInSteps() {
        var frame = new Frame(16, 16);
        frame.clear(0xFFC0C0C0);
        var lights = new LightMap(16, 16, 2);
        lights.ambient(0xFFFFFFFF, .2);
        lights.point(4, 4, 6, 0xFFFFFFFF, 1);
        lights.apply(frame, null, 0);
        int lit = frame.pixels()[4 * 16 + 4] & 0xFF, dark = frame.pixels()[15 * 16 + 15] & 0xFF;
        assertTrue(lit > dark * 2, "Licht hellt auf");
        assertTrue(dark < 0xC0 / 2, "Grundlicht dunkelt ab");
    }

    @Test
    void fontMeasuresAndWrapsText() {
        var font = PixelFont.load();
        assertEquals(0, font.width(""));
        assertTrue(font.width("ABYSS", 2) == font.width("ABYSS") * 2);
        assertTrue(font.width("Brücke") > 0);
        var lines = font.wrap("Ein Boot. Vierundzwanzig Räume. Dein nächster Versuch.", 80);
        assertTrue(lines.size() > 1);
        lines.forEach(line -> assertTrue(font.width(line) <= 80 || !line.contains(" ")));
    }
}
