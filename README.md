# ABYSS · Vom Heck bis zur Brücke

Ein lokales 2D-Roguelite in Java/JavaFX: 18 Räume vom Heck eines riesigen U-Boots bis zur Brücke, mit Kampf, Werkstätten, Ausrüstung und wiederholbaren Tauchzyklen.

**Spielbarer Entwicklungsstand 0.2 vom 22.09.2026.** Weitgehend mit Codex erstellt. Teamreview, menschliches Spielgefühl und fachliche Modulfreigabe stehen noch aus. Der komplette Nachtlauf ist unter `docs/` nachvollziehbar dokumentiert.

## Sofort spielen

Auf Davids Mac **`dist/Abyss.app` doppelklicken**. Java ist im App-Bundle enthalten. Für den ersten Versuch in der Vorbereitung **Entdecker** aktivieren.

Alternativ liegt das Mac-Paket als ZIP unter `release/`. Entpacken und `Abyss.app` öffnen. Gebaut für **Apple Silicon macOS**, lokal signiert, nicht notarisiert. Andere Plattformen sind bisher nicht als Paket geprüft.

## Steuerung

| Aktion | Taste |
|---|---|
| Bewegen | A / D oder Pfeiltasten |
| Springen | Leertaste, W oder Pfeil hoch |
| Angreifen | J oder linke Maustaste; halten möglich |
| Blickrichtung beim Mausangriff | Maus links/rechts von der Figur |
| Ausweichen | Shift |
| Aktives Modul | K oder rechte Maustaste |
| Fund / Werkstatt / rechtes Schott | E in der Nähe |
| Reparaturset | Q; +35 Integrität, bis zu drei Sets mitnehmbar |
| Inventar / Bootskarte | I oder Tab / M; pausiert das Spiel |
| Pause | Esc; automatisch bei Fokusverlust |
| Vollbild | F11 |
| Screenshot | F12, Datei auf dem Schreibtisch |

Rote Markierungen kündigen Angriffe an. Nach dem Kampf den Fund in der Raummitte bergen und zum rechten Schott gehen. Werkstätten reparieren einmal gratis und bieten ein Modul für 15 Schrott. Elitewege sind schwerer und geben zwei Modulstufen; sichere Depots geben Vorräte. Beim Boss die Erholungsphase nutzen: Dann ist sein Kern offen. Nach dem Sieg kannst du mit deinem Build einen schwereren Zyklus beginnen.

## Enthalten

- Drei Sektionen, 18 Raumpositionen und 15 Hintergrundplatten.
- Drei reguläre Gegnertypen und drei unterschiedliche Bosse: Schottmeister, Reaktorkern und Lotse.
- Schildimpuls, Lichtbogen und Druckschild; zwölf passive Module mit je drei Stufen.
- Mehrere Patrouillen, Dampf-/Stromgefahren, Werkstätten und Routenentscheidungen.
- Lokale Bauplanfreischaltungen, Raum-Sicherung, Pause, Hilfe und Optionen.
- Sieben editierbare Blender-Modelle, 118 Animationsframes und 19 eigene Klänge/Musikstücke.
- Zerstörbare Vorratskisten, Inventar mit Item-Icons, Bootskarte und Raumbilder bei der Routenwahl.
- Lichtkegel, Stirnlampe, Parallaxe, Vordergrundnebel und sektorspezifische Musik.

Die Route kombiniert begrenzte Inhalte; Sektorreihenfolge und Werkstattpositionen bleiben fest. Eine menschliche Run-Dauer von 20-30 Minuten ist nicht belegt und für diesen kompakten Stand kein bestätigtes Ergebnis.

Details zum Ausbau: `docs/EXPANSION.md`. Neue Bosse an Raum 5/11/18; Werkstätten an 6/12. Vorratskisten mit J zerschlagen.

## Speicherstände

macOS: `~/Library/Application Support/Abyss`. Gesichert wird der **Raumeingang**. Nach Hauptmenü oder Neustart beginnst du dort erneut; Weiter aus der Pause erhält den aktuellen Kampf. Baupläne und Einstellungen bleiben separat erhalten. Vor dem Ersetzen beschädigter Dateien werden Originale als `.bak` kopiert. Ältere 12-Raum-Saves werden auf die längere Route migriert; ihre Originale bleiben beim nächsten Speichern ebenfalls als Backup erhalten.

QA verwendet separate Verzeichnisse unter `build/` oder temporäre Verzeichnisse. Die mitgelieferten Demoaufnahmen und Screenshots verwenden teilweise vorbereitete QA-Spielstände; sie sind keine menschlichen Testergebnisse.

## Quellcode starten

Voraussetzung: **JDK 25** und beim ersten Gradle-Build Internetzugang.

```sh
# Auf Davids eingerichtetem Mac:
source /Users/davidbass/abyss-tools/env.sh
./gradlew run
```

Oder `run.command` doppelklicken. Auf einem anderen Rechner `JAVA_HOME` auf ein JDK 25 setzen; anschließend `./gradlew run` bzw. `gradlew.bat run`. JavaFX lädt Gradle passend zur Plattform. Der tatsächliche Spieltest ist bisher nur auf dem lokalen Mac erfolgt.

## Prüfen und bauen

```sh
./gradlew test checkJavaFormat javadoc
./gradlew uiSmoke
./gradlew renderSoak --args='--seconds=180 --speed=4'
python3 tools/package_mac.py
```

Derzeit 53 JUnit-Testfälle/-konfigurationen, darunter 144 Kampagnensimulationen. Zusätzlich 23 Prüfungen innerhalb von JavaFX. Ein 180-Sekunden-Render-Probelauf durchlief mehrere vollständige Runs ohne unbehandelte Ausnahme. Details und Grenzen: `docs/TESTSTRATEGIE.md`.

Weitere Werkzeuge:

```sh
./gradlew formatJava
python3 tools/capture_views.py
./gradlew renderDemo
python3 tools/write_diagrams.py
python3 tools/asset_manifest.py
```

Für Audio-/PDF-Werkzeuge optional eine Python-Umgebung mit `tools/requirements.txt` einrichten. Auf diesem Mac besteht `.venv-tools`:

```sh
.venv-tools/bin/python tools/create_audio.py
.venv-tools/bin/python tools/create_report.py
/Applications/Blender.app/Contents/MacOS/Blender --background --python tools/render_actors.py
```

## Orientierung

| Pfad | Inhalt |
|---|---|
| `src/main/java/ch/zhaw/abyss/domain` | Spiellogik ohne JavaFX oder Dateizugriff |
| `application` / `ports` | Anwendungsfälle, Profil und Speichervertrag |
| `infrastructure` / `ui` | Dateispeicher, Audio und native Darstellung |
| `src/test` / `src/qa` | JUnit-Tests, Testspieler, UI-Prüfung und Demoexport |
| `art-source` / `tools` | Editierbare Assets und Produktionsskripte |
| `docs` | Anforderungen, Architektur, KI-Einsatz, Risiken, Credits und zwölf UML-Quellen |
| `output/pdf` | 21-seitiger technischer Bericht als Arbeitsfassung |
| `output/video` | 120 Sekunden gekennzeichnete Gameplay-Demo |
| `build/docs/javadoc` | Generierte API-Dokumentation |
| `release` | Mac-ZIP, Quellpaket mit Javadoc und Prüfsummen |

JavaFX 26.0.2 ist die einzige Produktionsbibliothek. JUnit 6.0.3 und google-java-format 1.36.0 sind Test-/Build-Werkzeuge. Gradle Wrapper: 9.3.1. Quellen und Lizenzen: `docs/CREDITS.md`. Das frühere Konzept bleibt unter `docs/KONZEPT.md` ausdrücklich als historischer Plan erhalten.
