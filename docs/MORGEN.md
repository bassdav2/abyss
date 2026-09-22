# ABYSS - Stand für den Morgen

## Direkt loslegen

`/Users/davidbass/abyss/dist/Abyss.app` doppelklicken. Die Anwendung enthält Java bereits. Für einen ersten Versuch: **Neuer Tauchgang -> Entdecker -> Starten**.

A/D bewegen, Leertaste springen, J oder linke Maus angreifen, Shift ausweichen, K oder rechte Maus für das aktive Modul. Nach dem Kampf mit E den Fund in der Mitte bergen und am rechten Schott weitergehen. Esc pausiert.

## Was fertig vorliegt

- Ein zusammenhängender Run mit zwölf Räumen, drei Sektionen, drei Gegnertypen und Brückenboss.
- Routenwahl, Werkstätten, sechs passive Module, drei aktive Fähigkeiten, Baupläne und Folgezyklen.
- Elf Raumgrafiken, fünf bearbeitbare Blender-Modelle mit 86 Animationsframes, Audio und native Menüs.
- Lokales Speichern am Raumeingang; Profile und Teststände getrennt.
- Mac-App, vollständiger Java-Quellcode, Build-/Asset-Skripte und generierte API-Dokumentation.
- 21-seitiger technischer Bericht als Arbeitsfassung, zwölf editierbare UML-Diagramme und offengelegter KI-Einsatz.
- 82 Sekunden tatsächliches Gameplay als gekennzeichnete automatisierte Demo.

## Nachweise

41 JUnit-Testfälle/-konfigurationen bestanden. Darin 144 komplette Kampagnensimulationen mit normalen Spieleingaben; alle erreichten die Brücke. Zusätzlich 18 Prüfungen innerhalb der JavaFX-Oberfläche. Ein 180-Sekunden-Render-Probelauf mit vierfacher Simulation durchlief acht Runs ohne unbehandelte Ausnahme. Alle 16 nativen Audioclips wurden lautlos geladen und aufgerufen. App-Bundle separat gestartet; Signatur und 121 eingebettete Ressourcen per Prüfsumme kontrolliert. Das Quell-ZIP wurde in einem frischen Verzeichnis entpackt und mit Test-, Format- und Javadoc-Prüfung vollständig neu gebaut (bestehender Gradle-Abhängigkeitscache). Auch die separat entpackte Mac-App startet erfolgreich. Archiv-Prüfsummen stehen in `release/SHA256SUMS.txt`.

Die Demo und Screenshots nutzen zum Teil vorbereitete gültige QA-Spielstände. Bot-Zeiten sind keine menschlichen Spieldauern. Details: TESTSTRATEGIE.md und qa/test-summary.json.

## Als Erstes selbst prüfen

Der Desktop war gesperrt; deshalb stehen ein echter Maus-/Tastaturtest am Betriebssystem und die Hörprüfung noch aus. Besonders wertvoll sind jetzt zehn Minuten tatsächliches Spielen: Sind Reichweite, Dash, Fund und Schott sofort verständlich? Fühlt sich das Bossfenster fair an? Gibt es unangenehme Soundpegel? Diese Antworten können automatische Tests nicht geben.

Für PM3: Der Code und Bericht wurden weitgehend von Codex erstellt. Team und Fachdozent müssen Themen-/Bibliothekswahl und die Verwendung dieses Stands bestätigen. Rollen, Personenaufwand, Nutzertests und Freigaben wurden nicht erfunden. Nichts wurde auf Moodle abgegeben oder extern veröffentlicht.

Die App ist für Apple Silicon macOS gebaut, ad-hoc signiert und nicht notarisiert. Windows/Linux, Gamepad und freie Tastenbelegung sind nicht fertig geprüft. Der Run ist kompakt; die frühere Idee von 20-30 Minuten ist keine gemessene Zusage.

## Dateien

- Spiel: `dist/Abyss.app`
- Mac-ZIP und Quellpaket mit Javadoc: `release/`
- Demo: `output/video/Abyss_Gameplay_Demo.mp4`
- PDF: `output/pdf/Abyss_Technischer_Bericht_Arbeitsfassung.pdf`
- Code-Einstieg: `src/main/java/ch/zhaw/abyss/domain/GameRun.java`
- API: `build/docs/javadoc/index.html` beziehungsweise `api/index.html` im Quellpaket
- Arbeitsjournal: `docs/WORK_PLAN.md`

Das ursprüngliche Konzept bleibt als historischer Vergleich erhalten. Die vorhandene Präsentation im Semester-Wiki wurde durch diesen Nachtlauf nicht überschrieben. Für eine Vorführung zeigt das neue Video den tatsächlich implementierten Stand.
