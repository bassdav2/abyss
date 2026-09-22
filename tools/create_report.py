"""Baut die bebilderte PM3-Arbeitsfassung aus dem tatsächlichen Projektstand.
Python: Codex-Runtime mit reportlab, svglib und pypdf.
"""
from pathlib import Path
import json, re, html, xml.etree.ElementTree as ET
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.colors import HexColor
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import Paragraph, Table, TableStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.utils import ImageReader
from reportlab.graphics import renderPDF
from svglib.svglib import svg2rlg

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'output/pdf'
OUT.mkdir(parents=True,exist_ok=True)
FILE=OUT/'Abyss_Technischer_Bericht_Arbeitsfassung.pdf'
FONTS=ROOT/'src/main/resources/fonts'
for name,file in [('Body','Barlow-Regular.ttf'),('Medium','Barlow-Medium.ttf'),('Display','BarlowCondensed-SemiBold.ttf')]:
    pdfmetrics.registerFont(TTFont(name,str(FONTS/file)))
pdfmetrics.registerFontFamily('Body',normal='Body',bold='Medium',italic='Body',boldItalic='Medium')
INK=HexColor('#102B35');TEAL=HexColor('#2B6973');GREY=HexColor('#52717B');AMBER=HexColor('#C2934A')
styles={
 'body':ParagraphStyle('body',fontName='Body',fontSize=11,leading=15,textColor=INK,spaceAfter=8),
 'small':ParagraphStyle('small',fontName='Body',fontSize=9,leading=12,textColor=GREY,spaceAfter=7),
 'h2':ParagraphStyle('h2',fontName='Display',fontSize=20,leading=24,textColor=INK,spaceAfter=9),
 'cell':ParagraphStyle('cell',fontName='Body',fontSize=9.5,leading=12.6,textColor=INK),
}
c=canvas.Canvas(str(FILE),pagesize=A4,pageCompression=1)
c.setTitle('ABYSS - Technischer Bericht I - Arbeitsfassung')
c.setAuthor('ABYSS Projekt / mit Codex erstellter Entwurf')
c.setSubject('Implementierter JavaFX-Spielstand, Architektur, Use Cases, Tests und offene Teamarbeit')
W,H=A4;M=43;Y=0;PAGE=0

def page(title,kicker='ABYSS / TECHNISCHER BERICHT I',wide=False):
    global W,H,Y,PAGE
    if PAGE:c.showPage()
    PAGE+=1;W,H=landscape(A4) if wide else A4;c.setPageSize((W,H))
    c.setFillColor(HexColor('#FAFCFB'));c.rect(0,0,W,H,fill=1,stroke=0)
    c.setFillColor(TEAL);c.setFont('Medium',9);c.drawString(M,H-35,kicker)
    c.setFillColor(INK);c.setFont('Display',29);c.drawString(M,H-77,title)
    c.setStrokeColor(HexColor('#CBDBDC'));c.line(M,42,W-M,42)
    c.setFont('Body',8);c.setFillColor(GREY)
    c.drawString(M,28,'Arbeitsfassung 22.09.2026 · KI-Einsatz offengelegt · keine Team-/Dozentenabnahme')
    c.drawRightString(W-M,28,f'{PAGE:02d}')
    Y=H-104
    c.bookmarkPage(f'p{PAGE}');c.addOutlineEntry(title,f'p{PAGE}',0)

def para(text,kind='body',gap=6):
    global Y
    text=html.escape(text).replace('\n','<br/>')
    text=re.sub(r'\*\*(.+?)\*\*',r'<b>\1</b>',text)
    p=Paragraph(text,styles[kind]);_,height=p.wrap(W-2*M,1000)
    if Y-height<57:raise RuntimeError(f'Overflow page {PAGE}: {text[:90]} y={Y} height={height}')
    p.drawOn(c,M,Y-height);Y-=height+gap

def bullets(lines):
    for line in lines:para('• '+line)

def table(headers,rows,widths=None):
    global Y
    data=[[Paragraph(html.escape(str(x)),styles['cell']) for x in row] for row in [headers]+rows]
    t=Table(data,colWidths=widths or [(W-2*M)/len(headers)]*len(headers),hAlign='LEFT')
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),HexColor('#DCEBEC')),('ROWBACKGROUNDS',(0,1),(-1,-1),[HexColor('#F0F5F4'),HexColor('#FFFFFF')]),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),('LINEBELOW',(0,0),(-1,0),.8,TEAL)]))
    _,height=t.wrap(W-2*M,1000)
    if Y-height<57:raise RuntimeError(f'Table overflow page {PAGE}: {height}, y={Y}')
    t.drawOn(c,M,Y-height);Y-=height+12

def picture(path,maxheight=300):
    global Y
    img=ImageReader(str(ROOT/path));iw,ih=img.getSize();scale=min((W-2*M)/iw,maxheight/ih)
    w,h=iw*scale,ih*scale
    c.drawImage(img,(W-w)/2,Y-h,w,h,mask='auto');Y-=h+12

def diagram(name,maxheight=490):
    global Y
    d=svg2rlg(str(ROOT/'docs/diagrams'/f'{name}.svg'))
    scale=min((W-2*M)/d.width,maxheight/d.height,(Y-65)/d.height)
    original_width=d.width;original_height=d.height
    d.scale(scale,scale);d.width*=scale;d.height*=scale
    renderPDF.draw(d,c,(W-original_width*scale)/2,Y-original_height*scale)
    Y-=original_height*scale+12

def section(md,heading,next_heading=None):
    text=(ROOT/'docs'/md).read_text();part=text.split(heading,1)[1]
    return part.split(next_heading,1)[0] if next_heading else part

def seq(title,name,caption):
    page(title,wide=True);para(caption,'small');diagram(name,420)

page('Vom Heck bis zur Brücke.','ABYSS / PM3 / ENTWICKLUNGSSTAND')
para('Ein lokales 2D-Roguelite in Java und JavaFX. Ein vollständiger Weg durch zwölf Räume, mit Ausrüstung, Werkstätten, Boss und wiederholbarem Tauchzyklus.','h2')
picture('docs/qa/screens/boss.png',300)
para('**Technischer Bericht I - Arbeitsfassung**')
para('Dieser Bericht beschreibt den tatsächlich implementierten Stand des autonomen Nachtlaufs vom 22.09.2026. Code und Entwürfe wurden weitgehend mit Codex erstellt. Automatisierte Tests und Layoutprüfungen sind vorhanden; das Team muss die Entscheidungen verstehen, prüfen und vor einer Übernahme selbst begründen.')
para('Keine abgegebene oder bewertete Modulleistung. Keine erfundenen Teamstunden, Testpersonen oder Freigaben.','small')

page('Leseführung und fachlicher Rahmen')
para('**Ziel:** Eine Person beginnt am hintersten Ende eines riesigen U-Boots und erreicht über bewachte Räume dessen Brücke. Niederlagen führen zu neuen Versuchen. Baupläne eröffnen alternative Startmodule; ein Sieg kann in einen weiteren Zyklus übergehen.')
table(['Bereich','Inhalt'],[
 ['Anforderungen','Use-Case-Modell, vollständig beschriebener Kernfall, Erweiterungen und Qualitätsziele'],
 ['Architektur / Design','Konzeptuelles Domänenmodell, Entscheidungen, Paketstruktur, DCD, SSD und vier Interaktionsdiagramme'],
 ['Implementation','Spielumfang, UI, Asset-Pipeline, lokale Auslieferung und Grenzen'],
 ['Nachweise','Tests, reale Renderwerte, offene menschliche Abnahme, Projektplanung und KI-Einsatz']],[125,W-2*M-125])
para('**Kursbasis:** PM3-Kick-off, physische Seiten 17/19; M2-Auftrag, Seiten 1-3; KI-Verwendung, Seite 1. Der lokale Kursbestand stammt vom 14.09.2026. Später geänderte Moodle-Vorgaben sind dadurch nicht bestätigt.')
para('Der M2-Auftrag verlangt unter anderem Quellcode inklusive Konfiguration und generierter API-Dokumentation sowie einen technischen Bericht von ungefähr 20 Seiten. Diese Arbeitsfassung orientiert sich an seiner Struktur. Teamaufwand und Abnahme bleiben ausdrücklich offen.')
para('**Scope:** Einzelspieler, Desktop, Tastatur/Maus, lokale Dateien. Keine Anmeldung, kein Netzwerkdienst, keine relationale Datenbank. Zielsystem dieses Builds ist Apple Silicon macOS. JavaFX-/Themenfreigabe mit dem Fachdozenten abstimmen.')
para('Editierbare Details: ANFORDERUNGEN.md, ARCHITEKTUR.md, TESTSTRATEGIE.md, ASSET_PIPELINE.md, PROJEKTMANAGEMENT.md und KI_EINSATZ.md unter docs/. Diagramme liegen als PlantUML und SVG vor.','small')

page('1 / Use-Case-Modell')
para('Primärer Akteur ist die spielende Person. Das Diagramm grenzt die lokale Anwendung gegenüber ihr ab. Save-Dateien sind ein technischer Speicheradapter und kein eigenständiger Benutzer.','small')
diagram('01-use-cases',495)
para('Fachlicher Kern ist UC-02: eine Begegnung bewältigen, den Build verändern und zum nächsten Raum vordringen. Dieser Fall wird vollständig ausformuliert. UC-01, UC-03, UC-04 und UC-05 sind casual beschrieben; die übrigen Fälle brief.','small')

page('1.1 / UC-02 - Zum nächsten Raum')
para('**Scope / Level:** ABYSS, Benutzerziel. **Akteur:** Spieler/in. **Auslöser:** Ein gültiger Tauchgang beginnt oder wird fortgesetzt.')
para('**Interessen:** Verständliche Gefahren, verlässliche Steuerung und Belohnungen; für das Team reproduzierbare Fehler und testbare Verantwortlichkeiten. **Vorbedingungen:** Eine lebende Figur in einem gültigen aktiven Raum.')
para('**Erfolgsgarantie:** Der gewählte nächste Raum ist aktiv. Build und Ressourcen werden übernommen. Sein Einstieg wird lokal gesichert, sofern der Speicherzugriff funktioniert. **Minimalgarantie:** Ein ungesicherter Raum kann nicht übersprungen werden; ungültige Wahl verändert den Raum nicht.')
para('Standardszenario','h2')
steps=['Das System zeigt Raum, Figur, Ressourcen und verbleibende Patrouillen.','Die Person bewegt sich, greift an und weicht angekündigten Angriffen aus.','Das System prüft Treffer, Energie, Abklingzeiten und Schaden.','Nach der letzten Patrouille entriegelt das System das rechte Schott und stellt einen Fund bereit.','Die Person nähert sich dem Fund und interagiert.','Das System zeigt bis zu drei noch nicht maximierte passive Module.','Die Person wählt ein Modul. Das System installiert die zulässigen Stufen genau einmal.','Die Person geht zum rechten Schott und interagiert.','Das System zeigt ein oder zwei nächste Räume mit Risiko und Belohnungsart.','Die Person wählt. Das System übernimmt den Build, betritt den Raum und sichert den Einstieg.']
for i,line in enumerate(steps,1):para(f'{i}. {line}',gap=4)

page('1.2 / Erweiterungen und Ausnahmen')
table(['Stelle','Abweichung und erwartetes Verhalten'],[
 ['2a','Pause oder Fokusverlust: Simulation stoppt; Weiter setzt denselben Zustand fort.'],
 ['3a','Zu wenig Energie oder laufende Abklingzeit: kein Modulangriff, kein Energieabzug.'],
 ['3b','Integrität null: Niederlage, Profilfortschritt verbuchen und aktiven Checkpoint entfernen.'],
 ['4a','Weitere Patrouille: sichtbare Warnung, danach 2,2 Sekunden Abstand bis zur nächsten Welle.'],
 ['4b','Brücke: Boss besiegt -> Sieg und UC-05 statt normaler Routenwahl.'],
 ['5a','Depot: Heilung und Schrott statt Modul.'],
 ['5b','Werkstatt: einmal gratis reparieren; optionales Modul kostet 15 Schrott.'],
 ['6a','Alle Module auf Stufe 3: Vorräte statt weiterer Module.'],
 ['7a','Schrott reicht nicht: Kauf deaktiviert, Reparatur und Weiterreise bleiben möglich.'],
 ['8a','Fund wird ausgelassen: keine nachträgliche Gutschrift.'],
 ['9a','Nur ein zulässiger Raum: genau dieses Angebot anzeigen.'],
 ['10a','Speicherfehler: Run weiter spielbar, fehlgeschlagene Sicherung wird gemeldet.']],[48,W-2*M-48])
para('**Besondere Anforderungen:** Domäne im festen 1/120-s-Schritt; kein Schaden durch Render- oder Audioereignisse. Save beschreibt den Raumeingang. **Häufigkeit:** bis zu zwölf Räume pro Zyklus. **Offen:** menschliche Run-Dauer und endgültiges Balancing.','small')

page('1.3 / Weitere Benutzerziele')
for title,body in [
 ('UC-01 - Tauchgang beginnen','Vorbereitung öffnen, freigeschaltetes aktives Modul und optional Entdecker wählen. Ein optionaler ganzzahliger Seed reproduziert Angebote. Ein ungültiger Seed startet nichts; ein neuer Run ersetzt die Raum-Sicherung und beginnt mit frischem Build. Baupläne bleiben.'),
 ('UC-03 - Ausrüstung verbessern','Aus bis zu drei passiven Modulen wählen. Normale Funde geben eine Stufe, Elitefunde zwei bis maximal drei. Werkstattmodule kosten 15 Schrott. Maximal ausgebaute Module erscheinen nicht; doppelte Auswahl ist ausgeschlossen.'),
 ('UC-04 - Unterbrechen und fortsetzen','Esc pausiert. Weiter setzt den Kampf fort. Nach Hauptmenü oder Programmende beginnt Fortsetzen beim letzten Raumeingang. Änderungen innerhalb dieses Raums werden zurückgesetzt. Unlesbare Saves werden gemeldet und vor dem Ersetzen gesichert.'),
 ('UC-05 - Brücke und nächster Zyklus','Nach dem Boss erscheinen Ergebnis, Kills, Zeit und Zyklus. Hauptmenü beendet den Run. Ein nächster Zyklus behält den Build, heilt bis zu 50 Integrität, füllt Energie und erhöht die Gegnerstärke bis zu Obergrenzen.'),
 ('UC-06 bis UC-08 - Brief','Nach Niederlage mit neuem Seed erneut versuchen. Baupläne im Archiv ansehen: Raum 4 schaltet Lichtbogen, Raum 8 Druckschild frei. Hilfe und Optionen erlauben Lautstärke, Musik, reduzierte Bewegung, Vollbild und Entdecker-Vorgabe.')]:
    para(title,'h2');para(body)

page('2 / Anforderungen und Spielregeln')
table(['ID','Anforderung / Akzeptanz'],[
 ['F-01','Bewegung, Sprung, Nahkampf, Dash und Modul. Domain-, Input- und JavaFX-Komponententests.'],
 ['F-02','Drei Gegnertypen plus Boss mit Vorwarnungen. Zustandsmaschine, Screenshots und vollständige Runs.'],
 ['F-03','Zwölf erreichbare Raumpositionen, Werkstätten bei 4/8. Generator über 1000 Seeds geprüft.'],
 ['F-04','Sechs passive und drei aktive Module. Stufen-, Kosten-, Freischalt- und Energielimits.'],
 ['F-05','Niederlage, Sieg, Neustart und Folgezyklus. Explizite Phasen und Anwendungstests.'],
 ['F-06','Lokale Raum-Sicherung und Profil. Dateiformat-, Roundtrip- und Fehlerfalltests.'],
 ['F-07','Pause, Hilfe, Optionen und Audio. Native Komponententests; Hör-/Geräteprüfung offen.']],[48,W-2*M-48])
para('**Qualität:** reine Java-Domäne ohne Fenster; Speicherfehler dürfen den Start nicht verhindern; lesbares HUD bei minimalem Fenster; Ziel flüssiger Darstellung auf dem Ziel-Mac; nachvollziehbare Assets und KI-Verwendung.')
para('**Regeln:** maximal drei Stufen je passivem Modul; 0,72 s Schutzzeit nach Schaden; Dash mit kurzer Unverwundbarkeit. Boss nimmt ausserhalb seiner Erholung 22 Prozent Schaden und beschleunigt unter 50 Prozent Integrität. Entdecker: 150 statt 100 Start-Integrität, 20 Prozent mehr Basisschaden.')
para('**Begrenzt:** horizontale Kampfspur, keine Plattformrätsel oder Leitern, kein Multiplayer. Die frühere Zielzeit von 20-30 Minuten ist nicht bestätigt; der implementierte Run ist kompakter. Zwölf Positionen bedeuten keine unendlich neuen Geometrien.','small')

page('3 / Konzeptuelles Domänenmodell')
diagram('02-domain',330)
para('Das Modell beschreibt Fachbegriffe und Beziehungen, keine JavaFX-Klassen oder Methoden. Ein Run besitzt eine Figur, Projektile und einen aktuellen Raum; das Profil kennt permanente Baupläne.','small')
table(['Begriff','Bedeutung'],[
 ['Run / Zyklus','Versuch vom Heck bis zur Brücke; erneuter Zwölf-Raum-Weg nach Sieg.'],
 ['Build','Aktives Modul, installierte passive Stufen und Ressourcen des aktuellen Runs.'],
 ['Route / Seed','Gewählte Raumzweige; ganze Zahl für reproduzierbare Angebote.'],
 ['Integrität / Energie','Lebenspunkte; regenerierende Ressource für aktive Fähigkeiten.'],
 ['Bauplan','Permanente Freischaltung einer alternativen Startfähigkeit.'],
 ['Checkpoint','Unveränderlicher Einstieg in einen Raum, keine Kampfmomentaufnahme.']],[125,W-2*M-125])
para('Weitere Begriffe: Patrouille = Gegnerwelle; Vorwarnung = Signal vor Angriff/Gefahr; Schrott = Run-Währung; Profil = Einstellungen, Baupläne und Statistik; Lotse = Brückenboss. Vollständiges Glossar: docs/GLOSSAR.md.','small')

page('4 / Architekturentscheidungen')
table(['Einflussfaktor','Gewählte Lösung'],[
 ['Modulrahmen / Erklärbarkeit','Java-Domäne, JavaFX als UI-Bibliothek, keine Engine oder relationale Datenbank.'],
 ['Reaktiver Kampf','Feste 1/120-s-Simulation und separate JavaFX-Darstellung.'],
 ['Unterbruch / Reproduktion','Seed-basierte Räume und unveränderlicher Einstieg als Save.'],
 ['Begrenzter Umfang / Grafik','Zwölf Positionen, modulare Varianten, gemeinsame Blender-Figurenquelle.']],[155,W-2*M-155])
para('**ADR-01 / Canvas:** JavaFX übernimmt Fenster, Controls, Eingaben und Zeichnen. Eigene Regeln bleiben direkt testbar. Nachteil gegenüber einer Engine: Physik, Kampf und Animation müssen selbst gepflegt werden.')
para('**ADR-02 / Fester Schritt:** Reale Framezeit wird gesammelt, die Domäne in kleinen Schritten aktualisiert. Einzelne lange Renderpausen werden auf 100 ms begrenzt. Dies ist keine universell deterministische Replay-Engine.')
para('**ADR-03 / Raum-Checkpoint:** Gegner und Projektile werden beim Laden neu erzeugt. Das begrenzt Komplexität und erklärt den möglichen Verlust von Veränderungen innerhalb eines Raums.')
para('**ADR-04 / Repository-Port:** Kleine versionierte Textdateien, Werteprüfung, temporäres Schreiben und atomisches Umbenennen je Datei. Profil und Checkpoint sind keine gemeinsame Transaktion.')
para('**ADR-05 / Ereignisse:** Audio und Partikel konsumieren GameEvent-Werte. Sie berechnen keine Treffer. **ADR-06 / Generator:** feste Sektionen und sichere Werkstätten; Variation in Angeboten und Begegnungen. **ADR-07 / Assets:** gemeinsam gerenderte Figuren, separate statische Hintergrundplatten.','small')

page('4.1 / Logische Architektur',wide=True)
para('Die Pfeile zeigen gerichtete Verwendungsbeziehungen. Die Anwendung kennt den Speichervertrag; die konkrete Dateiimplementierung wird am Programmstart eingesetzt.','small')
diagram('03-architecture',300)
para('domain enthält keinerlei JavaFX- oder Dateizugriff. GameWindow koordiniert Bildschirmwechsel und öffentliche Aktionen. GameRenderer liest den Spielzustand; AudioSystem konsumiert Ereignisse. GameService koordiniert Profil und Persistenz.','small')
para('Grenze: GameRun und GameWindow sind die grössten Klassen. Vor einer deutlichen Umfangserweiterung sollten Kampf-/Raumverantwortlichkeiten bzw. Bildschirmklassen gezielt aufgeteilt werden.','small')

page('5 / Design-Klassendiagramm',wide=True)
para('Auszug der tatsächlichen Klassen und Schnittstellen; Paketnamen und vollständige API stehen im Quellcode und in der generierten Javadoc.','small')
diagram('04-design-classes',425)

page('5.1 / System-Sequenzdiagramm')
para('UC-02 in Blackbox-Sicht. Fachliche Systemoperationen statt interner Objektnamen.','small')
diagram('05-ssd-progress',560)

seq('5.2 / Tauchgang starten','06-start-sequence','Systemoperation Start: Freischaltung prüfen, Domain-Run erzeugen und den Einstieg über das Repository sichern.')
seq('5.3 / Kampfeingabe verarbeiten','07-combat-sequence','Systemoperation Kampfeingabe: Regeln verändern die Domäne; entnommene Ereignisse lösen ausschliesslich Feedback aus.')
seq('5.4 / Modul installieren','08-reward-sequence','Systemoperation Modulwahl: Phase, Angebot, Kosten und Stufengrenze prüfen; genau eine gültige Auswahl übernehmen.')
seq('5.5 / Raum wählen und sichern','09-room-sequence','Systemoperation Routenwahl: zulässigen Raum betreten, Profilfortschritt verbuchen und den neuen Einstieg speichern.')

page('6 / Implementation und Oberfläche')
picture('docs/qa/screens/route.png',228)
para('Tatsächlich gerenderte Routenwahl. Risiko und Belohnungsart werden vor dem Betreten genannt.','small')
picture('docs/qa/screens/reward.png',220)
para('Tatsächlich gerenderte Werkstatt. Reparatur ist getrennt vom kostenpflichtigen Modul. Bilder stammen aus isolierten QA-Profilen, nicht aus einem behaupteten menschlichen Testlauf.','small')
para('Die UI umfasst Titel, Vorbereitung, Archiv, Hilfe, Optionen, Spiel, Pause, Route, Bergung und Ergebnis. Esc pausiert; Fokusverlust pausiert automatisch. Der Renderer arbeitet in 1600 x 900 logischen Einheiten und skaliert im Fenster.','small')

page('6.1 / Assets, Build und Grenzen')
manifest=json.loads((ROOT/'art-source/asset-manifest.json').read_text())
assets=manifest['assets'];rooms=sum(a['path'].startswith('art/rooms/') for a in assets);actors=sum(a['path'].startswith('art/actors/') and a['path'].endswith('.png') for a in assets)
table(['Bestand','Stand'],[
 ['Räume',f'{rooms} konsistente Hintergrundplatten für zwölf Raumpositionen; Werkstätten und Depots können wiederverwendet werden.'],
 ['Figuren',f'5 editierbare Blender-Modelle, {actors} transparente Animationsframes; gemeinsame Kamera und Materialien.'],
 ['Audio','16 eigene synthetische WAV-Dateien; keine fremden Samples.'],
 ['UI','Barlow / Barlow Condensed, lizenzierte Fonts mit OFL-Hinweisen; getrenntes HUD und Effekte.'],
 ['Runtime','Java 25, JavaFX 26.0.2; Gradle Wrapper 9.3.1. JUnit 6.0.3 nur für Tests.']],[105,W-2*M-105])
para('**Konsistenz:** Boden y = 620 auf einer 1600 x 900 Bühne. Hintergrund enthält keine Figur oder UI. Interaktive Kisten, Gefahren, Schott-Hinweise und Trefferfeedback bleiben separat. Modellexporte haben denselben Fußanker und definierte Framezahlen.')
para('**Build:** ./gradlew test checkJavaFormat javadoc; python3 tools/package_mac.py. Die Mac-App enthält eine eigene Java-/JavaFX-Laufzeit. Aus dem Quellcode genügt eine passende Java-25-Installation; JavaFX wird über Gradle geladen.')
para('**Save:** macOS unter Library/Application Support/Abyss. Testprofile liegen separat unter build oder in temporären Verzeichnissen. Unlesbare Dateien bleiben vor dem Ersetzen als Backup erhalten.')
para('**Grenzen:** Nur Apple Silicon macOS wurde lokal gebaut. Keine Notarisierung, keine bestätigten Windows-/Linux-Pakete. Keine frei belegbaren Tasten oder Gamepad-Steuerung. Figuren sind vorgerenderte 3D-Modelle, Hintergründe detaillierte Rasterplatten; Art-Abnahme und menschliches Spielgefühl bleiben offen.','small')

page('6.2 / Tests und beobachtete Ergebnisse')
test_count=0;failures=0
for file in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
    root=ET.parse(file).getroot();test_count+=int(root.attrib['tests']);failures+=int(root.attrib['failures'])+int(root.attrib['errors'])
soak=json.loads((ROOT/'docs/qa/render-soak.json').read_text())
table(['Ebene','Beobachteter Nachweis'],[
 ['JUnit',f'{test_count} Testfälle/-konfigurationen, {failures} Fehler. Kampfregeln, Generator, Persistenz, Anwendung und Eingabeflanken.'],
 ['Generator','1000 Seeds mit gültigen Angeboten über alle zwölf Positionen.'],
 ['Kampagnen','144 Runs: 3 Startmodule x 2 Schwierigkeiten x 2 Routenpräferenzen x 12 Seeds; alle erreichten im aufgezeichneten Lauf den Sieg.'],
 ['JavaFX-UI','18 Komponentenprüfungen: Seed, Start, Bewegung, Pause, Optionen, Hilfe, Werkstatt, Modul, Route, Save und Resume.'],
 ['Render-Probelauf',f'{soak["seconds"]:.0f} s, {soak["frames"]} Frames, {soak["completedRuns"]} vollständige Runs, {soak["uncaughtErrors"]} unbehandelte Fehler. Simulation vierfach beschleunigt.'],
 ['CPU / Frame-Abstand',f'CPU-Zeichenkosten P95 {soak["updateAndDrawCpuMsP95"]:.3f} ms; Frame-Abstand P95 {soak["frameIntervalMsP95"]:.3f} ms. Keine GPU-Endzeitmessung.'],
 ['Audio','16 native Clips geladen und mit Lautstärke null aufgerufen. Kein Hörtest.']],[112,W-2*M-112])
para('Der Testspieler verwendet reguläre InputFrames; er erhält keine Unverwundbarkeit und tötet Gegner nicht direkt. Er reagiert aber exakt und überspringt menschliche Lese- und Laufpausen zwischen Räumen. Seine Dauer ist deshalb keine gemessene menschliche Run-Zeit.')
para('Offen: echter Erstnutzertest, Kampfgefühl, Audioabmischung, Betriebssystem-Eingaben, Vollbild-/Monitorwechsel und andere Geräte. Der Desktop war im Nachtlauf gesperrt; Komponententests wurden innerhalb von JavaFX ausgeführt.','small')

page('7 / Projektmanagement und Risiken')
para('Der autonome Nachtlauf ist keine nachträglich erfundene Teamiteration. Tatsächliche Personenstunden, Rollen, Besprechungen und Abnahmen liegen nicht vor. Das Arbeitsjournal dokumentiert technische Meilensteine; die zugestandenen zwölf Stunden sind eine Obergrenze, kein Ist-Aufwand.')
table(['Nächste Iteration','Vorgeschlagenes Ergebnis / Schätzung'],[
 ['Verstehen / abgrenzen','Modulrahmen und Themenwahl bestätigen, Ownership verteilen, Architektur erklären: 4-8 Personenstunden.'],
 ['Erstnutzertest','3-5 echte Personen beobachten, erste zehn Minuten prüfen, Probleme priorisieren: 6-10 Personenstunden.'],
 ['Spielgefühl','Trefferfeedback, Gegnerdruck, Reichweite und Run-Dauer anhand Beobachtungen korrigieren: 8-16 Personenstunden.'],
 ['Zielplattform / Abgabe','Teamrechner bauen/testen, Bericht überarbeiten und Quellen/Javadoc paketieren: 6-12 Personenstunden.']],[130,W-2*M-130])
para('Die Spannen sind neue Vorschläge, keine gemessenen Zeiten. Nach jeder tatsächlichen Iteration Plan und Ist-Aufwand, erreichte Ziele und Massnahmen vergleichen.')
table(['Risiko','Massnahme'],[
 ['Übernommener Code wird nicht verstanden','Hoch / hoch: eigener Ablauf- und Änderungsreview je Teilgebiet.'],
 ['Bibliothek oder Thema nicht freigegeben','Offen / hoch: Fachdozentenentscheid einholen.'],
 ['Kampf repetitiv / Stil uneinheitlich','Mittel / hoch: menschlicher Test und gemeinsamer Art-Pass.'],
 ['Andere Rechner / Save-Abbruch','Mittel / hoch bzw. niedrig / mittel: Plattformtests, Backups, bekannte Zwei-Dateien-Grenze.'],
 ['Umfang wächst','Hoch / hoch: klaren Kern halten; Multiplayer und Enginewechsel separat entscheiden.']],[170,W-2*M-170])

page('8 / KI-Einsatz und Quellen')
para('Der Ausgangsstand wurde weitgehend von Codex erstellt. David gab Idee, Art Direction und autonomen Arbeitsauftrag vor. Das Team muss Ergebnisse vor einer Abgabe selbst verstehen, prüfen und begründen. Die Tabelle folgt dem lokalen KI-Merkblatt, Seite 1.','small')
table(['KI / Ziel','Aufwand','Verwendung'],[
 ['Codex / Implementierung','Hoch','Weitgehend übernommen; gebaut, getestet und korrigiert.'],
 ['Codex / Tests und QA','Mittel-hoch','Testcode und Protokolle im Projekt; keine behauptete menschliche Abnahme.'],
 ['Imagegen / Raumgrafiken','Mittel','Rasterplatten mit Referenzbild; Prompts und Exporte erhalten.'],
 ['Codex / Blender und Audio','Hoch / mittel','Editierbare Modelle, Exportscript und eigene Synthese-WAVs.'],
 ['Codex / Bericht und UML','Hoch','Arbeitsfassung an tatsächlicher Implementierung; Teamreview ausstehend.']],[170,85,W-2*M-255])
para('Quellenbestand','h2')
para('1. PM3 HS26: Einführung und Kick-off.pdf, physische Seiten 17/19. JavaFX-Richtung, Java-Schwerpunkt, eigene Architektur und Abstimmung externer Bibliotheken.\n2. Auftrag Lösungsarchitektur (M2).pdf, Version 1.0, Seiten 1-3. Berichtstruktur, Quellcode/Javadoc, Use Cases, Architektur, mindestens vier Interaktionsdiagramme und Projektmanagement.\n3. Verwendung von KI im PM3.pdf, Version 1.0, Seite 1. Nachvollziehbare Dokumentation von Tool, Ziel, Aufwand und Übernahme.','small')
para('Originale unverändert im lokalen Semester-Wiki, Export 14.09.2026. Quellen-IDs und SHA-256-Werte stehen in docs/CREDITS.md. Keine Behauptung aktueller Moodle-Prüfung.','small')
para('Drittbestandteile: Barlow / Barlow Condensed unter SIL OFL 1.1 (google/fonts); OpenJFX und OpenJDK mit Laufzeit-Lizenzdateien. Entwicklungswerkzeuge umfassen Gradle, JUnit, Blender, PlantUML und google-java-format. Keine fremden Soundsamples oder Spritepacks.','small')
para('Vertiefung und editierbare Belege: docs/ANFORDERUNGEN.md, ARCHITEKTUR.md, TESTSTRATEGIE.md, PROJEKTMANAGEMENT.md, KI_EINSATZ.md, CREDITS.md, GLOSSAR.md sowie docs/diagrams/.','small')

c.save()
print('REPORT_READY',FILE,'pages',PAGE)
