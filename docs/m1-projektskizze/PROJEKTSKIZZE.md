# M1 · Projektskizze ABYSS

*Arbeitsfassung nach dem PM3-Auftrag „Projektskizze (M1)“, Stand 25.09.2026. Gliederung gemäss den neun geforderten Kapiteln. Stellen mit `[TEAM]` müssen durch das Projektteam ergänzt oder bestätigt werden; die Abgabe erfolgt in der SoE-Vorlage.*

| | |
|---|---|
| Projekt | ABYSS – Vom Heck bis zur Brücke |
| Modul | PM3 Software-Projekt 3, HS26 |
| Team | David, `[TEAM: Nachnamen und weitere Mitglieder]` |
| Meilenstein | M1, SW3 (Woche ab 28.09.2026) |

---

## 1 Ausgangslage

Im Modul PM3 entwickelt ein Studierendenteam innerhalb eines Semesters eine lauffähige Software mit eigener Architektur in Java; als Oberflächentechnologie ist JavaFX vorgesehen. Die Teamidee ist ein Einzelspieler-Computerspiel für Desktop-Rechner.

Kurze, wiederholbare Spielsitzungen sind ein verbreitetes Muster in aktuellen Actionspielen. Sogenannte Roguelites kombinieren zufällig zusammengestellte Durchläufe mit dauerhaftem Fortschritt zwischen den Versuchen; Beispiele sind *Dead Cells* [1] und *Hades* [2]. Spiele, die ein einzelnes Fahrzeug als Welt nutzen, setzen meist auf die Steuerung des Fahrzeugs und seiner Systeme, etwa *FTL: Faster Than Light* [3] oder *Barotrauma* [4].

Das Team hat keinen externen Auftraggeber. Anforderungen werden deshalb aus einer vorläufigen Persona abgeleitet und mit möglichen Spielenden aus dem Umfeld überprüft (siehe Kapitel 6 und 9).

## 2 Idee

ABYSS ist ein 2D-Action-Roguelite im Inneren eines sehr langen U-Boots. Die Spielfigur beginnt im Heck und kämpft sich Raum für Raum bis zur Brücke vor. Die räumliche Hierarchie – hinten enge Wartungsräume, vorne die Kontrolle über das Schiff – ist von der Film- und Serienidee *Snowpiercer* [5] inspiriert; Figuren, Namen und Handlung sind eigenständig.

Jeder Durchlauf („Tauchgang“) führt durch 24 Räume in vier Sektionen. An Abzweigungen wählt die spielende Person zwischen Risiko und Sicherheit, sammelt Module für ihren Build und stellt sich am Ende jeder Sektion einem Wächter. Bei einer Niederlage beginnt der nächste Versuch im Heck mit neuer Route; dauerhaft erworbene Freischaltungen bleiben erhalten.

Das gelöste Problem: Spielende mit wenig Zeit erhalten abgeschlossene, abwechslungsreiche Sitzungen mit verständlichen Entscheidungen. Der Kernnutzen ist ein kurzer, fairer Spannungsbogen, der sich bei jedem Versuch anders anfühlt.

## 3 Kundennutzen

Die vorläufige Persona **Lina** (22, Studentin) spielt am Laptop in Pausen von 20 bis 30 Minuten. Sie mag kurze Actionspiele und abwechslungsreiche Ausrüstung. Scheitern akzeptiert sie, wenn sie die Ursache versteht. Die Persona ist eine Annahme und wird im Semester überprüft.

Für Lina wird Folgendes besser möglich:

- **Einstieg ohne Hürde:** Das Spiel startet lokal ohne Konto oder Internetverbindung. Ein ruhiger erster Raum erklärt Bewegung und Ausweichen.
- **Unterbrechbar:** Pause jederzeit; beim Beenden wird der letzte Raumeingang gesichert.
- **Verständliche Niederlagen:** Gegner kündigen Angriffe sichtbar an, sodass Fehler nachvollziehbar sind.
- **Abwechslung:** Route, Räume, Gegner und Belohnungen ändern sich pro Versuch; Freischaltungen eröffnen neue Spielweisen statt reiner Zahlensteigerungen.

## 4 Stand der Technik und Konkurrenzanalyse

Tabelle 1 vergleicht ABYSS mit vier etablierten Spielen, die jeweils einen Teil der Idee abdecken.

*Tabelle 1: Einordnung von ABYSS gegenüber verwandten Spielen (eigene Darstellung nach [1]–[4])*

| Spiel | Genre / Kern | Welt | Gemeinsamkeit mit ABYSS | Unterschied zu ABYSS |
|---|---|---|---|---|
| Dead Cells [1] | Roguelite-Action-Plattformer | wechselndes Schloss | Durchläufe mit Permadeath und Fortschritt | keine lineare Reise durch ein Fahrzeug |
| Hades [2] | Roguelite-Dungeon-Crawler | Unterwelt, Raumfolge | Raum-für-Raum-Kämpfe, dauerhafte Upgrades | Draufsicht, Mythologie statt Maschinenwelt |
| FTL [3] | Raumschiff-Roguelike | prozedurale Galaxie | Routenwahl, Entscheidungen, Permadeath | Steuerung des Schiffs, kein Plattformkampf |
| Barotrauma [4] | Kooperative U-Boot-Simulation | Ozean auf Europa | U-Boot-Setting, 2D | Mehrspieler-Simulation, keine Roguelite-Kampagne |

Aus Tabelle 1 geht hervor, dass keines der Vergleichsspiele eine lineare Kampfreise durch das Innere eines einzigen Fahrzeugs mit Roguelite-Wiederholung verbindet. ABYSS grenzt sich zusätzlich durch interaktive Raumtechnik (Pressen, Laser, Notschalter) ab, die sich gegen Gegner einsetzen lässt. Als Studienprojekt konkurriert ABYSS nicht kommerziell; die Einordnung dient der Abgrenzung des Umfangs.

## 5 Hauptablauf / Kontextszenario

Lina hat nach einer Vorlesung eine halbe Stunde Zeit und startet einen neuen Tauchgang. Sie wählt eine Taucherin, deren Stärken ihr in einer kurzen Beschreibung erklärt werden, und beginnt im Heck des U-Boots. Im ersten Raum lernt sie, auszuweichen, als ein Gegner seinen Angriff sichtbar ankündigt. Nach dem Sieg über die Patrouille öffnet sich das Schott, und Lina wählt aus mehreren Fundstücken eines, das ihren Angriff verstärkt.

Vor dem nächsten Raum sieht sie zwei mögliche Wege: einen Frachtraum mit Gegnern und ein sicheres Depot mit Vorräten. Weil ihre Figur angeschlagen ist, wählt sie das Depot. Später gerät sie in einen Raum, in dem eine Hydraulikpresse arbeitet. Sie lockt einen schwer gepanzerten Gegner unter die Presse und nutzt so die Umgebung zu ihrem Vorteil.

Am Ende der ersten Sektion unterliegt sie dem Wächter knapp. Sie versteht, dass sie dessen Stampfangriff zu spät erkannt hat. Ihre gesammelten Datenkerne bleiben erhalten; sie schaltet damit eine neue Taucherin frei und nimmt sich vor, beim nächsten Versuch einen anderen Weg zu wählen. Das gewünschte Resultat ist eine abgeschlossene, verständliche Sitzung, die zum nächsten Versuch motiviert.

## 6 Weitere Anforderungen

**Wichtige Funktionen** (Details: [ANFORDERUNGEN.md](../ANFORDERUNGEN.md))

- Plattformbewegung mit Sprung, Ausweichen, Kombinationsangriffen und aktiven Modulen.
- 24 Räume in vier Sektionen mit Routenwahl, Wächtern, Händlern, Werkstätten und Kapellen.
- Module mit Seltenheiten und Resonanzen, Klassen, Garderobe, Archiv mit Freischaltungen.
- Raumzustände (z. B. Stromausfall, Hüllenbruch) und Raumtechnik (Pressen, Laser, Notschalter).
- Sicherung am Raumeingang, Fortsetzen nach dem Beenden, lokales Profil.

**Nichtfunktionale Anforderungen**

| ID | Anforderung | Messgrösse |
|---|---|---|
| Q-01 | Testbarkeit | Spiellogik ohne JavaFX und Dateisystem testbar |
| Q-02 | Robustheit | beschädigte Spielstände verhindern den Start nicht |
| Q-03 | Performance | Zeichnen eines Bildes deutlich unter 16 ms (60 FPS) |
| Q-04 | Bedienbarkeit | vier von fünf Testpersonen erkennen einen angekündigten Angriff ohne Hilfe |
| Q-05 | Nachvollziehbarkeit | KI-Einsatz, Quellen und Tests dokumentiert |

**Weiterführende Ideen:** Gamepad-Unterstützung (benötigt eine Bibliothek und die Zustimmung der Dozierenden), frei belegbare Tasten, weitere Sektionen und Bosse, Story-Fragmente mit alternativen Enden.

## 7 Ressourcen

- **Fähigkeiten im Team:** `[TEAM: Java-, JavaFX-, Grafik- und Testerfahrung je Person]`.
- **Werkzeuge:** Java 25, JavaFX 26, Gradle, JUnit, Git; keine Frameworks oder Game-Engines gemäss Kursvorgabe.
- **Fehlendes Know-how:** Spielphysik, Kollisionen, Echtzeit-Darstellung und Balancing sind für das Team neu und werden früh im Prototyp erprobt.
- **Aufwand und Abgrenzung:** `[TEAM: verfügbare Personenstunden pro Woche]`. Für das Semester sind Einzelspieler, Tastatur/Maus und macOS als geprüfte Plattform vorgesehen; Mehrspieler, Online-Funktionen und Storykampagnen sind ausgeschlossen.

## 8 Risiken

| Risiko | Wahrscheinlichkeit / Auswirkung | Massnahme |
|---|---|---|
| Team versteht umfangreichen, teilweise KI-generierten Code nicht ausreichend | hoch / hoch | Verantwortung pro Paket verteilen, Code-Reviews, eigene Begründungen in den Berichten |
| Spielidee oder Technologie nicht vom Dozenten freigegeben | offen / hoch | früh abstimmen; JavaFX und eigene Architektur belegen |
| Kampf wirkt unfair oder eintönig | mittel / hoch | Spieltests mit Personen aus dem Umfeld, Anpassung nach Beobachtung |
| Echtzeit-Darstellung zu langsam | niedrig / mittel | eigene Pixel-Pipeline mit Messung der Zeichenzeit |
| Andere Betriebssysteme des Teams | mittel / mittel | plattformneutraler Java-Code, Test auf Teamrechnern |

## 9 Grobplanung

**Use Cases** (Übersicht in Abbildung 1): Tauchgang vorbereiten, zum nächsten Raum vordringen (Kernfall), Bergung wählen, handeln, unterbrechen und fortsetzen, Brücke erobern, erneut tauchen, freischalten, Aussehen anpassen, Ausrüstung ansehen, Optionen einstellen.

![Use-Case-Übersicht](../diagrams/01-use-cases.svg)

*Abbildung 1: Use-Case-Übersicht von ABYSS mit der Spielerin als Akteurin und dem lokalen Dateisystem als Nachbarsystem.*

Abbildung 1 zeigt, dass alle Anwendungsfälle von einer einzigen Person ausgelöst werden; das Dateisystem ist nur für Sichern, Fortsetzen und das Archiv beteiligt.

*Tabelle 2: Meilensteine und Iterationsziele*

| Meilenstein | Termin laut HS26-Plan | Ziel |
|---|---|---|
| M1 Projektskizze | SW3 (ab 28.09.2026) | Idee, Nutzen, Plan präsentieren |
| Iteration 1 | SW4–SW6 | Spielgefühl: Bewegung, Ausweichen, Angriff, ein Gegnertyp |
| Iteration 2 | SW6–SW9 | vollständiger kurzer Durchlauf mit Routenwahl und Sicherung |
| M2 Lösungsarchitektur | SW9 (ab 09.11.2026) | Architektur durch Prototyp belegt, Technischer Bericht I |
| Iteration 3 | SW9–SW12 | Inhalt, Bosse, Meta-Fortschritt, Spieltests |
| M3 Prototyp | SW13 (ab 07.12.2026) | fertiger Prototyp, Technischer Bericht II, Abschlusspräsentation |

Hinweis: Ein weit fortgeschrittener Prototyp (Version 1.1) liegt bereits vor; die Iterationen dienen deshalb vor allem dem Verständnis durch das Team, Spieltests und Verfeinerung (siehe [PROJEKTMANAGEMENT.md](../PROJEKTMANAGEMENT.md)).

---

## Quellen

[1] Motion Twin, *Dead Cells*. Motion Twin, 2018. [Online]. Available: https://dead-cells.com/ (accessed Sep. 25, 2026).

[2] Supergiant Games, *Hades*. Supergiant Games, 2020. [Online]. Available: https://www.supergiantgames.com/games/hades/ (accessed Sep. 25, 2026).

[3] Subset Games, *FTL: Faster Than Light*. Subset Games, 2012. [Online]. Available: https://store.steampowered.com/app/212680/FTL_Faster_Than_Light/ (accessed Sep. 25, 2026).

[4] FakeFish and Undertow Games, *Barotrauma*. Daedalic Entertainment, 2023. [Online]. Available: https://barotraumagame.com/ (accessed Sep. 25, 2026).

[5] J. Bong, *Snowpiercer*. CJ Entertainment, 2013. [Film].
