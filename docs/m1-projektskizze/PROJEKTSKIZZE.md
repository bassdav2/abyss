# Projektskizze ABYSS – Vom Heck bis zur Brücke

*PM3 HS26 · Meilenstein M1 · Team ABYSS · Abgabe 29. September 2026 · Überarbeitete Fassung nach dem Feedback vom 6. Oktober 2026. Lesefassung; massgeblich ist [ABYSS_Projektskizze_M1_ueberarbeitet.pdf](ABYSS_Projektskizze_M1_ueberarbeitet.pdf) (Word: [ABYSS_Projektskizze_M1_ueberarbeitet.docx](ABYSS_Projektskizze_M1_ueberarbeitet.docx)). Die abgegebene Fassung liegt unverändert als [ABYSS_Projektskizze_M1.pdf](ABYSS_Projektskizze_M1.pdf) vor. Erzeugt mit `tools/create_projektskizze.py`.*

## 1. Ausgangslage

Kurze, in sich abgeschlossene Spielsitzungen sind ein verbreitetes Muster bei Computerspielen für Desktop-Rechner. Ein Genre, das darauf aufbaut, sind sogenannte **Roguelites**: Jeder Durchlauf wird zufällig zusammengestellt, endet mit Sieg oder Niederlage und beginnt danach von vorn, wobei ein Teil des Fortschritts über die Versuche hinweg erhalten bleibt [1]. Wie gross die Nachfrage nach solchen Spielen ist, zeigen zwei Beispiele: *Dead Cells* wurde nach Angaben der Entwickler mehr als zehn Millionen Mal verkauft [2], *Hades* überschritt kurz nach dem Erscheinen der Vollversion eine Million verkaufte Exemplare [3].

Eine zweite Gruppe von Spielen nutzt ein einzelnes Fahrzeug als Spielwelt. In *FTL: Faster Than Light* führen Spielende ein Raumschiff durch eine zufällig erzeugte Galaxie [4], in *Barotrauma* bedient eine Mannschaft gemeinsam ein U-Boot [5]. Im Mittelpunkt steht dort die Steuerung des Fahrzeugs und seiner Systeme, nicht der Weg einer einzelnen Figur durch das Innere des Fahrzeugs.

Für Spielende mit wenig Zeit bleibt dabei aus Sicht des Teams eine Lücke. Erstens verbindet keines dieser Spiele den direkten Kampf einer einzelnen Figur mit der Reise durch ein Fahrzeug, obwohl gerade das Innere eines Fahrzeugs eine natürliche Abfolge von Räumen bildet. Zweitens passen Durchläufe nicht immer in eine kurze Pause, und Niederlagen wirken unfair, wenn Angriffe ohne erkennbare Vorwarnung treffen. Beides sind Annahmen, die das Team in Interviews und Spieltests überprüft (vgl. Kapitel 3).

ABYSS entsteht im Rahmen eines Innovationswettbewerbs: Innerhalb eines Semesters entwickelt das Team einen Prototyp, der die zentralen Funktionen und den Kundennutzen zeigt. Erst nach der Präsentation am Semesterende wird entschieden, ob daraus ein richtiges Projekt entsteht [6]. Einen externen Auftraggeber gibt es nicht; die Zielgruppe wird deshalb über eine Persona beschrieben.

## 2. Idee

ABYSS ist ein zweidimensionales Action-Roguelite, das vollständig im Inneren eines sehr langen U-Boots spielt. Die Spielfigur gehört zur Besatzung und beginnt im Heck. Raum für Raum kämpft sie sich bis zur Brücke vor, um die Kontrolle über das Boot zu übernehmen. Die räumliche Ordnung – hinten enge Wartungsräume, vorne die Schiffsführung – ist vom Film *Snowpiercer* inspiriert [7]; Figuren, Namen und Handlung sind eigenständig.

Wie Abbildung 1 zeigt, ist das Boot in vier Sektionen mit je sechs Räumen gegliedert. Jede Sektion endet mit einem Wächter, einem besonders starken Gegner. Vor jedem Raum wählen Spielende an einer Abzweigung zwischen zwei Wegen, etwa zwischen einem Kampfraum mit höherer Belohnung und einem ruhigeren Versorgungsraum. Ein Durchlauf, im Spiel «Tauchgang» genannt, ist auf eine Sitzung von etwa 30 Minuten ausgelegt; diese Dauer ist eine Annahme und wird in Spieltests überprüft.

Damit setzt ABYSS bei der Lücke aus Kapitel 1 an: Jeder Tauchgang ist ein abgeschlossener Spannungsbogen für eine Sitzung, jede Niederlage ist dank angekündigter Angriffe nachvollziehbar, und weil Route, Räume, Gegner und Belohnungen jedes Mal neu zusammengestellt werden, spielt sich jeder Versuch anders.

![Abbildung 1](abbildungen/abb-boot.png)

*Abbildung 1: Aufbau des U-Boots: 24 Räume in vier Sektionen vom Heck bis zur Brücke; jede Sektion endet mit einem Wächter (Bootskarte im Prototyp, eigene Beschriftung)*

Eine Bildschirmansicht des bestehenden Prototyps zeigt Abbildung 2. Die nummerierten Markierungen bezeichnen (1) Leben, Energie und Reparatursets der Spielfigur, (2) die aktuelle Angriffswelle, (3) die Raumposition 15 von 24 sowie gesammelten Schrott und Datenkerne, (4) die Spielfigur mit Stirnlampe, (5) die rote Markierung, mit der ein Gegner seinen Angriff ankündigt, (6) Waffe, aktives Modul und Ausweichen sowie (7) die installierten Module. Alle Grafiken werden im Programmcode als Pixelgrafik mit 480 × 270 Bildpunkten erzeugt und für die Anzeige ganzzahlig vergrössert.

![Abbildung 2](abbildungen/abb-bildschirm.png)

*Abbildung 2: Bildschirmansicht des Prototyps im Forschungsdeck mit nummerierten Bedienelementen*

## 3. Kundennutzen

Der eigentliche Kundennutzen von ABYSS ist **Spielspass**: spannende, abwechslungsreiche Unterhaltung, die auch in eine kurze Pause passt. Die Hauptnutzenden sind Einzelspielende, die am eigenen Laptop oder Desktop-Rechner spielen. Stellvertretend für diese Gruppe steht die folgende Persona. Sie ist eine Annahme des Teams; ihre Merkmale werden in Interviews und Spieltests überprüft.

> **Persona Lina (22)** studiert und spielt am Laptop zwischen Vorlesungen oder am Abend, meist 20 bis 30 Minuten am Stück. Sie mag kurze Actionspiele und abwechslungsreiche Ausrüstung. Niederlagen akzeptiert sie, wenn sie deren Ursache erkennt. Lange Einführungen und einen Zwang zur Internetverbindung lehnt sie ab.

Für Lina und vergleichbare Spielende verstärken folgende Eigenschaften den Spielspass:

- **Abgeschlossene Sitzung:** Ein Durchlauf passt in eine Pause. Beim Beenden wird der Eingang des aktuellen Raums gesichert, sodass eine Unterbrechung ohne Verlust möglich ist.
- **Nachvollziehbare Niederlagen:** Gegner kündigen ihre Angriffe sichtbar an (Markierung 5 in Abbildung 2). Eine Niederlage lässt sich dadurch auf eine konkrete Situation zurückführen.
- **Abwechslung statt reiner Zahlensteigerung:** Route, Räume, Raumzustände und Belohnungen werden pro Durchlauf neu zusammengestellt. Dauerhafte Freischaltungen erweitern die Auswahl an Figuren, Waffen und Modulen, statt nur Werte zu erhöhen.
- **Eigene Entscheidungen:** Die Routenwahl verlangt eine Abwägung zwischen Risiko und Belohnung. Anlagen im Raum, etwa Hydraulikpressen, lassen sich gezielt gegen Gegner einsetzen.
- **Einstieg ohne Hürden:** Das Spiel läuft lokal ohne Konto und ohne Internetverbindung. Der erste Raum führt ohne Texttafeln in die Steuerung ein.

## 4. Stand der Technik / Konkurrenzanalyse

Die in Tabelle 1 aufgeführten Spiele decken jeweils einen Teil der Idee ab. Ausgewählt wurden zwei kommerziell erfolgreiche Roguelites und zwei Spiele, deren Welt aus einem einzelnen Fahrzeug besteht.

*Tabelle 1: Einordnung von ABYSS gegenüber verwandten Spielen (eigene Darstellung)*

| Spiel | Entwicklung, Jahr | Spielprinzip | Gemeinsamkeit mit ABYSS | Unterschied zu ABYSS |
|---|---|---|---|---|
| Dead Cells [8] | Motion Twin, 2018 | 2D-Action-Plattformer mit zufälligen Durchläufen | Plattformkampf, Neubeginn nach Niederlage, dauerhafte Freischaltungen | Welt ist ein verzweigtes Schloss, keine lineare Reise durch ein Fahrzeug |
| Hades [9] | Supergiant Games, 2020 | Action-Roguelite in Draufsicht | Raumfolge mit Belohnungswahl nach jedem Raum | Draufsicht statt Seitenansicht, mythologische Unterwelt statt Maschinenwelt |
| FTL: Faster Than Light [4] | Subset Games, 2012 | Weltraum-Strategiespiel mit Roguelike-Elementen | Routenwahl, Entscheidungen unter Risiko, ein Fahrzeug als Welt | Steuerung des Schiffs statt Kampf einer Figur |
| Barotrauma [5] | FakeFish und Undertow Games, 2023 | Kooperative U-Boot-Simulation | U-Boot als Spielwelt in 2D-Seitenansicht | Mehrspieler-Simulation ohne Roguelite-Durchläufe |

Aus Tabelle 1 geht hervor, dass keines der Vergleichsspiele den Plattformkampf einer einzelnen Figur mit einer linearen Reise durch das Innere eines Fahrzeugs verbindet. Daraus ergeben sich drei Vorteile von ABYSS:

- **Ein Boot als ganze Welt:** Der Weg vom Heck zur Brücke ist zugleich Spielwelt und Fortschrittsanzeige. Spielende sehen jederzeit, wie weit sie gekommen sind und was vor ihnen liegt.
- **Das Boot kämpft mit:** Förderbänder, Pressen, Lasergitter und Notschalter treffen Figur und Gegner gleichermassen und lassen sich gezielt gegen Wächter einsetzen.
- **Ein ganzer Durchlauf pro Pause:** Ein Tauchgang ist auf etwa 30 Minuten ausgelegt, Angriffe sind angekündigt, und am Raumeingang wird gesichert. Dead Cells und Hades bieten ähnliche Kämpfe, aber keine Reise durch ein Fahrzeug; FTL und Barotrauma bieten das Fahrzeug, aber keinen Kampf einer einzelnen Figur.

ABYSS positioniert sich damit zwischen den Action-Roguelites und den Fahrzeugspielen und spricht Spielende beider Gruppen an.

## 5. Hauptablauf (Kontextszenario)

Lina hat nach einer Vorlesung eine halbe Stunde Zeit. Sie startet das Spiel auf ihrem Laptop und beginnt einen neuen Tauchgang. In der Schleuse wählt sie eine von mehreren Figuren; eine kurze Beschreibung nennt jeweils deren Stärken und Schwächen. Sie entscheidet sich für die Mechanikerin, weil deren Werte ausgewogen sind.

Der erste Raum im Heck ist ruhig und zeigt, wie sich die Figur bewegt, springt und ausweicht. Im zweiten Raum trifft Lina auf eine Patrouille. Ein Gegner leuchtet kurz rot auf, bevor er zuschlägt; Lina weicht aus und besiegt ihn. Sobald alle Gegner besiegt sind, öffnet sich das Schott zum nächsten Raum, und in einer Bergungskapsel werden drei Module angeboten. Lina wählt eines, das ihren Angriff verstärkt.

An der nächsten Abzweigung zeigen zwei Bildschirme, was hinter den Schotts liegt: ein Frachtraum mit zwei Gegnerwellen und ein Depot mit Vorräten. Da das Leben ihrer Figur gesunken ist, wählt sie das Depot und repariert ihre Ausrüstung. Einige Räume später arbeitet in einem Maschinenraum eine Hydraulikpresse. Lina lockt einen gepanzerten Gegner unter die Presse und nutzt die Anlage so zu ihrem Vorteil.

Am Ende der Hecksektion tritt sie gegen den ersten Wächter an und verliert knapp, weil sie dessen Stampfangriff zu spät erkennt. Die Auswertung zeigt, wie weit sie gekommen ist und wie viele Datenkerne sie gesammelt hat. Im Archiv schaltet sie damit eine weitere Figur frei. Nach knapp dreissig Minuten beendet Lina das Spiel mit dem Vorsatz, beim nächsten Versuch den Angriff früher zu erkennen und eine andere Route zu wählen.

Abbildung 3 fasst diesen Ablauf schematisch zusammen. Der Kern ist die Schleife aus Raum, Bergung und Routenwahl, die sich bis zur Brücke wiederholt. Sieg und Niederlage führen beide zur Auswertung und ins Archiv, von wo aus der nächste Tauchgang beginnt.

![Abbildung 3](abbildungen/abb-ablauf.png)

*Abbildung 3: Ablauf eines Tauchgangs mit der Schleife aus Raum, Bergung und Routenwahl (eigene Darstellung)*

## 6. Weitere Anforderungen

### 6.1. Funktionale Anforderungen

Neben dem Hauptablauf sind folgende Funktionen vorgesehen:

- Plattformbewegung mit Laufen, Sprung, Luftsprung, Ausweichen und Fallen durch Laufstege
- Nahkampf mit Kombinationen, Fernkampfwaffen und aktive Module, die Energie verbrauchen
- vier Sektionen mit je sechs Räumen, Routenwahl mit Vorschau und Sonderräume (Händler, Werkstatt, Kapelle, Versorgung)
- Gegner mit angekündigten Angriffen, Elite-Varianten und ein Wächter pro Sektion mit mehreren Phasen
- Raumzustände wie Stromausfall, Hüllenbruch oder Schlagseite, die in der Routenwahl angekündigt werden
- Raumtechnik (Förderbänder, Dampfdüsen, Pressen, Lasergitter, Notschalter), die Figur und Gegner betrifft
- Module in mehreren Seltenheitsstufen und Zusatzboni für bestimmte Modulpaare («Resonanzen»)
- dauerhafter Fortschritt über ein Archiv mit Freischaltungen, eine Garderobe und ein Logbuch mit Zielen
- Sicherung am Raumeingang, Fortsetzen nach dem Beenden und ein lokales Profil
- Optionen für Lautstärke, Bildschirmmodus und Effekte; Bedienung mit Maus und Tastatur

### 6.2. Nicht-funktionale Anforderungen

Tabelle 2 gruppiert die nicht-funktionalen Anforderungen nach der FURPS+-Checkliste [10]. Zu jeder Anforderung ist ein Kriterium angegeben, mit dem sich am Projektende prüfen lässt, ob sie erfüllt ist.

*Tabelle 2: Nicht-funktionale Anforderungen nach FURPS+ mit Prüfkriterium*

| Kategorie | ID | Anforderung | Prüfkriterium |
|---|---|---|---|
| Functionality (Sicherheit) | Q-07 | Datenschutz: Es werden keine Daten übertragen. | Kein Netzwerkzugriff; Spielstände liegen ausschliesslich lokal. |
| Usability | Q-04 | Angriffe sind erkennbar, der Einstieg gelingt ohne Anleitung. | Vier von fünf Testpersonen erkennen eine Angriffsankündigung ohne Erklärung und schliessen den ersten Raum ohne Hilfe ab. |
| Reliability | Q-02 | Wiederanlauf und Datensicherung: Beschädigte oder veraltete Spielstände verhindern den Start nicht; ein Unterbruch kostet höchstens den aktuellen Raum. | Mit einer defekten Datei startet das Spiel mit neuem Profil, das Original bleibt als Sicherung erhalten; Fortsetzen beginnt am letzten Raumeingang. |
| Performance | Q-03 | Die Darstellung läuft flüssig mit 60 Bildern pro Sekunde. | Die Berechnung eines Bildes dauert im Mittel unter 16 ms auf einem aktuellen Laptop. |
| Supportability | Q-01 | Wartbarkeit: Die Spielregeln sind unabhängig von Oberfläche und Speicherung testbar. | Automatische Tests der Spiellogik laufen ohne Fenster. |
| + Implementation | Q-06 | Plattformen: ABYSS läuft als Desktop-Anwendung unter macOS, Windows und Linux; eine Mobile-App ist nicht vorgesehen. | Start und ein vollständiger Raum auf allen drei Betriebssystemen. |
| + Legal | Q-05 | KI-Einsatz und Quellen sind offengelegt. | Deklaration nach SoE-Richtlinie, Quellen nach IEEE. |

Das Team besteht aus sechs Personen, die mit macOS, Windows und Linux arbeiten. Q-06 lässt sich deshalb laufend im Team prüfen.

## 7. Weiterführende Ideen

Der Prototyp aus dem Semester belegt die Kernschleife aus Kampf, Bergung und Routenwahl. Überzeugt er, kann daraus ein Produkt werden, das sich verkaufen und über Jahre erweitern lässt. Eine Version 2.0 könnte so aussehen:

- **Zu zweit tauchen:** Eine zweite Taucherin kämpft im selben Boot mit, am selben Rechner oder online. Räume und Wächter passen sich an die Zahl der Spielenden an.
- **Neue Boote als Erweiterungen:** Ein Eisbrecher im Packeis, eine verlassene Tiefseestation oder ein Frachter voller Schmuggelware, jeweils mit eigenen Sektionen, Gegnern und Wächtern. Jede Erweiterung lässt sich als Zusatzinhalt verkaufen.
- **Die Werft:** Ein Editor, mit dem Spielende eigene Räume und ganze Boote bauen und über einen kurzen Code mit anderen teilen.
- **Saisonale Tauchgänge:** Jede Woche ein gemeinsames Boot für alle, mit Online-Bestenliste und Belohnungen für die Garderobe.
- **Die Geschichte der Besatzung:** Logbuchfragmente erzählen, warum das Boot gekapert wurde; je nach Route gibt es andere Enden.
- **Mehr Plattformen und Barrierefreiheit:** Gamepad-Steuerung, Handheld-PCs und später Konsolen, dazu frei belegbare Tasten und Farbschemata für Farbfehlsichtige.

## 8. Ressourcen

**Team und Fähigkeiten.** Das Team besteht aus Studierenden im dritten Semester des Bachelorstudiengangs Informatik. Alle Mitglieder bringen Java-Kenntnisse aus den vorangehenden Programmiermodulen sowie Grundlagen der Analyse und des Entwurfs aus dem parallel besuchten Modul Software-Entwicklung 1 mit. Die Rollen werden entlang der Pakete der Anwendung verteilt: Spiellogik, Darstellung, Oberfläche, Speicherung sowie Test und Qualität.

**Fehlendes Know-how.** Die Kursvorgaben verlangen Java mit einer selbst definierten, mindestens geschichteten Architektur und schliessen Frameworks und relationale Datenbanken aus; für die Oberfläche ist JavaFX die erste Wahl [6]. Ein verbreitetes Spiel-Framework wie libGDX [11] kommt deshalb nicht in Frage, und ABYSS baut auf der Client-Plattform JavaFX [12] eine eigene Spielschicht auf. Spielphysik und Kollisionserkennung, Echtzeitdarstellung, Spieldesign mit Balancing sowie die Durchführung von Nutzertests sind für das Team daher neu. Diese Themen werden früh im Prototyp erprobt und mit Literatur zu Entwurfsmustern für Spiele [13] erarbeitet.

**Bestehender Prototyp.** Ein technischer Prototyp liegt bereits vor. Er wurde mit Unterstützung generativer KI erstellt (vgl. Anhang) und belegt, dass die Kernmechaniken in Java und JavaFX umsetzbar sind. Im Semester erarbeitet sich das Team diesen Code, prüft und testet ihn und entwickelt ihn gezielt weiter. Dieser Einarbeitungsaufwand ist in der Schätzung berücksichtigt.

**Aufwand.** Gemäss Kursvorgabe stehen pro Person rund 120 Stunden bis zur Semesterwoche 13 zur Verfügung [6]. Bei sechs Teammitgliedern ergibt sich ein Rahmen von etwa 720 Stunden, der sich wie in Tabelle 3 verteilt.

*Tabelle 3: Grobe Aufwandsschätzung für das Semester bei sechs Teammitgliedern*

| Arbeitspaket | Anteil | Stunden |
|---|---|---|
| Projektmanagement, Reviews und Präsentationen | 15 % | 108 |
| Analyse und Anforderungen (Use Cases, Domänenmodell, Interviews) | 10 % | 72 |
| Einarbeitung in den Prototyp, Architektur und Design | 20 % | 144 |
| Implementation und Erweiterung | 30 % | 216 |
| Test, Spieltests und Balancing | 10 % | 72 |
| Technische Berichte und Dokumentation | 15 % | 108 |
| **Total** | **100 %** | **720** |

Innerhalb der 14 Semesterwochen sind damit ein vollständiger, getesteter Durchlauf durch alle vier Sektionen, die Berichte und Nutzertests mit Personen aus dem Umfeld realistisch. Die weiterführenden Ideen aus Kapitel 7 folgen erst nach dem Semester. An Sachmitteln werden nur die eigenen Rechner und kostenlose Werkzeuge (Git, Gradle, eine Java-Entwicklungsumgebung) benötigt. Die UI-Technologie JavaFX und allfällige Bibliotheken werden gemäss den Randbedingungen mit der Fachdozentin oder dem Fachdozenten abgesprochen.

## 9. Risiken

Tabelle 4 nennt die Risiken, die über das übliche Mass eines Semesterprojekts hinausgehen, mit Einschätzung und geplanter Massnahme. Abbildung 4 ordnet sie nach Eintrittswahrscheinlichkeit und Schadenspotenzial ein.

*Tabelle 4: Risiken mit Eintrittswahrscheinlichkeit, Schadenspotenzial und Massnahme*

| Nr. | Risiko | Wahrsch. | Schaden | Massnahme |
|---|---|---|---|---|
| R1 | Das Team versteht den KI-gestützt erstellten Prototyp nicht ausreichend und kann Entscheide nicht begründen. | hoch | hoch | Verantwortung pro Paket, Code-Reviews; jede Person erklärt ihr Paket im Review; Änderungen nur mit eigenem Test |
| R2 | Der Umfang wächst über das Semester hinaus. | mittel | hoch | Arbeitspakete nach Rang abarbeiten (vgl. Tabelle 7); neue Inhalte erst nach Erreichen der Iterationsziele |
| R3 | Steuerung und Fairness überzeugen die Testpersonen nicht. | mittel | hoch | ab Iteration 3 Spieltests mit drei bis fünf Personen, Anpassung nach Beobachtung |
| R4 | Die Dozierenden akzeptieren den KI-Einsatz nicht. | mittel | hoch | Freigabe im ersten Iterations-Review einholen, vollständige Deklaration nach SoE-Richtlinie |
| R5 | Die Echtzeitdarstellung ist auf schwächeren Rechnern zu langsam. | niedrig | mittel | geringe interne Auflösung, Messung der Zeichenzeit in jedem Build |
| R6 | Unter Windows oder Linux treten Probleme auf. | mittel | mittel | plattformneutraler Java-Code, Build mit Gradle, laufender Test auf den Rechnern des Teams |
| R7 | Teammitglieder fallen aus oder sind unterschiedlich verfügbar. | mittel | mittel | Wissen verteilen (Pair Programming), Aufgaben im Iterationsplan sichtbar machen |
| R8 | Spielstände werden beschädigt. | niedrig | mittel | Validierung, temporäres Schreiben, Sicherung der Originale, Integrationstests |

![Abbildung 4](abbildungen/abb-risikomatrix.png)

*Abbildung 4: Risikomatrix mit Eintrittswahrscheinlichkeit und Schadenspotenzial (eigene Darstellung)*

Die Risiken R1 und R4 hängen mit dem KI-gestützten Prototyp zusammen. Sie werden deshalb zuerst bearbeitet: Die Freigabe wird im ersten Iterations-Review eingeholt, die Einarbeitung ist das Hauptziel der Iteration 2 (vgl. Tabelle 5).

## 10. Grobplanung

Das Projekt folgt dem Kursprozess mit sechs Iterationen zu je zwei Wochen und drei Meilensteinen [14]. Tabelle 5 zeigt für jede Iteration das Ziel, die bearbeiteten Arbeitspakete und den Abschluss. Use Cases und Arbeitspakete sind in Kapitel 10.1 beschrieben.

*Tabelle 5: Grobplanung mit Iterationen, Zielen und Meilensteinen (SW = Semesterwoche)*

| Iteration | Zeitraum | Ziel | Arbeits­pakete | Abschluss |
|---|---|---|---|---|
| 1 | SW 2–3 | Idee, Anforderungen und Projektskizze; Prototyp läuft unter macOS, Windows und Linux | Übersicht | M1 Projektskizze, SW 3 (ab 28.09.2026) |
| 2 | SW 4–5 | Kernablauf verstehen und absichern: Bewegung, Kampf, erster Raum; Code-Walkthrough je Paket | 1, 2 | Review, SW 5 |
| 3 | SW 6–7 | Durchlauf mit Routenwahl, Bergung und Sicherung; erste Spieltests | 3, 4, 7 | Review, SW 7 |
| 4 | SW 8–9 | Architektur belegen: Domänenmodell, Design, Systemoperationen, Technischer Bericht I | 5, 6, 8, 9 | M2 Lösungsarchitektur, SW 9 (ab 09.11.2026) |
| 5 | SW 10–11 | Inhalte und dauerhafter Fortschritt, Balancing nach Spieltests | 10–13 | Review, SW 11 |
| 6 | SW 12–13 | Stabilisierung, Optionen, Technischer Bericht II, Abschlusspräsentation | 14 | M3 Prototyp, SW 13 (ab 07.12.2026) |

### 10.1. Use Cases

Abbildung 5 zeigt die Use Cases im Überblick. Einziger Akteur ist die spielende Person. Profil und Raum-Sicherung speichert das System selbst; das lokale Dateisystem gehört deshalb zum System und ist kein Akteur. Der wichtigste Use Case ist UC-01 «Tauchgang spielen», also eine ganze Runde vom Heck bis zur Brücke. Bergung, Handel, Unterbrechen und Fortsetzen, Sieg und Niederlage sind Varianten davon und werden als Erweiterungen von UC-01 beschrieben.

![Abbildung 5](abbildungen/abb-use-cases.png)

*Abbildung 5: Use-Case-Diagramm von ABYSS (UML) mit dem Kernfall UC-01 und seinen Varianten*

*Tabelle 6: Use Cases mit Kurzbeschreibung, relativem Rang und Abhängigkeiten*

| ID | Use Case | Kurzbeschreibung | Rang | Abhängig von |
|---|---|---|---|---|
| UC-01 | Tauchgang spielen | Ausrüstung wählen, Raum für Raum kämpfen, Belohnungen und Route wählen, bis zur Brücke oder bis zur Niederlage (Kernfall) | 1 | – |
| UC-02 | Fortschritt freischalten und Figur gestalten | Datenkerne im Archiv gegen Figuren, Waffen und Module eintauschen, Logbuch-Ziele ansehen, Aussehen in der Garderobe wählen | 2 | UC-01 (Datenkerne entstehen in Tauchgängen) |
| UC-03 | Spiel einstellen | Lautstärke, Bildschirmmodus und Effekte anpassen, Steuerung ansehen | 3 | – |

Muss- und Soll-Prioritäten legt das Team am Projektanfang bewusst noch nicht fest. Stattdessen sind die Use Cases und ihre Varianten relativ zueinander priorisiert: Rang 1 ist am wichtigsten und wird zuerst abgesichert. Die Varianten eignen sich als Arbeitspakete; Tabelle 7 und Abbildung 6 zeigen ihren Rang und ihre Abhängigkeiten. Die Rangfolge folgt dem Kundennutzen: zuerst kommt, was den Spielspass eines Tauchgangs trägt.

*Tabelle 7: Arbeitspakete mit relativem Rang und Voraussetzungen*

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

![Abbildung 6](abbildungen/abb-arbeitspakete.png)

*Abbildung 6: Arbeitspakete mit Rang und Abhängigkeiten; ein Pfeil zeigt von der Voraussetzung zum abhängigen Paket (eigene Darstellung)*

## 11. Quellenverzeichnis

[1] J. Harris, *Exploring Roguelike Games*. Boca Raton, FL, USA: CRC Press, 2020.

[2] C. Kerr, “Dead Cells has topped 10 million sales worldwide,” *Game Developer*, Jun. 5, 2023. [Online]. Available: https://www.gamedeveloper.com/business/dead-cells-has-topped-10-million-sales-worldwide. [Accessed: Sep. 26, 2026].

[3] Supergiant Games, “Hades has now sold more than 1,000,000 copies,” X (formerly Twitter), Sep. 20, 2020. [Online]. Available: https://x.com/SupergiantGames/status/1307744738552938496. [Accessed: Sep. 26, 2026].

[4] Subset Games, *FTL: Faster Than Light*. [Computer game]. Subset Games, 2012. [Online]. Available: https://store.steampowered.com/app/212680/. [Accessed: Sep. 26, 2026].

[5] FakeFish and Undertow Games, *Barotrauma*. [Computer game]. Daedalic Entertainment, 2023. [Online]. Available: https://barotraumagame.com/. [Accessed: Sep. 26, 2026].

[6] ZHAW School of Engineering, “Einführung und Kick-off,” Folien Software-Projekt 3 (PM3), Ausgabe HS26, Winterthur, 2026.

[7] J. Bong, *Snowpiercer*. [Film]. CJ Entertainment, 2013.

[8] Motion Twin, *Dead Cells*. [Computer game]. Motion Twin, 2018.

[9] Supergiant Games, *Hades*. [Computer game]. Supergiant Games, 2020.

[10] ZHAW School of Engineering, “LE03 – Anforderungsanalyse II,” Zusammenfassung Software-Entwicklung 1 (SWEN1), Ausgabe HS26, Winterthur, 2026.

[11] libGDX, “libGDX – a cross-platform Java game development framework based on OpenGL (ES),” libgdx.com. [Online]. Available: https://libgdx.com/. [Accessed: Sep. 26, 2026].

[12] OpenJFX, “JavaFX – an open source, next generation client application platform,” openjfx.io. [Online]. Available: https://openjfx.io/. [Accessed: Sep. 26, 2026].

[13] R. Nystrom, *Game Programming Patterns*. Genever Benning, 2014. [Online]. Available: https://gameprogrammingpatterns.com/. [Accessed: Sep. 26, 2026].

[14] ZHAW School of Engineering, “Projektmanagement,” Folien zum Kick-off Software-Projekt 3 (PM3), Winterthur, 2026.

[15] ZHAW School of Engineering, “Anhang zur ZHAW-Richtlinie «KI bei Leistungsnachweisen»: Deklarationspflicht von generativer KI bei Arbeiten an der SoE,” Version 1.0.0, Winterthur, Feb. 2024.

[16] ZHAW School of Engineering, “Rahmenbedingungen zum Einsatz von KI im PM3,” Version 1.0, Kursunterlagen HS26, Winterthur, 2026.

## 13. Glossar

- **Arbeitspaket:** Abgegrenzter Teil der Arbeit mit Rang und Voraussetzungen; hier aus den Varianten der Use Cases abgeleitet.
- **Bergung:** Auswahl von Belohnungen nach einem gesicherten Raum; es wird eines von mehreren Angeboten übernommen.
- **Datenkerne:** Dauerhafte Währung, die über Tauchgänge hinweg erhalten bleibt und im Archiv gegen Freischaltungen eingetauscht wird.
- **FURPS+:** Checkliste für weitere Anforderungen: Functionality, Usability, Reliability, Performance, Supportability, ergänzt um Randbedingungen wie Implementation und Legal.
- **Iteration:** Zeitabschnitt von zwei Wochen, in dem ein überprüfbares Teilergebnis entsteht.
- **Kontextszenario:** Erzählende Beschreibung, wie eine Person die fertige Anwendung in einer typischen Situation nutzt.
- **Leben:** Lebenspunkte der Spielfigur; bei null endet der Tauchgang mit einer Niederlage.
- **Modul:** Ausrüstungsgegenstand, der Werte oder Fähigkeiten der Spielfigur verändert; aktive Module werden gezielt ausgelöst.
- **Persona:** Fiktive, aber begründete Beschreibung einer typischen Person der Zielgruppe.
- **Raumtechnik:** Anlagen in einem Raum, etwa Pressen oder Lasergitter, die Spielfigur und Gegner gleichermassen betreffen.
- **Raumzustand:** Besondere Regel für einen Raum, etwa Stromausfall oder Hüllenbruch, die vor dem Betreten angekündigt wird.
- **Resonanz:** Zusatzbonus, der entsteht, wenn zwei bestimmte Module im selben Tauchgang installiert sind.
- **Roguelite:** Spielgenre mit zufällig zusammengestellten Durchläufen, Neubeginn nach einer Niederlage und teilweise dauerhaftem Fortschritt.
- **Seed:** Startwert des Zufallsgenerators; derselbe Seed erzeugt dieselbe Abfolge von Räumen.
- **Tauchgang:** Ein vollständiger Durchlauf vom Heck bis zur Brücke oder bis zur Niederlage.
- **Use Case:** Anwendungsfall: Beschreibung einer Interaktion zwischen einer Akteurin und dem System mit einem bestimmten Ziel.
- **Wächter:** Besonders starker Gegner am Ende jeder Sektion.

## 14. Anhang: Deklaration des KI-Einsatzes

Generative KI-Systeme kamen in mehreren Phasen dieses Projekts zum Einsatz [15]. OpenAI Codex wurde für den ersten spielbaren Prototyp verwendet, OpenAI Imagegen für Konzeptbilder in der Ideenphase. Anthropic Claude (Claude Code) wurde für die Weiterentwicklung des Prototyps sowie für den Entwurf dieser Projektskizze, ihrer Abbildungen und der Präsentationsfolien eingesetzt, ebenso für die Überarbeitung nach dem Feedback des Fachdozenten. Die Verantwortung für Inhalt, Richtigkeit und Quellen liegt beim Team. Tabelle 8 dokumentiert den Einsatz gemäss den Rahmenbedingungen des Moduls [16].

*Tabelle 8: Einsatz generativer KI im Projekt*

| KI | Ziel der Verwendung | Aufwand für den Prompt | Resultat verwendet | Art der Verwendung |
|---|---|---|---|---|
| OpenAI Codex | Erster spielbarer Prototyp (Version 0.1/0.2), Tests, Figurenmodelle, Klänge | Hoch: langer Auftrag mit vielen Prüfschritten | Ja | Weitgehend übernommen; ab Version 1.0 grösstenteils ersetzt |
| OpenAI Imagegen | Konzeptbilder für frühe Präsentationen | Mittel | Ja | Nur in der Konzeptphase, nicht im Spiel |
| Anthropic Claude (Claude Code) | Prototyp 1.0/1.1: Spiellogik, Pixelgrafik, Oberfläche, Tests | Hoch: autonomer Auftrag mit Prüfschleifen | Ja | Weitgehend übernommen; wird im Semester vom Team geprüft, erklärt und weiterentwickelt |
| Anthropic Claude (Claude Code) | Entwurf und Überarbeitung dieser Projektskizze, Abbildungen und Präsentationsfolien, Quellenrecherche | Mittel | Ja | Entwurf; Inhalte, Annahmen und Quellen werden vom Team geprüft und verantwortet |
