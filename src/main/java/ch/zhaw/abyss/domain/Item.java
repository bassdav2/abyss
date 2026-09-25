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
            2),
    SPIKES(
            "Dornenpanzer",
            "Nahkampfangreifer\nerleiden 14 Schaden",
            "Wer dich packt, blutet Öl.",
            Rarity.COMMON),
    ARC_COIL(
            "Teslaspule",
            "Dritter Treffer:\nKettenblitz 15 Schaden",
            "Springt auf einen zweiten Gegner über.",
            Rarity.RARE,
            3),
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
            "Besiegte Gegner\nexplodieren (20 Schaden)",
            "Instabile Batterien überall.",
            Rarity.RARE,
            2,
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
            2,
            false),
    DEPTH_RUSH(
            "Tiefenrausch",
            "Alle 10 Abschüsse:\n+3 % Schaden (Run)",
            "Je tiefer, desto klarer.",
            Rarity.RARE,
            2,
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
            1,
            false),
    CRIT_DAMAGE(
            "Hohlspitzen", "+40 % kritischer Schaden", "Wenn es sitzt, dann richtig.", Rarity.RARE),
    COMPASS(
            "Messingkompass",
            "+1 Auswahl bei\njeder Bergung",
            "Zeigt immer auf das Wertvollste.",
            Rarity.LEGENDARY,
            1,
            false),
    SECOND_HEART(
            "Notfallkapsel",
            "Einmal wiederbeleben\nmit 50 % Integrität",
            "Ein zweiter Atemzug in der Tiefe.",
            Rarity.LEGENDARY),
    PHASE_CORE(
            "Phasenkern",
            "Ausweichen hinterlässt ein\nexplodierendes Nachbild",
            "Du bist schon weg, wenn es knallt.",
            Rarity.LEGENDARY,
            1,
            false),
    SINGULARITY(
            "Singularitätszelle",
            "−50 % Modul-Abklingzeit\n+25 % Modulschaden",
            "Ein Stern im Taschenformat.",
            Rarity.LEGENDARY,
            1,
            false),
    LEVIATHAN_TOOTH(
            "Leviathanzahn",
            "Kritische Treffer: Kettenblitz\nund +2 Integrität",
            "Vom grössten Wesen der Tiefe.",
            Rarity.LEGENDARY,
            1,
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
            1);
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
            case COMMON -> 3;
            case RARE -> 2;
            case LEGENDARY, CURSED -> 1;
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
            case CURSED -> 0;
        };
    }

    /**
     * @return alle Module, die im normalen Bergungspool auftauchen können
     */
    public static List<Item> lootable() {
        return Arrays.stream(values()).filter(item -> !item.cursed()).toList();
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
