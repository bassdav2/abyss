package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.application.Achievement;
import ch.zhaw.abyss.application.Career;
import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.application.SkillTree;
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
import ch.zhaw.abyss.ui.render.WorldRenderer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Menüs ausserhalb des Tauchgangs, gezeichnet als Bordsysteme auf der Oberflächenebene in 960 ×
 * 540: Titel, Laufbahn, Schleuse (Vorbereitung), Garderobe, Archiv-Terminal,
 * Bordcomputer-Einstellungen und Steuerung. Alle Änderungen laufen über den {@link GameService};
 * hier liegt nur Darstellung und Auswahlzustand.
 */
final class MenuScreens {
    private static final int W = WorldRenderer.UW, H = WorldRenderer.UH, CX = W / 2;

    /** Oberkante der Fusszeile mit Zurück- und Haupttaste. */
    private static final int FOOT = H - 50;

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
    private int careerTab;
    private String careerNode;

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

    /** Dunkle Fusszeile über die ganze Breite. */
    private static void foot(Gui g) {
        var f = g.frame();
        f.fill(0, FOOT - 4, W, H - FOOT + 4, 0xE0070B10);
        f.fill(0, FOOT - 4, W, 2, 0xFF2A3440);
    }

    private static void cores(Gui g, int cores) {
        String label = "♦ " + cores + " KERNE";
        g.big(W - 16 - g.font().width(label, 2), 28, label, Pal.TEAL_5, 2);
    }

    private static SkillTree treeOf(DiverClass diver) {
        for (var tree : SkillTree.values()) if (tree.diver() == diver) return tree;
        return SkillTree.DEPTH;
    }

    /** Klassenname ohne Artikel, etwa für die Spindschilder. */
    private static String shortName(DiverClass diver) {
        String title = diver.title();
        int space = title.indexOf(' ');
        return (space > 0 ? title.substring(space + 1) : title).toUpperCase();
    }

    // --- Titel -----------------------------------------------------------------------------------

    void title(Gui g) {
        var service = nav.service();
        var profile = service.profile();
        boolean saved = service.saved().isPresent();
        int count = saved ? 5 : 4, w = saved ? 172 : 200, gap = 10;
        int x = CX - (count * w + (count - 1) * gap) / 2, y = 400;
        g.prefer(saved ? "resume" : "new");
        if (saved) {
            if (g.button("resume", x, y, w, 36, "FORTSETZEN", Tone.AMBER, true)) nav.resume();
            x += w + gap;
        }
        if (g.button("new", x, y, w, 36, "NEUER TAUCHGANG", saved ? Tone.STEEL : Tone.AMBER, true))
            nav.loadout();
        if (g.button("archive", x + (w + gap), y, w, 36, "ARCHIV", Tone.STEEL, true))
            nav.archive(0);
        if (g.button("wardrobe", x + 2 * (w + gap), y, w, 36, "GARDEROBE", Tone.STEEL, true))
            nav.wardrobe();
        if (g.button("settings", x + 3 * (w + gap), y, w, 36, "OPTIONEN", Tone.STEEL, true))
            nav.settings(false);
        if (g.button(
                "career",
                CX - 250,
                448,
                200,
                28,
                "LAUFBAHN · RANG " + profile.career().rank(),
                Tone.VIOLET,
                true)) nav.career();
        if (g.button("help", CX - 34, 452, 130, 20, "STEUERUNG", Tone.QUIET, true)) nav.help(false);
        if (g.button("quit", CX + 110, 452, 120, 20, "BEENDEN", Tone.QUIET, true)) nav.quit();
        g.center(
                CX,
                500,
                "♦ "
                        + profile.cores()
                        + "   ·   "
                        + profile.runs()
                        + " TAUCHGÄNGE   ·   "
                        + profile.wins()
                        + " BRÜCKEN   ·   BESTER RAUM "
                        + profile.bestRoom()
                        + "/"
                        + RoomGenerator.ROOM_COUNT
                        + "   ·   LAUFBAHNRANG "
                        + profile.career().rank(),
                Gui.MUTED);
        g.right(W - 12, H - 16, "V1.5", 0xFF3A4654);
    }

    // --- Laufbahn: Ränge, Skill-Bäume, Waffenmeisterschaft ---------------------------------------

    /**
     * Öffnet die Laufbahn, auf Wunsch direkt im Baum der zuletzt getauchten Klasse.
     *
     * @param tab Reiter: Bäume in Aufzählungsreihenfolge, danach die Waffenmeisterschaft
     */
    void openCareer(int tab) {
        careerTab = Math.max(0, Math.min(SkillTree.values().length, tab));
        careerNode = null;
    }

    void career(Gui g) {
        var service = nav.service();
        var profile = service.profile();
        var career = profile.career();
        var trees = SkillTree.values();
        g.header(
                "LAUFBAHN",
                "RANG " + career.rank() + " · " + career.points(SkillTree.DEPTH) + " PUNKTE FREI",
                Tone.VIOLET);
        var f = g.frame();
        int tabW = 98, gap = 6;
        int left = CX - ((trees.length + 1) * tabW + trees.length * gap) / 2;
        for (int i = 0; i <= trees.length; i++) {
            boolean weapons = i == trees.length;
            String label = weapons ? "WAFFEN" : trees[i].title().toUpperCase();
            boolean open = weapons || trees[i].open(profile);
            boolean active = i == careerTab;
            if (i == 3 || i == trees.length)
                f.fill(left + i * (tabW + gap) - gap / 2 - 1, 64, 1, 26, 0xFF3A4654);
            if (g.button(
                    "ctab." + i,
                    left + i * (tabW + gap),
                    64,
                    tabW,
                    26,
                    open ? label : "× " + label,
                    active ? Tone.VIOLET : open ? Tone.STEEL : Tone.QUIET,
                    true)) {
                careerTab = i;
                careerNode = null;
            }
        }
        g.text(left, 94, "KONTO", Gui.DIM);
        g.text(left + 3 * (tabW + gap), 94, "KLASSEN", Gui.DIM);
        g.prefer("ctab." + careerTab);
        foot(g);
        if (g.button("back", 16, FOOT + 10, 140, 24, "← ZURÜCK", Tone.QUIET, true)) nav.title();
        // Rangleiste in der Fusszeile
        int barX = 420, barW = 320;
        g.big(barX - 120, FOOT + 12, "RANG " + career.rank(), Pal.VIOLET_5, 2);
        f.fill(barX, FOOT + 16, barW, 10, Pal.OUTLINE);
        f.fill(
                barX + 1,
                FOOT + 17,
                (int) Math.round((barW - 2) * Career.rankProgress(career.xp())),
                8,
                Pal.VIOLET_4);
        g.right(W - 16, FOOT + 17, career.xp() + " EP", Pal.VIOLET_5);
        if (careerTab == trees.length) {
            masteries(g, career);
            return;
        }
        tree(g, profile, trees[careerTab]);
    }

    private void tree(Gui g, ch.zhaw.abyss.application.Profile profile, SkillTree tree) {
        var service = nav.service();
        var career = profile.career();
        var f = g.frame();
        boolean open = tree.open(profile);
        g.panel(8, 108, 944, 260);
        var nodes = tree.nodes();
        int w = 144, h = 70;
        java.util.function.IntUnaryOperator nx = c -> 24 + c * 154;
        java.util.function.IntUnaryOperator ny = r -> 120 + r * 82;
        // Verbindungen zuerst, damit Karten darüber liegen
        for (var node : nodes)
            for (String id : node.requires()) {
                var from = SkillTree.SkillNode.find(id).orElseThrow();
                int x0 = nx.applyAsInt(from.column()) + w, y0 = ny.applyAsInt(from.row()) + h / 2;
                int x1 = nx.applyAsInt(node.column()), y1 = ny.applyAsInt(node.row()) + h / 2;
                int color =
                        career.nodes().contains(id)
                                ? career.nodes().contains(node.id()) ? Pal.TEAL_5 : Pal.VIOLET_4
                                : 0xFF3A4654;
                int mid = (x0 + x1) / 2;
                f.fill(x0, y0, mid - x0, 2, color);
                f.fill(mid, Math.min(y0, y1), 2, Math.abs(y1 - y0) + 2, color);
                f.fill(mid, y1, x1 - mid, 2, color);
            }
        SkillTree.SkillNode shown = null;
        for (var node : nodes) {
            int x = nx.applyAsInt(node.column()), y = ny.applyAsInt(node.row());
            boolean owned = career.nodes().contains(node.id());
            boolean buyable = career.canBuy(node, profile);
            boolean reachable = open && career.reachable(node);
            int accent =
                    owned
                            ? Pal.TEAL_5
                            : buyable ? Pal.VIOLET_4 : reachable ? Pal.STEEL_5 : 0xFF3A4654;
            String id = "node." + node.id();
            if (g.card(id, x, y, w, h, accent, true)) careerNode = node.id();
            if (g.focused(id) && careerNode == null) shown = node;
            if (node.id().equals(careerNode)) shown = node;
            if (node.weapon() != null)
                g.icon(IconArt.weapon(node.weapon()), x + w - 38, y + h - 38, 2);
            g.text(
                    x + 8,
                    y + 4,
                    owned ? "AKTIV" : node.cost() + " P",
                    owned ? Pal.TEAL_5 : buyable ? Pal.VIOLET_5 : Gui.DIM);
            g.wrap(
                    x + 8,
                    y + 22,
                    w - 16,
                    node.title().toUpperCase(),
                    owned ? Gui.TEXT : reachable ? Gui.MUTED : Gui.DIM,
                    2);
            if (node.weapon() == null) g.wrap(x + 8, y + 46, w - 16, node.effect(), Gui.DIM, 2);
        }
        if (shown == null && !nodes.isEmpty()) shown = nodes.getFirst();
        g.screen(8, 376, 944, 106, Tone.VIOLET);
        String points =
                tree.classTree()
                        ? tree.diver().title().toUpperCase()
                                + " · RANG "
                                + career.rank(tree.diver())
                                + " · "
                                + career.points(tree)
                                + " P"
                        : "KONTO · RANG " + career.rank() + " · " + career.points(tree) + " P";
        g.right(940, 386, points, Pal.VIOLET_5);
        if (!open) {
            g.big(24, 388, "GESPERRT", Pal.RED_4, 2);
            g.text(24, 416, tree.requirement(), Gui.MUTED);
            return;
        }
        if (shown == null) return;
        g.big(24, 386, shown.title().toUpperCase(), Gui.TEXT, 2);
        g.wrap(24, 414, 640, shown.effect(), Pal.RUST_6, 3);
        if (!shown.requires().isEmpty() && !career.reachable(shown)) {
            var names = new ArrayList<String>();
            for (String id : shown.requires())
                if (!career.nodes().contains(id))
                    names.add(SkillTree.SkillNode.find(id).orElseThrow().title());
            g.text(24, 460, "Benötigt: " + String.join(", ", names), Gui.DIM);
        }
        boolean owned = career.nodes().contains(shown.id());
        boolean buyable = career.canBuy(shown, profile);
        String label = owned ? "AKTIV" : "FREISCHALTEN · " + shown.cost() + " P";
        if (g.button("buy", 732, 430, 208, 36, label, Tone.VIOLET, buyable && !owned)) {
            if (service.purchase(shown)) {
                nav.toast(
                        shown.weapon() != null
                                ? shown.weapon().title()
                                        + " freigeschaltet · in der Schleuse wählbar"
                                : shown.title() + " freigeschaltet");
            } else nav.toast("Nicht genug Punkte");
        }
    }

    private void masteries(Gui g, Career career) {
        var profile = nav.service().profile();
        var f = g.frame();
        g.panel(8, 108, 944, 374);
        var weapons = Weapon.values();
        for (int i = 0; i < weapons.length; i++) {
            var weapon = weapons[i];
            int col = i % 2, row = i / 2;
            int x = 24 + col * 468, y = 120 + row * 70;
            boolean owned = profile.owns(weapon);
            int level = career.mastery(weapon);
            g.icon(IconArt.weapon(weapon), x, y + 4, 2);
            g.big(x + 44, y + 2, weapon.title().toUpperCase(), owned ? Gui.TEXT : Gui.DIM, 2);
            g.right(
                    x + 440,
                    y + 6,
                    "STUFE " + level + "/10",
                    level >= 10 ? Pal.RUST_6 : Pal.VIOLET_5);
            f.fill(x + 44, y + 24, 396, 8, Pal.OUTLINE);
            f.fill(
                    x + 45,
                    y + 25,
                    (int) Math.round(394 * career.masteryProgress(weapon)),
                    6,
                    Pal.VIOLET_4);
            String note =
                    owned
                            ? "+"
                                    + (4 * level)
                                    + " % Schaden"
                                    + (level >= 5
                                            ? ", +" + (level >= 10 ? 10 : 5) + " % Kritik"
                                            : "")
                            : weapon.career()
                                    ? "Laufbahn: Endknoten eines Klassenbaums"
                                    : "Archiv: für Datenkerne";
            g.text(x + 44, y + 40, note, owned ? Gui.MUTED : Gui.DIM);
        }
        g.center(
                CX + 80,
                FOOT - 22,
                "Abschüsse mit einer Waffe steigern ihre Meisterschaft.",
                Gui.DIM);
    }

    // --- Schleuse: Vorbereitung ------------------------------------------------------------------

    void loadout(Gui g) {
        var service = nav.service();
        var profile = service.profile();
        var f = g.frame();
        g.header("SCHLEUSE", "WER TAUCHT HEUTE?", Tone.AMBER);
        cores(g, profile.cores());
        g.prefer("dive");
        // Spinde mit den Klassen
        var divers = DiverClass.values();
        DiverClass shown = loadout.diver();
        for (int i = 0; i < divers.length; i++) {
            var diver = divers[i];
            int x = 16 + i * 122, y = 70;
            boolean owned = profile.owns(diver);
            boolean selected = loadout.diver() == diver;
            int accent = selected ? Pal.RUST_6 : owned ? Pal.STEEL_5 : Pal.RED_3;
            String id = "diver." + i;
            if (g.card(id, x, y, 114, 178, accent, true)) {
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
            f.fill(x + 8, y + 22, 98, 132, 0xFF0B1118);
            for (int yy = y + 30; yy < y + 152; yy += 12) f.fill(x + 12, yy, 90, 2, 0xFF111922);
            f.fill(x + 44, y + 22, 26, 4, selected ? Pal.RUST_6 : 0xFF2A3440);
            if (selected) f.addRect(x + 20, y + 26, 74, 124, 0xFFFFD890, .10);
            var sprite =
                    nav.bank()
                            .diver(profile.cosmetics(), diver.weapon(), diver)
                            .get(DiverArt.Anim.IDLE)
                            .get(selected ? ((int) (g.time() * 4)) % 4 : 0);
            if (owned) g.sprite(sprite, x + 57, y + 152, 2);
            else {
                g.sprite(silhouette(sprite), x + 57, y + 152, 2);
                g.icon(IconArt.misc("lock"), x + 41, y + 70, 2);
            }
            f.fill(x + 8, y + 156, 98, 14, 0xFF151B22);
            g.center(x + 57, y + 159, shortName(diver), selected ? Pal.RUST_6 : Gui.DIM);
        }
        // Personalakte der fokussierten oder gewählten Klasse
        g.screen(636, 70, 308, 178, Tone.TEAL);
        g.big(648, 80, shown.title().toUpperCase(), Gui.TEXT, 2);
        g.text(
                648,
                104,
                Math.round(shown.health())
                        + " HP  ·  TEMPO "
                        + Math.round(shown.speed() * 100)
                        + " %",
                Pal.TEAL_5);
        int dy = 122 + g.wrap(648, 122, 284, shown.description(), Gui.MUTED, 4) + 8;
        var career = profile.career();
        g.text(
                648,
                dy,
                "LAUFBAHN · RANG "
                        + career.rank(shown)
                        + " · "
                        + career.points(treeOf(shown))
                        + " P FREI",
                Pal.VIOLET_4);
        g.text(
                648,
                dy + 16,
                "START: " + shown.weapon().title() + " · " + shown.module().title(),
                Gui.DIM);
        String status =
                loadout.diver() == shown
                        ? "AUSGEWÄHLT"
                        : profile.owns(shown)
                                ? "ENTER · WÄHLEN"
                                : "KAUFEN · " + shown.unlockCost() + " ♦";
        g.text(
                648,
                230,
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
                        16,
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
                        328,
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
                        640,
                        "DRUCK · MAX " + profile.maxPressure() + "/" + RunSetup.MAX_PRESSURE,
                        IconArt.misc("skull"),
                        loadout.pressure() == 0 ? "Normal" : "Druckstufe " + loadout.pressure(),
                        pressureInfo);
        if (d != 0)
            loadout =
                    loadout.withPressure(
                            Math.floorMod(loadout.pressure() + d, profile.maxPressure() + 1));
        // Startoptionen
        g.panel(16, 368, 612, 104);
        boolean explorer =
                g.toggle(
                        "explorer",
                        36,
                        386,
                        576,
                        "Entdecker · mehr Integrität, mehr Schaden",
                        loadout.explorer());
        if (explorer != loadout.explorer()) loadout = loadout.withExplorer(explorer);
        g.text(36, 430, "SEED", Gui.DIM);
        seed = g.field("seed", 80, 424, 220, seed, "zufällig");
        if (g.button("daily", 316, 422, 292, 26, "TAGESTAUCHGANG", Tone.TEAL, true))
            seed = "" + GameService.dailySeed(LocalDate.now());
        foot(g);
        if (g.button("back", 16, FOOT + 10, 140, 24, "← ZURÜCK", Tone.QUIET, true)) {
            service.rememberLoadout(loadout);
            nav.title();
        }
        if (g.button("wardrobe", 172, FOOT + 4, 180, 32, "GARDEROBE", Tone.STEEL, true)) {
            service.rememberLoadout(loadout);
            nav.wardrobe();
        }
        // Tauchhebel
        g.panel(640, 368, 304, 104);
        g.text(656, 380, "SCHLEUSE BEREIT", Pal.TEAL_5);
        boolean lamp = ((int) (g.time() * 2)) % 2 == 0;
        f.fill(920, 380, 10, 10, lamp ? Pal.GREEN_4 : Pal.GREEN_1);
        if (g.button("dive", 656, 400, 272, 48, "TAUCHEN  →", Tone.AMBER, true)) {
            long value = seed.isBlank() ? System.nanoTime() : Long.parseLong(seed);
            nav.startRun(loadout, value);
        }
        g.center(792, 456, seed.isBlank() ? "ZUFÄLLIGE ROUTE" : "SEED " + seed, Gui.DIM);
    }

    private int selector(
            Gui g, String id, int x, String kicker, Sprite icon, String value, String info) {
        g.panel(x, 260, 304, 98);
        int delta = g.stepper(id, x + 2, 262, 300, 94);
        g.text(x + 36, 270, kicker, Pal.TEAL_5);
        g.icon(icon, x + 34, 288, 2);
        g.big(x + 76, 296, value, Gui.TEXT, 2);
        g.wrap(x + 36, 326, 236, info, Gui.MUTED, 2);
        return delta;
    }

    // --- Garderobe -------------------------------------------------------------------------------

    void wardrobe(Gui g) {
        var service = nav.service();
        var profile = service.profile();
        var f = g.frame();
        g.header("GARDEROBE", "DEIN ANZUG. DEINE FARBEN.", Tone.TEAL);
        cores(g, profile.cores());
        var diver = loadout != null ? loadout.diver() : profile.loadout().diver();
        var weapon = loadout != null ? loadout.weapon() : profile.loadout().weapon();
        var anims = nav.bank().diver(look, weapon, diver);
        // Spiegel
        g.panel(16, 68, 336, 412);
        g.screen(28, 80, 312, 284, Tone.TEAL);
        f.addRect(80, 96, 208, 256, 0xFFB8E0FF, .05);
        var idle = anims.get(DiverArt.Anim.IDLE);
        g.sprite(idle.get(((int) (g.time() * 4)) % idle.size()), 184, 352, 6);
        var attack = anims.get(DiverArt.of(weapon.combo().getFirst().style()));
        var run = anims.get(DiverArt.Anim.RUN);
        var jump = anims.get(DiverArt.Anim.JUMP);
        g.sprite(attack.get(((int) (g.time() * 8)) % attack.size()), 84, 466, 2);
        g.sprite(run.get(((int) (g.time() * 10)) % run.size()), 184, 466, 2);
        g.sprite(jump.get(((int) (g.time() * 3)) % jump.size()), 284, 466, 2);
        // Wahlschalter
        String[] categories = {"suit", "helmet", "visor", "trim"};
        String[] titles = {"ANZUGFARBE", "HELMFORM", "VISIERFARBE", "METALLTON"};
        String[][] names = {
            DiverArt.SUIT_NAMES, DiverArt.HELMET_NAMES, DiverArt.VISOR_NAMES, DiverArt.TRIM_NAMES
        };
        int[] values = {look.suit(), look.helmet(), look.visor(), look.trim()};
        boolean allOwned = true;
        for (int c = 0; c < 4; c++) {
            int y = 68 + c * 94;
            g.panel(368, y, 576, 86);
            String id = Cosmetics.unlockId(categories[c], values[c]);
            boolean owned = profile.owns(id);
            allOwned &= owned;
            int delta = g.stepper("look." + c, 370, y + 2, owned ? 572 : 360, 82);
            if (delta != 0) look = cycle(look, c, delta, names[c].length);
            g.text(404, y + 16, titles[c], Pal.TEAL_5);
            g.big(404, y + 40, names[c][values[c]].toUpperCase(), owned ? Gui.TEXT : Gui.MUTED, 2);
            g.text(660, y + 16, (values[c] + 1) + "/" + names[c].length, Gui.DIM);
            if (!owned) {
                var unlock = Unlock.find(id).orElseThrow();
                g.text(748, y + 16, "GESPERRT", Pal.RED_4);
                if (g.button(
                        "buy." + c,
                        748,
                        y + 38,
                        184,
                        30,
                        unlock.cost() + " ♦ KAUFEN",
                        Tone.AMBER,
                        profile.canBuy(unlock))) {
                    if (service.purchase(unlock)) nav.toast("Freigeschaltet");
                }
            }
        }
        foot(g);
        if (g.button("back", 16, FOOT + 10, 140, 24, "← ZURÜCK", Tone.QUIET, true)) leaveWardrobe();
        if (g.button(
                "apply", W - 16 - 200, FOOT + 2, 200, 38, "ÜBERNEHMEN", Tone.AMBER, allOwned)) {
            if (service.cosmetics(look)) {
                nav.toast("Aussehen gespeichert");
                leaveWardrobe();
            }
        }
        if (!allOwned) g.right(W - 232, FOOT + 16, "ERST FREISCHALTEN", Pal.RED_4);
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

    /** Inhaltsbereich des Archivs rechts neben den Reitern. */
    private static final int AX = 220, AY = 66, AW = 724, AH = 300;

    void archive(Gui g) {
        var profile = nav.service().profile();
        g.header("ARCHIV", "WAS DU FINDEST, BLEIBT.", Tone.TEAL);
        cores(g, profile.cores());
        g.panel(16, 66, 192, 416);
        for (int i = 0; i < TABS.length; i++) {
            boolean active = i == archiveTab;
            if (g.button(
                    "tab." + i,
                    28,
                    80 + i * 44,
                    168,
                    34,
                    TABS[i],
                    active ? Tone.AMBER : Tone.STEEL,
                    true)) {
                archiveTab = i;
                archivePage = 0;
            }
        }
        g.prefer("tab." + archiveTab);
        foot(g);
        if (g.button("back", 16, FOOT + 10, 140, 24, "← ZURÜCK", Tone.QUIET, true)) nav.title();
        g.panel(AX - 4, AY, AW + 8, AH);
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
        int perPage = 20, pages = Math.max(1, (list.size() + perPage - 1) / perPage);
        archivePage = Math.min(archivePage, pages - 1);
        Unlock shown = null;
        for (int i = archivePage * perPage;
                i < Math.min(list.size(), (archivePage + 1) * perPage);
                i++) {
            var unlock = list.get(i);
            int slot = i - archivePage * perPage;
            int x = AX + 6 + (slot % 4) * 180, y = AY + 10 + (slot / 4) * 56;
            boolean owned = profile.owns(unlock.id());
            boolean buyable = profile.canBuy(unlock);
            int accent = owned ? Pal.GREEN_4 : buyable ? Pal.RUST_6 : Pal.STEEL_4;
            String id = "unlock." + unlock.id();
            if (g.card(id, x, y, 172, 50, accent, true) && !owned) {
                if (service.purchase(unlock)) nav.toast(unlock.title() + " freigeschaltet");
                else nav.toast(buyable ? "Nicht möglich" : "Nicht genug Datenkerne");
            }
            if (g.focused(id) || shown == null) shown = unlock;
            var icon = iconFor(unlock);
            if (icon != null) g.icon(icon, x + 8, y + 9, 2);
            g.wrap(x + 48, y + 8, 118, unlock.title(), owned ? Gui.TEXT : Gui.MUTED, 2);
            g.text(
                    x + 48,
                    y + 34,
                    owned ? "FREI" : unlock.cost() + " ♦",
                    owned ? Pal.GREEN_4 : buyable ? Pal.RUST_6 : Gui.DIM);
        }
        g.screen(AX, 376, AW, 106, Tone.TEAL);
        if (pages > 1) {
            if (g.button("prev", AX + AW - 116, 384, 30, 22, "<", Tone.STEEL, true))
                archivePage = Math.floorMod(archivePage - 1, pages);
            g.center(AX + AW - 66, 390, (archivePage + 1) + "/" + pages, Gui.MUTED);
            if (g.button("next", AX + AW - 46, 384, 30, 22, ">", Tone.STEEL, true))
                archivePage = Math.floorMod(archivePage + 1, pages);
        }
        if (shown == null) {
            g.text(AX + 16, 392, "Keine Einträge.", Gui.MUTED);
            return;
        }
        g.big(AX + 16, 388, shown.title().toUpperCase(), Gui.TEXT, 2);
        g.wrap(AX + 16, 414, AW - 160, shown.description(), Gui.MUTED, 3);
        boolean owned = profile.owns(shown.id());
        boolean requirement = shown.requires() == null || profile.owns(shown.requires());
        g.text(
                AX + 16,
                462,
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
                AX + 8,
                AY + 10,
                "ENTDECKT "
                        + profile.discovered().size()
                        + "/"
                        + items.length
                        + "   ·   "
                        + profile.totalKills()
                        + " GEGNER   ·   "
                        + profile.wins()
                        + " SIEGE",
                Pal.TEAL_5);
        Item shown = null;
        for (int i = 0; i < items.length; i++) {
            var item = items[i];
            int x = AX + 8 + (i % 18) * 40, y = AY + 28 + (i / 18) * 40;
            boolean known = profile.discovered().contains(item);
            String id = "item." + item.name();
            g.card(id, x, y, 36, 36, known ? rarityColor(item.rarity()) : Pal.STEEL_3, true);
            if (g.focused(id) || shown == null) shown = item;
            var icon = IconArt.item(item);
            g.icon(known ? icon : silhouette(icon), x + 2, y + 2, 2);
        }
        g.screen(AX, 376, AW, 106, Tone.TEAL);
        boolean known = profile.discovered().contains(shown);
        var icon = IconArt.item(shown);
        g.icon(known ? icon : silhouette(icon), AX + 14, 388, 4);
        int tx = AX + 96;
        if (!known) {
            g.big(tx, 392, "???", Gui.MUTED, 2);
            g.wrap(
                    tx,
                    420,
                    AW - 120,
                    "Noch nicht gefunden. Bergungen, Händler und Kapellen liefern neue Module.",
                    Gui.MUTED,
                    3);
            return;
        }
        g.big(tx, 386, shown.title().toUpperCase(), Gui.TEXT, 2);
        g.text(
                tx,
                410,
                shown.evolution()
                        ? "ENTFESSELT  ·  AUS "
                                + shown.base().title().toUpperCase()
                                + " + "
                                + shown.partner().title().toUpperCase()
                        : shown.rarity().title().toUpperCase()
                                + "  ·  BIS STUFE "
                                + shown.maxStacks(),
                rarityColor(shown.rarity()));
        g.wrap(tx, 426, AW - 120, shown.effect().replace('\n', ' '), Pal.RUST_6, 2);
        g.wrap(tx, 450, AW - 120, shown.description(), Gui.MUTED, 1);
        for (var synergy : Synergy.values())
            if (synergy.first() == shown || synergy.second() == shown) {
                var partner = synergy.first() == shown ? synergy.second() : synergy.first();
                g.text(
                        tx,
                        466,
                        "RESONANZ " + synergy.title().toUpperCase() + " MIT " + partner.title(),
                        Pal.VIOLET_4);
                break;
            }
    }

    private void logbook(Gui g) {
        var earned = nav.service().profile().achievements();
        var entries = Achievement.values();
        g.text(AX + 8, AY + 10, "ERREICHT " + earned.size() + "/" + entries.length, Pal.TEAL_5);
        Achievement shown = null;
        var f = g.frame();
        int rows = (entries.length + 2) / 3;
        for (int i = 0; i < entries.length; i++) {
            var entry = entries[i];
            boolean done = earned.contains(entry);
            // Drei Spalten, damit alle Einträge über das Detailfeld passen.
            int x = AX + 8 + (i / rows) * 240, y = AY + 30 + (i % rows) * 22;
            String id = "feat." + entry.name();
            g.target(id, x, y - 3, 234, 20);
            if (g.focused(id) || shown == null) shown = entry;
            boolean focused = g.focused(id);
            if (focused) f.fill(x, y - 3, 234, 20, 0x40F5C45E);
            f.fill(x + 2, y + 1, 8, 8, done ? Pal.GREEN_4 : 0xFF1A232C);
            f.rect(x, y - 1, 12, 12, done ? Pal.GREEN_2 : Pal.STEEL_3);
            g.text(x + 20, y + 1, entry.title(), done ? Gui.TEXT : focused ? Gui.TEXT : Gui.MUTED);
        }
        g.screen(AX, 376, AW, 106, Tone.TEAL);
        g.big(AX + 16, 388, shown.title().toUpperCase(), Gui.TEXT, 2);
        g.wrap(AX + 16, 416, AW - 32, shown.description(), Gui.MUTED, 2);
        boolean done = earned.contains(shown);
        g.big(
                AX + 16,
                452,
                done ? "ERREICHT" : "BELOHNUNG  +" + shown.reward() + " ♦",
                done ? Pal.GREEN_4 : Pal.RUST_6,
                2);
    }

    private void resonances(Gui g) {
        g.text(AX + 8, AY + 10, "ZWEI MODULE IM SELBEN TAUCHGANG ERGEBEN EINEN BONUS", Pal.TEAL_5);
        var all = Synergy.values();
        Synergy shown = null;
        for (int i = 0; i < all.length; i++) {
            var synergy = all[i];
            int x = AX + 6 + (i % 3) * 240, y = AY + 30 + (i / 3) * 86;
            String id = "syn." + synergy.name();
            g.card(id, x, y, 232, 78, Pal.VIOLET_4, true);
            if (g.focused(id) || shown == null) shown = synergy;
            g.icon(IconArt.item(synergy.first()), x + 10, y + 24, 2);
            g.big(x + 48, y + 32, "+", Gui.MUTED, 2);
            g.icon(IconArt.item(synergy.second()), x + 66, y + 24, 2);
            g.wrap(x + 110, y + 26, 114, synergy.title().toUpperCase(), Pal.VIOLET_4, 3);
        }
        g.screen(AX, 376, AW, 106, Tone.VIOLET);
        g.big(AX + 16, 388, shown.title().toUpperCase(), Pal.VIOLET_5, 2);
        g.text(AX + 16, 414, shown.first().title() + "  +  " + shown.second().title(), Gui.MUTED);
        g.wrap(AX + 16, 434, AW - 32, shown.effect(), Pal.RUST_6, 3);
    }

    static int rarityColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> Pal.STEEL_6;
            case RARE -> Pal.TEAL_5;
            case LEGENDARY -> Pal.RUST_6;
            case CURSED -> Pal.VIOLET_4;
            case MYTHIC -> Pal.MYTHIC;
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
        g.panel(120, 72, 720, 404);
        g.screen(136, 88, 688, 372, Tone.TEAL);
        String[] labels = {"GESAMTLAUTSTÄRKE", "MUSIK", "KAMERAWACKELN"};
        double[] values = {draft.masterVolume(), draft.musicVolume(), draft.screenShake()};
        for (int i = 0; i < 3; i++) {
            g.big(160, 110 + i * 38, labels[i], Gui.MUTED, 1);
            values[i] = g.slider("slider." + i, 420, 108 + i * 38, 300, values[i]);
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
            flags[i] = g.toggle("toggle." + i, 160, 240 + i * 40, 640, toggles[i], flags[i]);
        draft =
                new Settings(
                        values[0], values[1], flags[2], flags[3], flags[4], values[2], flags[0],
                        flags[1]);
        g.prefer("slider.0");
        foot(g);
        if (g.button("back", 16, FOOT + 10, 140, 24, "← ZURÜCK", Tone.QUIET, true)) back(inGame);
        if (g.button("apply", W - 16 - 200, FOOT + 2, 200, 38, "ÜBERNEHMEN", Tone.AMBER, true)) {
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
            {"I / TAB  ·  M", "Ausrüstung mit allen Modulen  ·  Bootskarte"},
            {"ESC · F11 · F12", "Pause · Vollbild · Bildschirmfoto"},
            {
                "MENÜS",
                "Pfeile oder Maus · Enter/Leertaste wählt · Ziffern wählen Karten · Esc zurück"
            }
        };
        g.panel(40, 70, 880, 404);
        g.screen(56, 86, 848, 372, Tone.AMBER);
        for (int i = 0; i < controls.length; i++) {
            int y = 104 + i * 30;
            g.big(80, y, controls[i][0], Pal.RUST_6, 2);
            g.wrap(400, y + 4, 488, controls[i][1], Gui.MUTED, 1);
        }
        g.big(80, 104 + 11 * 30 + 4, "ROTE MARKIERUNGEN KÜNDIGEN ANGRIFFE AN.", Pal.RED_4, 1);
        g.prefer("ok");
        foot(g);
        if (g.button("ok", W - 16 - 200, FOOT + 2, 200, 38, "VERSTANDEN", Tone.AMBER, true))
            back(inGame);
    }
}
