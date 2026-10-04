package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.render.SpriteBank;
import ch.zhaw.abyss.ui.render.WorldRenderer;

/**
 * Lastprobe für Schwärme: Der Testspieler spielt auf Druckstufe 5 über mehrere Zyklen, gemessen
 * werden Simulations- und Zeichenzeit je Bild, gruppiert nach der Zahl lebender Gegner.
 */
public final class SwarmBench {
    private SwarmBench() {}

    /**
     * @param args Seed, Zyklen
     */
    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 4242;
        int cycles = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int pressure = args.length > 2 ? Integer.parseInt(args[2]) : 5;
        var base = RunSetup.standard(seed, DiverClass.values()[0]);
        var run =
                new GameRun(
                        new RunSetup(
                                seed,
                                base.diver(),
                                base.weapon(),
                                base.module(),
                                false,
                                pressure,
                                base.itemPool(),
                                base.weaponPool(),
                                0,
                                0,
                                0));
        var renderer = new WorldRenderer(PixelFont.load(), new SpriteBank());
        var settings = Settings.DEFAULT;
        int[] limits = {50, 100, 200, 300, 600, 1000, Integer.MAX_VALUE};
        double[] sum = new double[limits.length], worst = new double[limits.length];
        double[] sim = new double[limits.length];
        int[] frames = new int[limits.length];
        int peak = 0;
        int lastRoom = -1;
        double roomStart = 0;
        for (int frame = 0; frame < 60 * 60 * 90; frame++) {
            if (run.phase() == GameRun.Phase.DEFEAT) break;
            if (run.phase() == GameRun.Phase.VICTORY) {
                if (run.cycle() + 1 >= cycles) break;
                run.nextCycle();
            }
            long t0 = System.nanoTime();
            for (int s = 0; s < 2; s++) run.update(1.0 / 120, CampaignPilot.input(run));
            long t1 = System.nanoTime();
            for (var event : run.drainEvents()) renderer.event(event, run, settings);
            renderer.update(1.0 / 60, run, settings);
            renderer.render(run, settings, true);
            long t2 = System.nanoTime();
            if (run.phase() == GameRun.Phase.ROOM_CLEARED) {
                int room = run.globalDepth();
                if (room != lastRoom) {
                    lastRoom = room;
                    System.out.printf(
                            "room %3d esc %2d %-8s %-12s %5.0fs kills %6d level %3d hp"
                                    + " %4.0f/%4.0f%n",
                            room,
                            run.escalation(),
                            run.room().kind(),
                            run.room().threat(),
                            run.elapsed() - roomStart,
                            run.kills(),
                            run.player().level(),
                            run.player().health(),
                            run.player().maxHealth());
                    roomStart = run.elapsed();
                }
                CampaignPilot.advance(run, 1);
            }
            int alive = (int) run.enemies().stream().filter(e -> e.alive()).count();
            peak = Math.max(peak, alive);
            int bucket = 0;
            while (alive >= limits[bucket]) bucket++;
            if (frame > 600) {
                double ms = (t2 - t1) / 1e6;
                sum[bucket] += ms;
                sim[bucket] += (t1 - t0) / 1e6;
                worst[bucket] = Math.max(worst[bucket], ms);
                frames[bucket]++;
            }
        }
        String[] names = {"0-49", "50-99", "100-199", "200-299", "300-599", "600-999", "1000+"};
        for (int i = 0; i < limits.length; i++)
            if (frames[i] > 0)
                System.out.printf(
                        "alive %-8s frames %6d  render avg %.2f ms  worst %.1f ms  sim avg %.3f"
                                + " ms%n",
                        names[i], frames[i], sum[i] / frames[i], worst[i], sim[i] / frames[i]);
        System.out.printf(
                "RESULT %s cycle %d depth %d level %d peak %d kills %d items %s%n",
                run.phase(),
                run.cycle(),
                run.room().depth(),
                run.player().level(),
                peak,
                run.kills(),
                run.player().items());
    }
}
