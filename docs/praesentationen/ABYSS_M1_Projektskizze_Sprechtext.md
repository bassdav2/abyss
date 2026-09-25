# Sprechtext · M1 Projektskizze

Pitch für die Geschäftsleitung nach dem PM3-Auftrag „Projektskizze (M1)“: etwa 10 Minuten plus 5 Minuten Fragen. Inhalt und Reihenfolge folgen den neun Kapiteln der Projektskizze.

Geplante Redezeit: etwa 10 Minuten, 12 Folien. PDF: [ABYSS_M1_Projektskizze.pdf](ABYSS_M1_Projektskizze.pdf).

## 01 · Titel (≈ 0.5 min)

- Team vorstellen: Namen, Rollen (wird vom Team ergänzt).
- Ein Satz zur Idee: „ABYSS ist ein Action-Roguelite: Man kämpft sich im Inneren eines U-Boots vom Heck bis zur Brücke vor.“
- Ziel der Präsentation: Die Geschäftsleitung soll entscheiden, ob das Projekt in dieser Form umgesetzt wird.

## 02 · Warum dieses Projekt? (≈ 1 min)

- Rahmen: PM3 verlangt ein Semesterprojekt in Java mit eigener Architektur, JavaFX als Oberfläche.
- Beobachtung: Viele spielen in kurzen Pausen. Roguelites passen dazu, weil jeder Versuch in sich abgeschlossen ist.
- Lücke: Spiele mit einem Fahrzeug als Welt steuern das Fahrzeug. Einen Kampf durch das Innere eines einzigen Schiffs gibt es kaum.
- Da es keinen Kunden gibt, arbeiten wir mit einer Persona und prüfen sie mit Testspielenden.

## 03 · Vom Heck bis zur Brücke (≈ 1 min)

- Das U-Boot ist die ganze Welt. Man startet ganz hinten und will nach vorne auf die Brücke.
- Ein Durchlauf heisst Tauchgang: 24 Räume in vier Sektionen, jede endet mit einem Wächter.
- Inspiration Snowpiercer: soziale und räumliche Hierarchie von hinten nach vorne. Figuren und Handlung sind eigenständig.
- Gelöstes Problem: abgeschlossene, abwechslungsreiche Sitzungen mit verständlichen Entscheidungen.

## 04 · Für wen und wozu? (≈ 1 min)

- Persona Lina ist eine Annahme und wird im Semester mit echten Testpersonen überprüft.
- Vier Nutzenversprechen: kein Einstiegshindernis, jederzeit unterbrechbar, faire und verständliche Niederlagen, jeder Versuch anders.
- Beispiel „faire Niederlage“: Jeder Gegner leuchtet vor seinem Angriff auf. So sieht Lina, was sie falsch gemacht hat.

## 05 · Stand der Technik (≈ 0.75 min)

- Vier bekannte Spiele decken jeweils einen Teil der Idee ab. Quellen siehe Projektskizze [1]–[4].
- Keines verbindet die lineare Reise durch ein Fahrzeug mit Roguelite-Durchläufen.
- Als Studienprojekt konkurrieren wir nicht kommerziell. Die Tabelle grenzt den Umfang ab.

## 06 · Eine halbe Stunde mit Lina (≈ 1.25 min)

- Lina hat nach der Vorlesung 30 Minuten und startet einen Tauchgang.
- Sie wählt eine Taucherin, lernt im ersten Raum auszuweichen und wählt nach dem Sieg ein Modul.
- An der Abzweigung wählt sie wegen tiefer Integrität das sichere Depot statt des Frachtraums.
- Sie nutzt eine Hydraulikpresse gegen einen gepanzerten Gegner: Die Umgebung wird zum Werkzeug.
- Gegen den Wächter verliert sie knapp. Sie versteht warum, behält Datenkerne und schaltet damit Neues frei. Ergebnis: Sie will den nächsten Versuch.

## 07 · Was muss das Spiel können? (≈ 0.75 min)

- Links die Kernfunktionen, Details in ANFORDERUNGEN.md (F-01 bis F-15).
- Rechts die Qualitätsanforderungen mit Messgrösse. So können wir am Ende prüfen, ob wir sie erfüllt haben.
- Q-04 wird mit Testpersonen gemessen: Erkennen sie einen angekündigten Angriff ohne Erklärung?
- Ideen für später sind bewusst getrennt, damit der Umfang realistisch bleibt.

## 08 · Womit bauen wir es? (≈ 0.75 min)

- Technik laut Kursvorgabe: Java, JavaFX, keine fremden Frameworks oder Engines.
- Neu für uns: Physik, Kollisionen, Echtzeit-Darstellung und Balancing. Deshalb kommen sie zuerst in den Prototyp.
- Klare Grenzen: kein Mehrspieler, nichts Online. Teamfähigkeiten und Stunden pro Woche ergänzt das Team.

## 09 · Was kann schiefgehen? (≈ 0.75 min)

- Grösstes Risiko: Der Prototyp ist stark KI-unterstützt entstanden. Das Team muss den Code verstehen und begründen können. Deshalb gibt es pro Paket eine verantwortliche Person und Reviews.
- Die Freigabe von Idee und Technik holen wir früh bei den Dozierenden ein.
- Spielgefühl ist schwer messbar: Wir beobachten Testpersonen, statt nur zu raten.

## 10 · Meilensteine und Iterationen (≈ 1 min)

- Drei Meilensteine laut HS26-Plan: M1 in SW3, M2 in SW9, M3 in SW13.
- Iteration 1 prüft das Spielgefühl, Iteration 2 einen kurzen vollständigen Durchlauf, Iteration 3 Inhalt und Spieltests.
- Der Kernfall ist „zum nächsten Raum vordringen“. Um ihn herum sind alle anderen Use Cases angeordnet.
- Offen gesagt: Ein Prototyp existiert schon. Wir nutzen die Zeit, um ihn zu verstehen, zu testen und zu verbessern.

## 11 · Was schon läuft (≈ 1 min)

- Kurze Live-Demo (1–2 Minuten): Titel, Schleuse, erster Raum, Bergung, Routenwahl.
- Zahlen des Prototyps 1.1: 24 Räume, 4 Wächter, 12 Gegnerarten, 41 passive Module, 5 Klassen, 106 automatische Tests.
- Transparenz: Der Prototyp ist mit KI-Unterstützung entstanden (Codex, Claude Code). Siehe KI_EINSATZ.md.

## 12 · Fragen (≈ 0.25 min)

- Kurz zusammenfassen: Idee, Nutzen für Lina, Alleinstellung, Plan.
- Um die Freigabe bitten und Fragen beantworten.
