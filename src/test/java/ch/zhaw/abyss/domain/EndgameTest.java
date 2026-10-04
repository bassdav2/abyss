package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;

/** Endgame: Eskalation ab Raum 20, riesige Schwärme, Bedrohungen und neue Schwarmarten. */
class EndgameTest {

    /** Lauf mit allen Modulen im Pool. */
    private static GameRun run(long seed) {
        var base = RunSetup.standard(seed, DiverClass.MECHANIC);
        var items = EnumSet.noneOf(Item.class);
        items.addAll(Item.lootable());
        return new GameRun(
                new RunSetup(
                        seed,
                        base.diver(),
                        base.weapon(),
                        base.module(),
                        false,
                        0,
                        items,
                        base.weaponPool(),
                        0,
                        0,
                        0));
    }

    /** Räumt Räume, bis die gewünschte Raumtiefe im gewünschten Zyklus erreicht ist. */
    private static void travel(GameRun run, int cycle, int depth) {
        for (int guard = 0; guard < 200; guard++) {
            if (run.cycle() == cycle && run.room().depth() == depth) return;
            TestRuns.clear(run);
            if (run.phase() == GameRun.Phase.VICTORY) run.nextCycle();
            else run.chooseNextRoom(0);
        }
        fail("Raum nicht erreicht");
    }

    @Test
    void escalationStartsAtRoomTwentyAndKeepsGrowingAcrossCycles() {
        assertEquals(0, RoomGenerator.escalation(0, 18));
        assertEquals(1, RoomGenerator.escalation(0, 19), "Raum 20");
        assertEquals(6, RoomGenerator.escalation(1, 0));
        assertEquals(53, RoomGenerator.escalation(2, 23));
        double last = 0;
        for (int e = 0; e < 80; e++) {
            assertTrue(RoomGenerator.surge(e) > last);
            last = RoomGenerator.surge(e);
        }
        assertTrue(RoomGenerator.surge(5) > 2, "Sprung ab Raum 20");
    }

    @Test
    void endgameRoomsAreWiderAndHoldThousandsOfSwarmEnemies() {
        int early = 0, late = 0;
        double narrow = 0, wide = 0;
        for (long seed = 1; seed < 30; seed++) {
            var first = new RoomGenerator(seed, 0, 0).room(21, 0);
            var third = new RoomGenerator(seed, 2, 0).room(21, 0);
            early += first.hordeTotal();
            late += third.hordeTotal();
            narrow += first.layout().width();
            wide += third.layout().width();
        }
        assertTrue(late / 29 >= 2000, "Endgame: tausende Gegner je Raum, nicht " + late / 29);
        assertTrue(late > early * 10, "Eskalation vervielfacht die Schwärme");
        assertTrue(wide > narrow * 1.8, "Endgame-Räume sind deutlich breiter");
    }

    @Test
    void theAliveCapReachesAThousandAtTheEndOfTheSecondCycle() {
        var run = run(17);
        assertTrue(run.hordeCap() < 60);
        travel(run, 1, 23);
        assertTrue(run.hordeCap() >= 900, "Kapazität " + run.hordeCap());
        assertTrue(run.hordeCap() <= GameRun.MAX_ALIVE);
    }

    @Test
    void endgameSwarmsMixAllEightAttackKinds() {
        var kinds = EnumSet.noneOf(EnemyKind.class);
        for (long seed = 1; seed < 20; seed++)
            for (var wave : new RoomGenerator(seed, 1, 0).room(9, 0).hordes())
                for (var horde : wave) kinds.add(horde.kind());
        assertTrue(
                kinds.containsAll(
                        EnumSet.of(
                                EnemyKind.MITE,
                                EnemyKind.NANODRONE,
                                EnemyKind.GLOWFISH,
                                EnemyKind.FUSE,
                                EnemyKind.SPITTER,
                                EnemyKind.CRAB,
                                EnemyKind.LANCER,
                                EnemyKind.PRISM)),
                kinds.toString());
        for (var kind : kinds) assertTrue(kind.swarm(), kind + " ist ein Schwarm");
    }

    @Test
    void threatsAppearFromTheEngineDeckAndAlmostAlwaysInTheEndgame() {
        var seen = EnumSet.noneOf(Threat.class);
        int endgame = 0, threatened = 0;
        for (long seed = 0; seed < 200; seed++) {
            var generator = new RoomGenerator(seed, 0, 0);
            for (int depth = 0; depth < RoomGenerator.ROOM_COUNT; depth++)
                for (var room : generator.choices(depth)) {
                    if (room.threat() == Threat.NONE) continue;
                    seen.add(room.threat());
                    assertTrue(depth >= RoomGenerator.SECTOR_ROOMS, "nicht in der Hecksektion");
                    assertTrue(
                            room.kind() == RoomPlan.Kind.COMBAT
                                    || room.kind() == RoomPlan.Kind.ELITE);
                }
            for (var room : new RoomGenerator(seed, 1, 0).choices(8))
                if (room.kind() == RoomPlan.Kind.COMBAT) {
                    endgame++;
                    if (room.threat() != Threat.NONE) threatened++;
                }
        }
        assertEquals(EnumSet.complementOf(EnumSet.of(Threat.NONE)), seen);
        assertTrue(threatened > endgame * .6, threatened + "/" + endgame);
    }

    @Test
    void threatRoomsShapeTheirEnemies() {
        boolean sky = false, nests = false, colossus = false;
        for (long seed = 0; seed < 300 && !(sky && nests && colossus); seed++)
            for (var room : new RoomGenerator(seed, 1, 0).choices(9)) {
                switch (room.threat()) {
                    case SKY -> {
                        sky = true;
                        for (var wave : room.hordes())
                            for (var horde : wave) assertTrue(horde.kind().flying());
                        for (var wave : room.waves())
                            for (var spawn : wave) assertTrue(spawn.kind().flying(), "" + spawn);
                    }
                    case NESTS -> {
                        nests = true;
                        assertTrue(
                                room.waves().getFirst().stream()
                                        .anyMatch(s -> s.kind() == EnemyKind.HIVE));
                    }
                    case COLOSSUS -> {
                        colossus = true;
                        for (var wave : room.waves()) {
                            assertTrue(wave.size() <= 6);
                            for (var spawn : wave)
                                if (spawn.kind() != EnemyKind.SMUGGLER)
                                    assertTrue(spawn.affix().elite());
                        }
                    }
                    default -> {}
                }
            }
        assertTrue(sky && nests && colossus);
    }

    @Test
    void armoredSwarmsOnlyFearCriticalHitsBurnAndExplosions() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var plain = TestRuns.place(run, EnemyKind.SENTINEL, 1200);
        var plated = TestRuns.place(run, EnemyKind.SENTINEL, 1300);
        plated.plated = true;
        double normal = run.combat.hitEnemy(plain, 10, Combat.Source.CHAIN, 0, 1000);
        double armored = run.combat.hitEnemy(plated, 10, Combat.Source.CHAIN, 0, 1000);
        assertEquals(normal, armored, 1e-9, "Blitze wirken voll");
        run.player.stats =
                new StatSheet(
                        100, 100, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 1, 0, 0, 0, 1, 0, 0, 1, 0, 3, 180,
                        1, 1, 0);
        double meleePlain = run.combat.hitEnemy(plain, 10, Combat.Source.MELEE, 0, 1000);
        double meleePlated = run.combat.hitEnemy(plated, 10, Combat.Source.MELEE, 0, 1000);
        assertEquals(meleePlain * .35, meleePlated, 1e-9, "normale Treffer gedämpft");
    }

    @Test
    void theCrabShellBlocksFromTheFrontButNotFromBehind() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.stats =
                new StatSheet(
                        100, 100, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 1, 0, 0, 0, 1, 0, 0, 1, 0, 3, 180,
                        1, 1, 0);
        var crab = TestRuns.place(run, EnemyKind.CRAB, 1200);
        crab.maxHealth = crab.health = 1e6;
        crab.facing = -1;
        double front = run.combat.hitEnemy(crab, 10, Combat.Source.MELEE, 0, 1100);
        double back = run.combat.hitEnemy(crab, 10, Combat.Source.MELEE, 0, 1300);
        assertEquals(back * .2, front, 1e-9);
    }

    @Test
    void aFallenNestTakesItsBroodWithIt() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.invulnerableTime = 1e9;
        var hive = TestRuns.place(run, EnemyKind.HIVE, 1200);
        hive.actionCooldown = 0;
        TestRuns.seconds(run, InputFrame.NONE, 8);
        long brood = run.enemies.stream().filter(e -> e.alive() && e.parentId == hive.id).count();
        assertTrue(brood >= 2, "Nest brütet: " + brood);
        run.combat.hitEnemy(hive, 1e9, Combat.Source.MACHINE, 0, 1000);
        assertTrue(
                run.enemies.stream().noneMatch(e -> e.alive() && e.parentId == hive.id),
                "Brut stirbt mit dem Nest");
    }

    @Test
    void fuseMitesBlowUpNextToThePlayerAndChainWhenCaught() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.x = 1000;
        var fuse = TestRuns.place(run, EnemyKind.FUSE, 1100);
        fuse.actionCooldown = 0;
        double before = run.player.health;
        TestRuns.seconds(run, InputFrame.NONE, 3);
        assertTrue(run.player.health < before, "Zündmilbe sprengt sich");
        assertFalse(fuse.alive());
        assertEquals(0, run.kills(), "Selbstzerstörung ist kein Abschuss");

        TestRuns.empty(run);
        var caught = TestRuns.place(run, EnemyKind.FUSE, 1600);
        var neighbour = TestRuns.place(run, EnemyKind.SENTINEL, 1640);
        run.combat.hitEnemy(caught, 1e6, Combat.Source.MACHINE, 0, 1500);
        assertTrue(neighbour.health() < neighbour.maxHealth(), "erwischt reisst sie Nachbarn mit");
    }

    @Test
    void lancersAimVisiblyAndPrismsFireRings() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.invulnerableTime = 1e9;
        var lancer = TestRuns.place(run, EnemyKind.LANCER, 900);
        lancer.actionCooldown = 0;
        boolean aimed = false;
        for (int i = 0; i < 120 * 4 && !aimed; i++) {
            run.update(TestRuns.STEP, InputFrame.NONE);
            aimed = lancer.telegraph() != null;
        }
        assertTrue(aimed, "Speerfisch zielt mit einer Linie");

        TestRuns.empty(run);
        var prism = TestRuns.place(run, EnemyKind.PRISM, 600);
        prism.actionCooldown = 0;
        TestRuns.seconds(run, InputFrame.NONE, 1.5);
        assertTrue(
                run.projectiles.stream().filter(q -> q.kind == Projectile.Kind.PRISM).count()
                        >= Behaviors.Prism.RING - 1);
    }

    @Test
    void hostileShotsAreCappedInGiantSwarms() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        for (int i = 0; i < GameRun.MAX_HOSTILE_SHOTS + 200; i++)
            run.shoot(Projectile.Kind.PRISM, false, 800, 300, 0, 0, 1, 8, 5);
        assertEquals(GameRun.MAX_HOSTILE_SHOTS, run.projectiles.size());
        run.shoot(Projectile.Kind.HARPOON, true, 800, 300, 0, 0, 1, 8, 5);
        assertEquals(GameRun.MAX_HOSTILE_SHOTS + 1, run.projectiles.size(), "eigene Geschosse");
    }

    @Test
    void trialsGrantACoreAndARareSalvage() {
        for (long seed = 1; seed < 400; seed++) {
            var run = run(seed);
            var generator = new RoomGenerator(seed, 0, 0);
            int target = -1;
            for (int depth = 6; depth < 18 && target < 0; depth++)
                if (generator.room(depth, 0).threat() != Threat.NONE) target = depth;
            if (target < 0) continue;
            travel(run, 0, target);
            int cores = run.player.cores;
            TestRuns.clear(run);
            run.loot.collectAll();
            assertTrue(run.player.cores > cores, "Datenkern für die bestandene Prüfung");
            assertTrue(
                    run.offers().stream()
                            .allMatch(o -> o.item() == null || o.item().rarity() != Rarity.COMMON),
                    "seltene Bergung");
            return;
        }
        fail("Keine Bedrohung gefunden");
    }

    @Test
    void threatReadinessFollowsTheBuild() {
        var run = run(3);
        var p = run.player;
        assertFalse(Threat.ARMORED.prepared(p));
        for (int i = 0; i < 6; i++) p.install(Item.LENS);
        assertTrue(Threat.ARMORED.prepared(p), "Kritik knackt Panzer");
        assertFalse(Threat.FLOOD.prepared(p));
        for (int i = 0; i < 5; i++) p.install(Item.ORBITAL);
        assertTrue(Threat.FLOOD.prepared(p));
        var unique = new HashSet<String>();
        for (var threat : Threat.values()) assertTrue(unique.add(threat.title()));
    }
}
