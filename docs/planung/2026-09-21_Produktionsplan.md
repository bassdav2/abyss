---
title: Abyss – Produktionsplan bis Version 1.0
created: 2026-09-21
status: Vorschlag, Umsetzung noch nicht gestartet
context: PM3 HS26
---

# Abyss – Produktionsplan bis Version 1.0

> **Richtungsänderung am 21.09.2026:** David hat Abyss zu einem Roguelite im Inneren eines sehr langen U-Boots weiterentwickelt. Für die weitere Planung gilt [Abyss v2 – Vom Heck bis zur Brücke](../praesentationen/archiv/2026-09-21_konzept-v2-roguelite/KONZEPT.md). Der folgende Plan dokumentiert die frühere Forschungssimulation.

**Empfehlung:** Ein atmosphärisches 2D-Spiel mit einem U-Boot, vier Abteilungen, vier handgebauten Missionen und etwa 35–45 Minuten Spielzeit für einen erfolgreichen ersten Durchlauf. Die Spielwelt läuft in JavaFX. Ein festes Blender-Modell und ein wiederverwendbarer Grafiksatz sichern die visuelle Konsistenz. Drei Wochen bleiben am Ende für Bedienung, Sound, Spielbalance, Fehler und Veröffentlichungspakete reserviert.

Die bestehenden Bilder definieren die gewünschte Atmosphäre. Sie sind noch keine fertigen Spielgrafiken: Boot, Räume, Wasser, Beleuchtung, Hintergrund und Oberfläche müssen separat steuerbar werden. Der genaue Detailgrad wird mit einer echten spielbaren Beispielmission geprüft, bevor die restlichen Grafiken produziert werden.

**Planungsannahmen:** PM3-Projekt, vorläufig sechs Teammitglieder, Desktop, Einzelspieler, Tastatur und Maus. Mac und Windows sind die geplanten Zielplattformen; mindestens ein konkreter Windows-Testrechner oder Runner muss zu Beginn verfügbar gemacht werden. Der Plan ist eine eigene Produktionsentscheidung, keine zusätzliche Kursvorgabe. Umfang und Aufwand sind nach dem ersten Prototyp neu zu schätzen.

**1. Was die fertige Version enthält**

- Vier Missionen inklusive eines interaktiven Tutorials. Eine kleine zusammenhängende Forschungsreise mit kurzen Briefings und einem Abschluss.
- Ein U-Boot mit Maschinenraum/Batterie, Pumpenraum, Labor und Cockpit. Die Raumgeometrie bleibt in allen Missionen gleich.
- Horizontales Fahren, Tauchen/Auftauchen über Ballast, Sammeln von Proben, Energieverbrauch, Kollisionen, Lecks, Wasserstände, Pumpen und Schotten.
- Eine nachvollziehbare Entscheidung zwischen weiteren Proben und rechtzeitiger Rückkehr.
- Missionsauswahl, Pause, Neustart, Erfolgs-/Teilerfolgs-/Fehlschlagsauswertung und ein verständlicher nächster Schritt nach jedem Ende.
- Lokaler Kampagnenfortschritt und Einstellungen. Gespeichert wird zwischen Missionen; ein beliebiger Zwischenspielstand während der Simulation gehört nicht zur ersten Version.
- Lautstärke für Effekte und Atmosphäre, Vollbild/Fenster, UI-Skalierung, abschaltbares Kamerawackeln und eine jederzeit erreichbare Steuerungshilfe.
- Eine installier- oder entpackbare Anwendung mit enthaltener Java-Laufzeit, Anleitung, Credits und Assetnachweisen.

**Umfangsgrenze für Version 1.0:** Vier Missionen, ein Boot, zwei wiederverwendbare Umgebungssets. Multiplayer, begehbare Crew, Bootsbau, Waffen, Monster, prozedurale Kampagne, komplexes Inventar, vollständige Strömungsmechanik und Live-3D sind spätere Erweiterungen. Diese Grenze schützt die Zeit für die ausdrücklich gewünschte Politur.

**2. Die vier Missionen**

| Mission | Zielzeit | Spielerlebnis | Was neu dazukommt |
|---|---:|---|---|
| 01 – Erster Tauchgang | 5–7 Min. | Im helleren Küstenwasser zwei Proben bergen und auftauchen | Fahren, Ballast, Proben, Anzeigen; kurze Hinweise genau bei Bedarf |
| 02 – Basaltgarten | 8–10 Min. | Drei Proben zwischen Felsen sammeln und eine Route wählen | Begrenzte Energie, vorsichtige Navigation; Kollisionen können ein kleines Leck verursachen |
| 03 – Bruchzone | 10–12 Min. | Nach einem angekündigten, reproduzierbaren Zwischenfall das Boot stabilisieren | Pumpenleistung, Schotten, verändertes Gewicht und Rückkehr unter Druck |
| 04 – Die letzte Probe | 12–15 Min. | Alle bisher gelernten Systeme nutzen; letzte Probe gegen Rückweg abwägen | Kombination bekannter Regeln, alternative sichere und riskante Routen; keine zusätzliche Grossmechanik |

Die Summe ist eine Zielgrösse für erfolgreiche Durchläufe. Lernzeit und Wiederholungen kommen hinzu. Die ersten beiden Missionen verwenden ein Küsten-/Basaltset, die letzten beiden ein Tiefsee-/Schluchtset. Routen, Sicht, Probenpositionen und Ereignisse unterscheiden die Missionen; zusätzliche Grafiksätze sind dafür nicht nötig.

Jede Mission hat Start, Ziele, Rückkehrzone, Kollisionsgeometrie, Ereignisauslöser, Ressourcenwerte und Endbedingungen als Daten. Ein Neustart setzt alles zuverlässig zurück. Für den Einstieg genügen kleine editierbare Leveldateien mit grafischer Debugansicht; ein eigener Level-Editor ist nicht eingeplant.

Erfolg bedeutet alle Pflichtproben plus sichere Rückkehr. Ein Teilerfolg sichert weniger Proben und zeigt, was erreicht wurde. Proben sind erst bei Rückkehr gesichert. Ein verlorenes Boot führt zu einer klaren Auswertung mit Ursache und Neustart. Jede Mission hat mindestens einen vorab getesteten Lösungsweg mit Reserven.

**3. Technik für PM3**

Empfohlener Projektstand: **OpenJDK 25, Gradle Wrapper 9.3.1 und JavaFX 26**. Vor dem ersten Projektcommit wird die konkrete verfügbare 26.x-Patchversion ausgewählt und festgeschrieben; keine dynamische `latest`-Abhängigkeit. JavaFX 26 benötigt mindestens JDK 24. Gradle 9.3.1 unterstützt JDK 25; für den separat installierten JDK 26 wäre mindestens Gradle 9.4 nötig. Der Wrapper und die IDE verwenden deshalb ausdrücklich JDK 25. [JavaFX-Anforderung](https://openjfx.io/highlights/26/) · [Gradle-Kompatibilität](https://docs.gradle.org/current/userguide/compatibility.html)

Der PM3-Kick-off nennt Java als erste Wahl, JavaFX als bevorzugte UI-Technologie, eine selbst definierte Architektur, getrennte Domänenlogik und keine architekturvorgebenden Frameworks oder relationale Datenbank. Spiele sind ausdrücklich erlaubt. JavaFX ist daher die geplante Spielplattform; Blender bleibt ein Werkzeug zur Assetproduktion. Die überschaubare Bibliotheksliste wird gemäss Kursvorgabe mit dem Fachdozenten abgestimmt. [Kick-off, PDF-S. 11–19](</Users/davidbass/Library/Mobile Documents/iCloud~md~obsidian/Documents/semester3wiki/Software-Projekt 3 HS26_20260914_0829/Einführung und Kick-off/Einführung und Kick-off.pdf>)

Die eigentliche 2D-Welt zeichnet ein JavaFX-Canvas. Menüs, Text, Schaltflächen und Regler werden als echte UI-Komponenten aufgebaut. Ein AnimationTimer treibt die Darstellung an; die Simulation verwendet eine feste Zeitschrittweite, zunächst 30 Schritte pro Sekunde. Rendering zielt auf 60 Bilder pro Sekunde und interpoliert bei Bedarf zwischen Zuständen. Nach Pause oder Fensterwechsel wird keine lange Zeitspanne auf einmal simuliert. [Canvas](https://openjfx.io/javadoc/26/javafx.graphics/javafx/scene/canvas/Canvas.html) · [AnimationTimer](https://openjfx.io/javadoc/26/javafx.graphics/javafx/animation/AnimationTimer.html)

```text
Eingaben und UI
      ↓ Befehle
Anwendungssteuerung: Mission starten, Probe bergen, Pumpen setzen, Schott schliessen
      ↓
Domänenmodell: Boot, Abteilungen, Energie, Ballast, Flutung, Missionsregeln
      ↓ Zustandsansicht und Ereignisse
Darstellung, Audio, Auswertung und lokale Speicherung
```

Das Domänenmodell kennt weder JavaFX noch Grafikdateien. Die Darstellung berechnet keine Energie- oder Flutungswerte selbst. Typische Klassen sind `Submarine`, `Compartment`, `Bulkhead`, `Battery`, `Pump`, `BallastTank`, `Leak`, `Sample`, `Mission` und `Simulation`. Einfache Ereignisse verbinden Simulation, HUD, Sound und Missionsprotokoll. Patterns entstehen aus konkreten Problemen, beispielsweise eine Zustandsmaschine für den Missionsablauf; eine vorgegebene Anzahl Patterns wird nicht künstlich eingebaut.

Das Modell bleibt bewusst begrenzt: Energie ist eine gemeinsame endliche Ressource; Pumpenleistung reduziert Wasser gegen Energieverbrauch; Wasser in Abteilungen erhöht Gewicht; geschlossene Schotten unterbrechen Wassertransfer. Ballast verändert den vertikalen Bewegungszustand. Eine vereinfachte Auftriebs-/Gewichtsrechnung oder dokumentierte Spielregel wird früh ausgewählt und durchgehend verwendet. Es gibt keine CFD-Flüssigkeitssimulation. Wasserstände und Bewegung müssen dieselben Modellwerte darstellen, auf denen auch die Spielentscheidungen beruhen.

Für einen fairen Energieausfall wird ein einmaliger manueller Notballast-Abwurf vorgesehen und bereits im Tutorial erklärt. Er schaltet keine leere Batterie wieder ein; seine Wirkung hängt vom Gewicht und der Flutung ab. Unrettbare Situationen enden verständlich. Ein jederzeit erreichbarer Missionsabbruch verhindert, dass der Spieler minutenlang ohne Handlungsmöglichkeit warten muss.

**4. Vorhandene Tools und tatsächlicher Installationsbedarf**

Am 21.09.2026 lokal geprüft:

| Werkzeug | Zweck im Projekt | Status und nächster Schritt |
|---|---|---|
| OpenJDK 25.0.2 | Einheitliche Java-Basis, Tests, Packaging | Vorhanden über Homebrew; Gradle verwendet es bereits. Explizit in IDE und Toolchain setzen |
| Gradle 9.3.1 | Wiederholbare Builds und Tests | Vorhanden. Wrapper in das neue Repository aufnehmen |
| JavaFX 26 | Welt, Oberfläche, Audio | Im untersuchten Gradle-Cache nicht vorhanden. Beim Projektstart als versionierte Abhängigkeit beziehen; kein separater grafischer Installer nötig |
| IntelliJ IDEA 2025.2.2 / VS Code | Entwicklung und Debugging | Beide vorhanden; eine gemeinsame Projektanleitung genügt |
| Blender 5.1.0 | Festes Boot, Bauteile, Materialien, gerenderte Animationen | Installiert; Version und CLI funktionieren. Kein zusätzlicher Blender-Connector erforderlich |
| Photoshop 2026 / 27.5.0 | Ebenen, Art-Review, manuelle Textur- und Kantenkorrekturen | Installation gefunden; Lizenz und interaktiver Betrieb hier nicht geprüft. Für den automatisierbaren Grundweg nicht zwingend |
| Integriertes Imagegen | Referenzen, Hintergründe, einzelne Textur-/Assetentwürfe | Verfügbar und für die Konzeptbilder bereits verwendet |
| Python | Blender-Skripte, Level-/Asset-Prüfungen, Manifeste, Buildhilfen | Vorhanden; Zusatzpakete nur in einer Projektumgebung, wenn tatsächlich nötig |
| FFmpeg 8.1 | Audiokonvertierung, Pegelprüfungen, Demo-Video | Vorhanden und Version geprüft |
| Git 2.51.0 | Quellcode, Konfiguration, Änderungen | Vorhanden. Für grosse Blender-/PSD-Quelldateien später Git LFS einplanen und dessen Installation dann prüfen |
| jpackage 25.0.2 | Anwendung mit eigener Laufzeit ausliefern | Im vorhandenen JDK enthalten und ausführbar |
| JUnit und eventuell kleiner JSON-Parser | Domänentests und lesbare Leveldateien | Projektabhängigkeiten, keine Desktop-Programme; genaue Versionen und Kursfreigabe bei Einrichtung |
| Audacity | Hörbarer Feinschliff an Loops und Effekten | In den üblichen App-Verzeichnissen nicht gefunden. Optional, nicht Voraussetzung für den Start |

Der Homebrew-JDK liegt unter `/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home`; zusätzlich sind JDK 24 und 26 installiert. Der festgestellte Blender-CLI-Pfad ist `/Applications/Blender.app/Contents/MacOS/Blender`. Diese lokalen Versionspfade sollten nicht ungeprüft auf Teamrechner übernommen werden.

**Installieren müssen wir vor dem ersten Prototyp voraussichtlich keine zusätzliche grosse Desktop-Anwendung.** JavaFX, Testbibliothek und Wrapper werden beim Einrichten des Projekts bezogen. Audacity ist später eine praktische kostenlose Ergänzung für Tonbearbeitung. Aseprite, Substance, Unity, Godot und ein Texturpaket-Abo sind für diesen Plan nicht nötig. [Audacity](https://www.audacityteam.org/faq/)

**Was Codex übernehmen kann:** Java-Code und Tests schreiben/ausführen, Blender über Python im Hintergrund steuern, Modelle und wiederholbare Exporte aufbauen, Imagegen für Referenzen und gezielte Bildvarianten nutzen, Assetdateien prüfen, Audio technisch verarbeiten und Builds testen. Blender bestätigt lokal `--background` und `--python` als unterstützte Optionen. Eine echte Render- und JavaFX-Probe erfolgt erst in der Umsetzung. Künstlerische Abnahme, Spielgefühl und die Prüfung auf einem zweiten Rechner brauchen zusätzlich sichtbare beziehungsweise spielbare Ergebnisse; ein Skript allein belegt deren Qualität nicht.

**5. Wie die Grafik konsistent bleibt**

Das wichtigste Prinzip: **Konsistenz entsteht durch dieselben Quelldateien, Geometrien, Materialien und Exportregeln.** Ein ähnlicher KI-Prompt garantiert keine identischen Räume, Schotten oder Proportionen. Neue Missionen verwenden deshalb dieselben bereits freigegebenen Assets.

**Schritt A – Eine verbindliche Stilseite.** Das korrigierte Erkundungsbild wird Hauptreferenz für das unbeschädigte Boot; Notfall- und Rückkehrbilder definieren Zustände und Stimmung. Festgeschrieben werden Seitenansicht, Raumreihenfolge, Bootsproportionen, Materialarten, Licht und UI. Vorgeschlagene Farbwerte: Tiefsee `#061117`, Stahl `#22343C`, Anzeigen `#BDECF4`, Arbeitslicht `#F5B55B`, Warnung `#F4514F`, positiver Zustand `#AADD88`. Das sind Designvorgaben, keine behaupteten Messwerte aus den Bildern. Alarm und Erfolg sind immer zusätzlich an Text und Symbol erkennbar.

**Schritt B – Ein Masterboot in Blender.** Ein flaches 3D-Querschnittsmodell dient als reproduzierbare Vorlage für 2D-Sprites. Vier Räume, drei Schotten, Rumpf, Pumpe, Batterie, Propeller und Probengreifer werden einmal sauber aufgebaut. Die orthografische Kamera, Rendergrösse und Bezugspunkte bleiben fest. Identische Materialien und Lichtvoreinstellungen werden wiederverwendet. Das Spiel benötigt Blender später nicht. [Orthografische Projektion im Blender-Handbuch](https://docs.blender.org/manual/id/5.1/editors/3dview/navigate/projections.html)

**Schritt C – Bewegliches von Unbeweglichem trennen.** Trockene Raumhintergründe, Rumpfvordergrund, Türen, Antrieb, Greifer und Lichtmasken werden getrennt exportiert. Wasser, Blasen, Lecks, Warnlicht und Scheinwerfer entstehen als überlagerte Effekte. Das Wasser wird auf die tatsächlich betroffene Raumform begrenzt. Dafür gibt es separate Masken beziehungsweise Polygone. So wird bei einem Leck nicht ein neues Boot generiert; derselbe Raum wird mit seinem aktuellen Wasserstand dargestellt.

```text
Hintergrundebenen → Umgebung/Kollisionen → trockene Räume
→ Wasser innerhalb der Raummasken → Geräte und Schotten
→ Rumpfvordergrund → Licht/Partikel → echte UI und Text
```

**Schritt D – Kleine Materialbibliothek.** Sechs Basisfamilien genügen: lackierter Stahl, gebürstetes Metall, Gummi, Glas, Basalt und Sediment. Jede erhält einen festgelegten Massstab, Farbton und Rauheitscharakter. Materialien werden in Blender wiederverwendet; Bildtexturen aus Imagegen werden zunächst als Entwürfe behandelt. Wiederholbare Oberflächen werden als Kachel geprüft, einschliesslich sichtbarer Nähte und eingebrannter Lichtflecken. Die Produktionsfreigabe erfolgt auf einem echten Bauteil in der Referenzbeleuchtung.

**Schritt E – Echte UI statt Text im Bild.** Alle Zahlen, Warnungen, Beschriftungen und Buttons werden im Spiel gesetzt. Fontvorschlag: IBM Plex Sans Condensed für Beschriftungen und IBM Plex Mono für Werte, jeweils mit mitgelieferter Lizenz. Lesbarkeit, Umlaute, Grössen und Abstände sind damit reproduzierbar. [IBM-Plex-Projekt und OFL](https://github.com/IBM/plex/blob/master/README.md?plain=1)

**Schritt F – Ein prüfbarer Export.** Zielansicht ist 1920×1080, zusätzlich geprüft bei 1280×720 und hoher Pixeldichte. Erste Assetgrössen: Boot ungefähr 1600×650 Pixel als logischer Produktionsrahmen; die tatsächlich sichtbare Grösse und nötige Detailreserve werden in der Beispielmission festgelegt. PNG mit Alpha für einzelne Elemente, zunächst normale PNG-Einzelbilder statt eines unnötig komplexen Atlas-Systems. Farbraum, Alpha-Behandlung, Exportmassstab und Renderer-Version stehen im Assetmanifest. Weiche Kanten werden gegen hellen und dunklen Hintergrund geprüft. Exportlauf erzeugt immer dieselben Dateinamen und Bezugspunkte.

Jedes Asset bekommt ID, Quelldatei, Version, Abmessungen, Mittelpunkt/Anker, Animationstakt und Herkunft. Physische Kollisionen und Interaktionspunkte stehen getrennt von den Texturen im Modell. Ein neues schönes Bild darf damit weder die Reichweite des Greifers noch die Geometrie des Boots verändern.

**Schritt G – Freigabe am Kontaktbogen und im Spiel.** Neue Assets werden gemeinsam mit Referenzboot, vorhandenen Felsen und HUD angesehen. Kontrolliert werden Perspektive, Materialmassstab, Helligkeit, Farbpalette und Detailgrad. Anschliessend erfolgt eine In-game-Prüfung bei normalem und rotem Licht. Prompts, angenommene Entwürfe und gezielte Korrekturen werden aufbewahrt. Automatische Prüfungen finden technische Fehler; stilistische Konsistenz wird zusätzlich visuell beurteilt.

**6. Begrenztes Asset- und Audiobudget**

| Bereich | Geplanter Umfang |
|---|---|
| Boot | Ein Mastermodell; vier Räume; drei Schotten; ein Greifer; ein Propeller; wiederverwendbare Leck-/Schadenseffekte |
| Materialien | Sechs freigegebene Basisfamilien |
| Umgebung | Zwei Sets; jeweils drei Hintergrundebenen; zusammen etwa 16–24 modulare Felsen und wenige Dekorationen |
| Forschung | Drei gut unterscheidbare Probentypen, visuell lesbare Greifpunkte |
| UI | Ein HUD, eine Systemansicht, Missionsauswahl, Pause/Einstellungen, Auswertung; etwa 16 konsistente Symbole |
| Animation | Propeller, Schotten, Greifer, Pumpenindikator; Wasserhöhe und Partikel bevorzugt aus Spielzustand statt vieler Einzelbilder |
| Ton | Etwa 12–16 kurze Effekte, drei leise Atmosphären-/Maschinenschleifen und ein zurückhaltendes Menü-/Abschlussmotiv |

Damit bleibt die Menge bei ungefähr 50–70 eigenständigen visuellen Bausteinen, abhängig von der Aufteilung, plus Animationsframes. Das ist eine Obergrenze für die Planung, keine Pflicht, jedes Teil einzeln neu zu erzeugen.

Audio macht Pumpenlast, Antrieb und Leck auch ohne Blick auf eine Zahl wahrnehmbar. Kurze Sonar-/UI-Töne können synthetisch erzeugt werden; Maschinen- und Wassergeräusche werden selbst aufgenommen oder mit klar dokumentierten Nutzungsrechten bezogen. Loop-Übergänge dürfen nicht klicken. Warnungen sollen verständlich bleiben und sich nicht unkontrolliert stapeln. Es ist kein spezielles KI-Audio-Tool als Voraussetzung eingeplant; FFmpeg und optional Audacity genügen für die Bearbeitung. Musik bekommt ein enges Budget, keine aufwendige adaptive Komposition.

**7. Bauphasen bis zur fertigen Version**

Die Wochen beziehen sich auf den lokalen HS26-Plan: M1 in SW3, M2 in SW9, M3 in SW13. Die genauen Abgabeuhrzeiten sind hier nicht erneut live geprüft. Vor dem Teamplan werden aktuelle Moodle-Termine abgeglichen. [PM3-Wochenplan](</Users/davidbass/Library/Mobile Documents/iCloud~md~obsidian/Documents/semester3wiki/Software-Projekt 3 HS26_20260914_0829/Allgemeines/Wochenplan PM3/data/image (27).png>)

| Phase | Zeitfenster | Ergebnis und Abnahmekriterium |
|---|---|---|
| 1. Umfang und technische Risiken | SW2–3, 21.09.–04.10. | M1-Unterlagen rechtzeitig zum tatsächlichen Termin. Repository, ein startbarer JavaFX-Build, klarer Spielablauf, Stilseite und sehr kleines Testlevel. Proben sammeln und zurückkehren funktionieren mit Platzhaltern. Ein Paket läuft früh auf dem zweiten Zielsystem |
| 2. Eine fertige Beispielmission | SW4–5, 05.–18.10. | Ein kurzer Durchlauf mit Bootsgrafik, Greifer, einem Leck, Wasser, Pumpen, HUD, Sound und Auswertung. Er muss optisch und spielerisch den Zielcharakter treffen. Erst dann Serienproduktion der übrigen Assets |
| 3. Vollständige Kernsysteme | SW6–7, 19.10.–01.11. | Schotten, Energie, Ballast und Flutung arbeiten zusammen. Missionen 2 und 3 sind spielbar; Einstellungen und Fortschritt funktionieren. Tests und UML werden parallel aktualisiert |
| 4. Alle Inhalte spielbar | SW8–9, 02.–15.11. | Für M2 liegt vor der echten Frist ein ausführbarer Beleg der wichtigen Use Cases vor. Interner Zielstand 08.11.: vier Missionen vom Start bis Ende spielbar, finale Assetliste, dokumentierte Architektur. Danach keine neuen grossen Funktionen |
| 5. Spieltests und Feinschliff | SW10–11, 16.–29.11. | Mindestens zwei Testrunden mit Personen ausserhalb des Entwicklerteams. Tutorial, Balance, Lesbarkeit, Audio, Animation und Performance korrigieren. Inhalte danach einfrieren |
| 6. Veröffentlichungskandidat | SW12 bis vor M3, ab 30.11. | Interner Zieltermin 06.12. für getestete Pakete, Installationsprüfung, Anleitung, Credits und Bericht II. M3-Unterlagen und Präsentation vor tatsächlicher Frist in SW13 fertig; verbleibende Tage nur für Fehlerkorrekturen |

**Die entscheidende Schranke ist Phase 2.** Wenn das schöne Boot nach zwei Wochen Art-Arbeit noch nicht kontrollierbar und in einer kurzen Mission vollständig spielbar ist, werden Detailgrad und Animation vereinfacht. Eine vollständige Mission mit hochwertigem Kern liefert die belastbare Grundlage für den Rest.

In jeder Phase werden Analyse, Implementierung und Tests verbunden. Dokumentation wird am tatsächlichen Code nachgeführt. Zu M2 gehören unter anderem Use Cases, Domänenmodell, Architektur, DCD, Interaktionsdiagramme für mindestens vier Systemoperationen und eine partielle Implementation der wichtigsten Use Cases. Zu M3 gehören der vollständige Quellcode mit Konfiguration und Javadoc sowie Bericht II. [M2-Auftrag, S. 2–3](</Users/davidbass/Library/Mobile Documents/iCloud~md~obsidian/Documents/semester3wiki/Software-Projekt 3 HS26_20260914_0829/Allgemeines/Arbeitsaufträge/Auftrag Lösungsarchitektur (M2).pdf>) · [M3-Auftrag, S. 1–3](</Users/davidbass/Library/Mobile Documents/iCloud~md~obsidian/Documents/semester3wiki/Software-Projekt 3 HS26_20260914_0829/Allgemeines/Arbeitsaufträge/Auftrag Prototyp und technischer Bericht II (M3).pdf>)

**8. Aufwand und Arbeitsteilung**

Grobe erste Schätzung: **600 Teamstunden ab Projektstart dieses Plans, einschliesslich 60 Stunden Reserve**. Bei sechs Personen sind das rund 100 Stunden pro Person beziehungsweise etwa neun Stunden pro Woche über elf Wochen. Das ist ein Planungswert, keine Zusage. Der Kurs nennt 120 Stunden pro Person für das gesamte Modul. Bereits geleistete Arbeit, Unterricht und weitere gebundene Zeit müssen vor Zusage vom realen Restbudget abgezogen werden. [Kick-off, PDF-S. 18](</Users/davidbass/Library/Mobile Documents/iCloud~md~obsidian/Documents/semester3wiki/Software-Projekt 3 HS26_20260914_0829/Einführung und Kick-off/Einführung und Kick-off.pdf>)

| Arbeitspaket | Personenstunden, erste Schätzung |
|---|---:|
| Analyse, Planung, UML, Berichte und Präsentation | 130 |
| Simulation und Spielregeln | 150 |
| Grafik und reproduzierbare Assetproduktion | 90 |
| UI, Bedienung und Zustandsanzeigen | 60 |
| Level, Balance und Audio | 50 |
| Integration, Spieltests und Auslieferung | 60 |
| Reserve für unbekannte Probleme | 60 |
| Gesamt | 600 |

Mögliche Verantwortlichkeiten: zwei Personen für Simulation/Architektur, eine für UI, eine für Grafikproduktion, eine für Missionen/Audio und eine für Integration/Tests/Release. Das sind Zuständigkeiten, keine isolierten Teilprojekte: Jede Funktion wird gemeinsam integriert und von einer zweiten Person überprüft; alle tragen zur Dokumentation bei. Falls weniger als etwa 500 Reststunden verfügbar sind, ist die erste Kürzung eine Kampagne aus drei Missionen und ein gemeinsames Umgebungsset. Tutorial, vollständiger Spielablauf und Qualitätsprüfung bleiben erhalten.

**9. Woran wir „polished und fertig“ messen**

- Alle vier Missionen lassen sich vom Hauptmenü bis zur Auswertung durchspielen; Erfolge, Teilerfolge, Abbruch und Fehlschläge funktionieren.
- Eine neue Testperson kann im Tutorial ohne mündliche Hilfe fahren, eine Probe bergen und zurückkehren. Ziel: mindestens vier von fünf neuen Testpersonen schaffen dies; Probleme werden beobachtet und korrigiert.
- Pumpen, Schotten, Ballast und Energie haben verständliche Auswirkungen. HUD, sichtbarer Wasserstand und Simulationsmodell widersprechen sich nicht.
- Domänentests prüfen Energiegrenzen, Wassertransfer bei offenen/geschlossenen Schotten, Pumpen ohne Energie, Probe genau einmal aufnehmen, Missionsende und vollständigen Neustart.
- Reproduzierbare Eingaben bei gleichem Zufallsstartwert liefern innerhalb des unterstützten Builds dieselben fachlichen Ergebnisse. Numerische Vergleiche verwenden definierte Toleranzen, keinen ungeprüften plattformübergreifenden Bitgleichheitsanspruch.
- Pause, Fokusverlust, Fenstergrösse, Vollbild und Soundeinstellungen verhalten sich zuverlässig. Wichtige Warnungen sind nicht allein durch Farbe oder Ton erkennbar.
- Beschriftungen sind bei 1280×720 und 1920×1080 lesbar. Höhere Pixeldichte und UI-Skalierung werden auf den Zielgeräten geprüft.
- Leistungsziel: überwiegend 60 FPS bei 1080p, 95. Perzentil der Framezeit unter 20 ms im vereinbarten Belastungsfall. Dieses Ziel wird auf Davids Mac und einem benannten schwächeren Teamgerät gemessen und bei Bedarf durch reduzierte Partikel/Beleuchtung erreicht; es ist noch kein Benchmarkresultat.
- Mindestens ein 60-minütiger Dauertest und wiederholte Missionswechsel ohne Absturz, hängenden Sound oder stetig wachsenden Speicherverbrauch.
- Ein frisches Benutzerkonto kann das ausgelieferte Paket ohne IDE und ohne separate Java-Installation starten. Fortschritt und Einstellungen überstehen einen Neustart; beschädigte Einstellungsdateien führen zu einer erklärten Rücksetzung.
- Alle Platzhalter, Testtexte und unbeabsichtigten Debuganzeigen sind entfernt. Fehlende Dateien führen nicht zu einem kommentarlosen schwarzen Bildschirm.
- Grafikexporte stimmen in Perspektive, Proportionen, Licht und Materialmassstab überein. Keine flackernden Schotten, Wasser ausserhalb der Räume oder sichtbaren Alpharänder.
- Sounds reagieren direkt, sind in der Lautstärke abgestimmt und werden sauber beendet. Übergänge, Eingabefeedback und Ergebnisbildschirm fühlen sich zusammengehörig an.
- Für alle verwendeten externen Fonts, Geräusche und Texturen sind Herkunft und Nutzung nachvollziehbar. Die Verwendung von KI wird in der vom Kurs verlangten Tabelle dokumentiert. [KI-Vorgaben, S. 1](</Users/davidbass/Library/Mobile Documents/iCloud~md~obsidian/Documents/semester3wiki/Software-Projekt 3 HS26_20260914_0829/Allgemeines/Verwendung von KI im PM3.pdf>)

**10. Auslieferung und Grenzen des Release-Plans**

Die erste fertige Version ist eine offline spielbare Desktop-Anwendung für Kurs und Team. `jpackage` kann Laufzeit und Anwendung gemeinsam verpacken. Mac- und Windows-Pakete müssen auf dem jeweiligen Zielsystem gebaut und geprüft werden; ein Mac-Build allein bestätigt Windows nicht. Zuerst werden entpackbare App-Images getestet. Ein Windows-Installer kann später zusätzliche Installer-Werkzeuge benötigen. [Offizielle Packaging-Dokumentation](https://docs.oracle.com/en/java/javase/26/jpackage/packaging-overview.html)

Öffentlicher Vertrieb mit signierten/notarisierten macOS-Paketen, einem Store-Auftritt und weiteren Betriebssystemen wäre ein zusätzlicher Release-Schritt. Konten, Zertifikate und deren Kosten sind in den 600 Stunden nicht eingeplant. Die grundsätzliche Paketierung und Startbarkeit werden trotzdem schon in Phase 1 ausprobiert, damit daraus am Ende kein unerwarteter Blocker wird.

**11. Konkrete erste fünf Arbeitstage nach dem Startentscheid**

1. **Tag 1:** Restbudget und Zielrechner bestätigen; vier Missionen und Regeln festlegen; Kursvorgaben/Bibliotheken abgleichen; neues Git-Repository ausserhalb des iCloud-Wiki anlegen, beispielsweise `/Users/davidbass/abyss`. Java-/Gradle-Versionen und Startanleitung festschreiben. Die bisherigen Konzeptbilder bleiben Referenzen.
2. **Tag 2:** Ein JavaFX-Fenster mit Canvas, HUD, Eingabe, fester Simulationszeit und kleinem Testlevel. Energieverbrauch und Bewegung sind sichtbar; erster Build auf dem zweiten Zielsystem.
3. **Tag 3:** Eine Probe bergen, zurückkehren und eine Auswertung sehen. Noch mit einfachen Formen. Parallel ein einzelnes Musterzimmer mit Material- und Lichtvorgaben in Blender herstellen.
4. **Tag 4:** Ein Leck, eine Pumpe und ein Schott bilden eine vollständig testbare Kette. Wasserstände entstehen aus dem Modell. Das Musterzimmer wird als echte separate Grafik in den laufenden Prototyp eingesetzt.
5. **Tag 5:** Drei- bis fünfminütige Beispielmission spielen, erste Person ausserhalb des Implementierers testen lassen, Framezeiten messen und den Assetweg bewerten. Danach Aufwand und Detailgrad verbindlich anpassen.

Die Tage sind Teamarbeitstage mit aufgeteilten Aufgaben und gemeinsamer Integration; sie behaupten keine fünf Arbeitstage für eine einzelne Person. Ab hier liefert jede weitere Woche eine spielbare Verbesserung.

**Ergebnis dieses Plans:** Vier überzeugende Missionen, ein stabiler visueller Stil und eine auslieferbare kleine Kampagne sind das Ziel. Der erste Umsetzungsschritt ist ein spielbarer Abschnitt mit einer freigegebenen Grafikqualität. Vollständige Assetproduktion und zusätzliche Inhalte folgen erst auf dieser überprüften Grundlage.
