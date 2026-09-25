package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.DiverArt;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Exportiert die prozedural gemalten Pixelgrafiken als vergrösserte Übersichtsbögen. Dient der
 * visuellen Prüfung der Asset-Erzeugung ohne laufendes Spiel.
 */
public final class ArtSheet {
    private ArtSheet() {}

    /**
     * @param args Zielverzeichnis, Vorgabe {@code build/art}
     * @throws IOException bei Schreibfehlern
     */
    public static void main(String[] args) throws IOException {
        var out = Path.of(args.length > 0 ? args[0] : "build/art");
        Files.createDirectories(out);
        var rows = new ArrayList<List<Sprite>>();
        var look = Cosmetics.DEFAULT;
        var anims = DiverArt.build(look, Weapon.WRENCH, DiverClass.MECHANIC);
        for (var entry : anims.entrySet()) rows.add(entry.getValue());
        for (var weapon : Weapon.values()) {
            var set = DiverArt.build(look, weapon, DiverClass.MECHANIC);
            var row = new ArrayList<Sprite>();
            row.addAll(set.get(DiverArt.Anim.IDLE).subList(0, 1));
            row.addAll(set.get(DiverArt.of(weapon.combo().getFirst().style())));
            row.addAll(set.get(DiverArt.of(weapon.air().style())));
            rows.add(row);
        }
        var variants = new ArrayList<Sprite>();
        for (int helmet = 0; helmet < Cosmetics.HELMETS; helmet++)
            for (int suit = 0; suit < Cosmetics.SUITS; suit += 2)
                variants.add(
                        DiverArt.build(
                                        new Cosmetics(
                                                suit + helmet % 2,
                                                helmet,
                                                (suit + helmet) % 6,
                                                helmet),
                                        Weapon.WRENCH,
                                        helmet == 2 ? DiverClass.TITAN : DiverClass.MECHANIC)
                                .get(DiverArt.Anim.IDLE)
                                .getFirst());
        rows.add(variants);
        write(rows, out.resolve("diver.png"), 6);
        var enemies = new ArrayList<List<Sprite>>();
        var bosses = new ArrayList<List<Sprite>>();
        for (var kind : ch.zhaw.abyss.domain.EnemyKind.values()) {
            var set = ch.zhaw.abyss.ui.art.EnemyArt.build(kind);
            var row = new ArrayList<Sprite>();
            for (var anim : ch.zhaw.abyss.ui.art.EnemyArt.Anim.values()) row.addAll(set.get(anim));
            (kind.boss() ? bosses : enemies).add(row);
        }
        write(enemies, out.resolve("enemies.png"), 5);
        write(bosses, out.resolve("bosses.png"), 3);
    }

    /**
     * Schreibt Zeilen von Sprites als Bogen.
     *
     * @param rows Zeilen
     * @param file Ziel
     * @param scale Vergrösserung
     * @throws IOException bei Schreibfehlern
     */
    static void write(List<List<Sprite>> rows, Path file, int scale) throws IOException {
        int cell = 0, columns = 0;
        for (var row : rows) {
            columns = Math.max(columns, row.size());
            for (var s : row) cell = Math.max(cell, Math.max(s.width(), s.height()));
        }
        cell += 4;
        var image =
                new BufferedImage(
                        columns * cell * scale,
                        rows.size() * cell * scale,
                        BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < rows.size(); r++)
            for (int c = 0; c < rows.get(r).size(); c++) {
                var s = rows.get(r).get(c);
                int ox = c * cell + 2, oy = r * cell + 2;
                for (int y = 0; y < cell - 4; y++)
                    for (int x = 0; x < cell - 4; x++) {
                        int bg = ((x / 4 + y / 4) % 2 == 0) ? 0xFF2A3440 : 0xFF222A34;
                        int color = s.get(x, y);
                        if (color >>> 24 == 0) color = bg;
                        var glow = s.glow();
                        if (glow != null && glow.get(x, y) >>> 24 != 0) color = glow.get(x, y);
                        for (int sy = 0; sy < scale; sy++)
                            for (int sx = 0; sx < scale; sx++)
                                image.setRGB((ox + x) * scale + sx, (oy + y) * scale + sy, color);
                    }
            }
        ImageIO.write(image, "png", file.toFile());
        System.out.println("ART " + file);
    }
}
