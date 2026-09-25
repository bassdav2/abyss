package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Achievement;
import ch.zhaw.abyss.domain.ActiveModule;
import ch.zhaw.abyss.domain.Affix;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomTheme;
import ch.zhaw.abyss.domain.ShrineDeal;
import ch.zhaw.abyss.domain.Synergy;
import ch.zhaw.abyss.domain.Weapon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Erzeugt den Inhaltskatalog {@code docs/INHALTE.md} direkt aus den Domänen-Enums. */
public final class ContentCatalog {
    private ContentCatalog() {}

    /**
     * @param args Zieldatei, Vorgabe {@code docs/INHALTE.md}
     * @throws IOException bei Schreibfehlern
     */
    public static void main(String[] args) throws IOException {
        var out =
                new StringBuilder(
                        "# Spielinhalte · Version 1.0\n\n"
                                + "Automatisch aus dem Quellcode erzeugt (`./gradlew"
                                + " contentCatalog`).\n\n");
        out.append(
                "## Klassen\n\n"
                        + "| Klasse | Integrität | Tempo | Waffe | Modul | Kerne | Eigenheit |\n"
                        + "|---|---|---|---|---|---|---|\n");
        for (var d : DiverClass.values())
            out.append(
                    String.format(
                            "| %s | %.0f | %.0f %% | %s | %s | %d | %s |%n",
                            d.title(),
                            d.health(),
                            d.speed() * 100,
                            d.weapon().title(),
                            d.module().title(),
                            d.unlockCost(),
                            d.description()));
        out.append(
                "\n"
                        + "## Waffen\n\n"
                        + "| Waffe | Kombination | Luftangriff | Beschreibung |\n"
                        + "|---|---|---|---|\n");
        for (var w : Weapon.values()) {
            var combo = new StringBuilder();
            for (var s : w.combo())
                combo.append(combo.isEmpty() ? "" : " → ").append(Math.round(s.damage()));
            out.append(
                    String.format(
                            "| %s | %s Schaden | %.0f | %s |%n",
                            w.title(), combo, w.air().damage(), w.description()));
        }
        out.append(
                "\n"
                        + "## Aktive Module\n\n"
                        + "| Modul | Energie | Abklingzeit | Kerne | Wirkung |\n"
                        + "|---|---|---|---|---|\n");
        for (var m : ActiveModule.values())
            out.append(
                    String.format(
                            "| %s | %d | %.1f s | %d | %s |%n",
                            m.title(), m.cost(), m.cooldown(), m.unlockCost(), m.description()));
        out.append(
                "\n"
                        + "## Passive Module\n\n"
                        + "| Modul | Seltenheit | Stufen | Anfangs verfügbar | Wirkung |\n"
                        + "|---|---|---|---|---|\n");
        for (var i : Item.values())
            out.append(
                    String.format(
                            "| %s | %s | %d | %s | %s |%n",
                            i.title(),
                            i.rarity().title(),
                            i.maxStacks(),
                            i.cursed()
                                    ? "nur Kapelle"
                                    : i.startsUnlocked()
                                            ? "ja"
                                            : "Archiv, " + i.unlockCost() + " Kerne",
                            i.effect().replace("\n", " ")));
        out.append(
                "\n"
                        + "## Gegner\n\n"
                        + "| Gegner | Integrität | ab Sektion | Bedrohung | Schrott |\n"
                        + "|---|---|---|---|---|\n");
        for (var e : EnemyKind.values())
            out.append(
                    String.format(
                            "| %s%s | %.0f | %d | %d | %d |%n",
                            e.title(),
                            e.boss() ? " (Boss)" : "",
                            e.baseHealth(),
                            e.minSector() + 1,
                            e.threat(),
                            e.salvage()));
        out.append("\n## Elite-Eigenschaften\n\n");
        for (var a : Affix.values())
            if (a.elite())
                out.append("- **")
                        .append(a.title())
                        .append(":** ")
                        .append(a.description())
                        .append('\n');
        out.append("\n## Druckkapelle\n\n");
        for (var d : ShrineDeal.values())
            out.append("- **")
                    .append(d.title())
                    .append(":** ")
                    .append(d.cost())
                    .append(" → ")
                    .append(d.reward())
                    .append('\n');
        out.append("\n## Raumthemen\n\n");
        for (var t : RoomTheme.values())
            out.append("- ")
                    .append(t.title())
                    .append(
                            t.sector() >= 0
                                    ? " (Sektion " + (t.sector() + 1) + ")"
                                    : " (sektionsübergreifend)")
                    .append('\n');
        out.append("\n## Resonanzen\n\n| Resonanz | Module | Bonus |\n|---|---|---|\n");
        for (var r : Synergy.values())
            out.append(
                    String.format(
                            "| %s | %s + %s | %s |%n",
                            r.title(), r.first().title(), r.second().title(), r.effect()));
        out.append("\n## Raumzustände\n\n");
        for (var c : RoomCondition.values())
            if (c != RoomCondition.NONE)
                out.append("- **")
                        .append(c.title())
                        .append(":** ")
                        .append(c.description())
                        .append('\n');
        out.append("\n## Logbuch\n\n| Eintrag | Bedingung | Kerne |\n|---|---|---|\n");
        for (var a : Achievement.values())
            out.append(String.format("| %s | %s | %d |%n", a.title(), a.description(), a.reward()));
        var target = Path.of(args.length > 0 ? args[0] : "docs/INHALTE.md");
        Files.writeString(target, out.toString());
        System.out.println("CATALOG " + target);
    }
}
