# ABYSS – Nachtlauf

Beginn: 22.09.2026, 01:09 Uhr Europe/Zurich. Autorisiertes Arbeitsfenster: etwa zwölf Stunden, bis ungefähr 13:09 Uhr. Fokus: tatsächlich spielbares, testbares Spiel; kein Versprechen eines kommerziell fertig getesteten Produkts.

## Lieferziel

- Java 25 / JavaFX 26.0.2, eigene Domäne und Spielsysteme ohne Engine-Framework.
- Ein abgeschlossener Run mit zwölf Räumen, drei visuellen Sektionen, Verzweigungen, Werkstätten, drei Gegnertypen und einem Boss.
- Bewegung, Sprung, Ausweichen, Nahkampf und aktive Module; passive Build-Upgrades.
- Tod, Neustart, Sieg, schwererer Folgezyklus und lokale Bauplan-Freischaltungen.
- Menüs, Audio, verständliches Feedback, Einstellungen und Save beim Raumwechsel.
- Konsistente Art-Pipeline, native Mac-Anwendung, Quellcode, Tests, PM3-Architektur- und KI-Dokumentation.

## Reihenfolge

1. Spielmodell, Run-Generator, Kampf und sinnvolle Domain-Tests.
2. Native Darstellung und ein vollständiger kurzer spielbarer Ablauf.
3. Raumkunst, Figurenanimationen, Sound und lesbare Benutzeroberfläche.
4. Alle zwölf Räume, Build-Variationen, Boss und Wiederholungsschleife.
5. Speichern, Menüs, Einstellmöglichkeiten, Fehlerbehandlung und Balancing.
6. Automatisierte und echte UI-Prüfung, Paketierung, nachvollziehbarer Morgen-Handoff.

## Kursgrenzen

Basis: PM3-Kick-off HS26, insbesondere physische PDF-Seiten 17/19, M2-Auftrag Seiten 2–3 und KI-Dokumentation Seite 1. Bibliotheks-/UI-Freigabe und die fachlichen Entscheidungen des Teams bleiben offen. Ein autonom erstellter Stand ersetzt weder die Teamarbeit noch deren Verständnis oder die Abnahme durch den Fachdozenten. Es wird nichts eingereicht und kein externes Repository angelegt.

## Arbeitsjournal

- 01:09: Aktives Ziel gestartet. Bestehende Tools und Konzeptbilder geprüft. Neues, getrenntes Projekt `~/abyss` angelegt.

- 01:28: Erster nativer Start samt gerenderten Screenshots. 17 Tests bestanden nach Korrektur der kanonischen Upgrade-Speicherung.
- 01:31: Selbstständige Mac-App mit eingebauter Java-25-/JavaFX-Laufzeit erzeugt und per App-Binary gestartet. Bundle-Screenshot erfolgreich.
- 01:35: 24 Kampagnensimulationen mit regulären Eingaben (12 Seeds je Schwierigkeitsgrad) durchlaufen den gesamten Run. Spätere Räume besitzen mehrere Patrouillen; der Boss hat geschützte und verwundbare Phasen. Diese Simulation ersetzt keinen menschlichen Usability-Test.
- Native Computer-Use-Prüfung derzeit blockiert: Mac gesperrt, automatisches Entsperren nicht möglich. Keine Umgehung versucht; unabhängige Tests und Entwicklung gehen weiter.
- 01:50: 144 Kampagnensimulationen über alle drei Startmodule, zwei Schwierigkeiten und zwei Routenpräferenzen erfolgreich. Native Audio-Ladung lautlos geprüft.
- 01:54: 18 JavaFX-Komponentenprüfungen erfolgreich; 180 Sekunden Render-Probelauf mit acht vollständigen Runs, 21.583 Frames und null unbehandelten Ausnahmen.
- 02:04: 21-seitige technische Arbeitsfassung erzeugt. Elf Raumplatten und überarbeitete Gegnersilhouetten im neuen Mac-Bundle. Alle PDF-Seiten visuell geprüft; eine unschöne Tabellenüberschrift korrigiert.
- 02:07: 82-Sekunden-Gameplay-Demo erstellt und anhand mehrerer Frames geprüft. 41 JUnit-Testfälle/-konfigurationen bestanden. Bundle-Ressourcen per SHA-256 gegen 121 Inventareinträge geprüft; ad-hoc Codesign-Verifikation erfolgreich.
