# ABYSS 0.2 - Start und Überblick

Stand: 22.09.2026. Der erweiterte Stand ist eine spielbare lokale Java-/JavaFX-Anwendung für Apple Silicon macOS.

## Direkt loslegen

`/Users/davidbass/abyss/dist/Abyss.app` doppelklicken. Java ist enthalten. Für einen ersten Versuch: **Neuer Tauchgang -> Entdecker -> Starten**.

A/D bewegen, Leertaste springen, J oder linke Maus angreifen, Shift ausweichen, K oder rechte Maus für das aktive Modul. **Q** verwendet ein Reparaturset, **I/Tab** öffnet das Inventar, **M** die Bootskarte. Nach dem Kampf mit E den Fund in der Mitte bergen und am rechten Schott weitergehen. Esc pausiert. Inventar und Karte pausieren ebenfalls.

## Neu gegenüber dem ersten Nachtstand

- **18 statt 12 Räume**, drei Sektionen und zwölf Positionen mit Routenwahl.
- **Drei Bosse:** Schottmeister in Raum 5, Reaktorkern in Raum 11, Lotse in Raum 18. Eigene Silhouetten, Angriffsmuster, Panzerung und Erholungsfenster.
- **Zwölf passive Items** mit drei Stufen; zusätzlich Vorratskisten und mitnehmbare Reparatursets. Ein Run beginnt mit einem Set; maximal drei, Nachkauf in Werkstätten für 20 Schrott.
- Neue Räume: Torpedomagazin, Ballastkammer, Krankenstation und Sauerstoffgarten. Insgesamt 15 Raumgrafiken, sieben editierbare Blender-Modelle und 118 Animationsframes.
- Neues HUD, ein Inventar mit erklärten Effekten, Bootskarte und Routenbilder. Parallaxe, Stirnlampe, Lichtkegel, Nebel, Vordergrundkabel und Kondenswasser geben den Räumen mehr Tiefe.
- Drei zusätzliche Musik-Loops für spätere Sektionen und Bosskämpfe. Insgesamt 19 eigene Audiodateien.
- Bestehende Version-1-Spielstände werden auf die längere Route übertragen und vor dem nächsten Überschreiben gesichert.

Detaillierte Spielregeln und Grafikquellen stehen in EXPANSION.md. Die Spielspur bleibt horizontal; dekorative Bildobjekte sind keine Plattformen.

## Was geprüft wurde

**53 JUnit-Testfälle/-konfigurationen bestanden**, darunter 144 komplette Kampagnensimulationen mit regulären Spieleingaben; alle erreichten den Sieg. **23 JavaFX-Komponentenprüfungen** testen unter anderem Inventar, Karte und Werkstattkauf. Ein 180-Sekunden-Renderlauf mit vierfacher Simulation lieferte 21.547 Frames und fünf vollständige Runs ohne unbehandelte Fehler. Alle 19 nativen Audioclips wurden lautlos geladen und aufgerufen.

Das neue App-Bundle wurde mit eigener Laufzeit gestartet; Signatur und 160 eingebettete Ressourcen wurden kontrolliert. Die Demo und Screenshots verwenden teilweise vorbereitete gültige QA-Spielstände. Bot-Zeiten sind keine menschlichen Spieldauern. Details und Grenzen: TESTSTRATEGIE.md, qa/test-summary.json und qa/release-check.json.

## Jetzt selbst spielen

Der Desktop war gesperrt; deshalb stehen ein echter Maus-/Tastaturtest am Betriebssystem und die Hörprüfung noch aus. Besonders hilfreich sind zehn Minuten tatsächliches Spielen: Sind Dash, Fund, Q-Heilung und Schott verständlich? Sind die Bossvorwarnungen gut lesbar? Stimmen die Soundpegel? Automatische Tests beantworten diese Fragen nicht.

Für PM3 wurden Anforderungen, Architektur, UML und der 21-seitige Bericht an den Ausbau angepasst. Der Code und Bericht wurden weitgehend von Codex erstellt. Teamentscheidungen, gemessene Personenstunden, Nutzertests und Freigaben sind nicht erfunden. Der Bericht bleibt eine Arbeitsfassung.

Die App ist ad-hoc signiert und nicht notarisiert. Windows/Linux, Gamepad und freie Tastenbelegung sind nicht geprüft. Die frühere Idee von 20-30 Minuten pro Run ist keine gemessene Zusage.

## Dateien

- Spiel: `dist/Abyss.app`
- Mac-ZIP: `release/Abyss_v0.2_macOS_AppleSilicon_2026-09-22.zip`
- Quellcode mit Javadoc: `release/Abyss_v0.2_Source_2026-09-22.zip`
- 120-Sekunden-Demo: `output/video/Abyss_Gameplay_Demo.mp4`
- Technischer Bericht: `output/pdf/Abyss_Technischer_Bericht_Arbeitsfassung.pdf`
- Code-Einstieg: `src/main/java/ch/zhaw/abyss/domain/GameRun.java`
- Arbeitsjournal: `docs/WORK_PLAN.md`
- Prüfsummen: `release/SHA256SUMS.txt`

Der erste Stand bleibt im lokalen Git-Verlauf erhalten. Die Präsentation im Semester-Wiki wurde durch diesen Ausbau nicht überschrieben. Das neue Video zeigt den tatsächlich implementierten Stand. Es wurde nichts eingereicht oder extern veröffentlicht.
