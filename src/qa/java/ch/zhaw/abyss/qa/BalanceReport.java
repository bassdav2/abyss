package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunSetup;

import java.util.Map;
import java.util.TreeMap;

/**
 * Balancebericht: Der Testspieler absolviert viele Tauchgänge je Klasse. Gemeldet werden Siegquote,
 * durchschnittliche Tiefe, Spielzeit und Todesorte. Bot-Zeiten sind keine menschlichen Spielzeiten.
 */
public final class BalanceReport {
    private BalanceReport() {}

    /**
     * Führt einen einzelnen Tauchgang mit dem Testspieler aus.
     *
     * @param setup Startbedingungen
     * @param maxSeconds Obergrenze der simulierten Zeit
     * @return beendeter oder abgebrochener Tauchgang
     */
    public static GameRun play(RunSetup setup, double maxSeconds) {
        var run = new GameRun(setup);
        int ticks = 0, limit = (int) (maxSeconds * 120);
        while (run.phase() != GameRun.Phase.DEFEAT
                && run.phase() != GameRun.Phase.VICTORY
                && ticks++ < limit) {
            if (run.phase() == GameRun.Phase.ROOM_CLEARED)
                CampaignPilot.advance(run, (run.room().depth() / 2) % 2);
            else run.update(1.0 / 120, CampaignPilot.input(run));
            run.drainEvents();
        }
        return run;
    }

    /**
     * @param args Seeds pro Klasse, Vorgabe 12
     */
    public static void main(String[] args) {
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 12;
        for (var diver : DiverClass.values()) {
            int wins = 0, stalls = 0;
            double depth = 0, time = 0;
            Map<String, Integer> deaths = new TreeMap<>();
            for (int seed = 1; seed <= seeds; seed++) {
                var run = play(RunSetup.standard(seed * 104_729L, diver), 3600);
                if (run.phase() == GameRun.Phase.VICTORY) wins++;
                else if (run.phase() == GameRun.Phase.DEFEAT)
                    deaths.merge(
                            String.format("%02d %s", run.room().depth(), run.room().kind()),
                            1,
                            Integer::sum);
                else stalls++;
                depth += run.room().depth() + 1;
                time += run.elapsed();
            }
            System.out.printf(
                    "%-10s wins %2d/%d  stalls %d  depth %.1f  time %.0fs  deaths %s%n",
                    diver, wins, seeds, stalls, depth / seeds, time / seeds, deaths);
        }
    }
}
