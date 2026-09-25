package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.IconArt;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.PixelFont;

import java.util.ArrayList;
import java.util.List;

/**
 * Pixel-HUD: Integrität, Energie, Schild, Reparatursets, Ressourcen, Waffe, Modul, Ausweichen,
 * Module, Bossleiste sowie Meldungen, Banner, Raumkarten und Boss-Auftritte. Zeichnet nach der
 * Nachbearbeitung, damit die Anzeige immer scharf und unbeleuchtet bleibt.
 */
public final class HudRenderer {
    private static final int TEXT = Pal.BONE, MUTED = 0xFF8FA6B2, SHADOW = Pal.OUTLINE;
    private final PixelFont font;
    private final List<Message> toasts = new ArrayList<>();
    private Message banner, card;
    private double toastY = 36;
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

    /**
     * @param dt Sekunden
     */
    public void update(double dt) {
        time += dt;
        scrapBump = Math.max(0, scrapBump - dt);
        for (var t : toasts) t.age += dt;
        toasts.removeIf(t -> t.age > t.max);
        if (banner != null && (banner.age += dt) > banner.max) banner = null;
        if (card != null && (card.age += dt) > card.max) card = null;
        if (bossName != null && (bossTime += dt) > 2.8) bossName = null;
        double target = card != null && bossName == null ? 102 : 36;
        toastY += (target - toastY) * Math.min(1, dt * 10);
    }

    /**
     * Zeichnet das HUD.
     *
     * @param f Ziel
     * @param run Tauchgang
     * @param clock Zeit für Animationen
     */
    public void draw(Frame f, GameRun run, double clock) {
        var p = run.player();
        // Integrität mit nachlaufender Schadensanzeige
        if (shownHealth < 0 || shownHealth < p.health()) shownHealth = p.health();
        shownHealth += (p.health() - shownHealth) * .08;
        panel(f, 4, 4, 122, 27);
        f.draw(IconArt.misc("heart"), 13, 12, false);
        bar(
                f,
                22,
                7,
                92,
                7,
                p.health() / p.maxHealth(),
                shownHealth / p.maxHealth(),
                p.health() < p.maxHealth() * .3 ? Pal.RED_4 : Pal.RED_3,
                10 / p.maxHealth());
        if (p.stats().maxShield() > 0) {
            int w = (int) Math.round(92 * Math.min(1, p.shield() / Math.max(1, p.maxHealth())));
            f.fill(22, 7, w, 2, Pal.TEAL_5);
        }
        String health = Math.round(p.health()) + "/" + Math.round(p.maxHealth());
        font.drawOutlined(f, health, 68 - font.width(health) / 2, 7, TEXT, SHADOW, 1);
        f.draw(IconArt.misc("energy"), 13, 22, false);
        bar(
                f,
                22,
                18,
                60,
                3,
                p.energy() / p.maxEnergy(),
                p.energy() / p.maxEnergy(),
                Pal.TEAL_4,
                0);
        font.drawShadow(f, "" + Math.round(p.energy()), 86, 16, Pal.TEAL_5, SHADOW, 1);
        for (int i = 0; i < p.stats().maxRepairKits(); i++) {
            int x = 22 + i * 8, y = 23;
            if (i < p.repairKits()) {
                f.fill(x, y, 6, 5, Pal.RED_2);
                f.fill(x + 2, y + 1, 2, 3, Pal.BONE);
                f.fill(x + 1, y + 2, 4, 1, Pal.BONE);
            } else f.rect(x, y, 6, 5, 0xFF3A4450);
        }
        font.drawShadow(f, "Q", 22 + p.stats().maxRepairKits() * 8 + 1, 22, MUTED, SHADOW, 1);

        // Ressourcen oben rechts
        panel(f, 382, 4, 94, 27);
        String depth = String.format("%02d", run.room().depth() + 1);
        font.drawShadow(f, depth, 388, 7, TEXT, SHADOW, 2);
        font.drawShadow(f, "/" + RoomGenerator.ROOM_COUNT, 412, 13, MUTED, SHADOW, 1);
        String cycle = "Z" + (run.cycle() + 1) + (run.pressure() > 0 ? " D" + run.pressure() : "");
        font.drawShadow(f, cycle, 388, 22, run.pressure() > 0 ? Pal.RED_4 : MUTED, SHADOW, 1);
        f.draw(IconArt.misc("scrap"), 441, 11, false);
        font.drawShadow(
                f,
                "" + p.salvage(),
                450,
                8 - (scrapBump > 0 ? 1 : 0),
                scrapBump > 0 ? Pal.RUST_7 : Pal.RUST_6,
                SHADOW,
                1);
        f.draw(IconArt.misc("core"), 441, 24, false);
        font.drawShadow(f, "" + p.cores(), 450, 21, Pal.TEAL_5, SHADOW, 1);

        // Wellenanzeige oder Countdown des Hüllenbruchs
        double breach = run.breachRemaining();
        if (breach > 0) {
            boolean blink = breach < 10 && ((int) (time * 4)) % 2 == 0;
            String text = String.format("HÜLLENBRUCH  00:%02d", (int) Math.ceil(breach));
            font.drawOutlined(
                    f,
                    text,
                    240 - font.width(text) / 2,
                    5,
                    blink ? Pal.WHITE : Pal.RED_4,
                    Pal.OUTLINE,
                    1);
            int w = 100, filled = (int) Math.round(w * breach / GameRun.BREACH_TIME);
            f.fill(190, 15, w, 3, Pal.OUTLINE);
            f.fill(191, 16, filled - 2, 1, Pal.RED_4);
        } else if (run.room().waveCount() > 1 && run.phase() == GameRun.Phase.RUNNING) {
            String wave = "WELLE " + (run.wave() + 1) + "/" + run.room().waveCount();
            font.drawCentered(f, wave, 240, 6, MUTED, SHADOW, 1);
        }

        // Waffe, Modul und Ausweichen unten links
        int by = 238;
        slot(f, 6, by, IconArt.weapon(p.weapon()), "J", 0, TEXT);
        for (int i = 0; i < Weapon.MAX_LEVEL; i++)
            f.fill(8 + i * 5, by + 21, 4, 2, i < p.weaponLevel() ? Pal.RUST_6 : 0xFF3A4450);
        double cooldown = p.abilityCooldown() / Math.max(.01, p.abilityCooldownTotal());
        boolean ready = cooldown <= 0 && p.energy() >= p.module().cost();
        slot(f, 30, by, IconArt.module(p.module()), "K", cooldown, ready ? Pal.TEAL_5 : MUTED);
        if (!ready && cooldown <= 0) f.fill(31, by + 21, 20, 2, Pal.RED_3);
        slot(f, 54, by, IconArt.misc("dash"), "SH", p.dashCooldown() / .9, TEXT);
        font.drawShadow(f, p.weapon().title(), 82, by + 3, TEXT, SHADOW, 1);
        font.drawShadow(
                f,
                p.module().title() + " · " + p.module().cost() + "E",
                82,
                by + 13,
                ready ? Pal.TEAL_5 : MUTED,
                SHADOW,
                1);

        // Module unten rechts
        var items = new ArrayList<Item>();
        for (var item : Item.values()) if (p.stacks(item) > 0) items.add(item);
        int perRow = 10, shown = Math.min(items.size(), 20);
        for (int i = 0; i < shown; i++) {
            var item = items.get(i);
            int col = i % perRow, row = i / perRow;
            int x = 474 - (perRow - col) * 17,
                    y = shown > perRow ? 237 + row * 17 - 17 * (row == 0 ? 0 : 0) : 254;
            if (shown > perRow) y = 237 + row * 17;
            f.fill(x - 1, y - 1, 16, 16, 0xC00A0F18);
            f.rect(x - 1, y - 1, 16, 16, Frame.alpha(IconArt.rarityColor(item.rarity()), .6));
            f.draw(IconArt.item(item), x + 7, y + 7, false);
            if (p.stacks(item) > 1)
                font.drawOutlined(f, "" + p.stacks(item), x + 10, y + 8, TEXT, SHADOW, 1);
        }
        if (items.size() > shown)
            font.drawShadow(f, "+" + (items.size() - shown), 300, 250, MUTED, SHADOW, 1);

        drawHints(f, run);
        drawBoss(f, run);
        drawMessages(f);
        drawBossIntro(f);
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
        int y = 200;
        for (String line : lines) {
            int w = font.width(line) + 10;
            f.fill(240 - w / 2, y - 2, w, 11, 0x9005080E);
            font.drawCentered(f, line, 240, y, TEXT, SHADOW, 1);
            y += 12;
        }
    }

    private void drawBoss(Frame f, GameRun run) {
        var boss = run.boss();
        if (boss == null
                || boss.state() == ch.zhaw.abyss.domain.Enemy.State.SPAWNING && bossName != null)
            return;
        int w = 240, x = 120, y = 222;
        font.drawCentered(
                f,
                boss.kind().title().toUpperCase(),
                240,
                y - 11,
                boss.enraged() ? Pal.RED_4 : Pal.RUST_6,
                SHADOW,
                1);
        f.fill(x - 2, y - 2, w + 4, 9, Pal.OUTLINE);
        f.fill(x, y, w, 5, Pal.RED_0);
        int filled = (int) Math.round(w * boss.healthRatio());
        f.fill(x, y, filled, 5, boss.armored() ? Pal.RED_3 : Pal.RUST_6);
        f.fill(x, y, filled, 1, boss.armored() ? Pal.RED_4 : Pal.RUST_7);
        for (int k = 1; k < 3; k++) f.fill(x + w * k / 3, y, 1, 5, Pal.OUTLINE);
        String state = boss.armored() ? "PANZERUNG AKTIV" : "KERN OFFEN · JETZT ANGREIFEN";
        if (!boss.armored() || ((int) (time * 2)) % 2 == 0)
            font.drawCentered(f, state, 240, y + 7, boss.armored() ? MUTED : Pal.RUST_7, SHADOW, 1);
    }

    /**
     * Zeichnet nur Meldungen, etwa über Menühintergründen.
     *
     * @param f Ziel
     */
    public void drawMessagesOnly(Frame f) {
        drawToasts(f, 36);
    }

    /**
     * Zeichnet die neuesten Meldungen kompakt rechts in der Kopfzeile der Bordsysteme, damit sie
     * keine Karten oder Tasten verdecken.
     *
     * @param f Ziel
     */
    public void drawMessagesCompact(Frame f) {
        int shown = 0;
        for (int i = toasts.size() - 1; i >= 0 && shown < 2; i--, shown++) {
            var t = toasts.get(i);
            double a = Math.min(1, Math.min(t.age * 6, (t.max - t.age) * 2));
            String text = t.text;
            while (font.width(text) > 250 && text.length() > 4)
                text = text.substring(0, text.length() - 2);
            if (!text.equals(t.text)) text = text + "…";
            int w = font.width(text) + 10, y = 3 + shown * 13;
            f.fill(476 - w, y - 1, w, 11, Frame.alpha(0xFF050A12, .92 * a));
            f.fill(476 - w, y - 1, 1, 11, Frame.alpha(t.color, a));
            font.draw(f, text, 471 - font.width(text), y + 1, Frame.alpha(t.color, a), 1);
        }
    }

    private void drawMessages(Frame f) {
        boolean cardShown = card != null && bossName == null;
        drawToasts(f, (int) Math.round(toastY));
        if (cardShown) {
            double a = Math.min(1, Math.min(card.age * 3, (card.max - card.age) * 1.5));
            int slide = (int) Math.round(Math.max(0, .3 - card.age) * 60);
            font.drawCentered(
                    f, card.sub, 240, 62 - slide, Frame.alpha(MUTED, a), Frame.alpha(SHADOW, a), 1);
            font.drawCentered(
                    f,
                    card.text.toUpperCase(),
                    240,
                    74 - slide,
                    Frame.alpha(TEXT, a),
                    Frame.alpha(SHADOW, a),
                    2);
            int lw = (int) (Math.min(1, card.age * 2) * 120);
            f.fill(240 - lw, 94 - slide, lw * 2, 1, Frame.alpha(Pal.RUST_5, a * .8));
        }
        if (banner != null) {
            double a = Math.min(1, Math.min(banner.age * 5, (banner.max - banner.age) * 2));
            int scale = 3;
            int w = font.width(banner.text, scale);
            int grow = (int) (Math.min(1, banner.age * 4) * (w / 2 + 30));
            f.fill(240 - grow, 104, grow * 2, 34, Frame.alpha(0xFF03060C, .7 * a));
            f.fill(240 - grow, 104, grow * 2, 1, Frame.alpha(banner.color, a));
            f.fill(240 - grow, 137, grow * 2, 1, Frame.alpha(banner.color, a));
            font.drawOutlined(
                    f,
                    banner.text,
                    240 - w / 2,
                    108,
                    Frame.alpha(banner.color, a),
                    Frame.alpha(SHADOW, a),
                    scale);
            if (!banner.sub.isEmpty())
                font.drawCentered(
                        f, banner.sub, 240, 142, Frame.alpha(TEXT, a), Frame.alpha(SHADOW, a), 1);
        }
    }

    private void drawToasts(Frame f, int top) {
        int y = top;
        for (var t : toasts) {
            double a = Math.min(1, Math.min(t.age * 6, (t.max - t.age) * 2));
            int w = font.width(t.text) + 12;
            f.fill(240 - w / 2, y - 3, w, 13, Frame.alpha(0xFF050A12, .78 * a));
            f.fill(240 - w / 2, y - 3, 1, 13, Frame.alpha(t.color, a));
            f.fill(240 + w / 2 - 1, y - 3, 1, 13, Frame.alpha(t.color, a));
            font.drawCentered(
                    f, t.text, 240, y, Frame.alpha(t.color, a), Frame.alpha(SHADOW, a), 1);
            y += 15;
        }
    }

    private void drawBossIntro(Frame f) {
        if (bossName == null) return;
        double t = bossTime;
        double bars = Math.min(1, t * 3) * Math.min(1, (2.8 - t) * 3);
        int h = (int) Math.round(35 * bars);
        f.fill(0, 0, 480, h, Pal.INK);
        f.fill(0, 270 - h, 480, h, Pal.INK);
        double a = Math.min(1, Math.max(0, (t - .3) * 3)) * Math.min(1, (2.8 - t) * 2);
        if (a <= 0) return;
        int w = font.width(bossName.toUpperCase(), 3);
        int x = 240 - w / 2 + (int) Math.round(Math.max(0, .6 - t) * 80);
        font.drawOutlined(
                f,
                bossName.toUpperCase(),
                x,
                56,
                Frame.alpha(Pal.RUST_6, a),
                Frame.alpha(SHADOW, a),
                3);
        font.drawCentered(
                f, bossSubtitle, 240, 84, Frame.alpha(TEXT, a), Frame.alpha(SHADOW, a), 1);
        f.fill(240 - w / 2, 51, w, 1, Frame.alpha(Pal.RED_3, a));
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
        f.fill(x - 6, y, 12, 12, Pal.OUTLINE);
        f.fill(x - 5, y + 1, 10, 10, 0xFF1E2A36);
        f.fill(x - 5, y + 1, 10, 1, 0xFF3E5262);
        font.draw(f, key, x - font.width(key) / 2, y + 3, TEXT, 1);
        font.drawCentered(f, label, x, y + 15, Pal.TEAL_5, SHADOW, 1);
    }

    private static void panel(Frame f, int x, int y, int w, int h) {
        f.fill(x, y, w, h, 0xB0060B14);
        f.fill(x, y, w, 1, 0x803E5262);
        f.fill(x, y + h - 1, w, 1, 0x80000000);
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
        f.fill(x, y, valueW, 1, Pal.shade(color, 1.35));
        if (h > 3) f.fill(x, y + h - 1, valueW, 1, Pal.shade(color, .7));
        if (tick > 0)
            for (double t = tick; t < 1; t += tick)
                f.fill(x + (int) Math.round(w * t), y + 1, 1, h - 1, Frame.alpha(Pal.OUTLINE, .5));
    }

    private void slot(
            Frame f,
            int x,
            int y,
            ch.zhaw.abyss.ui.pixel.Sprite icon,
            String key,
            double cooldown,
            int border) {
        f.fill(x, y, 22, 22, 0xD0080D16);
        f.rect(x, y, 22, 22, Frame.alpha(border, .8));
        f.draw(icon, x + 11, y + 11, false);
        if (cooldown > 0) {
            int h = (int) Math.round(20 * Math.min(1, cooldown));
            f.fill(x + 1, y + 1, 20, h, 0xB0060A10);
        }
        font.drawOutlined(f, key, x + 21 - font.width(key), y + 14, MUTED, SHADOW, 1);
    }
}
