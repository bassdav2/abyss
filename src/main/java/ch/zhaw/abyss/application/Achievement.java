package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.Rarity;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.Synergy;

import java.util.Set;

/**
 * Einträge im Logbuch: dauerhafte Ziele über viele Tauchgänge. Jeder Eintrag wird genau einmal
 * erreicht und bringt einmalig Datenkerne.
 */
public enum Achievement {
    /** Ersten Raum gesichert. */
    FIRST_ROOM("Erster Schritt", "Sichere den ersten Raum.", 2),
    /** Erster Sektorwächter. */
    WARDEN("Schottbrecher", "Besiege den Schottmeister.", 4),
    /** Zweiter Sektorwächter. */
    REACTOR("Kernschmelze", "Besiege den Reaktorkern.", 5),
    /** Dritter Sektorwächter. */
    BROOD("Brutkasten", "Besiege die Brutmutter.", 6),
    /** Sieg über den Lotsen. */
    BRIDGE("Kommando übernommen", "Erobere die Brücke.", 8),
    /** Zweiter Zyklus. */
    CYCLE("Tiefer als tief", "Erreiche den zweiten Zyklus.", 6),
    /** Sieg unter hohem Druck. */
    PRESSURE("Unter Druck", "Erobere die Brücke auf Druckstufe 3 oder höher.", 10),
    /** Viele Module entdeckt. */
    COLLECTOR("Sammler", "Entdecke 20 verschiedene Module.", 5),
    /** Alle Module entdeckt. */
    CODEX("Vollständiges Kompendium", "Entdecke alle regulären Module.", 12),
    /** Drei legendäre Module gleichzeitig. */
    LEGENDS("Legendenschmiede", "Trage drei legendäre Module in einem Tauchgang.", 6),
    /** Sieg mit einem verfluchten Modul. */
    CURSED("Fluch als Werkzeug", "Erobere die Brücke mit einem verfluchten Modul.", 8),
    /** Viele Abschüsse. */
    HUNTER("Jäger der Tiefe", "Besiege insgesamt 500 Gegner.", 6),
    /** Viel Schrott auf einmal. */
    HOARDER("Schrottkönig", "Besitze 250 Schrott gleichzeitig.", 4),
    /** Drei Resonanzen gleichzeitig. */
    RESONANCE("Im Einklang", "Aktiviere drei Resonanzen in einem Tauchgang.", 5),
    /** Hüllenbruch überstanden. */
    SEALED("Dicht gehalten", "Überstehe einen Hüllenbruch.", 4),
    /** Raum mit Schlagseite gesichert. */
    SEAWORTHY("Seefest", "Sichere einen Raum mit Schlagseite.", 3),
    /** Schmugglerdrohne abgeschossen. */
    SMUGGLER("Schmugglerjagd", "Schiess eine Schmugglerdrohne ab, bevor sie entkommt.", 4),
    /** Raum im Stromausfall gesichert. */
    DARKNESS("Im Dunkeln", "Sichere einen Raum im Stromausfall.", 3),
    /** Raum unter Alarm gesichert. */
    RED_ALERT("Rote Welle", "Sichere einen Raum unter Alarmstufe Rot.", 3),
    /** Raum mit Druckleck gesichert. */
    FLOODED("Nasse Füsse", "Sichere einen Raum mit Druckleck.", 3),
    /** Sieg als Mechanikerin. */
    WIN_MECHANIC("Die Mechanikerin am Steuer", "Erobere die Brücke als Mechanikerin.", 4),
    /** Sieg als Harpunier. */
    WIN_HARPOONER("Der Harpunier am Steuer", "Erobere die Brücke als Harpunier.", 4),
    /** Sieg als Schweisserin. */
    WIN_WELDER("Die Schweisserin am Steuer", "Erobere die Brücke als Schweisserin.", 4),
    /** Sieg als Koloss. */
    WIN_TITAN("Der Koloss am Steuer", "Erobere die Brücke als Koloss.", 4),
    /** Sieg als Funkerin. */
    WIN_SPARK("Die Funkerin am Steuer", "Erobere die Brücke als Funkerin.", 4),
    /** Sieg mit allen Klassen. */
    ALL_CLASSES("Die ganze Crew", "Erobere die Brücke mit jeder Klasse.", 15);

    private final String title;
    private final String description;
    private final int reward;

    Achievement(String title, String description, int reward) {
        this.title = title;
        this.description = description;
        this.reward = reward;
    }

    /**
     * @return Anzeigename
     */
    public String title() {
        return title;
    }

    /**
     * @return Bedingung in einem Satz
     */
    public String description() {
        return description;
    }

    /**
     * @return einmalige Datenkerne
     */
    public int reward() {
        return reward;
    }

    /**
     * Prüft die Bedingung gegen den aktuellen Tauchgang und das bereits aktualisierte Profil.
     *
     * @param run laufender oder beendeter Tauchgang
     * @param profile Profil nach Verbuchung des Fortschritts
     * @param earned bisher und in diesem Durchgang erreichte Einträge
     * @return {@code true}, wenn die Bedingung erfüllt ist
     */
    boolean met(GameRun run, Profile profile, Set<Achievement> earned) {
        boolean win = run.phase() == GameRun.Phase.VICTORY;
        int passed =
                run.cycle() > 0 || win
                        ? RoomGenerator.ROOM_COUNT
                        : run.phase() == GameRun.Phase.ROOM_CLEARED
                                ? run.room().depth() + 1
                                : run.room().depth();
        var items = run.player().items().keySet();
        return switch (this) {
            case FIRST_ROOM -> passed >= 1;
            case WARDEN -> passed >= 5;
            case REACTOR -> passed >= 11;
            case BROOD -> passed >= 17;
            case BRIDGE -> win;
            case CYCLE -> run.cycle() >= 1;
            case PRESSURE -> win && run.pressure() >= 3;
            case COLLECTOR -> profile.discovered().size() >= 20;
            case CODEX -> profile.discovered().containsAll(Item.lootable());
            case LEGENDS ->
                    items.stream().filter(item -> item.rarity() == Rarity.LEGENDARY).count() >= 3;
            case CURSED -> win && items.stream().anyMatch(Item::cursed);
            case HUNTER -> profile.totalKills() >= 500;
            case HOARDER -> run.player().salvage() >= 250;
            case RESONANCE -> Synergy.activeIn(run.player().items()).size() >= 3;
            case DARKNESS -> run.clearedConditions().contains(RoomCondition.BLACKOUT);
            case SEALED -> run.clearedConditions().contains(RoomCondition.BREACH);
            case SEAWORTHY -> run.clearedConditions().contains(RoomCondition.LIST);
            case SMUGGLER -> run.smugglersCaught() > 0;
            case RED_ALERT -> run.clearedConditions().contains(RoomCondition.ALARM);
            case FLOODED -> run.clearedConditions().contains(RoomCondition.LEAK);
            case WIN_MECHANIC -> win && run.setup().diver() == DiverClass.MECHANIC;
            case WIN_HARPOONER -> win && run.setup().diver() == DiverClass.HARPOONER;
            case WIN_WELDER -> win && run.setup().diver() == DiverClass.WELDER;
            case WIN_TITAN -> win && run.setup().diver() == DiverClass.TITAN;
            case WIN_SPARK -> win && run.setup().diver() == DiverClass.SPARK;
            case ALL_CLASSES ->
                    earned.containsAll(
                            Set.of(WIN_MECHANIC, WIN_HARPOONER, WIN_WELDER, WIN_TITAN, WIN_SPARK));
        };
    }
}
