package ch.zhaw.abyss.ui.gui;

import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * Bordsystem-Oberfläche im Pixelbild: Menüs, Karten und Regler werden in denselben 480 × 270 großen
 * Framebuffer gezeichnet wie die Spielwelt, damit sie wie ein Teil des U-Boots wirken.
 *
 * <p>Arbeitet im Immediate-Mode: Jeder Bildschirm ruft pro Bild die Bedienelemente auf; diese
 * zeichnen sich und melden zurück, ob sie ausgelöst wurden. Maus und Tastatur werden gesammelt und
 * einmal pro Bild ausgewertet. Pfeiltasten wandern räumlich zum nächsten Element in der gedrückten
 * Richtung.
 */
public final class Gui {
    /** Farbfamilien für Tasten, Rahmen und Hervorhebungen. */
    public enum Tone {
        /** Graues Metall für normale Tasten. */
        STEEL(0xFF151B22, 0xFF26303A, 0xFF3E4A56, Pal.STEEL_6, Pal.BONE),
        /** Messing für die Hauptaktion. */
        AMBER(Pal.RUST_2, Pal.RUST_4, Pal.RUST_5, Pal.RUST_6, Pal.INK),
        /** Bordcomputer-Türkis. */
        TEAL(Pal.TEAL_0, Pal.TEAL_1, Pal.TEAL_2, Pal.TEAL_5, Pal.TEAL_6),
        /** Warnrot für riskante Aktionen. */
        RED(Pal.RED_0, Pal.RED_1, Pal.RED_2, Pal.RED_4, Pal.BONE),
        /** Violett der Druckkapelle und der Resonanzen. */
        VIOLET(Pal.VIOLET_0, Pal.VIOLET_1, Pal.VIOLET_2, Pal.VIOLET_4, Pal.VIOLET_5),
        /** Zurückhaltende Textaktion ohne Tastenkörper. */
        QUIET(0x00000000, 0x00000000, 0x00000000, Pal.STEEL_6, Pal.STEEL_6);
        final int dark, base, light, glow, label;

        Tone(int dark, int base, int light, int glow, int label) {
            this.dark = dark;
            this.base = base;
            this.light = light;
            this.glow = glow;
            this.label = label;
        }

        /**
         * @return Leuchtfarbe der Familie
         */
        public int glow() {
            return glow;
        }
    }

    /** Richtungen der Tastaturnavigation. */
    public enum Nav {
        /** Nach oben. */
        UP,
        /** Nach unten. */
        DOWN,
        /** Nach links. */
        LEFT,
        /** Nach rechts. */
        RIGHT
    }

    private record Target(String id, int x, int y, int w, int h) {
        double cx() {
            return x + w / 2.0;
        }

        double cy() {
            return y + h / 2.0;
        }
    }

    /** Textfarben der Oberfläche. */
    public static final int TEXT = Pal.BONE, MUTED = 0xFF8FA3B0, DIM = 0xFF55646F;

    /** Zeilenhöhe der Pixelschrift. */
    public static final int LINE = PixelFont.LINE;

    private static final String[] CURSOR = {
        "X.......",
        "XX......",
        "XoX.....",
        "XooX....",
        "XoooX...",
        "XooooX..",
        "XoooooX.",
        "XooXXXX.",
        "XoX.....",
        "XX......",
        "X......."
    };

    private final PixelFont font;
    private Frame f;
    private double time;
    private int mouseX = -100, mouseY = -100;
    private boolean mouseMovedPending, mouseDown, clickPending, confirmPending, backspacePending;
    private Nav navPending;
    private final StringBuilder typedPending = new StringBuilder();
    private boolean mouseMoved, click, confirm, backspace, navUsed, fired, cursor;
    private Nav nav;
    private String typed = "";
    private String focus, preferred;
    private final List<Target> targets = new ArrayList<>();
    private Runnable onConfirm = () -> {};

    /**
     * @param font Pixelschrift
     */
    public Gui(PixelFont font) {
        this.font = font;
    }

    // --- Eingabe, vom JavaFX-Thread gesammelt ----------------------------------------------------

    /**
     * @param x Mausposition in Bildpixeln
     * @param y Mausposition in Bildpixeln
     */
    public void mouse(double x, double y) {
        int nx = (int) Math.floor(x), ny = (int) Math.floor(y);
        if (nx != mouseX || ny != mouseY) mouseMovedPending = true;
        mouseX = nx;
        mouseY = ny;
    }

    /**
     * @param down {@code true} beim Drücken der linken Maustaste
     */
    public void mouseButton(boolean down) {
        if (down && !mouseDown) clickPending = true;
        mouseDown = down;
    }

    /**
     * @param direction gedrückte Richtungstaste
     */
    public void nav(Nav direction) {
        navPending = direction;
    }

    /** Bestätigen, etwa mit Eingabe, Leertaste oder E. */
    public void confirm() {
        confirmPending = true;
    }

    /** Löscht das letzte Zeichen im fokussierten Eingabefeld. */
    public void backspace() {
        backspacePending = true;
    }

    /**
     * @param text getippte Zeichen
     */
    public void type(String text) {
        if (text != null) typedPending.append(text);
    }

    /**
     * @param action Rückmeldung beim Auslösen eines Elements, etwa ein Klickgeräusch
     */
    public void onConfirm(Runnable action) {
        onConfirm = action;
    }

    /**
     * @param visible eigenen Pixel-Mauszeiger zeichnen
     */
    public void cursor(boolean visible) {
        cursor = visible;
    }

    /** Vergisst den Fokus, etwa beim Wechsel des Bildschirms. */
    public void reset() {
        focus = null;
        preferred = null;
        targets.clear();
        clickPending = false;
        confirmPending = false;
        navPending = null;
        typedPending.setLength(0);
    }

    // --- Bildablauf ------------------------------------------------------------------------------

    /**
     * Beginnt ein Bild und übernimmt die gesammelte Eingabe.
     *
     * @param frame Ziel
     * @param dt Sekunden seit dem letzten Bild
     */
    public void begin(Frame frame, double dt) {
        f = frame;
        time += dt;
        mouseMoved = mouseMovedPending;
        click = clickPending;
        confirm = confirmPending;
        backspace = backspacePending;
        nav = navPending;
        typed = typedPending.toString();
        mouseMovedPending = clickPending = confirmPending = backspacePending = false;
        navPending = null;
        typedPending.setLength(0);
        navUsed = false;
        fired = false;
        targets.clear();
    }

    /** Beendet das Bild: Fokus nachführen, Navigation anwenden, Mauszeiger zeichnen. */
    public void end() {
        if (!targets.isEmpty() && find(focus) == null)
            focus = find(preferred) != null ? preferred : targets.getFirst().id();
        if (nav != null && !navUsed) move(nav);
        if (cursor && mouseX >= 0 && mouseY >= 0 && mouseX < f.width() && mouseY < f.height())
            drawCursor(mouseX, mouseY);
    }

    /**
     * @param id Element, das beim Öffnen eines Bildschirms den Fokus erhält
     */
    public void prefer(String id) {
        preferred = id;
    }

    /**
     * @param id Element
     * @return {@code true}, wenn es den Fokus hat
     */
    public boolean focused(String id) {
        return id.equals(focus);
    }

    /**
     * @param id Element, das den Fokus erhalten soll
     */
    public void focus(String id) {
        focus = id;
    }

    /**
     * @return Sekunden seit Start, für Animationen
     */
    public double time() {
        return time;
    }

    /**
     * @return Zielbild des aktuellen Bildes
     */
    public Frame frame() {
        return f;
    }

    /**
     * @return Pixelschrift
     */
    public PixelFont font() {
        return font;
    }

    /**
     * Registriert eine Fläche, die Fokus erhalten kann.
     *
     * @param id eindeutiger Name im Bildschirm
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     */
    public void target(String id, int x, int y, int w, int h) {
        targets.add(new Target(id, x, y, w, h));
        if (mouseMoved && inside(x, y, w, h)) focus = id;
    }

    /**
     * Registriert eine Fläche und prüft, ob sie ausgelöst wurde.
     *
     * @return {@code true} bei Klick oder Bestätigung mit Fokus
     */
    private boolean activated(String id, int x, int y, int w, int h, boolean enabled) {
        target(id, x, y, w, h);
        if (!enabled || fired) return false;
        boolean hit = click && inside(x, y, w, h) || confirm && id.equals(focus);
        if (hit) {
            fired = true;
            click = false;
            confirm = false;
            focus = id;
            onConfirm.run();
        }
        return hit;
    }

    /**
     * Fragt eine waagrechte Eingabe für das fokussierte Element ab und verbraucht sie.
     *
     * @param id Element
     * @return -1, 0 oder 1
     */
    public int horizontal(String id) {
        if (!id.equals(focus) || nav == null || navUsed) return 0;
        if (nav == Nav.LEFT || nav == Nav.RIGHT) {
            navUsed = true;
            return nav == Nav.LEFT ? -1 : 1;
        }
        return 0;
    }

    private boolean inside(int x, int y, int w, int h) {
        return mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
    }

    private Target find(String id) {
        if (id == null) return null;
        for (var t : targets) if (t.id().equals(id)) return t;
        return null;
    }

    private void move(Nav direction) {
        var current = find(focus);
        if (current == null) return;
        Target best = null;
        double bestScore = Double.MAX_VALUE;
        for (var t : targets) {
            if (t == current) continue;
            double primary, gap, drift;
            switch (direction) {
                case UP -> {
                    if (t.cy() >= current.cy() - 1) continue;
                    primary = current.y() - (t.y() + t.h());
                    gap = Math.abs(t.cx() - current.cx()) - (t.w() + current.w()) / 2.0;
                    drift = Math.abs(t.cx() - current.cx());
                }
                case DOWN -> {
                    if (t.cy() <= current.cy() + 1) continue;
                    primary = t.y() - (current.y() + current.h());
                    gap = Math.abs(t.cx() - current.cx()) - (t.w() + current.w()) / 2.0;
                    drift = Math.abs(t.cx() - current.cx());
                }
                case LEFT -> {
                    if (t.cx() >= current.cx() - 1) continue;
                    primary = current.x() - (t.x() + t.w());
                    gap = Math.abs(t.cy() - current.cy()) - (t.h() + current.h()) / 2.0;
                    drift = Math.abs(t.cy() - current.cy());
                }
                default -> {
                    if (t.cx() <= current.cx() + 1) continue;
                    primary = t.x() - (current.x() + current.w());
                    gap = Math.abs(t.cy() - current.cy()) - (t.h() + current.h()) / 2.0;
                    drift = Math.abs(t.cy() - current.cy());
                }
            }
            double score = Math.max(0, primary) + 3 * Math.max(0, gap) + .1 * drift;
            if (score < bestScore) {
                bestScore = score;
                best = t;
            }
        }
        if (best != null) focus = best.id();
    }

    // --- Text ------------------------------------------------------------------------------------

    /**
     * @param text Text
     * @return Breite in Pixeln
     */
    public int width(String text) {
        return font.width(text, 1);
    }

    /**
     * Zeichnet Text mit Schatten.
     *
     * @param x linke Kante
     * @param y Oberkante
     * @param text Text
     * @param color Farbe
     */
    public void text(int x, int y, String text, int color) {
        font.drawShadow(f, text, x, y, color, 0xC0000000, 1);
    }

    /**
     * Zeichnet grossen Text mit Kontur.
     *
     * @param x linke Kante
     * @param y Oberkante
     * @param text Text
     * @param color Farbe
     * @param scale Vergrösserung
     */
    public void big(int x, int y, String text, int color, int scale) {
        font.drawOutlined(f, text, x, y, color, Pal.OUTLINE, scale);
    }

    /**
     * Zeichnet zentrierten Text.
     *
     * @param cx Mitte
     * @param y Oberkante
     * @param text Text
     * @param color Farbe
     */
    public void center(int cx, int y, String text, int color) {
        text(cx - width(text) / 2, y, text, color);
    }

    /**
     * Zeichnet rechtsbündigen Text.
     *
     * @param right rechte Kante
     * @param y Oberkante
     * @param text Text
     * @param color Farbe
     */
    public void right(int right, int y, String text, int color) {
        text(right - width(text), y, text, color);
    }

    /**
     * Bricht Text um und zeichnet höchstens {@code maxLines} Zeilen.
     *
     * @param x linke Kante
     * @param y Oberkante
     * @param w maximale Breite
     * @param text Text
     * @param color Farbe
     * @param maxLines höchstens so viele Zeilen; überzählige enden mit Auslassungspunkten
     * @return benutzte Höhe in Pixeln
     */
    public int wrap(int x, int y, int w, String text, int color, int maxLines) {
        var lines = font.wrap(text, w);
        int count = Math.min(lines.size(), maxLines);
        for (int i = 0; i < count; i++) {
            String line = lines.get(i);
            if (i == count - 1 && lines.size() > count) {
                while (!line.isEmpty() && width(line + "…") > w)
                    line = line.substring(0, line.length() - 1);
                line = line + "…";
            }
            text(x, y + i * LINE, line, color);
        }
        return count * LINE;
    }

    /**
     * Zeichnet ein Symbol vergrössert.
     *
     * @param sprite Symbol
     * @param x linke Kante
     * @param y obere Kante
     * @param scale Vergrösserung
     */
    public void icon(Sprite sprite, int x, int y, int scale) {
        f.drawScaled(sprite, x, y, scale, 1);
    }

    // --- Flächen ---------------------------------------------------------------------------------

    /**
     * Schottplatte mit Fase und Nieten.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     */
    public void panel(int x, int y, int w, int h) {
        f.fill(x, y, w, h, 0xF40D141B);
        f.rect(x, y, w, h, Pal.OUTLINE);
        f.fill(x + 1, y + 1, w - 2, 1, 0xFF3A4654);
        f.fill(x + 1, y + 2, 1, h - 3, 0xFF232D38);
        f.fill(x + 1, y + h - 2, w - 2, 1, 0xFF05080C);
        f.fill(x + w - 2, y + 2, 1, h - 3, 0xFF05080C);
        rivet(x + 3, y + 3);
        rivet(x + w - 5, y + 3);
        rivet(x + 3, y + h - 5);
        rivet(x + w - 5, y + h - 5);
    }

    private void rivet(int x, int y) {
        f.pixel(x, y, 0xFF6E7A85);
        f.pixel(x + 1, y, 0xFF3E4A56);
        f.pixel(x, y + 1, 0xFF3E4A56);
        f.pixel(x + 1, y + 1, 0xFF151B22);
    }

    /**
     * Eingelassener Bildschirm mit Zeilenraster.
     *
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param tone Leuchtfarbe
     */
    public void screen(int x, int y, int w, int h, Tone tone) {
        f.fill(x, y, w, h, 0xF6040A0E);
        f.fill(x, y, w, 1, 0xFF000000);
        f.fill(x, y, 1, h, 0xFF000000);
        f.fill(x, y + h - 1, w, 1, 0xFF26323C);
        f.fill(x + w - 1, y, 1, h, 0xFF26323C);
        for (int yy = y + 2; yy < y + h - 1; yy += 2)
            f.fill(x + 1, yy, w - 2, 1, Frame.alpha(tone.glow, .05));
    }

    /**
     * Kopfzeile eines Bildschirms mit Warnstreifen, Kennung und Titel.
     *
     * @param kicker kleine Kennung, etwa „ARCHIV“
     * @param title Titel
     * @param tone Farbe der Kennung
     */
    public void header(String kicker, String title, Tone tone) {
        f.fill(0, 0, f.width(), 30, 0xF0070B10);
        f.fill(0, 30, f.width(), 1, 0xFF2A3440);
        f.fill(0, 31, f.width(), 1, 0xFF000000);
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 30; y++) if (((x + y) / 4) % 2 == 0) f.pixel(x, y, 0xFF3A2A10);
        text(22, 5, kicker, tone.glow);
        big(22, 15, title, TEXT, 1);
        f.fill(22 + width(kicker) + 4, 8, 30, 1, Frame.alpha(tone.glow, .5));
    }

    // --- Bedienelemente --------------------------------------------------------------------------

    /**
     * Taste mit Tastenkörper, die beim Drücken einsinkt.
     *
     * @param id eindeutiger Name im Bildschirm
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param label Beschriftung
     * @param tone Farbfamilie
     * @param enabled auslösbar
     * @return {@code true}, wenn ausgelöst
     */
    public boolean button(
            String id, int x, int y, int w, int h, String label, Tone tone, boolean enabled) {
        boolean hit = activated(id, x, y, w, h, enabled);
        boolean focused = focused(id);
        boolean pressed = enabled && mouseDown && inside(x, y, w, h);
        if (tone == Tone.QUIET) {
            int color = !enabled ? DIM : focused ? TEXT : MUTED;
            center(x + w / 2, y + (h - 7) / 2, label, color);
            if (focused) {
                int lw = width(label) + 6;
                f.fill(x + w / 2 - lw / 2, y + h - 2, lw, 1, Frame.alpha(Pal.RUST_6, .8));
            }
            return hit;
        }
        var t = enabled ? tone : Tone.STEEL;
        int sink = pressed ? 1 : 0;
        f.fill(x, y + 1, w, h, Pal.OUTLINE);
        f.fill(x + 1, y + h - 1, w - 2, 1, 0xFF000000);
        int top = y + sink;
        f.fill(x + 1, top, w - 2, h - 2, enabled ? t.base : 0xFF151B22);
        f.fill(x + 1, top, w - 2, 1, enabled ? t.light : 0xFF232B33);
        f.fill(x + 1, top + h - 3, w - 2, 1, enabled ? t.dark : 0xFF0C1015);
        f.rect(x, top - 1, w, h, Pal.OUTLINE);
        if (focused && enabled) {
            double pulse = .55 + .45 * Math.sin(time * 6);
            f.rect(x - 1, top - 2, w + 2, h + 2, Frame.alpha(t.glow, pulse));
            f.fill(x + 1, top, w - 2, 1, Pal.mix(t.light, Pal.WHITE, .35));
            chevron(x - 5, top + h / 2 - 3, t.glow, false);
            chevron(x + w + 2, top + h / 2 - 3, t.glow, true);
        }
        int color = enabled ? t.label : 0xFF4A5560;
        if (focused && enabled && tone == Tone.STEEL) color = Pal.RUST_7;
        int ly = top + (h - 2 - 7) / 2;
        font.draw(f, label, x + w / 2 - width(label) / 2, ly, color, 1);
        return hit;
    }

    private void chevron(int x, int y, int color, boolean left) {
        for (int i = 0; i < 3; i++) {
            int len = 3 - Math.abs(i - 1) * 2 + 1;
            f.fill(left ? x + (3 - len) : x, y + i + (i > 1 ? 1 : 0), len, 1, color);
        }
        f.fill(left ? x : x + 2, y + 1, 1, 3, color);
    }

    /**
     * Hologramm-Karte als auswählbare Fläche. Den Inhalt zeichnet der Aufrufer.
     *
     * @param id eindeutiger Name im Bildschirm
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @param accent Rahmenfarbe, etwa nach Seltenheit
     * @param enabled auslösbar
     * @return {@code true}, wenn ausgelöst
     */
    public boolean card(String id, int x, int y, int w, int h, int accent, boolean enabled) {
        boolean hit = activated(id, x, y, w, h, enabled);
        boolean focused = focused(id);
        f.fill(x, y, w, h, 0xE8061018);
        for (int yy = y + 1; yy < y + h - 1; yy += 2)
            f.fill(x + 1, yy, w - 2, 1, Frame.alpha(accent, .035));
        f.rect(x, y, w, h, Frame.alpha(accent, focused ? .95 : .45));
        f.fill(x + 1, y + 1, w - 2, 10, Frame.alpha(accent, focused ? .28 : .16));
        int arm = 5 + (focused ? (int) Math.round(1 + Math.sin(time * 5)) : 0);
        int bracket = focused ? Pal.mix(accent, Pal.WHITE, .4) : Frame.alpha(accent, .8);
        int o = focused ? -2 : 0;
        corner(x + o, y + o, arm, bracket, 1, 1);
        corner(x + w - 1 - o, y + o, arm, bracket, -1, 1);
        corner(x + o, y + h - 1 - o, arm, bracket, 1, -1);
        corner(x + w - 1 - o, y + h - 1 - o, arm, bracket, -1, -1);
        if (focused) {
            int sweep = (int) ((time * 90) % (w + h)) - h;
            for (int i = 0; i < h - 2; i++) {
                int sx = x + 1 + sweep + i / 2;
                if (sx >= x + 1 && sx < x + w - 3) f.addRect(sx, y + h - 2 - i, 3, 1, accent, .10);
            }
        }
        if (!enabled) f.fill(x + 1, y + 1, w - 2, h - 2, 0x70040608);
        return hit;
    }

    private void corner(int x, int y, int arm, int color, int dx, int dy) {
        for (int i = 0; i < arm; i++) {
            f.pixel(x + i * dx, y, color);
            f.pixel(x, y + i * dy, color);
        }
    }

    /**
     * Waagrechter Wahlschalter mit Pfeilen links und rechts.
     *
     * @param id eindeutiger Name im Bildschirm
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param h Höhe
     * @return -1, 0 oder 1
     */
    public int stepper(String id, int x, int y, int w, int h) {
        target(id, x, y, w, h);
        int delta = horizontal(id);
        boolean focused = focused(id);
        if (!fired && click && inside(x, y, 14, h)) delta = -1;
        if (!fired && click && inside(x + w - 14, y, 14, h)) delta = 1;
        if (click && (inside(x, y, 14, h) || inside(x + w - 14, y, 14, h))) {
            fired = true;
            click = false;
        }
        int color = focused ? Pal.RUST_6 : Pal.STEEL_5;
        arrow(x + 4, y + h / 2 - 3, color, true);
        arrow(x + w - 8, y + h / 2 - 3, color, false);
        if (focused) f.rect(x, y, w, h, Frame.alpha(Pal.RUST_6, .45 + .3 * Math.sin(time * 6)));
        if (delta != 0) onConfirm.run();
        return delta;
    }

    private void arrow(int x, int y, int color, boolean left) {
        for (int i = 0; i < 4; i++) {
            int len = i < 2 ? i + 1 : 4 - i;
            f.fill(left ? x + 4 - len : x, y + i + (i > 1 ? 1 : 0), len, 1, color);
        }
        f.fill(left ? x + 1 : x, y + 2, 3, 1, color);
    }

    /**
     * Pegel-Schieberegler aus zehn Segmenten.
     *
     * @param id eindeutiger Name im Bildschirm
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param value Wert 0 bis 1
     * @return neuer Wert
     */
    public double slider(String id, int x, int y, int w, double value) {
        target(id, x - 2, y - 2, w + 4, 11);
        double next = value + horizontal(id) * .1;
        if (mouseDown && inside(x - 2, y - 3, w + 4, 13) && !fired)
            next = (mouseX - x) / (double) (w - 1);
        next = Math.max(0, Math.min(1, Math.round(next * 20) / 20.0));
        boolean focused = focused(id);
        int segments = 10, seg = (w - (segments - 1)) / segments;
        for (int i = 0; i < segments; i++) {
            boolean on = next > i / (double) segments + .001;
            int c = on ? Pal.mix(Pal.TEAL_4, Pal.RUST_5, i / (double) segments) : 0xFF1A232C;
            f.fill(x + i * (seg + 1), y, seg, 7, c);
            if (on) f.fill(x + i * (seg + 1), y, seg, 1, Pal.mix(c, Pal.WHITE, .4));
        }
        if (focused) f.rect(x - 2, y - 2, w + 3, 11, Frame.alpha(Pal.RUST_6, .7));
        right(x + w + 26, y, Math.round(next * 100) + " %", focused ? TEXT : MUTED);
        return next;
    }

    /**
     * Kippschalter mit Beschriftung.
     *
     * @param id eindeutiger Name im Bildschirm
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite der gesamten Zeile
     * @param label Beschriftung
     * @param value aktueller Zustand
     * @return neuer Zustand
     */
    public boolean toggle(String id, int x, int y, int w, String label, boolean value) {
        boolean hit = activated(id, x - 2, y - 2, w, 12, true);
        boolean next = hit != value;
        boolean focused = focused(id);
        f.fill(x, y, 15, 8, Pal.OUTLINE);
        f.fill(x + 1, y + 1, 13, 6, next ? Pal.TEAL_1 : 0xFF151B22);
        int knob = next ? x + 8 : x + 1;
        f.fill(knob, y + 1, 6, 6, next ? Pal.TEAL_5 : Pal.STEEL_4);
        f.fill(knob, y + 1, 6, 1, next ? Pal.TEAL_6 : Pal.STEEL_6);
        text(x + 21, y, label, focused ? TEXT : MUTED);
        if (focused) f.rect(x - 2, y - 2, w, 12, Frame.alpha(Pal.RUST_6, .6));
        return next;
    }

    /**
     * Eingabefeld für Ziffern.
     *
     * @param id eindeutiger Name im Bildschirm
     * @param x linke Kante
     * @param y obere Kante
     * @param w Breite
     * @param value aktueller Inhalt
     * @param placeholder Hinweis bei leerem Feld
     * @return neuer Inhalt
     */
    public String field(String id, int x, int y, int w, String value, String placeholder) {
        target(id, x, y, w, 13);
        if (click && inside(x, y, w, 13)) {
            focus = id;
            click = false;
        }
        boolean focused = focused(id);
        String next = value;
        if (focused) {
            for (char c : typed.toCharArray())
                if (Character.isDigit(c) && next.length() < 12) next = next + c;
            if (backspace && !next.isEmpty()) next = next.substring(0, next.length() - 1);
        }
        screen(x, y, w, 13, Tone.TEAL);
        if (focused) f.rect(x - 1, y - 1, w + 2, 15, Frame.alpha(Pal.RUST_6, .7));
        if (next.isEmpty()) text(x + 4, y + 3, placeholder, DIM);
        else text(x + 4, y + 3, next, Pal.TEAL_6);
        if (focused && ((int) (time * 2)) % 2 == 0)
            f.fill(x + 5 + width(next), y + 3, 4, 7, Pal.TEAL_5);
        return next;
    }

    private void drawCursor(int x, int y) {
        for (int row = 0; row < CURSOR.length; row++)
            for (int col = 0; col < CURSOR[row].length(); col++) {
                char c = CURSOR[row].charAt(col);
                if (c == 'X') f.pixel(x + col, y + row, Pal.OUTLINE);
                else if (c == 'o') f.pixel(x + col, y + row, row < 3 ? Pal.WHITE : Pal.BONE);
            }
    }
}
