# Teststrategie und Prüfnachweise

Stand 22.09.2026. Ergebnisse gelten für den lokal geprüften Entwicklungsstand auf Apple Silicon macOS. Automatisierte Tests und visuelle Screenshots sind keine menschlichen Usability- oder Hörtests.

## Ebenen

1. **Unit/Domain:** Bewegung, Richtungen, Treffer, Dash, Energie, Schaden, Belohnungsregeln, Obergrenzen, Raumabschluss, Boss-Panzerung, Gefahren und Save-Invarianten. Keine JavaFX-Initialisierung, keine Dateisystemzugriffe. Die Tests adressieren Spielregeln und Fehlergrenzen.
2. **Integration/Persistenz:** Temporäre Verzeichnisse, tatsächliches UTF-8-Dateiformat, neuer Repository-/Service-Aufruf nach Speichern. Unbekannte Versionen, beschädigte Werte und Sicherungskopien werden geprüft.
3. **Kampagnensimulation:** `CampaignPilot` nutzt öffentliche `InputFrame`-Eingaben. Keine direkte Mutation von Gegnergesundheit, keine unverwundbare Figur. Je zwölf Seeds für drei Startmodule, zwei Schwierigkeiten und zwei Routenpräferenzen: 144 Runs. Der Test verlangt mindestens einen Sieg je Konfiguration und keinen blockierten Run. Im aufgezeichneten Lauf gewannen alle 144.
4. **JavaFX-Komponenten:** `UiSmoke` löst JavaFX-KeyEvents und Button-Aktionen innerhalb der Anwendung aus. Geprüft sind Start/Seed, Bewegung, Pause/Weiter, Hilfe, Optionen, Werkstatt, Modulwahl, Routenwahl, persistierter Raumwechsel und Fortsetzen. 18 Prüfungen. Keine Betriebssystem-Eingaben.
5. **Rendering/Audio:** `RenderSoak` zeichnet reale JavaFX-Frames und spielt automatisierte Runs. Native AudioClip-Objekte werden aus allen 16 Dateien geladen und mit Lautstärke null aufgerufen. Der 180-Sekunden-Lauf mit vierfacher Simulationszeit durchlief acht vollständige Runs ohne unbehandelte Ausnahme. Eine stumme Initialisierungsprüfung kann keine Klangqualität bestätigen.
6. **Visuelle Layoutprüfung:** Tatsächlich gerenderte JavaFX-Screens von Titel, Vorbereitung, Archiv, Optionen, Hilfe, Pause, Route, Bergung und Gameplay. Kleinstes Fenster separat geprüft. Keine aus Konzeptbildern behaupteten Spielansichten.
7. **Auslieferung:** Separates `.app`-Bundle mit eigener Laufzeit starten; Ressourcen, Save-Pfad und Screenshot aus dem Bundle prüfen. ZIP und Quellpaket auf vorhandene Dateien und Prüfsummen prüfen. Beide Archive wurden separat entpackt: Die Mac-App startet und ihre Signatur ist gültig; das Quellprojekt baut in einem zuvor leeren Projektverzeichnis mit allen 41 Tests, Formatprüfung und Javadoc. Der vorhandene Gradle-Abhängigkeitscache wurde weiterverwendet. Details: `qa/release-check.json`.

## Reproduktion

```sh
./gradlew test checkJavaFormat
./gradlew uiSmoke
./gradlew renderSoak --args='--seconds=180 --speed=4'
python3 tools/capture_views.py
python3 tools/package_mac.py
```

Java 25 muss über `JAVA_HOME` oder eine vom Gradle-Toolchain-Mechanismus erkannte Installation verfügbar sein. `run.command` findet auf Davids Mac die eingerichtete Version. Tests verwenden temporäre bzw. `build/`-Verzeichnisse. Der normale Spielstand wird nicht für QA benutzt.

## Konkrete Grenzen

- Die Kampf-Tests verwenden teilweise gezielt gesetzte Gegnerzustände, um Grenzfälle isoliert zu prüfen. Nur die separate Kampagnensimulation ist ein Nachweis kompletter Runs mit regulären Spieleingaben.
- Der Bot erkennt Angriffsvorwarnungen exakt, wählt Upgrades ohne Lesepause und wechselt Räume ohne die menschliche Laufstrecke bis zum Schott. Seine Zeiten sind keine erwartete Spieldauer.
- Die Aufzeichnung der Renderkosten umfasst CPU-Simulation und Zeichenbefehle, nicht die spätere GPU-Fertigstellung. Das Ergebnis gilt nicht automatisch für andere Rechner.
- Während des Nachtlaufs war der Mac für normale Desktop-Bedienung gesperrt. Maus-/Tastaturgefühl am Betriebssystem, Vollbild-/Monitorwechsel und akustische Abmischung bleiben manuell zu prüfen.
- Keine Windows-/Linux-Auslieferung oder Notarisierung wurde dadurch bestätigt.

## Morgen: kurzer manueller Abnahmelauf

| Schritt | Erwartetes Ergebnis | Ist-Status |
|---|---|---|
| App per Doppelklick starten | Titel erscheint ohne Terminal/Java-Installation | Vom Nutzer noch zu prüfen |
| Entdecker-Run beginnen, Hilfe lesen | Steuerung ohne zusätzliche Erklärung verständlich | Offen |
| Erste Begegnung spielen | Treffer, Sprung, Dash und Modul fühlen sich kontrollierbar an | Offen |
| Fund nehmen, weitergehen | Belohnung und Routenwahl werden verstanden | Offen |
| Raum verlassen, App beenden, fortsetzen | Genau der erklärte Raumeingang wird wiederhergestellt | Automatisch geprüft; manuell offen |
| Audio bei geringer Lautstärke | Keine unangenehmen Pegelsprünge, verständliches Feedback | Offen |
| Boss / neuer Zyklus | Panzerungsfenster und Angriffsmuster erkennbar | Automatisch erreichbar; menschlich offen |

Tester, Datum, Geräte, gemessene Dauer und Beobachtungen erst nach tatsächlicher Durchführung eintragen. Kein erfundenes Testpersonenprotokoll.
