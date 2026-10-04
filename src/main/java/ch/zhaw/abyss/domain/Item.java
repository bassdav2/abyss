package ch.zhaw.abyss.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Passive Module eines Tauchgangs. Werte-Module wirken über {@link StatSheet}; Auslöser-Module
 * werden an klar benannten Stellen im Kampf ({@link Combat}) abgefragt.
 */
public enum Item {
    SERVO(
            "Servoverstärker",
            "+15 % Werkzeugschaden",
            "Mehr Kraft hinter jedem Schlag.",
            Rarity.COMMON),
    PLATING(
            "Verbundpanzerung",
            "−10 % erlittener Schaden",
            "Geschichteter Stahl nimmt die Wucht.",
            Rarity.COMMON),
    CAPACITOR(
            "Kondensator",
            "+20 Energie\n+15 % Modulschaden",
            "Speichert die Energie des Bootes.",
            Rarity.COMMON),
    MEDICAL(
            "Notfallreserve",
            "+20 max. Integrität\nheilt sofort 30",
            "Ein Puffer für den nächsten Raum.",
            Rarity.COMMON),
    COOLANT(
            "Kühlkreislauf",
            "−12 % Abklingzeit\nfür Ausweichen und Modul",
            "Ein kühler Antrieb reagiert schneller.",
            Rarity.COMMON),
    RECOVERY(
            "Rückgewinnung",
            "+3 Integrität\npro Abschuss",
            "Restenergie wird zu deiner Reserve.",
            Rarity.COMMON),
    LANCE(
            "Teleskopstange",
            "+15 % Reichweite",
            "Erreiche Drohnen und halte Abstand.",
            Rarity.COMMON),
    OVERCLOCK(
            "Übertakter",
            "+10 % Angriffstempo",
            "Der Werkzeugantrieb läuft über Nenndrehzahl.",
            Rarity.COMMON),
    THRUSTER(
            "Strömungsantrieb",
            "+10 % Lauf- und\nAusweichtempo",
            "Eine kleine Turbine verändert deinen Rhythmus.",
            Rarity.COMMON),
    SIPHON(
            "Energiesiphon",
            "+4 Energie pro Abschuss\n+1 Energie pro Sekunde",
            "Speist das aktive Modul aus Restladung.",
            Rarity.COMMON),
    REGEN(
            "Reparaturschwarm",
            "+8 Integrität\nnach jedem Raum",
            "Kleine Wartungsdrohnen schliessen die Lecks.",
            Rarity.COMMON),
    MAGNET(
            "Schrottmagnet",
            "+30 % Schrott\ngrösserer Sammelradius",
            "Nichts Metallisches entkommt dir.",
            Rarity.COMMON),
    LENS(
            "Glasfaserlinse",
            "+7 % kritische\nTrefferchance",
            "Findet die Schwachstelle im Panzer.",
            Rarity.COMMON),
    BALLAST("Ballastgurt", "+35 % Rückstoss", "Schwere Gewichte, schwere Schläge.", Rarity.COMMON),
    IGNITER(
            "Zündkerze",
            "+15 % Brandchance",
            "Treffer setzen Öl und Kabel in Brand.",
            Rarity.COMMON),
    CRYO_COIL(
            "Kälteschlange",
            "+15 % Kältechance\nverlangsamt Gegner",
            "Flüssiger Stickstoff im Werkzeugkopf.",
            Rarity.COMMON),
    TOOLBELT(
            "Werkzeuggürtel",
            "+1 Reparaturset\n+1 Kapazität",
            "Mehr Taschen, mehr Überleben.",
            Rarity.COMMON,
            4),
    SPIKES(
            "Dornenpanzer",
            "Nahkampfangreifer\nerleiden 14 Schaden",
            "Wer dich packt, blutet Öl.",
            Rarity.COMMON),
    AREA(
            "Druckkammer",
            "+12 % Wirkungsbereich\nvon Schlägen und Explosionen",
            "Mehr Druck, mehr Fläche.",
            Rarity.COMMON),
    CHARGER(
            "Ladungsverstärker",
            "+15 % Überladung\naus Energiesplittern",
            "Saugt jeden Funken aus den Trümmern.",
            Rarity.COMMON),
    BLOODRUSH(
            "Blutrausch",
            "Abschüsse beschleunigen\nAngriffe kurzzeitig",
            "Je mehr fallen, desto schneller wirst du.",
            Rarity.COMMON),
    ARC_COIL(
            "Teslaspule",
            "Dritter Treffer: Kettenblitz\n15 Schaden, +1 Sprung je Stufe",
            "Springt von Gegner zu Gegner.",
            Rarity.RARE,
            6),
    JETPACK(
            "Druckluftdüse",
            "+1 Sprung in der Luft",
            "Ein kurzer Stoss aus der Pressluftflasche.",
            Rarity.RARE),
    ADRENALINE(
            "Adrenalinpumpe",
            "+30 % Schaden unter\n35 % Integrität",
            "Gefahr schärft die Sinne.",
            Rarity.RARE),
    AMBUSH(
            "Hinterhalt-Protokoll",
            "+35 % Schaden gegen\nabgewandte Gegner",
            "Der Rücken ist nie gepanzert.",
            Rarity.RARE),
    AFTERBURNER(
            "Nachbrenner",
            "Nach dem Ausweichen:\nnächster Treffer +60 %",
            "Schwung in Schaden verwandeln.",
            Rarity.RARE),
    DASH_BLADE(
            "Klingenrumpf",
            "Ausweichen verursacht\n18 Schaden",
            "Scharfe Kanten an deinem Anzug.",
            Rarity.RARE),
    VALVE(
            "Überdruckventil",
            "Bei Treffer: Druckwelle\n25 Schaden",
            "Zu viel Druck muss irgendwohin.",
            Rarity.RARE),
    CHAIN_REACTION(
            "Kettenreaktion",
            "Besiegte Gegner explodieren\n(20 Schaden je Stufe)",
            "Instabile Batterien überall.",
            Rarity.RARE,
            5,
            false),
    NANITES(
            "Nanitenkultur",
            "Lebensraub:\n4 % des Schadens",
            "Winzige Helfer reparieren mit fremdem Metall.",
            Rarity.RARE),
    BARRIER(
            "Barrierenfeld",
            "Blockt den ersten\nTreffer in jedem Raum",
            "Ein Schimmer, der genau einmal hält.",
            Rarity.RARE),
    LANTERN(
            "Lumineszenz",
            "+20 % Schaden an Gegnern\nim Lichtkegel",
            "Deine Lampe zeigt die Schwachstellen.",
            Rarity.RARE,
            4,
            false),
    DEPTH_RUSH(
            "Tiefenrausch",
            "Alle 10 Abschüsse:\n+3 % Schaden (Run)",
            "Je tiefer, desto klarer.",
            Rarity.RARE,
            5,
            false),
    SHIELD_CELL(
            "Schildzelle",
            "+20 Schild, lädt\nausserhalb von Treffern",
            "Ein Energiefeld unter der Jacke.",
            Rarity.RARE),
    HOMING(
            "Zielsucher",
            "Geschosse lenken nach\n+20 % Geschossschaden",
            "Ein kleiner Kreisel in jeder Spitze.",
            Rarity.RARE,
            3,
            false),
    CRIT_DAMAGE(
            "Hohlspitzen", "+40 % kritischer Schaden", "Wenn es sitzt, dann richtig.", Rarity.RARE),
    BLADE_WAVE(
            "Klingenwelle",
            "Schläge schleudern eine\nDruckklinge (40 % Schaden)",
            "Die Luft selbst wird zur Schneide.",
            Rarity.RARE),
    ORBITAL(
            "Kreiselmesser",
            "+1 kreisende Klinge\n(12 Schaden je Treffer)",
            "Drei Rotorblätter, die nie stillstehen.",
            Rarity.RARE,
            6),
    TESLA_FIELD(
            "Teslafeld",
            "Blitzt alle 1,2 s auf\n2 Gegner (+1 je Stufe)",
            "Die Luft um dich knistert.",
            Rarity.RARE),
    MULTISHOT(
            "Mehrfachlader",
            "+1 Geschoss für Harpune,\nKlingen, Drohne, Torpedo",
            "Ein Magazin mit zu vielen Kammern.",
            Rarity.RARE,
            4),
    COMPASS(
            "Messingkompass",
            "+1 Auswahl bei\njeder Bergung",
            "Zeigt immer auf das Wertvollste.",
            Rarity.LEGENDARY,
            2,
            false),
    SECOND_HEART(
            "Notfallkapsel",
            "Einmal wiederbeleben\nmit 50 % Integrität",
            "Ein zweiter Atemzug in der Tiefe.",
            Rarity.LEGENDARY,
            1),
    PHASE_CORE(
            "Phasenkern",
            "Ausweichen hinterlässt ein\nexplodierendes Nachbild",
            "Du bist schon weg, wenn es knallt.",
            Rarity.LEGENDARY,
            3,
            false),
    SINGULARITY(
            "Singularitätszelle",
            "−50 % Modul-Abklingzeit\n+25 % Modulschaden",
            "Ein Stern im Taschenformat.",
            Rarity.LEGENDARY,
            2,
            false),
    LEVIATHAN_TOOTH(
            "Leviathanzahn",
            "Kritische Treffer: Kettenblitz\nund +2 Integrität",
            "Vom grössten Wesen der Tiefe.",
            Rarity.LEGENDARY,
            3,
            false),
    OVERCHARGE(
            "Überladungskern",
            "+6 % Schaden, +6 % Tempo\n+6 Integrität (unbegrenzt)",
            "Wenn nichts mehr passt, wird es heisser.",
            Rarity.LEGENDARY,
            999),
    NOVA(
            "Druckwellenkern",
            "Alle 12 Abschüsse: Nova\n(weniger je Stufe)",
            "Das Boot atmet aus. Alles fliegt.",
            Rarity.LEGENDARY,
            3,
            false),
    GLASS_HULL(
            "Gläserner Rumpf",
            "+40 % Schaden\n−30 % max. Integrität",
            "Stärker, aber zerbrechlich.",
            Rarity.CURSED,
            1),
    GREED(
            "Gier der Tiefe",
            "+60 % Schrott\nGegner +15 % Schaden",
            "Mehr Beute, mehr Wut.",
            Rarity.CURSED,
            1),
    FEVER(
            "Druckfieber",
            "+25 % Angriffstempo\n−1 Energie pro Sekunde",
            "Das Herz rast. Die Batterie auch.",
            Rarity.CURSED,
            1),

    /** Grenzbrecher: hebt die Höchststufe aller Standard- und Seltenmodule. */
    LIMIT_BREAK(
            "Grenzbrecher",
            "+2 Höchststufe für alle\nStandard- und Seltenmodule",
            "Die Sicherungen sind längst durchgebrannt.",
            Rarity.LEGENDARY,
            99,
            false),

    // --- Entfesselungen: ein ausgereiztes Modul verschmilzt mit seinem Partner -----------------

    /** Kreiselmesser und Druckkammer. */
    STORM_BLADES(
            "Klingensturm",
            "Zweiter Rotorring, dreifacher\nSchaden, schleudert Klingen",
            "Ein Wirbel aus Stahl, der nie zur Ruhe kommt.",
            Rarity.MYTHIC,
            1,
            false),
    /** Teslafeld und Teslaspule. */
    THUNDERHEAD(
            "Gewitterkern",
            "Blitze alle 0,4 s in 8 Gegner,\njeder springt dreimal weiter",
            "Im Boot zieht ein Gewitter auf.",
            Rarity.MYTHIC,
            1,
            false),
    /** Kettenreaktion und Druckwellenkern. */
    SUPERNOVA(
            "Supernova",
            "Abschussexplosionen doppelt,\nNova alle 5 Abschüsse",
            "Jeder Treffer ein kleiner Stern.",
            Rarity.MYTHIC,
            1,
            false),
    /** Klingenwelle und Mehrfachlader. */
    TEMPEST(
            "Klingenorkan",
            "Jeder Schlag: Fächer aus fünf\ndurchschlagenden Klingen",
            "Die Luft schneidet in alle Richtungen.",
            Rarity.MYTHIC,
            1,
            false),
    /** Zielsucher und Kondensator. */
    MISSILE_SWARM(
            "Raketenschwarm",
            "Alle 1,2 s: sechs zielsuchende\nMinitorpedos",
            "Ein Magazin voller kleiner Jäger.",
            Rarity.MYTHIC,
            1,
            false),
    /** Glasfaserlinse und Hohlspitzen. */
    DEATH_EYE(
            "Todesblick",
            "+25 % Kritik, Überkritik ×1,5\nkritische Treffer explodieren",
            "Es sieht jede Schwachstelle gleichzeitig.",
            Rarity.MYTHIC,
            1,
            false),
    /** Schildzelle und Verbundpanzerung. */
    BASTION(
            "Bollwerk",
            "+120 Schild, schnelle Ladung\nBruch löst eine Druckwelle aus",
            "Eine Festung, die zurückschlägt.",
            Rarity.MYTHIC,
            1,
            false),
    /** Zündkerze und Übertakter. */
    INFERNO(
            "Höllenglut",
            "+40 % Brand, Brand ×3\nBrennende stecken Nachbarn an",
            "Das Feuer springt von Rumpf zu Rumpf.",
            Rarity.MYTHIC,
            1,
            false),
    /** Kälteschlange und Kühlkreislauf. */
    ABSOLUTE_ZERO(
            "Nullpunkt",
            "Alle 3 s: Frostwelle\nGefrorene erleiden doppelten Schaden",
            "Selbst die Zeit wird spröde.",
            Rarity.MYTHIC,
            1,
            false),
    /** Nanitenkultur und Rückgewinnung. */
    BLOOD_PACT(
            "Blutsauger",
            "+8 % Lebensraub (bis 40 %)\nAbschüsse heilen 1 %",
            "Die Naniten haben Hunger.",
            Rarity.MYTHIC,
            1,
            false),
    /** Phasenkern und Klingenrumpf. */
    PHASE_STORM(
            "Phasensturm",
            "Ausweichen doppelt so oft, Spur\naus Explosionen, Abschüsse laden",
            "Du bist überall und nirgends.",
            Rarity.MYTHIC,
            1,
            false);
    private final String title, effect, description;
    private final Rarity rarity;
    private final int maxStacks;
    private final boolean startsUnlocked;

    Item(String title, String effect, String description, Rarity rarity) {
        this(title, effect, description, rarity, defaultStacks(rarity), true);
    }

    Item(String title, String effect, String description, Rarity rarity, int maxStacks) {
        this(title, effect, description, rarity, maxStacks, true);
    }

    Item(
            String title,
            String effect,
            String description,
            Rarity rarity,
            int maxStacks,
            boolean startsUnlocked) {
        this.title = title;
        this.effect = effect;
        this.description = description;
        this.rarity = rarity;
        this.maxStacks = maxStacks;
        this.startsUnlocked = startsUnlocked;
    }

    private static int defaultStacks(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 8;
            case RARE -> 5;
            case LEGENDARY -> 2;
            case CURSED, MYTHIC -> 1;
        };
    }

    /**
     * @return angezeigter Name
     */
    public String title() {
        return title;
    }

    /**
     * @return Wirkung pro Stufe, zeilenweise
     */
    public String effect() {
        return effect;
    }

    /**
     * @return kurzer Stimmungstext
     */
    public String description() {
        return description;
    }

    /**
     * @return Seltenheit
     */
    public Rarity rarity() {
        return rarity;
    }

    /**
     * @return höchste Stufe innerhalb eines Tauchgangs
     */
    public int maxStacks() {
        return maxStacks;
    }

    /**
     * @return {@code true}, wenn das Modul ohne Freischaltung im Bergungspool liegt
     */
    public boolean startsUnlocked() {
        return startsUnlocked;
    }

    /**
     * @return {@code true} für Module, die nur über Druckkapellen erhältlich sind
     */
    public boolean cursed() {
        return rarity == Rarity.CURSED;
    }

    /**
     * @return Kosten in Datenkernen für die dauerhafte Freischaltung
     */
    public int unlockCost() {
        return switch (rarity) {
            case COMMON -> 4;
            case RARE -> 7;
            case LEGENDARY -> 12;
            case CURSED, MYTHIC -> 0;
        };
    }

    /**
     * @return alle Module, die im normalen Bergungspool auftauchen können
     */
    public static List<Item> lootable() {
        return Arrays.stream(values())
                .filter(
                        item ->
                                !item.cursed()
                                        && !item.evolution()
                                        && item != OVERCHARGE
                                        && item != LIMIT_BREAK)
                .toList();
    }

    /**
     * @return {@code true} für Entfesselungen, die nur aus einem Rezept entstehen
     */
    public boolean evolution() {
        return rarity == Rarity.MYTHIC;
    }

    /**
     * @return Modul, das ausgereizt sein muss, oder {@code null} ausser bei Entfesselungen
     */
    public Item base() {
        return switch (this) {
            case STORM_BLADES -> ORBITAL;
            case THUNDERHEAD -> TESLA_FIELD;
            case SUPERNOVA -> CHAIN_REACTION;
            case TEMPEST -> BLADE_WAVE;
            case MISSILE_SWARM -> HOMING;
            case DEATH_EYE -> LENS;
            case BASTION -> SHIELD_CELL;
            case INFERNO -> IGNITER;
            case ABSOLUTE_ZERO -> CRYO_COIL;
            case BLOOD_PACT -> NANITES;
            case PHASE_STORM -> PHASE_CORE;
            default -> null;
        };
    }

    /**
     * @return Partnermodul, das mindestens einmal installiert sein muss, oder {@code null}
     */
    public Item partner() {
        return switch (this) {
            case STORM_BLADES -> AREA;
            case THUNDERHEAD -> ARC_COIL;
            case SUPERNOVA -> NOVA;
            case TEMPEST -> MULTISHOT;
            case MISSILE_SWARM -> CAPACITOR;
            case DEATH_EYE -> CRIT_DAMAGE;
            case BASTION -> PLATING;
            case INFERNO -> OVERCLOCK;
            case ABSOLUTE_ZERO -> COOLANT;
            case BLOOD_PACT -> RECOVERY;
            case PHASE_STORM -> DASH_BLADE;
            default -> null;
        };
    }

    /**
     * @return alle Entfesselungen in Katalogreihenfolge
     */
    public static List<Item> evolutions() {
        return Arrays.stream(values()).filter(Item::evolution).toList();
    }

    /**
     * Entfesselung, die ein Modul als Grundlage hat.
     *
     * @param base ausgereiztes Modul
     * @return Entfesselung oder {@code null}
     */
    public static Item evolutionOf(Item base) {
        for (var item : values()) if (item.base() == base) return item;
        return null;
    }

    /**
     * Sucht ein Modul tolerant nach Namen, damit alte Spielstände nicht an Umbenennungen scheitern.
     *
     * @param name gespeicherter Enum-Name
     * @return passendes Modul oder {@code null}
     */
    public static Item parse(String name) {
        for (Item item : values()) if (item.name().equals(name)) return item;
        return null;
    }
}
