# ABYSS 1.1 – Überblick für David

Stand 25.09.2026, Nachmittag. Version 1.1 baut auf dem Nachtstand 1.0 auf; was dazugekommen ist, steht direkt unter der Steuerung. Aus deinem Prototyp ist über Nacht ein vollständiges Pixel-Art-Roguelite geworden. Es bleibt dabei: ein riesiges U-Boot, du startest ganz hinten im Heck und kämpfst dich Raum für Raum bis zur Brücke vor. Geschrieben ist alles in Java mit JavaFX, ohne Frameworks und ohne neue Bibliotheken.

## Sofort spielen

`Spiel/Abyss.app` im Ordner `Documents/Abyss` doppelklicken, Java ist dabei. Beim ersten Start fragt macOS eventuell nach, weil die App nicht notarisiert ist: Rechtsklick → Öffnen.

Aus dem Quellcode: `./run.command` oder `./gradlew run` (JDK 25).

| Taste | Aktion |
|---|---|
| A / D (oder Pfeile) | laufen |
| Leertaste / W | springen (gedrückt halten = höher), auf Laufstegen landen |
| S + Leertaste | durch einen Laufsteg nach unten fallen |
| J / linke Maus | angreifen (Kombination), in der Luft: Luftangriff |
| Shift / L | ausweichen (kurz unverwundbar) |
| K / rechte Maus | aktives Modul (kostet Energie) |
| E | Bergung öffnen, handeln, Schott benutzen, Notschalter bedienen |
| Q | Reparaturset |
| I / Tab · M | Ausrüstung · Bootskarte |
| Esc | Pause · in Menüs zurück |
| Menüs | Pfeile/WASD oder Maus, Enter/Leertaste wählt, Ziffern wählen Karten direkt |

Für den ersten Versuch: **Neuer Tauchgang → Die Mechanikerin → Tauchen**. Bis du zum ersten Mal einen Raum sicherst, läuft vor dem Tauchgang ein kurzer Auftakt, den jede Taste überspringt. Wer es gemütlicher will, schaltet in der Vorbereitung **Entdecker** ein.

## Neu in Version 1.1

**Menüs wie im Boot.** Alle Menüs sind jetzt Bordsysteme im selben Pixelbild wie das Spiel. Die Vorbereitung ist eine Schleuse mit Spinden, die Garderobe ein Spiegel mit animierter Figur, das Archiv ein Terminal. Bergungen erscheinen als schwebende Hologramm-Karten, der Händler als Regal, und in der Routenwahl siehst du Kamerabilder der nächsten Räume. Tasten sinken beim Drücken ein, Karten haben Zielrahmen, und statt des Systempfeils gibt es einen Pixel-Mauszeiger. Die fremden Schriften sind weg; alles nutzt die eigene Pixelschrift.

**Interaktive Räume.** Neu ist Raumtechnik je Sektion:
- Förderbänder schieben dich und die Gegner.
- Dampfdüsen schleudern dich auf hohe Laufstege.
- Turbinenwind drückt in Abständen alles zur Seite.
- Hydraulikpressen stampfen nach einer Warnung auf den Boden.
- Lasergitter takten an und aus.
- Notschalter lösen auf E einen Effekt der Sektion aus: Torpedo, Dampfventil, Kältekammer oder Überlast.

Gegner lassen sich in Pressen und Laser locken.

**Bossarenen.** Jeder Wächter kämpft zwischen Anlagen: der Schottmeister zwischen zwei Pressen, der Lotse zwischen Lasergittern. Trifft eine Anlage den Boss, durchschlägt sie seine Panzerung und legt den Kern kurz frei.

**Mehr Spannung.** Zwei neue Raumzustände:
- *Hüllenbruch:* 40 Sekunden überleben, während Gegner nachströmen und das Wasser steigt.
- *Schlagseite:* Das Boot krängt, alles rutscht, Trümmer fallen, und das Bild kippt.

Dazu kommen *Schmugglerdrohnen*, die mit Beute fliehen und nach 15 Sekunden entkommen.

**Mehr Abteilungen.** Acht neue Raumthemen mit eigener Einrichtung: Kombüse, Wäscherei, Kesselraum, Generatorraum, Datenarchiv, Kryolabor, Waffenkammer, Kartenraum. Insgesamt sind es 33.

**Kleineres.** Drei neue Logbuch-Einträge (26 insgesamt). Der Koloss ist etwas schneller. Die Tabelle aller Inhalte steht in `docs/INHALTE.md`.

## Was mit 1.0 kam

**Grafik.** Statt Hintergrundbild und Blender-Renderings gibt es jetzt echte Pixel-Art in 480 × 270, hochskaliert ohne Weichzeichnung. Alles wird im Java-Code gemalt: Figur mit 15 Animationen, zwölf Gegnerarten, vier Bosse, 25 Raumthemen, Symbole für alle Items. Dazu gestuftes, gerastertes Licht (Deckenlampen, flackernde Leuchten, Stirnlampe, leuchtende Augen), Bloom, eine Farbstimmung pro Sektion, Partikel (Funken, Glut, Rauch, Blasen, Trümmer, Eis), Trefferpause, Zeitlupe beim Bosstod, Bildschirmwackeln, Schadenszahlen, Parallaxe-Meer hinter den Bullaugen mit Fischschwärmen und einem Leviathan. Optional gibt es einen Röhrenfilter. Das Titel-U-Boot ist neu gemalt, und unter dem Boden sieht man jetzt das Unterdeck mit Bilgenwasser.

**Spiel.** Plattform-Kampf mit Laufstegen, Sprüngen, Ausweichen, Kombinationen, Luftangriff und Bodenstampfer. Der Weg führt durch **24 Räume in vier Sektionen**, mit Routenwahl, Elite-Räumen, Schwarzmarkt, Werkstatt, Versorgungsdepot und Druckkapelle (Opfer gegen Macht). Manche Räume haben einen **Raumzustand**: Bei *Stromausfall* leuchtet nur deine Stirnlampe (dafür mehr Schrott, nach dem Sichern flackert das Licht wieder an), bei *Alarmstufe Rot* kreisen Warnlichter und es kommen mehr Gegner (dafür seltene Bergung), beim *Druckleck* tropft Wasser und Dampf schiesst aus dem Boden (dafür zusätzliche Kisten). **Vier Bosse** mit je drei Phasen: Schottmeister, Reaktorkern, Brutmutter, Lotse. Wer die Brücke erobert, sieht das Boot in einer eigenen Szene zur Oberfläche aufsteigen. Danach geht es weiter mit **Zyklen** und **Druckstufen 0–5**.

**Inhalt.** 5 Klassen, 7 Waffen (jede mit eigener Kombination; neu ist der Enterhaken, dessen dritter Stoss Gegner zu dir heranzieht), 8 aktive Module, 41 passive Module in vier Seltenheiten (inklusive verfluchter), **9 Resonanzen** (Modulpaare mit Zusatzbonus, z. B. Zündkerze + Übertakter = Feuersturm; die Bergungskarte zeigt an, wenn ein Angebot eine Resonanz vervollständigt; alle Paare stehen im Archiv unter „Resonanzen“), 12 Gegnerarten (neu im Kommandodeck: Sicherheitsautomat mit Schockstab-Sprungangriff und Suchlichtsonde, deren Scheinwerfer dich verfolgt), 5 Elite-Eigenschaften, 5 Zustände (Brand, Kälte, Frost, Markierung, Schock). Die vollständige Liste erzeugt `./gradlew contentCatalog` in `docs/INHALTE.md`.

**Fortschritt und Anpassung.** Datenkerne als dauerhafte Währung, dazu ein Archiv zum Freischalten von Klassen, Waffen, Modulen, Bauplänen und Ausrüstung. In der Garderobe wählst du Anzugfarbe, Helmform, Visier und Metallton. Das Kompendium zeigt entdeckte Module (unentdeckte als Silhouette), das **Logbuch** 23 Ziele, die einmalig Kerne bringen. Mit **Tagestauchgang** bekommen alle am selben Tag dieselbe Route. Dein alter Spielstand von 0.2 wird beim ersten Start übernommen und vorher als `.bak` gesichert.

**Ton.** 15 neue selbst erzeugte Klänge, insgesamt 34.

## Was geprüft ist

- **106 JUnit-Tests** grün, dazu Formatprüfung und Javadoc ohne Warnungen.
- **Kampagnensimulation:** Ein Testspieler spielt mit normalen Eingaben komplette Tauchgänge, pro Klasse 8 Seeds und einen Lauf auf Druckstufe 5. Kein Raum bleibt hängen.
- **Balancebericht** über 30 Seeds pro Klasse: vier Klassen 30/30, der Koloss 26/30 (für den Bot die schwerste Klasse, deshalb auf 150 Integrität und 20 % Schutz angehoben). Dabei gefundene Hänger (Gegner auf hohen Stegen, Harpune gegen Flieger) sind behoben.
- **JavaFX-Prüfung** mit echtem Fenster: 37 Schritte durch alle Bordsystem-Bildschirme und Spielzustände, vom Auftakt bis zur Siegesszene. Die Bildschirmfotos liegen in `docs/qa/screens`.
- **Render-Probelauf:** rund 2 ms pro Bild, also weit unter den 16 ms für 60 FPS.
- **Architektur:** Domäne, Anwendung, Ports, Infrastruktur und UI sind getrennt, Controller enthalten keine Spiellogik. `GameRun` und der Renderer sind in kleinere Klassen aufgeteilt. Die 13 UML-Diagramme in `docs/diagrams` sind neu erzeugt.

## Was nur du prüfen kannst

Automatische Tests sagen nichts darüber, ob es sich gut anfühlt. Bitte etwa 15 Minuten selbst spielen und auf diese Fragen achten:

1. Fühlen sich Sprung, Ausweichen und Treffer gut an?
2. Sind die Vorwarnungen der Gegner und Bosse lesbar?
3. Stimmen Lautstärke und Tempo der Klänge? Gehört wurden sie bisher nicht, nur geladen.
4. Ist die Schwierigkeit passend? Die Siegquoten des Testspielers sind obere Grenzen, denn der Bot reagiert perfekt.

Nicht geprüft sind Windows/Linux, Gamepad, Vollbild auf mehreren Monitoren und Tests mit echten Menschen.

## Für PM3

- Kursvorgaben eingehalten: Java, JavaFX, eigene Schichten, keine Frameworks oder Datenbank, keine neuen Bibliotheken. Die drei Pixel-Schriften stehen unter OFL, Herkunft siehe `CREDITS.md`.
- Der KI-Einsatz ist in `docs/KI_EINSATZ.md` dokumentiert (Tool, Ziel, Übernahme). **Wichtig für die Abgabe:** Der Code von 1.0 wurde weitgehend von Claude geschrieben. Das muss das Team im Bericht offenlegen und selbst verstehen können.
- Aktualisiert sind Anforderungen/Use Cases, Architektur, Teststrategie, Glossar, Asset-Pipeline, Credits und alle Diagramme. Der technische Bericht als PDF (`output/pdf`) beschreibt noch 0.2.
- **Nichts wurde committet oder eingereicht.** Alle Änderungen liegen uncommittet im Repository `Projekt`, gelöschte 0.2-Grafiken sind als Löschung vorgemerkt. Wenn du willst, committe ich das als Version 1.0.

## Wichtige Dateien

- Einstieg in den Code: `src/main/java/ch/zhaw/abyss/domain/GameRun.java`
- Architektur: `docs/ARCHITEKTUR.md`
- Spielinhalte: `docs/INHALTE.md`
- Arbeitsjournal: `docs/WORK_PLAN.md`
- Demo-Video: `Spiel/Gameplay-Demo.mp4`
- Pakete: `Pakete/Abyss_v1.0_*.zip`, Prüfsummen in `SHA256SUMS.txt`
