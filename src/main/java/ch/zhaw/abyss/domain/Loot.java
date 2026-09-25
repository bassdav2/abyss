package ch.zhaw.abyss.domain;

import java.util.List;

/**
 * Beute des Raums: Schrott, Heilung, Energie und Datenkerne fallen, springen und werden vom
 * Sammelradius angezogen; Kisten zerbrechen in Beute oder explodieren. Jeder Wert wird genau einmal
 * gutgeschrieben.
 */
final class Loot {
    private final GameRun run;

    Loot(GameRun run) {
        this.run = run;
    }

    /**
     * Bewegt liegende Beute und sammelt sie in Reichweite ein.
     *
     * @param dt Sekunden
     */
    void update(double dt) {
        var p = run.player;
        boolean cleared = run.phase() == GameRun.Phase.ROOM_CLEARED;
        if (cleared) run.clearTime += dt;
        double magnet = cleared && run.clearTime > .9 ? 1e9 : p.stats.magnetRadius();
        var layout = run.layout();
        for (var pickup : run.pickups) {
            pickup.age += dt;
            double tx = p.x, ty = p.centerY();
            double distance = Math.hypot(tx - pickup.x, ty - pickup.y);
            if (pickup.age > .45 && distance < magnet && p.alive()) {
                double pull = 520 + pickup.age * 900;
                pickup.vx = (tx - pickup.x) / Math.max(1, distance) * pull;
                pickup.vy = (ty - pickup.y) / Math.max(1, distance) * pull;
                pickup.x += pickup.vx * dt;
                pickup.y += pickup.vy * dt;
            } else {
                pickup.vy += 1700 * dt;
                pickup.x = GameRun.clamp(pickup.x + pickup.vx * dt, 60, layout.width() - 60);
                pickup.y += pickup.vy * dt;
                double surface = layout.surfaceBelow(pickup.x, pickup.y - 12 - pickup.vy * dt);
                if (pickup.y >= surface && pickup.vy > 0) {
                    pickup.y = surface;
                    pickup.vy = Math.abs(pickup.vy) > 140 ? -pickup.vy * .38 : 0;
                    pickup.vx *= .6;
                }
            }
            if (distance < 46 && pickup.age > .3 && p.alive()) collect(pickup);
        }
        run.pickups.removeIf(pickup -> pickup.collected);
    }

    /** Schreibt alle liegende Beute sofort gut, etwa beim Raumwechsel oder Sieg. */
    void collectAll() {
        for (var pickup : List.copyOf(run.pickups)) collect(pickup);
        run.pickups.clear();
    }

    private void collect(Pickup pickup) {
        if (pickup.collected) return;
        var p = run.player;
        pickup.collected = true;
        switch (pickup.kind) {
            case SCRAP -> p.addSalvage(pickup.value);
            case HEALTH -> p.heal(pickup.value);
            case ENERGY -> p.addEnergy(pickup.value);
            case CORE -> p.cores += (int) pickup.value;
        }
        run.emit(
                new GameEvent(
                        pickup.kind == Pickup.Kind.CORE
                                ? GameEvent.Type.CORE
                                : GameEvent.Type.PICKUP,
                        pickup.x,
                        pickup.y,
                        pickup.value,
                        pickup.kind.name()));
    }

    /**
     * Verteilt Schrott auf mehrere Beutestücke.
     *
     * @param x Ursprung
     * @param y Ursprung
     * @param value Gesamtwert
     */
    void dropScrap(double x, double y, int value) {
        int orbs = Math.max(1, Math.min(6, value / 3));
        int remaining = value;
        for (int i = 0; i < orbs; i++) {
            int amount = i == orbs - 1 ? remaining : value / orbs;
            remaining -= amount;
            if (amount > 0) drop(Pickup.Kind.SCRAP, amount, x, y);
        }
    }

    /**
     * Lässt ein Beutestück mit zufälligem Schwung fallen.
     *
     * @param kind Art
     * @param value Wert
     * @param x Ursprung
     * @param y Ursprung
     */
    void drop(Pickup.Kind kind, double value, double x, double y) {
        double angle = -Math.PI / 2 + (run.rng.nextDouble() - .5) * 1.8;
        double speed = 260 + run.rng.nextDouble() * 280;
        run.pickups.add(
                new Pickup(
                        run.nextId(),
                        kind,
                        value,
                        x,
                        Math.min(y, GameRun.FLOOR - 10),
                        Math.cos(angle) * speed,
                        Math.sin(angle) * speed));
    }

    /**
     * Öffnet eine Kiste: Beute fällt heraus oder Sprengstoff explodiert.
     *
     * @param crate zerstörte Kiste
     */
    void breakCrate(SupplyCrate crate) {
        double x = crate.x(), y = crate.y() - 30;
        run.emit(new GameEvent(GameEvent.Type.CRATE_BREAK, x, y, crate.kind().ordinal(), ""));
        switch (crate.kind()) {
            case REPAIR -> drop(Pickup.Kind.HEALTH, 18, x, y);
            case ENERGY -> drop(Pickup.Kind.ENERGY, 30, x, y);
            case SALVAGE -> dropScrap(x, y, 9);
            case EXPLOSIVE -> {
                run.combat.explode(x, y, 165, 40, false, true, Status.BURN);
                var p = run.player;
                if (Math.hypot(p.x - x, p.centerY() - y) < 150)
                    run.combat.hurtPlayer(10, x, null, false);
            }
        }
    }
}
