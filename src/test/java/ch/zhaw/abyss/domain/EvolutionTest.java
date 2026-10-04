package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;

/** Entfesselungen, Grenzbrecher und Überkritik: das Upgrade-Endgame. */
class EvolutionTest {

    private static void max(Player p, Item item) {
        while (p.stacks(item) < item.maxStacks()) p.install(item);
    }

    @Test
    void everyEvolutionHasAUniqueRecipeOfLootableModules() {
        var bases = new HashMap<Item, Item>();
        for (var evolution : Item.evolutions()) {
            assertNotNull(evolution.base(), evolution.name());
            assertNotNull(evolution.partner(), evolution.name());
            assertTrue(Item.lootable().contains(evolution.base()));
            assertTrue(Item.lootable().contains(evolution.partner()));
            assertNull(bases.put(evolution.base(), evolution), "eine Entfesselung je Modul");
            assertEquals(evolution, Item.evolutionOf(evolution.base()));
            assertFalse(Item.lootable().contains(evolution), "nur über das Rezept");
            assertEquals(1, evolution.maxStacks());
        }
        assertEquals(11, Item.evolutions().size());
    }

    @Test
    void aMaxedModuleAndItsPartnerOfferTheEvolutionFirst() {
        var run = TestRuns.allItems();
        var p = run.player;
        max(p, Item.ORBITAL);
        assertNull(run.rewards.evolution(), "Partner fehlt noch");
        p.install(Item.AREA);
        assertEquals(Item.STORM_BLADES, run.rewards.evolution());
        run.gainXp(GameRun.xpToNext(p.level) + 1);
        assertEquals(Item.STORM_BLADES, run.levelOffers().getFirst().item());
        int before = Arsenal.orbitalCount(p);
        assertTrue(run.chooseLevelUp(run.levelOffers().getFirst()));
        assertEquals(1, p.stacks(Item.STORM_BLADES));
        assertEquals(before + 6, Arsenal.orbitalCount(p), "zweiter Rotorring");
        assertNull(run.rewards.evolution(), "nur einmal");
        assertTrue(run.drainEvents().stream().anyMatch(e -> e.type() == GameEvent.Type.EVOLUTION));
    }

    @Test
    void evolutionsCannotBeTakenWithoutTheirRecipe() {
        var run = TestRuns.allItems();
        assertFalse(run.rewards.grant(Offer.item(Item.THUNDERHEAD, 0)));
        assertEquals(0, run.player.stacks(Item.THUNDERHEAD));
    }

    @Test
    void theThunderheadStrikesFarMoreOftenThanATeslaField() {
        int plain = strikes(false), storm = strikes(true);
        assertTrue(storm > plain * 4, storm + " gegen " + plain);
    }

    private static int strikes(boolean evolved) {
        var run = TestRuns.allItems();
        TestRuns.empty(run);
        var p = run.player;
        max(p, Item.TESLA_FIELD);
        p.install(Item.ARC_COIL);
        if (evolved) p.install(Item.THUNDERHEAD);
        p.invulnerableTime = 1e9;
        for (int i = 0; i < 20; i++) {
            var e = TestRuns.place(run, EnemyKind.SENTINEL, p.x + 60 + i * 12);
            e.maxHealth = e.health = 1e9;
            e.state = Enemy.State.STUNNED;
            e.stateTime = 1e9;
        }
        run.drainEvents();
        TestRuns.seconds(run, InputFrame.NONE, 4);
        return (int)
                run.drainEvents().stream()
                        .filter(
                                e ->
                                        e.type() == GameEvent.Type.HIT
                                                || e.type() == GameEvent.Type.CRIT)
                        .count();
    }

    @Test
    void theLimitBreakerRaisesCapsAndSurvivesARestore() {
        var run = TestRuns.allItems();
        var p = run.player;
        max(p, Item.SERVO);
        assertEquals(8, p.maxStacks(Item.SERVO));
        p.install(Item.LIMIT_BREAK);
        assertEquals(10, p.maxStacks(Item.SERVO));
        assertEquals(2, p.maxStacks(Item.COMPASS), "legendäre Module bleiben begrenzt");
        p.install(Item.SERVO);
        p.install(Item.SERVO);
        assertEquals(10, p.stacks(Item.SERVO));
        TestRuns.clear(run);
        run.chooseNextRoom(0);
        var restored =
                GameRun.restore(run.checkpoint(), run.setup().itemPool(), run.setup().weaponPool());
        assertEquals(10, restored.player().stacks(Item.SERVO));
    }

    @Test
    void theLimitBreakerAppearsWhenTheBuildIsCapped() {
        var run = TestRuns.allItems();
        var p = run.player;
        for (var item : new Item[] {Item.SERVO, Item.PLATING, Item.LENS, Item.OVERCLOCK})
            max(p, item);
        boolean offered = false;
        for (int i = 0; i < 40 && !offered; i++) {
            run.gainXp(GameRun.xpToNext(p.level) + 1);
            offered = run.levelOffers().stream().anyMatch(o -> o.item() == Item.LIMIT_BREAK);
            while (p.pendingLevelUps > 0) run.chooseLevelUp(run.levelOffers().getLast());
        }
        assertTrue(offered);
    }

    @Test
    void criticalChanceAboveOneHundredPercentBecomesOvercrit() {
        assertEquals(0, StatSheet.critTier(.3, .5));
        assertEquals(1, StatSheet.critTier(.3, .1));
        assertEquals(1, StatSheet.critTier(1.4, .9));
        assertEquals(2, StatSheet.critTier(1.4, .1));
        assertEquals(3, StatSheet.critTier(2.6, .2));
        assertEquals(3, StatSheet.critTier(3.5, .0), "höchstens drei Stufen");
        var items = new EnumMap<Item, Integer>(Item.class);
        items.put(Item.LENS, 20);
        var sheet = StatSheet.compute(DiverClass.HARPOONER, Weapon.HARPOON, 0, items, 0);
        assertTrue(sheet.critChance() > 1, "Kritik wird nicht mehr bei 100 % gekappt");
    }

    @Test
    void overcritHitsDealMoreThanNormalCrits() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var target = TestRuns.place(run, EnemyKind.SENTINEL, 1200);
        target.maxHealth = target.health = 1e9;
        run.player.stats =
                new StatSheet(
                        100, 100, 1, 1, 1, 1, 1, 1, 1, 1, 1.0, 2, 1, 0, 0, 0, 1, 0, 0, 1, 0, 3, 180,
                        1, 1, 0);
        double crit = run.combat.hitEnemy(target, 10, Combat.Source.MELEE, 0, 1100);
        run.player.stats =
                new StatSheet(
                        100, 100, 1, 1, 1, 1, 1, 1, 1, 1, 3.0, 2, 1, 0, 0, 0, 1, 0, 0, 1, 0, 3, 180,
                        1, 1, 0);
        double overcrit = run.combat.hitEnemy(target, 10, Combat.Source.MELEE, 0, 1100);
        assertEquals(crit * 2.2, overcrit, 1e-6, "Stufe drei: +60 % je weiterer Stufe");
    }

    @Test
    void theBastionStrikesBackWhenItsShieldBreaks() {
        var run = TestRuns.allItems();
        TestRuns.empty(run);
        var p = run.player;
        max(p, Item.SHIELD_CELL);
        p.install(Item.PLATING);
        p.install(Item.BASTION);
        assertTrue(p.stats.maxShield() >= 200);
        p.shield = 5;
        var e = TestRuns.place(run, EnemyKind.SENTINEL, p.x + 120);
        double health = p.health;
        run.combat.hurtPlayer(50, p.x + 50, null, false);
        assertEquals(health, p.health, 1e-9, "der brechende Schild schluckt den Treffer");
        assertTrue(e.health() < e.maxHealth(), "Druckwelle trifft Gegner");
        assertTrue(p.invulnerableTime > 1);
    }
}
