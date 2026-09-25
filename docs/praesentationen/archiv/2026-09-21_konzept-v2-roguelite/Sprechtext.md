# Abyss v2 – Sprechtext

Acht Folien. Zielzeit ca. 4:15 Minuten, dazu Reserve für den Start. Die Notizen stehen auch direkt in der PowerPoint.

## 1. Die neue Spielidee · ca. 20 Sekunden

Abyss ist jetzt ein 2D-Roguelite im Inneren eines riesigen U-Boots. Wir beginnen ganz hinten und kämpfen uns bis zur Brücke vor. Die Inspiration ist diese räumliche Hierarchie: hinten enge Wartungsräume, vorne die Kontrolle über das ganze Schiff. Die Bilder zeigen unsere Konzeptidee, noch kein fertiges Spiel.

## 2. Was man spielt · ca. 30 Sekunden

Wir steuern eine einzelne Figur statt des U-Boots. Sie bewegt sich durch verbundene Räume, weicht Angriffen aus und setzt ein Werkzeug im Kampf ein. Gegner und Gefahren sollen ihre Aktionen erkennbar ankündigen. Zwischen Kämpfen gibt es Abzweigungen und sichere Räume. Der Weg zur Brücke bleibt das klare Ziel, aber Begegnungen und Beute ändern sich von Versuch zu Versuch.

## 3. Der Game Loop · ca. 35 Sekunden

Ein Versuch beginnt im Heck. Wir kombinieren vorgefertigte Räume zu einer neuen Route und variieren Gegner und Beute. Während des Runs passen wir die Ausrüstung an. Bei einer Niederlage gehen die aktuellen Gegenstände verloren. Entdeckte Baupläne bleiben als zusätzliche Startmöglichkeiten erhalten. Wer die Brücke erreicht, kann mit seinem Build einen schwierigeren Tauchzyklus beginnen. Das ermöglicht wiederholbare Durchläufe. Wir müssen dafür aber erreichbare Wege und faire Kombinationen sicherstellen.

## 4. Persona · ca. 25 Sekunden

Unsere vorläufige Persona ist Lina, 22 und Studentin. Sie möchte am Laptop etwa 20 bis 30 Minuten spielen. Sie mag kurze Action-Spiele und unterschiedliche Ausrüstung. Scheitern ist für sie okay, wenn sie die Ursache versteht und danach etwas anders ausprobieren kann. Sie braucht einen schnellen Einstieg und eine Pause bei Unterbrechungen. Diese Persona ist eine Annahme, die wir noch überprüfen.

## 5. Priorisierte Usability-Anforderungen · ca. 45 Sekunden

Vier Bereiche sind besonders wichtig. Lernförderlichkeit: Eine sichere erste Begegnung vermittelt Bewegung und Ausweichen. Selbstbeschreibungsfähigkeit: Lina muss ihren Zustand und die angebotenen Wege verstehen. Steuerbarkeit: Sie kann jederzeit pausieren und danach weiterspielen. Fehlertoleranz: Ein versehentlicher Abbruch wird abgefangen, weil sonst der ganze Run verloren geht. Bewusst gewählte Risiken bleiben Teil des Spiels. Aufgabenangemessenheit, Erwartungskonformität und Individualisierbarkeit ergänzen das: relevante Informationen bleiben sichtbar, die Bedienung konsistent, und Lautstärke sowie Effekte sind anpassbar.

## 6. Kontextszenario · ca. 35 Sekunden

Lina beginnt einen Run in ihrer Lernpause. Nach mehreren Räumen hat ihre Figur nur noch wenig Gesundheit. Jetzt kann sie eine Werkstatt aufsuchen oder ein bewachtes Depot mit zusätzlicher Beute riskieren. Sie erkennt beide Möglichkeiten und entscheidet sich für die Werkstatt. Dort stabilisiert sie ihre Figur und wählt einen defensiven Schildimpuls. Im nächsten Kampf erkennt sie einen angekündigten Angriff rechtzeitig, weicht aus und erreicht die nächste Sektion. Das vollständige Szenario beschreibt diesen Ablauf aus ihrer Sicht, ohne konkrete Oberflächenelemente vorzuschreiben.

## 7. Anforderungen ermitteln und prüfen · ca. 40 Sekunden

Wir haben keinen externen Auftraggeber. Deshalb beziehen wir mögliche Spielende aus unserem Umfeld ein, mit unterschiedlicher Erfahrung. Zuerst fragen wir nach ihren Gewohnheiten und typischen Frustmomenten. Danach lassen wir konkrete Aufgaben im Prototyp bearbeiten und beobachten sie beim lauten Denken. Als vorgeschlagenes Kriterium sollen vier von fünf Personen einen angekündigten Angriff erkennen und ihre Wahl zwischen zwei Wegen begründen können, ohne Hilfe von uns. Das ist ein Testziel. Wir haben diese Tests noch nicht durchgeführt. Aus den Beobachtungen überarbeiten wir Persona und Anforderungen.

## 8. Ziel und Wiederholung · ca. 25 Sekunden

An der Brücke wartet der Wächter als klarer Abschluss eines Runs. Danach können wir einen weiteren Tauchzyklus mit neuen Begegnungen und schwierigeren Bedingungen starten. Für die erste Version schlagen wir drei Sektionen und zwölf Räume pro Run vor. Den Umfang prüfen wir mit einem spielbaren Ausschnitt aus wenigen Räumen. Entscheidend ist, dass Kämpfe, Entscheidungen und der nächste Versuch verständlich und motivierend zusammenpassen.
