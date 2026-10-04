# ABYSS 1.5 – Überblick für David

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

## Neu in Version 1.5 · Klare Sicht

Stand 04.10.2026. Die Oberfläche war mit dem Endgame eng geworden. Deshalb liegen HUD und alle Menüs jetzt auf einer eigenen Ebene in doppelter Auflösung (960 × 540). Die Spielwelt bleibt Pixel-Art in 480 × 270. Fliesstext nutzt die Pixelschrift einfach, Überschriften und wichtige Zahlen doppelt. Auf jeden Bildschirm passt damit etwa doppelt so viel, ohne dass der Pixelstil verloren geht.

- **HUD:**
  - Oben links stehen Integrität, Energie, Sets und Überladung.
  - Oben rechts stehen Raum, Zyklus, Eskalation, Schrott und Kerne.
  - Oben in der Mitte zeigt **eine** Statuszeile Welle und Schwarm, darunter die Bedrohung.
  - Unten links liegen Waffe, Modul und Ausweichen, unten rechts die Module, Entfesselungen zuerst.
  - Schadenszahlen sind kleiner, Banner warten, bis die Raumkarte weg ist.
- **Bergung und Levelaufstieg:** Die Wirkung steht gross auf der Karte. Darunter siehst du, zu welcher Entfesselung ein Modul führt.
- **Ausrüstung:**
  - 15 Werte, darunter Fläche, Modulschaden, Schild, zusätzliche Geschosse und Überladung.
  - Ein Raster für 60 Module.
  - Zu jedem Modul der Stand seiner Entfesselung, z. B. „Kreiselmesser 5/6 · Druckkammer fehlt“.
- **Laufbahn:** Die Reiter sind ausgeschrieben und in Konto und Klassen getrennt. Die Knoten zeigen ihre Wirkung direkt, die Rangleiste steht in der Fusszeile.
- **Weitere Bildschirme:**
  - Routenkarten mit grossem Kamerabild, Bedrohung und Build-Check.
  - Eine Pause mit Überblick über den Tauchgang.
  - Ein Archiv mit 20 Einträgen pro Seite.
  - Ein Logbuch in drei Spalten.
  - In der Schleuse der Laufbahnrang jeder Klasse.
- **Meldungen** aus dem Tauchgang verschwinden beim Wechsel ins Menü.

Die Zeichenzeit steigt dadurch nur leicht: im Mittel 2,2 statt 2,0 ms pro Bild.

## Neu in Version 1.4 · Entfesselt (Endgame)

Stand 03.10.2026. Ab Raum 20 und nach dem ersten Sieg wird das Spiel bewusst verrückt.

**Eskalation ab Raum 20.** Ab dem 20. Raum steigt die Eskalation mit jedem Raum, über alle Zyklen hinweg. Oben rechts zeigt das HUD sie als „E…“ an.
- Schwärme wachsen quadratisch. Im zweiten Zyklus sind es Räume mit tausenden Gegnern, im dritten bis zu 1600 gleichzeitig.
- Kampfräume werden bis zu drei Bildschirme breiter und bekommen ein Oberdeck.
- Es gibt bis zu vier Wellen und deutlich mehr Elitegegner, die Besatzungen aller Decks mischen sich.
- Das Licht kippt ins Violette des Abgrunds.
- Beim ersten Mal kündigt ein Banner an: „Der Abgrund erwacht“.

**Acht Schwarmangriffe.** Fünf neue Schwarmarten:
- Säurespucker werfen Säure im Bogen.
- Zündmilben rennen heran und sprengen sich. Wer sie vorher erwischt, sprengt ihre Nachbarn.
- Prismaquallen entlassen Ringe aus Regenbogengeschossen.
- Speerfische zielen mit einer sichtbaren Linie und schiessen quer durch den Raum.
- Panzerkrabben fangen Treffer von vorn fast ganz ab.

Zusammen mit Rostmilben, Glimmfischen und Nanodrohnen greift der Endgame-Schwarm auf acht verschiedene Arten an. Dazu kommen Brutnester, die Milben speien. Fällt ein Nest, stirbt seine Brut.

**Bedrohungen.** Kampfräume tragen jetzt oft eine Bedrohung, die einen bestimmten Build verlangt. Im ersten Zyklus ist das ab dem Maschinendeck selten, im Endgame fast immer:
- **Panzerschwarm:** Nur kritische Treffer, Brand, Explosionen und Blitze wirken voll.
- **Flutwelle:** dreimal so viele, halb so zähe Gegner. Hilft: Flächenschaden.
- **Kolosse:** wenige riesige Elitegegner mit vierfacher Integrität.
- **Luftschlag:** nur fliegende Gegner.
- **Brutnester**
- **Sperrfeuer:** Geschosse und Einschläge.
- **Regeneration:** Gegner heilen sich, solange sie weder brennen noch frieren.

Die Routenwahl zeigt die Bedrohung und ob dein Build „bereit“ oder „unvorbereitet“ ist. Wer sie besteht, bekommt eine seltene Bergung und einen Datenkern.

**Entfesselungen.** Ist ein Modul ausgereizt und sein Partner installiert, liegt beim nächsten Levelaufstieg eine schillernde Karte obenauf. Es gibt elf:

| Entfesselung | aus | Wirkung |
|---|---|---|
| Klingensturm | Kreiselmesser + Druckkammer | zweiter Rotorring, dreifacher Schaden, schleudert Klingen |
| Gewitterkern | Teslafeld + Teslaspule | Blitze von der Decke in acht Gegner, die weiterspringen |
| Supernova | Kettenreaktion + Druckwellenkern | doppelt so grosse Abschussexplosionen, Nova alle 5 Abschüsse |
| Klingenorkan | Klingenwelle + Mehrfachlader | jeder Schlag ein Fächer aus durchschlagenden Klingen |
| Raketenschwarm | Zielsucher + Kondensator | alle 1,2 s Minitorpedos |
| Todesblick | Glasfaserlinse + Hohlspitzen | mehr Kritik, kritische Treffer explodieren |
| Bollwerk | Schildzelle + Verbundpanzerung | riesiger Schild, beim Bruch eine Druckwelle |
| Höllenglut | Zündkerze + Übertakter | Brand stärker, Brennende stecken Nachbarn an |
| Nullpunkt | Kälteschlange + Kühlkreislauf | Frostwelle, Gefrorene erleiden doppelten Schaden |
| Blutsauger | Nanitenkultur + Rückgewinnung | Lebensraub bis 40 %, Abschüsse heilen |
| Phasensturm | Phasenkern + Klingenrumpf | Ausweichen doppelt so oft, Spur aus Explosionen |

**Grenzbrecher und Überkritik.** Stehen mehrere Module an ihrer Grenze, bieten Levelaufstiege den **Grenzbrecher** an: +2 Höchststufe für alle Standard- und Seltenmodule, stapelbar. Kritische Chance über 100 % wird zur **Überkritik**: zwei oder drei Ausrufezeichen, violett oder schillernd, viel mehr Schaden.

**Bosse mit Lernkurve.** Jeder Wächter hat neue Muster, und jedes hat eine Lösung, die man lernen kann:
- **Schottmeister:** Bei der Finte stürmt er los, bremst und kommt schneller zurück. Wer zu früh ausweicht, läuft hinein. Trifft sein Ankerwurf, zieht er dich heran und stampft sofort. Beim Schottfall krachen sieben Schotts als Welle durch die Arena, mit genau einer Lücke.
- **Reaktorkern:**
  - Die Kernspirale dreht sich um ihn, wer mitläuft, bleibt in der Lücke.
  - Gitterstrahlen fallen in zwei versetzten Reihen.
  - In der letzten Phase droht die Kernschmelze. Der Notschalter kühlt den Kern und legt ihn lange frei, aber nur, wenn du ihn vorher nicht verbraucht hast. Sonst musst du genau im richtigen Moment ausweichen.
- **Brutmutter:** Sie legt Eier, die zu Milben schlüpfen, wenn du sie nicht schnell zerschlägst. Dazu kommen eine Tintenwolke, in der nur ihr Köder leuchtet, und ein Sog mit Säureregen.
- **Lotse:**
  - Torpedos verfolgen dich, betäuben ihn aber, wenn du sie in ihn lenkst.
  - Beim Kreuzfeuer von beiden Wänden kommt erst ein Schuss auf Kopfhöhe, dann einer in Sprunghöhe.
  - Das Sperrfeuer fällt im Schachbrettmuster.
  - Ein Enterkommando kommt dazu.

Ab dem zweiten Zyklus sind die Wächter **entfesselt**: Nietenringe mit Druckwellen auf zwei Höhen, Prismenspiralen, Leuchtsporen und Fischzüge auf drei Höhen.

**Die Prismenkaiserin.** Ab dem zweiten Zyklus wartet auf der Brücke statt des Lotsen die Prismenkaiserin, ein Bullet-Hell-Kampf aus Licht:
- Prismenbolzen hängen in der Luft und schnellen dann auf deine Position zu.
- Lichtlanzen kommen in Reihen mit einer Lücke. In der ersten Phase liegt die Lücke am Boden, später musst du springen oder auf die Stege.
- Beim Lanzenregen fällt jede Salve versetzt.
- Der Ewige Regenbogen ist eine Spirale aus Geschossen.
- Bei Lichtstürzen jagt sie quer durch den Saal.
- Beim Sonnentanz drehen sich Strahlen um sie. Unter ihr sind die Lücken am langsamsten.

Nach jedem Muster sinkt sie erschöpft herab, das ist dein Fenster zum Zuschlagen. Bei jedem Phasenwechsel entrückt sie kurz und lässt einen Sternenbruch los. Nach vier Minuten Kampf rast sie.

**Laufbahn.** Abschüsse über 500 pro Tauchgang zählen mit abnehmendem Ertrag. Ein riesiger Endgame-Lauf bringt viele Ränge, füllt aber nicht alle Bäume auf einmal.

**Ehrlich:** Wie schwer das für Menschen ist, ist nicht getestet. Der Testspieler reagiert perfekt, hat aber keine Laufbahnboni. Den ersten Zyklus gewinnt er mit allen Klassen (40 von 40 Läufen). In zwei Endgame-Läufen besiegte er die Prismenkaiserin im zweiten Zyklus. Einer fiel im dritten Zyklus an ihr, der andere spielte mit einem ausgereizten Build alle fünf Zyklen durch. Weil dort die Wächter in wenigen Sekunden fielen, sind sie ab Eskalation 40 jetzt zusätzlich zäher. Bei über 1000 Gegnern braucht ein Bild im Mittel knapp 4 ms zum Zeichnen und 1 ms Simulation.

## Neu in Version 1.3 · Laufbahn

**Dauerhaft stärker werden.** Jeder Tauchgang gibt Laufbahn-Erfahrung, auch eine Niederlage. Es gibt einen Laufbahnrang und einen Rang je Klasse. Jeder Rang bringt einen Skill-Punkt, jeder Laufbahnrang zusätzlich drei Datenkerne. Spätere Zyklen und höhere Druckstufen geben deutlich mehr Erfahrung.

**Acht Skill-Bäume.** Im Hauptmenü unter **Laufbahn**:
- **Tiefenbaum** (immer offen): Kraft, Überladung und Rumpf. Endknoten: ein zusätzliches Geschoss.
- **Arsenal** (nach 5 Knoten im Tiefenbaum): Werkstattstufe zu Beginn, seltene Startmodule, Kreiselmesser, Doppellauf.
- **Abgrund** (nach dem ersten Sieg): mehr Datenkerne, grössere Schwärme für mehr Erfahrung, eine zusätzliche Karte.
- **Fünf Klassenbäume**: Sie wirken nur mit ihrer Klasse. Die Endknoten der Mechanikerin, der Schweisserin und des Koloss schalten neue Waffen frei.

**Drei neue Waffen, nur über die Laufbahn:**
- Tiefenbohrer: fünf rasende Stösse.
- Plasmawerfer: explodierende Plasmakugeln.
- Tiefseesense: weite Schwünge, der dritte mäht rundherum.

**Waffenmeisterschaft.** Jede Waffe steigt durch Abschüsse bis Stufe 10 auf. Das bringt +4 % Schaden je Stufe und ab Stufe 5 und 10 mehr Kritik. Den Stand zeigt der Reiter „WAFFEN“ in der Laufbahn.

## Neu in Version 1.2 · Schwarm-Update

**Schwärme.** Neben den bekannten Gegnern strömen jetzt Rostmilben (Heck), Nanodrohnen (Maschinen- und Kommandodeck) und Glimmfische (Forschungsdeck) in Pulks aus Lüftungen und Schotts. Oben in der Mitte zählt „SCHWARM“ mit, wie viele noch kommen. Schon der erste Raum hat fünf Gegner, im Endgame sind es mehrere hundert gleichzeitig. Schwarmbisse unterbrechen deine Angriffe nicht.

**Überladung und Levelaufstiege.** Jeder Abschuss lässt einen violetten Energiesplitter fallen. Die violette Leiste unter der Energie füllt sich; ist sie voll, steht die Zeit still und du wählst eine von drei Karten (Ziffern 1–3). Ein normaler Tauchgang bringt etwa 16–20 Aufstiege, spätere Zyklen viel mehr.

**Viel stärkere Upgrades.** Module stapeln bis zu 8-fach (selten 5-fach) und multiplikativ. Neue Horden-Werkzeuge:
- Klingenwelle: jeder Schlag schleudert eine Druckklinge.
- Kreiselmesser: Klingen kreisen um dich.
- Teslafeld: blitzt regelmässig mehrere Gegner an.
- Mehrfachlader: zusätzliche Harpunen, Klingen, Drohnenschüsse und Torpedos.
- Druckwellenkern: eine Nova nach einigen Abschüssen.
- Blutrausch: Abschüsse beschleunigen deine Angriffe.
- Druckkammer: grössere Schläge und Explosionen.

Kettenreaktion und Teslaspule springen jetzt durch ganze Schwärme. Werkstattstufen gehen bis 8. Ist dein Build ausgereizt, bietet jeder Aufstieg den unbegrenzt stapelbaren Überladungskern an.

**Tiefenbaum.** Im Archiv unter „Tiefenbaum“ kaufst du mit Datenkernen dauerhafte Stufen: Schneidwerk (Schaden), Taktgeber (Tempo), Druckverstärker (Fläche), Datenlink (Überladung), Magnetspule, Zielsystem und „Erweiterte Auswahl“ für eine vierte Karte.

**Härter mit der Tiefe.** Gegner werden mit jeder Raumtiefe und jedem Zyklus exponentiell zäher. Der Testspieler gewinnt den ersten Zyklus immer; auf Druckstufe 5 stirbt er im zweiten Zyklus. Wie schwer es für Menschen ist, ist noch nicht getestet.

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
