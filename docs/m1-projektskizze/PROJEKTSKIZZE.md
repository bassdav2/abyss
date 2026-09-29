# Projektskizze ABYSS – Vom Heck bis zur Brücke

*PM3 HS26 · Meilenstein M1 · Team ABYSS · Abgabe 29. September 2026. Lesefassung; massgeblich ist [ABYSS_Projektskizze_M1.pdf](ABYSS_Projektskizze_M1.pdf) (Word: [ABYSS_Projektskizze_M1.docx](ABYSS_Projektskizze_M1.docx)). Erzeugt mit `tools/create_projektskizze.py`.*

## 1. Ausgangslage

Kurze, in sich abgeschlossene Spielsitzungen sind ein verbreitetes Muster bei Computerspielen für Desktop-Rechner. Ein Genre, das darauf aufbaut, sind sogenannte **Roguelites**: Jeder Durchlauf wird zufällig zusammengestellt, endet mit Sieg oder Niederlage und beginnt danach von vorn, wobei ein Teil des Fortschritts über die Versuche hinweg erhalten bleibt [1]. Wie gross die Nachfrage nach solchen Spielen ist, zeigen zwei Beispiele: *Dead Cells* wurde nach Angaben der Entwickler mehr als zehn Millionen Mal verkauft [2], *Hades* überschritt kurz nach dem Erscheinen der Vollversion eine Million verkaufte Exemplare [3].

Eine zweite Gruppe von Spielen nutzt ein einzelnes Fahrzeug als Spielwelt. In *FTL: Faster Than Light* führen Spielende ein Raumschiff durch eine zufällig erzeugte Galaxie [4], in *Barotrauma* bedient eine Mannschaft gemeinsam ein U-Boot [5]. Im Mittelpunkt steht dort die Steuerung des Fahrzeugs und seiner Systeme, nicht der Weg einer einzelnen Figur durch das Innere des Fahrzeugs.

Das Projektteam entwickelt im Modul Software-Projekt 3 (PM3) innerhalb eines Semesters eine Anwendung in Java mit selbst definierter, mindestens geschichteter Architektur; Frameworks und relationale Datenbanken sind ausgeschlossen [6]. Einen externen Auftraggeber gibt es nicht. Die Zielgruppe wird deshalb über eine Persona beschrieben, deren Annahmen im Projektverlauf mit Testpersonen überprüft werden (vgl. Kapitel 3).

## 2. Idee

ABYSS ist ein zweidimensionales Action-Roguelite, das vollständig im Inneren eines sehr langen U-Boots spielt. Die Spielfigur gehört zur Besatzung und beginnt im Heck. Raum für Raum kämpft sie sich bis zur Brücke vor, um die Kontrolle über das Boot zu übernehmen. Die räumliche Ordnung – hinten enge Wartungsräume, vorne die Schiffsführung – ist vom Film *Snowpiercer* inspiriert [7]; Figuren, Namen und Handlung sind eigenständig.

Wie Abbildung 1 zeigt, ist das Boot in vier Sektionen mit je sechs Räumen gegliedert. Jede Sektion endet mit einem Wächter, einem besonders starken Gegner. Vor jedem Raum wählen Spielende an einer Abzweigung zwischen zwei Wegen, etwa zwischen einem Kampfraum mit höherer Belohnung und einem ruhigeren Versorgungsraum. Ein Durchlauf, im Spiel «Tauchgang» genannt, ist auf eine Sitzung von etwa 30 Minuten ausgelegt; diese Dauer ist eine Annahme und wird in Spieltests überprüft.

Die Idee geht von folgendem Problem aus: Spielende mit wenig Zeit suchen Spiele, die innerhalb einer Sitzung einen vollständigen Spannungsbogen bieten und deren Niederlagen nachvollziehbar sind. Der Kernnutzen von ABYSS ist deshalb ein abgeschlossener, fairer Durchlauf pro Sitzung, der sich bei jedem Versuch anders spielt, weil Route, Räume, Gegner und Belohnungen neu zusammengestellt werden.

![Abbildung 1](abbildungen/abb-boot.png)

*Abbildung 1: Aufbau des U-Boots: 24 Räume in vier Sektionen vom Heck bis zur Brücke; jede Sektion endet mit einem Wächter (Bootskarte im Prototyp, eigene Beschriftung)*

Eine Bildschirmansicht des bestehenden Prototyps zeigt Abbildung 2. Die nummerierten Markierungen bezeichnen (1) Integrität, Energie und Reparatursets der Spielfigur, (2) die aktuelle Angriffswelle, (3) die Raumposition 15 von 24 sowie gesammelten Schrott und Datenkerne, (4) die Spielfigur mit Stirnlampe, (5) die rote Markierung, mit der ein Gegner seinen Angriff ankündigt, (6) Waffe, aktives Modul und Ausweichen sowie (7) die installierten Module. Alle Grafiken werden im Programmcode als Pixelgrafik mit 480 × 270 Bildpunkten erzeugt und für die Anzeige ganzzahlig vergrössert.

![Abbildung 2](abbildungen/abb-bildschirm.png)

*Abbildung 2: Bildschirmansicht des Prototyps im Forschungsdeck mit nummerierten Bedienelementen*

## 3. Kundennutzen

Die Hauptnutzenden sind Einzelspielende, die am eigenen Laptop oder Desktop-Rechner in kurzen Pausen spielen. Stellvertretend für diese Gruppe steht die folgende Persona. Sie ist eine Annahme des Teams; ihre Merkmale werden in Interviews und Spieltests überprüft.

> **Persona Lina (22)** studiert und spielt am Laptop zwischen Vorlesungen oder am Abend, meist 20 bis 30 Minuten am Stück. Sie mag kurze Actionspiele und abwechslungsreiche Ausrüstung. Niederlagen akzeptiert sie, wenn sie deren Ursache erkennt. Lange Einführungen und einen Zwang zur Internetverbindung lehnt sie ab.

Für Lina und vergleichbare Spielende ergibt sich folgender Nutzen:

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

Aus Tabelle 1 geht hervor, dass keines der Vergleichsspiele den Plattformkampf einer einzelnen Figur mit einer linearen Reise durch das Innere eines Fahrzeugs verbindet. ABYSS setzt an dieser Stelle an: Das U-Boot ist zugleich Spielwelt und Fortschrittsanzeige, und seine Technik – Förderbänder, Pressen, Lasergitter und Notschalter – wird Teil des Kampfes. Als Studienprojekt tritt ABYSS nicht in kommerzielle Konkurrenz zu diesen Spielen; die Einordnung dient der Abgrenzung des Umfangs.

Technisch werden Spiele in Java häufig mit einem Spiel-Framework wie libGDX umgesetzt, das Grafik, Eingabe und Audio auf Basis von OpenGL für mehrere Plattformen bereitstellt [10]. Ein solches Framework kommt für ABYSS nicht in Frage, weil die Randbedingungen des Moduls Frameworks ausschliessen und JavaFX als erste Wahl für die Oberfläche nennen [6]. ABYSS verwendet deshalb die Client-Plattform JavaFX [11] und eine eigene Spielschicht. Spielregeln, Darstellung und Speicherung werden als getrennte Schichten selbst entworfen, getestet und im Technischen Bericht begründet. Bewährte Entwurfsmuster für Spielschleife und Zustandsautomaten sind in der Literatur beschrieben [12].

## 5. Hauptablauf (Kontextszenario)

Lina hat nach einer Vorlesung eine halbe Stunde Zeit. Sie startet das Spiel auf ihrem Laptop und beginnt einen neuen Tauchgang. In der Schleuse wählt sie eine von mehreren Figuren; eine kurze Beschreibung nennt jeweils deren Stärken und Schwächen. Sie entscheidet sich für die Mechanikerin, weil deren Werte ausgewogen sind.

Der erste Raum im Heck ist ruhig und zeigt, wie sich die Figur bewegt, springt und ausweicht. Im zweiten Raum trifft Lina auf eine Patrouille. Ein Gegner leuchtet kurz rot auf, bevor er zuschlägt; Lina weicht aus und besiegt ihn. Sobald alle Gegner besiegt sind, öffnet sich das Schott zum nächsten Raum, und in einer Bergungskapsel werden drei Module angeboten. Lina wählt eines, das ihren Angriff verstärkt.

An der nächsten Abzweigung zeigen zwei Bildschirme, was hinter den Schotts liegt: ein Frachtraum mit zwei Gegnerwellen und ein Depot mit Vorräten. Da die Integrität ihrer Figur gesunken ist, wählt sie das Depot und repariert ihre Ausrüstung. Einige Räume später arbeitet in einem Maschinenraum eine Hydraulikpresse. Lina lockt einen gepanzerten Gegner unter die Presse und nutzt die Anlage so zu ihrem Vorteil.

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

Tabelle 2 fasst die nicht-funktionalen Anforderungen zusammen. Zu jeder Anforderung ist ein Kriterium angegeben, mit dem sich am Projektende prüfen lässt, ob sie erfüllt ist.

*Tabelle 2: Nicht-funktionale Anforderungen mit Prüfkriterium*

| ID | Anforderung | Prüfkriterium |
|---|---|---|
| Q-01 | Testbarkeit: Die Spielregeln sind unabhängig von Oberfläche und Dateisystem. | Automatische Tests der Spiellogik laufen ohne Fenster. |
| Q-02 | Robustheit: Beschädigte oder veraltete Spielstände verhindern den Start nicht. | Mit einer defekten Datei startet das Spiel mit neuem Profil; das Original bleibt als Sicherung erhalten. |
| Q-03 | Performance: Die Darstellung läuft flüssig. | Die Berechnung eines Bildes dauert im Mittel unter 16 ms (60 Bilder pro Sekunde) auf einem aktuellen Laptop. |
| Q-04 | Bedienbarkeit: Angriffe sind erkennbar, der Einstieg gelingt ohne Anleitung. | Vier von fünf Testpersonen erkennen eine Angriffsankündigung ohne Erklärung und schliessen den ersten Raum ohne Hilfe ab. |
| Q-05 | Nachvollziehbarkeit: KI-Einsatz, Quellen und Tests sind dokumentiert. | Deklaration nach SoE-Richtlinie, Quellen nach IEEE, Testergebnisse im Repository. |
| Q-06 | Portabilität: Das Spiel läuft auf den Rechnern des Teams. | Start unter macOS, Windows und Linux auf allen Teamrechnern. |
| Q-07 | Datenschutz: Es werden keine Daten übertragen. | Kein Netzwerkzugriff; Spielstände liegen ausschliesslich lokal. |

## 7. Weiterführende Ideen

Über den Semesterumfang hinaus kann sich die Anwendung in folgende Richtungen entwickeln:

- Unterstützung von Gamepads, wofür eine zusätzliche Bibliothek nötig ist, deren Einsatz mit den Dozierenden abzusprechen wäre
- frei belegbare Tasten und weitere Optionen zur Barrierefreiheit, etwa Farbschemata für Farbfehlsichtige
- weitere Sektionen, Gegner und Wächter sowie Story-Fragmente mit alternativen Enden
- eine Bestenliste für den täglichen Tauchgang mit gemeinsamem Startwert, die eine Serverkomponente erfordert
- eine Veröffentlichung auf einer Vertriebsplattform nach Abschluss des Semesters

## 8. Ressourcen

**Team und Fähigkeiten.** Das Team besteht aus Studierenden im dritten Semester des Bachelorstudiengangs Informatik. Alle Mitglieder bringen Java-Kenntnisse aus den vorangehenden Programmiermodulen sowie Grundlagen der Analyse und des Entwurfs aus dem parallel besuchten Modul Software-Entwicklung 1 mit. Die Rollen werden entlang der Pakete der Anwendung verteilt: Spiellogik, Darstellung, Oberfläche, Speicherung sowie Test und Qualität.

**Fehlendes Know-how.** Spielphysik und Kollisionserkennung, Echtzeitdarstellung, Spieldesign mit Balancing sowie die Durchführung von Nutzertests sind für das Team neu. Diese Themen werden früh im Prototyp erprobt und mit Literatur zu Entwurfsmustern für Spiele [12] erarbeitet.

**Bestehender Prototyp.** Ein technischer Prototyp (Version 1.1) liegt bereits vor. Er wurde mit Unterstützung generativer KI erstellt (vgl. Anhang) und belegt, dass die Kernmechaniken in Java und JavaFX umsetzbar sind. Im Semester erarbeitet sich das Team diesen Code, prüft und testet ihn und entwickelt ihn gezielt weiter. Dieser Einarbeitungsaufwand ist in der Schätzung berücksichtigt.

**Aufwand.** Gemäss Kursvorgabe stehen pro Person rund 120 Stunden bis zur Semesterwoche 13 zur Verfügung [6]. Bei angenommen fünf Teammitgliedern ergibt sich ein Rahmen von etwa 600 Stunden, der sich wie in Tabelle 3 verteilt.

*Tabelle 3: Grobe Aufwandsschätzung für das Semester bei fünf Teammitgliedern*

| Arbeitspaket | Anteil | Stunden |
|---|---|---|
| Projektmanagement, Reviews und Präsentationen | 15 % | 90 |
| Analyse und Anforderungen (Use Cases, Domänenmodell, Interviews) | 10 % | 60 |
| Einarbeitung in den Prototyp, Architektur und Design | 20 % | 120 |
| Implementation und Erweiterung | 30 % | 180 |
| Test, Spieltests und Balancing | 10 % | 60 |
| Technische Berichte und Dokumentation | 15 % | 90 |
| **Total** | **100 %** | **600** |

Innerhalb der 14 Semesterwochen sind damit ein vollständiger, getesteter Durchlauf durch alle vier Sektionen, die Berichte und Nutzertests mit Personen aus dem Umfeld realistisch. Die weiterführenden Ideen aus Kapitel 7 sowie Tests auf weiteren Plattformen folgen erst nach dem Semester. An Sachmitteln werden nur die eigenen Rechner und kostenlose Werkzeuge (Git, Gradle, eine Java-Entwicklungsumgebung) benötigt. Die UI-Technologie JavaFX und allfällige Bibliotheken werden gemäss den Randbedingungen mit der Fachdozentin oder dem Fachdozenten abgesprochen.

## 9. Risiken

Tabelle 4 nennt die Risiken, die über das übliche Mass eines Semesterprojekts hinausgehen, mit Einschätzung und geplanter Massnahme.

*Tabelle 4: Risiken mit Eintrittswahrscheinlichkeit, Auswirkung und Massnahme*

| Nr. | Risiko | Wahrsch. | Auswirk. | Massnahme |
|---|---|---|---|---|
| R1 | Das Team versteht den KI-gestützt erstellten Prototyp nicht ausreichend und kann Entscheide nicht begründen. | hoch | hoch | Verantwortung pro Paket, Code-Reviews; jede Person erklärt ihr Paket im Review; Änderungen nur mit eigenem Test |
| R2 | Der Umfang wächst über das Semester hinaus. | mittel | hoch | Use Cases priorisieren; neue Inhalte erst nach Erreichen der Iterationsziele |
| R3 | Steuerung und Fairness überzeugen die Testpersonen nicht. | mittel | hoch | ab Iteration 3 Spieltests mit drei bis fünf Personen, Anpassung nach Beobachtung |
| R4 | Die Akzeptanz des KI-Einsatzes durch die Dozierenden ist unklar. | offen | hoch | frühe Abstimmung, vollständige Deklaration nach SoE-Richtlinie |
| R5 | Die Echtzeitdarstellung ist auf schwächeren Rechnern zu langsam. | niedrig | mittel | geringe interne Auflösung, Messung der Zeichenzeit in jedem Build |
| R6 | Unterschiedliche Betriebssysteme im Team führen zu Problemen. | mittel | mittel | plattformneutraler Java-Code, Build mit Gradle, Test auf allen Teamrechnern |
| R7 | Teammitglieder fallen aus oder sind unterschiedlich verfügbar. | mittel | mittel | Wissen verteilen (Pair Programming), Aufgaben im Iterationsplan sichtbar machen |

Die Risiken R1 und R4 hängen mit dem KI-gestützten Prototyp zusammen. Sie werden deshalb zuerst bearbeitet: Die Abstimmung erfolgt unmittelbar nach M1, die Einarbeitung ist das Hauptziel der Iteration 2 (vgl. Tabelle 5).

## 10. Grobplanung

Das Projekt folgt dem Kursprozess mit sechs Iterationen zu je zwei Wochen und drei Meilensteinen [13]. Tabelle 5 zeigt für jede Iteration das Ziel, die bearbeiteten Use Cases und den Abschluss. Die Use Cases sind in Kapitel 10.1 beschrieben.

*Tabelle 5: Grobplanung mit Iterationen, Zielen und Meilensteinen (SW = Semesterwoche)*

| Iteration | Zeitraum | Ziel | Use Cases | Abschluss |
|---|---|---|---|---|
| 1 | SW 2–3 | Idee, Anforderungen und Projektskizze; Prototyp läuft auf allen Teamrechnern | Übersicht | M1 Projektskizze, SW 3 (ab 28.09.2026) |
| 2 | SW 4–5 | Kernablauf verstehen und absichern: Bewegung, Kampf, erster Raum; Code-Walkthrough je Paket | UC-01, UC-02 | Review, SW 5 |
| 3 | SW 6–7 | Durchlauf mit Routenwahl, Bergung und Sicherung; erste Spieltests | UC-02, UC-03, UC-05 | Review, SW 7 |
| 4 | SW 8–9 | Architektur belegen: Domänenmodell, Design, Systemoperationen, Technischer Bericht I | UC-04, UC-06, UC-07 | M2 Lösungsarchitektur, SW 9 (ab 09.11.2026) |
| 5 | SW 10–11 | Inhalte und dauerhafter Fortschritt, Balancing nach Spieltests | UC-08, UC-09, UC-10 | Review, SW 11 |
| 6 | SW 12–13 | Stabilisierung, Optionen, Technischer Bericht II, Abschlusspräsentation | UC-11 | M3 Prototyp, SW 13 (ab 07.12.2026) |

### 10.1. Use Cases

Abbildung 4 zeigt die Use Cases im Überblick. Alle Anwendungsfälle werden von einer einzigen Akteurin, der spielenden Person, ausgelöst; das lokale Dateisystem ist nur beim Sichern, Fortsetzen und im Archiv beteiligt. Tabelle 6 beschreibt die Use Cases kurz und ordnet ihnen eine Priorität zu.

![Abbildung 4](abbildungen/abb-use-cases.png)

*Abbildung 4: Use-Case-Diagramm von ABYSS (UML); hervorgehoben ist der Kernfall UC-02*

*Tabelle 6: Use Cases mit Kurzbeschreibung und Priorität*

| ID | Use Case | Kurzbeschreibung | Priorität |
|---|---|---|---|
| UC-01 | Tauchgang vorbereiten | Figur, Waffe und Startbedingungen wählen, Tauchgang beginnen | Muss |
| UC-02 | Zum nächsten Raum vordringen | Raum betreten, Gegner besiegen, Raum sichern, Schott öffnen (Kernfall) | Muss |
| UC-03 | Bergung wählen | eines von mehreren angebotenen Modulen oder Waffen übernehmen | Muss |
| UC-04 | Handeln | Schrott bei Händlerin, Werkstatt oder Kapelle einsetzen | Muss |
| UC-05 | Unterbrechen und fortsetzen | pausieren, beenden und am Raumeingang weiterspielen | Muss |
| UC-06 | Brücke erobern | letzten Wächter besiegen und einen weiteren Zyklus beginnen | Muss |
| UC-07 | Erneut tauchen | nach einer Niederlage die Auswertung ansehen und neu beginnen | Muss |
| UC-08 | Im Archiv freischalten | Datenkerne gegen Figuren, Waffen und Module eintauschen | Soll |
| UC-09 | Aussehen anpassen | Farben und Helmform der Figur in der Garderobe wählen | Soll |
| UC-10 | Ausrüstung und Karte ansehen | installierte Module und den Weg durch das Boot anzeigen | Soll |
| UC-11 | Optionen einstellen | Lautstärke, Bildschirmmodus und Effekte anpassen | Muss |

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

[10] libGDX, “libGDX – a cross-platform Java game development framework based on OpenGL (ES),” libgdx.com. [Online]. Available: https://libgdx.com/. [Accessed: Sep. 26, 2026].

[11] OpenJFX, “JavaFX – an open source, next generation client application platform,” openjfx.io. [Online]. Available: https://openjfx.io/. [Accessed: Sep. 26, 2026].

[12] R. Nystrom, *Game Programming Patterns*. Genever Benning, 2014. [Online]. Available: https://gameprogrammingpatterns.com/. [Accessed: Sep. 26, 2026].

[13] ZHAW School of Engineering, “Projektmanagement,” Folien zum Kick-off Software-Projekt 3 (PM3), Winterthur, 2026.

[14] ZHAW School of Engineering, “Anhang zur ZHAW-Richtlinie «KI bei Leistungsnachweisen»: Deklarationspflicht von generativer KI bei Arbeiten an der SoE,” Version 1.0.0, Winterthur, Feb. 2024.

[15] ZHAW School of Engineering, “Rahmenbedingungen zum Einsatz von KI im PM3,” Version 1.0, Kursunterlagen HS26, Winterthur, 2026.

## 13. Glossar

- **Bergung:** Auswahl von Belohnungen nach einem gesicherten Raum; es wird eines von mehreren Angeboten übernommen.
- **Datenkerne:** Dauerhafte Währung, die über Tauchgänge hinweg erhalten bleibt und im Archiv gegen Freischaltungen eingetauscht wird.
- **Integrität:** Lebenspunkte der Spielfigur; bei null endet der Tauchgang mit einer Niederlage.
- **Iteration:** Zeitabschnitt von zwei Wochen, in dem ein überprüfbares Teilergebnis entsteht.
- **Kontextszenario:** Erzählende Beschreibung, wie eine Person die fertige Anwendung in einer typischen Situation nutzt.
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

Generative KI-Systeme kamen in mehreren Phasen dieses Projekts zum Einsatz [14]. OpenAI Codex wurde für den ersten spielbaren Prototyp verwendet, OpenAI Imagegen für Konzeptbilder in der Ideenphase. Anthropic Claude (Claude Code) wurde für die Weiterentwicklung des Prototyps zu Version 1.1 sowie für den Entwurf dieser Projektskizze, ihrer Abbildungen und der Präsentationsfolien eingesetzt. Die Verantwortung für Inhalt, Richtigkeit und Quellen liegt beim Team. Tabelle 7 dokumentiert den Einsatz gemäss den Rahmenbedingungen des Moduls [15].

*Tabelle 7: Einsatz generativer KI im Projekt*

| KI | Ziel der Verwendung | Aufwand für den Prompt | Resultat verwendet | Art der Verwendung |
|---|---|---|---|---|
| OpenAI Codex | Erster spielbarer Prototyp (Version 0.1/0.2), Tests, Figurenmodelle, Klänge | Hoch: langer Auftrag mit vielen Prüfschritten | Ja | Weitgehend übernommen; ab Version 1.0 grösstenteils ersetzt |
| OpenAI Imagegen | Konzeptbilder für frühe Präsentationen | Mittel | Ja | Nur in der Konzeptphase, nicht im Spiel |
| Anthropic Claude (Claude Code) | Prototyp 1.0/1.1: Spiellogik, Pixelgrafik, Oberfläche, Tests | Hoch: autonomer Auftrag mit Prüfschleifen | Ja | Weitgehend übernommen; wird im Semester vom Team geprüft, erklärt und weiterentwickelt |
| Anthropic Claude (Claude Code) | Entwurf dieser Projektskizze, Abbildungen und Präsentationsfolien, Quellenrecherche | Mittel | Ja | Entwurf; Inhalte, Annahmen und Quellen werden vom Team geprüft und verantwortet |
