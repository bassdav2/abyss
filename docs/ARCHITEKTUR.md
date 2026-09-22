# Architektur und Design - Arbeitsfassung

Stand 22.09.2026. Beschreibt den implementierten Stand, keine bereits durch das Team beschlossene Architektur. Paketnamen sind relativ zu `ch.zhaw.abyss`.

## Kontext und Grenzen

ABYSS ist ein lokales Einzelspieler-Desktopspiel. Eine Figur kämpft sich vom Heck eines langen U-Boots durch zwölf Räume bis zur Brücke. Es gibt keine Serververbindung, kein Konto, keinen Mehrspielerbetrieb und keine relationale Datenbank. JavaFX ist die einzige Produktionsbibliothek; Gradle dient dem Build, JUnit den Tests. JavaFX-Freigabe und Themenwahl bleiben mit dem Fachdozenten abzustimmen.

## Vier zentrale Einflussfaktoren

| Einflussfaktor | Konsequenz | Verifikation |
|---|---|---|
| PM3: überwiegend Java und eigene Architektur | Java-Domäne ohne Engine-Framework; UI und Dateien bleiben ausserhalb der Regeln | Abhängigkeitsprüfung und reine Domain-Tests |
| Reaktionsfähiger 2D-Kampf | Fester Simulationsschritt von 1/120 s; JavaFX Canvas zeichnet unabhängig davon | Eingabe-/Kampf-Tests und Render-Probelauf |
| Reproduzierbare Runs und verlässlicher Unterbruch | Seed-basierte Raumwahl, unveränderlicher Checkpoint am Raumeingang | 1000 Seeds; Roundtrip- und Resume-Tests |
| Begrenzter Semesterumfang und konsistente Assets | Zwölf Raumpositionen, wiederverwendbare Raumtypen, gemeinsame Figurenquelle | Assetmanifest, Blender-Quellen, vollständiger Run |

## Logische Architektur

- **domain:** `GameRun` bildet die Konsistenzgrenze eines Tauchgangs. Enthält `Player`, `Enemy`, `Projectile`, `Hazard`, aktuelle `RoomPlan`, Route und Beute. `EnemyAi` ist eine explizite Zustandsmaschine; `RoomGenerator` erzeugt regelkonforme Raumangebote. Keine Imports aus JavaFX, I/O oder Anwendungsschichten.
- **application:** `GameService` koordiniert Start, Fortsetzen, Fortschrittsverbuchung und Speichern. `Profile` und `Settings` sind unveränderliche Werte. Er kennt nur den Speichervertrag, keine konkreten Dateien.
- **ports:** `GameRepository` beschreibt Laden, Speichern und Entfernen der lokalen Sicherung.
- **infrastructure:** `FileGameRepository` implementiert den Vertrag mit versionierten Properties-Dateien. `AudioSystem` übersetzt Ereignisse in JavaFX-Audio.
- **ui:** `GameWindow` verwaltet Bildschirmzustände und Benutzeraktionen. `InputController` übersetzt Tastatur/Maus in `InputFrame`. `GameRenderer` zeichnet nur lesend aus der Domäne. `AssetCatalog` lädt gemeinsame Assets; `ParticleField` ist rein visuell.
- **Composition root:** `AbyssApplication` verbindet das konkrete Datei-Repository mit Service und Fenster. `Launcher` startet die Anwendung.

Die Darstellung darf die Domäne lesen und öffentliche Aktionen aufrufen. Dateizugriff erfolgt über den Service. Treffer, Energiekosten, Belohnungen, Schadensregeln und zulässige Raumwechsel liegen ausschliesslich in der Domäne. Audio und Effekte reagieren auf entnommene `GameEvent`-Werte und können keine Treffer erzeugen.

## Architekturentscheidungen

### ADR-01 - JavaFX Canvas und eigene Domäne

Status: implementierter Vorschlag. JavaFX bietet Fenster, Controls, Eingaben, Zeichenfläche und Audio. Eigene Regeln erlauben direkte Unit-Tests und machen die im Modul erwarteten Verantwortlichkeiten sichtbar. Alternative: LibGDX/Godot/Unity. Diese würden mehr Engine-Funktionalität liefern, vergrössern aber die Abhängigkeit und passen schlechter zur gegenwärtigen Java-/Framework-Vorgabe. Nachteil der Wahl: Kollision, Animation und Spielschleife werden selbst gepflegt.

### ADR-02 - Fester Simulationsschritt

`AnimationTimer` sammelt reale Zeit; die Domäne erhält Schritte von 1/120 s. Ein einzelner Render-Abstand wird auf 100 ms begrenzt, um nach langen Unterbrechungen keine minutenlange Aufholschleife auszuführen. Pause und Fokusverlust stoppen die Simulation. Dadurch hängen Schadens- und Bewegungswerte nicht direkt von einer einzelnen Bildrate ab. Dies ist keine vollständig deterministische Replay-Engine: Eingabezeitpunkte und spätere Änderungen an Regeln können ein Ergebnis verändern.

### ADR-03 - Unveränderlicher Checkpoint am Raum-Einstieg

Gespeichert werden Seed, Zyklus, Tiefe, gewählter Zweig, Startmodul, Ressourcen, passive Upgrades, Statistiken und Route. Gegner, Projektile und Animationen werden beim Laden aus dem Einstieg rekonstruiert. Ein Raum muss nach dem Verlassen erneut gespielt werden. Das begrenzt die Save-Komplexität und verhindert halbe Kampfschnappschüsse. Die UI erklärt diese Regel.

### ADR-04 - Datei-Repository hinter einem Port

Zwei kleine UTF-8-Properties-Dateien benötigen keine Datenbank oder Serialisierungsbibliothek. Formatversion und Werte werden geprüft. Schreiben erfolgt in eine temporäre Datei und nach Möglichkeit über atomisches Umbenennen. Vor dem Ersetzen einer nicht lesbaren Datei entsteht eine `.bak`-Kopie. Atomizität gilt je Datei; Profil und Checkpoint sind keine gemeinsame Transaktion. Ein Prozessabbruch exakt zwischen zwei Schreiboperationen kann deshalb Statistik und Raum-Sicherung auseinanderlaufen lassen. Der Spielstand bleibt lokal und manipulierbar; Anti-Cheat ist kein Ziel.

### ADR-05 - Ereignisse für Feedback

Die Domäne erzeugt z.B. `HIT`, `DASH`, `ROOM_CLEAR` und `VICTORY`. Pro Darstellungsframe entnimmt die UI die seit dem letzten Frame gesammelten Ereignisse. Renderer und Audio werten sie separat aus. So bleiben Audioausfall, Partikelbudget und reduzierte Bewegung ohne Wirkung auf den Kampf.

### ADR-06 - Begrenzter Generator statt beliebiger Geometrie

Die achtzehn Raumpositionen folgen drei Sektionen. Sektorwächter liegen an 5 und 11, Werkstätten an 6 und 12, die Brücke an 18. An zwölf Positionen gibt es zwei Routenangebote. Seed und Zweig bestimmen Begegnungen und Raumvariante; der zentrale Weg nach vorn bleibt immer vorhanden. Geometrie und Sektorreihenfolge werden nicht zufällig permutiert. Der Wiederspielwert kommt aus Gegnerzusammenstellung, Risiko-/Vorratswahl, Modulangeboten und Folgezyklen.

### ADR-07 - Blender-Figuren und gerasterte Hintergrundplatten

Figuren werden aus editierbaren Blender-Modellen in transparente Animationsframes gerendert. Kamera, Materialdefinition, Bildgrösse und Ankerpunkt sind gemeinsam. Hintergrundplatten wurden mit Imagegen erstellt und auf feste Bodenhöhe und Perspektive abgestimmt. Interaktive Kisten, Schotteffekte, Gefahren und Trefferanzeigen werden separat gezeichnet. Ein Hintergrundbild ist keine Kollisionsgeometrie. Fonts werden mit Lizenz mitgeliefert.

## Wichtige Verantwortlichkeiten

| Klasse | Verantwortung | Bewusst nicht zuständig für |
|---|---|---|
| GameRun | Spielregeln, Kampf, Räume, Upgrades und Run-Phase | Dateien, Controls, Audio |
| RoomGenerator | Gültige Angebote aus Seed, Tiefe und Zyklus | Aktueller Lebenszustand |
| EnemyAi | Entscheidungs- und Angriffszustände der Gegner | Animationsdateien |
| GameService | Anwendungsfälle, Profilfortschritt, Speicherkoordination | Trefferberechnung |
| FileGameRepository | Versioniertes Lesen und Schreiben | Menüablauf |
| GameWindow | Bildschirmwechsel und Eingabeübersetzung | Schadensformeln |
| GameRenderer | Zeichenbefehle und sichtbares Feedback | Mutation von Spielerwerten |

## Bekannte Architekturgrenzen

`GameRun` und `GameWindow` sind die grössten Klassen. Eine weitere Ausweitung um Waffen, Dialoge oder Plattformphysik sollte vorher eine gezielte Aufteilung in Kampf-/Raumsysteme bzw. Bildschirmklassen auslösen. Für den aktuellen begrenzten Umfang bleibt die zentrale Konsistenzgrenze verständlich. Der Renderer zeichnet in logischen 1600 x 900 Einheiten; das Fenster skaliert die Oberfläche. Gamepad, frei belegbare Tasten, Screenreader-Spielbedienung, Cloud-Saves und Netzwerkspiel sind nicht implementiert.

Die Diagrammquellen unter `docs/diagrams/` gehören zum Stand. Ein konzeptuelles Domänenmodell und ein Design-Klassendiagramm sind absichtlich getrennt: Ersteres beschreibt Fachbegriffe, letzteres tatsächlich vorhandene Klassen.

## Erweiterung 0.2

`SupplyCrate` kapselt Inhalt, Treffer und einmalige Auszahlung. `EnvironmentRenderer` übernimmt rein visuelle Tiefenebenen; `ItemGlyph` liefert gemeinsame Icons für HUD, Inventar und Bergung. Beide verändern keine Spielregeln. Das Repository liest Version 1 und 2; neue Daten verwenden Version 2. Alte Raumpositionen werden sektortreu abgebildet und ursprüngliche Dateien vor dem Überschreiben gesichert.
