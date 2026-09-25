> **Historisch:** Diese Seite beschreibt den Ausbau 0.2. Den aktuellen Stand beschreiben `MORGEN.md`, `INHALTE.md` und `ARCHITEKTUR.md`.

# ABYSS 0.2 - Ausbau am 22.09.2026

Auf Davids ausdrücklichen Wunsch wird der erste Nachtstand erweitert. Der ursprüngliche lokale Commit 608c280 bleibt als Vergleich erhalten.

## Spielbarer Inhalt

18 statt 12 Räume, weiterhin drei Sektionen. Ein eigener Sektorwächter steht an Raum 5 und 11, der Lotse an Raum 18. Werkstätten folgen an Raum 6 und 12. Zwölf Positionen bieten zwei Abzweige. Torpedomagazin, Krankenstation, Ballastkammer und Sauerstoffgarten ergänzen die Umgebung. Riskante Abzweige haben passende alternative Raumgrafiken.

- **Schottmeister:** Druckpanzer auf Raupen, abwechselnd Ansturm und Bodenwelle; lange Erholung. Panzerung lässt 50 Prozent Schaden durch.
- **Reaktorkern:** Sechsbeinige Maschine mit offener Energiekugel; Bodenwellen und breites Fächerfeuer, nach halber Integrität dichter. Panzerung lässt 38 Prozent Schaden durch.
- **Lotse:** Bisheriger Endboss mit Bodenwelle, Fächerfeuer und Ansturm; Panzerung lässt 22 Prozent Schaden durch. Alle Bosse sind in ihrer Erholung voll verwundbar.

Die sechs neuen passiven Items verändern Reichweite, Schlagtempo, Bewegung, Energierückgewinnung, Kettenblitze und Reparatur beim Raumabschluss. Insgesamt zwölf Items mit jeweils drei Stufen. Sie sind keine neue dauerhafte Charakterprogression: Sie gehören zum aktuellen Run und seinen Folgezyklen.

Zerstörbare Vorratskisten enthalten Reparaturgel (+18 Integrität), Energiezellen (+30 Energie) oder Ersatzteile (+8 Schrott). Kisten werden mit normalen Werkzeugschlägen geöffnet und zahlen nur einmal aus. Position und Inhalt entstehen deterministisch aus Seed, Raum und Zyklus.

Reparatursets werden mit Q gezielt eingesetzt (+35 Integrität). Ein frischer Run beginnt mit einem Set; maximal drei sind mitnehmbar. Werkstätten verkaufen zusätzliche Sets für 20 Schrott. Volle Integrität verbraucht kein Set. Die Q-Taste reagiert auf einen neuen Tastendruck, nicht auf die Wiederholung bei gehaltenem Q.

## Oberfläche und Atmosphäre

- I oder Tab: pausierendes Inventar mit allen zwölf Items, Stufen und aktuellen Charakterwerten.
- M: pausierende Bootskarte, eigene Position, vergangene Räume, bevorstehende Sektorwächter.
- Routenentscheidungen mit echten Raumvorschauen; Item-Piktogramme auf Bergungskarten, im HUD und Inventar identisch.
- Kompakteres HUD mit Ressourcen, Fortschrittslinie, Icon-Inventar und getrennten Dash-/Fähigkeitsanzeigen.
- Langsam versetzter Hintergrund, Lichtkegel, Stirnlampe, Vordergrundkabel, Bodennebel und Kondenswasser. Reduzierte Bewegung stoppt die bewegten Umwelteffekte.

Die räumliche Wirkung entsteht aus mehreren Render-Ebenen. Die eigentliche Spielspur bleibt horizontal; dekorative Bildobjekte sind keine Plattformen.

## Bestehende Speicherstände

Neue Profile und Checkpoints verwenden Version 2. Version 1 wird beim Lesen auf die entsprechende Sektion der längeren Route übertragen; Items und Ressourcen bleiben bestehen. Vor dem nächsten Überschreiben wird die ursprüngliche Datei als .bak gesichert. Es wird weder ein Save stillschweigend gelöscht noch ein älterer Run als neuer vollständiger 18-Raum-Sieg verbucht.

## Grafikquellen

Vier neue Raumplatten per eingebautem ImageGen, als Stilreferenz die existierende Hecksektion. Kein externer API-Aufruf mit eigenem Schlüssel. Zwei zusätzliche vollständig editierbare Blender-Modelle mit je 16 Animationsframes. Die zwölf UI-Piktogramme sind eigene Canvas-Zeichnungen in ItemGlyph.java, keine heruntergeladenen Icons. Prompts: ART_PROMPTS.md.
