# Abyss – lokale Werkzeuge

Stand: 21.09.2026. Installations- und Funktionspruefung fuer den geplanten Java/JavaFX-Aufbau. Dieser Ordner ist ein Toolcheck, noch kein Spielprojekt.

## Neu installiert

- Git LFS 3.8.0; globale Git-LFS-Filter aktiviert, keine existierenden Repository-Hooks geaendert.
- PlantUML 1.2026.8 und Graphviz 16.1.0.
- Audacity 4.0.0 unter `/Applications/Audacity 4.app`.
- OpenJDK 25.0.4.1 als eigenstaendige Homebrew-Formel `openjdk@25`.
- JavaFX 26.0.2 (Base, Graphics, Controls, Media, macOS ARM64) und JUnit 6.0.3 ueber Gradle fuer `verification/` heruntergeladen.

Homebrew hat ausserdem benoetigte Bibliotheken aktualisiert und fuer PlantUML OpenJDK 27 installiert. Das unversionierte Homebrew-Java zeigt dadurch auf Java 27. Fuer Abyss wird Java 25 ausdruecklich ueber `env.sh` und die Gradle-Konfiguration dieses Toolchecks ausgewaehlt. Bestehende Java-Versionen wurden nicht entfernt. Es wurden keine Shell-Startdateien geaendert.

## Verwendung

Java 25 in der aktuellen Terminal-Sitzung aktivieren:

```sh
source /Users/davidbass/abyss-tools/env.sh
```

JavaFX/JUnit-Funktionspruefung erneut ausfuehren:

```sh
/Users/davidbass/abyss-tools/check-java.sh --rerun-tasks
```

Diagramm erzeugen:

```sh
plantuml -tsvg /Users/davidbass/abyss-tools/verification/toolcheck.puml
```

## Pruefungen

- Git LFS: Version und konfigurierte Git-Filter geprueft. ZHAW-Serverunterstuetzung noch nicht getestet.
- JavaFX/JUnit: nativen Grafikprozess initialisiert, Controls geladen und ein Canvas gerendert; erwarteter Pixelwert wird durch JUnit geprueft. JavaFX wird ueber den Java-Modulpfad geladen.
- PlantUML/Graphviz: Klassendiagramm als SVG erzeugt, XML und Beschriftungen geprueft.
- Audacity: Installation, Bundle-Version und gestarteter Prozess bestaetigt. UI-Pruefung ueber Computer Use scheiterte an einem Timeout; Audioaufnahme und Export sind noch nicht getestet.
- Blender 5.1.0: Python-Skript im Hintergrund erfolgreich ausgefuehrt.
- FFmpeg 8.1 und jpackage: Versionsaufrufe erfolgreich.

Testbericht: `verification/build/reports/tests/test/index.html`.

## Bereits vorhanden

IntelliJ IDEA, VS Code, Blender, Photoshop, Python, FFmpeg, Git und Gradle. Photoshop-Lizenzstatus ist nicht Teil dieser Pruefung.

## Spaetere Projekteinrichtung

Das eigentliche Team-Repository, dessen Bibliotheksfreigabe gemaess PM3 und die Spielimplementierung folgen separat. Die JavaFX/JUnit-Konfiguration hier dient ausschliesslich der lokalen Bereitschaftspruefung und ist auf diesen Apple-Silicon-Mac zugeschnitten.
