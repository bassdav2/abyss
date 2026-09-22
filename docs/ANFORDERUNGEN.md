# Anforderungen und Use Cases - Arbeitsfassung

Stand 22.09.2026. Aus Davids Spielidee und dem lokalen Kursbestand abgeleitet. Dies ist ein prüfbarer Vorschlag für das Team, keine erhobene oder durch den Dozenten abgenommene Spezifikation.

## Produktziel und Akteur

Eine Person spielt lokal am Desktop einen actionorientierten Tauchgang vom Heck bis zur Brücke. Jeder Raum stellt eine überschaubare Kampf- oder Ausrüstungsentscheidung. Niederlage führt zu einem neuen Versuch; Baupläne bleiben erhalten. Nach dem Sieg ist ein schwererer weiterer Zyklus freiwillig möglich.

Primärer Akteur: Spieler/in. Das lokale Dateisystem ist ein technisches Nachbarsystem. Es gibt keine Anmeldung und keinen Onlinedienst.

## Use-Case-Übersicht

| ID | Use Case | Priorität | Beschreibungstiefe |
|---|---|---|---|
| UC-01 | Tauchgang beginnen | Muss | Casual |
| UC-02 | Zum nächsten Raum vordringen | Muss, fachlicher Kern | Fully dressed |
| UC-03 | Ausrüstung verbessern | Muss | Casual |
| UC-04 | Tauchgang unterbrechen und fortsetzen | Muss | Casual |
| UC-05 | Brücke erobern und weiteren Zyklus beginnen | Muss | Casual |
| UC-06 | Nach Niederlage erneut versuchen | Muss | Brief |
| UC-07 | Baupläne ansehen und Startmodul wählen | Muss | Brief |
| UC-08 | Einstellungen und Hilfe verwenden | Muss | Brief |

## UC-02 - Zum nächsten Raum vordringen (fully dressed)

**Ziel:** Eine Begegnung bewältigen, optional ihren Fund nutzen und einen zulässigen nächsten Raum betreten.

**Scope/Level:** ABYSS, Benutzerziel. **Primärer Akteur:** Spieler/in. **Auslöser:** Ein Tauchgang wird gestartet oder fortgesetzt.

**Stakeholder und Interessen:** Die spielende Person erwartet verständliche Gefahren, verlässliche Steuerung, faire Belohnungen und einen sicheren Unterbruch. Das Projektteam benötigt nachvollziehbare Regeln, reproduzierbare Fehler und testbare Verantwortlichkeiten.

**Vorbedingungen:** Ein gültiger Run ist aktiv. Die Figur lebt. Der aktuelle Raum ist aus einer gültigen Route entstanden.

**Erfolgsgarantie:** Der gewählte nächste Raum ist aktiv. Ressourcen und installierte Module werden übernommen. Ein neuer Einstieg wird als lokale Sicherung angelegt, sofern der Speicherzugriff funktioniert.

**Minimalgarantie:** Ein ungesicherter Raum kann nicht übersprungen werden. Eine ungültige Wahl verändert den aktuellen Raum nicht. Ein Speicherfehler beendet die Anwendung nicht und wird gemeldet.

**Hauptszenario:**

1. Das System zeigt den Raum, die Figur, Ressourcen und verbliebene Patrouillen.
2. Die Person bewegt sich, greift an und weicht angekündigten Angriffen aus.
3. Das System prüft Treffer, Energie, Abklingzeiten und Schaden nach den Spielregeln.
4. Nach der letzten besiegten Patrouille markiert das System den Raum als gesichert und entriegelt das rechte Schott.
5. Die Person nähert sich dem Fund und betätigt die Interaktionstaste.
6. Das System zeigt bis zu drei noch nicht maximierte passive Module.
7. Die Person wählt ein Modul; das System installiert die zulässige Zahl Stufen genau einmal.
8. Die Person geht zum rechten Schott und interagiert.
9. Das System zeigt ein oder zwei nächste Räume mit Risiko und Belohnungsart.
10. Die Person wählt einen Raum. Das System übernimmt den Build, betritt den Raum und speichert den neuen Einstieg.

**Erweiterungen:**

- 2a: Die Person pausiert oder das Fenster verliert den Fokus. Simulation und Ressourcenverbrauch stoppen; Weiter setzt denselben Zustand fort.
- 3a: Für das aktive Modul fehlen Energie oder Abklingzeit. Es wird kein Angriff ausgelöst und keine Energie abgezogen.
- 3b: Die Integrität erreicht null. Niederlage wird angezeigt; der aktive Checkpoint wird entfernt und der Profilfortschritt verbucht.
- 4a: Weitere Patrouillen sind vorgesehen. Ein sichtbarer Hinweis kündigt sie an; nach 2,2 Sekunden entsteht die nächste Welle.
- 5a: Der Raum ist ein Depot. Vorräte heilen und liefern Schrott, aber kein Modul.
- 5b: Der Raum ist eine Werkstatt. Die Interaktion repariert einmal gratis; das optionale Modul kostet 15 Schrott.
- 6a: Alle passiven Module sind maximiert. Das System bietet Vorräte an.
- 7a: In der Werkstatt reicht der Schrott nicht. Kauf ist deaktiviert; Reparatur und Weiterreise bleiben möglich.
- 8a: Die Person verlässt den Raum ohne Fund. Der nicht genommene Fund wird nicht nachträglich gutgeschrieben.
- 9a: Nur ein nächster Raum ist zulässig (Werkstatt, Sektorwächter oder Brücke). Das System zeigt genau diesen Weg.
- 10a: Speichern schlägt fehl. Der aktuelle Run kann weitergespielt werden; die UI meldet, dass die neue Sicherung nicht verlässlich auf dem Datenträger liegt.
- 4b: Der Raum ist die Brücke. Nach dem Boss endet der Run mit Sieg; UC-05 folgt statt normaler Routenwahl.

**Besondere Anforderungen:** Maximal 1/30 s je Domain-Aufruf, im Spiel 1/120 s. Kein Schaden durch reine Render- oder Audioereignisse. Raum-Sicherung stellt einen Einstieg wieder her, keinen beliebigen Kampfzeitpunkt. Hindernisse im Hintergrund sind Dekoration; die Spielspur ist horizontal.

**Häufigkeit:** Einmal je Raum, bis zu achtzehnmal pro Zyklus. **Offene Punkte:** Menschliche Spieldauer, gewünschter Schwierigkeitsgrad und finale Modulnamen müssen mit dem Team erprobt werden.

## Weitere Use Cases

### UC-01 - Tauchgang beginnen

Die Person öffnet die Vorbereitung, wählt ein freigeschaltetes aktives Modul und optional den Entdecker-Modus. Ein optionaler ganzzahliger Seed macht die Route reproduzierbar. Start erzeugt einen frischen Build und einen neuen Checkpoint im Heck. Ein unbekannter Bauplan ist nicht auswählbar. Ein ungültiger Seed startet keinen Run. Ein neuer Start ersetzt die laufende Raum-Sicherung; dauerhafte Baupläne bleiben erhalten.

### UC-03 - Ausrüstung verbessern

Nach einer Begegnung wählt die Person aus höchstens drei passiven Modulen. Ein normales Angebot installiert eine Stufe, ein Elite- oder Sektorwächterfund zwei bis zur Obergrenze drei. Maximal ausgebaute Module werden nicht angeboten. Werkstätten verlangen 15 Schrott für ein Modul. Eine Auswahl kann nicht doppelt abgerechnet werden. Im aktuellen Run verfügbare Ressourcen sind immer sichtbar.

### UC-04 - Unterbrechen und fortsetzen

Während des Spiels öffnet Esc die Pause. Weiter führt denselben Kampf fort. Wer ins Hauptmenü wechselt oder die Anwendung schliesst, kann den gespeicherten Raumeingang fortsetzen. Nicht abgeschlossene Änderungen innerhalb dieses Raums werden zurückgesetzt. Beschädigte bzw. unbekannte Save-Versionen verhindern den normalen Start nicht; vor dem Ersetzen werden die Originale gesichert. Ein fehlender Save zeigt kein Fortsetzen an.

### UC-05 - Brücke und nächster Zyklus

Nach dem letzten Gegner zeigt das System Sieg, erreichte Räume, Kills, Tauchzeit und Zyklus. Die Person kann ins Hauptmenü zurückkehren oder mit ihrem aktuellen Build einen neuen Zyklus starten. Die Figur erhält bis zu 50 Integrität und volle Energie. Gegner werden bis zu einer definierten Obergrenze stärker. Route und Raumzustände entstehen neu. Der neue Einstieg wird gesichert.

### UC-06 bis UC-08 - Brief

- **UC-06:** Niederlage -> Ergebnis -> erneuter Versuch mit neuem Seed und dem letzten aktiven Modul. Keine alten passiven Upgrades; gesicherte Baupläne bleiben.
- **UC-07:** Im Archiv Freischaltungen prüfen. Raum 6 schaltet Lichtbogen frei, Raum 12 Druckschild. Diese bieten Alternativen für neue Starts.
- **UC-08:** Gesamtlautstärke, Musik, reduzierte Bewegung, Vollbild und Entdecker-Vorgabe einstellen. Steuerung jederzeit aus dem Hauptmenü oder der Pause nachlesen.

## Funktionale Anforderungen und Akzeptanz

| ID | Anforderung | Akzeptanzbeleg |
|---|---|---|
| F-01 | Bewegung, Sprung, Nahkampf, Dash und Modul | Domain- und Input-Tests; JavaFX-Komponententest |
| F-02 | Drei reguläre Gegnertypen und drei Bosse mit angekündigten Angriffen | Zustandsmaschine; Kampagnensimulation; Screenshots |
| F-03 | Achtzehn erreichbare Räume, feste Werkstätten, Routenwahl | Generator-Test über 1000 Seeds; vollständige Runs |
| F-04 | Zwölf passive Module, drei aktive Startmodule | Stack-/Energie-/Freischaltregeln und UI |
| F-05 | Tod, Sieg, erneuter Versuch und Folgezyklus | Phasen-Tests und Ergebnisanzeige |
| F-06 | Lokale Raum-Sicherung und permanentes Profil | Repository-Roundtrip, Fehlerfälle, UI-Fortsetzen |
| F-07 | Audio, Pause, Hilfe, Optionen und Vollbild | Native Clip-Ladung, JavaFX-Menütest; manuelle Hör-/Vollbildprüfung offen |

## Qualitätsanforderungen

- **Q-01 Testbarkeit:** Domäne muss ohne JavaFX-Fenster und ohne Dateisystem laufen. Automatisiert geprüft.
- **Q-02 Robustheit:** Defekte lokale Daten dürfen den Titelbildschirm nicht verhindern. Beim Ersetzen bleiben Originaldaten als Backup erhalten. Automatisiert geprüft.
- **Q-03 Performance:** Ziel sind flüssige 60 Bilder/s auf dem Ziel-Mac; Messung von CPU-Zeichenkosten und tatsächlichen Frame-Abständen ist vorhanden. Die Messung ersetzt keine Prüfung auf anderen Rechnern.
- **Q-04 Bedienbarkeit:** HUD, Gefahren und Interaktionen müssen im minimalen Fenster lesbar sein. Visuelle Layoutprüfung vorhanden; menschlicher Erstnutzertest offen.
- **Q-05 Nachvollziehbarkeit:** Quellen, KI-Einsatz, Asset-Pipeline, Tests und Build-Schritte bleiben im Projekt. Keine Behauptung einer bereits durchgeführten Teamabnahme.
- **Q-06 Portabilität:** Java-Quellcode soll auf macOS/Windows/Linux baubar bleiben. Ausgeliefert und geprüft wird zunächst nur Apple Silicon macOS. Betriebssystemspezifische Pakete brauchen eigene Builds.

## Spielregeln und Scope

Maximal drei Stufen je passivem Modul. Integrität und Energie werden begrenzt; Treffer haben 0,72 s Schutzzeit gegen Mehrfachschaden. Ein Dash schützt kurz und hat eine Abklingzeit. Die Bosse nehmen ausserhalb ihrer Erholung nur 50 (Schottmeister), 38 (Reaktorkern) bzw. 22 Prozent (Lotse) Schaden. Nach der Hälfte seiner Integrität wird sein Angriffstempo erhöht. Der Entdecker-Modus beginnt mit 150 statt 100 Integrität und erhöht Basisschaden um 20 Prozent.

Der ursprüngliche Wunsch nach 20-30 Minuten je Run ist weiterhin eine Planungsannahme und derzeit nicht durch menschliche Tests bestätigt. Der implementierte Stand ist kompakter. Es gibt achtzehn Raumpositionen, nicht achtzehn beliebig prozedural erzeugte Geometrien. Plattformrätsel, Leitern, Mehrspieler, Storydialogsystem und frei belegbare Tasten gehören nicht zum aktuellen Stand.

## Ergänzungen 0.2

- **UC-09:** I/Tab öffnet das Inventar mit zwölf Itemtypen, aktuellen Stufen und Charakterwerten. M öffnet die 18-Raum-Karte mit bisheriger Route und Bosspositionen. Beide halten den Run an.
- **UC-10:** Normale Werkzeugschläge zerstören Vorratskisten; der Inhalt wird genau einmal gutgeschrieben. Reparaturgel und Energiezellen respektieren Maximalwerte.
- **F-08:** Alte Save-Version 1 wird auf Format 2 übertragen, mit unverändertem Original als Backup vor dem ersten Überschreiben.

- **UC-11:** Reparaturset mit Q einsetzen, nur wenn Integrität fehlt und ein Set vorhanden ist. Bis maximal drei Sets; Werkstattkauf kostet 20 Schrott und wird genau einmal abgerechnet. Raum-Checkpoints erhalten den Vorrat.
