package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Bergungen, Schwarzmarkt, Werkstatt, Versorgung, Druckkapelle und Reparatursets. */
class RewardTest {

    private static GameRun reach(Predicate<RoomPlan> target) {
        for (long seed = 1; seed < 400; seed++) {
            var run = new GameRun(RunSetup.standard(seed, DiverClass.MECHANIC).withSeed(seed));
            for (int guard = 0; guard < RoomGenerator.ROOM_COUNT - 1; guard++) {
                if (target.test(run.room())) return run;
                TestRuns.clear(run);
                var choices = run.nextRooms();
                int branch = 0;
                for (int i = 0; i < choices.size(); i++)
                    if (target.test(choices.get(i))) branch = i;
                run.chooseNextRoom(branch);
            }
        }
        throw new AssertionError("Raum nicht gefunden");
    }

    @Test
    void combatRewardOffersDistinctItemsAndClosesAfterOnePick() {
        var run = TestRuns.standard();
        TestRuns.clear(run);
        var offers = run.offers();
        assertEquals(3, offers.size());
        assertEquals(offers.size(), offers.stream().distinct().count());
        assertTrue(offers.stream().allMatch(o -> o.price() == 0));
        assertTrue(run.take(offers.getFirst()));
        assertFalse(run.rewardAvailable());
        assertTrue(run.offers().isEmpty());
        assertFalse(run.take(offers.get(1)), "zweite Wahl ist nicht möglich");
    }

    @Test
    void alarmRoomsOfferAtLeastRareModules() {
        var run = reach(room -> room.condition() == RoomCondition.ALARM);
        TestRuns.clear(run);
        assertTrue(run.rewardAvailable());
        assertFalse(run.offers().isEmpty());
        for (var offer : run.offers())
            if (offer.type() == Offer.Type.ITEM)
                assertTrue(offer.item().rarity().ordinal() >= Rarity.RARE.ordinal());
    }

    @Test
    void completingAResonanceBoostsStatsAndIsAnnouncedOnce() {
        var run = TestRuns.allItems();
        TestRuns.clear(run);
        double crit = run.player().stats().critChance();
        run.player.install(Item.LENS);
        var offer = Offer.item(Item.CRIT_DAMAGE, 0);
        assertEquals(
                List.of(Synergy.MARKSMAN),
                Synergy.completedBy(run.player().items(), Item.CRIT_DAMAGE));
        run.rewards.available = true;
        run.rewards.offers = List.of(offer);
        run.drainEvents();
        assertTrue(run.take(offer));
        var announced =
                run.drainEvents().stream().filter(e -> e.type() == GameEvent.Type.SYNERGY).toList();
        assertEquals(1, announced.size());
        assertEquals("Scharfschütze", announced.getFirst().text());
        assertEquals(crit + .07 + .08, run.player().stats().critChance(), 1e-9);
        assertTrue(run.chooseNextRoom(0), "neue Raum-Sicherung mit beiden Modulen");
        var restored =
                GameRun.restore(run.checkpoint(), run.setup().itemPool(), run.setup().weaponPool());
        TestRuns.clear(restored);
        var unrelated = Offer.item(Item.SERVO, 0);
        restored.rewards.available = true;
        restored.rewards.offers = List.of(unrelated);
        restored.drainEvents();
        assertTrue(restored.take(unrelated));
        assertTrue(
                restored.drainEvents().stream().noneMatch(e -> e.type() == GameEvent.Type.SYNERGY),
                "bekannte Resonanz nach dem Fortsetzen nicht erneut melden");
        assertEquals(List.of(Synergy.MARKSMAN), Synergy.activeIn(restored.player().items()));
    }

    @Test
    void resonanceBonusesFollowTheirDescriptions() {
        var base = StatSheet.compute(DiverClass.MECHANIC, Weapon.WRENCH, 0, Map.of(), 0);
        var fortress =
                StatSheet.compute(
                        DiverClass.MECHANIC,
                        Weapon.WRENCH,
                        0,
                        Map.of(Item.PLATING, 1, Item.SHIELD_CELL, 1),
                        0);
        assertEquals(40, fortress.maxShield(), 1e-9);
        var heavy =
                StatSheet.compute(
                        DiverClass.MECHANIC,
                        Weapon.WRENCH,
                        0,
                        Map.of(Item.BALLAST, 1, Item.SERVO, 1),
                        0);
        assertEquals(base.damage() * 1.15 * 1.1, heavy.damage(), 1e-9);
        var scrapper =
                StatSheet.compute(
                        DiverClass.MECHANIC,
                        Weapon.WRENCH,
                        0,
                        Map.of(Item.MAGNET, 1, Item.TOOLBELT, 1),
                        0);
        assertEquals(base.maxRepairKits() + 2, scrapper.maxRepairKits());
        assertEquals(base.scrapGain() + .3 + .2, scrapper.scrapGain(), 1e-9);
    }

    @Test
    void compassAddsAFourthChoice() {
        var run = TestRuns.allItems();
        run.player.install(Item.COMPASS);
        TestRuns.clear(run);
        assertEquals(4, run.offers().size());
    }

    @Test
    void bossRewardsContainALegendaryWhenUnlocked() {
        var run = TestRuns.allItems();
        while (run.room().kind() != RoomPlan.Kind.BOSS) {
            TestRuns.clear(run);
            run.chooseNextRoom(0);
        }
        TestRuns.clear(run);
        assertEquals(Rarity.LEGENDARY, run.offers().getFirst().item().rarity());
    }

    @Test
    void merchantSellsForScrapAndRemovesSoldItems() {
        var run = reach(room -> room.kind() == RoomPlan.Kind.MERCHANT);
        assertEquals(GameRun.Phase.ROOM_CLEARED, run.phase());
        run.player.x = run.layout().rewardX();
        assertEquals(Interaction.MERCHANT, run.interaction());
        var item =
                run.offers().stream()
                        .filter(o -> o.type() == Offer.Type.ITEM)
                        .findFirst()
                        .orElseThrow();
        run.player.salvage = item.price() - 1;
        assertFalse(run.take(item), "zu wenig Schrott");
        run.player.salvage = item.price() + 5;
        assertTrue(run.take(item));
        assertEquals(5, run.player.salvage());
        assertFalse(run.offers().contains(item));
        assertTrue(run.rewardAvailable(), "Händlerin bleibt offen");
    }

    @Test
    void workshopRepairsOnceAndUpgradesTheWeapon() {
        var run = reach(room -> room.kind() == RoomPlan.Kind.WORKSHOP);
        run.player.health = 20;
        assertTrue(run.repair());
        assertFalse(run.repair());
        assertEquals(60, run.player.health, 1e-6);
        var upgrade =
                run.offers().stream()
                        .filter(o -> o.type() == Offer.Type.WEAPON_UPGRADE)
                        .findFirst()
                        .orElseThrow();
        run.player.salvage = 500;
        double before = run.player.stats().damage();
        assertTrue(run.take(upgrade));
        assertEquals(1, run.player.weaponLevel());
        assertTrue(run.player.stats().damage() > before);
        assertTrue(
                run.offers().stream()
                        .anyMatch(
                                o ->
                                        o.type() == Offer.Type.WEAPON_UPGRADE
                                                && o.price() > upgrade.price()));
    }

    @Test
    void cacheGivesSuppliesAndAKit() {
        var run = reach(room -> room.kind() == RoomPlan.Kind.CACHE);
        TestRuns.clear(run);
        run.player.health = 10;
        run.player.repairKits = 0;
        var supplies = run.offers().getFirst();
        assertEquals(Offer.Type.SUPPLIES, supplies.type());
        assertTrue(run.take(supplies));
        assertEquals(35, run.player.health, 1e-6);
        assertEquals(1, run.player.repairKits());
    }

    @Test
    void shrineAllowsExactlyOneDeal() {
        var run = reach(room -> room.kind() == RoomPlan.Kind.SHRINE);
        run.player.salvage = 100;
        run.player.x = run.layout().rewardX();
        assertEquals(Interaction.SHRINE, run.interaction());
        var deals = run.deals();
        assertFalse(deals.isEmpty());
        assertTrue(run.acceptDeal(deals.getFirst()));
        assertTrue(run.dealTaken());
        assertFalse(run.acceptDeal(deals.getLast()));
        assertEquals(Interaction.NONE, run.interaction());
    }

    @Test
    void glassHullTradesHealthForDamage() {
        var run = TestRuns.standard();
        double health = run.player.maxHealth, damage = run.player.stats().damage();
        run.player.install(Item.GLASS_HULL);
        assertEquals(health * .7, run.player.maxHealth, 1e-6);
        assertEquals(damage + .4, run.player.stats().damage(), 1e-6);
    }

    @Test
    void repairKitsHealOnlyWhenNeededAndAreCapped() {
        var run = TestRuns.standard();
        int kits = run.player.repairKits();
        assertFalse(run.useRepairKit(), "volle Integrität verbraucht kein Set");
        run.player.health = 50;
        assertTrue(run.useRepairKit());
        assertEquals(85, run.player.health, 1e-6);
        assertEquals(kits - 1, run.player.repairKits());
        run.player.repairKits = run.player.stats().maxRepairKits();
        run.player.install(Item.TOOLBELT);
        assertEquals(run.player.stats().maxRepairKits(), run.player.repairKits());
    }

    @Test
    void weaponOffersSwapTheWeaponButKeepItsLevel() {
        var run = TestRuns.allItems();
        run.player.weaponLevel = 2;
        var offer = Offer.weapon(Weapon.ANCHOR, 0);
        TestRuns.clear(run);
        assertFalse(run.take(offer), "nur tatsächlich angebotene Waffen");
        assertEquals(Weapon.WRENCH, run.player.weapon());
    }
}
