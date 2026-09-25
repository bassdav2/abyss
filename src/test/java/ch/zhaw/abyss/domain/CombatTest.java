package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Waffen, Treffer, Module, Zustände, Gegnerverhalten und Bosse. */
class CombatTest {
    private static final InputFrame ATTACK = InputFrame.builder().attack().build();

    @Test
    void meleeHitsOnlyInFrontAndOnlyOncePerSwing() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var front = TestRuns.place(run, EnemyKind.SENTINEL, run.player.x + 110);
        var behind = TestRuns.place(run, EnemyKind.SENTINEL, run.player.x - 110);
        run.update(TestRuns.STEP, ATTACK);
        assertEquals(front.maxHealth(), front.health(), "vor dem aktiven Fenster kein Treffer");
        TestRuns.tick(run, InputFrame.NONE, 12);
        double afterFirst = front.health();
        assertTrue(afterFirst < front.maxHealth());
        assertEquals(behind.maxHealth(), behind.health());
        TestRuns.tick(run, InputFrame.NONE, 20);
        assertEquals(afterFirst, front.health(), "ein Schlag trifft genau einmal");
    }

    @Test
    void comboAdvancesAndResetsAfterAPause() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        assertEquals(0, run.player.comboStep());
        TestRuns.tick(run, ATTACK, 1);
        assertEquals(1, run.player.comboStep());
        TestRuns.seconds(run, ATTACK, .45);
        assertTrue(run.player.comboStep() != 1, "gehaltener Angriff führt die Kombination fort");
        TestRuns.seconds(run, InputFrame.NONE, 1.5);
        TestRuns.tick(run, ATTACK, 1);
        assertEquals(1, run.player.comboStep(), "nach einer Pause beginnt die Kombination neu");
    }

    @Test
    void grappleHookPullsEnemiesTowardsThePlayer() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var enemy = TestRuns.place(run, EnemyKind.SCUTTLER, run.player.x + 320);
        double before = enemy.x();
        double pull = Weapon.GRAPPLE.combo().getLast().knockback();
        assertTrue(pull < 0, "dritter Stoss zieht statt zu stossen");
        run.combat.hitEnemy(enemy, 1, Combat.Source.MELEE, pull, run.player.x);
        assertEquals(Enemy.State.STUNNED, enemy.state());
        TestRuns.seconds(run, InputFrame.NONE, .4);
        assertTrue(enemy.x() < before - 150, "Gegner wird herangezogen");
        assertTrue(enemy.x() > run.player.x, "aber nicht an der Figur vorbei");
    }

    @Test
    void mouseAimTurnsThePlayerForTheAttack() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var enemy = TestRuns.place(run, EnemyKind.SENTINEL, run.player.x - 100);
        TestRuns.tick(run, InputFrame.builder().right().attack().aim(-1).build(), 1);
        assertEquals(-1, run.player.facing());
        TestRuns.tick(run, InputFrame.builder().right().build(), 14);
        assertTrue(enemy.health() < enemy.maxHealth());
    }

    @Test
    void servoIncreasesAndPlatingReducesDamage() {
        var plain = TestRuns.standard();
        var boosted = TestRuns.standard();
        boosted.player.install(Item.SERVO);
        boosted.player.install(Item.SERVO);
        var a = TestRuns.place(plain, EnemyKind.SENTINEL, 900);
        var b = TestRuns.place(boosted, EnemyKind.SENTINEL, 900);
        plain.rng = new java.util.Random(1);
        boosted.rng = new java.util.Random(1);
        plain.player.stats = withoutCrit(plain.player.stats);
        boosted.player.stats = withoutCrit(boosted.player.stats);
        double dealtPlain = plain.combat.hitEnemy(a, 20, Combat.Source.MELEE, 0, 800);
        double dealtBoosted = boosted.combat.hitEnemy(b, 20, Combat.Source.MELEE, 0, 800);
        assertEquals(dealtPlain * 1.3, dealtBoosted, 1e-6);
        var armored = TestRuns.standard();
        armored.player.install(Item.PLATING);
        armored.player.invulnerableTime = 0;
        plain.player.invulnerableTime = 0;
        plain.combat.hurtPlayer(20, 0, null, false);
        armored.combat.hurtPlayer(20, 0, null, false);
        assertEquals(20, plain.player.maxHealth - plain.player.health, 1e-6);
        assertEquals(18, armored.player.maxHealth - armored.player.health, 1e-6);
    }

    private static StatSheet withoutCrit(StatSheet s) {
        return new StatSheet(
                s.maxHealth(),
                s.maxEnergy(),
                s.damage(),
                s.abilityDamage(),
                s.attackSpeed(),
                s.reach(),
                s.moveSpeed(),
                s.cooldown(),
                s.moduleCooldown(),
                s.damageTaken(),
                0,
                s.critDamage(),
                s.knockback(),
                s.lifesteal(),
                s.extraJumps(),
                s.energyRegen(),
                s.scrapGain(),
                0,
                0,
                s.burnPower(),
                s.maxShield(),
                s.maxRepairKits(),
                s.magnetRadius());
    }

    @Test
    void itemStacksAreCappedPerRarity() {
        var run = TestRuns.standard();
        for (int i = 0; i < Item.SERVO.maxStacks(); i++) run.player.install(Item.SERVO);
        assertThrows(IllegalStateException.class, () -> run.player.install(Item.SERVO));
        run.player.install(Item.SECOND_HEART);
        assertThrows(IllegalStateException.class, () -> run.player.install(Item.SECOND_HEART));
        assertEquals(3, Item.SERVO.maxStacks());
        assertEquals(1, Item.COMPASS.maxStacks());
    }

    @Test
    void emergencyCapsuleRevivesOnce() {
        var run = TestRuns.standard();
        run.player.install(Item.SECOND_HEART);
        run.player.invulnerableTime = 0;
        run.combat.hurtPlayer(10_000, 0, null, false);
        assertTrue(run.player.alive());
        assertEquals(run.player.maxHealth * .5, run.player.health, 1e-6);
        assertTrue(run.player.reviveUsed());
        run.player.invulnerableTime = 0;
        run.combat.hurtPlayer(10_000, 0, null, false);
        assertFalse(run.player.alive());
    }

    @Test
    void barrierBlocksTheFirstHitOfEachRoom() {
        var run = TestRuns.standard();
        run.player.install(Item.BARRIER);
        run.player.barrierCharges = 1;
        run.player.invulnerableTime = 0;
        assertFalse(run.combat.hurtPlayer(30, 0, null, false));
        assertEquals(run.player.maxHealth, run.player.health);
        run.player.invulnerableTime = 0;
        assertTrue(run.combat.hurtPlayer(30, 0, null, false));
    }

    @Test
    void burningDealsDamageOverTimeAndChillSlowsTheBrain() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var enemy = TestRuns.place(run, EnemyKind.SENTINEL, 1200);
        enemy.statuses.ignite(2, 10);
        double before = enemy.health;
        TestRuns.seconds(run, InputFrame.NONE, 1);
        assertEquals(before - 10, enemy.health, 1.5);
        assertEquals(1.0, enemy.statuses.slowFactor());
        enemy.statuses.apply(Status.CHILL, 1);
        assertEquals(.6, enemy.statuses.slowFactor());
    }

    @Test
    void bossesAreNotFrozenButChilledInstead() {
        var run = TestRuns.standard();
        var boss = TestRuns.place(run, EnemyKind.WARDEN, 1200);
        run.combat.applyStatus(boss, Status.FREEZE, 2);
        assertFalse(boss.statuses.active(Status.FREEZE));
        assertTrue(boss.statuses.active(Status.CHILL));
        var drone = TestRuns.place(run, EnemyKind.DRONE, 900);
        run.combat.applyStatus(drone, Status.FREEZE, 2);
        assertTrue(drone.statuses.active(Status.FREEZE));
    }

    @Test
    void enemiesTelegraphBeforeTheyStrike() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var scuttler = TestRuns.place(run, EnemyKind.SCUTTLER, run.player.x + 180);
        scuttler.actionCooldown = 0;
        run.player.invulnerableTime = 10;
        boolean sawWindup = false, sawStrikeAfterWindup = false;
        for (int i = 0; i < 240; i++) {
            run.update(TestRuns.STEP, InputFrame.NONE);
            if (scuttler.state() == Enemy.State.WINDUP) {
                sawWindup = true;
                assertNotNull(scuttler.telegraph());
            }
            if (scuttler.state() == Enemy.State.STRIKE && sawWindup) sawStrikeAfterWindup = true;
        }
        assertTrue(sawWindup && sawStrikeAfterWindup);
    }

    @Test
    void shieldBearerBlocksFrontalHitsButNotHitsFromBehind() {
        var run = TestRuns.standard();
        var bearer = TestRuns.place(run, EnemyKind.SHIELDBEARER, 1000);
        bearer.facing = -1;
        run.player.stats = withoutCrit(run.player.stats);
        double front = run.combat.hitEnemy(bearer, 20, Combat.Source.MELEE, 0, 900);
        double back = run.combat.hitEnemy(bearer, 20, Combat.Source.MELEE, 0, 1100);
        assertTrue(back > front * 5);
    }

    @Test
    void bossArmorOpensOnlyDuringRecovery() {
        var run = TestRuns.standard();
        var boss = TestRuns.place(run, EnemyKind.WARDEN, 1200);
        run.player.stats = withoutCrit(run.player.stats);
        double armored = run.combat.hitEnemy(boss, 20, Combat.Source.MELEE, 0, 1000);
        boss.state = Enemy.State.RECOVER;
        double open = run.combat.hitEnemy(boss, 20, Combat.Source.MELEE, 0, 1000);
        assertEquals(armored / EnemyKind.WARDEN.armor(), open, 1e-6);
    }

    @Test
    void bossesChangePhaseAtTwoThirdsAndOneThird() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var boss = TestRuns.place(run, EnemyKind.REACTOR, 1200);
        boss.health = boss.maxHealth * .6;
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertEquals(1, boss.phase());
        assertTrue(boss.enraged());
        boss.health = boss.maxHealth * .2;
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertEquals(2, boss.phase());
    }

    @Test
    void harpoonsAreAimAssistedTowardsFlyingEnemies() {
        var run = TestRuns.of(DiverClass.HARPOONER);
        TestRuns.empty(run);
        var drone = TestRuns.place(run, EnemyKind.DRONE, run.player.x + 400);
        drone.actionCooldown = 99;
        TestRuns.tick(run, ATTACK, 20);
        var harpoon = run.projectiles().stream().filter(Projectile::friendly).findFirst();
        assertTrue(harpoon.isPresent() || drone.health() < drone.maxHealth());
        TestRuns.seconds(run, InputFrame.NONE, 1);
        assertTrue(drone.health() < drone.maxHealth());
    }

    @Test
    void explosiveCratesDamageNearbyEnemies() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var crate = new SupplyCrate(900, GameRun.FLOOR, SupplyCrate.Kind.EXPLOSIVE);
        run.crates.add(crate);
        var enemy = TestRuns.place(run, EnemyKind.SCUTTLER, 960);
        crate.hit(999);
        run.breakCrate(crate);
        assertTrue(enemy.health() < enemy.maxHealth());
        assertTrue(enemy.statuses().active(Status.BURN));
    }

    @Test
    void everyActiveModuleCanBeUsedWithEnoughEnergy() {
        for (var module : ActiveModule.values()) {
            var setup =
                    new RunSetup(
                            1,
                            DiverClass.MECHANIC,
                            Weapon.WRENCH,
                            module,
                            false,
                            0,
                            RunSetup.defaultItems(),
                            RunSetup.defaultWeapons(),
                            0,
                            0,
                            0);
            var run = new GameRun(setup);
            TestRuns.empty(run);
            TestRuns.place(run, EnemyKind.SENTINEL, run.player.x + 200);
            double energy = run.player.energy;
            run.update(TestRuns.STEP, InputFrame.builder().ability().build());
            assertTrue(run.player.energy < energy, module + " verbraucht Energie");
            assertTrue(run.player.abilityCooldown() > 0, module + " hat eine Abklingzeit");
            assertDoesNotThrow(() -> TestRuns.seconds(run, InputFrame.NONE, 3), module.name());
        }
    }

    @Test
    void allEnemyKindsAndBossesSurviveAMinuteOfSimulationWithoutErrors() {
        for (var kind : EnemyKind.values()) {
            var run = TestRuns.standard();
            TestRuns.empty(run);
            var enemy = TestRuns.place(run, kind, 1100);
            enemy.actionCooldown = 0;
            run.player.health = 1e6;
            run.player.maxHealth = 1e6;
            assertDoesNotThrow(
                    () -> {
                        for (int i = 0; i < 120 * 60 && run.phase() == GameRun.Phase.RUNNING; i++) {
                            run.player.invulnerableTime = 0;
                            run.update(TestRuns.STEP, InputFrame.NONE);
                        }
                    },
                    kind.name());
        }
    }
}
