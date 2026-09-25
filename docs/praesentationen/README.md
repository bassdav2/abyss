# Präsentationen

| Foliensatz | Anlass | Umfang | Sprechtext |
|---|---|---|---|
| [ABYSS_M1_Projektskizze.pdf](ABYSS_M1_Projektskizze.pdf) | M1 · Pitch an die Geschäftsleitung (SW3) | 12 Folien, ≈ 10 min | [Sprechtext](ABYSS_M1_Projektskizze_Sprechtext.md) |
| [ABYSS_Aufbau_und_Architektur.pdf](ABYSS_Aufbau_und_Architektur.pdf) | M2 Lösungsarchitektur und M3 Prototyp: wie ABYSS aufgebaut ist | 17 Folien; M2-Teil 1–9 (≈ 10 min), M3-Teil 10–17 (≈ 10 min) | [Sprechtext](ABYSS_Aufbau_und_Architektur_Sprechtext.md) |

![M1](vorschau/ABYSS_M1_Projektskizze.png) ![Architektur](vorschau/ABYSS_Aufbau_und_Architektur.png)

Die Folien folgen den Kapiteln der Berichte in `docs/m1-projektskizze`, `docs/m2-loesungsarchitektur` und `docs/m3-prototyp`. Die PM3-Vorgabe von etwa 10 Minuten plus 5 Minuten Fragen passt zum M1-Satz. Für M2 oder M3 wählt das Team die passenden Folien aus dem Architektur-Satz. Namen und Rollen stellt das Team mündlich vor; auf den Folien steht „Team ABYSS“.

## Neu erzeugen

```bash
./gradlew sceneShot --args="build/scenes 0,4,9,14,19 4"
python3 tools/create_slides.py
```

Der Generator setzt die Titel in der eigenen Pixelschrift des Spiels (`src/main/resources/pixel/font.txt`), nutzt echte Spielszenen aus dem Renderer, Bildschirmfotos aus `docs/qa/screens` und rendert die UML-Diagramme per `rsvg-convert` scharf. Texte und Sprechnotizen stehen direkt in `tools/create_slides.py`.

## Archiv

| Ordner | Inhalt |
|---|---|
| [archiv/2026-09-21_konzeptdemo](archiv/2026-09-21_konzeptdemo) | erste Konzeptdemo: Forschungs-U-Boot, vier KI-Konzeptbilder, HTML-Bildfolge |
| [archiv/2026-09-22_swen1-le02-forschungs-u-boot](archiv/2026-09-22_swen1-le02-forschungs-u-boot) | SWEN1-LE02-Präsentation (Persona, Usability, Anforderungen) mit PPTX, PDF und Abgabe |
| [archiv/2026-09-21_konzept-v2-roguelite](archiv/2026-09-21_konzept-v2-roguelite) | Konzept v2 „Vom Heck bis zur Brücke“ mit fünf Konzeptbildern, PPTX, PDF, Sprechtext |

Die Konzeptbilder im Archiv sind KI-generierte Ansichten aus der Planungsphase, keine Screenshots des Spiels.
