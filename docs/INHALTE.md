# Spielinhalte · Version 1.0

Automatisch aus dem Quellcode erzeugt (`./gradlew contentCatalog`).

## Klassen

| Klasse | Integrität | Tempo | Waffe | Modul | Kerne | Eigenheit |
|---|---|---|---|---|---|---|
| Die Mechanikerin | 100 | 100 % | Rohrzange | Schildimpuls | 0 | Ausgewogen. Werkstätten sind für sie 25 % günstiger, sie trägt ein Reparaturset mehr. |
| Der Harpunier | 85 | 105 % | Harpunenwerfer | Sonarpuls | 12 | Kämpft auf Distanz. +10 % kritische Trefferchance, aber weniger Integrität. |
| Die Schweisserin | 95 | 100 % | Schweisslanze | Druckschild | 16 | Setzt alles in Brand. +20 % Brandchance, Brand wirkt doppelt so stark. |
| Der Koloss | 150 | 92 % | Ankerhammer | Minitorpedo | 22 | Schwerer Panzeranzug: 150 Integrität und 20 % Schadensreduktion, aber langsamer. |
| Die Funkerin | 90 | 108 % | Tesla-Handschuh | Wartungsdrohne | 28 | Energiequelle auf zwei Beinen: +40 Energie, doppelte Energierückgewinnung. |

## Waffen

| Waffe | Kombination | Luftangriff | Beschreibung |
|---|---|---|---|
| Rohrzange | 20 → 20 → 31 Schaden | 19 | Ausgewogene Dreierkombination. Der dritte Schlag schleudert Gegner zurück. |
| Bergungsmesser | 10 → 10 → 11 → 18 Schaden | 11 | Vier blitzschnelle Schnitte. Deutlich höhere kritische Trefferchance. |
| Schweisslanze | 16 → 16 → 26 Schaden | 16 | Lange Plasmastösse. Treffer entzünden Gegner häufig. |
| Ankerhammer | 44 → 60 Schaden | 45 | Langsam und verheerend. Der zweite Schlag erzeugt Bodenwellen, in der Luft stampfst du auf. |
| Harpunenwerfer | 24 → 24 → 34 Schaden | 20 | Durchschlagende Harpunen auf Distanz. Jeder dritte Schuss trifft mehrere Gegner. |
| Tesla-Handschuh | 12 → 12 → 18 Schaden | 12 | Schnelle Schläge. Jeder Treffer springt als Blitz auf einen weiteren Gegner über. |
| Enterhaken | 15 → 15 → 22 Schaden | 14 | Weite Hakenstösse. Der dritte Stoss zieht getroffene Gegner zu dir heran. |
| Tiefenbohrer | 8 → 8 → 8 → 9 → 16 Schaden | 8 | Fünf rasende Bohrstösse mit wenig Rückstoss. Zerlegt dichte Schwärme im Sekundentakt. |
| Plasmawerfer | 20 → 20 → 34 Schaden | 18 | Plasmakugeln, die beim Aufprall explodieren. Der dritte Schuss ist eine grosse Ladung. |
| Tiefseesense | 24 → 24 → 36 Schaden | 22 | Weite Sensenschwünge. Der dritte Schwung mäht rundherum, auch hinter dir. |

## Aktive Module

| Modul | Energie | Abklingzeit | Kerne | Wirkung |
|---|---|---|---|---|
| Schildimpuls | 35 | 6.5 s | 0 | Ein Druckimpuls trifft, betäubt und stösst Gegner in deiner Nähe weg. |
| Lichtbogen | 30 | 4.5 s | 6 | Ein elektrisches Geschoss durchschlägt alle Gegner in einer Linie. |
| Druckschild | 40 | 9.0 s | 8 | Vier Sekunden Schutz: 75 % weniger Schaden und Rückstoss für Angreifer. |
| Minitorpedo | 45 | 7.0 s | 10 | Ein suchender Torpedo explodiert in einem grossen Radius. |
| Sonarpuls | 30 | 10.0 s | 6 | Markiert alle Gegner: Sie erleiden sechs Sekunden lang 50 % mehr Schaden. |
| Kryogranate | 40 | 8.0 s | 8 | Eine Granate friert Gegner im Umkreis für zwei Sekunden ein. |
| Wartungsdrohne | 50 | 16.0 s | 14 | Eine Begleitdrohne feuert zwölf Sekunden lang auf nahe Gegner. |
| Überlastung | 45 | 14.0 s | 10 | Sechs Sekunden: +40 % Angriffs- und Lauftempo, Treffer laden Energie. |

## Passive Module

| Modul | Seltenheit | Stufen | Anfangs verfügbar | Wirkung |
|---|---|---|---|---|
| Servoverstärker | Standard | 8 | ja | +15 % Werkzeugschaden |
| Verbundpanzerung | Standard | 8 | ja | −10 % erlittener Schaden |
| Kondensator | Standard | 8 | ja | +20 Energie +15 % Modulschaden |
| Notfallreserve | Standard | 8 | ja | +20 max. Integrität heilt sofort 30 |
| Kühlkreislauf | Standard | 8 | ja | −12 % Abklingzeit für Ausweichen und Modul |
| Rückgewinnung | Standard | 8 | ja | +3 Integrität pro Abschuss |
| Teleskopstange | Standard | 8 | ja | +15 % Reichweite |
| Übertakter | Standard | 8 | ja | +10 % Angriffstempo |
| Strömungsantrieb | Standard | 8 | ja | +10 % Lauf- und Ausweichtempo |
| Energiesiphon | Standard | 8 | ja | +4 Energie pro Abschuss +1 Energie pro Sekunde |
| Reparaturschwarm | Standard | 8 | ja | +8 Integrität nach jedem Raum |
| Schrottmagnet | Standard | 8 | ja | +30 % Schrott grösserer Sammelradius |
| Glasfaserlinse | Standard | 8 | ja | +7 % kritische Trefferchance |
| Ballastgurt | Standard | 8 | ja | +35 % Rückstoss |
| Zündkerze | Standard | 8 | ja | +15 % Brandchance |
| Kälteschlange | Standard | 8 | ja | +15 % Kältechance verlangsamt Gegner |
| Werkzeuggürtel | Standard | 4 | ja | +1 Reparaturset +1 Kapazität |
| Dornenpanzer | Standard | 8 | ja | Nahkampfangreifer erleiden 14 Schaden |
| Druckkammer | Standard | 8 | ja | +12 % Wirkungsbereich von Schlägen und Explosionen |
| Ladungsverstärker | Standard | 8 | ja | +15 % Überladung aus Energiesplittern |
| Blutrausch | Standard | 8 | ja | Abschüsse beschleunigen Angriffe kurzzeitig |
| Teslaspule | Selten | 6 | ja | Dritter Treffer: Kettenblitz 15 Schaden, +1 Sprung je Stufe |
| Druckluftdüse | Selten | 5 | ja | +1 Sprung in der Luft |
| Adrenalinpumpe | Selten | 5 | ja | +30 % Schaden unter 35 % Integrität |
| Hinterhalt-Protokoll | Selten | 5 | ja | +35 % Schaden gegen abgewandte Gegner |
| Nachbrenner | Selten | 5 | ja | Nach dem Ausweichen: nächster Treffer +60 % |
| Klingenrumpf | Selten | 5 | ja | Ausweichen verursacht 18 Schaden |
| Überdruckventil | Selten | 5 | ja | Bei Treffer: Druckwelle 25 Schaden |
| Kettenreaktion | Selten | 5 | Archiv, 7 Kerne | Besiegte Gegner explodieren (20 Schaden je Stufe) |
| Nanitenkultur | Selten | 5 | ja | Lebensraub: 4 % des Schadens |
| Barrierenfeld | Selten | 5 | ja | Blockt den ersten Treffer in jedem Raum |
| Lumineszenz | Selten | 4 | Archiv, 7 Kerne | +20 % Schaden an Gegnern im Lichtkegel |
| Tiefenrausch | Selten | 5 | Archiv, 7 Kerne | Alle 10 Abschüsse: +3 % Schaden (Run) |
| Schildzelle | Selten | 5 | ja | +20 Schild, lädt ausserhalb von Treffern |
| Zielsucher | Selten | 3 | Archiv, 7 Kerne | Geschosse lenken nach +20 % Geschossschaden |
| Hohlspitzen | Selten | 5 | ja | +40 % kritischer Schaden |
| Klingenwelle | Selten | 5 | ja | Schläge schleudern eine Druckklinge (40 % Schaden) |
| Kreiselmesser | Selten | 6 | ja | +1 kreisende Klinge (12 Schaden je Treffer) |
| Teslafeld | Selten | 5 | ja | Blitzt alle 1,2 s auf 2 Gegner (+1 je Stufe) |
| Mehrfachlader | Selten | 4 | ja | +1 Geschoss für Harpune, Klingen, Drohne, Torpedo |
| Messingkompass | Legendär | 2 | Archiv, 12 Kerne | +1 Auswahl bei jeder Bergung |
| Notfallkapsel | Legendär | 1 | ja | Einmal wiederbeleben mit 50 % Integrität |
| Phasenkern | Legendär | 3 | Archiv, 12 Kerne | Ausweichen hinterlässt ein explodierendes Nachbild |
| Singularitätszelle | Legendär | 2 | Archiv, 12 Kerne | −50 % Modul-Abklingzeit +25 % Modulschaden |
| Leviathanzahn | Legendär | 3 | Archiv, 12 Kerne | Kritische Treffer: Kettenblitz und +2 Integrität |
| Überladungskern | Legendär | 999 | bei ausgereiztem Build | +6 % Schaden, +6 % Tempo +6 Integrität (unbegrenzt) |
| Druckwellenkern | Legendär | 3 | Archiv, 12 Kerne | Alle 12 Abschüsse: Nova (weniger je Stufe) |
| Gläserner Rumpf | Verflucht | 1 | nur Kapelle | +40 % Schaden −30 % max. Integrität |
| Gier der Tiefe | Verflucht | 1 | nur Kapelle | +60 % Schrott Gegner +15 % Schaden |
| Druckfieber | Verflucht | 1 | nur Kapelle | +25 % Angriffstempo −1 Energie pro Sekunde |
| Grenzbrecher | Legendär | 99 | bei ausgereiztem Build | +2 Höchststufe für alle Standard- und Seltenmodule |

## Entfesselungen

Ist ein Modul auf seiner Grundhöchststufe und der Partner mindestens einmal installiert, liegt die Entfesselung beim nächsten Levelaufstieg obenauf (auch in Elite- und Bossbergungen).

| Entfesselung | Grundmodul (voll) | Partner | Wirkung |
|---|---|---|---|
| Klingensturm | Kreiselmesser (6) | Druckkammer | Zweiter Rotorring, dreifacher Schaden, schleudert Klingen |
| Gewitterkern | Teslafeld (5) | Teslaspule | Blitze alle 0,4 s in 8 Gegner, jeder springt dreimal weiter |
| Supernova | Kettenreaktion (5) | Druckwellenkern | Abschussexplosionen doppelt, Nova alle 5 Abschüsse |
| Klingenorkan | Klingenwelle (5) | Mehrfachlader | Jeder Schlag: Fächer aus fünf durchschlagenden Klingen |
| Raketenschwarm | Zielsucher (3) | Kondensator | Alle 1,2 s: sechs zielsuchende Minitorpedos |
| Todesblick | Glasfaserlinse (8) | Hohlspitzen | +25 % Kritik, Überkritik ×1,5 kritische Treffer explodieren |
| Bollwerk | Schildzelle (5) | Verbundpanzerung | +120 Schild, schnelle Ladung Bruch löst eine Druckwelle aus |
| Höllenglut | Zündkerze (8) | Übertakter | +40 % Brand, Brand ×3 Brennende stecken Nachbarn an |
| Nullpunkt | Kälteschlange (8) | Kühlkreislauf | Alle 3 s: Frostwelle Gefrorene erleiden doppelten Schaden |
| Blutsauger | Nanitenkultur (5) | Rückgewinnung | +8 % Lebensraub (bis 40 %) Abschüsse heilen 1 % |
| Phasensturm | Phasenkern (3) | Klingenrumpf | Ausweichen doppelt so oft, Spur aus Explosionen, Abschüsse laden |

## Gegner

| Gegner | Integrität | ab Sektion | Bedrohung | Schrott |
|---|---|---|---|---|
| Schrottläufer | 44 | 1 | 1 | 2 |
| Wachdrohne | 34 | 1 | 1 | 2 |
| Schottwächter | 95 | 1 | 3 | 5 |
| Kugelbombe | 26 | 1 | 1 | 2 |
| Schweissroboter | 75 | 2 | 2 | 4 |
| Geschützturm | 60 | 2 | 2 | 3 |
| Minenleger | 80 | 2 | 2 | 4 |
| Leuchtqualle | 42 | 3 | 2 | 3 |
| Tiefseeaal | 58 | 3 | 2 | 3 |
| Schildträger | 110 | 3 | 3 | 6 |
| Sicherheitsautomat | 120 | 4 | 3 | 6 |
| Suchlichtsonde | 50 | 4 | 2 | 3 |
| Schmugglerdrohne | 70 | 1 | 0 | 26 |
| Rostmilbe | 10 | 1 | 0 | 0 |
| Glimmfisch | 8 | 3 | 0 | 0 |
| Nanodrohne | 12 | 2 | 0 | 0 |
| Säurespucker | 14 | 2 | 0 | 0 |
| Zündmilbe | 9 | 2 | 0 | 0 |
| Prismaqualle | 22 | 4 | 0 | 0 |
| Speerfisch | 16 | 3 | 0 | 0 |
| Panzerkrabbe | 30 | 3 | 0 | 0 |
| Brutnest | 170 | 2 | 2 | 3 |
| Brutei | 28 | 3 | 0 | 0 |
| Der Schottmeister (Boss) | 600 | 1 | 0 | 35 |
| Der Reaktorkern (Boss) | 720 | 2 | 0 | 40 |
| Die Brutmutter (Boss) | 820 | 3 | 0 | 45 |
| Der Lotse (Boss) | 1100 | 4 | 0 | 60 |
| Die Prismenkaiserin (Boss) | 450 | 4 | 0 | 80 |

## Elite-Eigenschaften

- **Gepanzert:** Erleidet 40 % weniger Schaden.
- **Instabil:** Explodiert beim Zerstören.
- **Flink:** Bewegt sich und greift deutlich schneller an.
- **Selbstreparatur:** Stellt ohne Treffer Integrität wieder her.
- **Schildgenerator:** Ein Schild fängt Schaden ab und lädt sich wieder auf.

## Druckkapelle

- **Integrität opfern:** −25 % max. Integrität dauerhaft → Ein legendäres Modul
- **Gläserner Rumpf:** Verfluchtes Modul: −30 % Integrität → +40 % Schaden
- **Gier der Tiefe:** Verfluchtes Modul: Gegner +15 % Schaden → +60 % Schrott, 40 Schrott sofort
- **Druckfieber:** Verfluchtes Modul: −1 Energie/s → +25 % Angriffstempo
- **Schrott opfern:** −40 Schrott → Volle Integrität und ein Reparaturset
- **Vorräte opfern:** Alle Reparatursets → Zwei seltene Module

## Raumthemen

- Mannschaftsquartier (Sektion 1)
- Frachtraum (Sektion 1)
- Verlassene Messe (Sektion 1)
- Torpedomagazin (Sektion 1)
- Arrestzellen (Sektion 1)
- Das verriegelte Schott (Sektion 1)
- Kombüse (Sektion 1)
- Wäscherei (Sektion 1)
- Turbinenhalle (Sektion 2)
- Kühlkreislauf (Sektion 2)
- Ballastkammer (Sektion 2)
- Pumpenraum (Sektion 2)
- Reaktorkammer (Sektion 2)
- Kesselraum (Sektion 2)
- Generatorraum (Sektion 2)
- Forschungslabor (Sektion 3)
- Sauerstoffgarten (Sektion 3)
- Krankenstation (Sektion 3)
- Probenbecken (Sektion 3)
- Die Brutkammer (Sektion 3)
- Datenarchiv (Sektion 3)
- Kryolabor (Sektion 3)
- Sicherheitszentrale (Sektion 4)
- Beobachtungsdeck (Sektion 4)
- Kommandohalle (Sektion 4)
- Signalzentrale (Sektion 4)
- Die Brücke (Sektion 4)
- Waffenkammer (Sektion 4)
- Kartenraum (Sektion 4)
- Werkstatt (sektionsübergreifend)
- Schwarzmarkt (sektionsübergreifend)
- Die Druckkapelle (sektionsübergreifend)
- Vergessenes Depot (sektionsübergreifend)

## Resonanzen

| Resonanz | Module | Bonus |
|---|---|---|
| Feuersturm | Zündkerze + Übertakter | Brand wirkt 50 % stärker. |
| Permafrost | Kälteschlange + Teslaspule | +15 % Kältechance. |
| Scharfschütze | Glasfaserlinse + Hohlspitzen | +8 % kritische Trefferchance. |
| Festung | Verbundpanzerung + Schildzelle | +20 Schild. |
| Blutkreislauf | Nanitenkultur + Rückgewinnung | +3 % Lebensraub. |
| Energiekreislauf | Kondensator + Energiesiphon | +20 Energie und +2 Energie pro Sekunde. |
| Sturmläufer | Strömungsantrieb + Kühlkreislauf | −10 % Abklingzeiten, +5 % Tempo. |
| Schrotthändler | Schrottmagnet + Werkzeuggürtel | +20 % Schrott und +1 Reparaturset-Kapazität. |
| Schwergewicht | Ballastgurt + Servoverstärker | +10 % Schaden. |

## Bedrohungen

Prüfungen für den Build: im ersten Zyklus ab dem Maschinendeck selten, im Endgame fast in jedem Kampfraum. Bestanden gibt es eine seltene Bergung und einen Datenkern.

| Bedrohung | Wirkung | Hilft |
|---|---|---|
| Panzerschwarm | Alle Gegner tragen Panzer. Normale Treffer wirken nur zu 35 %. | Kritische Treffer, Brand, Explosionen, Blitze |
| Flutwelle | Dreimal so viele Schwarmgegner mit halber Integrität. | Flächenschaden, Kreiselmesser, Teslafeld, Kettenreaktion |
| Kolosse | Wenige Elitegegner, riesig und vierfach so zäh. | Einzelschaden, Kritik, Werkstattstufe |
| Luftschlag | Nur fliegende Gegner, hoch über dem Boden. | Geschosse, Zielsucher, Teslafeld, Sprünge |
| Brutnester | Nester speien Milben. Fällt ein Nest, stirbt seine Brut. | Hoher Einzelschaden, schnelles Vorrücken |
| Sperrfeuer | Spucker und Prismaquallen füllen den Raum mit Geschossen, dazu Einschläge. | Ausweichen, Schild, Barriere, Tempo |
| Regeneration | Gegner heilen sich rasch, solange sie weder brennen noch frieren. | Brand, Kälte, Schadensspitzen |

## Eskalation

Ab Raum 20 des ersten Zyklus steigt die Eskalation um 1 je Raum, über alle Zyklen hinweg. Schwarmgrösse ×(1 + 0,22·E + 0,007·E²), bis zu 1600 gleichzeitig lebende Gegner, Räume bis drei Bildschirme breiter, bis zu vier Wellen, alle Schwarmarten gemischt, mehr Elitegegner.

| Raum | Eskalation | Schwarmfaktor |
|---|---|---|
| Zyklus 1, Raum 20 | 1 | ×1.2 |
| Zyklus 1, Raum 24 | 5 | ×2.3 |
| Zyklus 2, Raum 12 | 17 | ×6.8 |
| Zyklus 2, Raum 24 | 29 | ×13.3 |
| Zyklus 3, Raum 12 | 41 | ×21.8 |
| Zyklus 3, Raum 24 | 53 | ×32.3 |

## Raumzustände

- **Stromausfall:** Nur Stirnlampe und Gegneraugen leuchten. +60 % Schrott.
- **Alarmstufe Rot:** Ein zusätzlicher Gegner je Welle. Seltene Bergung.
- **Druckleck:** Zwei zusätzliche Dampfaustritte. Zwei weitere Vorratskisten.
- **Hüllenbruch:** Halte 40 Sekunden durch, bis das Leck dicht ist. Seltene Bergung.
- **Schlagseite:** Das Boot krängt: alles rutscht, Trümmer fallen. +40 % Schrott.

## Logbuch

| Eintrag | Bedingung | Kerne |
|---|---|---|
| Erster Schritt | Sichere den ersten Raum. | 2 |
| Schottbrecher | Besiege den Schottmeister. | 4 |
| Kernschmelze | Besiege den Reaktorkern. | 5 |
| Brutkasten | Besiege die Brutmutter. | 6 |
| Kommando übernommen | Erobere die Brücke. | 8 |
| Tiefer als tief | Erreiche den zweiten Zyklus. | 6 |
| Unter Druck | Erobere die Brücke auf Druckstufe 3 oder höher. | 10 |
| Sammler | Entdecke 20 verschiedene Module. | 5 |
| Vollständiges Kompendium | Entdecke alle regulären Module. | 12 |
| Legendenschmiede | Trage drei legendäre Module in einem Tauchgang. | 6 |
| Fluch als Werkzeug | Erobere die Brücke mit einem verfluchten Modul. | 8 |
| Jäger der Tiefe | Besiege insgesamt 500 Gegner. | 6 |
| Schrottkönig | Besitze 250 Schrott gleichzeitig. | 4 |
| Im Einklang | Aktiviere drei Resonanzen in einem Tauchgang. | 5 |
| Dicht gehalten | Überstehe einen Hüllenbruch. | 4 |
| Seefest | Sichere einen Raum mit Schlagseite. | 3 |
| Schmugglerjagd | Schiess eine Schmugglerdrohne ab, bevor sie entkommt. | 4 |
| Im Dunkeln | Sichere einen Raum im Stromausfall. | 3 |
| Rote Welle | Sichere einen Raum unter Alarmstufe Rot. | 3 |
| Nasse Füsse | Sichere einen Raum mit Druckleck. | 3 |
| Die Mechanikerin am Steuer | Erobere die Brücke als Mechanikerin. | 4 |
| Der Harpunier am Steuer | Erobere die Brücke als Harpunier. | 4 |
| Die Schweisserin am Steuer | Erobere die Brücke als Schweisserin. | 4 |
| Der Koloss am Steuer | Erobere die Brücke als Koloss. | 4 |
| Die Funkerin am Steuer | Erobere die Brücke als Funkerin. | 4 |
| Die ganze Crew | Erobere die Brücke mit jeder Klasse. | 15 |
| Der Abgrund erwacht | Erreiche Raum 20, ab dem jeder Raum eskaliert. | 4 |
| Prüfling | Sichere einen Raum mit einer Bedrohung. | 4 |
| Entfesselt | Entfessle ein ausgereiztes Modul mit seinem Partner. | 6 |
| Überkritisch | Erreiche mehr als 200 % kritische Chance. | 6 |
| Tausend Augen | Sichere einen Raum ab Eskalation 30. | 10 |
| Prismenbrecher | Besiege die Prismenkaiserin. | 15 |

## Laufbahn: Skill-Bäume

Jeder Laufbahnrang gibt einen Punkt für Tiefe, Arsenal und Abgrund, jeder Klassenrang einen Punkt für den Baum dieser Klasse.

### Tiefenbaum

*Öffnet sich: Immer offen.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Schneidwerk I | +8 % Werkzeugschaden | 1 | – |
| Schneidwerk II | +8 % Werkzeugschaden | 1 | Schneidwerk I |
| Taktgeber | +8 % Angriffstempo | 2 | Schneidwerk II |
| Zielsystem | +5 % kritische Trefferchance | 2 | Taktgeber |
| Schneidwerk III | +12 % Werkzeugschaden | 3 | Zielsystem |
| Datenlink I | +12 % Überladung | 1 | – |
| Magnetspule | +30 % Sammelradius | 1 | Datenlink I |
| Datenlink II | +15 % Überladung | 2 | Magnetspule |
| Erweiterte Auswahl | +1 Karte bei jedem Levelaufstieg | 3 | Datenlink II |
| Frühstart | 2 Levelaufstiege direkt zu Beginn | 3 | Erweiterte Auswahl |
| Verstärkter Anzug | +15 maximale Integrität | 1 | – |
| Druckplatten | −6 % erlittener Schaden | 1 | Verstärkter Anzug |
| Druckfeld | +10 % Wirkungsbereich | 2 | Druckplatten |
| Titanrippen | +25 maximale Integrität | 2 | Druckfeld |
| Tiefenpanzer | −10 % erlittener Schaden | 3 | Titanrippen |
| Abgrundblick | +1 Geschoss für alle Schusswerkzeuge | 4 | Schneidwerk III, Frühstart, Tiefenpanzer |

### Arsenal

*Öffnet sich: 5 Knoten im Tiefenbaum freischalten.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Schmiede I | Waffe beginnt auf Werkstattstufe 1 | 1 | – |
| Schmiede II | +1 Werkstattstufe zu Beginn | 2 | Schmiede I |
| Schmiede III | +1 Werkstattstufe zu Beginn | 3 | Schmiede II |
| Waffenkenner | +50 % Waffenmeisterschaft | 1 | – |
| Hohlschliff | +40 % kritischer Schaden | 2 | Waffenkenner |
| Beutekiste | 1 seltenes Modul zu Beginn | 3 | Hohlschliff |
| Kreiselwerk | +1 Kreiselmesser dauerhaft | 2 | – |
| Sprengkunst | +12 % Wirkungsbereich | 2 | Kreiselwerk |
| Doppellauf | +1 Geschoss für alle Schusswerkzeuge | 4 | Sprengkunst |
| Volles Arsenal | +1 seltenes Modul zu Beginn | 5 | Schmiede III, Beutekiste, Doppellauf |

### Abgrund

*Öffnet sich: Einmal die Brücke erobern.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Kernsammler I | +25 % Datenkerne nach jedem Tauchgang | 2 | – |
| Kernsammler II | +25 % Datenkerne | 3 | Kernsammler I |
| Kernsammler III | +50 % Datenkerne | 4 | Kernsammler II |
| Schwarmköder | +25 % grössere Schwärme | 2 | – |
| Schwarmgier | +25 % Überladung | 3 | Schwarmköder |
| Tiefenrausch | +12 % Angriffstempo | 3 | Schwarmgier |
| Überdruck | +15 % Werkzeugschaden | 3 | – |
| Endlose Wahl | +1 Karte bei jedem Levelaufstieg | 4 | Überdruck |
| Abgrundrotor | +1 Kreiselmesser dauerhaft | 4 | Endlose Wahl |
| Leviathan | +1 Geschoss für alle Schusswerkzeuge | 6 | Kernsammler III, Tiefenrausch, Abgrundrotor |

### Mechanikerin

*Öffnet sich: Die Mechanikerin im Archiv freischalten.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Werkzeugsatz | +1 Reparaturset | 1 | – |
| Servorotor | +1 Kreiselmesser dauerhaft | 2 | Werkzeugsatz |
| Meisterzange | +10 % Werkzeugschaden | 3 | Servorotor |
| Drehmoment | +10 % Werkzeugschaden | 1 | – |
| Takt der Maschine | +8 % Angriffstempo | 2 | Drehmoment |
| Werkanzug | +20 maximale Integrität | 1 | – |
| Notreparatur | −8 % erlittener Schaden | 2 | Werkanzug |
| Tiefenbohrer | Neue Waffe: Fünf rasende Bohrstösse mit wenig Rückstoss. Zerlegt dichte Schwärme im Sekundentakt. | 4 | Meisterzange, Takt der Maschine, Notreparatur |

### Harpunier

*Öffnet sich: Der Harpunier im Archiv freischalten.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Ruhige Hand | +5 % kritische Trefferchance | 1 | – |
| Widerhaken | +30 % kritischer Schaden | 2 | Ruhige Hand |
| Doppelschuss | +1 Geschoss für alle Schusswerkzeuge | 3 | Widerhaken |
| Schnelles Nachladen | +10 % Angriffstempo | 1 | – |
| Weitsicht | +30 % Sammelradius | 2 | Schnelles Nachladen |
| Flossen | +8 % Lauftempo | 1 | – |
| Hakenschlag | −10 % Abklingzeiten | 2 | Flossen |
| Sturmharpune | +1 Geschoss für alle Schusswerkzeuge | 4 | Doppelschuss, Weitsicht, Hakenschlag |

### Schweisserin

*Öffnet sich: Die Schweisserin im Archiv freischalten.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Brennstoff | +10 % Brandchance | 1 | – |
| Weissglut | +15 % Modul- und Explosionsschaden | 2 | Brennstoff |
| Flammenkegel | +10 % Wirkungsbereich | 3 | Weissglut |
| Schutzmaske | −6 % erlittener Schaden | 1 | – |
| Ruhige Naht | +10 % Werkzeugschaden | 2 | Schutzmaske |
| Hitzekern | +20 maximale Integrität | 1 | – |
| Aufladung | +30 maximale Energie | 2 | Hitzekern |
| Plasmawerfer | Neue Waffe: Plasmakugeln, die beim Aufprall explodieren. Der dritte Schuss ist eine grosse Ladung. | 4 | Flammenkegel, Ruhige Naht, Aufladung |

### Koloss

*Öffnet sich: Der Koloss im Archiv freischalten.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Dicke Platten | +30 maximale Integrität | 1 | – |
| Standfest | −8 % erlittener Schaden | 2 | Dicke Platten |
| Wucht | +12 % Werkzeugschaden | 3 | Standfest |
| Stampfer | +12 % Wirkungsbereich | 1 | – |
| Marschtritt | +6 % Lauftempo | 2 | Stampfer |
| Eisenwille | +1 Reparaturset | 1 | – |
| Zorn | +8 % Angriffstempo | 2 | Eisenwille |
| Tiefseesense | Neue Waffe: Weite Sensenschwünge. Der dritte Schwung mäht rundherum, auch hinter dir. | 4 | Wucht, Marschtritt, Zorn |

### Funkerin

*Öffnet sich: Die Funkerin im Archiv freischalten.*

| Knoten | Wirkung | Punkte | Voraussetzung |
|---|---|---|---|
| Zusatzzellen | +30 maximale Energie | 1 | – |
| Energiefluss | −10 % Abklingzeiten | 2 | Zusatzzellen |
| Fokus | +20 % Modul- und Explosionsschaden | 3 | Energiefluss |
| Statik | +4 % kritische Trefferchance | 1 | – |
| Datenstrom | +15 % Überladung | 2 | Statik |
| Relais | +1 Karte bei jedem Levelaufstieg | 1 | – |
| Feldschild | −6 % erlittener Schaden | 2 | Relais |
| Funkenkrone | +2 Kreiselmesser dauerhaft | 4 | Fokus, Datenstrom, Feldschild |
