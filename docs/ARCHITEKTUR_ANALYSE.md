# Architektur- und Code-Analyse · Stand 13.10.2026

Anlass ist das Feedback im Iterations-Review vom 13.10.2026. Bewertet wird vor allem der technische Teil: eine saubere Schichtenarchitektur, ein korrektes Domänenmodell und Code, der Abstraktion, Patterns, Vererbung, hohe Kohäsion und geringe Kopplung zeigt. Die Oberfläche ist gut, steht bei der Bewertung aber nicht im Vordergrund. Das alte Architekturdiagramm zeigte zu viel UI und wurde deshalb neu gezeichnet ([03-architecture](diagrams/03-architecture.svg)).

Die Zahlen unten wurden am Code gemessen (`src/main/java`, Kommentare und Leerzeilen nicht mitgezählt). Die Grundsätze stammen aus SWEN1: GRASP (Information Expert, Creator, Controller, Low Coupling, High Cohesion, Polymorphism, Indirection, Protected Variations; GRASP-Übersicht, PDF-Seite 1) und die Schichtentrennung in Präsentation, Anwendung, Domäne und technische Dienste (V2 GUI-Architekturen, PDF-Seiten 5–8).

## 1 Was bereits gut ist

| Kriterium | Beleg im Code |
|---|---|
| Schichten, Abhängigkeiten nach innen | `domain` importiert keine andere Schicht, kein JavaFX, kein Dateisystem (über alle Importe geprüft). `application` hängt nur von `domain` und `ports` ab. |
| Strategie | `EnemyBehavior` mit 28 Implementierungen (23 Gegnerarten, 5 Wächter) |
| Schablonenmethode | `Brain.update` ist `final` und legt den Ablauf Annähern → Ausholen → Angriff → Erholung fest; Unterklassen füllen nur Einstiegspunkte |
| Fabrikmethode | `EnemyKind.behavior()` ordnet jeder Art ihr Verhalten zu |
| Vererbung | `Actor` ← `Player`, `Enemy`; `Brain` ← 28 Verhaltensklassen |
| Aggregat, Controller | `GameRun` ist die Konsistenzgrenze eines Tauchgangs, `GameService` nimmt die Systemoperationen entgegen |
| Port/Adapter | `GameRepository` (Port) und `FileGameRepository` (Adapter); die Domäne bleibt ohne Dateien testbar |
| Beobachter (Pull) | Darstellung und Audio lesen die `GameEvent`-Liste und verändern keine Regeln |
| Wertobjekte | Records wie `RoomPlan`, `RunCheckpoint`, `StatSheet`, `Offer` |
| Kapselung von Sammlungen | `GameRun` gibt Listen nur lesend heraus (`Collections.unmodifiableList`) |
| Globale Zustände | keine veränderlichen `static`-Felder |
| Tests | 204 automatische Tests, alle grün (Lauf vom 05.10.2026) |

## 2 Schwachstellen

| Nr. | Problem | Messung | Verletztes Prinzip |
|---|---|---|---|
| S1 | **Zu grosse Klassen.** `GameRun` erledigt zu viel selbst. | `GameRun` 1 078 Zeilen und 104 Methoden; `RoomGenerator` 880; in `ui`: `WorldRenderer` 1 309, `MenuScreens` 942, `RunScreens` 867. `Behaviors.java` enthält 23 Klassen in einer Datei. | High Cohesion |
| S2 | **Mitarbeiter kennen das ganze Aggregat.** Sieben Mitarbeiter (`Combat`, `PlayerMotor`, `Arsenal`, `Ballistics`, `Loot`, `Rewards`, `Machinery`) bekommen `GameRun` im Konstruktor und greifen direkt auf seine paketinternen Felder zu. | z. B. 35 Zugriffe aus `Behaviors`, 33 aus `Arsenal`, 19 aus `Machinery` | Low Coupling, Indirection |
| S3 | **Offene Felder in Entitäten.** Andere Klassen setzen Zustand direkt, statt Methoden aufzurufen. | 81 paketinterne, veränderliche Felder in `domain` (`Player` 26, `Enemy` 17, `Projectile` 13, `Actor` 8); 69 direkte Zuweisungen wie `e.vx = …` oder `e.state = …` in `Brain`, `Behaviors`, `Combat` | Information Expert, Kapselung |
| S4 | **Fallunterscheidungen nach Typ** statt Polymorphie | 53 `switch (kind)`, davon in `domain`: `RoomGenerator` 10, `Hazard` 4, `Fixture` 4 | Polymorphism, Protected Variations |
| S5 | **Port im falschen Paket.** `ports.GameRepository` importiert `application.Profile`. Damit hängt `ports` von der Anwendung ab, statt zu ihr zu gehören. | 1 Datei | Dependency Inversion |
| S6 | **Kein eigener Startpunkt.** Die Präsentation erzeugt die Adapter selbst: `AbyssApplication` das `FileGameRepository`, `GameWindow` das `AudioSystem`. | 2 Importe von `ui` nach `infrastructure` | Schichtentrennung |
| S7 | **Architekturregeln nur von Hand geprüft.** Kein Test verhindert, dass jemand eine verbotene Abhängigkeit einbaut. | – | Protected Variations |

Die Präsentation ruft Domänenobjekte auch direkt auf, um Zustand zu lesen. Das ist eine zulässige, lockere Schichtung, solange sie dokumentiert ist und Änderungen nur über `GameService` und `GameRun` laufen.

## 3 Refactoring-Plan

Die Arbeit verteilt sich auf die Pakete aus der [Paketverantwortung](planung/Paketverantwortung.md). Jede Änderung braucht einen eigenen Test, eine zweite Person liest mit, und die 204 bestehenden Tests müssen grün bleiben.

| Prio | Aufgabe | Löst | Paket | Ziel, messbar |
|---|---|---|---|---|
| 1 | `ArchitectureTest` in JUnit: liest die Importe aller Klassen und prüft die Schichtregeln, ohne Zusatzbibliothek | S7 | D | Test schlägt fehl, wenn `domain` eine andere Schicht oder JavaFX importiert |
| 1 | `GameRepository` nach `application` verschieben, Paket `ports` auflösen | S5 | D | `application` hat keine Abhängigkeit mehr nach aussen |
| 1 | Startpunkt (Composition Root) in `Launcher` bzw. eigenes Paket `bootstrap`: dort Adapter erzeugen und an die Präsentation übergeben | S6 | E | `ui` importiert nichts aus `infrastructure` |
| 1 | `Enemy` und `Player` kapseln: Felder `private`, Zustandswechsel über sprechende Methoden (`enterState`, `stun`, `applyKnockback` …) | S3 | B, A | 0 nicht-private veränderliche Felder in `Enemy` und `Player` |
| 2 | `GameRun` aufteilen: Raumablauf (`take`, `interaction`, `chooseNextRoom`) und Wellensteuerung in eigene Klassen | S1 | C, A | `GameRun` unter 600 Zeilen, Mitarbeiter je eine Verantwortung |
| 2 | Mitarbeiter von `GameRun` entkoppeln: schmale Schnittstellen statt des ganzen Aggregats (z. B. `CombatContext` für `Combat`) | S2 | A, B | Kein Mitarbeiter greift mehr direkt auf Felder von `GameRun` zu |
| 2 | `Behaviors.java` in einzelne Dateien und ein Unterpaket `domain.enemy` aufteilen | S1 | B | eine Klasse pro Datei |
| 2 | Typ-Fallunterscheidungen in `RoomGenerator`, `Hazard`, `Fixture` durch Polymorphie ersetzen (Verhalten im Enum oder Strategie pro Art) | S4 | C | 0 `switch (kind)` in `domain` |
| 3 | `WorldRenderer`, `MenuScreens`, `RunScreens` in kleinere Klassen zerlegen (je Bildschirm oder Ebene eine Klasse) | S1 | E, F | keine Klasse über 600 Zeilen in `ui` |
| 3 | Zeichner pro Art über eine Zuordnung registrieren statt `switch (kind)` im Renderer | S4 | F | `WorldRenderer` ohne `switch (kind)` |

Priorität 1 ist bis zur nächsten Iteration machbar und verbessert die Architektur sichtbar. Priorität 2 ist der Kern für den Technischen Bericht I (M2, fällig 09.11.2026), denn Kapitel 4 und 5 begründen dort Architektur, Design-Klassendiagramm und Patterns. Priorität 3 folgt bis M3.

## 4 Was das für den Technischen Bericht I heisst

- Kapitel 4 (Architektur) zeigt das neue Schichtendiagramm und nennt die Regeln, die `ArchitectureTest` automatisch prüft.
- Kapitel 5 (Design) begründet jede Entscheidung mit GRASP und dem Pattern: welches Problem, welche Alternative, warum diese Lösung.
- Die Messgrössen aus Abschnitt 2 werden vorher und nachher angegeben. Das belegt die Verbesserung nachvollziehbar.

## 5 Umgesetzt am 26.10.2026 (vor Iterations-Review 3)

Grundlage: Feedback aus Iterations-Review 2 (13.10.) und LE06 (20.10.). Das Spielverhalten ist unverändert, belegt durch 210 Unit-Tests und 59 Schritte im UI-Durchlauf (`./gradlew uiSmoke`).

| Änderung | Prinzip / Pattern | Vorher | Nachher |
|---|---|---|---|
| Systemoperationen laufen über `GameService` (`step`, `chooseNextRoom`, `take`, `acceptDeal`, `repair`, `useConsole`, `chooseLevelUp`). Die UI entscheidet nicht mehr, wann gesichert oder verbucht wird. | GRASP Controller, Trennung UI/Domäne | 8 verändernde Aufrufe von `GameRun` aus `ui`, Sichern nach Raumwechsel in `RunScreens` | 0 |
| `GameRepository` liegt in `application`, Paket `ports` aufgelöst | Dependency Inversion, zyklenfreie Pakete | Zyklus `application` ↔ `ports` | 0 Zyklen |
| `Boss extends Enemy` mit Fabrikmethode `Enemy.create`; Wächter überschreiben Rückstoss, Betäubung, Schadensgrenze, Frost, Kern freilegen, Beute | Vererbung, GRASP Polymorphism, Information Expert, GoF Fabrikmethode | 28 `kind.boss()`-Abfragen in `domain` | 7, nur noch bei der Erzeugung |
| Basisklasse `Body` für `Actor`, `Projectile`, `Pickup`; `Physics.move` wird `Actor.move` | Generalisierung, Verantwortung möglichst weit oben, keine Duplikate | Schwerkraft 3-mal einzeln programmiert, statische Hilfsklasse | 1 Implementierung (`Body.fall`), `Physics` entfernt |
| `ArchitectureTest` prüft Schichtregeln und Zyklen über die Importe | Protected Variations | Regeln nur von Hand geprüft | 6 automatische Regeln |

Damit sind S5 und S7 erledigt, S1, S2 und S4 teilweise. Offen für Iteration 4: Felder `private` (S3), Unterpakete in `domain`, eigener Startpunkt und Audio-Interface (S6), Zustand der Gegnerverhalten in eigene Objekte, `GameRun` weiter aufteilen.
