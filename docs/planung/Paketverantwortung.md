# Paketverantwortung · Einarbeitung ins Team

Stand 12.10.2026. Der Prototyp ist weitgehend fertig und entstand grösstenteils KI-gestützt (siehe [KI_EINSATZ.md](../KI_EINSATZ.md)). Damit das ganze Team ihn verantworten kann (Risiko R1), übernimmt jede der sechs Personen ein Paket. Wer ein Paket übernimmt, erklärt es im Review, pflegt dessen Tests und setzt dort eigene Verbesserungen um.

Seit dem Review vom 13.10.2026 liegt der Schwerpunkt auf der technischen Qualität: Schichten, Domänenmodell, Patterns, Kohäsion und Kopplung. Die Aufgaben pro Paket sind deshalb Refactorings aus der [Architektur-Analyse](../ARCHITEKTUR_ANALYSE.md), keine neuen Inhalte.

Die PM3-Regeln zum KI-Einsatz verlangen, dass ein Resultat erst verwendet wird, wenn man es mit eigenen Worten begründen kann. Genau das ist das Ziel dieser Aufteilung.

## Spielregeln

1. **Feature-Stopp für KI-Ausbau:** Ab sofort keine grossen neuen Inhalte mehr per KI ohne Teamentscheid. Neue Arbeit entsteht in den Paketen (Risiko R2).
2. **Eigene Änderungen nur mit eigenem Test**, und eine zweite Person liest mit (Code-Review).
3. **KI-Einsatz weiter dokumentieren**, auch für kleine Hilfen (Tabelle in KI_EINSATZ.md).
4. **Aufwand pro Person festhalten**, Soll und Ist pro Iteration (Iterationsplan).

## Übersicht

| Paket | Inhalt | Umfang | Arbeitspakete (Rang) | Person |
|---|---|---|---|---|
| A | Spielfigur, Bewegung, Kampf | ≈ 4 900 Zeilen | 1, 2 | `[Name]` |
| B | Gegner und Bosse | ≈ 4 000 Zeilen | 6, 9 | `[Name]` |
| C | Boot, Räume, Belohnungen | ≈ 4 000 Zeilen | 3, 4, 8, 10 | `[Name]` |
| D | Anwendung, Fortschritt, Speicherung | ≈ 2 900 Zeilen | 5, 7, 12, 13 | `[Name]` |
| E | Oberfläche, Bedienung, Audio | ≈ 8 300 Zeilen | 11, 14 | `[Name]` |
| F | Grafik und Darstellung | ≈ 10 400 Zeilen | – (Q-03) | `[Name]` |
| quer | Architektur, Integration, Einarbeitung der anderen | – | – | David |

Die Zeilenzahlen beziehen sich auf `src/main/java/ch/zhaw/abyss`. Grafik und Audio sind gross, aber gleichförmig: Wer die Grundidee einer Klasse versteht, versteht die übrigen schnell.

---

## Paket A · Spielfigur, Bewegung, Kampf

**Klassen (domain):** `GameRun` (Aggregat, gemeinsam mit allen), `Player`, `PlayerMotor`, `Physics`, `Platform`, `InputFrame`, `Combat`, `Arsenal`, `Ballistics`, `Weapon`, `ActiveModule`, `Projectile`, `Swing`, `Beam`, `Actor`, `StatSheet`, `Status`, `StatusSet`
**Tests:** `MovementTest`, `CombatTest`, `GameRunTest`
**Einstieg:** `GameRun.update(seconds, input)`, danach `PlayerMotor` und `Combat`

**In zwei Minuten erklären**
- `GameRun` ist die einzige öffentliche Änderungsschnittstelle eines Tauchgangs. Die Oberfläche liefert pro Schritt einen `InputFrame`, und `GameRun` verteilt die Arbeit an paketinterne Mitarbeiter (`PlayerMotor`, `Arsenal`, `Ballistics` …).
- Die Simulation läuft mit festem Schritt 1/120 s (`GameWindow.STEP`). Darum verhält sich das Spiel auf schnellen und langsamen Rechnern gleich.
- Laufstege sind einseitig: Von unten springt man hindurch, mit Unten + Sprung fällt man hindurch.

**Mögliche Dozentenfragen:** Warum ist `GameRun` ein Aggregat? Warum ein fester Simulationsschritt statt der echten Bildzeit? Wie testet ihr Bewegung ohne Fenster?
**Eigene Aufgabe in Iteration 3:** `Player` kapseln (Felder `private`, sprechende Methoden) und `Combat` über eine schmale Schnittstelle statt über das ganze `GameRun` anbinden ([Analyse](../ARCHITEKTUR_ANALYSE.md), S2 und S3).

## Paket B · Gegner und Bosse

**Klassen (domain):** `Enemy`, `EnemyKind`, `EnemyBehavior`, `Behaviors`, `Brain`, `WardenBrain`, `ReactorBrain`, `BroodBrain`, `CaptainBrain`, `EmpressBrain`, `EnemyGrid`, `Telegraph`, `Threat`, `Lance`, `Affix`
**Tests:** `BossMechanicsTest`, `CaptainTest`, `SwarmTest`, `EvolutionTest`, `EndgameTest`
**Einstieg:** `Brain.update` (Schablonenmethode), danach eine Bossklasse, z. B. `CaptainBrain`

**In zwei Minuten erklären**
- Jeder Gegner folgt derselben Zustandsmaschine: Annähern → Ausholen mit Vorwarnung → Angriff → Erholung. `Brain.update` ist `final` und legt den Ablauf fest; Unterklassen überschreiben nur Einstiegspunkte (Schablonenmethode).
- `EnemyBehavior` ist die Strategie-Schnittstelle, `EnemyKind.behavior()` die Fabrikmethode, die jeder Art ihr Verhalten zuordnet.
- Jeder Angriff hat eine sichtbare Vorwarnung (`Telegraph`). Das ist die wichtigste Usability-Anforderung Q-04.

**Mögliche Dozentenfragen:** Warum Schablonenmethode und Strategie zugleich? Wie kommt ein neuer Gegner ins Spiel, ohne dass man `GameRun` ändert?
**Eigene Aufgabe in Iteration 3:** `Enemy` kapseln, sodass `Brain` Zustände nur noch über Methoden wechselt, und `Behaviors.java` in einzelne Dateien im Unterpaket `domain.enemy` aufteilen ([Analyse](../ARCHITEKTUR_ANALYSE.md), S1 und S3).

## Paket C · Boot, Räume, Belohnungen

**Klassen (domain):** `RoomGenerator`, `RoomPlan`, `RoomLayout`, `RoomTheme`, `RoomCondition`, `Hazard`, `Fixture`, `Machinery`, `Rewards`, `Loot`, `Offer`, `Item`, `Rarity`, `Synergy`, `ShrineDeal`, `SupplyCrate`, `Pickup`, `MetaBonus`, `RunSetup`, `DiverClass`
**Tests:** `RoomGeneratorTest`, `RewardTest`, `MachineryTest`, `RoomEventTest`
**Einstieg:** `RoomGenerator` (Konstruktor mit Seed, Zyklus, Druckstufe), danach `Rewards` und `GameRun.take(offer)`

**In zwei Minuten erklären**
- Der Generator baut aus einem Seed immer dieselbe Route: vier Sektionen mit je sechs Räumen, Wächter und Werkstätten an festen Stellen. Deshalb lassen sich Tauchgänge reproduzieren und testen (Generator-Tests über viele Seeds).
- Nach einem gesicherten Raum bietet die Bergung drei bis vier Angebote nach Seltenheit; pro Bergung gibt es genau eine Wahl.
- Raumtechnik (Presse, Laser, Notschalter) ist ein Mitarbeiter des Aggregats und trifft Figur und Gegner gleichermassen.

**Mögliche Dozentenfragen:** Wie stellt ihr sicher, dass jede Route spielbar ist? Warum feste Wächterpositionen?
**Eigene Aufgabe in Iteration 3:** den Raumablauf (`take`, `interaction`, `chooseNextRoom`) aus `GameRun` in eine eigene Klasse ziehen und die `switch (kind)` in `RoomGenerator`, `Hazard` und `Fixture` durch Polymorphie ersetzen ([Analyse](../ARCHITEKTUR_ANALYSE.md), S1 und S4).

## Paket D · Anwendung, Fortschritt, Speicherung

**Klassen:** `application`: `GameService`, `Profile`, `Loadout`, `Unlock`, `Achievement`, `Career`, `SkillTree`, `Cosmetics`, `Settings` · `ports/GameRepository` · `infrastructure/FileGameRepository` · `domain/RunCheckpoint`
**Tests:** `GameServiceTest`, `FileGameRepositoryTest`, `CareerTest`, `CareerWeaponTest`
**Einstieg:** `GameService.start(loadout, seed)` und `GameService.resume()`

**In zwei Minuten erklären**
- `GameService` setzt die Use Cases um (Tauchgang starten, fortsetzen, beenden) und kennt die Speicherung nur über den Port `GameRepository` (Port/Adapter). Darum lässt sich die Domäne ohne Dateisystem testen (Q-01).
- `FileGameRepository` speichert versioniert (aktuell Version 3), migriert alte Stände und sichert ein Original vor dem ersten Überschreiben als `.bak` (Q-02).
- Gesichert wird am Raumeingang (`RunCheckpoint`); ein Unterbruch kostet höchstens den aktuellen Raum.

**Mögliche Dozentenfragen:** Warum ein Port statt direkter Dateizugriffe? Was passiert bei einer beschädigten Datei?
**Eigene Aufgabe in Iteration 3:** `ArchitectureTest` schreiben, der die Schichtregeln über die Importe prüft, und `GameRepository` aus `ports` nach `application` verschieben ([Analyse](../ARCHITEKTUR_ANALYSE.md), S5 und S7).

## Paket E · Oberfläche, Bedienung, Audio

**Klassen:** `ui`: `GameWindow`, `MenuScreens`, `RunScreens`, `Navigator`, `InputController`, `PixelView`, `AbyssApplication` · `ui/gui/Gui` · `ui/render/HudRenderer` · `infrastructure/AudioSystem` und `infrastructure/music/*`
**Tests:** `InputControllerTest`, `MusicEngineTest`, `MusicCueTest`; Oberflächenprüfung mit `./gradlew uiSmoke`
**Einstieg:** `GameWindow` (Spielschleife, fester Schritt), danach `Gui` und `Navigator`

**In zwei Minuten erklären**
- Alle Menüs sind als „Bordsystem“ direkt ins Pixelbild gezeichnet: `Gui` ist eine Immediate-Mode-Oberfläche, die mit der Spielschleife läuft, vollständig mit Maus und Tastatur.
- `Navigator` wechselt zwischen den Bildschirmen (siehe Navigationsdiagramm `docs/diagrams/12-navigation`).
- Die Musik entsteht zur Laufzeit aus Partituren (`Scores`) und folgt der Intensität des Spiels (`MusicDirector`). Klänge laufen über einen eigenen Mischer mit fester Stimmenzahl (`SoundMixer`), damit es nicht ruckelt.

**Mögliche Dozentenfragen:** Warum keine JavaFX-Steuerelemente für die Menüs? Wie hängt die Oberfläche von der Domäne ab, und umgekehrt?
**Eigene Aufgabe in Iteration 3:** einen Startpunkt (Composition Root) einführen, der die Adapter erzeugt und übergibt, damit `ui` nichts mehr aus `infrastructure` importiert; danach `MenuScreens` und `RunScreens` in kleinere Klassen zerlegen ([Analyse](../ARCHITEKTUR_ANALYSE.md), S1 und S6).

## Paket F · Grafik und Darstellung

**Klassen:** `ui/pixel`: `Frame`, `LightMap`, `PixelFont`, `PostProcess`, `Sprite` · `ui/art/*` (prozedurale Pixel-Art) · `ui/render` ohne `HudRenderer`: `WorldRenderer`, `Effects`, `EventEffects`, `ActorPainter`, `MachinePainter`, `Camera`, `Ocean`, `SubmarineScene`, `SpriteBank`, `ScreenFeel`
**Tests:** `FrameTest`, `RenderSmokeTest`; Messung mit `./gradlew renderSoak`, Übersichtsbögen mit `artSheet`
**Einstieg:** Render-Pipeline `docs/diagrams/13-render-pipeline`, danach `WorldRenderer`

**In zwei Minuten erklären**
- Gezeichnet wird in einen eigenen Software-Framebuffer (`Frame`). `PixelView` vergrössert ihn ganzzahlig und lädt ihn als JavaFX-Bild hoch.
- Alle Grafiken werden im Code erzeugt (`ui/art`), es gibt keine Bilddateien. Licht entsteht über eine Lichtkarte (`LightMap`), danach folgen Nachbearbeitungseffekte (`PostProcess`).
- Darstellung und Effekte verändern keine Spielregeln: Sie lesen nur die Ereignisliste (`GameEvent`, Beobachter im Pull-Verfahren).

**Mögliche Dozentenfragen:** Warum ein eigener Framebuffer statt JavaFX-Formen? Wie messt ihr die Performance Q-03?
**Eigene Aufgabe in Iteration 3:** `WorldRenderer` in Ebenen zerlegen und die Zeichner pro Art über eine Zuordnung registrieren statt `switch (kind)`; Performance Q-03 vorher und nachher messen ([Analyse](../ARCHITEKTUR_ANALYSE.md), S1 und S4).

---

## Bis zum Review (pro Person, etwa 1 bis 2 Stunden)

1. Paket wählen und sich in der Übersicht oben eintragen.
2. Spiel auf dem eigenen Rechner bauen und starten (`./gradlew run`) und Betriebssystem plus Ergebnis melden. Das ist der erste echte Nachweis für Q-06.
3. Einen Tauchgang spielen und drei Beobachtungen notieren (Was war unklar? Was war unfair? Was hat Spass gemacht?).
4. Die eigene Paketseite lesen, den Einstieg im Code öffnen und eine Minute Erklärung vorbereiten.
5. Den eigenen Aufwand in Iteration 2 ehrlich in Stunden angeben.
