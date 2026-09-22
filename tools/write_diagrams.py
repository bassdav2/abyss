"""Erzeugt editierbare UML-Quellen, die den implementierten Stand beschreiben."""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/diagrams'
OUT.mkdir(parents=True,exist_ok=True)
STYLE='''@startuml
skinparam backgroundColor #FFFFFF
skinparam defaultFontName Arial
skinparam defaultFontSize 15
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
hide empty members
'''
DIAGRAMS={
'01-use-cases':r'''left to right direction
actor "Spieler/in" as Player
rectangle "ABYSS · lokales Desktopspiel" {
 usecase "UC-01\nTauchgang beginnen" as Start
 usecase "UC-02\nZum nächsten Raum vordringen" as Progress
 usecase "UC-03\nAusrüstung verbessern" as Upgrade
 usecase "UC-04\nUnterbrechen / fortsetzen" as Resume
 usecase "UC-05\nBrücke / nächster Zyklus" as Win
 usecase "UC-06\nErneut versuchen" as Retry
 usecase "UC-07\nBaupläne / Startmodul" as Blueprint
 usecase "UC-08\nOptionen / Hilfe" as Options
}
Player --> Start
Player --> Progress
Player --> Upgrade
Player --> Resume
Player --> Win
Player --> Retry
Player --> Blueprint
Player --> Options
note right of Resume
Lokale Save-Dateien sind ein
technischer Speicheradapter.
Keine Online-Akteure oder Konten.
end note
''',
'02-domain':r'''class Tauchgang {
 Seed
 Zyklus
 Zustand
}
class Spielfigur {
 Integrität
 Energie
 Schrott
}
class Raum {
 Position
 Sektion
 Typ
}
class Gegner {
 Integrität
 Angriffszustand
}
class Projektil
class Gefahr
class "Aktives Modul" as Active
class "Passives Modul" as Passive
class "Installierte Modulstufe" as Stack {
 Stufe: 1..3
}
class Bauplan
class "Raum-Einstieg" as Save
class Spielerprofil
Tauchgang "1" *-- "1" Spielfigur
Tauchgang "1" --> "1" Raum : befindet sich in
Raum "1" -- "0..*" Gegner : Begegnung
Raum "1" -- "0..*" Gefahr
Tauchgang "1" *-- "0..*" Projektil
Spielfigur "1" --> "1" Active
Spielfigur "1" *-- "0..6" Stack
Stack "0..*" --> "1" Passive
Spielerprofil "1" -- "1..3" Bauplan : kennt
Bauplan "1" --> "1" Active : ermöglicht
Tauchgang "1" --> "1" Save : gesicherter Einstieg
note bottom of Save
Konzeptuelles Modell:
keine UI-Klassen, keine Methoden,
keine Dateiformate.
end note
''',
'03-architecture':r'''top to bottom direction
package "ui" {
 [GameWindow]
 [InputController]
 [GameRenderer]
 [AssetCatalog]
}
package "application" {
 [GameService]
 [Profile / Settings]
}
package "ports" {
 interface GameRepository
}
package "domain" {
 [GameRun]
 [RoomGenerator]
 [EnemyAi]
 [Actors / Hazards / Projectiles]
 [RunCheckpoint / GameEvent]
}
package "infrastructure" {
 [FileGameRepository]
 [AudioSystem]
}
[GameWindow] --> [InputController]
[GameWindow] --> [GameService]
[GameWindow] --> [GameRun] : Spielaktionen
[GameWindow] --> [GameRenderer]
[GameWindow] --> [AudioSystem] : Ereignisse
[GameRenderer] --> [GameRun] : nur lesen
[GameRenderer] --> [AssetCatalog]
[GameService] --> [GameRun]
[GameService] --> GameRepository
[GameService] --> [Profile / Settings]
[FileGameRepository] ..|> GameRepository
[FileGameRepository] --> [RunCheckpoint / GameEvent] : Checkpoint-Daten
[GameRun] --> [RoomGenerator]
[GameRun] --> [EnemyAi]
[GameRun] --> [Actors / Hazards / Projectiles]
[GameRun] --> [RunCheckpoint / GameEvent]
note bottom of "domain"
Keine Abhängigkeit auf JavaFX,
Dateisystem oder Infrastruktur.
end note
''',
'04-design-classes':r'''class GameWindow {
 - service: GameService
 - run: GameRun
 - screen: Screen
 - chooseRoute(index)
 - chooseReward(index)
}
class GameService {
 - repository: GameRepository
 - profile: Profile
 + start(seed, module, explorer): GameRun
 + resume(): Optional<GameRun>
 + saveRoom()
 + recordOutcome()
 + nextCycle(): boolean
}
interface GameRepository {
 + loadProfile(): Profile
 + loadCheckpoint(): Optional<RunCheckpoint>
 + saveProfile(profile)
 + saveCheckpoint(checkpoint)
 + clearCheckpoint()
}
class FileGameRepository
class GameRun {
 - player: Player
 - phase: Phase
 + update(seconds, input)
 + claimReward(upgrade): boolean
 + chooseNextRoom(branch): boolean
 + checkpoint(): RunCheckpoint
 + drainEvents(): List<GameEvent>
}
class RoomGenerator {
 + choices(depth): List<RoomPlan>
}
class Player {
 + health(): double
 + energy(): double
 + upgrades(): Map<Upgrade,Integer>
}
class Enemy
class RunCheckpoint <<record>>
class InputFrame <<record>>
class GameEvent <<record>>
GameWindow --> GameService
GameWindow --> GameRun
GameService --> GameRepository
FileGameRepository ..|> GameRepository
GameService --> GameRun
GameRun *-- Player
GameRun *-- "0..*" Enemy
GameRun --> RoomGenerator
GameRun --> RunCheckpoint
GameRun ..> InputFrame
GameRun ..> GameEvent
''',
'05-ssd-progress':r'''actor "Spieler/in" as Player
participant ":ABYSS" as System
loop Bis letzter Gegner besiegt
 Player -> System : bewegen(richtung), springen(),\nangreifen(), ausweichen(), modulAktivieren()
 System --> Player : Position, Ressourcen,\nGefahren und Trefferfeedback
end
System --> Player : Raum gesichert, Fund und Schott verfügbar
Player -> System : interagieren(Fund)
System --> Player : Modulangebote
Player -> System : modulWählen(index)
System --> Player : aktualisierter Build
Player -> System : interagieren(Schott)
System --> Player : nächste Raumangebote
Player -> System : routeWählen(index)
System --> Player : neuer Raum, gesicherter Einstieg
note over System
Blackbox-Sicht: keine internen Klassen.
Systemoperationen sind fachlich benannt.
end note
''',
'06-start-sequence':r'''actor Player
participant GameWindow as UI
participant GameService as Service
participant GameRun as Run
participant FileGameRepository as Repo
Player -> UI : Start mit Modul / Seed
UI -> Service : start(seed, module, explorer)
Service -> Service : Freischaltung prüfen
create Run
Service -> Run : new GameRun(...)
Run -> Run : enterRoom(room 0)\nCheckpoint erzeugen
Service -> Service : Profil aktualisieren
Service -> Repo : saveProfile(profile)
Service -> Run : checkpoint()
Run --> Service : unveränderlicher Einstieg
Service -> Repo : saveCheckpoint(checkpoint)
Service --> UI : run
UI --> Player : Spielbildschirm
''',
'07-combat-sequence':r'''participant GameWindow as UI
participant InputController as Input
participant GameRun as Run
participant Player
participant Enemy
participant GameRenderer as Renderer
participant AudioSystem as Audio
UI -> Input : frame(playerX)
Input --> UI : InputFrame
UI -> Run : update(1/120, input)
Run -> Player : Bewegung / Cooldowns
alt Angriff zulässig und Treffer in Reichweite
 Run -> Enemy : Schaden anwenden
 Run -> Run : HIT / ENEMY_DOWN sammeln
end
Run -> Run : Gegnerzustände, Projektile, Gefahren\nund Raumabschluss aktualisieren
UI -> Run : drainEvents()
Run --> UI : List<GameEvent>
UI -> Renderer : event(event, settings)
UI -> Audio : event(event)
UI -> Renderer : render(run, settings, ...)
note over Renderer,Audio
Feedback verändert keine Spielregeln.
end note
''',
'08-reward-sequence':r'''actor Player
participant GameWindow as UI
participant GameRun as Run
participant "player:Player" as Character
Player -> UI : Modulkarte wählen
UI -> Run : claimReward(upgrade)
Run -> Run : Phase, Angebot, Verfügbarkeit\nund Werkstattkosten prüfen
alt gültige Wahl
 Run -> Character : Schrott abziehen (Werkstatt)
 loop 1 oder 2 Stufen, maximal 3 insgesamt
  Run -> Character : upgrade(upgrade)
 end
 Run -> Run : rewardAvailable = false\nUPGRADE-Ereignis erzeugen
 Run --> UI : true
 UI --> Player : Spiel mit aktualisiertem Build
else nicht zulässig
 Run --> UI : false
end
''',
'09-room-sequence':r'''actor Player
participant GameWindow as UI
participant GameRun as Run
participant RoomGenerator as Generator
participant GameService as Service
participant FileGameRepository as Repo
Player -> UI : Raumkarte wählen
UI -> Run : chooseNextRoom(index)
Run -> Run : Phase ROOM_CLEARED prüfen
Run -> Generator : choices(depth + 1)
Generator --> Run : zulässige Räume
Run -> Run : enterRoom(choice)\nGegner / Gefahren erzeugen\nneuen Checkpoint bilden
Run --> UI : true
UI -> Service : saveRoom()
Service -> Service : Kills und Baupläne verbuchen
Service -> Repo : saveProfile(profile)
Service -> Run : checkpoint()
Run --> Service : RunCheckpoint
Service -> Repo : saveCheckpoint(checkpoint)
UI --> Player : neuer Raum
note right of Repo
Temporär schreiben, dann umbenennen.
Je Datei atomar, keine Transaktion
über beide Dateien.
end note
''',
'10-resume-sequence':r'''actor Player
participant GameWindow as UI
participant GameService as Service
participant FileGameRepository as Repo
participant GameRun as Run
note over Service,Repo
Beim Programmstart
end note
Service -> Repo : loadProfile()
Repo --> Service : Profile
Service -> Repo : loadCheckpoint()
Repo -> Run : restore(checkpoint) zur Validierung
Repo --> Service : Optional<RunCheckpoint>
Player -> UI : Fortsetzen
UI -> Service : resume()
Service -> Run : restore(saved)
Run -> Run : Werte / Route prüfen\nRaum-Einstieg rekonstruieren
Run --> Service : GameRun
Service --> UI : Optional<GameRun>
UI --> Player : gespeicherter Raumeingang
''',
'11-enemy-state':r'''[*] --> APPROACH
APPROACH --> WINDUP : in Reichweite / Angriff bereit
WINDUP --> STRIKE : Vorwarnzeit endet
STRIKE --> RECOVER : Angriffszeit endet
RECOVER --> APPROACH : Erholung endet
APPROACH --> STUNNED : Treffer / Impuls (kein Boss)
RECOVER --> STUNNED : Treffer / Impuls (kein Boss)
WINDUP --> STUNNED : Impuls (kein Boss)
STRIKE --> STUNNED : Impuls (kein Boss)
STUNNED --> APPROACH : Betäubung endet
note right of RECOVER
Boss ist hier vollständig verwundbar.
Sonst nur 22% Schaden.
Bei <50% Integrität kürzere Zeiten.
end note
''',
'12-navigation':r'''[*] --> Titel
Titel --> Vorbereitung : Neuer Tauchgang
Vorbereitung --> Spiel : Start
Titel --> Spiel : Fortsetzen
Titel --> Optionen
Titel --> Hilfe
Titel --> Archiv
Spiel --> Pause : Esc / Fokusverlust
Pause --> Spiel : Weiter
Pause --> Optionen
Pause --> Hilfe
Pause --> Titel
Spiel --> Bergung : E am Fund
Bergung --> Spiel : Modul / Zurück
Spiel --> Route : E am Schott
Route --> Spiel : Raumwahl / Zurück
Spiel --> Ergebnis : Niederlage / Sieg
Ergebnis --> Spiel : Neuer Versuch / nächster Zyklus
Ergebnis --> Titel
'''
}
for name,body in DIAGRAMS.items():
    (OUT/(name+'.puml')).write_text(STYLE+body+'\n@enduml\n')
for fmt in ['svg','png']:
    subprocess.run(['plantuml','-charset','UTF-8','-t'+fmt,*map(str,OUT.glob('*.puml'))],check=True)
print('DIAGRAMS_READY',len(DIAGRAMS))
