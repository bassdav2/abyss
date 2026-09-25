# ABYSS v2 – Vom Heck bis zur Brücke

Stand: 21.09.2026. Neuer Konzeptvorschlag nach Davids Richtungsentscheid. Dieser Entwurf ersetzt für die weitere Planung die frühere Forschungssimulation mit einem kleinen U-Boot. Das Spiel ist noch nicht implementiert.

## Spielidee

Ein riesiges U-Boot bildet die gesamte Spielwelt. Eine einzelne Figur beginnt in den engen Wartungs- und Wohnbereichen am Heck und arbeitet sich durch bewachte Sektionen bis zur Brücke vor. Die räumliche Hierarchie greift Davids Snowpiercer-Assoziation auf. Die Welt, Figuren und Gegner werden eigenständig gestaltet.

Die Kamera zeigt einen horizontalen 2D-Querschnitt. Man bewegt die Figur, weicht Angriffen aus und kämpft mit einem industriellen Werkzeug. Die Aufmerksamkeit liegt auf dem Innenraum. Tiefsee, Bullaugen, Metallgeräusche, Lecks und Druckschotten vermitteln die Umgebung. Eine vollständige U-Boot-Steuerung mit Ballast, Pumpenmanagement und Auftrieb gehört nicht mehr zum Kernspiel.

## Der wiederholbare Ablauf

1. Im Heck ein freigeschaltetes Startmodul wählen.
2. Einen Raum erkunden und seine Begegnung bewältigen. Ein Raum kann Kampf, Gefahr, Werkstatt oder Beute enthalten.
3. An Abzweigungen eine verständliche Entscheidung treffen: zum Beispiel sichere Erholung oder zusätzliche Beute hinter einem Gegner.
4. Mit gefundenen Modulen die aktuelle Ausrüstung verändern und weiter zur Brücke vordringen.
5. Den Wächter an der Brücke besiegen. Danach den Run abschliessen oder freiwillig einen weiteren Tauchzyklus beginnen.

**Niederlage:** Aktuelle Beute und Run-Fortschritt gehen verloren. Entdeckte Baupläne bleiben als weitere Startmöglichkeiten erhalten. Der nächste Versuch beginnt im Heck mit einem neuen Seed. Baupläne ermöglichen andere Spielweisen, nicht unbegrenzte permanente Schadenssteigerungen.

**Sieg und Fortsetzung:** Wer weiterspielen möchte, behält seinen aktuellen Build und beginnt eine neu kombinierte Route. Zusätzliche Gefahrenregeln erhöhen die Schwierigkeit. Gesundheits- und Schadenswerte erhalten sinnvolle Obergrenzen. Der Loop kann weiterlaufen, während Kombinationen und Situationen variieren. Ein Ausstieg nach einem Sieg bleibt möglich.

**Was „endlos“ bedeutet:** Wiederholbare Kombinationen eines begrenzten, sorgfältig gebauten Inhaltsbestands. Wir erzeugen keine unendlich vielen einzigartigen Grafiken oder Geschichten. Die handgebauten Räume werden nach Regeln verbunden, statt bei jeder Runde komplett neu gezeichnet zu werden. Ihre Reihenfolge und Begegnungen können sich pro Durchlauf ändern. Innerhalb einer laufenden Runde bleibt die räumliche Welt konsistent.

## Vorschlag für Version 1.0

| Bestandteil | Begrenzter erster Umfang |
|---|---|
| Spielmodus | Einzelspieler, Desktop, Tastatur und Maus |
| Spielfigur | Eine Figur mit Bewegung, Ausweichen, Basisangriff und einer Modulaktion |
| Welt | Drei Sektionen: Heck, Maschinenbereich, Kommando |
| Run | Zwölf Räume auf der gewählten Route, inklusive Werkstätten und Abschlussraum |
| Raumvorrat | Zunächst zwölf handgebaute Vorlagen mit kompatiblen Ein- und Ausgängen |
| Gegner | Drei Grundtypen mit gut lesbaren Angriffen und ein Brückenboss |
| Ausrüstung | Sechs klar unterscheidbare Module, begrenzte Kombinationsregeln |
| Fortschritt | Baupläne für alternative Startmodule |
| Wiederspielwert | Neue Raumfolgen, Begegnungen, Beute und optionale weitere Tauchzyklen |
| Zielzeit | Etwa 20–30 Minuten pro erfolgreichem ersten Run, noch durch Spieltests zu prüfen |

Diese Mengen sind Planungsannahmen. Zuerst entsteht ein Ausschnitt aus drei Räumen mit einem Gegner und zwei Modulen. Erst wenn Bewegung, Treffergefühl und Entscheidungen funktionieren, wird der Umfang bestätigt.

## Konsistente Grafik

Die fünf Bilder im Ordner `images` definieren die Richtung. Sie sind Konzeptbilder und enthalten noch keine verwendbaren Animationen oder getrennten Spielebenen.

- Direkte Seitenansicht, feste Bodenhöhe und einheitliche Figurengrösse.
- Eine wiedererkennbare Figur: senfgelbe Jacke, dunkle Hose, türkisfarbener Atemrucksack und Stirnlampe.
- Gemeinsame Bauteile für Stahlwände, Türen, Rohre, Leitern, Treppen und Bullaugen. Die gleiche Tür bleibt überall dieselbe Quelldatei.
- Hinten warmes, abgenutztes Metall; vorne zunehmend kühles Licht und kontrollierte Räume. Diese Unterschiede entstehen aus abgestimmten Material- und Lichtvarianten.
- Bewegliche Figuren, Gegner, Türen, Effekte und Vordergrund werden getrennt exportiert. Hintergrundmalerei enthält keine spielrelevanten Elemente, die später separat reagieren müssen.
- Pro Grafik gibt es Quelle, Exportparameter, Grösse, Ankerpunkt und eine Versionsnummer. KI-Referenzen allein garantieren keine Konsistenz.

Blender kann die orthografischen Räume, wiederverwendbaren Requisiten und gegebenenfalls die Figurenanimationen liefern. Für den ersten spielbaren Ausschnitt reichen einfache Platzhalter. Imagegen dient der Art Direction und ausgewählten Hintergrundentwürfen. Die vorhandene Java/JavaFX-Richtung bleibt der Ausgangspunkt für PM3; Änderungen an Bibliotheken werden anhand der Kursvorgaben entschieden.

## Was sich technisch ändert

Im Zentrum stehen jetzt Figurenbewegung, Kollisionen, Gegnerzustände, Angriffe, Schaden, Ausrüstung und der Run-Ablauf. Raumvorlagen werden als Daten mit Anschlüssen und zulässigen Begegnungen beschrieben. Ein Seed reproduziert einen Durchlauf und hilft bei Fehlern. Die Spiellogik bleibt von JavaFX und Grafikdateien getrennt.

Die Generierung verbindet nur passende Anschlüsse und prüft, dass Start, gewählte Route und Brücke erreichbar sind. Treppen und Türen müssen auch in der Kollisionsgeometrie funktionieren. Gefahren dürfen einen frisch betretenen Raum nicht ohne Reaktionszeit tödlich machen. Sichere Räume erscheinen an kontrollierten Stellen. Beuteangebote vermeiden Kombinationen, die ein Fortkommen zwingend voraussetzen, aber nicht garantieren.

Eine kompakte Zustandsmaschine steuert Start, Raumwechsel, Pause, Niederlage, Sieg und nächsten Zyklus. Baupläne und Einstellungen werden lokal gespeichert. Ein sicherer Unterbruch zwischen Räumen ist als Komfortfunktion zu prüfen; eine beliebige Momentaufnahme während eines Kampfes erhöht den Aufwand und ist kein stillschweigender Bestandteil des ersten Prototyps.

## Reihenfolge der Umsetzung

1. **Spielgefühl:** Ein Raum, Bewegung, Ausweichen, Angriff, ein Gegner. Abnahme durch tatsächliches Spielen.
2. **Ein vollständiger kurzer Run:** Drei Räume, eine Abzweigung, zwei Module, Niederlage und Neustart. Schon hier den geplanten Nutzertest durchführen.
3. **Grafischer Musterraum:** Feste Kamera, Figur, Beleuchtung und ein freigegebenes Raumset. Lesbarkeit und Performance unter Bewegung prüfen.
4. **Inhalt und Generierung:** Weitere Räume, Gegner, zwölf Räume pro Route und ein Abschlussboss. Viele Seeds auf Erreichbarkeit und zulässige Kombinationen prüfen.
5. **Langzeitablauf:** Baupläne, optionale weitere Zyklen, verständliche Ergebnisanzeige und begrenzte Schwierigkeitsmodifikatoren.
6. **Politur und Auslieferung:** Audio, Trefferfeedback, Bedienung, Pause, Einstellungen, Performance und ein paketierter Build auf den Zielsystemen.

Der Wiederholungsloop spart den Bau einer grossen linearen Kampagne. Er verschiebt den Aufwand zu Kampfgefühl, Balancing und Raumkombinationen. Diese Teile werden deshalb vor der Produktion vieler Assets erprobt.

## Neue Bildserie

1. Das gesamte lange Boot: räumliche Hierarchie und Ziel der Reise.
2. Raumkampf: Figur und Gegner mit angekündigtem Angriff.
3. Werkstatt: Wahl zwischen offensivem und defensivem Modul.
4. Abzweigung: erreichbare Werkstatt oder bewachtes Depot.
5. Brücke: Wächter als Abschluss des Runs.

Die genauen Prompts stehen in `PROMPTS.md`. Die überarbeitete Präsentation und die SWEN-Abgabe beschreiben dieses Konzept.
