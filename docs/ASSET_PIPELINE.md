# Art Direction und Asset-Pipeline

## Verbindliche Grundlage dieses Stands

Die Spielwelt ist ein horizontaler Querschnitt eines alten, sehr langen U-Boots. Hinten dominieren abgenutztes Metall und Amberlicht, vorne kühles Blau und kontrollierte Räume. Palette: fast schwarzes Blau `#06131c`, Cyan `#80e0dc`, Amber `#eaba72`, helles Grau `#edf0e9`. Die Figur trägt eine gelbe Arbeitsjacke, dunkle Hose, Atemflaschen und eine Lampe.

Logische Bühne: **1600 x 900**. Die Füße stehen auf **y = 620**. Raumplatten werden auf diese Bühne skaliert; ihre Gehspur liegt bei rund 69 Prozent der Bildhöhe. Zwischen x = 72 und 1528 ist die Laufspur frei. Linke und rechte Schotte behalten ihre Lage. Keine Perspektivkorridore oder eingebrannten Figuren/UI in Hintergrundplatten.

## Quellen und Exporte

| Typ | Quelle | Reproduzierbarer Export / Prüfung |
|---|---|---|
| Hintergrundplatten | Imagegen, Prompts und Referenzen in ART_PROMPTS.md | PNG; gleiche Kamera/Bodenhöhe visuell prüfen. KI-Ausgaben selbst sind nicht pixelidentisch reproduzierbar. |
| Protagonist und Gegner | tools/render_actors.py, gespeicherte art-source/*.blend | Orthografische Kamera, transparente RGBA-Frames, gemeinsame Materialien |
| Animation | Pose-Funktionen im Blender-Skript | Je Figur idle, walk, attack, hurt; Spieler zusätzlich dash |
| Interaktive Effekte | GameRenderer / ParticleField | Kisten, Gefahren, Warnungen, Partikel und HUD separat von Hintergrundbildern |
| Klänge/Musik | tools/create_audio.py | 44,1 kHz, 16-Bit-PCM-WAV; Synthese ohne externe Samples |
| Typografie | Barlow / Barlow Condensed | Mitgelieferte TTF-Dateien und OFL-Lizenzen |
| App-Icon | tools/MakeIcon.java | PNG -> ICNS beim Paketieren |

## Figurenkonsistenz

Es gibt fünf gemeinsame Modellquellen: Spieler, Schrottläufer, Drohne, Schottwächter und Lotse. Die Modelle werden nicht für jedes Bild neu mit Bild-KI entworfen. Materialnamen, orthografische Kamera, Licht, Bildgröße und Fußanker bleiben gleich. Der Schrottläufer besitzt eine niedrige vierbeinige Silhouette, der Wächter Druckhelm und Schutzplatte, der Boss einen größeren Druckanzug mit Energiekern.

`art/actors/manifest.json` beschreibt Framezahl und Anker: normal `(0.5, 0.944)`. Die Drohne erhält in der Darstellung einen angepassten Anker für ihren schwebenden Körper. Der Renderer wählt Frames anhand fachlicher Zustände; der sichtbare Animationsframe entscheidet nicht über einen Treffer.

## Änderungen ausführen

```sh
# Alle Figuren neu rendern
/Applications/Blender.app/Contents/MacOS/Blender --background --python tools/render_actors.py

# Nur bestimmte Figuren neu rendern
/Applications/Blender.app/Contents/MacOS/Blender --background --python tools/render_actors.py -- scuttler sentinel captain

# Originalklänge neu erzeugen
python3 tools/create_audio.py

# Echtes Gameplay und Menü-Layouts prüfen
python3 tools/capture_views.py
```

Die `.blend`-Dateien speichern den jeweiligen Ausgangszustand. Änderungen von Hand an einer `.blend`-Datei werden vom prozeduralen Komplett-Render nicht automatisch zurück in das Python-Skript übernommen. Für dauerhafte, reproduzierbare Änderungen zuerst die Modellierungsquelle anpassen oder eine bewusst neue manuell gepflegte Quelldatei einführen.

## Freigaberegel für neue Grafiken

Eine neue Raumplatte zuerst mit der bestehenden Figur, einem Gegner, dem HUD und der Kollisionsspur im tatsächlichen Renderer prüfen. Nicht allein nach der Schönheit des Einzelbilds entscheiden. Lesen sich Figur, Schott und Gefahren bei kleinem Fenster? Ist die Spur frei? Stimmen Maßstab und Licht? Stimmen Hintergrundboden und Füße? Erst danach die Platte in AssetCatalog zuweisen und das Manifest aktualisieren.

Die Hintergründe sind detaillierte Rasterplatten, Figuren sind vorgerenderte 3D-Modelle. Dieser Mischstil ist transparent dokumentiert und benötigt noch die Art-Abnahme des Teams. Es wird nicht behauptet, dass die Konzeptbilder bereits vollständig animierte Spielgrafik darstellen.
