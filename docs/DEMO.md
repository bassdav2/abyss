# Gameplay-Demo 1.0

`output/video/Abyss_Gameplay_Demo.mp4` (Kopie: `Spiel/Gameplay-Demo.mp4`) zeigt knapp 90 Sekunden aus dem echten Pixel-Renderer von ABYSS 1.0, verlustfrei hochskaliert von 480 × 270 auf 1920 × 1080. Gespielt wird vom automatischen Testspieler (`qa/CampaignPilot`) mit normalen Eingaben. Jeder Ausschnitt ist ein frischer Tauchgang mit festem Seed; der Testspieler spielt bis zur gezeigten Raumposition vor. Eine Einblendung oben in der Mitte weist das Video als automatische Demo aus.

| Zeit | Inhalt |
|---|---|
| 0–5 s | Titel mit dem U-Boot im Längsschnitt |
| 5–18 s | Hecksektion, erste Räume (Mechanikerin, Harpunier) |
| 18–28 s | Schottmeister, erster Sektorwächter |
| 28–35 s | Maschinendeck |
| 35–45 s | Reaktorkern |
| 45–52 s | Forschungsdeck |
| 52–62 s | Brutmutter |
| 62–69 s | Kommandodeck |
| 69–81 s | Lotse auf der Brücke |
| 81–89 s | Siegesszene: Das Boot steigt zur Oberfläche auf |

Die Klassen wechseln von Ausschnitt zu Ausschnitt. Als Tonspur läuft die eigene synthetische Musik in Schleife; Kampfklänge sind nicht eingemischt. Die Aufnahme ersetzt keinen menschlichen Spieltest: Der Bot reagiert fehlerfrei und geht direkt auf das nächste Ziel.

Erzeugen (FFmpeg erforderlich):

```sh
./gradlew renderDemo
```
