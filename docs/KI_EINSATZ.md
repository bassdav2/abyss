# KI-Einsatz im ABYSS-Nachtlauf

Stand 22.09.2026. Der Code, die Tests und die nachfolgenden Entwürfe wurden weitgehend von Codex erstellt. David gab Idee, Art Direction, technische Zielrichtung und den autonomen Arbeitsauftrag vor. Es wird ausdrücklich keine eigenständige Erstellung durch das studentische Team behauptet.

Grundlage für diese Tabelle: lokaler PM3-Kursbestand, **Verwendung von KI im PM3.pdf, physische Seite 1**, Version 1.0, Quellen-ID `PM3-ddd8548018c7`. Die dort verlangten Spalten sind übernommen. Aufwand beschreibt qualitativ den Prompt-/Iterationsaufwand, keine erfundene menschliche Arbeitszeit.

| KI | Ziel der Verwendung | Aufwand für den Prompt | Resultat verwendet | Art der Verwendung |
|---|---|---|---|---|
| OpenAI Codex | Lokales Java-/JavaFX-Projekt, eigene Domäne, Kampf, Räume, Speichern und Menüs umsetzen | Hoch: längerer Auftrag mit vielen Implementierungs-/Prüfschritten | Ja | Weitgehend übernommen; automatisch gebaut, getestet und anschliessend korrigiert |
| OpenAI Codex | Tests, JavaFX-Komponententest und Render-Probelauf erstellen | Mittel bis hoch: mehrere Prüffälle und Fehlerkorrekturen | Ja | Testcode im Repository; Ergebnisse unter docs/qa und build/test-results |
| OpenAI Imagegen | Zusammenpassende Hintergrundplatten aus freigegebener Bildrichtung erzeugen | Mittel: konkrete Raum-Prompts und Referenzbild, visuelle Prüfung | Ja | Rastergrafiken als statische Hintergründe; Prompts unter ART_PROMPTS.md |
| OpenAI Codex | Reproduzierbare Figurenmodelle und Animations-Exports in Blender programmieren | Hoch: erste Modelle, Sichtprüfung, Silhouettenüberarbeitung | Ja | Python-Modellierungsquelle, .blend-Dateien und PNG-Frames vorhanden |
| OpenAI Codex | Eigene synthetische Klänge und Musik programmieren | Mittel | Ja | Synthese-Skript und WAV-Dateien; keine fremden Audio-Samples |
| OpenAI Codex | Architektur, Use Cases, UML, Teststrategie und technischen PDF-Entwurf verfassen | Hoch | Ja, als Arbeitsfassung | An tatsächlichen Klassen/Tests ausgerichtet; Teamprüfung und eigene Begründung ausstehend |

## Nachvollziehbarkeit und Übernahme

- Ausgangsauftrag: ein 2D-Roguelite im sehr langen U-Boot, vom Heck bis zur Brücke, mit konsistenter Grafik und wiederholbarem Run.
- Freigabe zum autonomen Arbeiten: etwa zwölf Stunden, so viel wie sinnvoll möglich umsetzen. Kein neuer Auftrag zu Moodle-Abgabe oder externer Veröffentlichung.
- Verwendete Produktionsabhängigkeit: JavaFX. Test-/Build-Werkzeuge sind gesondert dokumentiert.
- Automatische Tests beweisen bestimmte Eigenschaften; sie ersetzen kein Verständnis des Teams und keine fachliche Freigabe.
- Die genaue Prozessdauer steht im Arbeitsjournal. Sie ist weder Team-Personenaufwand noch eine nachträglich erfundene Semesterplanung.
- Der Umfang dieses KI-Einsatzes ist vor einer Kursabgabe mit den geltenden Richtlinien und dem Fachdozenten abzugleichen. Das Team sollte nur Inhalte übernehmen, die es selbst erklären, prüfen und begründen kann.

## Konkreter Lern-/Review-Vorschlag

Jedes Teammitglied kann einen klar abgegrenzten Teil übernehmen: Eingaben/Spielschleife, Domain-Kampf, Gegner/Generator, Persistenz, Darstellung/Assets oder Teststrategie. Für den eigenen Teil zunächst Ablauf erklären, eine Änderung selbst umsetzen und den zugehörigen Test begründen. Zuordnung und tatsächlichen Aufwand muss das Team selbst festhalten; hier werden keine Namen oder erledigten Teamleistungen erfunden.
