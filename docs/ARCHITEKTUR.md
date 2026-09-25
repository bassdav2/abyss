# Architektur und Design · Version 1.1

Stand 25.09.2026. Beschreibt den implementierten Stand; Teamentscheide und Dozentenabsprachen sind nicht belegt. Paketnamen relativ zu `ch.zhaw.abyss`. Diagramme: `docs/diagrams` (PlantUML-Quellen und gerenderte PNG/SVG).

## Kontext und Grenzen

ABYSS ist ein lokales Einzelspieler-Desktopspiel. Eine Figur kämpft sich vom Heck eines U-Boots durch 24 Räume in vier Sektionen bis zur Brücke. Kein Server, kein Konto, keine Datenbank. Produktionsbibliothek ist ausschliesslich JavaFX (Fenster, Zeichenfläche, Eingabe, Audio, Bildausgabe). Gradle baut, JUnit testet. Die Vorgaben aus dem PM3-Kick-off (Java, eigene geschichtete Architektur, keine Frameworks, JavaFX-Controller ohne Fachlogik) sind die wichtigsten Einflussfaktoren.

## Einflussfaktoren

| Einflussfaktor | Konsequenz | Verifikation |
|---|---|---|
| PM3: Java, eigene Architektur, keine Frameworks | Eigene Domäne statt Engine; JavaFX nur in `ui`/`infrastructure` | Domain-Tests ohne JavaFX; Paketabhängigkeiten |
| Reaktionsfähiger 2D-Kampf mit Plattformen | Fester Simulationsschritt 1/120 s, eigene Physik mit einseitigen Laufstegen | Bewegungs- und Kampftests, Kampagnensimulation |
| Pixel-Art-Look mit dynamischem Licht | Eigener Software-Framebuffer 480 × 270, prozedurale Grafik, Upload als `WritableImage` | Render-Smoke-Test, Render-Probelauf (≈2 ms/Bild) |
| Wiederspielwert | Seed-basierter Generator, Meta-Progression, Zyklen, Druckstufen | Generator-Tests über 600 Seeds, Balancebericht |
| Verlässlicher Unterbruch | Unveränderlicher Checkpoint am Raumeingang, versioniertes Dateiformat mit Migration | Rundreise-, Fehler- und Migrationstests |

## Logische Architektur (Diagramm 03)

- **domain** – reine Spielregeln. `GameRun` ist Aggregat und Konsistenzgrenze eines Tauchgangs und einzige öffentliche Änderungsschnittstelle. Intern arbeitet es mit paketinternen Mitarbeiterklassen: `PlayerMotor` (Bewegung, Sprünge, Ausweichen), `Arsenal` (Kombinationen, Luftangriffe, Harpunen, aktive Module, Drohne), `Ballistics` (Geschosse), `Loot` (Beute und Kisten) und `Rewards` (Bergung, Handel, Kapelle). `StatSheet` leitet alle Kampfwerte aus Klasse, Waffenstufe, Modulen und aktiven Resonanzen (`Synergy`) ab. `Combat` bündelt Schadensregeln (Kritik, Panzerung, Zustände, Modul-Auslöser, Explosionen, Kettenblitze), `Physics` die Bewegung mit Laufstegen. Gegnerverhalten sind Strategien (`EnemyBehavior`), gemeinsam über die Schablone `Brain`. `RoomGenerator` erzeugt reproduzierbare `RoomPlan`s mit `RoomLayout`, Wellen, Gefahren, Kisten, Raumtechnik (`Fixture`, gesteuert von `Machinery`) und `RoomCondition` (Raumzustand, aus einem eigenen Zufallsstrom, damit die übrigen Raumdaten eines Seeds unverändert bleiben). Keine Importe aus JavaFX, `java.io` oder den äusseren Schichten.
- **application** – `GameService` koordiniert Anwendungsfälle (starten, fortsetzen, sichern, Ergebnis verbuchen, freischalten, Aussehen). Unveränderliche Werte: `Profile`, `Loadout`, `Cosmetics`, `Settings`, `Unlock`-Katalog, `Achievement` (Logbuch; Bedingungen werden nach jeder Verbuchung gegen Tauchgang und Profil geprüft).
- **ports** – `GameRepository` als Speichervertrag.
- **infrastructure** – `FileGameRepository` (Properties-Format 3, Migration 1/2, atomares Schreiben, `.bak`-Sicherung); `AudioSystem` übersetzt Ereignisse in Klänge.
- **ui.pixel** – JavaFX-freie Pixelgrundlagen: `Frame` (Framebuffer mit Alpha, Clipping, Sprites, Linien), `Sprite` (mit Leuchtebene), `LightMap` (gestuftes Licht mit Bayer-Raster), `PostProcess` (Bloom, Farbstimmung, Vignette, Aberration), `PixelFont`.
- **ui.art** – prozedurale Pixel-Art: `Painter` (Formen, Kugelschattierung, Kontur, gedrehte Stempel), `DiverArt` (Figur mit Skelett-Posen und Garderobe), `EnemyArt`/`BossArt`, `RoomArt` (Raumstreifen, Lichter, Requisiten), `PropArt`, `IconArt`, Palette `Pal`.
- **ui.render** – `WorldRenderer` zeichnet einen `GameRun` lesend; `ActorPainter` (Figuren, Hiebspuren), `EventEffects` (Ereignis → Effekt), `ScreenFeel` (Blitz, Trefferpause, Zeitlupe), `MachinePainter` (Raumtechnik), `HudRenderer`, `Effects`, `Camera`, `Ocean`, `SubmarineScene` (Titel, Bootskarte, Auftakt, Siegesszene), `SpriteBank` (Zwischenspeicher). Raumzustände wirken im Renderer nur auf Licht und Partikel (Stromausfall, Alarmlichter, Lecks); ihre Regeln liegen im Generator und in `Rewards`.
- **ui.gui** – `Gui`: Bordsystem-Oberfläche im Framebuffer (Immediate-Mode): Schottplatten, Tasten mit Druckeffekt, Hologramm-Karten, Wahlschalter, Pegelregler, Kippschalter, Ziffernfeld, räumliche Tastaturnavigation und eigener Pixel-Mauszeiger.
- **ui** – `GameWindow` (Spielschleife, Eingabe, Navigation, Dispatch der Bildschirme pro Bild), `MenuScreens` und `RunScreens` (zeichnen ihre Bildschirme mit `Gui`, halten nur Auswahlzustand), `PixelView` (pixelgenaue Ausgabe mit Maus-Umrechnung), `InputController`. JavaFX liefert Fenster, Zeichenfläche, Eingabe und Audio.
- **Composition Root** – `AbyssApplication` verbindet Dateispeicher, Dienst und Fenster; `Launcher` startet.

Abhängigkeiten zeigen nach innen: UI → Anwendung → Domäne; Infrastruktur implementiert den Port. Die Darstellung liest die Domäne und ruft nur öffentliche Aktionen auf (`take`, `acceptDeal`, `chooseNextRoom`, `repair`, `useRepairKit`, `nextCycle`). Was die Interaktionstaste auslöst, entscheidet die Domäne (`GameRun.interaction()`), nicht der Controller.

## Entwurfsmuster

| Muster | Umsetzung | Nutzen |
|---|---|---|
| Strategie | `EnemyBehavior` mit 17 Implementierungen; `Weapon` als Daten-Strategie der Angriffe | Neue Gegner ohne Änderung an `GameRun` |
| Schablonenmethode | `Brain.update` (final) steuert Annähern → Ausholen → Angriff → Erholung; Arten überschreiben Einstiegspunkte | Gemeinsame, testbare Zustandsmaschine |
| Fabrikmethode | `EnemyKind.behavior()` | Zuordnung Art → Verhalten an einer Stelle |
| Beobachter (Pull) | `GameEvent`-Liste, von UI und Audio pro Bild entnommen | Effekte/Audio ohne Rückwirkung auf Regeln |
| Aggregat | `GameRun` als einzige Änderungsschnittstelle eines Tauchgangs | Konsistenz und Prüfbarkeit |
| Wertobjekte | Records: `RoomPlan`, `RunSetup`, `RunCheckpoint`, `StatSheet`, `Offer`, `Profile` | Unveränderlichkeit, einfache Tests |
| Port/Adapter | `GameRepository` / `FileGameRepository` | Speicher austauschbar, Speicher im Arbeitsspeicher für Tests |
| Baukasten | `InputFrame.Builder`, `Profile.Builder` | Lesbare Tests und Änderungen |
| Zwischenspeicher | `SpriteBank`, `PropArt`, `IconArt` | Grafik nur einmal malen |

## Architekturentscheidungen

### ADR-01 · JavaFX und eigene Domäne (unverändert)
JavaFX liefert Fenster, Controls, Eingaben und Audio. Die Spielregeln bleiben eigenes, direkt testbares Java. Alternativen wie LibGDX, Godot oder Unity würden die Java-/Framework-Vorgaben verletzen.

### ADR-02 · Fester Simulationsschritt, Trefferpause und Zeitlupe in der UI
`GameWindow` sammelt reale Zeit und ruft `GameRun.update` in Schritten von 1/120 s auf; ein Bild ist auf 100 ms begrenzt. Trefferpausen (35–70 ms) und Zeitlupe (Bossniederlage, eigener Tod) sind reine Präsentation: Die UI speist in dieser Zeit weniger Simulationszeit ein; die Regeln bleiben unverändert.

### ADR-03 · Checkpoint am Raumeingang (erweitert)
Gespeichert werden Seed, Zyklus, Druckstufe, Route, Klasse, Waffe mit Stufe, Modul, Ressourcen, Module, Kerne, Notfallkapsel, Tiefenrausch und Integritätsopfer. Gegner und Geschosse werden beim Laden aus dem Raumplan rekonstruiert. `GameRun.restore` validiert alle Werte und lehnt Manipulationen ab.

### ADR-04 · Dateiformat 3 mit Migration
Zwei Properties-Dateien, atomar geschrieben. Format 1/2 wird gelesen: Profilfortschritt skaliert, alte Freischaltungen übernommen, Raum-Sicherungen bestmöglich in dieselbe Sektion übertragen; das Original bleibt als `.bak`. Profil und Checkpoint sind keine gemeinsame Transaktion.

### ADR-05 · Ereignisse für Rückmeldung (erweitert)
50 Ereignisarten (Treffer, Krit, Block, Explosion, Boss-Auftritt, Phasenwechsel, Raumtechnik …). Pro Bild entnimmt die UI alle Ereignisse; Renderer und Audio werten sie getrennt aus.

### ADR-06 · Generator mit festen Wächtern und variabler Geometrie
Vier Sektionen à sechs Positionen; Wächter an 5, 11, 17 und die Brücke an 24, Werkstätten danach. Sonderräume (Elite, Schwarzmarkt, Kapelle, Versorgung) werden pro Sektion gemischt. Seed und Abzweig bestimmen Thema, Breite (1–1,75 Bildschirme), Laufstege aus Vorlagen, Wellen nach Bedrohungsbudget, Gefahren und Kisten.

### ADR-07 · Software-Pixel-Pipeline statt Bildplatten (neu)
Die Welt wird in einen 480 × 270 Framebuffer gezeichnet (Farb- und Leuchtebene), mit einer Lichtkarte in halber Auflösung multipliziert, die in 9 Stufen quantisiert und per Bayer-Matrix gerastert wird. Bloom entsteht aus der Leuchtebene. Das fertige Bild wird einmal pro Bild in ein `WritableImage` geschrieben und ohne Glättung skaliert. Vorteile: pixelgenauer Look, dynamisches Licht, volle Kontrolle, ohne JavaFX testbar, ≈2 ms pro Bild. Nachteil: Zeichenfunktionen werden selbst gepflegt.

### ADR-08 · Prozedurale Pixel-Art in Java (neu)
Figuren, Gegner, Räume, Symbole und Effekte werden beim Start aus Grundformen gemalt (Kugelschattierung, Konturen, Skelettposen mit Zweigelenk-Kinematik). Dadurch sind Garderobe und Waffen frei kombinierbar, die Grafik ist reproduzierbar und versionierbar, und es entstehen keine Lizenzfragen. Der frühere Mischstil (ImageGen-Platten, Blender-Renderings) ist abgelöst; die Quellen bleiben in `art-source` und der Git-Historie.

### ADR-09 · Menüs als Bordsysteme im Framebuffer (geändert in 1.1)
In 1.0 waren Menüs JavaFX-Controls mit CSS über der Pixelfläche; sie wirkten wie ein Fremdkörper. Seit 1.1 zeichnet ein eigenes Immediate-Mode-GUI (`ui.gui.Gui`) alle Menüs, Karten und Regler in denselben 480 × 270 Framebuffer: Schottplatten, Terminals, Hologramm-Karten, Kamerabilder der nächsten Räume. Maus und Tastatur werden gesammelt und einmal pro Bild ausgewertet; Pfeiltasten wandern räumlich zum nächsten Element. Vorteile: einheitlicher Pixel-Look, Menüs wirken wie Teil des U-Boots, keine Abhängigkeit von Schriftdateien, Menüs laufen mit der Spielschleife. Nachteil: Layout und Bedienelemente werden selbst gepflegt. Controller rufen weiterhin nur Anwendungsfälle und öffentliche Domänenaktionen auf.

### ADR-10 · Raumtechnik als Aggregat-Mitarbeiter (neu in 1.1)
Förderbänder, Dampfdüsen, Turbinenwind, Pressen, Lasergitter und Notschalter sind `Fixture`s im `GameRun`. `Fixture` kennt nur Art, Lage und Takt; `Machinery` berechnet Schub, Starts, Treffer und Schalter-Effekte. Anlagen-Treffer nutzen die Quelle `MACHINE`, die die Panzerung von Wächtern durchschlägt und ihren Kern kurz freilegt. So werden Bossarenen interaktiv, ohne die Bossverhalten anzufassen. Der Generator verteilt Anlagen aus einem eigenen Zufallsstrom.

## Verantwortlichkeiten

| Klasse | Verantwortung | Bewusst nicht zuständig |
|---|---|---|
| GameRun | Phasen, Räume, Wellen, Sicherung; koordiniert die Mitarbeiterklassen | Dateien, Controls, Grafik |
| PlayerMotor / Arsenal | Bewegung bzw. Angriffe und Module der Figur | Gegnerlogik |
| Ballistics / Loot / Rewards | Geschosse, Beute, Belohnungen und Handel | Eingaben |
| Combat | Schadensberechnung, Auslöser-Module, Explosionen | Eingaben, Raumfolge |
| Brain + Unterklassen | Entscheidungen und Angriffsmuster der Gegner | Animation |
| RoomGenerator | Reproduzierbare Raumpläne | Laufzeitzustand |
| StatSheet | Abgeleitete Werte aus Klasse, Waffe und Modulen | Ressourcen |
| GameService | Anwendungsfälle, Profil, Kerne, Freischaltungen | Trefferregeln |
| FileGameRepository | Lesen, Schreiben, Migration | Menüablauf |
| WorldRenderer | Bild aus Domänenzustand, Effekte aus Ereignissen | Regeln |
| GameWindow | Spielschleife, Eingabe, Navigation | Spielregeln |

## Bekannte Grenzen

Die grössten Klassen sind `WorldRenderer` (~1200 Zeilen inkl. Formatierung), `GameRun` (~1000 Zeilen) und `Gui` (~760 Zeilen). `WorldRenderer` delegiert bereits an `ActorPainter`, `EventEffects`, `HudRenderer` und `Effects`; weitere Zeichner (Requisiten, Licht) wären der nächste Schnitt. Gamepad, frei belegbare Tasten, Screenreader-Bedienung und Netzwerkspiel fehlen. Geprüft wurde nur auf Apple-Silicon-macOS.
