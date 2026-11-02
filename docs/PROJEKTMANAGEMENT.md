# Projektmanagement - Vorschlag und offener Teamteil

Der Nachtlauf ist eine autonome Implementierung durch Codex am 22.09.2026. Er ist keine abgeschlossene studentische Iteration. Verbindliche Verantwortlichkeiten, Teamtermine, effektive Personenstunden, Reviews und Dozentenfreigaben liegen nicht vor und werden hier nicht erfunden.

## Plan / beobachtetes Ergebnis des Nachtlaufs

| Arbeitspaket | Vorgesehen | Beobachtetes Ergebnis |
|---|---|---|
| Basis und Kampf | Eigene Java-Domäne, Bewegung, Angriff, Gegner | Implementiert und Domain-getestet |
| Vollständiger Run | Zwölf Räume, Werkstätten, Module, Boss | Automatisch vom Start bis zum Sieg durchspielbar |
| Wiederspielwert | Seed, Varianten, Freischaltungen, Folgezyklus | Implementiert; Balancing braucht menschliche Erprobung |
| Grafik und Ton | Einheitliche Raumrichtung, reproduzierbare Figuren, Feedback | Raumplatten, Blender-Frames, eigene Synthese-WAVs |
| Benutzerführung | Native Menüs, Pause, Optionen, Speichern | JavaFX-Komponententest und visuelle Layoutprüfung |
| Auslieferung | Startbare Mac-App und Quellen | Lokales Bundle; endgültiger Buildstatus im Handoff |
| Kursdokumentation | Reale Architektur, Use Cases, UML, Teststrategie, KI-Einsatz | Editierbare Arbeitsfassung, keine Abgabe |

## Version 1.0 (autonomer Lauf mit Claude Code, 25.09.2026)

| Arbeitspaket | Ergebnis |
|---|---|
| Pixel-Art-Umstellung | Software-Framebuffer 480 × 270, gestuftes Licht, Bloom, prozedurale Grafik für Figur, Gegner, Bosse, Räume, Symbole |
| Spieltiefe | Laufstege, Kombinationen, 24 Räume in vier Sektionen, Raumzustände, zwölf Gegnerarten, vier Bosse mit Phasen, 41 Module, neun Resonanzen, fünf Klassen, Druckstufen |
| Meta-Fortschritt | Datenkerne, Archiv, Garderobe, Kompendium, Logbuch, Tagestauchgang, Profilmigration |
| Qualität | 106 Tests, Kampagnensimulation, Balancebericht, JavaFX-Prüfung, Render-Probelauf; Aufteilung von `GameRun` und Renderer |
| Dokumentation | Anforderungen, Architektur, 13 UML-Diagramme, Teststrategie, KI-Einsatz, Inhalte |

Auch dieser Lauf ist keine studentische Iteration. Das Risiko, dass das Team den Code nicht ausreichend versteht, ist mit 1.0 gewachsen; Iteration I-1 wird dadurch wichtiger. Das Risiko einer uneinheitlichen Grafik ist entschärft, weil alle Grafik jetzt aus einer Palette und einer Pipeline stammt.

Detaillierte Zeitpunkte stehen in WORK_PLAN.md. Der ursprünglich zugestandene Zeitraum war eine Obergrenze, kein Nachweis von zwölf tatsächlich geleisteten Stunden.

## Nächste menschliche Iterationen - Schätzung zur Diskussion

| Iteration | Ziel / konkretes Ergebnis | Grobe Team-Aufwandsspanne |
|---|---|---|
| I-1: Verstehen und abgrenzen | Modulvorgaben bestätigen, Idee und Bibliothek abstimmen, Verantwortlichkeiten verteilen, Architektur gemeinsam erklären | 4-8 Personenstunden |
| I-2: Erstnutzertest | Drei bis fünf echte Testpersonen, erste 10 Minuten beobachten, Verständlichkeitsprobleme und Fehler priorisieren | 6-10 Personenstunden |
| I-3: Spielgefühl | Trefferfeedback, Reichweite, Gegnerdruck und Run-Dauer anhand Beobachtungen verbessern | 8-16 Personenstunden |
| I-4: Zielplattform / Abgabe | Relevante Teamrechner bauen/testen, Bericht überarbeiten, Javadoc und Quellen reproduzierbar paketieren | 6-12 Personenstunden |

Diese Spannen sind neue Schätzungen, keine erfassten Ist-Zeiten. Reihenfolge und Aufwand sind durch das Team zu bestätigen. Erst nach der Iteration Ist-Aufwand, erreichte Ziele und Massnahmen eintragen.

## Risikoliste

Stand Iteration 3 (26.10.2026), Nummern wie in der Projektskizze M1, fortgeführt. Visualisiert in `docs/diagrams/15-risikomatrix.svg` (Quelle: `RISKS` in `tools/write_diagrams.py`).

| Nr. | Risiko | Wahrsch. / Schaden | Änderung seit Iteration 2 | Massnahme / Status |
|---|---|---|---|---|
| R1 | Team versteht den KI-gestützten Prototyp nicht ausreichend | hoch / hoch | gleich | Paketverantwortung, Code-Walkthroughs, Änderungen nur mit Test |
| R2 | Umfang wächst über das Semester hinaus | mittel / hoch | gleich | Feature-Stopp, Arbeit fliesst in Refactorings statt Inhalte |
| R3 | Steuerung und Fairness überzeugen nicht | mittel / mittel | Schaden gesunken | Spielspass ist laut Dozent nicht zentral für die Bewertung |
| R4 | KI-Einsatz wird nicht akzeptiert | niedrig / hoch | Wahrscheinlichkeit gesunken | Zwei Reviews ohne Einwand; Deklaration wird laufend ergänzt |
| R5 | Darstellung auf schwächeren Rechnern zu langsam | niedrig / mittel | gleich | Zeichenzeit messen |
| R6 | Probleme unter Windows oder Linux | mittel / mittel | gleich | Test auf den Rechnern im Team |
| R7 | Teammitglieder fallen aus oder fehlen beim Feedback | mittel / mittel | gleich | Teilnahme am Feedback ist ab LE07 Pflicht für die Unterrichtspunkte |
| R8 | Spielstände werden beschädigt | niedrig / mittel | gleich | Validierung, Sicherung der Originale, Tests |
| R9 | Technische Qualität (Kohäsion, Kopplung, Patterns) genügt der Bewertung nicht | mittel / hoch | **neu** | Refactoring-Plan in ARCHITEKTUR_ANALYSE.md, erste Schritte umgesetzt, ArchitectureTest |
| R10 | Refactorings brechen das lauffähige Spiel | niedrig / mittel | **neu** | 210 Unit-Tests und UI-Durchlauf vor jeder Abgabe |

## Offene Entscheidungen

Projektname, Teamrollen, finale Zielplattformen, finale Run-Dauer, Abgabetermine, Architekturfreigabe, Umgang mit dem weitgehend KI-erstellten Ausgangsstand und tatsächlicher Nutzertest. Diese Punkte gehören in das erste Teamgespräch.
