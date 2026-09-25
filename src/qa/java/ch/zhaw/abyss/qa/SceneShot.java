package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Fixture;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomGenerator;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.render.SpriteBank;
import ch.zhaw.abyss.ui.render.WorldRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * Rendert echte Spielszenen ohne Fenster: Der Testspieler spielt, der Pixel-Renderer zeichnet, das
 * Bild wird als PNG gespeichert. Dient der visuellen Prüfung von Räumen, Licht und Effekten.
 */
public final class SceneShot {
    private SceneShot() {}

    /**
     * @param args Zielverzeichnis, Raumtiefen (kommagetrennt), Sekunden pro Raum; zusätzlich
     *     entstehen Titel- und Kartenbild
     * @throws IOException bei Schreibfehlern
     */
    public static void main(String[] args) throws IOException {
        var out = Path.of(args.length > 0 ? args[0] : "build/scenes");
        Files.createDirectories(out);
        String[] depths = (args.length > 1 ? args[1] : "0,6,12,18").split(",");
        double seconds = args.length > 2 ? Double.parseDouble(args[2]) : 4;
        var font = PixelFont.load();
        var bank = new SpriteBank();
        var settings = Settings.DEFAULT;
        var menu = new WorldRenderer(font, bank);
        for (int frame = 0; frame < 90; frame++) menu.update(1.0 / 60, null, settings);
        menu.renderTitle(settings, true);
        save(menu, out.resolve("title.png"));
        var start = new GameRun(RunSetup.standard(7919, DiverClass.values()[0]));
        menu.renderMap(start);
        save(menu, out.resolve("map.png"));
        for (double t : new double[] {2, 5, 8}) {
            menu.renderEnding(t, settings);
            save(menu, out.resolve(String.format("ending-%.0f.png", t)));
        }
        for (var condition : RoomCondition.values()) {
            if (condition == RoomCondition.NONE) continue;
            long seed = findSeed(condition);
            var run = new GameRun(RunSetup.standard(seed, DiverClass.values()[0]));
            int target = conditionDepth(seed, condition);
            var renderer = new WorldRenderer(font, bank);
            for (int guard = 0; run.room().depth() < target && guard < 120 * 60 * 20; guard++) {
                run.update(1.0 / 120, CampaignPilot.input(run));
                run.drainEvents();
                if (run.phase() == GameRun.Phase.ROOM_CLEARED) CampaignPilot.advance(run, 0);
                if (run.phase() == GameRun.Phase.DEFEAT) break;
            }
            for (int frame = 0; frame < seconds * 60; frame++) {
                for (int s = 0; s < 2; s++) run.update(1.0 / 120, CampaignPilot.input(run));
                for (var event : run.drainEvents()) renderer.event(event, run, settings);
                renderer.update(1.0 / 60, run, settings);
            }
            renderer.render(run, settings, true);
            save(renderer, out.resolve("condition-" + condition.name().toLowerCase() + ".png"));
        }
        for (var machine : Fixture.Kind.values()) {
            long seed = 1;
            int target = -1;
            for (; seed < 500 && target < 0; seed++) {
                var generator = new RoomGenerator(seed, 0);
                for (int depth = 1; depth < 22 && target < 0; depth++)
                    if (generator.room(depth, 0).fixtures().stream()
                            .anyMatch(f -> f.kind() == machine)) target = depth;
            }
            var run = new GameRun(RunSetup.standard(seed - 1, DiverClass.values()[0]));
            var renderer = new WorldRenderer(font, bank);
            for (int guard = 0; run.room().depth() < target && guard < 120 * 60 * 20; guard++) {
                run.update(1.0 / 120, CampaignPilot.input(run));
                run.drainEvents();
                if (run.phase() == GameRun.Phase.ROOM_CLEARED) CampaignPilot.advance(run, 0);
                if (run.phase() == GameRun.Phase.DEFEAT) break;
            }
            double focus =
                    run.fixtures().stream()
                            .filter(f -> f.kind() == machine)
                            .mapToDouble(Fixture::x)
                            .findFirst()
                            .orElse(0);
            boolean shot = false;
            for (int frame = 0; frame < 60 * 30 && !shot; frame++) {
                for (int s = 0; s < 2; s++) run.update(1.0 / 120, CampaignPilot.input(run));
                for (var event : run.drainEvents()) renderer.event(event, run, settings);
                renderer.update(1.0 / 60, run, settings);
                boolean visible = Math.abs(run.player().x() - focus) < 220;
                boolean lively =
                        run.fixtures().stream()
                                .anyMatch(f -> f.kind() == machine && (f.active() || f.warning()));
                if (frame > 60 && visible && (lively || frame > 900)) shot = true;
            }
            renderer.render(run, settings, true);
            save(renderer, out.resolve("machine-" + machine.name().toLowerCase() + ".png"));
        }
        for (String d : depths) {
            int target = Integer.parseInt(d.trim());
            var diver = DiverClass.values()[target % DiverClass.values().length];
            var run = new GameRun(RunSetup.standard(7919 + target, diver));
            var renderer = new WorldRenderer(font, bank);
            // Bis zur Zieltiefe vorspulen, ohne zu zeichnen.
            int guard = 0;
            while (run.room().depth() < target && guard++ < 120 * 60 * 20) {
                run.update(1.0 / 120, CampaignPilot.input(run));
                run.drainEvents();
                if (run.phase() == GameRun.Phase.ROOM_CLEARED)
                    CampaignPilot.advance(run, run.room().depth() % 2);
                if (run.phase() == GameRun.Phase.DEFEAT) break;
            }
            for (int frame = 0; frame < seconds * 60; frame++) {
                for (int s = 0; s < 2; s++) run.update(1.0 / 120, CampaignPilot.input(run));
                for (var event : run.drainEvents()) renderer.event(event, run, settings);
                renderer.update(1.0 / 60, run, settings);
            }
            renderer.render(run, settings, true);
            save(renderer, out.resolve(String.format("scene-%02d.png", target)));
        }
    }

    private static long findSeed(RoomCondition condition) {
        for (long seed = 1; ; seed++) if (conditionDepth(seed, condition) >= 0) return seed;
    }

    private static int conditionDepth(long seed, RoomCondition condition) {
        var generator = new RoomGenerator(seed, 0);
        for (int depth = 2; depth < 12; depth++)
            if (generator.room(depth, 0).condition() == condition) return depth;
        return -1;
    }

    static void save(WorldRenderer renderer, Path file) throws IOException {
        var f = renderer.frame();
        int scale = 3;
        var image =
                new BufferedImage(
                        f.width() * scale, f.height() * scale, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < f.height(); y++)
            for (int x = 0; x < f.width(); x++) {
                int c = f.pixels()[y * f.width() + x];
                for (int sy = 0; sy < scale; sy++)
                    for (int sx = 0; sx < scale; sx++)
                        image.setRGB(x * scale + sx, y * scale + sy, c);
            }
        ImageIO.write(image, "png", file.toFile());
        System.out.println("SCENE " + file);
    }
}
