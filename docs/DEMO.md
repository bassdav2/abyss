# Gameplay-Demo

`output/video/Abyss_Gameplay_Demo.mp4` ist ein 82-Sekunden-Video mit tatsächlich durch den JavaFX-Renderer gezeichnetem Gameplay. Der Testspieler verwendet reguläre Spieleingaben. Maschinenraum und Boss starten aus vorbereiteten, gültigen QA-Checkpoints mit passiven Upgrades. Zwei Menübilder stammen aus echten JavaFX-Screenshots. Eine permanente Einblendung benennt den automatisierten Charakter der Aufnahme.

| Zeit | Inhalt |
|---|---|
| 0-3 s | Titel / Konzept des langen Boots |
| 3-21 s | Start im Heck, Bewegung, Nahkampf und Ausweichen |
| 21-26 s | Werkstatt und Modulentscheidung |
| 26-44 s | Maschinenbereich mit Lichtbogen und Raumgefahren |
| 44-49 s | Routenwahl |
| 49-79 s | Brückenboss und Panzerungsfenster |
| 79-82 s | Abschlussbild |

Die Tonspur verwendet die eigene synthetische Musik. Sie ist kein Mitschnitt der während des Tests hörbaren Ausgabe; die automatischen Tests liefen stumm. Erzeugen: `./gradlew renderDemo` (FFmpeg erforderlich). Die Aufnahme ersetzt keinen menschlichen Spieltest.
