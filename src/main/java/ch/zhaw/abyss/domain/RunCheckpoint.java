package ch.zhaw.abyss.domain;

import java.util.List;
import java.util.Map;

/** Gespeichert wird ein Raum-Einstieg, nicht eine beliebige Kampfmomentaufnahme. */
public record RunCheckpoint(
        long seed,
        int cycle,
        int depth,
        int branch,
        ActiveModule module,
        boolean explorer,
        double health,
        double energy,
        int salvage,
        Map<Upgrade, Integer> upgrades,
        int kills,
        double elapsed,
        List<Integer> route) {
    public RunCheckpoint {
        upgrades = Map.copyOf(upgrades);
        route = List.copyOf(route);
    }
}
