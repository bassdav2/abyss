package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Threat;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.IconArt;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * HUD auf der Oberflächenebene in doppelter Auflösung (960 × 540): Integrität, Energie, Schild,
 * Reparatursets und Überladung oben links, Raum und Ressourcen oben rechts, eine einzige
 * Statuszeile oben in der Mitte, Waffe und Module unten, die Bossleiste unten in der Mitte sowie
 * Meldungen, Banner, Raumkarten und Boss-Auftritte. Zeichnet nach der Nachbearbeitung der Welt,
 * damit die Anzeige immer scharf und unbeleuchtet bleibt.
 */
public final class HudRenderer {
    /** Breite der Oberflächenebene. */
    public static final int W = WorldRenderer.UW;

    /** Höhe der Oberflächenebene. */
    public static final int H = WorldRenderer.UH;

    private static final int TEXT = Pal.BONE, MUTED = 0xFF8FA6B2, DIM = 0xFF55646F;
    private static final int SHADOW = Pal.OUTLINE;

    /** Module, die unten rechts höchstens sichtbar sind; der Rest steht unter Tab. */
    static final int MODULE_COLUMNS = 14, MODULE_ROWS = 2;

    private final PixelFont font;
    private final List<Message> toasts = new ArrayList<>();
    private Message banner, card;
    private double toastY = 46;
    private String bossName, bossSubtitle;
    private double bossTime, scrapBump, time;
    private double shownHealth = -1;

    private static final class Message {
        final String text, sub;
        final int color;
        final double max;
        double age;

        Message(String text, String sub, int color, double max) {
            this.text = text;
            this.sub = sub;
            this.color = color;
            this.max = max;
        }
    }

    /**
     * @param font Pixelschrift
     */
    public HudRenderer(PixelFont font) {
        this.font = font;
    }

    /**
     * Kurze Meldung oben in der Mitte.
     *
     * @param text Text
     * @param color Farbe
     */
    public void toast(String text, int color) {
        if (text == null || text.isBlank()) return;
        for (var t : toasts)
            if (t.text.equals(text)) {
                t.age = 0;
                return;
            }
        toasts.add(new Message(text, "", color, 2.6));
        if (toasts.size() > 3) toasts.removeFirst();
    }

    /**
     * Grosses Banner in der Bildmitte.
     *
     * @param text Titel
     * @param sub Untertitel
     * @param color Farbe
     * @param seconds Dauer
     */
    public void banner(String text, String sub, int color, double seconds) {
        banner = new Message(text, sub, color, seconds);
    }

    /**
     * Raumkarte beim Betreten.
     *
     * @param room Raum
     */
    public void roomCard(RoomPlan room) {
        banner = null;
        bossName = null;
        card =
                new Message(
                        room.title(),
                        room.sectorName()
                                + " · "
                                + room.typeName().toUpperCase()
                                + " · "
                                + String.format(
                                        "%02d/%d", room.depth() + 1, RoomGenerator.ROOM_COUNT),
                        0,
                        2.8);
        var condition = room.condition();
        if (condition != RoomCondition.NONE)
            toast(
                    condition.title().toUpperCase() + " · " + condition.description(),
                    switch (condition) {
                        case ALARM -> Pal.RED_4;
                        case LEAK -> Pal.TEAL_5;
                        default -> Pal.RUST_6;
                    });
    }

    /**
     * Filmischer Boss-Auftritt mit Balken.
     *
     * @param name Name
     * @param subtitle Untertitel
     */
    public void bossIntro(String name, String subtitle) {
        bossName = name;
        bossSubtitle = subtitle;
        bossTime = 0;
        card = null;
        banner = null;
        toasts.clear();
    }

    /**
     * Meldet eingesammelte Beute für den Zähler.
     *
     * @param kind Beuteart
     * @param value Wert
     */
    public void pickup(String kind, double value) {
        if ("SCRAP".equals(kind)) scrapBump = .25;
    }

    /** Entfernt alle Meldungen. */
    public void clear() {
        toasts.clear();
        banner = null;
        card = null;
        bossName = null;
    }

    /** Entfernt nur die kurzen Meldungen, etwa beim Wechsel in ein Menü ausserhalb des Spiels. */
    public void clearToasts() {
        toasts.clear();
    }

    /**
     * @param dt Sekunden
     */
    public void update(double dt) {
        time += dt;
        scrapBump = Math.max(0, scrapBump - dt);
        for (var t : toasts) t.age += dt;
        toasts.removeIf(t -> t.age > t.max);
        // Ein Banner wartet, bis die Raumkarte verschwunden ist, damit sich nichts überlagert.
        boolean cardShown = card != null && bossName == null;
        if (banner != null && !cardShown && (banner.age += dt) > banner.max) banner = null;
        if (card != null && (card.age += dt) > card.max) card = null;
        if (bossName != null && (bossTime += dt) > 2.8) bossName = null;
        double target = card != null && bossName == null ? 186 : 46;
        toastY += (target - toastY) * Math.min(1, dt * 10);
    }

    /**
     * Zeichnet das HUD auf die Oberflächenebene.
     *
     * @param f Ziel in 960 × 540
     * @param run Tauchgang
     * @param clock Zeit für Animationen
     */
    public void draw(Frame f, GameRun run, double clock) {
        vitals(f, run);
        resources(f, run);
        status(f, run);
        loadout(f, run);
        modules(f, run);
        drawHints(f, run);
        drawBoss(f, run);
        drawMessages(f);
        drawBossIntro(f);
    }

    // --- Oben links: Integrität, Energie, Sets, Überladung ---------------------------------------

    private void vitals(Frame f, GameRun run) {
        var p = run.player();
        // Integrität mit nachlaufender Schadensanzeige
        if (shownHealth < 0 || shownHealth < p.health()) shownHealth = p.health();
        shownHealth += (p.health() - shownHealth) * .08;
        panel(f, 8, 8, 300, 70);
        icon(f, IconArt.misc("heart"), 24, 22);
        bar(
                f,
                42,
                16,
                252,
                12,
                p.health() / p.maxHealth(),
                shownHealth / p.maxHealth(),
                p.health() < p.maxHealth() * .3 ? Pal.RED_4 : Pal.RED_3,
                10 / p.maxHealth());
        if (p.stats().maxShield() > 0) {
            int w = (int) Math.round(252 * Math.min(1, p.shield() / Math.max(1, p.maxHealth())));
            f.fill(42, 16, w, 3, Pal.TEAL_5);
        }
        String health = Math.round(p.health()) + " / " + Math.round(p.maxHealth());
        font.drawOutlined(f, health, 168 - font.width(health) / 2, 19, TEXT, SHADOW, 1);
        icon(f, IconArt.misc("energy"), 24, 41);
        bar(
                f,
                42,
                38,
                170,
                6,
                p.energy() / p.maxEnergy(),
                p.energy() / p.maxEnergy(),
                Pal.TEAL_4,
                0);
        font.drawShadow(f, Math.round(p.energy()) + " E", 220, 38, Pal.TEAL_5, SHADOW, 1);
        // Reparatursets als kleine Kreuze, daneben die Taste
        int kits = p.stats().maxRepairKits();
        for (int i = 0; i < kits; i++) {
            int x = 252 + (i % 4) * 11, y = 36 + (i / 4) * 10;
            if (i < p.repairKits()) {
                f.fill(x, y, 9, 8, Pal.RED_2);
                f.fill(x + 3, y + 1, 3, 6, Pal.BONE);
                f.fill(x + 1, y + 3, 7, 2, Pal.BONE);
            } else f.rect(x, y, 9, 8, 0xFF3A4450);
        }
        // Überladung: violette Leiste zur nächsten Stufe, Stufe als grosse Zahl
        double need = GameRun.xpToNext(p.level());
        boolean pending = run.pendingLevelUps() > 0 && ((int) (time * 6)) % 2 == 0;
        String level = "" + p.level();
        font.drawOutlined(
                f,
                level,
                24 - font.width(level, 2) / 2,
                56,
                pending ? Pal.WHITE : Pal.VIOLET_5,
                SHADOW,
                2);
        f.fill(42, 60, 252, 8, Pal.OUTLINE);
        int xpWidth = (int) Math.round(250 * Math.min(1, p.xp() / need));
        f.fill(43, 61, xpWidth, 6, Pal.VIOLET_3);
        f.fill(43, 61, xpWidth, 2, Pal.VIOLET_5);
        font.drawShadow(f, "ÜBERLADUNG", 46, 50, Frame.alpha(Pal.VIOLET_4, .8), SHADOW, 1);
    }

    // --- Oben rechts: Raum, Zyklus, Eskalation, Schrott, Kerne -----------------------------------

    private void resources(Frame f, GameRun run) {
        var p = run.player();
        int x = W - 8 - 216;
        panel(f, x, 8, 216, 62);
        String depth = String.format("%02d", run.room().depth() + 1);
        font.drawShadow(f, depth, x + 10, 14, TEXT, SHADOW, 3);
        font.drawShadow(
                f,
                "/" + RoomGenerator.ROOM_COUNT,
                x + 14 + font.width(depth, 3),
                28,
                MUTED,
                SHADOW,
                1);
        String cycle =
                "ZYKLUS " + (run.cycle() + 1) + (run.pressure() > 0 ? " · D" + run.pressure() : "");
        font.drawShadow(f, cycle, x + 10, 42, run.pressure() > 0 ? Pal.RED_4 : MUTED, SHADOW, 1);
        if (run.escalation() > 0)
            font.drawShadow(
                    f,
                    "ESKALATION " + run.escalation(),
                    x + 10,
                    54,
                    Pal.prism(time * .2, .45),
                    SHADOW,
                    1);
        icon(f, IconArt.misc("scrap"), x + 136, 24);
        font.drawShadow(
                f,
                "" + p.salvage(),
                x + 150,
                17 - (scrapBump > 0 ? 1 : 0),
                scrapBump > 0 ? Pal.RUST_7 : Pal.RUST_6,
                SHADOW,
                2);
        icon(f, IconArt.misc("core"), x + 136, 50);
        font.drawShadow(f, "" + p.cores(), x + 150, 43, Pal.TEAL_5, SHADOW, 2);
    }

    // --- Oben Mitte: eine Statuszeile und die Bedrohung ------------------------------------------

    private void status(Frame f, GameRun run) {
        int cx = W / 2;
        double breach = run.breachRemaining();
        if (breach > 0) {
            boolean blink = breach < 10 && ((int) (time * 4)) % 2 == 0;
            String text = String.format("HÜLLENBRUCH  00:%02d", (int) Math.ceil(breach));
            font.drawOutlined(
                    f,
                    text,
                    cx - font.width(text, 2) / 2,
                    10,
                    blink ? Pal.WHITE : Pal.RED_4,
                    Pal.OUTLINE,
                    2);
            int w = 200, filled = (int) Math.round(w * breach / GameRun.BREACH_TIME);
            f.fill(cx - w / 2, 30, w, 4, Pal.OUTLINE);
            f.fill(cx - w / 2 + 1, 31, Math.max(0, filled - 2), 2, Pal.RED_4);
        }
        if (run.phase() != GameRun.Phase.RUNNING) return;
        int swarm = run.hordeRemaining();
        for (var e : run.enemies()) if (e.alive() && e.kind().swarm()) swarm++;
        var parts = new ArrayList<String>();
        if (run.room().waveCount() > 1)
            parts.add("WELLE " + (run.wave() + 1) + "/" + run.room().waveCount());
        if (swarm > 0) parts.add("SCHWARM " + swarm);
        int y = breach > 0 ? 40 : 12;
        if (!parts.isEmpty()) {
            String line = String.join("   ·   ", parts);
            int w = font.width(line) + 20;
            f.fill(cx - w / 2, y - 4, w, 15, 0x9005080E);
            font.drawCentered(f, line, cx, y, swarm > 0 ? Pal.RED_4 : MUTED, SHADOW, 1);
            y += 16;
        }
        var threat = run.room().threat();
        if (threat != Threat.NONE) {
            String text = "BEDROHUNG · " + threat.title().toUpperCase();
            int w = font.width(text) + 24;
            int color = ((int) (time * 2)) % 2 == 0 ? Pal.RED_4 : Pal.RUST_6;
            f.fill(cx - w / 2, y - 3, w, 13, 0xC0200608);
            f.rect(cx - w / 2, y - 3, w, 13, Frame.alpha(color, .8));
            font.drawCentered(f, text, cx, y, color, SHADOW, 1);
        }
    }

    // --- Unten links: Waffe, Modul, Ausweichen ---------------------------------------------------

    private void loadout(Frame f, GameRun run) {
        var p = run.player();
        int y = H - 8 - 42;
        slot(f, 8, y, IconArt.weapon(p.weapon()), "J", 0, TEXT);
        for (int i = 0; i < Weapon.MAX_LEVEL; i++)
            f.fill(10 + i * 5, y + 44, 3, 3, i < p.weaponLevel() ? Pal.RUST_6 : 0xFF3A4450);
        double cooldown = p.abilityCooldown() / Math.max(.01, p.abilityCooldownTotal());
        boolean ready = cooldown <= 0 && p.energy() >= p.module().cost();
        slot(f, 56, y, IconArt.module(p.module()), "K", cooldown, ready ? Pal.TEAL_5 : MUTED);
        if (!ready && cooldown <= 0) f.fill(58, y + 44, 38, 3, Pal.RED_3);
        slot(f, 104, y, IconArt.misc("dash"), "SH", p.dashCooldown() / .9, TEXT);
        font.drawShadow(f, p.weapon().title(), 156, y + 6, TEXT, SHADOW, 1);
        font.drawShadow(
                f,
                "Stufe " + p.weaponLevel() + "/" + Weapon.MAX_LEVEL,
                156,
                y + 18,
                Pal.RUST_6,
                SHADOW,
                1);
        font.drawShadow(
                f,
                p.module().title() + " · " + p.module().cost() + " E",
                156,
                y + 30,
                ready ? Pal.TEAL_5 : MUTED,
                SHADOW,
                1);
    }

    // --- Unten rechts: Entfesselungen zuerst, dann die stärksten Module --------------------------

    private void modules(Frame f, GameRun run) {
        var p = run.player();
        var items = new ArrayList<Item>();
        for (var item : Item.values()) if (p.stacks(item) > 0) items.add(item);
        items.sort(
                Comparator.comparing((Item item) -> !item.evolution())
                        .thenComparing(item -> -item.rarity().ordinal())
                        .thenComparing(item -> -p.stacks(item)));
        int capacity = MODULE_COLUMNS * MODULE_ROWS;
        int shown = Math.min(items.size(), items.size() > capacity ? capacity - 1 : capacity);
        int cell = 20;
        int right = W - 8;
        for (int i = 0; i < shown; i++) {
            var item = items.get(i);
            int col = i % MODULE_COLUMNS, row = i / MODULE_COLUMNS;
            int x = right - (MODULE_COLUMNS - col) * cell;
            int y = H - 8 - (MODULE_ROWS - row) * cell;
            if (items.size() <= MODULE_COLUMNS) y = H - 8 - cell;
            int border =
                    item.evolution()
                            ? Pal.prism(time * .3 + i * .1, .5)
                            : IconArt.rarityColor(item.rarity());
            f.fill(x, y, cell - 2, cell - 2, 0xC00A0F18);
            f.rect(x, y, cell - 2, cell - 2, Frame.alpha(border, .7));
            f.draw(IconArt.item(item), x + 9, y + 9, false);
            if (p.stacks(item) > 1) {
                String n = "" + p.stacks(item);
                font.drawOutlined(f, n, x + 17 - font.width(n), y + 10, TEXT, SHADOW, 1);
            }
        }
        if (items.size() > shown) {
            String more = "+" + (items.size() - shown);
            int x = right - cell, y = H - 8 - cell;
            f.fill(x, y, cell - 2, cell - 2, 0xC00A0F18);
            f.rect(x, y, cell - 2, cell - 2, Frame.alpha(MUTED, .6));
            font.drawCentered(f, more, x + 9, y + 6, MUTED, SHADOW, 1);
            font.drawShadow(
                    f,
                    "TAB · ALLE MODULE",
                    right - font.width("TAB · ALLE MODULE"),
                    H - 8 - MODULE_ROWS * cell - 12,
                    DIM,
                    SHADOW,
                    1);
        }
    }

    private void drawHints(Frame f, GameRun run) {
        if (run.room().depth() != 0 || run.cycle() != 0) return;
        String[] lines;
        if (run.phase() == GameRun.Phase.RUNNING && run.roomTime() < 16)
            lines =
                    new String[] {
                        "A/D BEWEGEN  ·  LEERTASTE SPRINGEN  ·  J ANGREIFEN",
                        "SHIFT AUSWEICHEN  ·  K MODUL  ·  ROTE MARKIERUNGEN = ANGRIFF"
                    };
        else if (run.phase() == GameRun.Phase.ROOM_CLEARED)
            lines =
                    run.rewardAvailable()
                            ? new String[] {
                                "E AN DER BERGUNGSKAPSEL: MODUL WÄHLEN",
                                "DANACH RECHTS DURCHS SCHOTT"
                            }
                            : new String[] {"RECHTS DURCHS SCHOTT: E ÖFFNET DIE ROUTENWAHL"};
        else return;
        int y = 392;
        for (String line : lines) {
            int w = font.width(line, 2) + 20;
            f.fill(W / 2 - w / 2, y - 4, w, 22, 0x9005080E);
            font.drawCentered(f, line, W / 2, y, TEXT, SHADOW, 2);
            y += 24;
        }
    }

    private void drawBoss(Frame f, GameRun run) {
        var boss = run.boss();
        if (boss == null
                || boss.state() == ch.zhaw.abyss.domain.Enemy.State.SPAWNING && bossName != null)
            return;
        int w = 480, x = W / 2 - w / 2, y = H - 74;
        font.drawCentered(
                f,
                boss.kind().title().toUpperCase(),
                W / 2,
                y - 20,
                boss.enraged() ? Pal.RED_4 : Pal.RUST_6,
                SHADOW,
                2);
        f.fill(x - 2, y - 2, w + 4, 14, Pal.OUTLINE);
        f.fill(x, y, w, 10, Pal.RED_0);
        int filled = (int) Math.round(w * boss.healthRatio());
        f.fill(x, y, filled, 10, boss.armored() ? Pal.RED_3 : Pal.RUST_6);
        f.fill(x, y, filled, 2, boss.armored() ? Pal.RED_4 : Pal.RUST_7);
        for (int k = 1; k < 3; k++) f.fill(x + w * k / 3, y, 1, 10, Pal.OUTLINE);
        String state = boss.armored() ? "PANZERUNG AKTIV" : "KERN OFFEN · JETZT ANGREIFEN";
        if (!boss.armored() || ((int) (time * 2)) % 2 == 0)
            font.drawCentered(
                    f, state, W / 2, y + 15, boss.armored() ? MUTED : Pal.RUST_7, SHADOW, 1);
    }

    // --- Meldungen -------------------------------------------------------------------------------

    /**
     * Zeichnet nur Meldungen, etwa über Menühintergründen.
     *
     * @param f Ziel
     */
    public void drawMessagesOnly(Frame f) {
        drawToasts(f, 46);
    }

    /**
     * Zeichnet die neuesten Meldungen kompakt rechts in der Kopfzeile der Bordsysteme, damit sie
     * keine Karten oder Tasten verdecken.
     *
     * @param f Ziel
     */
    public void drawMessagesCompact(Frame f) {
        int shown = 0;
        // Nur die neueste Meldung in der oberen Zeile; darunter liegt die Kernanzeige.
        for (int i = toasts.size() - 1; i >= 0 && shown < 1; i--, shown++) {
            var t = toasts.get(i);
            double a = Math.min(1, Math.min(t.age * 6, (t.max - t.age) * 2));
            String text = t.text;
            while (font.width(text) > 480 && text.length() > 4)
                text = text.substring(0, text.length() - 2);
            if (!text.equals(t.text)) text = text + "…";
            int w = font.width(text) + 16, y = 6 + shown * 17;
            f.fill(W - 8 - w, y - 3, w, 14, Frame.alpha(0xFF050A12, .92 * a));
            f.fill(W - 8 - w, y - 3, 2, 14, Frame.alpha(t.color, a));
            font.draw(f, text, W - 16 - font.width(text), y, Frame.alpha(t.color, a), 1);
        }
    }

    private void drawMessages(Frame f) {
        boolean cardShown = card != null && bossName == null;
        drawToasts(f, (int) Math.round(toastY));
        if (cardShown) {
            double a = Math.min(1, Math.min(card.age * 3, (card.max - card.age) * 1.5));
            int slide = (int) Math.round(Math.max(0, .3 - card.age) * 120);
            font.drawCentered(
                    f,
                    card.sub,
                    W / 2,
                    112 - slide,
                    Frame.alpha(MUTED, a),
                    Frame.alpha(SHADOW, a),
                    1);
            font.drawOutlined(
                    f,
                    card.text.toUpperCase(),
                    W / 2 - font.width(card.text.toUpperCase(), 4) / 2,
                    128 - slide,
                    Frame.alpha(TEXT, a),
                    Frame.alpha(SHADOW, a),
                    4);
            int lw = (int) (Math.min(1, card.age * 2) * 220);
            f.fill(W / 2 - lw, 166 - slide, lw * 2, 1, Frame.alpha(Pal.RUST_5, a * .8));
        }
        if (banner != null && !cardShown) {
            double a = Math.min(1, Math.min(banner.age * 5, (banner.max - banner.age) * 2));
            int scale = font.width(banner.text, 4) > W - 80 ? 3 : 4;
            int w = font.width(banner.text, scale);
            int grow = (int) (Math.min(1, banner.age * 4) * (w / 2 + 50));
            int top = 210;
            f.fill(W / 2 - grow, top, grow * 2, 52, Frame.alpha(0xFF03060C, .72 * a));
            f.fill(W / 2 - grow, top, grow * 2, 2, Frame.alpha(banner.color, a));
            f.fill(W / 2 - grow, top + 50, grow * 2, 2, Frame.alpha(banner.color, a));
            font.drawOutlined(
                    f,
                    banner.text,
                    W / 2 - w / 2,
                    top + 26 - 7 * scale / 2 - 1,
                    Frame.alpha(banner.color, a),
                    Frame.alpha(SHADOW, a),
                    scale);
            if (!banner.sub.isEmpty()) {
                int sub = font.width(banner.sub, 2) > W - 60 ? 1 : 2;
                font.drawCentered(
                        f,
                        banner.sub,
                        W / 2,
                        top + 60,
                        Frame.alpha(TEXT, a),
                        Frame.alpha(SHADOW, a),
                        sub);
            }
        }
    }

    private void drawToasts(Frame f, int top) {
        int y = top;
        for (var t : toasts) {
            double a = Math.min(1, Math.min(t.age * 6, (t.max - t.age) * 2));
            int w = font.width(t.text) + 20;
            f.fill(W / 2 - w / 2, y - 4, w, 16, Frame.alpha(0xFF050A12, .8 * a));
            f.fill(W / 2 - w / 2, y - 4, 2, 16, Frame.alpha(t.color, a));
            f.fill(W / 2 + w / 2 - 2, y - 4, 2, 16, Frame.alpha(t.color, a));
            font.drawCentered(
                    f, t.text, W / 2, y, Frame.alpha(t.color, a), Frame.alpha(SHADOW, a), 1);
            y += 18;
        }
    }

    private void drawBossIntro(Frame f) {
        if (bossName == null) return;
        double t = bossTime;
        double bars = Math.min(1, t * 3) * Math.min(1, (2.8 - t) * 3);
        int h = (int) Math.round(70 * bars);
        f.fill(0, 0, W, h, Pal.INK);
        f.fill(0, H - h, W, h, Pal.INK);
        double a = Math.min(1, Math.max(0, (t - .3) * 3)) * Math.min(1, (2.8 - t) * 2);
        if (a <= 0) return;
        String name = bossName.toUpperCase();
        int scale = font.width(name, 6) > W - 60 ? 5 : 6;
        int w = font.width(name, scale);
        int x = W / 2 - w / 2 + (int) Math.round(Math.max(0, .6 - t) * 160);
        font.drawOutlined(
                f, name, x, 110, Frame.alpha(Pal.RUST_6, a), Frame.alpha(SHADOW, a), scale);
        font.drawCentered(
                f, bossSubtitle, W / 2, 168, Frame.alpha(TEXT, a), Frame.alpha(SHADOW, a), 2);
        f.fill(W / 2 - w / 2, 100, w, 2, Frame.alpha(Pal.RED_3, a));
    }

    /**
     * Tastenhinweis über einem Objekt.
     *
     * @param f Ziel
     * @param key Taste
     * @param label Beschriftung
     * @param x Mitte
     * @param y Oberkante
     */
    public void keyPrompt(Frame f, String key, String label, int x, int y) {
        int w = Math.max(22, font.width(key, 2) + 10);
        f.fill(x - w / 2, y, w, 22, Pal.OUTLINE);
        f.fill(x - w / 2 + 1, y + 1, w - 2, 20, 0xFF1E2A36);
        f.fill(x - w / 2 + 1, y + 1, w - 2, 2, 0xFF3E5262);
        font.draw(f, key, x - font.width(key, 2) / 2, y + 4, TEXT, 2);
        int lw = font.width(label) + 12;
        f.fill(x - lw / 2, y + 25, lw, 14, 0xA005080E);
        font.drawCentered(f, label, x, y + 28, Pal.TEAL_5, SHADOW, 1);
    }

    // --- Bausteine -------------------------------------------------------------------------------

    private static void icon(Frame f, Sprite sprite, int cx, int cy) {
        f.draw(sprite, cx, cy, false);
    }

    private static void panel(Frame f, int x, int y, int w, int h) {
        f.fill(x, y, w, h, 0xB8060B14);
        f.fill(x, y, w, 1, 0x903E5262);
        f.fill(x, y + h - 1, w, 1, 0x90000000);
        f.fill(x, y, 1, h, 0x50223040);
        f.fill(x + w - 1, y, 1, h, 0x50000000);
    }

    private static void bar(
            Frame f,
            int x,
            int y,
            int w,
            int h,
            double value,
            double trail,
            int color,
            double tick) {
        f.fill(x - 1, y - 1, w + 2, h + 2, Pal.OUTLINE);
        f.fill(x, y, w, h, 0xFF1A1418);
        int trailW = (int) Math.round(w * Math.max(0, Math.min(1, trail)));
        int valueW = (int) Math.round(w * Math.max(0, Math.min(1, value)));
        if (trailW > valueW) f.fill(x + valueW, y, trailW - valueW, h, Pal.BONE);
        f.fill(x, y, valueW, h, color);
        f.fill(x, y, valueW, 2, Pal.shade(color, 1.35));
        if (h > 4) f.fill(x, y + h - 2, valueW, 2, Pal.shade(color, .7));
        if (tick > 0 && tick * w >= 4)
            for (double t = tick; t < 1; t += tick)
                f.fill(x + (int) Math.round(w * t), y + 2, 1, h - 2, Frame.alpha(Pal.OUTLINE, .5));
    }

    private void slot(Frame f, int x, int y, Sprite icon, String key, double cooldown, int border) {
        f.fill(x, y, 42, 42, 0xD0080D16);
        f.rect(x, y, 42, 42, Frame.alpha(border, .8));
        f.drawScaled(icon, x + 5, y + 5, 2, 1);
        if (cooldown > 0) {
            int h = (int) Math.round(40 * Math.min(1, cooldown));
            f.fill(x + 1, y + 1, 40, h, 0xB0060A10);
        }
        font.drawOutlined(f, key, x + 40 - font.width(key), y + 32, MUTED, SHADOW, 1);
    }
}
