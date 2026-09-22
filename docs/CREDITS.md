# Quellen, Werkzeuge und Lizenzen

## Projektmaterial

ABYSS-Spielidee und Richtungsentscheid: David; gemeinsamer PM3-Projektkontext, Teamzuordnung noch offen. Spielcode, Tests, Modellierungs-/Audioskripte und Dokumentationsentwürfe: mit OpenAI Codex erstellt. Hintergrundplatten: OpenAI Imagegen, Prompts im Projekt. Keine Figuren, Namen, Logos oder Filmszenen aus Snowpiercer wurden als Assets übernommen; die räumliche Bewegung vom hinteren zum vorderen Bereich war eine Konzeptassoziation.

Für den Projektcode wird hier keine neue Open-Source-Lizenz im Namen des Teams erklärt. Eine mögliche Veröffentlichung und Lizenzwahl sind eine eigene Teamentscheidung.

## Enthaltene Drittbestandteile

- **Barlow / Barlow Condensed**, Jeremy Tribby und Mitwirkende, SIL Open Font License 1.1. Die Original-Lizenzdateien liegen bei den Fonts unter `src/main/resources/fonts/`. Herkunft: https://github.com/google/fonts/tree/main/ofl/barlow und https://github.com/google/fonts/tree/main/ofl/barlowcondensed.
- **OpenJFX 26.0.2**, Laufzeitbibliothek. Herkunft: Maven Central, Gruppe `org.openjfx`; Projekt https://openjfx.io/. Die Laufzeit enthält ihre eigenen Lizenz-/Hinweisdateien.
- **OpenJDK 25**, mit der Mac-App gebündelte Laufzeit. Die von `jlink` übernommenen Lizenzdateien unter `legal/` bleiben im Bundle. Herkunft der lokalen Installation: Homebrew-Formel `openjdk@25`.

## Entwicklungswerkzeuge (nicht Teil der Spiellogik)

- Gradle Wrapper 9.3.1: reproduzierbarer Build.
- JUnit 6.0.3: Tests, nicht im Spiel-Bundle.
- Blender 5.1: prozedurale Modelle und Animationsexporte.
- PlantUML und Graphviz: editierbare UML-Diagramme.
- google-java-format 1.36.0: einheitliche Java-Formatierung, nur Werkzeugkonfiguration `formatter`. Offizielle Quelle: https://github.com/google/google-java-format/releases/tag/v1.36.0. Erforderliche JDK-Exports entsprechend https://github.com/google/google-java-format/blob/v1.36.0/README.md.
- Python/NumPy: eigene Klangsynthetisierung; Python/ReportLab: PDF-Arbeitsfassung.

Keine fremden Sound-Samples oder heruntergeladenen Spiel-Spritepacks wurden für diesen Stand verwendet.

## Lokale Kursquellen

Die Originale liegen im persönlichen Semester-Wiki und werden unverändert gelassen. Sie sind nicht als Teil des veröffentlichbaren Spiels kopiert.

1. **Einführung und Kick-off.pdf**, PM3 HS26, physische PDF-Seiten 17 und 19: JavaFX-Richtung, Java-Schwerpunkt, eigene Architektur, Bibliotheksabstimmung. Quellen-ID `PM3-5c00363db893`.
2. **Auftrag Lösungsarchitektur (M2).pdf**, Version 1.0, Seiten 1-3: Quellcode/Javadoc und Technischer Bericht I, Use Cases, Domänenmodell, Architektur, Design, mindestens vier Interaktionsdiagramme, Tests und Projektmanagement. Quellen-ID `PM3-301d1f86d58d`, SHA-256 `6186b9a35464bec07ff327a2cbfded6e87a1dedc27805471733cc731003c5856`.
3. **Verwendung von KI im PM3.pdf**, Version 1.0, Seite 1: Dokumentation von Tool, Ziel, Prompt-Aufwand, Übernahme und Nutzungsart. Quellen-ID `PM3-ddd8548018c7`, SHA-256 `a09a4362f7bd39abfd57e4f0c389d87379822701c36395d2b89bdbbd84652b02`.

Die Kursdateien stammen aus dem lokalen Export vom 14.09.2026. Später geänderte Moodle-Vorgaben sind damit nicht automatisch abgedeckt. Der Bericht ist eine Arbeitsfassung und keine bereits eingereichte oder bewertete Abgabe.
