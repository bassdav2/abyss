# Sprechtext · Aufbau und Architektur

Präsentation zu Aufbau und Architektur für M2 (Lösungsarchitektur) und M3 (Prototyp). Inhalt aus TECHNISCHER_BERICHT_I.md und TECHNISCHER_BERICHT_II.md.

Geplante Redezeit: etwa 20 Minuten, 17 Folien. PDF: [ABYSS_Aufbau_und_Architektur.pdf](ABYSS_Aufbau_und_Architektur.pdf).

## 01 · Titel (≈ 0.5 min)

- Worum es geht: wie ABYSS aufgebaut ist, von den Use Cases bis zur Implementation.
- Grundlage: Technischer Bericht I (M2) und II (M3) im Ordner docs.

## 02 · Was ist gebaut? (≈ 1 min)

- Umfang Version 1.1: 95 Klassen im Hauptcode, rund 23 000 Zeilen, 13 Testklassen mit 106 Tests, 8 QA-Werkzeuge.
- Technik: nur Java und JavaFX. Physik, Rendering und GUI sind selbst gebaut.
- Ein Bild wird im Mittel in 1,9 ms berechnet. Das ist weit unter den 16 ms für 60 FPS.

## 03 · Use-Case-Modell (≈ 1 min)

- Eine Akteurin, die Spielerin. Das Dateisystem ist Nachbarsystem für Sichern und Fortsetzen.
- UC-02 ist der Kernfall und vollständig ausgearbeitet (fully dressed), inklusive Erweiterungen wie Raumzustände und Raumtechnik.
- Die anderen Fälle sind je nach Wichtigkeit casual oder brief beschrieben.

## 04 · Kernfall und Systemsequenz (≈ 1 min)

- Das Systemsequenzdiagramm zeigt die Systemoperationen des Kernfalls aus Sicht der Spielerin.
- Wichtig ist der feste Simulationsschritt: Die Spiellogik läuft unabhängig von der Bildrate.
- Erweiterungen decken Niederlage, Raumzustände und die Bedienung der Raumtechnik ab.

## 05 · Domänenmodell (≈ 1 min)

- Das Domänenmodell zeigt die Begriffe der Spielwelt, noch ohne Software-Klassen.
- Ein Tauchgang besteht aus Räumen, jeder Raum hat Gegner, einen möglichen Zustand und Anlagen.
- Das Profil überlebt die Tauchgänge: Datenkerne, Freischaltungen, Logbuch.

## 06 · Schichtenarchitektur (≈ 1.5 min)

- Die Abhängigkeiten zeigen nur nach innen: UI → Application → Domain. Die Domäne kennt weder JavaFX noch Dateien.
- Speicher läuft über einen Port (GameRepository). Die Datei-Implementierung ist austauschbar und im Test ersetzbar.
- Dadurch sind Spielregeln ohne Fenster testbar (Q-01).

## 07 · Einflussfaktoren und Entscheidungen (≈ 1.5 min)

- Die vier wichtigsten Einflussfaktoren und wie die Architektur darauf antwortet, jeweils mit Beleg.
- Zehn Architekturentscheidungen sind in ARCHITEKTUR.md als ADR festgehalten. ADR-09 und ADR-10 sind in Version 1.1 neu bzw. geändert.

## 08 · Design-Klassen und Muster (≈ 1.5 min)

- GameRun ist das Aggregat. Es delegiert an package-private Mitarbeiter wie PlayerMotor, Arsenal, Ballistics, Loot, Rewards und Machinery.
- Gegnerverhalten ist ein Strategie-Muster: 17 Verhaltensklassen, zugeordnet über eine Fabrikmethode in EnemyKind.
- Die Domäne erzeugt Ereignisse. Renderer und Audio lesen sie, ohne die Regeln zu beeinflussen.

## 09 · Systemoperationen (≈ 1.5 min)

- Fünf Systemoperationen mit je einem Interaktionsdiagramm (verlangt sind mindestens vier).
- Links der Kampfschritt: GameRun aktualisiert Figur, Gegner, Geschosse und Anlagen und erzeugt Ereignisse.
- Rechts der Raumwechsel: Generator erzeugt den Raum deterministisch aus dem Seed, danach wird der Checkpoint gesetzt.

## 10 · Spielschleife und Render-Pipeline (≈ 1.5 min)

- Die Simulation läuft in festen Schritten, das Zeichnen so oft wie der Bildschirm es erlaubt.
- Gezeichnet wird in einen eigenen Pixel-Puffer von 480 × 270 Punkten. Licht und Bloom rechnen wir selbst.
- Erst am Ende wird das Bild an JavaFX übergeben und scharf vergrössert.

## 11 · Gegner als Zustandsmaschine (≈ 1 min)

- Alle Gegner teilen eine Zustandsmaschine. Wichtig ist der Zustand „Ankündigen“: Er macht Angriffe lesbar.
- Das Verhalten pro Art steckt in einer Strategie-Klasse. So bleibt GameRun unverändert, wenn ein Gegner dazukommt.
- Die Figuren rechts sind nicht gezeichnet, sondern werden im Code Pixel für Pixel erzeugt.

## 12 · Bordsystem-GUI und Navigation (≈ 1 min)

- Links oben die Navigation zwischen allen Bildschirmen.
- Seit Version 1.1 sind die Menüs keine JavaFX-Controls mehr, sondern Teil des Spielbilds. Knöpfe, Schieber und Karten zeichnet die Klasse Gui.
- Vorteil: einheitlicher Pixel-Look und dieselbe Bedienung mit Maus und Tastatur.

## 13 · Raumtechnik und Raumzustände (≈ 1 min)

- Sechs Anlagentypen: Förderband, Dampfdüse, Lüfter, Presse, Laser, Notschalter.
- Fünf Raumzustände: Stromausfall, Alarm, Leck, Hüllenbruch, Schlagseite. Jeder mit eigener Regel und Belohnung.
- Anlagen sind fair: Sie treffen auch die Figur, kündigen sich aber an. In Bossarenen legen sie den Kern des Wächters frei.

## 14 · Teststrategie (≈ 1 min)

- Viele schnelle Unit-Tests unten, wenige breite System- und Oberflächentests oben.
- Ein Testspieler (Bot) spielt ganze Tauchgänge. Damit prüfen wir Balance und Abstürze über viele Seeds.
- Was fehlt: Tests mit echten Menschen. Die kommen in Iteration 3.

## 15 · KI-Einsatz (≈ 1 min)

- Offen und vollständig: Code und Dokumente sind stark KI-unterstützt entstanden.
- Die Tabelle folgt der PM3-Vorgabe mit Ziel, Aufwand, Verwendung und Art der Übernahme.
- Wichtig für die Bewertung: Das Team muss jede Klasse erklären können. Dafür gibt es die Paketverantwortung.

## 16 · Stand und Ausblick (≈ 1 min)

- Der Prototyp ist spielbar von der Schleuse bis zur Brücke.
- Offen sind vor allem Spieltests mit Menschen und die Prüfung auf anderen Betriebssystemen.
- Das Spiel wird weiter ausgebaut. Neue Inhalte folgen der bestehenden Architektur (Strategie für Gegner, Enums für Inhalte).

## 17 · Demo und Fragen (≈ 1.5 min)

- Live-Demo, falls Zeit bleibt, sonst Gameplay-Demo.mp4.
- Fragen beantworten.
