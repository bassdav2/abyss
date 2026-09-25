# KI-Einsatz in ABYSS

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

## Fortsetzung 0.2

David beauftragte nach Sichtung des ersten Spiels ausdrücklich mehr Items, UI-Überarbeitung, Atmosphäre, Räume und Bosskämpfe. Codex setzte 18 Räume, drei Bosse, zwölf passive Items, Vorratskisten, Inventar/Karte und Save-Migration um. Vier neue ImageGen-Platten, zwei weitere Blender-Modelle, eigene Canvas-Icons und drei synthetische Musikloops ergänzen die Assets. Tests, Code, Grafiken und Dokumentation bleiben überwiegend KI-gestützt erstellt; keine neue Teamabnahme wird behauptet.

## Version 1.0 (25.09.2026) · Claude Code

David beauftragte in der Nacht vom 24. auf den 25.09.2026 per Sprachnachricht einen etwa zwölfstündigen autonomen Lauf: den Prototyp zu einem fertigen Spiel weiterentwickeln, deutlich stärker Richtung Pixel-Art und Indie-Look, bessere Effekte, viel wiederholbarer Inhalt (Items, Upgrades, Klassen, Aussehen, Bosse, Fähigkeiten, Mechaniken). Unverändert bleiben sollte: U-Boot, Start im Heck, Kampf Raum für Raum bis nach vorn, Roguelike, JavaFX.

| KI | Ziel der Verwendung | Aufwand für den Prompt | Resultat verwendet | Art der Verwendung |
|---|---|---|---|---|
| Anthropic Claude (Opus 5.5) in Claude Code | Kursvorgaben lesen, Prototyp analysieren, neue Domäne entwerfen und implementieren (Plattformphysik, Waffen, 41 Module, 8 Fähigkeiten, 5 Klassen, 12 Gegner, 4 Bosse, Generator mit Raumzuständen, Belohnungen, Meta-Progression mit Logbuch) | Ein Auftrag, danach autonom mit vielen Prüf- und Korrekturschleifen | Ja | Code weitgehend übernommen; automatisch gebaut, getestet, per Testspieler balanciert und korrigiert |
| Anthropic Claude (Opus 5.5) | Software-Pixel-Renderpipeline, prozedurale Pixel-Art (Figuren, Gegner, Räume, Symbole), Licht, Effekte, HUD | wie oben | Ja | Grafik entsteht vollständig aus Java-Code; visuell über exportierte Bögen und Szenen geprüft und nachgebessert |
| Anthropic Claude (Opus 5.5) | JavaFX-Menüs, Archiv, Garderobe, Handel, Route, Karte | wie oben | Ja | Über Bildschirmfotos geprüft, Layoutfehler korrigiert |
| Anthropic Claude (Opus 5.5) | Zusätzliche Klänge und Forschungsdeck-Musik per NumPy-Synthese (`tools/create_audio_extra.py`) | gering | Ja | Eigene Synthese, keine Samples; nicht angehört |
| Anthropic Claude (Opus 5.5) | Tests, QA-Werkzeuge, UML-Quellen, Dokumentation | wie oben | Ja, als Arbeitsfassung | An tatsächlichen Klassen ausgerichtet; Teamprüfung ausstehend |
| Anthropic Claude (Opus 5.5), Version 1.1 | PM3-Berichte M1–M3 als Markdown-Arbeitsfassung, zwei Foliensätze mit Sprechtext (`tools/create_slides.py`), Repository-Wegweiser | Auftrag „Repo mit Dokumentation nach den Schulunterlagen“ | Ja, als Arbeitsfassung | Inhalte an Code, Tests und PM3-Aufträgen ausgerichtet; `[TEAM]`-Stellen und eigene Begründungen ergänzt das Team |
| Anthropic Claude (Opus 5.5), Version 1.1 | Bordsystem-GUI im Framebuffer (ersetzt JavaFX-Controls), Raumtechnik mit Bossarenen, Hüllenbruch, Schlagseite, Schmugglerdrohne, acht neue Raumthemen | ein zweiter Auftrag („auf das nächste Level bringen“), danach autonom | Ja | Mit Unit-Tests, Bot-Balance, JavaFX-Prüfung und Szenenbildern geprüft; Spielgefühl nicht von Menschen getestet |

Verwendete Fremdbestandteile in 1.0: die Schriften Pixelify Sans, Silkscreen und Jersey 10 (SIL Open Font License, aus dem Google-Fonts-Repository); seit 1.1 entfernt, alle Texte nutzen die eigene Pixelschrift. Es wurde keine Bild-KI verwendet; die früheren ImageGen-Hintergründe und Blender-Figuren sind durch prozedurale Pixel-Art ersetzt und nur noch in `art-source` bzw. der Git-Historie enthalten.

Hinweis für das Team: Der Umfang des KI-Einsatzes ist vor einer Abgabe mit dem Fachdozenten abzustimmen. Übernommen werden sollte nur, was das Team selbst erklären, ändern und begründen kann; die Architektur- und Testdokumente eignen sich als Einstieg, um die Verantwortlichkeiten der Klassen nachzuvollziehen.
