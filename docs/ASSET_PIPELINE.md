# Art Direction und Asset-Pipeline · Version 1.0

## Stil

ABYSS 1.0 ist ein Pixel-Art-Spiel in nativer Auflösung **480 × 270**. Die Welt ist ein seitlicher Längsschnitt durch ein altes, sehr langes U-Boot. Jede Sektion hat eine eigene Farbstimmung: Hecksektion Rost und Amber, Maschinendeck Grün und Türkis, Forschungsdeck Violett und Biolumineszenz, Kommandodeck kühles Blau. Figuren tragen eine einpixelige dunkle Kontur und leuchtende Akzente (Visier, Augen, Lampen), die in der Dunkelheit sichtbar bleiben.

Grundpalette: `ui/art/Pal.java` (Tinte/Meer, Stahl, Rost/Messing, Türkis, Gefahr, Grün/Violett, Helltöne). Welteinheiten werden mit 0,3 in Pixel umgerechnet; der Boden liegt bei Pixelzeile 186.

## Alles prozedural

| Grafik | Quelle | Technik |
|---|---|---|
| Spielfigur | `DiverArt` | Skelett-Posen (Hüfte, Hände, Füsse), Knie/Ellbogen per Zweigelenk-Kinematik, Kugelschattierung, Kontur; 15 Animationen; Garderobe als Farb-/Formparameter; Klassen-Accessoires |
| Waffen | `DiverArt.weapon` | kleine Bitmaps, per inverser Abbildung lückenlos gedreht |
| Gegner und Bosse | `EnemyArt`, `BossArt` | Grundformen mit Schattierung, Animation über Gliedmassenpositionen; 6 Animationen je Art |
| Räume (33 Abteilungen) | `RoomArt` | Wandplatten mit Raster, Rumpfrippen, Deckenrohre, Bullaugen/Panoramafenster (transparent zum Meer), themenspezifische Requisiten, Laufstege, Bodengitter, Schablonenschrift, Rost/Algen; liefert Lichter, animierte Elemente und Vordergrund |
| Meer | `Ocean` | Farbverlauf mit Bayer-Raster, Lichtschächte, Felsnadeln, Tang, Fischschwärme, Leuchtwesen, Leviathan, Meeresschnee – alles mit Parallaxe |
| Objekte | `PropArt` | Kisten, Bergungskapsel, Händlerin, Kapelle, Werkbank, Schott, Beute, Geschosse |
| Symbole | `IconArt` | 16 × 16 für 41 Module, 7 Waffen, 8 Fähigkeiten, Ressourcen, Raumarten |
| Titel und Karte | `SubmarineScene` | Boot im Längsschnitt mit 24 beleuchteten Räumen; Auftakt und Siegesszene |
| Raumtechnik | `MachinePainter` | Förderband mit laufenden Pfeilen, Dampfdüse mit Glühring, Turbinenrad, Presse mit Stempel und Warnzone, Laser-Emitter mit Leuchtstrahl, Konsole mit Bildschirm und Ladebalken |
| Menüs | `ui.gui.Gui` | Schottplatten mit Nieten, Terminals mit Zeilenraster, Tasten mit Druckeffekt, Hologramm-Karten mit Zielrahmen, Pegelregler, Kippschalter, Pixel-Mauszeiger |
| Effekte | `Effects` | Partikel (Funken, Glut, Rauch, Staub, Blasen, Trümmer, Schleim, Eis, Sterne, Dampf, Flammen), Ringe, Blitze, Explosionen, Nachbilder, Bodenflecken |
| Schrift | `pixel/font.txt` | eigene Bitmap-Schrift inkl. Umlauten |

Die Leuchtebene jedes Sprites (Grossbuchstaben-Pixel beim Malen) wird additiv in einen eigenen Puffer gezeichnet, bleibt von der Lichtkarte unberührt und speist den Bloom.

## Licht und Nachbearbeitung

`LightMap` addiert Grundlicht, Deckenlampen (ruhig, flackernd, pulsierend, defekt), Lichtkegel, Fensterlicht, Stirnlampe, Gegneraugen, Geschosse und Explosionen in halber Auflösung. Beim Anwenden wird das Licht in neun Stufen quantisiert und mit einer 4×4-Bayer-Matrix gerastert – daher die gestuften Lichtkegel. `PostProcess` ergänzt Bloom, Farbstimmung pro Sektion, Vignette (rot pulsierend bei niedriger Integrität), chromatische Aberration bei Treffern und Blitze. Der optionale Röhrenfilter zeichnet in `PixelView` feine Zeilen über das skalierte Bild.

## Prüfen und ändern

```sh
./gradlew artSheet     # build/art/diver.png, enemies.png, bosses.png
./gradlew sceneShot    # build/scenes/scene-XX.png aus echten Spielszenen
./gradlew uiSmoke --args="--capture=docs/qa/screens"
```

Neue Grafik zuerst im Übersichtsbogen, dann in einer echten Szene mit Licht prüfen: Liest sich die Silhouette vor dem Hintergrund? Ist die Vorwarnung sichtbar? Stimmt die Grösse zur Trefferzone (Welteinheiten × 0,3)?

## Historische Quellen

Die ImageGen-Hintergründe und Blender-Figuren der Version 0.2 sind ersetzt. `art-source/*.blend`, `tools/render_actors.py` und die Prompts (`docs/ART_PROMPTS.md`, `docs/CONCEPT-PROMPTS.md`) bleiben als Entstehungsgeschichte erhalten; die Bilder liegen in der Git-Historie.
