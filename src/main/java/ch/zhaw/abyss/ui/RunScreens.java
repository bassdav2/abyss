package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.Offer;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Synergy;
import ch.zhaw.abyss.domain.Threat;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.DiverArt;
import ch.zhaw.abyss.ui.art.IconArt;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.art.RoomArt;
import ch.zhaw.abyss.ui.gui.Gui;
import ch.zhaw.abyss.ui.gui.Gui.Tone;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.Sprite;
import ch.zhaw.abyss.ui.render.WorldRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bildschirme während eines Tauchgangs, gezeichnet über der angehaltenen Welt auf der
 * Oberflächenebene in 960 × 540: Pause, Bergung als Hologramm-Karten, Händler und Werkstatt,
 * Druckkapelle, Routenwahl mit Kamerabildern, Ausrüstung, Bootskarte und Ergebnis. Sie zeigen
 * Domänenwerte an und rufen nur öffentliche Domänenaktionen auf; Preise und Regeln prüft die
 * Domäne.
 */
final class RunScreens {
    private static final int W = WorldRenderer.UW, H = WorldRenderer.UH, CX = W / 2;

    /** Oberkante der Fusszeile mit Ressourcen und Haupttaste. */
    private static final int FOOT = H - 50;

    private final Navigator nav;

    RunScreens(Navigator nav) {
        this.nav = nav;
    }

    // --- Pause -----------------------------------------------------------------------------------

    void pause(Gui g) {
        var run = nav.run();
        var p = run.player();
        g.panel(CX - 150, 92, 300, 330);
        g.screen(CX - 134, 108, 268, 62, Tone.TEAL);
        g.bigCenter(CX, 118, "PAUSE", Gui.TEXT, 4);
        g.center(CX, 154, "DAS BOOT WARTET.", Pal.TEAL_5);
        String[] labels = {"WEITERSPIELEN", "AUSRÜSTUNG", "BOOTSKARTE", "OPTIONEN", "STEUERUNG"};
        Runnable[] actions = {
            nav::play, nav::inventory, nav::map, () -> nav.settings(true), () -> nav.help(true)
        };
        for (int i = 0; i < labels.length; i++)
            if (g.button(
                    "pause." + i,
                    CX - 120,
                    186 + i * 40,
                    240,
                    30,
                    labels[i],
                    i == 0 ? Tone.AMBER : Tone.STEEL,
                    true)) actions[i].run();
        if (g.button("menu", CX - 120, 390, 240, 20, "ZUM HAUPTMENÜ", Tone.QUIET, true))
            nav.title();
        g.prefer("pause.0");
        // Überblick über den laufenden Tauchgang
        int x = CX + 170;
        g.panel(x, 92, 290, 330);
        g.text(x + 16, 106, "TAUCHGANG", Pal.TEAL_5);
        String[][] facts = {
            {"RAUM", String.format("%02d/%d", run.room().depth() + 1, RoomGenerator.ROOM_COUNT)},
            {"ZYKLUS", "" + (run.cycle() + 1)},
            {"DRUCKSTUFE", "" + run.pressure()},
            {"ESKALATION", run.escalation() > 0 ? "" + run.escalation() : "–"},
            {"ÜBERLADUNG", "STUFE " + p.level()},
            {"GEGNER BESIEGT", "" + run.kills()},
            {
                "TAUCHZEIT",
                String.format("%02d:%02d", (int) run.elapsed() / 60, (int) run.elapsed() % 60)
            },
            {"MODULE", "" + p.items().values().stream().mapToInt(Integer::intValue).sum()},
            {"SCHROTT", "" + p.salvage()},
            {"DATENKERNE", "" + p.cores()}
        };
        for (int i = 0; i < facts.length; i++) {
            g.text(x + 16, 130 + i * 20, facts[i][0], Gui.DIM);
            g.right(x + 274, 130 + i * 20, facts[i][1], Pal.RUST_6);
        }
        var threat = run.room().threat();
        if (threat != Threat.NONE)
            g.wrap(
                    x + 16,
                    340,
                    258,
                    "BEDROHUNG: " + threat.title() + ". Hilft: " + threat.counter(),
                    Pal.RED_4,
                    5);
        g.center(
                CX,
                460,
                String.format(
                        "RAUM %02d/%d  ·  SEED %d",
                        run.room().depth() + 1, RoomGenerator.ROOM_COUNT, run.seed()),
                Gui.DIM);
        g.center(CX, 476, "Beim Verlassen startest du später am letzten Raumeingang.", Gui.DIM);
    }

    // --- Bergung, Händlerin, Werkstatt -----------------------------------------------------------

    void reward(Gui g) {
        var run = nav.run();
        var p = run.player();
        var kind = run.room().kind();
        String kicker, title;
        switch (kind) {
            case MERCHANT -> {
                kicker = "SCHWARZMARKT";
                title = "„ALLES HAT SEINEN PREIS.“";
            }
            case WORKSHOP -> {
                kicker = "WERKSTATT";
                title = run.repaired() ? "REPARIERT. VOLLE ENERGIE." : "DURCHATMEN.";
            }
            case ELITE -> {
                kicker = "SELTENE BERGUNG";
                title = "MACH DIESEN RUN ZU DEINEM.";
            }
            case BOSS -> {
                kicker = "WÄCHTERBERGUNG";
                title = "DIE BEUTE EINES WÄCHTERS.";
            }
            default -> {
                kicker = "BERGUNGSKAPSEL";
                title = "WÄHLE EIN MODUL.";
            }
        }
        boolean shop = kind == RoomPlan.Kind.MERCHANT || kind == RoomPlan.Kind.WORKSHOP;
        g.header(kicker, title, shop ? Tone.AMBER : Tone.TEAL);
        var offers = run.offers();
        if (shop || offers.size() > 4) {
            int columns = 4, w = 222, h = 168, gap = 12;
            int left = CX - (columns * w + (columns - 1) * gap) / 2;
            for (int i = 0; i < offers.size(); i++)
                compactCard(
                        g,
                        offers.get(i),
                        i,
                        left + (i % columns) * (w + gap),
                        72 + (i / columns) * (h + 14),
                        w,
                        h);
        } else cards(g, offers, 76);
        g.prefer("offer.0");
        footer(
                g,
                p.salvage(),
                p.repairKits(),
                p.stats().maxRepairKits(),
                p.health(),
                p.maxHealth());
        String leave = offers.isEmpty() || shop ? "FERTIG  →" : "SPÄTER";
        if (g.button("leave", W - 16 - 160, FOOT + 10, 160, 30, leave, Tone.STEEL, true))
            nav.play();
    }

    /** Grosse Karten nebeneinander, mittig und leicht schwebend. */
    private void cards(Gui g, List<Offer> offers, int top) {
        int count = Math.max(1, offers.size());
        int w = count >= 4 ? 208 : 236, gap = count >= 4 ? 14 : 22;
        int left = CX - (count * w + (count - 1) * gap) / 2;
        for (int i = 0; i < offers.size(); i++) {
            int bob = (int) Math.round(Math.sin(g.time() * 2 + i * 1.3) * 2);
            fullCard(g, offers.get(i), i, left + i * (w + gap), top + bob, w, 386);
        }
    }

    // --- Überladung: Levelaufstieg mitten im Kampf -----------------------------------------------

    /** Während der Levelauswahl lösen Karten {@link #chooseLevel(int)} statt einer Bergung aus. */
    private boolean levelMode;

    void levelUp(Gui g) {
        var run = nav.run();
        var p = run.player();
        int shown = p.level() - run.pendingLevelUps() + 1;
        g.header(
                "ÜBERLADUNG · STUFE " + shown,
                run.pendingLevelUps() > 1
                        ? "WÄHLE · NOCH " + (run.pendingLevelUps() - 1) + " WEITERE"
                        : "WÄHLE EINE VERSTÄRKUNG",
                Tone.VIOLET);
        levelMode = true;
        cards(g, run.levelOffers(), 76);
        levelMode = false;
        g.prefer("offer.0");
        var f = g.frame();
        f.fill(0, FOOT, W, H - FOOT, 0xF0070B10);
        f.fill(0, FOOT, W, 2, 0xFF2A3440);
        g.big(16, FOOT + 16, "STUFE " + p.level(), Pal.VIOLET_4, 2);
        g.text(
                160,
                FOOT + 20,
                "MODULE "
                        + p.items().values().stream().mapToInt(Integer::intValue).sum()
                        + "   ·   INTEGRITÄT "
                        + Math.round(p.health())
                        + "/"
                        + Math.round(p.maxHealth())
                        + "   ·   DIE ZEIT STEHT STILL   ·   ZIFFERN 1–4 WÄHLEN",
                Gui.MUTED);
    }

    void chooseLevel(int index) {
        var run = nav.run();
        var offers = run.levelOffers();
        if (index < 0 || index >= offers.size()) return;
        if (run.chooseLevelUp(offers.get(index))) {
            if (run.pendingLevelUps() == 0) nav.play();
        } else nav.toast("Nicht möglich");
    }

    private void footer(Gui g, int salvage, int kits, int maxKits, double health, double max) {
        var f = g.frame();
        f.fill(0, FOOT, W, H - FOOT, 0xF0070B10);
        f.fill(0, FOOT, W, 2, 0xFF2A3440);
        g.icon(IconArt.misc("scrap"), 16, FOOT + 14, 2);
        g.big(56, FOOT + 18, salvage + " SCHROTT", Pal.RUST_6, 2);
        g.text(
                280,
                FOOT + 22,
                "SETS "
                        + kits
                        + "/"
                        + maxKits
                        + "   ·   INTEGRITÄT "
                        + Math.round(health)
                        + "/"
                        + Math.round(max),
                Gui.MUTED);
    }

    private void fullCard(Gui g, Offer offer, int index, int x, int y, int w, int h) {
        var p = nav.run().player();
        boolean enabled = possible(offer);
        int accent = accent(offer);
        String id = "offer." + index;
        if (g.card(id, x, y, w, h, accent, enabled)) {
            if (levelMode) chooseLevel(index);
            else choose(index);
        }
        boolean focused = g.focused(id);
        var f = g.frame();
        boolean evolution = offer.type() == Offer.Type.ITEM && offer.item().evolution();
        if (evolution)
            // Entfesselung: schillernder Rahmen, der um die Karte läuft
            for (int i = 0; i < 2 * (w + h); i += 2) {
                int px, py;
                if (i < w) {
                    px = x + i;
                    py = y - 2;
                } else if (i < w + h) {
                    px = x + w + 1;
                    py = y + i - w;
                } else if (i < 2 * w + h) {
                    px = x + w - (i - w - h);
                    py = y + h + 1;
                } else {
                    px = x - 2;
                    py = y + h - (i - 2 * w - h);
                }
                f.fill(px, py, 2, 2, Pal.prism(i / 160.0 - g.time() * .6));
            }
        g.text(x + 10, y + 4, tag(offer), Pal.mix(accent, Pal.WHITE, .2));
        // Symbolschacht
        int cx = x + w / 2;
        f.fill(cx - 40, y + 28, 80, 80, 0xFF03080C);
        f.rect(cx - 40, y + 28, 80, 80, Frame.alpha(accent, .5));
        if (focused) f.addRect(cx - 39, y + 29, 78, 78, accent, .08 + .04 * Math.sin(g.time() * 6));
        g.icon(icon(offer), cx - 32, y + 36, 4);
        int ty = y + 122;
        ty += g.wrap(x + 12, ty, w - 24, offer.title().toUpperCase(), Gui.TEXT, 2, 2) + 8;
        String effect = offer.effect().replace('\n', ' ');
        // Die Wirkung ist das Wichtigste auf der Karte: gross, wenn sie hineinpasst.
        int scale = g.fit(effect, w - 24, 4);
        ty += g.wrap(x + 12, ty, w - 24, effect, Pal.RUST_6, scale == 2 ? 4 : 5, scale) + 8;
        if (evolution)
            g.wrap(
                    x + 12,
                    ty,
                    w - 24,
                    "Aus " + offer.item().base().title() + " + " + offer.item().partner().title(),
                    Pal.MYTHIC,
                    3);
        else if (offer.type() == Offer.Type.ITEM)
            g.wrap(x + 12, ty, w - 24, offer.item().description(), Gui.MUTED, 3);
        if (offer.type() == Offer.Type.ITEM && !evolution) {
            var completes = Synergy.completedBy(p.items(), offer.item());
            if (!completes.isEmpty()) {
                f.fill(x + 2, y + h - 62, w - 4, 18, 0x60401A60);
                g.center(
                        cx,
                        y + h - 57,
                        "+ RESONANZ " + completes.getFirst().title().toUpperCase(),
                        Pal.VIOLET_4);
            }
            var next = Item.evolutionOf(offer.item());
            if (next != null && p.stacks(next) == 0)
                g.center(
                        cx,
                        y + h - 76,
                        "→ " + next.title().toUpperCase() + " mit " + next.partner().title(),
                        Frame.alpha(Pal.MYTHIC, .8));
        }
        String action =
                "["
                        + (index + 1)
                        + "] "
                        + (offer.type() == Offer.Type.WEAPON ? "AUSRÜSTEN" : "NEHMEN");
        g.bigCenter(
                cx,
                y + h - 30,
                enabled ? action : "NICHT MÖGLICH",
                enabled ? Pal.RUST_6 : Gui.DIM,
                2);
    }

    private void compactCard(Gui g, Offer offer, int index, int x, int y, int w, int h) {
        var p = nav.run().player();
        boolean affordable = p.salvage() >= offer.price();
        boolean enabled = possible(offer) && affordable;
        int accent = accent(offer);
        String id = "offer." + index;
        if (g.card(id, x, y, w, h, accent, enabled)) choose(index);
        g.text(x + 8, y + 4, tag(offer), Pal.mix(accent, Pal.WHITE, .2));
        g.icon(icon(offer), x + 10, y + 26, 3);
        g.wrap(x + 66, y + 28, w - 76, offer.title().toUpperCase(), Gui.TEXT, 3);
        g.wrap(x + 10, y + 80, w - 20, offer.effect().replace('\n', ' '), Pal.RUST_6, 4);
        if (offer.type() == Offer.Type.ITEM) {
            var completes = Synergy.completedBy(p.items(), offer.item());
            if (!completes.isEmpty())
                g.text(
                        x + 10,
                        y + h - 42,
                        "+ RESONANZ " + completes.getFirst().title().toUpperCase(),
                        Pal.VIOLET_4);
        }
        String price = offer.price() > 0 ? offer.price() + " SCHROTT" : "GRATIS";
        g.text(x + 10, y + h - 22, "[" + (index + 1) + "]", Gui.DIM);
        String label = possible(offer) ? price : "VOLL";
        g.big(
                x + w - 10 - g.font().width(label, 2),
                y + h - 26,
                label,
                !possible(offer) ? Gui.DIM : affordable ? Pal.RUST_6 : Pal.RED_4,
                2);
    }

    private boolean possible(Offer offer) {
        var p = nav.run().player();
        return switch (offer.type()) {
            case ITEM -> p.stacks(offer.item()) < p.maxStacks(offer.item());
            case REPAIR_KIT -> p.repairKits() < p.stats().maxRepairKits();
            case HEAL -> p.health() < p.maxHealth();
            case WEAPON_UPGRADE -> p.weaponLevel() < Weapon.MAX_LEVEL;
            default -> true;
        };
    }

    private static int accent(Offer offer) {
        return switch (offer.type()) {
            case ITEM -> MenuScreens.rarityColor(offer.item().rarity());
            case WEAPON, WEAPON_UPGRADE -> Pal.RUST_5;
            default -> Pal.GREEN_4;
        };
    }

    private String tag(Offer offer) {
        var p = nav.run().player();
        return switch (offer.type()) {
            case ITEM ->
                    offer.item().evolution()
                            ? "ENTFESSELUNG"
                            : offer.item().rarity().title().toUpperCase()
                                    + " "
                                    + (p.stacks(offer.item()) + 1)
                                    + "/"
                                    + p.maxStacks(offer.item());
            case WEAPON -> "WAFFE";
            case WEAPON_UPGRADE -> "STUFE " + (p.weaponLevel() + 1) + "/" + Weapon.MAX_LEVEL;
            default -> "VORRAT";
        };
    }

    private Sprite icon(Offer offer) {
        var p = nav.run().player();
        return switch (offer.type()) {
            case ITEM -> IconArt.item(offer.item());
            case WEAPON -> IconArt.weapon(offer.weapon());
            case REPAIR_KIT -> IconArt.misc("kit");
            case HEAL -> IconArt.misc("heart");
            case WEAPON_UPGRADE -> IconArt.weapon(p.weapon());
            case SUPPLIES -> IconArt.misc("cache");
        };
    }

    /**
     * Nimmt ein Angebot per Index, etwa über die Zifferntasten.
     *
     * @param index Index in den aktuellen Angeboten
     */
    void choose(int index) {
        var run = nav.run();
        if (index < 0 || index >= run.offers().size()) return;
        boolean shop =
                run.room().kind() == RoomPlan.Kind.MERCHANT
                        || run.room().kind() == RoomPlan.Kind.WORKSHOP;
        if (run.take(run.offers().get(index))) {
            if (!shop) nav.play();
        } else nav.toast("Nicht möglich");
    }

    // --- Druckkapelle ----------------------------------------------------------------------------

    void shrine(Gui g) {
        var run = nav.run();
        var f = g.frame();
        g.header("DRUCKKAPELLE", "EIN OPFER, EIN HANDEL.", Tone.VIOLET);
        var deals = run.deals();
        int w = 264, gap = 24;
        int left = CX - (deals.size() * w + (deals.size() - 1) * gap) / 2;
        for (int i = 0; i < deals.size(); i++) {
            var deal = deals.get(i);
            int x = left + i * (w + gap),
                    y = 80 + (int) Math.round(Math.sin(g.time() * 1.6 + i) * 2);
            if (g.card("offer." + i, x, y, w, 372, Pal.VIOLET_4, !run.dealTaken())) acceptDeal(i);
            g.text(x + 10, y + 4, "HANDEL " + (i + 1), Pal.VIOLET_5);
            g.icon(IconArt.misc("shrine"), x + w / 2 - 32, y + 32, 4);
            f.addRect(x + w / 2 - 36, y + 28, 72, 72, Pal.VIOLET_3, .06);
            int ty = y + 116;
            ty += g.wrap(x + 12, ty, w - 24, deal.title().toUpperCase(), Gui.TEXT, 2, 2) + 12;
            g.text(x + 12, ty, "OPFER", Gui.DIM);
            ty += Gui.LINE + 2;
            ty += g.wrap(x + 12, ty, w - 24, deal.cost(), Pal.RED_4, 4) + 12;
            g.text(x + 12, ty, "GABE", Gui.DIM);
            ty += Gui.LINE + 2;
            g.wrap(x + 12, ty, w - 24, deal.reward(), Pal.RUST_6, 4);
            g.bigCenter(x + w / 2, y + 340, "[" + (i + 1) + "] ANNEHMEN", Pal.VIOLET_4, 2);
        }
        g.prefer("offer.0");
        var p = run.player();
        footer(
                g,
                p.salvage(),
                p.repairKits(),
                p.stats().maxRepairKits(),
                p.health(),
                p.maxHealth());
        if (g.button("leave", W - 16 - 160, FOOT + 10, 160, 30, "GEHEN", Tone.STEEL, true))
            nav.play();
    }

    /**
     * Nimmt einen Kapellenhandel per Index an.
     *
     * @param index Index
     */
    void acceptDeal(int index) {
        var run = nav.run();
        if (index < 0 || index >= run.deals().size()) return;
        if (run.acceptDeal(run.deals().get(index))) nav.play();
        else nav.toast("Dafür fehlt dir etwas");
    }

    // --- Routenwahl ------------------------------------------------------------------------------

    void route(Gui g) {
        var run = nav.run();
        var f = g.frame();
        g.header("NAVIGATION", "DEIN WEG NACH VORN.", Tone.TEAL);
        // Routenleiste
        int count = RoomGenerator.ROOM_COUNT;
        int step = (W - 96) / (count - 1);
        for (int i = 0; i < count; i++) {
            int x = 48 + i * step;
            boolean passed = i <= run.room().depth();
            boolean next = i == run.room().depth() + 1;
            boolean boss = RoomGenerator.bossDepth(i);
            if (i < count - 1)
                f.fill(x + 6, 75, step - 6, 2, i < run.room().depth() ? Pal.RUST_3 : 0xFF1D2A38);
            int color = next ? Pal.TEAL_5 : passed ? Pal.RUST_6 : boss ? Pal.RED_2 : 0xFF2A3440;
            if (boss) {
                f.fill(x - 1, 71, 10, 10, color);
                f.fill(x + 3, 68, 2, 16, color);
            } else f.fill(x + 1, 73, 6, 6, color);
            if (next && ((int) (g.time() * 3)) % 2 == 0) f.rect(x - 3, 69, 14, 14, Pal.TEAL_5);
            if (i % SECTOR == 0)
                g.text(
                        x,
                        88,
                        RoomPlan.sectorName(i / SECTOR).substring(0, 4),
                        i / SECTOR == run.room().sector() ? Pal.RUST_6 : Gui.DIM);
        }
        var choices = run.nextRooms();
        int w = 440, gap = 24;
        int left = CX - (choices.size() * w + (choices.size() - 1) * gap) / 2;
        for (int i = 0; i < choices.size(); i++)
            routeCard(g, choices.get(i), i, left + i * (w + gap), 104, w, 374);
        g.prefer("offer.0");
        if (g.button("stay", 16, FOOT + 14, 240, 24, "← NOCH IM RAUM BLEIBEN", Tone.QUIET, true))
            nav.play();
        g.right(W - 16, FOOT + 22, "[1] / [2] ODER ENTER", Gui.DIM);
    }

    private static final int SECTOR = RoomGenerator.SECTOR_ROOMS;

    private void routeCard(Gui g, RoomPlan room, int index, int x, int y, int w, int h) {
        var run = nav.run();
        var f = g.frame();
        int accent =
                switch (room.kind()) {
                    case ELITE, BOSS, BRIDGE -> Pal.RED_4;
                    case SHRINE -> Pal.VIOLET_4;
                    case MERCHANT, WORKSHOP, CACHE -> Pal.GREEN_4;
                    default -> Pal.TEAL_5;
                };
        if (g.card("offer." + index, x, y, w, h, accent, true)) chooseRoute(index);
        // Kamerabild des nächsten Raums, im Raster der Pixelwelt verdoppelt
        int mx = x + 8, my = y + 20, mw = w - 16, mh = 176;
        g.screen(mx, my, mw, mh, Tone.TEAL);
        var art = nav.bank().room(room, run.seed());
        int sw = mw / 2, sh = mh / 2;
        int srcLeft = Math.max(0, Math.min(art.width() - sw, art.width() / 2 - sw / 2));
        int srcTop = RoomArt.FLOOR - sh + 12;
        int[] px = art.pixels();
        double flicker = .82 + .06 * Math.sin(g.time() * 40 + index);
        for (int yy = 1; yy < sh - 1; yy++)
            for (int xx = 1; xx < sw - 1; xx++) {
                int c = px[(srcTop + yy) * art.width() + srcLeft + xx];
                if (c >>> 24 == 0) c = 0xFF0C2436;
                if (yy % 2 == 0) c = Pal.mix(c, 0xFF000000, .2);
                f.fill(mx + xx * 2, my + yy * 2, 2, 2, Pal.mix(0xFF000000, c, flicker));
            }
        int noise = (int) (g.time() * 60) + index * 17;
        for (int k = 0; k < 20; k++) {
            int nx = mx + 2 + Math.floorMod(noise * 31 + k * 97, mw - 4);
            int ny = my + 2 + Math.floorMod(noise * 17 + k * 53, mh - 4);
            f.fill(nx, ny, 2, 2, 0x60FFFFFF);
        }
        g.text(mx + 8, my + 6, String.format("CAM %02d", room.depth() + 1), Pal.TEAL_5);
        if (((int) (g.time() * 2)) % 2 == 0) f.fill(mx + mw - 18, my + 7, 8, 8, Pal.RED_3);
        var threat = room.threat();
        if (threat != Threat.NONE) {
            f.fill(mx + 2, my + 24, mw - 4, 18, 0xD0400A30);
            f.fill(mx + 2, my + 41, mw - 4, 1, Pal.MYTHIC);
            g.center(
                    mx + mw / 2,
                    my + 29,
                    "BEDROHUNG · " + threat.title().toUpperCase(),
                    Pal.MYTHIC);
        }
        if (room.condition() != RoomCondition.NONE) {
            f.fill(mx + 2, my + mh - 22, mw - 4, 20, 0xD0601008);
            f.fill(mx + 2, my + mh - 22, mw - 4, 1, Pal.RED_4);
            boolean blink = ((int) (g.time() * 3)) % 2 == 0;
            g.center(
                    mx + mw / 2,
                    my + mh - 16,
                    (blink ? "! " : "  ")
                            + room.condition().title().toUpperCase()
                            + (blink ? " !" : "  "),
                    Pal.RED_5);
        }
        // Beschreibung
        int ty = my + mh + 12;
        g.icon(IconArt.misc(iconName(room.kind())), x + 10, ty - 2, 2);
        g.text(
                x + 50,
                ty,
                (index + 1) + " · " + room.typeName().toUpperCase() + " · " + room.sectorName(),
                Gui.DIM);
        g.big(x + 50, ty + 14, room.title().toUpperCase(), Gui.TEXT, 2);
        int dy = ty + 44;
        if (threat != Threat.NONE) {
            dy += g.wrap(x + 12, dy, w - 24, threat.description(), Pal.RED_4, 2) + 4;
            boolean ready = threat.prepared(run.player());
            dy +=
                    g.wrap(
                                    x + 12,
                                    dy,
                                    w - 24,
                                    (ready
                                                    ? "DEIN BUILD IST BEREIT"
                                                    : "UNVORBEREITET · HILFT: " + threat.counter())
                                            .toUpperCase(),
                                    ready ? Pal.GREEN_4 : Pal.RUST_6,
                                    2)
                            + 4;
        }
        if (room.condition() != RoomCondition.NONE)
            g.wrap(x + 12, dy, w - 24, room.condition().description(), Pal.RED_4, 2);
        else if (threat == Threat.NONE)
            g.wrap(x + 12, dy, w - 24, room.description(), Gui.MUTED, 3);
        g.text(x + 12, y + h - 20, threat(room), Pal.mix(accent, Pal.WHITE, .2));
    }

    /**
     * Betritt einen Raum per Index.
     *
     * @param index Index
     */
    void chooseRoute(int index) {
        var run = nav.run();
        if (run.chooseNextRoom(index)) {
            nav.service().saveRoom();
            nav.play();
        }
    }

    private static String threat(RoomPlan room) {
        if (!room.hostile()) return "KEINE GEGNER";
        int count = room.waves().stream().mapToInt(List::size).sum();
        boolean elite =
                room.waves().stream().flatMap(List::stream).anyMatch(s -> s.affix().elite());
        int swarm = room.hordeTotal();
        return room.waveCount()
                + (room.waveCount() == 1 ? " WELLE" : " WELLEN")
                + " · "
                + count
                + " GEGNER"
                + (swarm > 0 ? " + " + swarm + " SCHWARM" : "")
                + (elite ? " · ELITE" : "");
    }

    private static String iconName(RoomPlan.Kind kind) {
        return switch (kind) {
            case COMBAT -> "combat";
            case ELITE -> "elite";
            case WORKSHOP -> "workshop";
            case CACHE -> "cache";
            case MERCHANT -> "shop";
            case SHRINE -> "shrine";
            case BOSS, BRIDGE -> "boss";
        };
    }

    // --- Ausrüstung ------------------------------------------------------------------------------

    void inventory(Gui g) {
        var run = nav.run();
        var p = run.player();
        var s = p.stats();
        var f = g.frame();
        g.header("AUSRÜSTUNG", p.diver().title().toUpperCase(), Tone.TEAL);
        // Figur und Werte
        g.panel(16, 66, 300, 414);
        g.screen(28, 78, 276, 124, Tone.TEAL);
        var look = nav.service().profile().cosmetics();
        var idle = nav.bank().diver(look, p.weapon(), p.diver()).get(DiverArt.Anim.IDLE);
        f.addRect(116, 84, 100, 112, 0xFFB8E0FF, .05);
        g.sprite(idle.get(((int) (g.time() * 4)) % idle.size()), 166, 198, 2);
        String[][] stats = {
            {"INTEGRITÄT", Math.round(p.health()) + " / " + Math.round(p.maxHealth())},
            {"SCHILD", Math.round(p.shield()) + " / " + Math.round(s.maxShield())},
            {"SCHADEN", pct(s.damage())},
            {"MODULSCHADEN", pct(s.abilityDamage())},
            {"ANGRIFFSTEMPO", pct(s.attackSpeed())},
            {"REICHWEITE · FLÄCHE", pct(s.reach()) + " · " + pct(s.area())},
            {"LAUFTEMPO", pct(s.moveSpeed())},
            {"KRIT. CHANCE", Math.round(s.critChance() * 100) + " %"},
            {"KRIT. SCHADEN", "×" + String.format(Locale.ROOT, "%.1f", s.critDamage())},
            {"ERLITTEN", Math.round(s.damageTaken() * 100) + " %"},
            {"ABKLINGZEIT", Math.round(s.cooldown() * 100) + " %"},
            {
                "BRAND / KÄLTE",
                Math.round(s.burnChance() * 100) + " / " + Math.round(s.chillChance() * 100) + " %"
            },
            {"LEBENSRAUB", Math.round(s.lifesteal() * 100) + " %"},
            {"ZUSÄTZL. GESCHOSSE", "+" + s.extraProjectiles()},
            {"ÜBERLADUNG", pct(s.xpGain())},
        };
        for (int i = 0; i < stats.length; i++) {
            int y = 214 + i * 17;
            g.text(30, y, stats[i][0], Gui.DIM);
            g.right(302, y, stats[i][1], Pal.RUST_6);
        }
        // Waffe und Modul
        g.panel(328, 66, 302, 60);
        g.icon(IconArt.weapon(p.weapon()), 340, 78, 2);
        g.big(384, 78, p.weapon().title().toUpperCase(), Gui.TEXT, 2);
        g.text(384, 100, "STUFE " + p.weaponLevel() + "/" + Weapon.MAX_LEVEL, Pal.RUST_6);
        g.panel(642, 66, 302, 60);
        g.icon(IconArt.module(p.module()), 654, 78, 2);
        g.big(698, 78, p.module().title().toUpperCase(), Gui.TEXT, 2);
        g.text(698, 100, p.module().cost() + " ENERGIE", Pal.TEAL_5);
        // Modulraster
        var items = new ArrayList<Item>();
        for (var item : Item.values()) if (p.stacks(item) > 0) items.add(item);
        g.panel(328, 134, 616, 180);
        Item shown = null;
        for (int i = 0; i < Math.min(items.size(), 60); i++) {
            var item = items.get(i);
            int x = 340 + (i % 15) * 40, y = 144 + (i / 15) * 40;
            String id = "item." + item.name();
            g.card(id, x, y, 36, 36, MenuScreens.rarityColor(item.rarity()), true);
            if (g.focused(id) || shown == null) shown = item;
            g.icon(IconArt.item(item), x + 2, y + 2, 2);
            if (p.stacks(item) > 1) {
                String n = "" + p.stacks(item);
                f.fill(x + 34 - g.width(n) - 3, y + 24, g.width(n) + 4, 11, 0xE0000000);
                g.font().draw(f, n, x + 34 - g.width(n) - 1, y + 26, Pal.RUST_6, 1);
            }
        }
        g.screen(328, 322, 616, 158, Tone.TEAL);
        if (shown == null) {
            g.wrap(
                    344,
                    338,
                    584,
                    "Noch keine Module installiert. Bergungen und Händler liefern sie.",
                    Gui.MUTED,
                    3);
            g.target("empty", 328, 322, 1, 1);
        } else {
            g.icon(IconArt.item(shown), 340, 334, 3);
            g.big(400, 334, shown.title().toUpperCase(), Gui.TEXT, 2);
            g.text(
                    400,
                    358,
                    shown.rarity().title().toUpperCase()
                            + "  ·  STUFE "
                            + p.stacks(shown)
                            + "/"
                            + p.maxStacks(shown),
                    MenuScreens.rarityColor(shown.rarity()));
            int ty = 386;
            ty += g.wrap(340, ty, 588, shown.effect().replace('\n', ' '), Pal.RUST_6, 2) + 4;
            ty += g.wrap(340, ty, 588, shown.description(), Gui.MUTED, 1) + 6;
            evolutionHint(g, p, shown, 340, ty);
        }
        var resonances = Synergy.activeIn(p.items());
        var text = new StringBuilder();
        for (var synergy : resonances)
            text.append(text.isEmpty() ? "RESONANZ: " : "  ·  ")
                    .append(synergy.title().toUpperCase());
        var fb = g.frame();
        fb.fill(0, FOOT, W, H - FOOT, 0xF0070B10);
        fb.fill(0, FOOT, W, 2, 0xFF2A3440);
        if (!text.isEmpty()) g.wrap(16, FOOT + 12, 740, text.toString(), Pal.VIOLET_4, 2);
        else g.text(16, FOOT + 18, "Noch keine Resonanz aktiv.", Gui.DIM);
        g.prefer(items.isEmpty() ? "resume" : "item." + items.getFirst().name());
        if (g.button("resume", W - 16 - 160, FOOT + 10, 160, 30, "WEITER  →", Tone.AMBER, true))
            nav.play();
    }

    /** Zeigt, wohin ein Modul führt: die Entfesselung, ihr Partner und wie weit der Build ist. */
    private static void evolutionHint(
            Gui g, ch.zhaw.abyss.domain.Player p, Item item, int x, int y) {
        Item evolution = item.evolution() ? null : Item.evolutionOf(item);
        Item fromPartner = null;
        for (var candidate : Item.evolutions())
            if (candidate.partner() == item) fromPartner = candidate;
        if (item.evolution()) {
            g.text(
                    x,
                    y,
                    "ENTFESSELT AUS " + item.base().title() + " + " + item.partner().title(),
                    Pal.MYTHIC);
            return;
        }
        var target = evolution != null ? evolution : fromPartner;
        if (target == null) return;
        if (p.stacks(target) > 0) {
            g.text(x, y, "ENTFESSELT: " + target.title().toUpperCase(), Pal.MYTHIC);
            return;
        }
        var base = target.base();
        boolean full = p.stacks(base) >= base.maxStacks();
        boolean partner = p.stacks(target.partner()) > 0;
        g.text(
                x,
                y,
                "→ "
                        + target.title().toUpperCase()
                        + ":  "
                        + base.title()
                        + " "
                        + p.stacks(base)
                        + "/"
                        + base.maxStacks()
                        + (full ? " ✓" : "")
                        + "  ·  "
                        + target.partner().title()
                        + (partner ? " ✓" : " fehlt"),
                full && partner ? Pal.MYTHIC : Frame.alpha(Pal.MYTHIC, .75));
    }

    private static String pct(double factor) {
        return Math.round(factor * 100) + " %";
    }

    // --- Bootskarte ------------------------------------------------------------------------------

    void map(Gui g) {
        var run = nav.run();
        var f = g.frame();
        g.header("BOOTSKARTE", "VIERUNDZWANZIG SCHOTTS.", Tone.TEAL);
        String[] sectors = {"HECK", "MASCHINEN", "FORSCHUNG", "KOMMANDO"};
        String[] bosses = {"SCHOTTMEISTER", "REAKTORKERN", "BRUTMUTTER", "LOTSE"};
        int roomW = (440 - 110) / RoomGenerator.ROOM_COUNT;
        for (int i = 0; i < 4; i++) {
            // Positionen der Pixelwelt-Karte, auf die feine Ebene übertragen
            int cx = (20 + 40 + i * 6 * roomW + 3 * roomW) * WorldRenderer.UI;
            boolean here = i == run.room().sector();
            g.bigCenter(cx, 136, sectors[i], here ? Pal.RUST_6 : Pal.TEAL_5, 2);
            f.fill(cx - 60, 160, 120, 2, here ? Pal.RUST_4 : 0xFF2A3440);
            String boss = i == 3 && run.cycle() > 0 ? "PRISMENKAISERIN" : bosses[i];
            g.center(cx, 372 + (i % 2) * 18, boss, Gui.DIM);
        }
        var room = run.room();
        g.panel(16, 412, 928, 52);
        g.center(
                CX,
                422,
                String.format(
                        "RAUM %02d/%d  ·  %s  ·  %s",
                        room.depth() + 1,
                        RoomGenerator.ROOM_COUNT,
                        room.title().toUpperCase(),
                        room.typeName().toUpperCase()),
                Pal.RUST_6);
        g.center(
                CX,
                440,
                String.format(
                        "ZYKLUS %d  ·  ESKALATION %d  ·  %d GEGNER BESIEGT",
                        run.cycle() + 1, run.escalation(), run.kills()),
                Gui.MUTED);
        g.prefer("resume");
        if (g.button("resume", W - 16 - 160, FOOT + 10, 160, 30, "WEITER  →", Tone.AMBER, true))
            nav.play();
        g.text(16, FOOT + 22, "M · ZURÜCK", Gui.DIM);
    }

    // --- Ergebnis --------------------------------------------------------------------------------

    void outcome(Gui g) {
        var run = nav.run();
        var service = nav.service();
        var f = g.frame();
        boolean won = run.phase() == GameRun.Phase.VICTORY;
        g.header(
                won ? "BRÜCKE EROBERT" : "SIGNAL VERLOREN",
                won ? "DU BESTIMMST DEN KURS." : "DAS BOOT FÄHRT WEITER. DU AUCH.",
                won ? Tone.AMBER : Tone.RED);
        g.panel(16, 66, 928, 112);
        String[] values = {
            String.format("%02d/%d", run.room().depth() + 1, RoomGenerator.ROOM_COUNT),
            "" + run.kills(),
            String.format("%02d:%02d", (int) run.elapsed() / 60, (int) run.elapsed() % 60),
            "+" + service.lastCoreReward()
        };
        String[] names = {"ERREICHTER RAUM", "GEGNER BESIEGT", "TAUCHZEIT", "DATENKERNE"};
        for (int i = 0; i < 4; i++) {
            int x = 40 + i * 228;
            g.big(x, 86, values[i], won ? Pal.RUST_6 : Gui.TEXT, 4);
            g.text(x, 128, names[i], Gui.DIM);
            if (i > 0) f.fill(x - 16, 82, 1, 80, 0xFF2A3440);
        }
        g.text(
                40,
                150,
                "ZYKLUS "
                        + (run.cycle() + 1)
                        + "  ·  DRUCKSTUFE "
                        + run.pressure()
                        + (run.escalation() > 0 ? "  ·  ESKALATION " + run.escalation() : "")
                        + "  ·  ÜBERLADUNG STUFE "
                        + run.player().level(),
                Gui.MUTED);
        g.text(16, 192, "BUILD", Pal.TEAL_5);
        int i = 0;
        for (var item : Item.values())
            if (run.player().stacks(item) > 0 && i < 48) {
                int x = 16 + (i % 24) * 38, y = 208 + (i / 24) * 38;
                f.fill(x, y, 34, 34, 0xFF061018);
                f.rect(x, y, 34, 34, Frame.alpha(MenuScreens.rarityColor(item.rarity()), .6));
                g.icon(IconArt.item(item), x + 1, y + 1, 2);
                i++;
            }
        if (i == 0) g.text(16, 212, "Keine Module installiert.", Gui.MUTED);
        g.icon(IconArt.weapon(run.player().weapon()), 16, 290, 2);
        g.text(
                58,
                302,
                run.player().weapon().title() + " · Stufe " + run.player().weaponLevel(),
                Gui.MUTED);
        var report = service.lastCareer();
        if (report.xp() > 0) {
            String line =
                    "+"
                            + report.xp()
                            + " LAUFBAHN-EP  ·  RANG "
                            + report.rank()
                            + (report.rankUps() > 0 ? " (+" + report.rankUps() + ")" : "")
                            + "  ·  "
                            + run.player().diver().title().toUpperCase()
                            + " RANG "
                            + report.classRank()
                            + (report.classRankUps() > 0
                                    ? " (+" + report.classRankUps() + ")"
                                    : "");
            g.big(
                    16,
                    336,
                    line,
                    report.rankUps() + report.classRankUps() > 0 ? Pal.VIOLET_5 : Pal.VIOLET_4,
                    1);
            if (!report.masteryUps().isEmpty()) {
                var upgraded = new ArrayList<String>();
                for (var weapon : report.masteryUps()) upgraded.add(weapon.title());
                g.text(16, 354, "MEISTERSCHAFT ↑ " + String.join(", ", upgraded), Pal.RUST_6);
            }
        }
        g.big(16, 384, "♦ " + service.profile().cores() + " DATENKERNE IM ARCHIV", Pal.TEAL_5, 2);
        var fb = g.frame();
        fb.fill(0, FOOT - 6, W, H - FOOT + 6, 0xF0070B10);
        fb.fill(0, FOOT - 6, W, 2, 0xFF2A3440);
        if (g.button("menu", 16, FOOT + 12, 170, 24, "ZUM HAUPTMENÜ", Tone.QUIET, true))
            nav.title();
        if (g.button("career", 300, FOOT + 6, 180, 32, "LAUFBAHN", Tone.VIOLET, true)) nav.career();
        if (g.button("archive", 494, FOOT + 6, 180, 32, "ARCHIV", Tone.STEEL, true)) nav.archive(0);
        if (won) {
            if (g.button(
                    "primary", W - 16 - 256, FOOT, 256, 40, "NÄCHSTER ZYKLUS  →", Tone.AMBER, true))
                nav.nextCycle();
        } else if (g.button(
                "primary", W - 16 - 256, FOOT, 256, 40, "ERNEUT TAUCHEN [R]", Tone.AMBER, true))
            nav.startRun(service.profile().loadout(), System.nanoTime());
        g.prefer("primary");
    }
}
