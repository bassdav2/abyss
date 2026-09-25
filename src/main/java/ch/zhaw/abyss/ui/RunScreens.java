package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.Offer;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Synergy;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.DiverArt;
import ch.zhaw.abyss.ui.art.IconArt;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.art.RoomArt;
import ch.zhaw.abyss.ui.gui.Gui;
import ch.zhaw.abyss.ui.gui.Gui.Tone;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bildschirme während eines Tauchgangs, gezeichnet über der angehaltenen Welt: Pause, Bergung als
 * Hologramm-Karten, Händler und Werkstatt, Druckkapelle, Routenwahl mit Kamerabildern, Ausrüstung,
 * Bootskarte und Ergebnis. Sie zeigen Domänenwerte an und rufen nur öffentliche Domänenaktionen
 * auf; Preise und Regeln prüft die Domäne.
 */
final class RunScreens {
    private final Navigator nav;

    RunScreens(Navigator nav) {
        this.nav = nav;
    }

    // --- Pause -----------------------------------------------------------------------------------

    void pause(Gui g) {
        var run = nav.run();
        g.panel(150, 44, 180, 184);
        g.screen(158, 52, 164, 34, Tone.TEAL);
        g.big(240 - g.width("PAUSE"), 56, "PAUSE", Gui.TEXT, 2);
        g.center(240, 74, "DAS BOOT WARTET.", Pal.TEAL_5);
        String[] labels = {"WEITERSPIELEN", "AUSRÜSTUNG", "BOOTSKARTE", "OPTIONEN", "STEUERUNG"};
        Runnable[] actions = {
            nav::play, nav::inventory, nav::map, () -> nav.settings(true), () -> nav.help(true)
        };
        for (int i = 0; i < labels.length; i++)
            if (g.button(
                    "pause." + i,
                    170,
                    96 + i * 22,
                    140,
                    17,
                    labels[i],
                    i == 0 ? Tone.AMBER : Tone.STEEL,
                    true)) actions[i].run();
        if (g.button("menu", 170, 208, 140, 12, "ZUM HAUPTMENÜ", Tone.QUIET, true)) nav.title();
        g.prefer("pause.0");
        g.center(
                240,
                240,
                String.format(
                        "RAUM %02d/%d  ·  SEED %d",
                        run.room().depth() + 1, RoomGenerator.ROOM_COUNT, run.seed()),
                Gui.DIM);
        g.center(240, 252, "Beim Verlassen startest du später am letzten Raumeingang.", Gui.DIM);
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
            int columns = 4, w = 110, h = 90, gap = 6;
            int left = 240 - (columns * w + (columns - 1) * gap) / 2;
            for (int i = 0; i < offers.size(); i++)
                compactCard(
                        g,
                        offers.get(i),
                        i,
                        left + (i % columns) * (w + gap),
                        40 + (i / columns) * (h + 8),
                        w,
                        h);
        } else {
            int count = Math.max(1, offers.size());
            int w = count >= 4 ? 106 : 124, gap = count >= 4 ? 6 : 10;
            int left = 240 - (count * w + (count - 1) * gap) / 2;
            for (int i = 0; i < offers.size(); i++) {
                int bob = (int) Math.round(Math.sin(g.time() * 2 + i * 1.3) * 1.2);
                fullCard(g, offers.get(i), i, left + i * (w + gap), 42 + bob, w, 176);
            }
        }
        g.prefer("offer.0");
        footer(
                g,
                p.salvage(),
                p.repairKits(),
                p.stats().maxRepairKits(),
                p.health(),
                p.maxHealth());
        String leave = offers.isEmpty() || shop ? "FERTIG  →" : "SPÄTER";
        if (g.button("leave", 374, 238, 98, 18, leave, Tone.STEEL, true)) nav.play();
    }

    private void footer(Gui g, int salvage, int kits, int maxKits, double health, double max) {
        var f = g.frame();
        f.fill(0, 230, 480, 40, 0xF0070B10);
        f.fill(0, 230, 480, 1, 0xFF2A3440);
        g.icon(IconArt.misc("scrap"), 8, 240, 1);
        g.big(28, 244, salvage + " SCHROTT", Pal.RUST_6, 1);
        g.text(
                120,
                244,
                "SETS "
                        + kits
                        + "/"
                        + maxKits
                        + "  ·  INTEGRITÄT "
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
        if (g.card(id, x, y, w, h, accent, enabled)) choose(index);
        boolean focused = g.focused(id);
        var f = g.frame();
        g.text(x + 4, y + 2, tag(offer), Pal.mix(accent, Pal.WHITE, .2));
        // Symbolschacht
        int cx = x + w / 2;
        f.fill(cx - 20, y + 16, 40, 40, 0xFF03080C);
        f.rect(cx - 20, y + 16, 40, 40, Frame.alpha(accent, .5));
        if (focused) f.addRect(cx - 19, y + 17, 38, 38, accent, .08 + .04 * Math.sin(g.time() * 6));
        g.icon(icon(offer), cx - 16, y + 20, 2);
        int ty = y + 62;
        ty += g.wrap(x + 6, ty, w - 12, offer.title().toUpperCase(), Gui.TEXT, 2) + 3;
        ty += g.wrap(x + 6, ty, w - 12, offer.effect().replace('\n', ' '), Pal.RUST_6, 3) + 3;
        if (offer.type() == Offer.Type.ITEM)
            g.wrap(x + 6, ty, w - 12, offer.item().description(), Gui.MUTED, 3);
        if (offer.type() == Offer.Type.ITEM) {
            var completes = Synergy.completedBy(p.items(), offer.item());
            if (!completes.isEmpty()) {
                f.fill(x + 1, y + h - 30, w - 2, 11, 0x60401A60);
                g.center(
                        cx,
                        y + h - 28,
                        "+ " + completes.getFirst().title().toUpperCase(),
                        Pal.VIOLET_4);
            }
        }
        String action =
                "["
                        + (index + 1)
                        + "] "
                        + (offer.type() == Offer.Type.WEAPON ? "AUSRÜSTEN" : "NEHMEN");
        g.center(
                cx, y + h - 14, enabled ? action : "NICHT MÖGLICH", enabled ? Pal.RUST_6 : Gui.DIM);
    }

    private void compactCard(Gui g, Offer offer, int index, int x, int y, int w, int h) {
        var p = nav.run().player();
        boolean affordable = p.salvage() >= offer.price();
        boolean enabled = possible(offer) && affordable;
        int accent = accent(offer);
        String id = "offer." + index;
        if (g.card(id, x, y, w, h, accent, enabled)) choose(index);
        g.text(x + 4, y + 2, tag(offer), Pal.mix(accent, Pal.WHITE, .2));
        g.icon(icon(offer), x + 5, y + 16, 1);
        g.wrap(x + 25, y + 15, w - 29, offer.title().toUpperCase(), Gui.TEXT, 2);
        g.wrap(x + 5, y + 38, w - 10, offer.effect().replace('\n', ' '), Pal.RUST_6, 2);
        if (offer.type() == Offer.Type.ITEM) {
            var completes = Synergy.completedBy(p.items(), offer.item());
            if (!completes.isEmpty())
                g.text(
                        x + 5,
                        y + 62,
                        "+ " + completes.getFirst().title().toUpperCase(),
                        Pal.VIOLET_4);
        }
        String price = offer.price() > 0 ? offer.price() + " SCHROTT" : "GRATIS";
        g.text(x + 5, y + h - 12, "[" + (index + 1) + "]", Gui.DIM);
        g.right(
                x + w - 5,
                y + h - 12,
                possible(offer) ? price : "VOLL",
                !possible(offer) ? Gui.DIM : affordable ? Pal.RUST_6 : Pal.RED_4);
    }

    private boolean possible(Offer offer) {
        var p = nav.run().player();
        return switch (offer.type()) {
            case ITEM -> p.stacks(offer.item()) < offer.item().maxStacks();
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
                    offer.item().rarity().title().toUpperCase()
                            + " "
                            + (p.stacks(offer.item()) + 1)
                            + "/"
                            + offer.item().maxStacks();
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
        int w = 132, gap = 10;
        int left = 240 - (deals.size() * w + (deals.size() - 1) * gap) / 2;
        for (int i = 0; i < deals.size(); i++) {
            var deal = deals.get(i);
            int x = left + i * (w + gap), y = 42 + (int) Math.round(Math.sin(g.time() * 1.6 + i));
            if (g.card("offer." + i, x, y, w, 176, Pal.VIOLET_4, !run.dealTaken())) acceptDeal(i);
            g.text(x + 4, y + 2, "HANDEL " + (i + 1), Pal.VIOLET_5);
            g.icon(IconArt.misc("shrine"), x + w / 2 - 16, y + 18, 2);
            f.addRect(x + w / 2 - 18, y + 16, 36, 36, Pal.VIOLET_3, .06);
            int ty = y + 58;
            ty += g.wrap(x + 6, ty, w - 12, deal.title().toUpperCase(), Gui.TEXT, 2) + 4;
            g.text(x + 6, ty, "OPFER", Gui.DIM);
            ty += Gui.LINE;
            ty += g.wrap(x + 6, ty, w - 12, deal.cost(), Pal.RED_4, 3) + 4;
            g.text(x + 6, ty, "GABE", Gui.DIM);
            ty += Gui.LINE;
            g.wrap(x + 6, ty, w - 12, deal.reward(), Pal.RUST_6, 3);
            g.center(x + w / 2, y + 162, "[" + (i + 1) + "] ANNEHMEN", Pal.VIOLET_4);
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
        if (g.button("leave", 374, 238, 98, 18, "GEHEN", Tone.STEEL, true)) nav.play();
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
        for (int i = 0; i < count; i++) {
            int x = 22 + i * 19;
            boolean passed = i <= run.room().depth();
            boolean next = i == run.room().depth() + 1;
            boolean boss = RoomGenerator.bossDepth(i);
            if (i < count - 1)
                f.fill(x + 3, 39, 16, 1, i < run.room().depth() ? Pal.RUST_3 : 0xFF1D2A38);
            int color = next ? Pal.TEAL_5 : passed ? Pal.RUST_6 : boss ? Pal.RED_2 : 0xFF2A3440;
            if (boss) {
                f.fill(x, 37, 5, 5, color);
                f.pixel(x + 2, 36, color);
                f.pixel(x + 2, 42, color);
            } else f.fill(x + 1, 38, 3, 3, color);
            if (next && ((int) (g.time() * 3)) % 2 == 0) f.rect(x - 1, 36, 7, 7, Pal.TEAL_5);
        }
        var choices = run.nextRooms();
        int w = 216, gap = 12;
        int left = 240 - (choices.size() * w + (choices.size() - 1) * gap) / 2;
        for (int i = 0; i < choices.size(); i++)
            routeCard(g, choices.get(i), i, left + i * (w + gap), 50, w, 180);
        g.prefer("offer.0");
        if (g.button("stay", 8, 244, 130, 14, "← NOCH IM RAUM BLEIBEN", Tone.QUIET, true))
            nav.play();
        g.right(472, 246, "[1] / [2] ODER ENTER", Gui.DIM);
    }

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
        // Kamerabild des nächsten Raums
        int mx = x + 5, my = y + 13, mw = w - 10, mh = 84;
        g.screen(mx, my, mw, mh, Tone.TEAL);
        var art = nav.bank().room(room, run.seed());
        int srcLeft = Math.max(0, Math.min(art.width() - mw, art.width() / 2 - mw / 2));
        int srcTop = RoomArt.FLOOR - mh + 12;
        int[] px = art.pixels();
        double flicker = .82 + .06 * Math.sin(g.time() * 40 + index);
        for (int yy = 1; yy < mh - 1; yy++)
            for (int xx = 1; xx < mw - 1; xx++) {
                int c = px[(srcTop + yy) * art.width() + srcLeft + xx];
                if (c >>> 24 == 0) c = 0xFF0C2436;
                if (yy % 2 == 0) c = Pal.mix(c, 0xFF000000, .25);
                f.pixel(mx + xx, my + yy, Pal.mix(0xFF000000, c, flicker));
            }
        int noise = (int) (g.time() * 60) + index * 17;
        for (int k = 0; k < 12; k++) {
            int nx = mx + 1 + Math.floorMod(noise * 31 + k * 97, mw - 2);
            int ny = my + 1 + Math.floorMod(noise * 17 + k * 53, mh - 2);
            f.pixel(nx, ny, 0x60FFFFFF);
        }
        g.text(mx + 4, my + 3, String.format("CAM %02d", room.depth() + 1), Pal.TEAL_5);
        if (((int) (g.time() * 2)) % 2 == 0) f.fill(mx + mw - 10, my + 4, 5, 5, Pal.RED_3);
        if (room.condition() != RoomCondition.NONE) {
            f.fill(mx + 1, my + mh - 13, mw - 2, 12, 0xD0601008);
            f.fill(mx + 1, my + mh - 13, mw - 2, 1, Pal.RED_4);
            boolean blink = ((int) (g.time() * 3)) % 2 == 0;
            g.center(
                    mx + mw / 2,
                    my + mh - 11,
                    (blink ? "! " : "  ")
                            + room.condition().title().toUpperCase()
                            + (blink ? " !" : "  "),
                    Pal.RED_5);
        }
        // Beschreibung
        int ty = y + mh + 17;
        g.icon(IconArt.misc(iconName(room.kind())), x + 5, ty - 1, 1);
        g.text(
                x + 25,
                ty,
                (index + 1) + " · " + room.typeName().toUpperCase() + " · " + room.sectorName(),
                Gui.DIM);
        g.text(x + 25, ty + 10, room.title().toUpperCase(), Gui.TEXT);
        if (room.condition() != RoomCondition.NONE)
            g.wrap(x + 6, ty + 24, w - 12, room.condition().description(), Pal.RED_4, 3);
        else g.wrap(x + 6, ty + 24, w - 12, room.description(), Gui.MUTED, 3);
        g.text(x + 6, y + h - 12, threat(room), Pal.mix(accent, Pal.WHITE, .2));
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
        return room.waveCount()
                + (room.waveCount() == 1 ? " WELLE" : " WELLEN")
                + " · "
                + count
                + " GEGNER"
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
        g.panel(4, 36, 146, 196);
        g.screen(10, 42, 134, 60, Tone.TEAL);
        var look = nav.service().profile().cosmetics();
        var idle = nav.bank().diver(look, p.weapon(), p.diver()).get(DiverArt.Anim.IDLE);
        f.addRect(52, 46, 50, 54, 0xFFB8E0FF, .05);
        f.draw(idle.get(((int) (g.time() * 4)) % idle.size()), 77, 98, false);
        String[][] stats = {
            {"INTEGRITÄT", Math.round(p.health()) + "/" + Math.round(p.maxHealth())},
            {"SCHADEN", pct(s.damage())},
            {"ANGRIFFSTEMPO", pct(s.attackSpeed())},
            {"REICHWEITE", pct(s.reach())},
            {"LAUFTEMPO", pct(s.moveSpeed())},
            {"KRIT. CHANCE", Math.round(s.critChance() * 100) + " %"},
            {"KRIT. SCHADEN", "×" + String.format(Locale.ROOT, "%.1f", s.critDamage())},
            {"ERLITTEN", Math.round(s.damageTaken() * 100) + " %"},
            {"ABKLINGZEIT", Math.round(s.cooldown() * 100) + " %"},
            {
                "BRAND / KÄLTE",
                Math.round(s.burnChance() * 100) + "/" + Math.round(s.chillChance() * 100) + " %"
            },
            {"LEBENSRAUB", Math.round(s.lifesteal() * 100) + " %"},
        };
        for (int i = 0; i < stats.length; i++) {
            g.text(10, 108 + i * 11, stats[i][0], Gui.DIM);
            g.right(144, 108 + i * 11, stats[i][1], Pal.RUST_6);
        }
        // Waffe und Modul
        g.panel(154, 36, 158, 38);
        g.icon(IconArt.weapon(p.weapon()), 160, 45, 1);
        g.text(180, 42, p.weapon().title().toUpperCase(), Gui.TEXT);
        g.text(180, 54, "STUFE " + p.weaponLevel() + "/" + Weapon.MAX_LEVEL, Pal.RUST_6);
        g.panel(316, 36, 158, 38);
        g.icon(IconArt.module(p.module()), 322, 45, 1);
        g.text(342, 42, p.module().title().toUpperCase(), Gui.TEXT);
        g.text(342, 54, p.module().cost() + " ENERGIE", Pal.TEAL_5);
        // Modulraster
        var items = new ArrayList<Item>();
        for (var item : Item.values()) if (p.stacks(item) > 0) items.add(item);
        g.panel(154, 78, 320, 66);
        Item shown = null;
        for (int i = 0; i < Math.min(items.size(), 45); i++) {
            var item = items.get(i);
            int x = 160 + (i % 15) * 20, y = 83 + (i / 15) * 19;
            String id = "item." + item.name();
            g.card(id, x, y, 18, 18, MenuScreens.rarityColor(item.rarity()), true);
            if (g.focused(id) || shown == null) shown = item;
            g.icon(IconArt.item(item), x + 1, y + 1, 1);
            if (p.stacks(item) > 1) {
                f.fill(x + 12, y + 11, 6, 7, 0xE0000000);
                g.font().draw(f, "" + p.stacks(item), x + 13, y + 11, Pal.RUST_6, 1);
            }
        }
        g.screen(154, 148, 320, 80, Tone.TEAL);
        if (shown == null) {
            g.wrap(
                    162,
                    156,
                    304,
                    "Noch keine Module installiert. Bergungen und Händler liefern sie.",
                    Gui.MUTED,
                    3);
            g.target("empty", 154, 148, 1, 1);
        } else {
            g.icon(IconArt.item(shown), 160, 154, 2);
            g.text(
                    196,
                    154,
                    shown.title().toUpperCase() + "  " + p.stacks(shown) + "/" + shown.maxStacks(),
                    Gui.TEXT);
            g.text(
                    196,
                    166,
                    shown.rarity().title().toUpperCase(),
                    MenuScreens.rarityColor(shown.rarity()));
            g.wrap(160, 190, 308, shown.effect().replace('\n', ' '), Pal.RUST_6, 2);
            g.wrap(160, 212, 308, shown.description(), Gui.MUTED, 1);
        }
        var resonances = Synergy.activeIn(p.items());
        var text = new StringBuilder();
        for (var synergy : resonances)
            text.append(text.isEmpty() ? "RESONANZ: " : "  ·  ")
                    .append(synergy.title().toUpperCase());
        if (!text.isEmpty()) g.wrap(8, 240, 350, text.toString(), Pal.VIOLET_4, 2);
        g.prefer(items.isEmpty() ? "resume" : "item." + items.getFirst().name());
        if (g.button("resume", 364, 238, 110, 18, "WEITER  →", Tone.AMBER, true)) nav.play();
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
            int cx = 20 + 40 + i * 6 * roomW + 3 * roomW;
            boolean here = i == run.room().sector();
            g.center(cx, 72, sectors[i], here ? Pal.RUST_6 : Pal.TEAL_5);
            f.fill(cx - 30, 82, 60, 1, here ? Pal.RUST_4 : 0xFF2A3440);
            g.center(cx, 186 + (i % 2) * 10, bosses[i], Gui.DIM);
        }
        var room = run.room();
        g.panel(8, 206, 464, 26);
        g.center(
                240,
                214,
                String.format(
                        "RAUM %02d/%d  ·  %s  ·  %s  ·  ZYKLUS %d  ·  %d GEGNER BESIEGT",
                        room.depth() + 1,
                        RoomGenerator.ROOM_COUNT,
                        room.title().toUpperCase(),
                        room.typeName().toUpperCase(),
                        run.cycle() + 1,
                        run.kills()),
                Pal.RUST_6);
        g.prefer("resume");
        if (g.button("resume", 364, 240, 110, 18, "WEITER  →", Tone.AMBER, true)) nav.play();
        g.text(8, 246, "M · ZURÜCK", Gui.DIM);
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
        g.panel(8, 38, 464, 64);
        String[] values = {
            String.format("%02d/%d", run.room().depth() + 1, RoomGenerator.ROOM_COUNT),
            "" + run.kills(),
            String.format("%02d:%02d", (int) run.elapsed() / 60, (int) run.elapsed() % 60),
            "+" + service.lastCoreReward()
        };
        String[] names = {"ERREICHTER RAUM", "GEGNER BESIEGT", "TAUCHZEIT", "DATENKERNE"};
        for (int i = 0; i < 4; i++) {
            int x = 20 + i * 114;
            g.big(x, 48, values[i], won ? Pal.RUST_6 : Gui.TEXT, 2);
            g.text(x, 72, names[i], Gui.DIM);
            if (i > 0) f.fill(x - 8, 46, 1, 44, 0xFF2A3440);
        }
        g.text(8, 110, "BUILD", Pal.TEAL_5);
        int i = 0;
        for (var item : Item.values())
            if (run.player().stacks(item) > 0 && i < 44) {
                int x = 8 + (i % 22) * 21, y = 122 + (i / 22) * 20;
                f.fill(x, y, 18, 18, 0xFF061018);
                f.rect(x, y, 18, 18, Frame.alpha(MenuScreens.rarityColor(item.rarity()), .6));
                g.icon(IconArt.item(item), x + 1, y + 1, 1);
                i++;
            }
        if (i == 0) g.text(8, 124, "Keine Module installiert.", Gui.MUTED);
        g.icon(IconArt.weapon(run.player().weapon()), 8, 168, 1);
        g.text(
                28,
                172,
                run.player().weapon().title() + " · Stufe " + run.player().weaponLevel(),
                Gui.MUTED);
        g.text(8, 196, "♦ " + service.profile().cores() + " DATENKERNE IM ARCHIV", Pal.TEAL_5);
        if (won) {
            if (g.button("primary", 330, 236, 142, 20, "NÄCHSTER ZYKLUS  →", Tone.AMBER, true))
                nav.nextCycle();
        } else if (g.button("primary", 330, 236, 142, 20, "ERNEUT TAUCHEN [R]", Tone.AMBER, true))
            nav.startRun(service.profile().loadout(), System.nanoTime());
        if (g.button("archive", 220, 238, 100, 16, "ARCHIV", Tone.STEEL, true)) nav.archive(0);
        if (g.button("menu", 8, 240, 100, 14, "ZUM HAUPTMENÜ", Tone.QUIET, true)) nav.title();
        g.prefer("primary");
    }
}
