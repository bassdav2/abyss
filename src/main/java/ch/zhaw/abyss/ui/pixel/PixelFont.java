package ch.zhaw.abyss.ui.pixel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Eigene Bitmap-Schrift mit variabler Zeichenbreite für HUD und Effekte in Pixelauflösung. Die
 * Glyphen werden aus einer Textressource gelesen ({@code #} ist gesetzt).
 */
public final class PixelFont {
    /** Zeilenhöhe in Pixeln bei Skalierung 1. */
    public static final int LINE = 11;

    private record Glyph(boolean[] bits, int width, int height) {}

    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    /**
     * Lädt eine Schrift.
     *
     * @param stream UTF-8-Ressource
     * @throws IOException bei Lesefehlern
     */
    public PixelFont(InputStream stream) throws IOException {
        try (var reader =
                new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            Integer current = null;
            var rows = new ArrayList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") && current == null) continue;
                if (line.startsWith("@") && line.length() > 1) {
                    if (current != null) glyphs.put(current, parse(rows));
                    current = line.codePointAt(1);
                    rows.clear();
                } else if (current != null && !line.isEmpty()) rows.add(line);
            }
            if (current != null) glyphs.put(current, parse(rows));
        }
        glyphs.put((int) ' ', new Glyph(new boolean[0], 3, 0));
    }

    /**
     * @return Standardschrift aus den Ressourcen
     */
    public static PixelFont load() {
        try (var stream = PixelFont.class.getResourceAsStream("/pixel/font.txt")) {
            if (stream == null) throw new IllegalStateException("Pixelschrift fehlt");
            return new PixelFont(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Pixelschrift nicht lesbar", e);
        }
    }

    private static Glyph parse(List<String> rows) {
        int width = rows.stream().mapToInt(String::length).max().orElse(1);
        var bits = new boolean[width * rows.size()];
        for (int y = 0; y < rows.size(); y++)
            for (int x = 0; x < rows.get(y).length(); x++)
                bits[y * width + x] = rows.get(y).charAt(x) == '#';
        return new Glyph(bits, width, rows.size());
    }

    private Glyph glyph(int codePoint) {
        var glyph = glyphs.get(codePoint);
        if (glyph == null) glyph = glyphs.get(Character.toUpperCase(codePoint));
        if (glyph == null) glyph = glyphs.get(substitute(codePoint));
        return glyph == null ? glyphs.get((int) '?') : glyph;
    }

    /** Ersatz für typografische Zeichen ohne eigene Glyphe. */
    private static int substitute(int codePoint) {
        return switch (codePoint) {
            case 0x2212, 0x2010, 0x2011 -> '-';
            case 0x2014 -> 0x2013;
            case 0x2019, 0x2018, 0x201A -> '\'';
            case 0x00BB, 0x203A -> '>';
            case 0x00AB, 0x2039 -> '<';
            case 0x00A0 -> ' ';
            default -> codePoint;
        };
    }

    /**
     * @param text Text
     * @param scale ganzzahlige Vergrösserung
     * @return Breite in Pixeln
     */
    public int width(String text, int scale) {
        int width = 0;
        int count = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            width += glyph(cp).width + 1;
            count++;
            i += Character.charCount(cp);
        }
        return Math.max(0, (width - (count > 0 ? 1 : 0)) * scale);
    }

    /**
     * @param text Text
     * @return Breite bei Skalierung 1
     */
    public int width(String text) {
        return width(text, 1);
    }

    /**
     * Zeichnet Text; y ist die Oberkante der Versalien.
     *
     * @param frame Ziel
     * @param text Text
     * @param x linke Kante
     * @param y Oberkante
     * @param argb Farbe
     * @param scale Vergrösserung
     */
    public void draw(Frame frame, String text, int x, int y, int argb, int scale) {
        render(
                text,
                x,
                y,
                scale,
                (px, py) -> {
                    if (scale == 1) frame.pixel(px, py, argb);
                    else frame.fill(px, py, scale, scale, argb);
                });
    }

    /** Empfänger gesetzter Glyphenpixel. */
    @FunctionalInterface
    public interface PixelSink {
        /**
         * @param x linke Kante des Pixels
         * @param y obere Kante des Pixels
         */
        void set(int x, int y);
    }

    /**
     * Liefert alle gesetzten Pixel eines Textes an einen Empfänger, etwa zum Einmalen in Grafiken.
     *
     * @param text Text
     * @param x linke Kante
     * @param y Oberkante
     * @param scale Vergrösserung
     * @param sink Empfänger; erhält die linke obere Ecke jedes vergrösserten Pixels
     */
    public void render(String text, int x, int y, int scale, PixelSink sink) {
        int cursor = x;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            var glyph = glyph(cp);
            for (int gy = 0; gy < glyph.height; gy++)
                for (int gx = 0; gx < glyph.width; gx++)
                    if (glyph.bits[gy * glyph.width + gx])
                        sink.set(cursor + gx * scale, y + gy * scale);
            cursor += (glyph.width + 1) * scale;
            i += Character.charCount(cp);
        }
    }

    /**
     * Zeichnet Text mit dunklem Schatten unten rechts für Lesbarkeit auf jedem Hintergrund.
     *
     * @param frame Ziel
     * @param text Text
     * @param x linke Kante
     * @param y Oberkante
     * @param argb Farbe
     * @param shadow Schattenfarbe
     * @param scale Vergrösserung
     */
    public void drawShadow(
            Frame frame, String text, int x, int y, int argb, int shadow, int scale) {
        draw(frame, text, x + scale, y + scale, shadow, scale);
        draw(frame, text, x, y, argb, scale);
    }

    /**
     * Zeichnet zentrierten Text mit Schatten.
     *
     * @param frame Ziel
     * @param text Text
     * @param cx horizontale Mitte
     * @param y Oberkante
     * @param argb Farbe
     * @param shadow Schattenfarbe
     * @param scale Vergrösserung
     */
    public void drawCentered(
            Frame frame, String text, int cx, int y, int argb, int shadow, int scale) {
        drawShadow(frame, text, cx - width(text, scale) / 2, y, argb, shadow, scale);
    }

    /**
     * Zeichnet Text mit einem Pixel dunkler Kontur rundherum.
     *
     * @param frame Ziel
     * @param text Text
     * @param x linke Kante
     * @param y Oberkante
     * @param argb Farbe
     * @param outline Konturfarbe
     * @param scale Vergrösserung
     */
    public void drawOutlined(
            Frame frame, String text, int x, int y, int argb, int outline, int scale) {
        for (int oy = -1; oy <= 1; oy++)
            for (int ox = -1; ox <= 1; ox++)
                if (ox != 0 || oy != 0)
                    draw(frame, text, x + ox * scale, y + oy * scale, outline, scale);
        draw(frame, text, x, y, argb, scale);
    }

    /**
     * Bricht Text an Wortgrenzen um.
     *
     * @param text Text, Zeilenumbrüche werden übernommen
     * @param maxWidth maximale Breite in Pixeln
     * @return Zeilen
     */
    public List<String> wrap(String text, int maxWidth) {
        var lines = new ArrayList<String>();
        for (String paragraph : text.split("\n")) {
            var line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (width(candidate) > maxWidth && !line.isEmpty()) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else line = new StringBuilder(candidate);
            }
            lines.add(line.toString());
        }
        return lines;
    }
}
