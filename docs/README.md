# Dokumentation ABYSS · Wegweiser

Stand 25.09.2026, Version 1.1. Die Dokumentation folgt den Abgaben im Modul **PM3 Software-Projekt 3 (HS26)**: Projektskizze (M1), Technischer Bericht I zur Lösungsarchitektur (M2) und Technischer Bericht II zum Prototyp (M3), dazu die Präsentationen und die KI-Dokumentation. Die Originalunterlagen aus Moodle (Aufträge, Vorlagen, Folien der Dozierenden) liegen aus urheberrechtlichen Gründen **nicht** im Repository. Die Berichte nennen nur, welche Vorgabe sie umsetzen.

Stellen mit `[TEAM]` ergänzt oder bestätigt das Projektteam: Namen, Rollen, Stunden, eigene Begründungen. Die finalen Abgaben entstehen in der SoE-Vorlage. Die Markdown-Fassungen hier liefern den Inhalt dafür.

## Abgaben nach Meilenstein

| Meilenstein | Termin (HS26) | Dokument | Präsentation |
|---|---|---|---|
| **M1 Projektskizze** | SW3, ab 28.09.2026 | [m1-projektskizze/PROJEKTSKIZZE.md](m1-projektskizze/PROJEKTSKIZZE.md): neun Kapitel, Konkurrenztabelle, Use-Case-Abbildung, IEEE-Quellen | [ABYSS_M1_Projektskizze.pdf](praesentationen/ABYSS_M1_Projektskizze.pdf) + [Sprechtext](praesentationen/ABYSS_M1_Projektskizze_Sprechtext.md) |
| **M2 Lösungsarchitektur** | SW9, ab 09.11.2026 | [m2-loesungsarchitektur/TECHNISCHER_BERICHT_I.md](m2-loesungsarchitektur/TECHNISCHER_BERICHT_I.md): Use Cases, Domänenmodell, Architektur, Design, fünf Systemoperationen mit Interaktionsdiagrammen | [ABYSS_Aufbau_und_Architektur.pdf](praesentationen/ABYSS_Aufbau_und_Architektur.pdf) (Folien 1–9) |
| **M3 Prototyp** | SW13, ab 07.12.2026 | [m3-prototyp/TECHNISCHER_BERICHT_II.md](m3-prototyp/TECHNISCHER_BERICHT_II.md): englisches Abstract, Analyse, Design, Implementation, Resultate, Anhang | [ABYSS_Aufbau_und_Architektur.pdf](praesentationen/ABYSS_Aufbau_und_Architektur.pdf) (Folien 10–17) + [Sprechtext](praesentationen/ABYSS_Aufbau_und_Architektur_Sprechtext.md) |
| **KI-Einsatz** | jede Abgabe | [KI_EINSATZ.md](KI_EINSATZ.md): Tabelle nach PM3-Vorgabe (KI, Ziel, Aufwand, Verwendung, Art) | – |

## Aufbau des Systems (Fachdokumente)

| Dokument | Inhalt |
|---|---|
| [ANFORDERUNGEN.md](ANFORDERUNGEN.md) | Use Cases UC-01 bis UC-11, UC-02 fully dressed, funktionale (F-xx) und Qualitätsanforderungen (Q-xx) |
| [ARCHITEKTUR.md](ARCHITEKTUR.md) | Schichten, Pakete, Architekturentscheidungen ADR-01 bis ADR-10, Muster, Ereignisse |
| [TESTSTRATEGIE.md](TESTSTRATEGIE.md) | Teststufen, 106 JUnit-Tests, UI-Prüfung, Render-Dauertest, Balance-Testspieler |
| [GLOSSAR.md](GLOSSAR.md) | Fachbegriffe (Tauchgang, Bergung, Raumzustand, Resonanz …) |
| [INHALTE.md](INHALTE.md) | Generierter Katalog aller Inhalte: Gegner, Module, Räume, Zustände, Logbuch |
| [PROJEKTMANAGEMENT.md](PROJEKTMANAGEMENT.md) | Iterationen, Planung, offene Punkte |
| [ASSET_PIPELINE.md](ASSET_PIPELINE.md) · [CREDITS.md](CREDITS.md) | Wie Grafik und Klang entstehen, Herkunft und Lizenzen |
| [MORGEN.md](MORGEN.md) | Spielerischer Überblick über Version 1.1 für David |
| [WORK_PLAN.md](WORK_PLAN.md) | Arbeitsjournal der Entwicklungsläufe |

## UML-Diagramme

Quellen als PlantUML (`.puml`), gerendert als SVG und PNG. Neu erzeugen: `python3 tools/write_diagrams.py`.

| Nr. | Diagramm | Verwendet in |
|---|---|---|
| 01 | [Use-Case-Übersicht](diagrams/01-use-cases.svg) | M1, M2 |
| 02 | [Domänenmodell](diagrams/02-domain.svg) | M2 |
| 03 | [Logische Architektur](diagrams/03-architecture.svg) | M2, M3 |
| 04 | [Design-Klassendiagramm](diagrams/04-design-classes.svg) | M2 |
| 05 | [Systemsequenzdiagramm UC-02](diagrams/05-ssd-progress.svg) | M2 |
| 06 | [Sequenz: Tauchgang starten](diagrams/06-start-sequence.svg) | M2 |
| 07 | [Sequenz: Kampfschritt](diagrams/07-combat-sequence.svg) | M2 |
| 08 | [Sequenz: Bergung wählen](diagrams/08-reward-sequence.svg) | M2 |
| 09 | [Sequenz: Raumwechsel und Sicherung](diagrams/09-room-sequence.svg) | M2 |
| 10 | [Sequenz: Fortsetzen](diagrams/10-resume-sequence.svg) | M2 |
| 11 | [Zustandsmaschine Gegner](diagrams/11-enemy-state.svg) | M3 |
| 12 | [Bildschirmnavigation](diagrams/12-navigation.svg) | M2, M3 |
| 13 | [Render-Pipeline](diagrams/13-render-pipeline.svg) | M3 |

## Präsentationen

Alle Foliensätze und Sprechtexte: [praesentationen/README.md](praesentationen/README.md). Die aktuellen PDFs erzeugt `python3 tools/create_slides.py` aus echten Spielszenen, Bildschirmfotos und den UML-Diagrammen. Frühere Präsentationen (Konzeptdemo, SWEN1-Abgabe, Konzept v2) liegen unter [praesentationen/archiv](praesentationen/archiv).

## Nachweise und Qualitätssicherung

- [qa/test-summary.json](qa/test-summary.json): JUnit, Format, Javadoc, Balance je Klasse
- [qa/render-soak.json](qa/render-soak.json): 60-Sekunden-Dauertest, 30 733 Bilder, Ø 1,9 ms pro Bild
- [qa/ui-smoke.txt](qa/ui-smoke.txt): 37 Schritte durch alle Bildschirme im echten JavaFX-Fenster
- [qa/screens](qa/screens): Bildschirmfotos aller Menüs und Spielsituationen

## Planung und Geschichte

| Dokument | Stand |
|---|---|
| [planung/2026-09-18_Ideenpool.md](planung/2026-09-18_Ideenpool.md) | ursprüngliche Ideensammlung |
| [planung/2026-09-21_Toolchain-Setup.md](planung/2026-09-21_Toolchain-Setup.md) | lokale Werkzeuge (JDK 25, JavaFX, PlantUML, Git LFS) |
| [planung/2026-09-21_Produktionsplan.md](planung/2026-09-21_Produktionsplan.md) | Produktionsplan bis 1.0 |
| [KONZEPT.md](KONZEPT.md) · [CONCEPT-PROMPTS.md](CONCEPT-PROMPTS.md) | Konzept v2 (Roguelite im langen Boot) und Bildprompts |
| [EXPANSION.md](EXPANSION.md) · [ART_PROMPTS.md](ART_PROMPTS.md) | Ausbau 0.2 mit ImageGen-Hintergründen (inzwischen ersetzt) |
| [DEMO.md](DEMO.md) | Gameplay-Video aus dem echten Renderer |
