#!/usr/bin/env python3
"""Erzeugt die beiden ABYSS-Präsentationen als PDF und die zugehörigen Sprechtexte.

Quellen: eigene Pixelschrift (src/main/resources/pixel/font.txt), Spielszenen aus
`./gradlew sceneShot --args="build/scenes 0,4,9,14,19 4"`, Bildschirmfotos aus docs/qa/screens
und die UML-Diagramme aus docs/diagrams (SVG, per rsvg-convert hochaufgelöst gerendert).

Aufruf aus dem Projektordner:  python3 tools/create_slides.py
Ausgabe:                       docs/praesentationen/*.pdf und *_Sprechtext.md
"""
from __future__ import annotations

import shutil
import subprocess
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

from PIL import Image, ImageDraw, ImageFilter, ImageFont, JpegImagePlugin  # noqa: F401 (PDF-Export braucht JPEG)

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs' / 'praesentationen'
SCENES = ROOT / 'build' / 'scenes'
SCREENS = ROOT / 'docs' / 'qa' / 'screens'
DIAGRAMS = ROOT / 'docs' / 'diagrams'
CACHE = ROOT / 'build' / 'slides'
W, H = 1920, 1080

# Farben aus der Bordsystem-GUI (ui.gui.Gui / Pal)
BG_TOP, BG_BOTTOM = (11, 17, 23), (4, 7, 10)
PANEL, PANEL_LIGHT, EDGE = (13, 20, 27), (21, 27, 34), (58, 70, 84)
BONE, MUTED, DIM = (233, 226, 207), (143, 163, 176), (85, 100, 111)
AMBER, TEAL, RED, VIOLET, GREEN = (242, 166, 58), (79, 214, 196), (226, 86, 70), (170, 128, 240), (126, 214, 104)

SANS = '/System/Library/Fonts/Supplemental/Arial.ttf'
SANS_BOLD = '/System/Library/Fonts/Supplemental/Arial Bold.ttf'


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    try:
        return ImageFont.truetype(SANS_BOLD if bold else SANS, size)
    except OSError:
        return ImageFont.load_default(size)


# ---------------------------------------------------------------- Pixelschrift


class PixelFont:
    SUBSTITUTE = {'−': '-', '—': '–', '’': "'", '‘': "'", 'É': 'E', 'È': 'E', 'À': 'A'}

    def __init__(self, path: Path):
        self.glyphs: dict[str, list[str]] = {}
        current, rows = None, []
        for line in path.read_text(encoding='utf-8').splitlines():
            if line.startswith('@') and len(line) >= 2:
                if current is not None:
                    self.glyphs[current] = rows
                current, rows = line[1:], []
            elif current is not None and line:
                rows.append(line)
        if current is not None:
            self.glyphs[current] = rows

    def glyph(self, ch: str) -> list[str]:
        if ch == ' ':
            return ['...']
        ch = self.SUBSTITUTE.get(ch, ch)
        return self.glyphs.get(ch) or self.glyphs.get(ch.upper()) or self.glyphs['?']

    def width(self, text: str) -> int:
        return max(0, sum(max(len(r) for r in self.glyph(c)) + 1 for c in text) - 1)

    def mask(self, text: str, scale: int) -> Image.Image:
        w = max(1, self.width(text))
        m = Image.new('L', (w, 9), 0)
        px = m.load()
        x = 0
        for ch in text:
            rows = self.glyph(ch)
            if ch != ' ':
                for r, row in enumerate(rows):
                    for c, bit in enumerate(row):
                        if bit == '#':
                            px[x + c, r] = 255
            x += max(len(r) for r in rows) + 1
        return m.resize((w * scale, 9 * scale), Image.NEAREST)

    def draw(self, img, xy, text, scale, color, anchor='l', shadow=True, glow=None):
        m = self.mask(text, scale)
        x, y = xy
        if anchor == 'c':
            x -= m.width // 2
        elif anchor == 'r':
            x -= m.width
        if glow:
            pad = scale * 10
            g = Image.new('L', (m.width + 2 * pad, m.height + 2 * pad), 0)
            g.paste(m, (pad, pad))
            g = g.filter(ImageFilter.GaussianBlur(scale * 3)).point(lambda v: int(v * .55))
            img.paste(glow, (x - pad, y - pad), g)
        if shadow:
            img.paste((0, 0, 0), (x + max(1, scale // 2), y + max(1, scale // 2)), m)
        img.paste(color, (x, y), m)
        return m.width


PX = PixelFont(ROOT / 'src' / 'main' / 'resources' / 'pixel' / 'font.txt')


# ---------------------------------------------------------------- Bilder


def diagram(name: str) -> Path:
    """Rendert ein SVG-Diagramm dreifach vergrössert; fällt auf die PNG-Fassung zurück."""
    CACHE.mkdir(parents=True, exist_ok=True)
    target = CACHE / f'{name}@3x.png'
    svg = DIAGRAMS / f'{name}.svg'
    if not target.exists() and shutil.which('rsvg-convert') and svg.exists():
        subprocess.run(['rsvg-convert', '-z', '3', '-b', 'white', '-o', str(target), str(svg)], check=True)
    return target if target.exists() else DIAGRAMS / f'{name}.png'


def scene(name: str) -> Path:
    path = SCENES / f'{name}.png'
    if not path.exists():
        raise SystemExit(f'{path} fehlt – zuerst ./gradlew sceneShot --args="build/scenes 0,4,9,14,19 4" ausführen')
    return path


def screen(name: str) -> Path:
    return SCREENS / f'{name}.png'


def load_art(path: Path) -> Image.Image:
    """Lädt Pixelbilder und reduziert sie auf das 480 × 270-Grundraster, damit sie scharf skalieren."""
    im = Image.open(path).convert('RGB')
    if im.size[0] % 480 == 0 and im.size[1] % 270 == 0 and im.size[0] > 480:
        im = im.resize((480, 270), Image.NEAREST)
        im.info['pixel'] = True
    return im


def fitted(im: Image.Image, bw: int, bh: int, cover=False) -> Image.Image:
    s = (max if cover else min)(bw / im.width, bh / im.height)
    size = (max(1, round(im.width * s)), max(1, round(im.height * s)))
    pixel = im.info.get('pixel') or (s >= 1.5 and im.width <= 500)
    out = im.resize(size, Image.NEAREST if pixel else Image.LANCZOS)
    if cover:
        left, top = (out.width - bw) // 2, (out.height - bh) // 2
        out = out.crop((left, top, left + bw, top + bh))
    return out


# ---------------------------------------------------------------- Folie


@dataclass
class Slide:
    kicker: str
    title: str
    paint: Callable[['Canvas'], None]
    notes: list[str] = field(default_factory=list)
    minutes: float = 1.0
    plain: bool = False


class Canvas:
    def __init__(self, deck: str, index: int, total: int):
        self.img = Image.new('RGB', (W, H), BG_BOTTOM)
        self.d = ImageDraw.Draw(self.img)
        self.deck, self.index, self.total = deck, index, total
        for y in range(H):
            t = y / H
            c = tuple(round(a + (b - a) * t) for a, b in zip(BG_TOP, BG_BOTTOM))
            self.d.line([(0, y), (W, y)], fill=c)
        for y in range(8, H, 24):
            for x in range(8, W, 24):
                self.d.point((x, y), fill=(18, 27, 35))

    # -- Rahmen
    def header(self, kicker: str, title: str):
        d = self.d
        d.rectangle([0, 0, W, 150], fill=(7, 11, 16))
        for x in range(0, 26):
            for y in range(0, 150):
                if ((x + y) // 8) % 2 == 0:
                    d.point((x, y), fill=(74, 52, 18))
        d.rectangle([0, 150, W, 151], fill=(42, 52, 64))
        d.rectangle([0, 152, W, 154], fill=(0, 0, 0))
        PX.draw(self.img, (70, 34), kicker.upper(), 3, AMBER, shadow=False)
        PX.draw(self.img, (68, 72), title.upper(), 7, BONE)

    def footer(self):
        PX.draw(self.img, (70, H - 42), f'ABYSS · {self.deck}'.upper(), 2, DIM, shadow=False)
        PX.draw(self.img, (W - 70, H - 42), f'{self.index:02d}/{self.total:02d}', 2, DIM, anchor='r', shadow=False)

    # -- Bausteine
    def panel(self, x, y, w, h, fill=PANEL, accent=None):
        d = self.d
        d.rectangle([x, y, x + w, y + h], fill=fill, outline=(5, 8, 12), width=3)
        d.line([(x + 3, y + 3), (x + w - 3, y + 3)], fill=EDGE, width=2)
        d.line([(x + 3, y + 3), (x + 3, y + h - 3)], fill=(35, 45, 56), width=2)
        if accent:
            d.rectangle([x + 3, y + 3, x + 9, y + h - 3], fill=accent)

    def image(self, path: Path, x, y, w, h, caption=None, cover=False, white=False):
        im = load_art(path)
        pad = 18 if white else 0
        fit = fitted(im, w - 2 * pad, h - 2 * pad, cover)
        if white:
            self.d.rectangle([x, y, x + w, y + h], fill=(250, 250, 248), outline=(5, 8, 12), width=3)
            self.img.paste(fit, (x + (w - fit.width) // 2, y + (h - fit.height) // 2))
        else:
            ix, iy = x + (w - fit.width) // 2, y + (h - fit.height) // 2
            self.d.rectangle([ix - 6, iy - 6, ix + fit.width + 5, iy + fit.height + 5], fill=(5, 8, 12))
            self.d.rectangle([ix - 3, iy - 3, ix + fit.width + 2, iy + fit.height + 2], outline=EDGE, width=2)
            self.img.paste(fit, (ix, iy))
            y, h = iy - 6, fit.height + 12
        if caption:
            self.d.text((x + w // 2, y + h + 14), caption, font=font(22), fill=MUTED, anchor='ma')

    def text(self, x, y, w, content, size=32, color=BONE, bold=False, gap=1.3) -> int:
        """Absatz mit Umbruch; **fett** markiert Hervorhebungen. Liefert die neue y-Position."""
        words = []  # (Wort, fett, klebt am Vorgänger)
        parts = content.split('**')
        for i, part in enumerate(parts):
            touching = i > 0 and not parts[i - 1].endswith(' ') and not part.startswith(' ')
            for j, word in enumerate(part.split(' ')):
                if word:
                    words.append((word, bold or i % 2 == 1, touching and j == 0 and bool(words)))
        line, lw = [], 0
        space = font(size).getlength(' ')

        def flush(yy):
            xx = x
            for k, (word, b, glued) in enumerate(line):
                if k and not glued:
                    xx += space
                f = font(size, b)
                self.d.text((xx, yy), word, font=f, fill=color if not b or color != BONE else (255, 250, 236))
                xx += f.getlength(word)
            return yy + round(size * gap)

        for word, b, glued in words:
            wl = font(size, b).getlength(word)
            gap_before = 0 if glued or not line else space
            if line and lw + gap_before + wl > w and not glued:
                y = flush(y)
                line, lw, gap_before = [], 0, 0
            line.append((word, b, glued and bool(line)))
            lw += gap_before + wl
        if line:
            y = flush(y)
        return y

    def bullets(self, x, y, w, items, size=32, color=BONE, marker=AMBER, spacing=18) -> int:
        for item in items:
            self.d.rectangle([x, y + size * .38, x + 11, y + size * .38 + 11], fill=marker)
            y = self.text(x + 34, y, w - 34, item, size, color) + spacing
        return y

    def table(self, x, y, widths, rows, size=25, head=AMBER, row_pad=14) -> int:
        d = self.d
        total = sum(widths)
        for r, row in enumerate(rows):
            lines = []
            for c, cell in enumerate(row):
                lines.append(self.wrap(cell, font(size, r == 0 or c == 0), widths[c] - 28))
            h = max(len(l) for l in lines) * round(size * 1.25) + 2 * row_pad
            fill = head if r == 0 else (PANEL_LIGHT if r % 2 else PANEL)
            d.rectangle([x, y, x + total, y + h], fill=fill)
            cx = x
            for c, cell_lines in enumerate(lines):
                ty = y + row_pad
                for ln in cell_lines:
                    d.text((cx + 14, ty), ln, font=font(size, r == 0 or (c == 0)), fill=(10, 12, 14) if r == 0 else (BONE if c == 0 else (205, 212, 214)))
                    ty += round(size * 1.25)
                cx += widths[c]
            y += h
        d.rectangle([x, y, x + total, y + 2], fill=EDGE)
        return y

    @staticmethod
    def wrap(text, f, width):
        out, line = [], ''
        for word in text.split(' '):
            trial = (line + ' ' + word).strip()
            if f.getlength(trial) > width and line:
                out.append(line)
                line = word
            else:
                line = trial
        out.append(line)
        return out

    def stat(self, x, y, w, number, label, color=TEAL):
        self.panel(x, y, w, 170)
        PX.draw(self.img, (x + w // 2, y + 26), number, 8, color, anchor='c', glow=color)
        self.d.text((x + w // 2, y + 118), label, font=font(25), fill=MUTED, anchor='ma')

    def card(self, x, y, w, h, title, body, accent=TEAL, size=26):
        self.panel(x, y, w, h, accent=accent)
        PX.draw(self.img, (x + 30, y + 26), title.upper(), 3, accent)
        self.text(x + 30, y + 70, w - 56, body, size, BONE)

    def arrow(self, x1, y1, x2, y2, color=AMBER):
        self.d.line([(x1, y1), (x2, y2)], fill=color, width=5)
        self.d.polygon([(x2, y2 - 12), (x2 + 18, y2), (x2, y2 + 12)], fill=color)

    def backdrop(self, path: Path, darken=.55):
        im = fitted(load_art(path), W, H, cover=True)
        shade = Image.new('RGB', (W, H), (0, 0, 0))
        self.img.paste(Image.blend(im, shade, darken))

    def veil(self, solid: int, fade: int, color=(5, 8, 12), bottom=False):
        """Deckt den oberen (oder unteren) Bildteil ab, z. B. eingebrannte Titel oder HUD, und blendet weich aus."""
        alpha = Image.new('L', (1, H), 0)
        for y in range(H):
            d = H - 1 - y if bottom else y
            alpha.putpixel((0, y), 255 if d < solid else max(0, round(255 * (1 - (d - solid) / fade))))
        self.img.paste(color, (0, 0), alpha.resize((W, H)))

    def sprites(self, sheet: Path, scale: int, rows: list[int], x, y, w, h, zoom: int):
        """Stellt je Zeile eines ArtSheet-Bogens das erste Bild frei und reiht die Figuren auf."""
        im = Image.open(sheet).convert('RGBA')
        cell = im.width // 17
        figures = []
        for r in rows:
            c = im.crop((0, r * cell, cell, (r + 1) * cell))
            c = c.resize((cell // scale, cell // scale), Image.NEAREST)
            px = c.load()
            for yy in range(c.height):
                for xx in range(c.width):
                    if px[xx, yy][:3] in ((42, 52, 64), (34, 42, 52)):
                        px[xx, yy] = (0, 0, 0, 0)
            box = c.getbbox()
            if box:
                c = c.crop(box)
                figures.append(c.resize((c.width * zoom, c.height * zoom), Image.NEAREST))
        total = sum(f.width for f in figures)
        gap = max(10, (w - total) // max(1, len(figures) - 1)) if len(figures) > 1 else 0
        xx = x + max(0, (w - total - gap * (len(figures) - 1)) // 2)
        for f in figures:
            self.img.paste(f, (xx, y + h - f.height), f)
            xx += f.width + gap


def render(deck_name: str, file_stem: str, slides: list[Slide], intro: str):
    OUT.mkdir(parents=True, exist_ok=True)
    pages = []
    for i, slide in enumerate(slides, 1):
        c = Canvas(deck_name, i, len(slides))
        if not slide.plain:
            c.header(slide.kicker, slide.title)
        slide.paint(c)
        c.footer()
        pages.append(c.img)
    pdf = OUT / f'{file_stem}.pdf'
    pages[0].save(pdf, save_all=True, append_images=pages[1:], resolution=144, quality=90)
    preview = OUT / 'vorschau'
    preview.mkdir(exist_ok=True)
    pages[0].resize((960, 540), Image.LANCZOS).save(preview / f'{file_stem}.png', optimize=True)
    minutes = sum(s.minutes for s in slides)
    md = [f'# Sprechtext · {deck_name}', '', intro, '',
          f'Geplante Redezeit: etwa {minutes:.0f} Minuten, {len(slides)} Folien. PDF: [{pdf.name}]({pdf.name}).', '']
    for i, slide in enumerate(slides, 1):
        md.append(f'## {i:02d} · {slide.title} (≈ {slide.minutes:g} min)')
        md.append('')
        md += [f'- {n}' for n in slide.notes]
        md.append('')
    (OUT / f'{file_stem}_Sprechtext.md').write_text('\n'.join(md), encoding='utf-8')
    print(f'{pdf.relative_to(ROOT)}: {len(slides)} Folien, {pdf.stat().st_size / 1e6:.1f} MB')
    return pages


# ================================================================ M1 · Projektskizze


def m1_slides() -> list[Slide]:
    s: list[Slide] = []

    def title(c: Canvas):
        c.backdrop(scene('title'), .15)
        c.d.rectangle([0, 700, W, H], fill=(5, 8, 12))
        c.d.rectangle([0, 700, W, 704], fill=AMBER)
        PX.draw(c.img, (100, 752), 'M1 · PROJEKTSKIZZE', 4, AMBER, shadow=False)
        c.d.text((100, 820), 'Ein Pixel-Roguelite im Inneren eines langen U-Boots', font=font(46, True), fill=BONE)
        c.d.text((100, 890), 'PM3 Software-Projekt 3 · HS26 · Team ABYSS · Pitch an die Geschäftsleitung', font=font(30), fill=MUTED)
    s.append(Slide('', 'Titel', title, [
        'Team vorstellen: Namen, Rollen (wird vom Team ergänzt).',
        'Ein Satz zur Idee: „ABYSS ist ein Action-Roguelite: Man kämpft sich im Inneren eines U-Boots vom Heck bis zur Brücke vor.“',
        'Ziel der Präsentation: Die Geschäftsleitung soll entscheiden, ob das Projekt in dieser Form umgesetzt wird.'],
        0.5, plain=True))

    def ausgangslage(c: Canvas):
        y = c.text(90, 200, 820, 'Im PM3 entwickelt das Team in einem Semester eine lauffähige Java-Software mit **eigener Architektur**. JavaFX ist als Oberfläche vorgesehen.', 34)
        y = c.bullets(90, y + 30, 820, [
            '**Kurze Sitzungen** sind gefragt: 20–30 Minuten zwischen Vorlesungen.',
            '**Roguelites** verbinden zufällige Durchläufe mit Fortschritt über viele Versuche, z. B. Dead Cells, Hades.',
            '**Fahrzeug als Welt**: FTL oder Barotrauma steuern ein Schiff, kämpfen aber nicht Raum für Raum.',
            'Kein externer Auftraggeber: Anforderungen stammen aus einer **Persona** und werden mit Testpersonen überprüft.'], 30)
        c.image(scene('scene-14'), 980, 200, 850, 478, 'Prototyp 1.1: Kampf in der Forschungssektion')
        c.panel(90, 790, 1740, 190, accent=TEAL)
        PX.draw(c.img, (130, 820), 'LEITFRAGE', 3, TEAL)
        c.text(130, 866, 1660, 'Wie wird **ein einziges Fahrzeug** zu einer abwechslungsreichen Spielwelt für kurze, faire Sitzungen?', 34)
    s.append(Slide('1 · Ausgangslage', 'Warum dieses Projekt?', ausgangslage, [
        'Rahmen: PM3 verlangt ein Semesterprojekt in Java mit eigener Architektur, JavaFX als Oberfläche.',
        'Beobachtung: Viele spielen in kurzen Pausen. Roguelites passen dazu, weil jeder Versuch in sich abgeschlossen ist.',
        'Lücke: Spiele mit einem Fahrzeug als Welt steuern das Fahrzeug. Einen Kampf durch das Innere eines einzigen Schiffs gibt es kaum.',
        'Da es keinen Kunden gibt, arbeiten wir mit einer Persona und prüfen sie mit Testspielenden.'], 1))

    def idee(c: Canvas):
        c.image(scene('map'), 90, 185, 1740, 450, cover=True)
        PX.draw(c.img, (160, 214), 'HECK', 3, AMBER)
        PX.draw(c.img, (1760, 214), 'BRÜCKE', 3, TEAL, anchor='r')
        c.arrow(300, 226, 1560, 226, AMBER)
        cw = 555
        c.card(90, 675, cw, 305, '24 Räume, 4 Sektionen', 'Heck, Maschinen, Forschung, Kommando. Am Ende jeder Sektion wartet ein **Wächter**.', AMBER, 30)
        c.card(90 + cw + 37, 675, cw, 305, 'Raum für Raum', 'Kampf oder Halt: Händler, Werkstatt, Kapelle. An jedem Schott **wählt man den Weg**.', TEAL, 30)
        c.card(90 + 2 * (cw + 37), 675, cw, 305, 'Snowpiercer-Prinzip', 'Hinten Wartungsgänge, vorne die Kontrolle. **Scheitern** heisst: neuer Versuch im Heck.', VIOLET, 30)
    s.append(Slide('2 · Idee', 'Vom Heck bis zur Brücke', idee, [
        'Das U-Boot ist die ganze Welt. Man startet ganz hinten und will nach vorne auf die Brücke.',
        'Ein Durchlauf heisst Tauchgang: 24 Räume in vier Sektionen, jede endet mit einem Wächter.',
        'Inspiration Snowpiercer: soziale und räumliche Hierarchie von hinten nach vorne. Figuren und Handlung sind eigenständig.',
        'Gelöstes Problem: abgeschlossene, abwechslungsreiche Sitzungen mit verständlichen Entscheidungen.'], 1))

    def nutzen(c: Canvas):
        c.panel(90, 190, 560, 800, accent=AMBER)
        c.image(screen('wardrobe'), 130, 225, 480, 270)
        PX.draw(c.img, (130, 530), 'PERSONA LINA', 4, AMBER)
        c.bullets(130, 595, 490, ['22, Studentin, Laptop', 'spielt in Pausen von **20–30 Minuten**', 'mag kurze Actionspiele und abwechslungsreiche Ausrüstung', 'akzeptiert Scheitern, wenn sie **die Ursache versteht**'], 27, spacing=12)
        c.d.text((130, 940), 'Annahme, wird mit Testpersonen überprüft', font=font(22), fill=DIM)
        cards = [
            ('Einstieg ohne Hürde', 'Lokal, ohne Konto und Internet. Der erste Raum erklärt die Steuerung.', TEAL, screen('play-start')),
            ('Unterbrechbar', 'Pause jederzeit. Der letzte Raumeingang wird gesichert.', GREEN, screen('pause')),
            ('Faire Niederlagen', 'Gegner kündigen Angriffe sichtbar an. Fehler sind nachvollziehbar.', RED, scene('machine-press')),
            ('Abwechslung', 'Route, Räume, Gegner und Belohnungen ändern sich bei jedem Versuch.', VIOLET, screen('route'))]
        for i, (t, b, col, img) in enumerate(cards):
            x, y = 700 + (i % 2) * 575, 190 + (i // 2) * 410
            c.card(x, y, 555, 390, t, b, col, 26)
            c.image(img, x + 110, y + 190, 350, 180)
    s.append(Slide('3 · Kundennutzen', 'Für wen und wozu?', nutzen, [
        'Persona Lina ist eine Annahme und wird im Semester mit echten Testpersonen überprüft.',
        'Vier Nutzenversprechen: kein Einstiegshindernis, jederzeit unterbrechbar, faire und verständliche Niederlagen, jeder Versuch anders.',
        'Beispiel „faire Niederlage“: Jeder Gegner leuchtet vor seinem Angriff auf. So sieht Lina, was sie falsch gemacht hat.'], 1))

    def konkurrenz(c: Canvas):
        c.d.text((90, 188), 'Tabelle 1: Einordnung gegenüber verwandten Spielen (eigene Darstellung)', font=font(24), fill=MUTED)
        y = c.table(90, 230, [300, 380, 520, 540], [
            ['Spiel', 'Genre / Kern', 'Gemeinsam mit ABYSS', 'Unterschied'],
            ['Dead Cells', 'Roguelite-Action-Plattformer', 'Durchläufe, Permadeath, Fortschritt', 'keine lineare Reise durch ein Fahrzeug'],
            ['Hades', 'Roguelite-Dungeon-Crawler', 'Raum-für-Raum-Kämpfe, Upgrades', 'Draufsicht, Mythologie statt Maschinen'],
            ['FTL', 'Raumschiff-Roguelike', 'Routenwahl, Entscheidungen', 'Schiff steuern, kein Plattformkampf'],
            ['Barotrauma', 'Koop-U-Boot-Simulation', 'U-Boot-Setting, 2D', 'Mehrspieler-Simulation, keine Roguelite-Kampagne'],
            ['ABYSS', 'Action-Roguelite', '–', 'lineare Kampfreise im Fahrzeug + nutzbare Raumtechnik']], 29, row_pad=22)
        c.panel(90, y + 40, 1740, 150, accent=TEAL)
        c.text(130, y + 72, 1660, '**Alleinstellung:** Kein Vergleichsspiel verbindet eine lineare Kampfreise durch **ein** Fahrzeug mit Roguelite-Wiederholung. Dazu kommt Raumtechnik, die man gegen Gegner einsetzen kann: Pressen, Laser, Notschalter.', 30)
    s.append(Slide('4 · Konkurrenzanalyse', 'Stand der Technik', konkurrenz, [
        'Vier bekannte Spiele decken jeweils einen Teil der Idee ab. Quellen siehe Projektskizze [1]–[4].',
        'Keines verbindet die lineare Reise durch ein Fahrzeug mit Roguelite-Durchläufen.',
        'Als Studienprojekt konkurrieren wir nicht kommerziell. Die Tabelle grenzt den Umfang ab.'], 0.75))

    def szenario(c: Canvas):
        steps = [
            (screen('loadout'), 'Schleuse', 'Lina wählt eine Taucherin mit erklärten Stärken.'),
            (scene('scene-00'), 'Erster Raum', 'Angriff wird angekündigt, sie weicht aus.'),
            (screen('reward'), 'Bergung', 'Sie wählt ein Modul, das ihren Angriff stärkt.'),
            (screen('route-condition'), 'Routenwahl', 'Angeschlagen wählt sie das sichere Depot.'),
            (scene('machine-press'), 'Raumtechnik', 'Sie lockt einen Gepanzerten unter die Presse.'),
            (screen('boss'), 'Wächter', 'Knapp verloren. Die Ursache ist klar, Kerne bleiben.')]
        w, gap = 540, 60
        for i, (img, t, b) in enumerate(steps):
            x = 90 + (i % 3) * (w + gap)
            y = 185 + (i // 3) * 415
            c.image(img, x, y, w, 292)
            PX.draw(c.img, (x + 14, y + 306), f'{i + 1} · {t}'.upper(), 3, AMBER)
            c.text(x + 14, y + 344, w - 20, b, 23, MUTED)
            if i % 3 < 2:
                c.arrow(x + w + 10, y + 146, x + w + gap - 26, y + 146)
    s.append(Slide('5 · Kontextszenario', 'Eine halbe Stunde mit Lina', szenario, [
        'Lina hat nach der Vorlesung 30 Minuten und startet einen Tauchgang.',
        'Sie wählt eine Taucherin, lernt im ersten Raum auszuweichen und wählt nach dem Sieg ein Modul.',
        'An der Abzweigung wählt sie wegen tiefer Integrität das sichere Depot statt des Frachtraums.',
        'Sie nutzt eine Hydraulikpresse gegen einen gepanzerten Gegner: Die Umgebung wird zum Werkzeug.',
        'Gegen den Wächter verliert sie knapp. Sie versteht warum, behält Datenkerne und schaltet damit Neues frei. Ergebnis: Sie will den nächsten Versuch.'], 1.25))

    def anforderungen(c: Canvas):
        PX.draw(c.img, (90, 196), 'WICHTIGE FUNKTIONEN', 3, TEAL)
        c.bullets(90, 245, 800, [
            'Plattformbewegung: Sprung, Ausweichen, Kombos, aktive Module',
            '24 Räume, Routenwahl, Wächter, Händler, Werkstätten, Kapellen',
            'Module mit Seltenheit und **Resonanzen**, Klassen, Garderobe, Archiv',
            '**Raumzustände** (Stromausfall, Hüllenbruch …) und **Raumtechnik**',
            'Sicherung am Raumeingang, Fortsetzen, lokales Profil'], 29, marker=TEAL)
        PX.draw(c.img, (960, 196), 'QUALITÄT', 3, AMBER)
        c.table(960, 245, [130, 330, 410], [
            ['ID', 'Anforderung', 'Messgrösse'],
            ['Q-01', 'Testbarkeit', 'Spiellogik ohne JavaFX testbar'],
            ['Q-02', 'Robustheit', 'defekter Spielstand blockiert Start nicht'],
            ['Q-03', 'Performance', 'Bild deutlich unter 16 ms (60 FPS)'],
            ['Q-04', 'Bedienbarkeit', '4 von 5 Testpersonen erkennen Angriffsankündigung'],
            ['Q-05', 'Nachvollziehbarkeit', 'KI-Einsatz, Quellen, Tests dokumentiert']], 24)
        c.panel(90, 800, 1740, 170, accent=VIOLET)
        c.text(130, 830, 1660, '**Weiterführende Ideen:** Gamepad (braucht Bibliothek und Freigabe), frei belegbare Tasten, weitere Sektionen und Bosse, Story-Fragmente mit alternativen Enden.', 29)
    s.append(Slide('6 · Weitere Anforderungen', 'Was muss das Spiel können?', anforderungen, [
        'Links die Kernfunktionen, Details in ANFORDERUNGEN.md (F-01 bis F-15).',
        'Rechts die Qualitätsanforderungen mit Messgrösse. So können wir am Ende prüfen, ob wir sie erfüllt haben.',
        'Q-04 wird mit Testpersonen gemessen: Erkennen sie einen angekündigten Angriff ohne Erklärung?',
        'Ideen für später sind bewusst getrennt, damit der Umfang realistisch bleibt.'], 0.75))

    def ressourcen(c: Canvas):
        stack = [('Java 25', 'Sprache, Records, Sealed Types', AMBER), ('JavaFX 26', 'Fenster, Eingabe, Audio', TEAL),
                 ('Gradle', 'Build, Tests, Pakete', GREEN), ('JUnit', 'automatische Tests', VIOLET), ('Eigene Engine', 'keine Game-Engine laut Vorgabe', RED)]
        for i, (t, b, col) in enumerate(stack):
            x = 90 + i * 352
            c.panel(x, 190, 330, 190, accent=col)
            PX.draw(c.img, (x + 30, 220), t.upper(), 3, col)
            c.text(x + 30, 270, 280, b, 26, BONE)
        c.card(90, 410, 850, 300, 'Fehlendes Know-how', 'Spielphysik, Kollisionen, Echtzeit-Darstellung und Balancing sind **neu** für das Team. Wir erproben sie früh im Prototyp.', AMBER, 29)
        c.card(980, 410, 850, 300, 'Abgrenzung', '**Drin:** Einzelspieler, Tastatur/Maus, macOS geprüft. **Draussen:** Mehrspieler, Online, Storykampagne. Aufwand pro Person legt das Team fest.', TEAL, 29)
        c.panel(90, 740, 1740, 250)
        c.sprites(ROOT / 'build' / 'art' / 'enemies.png', 5, [0, 1, 2, 4, 6, 7, 8, 9, 10, 11, 12], 150, 760, 1640, 165, 3)
        c.d.text((960, 945), 'Keine Engine, keine fremden Grafiken: Figuren, Räume und Schrift entstehen im Java-Code.', font=font(24), fill=MUTED, anchor='ma')
    s.append(Slide('7 · Ressourcen', 'Womit bauen wir es?', ressourcen, [
        'Technik laut Kursvorgabe: Java, JavaFX, keine fremden Frameworks oder Engines.',
        'Neu für uns: Physik, Kollisionen, Echtzeit-Darstellung und Balancing. Deshalb kommen sie zuerst in den Prototyp.',
        'Klare Grenzen: kein Mehrspieler, nichts Online. Teamfähigkeiten und Stunden pro Woche ergänzt das Team.'], 0.75))

    def risiken(c: Canvas):
        c.table(90, 200, [620, 330, 790], [
            ['Risiko', 'Wahrsch. / Auswirkung', 'Massnahme'],
            ['Team versteht umfangreichen, teils KI-generierten Code nicht ausreichend', 'hoch / hoch', 'Paketverantwortung, Code-Reviews, eigene Begründungen in den Berichten'],
            ['Idee oder Technologie nicht freigegeben', 'offen / hoch', 'früh mit Dozierenden abstimmen, JavaFX und eigene Architektur belegen'],
            ['Kampf wirkt unfair oder eintönig', 'mittel / hoch', 'Spieltests mit Personen aus dem Umfeld, gezielt nachjustieren'],
            ['Echtzeit-Darstellung zu langsam', 'niedrig / mittel', 'eigene Pixel-Pipeline, Zeichenzeit messen (heute ≈ 1,9 ms)'],
            ['Unterschiedliche Betriebssysteme im Team', 'mittel / mittel', 'plattformneutraler Java-Code, Test auf allen Teamrechnern']], 30, row_pad=24)
    s.append(Slide('8 · Risiken', 'Was kann schiefgehen?', risiken, [
        'Grösstes Risiko: Der Prototyp ist stark KI-unterstützt entstanden. Das Team muss den Code verstehen und begründen können. Deshalb gibt es pro Paket eine verantwortliche Person und Reviews.',
        'Die Freigabe von Idee und Technik holen wir früh bei den Dozierenden ein.',
        'Spielgefühl ist schwer messbar: Wir beobachten Testpersonen, statt nur zu raten.'], 0.75))

    def planung(c: Canvas):
        x0, x1, y0 = 150, 1800, 330
        weeks = 14
        step = (x1 - x0) / weeks
        c.d.rectangle([x0, y0, x1, y0 + 6], fill=EDGE)
        for k in range(weeks + 1):
            x = x0 + k * step
            c.d.rectangle([x - 1, y0 - 10, x + 1, y0 + 16], fill=EDGE)
            if k < weeks:
                PX.draw(c.img, (int(x + step / 2), y0 - 50), f'SW{k + 1}', 2, MUTED, anchor='c', shadow=False)
        for week, label, col in [(3, 'M1 Projektskizze', AMBER), (9, 'M2 Lösungsarchitektur', TEAL), (13, 'M3 Prototyp', GREEN)]:
            x = x0 + (week - .5) * step
            c.d.line([(x, y0), (x, y0 + 290)], fill=col, width=3)
            c.d.polygon([(x, y0 - 16), (x + 16, y0 + 3), (x, y0 + 22), (x - 16, y0 + 3)], fill=col)
            c.d.text((x, y0 + 300), label, font=font(24, True), fill=col, anchor='ma')
        bars = [(3, 5.5, 'Iteration 1 · Spielgefühl', AMBER), (5.5, 8.3, 'Iteration 2 · Durchlauf', TEAL), (8.7, 12.3, 'Iteration 3 · Inhalt, Spieltests', VIOLET)]
        for i, (a, b, t, col) in enumerate(bars):
            y = y0 + 60 + i * 70
            c.d.rectangle([x0 + a * step, y, x0 + b * step, y + 50], fill=col)
            c.d.text((x0 + a * step + 14, y + 11), t, font=font(24, True), fill=(10, 12, 14))
        c.d.text((x0 + 2.5 * step, y0 + 335), 'ab 28.09.2026', font=font(22), fill=MUTED, anchor='ma')
        c.d.text((x0 + 8.5 * step, y0 + 335), 'ab 09.11.2026', font=font(22), fill=MUTED, anchor='ma')
        c.d.text((x0 + 12.5 * step, y0 + 335), 'ab 07.12.2026', font=font(22), fill=MUTED, anchor='ma')
        c.panel(90, 730, 1740, 260, accent=TEAL)
        c.text(130, 755, 1660, '**Use Cases:** Tauchgang vorbereiten, **zum nächsten Raum vordringen (Kernfall)**, Bergung wählen, handeln, unterbrechen und fortsetzen, Brücke erobern, erneut tauchen, freischalten, Aussehen anpassen, Optionen.', 29)
        c.text(130, 890, 1660, 'Ein weit fortgeschrittener Prototyp (1.1) existiert bereits. Die Iterationen dienen deshalb vor allem dem **Verständnis im Team**, Spieltests und Verfeinerung.', 26, MUTED)
    s.append(Slide('9 · Grobplanung', 'Meilensteine und Iterationen', planung, [
        'Drei Meilensteine laut HS26-Plan: M1 in SW3, M2 in SW9, M3 in SW13.',
        'Iteration 1 prüft das Spielgefühl, Iteration 2 einen kurzen vollständigen Durchlauf, Iteration 3 Inhalt und Spieltests.',
        'Der Kernfall ist „zum nächsten Raum vordringen“. Um ihn herum sind alle anderen Use Cases angeordnet.',
        'Offen gesagt: Ein Prototyp existiert schon. Wir nutzen die Zeit, um ihn zu verstehen, zu testen und zu verbessern.'], 1))

    def stand(c: Canvas):
        stats = [('24', 'Räume'), ('4', 'Wächter'), ('12', 'Gegnerarten'), ('41', 'Module'), ('5', 'Klassen'), ('106', 'Tests')]
        for i, (n, l) in enumerate(stats):
            c.stat(90 + i * 292, 190, 272, n, l, [AMBER, RED, VIOLET, TEAL, GREEN, BONE][i])
        c.image(scene('condition-breach'), 90, 400, 560, 315, 'Hüllenbruch: 40 s bis zur Flutung')
        c.image(scene('scene-04'), 680, 400, 560, 315, 'Wächter: der Schottmeister')
        c.image(scene('ending-8'), 1270, 400, 560, 315, 'Brücke erobert')
        c.panel(90, 800, 1740, 180, accent=AMBER)
        c.text(130, 830, 1660, '**Prototyp 1.1** läuft als Mac-App. Alles ist in Java gezeichnet: eigene Pixelschrift, Licht und Effekte, Menüs als Bordcomputer im Spielbild. Code und Grafik sind **weitgehend KI-unterstützt entstanden** und dokumentiert.', 29)
    s.append(Slide('Stand heute', 'Was schon läuft', stand, [
        'Kurze Live-Demo (1–2 Minuten): Titel, Schleuse, erster Raum, Bergung, Routenwahl.',
        'Zahlen des Prototyps 1.1: 24 Räume, 4 Wächter, 12 Gegnerarten, 41 passive Module, 5 Klassen, 106 automatische Tests.',
        'Transparenz: Der Prototyp ist mit KI-Unterstützung entstanden (Codex, Claude Code). Siehe KI_EINSATZ.md.'], 1))

    def schluss(c: Canvas):
        c.backdrop(scene('ending-2'), .1)
        c.veil(470, 260)
        PX.draw(c.img, (W // 2, 110), 'FRAGEN?', 12, BONE, anchor='c', glow=TEAL)
        c.d.text((W // 2, 290), 'Unser Antrag: ABYSS als PM3-Projekt umsetzen.', font=font(44, True), fill=BONE, anchor='ma')
        c.d.text((W // 2, 360), 'Nächste Schritte: Freigabe · Rollen verteilen · Iteration 1 „Spielgefühl“ · erste Spieltests', font=font(30), fill=MUTED, anchor='ma')
    s.append(Slide('', 'Fragen', schluss, [
        'Kurz zusammenfassen: Idee, Nutzen für Lina, Alleinstellung, Plan.',
        'Um die Freigabe bitten und Fragen beantworten.'], 0.25, plain=True))
    return s


# ================================================================ Aufbau & Architektur (M2/M3)


def arch_slides() -> list[Slide]:
    s: list[Slide] = []

    def title(c: Canvas):
        c.d.rectangle([0, 0, W, 520], fill=(5, 8, 12))
        band = fitted(load_art(scene('scene-14')), W, H - 524, cover=True)
        c.img.paste(Image.blend(band, Image.new('RGB', band.size), .2), (0, 524))
        c.d.rectangle([0, 520, W, 524], fill=TEAL)
        PX.draw(c.img, (100, 120), 'ABYSS', 16, BONE, glow=TEAL)
        PX.draw(c.img, (106, 290), 'AUFBAU UND ARCHITEKTUR', 5, TEAL)
        c.d.text((106, 380), 'M2 Lösungsarchitektur · M3 Prototyp · PM3 HS26 · Team ABYSS', font=font(34), fill=MUTED)
    s.append(Slide('', 'Titel', title, [
        'Worum es geht: wie ABYSS aufgebaut ist, von den Use Cases bis zur Implementation.',
        'Grundlage: Technischer Bericht I (M2) und II (M3) im Ordner docs.'], .5, plain=True))

    def ueberblick(c: Canvas):
        stats = [('95', 'Java-Klassen'), ('23 k', 'Zeilen Code'), ('106', 'JUnit-Tests'), ('1,9 ms', 'pro Bild')]
        for i, (n, l) in enumerate(stats):
            c.stat(90 + i * 440, 190, 420, n, l, [AMBER, TEAL, GREEN, VIOLET][i])
        c.image(scene('scene-14'), 90, 405, 980, 551)
        c.bullets(1120, 420, 710, [
            'Java 25, JavaFX 26, Gradle, keine Engine',
            'Eigene Domäne mit Plattformphysik und Kampf',
            'Software-Framebuffer 480 × 270 mit Licht und Bloom',
            'Alle Grafiken prozedural in Java gezeichnet',
            'Menüs als Bordcomputer im Spielbild'], 30)
    s.append(Slide('Überblick', 'Was ist gebaut?', ueberblick, [
        'Umfang Version 1.1: 95 Klassen im Hauptcode, rund 23 000 Zeilen, 13 Testklassen mit 106 Tests, 8 QA-Werkzeuge.',
        'Technik: nur Java und JavaFX. Physik, Rendering und GUI sind selbst gebaut.',
        'Ein Bild wird im Mittel in 1,9 ms berechnet. Das ist weit unter den 16 ms für 60 FPS.'], 1))

    def usecases(c: Canvas):
        d = c.d
        d.rectangle([520, 190, 1830, 1000], outline=EDGE, width=3)
        d.text((550, 204), 'ABYSS 1.1 · lokales Desktopspiel', font=font(24, True), fill=MUTED)
        cases = [('UC-01', 'Tauchgang vorbereiten', 'casual'), ('UC-02', 'Zum nächsten Raum vordringen', 'fully dressed'),
                 ('UC-03', 'Bergung wählen', 'casual'), ('UC-04', 'Handeln', 'casual'), ('UC-05', 'Unterbrechen / fortsetzen', 'casual'),
                 ('UC-06', 'Brücke erobern', 'casual'), ('UC-07', 'Erneut tauchen', 'brief'), ('UC-08', 'Im Archiv freischalten', 'brief'),
                 ('UC-09', 'Aussehen anpassen', 'brief'), ('UC-10', 'Ausrüstung / Karte ansehen', 'brief'), ('UC-11', 'Optionen und Steuerung', 'brief')]
        ax, ay = 270, 590
        spots = []
        for i, (uid, name, depth) in enumerate(cases):
            col, row = (0, i) if i < 6 else (1, i - 6)
            x = 600 + col * 620
            y = 260 + row * 122 + (col * 61)
            spots.append((x, y))
        for x, y in spots:
            d.line([(ax + 40, ay - 40), (x, y + 44)], fill=(60, 74, 88), width=2)
        for (x, y), (uid, name, depth) in zip(spots, cases):
            core = uid == 'UC-02'
            d.ellipse([x, y, x + 560, y + 88], fill=(40, 30, 12) if core else PANEL_LIGHT, outline=AMBER if core else EDGE, width=3)
            d.text((x + 280, y + 14), f'{uid} · {depth}', font=font(19), fill=AMBER if core else MUTED, anchor='ma')
            d.text((x + 280, y + 42), name, font=font(26, True), fill=BONE, anchor='ma')
        # Akteurin
        d.ellipse([ax - 28, ay - 170, ax + 28, ay - 114], outline=BONE, width=5)
        d.line([(ax, ay - 114), (ax, ay - 20)], fill=BONE, width=5)
        d.line([(ax - 60, ay - 80), (ax + 60, ay - 80)], fill=BONE, width=5)
        d.line([(ax, ay - 20), (ax - 45, ay + 50)], fill=BONE, width=5)
        d.line([(ax, ay - 20), (ax + 45, ay + 50)], fill=BONE, width=5)
        d.text((ax, ay + 70), 'Spielerin', font=font(30, True), fill=BONE, anchor='ma')
        d.text((ax, ay + 250), 'Nachbarsystem:', font=font(22), fill=MUTED, anchor='ma')
        d.text((ax, ay + 280), 'lokales Dateisystem', font=font(22, True), fill=MUTED, anchor='ma')
        d.text((ax, ay + 310), '(UC-05, UC-08)', font=font(22), fill=MUTED, anchor='ma')
    s.append(Slide('M2 · Anforderungen', 'Use-Case-Modell', usecases, [
        'Eine Akteurin, die Spielerin. Das Dateisystem ist Nachbarsystem für Sichern und Fortsetzen.',
        'UC-02 ist der Kernfall und vollständig ausgearbeitet (fully dressed), inklusive Erweiterungen wie Raumzustände und Raumtechnik.',
        'Die anderen Fälle sind je nach Wichtigkeit casual oder brief beschrieben.'], 1))

    def kernfall(c: Canvas):
        c.image(diagram('05-ssd-progress'), 90, 190, 620, 810, white=True)
        PX.draw(c.img, (760, 200), 'UC-02 HAUPTABLAUF', 3, AMBER)
        c.bullets(760, 250, 1070, [
            'Spielerin betritt den Raum, das System lädt Gegner, Zustand und Technik.',
            'Sie kämpft; das System simuliert in festen Schritten von 1/120 s.',
            'Alle Gegner besiegt: Raum gesichert, Bergung erscheint.',
            'Sie wählt eine Bergung und danach am Schott den nächsten Raum.',
            'Das System erzeugt den Raum aus dem Seed und sichert den Eingang.'], 28, spacing=12)
        c.panel(760, 790, 1070, 200, accent=RED)
        c.text(795, 815, 1010, '**Erweiterungen:** Integrität 0 → Niederlage (4a). Raumzustand, etwa Hüllenbruch mit 40 s bis zur Flutung (2c). Notschalter aktivieren (2d).', 26)
    s.append(Slide('M2 · Anforderungen', 'Kernfall und Systemsequenz', kernfall, [
        'Das Systemsequenzdiagramm zeigt die Systemoperationen des Kernfalls aus Sicht der Spielerin.',
        'Wichtig ist der feste Simulationsschritt: Die Spiellogik läuft unabhängig von der Bildrate.',
        'Erweiterungen decken Niederlage, Raumzustände und die Bedienung der Raumtechnik ab.'], 1))

    def domain(c: Canvas):
        c.image(diagram('02-domain'), 90, 190, 1740, 700, white=True)
        c.text(90, 915, 1740, 'Kernbegriffe: **Tauchgang** (Run) mit **Taucherin**, **Raum** mit Zustand und Anlagen, **Gegner**, **Modul**, **Bergung**, **Profil** mit Freischaltungen und Logbuch.', 28, MUTED)
    s.append(Slide('M2 · Analyse', 'Domänenmodell', domain, [
        'Das Domänenmodell zeigt die Begriffe der Spielwelt, noch ohne Software-Klassen.',
        'Ein Tauchgang besteht aus Räumen, jeder Raum hat Gegner, einen möglichen Zustand und Anlagen.',
        'Das Profil überlebt die Tauchgänge: Datenkerne, Freischaltungen, Logbuch.'], 1))

    def architektur(c: Canvas):
        c.image(diagram('03-architecture'), 90, 185, 1740, 480, white=True)
        c.table(90, 700, [520, 1220], [
            ['Schicht / Paket', 'Verantwortung'],
            ['domain', 'Spielregeln: GameRun als Aggregat, Combat, Physics, Gegnerstrategien, RoomGenerator'],
            ['application', 'GameService für Anwendungsfälle; Profile, Loadout, Unlock, Achievement'],
            ['ports / infrastructure', 'GameRepository ↔ FileGameRepository, AudioSystem'],
            ['ui.pixel · ui.art · ui.render · ui.gui · ui', 'Framebuffer, Pixel-Art, Renderer, Bordsystem-GUI, Fenster']], 24, row_pad=10)
    s.append(Slide('M2 · Architektur', 'Schichtenarchitektur', architektur, [
        'Die Abhängigkeiten zeigen nur nach innen: UI → Application → Domain. Die Domäne kennt weder JavaFX noch Dateien.',
        'Speicher läuft über einen Port (GameRepository). Die Datei-Implementierung ist austauschbar und im Test ersetzbar.',
        'Dadurch sind Spielregeln ohne Fenster testbar (Q-01).'], 1.5))

    def entscheidungen(c: Canvas):
        c.table(90, 195, [470, 700, 570], [
            ['Einflussfaktor', 'Lösung', 'Beleg'],
            ['Kursvorgabe: Java, eigene Architektur', 'Eigene Domäne statt Engine; JavaFX nur in ui und infrastructure', 'Domänentests ohne JavaFX'],
            ['Reaktionsfähiger 2D-Kampf', 'Fester Schritt 1/120 s, eigene Physik mit Laufstegen', 'MovementTest, Kampagnensimulation'],
            ['Pixel-Look mit Licht', 'Framebuffer 480 × 270, Lichtkarte, WritableImage', 'Render-Probelauf ≈ 1,9 ms/Bild'],
            ['Wiederspielwert, Unterbruch', 'Seed-Generator, Meta-Fortschritt, Checkpoint', '600 Seeds, Migrationstests']], 28, row_pad=20)
        adrs = ['01 JavaFX + eigene Domäne', '02 Fester Simulationsschritt', '03 Checkpoint am Raumeingang', '04 Dateiformat 3 mit Migration', '05 Ereignisse für Rückmeldung',
                '06 Generator mit festen Wächtern', '07 Software-Pixel-Pipeline', '08 Prozedurale Pixel-Art', '09 Menüs als Bordsysteme', '10 Raumtechnik als Mitarbeiter']
        PX.draw(c.img, (90, 700), 'ARCHITEKTURENTSCHEIDUNGEN (ADR)', 3, TEAL)
        for i, a in enumerate(adrs):
            x, y = 90 + (i % 5) * 352, 750 + (i // 5) * 115
            c.panel(x, y, 332, 98, accent=TEAL if i < 8 else AMBER)
            c.text(x + 26, y + 18, 290, a, 24)
    s.append(Slide('M2 · Architektur', 'Einflussfaktoren und Entscheidungen', entscheidungen, [
        'Die vier wichtigsten Einflussfaktoren und wie die Architektur darauf antwortet, jeweils mit Beleg.',
        'Zehn Architekturentscheidungen sind in ARCHITEKTUR.md als ADR festgehalten. ADR-09 und ADR-10 sind in Version 1.1 neu bzw. geändert.'], 1.5))

    def design(c: Canvas):
        c.image(diagram('04-design-classes'), 90, 185, 1740, 520, white=True)
        c.d.text((1820, 672), 'Überblick – lesbare Fassung: docs/diagrams/04-design-classes.svg', font=font(20), fill=(90, 96, 100), anchor='ra')
        c.table(90, 735, [390, 720, 630], [
            ['Muster', 'Umsetzung', 'Nutzen'],
            ['Strategie', 'EnemyBehavior mit 17 Implementierungen', 'neue Gegner ohne Änderung an GameRun'],
            ['Aggregat', 'GameRun mit Mitarbeitern (Arsenal, Loot, Machinery …)', 'ein Tauchgang bleibt konsistent'],
            ['Beobachter (Pull)', 'GameEvent-Liste für Renderer und Audio', 'Effekte ohne Rückwirkung auf Regeln'],
            ['Port / Adapter', 'GameRepository / FileGameRepository', 'Speicher austauschbar']], 23, row_pad=9)
    s.append(Slide('M2 · Design', 'Design-Klassen und Muster', design, [
        'GameRun ist das Aggregat. Es delegiert an package-private Mitarbeiter wie PlayerMotor, Arsenal, Ballistics, Loot, Rewards und Machinery.',
        'Gegnerverhalten ist ein Strategie-Muster: 17 Verhaltensklassen, zugeordnet über eine Fabrikmethode in EnemyKind.',
        'Die Domäne erzeugt Ereignisse. Renderer und Audio lesen sie, ohne die Regeln zu beeinflussen.'], 1.5))

    def operationen(c: Canvas):
        c.table(90, 195, [640, 520, 580], [
            ['Systemoperation', 'Auslöser', 'Diagramm'],
            ['GameService.start(loadout, seed)', 'Tauchgang beginnen', '06 Start'],
            ['GameRun.update(dt, input)', 'jeder Simulationsschritt', '07 Kampf'],
            ['GameRun.take(offer)', 'Bergung wählen', '08 Bergung'],
            ['GameRun.chooseNextRoom(branch)', 'nächsten Raum betreten', '09 Raumwechsel'],
            ['GameService.resume()', 'Tauchgang fortsetzen', '10 Fortsetzen']], 25, row_pad=10)
        c.image(diagram('07-combat-sequence'), 90, 560, 900, 440, white=True)
        c.image(diagram('09-room-sequence'), 1030, 560, 800, 440, white=True)
    s.append(Slide('M2 · Design', 'Systemoperationen', operationen, [
        'Fünf Systemoperationen mit je einem Interaktionsdiagramm (verlangt sind mindestens vier).',
        'Links der Kampfschritt: GameRun aktualisiert Figur, Gegner, Geschosse und Anlagen und erzeugt Ereignisse.',
        'Rechts der Raumwechsel: Generator erzeugt den Raum deterministisch aus dem Seed, danach wird der Checkpoint gesetzt.'], 1.5))

    def pipeline(c: Canvas):
        c.image(diagram('13-render-pipeline'), 90, 185, 520, 815, white=True)
        PX.draw(c.img, (660, 200), 'SPIELSCHLEIFE', 3, AMBER)
        c.bullets(660, 250, 560, [
            'AnimationTimer liefert Bildtakt',
            'Simulation in festen Schritten 1/120 s',
            'Ereignisse an Renderer und Audio',
            'Zeitlupe und Trefferpause nur in der UI'], 27, spacing=10)
        PX.draw(c.img, (660, 600), 'RENDERING', 3, TEAL)
        c.bullets(660, 650, 560, [
            'Framebuffer 480 × 270 als int-Array',
            'Lichtkarte, Bloom, Nebel',
            'Upload als WritableImage, ganzzahlig skaliert'], 27, marker=TEAL, spacing=10)
        c.image(scene('condition-blackout'), 1260, 200, 570, 320, 'Stromausfall: nur Lichtkegel')
        c.image(scene('machine-console'), 1260, 600, 570, 320, 'Notschalter mit Leuchten')
    s.append(Slide('M3 · Implementation', 'Spielschleife und Render-Pipeline', pipeline, [
        'Die Simulation läuft in festen Schritten, das Zeichnen so oft wie der Bildschirm es erlaubt.',
        'Gezeichnet wird in einen eigenen Pixel-Puffer von 480 × 270 Punkten. Licht und Bloom rechnen wir selbst.',
        'Erst am Ende wird das Bild an JavaFX übergeben und scharf vergrössert.'], 1.5))

    def gegner(c: Canvas):
        c.image(diagram('11-enemy-state'), 90, 185, 620, 815, white=True)
        c.panel(760, 190, 1070, 470)
        c.sprites(ROOT / 'build' / 'art' / 'enemies.png', 5, [0, 1, 2, 3, 4, 5, 6], 800, 215, 990, 170, 3)
        c.sprites(ROOT / 'build' / 'art' / 'enemies.png', 5, [7, 8, 9, 10, 11, 12], 800, 440, 990, 190, 3)
        c.bullets(760, 700, 1070, [
            'Gemeinsame Zustandsmaschine in Brain: Annähern → **Ankündigen** → Angreifen → Erholen',
            'Die Ankündigung macht Niederlagen verständlich (Q-04)',
            '12 Gegnerarten und 4 Wächter mit je drei Phasen'], 28)
    s.append(Slide('M3 · Implementation', 'Gegner als Zustandsmaschine', gegner, [
        'Alle Gegner teilen eine Zustandsmaschine. Wichtig ist der Zustand „Ankündigen“: Er macht Angriffe lesbar.',
        'Das Verhalten pro Art steckt in einer Strategie-Klasse. So bleibt GameRun unverändert, wenn ein Gegner dazukommt.',
        'Die Figuren rechts sind nicht gezeichnet, sondern werden im Code Pixel für Pixel erzeugt.'], 1))

    def gui(c: Canvas):
        c.image(diagram('12-navigation'), 90, 185, 900, 440, white=True)
        c.image(screen('title'), 1030, 185, 800, 450)
        c.image(screen('loadout'), 90, 665, 590, 332)
        c.image(screen('archive-resonances'), 715, 665, 590, 332)
        c.panel(1340, 665, 490, 332, accent=AMBER)
        c.text(1370, 690, 440, '**Immediate-Mode-GUI** (ADR-09): Menüs werden jedes Bild neu im Framebuffer gezeichnet. Maus, Tastatur und Fokus laufen über dieselbe Klasse Gui.', 25)
    s.append(Slide('M3 · Implementation', 'Bordsystem-GUI und Navigation', gui, [
        'Links oben die Navigation zwischen allen Bildschirmen.',
        'Seit Version 1.1 sind die Menüs keine JavaFX-Controls mehr, sondern Teil des Spielbilds. Knöpfe, Schieber und Karten zeichnet die Klasse Gui.',
        'Vorteil: einheitlicher Pixel-Look und dieselbe Bedienung mit Maus und Tastatur.'], 1))

    def technik(c: Canvas):
        imgs = [(scene('machine-press'), 'Presse'), (scene('machine-laser'), 'Lasergitter'), (scene('machine-vent_pad'), 'Dampfdüse'),
                (scene('condition-breach'), 'Hüllenbruch'), (scene('condition-leak'), 'Leck'), (scene('condition-alarm'), 'Alarm')]
        for i, (p, cap) in enumerate(imgs):
            c.image(p, 90 + (i % 3) * 590, 190 + (i // 3) * 330, 560, 262, cap)
        c.panel(90, 860, 1740, 130, accent=TEAL)
        c.text(125, 885, 1680, '**ADR-10:** Raumtechnik ist ein Mitarbeiter des Aggregats (Machinery, Fixture). Anlagen treffen als Quelle MACHINE und durchdringen die Panzerung der Wächter.', 27)
    s.append(Slide('M3 · Implementation', 'Raumtechnik und Raumzustände', technik, [
        'Sechs Anlagentypen: Förderband, Dampfdüse, Lüfter, Presse, Laser, Notschalter.',
        'Fünf Raumzustände: Stromausfall, Alarm, Leck, Hüllenbruch, Schlagseite. Jeder mit eigener Regel und Belohnung.',
        'Anlagen sind fair: Sie treffen auch die Figur, kündigen sich aber an. In Bossarenen legen sie den Kern des Wächters frei.'], 1))

    def qualitaet(c: Canvas):
        levels = [('Unit', '106 JUnit-Tests: Bewegung, Kampf, Generator, Belohnungen, Raumtechnik', AMBER, 1740),
                  ('Integration', 'GameService mit Speicher, Dateiformat mit Migration', TEAL, 1500),
                  ('System', 'Testspieler spielt ganze Tauchgänge, alle Klassen, 30 Seeds', GREEN, 1260),
                  ('Oberfläche', 'echtes JavaFX-Fenster, 37 Schritte durch alle Bildschirme', VIOLET, 1020),
                  ('Darstellung', '30 733 Bilder im Dauertest, 0 Fehler, Ø 1,9 ms', RED, 780)]
        for i, (t, b, col, w) in enumerate(reversed(levels)):
            x = 90 + (1740 - w) // 2
            y = 195 + i * 135
            c.panel(x, y, w, 118, accent=col)
            PX.draw(c.img, (x + 34, y + 22), t.upper(), 3, col)
            c.text(x + 34, y + 62, w - 60, b, 25)
        c.text(90, 890, 1740, 'Werkzeuge: Gradle-Tasks test, checkJavaFormat, javadoc, uiSmoke, renderSoak, balance, sceneShot. **Offen:** Spieltests mit echten Personen (Q-04).', 27, MUTED)
    s.append(Slide('M3 · Qualität', 'Teststrategie', qualitaet, [
        'Viele schnelle Unit-Tests unten, wenige breite System- und Oberflächentests oben.',
        'Ein Testspieler (Bot) spielt ganze Tauchgänge. Damit prüfen wir Balance und Abstürze über viele Seeds.',
        'Was fehlt: Tests mit echten Menschen. Die kommen in Iteration 3.'], 1))

    def ki(c: Canvas):
        c.table(90, 195, [420, 820, 500], [
            ['Werkzeug', 'Verwendung', 'Übernahme'],
            ['OpenAI Codex (0.1–0.2)', 'erster Prototyp, Tests, Blender-Figuren, Audio', 'weitgehend, danach ersetzt'],
            ['Claude Code (1.0–1.1)', 'Domäne, Pixel-Pipeline, GUI, Tests, Doku', 'weitgehend übernommen'],
            ['OpenAI Imagegen', 'Konzeptbilder, frühe Hintergründe', 'nur Konzept und Archiv']], 29, row_pad=20)
        for i, (t, b) in enumerate([('KI_EINSATZ.md', 'Tabelle nach PM3-Vorlage je Version'), ('CREDITS.md', 'Herkunft aller Assets und Werkzeuge'), ('WORK_PLAN.md', 'Arbeitsjournal der autonomen Läufe')]):
            c.card(90 + i * 590, 815, 560, 175, t, b, [AMBER, TEAL, VIOLET][i], 28)
        c.panel(90, 540, 1740, 240, accent=AMBER)
        c.bullets(130, 565, 1660, [
            'Jeder Einsatz ist in **KI_EINSATZ.md** nach PM3-Vorgabe dokumentiert.',
            'Das Team übernimmt nur, was es **selbst erklären und begründen** kann: Paketverantwortung und Reviews.',
            'Keine fremden Assets: Grafik, Schrift und Klang sind selbst erzeugt, Quellen in CREDITS.md.',
            'Umfang des KI-Einsatzes wird mit den Dozierenden abgestimmt.'], 27, spacing=10)
    s.append(Slide('Nachvollziehbarkeit', 'KI-Einsatz', ki, [
        'Offen und vollständig: Code und Dokumente sind stark KI-unterstützt entstanden.',
        'Die Tabelle folgt der PM3-Vorgabe mit Ziel, Aufwand, Verwendung und Art der Übernahme.',
        'Wichtig für die Bewertung: Das Team muss jede Klasse erklären können. Dafür gibt es die Paketverantwortung.'], 1))

    def ausblick(c: Canvas):
        c.panel(90, 195, 850, 790, accent=GREEN)
        PX.draw(c.img, (130, 225), 'ERREICHT (1.1)', 3, GREEN)
        c.bullets(130, 285, 780, ['Spielbar von der Schleuse bis zur Brücke', '24 Räume, 4 Wächter, 12 Gegnerarten', '41 Module, 8 Fähigkeiten, 5 Klassen', '33 Raumthemen, 5 Raumzustände, 6 Anlagen', 'Bordsystem-GUI mit Maus und Tastatur', '26 Erfolge, Archiv, Garderobe, Logbuch', '106 Tests, Mac-App, Javadoc'], 32, marker=GREEN, spacing=34)
        c.panel(980, 195, 850, 790, accent=AMBER)
        PX.draw(c.img, (1020, 225), 'OFFEN / NÄCHSTE SCHRITTE', 3, AMBER)
        c.bullets(1020, 285, 780, ['Spieltests mit Personen (Q-04)', 'Windows und Linux prüfen', 'Code-Verständnis im Team, Reviews', 'weitere Inhalte nach Davids Liste', 'Gamepad nur mit Freigabe', 'Berichte in die SoE-Vorlage übertragen'], 32, spacing=34)
    s.append(Slide('M3 · Resultate', 'Stand und Ausblick', ausblick, [
        'Der Prototyp ist spielbar von der Schleuse bis zur Brücke.',
        'Offen sind vor allem Spieltests mit Menschen und die Prüfung auf anderen Betriebssystemen.',
        'Das Spiel wird weiter ausgebaut. Neue Inhalte folgen der bestehenden Architektur (Strategie für Gegner, Enums für Inhalte).'], 1))

    def demo(c: Canvas):
        c.backdrop(scene('ending-2'), .1)
        c.veil(470, 260)
        PX.draw(c.img, (W // 2, 110), 'DEMO', 14, BONE, anchor='c', glow=AMBER)
        c.d.text((W // 2, 300), 'Live: Schleuse → Raum → Bergung → Route → Wächter', font=font(42, True), fill=BONE, anchor='ma')
        c.d.text((W // 2, 370), 'Danach Fragen · Code und Dokumentation im Repository', font=font(30), fill=MUTED, anchor='ma')
    s.append(Slide('', 'Demo und Fragen', demo, [
        'Live-Demo, falls Zeit bleibt, sonst Gameplay-Demo.mp4.',
        'Fragen beantworten.'], 1.5, plain=True))
    return s


if __name__ == '__main__':
    render('M1 Projektskizze', 'ABYSS_M1_Projektskizze', m1_slides(),
           'Pitch für die Geschäftsleitung nach dem PM3-Auftrag „Projektskizze (M1)“: etwa 10 Minuten plus 5 Minuten Fragen. Inhalt und Reihenfolge folgen den neun Kapiteln der Projektskizze.')
    render('Aufbau und Architektur', 'ABYSS_Aufbau_und_Architektur', arch_slides(),
           'Präsentation zu Aufbau und Architektur für M2 (Lösungsarchitektur) und M3 (Prototyp). Inhalt aus TECHNISCHER_BERICHT_I.md und TECHNISCHER_BERICHT_II.md.')
