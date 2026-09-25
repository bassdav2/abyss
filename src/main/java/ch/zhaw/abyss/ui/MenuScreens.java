package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.application.Achievement;
import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.application.Unlock;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.Rarity;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.domain.Synergy;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.DiverArt;
import ch.zhaw.abyss.ui.art.IconArt;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.gui.Gui;
import ch.zhaw.abyss.ui.gui.Gui.Tone;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Menüs ausserhalb des Tauchgangs, gezeichnet als Bordsysteme im Pixelbild: Titel, Schleuse
 * (Vorbereitung), Garderobe, Archiv-Terminal, Bordcomputer-Einstellungen und Steuerung. Alle
 * Änderungen laufen über den {@link GameService}; hier liegt nur Darstellung und Auswahlzustand.
 */
final class MenuScreens {
    private static final String[] TABS = {
        "TAUCHER",
        "WAFFEN",
        "MODULE",
        "BAUPLÄNE",
        "AUSRÜSTUNG",
        "GARDEROBE",
        "KOMPENDIUM",
        "LOGBUCH",
        "RESONANZEN"
    };

    private final Navigator nav;
    private Loadout loadout;
    private Cosmetics look;
    private Settings draft;
    private String seed = "";
    private int archiveTab, archivePage;

    MenuScreens(Navigator nav) {
        this.nav = nav;
    }

    // --- Öffnen: setzt den Auswahlzustand zurück -------------------------------------------------

    void openLoadout() {
        var profile = nav.service().profile();
        if (loadout == null) loadout = profile.loadout();
        if (!profile.owns(loadout.diver())) loadout = loadout.withDiver(DiverClass.MECHANIC);
        if (!profile.owns(loadout.module()) && loadout.module() != loadout.diver().module())
            loadout = loadout.withModule(loadout.diver().module());
        if (!profile.owns(loadout.weapon()) && loadout.weapon() != loadout.diver().weapon())
            loadout = loadout.withWeapon(loadout.diver().weapon());
        if (loadout.pressure() > profile.maxPressure())
            loadout = loadout.withPressure(profile.maxPressure());
    }

    void openWardrobe() {
        look = nav.service().profile().cosmetics();
    }

    void openArchive(int tab) {
        archiveTab = Math.max(0, Math.min(TABS.length - 1, tab));
        archivePage = 0;
    }

    void openSettings() {
        draft = nav.service().profile().settings();
    }

    // --- Titel -----------------------------------------------------------------------------------

    void title(Gui g) {
        var service = nav.service();
        var profile = service.profile();
        boolean saved = service.saved().isPresent();
        int count = saved ? 5 : 4, w = saved ? 86 : 100, gap = 5;
        int x = 240 - (count * w + (count - 1) * gap) / 2, y = 202;
        g.prefer(saved ? "resume" : "new");
        if (saved) {
            if (g.button("resume", x, y, w, 18, "FORTSETZEN", Tone.AMBER, true)) nav.resume();
            x += w + gap;
        }
        if (g.button("new", x, y, w, 18, "NEUER TAUCHGANG", saved ? Tone.STEEL : Tone.AMBER, true))
            nav.loadout();
        if (g.button("archive", x + (w + gap), y, w, 18, "ARCHIV", Tone.STEEL, true))
            nav.archive(0);
        if (g.button("wardrobe", x + 2 * (w + gap), y, w, 18, "GARDEROBE", Tone.STEEL, true))
            nav.wardrobe();
        if (g.button("settings", x + 3 * (w + gap), y, w, 18, "OPTIONEN", Tone.STEEL, true))
            nav.settings(false);
        if (g.button("help", 170, 228, 66, 12, "STEUERUNG", Tone.QUIET, true)) nav.help(false);
        if (g.button("quit", 244, 228, 66, 12, "BEENDEN", Tone.QUIET, true)) nav.quit();
        g.center(
                240,
                252,
                "♦ "
                        + profile.cores()
                        + "   ·   "
                        + profile.runs()
                        + " TAUCHGÄNGE   ·   "
                        + profile.wins()
                        + " BRÜCKEN   ·   BESTER RAUM "
                        + profile.bestRoom()
                        + "/"
                        + RoomGenerator.ROOM_COUNT,
                Gui.DIM);
        g.right(474, 260, "V1.1", 0xFF3A4654);
    }

    // --- Schleuse: Vorbereitung ------------------------------------------------------------------

    void loadout(Gui g) {
        var service = nav.service();
        var profile = service.profile();
        var f = g.frame();
        g.header("SCHLEUSE", "WER TAUCHT HEUTE?", Tone.AMBER);
        g.right(472, 12, "♦ " + profile.cores() + " KERNE", Pal.TEAL_5);
        g.prefer("dive");
        // Spinde mit den Klassen
        var divers = DiverClass.values();
        DiverClass shown = loadout.diver();
        for (int i = 0; i < divers.length; i++) {
            var diver = divers[i];
            int x = 8 + i * 61, y = 38;
            boolean owned = profile.owns(diver);
            boolean selected = loadout.diver() == diver;
            int accent = selected ? Pal.RUST_6 : owned ? Pal.STEEL_5 : Pal.RED_3;
            String id = "diver." + i;
            if (g.card(id, x, y, 57, 88, accent, true)) {
                if (owned) loadout = loadout.withDiver(diver);
                else
                    Unlock.find(Unlock.of(diver))
                            .ifPresent(
                                    unlock -> {
                                        if (service.purchase(unlock)) {
                                            loadout = loadout.withDiver(diver);
                                            nav.toast(diver.title() + " freigeschaltet");
                                        } else nav.toast("Nicht genug Datenkerne");
                                    });
            }
            if (g.focused(id)) shown = diver;
            // Spindrückwand, Lampe, Figur
            f.fill(x + 4, y + 12, 49, 66, 0xFF0B1118);
            for (int yy = y + 16; yy < y + 76; yy += 6) f.fill(x + 6, yy, 45, 1, 0xFF111922);
            f.fill(x + 22, y + 12, 13, 2, selected ? Pal.RUST_6 : 0xFF2A3440);
            if (selected) f.addRect(x + 10, y + 14, 37, 62, 0xFFFFD890, .10);
            var sprite =
                    nav.bank()
                            .diver(profile.cosmetics(), diver.weapon(), diver)
                            .get(DiverArt.Anim.IDLE)
                            .get(selected ? ((int) (g.time() * 4)) % 4 : 0);
            if (owned) f.draw(sprite, x + 28, y + 76, false);
            else {
                f.silhouette(sprite, x + 28, y + 76, false, 0xFF1A232C);
                g.icon(IconArt.misc("lock"), x + 20, y + 36, 1);
            }
            f.fill(x + 4, y + 78, 49, 6, 0xFF151B22);
            g.center(x + 28, y + 78, "0" + (i + 1), selected ? Pal.RUST_6 : Gui.DIM);
        }
        // Personalakte der fokussierten oder gewählten Klasse
        g.screen(316, 38, 158, 88, Tone.TEAL);
        g.text(322, 43, shown.title().toUpperCase(), Gui.TEXT);
        g.text(
                322,
                55,
                Math.round(shown.health())
                        + " HP  ·  TEMPO "
                        + Math.round(shown.speed() * 100)
                        + " %",
                Pal.TEAL_5);
        g.wrap(322, 68, 148, shown.description(), Gui.MUTED, 4);
        String status =
                loadout.diver() == shown
                        ? "AUSGEWÄHLT"
                        : profile.owns(shown)
                                ? "ENTER · WÄHLEN"
                                : "KAUFEN · " + shown.unlockCost() + " ♦";
        g.text(
                322,
                114,
                status,
                loadout.diver() == shown
                        ? Pal.RUST_6
                        : profile.owns(shown) || profile.cores() >= shown.unlockCost()
                                ? Pal.TEAL_5
                                : Pal.RED_4);
        // Waffe, Modul, Druck
        var weapons = new ArrayList<Weapon>();
        for (var weapon : Weapon.values())
            if (profile.owns(weapon) || weapon == loadout.diver().weapon()) weapons.add(weapon);
        var modules = new ArrayList<ActiveModule>();
        for (var module : ActiveModule.values())
            if (profile.owns(module) || module == loadout.diver().module()) modules.add(module);
        int d =
                selector(
                        g,
                        "weapon",
                        8,
                        "WAFFE",
                        IconArt.weapon(loadout.weapon()),
                        loadout.weapon().title(),
                        loadout.weapon().description());
        if (d != 0)
            loadout =
                    loadout.withWeapon(
                            weapons.get(
                                    Math.floorMod(
                                            weapons.indexOf(loadout.weapon()) + d,
                                            weapons.size())));
        d =
                selector(
                        g,
                        "module",
                        164,
                        "MODUL · " + loadout.module().cost() + " E",
                        IconArt.module(loadout.module()),
                        loadout.module().title(),
                        loadout.module().description());
        if (d != 0)
            loadout =
                    loadout.withModule(
                            modules.get(
                                    Math.floorMod(
                                            modules.indexOf(loadout.module()) + d,
                                            modules.size())));
        String pressureInfo =
                loadout.pressure() == 0
                        ? "Ein Sieg schaltet Druckstufe 1 frei."
                        : "+"
                                + loadout.pressure() * 12
                                + " % Gegnerintegrität, +"
                                + loadout.pressure() * 10
                                + " % Schaden.";
        d =
                selector(
                        g,
                        "pressure",
                        320,
                        "DRUCK · MAX " + profile.maxPressure() + "/" + RunSetup.MAX_PRESSURE,
                        IconArt.misc("skull"),
                        loadout.pressure() == 0 ? "Normal" : "Druckstufe " + loadout.pressure(),
                        pressureInfo);
        if (d != 0)
            loadout =
                    loadout.withPressure(
                            Math.floorMod(loadout.pressure() + d, profile.maxPressure() + 1));
        // Startoptionen
        g.panel(8, 186, 306, 50);
        boolean explorer =
                g.toggle(
                        "explorer",
                        16,
                        194,
                        290,
                        "Entdecker · mehr Integrität, mehr Schaden",
                        loadout.explorer());
        if (explorer != loadout.explorer()) loadout = loadout.withExplorer(explorer);
        g.text(16, 214, "SEED", Gui.DIM);
        seed = g.field("seed", 44, 211, 104, seed, "zufällig");
        if (g.button("daily", 156, 211, 150, 14, "TAGESTAUCHGANG", Tone.TEAL, true))
            seed = "" + GameService.dailySeed(LocalDate.now());
        if (g.button("back", 8, 246, 70, 14, "← ZURÜCK", Tone.QUIET, true)) {
            service.rememberLoadout(loadout);
            nav.title();
        }
        if (g.button("wardrobe", 86, 244, 90, 16, "GARDEROBE", Tone.STEEL, true)) {
            service.rememberLoadout(loadout);
            nav.wardrobe();
        }
        // Tauchhebel
        g.panel(322, 186, 152, 76);
        g.text(330, 192, "SCHLEUSE BEREIT", Pal.TEAL_5);
        boolean lamp = ((int) (g.time() * 2)) % 2 == 0;
        f.fill(458, 193, 6, 6, lamp ? Pal.GREEN_4 : Pal.GREEN_1);
        if (g.button("dive", 330, 208, 136, 30, "TAUCHEN  →", Tone.AMBER, true)) {
            long value = seed.isBlank() ? System.nanoTime() : Long.parseLong(seed);
            nav.startRun(loadout, value);
        }
        g.center(398, 246, seed.isBlank() ? "ZUFÄLLIGE ROUTE" : "SEED " + seed, Gui.DIM);
    }

    private int selector(
            Gui g, String id, int x, String kicker, Sprite icon, String value, String info) {
        g.panel(x, 132, 152, 48);
        int delta = g.stepper(id, x + 1, 133, 150, 46);
        g.text(x + 18, 137, kicker, Pal.TEAL_5);
        g.icon(icon, x + 16, 149, 1);
        g.text(x + 36, 152, value, Gui.TEXT);
        g.wrap(x + 18, 166, 118, info, Gui.MUTED, 1);
        return delta;
    }

    // --- Garderobe -------------------------------------------------------------------------------

    void wardrobe(Gui g) {
        var service = nav.service();
        var profile = service.profile();
        var f = g.frame();
        g.header("GARDEROBE", "DEIN ANZUG. DEINE FARBEN.", Tone.TEAL);
        g.right(472, 12, "♦ " + profile.cores() + " KERNE", Pal.TEAL_5);
        var diver = loadout != null ? loadout.diver() : profile.loadout().diver();
        var weapon = loadout != null ? loadout.weapon() : profile.loadout().weapon();
        var anims = nav.bank().diver(look, weapon, diver);
        // Spiegel
        g.panel(8, 38, 168, 198);
        g.screen(14, 44, 156, 140, Tone.TEAL);
        f.addRect(40, 52, 104, 128, 0xFFB8E0FF, .05);
        var idle = anims.get(DiverArt.Anim.IDLE);
        g.icon(idle.get(((int) (g.time() * 4)) % idle.size()), 20, 42, 3);
        var attack = anims.get(DiverArt.of(weapon.combo().getFirst().style()));
        var run = anims.get(DiverArt.Anim.RUN);
        var jump = anims.get(DiverArt.Anim.JUMP);
        f.draw(attack.get(((int) (g.time() * 8)) % attack.size()), 42, 228, false);
        f.draw(run.get(((int) (g.time() * 10)) % run.size()), 92, 228, false);
        f.draw(jump.get(((int) (g.time() * 3)) % jump.size()), 142, 228, false);
        // Wahlschalter
        String[] categories = {"suit", "helmet", "visor", "trim"};
        String[] titles = {"ANZUGFARBE", "HELMFORM", "VISIERFARBE", "METALLTON"};
        String[][] names = {
            DiverArt.SUIT_NAMES, DiverArt.HELMET_NAMES, DiverArt.VISOR_NAMES, DiverArt.TRIM_NAMES
        };
        int[] values = {look.suit(), look.helmet(), look.visor(), look.trim()};
        boolean allOwned = true;
        for (int c = 0; c < 4; c++) {
            int y = 38 + c * 46;
            g.panel(184, y, 290, 42);
            String id = Cosmetics.unlockId(categories[c], values[c]);
            boolean owned = profile.owns(id);
            allOwned &= owned;
            int delta = g.stepper("look." + c, 185, y + 1, owned ? 288 : 180, 40);
            if (delta != 0) look = cycle(look, c, delta, names[c].length);
            g.text(202, y + 8, titles[c], Pal.TEAL_5);
            g.big(202, y + 22, names[c][values[c]].toUpperCase(), owned ? Gui.TEXT : Gui.MUTED, 1);
            g.text(202 + 150, y + 8, (values[c] + 1) + "/" + names[c].length, Gui.DIM);
            if (!owned) {
                var unlock = Unlock.find(id).orElseThrow();
                g.text(372, y + 8, "GESPERRT", Pal.RED_4);
                if (g.button(
                        "buy." + c,
                        372,
                        y + 20,
                        94,
                        14,
                        unlock.cost() + " ♦ KAUFEN",
                        Tone.AMBER,
                        profile.canBuy(unlock))) {
                    if (service.purchase(unlock)) nav.toast("Freigeschaltet");
                }
            }
        }
        if (g.button("back", 184, 246, 70, 14, "← ZURÜCK", Tone.QUIET, true)) leaveWardrobe();
        if (g.button("apply", 364, 222, 110, 20, "ÜBERNEHMEN", Tone.AMBER, allOwned)) {
            if (service.cosmetics(look)) {
                nav.toast("Aussehen gespeichert");
                leaveWardrobe();
            }
        }
        if (!allOwned) g.right(356, 228, "ERST FREISCHALTEN", Pal.RED_4);
        g.prefer("look.0");
    }

    private void leaveWardrobe() {
        look = null;
        if (loadout != null) nav.loadout();
        else nav.title();
    }

    private static Cosmetics cycle(Cosmetics look, int category, int delta, int count) {
        return switch (category) {
            case 0 ->
                    new Cosmetics(
                            Math.floorMod(look.suit() + delta, count),
                            look.helmet(),
                            look.visor(),
                            look.trim());
            case 1 ->
                    new Cosmetics(
                            look.suit(),
                            Math.floorMod(look.helmet() + delta, count),
                            look.visor(),
                            look.trim());
            case 2 ->
                    new Cosmetics(
                            look.suit(),
                            look.helmet(),
                            Math.floorMod(look.visor() + delta, count),
                            look.trim());
            default ->
                    new Cosmetics(
                            look.suit(),
                            look.helmet(),
                            look.visor(),
                            Math.floorMod(look.trim() + delta, count));
        };
    }

    // --- Archiv-Terminal -------------------------------------------------------------------------

    void archive(Gui g) {
        var profile = nav.service().profile();
        g.header("ARCHIV", "WAS DU FINDEST, BLEIBT.", Tone.TEAL);
        g.right(472, 12, "♦ " + profile.cores() + " KERNE", Pal.TEAL_5);
        g.panel(4, 36, 96, 196);
        for (int i = 0; i < TABS.length; i++) {
            boolean active = i == archiveTab;
            if (g.button(
                    "tab." + i,
                    10,
                    42 + i * 20,
                    84,
                    16,
                    TABS[i],
                    active ? Tone.AMBER : Tone.STEEL,
                    true)) {
                archiveTab = i;
                archivePage = 0;
            }
        }
        g.prefer("tab." + archiveTab);
        if (g.button("back", 12, 246, 80, 14, "← ZURÜCK", Tone.QUIET, true)) nav.title();
        g.panel(102, 34, 374, 158);
        switch (archiveTab) {
            case 6 -> codex(g);
            case 7 -> logbook(g);
            case 8 -> resonances(g);
            default -> unlocks(g, Unlock.Category.values()[archiveTab]);
        }
    }

    private void unlocks(Gui g, Unlock.Category category) {
        var service = nav.service();
        var profile = service.profile();
        List<Unlock> list =
                Unlock.catalog().stream().filter(u -> u.category() == category).toList();
        int perPage = 12, pages = Math.max(1, (list.size() + perPage - 1) / perPage);
        archivePage = Math.min(archivePage, pages - 1);
        Unlock shown = null;
        for (int i = archivePage * perPage;
                i < Math.min(list.size(), (archivePage + 1) * perPage);
                i++) {
            var unlock = list.get(i);
            int slot = i - archivePage * perPage;
            int x = 106 + (slot % 3) * 124, y = 36 + (slot / 3) * 36;
            boolean owned = profile.owns(unlock.id());
            boolean buyable = profile.canBuy(unlock);
            int accent = owned ? Pal.GREEN_4 : buyable ? Pal.RUST_6 : Pal.STEEL_4;
            String id = "unlock." + unlock.id();
            if (g.card(id, x, y, 120, 32, accent, true) && !owned) {
                if (service.purchase(unlock)) nav.toast(unlock.title() + " freigeschaltet");
                else nav.toast(buyable ? "Nicht möglich" : "Nicht genug Datenkerne");
            }
            if (g.focused(id) || shown == null) shown = unlock;
            var icon = iconFor(unlock);
            if (icon != null) g.icon(icon, x + 4, y + 8, 1);
            g.wrap(x + 24, y + 5, 92, unlock.title(), owned ? Gui.TEXT : Gui.MUTED, 1);
            g.text(
                    x + 24,
                    y + 18,
                    owned ? "FREI" : unlock.cost() + " ♦",
                    owned ? Pal.GREEN_4 : buyable ? Pal.RUST_6 : Gui.DIM);
        }
        if (pages > 1) {
            if (g.button("prev", 380, 180, 20, 12, "<", Tone.STEEL, true))
                archivePage = Math.floorMod(archivePage - 1, pages);
            g.center(416, 182, (archivePage + 1) + "/" + pages, Gui.MUTED);
            if (g.button("next", 434, 180, 20, 12, ">", Tone.STEEL, true))
                archivePage = Math.floorMod(archivePage + 1, pages);
        }
        if (shown == null) {
            g.screen(106, 196, 368, 60, Tone.TEAL);
            g.text(114, 204, "Keine Einträge.", Gui.MUTED);
            return;
        }
        g.screen(106, 196, 368, 60, Tone.TEAL);
        g.text(114, 202, shown.title().toUpperCase(), Gui.TEXT);
        g.wrap(114, 214, 352, shown.description(), Gui.MUTED, 2);
        boolean owned = profile.owns(shown.id());
        boolean requirement = shown.requires() == null || profile.owns(shown.requires());
        g.text(
                114,
                240,
                owned
                        ? "FREIGESCHALTET"
                        : !requirement
                                ? "ERST DIE VORSTUFE FREISCHALTEN"
                                : profile.canBuy(shown)
                                        ? "ENTER · KAUFEN FÜR " + shown.cost() + " ♦"
                                        : "BENÖTIGT " + shown.cost() + " ♦",
                owned ? Pal.GREEN_4 : profile.canBuy(shown) ? Pal.RUST_6 : Pal.RED_4);
    }

    private static Sprite iconFor(Unlock unlock) {
        String[] parts = unlock.id().split(":");
        return switch (unlock.category()) {
            case DIVER -> IconArt.misc("heart");
            case WEAPON -> IconArt.weapon(Weapon.valueOf(parts[1]));
            case MODULE -> IconArt.module(ActiveModule.valueOf(parts[1]));
            case ITEM -> IconArt.item(Item.valueOf(parts[1]));
            case PERK ->
                    parts[1].startsWith("hull")
                            ? IconArt.misc("heart")
                            : parts[1].equals("kit") ? IconArt.misc("kit") : IconArt.misc("scrap");
            case COSMETIC -> IconArt.misc("shrine");
        };
    }

    private void codex(Gui g) {
        var profile = nav.service().profile();
        var items = Item.values();
        g.text(
                106,
                38,
                "ENTDECKT "
                        + profile.discovered().size()
                        + "/"
                        + items.length
                        + "  ·  "
                        + profile.totalKills()
                        + " GEGNER  ·  "
                        + profile.wins()
                        + " SIEGE",
                Pal.TEAL_5);
        Item shown = null;
        for (int i = 0; i < items.length; i++) {
            var item = items[i];
            int x = 106 + (i % 12) * 20, y = 52 + (i / 12) * 20;
            boolean known = profile.discovered().contains(item);
            String id = "item." + item.name();
            g.card(id, x, y, 18, 18, known ? rarityColor(item.rarity()) : Pal.STEEL_3, true);
            if (g.focused(id) || shown == null) shown = item;
            var icon = IconArt.item(item);
            g.icon(known ? icon : silhouette(icon), x + 1, y + 1, 1);
        }
        g.screen(106, 140, 368, 116, Tone.TEAL);
        boolean known = profile.discovered().contains(shown);
        var icon = IconArt.item(shown);
        g.icon(known ? icon : silhouette(icon), 114, 148, 2);
        if (!known) {
            g.big(152, 150, "???", Gui.MUTED, 2);
            g.wrap(
                    152,
                    172,
                    312,
                    "Noch nicht gefunden. Bergungen, Händler und Kapellen liefern neue Module.",
                    Gui.MUTED,
                    3);
            return;
        }
        g.text(152, 148, shown.title().toUpperCase(), Gui.TEXT);
        g.text(
                152,
                160,
                shown.rarity().title().toUpperCase() + "  ·  BIS STUFE " + shown.maxStacks(),
                rarityColor(shown.rarity()));
        g.wrap(114, 184, 352, shown.effect().replace('\n', ' '), Pal.RUST_6, 2);
        g.wrap(114, 208, 352, shown.description(), Gui.MUTED, 3);
        for (var synergy : Synergy.values())
            if (synergy.first() == shown || synergy.second() == shown) {
                var partner = synergy.first() == shown ? synergy.second() : synergy.first();
                g.text(
                        114,
                        244,
                        "RESONANZ " + synergy.title().toUpperCase() + " MIT " + partner.title(),
                        Pal.VIOLET_4);
                break;
            }
    }

    private void logbook(Gui g) {
        var earned = nav.service().profile().achievements();
        var entries = Achievement.values();
        g.text(106, 38, "ERREICHT " + earned.size() + "/" + entries.length, Pal.TEAL_5);
        Achievement shown = null;
        var f = g.frame();
        for (int i = 0; i < entries.length; i++) {
            var entry = entries[i];
            boolean done = earned.contains(entry);
            int x = 106 + (i / 12) * 186, y = 50 + (i % 12) * 12;
            String id = "feat." + entry.name();
            g.target(id, x, y - 1, 182, 11);
            if (g.focused(id) || shown == null) shown = entry;
            boolean focused = g.focused(id);
            if (focused) f.fill(x, y - 1, 182, 11, 0x40F5C45E);
            f.fill(x + 1, y + 1, 6, 6, done ? Pal.GREEN_4 : 0xFF1A232C);
            f.rect(x, y, 8, 8, done ? Pal.GREEN_2 : Pal.STEEL_3);
            g.text(x + 12, y, entry.title(), done ? Gui.TEXT : focused ? Gui.TEXT : Gui.MUTED);
        }
        g.screen(106, 200, 368, 56, Tone.TEAL);
        g.text(114, 206, shown.title().toUpperCase(), Gui.TEXT);
        g.wrap(114, 218, 352, shown.description(), Gui.MUTED, 2);
        boolean done = earned.contains(shown);
        g.text(
                114,
                242,
                done ? "ERREICHT" : "BELOHNUNG  +" + shown.reward() + " ♦",
                done ? Pal.GREEN_4 : Pal.RUST_6);
    }

    private void resonances(Gui g) {
        g.text(106, 38, "ZWEI MODULE IM SELBEN TAUCHGANG ERGEBEN EINEN BONUS", Pal.TEAL_5);
        var all = Synergy.values();
        Synergy shown = null;
        for (int i = 0; i < all.length; i++) {
            var synergy = all[i];
            int x = 106 + (i % 3) * 124, y = 52 + (i / 3) * 44;
            String id = "syn." + synergy.name();
            g.card(id, x, y, 120, 40, Pal.VIOLET_4, true);
            if (g.focused(id) || shown == null) shown = synergy;
            g.icon(IconArt.item(synergy.first()), x + 6, y + 14, 1);
            g.text(x + 25, y + 18, "+", Gui.MUTED);
            g.icon(IconArt.item(synergy.second()), x + 33, y + 14, 1);
            g.wrap(x + 54, y + 12, 62, synergy.title(), Pal.VIOLET_4, 2);
        }
        g.screen(106, 188, 368, 68, Tone.VIOLET);
        g.text(114, 194, shown.title().toUpperCase(), Pal.VIOLET_5);
        g.text(114, 206, shown.first().title() + "  +  " + shown.second().title(), Gui.MUTED);
        g.wrap(114, 222, 352, shown.effect(), Pal.RUST_6, 2);
    }

    static int rarityColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> Pal.STEEL_6;
            case RARE -> Pal.TEAL_5;
            case LEGENDARY -> Pal.RUST_6;
            case CURSED -> Pal.VIOLET_4;
        };
    }

    private static Sprite silhouette(Sprite sprite) {
        var pixels = sprite.pixels().clone();
        for (int i = 0; i < pixels.length; i++) if ((pixels[i] >>> 24) != 0) pixels[i] = 0xFF1E2A36;
        return new Sprite(
                pixels, sprite.width(), sprite.height(), sprite.anchorX(), sprite.anchorY());
    }

    // --- Bordcomputer und Steuerung --------------------------------------------------------------

    void settings(Gui g, boolean inGame) {
        g.header("BORDCOMPUTER", "EINSTELLUNGEN", Tone.TEAL);
        g.panel(60, 40, 360, 188);
        g.screen(68, 48, 344, 172, Tone.TEAL);
        String[] labels = {"GESAMTLAUTSTÄRKE", "MUSIK", "KAMERAWACKELN"};
        double[] values = {draft.masterVolume(), draft.musicVolume(), draft.screenShake()};
        for (int i = 0; i < 3; i++) {
            g.text(78, 58 + i * 18, labels[i], Gui.MUTED);
            values[i] = g.slider("slider." + i, 206, 58 + i * 18, 150, values[i]);
        }
        String[] toggles = {
            "Röhrenfilter · feine Bildschirmzeilen",
            "Schadenszahlen anzeigen",
            "Ruhige Darstellung · weniger Partikel",
            "Vollbild (auch F11)",
            "Entdecker als Vorgabe"
        };
        boolean[] flags = {
            draft.retroFilter(),
            draft.damageNumbers(),
            draft.reducedMotion(),
            draft.fullscreen(),
            draft.explorer()
        };
        for (int i = 0; i < toggles.length; i++)
            flags[i] = g.toggle("toggle." + i, 78, 118 + i * 18, 324, toggles[i], flags[i]);
        draft =
                new Settings(
                        values[0], values[1], flags[2], flags[3], flags[4], values[2], flags[0],
                        flags[1]);
        g.prefer("slider.0");
        if (g.button("back", 60, 240, 80, 14, "← ZURÜCK", Tone.QUIET, true)) back(inGame);
        if (g.button("apply", 310, 236, 110, 20, "ÜBERNEHMEN", Tone.AMBER, true)) {
            nav.applySettings(draft);
            back(inGame);
        }
    }

    private void back(boolean inGame) {
        if (inGame) nav.pause();
        else nav.title();
    }

    void help(Gui g, boolean inGame) {
        g.header("STEUERUNG", "LIES DEN RAUM. DANN HANDLE.", Tone.AMBER);
        String[][] controls = {
            {"A / D  ·  ← / →", "Bewegen"},
            {"LEERTASTE / W", "Springen · halten für höhere Sprünge"},
            {"S + LEERTASTE", "Durch einen Laufsteg nach unten fallen"},
            {"J / LINKE MAUS", "Angriff · halten für Kombination · in der Luft Luftangriff"},
            {"SHIFT / L", "Ausweichen · kurz unverwundbar, auch in der Luft"},
            {"K / RECHTE MAUS", "Aktives Modul · kostet Energie"},
            {"E", "Bergung, Händlerin, Werkstatt, Kapelle, Schott, Schalter"},
            {"Q", "Reparaturset · +35 Integrität"},
            {"I / TAB  ·  M", "Ausrüstung  ·  Bootskarte"},
            {"ESC · F11 · F12", "Pause · Vollbild · Bildschirmfoto"},
            {"MENÜS", "Pfeile oder Maus · Enter/Leertaste wählt · Esc zurück"}
        };
        g.panel(20, 38, 440, 190);
        g.screen(28, 46, 424, 174, Tone.AMBER);
        for (int i = 0; i < controls.length; i++) {
            g.text(38, 52 + i * 15, controls[i][0], Pal.RUST_6);
            g.wrap(160, 52 + i * 15, 284, controls[i][1], Gui.MUTED, 1);
        }
        g.text(38, 52 + 11 * 15 - 2, "ROTE MARKIERUNGEN KÜNDIGEN ANGRIFFE AN.", Pal.RED_4);
        g.prefer("ok");
        if (g.button("ok", 340, 236, 120, 20, "VERSTANDEN", Tone.AMBER, true)) back(inGame);
    }
}
