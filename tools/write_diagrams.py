"""Erzeugt editierbare UML-Quellen, die den implementierten Stand beschreiben, und rendert sie
mit PlantUML als SVG und PNG. Die Risikomatrix entsteht direkt als SVG (PNG über rsvg-convert)."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/diagrams'
OUT.mkdir(parents=True, exist_ok=True)
for old in OUT.glob('*'):
    old.unlink()
STYLE = '''@startuml
skinparam backgroundColor #FFFFFF
skinparam defaultFontName Arial
skinparam defaultFontSize 14
skinparam shadowing false
skinparam roundcorner 8
skinparam ArrowColor #355863
skinparam classBackgroundColor #EAF3F2
skinparam classBorderColor #355863
skinparam participantBackgroundColor #EAF3F2
skinparam participantBorderColor #355863
skinparam sequenceLifeLineBorderColor #7A979C
skinparam noteBackgroundColor #FFF2DA
skinparam noteBorderColor #D8AA64
skinparam packageBackgroundColor #F5F8F7
skinparam packageBorderColor #6E8B91
skinparam stateBackgroundColor #EAF3F2
skinparam stateBorderColor #355863
hide empty members
'''
DIAGRAMS = {
'01-use-cases': r'''left to right direction
actor "Spieler/in" as Player
rectangle "ABYSS · Desktopspiel für macOS, Windows und Linux" {
 usecase "UC-01\nTauchgang spielen" as Dive
 usecase "UC-02\nFortschritt freischalten\nund Figur gestalten" as Progress
 usecase "UC-03\nSpiel einstellen" as Options
}
Player --> Dive
Player --> Progress
Player --> Options
note right of Dive
 Kernfall (Rang 1), Varianten als Erweiterungen:
 Bergung wählen, in Sonderräumen handeln,
 unterbrechen und fortsetzen, Brücke erobern
 und nächster Zyklus, nach Niederlage erneut tauchen
end note
note right of Progress
 Rang 2, setzt UC-01 voraus
 (Datenkerne aus Tauchgängen)
end note
note right of Options
 Rang 3, unabhängig
end note
''',
'02-domain': r'''title Konzeptuelles Domänenmodell (Fachbegriffe)
class Tauchgang {
 Seed
 Zyklus
 Druckstufe
 Tauchzeit
}
class Boot
class Sektion {
 Name
}
class Raum {
 Tiefe
 Art
 Thema
}
class Raumzustand {
 Stromausfall
 Alarm
 Druckleck
 Hüllenbruch
 Schlagseite
}
class Raumtechnik {
 Art
 Takt
}
class Welle
class Gegner {
 Integrität
 Zustand
}
class Wächter
class "Taucher/in" as Diver {
 Integrität
 Energie
 Schrott
 Datenkerne
}
class Klasse
class Waffe {
 Stufe
}
class "Aktives Modul" as Ability
class "Passives Modul" as Item {
 Seltenheit
 Stufe
}
class Angebot {
 Preis
}
class Laufsteg
class Gefahr
class Beute
class Profil {
 Freischaltungen
 Aussehen
 Statistik
}
class Resonanz {
 Bonus
}
class "Logbuch-Eintrag" as Achievement {
 Bedingung
 Kerne
}
class "Raum-Sicherung" as Checkpoint
Boot "1" *-- "4" Sektion
Sektion "1" *-- "6" Raum
Tauchgang --> Boot
Tauchgang "1" *-- "1" Diver
Tauchgang --> "1" Raum : aktueller
Raum *-- "0..3" Welle
Welle *-- "1..6" Gegner
Wächter --|> Gegner
Raum *-- "0..*" Laufsteg
Raum *-- "0..*" Gefahr
Raum o-- "0..*" Beute
Raum o-- "0..6" Angebot
Diver --> Klasse
Diver --> Waffe
Diver --> Ability
Diver o-- "0..*" Item
Angebot ..> Item
Angebot ..> Waffe
Profil ..> Klasse : schaltet frei
Profil ..> Item : schaltet frei
Profil o-- "0..*" Achievement : erreicht
Raum --> "0..1" Raumzustand
Raum *-- "0..3" Raumtechnik
Resonanz --> "2" Item : verbindet
Tauchgang ..> Checkpoint : beim Raumeingang
''',
'03-architecture': r'''title Logische Architektur: Schichten und Abhängigkeiten (nach innen)
skinparam componentStyle rectangle
package "Präsentation (ui, JavaFX)" {
 [Bildschirme, Spielschleife, Eingabe\n(GameWindow, Screens, Gui)] as Screens
 [Darstellung\n(render, art, pixel)] as Render
}
package "Anwendung (application)" {
 [GameService\n«Controller»] as GS
 [Profile, Loadout, Unlock,\nCareer, SkillTree] as Meta
 [GameRepository\n«Interface, Port»] as Port
}
package "Domäne (domain, reines Java)" {
 [GameRun\n«Aggregat»] as Run
 [Mitarbeiter: PlayerMotor, Combat, Arsenal,\nBallistics, Loot, Rewards, Machinery] as Coll
 [EnemyBehavior\n«Interface, Strategie»] as Beh
 [Brain «Schablonenmethode»\n5 Wächter, 23 Gegnerarten] as Brain
 [RoomGenerator] as Gen
 [Wertobjekte: RoomPlan, RunCheckpoint,\nStatSheet, Offer] as VO
 [GameEvent-Liste] as Ev
}
package "Infrastruktur (infrastructure)" {
 [FileGameRepository\n«Adapter»] as File
 [AudioSystem, MusicEngine] as Audio
}
Screens -down-> GS : Systemoperationen
Screens -right-> Render
Render .down.> Run : liest Zustand
GS -down-> Run
GS -right-> Meta
GS -down-> Port
File .up.|> Port
Audio .left.> Ev : liest
Run *-down- Coll
Run -down-> Beh
Brain .right.|> Beh
Run -down-> Gen
Run -down-> VO
Run -right-> Ev
Screens .right.> File
legend bottom
 Abhängigkeiten zeigen nach innen: Präsentation → Anwendung → Domäne.
 Die Domäne importiert weder JavaFX noch Dateien noch andere Schichten; ArchitectureTest prüft das.
 Der Port liegt in der Anwendung, Infrastruktur implementiert ihn (Port/Adapter): keine Paketzyklen.
 Die Präsentation erzeugt die Adapter beim Start (bekannte Abweichung, im Test festgehalten).
endlegend
''',
'04-design-classes': r'''title Design-Klassendiagramm: Kern der Domänenschicht
skinparam classAttributeIconSize 0
skinparam packageStyle rectangle
package "application" {
 class GameService <<Controller>> {
  - repository: GameRepository
  - run: GameRun
  + start(loadout, seed): GameRun
  + resume(): Optional<GameRun>
  + step(seconds, input)
  + chooseNextRoom(branch): boolean
  + take(offer): boolean
  + acceptDeal(deal): boolean
  + saveRoom()
  + recordOutcome()
 }
 interface GameRepository <<Port>> {
  + loadProfile(): Profile
  + loadCheckpoint(): Optional<RunCheckpoint>
  + saveCheckpoint(checkpoint)
  + clearCheckpoint()
 }
}
package "infrastructure" {
 class FileGameRepository <<Adapter>>
}
package "domain" {
 class GameRun <<Aggregat>> {
  ~ phase: Phase
  ~ room: RoomPlan
  + update(seconds, input)
  + take(offer): boolean
  + chooseNextRoom(branch): boolean
  + checkpoint(): RunCheckpoint
  + drainEvents(): List<GameEvent>
 }
 class Combat {
  ~ hitEnemy(enemy, base, source, knockback, x): double
  ~ applyStatus(enemy, status, seconds)
 }
 abstract class Body {
  ~ x, y, vx, vy: double
  ~ fall(gravity, maxFall, dt)
  ~ advance(dt)
  + id(): long
 }
 abstract class Actor {
  ~ health, maxHealth: double
  ~ move(layout, dt)
  + bounds(): Bounds
 }
 class Player
 class Projectile
 class Pickup
 class Enemy {
  ~ kind: EnemyKind
  ~ state: State
  ~ {static} create(id, kind, affix, x, y, strength, delay): Enemy
  ~ stun(seconds): boolean
  ~ knockbackFactor(): double
  ~ limitDamage(damage): double
  ~ expose(state, seconds): boolean
  ~ onDefeated(run)
  + armored(): boolean
 }
 class Boss {
  ~ phaseFloor(): double
  ~ stun(seconds): boolean
  ~ knockbackFactor(): double
  ~ limitDamage(damage): double
  ~ expose(state, seconds): boolean
  ~ onDefeated(run)
  + armored(): boolean
 }
 interface EnemyBehavior <<Strategie>> {
  ~ update(enemy, run, dt)
 }
 abstract class Brain <<Schablonenmethode>> {
  + update(enemy, run, dt) {final}
  ~ windup(enemy, run, dt)
  ~ strike(enemy, run, dt)
 }
 class "WardenBrain, CaptainBrain, …\n28 Gegnerverhalten" as Brains
 enum EnemyKind {
  ~ behavior(): EnemyBehavior
  + boss(): boolean
 }
}
GameService --> GameRepository
GameService --> "0..1" GameRun : steuert
FileGameRepository ..|> GameRepository
GameRun *-- "1" Player
GameRun *-- "0..*" Enemy
GameRun *-- "0..*" Projectile
GameRun *-- "0..*" Pickup
GameRun *-- "1" Combat
Combat ..> Enemy
Body <|-- Actor
Body <|-- Projectile
Body <|-- Pickup
Actor <|-- Player
Actor <|-- Enemy
Enemy <|-- Boss
Enemy --> "1" EnemyKind
Enemy --> "1" EnemyBehavior
EnemyKind ..> EnemyBehavior : Fabrikmethode
EnemyBehavior <|.. Brain
Brain <|-- Brains
legend bottom
 + öffentlich   ~ paketintern   - privat. Paketinterne Felder auf private umstellen ist der nächste Schritt.
endlegend
''',
'05-ssd-progress': r'''title Systemsequenzdiagramm UC-01 Tauchgang spielen (Raumschleife, Schritte 3–7)
actor "Spieler/in" as P
participant ":ABYSS" as S
loop bis der Raum gesichert ist
 P -> S : bewegen, springen, ausweichen, angreifen, Modul
 S --> P : Bild, Vorwarnungen, Treffer, Welle
end
S --> P : Raum gesichert, Bergung und Schott offen
P -> S : interagieren (E) an der Bergungskapsel
S --> P : Angebote (3–4 Module oder Waffe)
P -> S : take(angebot)
S --> P : installiert
P -> S : interagieren (E) am Schott
S --> P : nächste Räume (1–2)
P -> S : chooseNextRoom(abzweig)
S --> P : neuer Raum, Raum-Sicherung geschrieben
''',
'06-start-sequence': r'''title Sequenz: Tauchgang beginnen (UC-01, Schritte 1–2)
actor "Spieler/in" as P
participant ":MenuScreens" as M
participant ":GameWindow" as W
participant ":GameService" as S
participant ":Profile" as Pr
participant ":GameRun" as R
participant ":GameRepository" as Repo
P -> M : TAUCHEN
M -> W : startRun(loadout, seed)
W -> S : start(loadout, seed)
S -> Pr : owns(diver), owns(module), maxPressure
S -> R ** : new GameRun(RunSetup)
R -> R : enterRoom(room(0,0))
S -> Repo : saveProfile(profile)
S -> Repo : saveCheckpoint(run.checkpoint())
S --> W : run
W -> W : play()
''',
'07-combat-sequence': r'''title Sequenz: Werkzeugtreffer in einem Simulationsschritt
participant ":GameWindow" as W
participant ":GameRun" as R
participant ":Combat" as C
participant ":Enemy" as E
participant ":WorldRenderer" as V
W -> R : update(1/120 s, input)
R -> R : updatePlayer(): startSwing / updateSwing
alt aktives Trefferfenster
 R -> C : hitEnemy(enemy, schaden, MELEE, rückstoss, x)
 C -> C : Kritik, Module, Panzerung, Zustände
 C -> E : Integrität senken, Rückstoss
 opt besiegt
  C -> R : Beute, Kerne, Auslöser-Module
 end
 C -> R : emit(HIT / CRIT)
end
R -> E : behavior.update(): Vorwarnung, Angriff
W -> R : drainEvents()
W -> V : event(e) → Funken, Zahlen, Trefferpause
''',
'08-reward-sequence': r'''title Sequenz: Bergung wählen (UC-01, Schritt 5)
actor "Spieler/in" as P
participant ":GameWindow" as W
participant ":GameRun" as R
participant ":Player" as Pl
P -> W : E
W -> R : interaction()
R --> W : REWARD
W -> W : reward() zeigt offers()
P -> W : Angebot 2
W -> R : take(offers().get(1))
R -> R : Phase, Angebot, Preis prüfen
R -> Pl : install(item) / weapon wechseln
Pl -> Pl : recompute(): StatSheet
R -> R : rewardAvailable = false
R --> W : true
W -> W : play()
''',
'09-room-sequence': r'''title Sequenz: Raumwechsel und Sicherung
participant ":RunScreens" as U
participant ":GameRun" as R
participant ":RoomGenerator" as G
participant ":GameService" as S
participant ":GameRepository" as Repo
U -> R : chooseNextRoom(abzweig)
R -> R : Phase ROOM_CLEARED prüfen
R -> G : choices(tiefe + 1)
G --> R : RoomPlan
R -> R : enterRoom(plan): Figur, Gefahren, Kisten, Checkpoint
R -> R : spawnWave() oder clearRoom()
U -> S : saveRoom()
S -> Repo : saveCheckpoint(run.checkpoint())
''',
'10-resume-sequence': r'''title Sequenz: Fortsetzen (UC-01, Erweiterung 1a)
participant ":GameWindow" as W
participant ":GameService" as S
participant ":FileGameRepository" as F
participant "GameRun" as R
S -> F : loadCheckpoint()
F --> S : RunCheckpoint (Format 3, ggf. migriert)
W -> S : resume()
S -> R : restore(checkpoint, itemPool, weaponPool)
R -> R : Werte prüfen, Route nachbauen, enterRoom()
R --> S : run
S --> W : Optional(run)
''',
'11-enemy-state': r'''title Zustandsmaschine eines Gegners (Brain)
[*] --> SPAWNING
SPAWNING --> APPROACH : Auftritt vorbei
APPROACH --> WINDUP : bereit und Abklingzeit 0\n/ Vorwarnung
WINDUP --> STRIKE : Ausholzeit abgelaufen
STRIKE --> RECOVER : Angriff beendet
RECOVER --> APPROACH : Erholung vorbei
APPROACH --> STUNNED : Treffer / Impuls
STUNNED --> APPROACH
APPROACH --> HIDDEN : Aal taucht ab
HIDDEN --> WINDUP : Figur in Reichweite
note right of RECOVER : Bosse: Panzerung aus,\nKern offen
''',
'12-navigation': r'''title Bildschirmnavigation
[*] --> Titel
Titel --> Vorbereitung
Titel --> Archiv
Titel --> Garderobe
Titel --> Optionen
Titel --> Steuerung
Titel --> Spiel : Fortsetzen
Vorbereitung --> Garderobe
Vorbereitung --> Spiel : Tauchen
Spiel --> Pause : Esc / Fokusverlust
Pause --> Spiel
Pause --> Ausrüstung
Pause --> Bootskarte
Pause --> Titel
Spiel --> Ausrüstung : I / Tab
Spiel --> Bootskarte : M
Spiel --> Bergung : E an Kapsel / Händlerin / Werkstatt
Spiel --> Kapelle : E am Altar
Spiel --> Route : E am Schott
Bergung --> Spiel
Kapelle --> Spiel
Route --> Spiel : Raumwahl
Spiel --> Ergebnis : Sieg / Niederlage
Ergebnis --> Spiel : nächster Zyklus / erneut
Ergebnis --> Archiv
Ergebnis --> Titel
''',
'13-render-pipeline': r'''title Pixel-Renderpipeline pro Bild (480 x 270)
start
:Meer mit Parallaxe, Lichtschächten, Meeresschnee;
:Raumstreifen aus RoomArt kopieren (Kamera);
:Animierte Requisiten, Gefahren, Kisten, Bergung, Schott;
:Vorwarnungen, Gegner, Figur, Geschosse, Beute, Hiebspuren;
:Partikel, Ringe, Blitze, Explosionen in Farb- und Leuchtebene;
:Lichtkarte: Grundlicht, Lampen, Stirnlampe, Augen, Blitzlicht;
:Licht gestuft mit Bayer-Raster anwenden + Leuchtebene addieren;
:Vordergrundsilhouetten;
:Bloom, Farbstimmung der Sektion, Vignette, Aberration, Blitz;
:Schadenszahlen, Hinweise, HUD in Pixelschrift;
:PixelView: WritableImage, Nachbarpixel-Skalierung, Röhrenfilter;
stop
''',
'14-arbeitspakete': r'''left to right direction
skinparam defaultFontSize 17
skinparam rectangleBackgroundColor #EAF3F2
skinparam rectangleBorderColor #355863
skinparam packageStyle rectangle
package "UC-01 Tauchgang spielen" {
 rectangle "1 Raum\nbewältigen" as P1
 rectangle "2 Tauchgang\nbeginnen" as P2
 rectangle "3 Raum sichern,\nRoute wählen" as P3
 rectangle "4 Bergung\nwählen" as P4
 rectangle "5 Niederlage,\nerneut tauchen" as P5
 rectangle "6 Wächter-\nkampf" as P6
 rectangle "7 Unterbrechen,\nfortsetzen" as P7
 rectangle "8 Handeln" as P8
 rectangle "9 Brücke,\nnächster Zyklus" as P9
 rectangle "10 Raumzustände,\nRaumtechnik" as P10
 rectangle "11 Ausrüstung,\nKarte ansehen" as P11
}
package "UC-02 Freischalten und gestalten" {
 rectangle "12 Archiv,\nLogbuch" as P12
 rectangle "13 Garderobe" as P13
}
package "UC-03 Einstellen" {
 rectangle "14 Optionen" as P14
}
P1 --> P3
P2 --> P3
P3 --> P4
P1 --> P5
P1 --> P6
P3 --> P7
P4 --> P8
P3 --> P9
P6 --> P9
P3 --> P10
P4 --> P11
P5 --> P12
P12 --> P13
legend right
 Zahl = relativer Rang (1 = zuerst)
 Pfeil: Voraussetzung → abhängiges Paket
endlegend
'''
}

# Risikoliste R1–R8 (Stand Iteration 2): (Nr., Kurztext, Eintrittswahrscheinlichkeit, Schadenspotenzial)
# mit 0 = niedrig, 1 = mittel, 2 = hoch. Quelle der Einschätzung: docs/PROJEKTMANAGEMENT.md#risikoliste.
RISKS = [
    ('R1', 'Team versteht KI-Prototyp nicht', 2, 2),
    ('R2', 'Umfang wächst zu stark', 1, 2),
    ('R3', 'Steuerung überzeugt nicht', 1, 1),
    ('R4', 'KI-Einsatz nicht akzeptiert', 0, 2),
    ('R5', 'Darstellung zu langsam', 0, 1),
    ('R6', 'Probleme unter Windows/Linux', 1, 1),
    ('R7', 'Ausfall im Team', 1, 1),
    ('R8', 'Spielstände beschädigt', 0, 1),
    ('R9', 'Technische Qualität ungenügend', 1, 2),
    ('R10', 'Refactoring bricht das Spiel', 0, 1),
]


def risk_matrix():
    """Zeichnet die Risikomatrix Eintrittswahrscheinlichkeit × Schadenspotenzial als SVG (druckgross beschriftet)."""
    W, H = 1040, 520
    x0, y0, cell_w, cell_h = 150, 72, 160, 118
    levels = ['niedrig', 'mittel', 'hoch']
    fills = ['#DCEFD9', '#EEF3D2', '#FBEFD2', '#F7D9B8', '#F2C2B6']
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" '
           'font-family="Arial, Helvetica, sans-serif">',
           f'<rect width="{W}" height="{H}" fill="#FFFFFF"/>',
           f'<text x="{W / 2}" y="38" font-size="24" font-weight="bold" fill="#102B35" text-anchor="middle">'
           'Risikomatrix ABYSS (Stand Iteration 3)</text>']
    for row in range(3):
        for col in range(3):
            impact = 2 - row
            x, y = x0 + col * cell_w, y0 + row * cell_h
            out.append(f'<rect x="{x}" y="{y}" width="{cell_w}" height="{cell_h}" '
                       f'fill="{fills[col + impact]}" stroke="#FFFFFF" stroke-width="3"/>')
    for i, name in enumerate(levels):
        out.append(f'<text x="{x0 + i * cell_w + cell_w / 2}" y="{y0 + 3 * cell_h + 28}" font-size="20" '
                   f'fill="#355863" text-anchor="middle">{name}</text>')
        out.append(f'<text x="{x0 - 12}" y="{y0 + (2 - i) * cell_h + cell_h / 2 + 7}" font-size="20" '
                   f'fill="#355863" text-anchor="end">{name}</text>')
    out.append(f'<text x="{x0 + 1.5 * cell_w}" y="{y0 + 3 * cell_h + 62}" font-size="21" font-weight="bold" '
               f'fill="#102B35" text-anchor="middle">Eintrittswahrscheinlichkeit</text>')
    out.append(f'<text transform="translate(30 {y0 + 1.5 * cell_h}) rotate(-90)" font-size="21" '
               f'font-weight="bold" fill="#102B35" text-anchor="middle">Schadenspotenzial</text>')
    cells = {}
    for nr, _, p, i in RISKS:
        cells.setdefault((p, i), []).append(nr)
    for (p, i), nrs in cells.items():
        cx, cy = x0 + p * cell_w + cell_w / 2, y0 + (2 - i) * cell_h + cell_h / 2
        for k, nr in enumerate(nrs):
            dx = (k - (len(nrs) - 1) / 2) * 50
            out.append(f'<circle cx="{cx + dx}" cy="{cy}" r="23" fill="#355863"/>')
            out.append(f'<text x="{cx + dx}" y="{cy + 6}" font-size="17" font-weight="bold" fill="#FFFFFF" '
                       f'text-anchor="middle">{nr}</text>')
    lx = x0 + 3 * cell_w + 36
    for k, (nr, text, _, _) in enumerate(RISKS):
        y = y0 + 14 + k * 40
        out.append(f'<circle cx="{lx + 17}" cy="{y}" r="17" fill="#355863"/>')
        out.append(f'<text x="{lx + 17}" y="{y + 5}" font-size="14" font-weight="bold" fill="#FFFFFF" '
                   f'text-anchor="middle">{nr}</text>')
        out.append(f'<text x="{lx + 44}" y="{y + 7}" font-size="20" fill="#102B35">{text}</text>')
    out.append('</svg>')
    svg = OUT / '15-risikomatrix.svg'
    svg.write_text('\n'.join(out) + '\n')
    subprocess.run(['rsvg-convert', '-z', '2', '-o', str(svg.with_suffix('.png')), str(svg)], check=True)


for name, body in DIAGRAMS.items():
    (OUT / (name + '.puml')).write_text(STYLE + body + '\n@enduml\n')
for fmt in ['svg', 'png']:
    subprocess.run(['plantuml', '-charset', 'UTF-8', '-t' + fmt, *map(str, sorted(OUT.glob('*.puml')))], check=True)
risk_matrix()
print('DIAGRAMS_READY', len(DIAGRAMS) + 1)
