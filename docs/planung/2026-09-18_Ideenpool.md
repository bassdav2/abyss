---
type: topic
module: Software-Projekt 3
status: draft
created: 2026-09-18
updated: 2026-09-18
---

# Kreative PM3-Projektideen

## 1. Abyss: U-Boot-Mission mit gekoppelten Systemen

Man steuert ein kleines Forschungs-U-Boot durch eine 2D-Unterwasserkarte, sammelt Proben und muss zur Oberfläche zurückkehren. Ballasttanks beeinflussen den Auftrieb; Antrieb und Pumpen verbrauchen Energie; Lecks fluten Abteilungen; geschlossene Schotten begrenzen Schäden, erschweren aber den Zugang.

**Interessanter Moment:** Ein Leck tritt auf. Mehr Pumpenleistung hilft gegen Wasser, reduziert aber die Energiereserve für den Rückweg. Ballast abwerfen verändert den Auftrieb. Der Spieler entscheidet zwischen Missionsziel und Rückkehr.

**PM3-Kern:** Getrennte Modelle für Abteilungen, Tanks, Energieverbraucher, Schäden, Aktionen, Simulationszeit und Missionszustand. Aus Eingaben entstehen Tiefe, Energieverlauf, Flutung und ein Missionsergebnis. Reproduzierbare Ereignisse und Modellregeln sind testbar.

**Begrenzung:** Ein Boot, eine kleine Karte, wenige Systeme und zwei oder drei Missionen; einfache 2D-Darstellung, keine vollständige Strömungsmechanik. Einen beherrschbaren Teil physikalisch modellieren, übrige Mechaniken ausdrücklich als Spielregeln definieren. Physik und Spielbalance nicht vermischen.

**Optional:** Automatischer Tiefenregler, ein zweiter Bootsentwurf, Wiederholung mit anderen Entscheidungen.

## 2. FloatLab: Boote bauen und scheitern lassen

Man baut einen vereinfachten Bootsrumpf aus wenigen Bauteilen, platziert Ladung und lässt die Konstruktion im virtuellen Wasser testen. Die App berechnet Gleichgewicht, Eintauchtiefe und bei entsprechendem Modell Neigung. Ein Leck verändert die Verteilung von Wasser und Gewicht.

**Interessanter Moment:** Dieselbe Ladung wird an anderer Stelle platziert. Zwei Entwürfe werden unter gleichen Bedingungen verglichen. Ein Fehler wird am Modell sichtbar, ohne reale Konstruktionen bauen zu müssen.

**PM3-Kern:** Rumpfgeometrie, Materialien, Lasten, Abteilungen, Kräfte und Experimentläufe. Die Rechenlogik liefert Ergebnisse und nachvollziehbare Begründungen. Ein einzelner Dichtevergleich wäre als Gesamtprojekt zu klein; Konstruktion, Belastungsfälle und Vergleich schaffen Substanz.

**Begrenzung:** Stark eingeschränkte Geometrie und ruhendes Wasser; zunächst statisches Gleichgewicht. Neigung und Flutung sind zusätzliche Modellierungsaufgaben. Keine allgemeine Schiffbau- oder Sicherheitssoftware.

**Einschätzung:** Sehr anschaulich und eigenständig, aber höheres Risiko bei mathematischer Modellierung als beim regelorientierten U-Boot-Spiel. Früh einen kleinen Rechenprototyp erstellen, bevor weitere Features zugesagt werden.

Physikalischer Anker: [OpenStax, Archimedes und Auftrieb](https://openstax.org/books/university-physics-volume-1/pages/14-4-archimedes-principle-and-buoyancy). Gewicht und Auftrieb erlauben ein begrenztes Modell; die Quelle belegt nicht die Vollständigkeit der vorgeschlagenen Simulation.

## 3. Maison: persönlicher Feinkost-Concierge

Die Eingabe ist ein Anlass: etwa sechs Gäste, ein Budget, gewünschter Stil, Ernährungswünsche und eine Servierzeit. Die App erzeugt Menüvarianten aus einem kuratierten Sortiment, berechnet Mengen und Kosten, berücksichtigt verfügbare Produkte sowie Zubereitungs-/Lieferfenster und erstellt einen Ablauf für den Abend.

**Interessanter Moment:** Ein Gast isst vegetarisch oder ein Hauptprodukt fällt aus. Das System stellt eine konsistente Alternative zusammen und erklärt die Änderungen. Varianten können nach Budget, Vielfalt oder geringem Vorbereitungsaufwand sortiert werden.

**PM3-Kern:** Menükomposition unter Bedingungen, Mengenberechnung, Produktalternativen, regelbasierte Kombinationen und Ablaufplanung. Zutaten-/Allergenangaben und Pairing-Regeln kommen aus einem definierten Demonstrationskatalog. Keine Behauptung einer medizinisch sicheren Empfehlung oder objektiv besten Geschmackskombination.

**Begrenzung:** Ein fiktiver Anbieter mit etwa 30–50 kuratierten Produkten; zwei Anlassarten; Bestellung/Lieferung im Prototyp simulieren. Eigene Regel- und Auswahlverfahren, kein LLM als alleiniger Entscheidungskern.

**Einordnung Moreira:** Der Anbieter führt Delikatessen und Wein und bietet bereits Heimlieferung an. Die Idee entsteht daher durch die zusätzliche Komposition eines ganzen Anlasses. Weder Kundenbedarf noch eine fehlende entsprechende interne Lösung sind damit bewiesen. [Offizielle Website](https://www.moreira-gourmet.ch/en).

## 4. Escape Architect: Escape-Room-Generator mit Lösbarkeitsprüfung

Man gibt Räume, Gegenstände, Schlösser, Mechaniken und eine Zielschwierigkeit vor. Die App baut daraus ein Rätsel oder prüft einen eigenen Entwurf. Sie findet Lösungsfolgen, erkennt unerreichbare Schlüssel und zeigt zu frühe Abkürzungen. Hinweise richten sich nach dem bisherigen Spielzustand.

**Interessanter Moment:** Ein Schlüssel liegt hinter genau der Tür, die er öffnen soll. Das System erkennt den unlösbaren Entwurf und schlägt eine Änderung vor. Danach generiert es einen ähnlichen, lösbaren Raum.

**PM3-Kern:** Zustandsmodell, Voraussetzungen und Effekte von Aktionen, Graphsuche, regelbasierte Erzeugung und Hinweise. Neue Ergebnisse sind spielbare Rätsel und nachgewiesene Lösungswege.

**Begrenzung:** Kleine Karten, begrenzte Gegenstandsmenge und feste Rätselmechaniken. Eine angezeigte Schwierigkeit ist eine definierte Heuristik, keine objektive Messung menschlicher Schwierigkeit.

**Einschätzung:** Besonders starkes Verhältnis von Originalität, eigenen Algorithmen und Testbarkeit; gute Alternative zur Physiksimulation.

## 5. WeatherLab: Wetter als Experiment

Zwei klar zu trennende Varianten:

- **Wetter-Sandbox:** vereinfachte Landschaft mit Meer, Berg und Stadt; Wind, Feuchtigkeit und Temperatur einstellen und die Entwicklung regelbasiert darstellen. Lehrmodell, keine reale Wetterprognose. Neu entstehen simulierte Verteilungen und Szenariovergleiche.
- **Begrenzte echte Vorhersage:** beispielsweise nur die Temperatur der nächsten Stunde an einer Messstation aus historischen Messreihen prognostizieren. Zwei selbst implementierte einfache Modelle gegen eine unveränderte Fortschreibung vergleichen. Training und spätere Testzeiträume sauber trennen. Datenverfügbarkeit müsste vor Zusage geprüft werden.

**Begrenzung:** Kein eigener umfassender Wetterdienst. Operative numerische Wettervorhersagen kombinieren umfangreiche Beobachtungen mit Atmosphärenmodellen; eine vergleichbare Qualität ist für diesen Projektumfang kein sinnvolles Versprechen. [NOAA NCEI](https://www.ncei.noaa.gov/products/weather-climate-models/numerical-weather-prediction).

**Einschätzung:** Spannend bei echtem Interesse am Modellieren. Höheres fachliches Validierungsrisiko; die Vorhersagevariante kann zudem stärker nach Datenanalyse als nach reichhaltigem OO-Design aussehen.

## 6. Biosphere: eine kleine geschlossene Welt

Ein künstliches Terrarium enthält wenige Pflanzen-/Tierarten, Wasser, Nahrung und Energie. Man verändert die Startbedingungen und versucht, ein stabiles System aufzubauen. Die Simulation erzeugt Ressourcen- und Populationsverläufe; Eingriffe können unerwartete Folgen auslösen.

**PM3-Kern:** Interagierende Akteure, Ressourcenzyklen, Zustandsänderungen und Strategien. Wiederholbare Simulationen erlauben Tests gegen definierte Modellannahmen.

**Begrenzung:** Drei Arten, wenige Ressourcen und transparente Spiel-/Modellregeln. Kein Anspruch auf eine validierte ökologische Prognose. Der Reiz liegt im Experimentieren und der Dynamik.

## Navigation

- [[modules/Software-Projekt-3/topics/PM3-Breiter-Ideenpool-und-Plattformwahl-2026-09-18|Weitere Projektideen und Plattformwahl]]
- [[modules/Software-Projekt-3/Software-Projekt-3-Hub]]
- [[modules/Software-Projekt-3/topics/PM3-Projektideen-und-oeffentliche-Beispiele]]
