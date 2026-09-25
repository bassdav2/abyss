# Anforderungen und Use Cases · Version 1.1

Stand 25.09.2026. Aus Davids Spielidee und dem lokalen Kursbestand abgeleitet; ein prüfbarer Vorschlag für das Team, keine vom Dozenten abgenommene Spezifikation. Diagramm: `docs/diagrams/01-use-cases`.

## Produktziel und Akteur

Eine Person spielt lokal am Desktop ein Pixel-Art-Roguelite: vom Heck eines langen U-Boots durch 24 Räume bis zur Brücke. Jeder Raum ist eine überschaubare Kampf-, Handels- oder Risikoentscheidung. Eine Niederlage beendet den Tauchgang; Datenkerne, Freischaltungen und Entdeckungen bleiben. Nach einem Sieg sind weitere Zyklen und höhere Druckstufen möglich.

Primärer Akteur: Spieler/in. Nachbarsystem: lokales Dateisystem. Kein Konto, kein Netzwerk.

## Use-Case-Übersicht

| ID | Use Case | Priorität | Tiefe |
|---|---|---|---|
| UC-01 | Tauchgang vorbereiten und beginnen | Muss | Casual |
| UC-02 | Zum nächsten Raum vordringen | Muss, Kern | Fully dressed |
| UC-03 | Bergung wählen | Muss | Casual |
| UC-04 | Handeln: Schwarzmarkt, Werkstatt, Druckkapelle | Muss | Casual |
| UC-05 | Unterbrechen und fortsetzen | Muss | Casual |
| UC-06 | Brücke erobern / nächster Zyklus | Muss | Casual |
| UC-07 | Nach Niederlage erneut tauchen | Muss | Brief |
| UC-08 | Im Archiv freischalten | Soll | Brief |
| UC-09 | Aussehen anpassen | Soll | Brief |
| UC-10 | Ausrüstung und Bootskarte ansehen | Soll | Brief |
| UC-11 | Optionen und Steuerung | Muss | Brief |

## UC-02 · Zum nächsten Raum vordringen (fully dressed)

**Ziel:** Alle Wellen eines Raums bewältigen, optional die Belohnung nutzen und einen zulässigen nächsten Raum betreten.
**Umfang/Ebene:** ABYSS, Benutzerziel. **Auslöser:** Tauchgang gestartet oder fortgesetzt.
**Stakeholder:** Spielende erwarten lesbare Gefahren, reaktionsschnelle Steuerung, faire Belohnungen und sicheren Unterbruch. Das Team braucht nachvollziehbare, testbare Regeln.
**Vorbedingungen:** Gültiger Tauchgang aktiv, Figur lebt, Raum stammt aus einer gültigen Route.
**Erfolgsgarantie:** Der gewählte Raum ist aktiv; Build und Ressourcen sind übernommen; der neue Raumeingang ist gesichert, sofern das Speichern gelingt.
**Minimalgarantie:** Ein ungesicherter Raum kann nicht verlassen werden. Ungültige Wahl ändert nichts. Speicherfehler beenden das Spiel nicht und werden gemeldet.

**Hauptszenario**

1. Das System zeigt Raum, Figur, Laufstege, Gegner, HUD und bei Raumbeginn Name und Sektion.
2. Die Person bewegt sich, springt auf Laufstege, weicht aus und greift mit Waffe und Modul an.
3. Das System kündigt jeden Gegnerangriff mit einer Vorwarnung an und berechnet Treffer, Kritik, Zustände, Panzerung und Modul-Auslöser.
4. Nach der letzten Welle markiert das System den Raum als gesichert, öffnet das rechte Schott und zeigt die Bergungskapsel.
5. Die Person öffnet die Kapsel (E).
6. Das System zeigt drei bis vier Angebote (Module nach Seltenheit, gelegentlich eine Waffe).
7. Die Person wählt ein Angebot; das System installiert es genau einmal.
8. Die Person geht zum Schott und interagiert.
9. Das System zeigt ein oder zwei nächste Räume mit Art, Vorschau, Beschreibung und Bedrohung.
10. Die Person wählt; das System betritt den Raum und sichert den Eingang.

**Erweiterungen**

- 2a: Pause oder Fokusverlust hält die Simulation an; Weiter setzt fort.
- 2b: Unten + Sprung auf einem Laufsteg lässt die Figur hindurchfallen.
- 2c: Die Figur steht an einem Notschalter: E löst den Effekt der Sektion aus (Torpedo, Dampfventil, Kältekammer, Überlast); danach lädt der Schalter 14 Sekunden.
- 2d: Ein Wächter gerät unter eine Presse oder in einen Laser: Der Treffer durchschlägt seine Panzerung und legt den Kern kurz frei.
- 3a: Energie oder Abklingzeit fehlt: kein Modul, keine Energie abgezogen.
- 3b: Integrität null: mit Notfallkapsel einmalige Wiederbelebung, sonst Niederlage (UC-07).
- 3c: Boss erreicht zwei Drittel bzw. ein Drittel Integrität: neue Phase mit neuen Mustern.
- 4b: Hüllenbruch: Der Raum ist nach 40 Sekunden gesichert, unabhängig von besiegten Gegnern; übrige Gegner fliehen.
- 4a: Weitere Welle vorgesehen: Ansage „Verstärkung“, nach 1,8 s neue Gegner links und rechts.
- 5a: Versorgungsraum: Vorräte statt Modul. 5b: Schwarzmarkt, Werkstatt, Kapelle: UC-04.
- 6a: Alle Module maximiert oder nicht freigeschaltet: weniger Angebote.
- 7a: Das gewählte Modul vervollständigt eine Resonanz: Die Karte kündigt sie an; nach der Wahl meldet das System den Bonus einmal.
- 8a: Schott ohne Bergung: nicht gewählte Belohnung verfällt.
- 9a: Nur ein Raum zulässig (Wächter, Werkstatt, Brücke).
- 9b: Ein Raum hat einen Raumzustand (Stromausfall, Alarmstufe Rot, Druckleck): Karte und Raumbeginn nennen Regel und Belohnung.
- 10a: Speicherfehler: Spiel läuft weiter, Meldung erscheint.
- 4c: Brücke: Nach dem Lotsen endet der Tauchgang mit Sieg (UC-06).

**Besondere Anforderungen:** Simulationsschritt 1/120 s, höchstens 1/30 s pro Aufruf. Darstellung und Audio verändern keine Regeln. Raum-Sicherung stellt einen Eingang wieder her.
**Häufigkeit:** bis zu 24-mal pro Zyklus.

## Weitere Use Cases (casual/brief)

- **UC-01:** Klasse (freigeschaltet), Waffe, Modul, Druckstufe (bis zur erreichten), Entdecker, optional Seed oder Tagestauchgang (Seed aus dem Datum) wählen. Bis zum ersten gesicherten Raum erzählt ein kurzer, überspringbarer Auftakt die Ausgangslage. Gesperrtes wird abgelehnt; ungültiger Seed startet nicht. Start ersetzt eine alte Raum-Sicherung.
- **UC-03:** Standardbergung ab Standard-, Elite ab Selten-, Wächter mit einem Legendär-Angebot. Eine Wahl schliesst die Kapsel.
- **UC-04:** Schwarzmarkt: vier Module (Preis nach Seltenheit, Sektion, Druck), Reparaturset, Notreparatur; Käufe einzeln. Werkstatt: einmal gratis Reparatur, zwei Module, Reparaturset, Waffenstufe (Mechanikerin −25 %). Kapelle: bis zu drei Handel, genau einer annehmbar; Opfer werden geprüft.
- **UC-05:** Esc pausiert; Hauptmenü oder Beenden setzt beim letzten Raumeingang fort. Beschädigte oder unbekannte Stände blockieren den Start nicht, Originale werden gesichert. Stände 0.1/0.2 werden übernommen.
- **UC-06:** Sieg zeigt zuerst eine Szene mit dem auftauchenden Boot (überspringbar), dann Raum, Abschüsse, Zeit, Kerne; nächster Zyklus mit gleichem Build und stärkeren Gegnern; Sieg auf Druckstufe n schaltet n+1 frei.
- **UC-07:** Niederlage zeigt Ergebnis; Kerne und Entdeckungen bleiben; R startet neu.
- **UC-08:** Archiv mit Kategorien Taucher, Waffen, Module, Baupläne, Ausrüstung, Garderobe; Kauf mit Datenkernen, Voraussetzungen; Kompendium entdeckter Module; Logbuch mit 26 Zielen, die beim ersten Erreichen Kerne gutschreiben.
- **UC-09:** Anzugfarbe, Helmform, Visier, Metallton mit Vorschau; gesperrte Optionen kaufen.
- **UC-10:** I/Tab: Werte, Waffe, Modul, alle Module; M: Längsschnitt des Bootes mit Position und Wächtern. Beide pausieren.
- **UC-11:** Lautstärken, Kamerawackeln, Röhrenfilter, Schadenszahlen, ruhige Darstellung, Vollbild, Entdecker-Vorgabe; Steuerungsübersicht.

## Funktionale Anforderungen

| ID | Anforderung | Nachweis |
|---|---|---|
| F-01 | Bewegung mit Laufstegen, Sprung (gehalten), Luftsprung, Ausweichen, Kombinationen, Luftangriff, Bodenstampfer | MovementTest, CombatTest |
| F-02 | Zwölf Gegnerarten, fünf Elite-Eigenschaften, vier Bosse mit drei Phasen und Vorwarnungen | CombatTest, CampaignSimulationTest |
| F-03 | 24 Räume in vier Sektionen, Routenwahl, feste Wächter und Werkstätten, Sonderräume | RoomGeneratorTest |
| F-04 | Sieben Waffen, acht aktive Module, 41 passive Module, fünf Klassen | CombatTest, RewardTest |
| F-05 | Bergung, Schwarzmarkt, Werkstatt, Versorgung, Kapelle | RewardTest |
| F-06 | Niederlage, Sieg, Zyklus, Druckstufen | GameRunTest, GameServiceTest |
| F-07 | Raum-Sicherung, Profil, Migration | FileGameRepositoryTest |
| F-08 | Meta-Progression und Garderobe | GameServiceTest, UI-Prüfung |
| F-09 | Audio, Pause, Optionen, Vollbild, Bildschirmfoto | UI-Prüfung; Hörtest offen |
| F-10 | Raumzustände mit Regel und Belohnung, angekündigt in Routenwahl und Raumbeginn | RoomGeneratorTest, RewardTest, UI-Prüfung |
| F-11 | Logbuch: Ziele einmalig erreicht, Kerne gutgeschrieben, gespeichert | GameServiceTest, FileGameRepositoryTest |
| F-12 | Resonanzen: Modulpaare mit Bonus, einmal gemeldet, auf Angebotskarten angekündigt | RewardTest, UI-Prüfung |
| F-13 | Raumtechnik: Band, Düse, Wind, Presse, Laser, Notschalter mit Sektionseffekt; Bossarenen mit Anlagen, die Panzerung durchschlagen | MachineryTest |
| F-14 | Raumereignisse: Hüllenbruch mit Countdown, Schlagseite, Schmugglerdrohne | RoomEventTest, MachineryTest |
| F-15 | Bordsystem-Oberfläche im Pixelbild, vollständig mit Maus und Tastatur bedienbar | UI-Prüfung |

## Qualitätsanforderungen

- **Q-01 Testbarkeit:** Domäne und Pixel-Pipeline laufen ohne JavaFX und Dateisystem.
- **Q-02 Robustheit:** Defekte Stände verhindern den Start nicht; Originale bleiben als Backup.
- **Q-03 Performance:** Zeichnen eines Bildes deutlich unter 16 ms; gemessen ≈ 2 ms im Mittel.
- **Q-04 Bedienbarkeit:** Vorwarnungen, Interaktionshinweise und Einstiegshilfe im ersten Raum; menschlicher Erstnutzertest offen.
- **Q-05 Nachvollziehbarkeit:** KI-Einsatz, Quellen, Tests und Build-Schritte dokumentiert.
- **Q-06 Portabilität:** Java-Quellcode plattformneutral; geprüft nur auf Apple-Silicon-macOS.

## Nicht im Umfang

Mehrspieler, Gamepad, frei belegbare Tasten, Storydialoge, Online-Bestenlisten. Die Run-Dauer von 20–30 Minuten ist eine Planungsannahme; der Testspieler braucht etwa drei Minuten, Menschen deutlich länger.
