"""Erzeugt editierbare UML-Quellen, die den implementierten Stand 1.0 beschreiben, und rendert sie
mit PlantUML als SVG und PNG."""
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
rectangle "ABYSS 1.0 · lokales Desktopspiel" {
 usecase "UC-01\nTauchgang vorbereiten\nund beginnen" as Start
 usecase "UC-02\nZum nächsten Raum\nvordringen" as Progress
 usecase "UC-03\nBergung wählen" as Reward
 usecase "UC-04\nHandeln: Schwarzmarkt,\nWerkstatt, Kapelle" as Trade
 usecase "UC-05\nUnterbrechen und\nfortsetzen" as Resume
 usecase "UC-06\nBrücke erobern /\nnächster Zyklus" as Win
 usecase "UC-07\nNach Niederlage\nerneut tauchen" as Retry
 usecase "UC-08\nIm Archiv freischalten" as Archive
 usecase "UC-09\nAussehen anpassen" as Wardrobe
 usecase "UC-10\nAusrüstung und\nBootskarte ansehen" as Inventory
 usecase "UC-11\nOptionen und Steuerung" as Options
}
actor "Lokales Dateisystem" as Disk
Player --> Start
Player --> Progress
Player --> Reward
Player --> Trade
Player --> Resume
Player --> Win
Player --> Retry
Player --> Archive
Player --> Wardrobe
Player --> Inventory
Player --> Options
Progress ..> Reward : <<extend>>
Progress ..> Trade : <<extend>>
Start --> Disk
Resume --> Disk
Archive --> Disk
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
'03-architecture': r'''title Logische Architektur (Schichten, Abhängigkeiten nach innen)
package "ui (JavaFX)" {
 [GameWindow] as GW
 [MenuScreens / RunScreens] as Screens
 [PixelView] as PV
 [InputController] as IC
}
package "ui.gui" {
 [Gui (Bordsystem, Immediate-Mode)] as Gui
}
package "ui.render" {
 [WorldRenderer] as WR
 [HudRenderer] as HR
 [MachinePainter] as MP
 [Effects / Camera / Ocean] as FX
}
package "ui.art" {
 [DiverArt / EnemyArt / BossArt] as Art
 [RoomArt / PropArt / IconArt] as RoomArt
}
package "ui.pixel" {
 [Frame / Sprite / LightMap / PostProcess / PixelFont] as Pixel
}
package "application" {
 [GameService] as GS
 [Profile / Loadout / Unlock / Achievement] as Meta
}
package "ports" {
 [GameRepository] as Port
}
package "infrastructure" {
 [FileGameRepository] as File
 [AudioSystem] as Audio
}
package "domain (reines Java)" {
 [GameRun] as Run
 [Combat / Physics / Machinery] as Rules
 [EnemyBehavior-Strategien] as AI
 [RoomGenerator] as Gen
}
GW --> Screens
GW --> Gui
Screens --> Gui
Gui --> Pixel
GW --> WR
WR --> MP
GW --> IC
GW --> PV
GW --> GS
GW --> Audio
Screens --> GS
WR --> HR
WR --> FX
WR --> Art
WR --> RoomArt
WR --> Pixel
Art --> Pixel
RoomArt --> Pixel
WR ..> Run : liest
GS --> Run
GS --> Meta
GS --> Port
File ..|> Port
Run --> Rules
Run --> AI
Run --> Gen
note bottom of Run : Keine Importe aus JavaFX,\nDateien oder Anwendungsschicht
note right of Pixel : Software-Framebuffer 480 x 270,\nohne JavaFX testbar
''',
'04-design-classes': r'''title Design-Klassendiagramm der Domänenschicht (Auszug)
class GameRun {
 - phase: Phase
 - room: RoomPlan
 - cycle: int
 + update(seconds, input)
 + interaction(): Interaction
 + take(offer): boolean
 + acceptDeal(deal): boolean
 + chooseNextRoom(branch): boolean
 + nextCycle(): boolean
 + checkpoint(): RunCheckpoint
 + drainEvents(): List<GameEvent>
 + {static} restore(saved, items, weapons): GameRun
}
class Combat {
 ~ hitEnemy(enemy, base, source, knockback, x): double
 ~ hurtPlayer(amount, x, source, melee): boolean
 ~ explode(x, y, radius, damage, ...)
 ~ chain(origin, damage, range)
}
class Physics {
 ~ {static} move(actor, layout, dt)
}
class PlayerMotor {
 ~ tick(dt)
 ~ move(dt, input)
}
class Arsenal {
 ~ update(dt, input)
 ~ slamImpact()
 ~ updateDrone(dt)
}
class Ballistics {
 ~ update(dt)
}
class Loot {
 ~ update(dt)
 ~ drop(kind, value, x, y)
 ~ breakCrate(crate)
}
class Rewards {
 ~ open(room)
 ~ take(offer): boolean
 ~ accept(deal): boolean
 ~ repair(): boolean
}
abstract class Actor {
 # x, y, vx, vy
 # health, maxHealth
 + bounds(): Bounds
}
class Player {
 - weapon: Weapon
 - items: Map<Item,Integer>
 - stats: StatSheet
 + stacks(item): int
}
class Enemy {
 - kind: EnemyKind
 - affix: Affix
 - state: State
 + telegraph(): Telegraph
 + armored(): boolean
}
interface EnemyBehavior {
 update(enemy, run, dt)
}
abstract class Brain {
 + update(enemy, run, dt) {final}
 # approach(...)
 # ready(...): boolean
 # strikeStart(...)
 # strike(...)
}
class WardenBrain
class ReactorBrain
class BroodBrain
class CaptainBrain
class "Behaviors.*\n(10 reguläre Arten)" as Regular
class Fixture {
 - kind: Kind
 - x, direction, sector
 + active(): boolean
 + warning(): boolean
}
class Machinery {
 ~ push(actor): double
 ~ update(dt)
 ~ use(): boolean
}
class RoomGenerator {
 + choices(depth): List<RoomPlan>
 + room(depth, branch): RoomPlan
}
class StatSheet <<record>> {
 + {static} compute(diver, weapon, level, items, bonus)
}
class RoomPlan <<record>>
class RoomLayout <<record>>
class Platform <<record>>
class RunSetup <<record>>
class RunCheckpoint <<record>>
class Offer <<record>>
enum Weapon
enum Item
enum ActiveModule
enum DiverClass
enum RoomCondition
enum Synergy
enum EnemyKind {
 ~ behavior(): EnemyBehavior
}
GameRun *-- Player
GameRun *-- "0..*" Enemy
GameRun *-- "0..*" Projectile
GameRun *-- "0..*" Hazard
GameRun *-- "0..*" Pickup
GameRun *-- "0..*" SupplyCrate
GameRun *-- Combat
GameRun *-- PlayerMotor
GameRun *-- Arsenal
GameRun *-- Ballistics
GameRun *-- Loot
GameRun *-- Rewards
GameRun *-- Machinery
GameRun *-- "0..*" Fixture
Machinery ..> Fixture
PlayerMotor ..> Physics
StatSheet ..> Synergy
Synergy ..> Item
Rewards ..> Offer
note top of Rewards : Mitarbeiterklassen des Aggregats:\nnur GameRun ist öffentlich
GameRun --> RoomGenerator
GameRun --> RoomPlan
GameRun --> RunSetup
GameRun ..> RunCheckpoint
RoomPlan *-- RoomLayout
RoomPlan --> RoomCondition
RoomLayout *-- "0..*" Platform
Actor <|-- Player
Actor <|-- Enemy
Player --> StatSheet
Player --> Weapon
Player --> ActiveModule
Player --> DiverClass
Player ..> Item
Enemy --> EnemyKind
Enemy --> EnemyBehavior
EnemyKind ..> EnemyBehavior : Fabrikmethode
EnemyBehavior <|.. Brain
Brain <|-- WardenBrain
Brain <|-- ReactorBrain
Brain <|-- BroodBrain
Brain <|-- CaptainBrain
Brain <|-- Regular
note right of Brain : Schablonenmethode:\ngemeinsame Zustandsmaschine,\nArten überschreiben Einstiegspunkte
note bottom of EnemyBehavior : Strategie: austauschbares\nVerhalten pro Gegnerart
''',
'05-ssd-progress': r'''title Systemsequenzdiagramm UC-02 Zum nächsten Raum vordringen
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
'06-start-sequence': r'''title Sequenz: Tauchgang beginnen (UC-01)
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
'08-reward-sequence': r'''title Sequenz: Bergung wählen (UC-03)
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
'10-resume-sequence': r'''title Sequenz: Fortsetzen (UC-05)
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
'''
}
for name, body in DIAGRAMS.items():
    (OUT / (name + '.puml')).write_text(STYLE + body + '\n@enduml\n')
for fmt in ['svg', 'png']:
    subprocess.run(['plantuml', '-charset', 'UTF-8', '-t' + fmt, *map(str, sorted(OUT.glob('*.puml')))], check=True)
print('DIAGRAMS_READY', len(DIAGRAMS))
