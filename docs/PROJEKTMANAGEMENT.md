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

| Risiko | Eintritt / Auswirkung | Massnahme / Status |
|---|---|---|
| Team versteht übernommenen KI-Code nicht ausreichend | Hoch / hoch | Ownership verteilen; Ablauf- und Änderungsreviews; vor Abgabe eigene Begründungen |
| UI-/Bibliothekswahl oder Spielthema nicht freigegeben | Offen / hoch | Fachdozent prüfen lassen; JavaFX und eigene Domäne belegen |
| Kampf wirkt repetitiv oder zu einfach | Mittel / hoch | Menschen testen; Bot-Ergebnisse nicht als Spassnachweis ausgeben |
| Grafik von Hintergründen und Figuren wirkt uneinheitlich | Niedrig / mittel | Seit 1.0 eine Palette und prozedurale Pixel-Pipeline; Übersichtsbögen (`artSheet`) prüfen |
| Andere Teamplattformen bauen nicht | Mittel / hoch | OS-/Architektur-spezifische JavaFX-Pakete; separate CI oder echte Geräteprüfung |
| Save-Dateien sind beschädigt | Niedrig / mittel | Validierung, temporäres Schreiben, Original-Backups; Integrationstests |
| Profil und Checkpoint werden durch Absturz inkonsistent | Niedrig / niedrig bis mittel | Je Datei atomar; keine gemeinsame Transaktion; bei Bedarf ein gemeinsames Snapshot-Format einführen |
| Wünsche wachsen über Semesterbudget | Hoch / hoch | Zwölf Räume und klarer Kern; keine Multiplayer-/Engine-Migration ohne bewussten Entscheid |

## Offene Entscheidungen

Projektname, Teamrollen, finale Zielplattformen, finale Run-Dauer, Abgabetermine, Architekturfreigabe, Umgang mit dem weitgehend KI-erstellten Ausgangsstand und tatsächlicher Nutzertest. Diese Punkte gehören in das erste Teamgespräch.
