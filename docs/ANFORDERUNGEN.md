# Anforderungen und Use Cases · Version 1.2

Stand 06.10.2026, überarbeitet nach dem Feedback zur Projektskizze M1: wenige, grössere Use Cases mit einem Kernfall, nur ein Akteur, relative Priorisierung mit Abhängigkeiten statt Muss/Soll und nicht-funktionale Anforderungen nach FURPS+. Ein prüfbarer Vorschlag für das Team, keine vom Dozenten abgenommene Spezifikation. Diagramme: `docs/diagrams/01-use-cases` und `docs/diagrams/14-arbeitspakete`.

## Produktziel und Akteur

Eine Person spielt lokal am Desktop ein Pixel-Art-Roguelite: vom Heck eines langen U-Boots durch 24 Räume bis zur Brücke. Jeder Raum ist eine überschaubare Kampf-, Handels- oder Risikoentscheidung. Eine Niederlage beendet den Tauchgang; Datenkerne, Freischaltungen und Entdeckungen bleiben. Nach einem Sieg sind weitere Zyklen und höhere Druckstufen möglich.

Der Kundennutzen ist Spielspass. Kurze abgeschlossene Durchläufe, nachvollziehbare Niederlagen, eigene Entscheidungen und ein einfacher Einstieg verstärken ihn.

Einziger Akteur: Spieler/in. Profil und Raum-Sicherung speichert das System selbst; das lokale Dateisystem liegt innerhalb der Systemgrenze und ist kein Akteur. Kein Konto, kein Netzwerk.

## Use-Case-Übersicht

| ID | Use Case | Rang | Abhängig von | Tiefe |
|---|---|---|---|---|
| UC-01 | Tauchgang spielen | 1 (Kernfall) | – | Fully dressed |
| UC-02 | Fortschritt freischalten und Figur gestalten | 2 | UC-01 (Datenkerne entstehen in Tauchgängen) | Casual |
| UC-03 | Spiel einstellen | 3 | – | Brief |

Der Rang ist relativ: 1 ist am wichtigsten und wird zuerst abgesichert. Die frühere Liste UC-01 bis UC-11 aus der Projektskizze geht in diesen drei Fällen auf. Bergung, Handel, Unterbrechen, Sieg und Niederlage sind Varianten von UC-01 und erscheinen dort als Erweiterungen.

## UC-01 · Tauchgang spielen (fully dressed)

**Ziel:** Einen Tauchgang vom Heck bis zur Brücke spielen und dabei Ausrüstung, Route und Kampf selbst entscheiden.
**Umfang/Ebene:** ABYSS, Benutzerziel. **Primärakteur:** Spieler/in. **Auslöser:** Die Person startet im Hauptmenü einen neuen Tauchgang.
**Stakeholder und Interessen:** Spielende erwarten Spielspass, lesbare Gefahren, reaktionsschnelle Steuerung, faire Belohnungen und einen sicheren Unterbruch. Das Team braucht nachvollziehbare, testbare Regeln.
**Vorbedingungen:** Das Spiel läuft; ein Profil ist geladen oder wurde neu angelegt.
**Erfolgsgarantie:** Der Tauchgang endet mit Sieg oder Niederlage; Datenkerne und Entdeckungen sind dem Profil gutgeschrieben.
**Minimalgarantie:** Ein ungesicherter Raum kann nicht verlassen werden. Ungültige Eingaben ändern nichts. Speicherfehler beenden das Spiel nicht und werden gemeldet.

**Hauptszenario**

1. Die Person wählt in der Schleuse Taucherklasse, Waffe, aktives Modul und Druckstufe und startet.
2. Das System stellt das Boot mit Route und Räumen zusammen und zeigt den ersten Raum mit Name und Sektion.
3. Die Person bewegt sich, springt auf Laufstege, weicht aus und greift mit Waffe und Modul an.
4. Das System kündigt jeden Gegnerangriff mit einer Vorwarnung an und berechnet Treffer. Nach der letzten Welle markiert es den Raum als gesichert, öffnet das Schott und zeigt die Bergungskapsel.
5. Die Person öffnet die Kapsel und wählt eines von drei bis vier Angeboten; das System installiert es genau einmal.
6. Die Person interagiert mit dem Schott; das System zeigt ein oder zwei nächste Räume mit Art, Vorschau, Beschreibung und Bedrohung.
7. Die Person wählt einen Raum; das System betritt ihn und sichert den Raumeingang.
   Die Schritte 3 bis 7 wiederholen sich bis zur Brücke. Jede Sektion endet mit einem Wächter.
8. Die Person besiegt auf der Brücke den Lotsen.
9. Das System zeigt den Sieg mit Auswertung (Räume, Abschüsse, Zeit, Datenkerne) und schreibt die Datenkerne dem Profil gut.

**Erweiterungen**

- *a: Pause oder Fokusverlust hält die Simulation an; Weiter setzt fort.
- *b: Unterbrechen: Die Person beendet über Pause oder Hauptmenü; der Tauchgang bleibt am letzten Raumeingang gesichert.
- *c: Die Person öffnet Ausrüstung (I/Tab) oder Bootskarte (M); beide pausieren.
- 1a: Fortsetzen: Ein gesicherter Tauchgang existiert; die Person wählt „Fortsetzen“ und das System setzt am letzten Raumeingang fort. Beschädigte oder unbekannte Stände blockieren den Start nicht, Originale werden gesichert.
- 1b: Die Person gibt einen Seed ein oder wählt den Tagestauchgang; ein ungültiger Seed startet nicht. Gesperrte Optionen werden abgelehnt.
- 3a: Unten + Sprung auf einem Laufsteg lässt die Figur hindurchfallen.
- 3b: An einem Notschalter löst E den Sektionseffekt aus; danach lädt der Schalter 14 Sekunden.
- 3c: Energie oder Abklingzeit fehlt: kein Modul, keine Energie abgezogen.
- 4a: Leben null: Mit Notfallkapsel einmalige Wiederbelebung, sonst Niederlage. Das System zeigt das Ergebnis; Datenkerne und Entdeckungen bleiben. Die Person kann sofort erneut tauchen (weiter bei 1).
- 4b: Wächterraum: Der Wächter ist ausserhalb seiner Erholungsphase gepanzert und wechselt bei zwei Dritteln und einem Drittel Leben in eine neue Phase. Ein Treffer durch Raumtechnik legt seinen Kern frei.
- 4c: Weitere Welle vorgesehen: Ansage „Verstärkung“, nach 1,8 s neue Gegner.
- 4d: Hüllenbruch: Der Raum ist nach 40 Sekunden gesichert; übrige Gegner fliehen.
- 5a: Sonderraum: Am Schwarzmarkt, in der Werkstatt oder in der Kapelle setzt die Person Schrott ein (Käufe einzeln, in der Kapelle genau ein Handel). Im Versorgungsraum gibt es Vorräte statt Modul.
- 5b: Das gewählte Modul vervollständigt eine Resonanz: Die Karte kündigt sie an, das System meldet den Bonus einmal.
- 5c: Alle Module maximiert oder nicht freigeschaltet: weniger Angebote. Schott ohne Bergung: Die nicht gewählte Belohnung verfällt.
- 6a: Nur ein Raum zulässig (Wächter, Werkstatt, Brücke).
- 6b: Ein Raum hat einen Raumzustand (Stromausfall, Alarmstufe Rot, Druckleck); Karte und Raumbeginn nennen Regel und Belohnung.
- 7a: Speicherfehler: Das Spiel läuft weiter, eine Meldung erscheint.
- 9a: Nächster Zyklus: Die Person taucht mit gleichem Build weiter; Gegner werden stärker. Ein Sieg auf Druckstufe n schaltet Stufe n+1 frei.

**Besondere Anforderungen:** Simulationsschritt 1/120 s, höchstens 1/30 s pro Aufruf. Darstellung und Audio verändern keine Regeln.
**Häufigkeit:** mehrmals pro Spielsitzung; ein Tauchgang dauert nach Planungsannahme 20 bis 30 Minuten.

## UC-02 · Fortschritt freischalten und Figur gestalten (casual)

Die Person öffnet das Archiv und tauscht Datenkerne gegen Taucherklassen, Waffen, Module, Baupläne und Ausrüstung; Voraussetzungen werden geprüft, gesperrte Einträge abgelehnt. Im Kompendium sieht sie entdeckte Module, im Logbuch 32 Ziele, die beim ersten Erreichen Kerne gutschreiben. In der Garderobe wählt sie Anzugfarbe, Helmform, Visier und Metallton mit Vorschau und kauft gesperrte Optionen. Alle Änderungen bleiben im Profil gespeichert.

## UC-03 · Spiel einstellen (brief)

Die Person stellt Lautstärken, Kamerawackeln, Röhrenfilter, Schadenszahlen, ruhige Darstellung, Vollbild und die Entdecker-Vorgabe ein und sieht die Steuerungsübersicht.

## Arbeitspakete: Rang und Abhängigkeiten

Die Varianten der Use Cases eignen sich als Arbeitspakete. Der Prototyp deckt alle Pakete bereits ab; der Rang legt fest, in welcher Reihenfolge das Team sie durcharbeitet, testet und verbessert und was bei Zeitmangel zuletzt kommt. Die Reihenfolge folgt dem Kundennutzen: zuerst das, was den Spielspass eines Tauchgangs trägt.

| Rang | Arbeitspaket | Use Case | Voraussetzung |
|---|---|---|---|
| 1 | Raum bewältigen: Bewegung, Kampf, Vorwarnung | UC-01 | – |
| 2 | Tauchgang beginnen: Ausrüstung wählen, Boot erzeugen | UC-01 | – |
| 3 | Raum sichern und nächsten Raum wählen | UC-01 | 1, 2 |
| 4 | Bergung wählen | UC-01 | 3 |
| 5 | Niederlage und erneut tauchen | UC-01 | 1 |
| 6 | Wächterkampf mit Phasen | UC-01 | 1 |
| 7 | Unterbrechen und fortsetzen | UC-01 | 3 |
| 8 | In Sonderräumen handeln | UC-01 | 4 |
| 9 | Brücke erobern, nächster Zyklus | UC-01 | 3, 6 |
| 10 | Raumzustände und Raumtechnik | UC-01 | 3 |
| 11 | Ausrüstung und Bootskarte ansehen | UC-01 | 4 |
| 12 | Im Archiv freischalten, Logbuch | UC-02 | 5 |
| 13 | Figur gestalten (Garderobe) | UC-02 | 12 |
| 14 | Optionen und Steuerung | UC-03 | – |

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

## Nicht-funktionale Anforderungen nach FURPS+

Gruppiert nach der FURPS+-Checkliste aus SWEN1 LE03 (Zusammenfassung, PDF-Seite 40). Die IDs Q-01 bis Q-07 bleiben aus der Projektskizze erhalten.

| Kategorie | ID | Anforderung | Prüfkriterium |
|---|---|---|---|
| Functionality (Sicherheit) | Q-07 | Datenschutz: Das Spiel überträgt keine Daten. | Kein Netzwerkzugriff; Profil und Spielstände liegen ausschliesslich lokal. |
| Usability | Q-04 | Angriffe sind erkennbar, der Einstieg gelingt ohne Anleitung. | Vier von fünf Testpersonen erkennen eine Angriffsankündigung ohne Erklärung und schliessen den ersten Raum ohne Hilfe ab. |
| Reliability | Q-02 | Wiederanlauf und Datensicherung: Beschädigte oder veraltete Spielstände verhindern den Start nicht; ein Unterbruch kostet höchstens den aktuellen Raum. | Mit einer defekten Datei startet das Spiel mit neuem Profil, das Original bleibt als Sicherung erhalten; Fortsetzen beginnt am letzten Raumeingang. |
| Performance | Q-03 | Die Darstellung läuft flüssig mit 60 Bildern pro Sekunde. | Die Berechnung eines Bildes dauert im Mittel unter 16 ms auf einem aktuellen Laptop; gemessen ≈ 2 ms. |
| Supportability (Wartbarkeit) | Q-01 | Die Spielregeln sind unabhängig von Oberfläche und Speicherung testbar. | Automatische Tests der Spiellogik laufen ohne Fenster und ohne Dateisystem. |
| + Implementation | Q-06 | Plattformen: ABYSS läuft als Desktop-Anwendung unter macOS, Windows und Linux; eine Mobile-App ist nicht vorgesehen. | Start und ein vollständiger Raum auf allen drei Betriebssystemen; das sechsköpfige Team deckt sie mit eigenen Rechnern ab. |
| + Implementation | – | Java mit JavaFX, eigene geschichtete Architektur, keine Frameworks, keine relationale Datenbank (PM3-Kick-off). | Einzige Produktionsbibliothek ist JavaFX. |
| + Legal | Q-05 | KI-Einsatz, Quellen und eigene Assets sind offengelegt. | Deklaration nach SoE-Richtlinie, Quellen nach IEEE, Herkunft der Assets in CREDITS.md. |

Interface und Operations: keine externen Schnittstellen und kein Serverbetrieb. Packaging: Auslieferung als Gradle-Projekt mit Wrapper (`./gradlew run`), für macOS zusätzlich als App-Paket.

## Nicht im Umfang

Mehrspieler, Gamepad, frei belegbare Tasten, Storydialoge, Online-Bestenlisten, Mobile-Apps. Die Run-Dauer von 20 bis 30 Minuten ist eine Planungsannahme; der Testspieler braucht etwa drei Minuten, Menschen deutlich länger.
