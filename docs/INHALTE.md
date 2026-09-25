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
| Servoverstärker | Standard | 3 | ja | +15 % Werkzeugschaden |
| Verbundpanzerung | Standard | 3 | ja | −10 % erlittener Schaden |
| Kondensator | Standard | 3 | ja | +20 Energie +15 % Modulschaden |
| Notfallreserve | Standard | 3 | ja | +20 max. Integrität heilt sofort 30 |
| Kühlkreislauf | Standard | 3 | ja | −12 % Abklingzeit für Ausweichen und Modul |
| Rückgewinnung | Standard | 3 | ja | +3 Integrität pro Abschuss |
| Teleskopstange | Standard | 3 | ja | +15 % Reichweite |
| Übertakter | Standard | 3 | ja | +10 % Angriffstempo |
| Strömungsantrieb | Standard | 3 | ja | +10 % Lauf- und Ausweichtempo |
| Energiesiphon | Standard | 3 | ja | +4 Energie pro Abschuss +1 Energie pro Sekunde |
| Reparaturschwarm | Standard | 3 | ja | +8 Integrität nach jedem Raum |
| Schrottmagnet | Standard | 3 | ja | +30 % Schrott grösserer Sammelradius |
| Glasfaserlinse | Standard | 3 | ja | +7 % kritische Trefferchance |
| Ballastgurt | Standard | 3 | ja | +35 % Rückstoss |
| Zündkerze | Standard | 3 | ja | +15 % Brandchance |
| Kälteschlange | Standard | 3 | ja | +15 % Kältechance verlangsamt Gegner |
| Werkzeuggürtel | Standard | 2 | ja | +1 Reparaturset +1 Kapazität |
| Dornenpanzer | Standard | 3 | ja | Nahkampfangreifer erleiden 14 Schaden |
| Teslaspule | Selten | 3 | ja | Dritter Treffer: Kettenblitz 15 Schaden |
| Druckluftdüse | Selten | 2 | ja | +1 Sprung in der Luft |
| Adrenalinpumpe | Selten | 2 | ja | +30 % Schaden unter 35 % Integrität |
| Hinterhalt-Protokoll | Selten | 2 | ja | +35 % Schaden gegen abgewandte Gegner |
| Nachbrenner | Selten | 2 | ja | Nach dem Ausweichen: nächster Treffer +60 % |
| Klingenrumpf | Selten | 2 | ja | Ausweichen verursacht 18 Schaden |
| Überdruckventil | Selten | 2 | ja | Bei Treffer: Druckwelle 25 Schaden |
| Kettenreaktion | Selten | 2 | Archiv, 7 Kerne | Besiegte Gegner explodieren (20 Schaden) |
| Nanitenkultur | Selten | 2 | ja | Lebensraub: 4 % des Schadens |
| Barrierenfeld | Selten | 2 | ja | Blockt den ersten Treffer in jedem Raum |
| Lumineszenz | Selten | 2 | Archiv, 7 Kerne | +20 % Schaden an Gegnern im Lichtkegel |
| Tiefenrausch | Selten | 2 | Archiv, 7 Kerne | Alle 10 Abschüsse: +3 % Schaden (Run) |
| Schildzelle | Selten | 2 | ja | +20 Schild, lädt ausserhalb von Treffern |
| Zielsucher | Selten | 1 | Archiv, 7 Kerne | Geschosse lenken nach +20 % Geschossschaden |
| Hohlspitzen | Selten | 2 | ja | +40 % kritischer Schaden |
| Messingkompass | Legendär | 1 | Archiv, 12 Kerne | +1 Auswahl bei jeder Bergung |
| Notfallkapsel | Legendär | 1 | ja | Einmal wiederbeleben mit 50 % Integrität |
| Phasenkern | Legendär | 1 | Archiv, 12 Kerne | Ausweichen hinterlässt ein explodierendes Nachbild |
| Singularitätszelle | Legendär | 1 | Archiv, 12 Kerne | −50 % Modul-Abklingzeit +25 % Modulschaden |
| Leviathanzahn | Legendär | 1 | Archiv, 12 Kerne | Kritische Treffer: Kettenblitz und +2 Integrität |
| Gläserner Rumpf | Verflucht | 1 | nur Kapelle | +40 % Schaden −30 % max. Integrität |
| Gier der Tiefe | Verflucht | 1 | nur Kapelle | +60 % Schrott Gegner +15 % Schaden |
| Druckfieber | Verflucht | 1 | nur Kapelle | +25 % Angriffstempo −1 Energie pro Sekunde |

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
| Der Schottmeister (Boss) | 600 | 1 | 0 | 35 |
| Der Reaktorkern (Boss) | 720 | 2 | 0 | 40 |
| Die Brutmutter (Boss) | 820 | 3 | 0 | 45 |
| Der Lotse (Boss) | 1100 | 4 | 0 | 60 |

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
