# Teststrategie und Prüfnachweise · Version 1.5

Stand 04.10.2026, geprüft auf Apple Silicon macOS mit JDK 25 und JavaFX 26.0.2. Automatisierte Tests, Bot-Läufe und Bildschirmfotos ersetzen keine menschlichen Usability-, Spielgefühl- oder Hörtests.

## Ebenen

1. **Unit-Tests der Domäne** (`src/test/.../domain`, ohne JavaFX und Dateien):
   - `GameRunTest` – Phasen, gesperrtes Schott, Wellen mit Verstärkungsansage, Checkpoint-Rundreise, Ablehnung manipulierter Stände, Niederlage, Schrittgrenze, Sieg und Folgezyklus, Ereignisse, Beute.
   - `MovementTest` – Wände, gehaltene vs. kurze Sprünge, Landung auf Laufstegen, Durchfallen und Durchspringen, Luftsprung nur mit Druckluftdüse, Ausweichen mit Unverwundbarkeit und Abklingzeit, Kühlkreislauf, Gegner fallen durch Stege.
   - `CombatTest` – Treffer nur vorn und einmal pro Schlag, Kombination und Reset, Maus-Zielrichtung, Servo und Panzerung exakt, Stufengrenzen, Notfallkapsel, Barriere, Brand und Kälte, Frost-Immunität der Bosse, Vorwarnung vor jedem Angriff, Schildträger-Frontblock, Bosspanzerung, Bossphasen, Harpunen-Zielhilfe, Sprengkisten, alle acht aktiven Module, alle 16 Gegnerarten (inklusive Bosse) eine Minute ohne Fehler.
   - `RewardTest` – Bergung einmalig und ohne Duplikate, Messingkompass, legendäre Wächterbergung, Schwarzmarkt-Preise, Werkstatt-Reparatur und Waffenstufe, Versorgung, genau ein Kapellenhandel, Gläserner Rumpf, Reparatursets, seltene Bergung in Alarmräumen, Resonanzen (Werte laut Beschreibung, genau eine Meldung, keine erneute Meldung nach dem Fortsetzen).
   - `RoomGeneratorTest` – 600 Seeds deterministisch und strukturell gültig, Wächter und Werkstätten an festen Positionen, Druckstufe vergrössert Wellen, ungültige Räume, begehbare Laufstege, Raumzustände nur in Kampfräumen ab Raum 3 und mit ihren Regeln (Schrott, zusätzliche Gegner, Lecks und Kisten).
   - `MachineryTest` – Förderband schiebt Figur und Gegner, Dampfdüse schleudert und lädt nach, Presse warnt und trifft einmal pro Takt, Laser trifft einmal pro Aktivierung und lässt sich abschalten, alle vier Notschalter-Effekte samt Abklingzeit, Anlagen durchschlagen Bosspanzerung und legen den Kern frei, Schlagseite schiebt und lässt Trümmer fallen, gefährliche Anlagen ruhen nach dem Sichern, gültige Platzierung über 200 Seeds inklusive Bossarenen.
   - `RoomEventTest` – Hüllenbruch mit Nachschub, Countdown, Wasserstand, Abdichten und seltener Bergung; Schmugglerdrohne flieht, greift nie an, entkommt nach 15 Sekunden oder bringt beim Abschuss einen Datenkern.
   - `EndgameTest` (neu in 1.4): Eskalation ab Raum 20 über alle Zyklen, wachsender Schwarmfaktor, breitere Endgame-Räume mit tausenden Schwarmgegnern, Kapazität bis über 900 lebende Gegner am Ende des zweiten Zyklus, alle acht Schwarmangriffe im Endgame, Bedrohungen erst ab dem Maschinendeck und im Endgame meist vorhanden, Zusammensetzung von Luftschlag, Brutnestern und Kolossen, Panzer- und Schalenregeln, Nest mit Brut, Zündmilbe, Speerfisch-Warnlinie, Prismaquallen-Ring, Grenze feindlicher Geschosse, Prüfungsbelohnung, Bereitschaftsanzeige.
   - `BossMechanicsTest` (neu in 1.4): Anker mit Heranziehen und Stampfer, Schottfall mit genau einer Lücke, Kernschmelze mit Notschalter und ohne, Bruteier schlüpfen oder werden zerschlagen, Tinte und Sog, Torpedos betäuben den Lotsen, Kreuzfeuer von beiden Wänden auf zwei Höhen, Prismenkaiserin erst ab Zyklus 2, Lanzenreihen mit Lücke am Boden, drehender Sonnentanz, Entrückung, schnellende Prismenbolzen, Raserei, entfesselte Muster.
   - `EvolutionTest` (neu in 1.4): elf eindeutige Rezepte, Entfesselung obenauf und einmalig, nicht ohne Rezept erhältlich, Gewitterkern trifft weit häufiger, Grenzbrecher hebt Grenzen und übersteht das Fortsetzen, Grenzbrecher-Angebot, Überkritik-Stufen und Schaden, Bollwerk.
2. **Systemnahe Kampagnensimulation** (`CampaignSimulationTest`): Der Testspieler (`qa/CampaignPilot`) spielt mit regulären Eingaben vollständige Tauchgänge: je Klasse acht Seeds; kein Raum darf hängen bleiben, mindestens drei Siege je Klasse. Zusätzlich je Klasse ein Lauf auf Druckstufe 5.
3. **Integration Anwendung/Persistenz**: `GameServiceTest` (Speicher im Arbeitsspeicher: Sieg einmal verbucht, Kerne, Druckstufe, Kompendium, Logbuch einmalig mit Kernen, Tagestauchgang-Seed, Folgezyklus nach Neustart, Freischaltungen, Voraussetzungen, Garderobe, Niederlage, Speicherfehler) und `FileGameRepositoryTest` (echtes Dateiformat in temporären Verzeichnissen: Rundreise inklusive Logbuch, unbekannte Version, beschädigte Werte, Sicherungskopien, Migration von Format 2).
4. **Pixel-Pipeline ohne Fenster**: `FrameTest` (Alpha, Clipping, Anker und Spiegelung, gestuftes Licht, Schrift) und `RenderSmokeTest` (vollständiger Durchlauf durch alle vier Sektionen, jedes achte Bild gezeichnet, Titel- und Kartenansicht).
5. **Eingabe**: `InputControllerTest` (Flanken statt Tastenwiederholung, gehaltener Sprung, Abtauchen, Maus-Zielrichtung, Aufräumen bei Menüwechsel).
6. **JavaFX-Komponentenprüfung** (`./gradlew uiSmoke`): echtes Fenster mit temporärem Speicherort; öffnet Titel, Vorbereitung, Garderobe, Archiv (Taucher, Baupläne, Kompendium, Logbuch, Resonanzen), Optionen, Steuerung, startet einen Tauchgang mit Auftakt, sichert den Raum, zeigt Bergung, Ausrüstung, Karte, Pause, Route, Schwarzmarkt, Boss, Werkstatt, eine Routenwahl mit Raumzustand, erobert die Brücke (Siegesszene, Auswertung) und zeigt eine Niederlage; lädt Audioclips. 37 Prüfungen. Mit `--args="--capture=Verzeichnis"` entsteht zu jedem Schritt ein Bildschirmfoto (`docs/qa/screens`).
7. **Render-Probelauf** (`./gradlew renderSoak`): Testspieler spielt fortlaufend, jedes Bild wird vollständig gezeichnet; misst Zeichenkosten und Fehler (`docs/qa/render-soak.json`).
8. **Balancebericht** (`./gradlew balance --args=30`): Siegquote, Tiefe und Bot-Spielzeit je Klasse über viele Seeds. Diente zum Finden von Hängern (Gegner auf hohen Stegen, Harpunen gegen Flieger) und zum Abgleich der Klassen.
9. **Visuelle Prüfung**: `artSheet` (alle Figuren, Gegner, Bosse), `sceneShot` (echte Szenen ohne Fenster), `endgameShots` (Endgame-Schwärme, Bedrohungen, Bossmuster und Prismenkaiserin aus vorbereiteten Raumeingängen), UI-Bildschirmfotos. Gefundene Fehler wurden behoben: überblendete Bosse, zu dunkle Räume, abgeschnittene Texte, fehlende Pfeil-Glyphen, überlappende Banner.

## Ergebnisse dieses Stands

| Prüfung | Ergebnis |
|---|---|
| JUnit | 172 Testfälle bestanden, darunter `EndgameTest`, `BossMechanicsTest` und `EvolutionTest` (neu in 1.4), Oberflächenebene in `RenderSmokeTest` und `FrameTest` (neu in 1.5), `SwarmTest`, `CareerTest` und `CareerWeaponTest` (siehe `docs/qa/test-summary.json`) |
| Formatprüfung und Javadoc (`-Xdoclint`) | bestanden, keine Compiler-Warnungen |
| UI-Komponentenprüfung | 58/58 auf der neuen Oberflächenebene, inklusive Levelaufstieg, Laufbahn, Endgame-Schwarm, Routenwahl mit Bedrohung, Entfesselung und Prismenkaiserin (`docs/qa/ui-smoke.txt`, Bildschirmfotos in `docs/qa/screens`) |
| Endgame-Bildproben | 14 Bilder ohne Fenster in 1920 × 1080 mit HUD: Schwarm mit über 1100 Gegnern, vier Bedrohungen, Kernschmelze, Prismenspirale, Tintenwolke, Fischzug, Kreuzfeuer und vier Muster der Prismenkaiserin (`./gradlew endgameShots`, `docs/qa/endgame`) |
| Render-Probelauf | 60 s, 26 980 Bilder, 0 Fehler, Mittel 2,2 ms, 95 % unter 2,4 ms inklusive Oberflächenebene; einzelne Spitze 77 ms beim ersten Aufbau (`docs/qa/render-soak.json`) |
| Schwarm-Lastprobe | Testspieler über fünf Zyklen, bis 1600 Gegner gleichzeitig. Bei 600–999 Gegnern ≈ 3,6 ms Zeichnen und 0,4 ms Simulation pro Bild, bei 1000+ Gegnern ≈ 3,9 ms und 1,0 ms. Einzelne Spitzen bis knapp 200 ms bei Raumwechseln und grossen Abschusswellen (`./gradlew swarmBench --args="777 5 0"`) |
| Balance | Testspieler ohne Laufbahnboni gewinnt Zyklus 1 mit jeder Klasse (8/8 je Klasse). Zwei Endgame-Läufe: beide besiegen die Prismenkaiserin im zweiten Zyklus, einer fällt im dritten Zyklus an ihr, der andere gewinnt alle fünf Zyklen. Danach wurden Wächter ab Eskalation 40 zäher, weil sie dort in Sekunden fielen. Kein menschlicher Spieltest |
| Balancebericht, 30 Seeds je Klasse | Mechanikerin, Harpunier, Schweisserin, Funkerin je 30/30; Koloss 26/30 (nach Anhebung auf 150 Integrität und 20 % Schutz); keine Hänger |

## Reproduktion

```sh
./gradlew test checkJavaFormat javadoc
./gradlew uiSmoke --args="--capture=docs/qa/screens"
./gradlew endgameShots --args="docs/qa/endgame"
./gradlew renderSoak --args='--seconds=60'
./gradlew balance --args='30'
python3 tools/package_mac.py
```

## Grenzen

- Der Testspieler liest Vorwarnungen exakt aus dem Zustand und reagiert ohne Verzögerung; seine Siegquoten sind obere Schranken, seine Zeiten keine menschlichen Spieldauern.
- Einige Kampftests setzen Gegner gezielt, um Regeln isoliert zu prüfen. Vollständige Läufe mit regulären Eingaben liefert die Kampagnensimulation.
- Audio wird nur geladen, nicht angehört. Vollbild, Fokusverlust und echte Maus-/Tastatureingaben am Betriebssystem sind nicht automatisiert geprüft.
- Nur Apple-Silicon-macOS wurde ausgeführt; Windows/Linux sind nicht geprüft.
