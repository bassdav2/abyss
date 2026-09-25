# Quellen, Werkzeuge und Lizenzen

## Projektmaterial

ABYSS-Spielidee und Richtungsentscheide: David; gemeinsamer PM3-Projektkontext, Teamzuordnung noch offen. Version 0.1/0.2: Code, Tests, Skripte und Dokumentationsentwürfe mit OpenAI Codex, Hintergrundplatten mit OpenAI ImageGen. Version 1.0: Code, prozedurale Pixel-Art, zusätzliche Klänge, Tests und Dokumentation mit Anthropic Claude (Claude Code). Keine Figuren, Namen, Logos oder Filmszenen aus Snowpiercer wurden übernommen; die Bewegung vom hinteren zum vorderen Ende ist eine Konzeptassoziation.

Für den Projektcode wird hier keine Open-Source-Lizenz im Namen des Teams erklärt. Veröffentlichung und Lizenzwahl sind eine Teamentscheidung.

## Enthaltene Drittbestandteile

- **OpenJFX 26.0.2**, Laufzeitbibliothek. Herkunft: Maven Central, Gruppe `org.openjfx`; https://openjfx.io/.
- **OpenJDK 25**, mit der Mac-App gebündelte Laufzeit (jlink); Lizenzdateien unter `legal/` im Bundle. Herkunft der lokalen Installation: Homebrew `openjdk@25`.

Seit Version 1.1 werden keine fremden Schriften mehr ausgeliefert: Menüs, HUD und Karten nutzen ausschliesslich die eigene Pixelschrift. Die in 1.0 verwendeten OFL-Schriften (Pixelify Sans, Silkscreen, Jersey 10) und die frühere Schrift Barlow sind entfernt.

## Eigene Inhalte

- **Pixelgrafik:** vollständig prozedural in Java (`ui/art`), keine Spritepacks, keine Bild-KI.
- **Schrift:** eigene 5×7-Bitmap-Schrift in `src/main/resources/pixel/font.txt` für HUD und alle Menüs.
- **Klänge und Musik:** eigene NumPy-Synthese (`tools/create_audio.py`, `tools/create_audio_extra.py`), keine Fremd-Samples.
- **App-Signet:** `tools/MakeIcon.java` aus der Figurengrafik.

## Entwicklungswerkzeuge (nicht Teil der Spiellogik)

- Gradle Wrapper 9.3.1, JUnit 6.0.3, google-java-format 1.36.0 (AOSP-Stil).
- PlantUML und Graphviz für die UML-Quellen in `docs/diagrams`.
- Python mit NumPy (Klangsynthese) und Pillow (nur für QA-Übersichten); ffmpeg für das Demo-Video.
- Blender 5.1 für die historischen Modelle in `art-source` (in 1.0 nicht mehr verwendet).

## Lokale Kursquellen

1. **Einführung und Kick-off.pdf**, PM3 HS26, Seiten 17 und 19: JavaFX als erste Wahl, Controller ohne Fachlogik, Java, eigene geschichtete Architektur, keine Frameworks, Bibliotheken mit Dozierenden absprechen. Quellen-ID `PM3-5c00363db893`.
2. **Auftrag Lösungsarchitektur (M2).pdf** und **Auftrag Prototyp und technischer Bericht II (M3).pdf**: Quellcode mit Javadoc, Use Cases, Domänenmodell, Architektur, Design, Interaktionsdiagramme, Tests; Bewertungsaspekte des Prototyps.
3. **Verwendung von KI im PM3.pdf**, Seite 1: Dokumentation von Tool, Ziel, Prompt-Aufwand, Übernahme und Nutzungsart. Quellen-ID `PM3-ddd8548018c7`.

Die Kursdateien stammen aus den lokalen Exporten vom 14./15.09.2026; spätere Moodle-Änderungen sind nicht abgedeckt.
