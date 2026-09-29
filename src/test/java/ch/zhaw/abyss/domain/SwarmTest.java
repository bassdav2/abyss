package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;

/** Schwarm-Update: Horden, räumliches Raster, Überladung, Horden-Werkzeuge und Skalierung. */
class SwarmTest {

    private static Enemy swarm(GameRun run, EnemyKind kind, double x, double y) {
        var enemy = new Enemy(run.nextId(), kind, Affix.NONE, x, y, 1, 99);
        enemy.state = Enemy.State.APPROACH;
        run.enemies.add(enemy);
        return enemy;
    }

    @Test
    void hordesGrowWithDepthCycleAndPressure() {
        for (long seed = 1; seed < 40; seed++) {
            var early = new RoomGenerator(seed, 0, 0);
            var late = new RoomGenerator(seed, 2, 5);
            assertEquals(3, early.room(0, 0).hordeTotal(), "erster Raum: drei Milben");
            int shallow = early.room(1, 0).hordeTotal();
            int deep = early.room(21, 0).hordeTotal();
            assertTrue(deep > shallow * 3, "tiefer bedeutet grössere Schwärme");
            assertTrue(late.room(21, 0).hordeTotal() > deep * 5, "Endgame: hunderte Gegner");
            for (var room : early.choices(9))
                if (!room.hostile()) assertEquals(0, room.hordeTotal());
        }
    }

    @Test
    void hordesStreamInUpToTheCapAndKeepTheWaveOpen() {
        var run = new GameRun(RunSetup.standard(5, DiverClass.MECHANIC).withSeed(5));
        for (int depth = 0; depth < 13; depth++) {
            TestRuns.clear(run);
            run.chooseNextRoom(0);
        }
        while (run.room().hordeTotal() < 40) {
            TestRuns.clear(run);
            run.chooseNextRoom(1 % run.nextRooms().size());
        }
        run.player.invulnerableTime = 1e9;
        int peak = 0;
        for (int i = 0; i < 120 * 6; i++) {
            run.update(TestRuns.STEP, InputFrame.NONE);
            peak = Math.max(peak, (int) run.enemies.stream().filter(Enemy::alive).count());
            assertTrue(run.enemies.stream().filter(Enemy::alive).count() <= run.hordeCap() + 12);
        }
        assertTrue(
                peak > run.room().waves().getFirst().size() + 10, "Schwarm strömt nach: " + peak);
        assertEquals(
                GameRun.Phase.RUNNING, run.phase(), "Welle bleibt offen, solange Gegner leben");
    }

    @Test
    void gridFindsTheSameEnemiesAsALinearScan() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var random = new Random(3);
        for (int i = 0; i < 300; i++)
            swarm(
                    run,
                    random.nextBoolean() ? EnemyKind.MITE : EnemyKind.GLOWFISH,
                    100 + random.nextDouble() * (run.layout().width() - 200),
                    GameRun.FLOOR - random.nextDouble() * 400);
        for (int probe = 0; probe < 50; probe++) {
            double x = random.nextDouble() * run.layout().width(), y = random.nextDouble() * 700;
            double radius = 40 + random.nextDouble() * 300;
            var found = new HashSet<>(run.grid().around(x, y, radius));
            for (var e : run.enemies)
                if (Math.hypot(e.x - x, e.centerY() - y) <= radius)
                    assertTrue(found.contains(e), "Raster übersieht keinen Gegner");
            var nearest = run.grid().nearest(x, y, radius, null);
            var linear =
                    run.enemies.stream()
                            .filter(e -> Math.hypot(e.x - x, e.centerY() - y) < radius)
                            .min(
                                    java.util.Comparator.comparingDouble(
                                            e -> Math.hypot(e.x - x, e.centerY() - y)))
                            .orElse(null);
            assertEquals(linear, nearest);
        }
    }

    @Test
    void chainReactionsThroughHundredsOfEnemiesRunIterativelyWithoutOverflow() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        for (int i = 0; i < 5; i++) run.player.install(Item.CHAIN_REACTION);
        for (int i = 0; i < 400; i++)
            swarm(run, EnemyKind.MITE, 700 + (i % 40) * 22, GameRun.FLOOR - (i / 40) * 5);
        run.player.x = 150;
        run.player.invulnerableTime = 1e9;
        var first = run.enemies.get(0);
        assertDoesNotThrow(() -> run.combat.hitEnemy(first, 1e6, Combat.Source.MELEE, 0, 150));
        for (int i = 0; i < 120 && run.enemies.stream().anyMatch(Enemy::alive); i++)
            run.update(TestRuns.STEP, InputFrame.NONE);
        long alive = run.enemies.stream().filter(Enemy::alive).count();
        assertEquals(0, alive, "die Kettenreaktion räumt den dichten Schwarm");
        assertTrue(run.kills() >= 400);
    }

    @Test
    void shardsFillTheOverloadAndLevelUpsOfferChoices() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        for (int i = 0; i < 12; i++) {
            var mite = swarm(run, EnemyKind.MITE, run.player.x + 30, GameRun.FLOOR);
            run.combat.hitEnemy(mite, 1e6, Combat.Source.MELEE, 0, run.player.x);
        }
        assertTrue(run.pickups.stream().anyMatch(p -> p.kind() == Pickup.Kind.SHARD));
        TestRuns.seconds(run, InputFrame.NONE, 2);
        assertTrue(run.player().level() >= 1, "Splitter heben die Überladungsstufe");
        assertTrue(run.pendingLevelUps() > 0);
        assertFalse(run.levelOffers().isEmpty());
        int items = run.player().items().values().stream().mapToInt(Integer::intValue).sum();
        int pending = run.pendingLevelUps();
        var offer = run.levelOffers().getFirst();
        run.player.invulnerableTime = 0;
        assertTrue(run.chooseLevelUp(offer));
        assertEquals(pending - 1, run.pendingLevelUps());
        assertTrue(run.player.invulnerableTime >= 1, "Schutz nach der Auswahl");
        if (offer.type() == Offer.Type.ITEM)
            assertEquals(
                    items + 1,
                    run.player().items().values().stream().mapToInt(Integer::intValue).sum());
        assertFalse(run.chooseLevelUp(Offer.item(Item.SERVO, 5)), "nur angebotene Karten");
    }

    @Test
    void checkpointKeepsOverloadLevelAndProgress() {
        var run = TestRuns.standard();
        run.player.level = 7;
        run.player.xp = 12.5;
        TestRuns.clear(run);
        run.chooseNextRoom(0);
        var saved = run.checkpoint();
        assertEquals(7, saved.level());
        var restored = GameRun.restore(saved, run.itemPool(), RunSetup.defaultWeapons());
        assertEquals(7, restored.player().level());
        assertEquals(12.5, restored.player().xp(), 1e-9);
        assertEquals(saved, restored.checkpoint());
    }

    @Test
    void hordeToolsHitSwarmsAroundTheDiver() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.invulnerableTime = 1e9;
        for (int i = 0; i < 3; i++) run.player.install(Item.ORBITAL);
        var near = swarm(run, EnemyKind.MITE, run.player.x + 100, GameRun.FLOOR);
        near.stateTime = 99;
        near.state = Enemy.State.STUNNED;
        TestRuns.seconds(run, InputFrame.NONE, 1.5);
        assertTrue(near.health() < near.maxHealth(), "Kreiselmesser treffen");
        assertEquals(6, run.orbitals().length);

        var tesla = TestRuns.standard();
        TestRuns.empty(tesla);
        tesla.player.invulnerableTime = 1e9;
        tesla.player.install(Item.TESLA_FIELD);
        var target = swarm(tesla, EnemyKind.GLOWFISH, tesla.player.x + 200, GameRun.FLOOR - 100);
        target.state = Enemy.State.STUNNED;
        target.stateTime = 99;
        TestRuns.seconds(tesla, InputFrame.NONE, 1.5);
        assertTrue(target.health() < target.maxHealth(), "Teslafeld blitzt");
    }

    @Test
    void bladeWavesAndMultishotFireExtraProjectiles() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.install(Item.BLADE_WAVE);
        run.player.install(Item.MULTISHOT);
        run.player.install(Item.MULTISHOT);
        var attack = InputFrame.builder().attack().build();
        for (int i = 0; i < 40; i++) run.update(TestRuns.STEP, i < 2 ? attack : InputFrame.NONE);
        long blades =
                run.projectiles.stream().filter(q -> q.kind == Projectile.Kind.BLADE).count()
                        + run.drainEvents().size() * 0;
        assertTrue(blades >= 1, "Klingenwelle fliegt");
        assertEquals(3, run.player.stats.extraProjectiles() + 1);
    }

    @Test
    void novaAndBloodrushReactToKills() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.install(Item.NOVA);
        run.player.install(Item.BLOODRUSH);
        run.drainEvents();
        for (int i = 0; i < 12; i++) {
            var mite = swarm(run, EnemyKind.MITE, run.player.x + 400, GameRun.FLOOR);
            run.combat.hitEnemy(mite, 1e6, Combat.Source.MELEE, 0, run.player.x);
        }
        assertTrue(
                run.drainEvents().stream().anyMatch(e -> e.type() == GameEvent.Type.NOVA),
                "Druckwellenkern entlädt sich");
        assertTrue(run.player().frenzy() > 0);
        assertTrue(Arsenal.frenzyBonus(run.player) > 0);
    }

    @Test
    void overchargeKeepsLevelUpsMeaningfulOnceTheBuildIsMaxed() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        for (var item : Item.lootable())
            while (run.player.stacks(item) < item.maxStacks()) run.player.install(item);
        run.player.weaponLevel = Weapon.MAX_LEVEL;
        run.gainXp(1000);
        assertTrue(run.pendingLevelUps() > 0);
        assertTrue(run.levelOffers().contains(Offer.item(Item.OVERCHARGE, 0)));
        double before = run.player.stats.damage();
        assertTrue(run.chooseLevelUp(Offer.item(Item.OVERCHARGE, 0)));
        assertTrue(run.player.stats.damage() > before);
    }

    @Test
    void metaBonusAndStackedModulesScaleMultiplicatively() {
        var items = new java.util.EnumMap<Item, Integer>(Item.class);
        items.put(Item.SERVO, 4);
        var plain = StatSheet.compute(DiverClass.MECHANIC, Weapon.WRENCH, 0, items, 0);
        var meta = new MetaBonus(1.4, 1.2, 1.16, 1.36, 1.5, .08, 1);
        var boosted = StatSheet.compute(DiverClass.MECHANIC, Weapon.WRENCH, 0, items, 0, meta);
        assertEquals(Math.pow(1.15, 4), plain.damage(), 1e-9);
        assertEquals(plain.damage() * 1.4, boosted.damage(), 1e-9);
        assertEquals(plain.attackSpeed() * 1.2, boosted.attackSpeed(), 1e-9);
        assertEquals(plain.area() * 1.16, boosted.area(), 1e-9);
        assertEquals(plain.magnetRadius() * 1.5, boosted.magnetRadius(), 1e-9);
        assertEquals(plain.critChance() + .08, boosted.critChance(), 1e-9);
    }

    @Test
    void swarmBitesDoNotInterruptTheDiversAttack() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var mite = swarm(run, EnemyKind.MITE, run.player.x, GameRun.FLOOR);
        run.player.swing = run.player.weapon.combo().getFirst();
        assertTrue(run.combat.hurtPlayer(5, mite.x, mite, true));
        assertNotNull(run.player.swing, "Angriff läuft weiter");
        assertTrue(run.player.invulnerableTime <= .5 + 1e-9);
    }
}
