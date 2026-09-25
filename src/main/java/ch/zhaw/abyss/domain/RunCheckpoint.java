package ch.zhaw.abyss.domain;

import java.util.List;
import java.util.Map;

/**
 * Gespeichert wird ein Raum-Einstieg, nicht eine beliebige Kampfmomentaufnahme. Alle Werte gelten
 * unmittelbar beim Betreten des Raums, vor dessen Belohnungen.
 *
 * @param seed Routen-Seed
 * @param cycle Zyklus
 * @param pressure Druckstufe
 * @param depth Raumposition
 * @param branch gewählter Abzweig
 * @param diver Klasse
 * @param weapon Waffe
 * @param weaponLevel Werkstattstufe der Waffe
 * @param module aktives Modul
 * @param explorer Entdeckermodus
 * @param health Integrität
 * @param energy Energie
 * @param salvage Schrott
 * @param repairKits Reparatursets
 * @param items Module mit Stufen
 * @param kills Abschüsse
 * @param elapsed Tauchzeit in Sekunden
 * @param route gewählte Abzweige bis einschliesslich dieses Raums
 * @param cores im Run gesammelte Datenkerne
 * @param reviveUsed Notfallkapsel bereits verbraucht
 * @param rushStacks Stufen des Tiefenrauschs
 * @param healthPenalty dauerhafte Integritätsopfer aus Druckkapellen
 * @param bonusHealth dauerhafte Bonusintegrität aus dem Archiv
 */
public record RunCheckpoint(
        long seed,
        int cycle,
        int pressure,
        int depth,
        int branch,
        DiverClass diver,
        Weapon weapon,
        int weaponLevel,
        ActiveModule module,
        boolean explorer,
        double health,
        double energy,
        int salvage,
        int repairKits,
        Map<Item, Integer> items,
        int kills,
        double elapsed,
        List<Integer> route,
        int cores,
        boolean reviveUsed,
        int rushStacks,
        double healthPenalty,
        double bonusHealth) {
    /** Kopiert Sammlungen unveränderlich. */
    public RunCheckpoint {
        items = Map.copyOf(items);
        route = List.copyOf(route);
    }
}
