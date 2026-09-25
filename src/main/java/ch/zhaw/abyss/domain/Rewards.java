package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Belohnungen eines gesicherten Raums: Bergungsangebote nach Seltenheit, Schwarzmarkt, Werkstatt,
 * Versorgung und Druckkapelle. Prüft Preise, Stufengrenzen und dass jede Wahl genau einmal
 * abgerechnet wird.
 */
final class Rewards {
    private final GameRun run;
    boolean available, repaired, dealTaken;
    List<Offer> offers = List.of();
    List<ShrineDeal> deals = List.of();

    Rewards(GameRun run) {
        this.run = run;
    }

    /** Setzt alles für einen neuen Raum zurück. */
    void reset() {
        available = false;
        repaired = false;
        dealTaken = false;
        offers = List.of();
        deals = List.of();
    }

    /**
     * Öffnet die Belohnung passend zur Raumart.
     *
     * @param room gesicherter Raum
     */
    void open(RoomPlan room) {
        available = true;
        switch (room.kind()) {
            case COMBAT ->
                    offers =
                            rewardOffers(
                                    room.condition() == RoomCondition.ALARM
                                                    || room.condition() == RoomCondition.BREACH
                                            ? Rarity.RARE
                                            : Rarity.COMMON);
            case ELITE -> offers = rewardOffers(Rarity.RARE);
            case BOSS -> {
                offers = rewardOffers(Rarity.LEGENDARY);
                run.player.heal(run.player.maxHealth * .3);
            }
            case CACHE -> offers = List.of(Offer.service(Offer.Type.SUPPLIES, 0));
            case MERCHANT -> offers = merchantOffers(room);
            case WORKSHOP -> offers = workshopOffers();
            case SHRINE -> deals = shrineDeals();
            default -> {}
        }
    }

    private List<Offer> rewardOffers(Rarity minimum) {
        var player = run.player;
        int count = 3 + player.stacks(Item.COMPASS);
        var result = new ArrayList<Offer>();
        for (var item : rollItems(count, minimum)) result.add(Offer.item(item, 0));
        if (minimum == Rarity.COMMON && !result.isEmpty() && run.rng.nextInt(100) < 22) {
            var weapons =
                    run.setup.weaponPool().stream()
                            .filter(w -> w != player.weapon)
                            .sorted()
                            .toList();
            if (!weapons.isEmpty())
                result.set(
                        result.size() - 1,
                        Offer.weapon(weapons.get(run.rng.nextInt(weapons.size())), 0));
        }
        return List.copyOf(result);
    }

    private List<Offer> merchantOffers(RoomPlan room) {
        var result = new ArrayList<Offer>();
        double scale = 1 + .1 * room.sector() + .1 * run.setup.pressure();
        for (var item : rollItems(4, Rarity.COMMON))
            result.add(Offer.item(item, (int) Math.round(item.rarity().price() * scale)));
        result.add(Offer.service(Offer.Type.REPAIR_KIT, 22));
        result.add(Offer.service(Offer.Type.HEAL, 18));
        return List.copyOf(result);
    }

    private double discount() {
        return run.player.diver == DiverClass.MECHANIC ? .75 : 1;
    }

    private List<Offer> workshopOffers() {
        var result = new ArrayList<Offer>();
        for (var item : rollItems(2, Rarity.COMMON))
            result.add(Offer.item(item, (int) Math.round(20 * discount())));
        result.add(Offer.service(Offer.Type.REPAIR_KIT, (int) Math.round(20 * discount())));
        upgradeOffer().ifPresent(result::add);
        return List.copyOf(result);
    }

    private java.util.Optional<Offer> upgradeOffer() {
        int level = run.player.weaponLevel;
        if (level >= Weapon.MAX_LEVEL) return java.util.Optional.empty();
        return java.util.Optional.of(
                Offer.service(
                        Offer.Type.WEAPON_UPGRADE,
                        (int) Math.round(Weapon.upgradePrice(level) * discount())));
    }

    private List<ShrineDeal> shrineDeals() {
        var player = run.player;
        var pool = new ArrayList<ShrineDeal>();
        for (var deal : ShrineDeal.values()) {
            boolean owned =
                    switch (deal) {
                        case GLASS -> player.stacks(Item.GLASS_HULL) > 0;
                        case GREED -> player.stacks(Item.GREED) > 0;
                        case FEVER -> player.stacks(Item.FEVER) > 0;
                        default -> false;
                    };
            if (!owned) pool.add(deal);
        }
        Collections.shuffle(pool, run.rng);
        return List.copyOf(pool.subList(0, Math.min(3, pool.size())));
    }

    /**
     * Würfelt verschiedene, noch nicht maximierte Module nach Seltenheitsgewicht.
     *
     * @param count gewünschte Anzahl
     * @param minimum Mindestseltenheit; bei {@link Rarity#LEGENDARY} ist nur das erste legendär
     * @return höchstens {@code count} verschiedene Module
     */
    List<Item> rollItems(int count, Rarity minimum) {
        var player = run.player;
        var candidates = new ArrayList<Item>();
        for (var item : Item.lootable())
            if (run.setup.itemPool().contains(item) && player.stacks(item) < item.maxStacks())
                candidates.add(item);
        var result = new ArrayList<Item>();
        for (int i = 0; i < count && !candidates.isEmpty(); i++) {
            Rarity floor =
                    minimum == Rarity.LEGENDARY
                            ? (i == 0 ? Rarity.LEGENDARY : Rarity.RARE)
                            : minimum;
            List<Item> eligible = List.of();
            for (int level = floor.ordinal(); level >= 0 && eligible.isEmpty(); level--) {
                int threshold = level;
                eligible =
                        candidates.stream().filter(c -> c.rarity().ordinal() >= threshold).toList();
            }
            int total = eligible.stream().mapToInt(c -> weight(c, minimum)).sum();
            int roll = run.rng.nextInt(Math.max(1, total));
            Item chosen = eligible.getLast();
            for (var candidate : eligible) {
                roll -= weight(candidate, minimum);
                if (roll < 0) {
                    chosen = candidate;
                    break;
                }
            }
            result.add(chosen);
            candidates.remove(chosen);
        }
        return result;
    }

    private int weight(Item item, Rarity minimum) {
        int base = item.rarity().weight();
        if (minimum != Rarity.COMMON && item.rarity() == Rarity.LEGENDARY) base *= 3;
        int sector = run.room().sector();
        return Math.max(1, base + (sector * (item.rarity() == Rarity.COMMON ? -8 : 4)));
    }

    /**
     * Nimmt ein Angebot an.
     *
     * @param offer Angebot
     * @return {@code true}, wenn angenommen und abgerechnet
     */
    boolean take(Offer offer) {
        if (run.phase() != GameRun.Phase.ROOM_CLEARED
                || !available && !shop()
                || !offers.contains(offer)) return false;
        var p = run.player;
        if (p.salvage < offer.price() || !apply(p, offer)) return false;
        p.salvage -= offer.price();
        if (offer.price() > 0)
            run.emit(
                    new GameEvent(GameEvent.Type.PURCHASE, p.x, p.y, offer.price(), offer.title()));
        if (shop()) {
            var remaining = new ArrayList<>(offers);
            if (offer.type() != Offer.Type.REPAIR_KIT && offer.type() != Offer.Type.HEAL)
                remaining.remove(offer);
            if (offer.type() == Offer.Type.WEAPON_UPGRADE) upgradeOffer().ifPresent(remaining::add);
            offers = List.copyOf(remaining);
        } else {
            available = false;
            offers = List.of();
        }
        return true;
    }

    private boolean apply(Player p, Offer offer) {
        switch (offer.type()) {
            case ITEM -> {
                if (p.stacks(offer.item()) >= offer.item().maxStacks()) return false;
                p.install(offer.item());
                run.emit(new GameEvent(GameEvent.Type.UPGRADE, p.x, p.y, 0, offer.item().title()));
            }
            case WEAPON -> {
                p.weapon = offer.weapon();
                p.comboStep = 0;
                p.recompute();
                run.emit(new GameEvent(GameEvent.Type.WEAPON, p.x, p.y, 0, offer.weapon().title()));
            }
            case REPAIR_KIT -> {
                if (p.repairKits >= p.stats.maxRepairKits()) return false;
                p.repairKits++;
                run.emit(
                        new GameEvent(
                                GameEvent.Type.SUPPLY, p.x, p.y - 65, 0, "Reparaturset verstaut"));
            }
            case HEAL -> {
                if (p.health >= p.maxHealth) return false;
                p.heal(40);
                run.emit(GameEvent.at(GameEvent.Type.HEAL, p.x, p.y));
            }
            case WEAPON_UPGRADE -> {
                if (p.weaponLevel >= Weapon.MAX_LEVEL) return false;
                p.weaponLevel++;
                p.recompute();
                run.emit(
                        new GameEvent(
                                GameEvent.Type.UPGRADE,
                                p.x,
                                p.y,
                                0,
                                p.weapon.title() + " Stufe " + p.weaponLevel));
            }
            case SUPPLIES -> {
                p.heal(25);
                p.addEnergy(35);
                p.salvage = Math.min(9999, p.salvage + 10);
                p.repairKits = Math.min(p.stats.maxRepairKits(), p.repairKits + 1);
                run.emit(GameEvent.at(GameEvent.Type.HEAL, p.x, p.y));
            }
        }
        return true;
    }

    private boolean shop() {
        var kind = run.room().kind();
        return kind == RoomPlan.Kind.MERCHANT || kind == RoomPlan.Kind.WORKSHOP;
    }

    /**
     * Nimmt einen Kapellenhandel an.
     *
     * @param deal Handel
     * @return {@code true}, wenn verbucht
     */
    boolean accept(ShrineDeal deal) {
        if (run.phase() != GameRun.Phase.ROOM_CLEARED
                || run.room().kind() != RoomPlan.Kind.SHRINE
                || dealTaken
                || !deals.contains(deal)) return false;
        var p = run.player;
        switch (deal) {
            case BLOOD_FOR_POWER -> {
                p.healthPenalty += p.maxHealth * .25;
                p.recompute();
                grantRandom(Rarity.LEGENDARY, 1);
            }
            case GLASS -> p.install(Item.GLASS_HULL);
            case GREED -> {
                p.install(Item.GREED);
                p.salvage = Math.min(9999, p.salvage + 40);
            }
            case FEVER -> p.install(Item.FEVER);
            case SCRAP_FOR_HEALTH -> {
                if (p.salvage < 40) return false;
                p.salvage -= 40;
                p.heal(p.maxHealth);
                p.repairKits = Math.min(p.stats.maxRepairKits(), p.repairKits + 1);
            }
            case KITS_FOR_ITEM -> {
                if (p.repairKits < 1) return false;
                p.repairKits = 0;
                grantRandom(Rarity.RARE, 2);
            }
        }
        dealTaken = true;
        available = false;
        boolean curse =
                deal == ShrineDeal.GLASS || deal == ShrineDeal.GREED || deal == ShrineDeal.FEVER;
        run.emit(
                new GameEvent(
                        curse ? GameEvent.Type.CURSE : GameEvent.Type.UPGRADE,
                        p.x,
                        p.y,
                        0,
                        deal.title()));
        return true;
    }

    private void grantRandom(Rarity minimum, int count) {
        var player = run.player;
        for (var item : rollItems(count, minimum))
            if (player.stacks(item) < item.maxStacks()) {
                player.install(item);
                run.emit(
                        new GameEvent(GameEvent.Type.UPGRADE, player.x, player.y, 0, item.title()));
            }
    }

    /**
     * Kostenlose Werkstattreparatur, einmal pro Werkstatt.
     *
     * @return {@code true}, wenn repariert
     */
    boolean repair() {
        if (run.room().kind() != RoomPlan.Kind.WORKSHOP
                || run.phase() != GameRun.Phase.ROOM_CLEARED
                || repaired) return false;
        run.player.heal(40);
        run.player.energy = run.player.maxEnergy();
        repaired = true;
        run.emit(GameEvent.at(GameEvent.Type.HEAL, run.player.x, run.player.y));
        return true;
    }
}
