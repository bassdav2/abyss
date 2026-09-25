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
- 02:12: Lokalen Git-Stand gesichert; Git-LFS-Prüfung erfolgreich. Mac- und Quell-ZIP erstellt und separat entpackt. Frisches Quellprojekt besteht alle 41 Tests, Formatprüfung und Javadoc; sieben Aufgaben tatsächlich ausgeführt. Entpackte App startet mit eigener Laufzeit, Signatur gültig. Abschlussstand und manuelle Restprüfungen in MORGEN.md.

## Beauftragter Ausbau 0.2

Der Nachtstand oben bleibt als historisches Journal erhalten. David beauftragte anschliessend ausdrücklich mehr Inhalt, Items, Bosskämpfe, eine überarbeitete UI und dichtere Atmosphäre. Zwischen abgeschlossenem Nachtstand und diesem Folgeauftrag wurde keine durchgehende Arbeitszeit behauptet.

- 12:17: Neues aktives Ziel begonnen, vorhandenen Stand und Quellen geprüft.
- 12:39: 18-Raum-Route mit drei Bossen, zwölf passiven Items, zerstörbaren Kisten, Inventar und Bootskarte integriert. Vier neue Raumplatten, zwei weitere Blender-Modelle und drei Musik-Loops erstellt. Erste Layoutfehler bei Karte, Inventar und kleinen HUD-Buttons korrigiert.
- 12:43: Reparatursets mit Q, Kapazität drei und Werkstattkauf ergänzt. 53 JUnit-Testfälle/-konfigurationen sowie 23 JavaFX-Komponentenprüfungen erfolgreich; 144 simulierte Kampagnen gewonnen.
- 12:47: Finale 120-Sekunden-Demo und 180-Sekunden-Renderlauf fertig. 21.547 Frames, fünf vollständige Runs, 29 Raum-/Abzweigvarianten und null unbehandelte Fehler. 19 native Audioclips stumm initialisiert. Neues Mac-Bundle gebaut; 160 Ressourcen stimmen per SHA-256 überein.
- 12:50: Neues Bundle mit eigener Laufzeit und isoliertem QA-Profil gestartet, gerenderten Screenshot erhalten. Desktop weiterhin gesperrt; echte Betriebssystem-Bedienung und Hörprüfung bleiben offen.
- 12:55: Abschliessende Layoutkorrekturen an Fund-Hinweisen und Titel übernommen; Video und 21-seitiger Bericht neu erzeugt und geprüft. Mac- und Quell-ZIP 0.2 entpackt: frischer Build mit allen 53 Tests, Formatprüfung und Javadoc erfolgreich, 212 Dateiprüfsummen identisch. Entpackte App startet mit eigener Laufzeit, Signatur gültig. Prüfprotokoll: qa/release-check.json.

## Version 1.0 · autonomer Lauf mit Claude Code (25.09.2026)

Auftrag per Sprachnachricht um ca. 01:14 Uhr: Prototyp zu einem fertigen Spiel weiterentwickeln, Pixel-Art und Indie-Look, bessere Effekte, viel wiederholbarer Inhalt, U-Boot vom Heck bis nach vorn beibehalten, JavaFX verwenden, Kursvorgaben beachten. Arbeitsfenster etwa zwölf Stunden. Zeitangaben gerundet.

- ca. 01:15: Kursunterlagen (Kick-off S. 17/19, M2, M3, KI-Vorgabe), Projektdokumente und Code 0.2 gelesen; Stand gebaut, Capture-Mechanik geprüft, Quellstand gesichert.
- ca. 02:00: Neue Domäne: Laufstege und Physik, sechs Waffen mit Kombinationen, 41 Module, acht Fähigkeiten, fünf Klassen, zehn Gegner mit Strategien, vier Bosse mit Phasen, 24-Raum-Generator mit Sonderräumen, Belohnungen, Handel, Kapelle. Erste Bot-Simulation ohne Ausnahmen.
- ca. 02:40: Anwendungsschicht (Profil mit Kernen, Freischaltkatalog, Garderobe, Vorbereitung) und Dateiformat 3 mit Migration.
- ca. 03:30: Software-Pixel-Pipeline (Framebuffer, gestuftes Licht, Bloom, eigene Pixelschrift) und prozedurale Pixel-Art für Figur, Gegner, Bosse, Räume, Symbole; Übersichtsbögen visuell geprüft.
- ca. 04:15: Welt-Renderer, Effekte, HUD, Titel-U-Boot; JavaFX-Fenster, Menüs und Pixel-Schriften. Erste echte Starts per Capture; Licht aufgehellt, Layoutfehler korrigiert.
- ca. 05:00: Neue Test-Suite (88 Fälle), Balancebericht über 150 Läufe: Hänger (Gegner auf hohen Stegen, Harpune gegen Flieger) gefunden und behoben; UI-Prüfung mit 24 Schritten und Bildschirmfotos; 34 Klänge.
- ca. 06:00: `GameRun` in Mitarbeiterklassen und `WorldRenderer` in Zeichner aufgeteilt, Verhalten per Balancebericht identisch; UML neu erzeugt; Dokumentation überarbeitet.
- ca. 06:15: Doku (Morgen-Übersicht, Demo, Projektmanagement), QA-Protokolle und Bildschirmfotos neu erzeugt. Bei der Durchsicht der Fotos behoben: Meldungen überlappten den Raumtitel, HUD-Texte schimmerten durchs Pausenmenü, alte Meldungen erschienen im Titel, Bossname verdeckte den Boss, Titel-U-Boot hinter Menütexten. Titel-U-Boot neu gemalt (schattierter Rumpf, Turm, Leitwerk), Treffer-Blitz der Figur verkürzt, Kompendium mit Silhouetten. Mac-App gebaut, in `Spiel/` ersetzt und mit temporärem Speicherort gestartet.
- ca. 06:35: Raumzustände (Stromausfall, Alarmstufe Rot, Druckleck) mit eigener Beleuchtung; Tagestauchgang; Logbuch mit Erfolgen (Profilfeld, Archiv-Reiter); zwei Gegner für das Kommandodeck (Sicherheitsautomat, Suchlichtsonde); Koloss auf 150 Integrität und 20 % Schutz angehoben.
- ca. 06:50: Resonanzen (neun Modulpaare) mit Hinweis auf Angebotskarten; Siegesszene mit auftauchendem Boot; UI-Prüfung auf 33 Schritte erweitert (Logbuch, Raumzustand in der Routenwahl, Siegesszene).
- ca. 07:00: Archiv-Reiter „Resonanzen“, Klang und Warnblitz beim Betreten eines Raums mit Zustand, sichtbares Unterdeck (Bilgenwasser, Unterflurlampen), Auftakt-Szene vor dem ersten Tauchgang; Demo-Video um die Siegesszene ergänzt; UI-Prüfung auf 37 Schritte erweitert.
- ca. 07:15: Finale Prüfläufe, Mac-App und Demo-Video ersetzt, Pakete 1.0 mit Prüfsummen erstellt und frisch entpackt gegengeprüft. Danach siebte Waffe (Enterhaken mit Heranziehen) ergänzt; Pakete werden am Ende erneut erzeugt.

## Version 1.1 · „Next Level“ (25.09.2026, Nachmittag)

Auftrag um ca. 12:26: interaktivere Räume, mehr Abteilungen, Gimmicks, spannenderes Spiel und Menüs, die sich wie ein Teil des Spiels anfühlen; Grafikstil beibehalten.

- ca. 12:30: Eigenes Immediate-Mode-GUI (`ui.gui.Gui`) im Framebuffer; alle Menü- und Tauchgangsbildschirme neu als Bordsysteme gezeichnet; JavaFX-Overlay, CSS und fremde Schriften entfernt. UI-Prüfung 37/37, Bildschirmfotos durchgesehen, Überlappungen korrigiert.
- ca. 12:45: Raumtechnik (`Fixture`, `Machinery`, `MachinePainter`): Förderband, Dampfdüse, Turbinenwind, Presse, Lasergitter, Notschalter mit vier Sektionseffekten; Testspieler weicht aus und nutzt Schalter.
- ca. 12:55: Hüllenbruch, Schmugglerdrohne, acht neue Raumthemen mit Requisiten, Ersatzglyphen für typografische Zeichen.
- ca. 13:05: Bossarenen mit Anlagen, Anlagen-Treffer durchschlagen Bosspanzerung; Schlagseite mit gekipptem Bild; Koloss-Tempo angehoben; Doku und UML auf 1.1.
