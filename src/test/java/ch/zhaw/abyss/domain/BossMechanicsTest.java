package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Erlernbare Bossmechaniken und die Prismenkaiserin. */
class BossMechanicsTest {

    private static GameRun arena() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.health = run.player.maxHealth = 1e6;
        return run;
    }

    private static Enemy boss(GameRun run, EnemyKind kind, int pattern) {
        var boss = TestRuns.place(run, kind, 1100);
        boss.maxHealth = boss.health = 1e7;
        boss.state = Enemy.State.APPROACH;
        boss.actionCooldown = 0;
        boss.attacks = pattern;
        return boss;
    }

    /** Lässt die Simulation laufen, bis der Boss das gewünschte Muster ausholt. */
    private static void until(GameRun run, Enemy boss, int pattern, Enemy.State state) {
        for (int i = 0; i < 120 * 30; i++) {
            if (boss.pattern == pattern && boss.state == state) return;
            run.update(TestRuns.STEP, InputFrame.NONE);
        }
        fail("Muster " + pattern + " nicht erreicht");
    }

    @Test
    void theWardensAnchorPullsThePlayerInAndForcesASlam() {
        var run = arena();
        run.player.x = 600;
        var warden = boss(run, EnemyKind.WARDEN, 2);
        until(run, warden, WardenBrain.ANCHOR, Enemy.State.STRIKE);
        boolean pulled = false;
        for (int i = 0; i < 120 * 2 && warden.step != WardenBrain.HOOKED; i++) {
            run.update(TestRuns.STEP, InputFrame.NONE);
            pulled |= run.player.lungeVelocity > 100;
        }
        assertEquals(WardenBrain.HOOKED, warden.step, "Anker trifft die stehende Figur");
        assertTrue(pulled, "Figur wird herangezogen");
        until(run, warden, WardenBrain.SLAM, Enemy.State.WINDUP);
        assertTrue(warden.stateTime < .4, "der Stampfer folgt sofort");
    }

    @Test
    void theBulkheadRainLeavesExactlyOneGap() {
        var run = arena();
        var warden = boss(run, EnemyKind.WARDEN, 0);
        warden.phase = 1;
        warden.attacks = 5;
        until(run, warden, WardenBrain.RAIN, Enemy.State.WINDUP);
        long columns = run.hazards.stream().filter(h -> h.kind() == Hazard.Kind.BARRAGE).count();
        assertEquals(6, columns, "sieben Schotts, eines fehlt");
    }

    @Test
    void ventingTheReactorDuringAMeltdownExposesItsCore() {
        var run = arena();
        var reactor = boss(run, EnemyKind.REACTOR, 0);
        reactor.phase = 2;
        reactor.attacks = 0;
        until(run, reactor, ReactorBrain.MELTDOWN, Enemy.State.WINDUP);
        assertTrue(ReactorBrain.vent(run));
        assertEquals(Enemy.State.STUNNED, reactor.state);
        assertFalse(reactor.armored(), "der Kern liegt frei");
        assertFalse(ReactorBrain.vent(run), "nur während der Kernschmelze");
    }

    @Test
    void anUnventedMeltdownHitsEveryoneWhoDoesNotDodge() {
        var run = arena();
        run.player.x = 200;
        var reactor = boss(run, EnemyKind.REACTOR, 0);
        reactor.phase = 2;
        until(run, reactor, ReactorBrain.MELTDOWN, Enemy.State.WINDUP);
        double before = run.player.health;
        until(run, reactor, ReactorBrain.MELTDOWN, Enemy.State.RECOVER);
        assertTrue(run.player.health < before, "Kernschmelze trifft über die ganze Arena");
    }

    @Test
    void broodEggsHatchIntoMitesUnlessSmashed() {
        var run = arena();
        run.player.invulnerableTime = 1e9;
        var brood = boss(run, EnemyKind.BROOD, 2);
        until(run, brood, BroodBrain.EGGS, Enemy.State.RECOVER);
        var eggs = run.enemies.stream().filter(e -> e.kind == EnemyKind.EGG).toList();
        assertEquals(3, eggs.size());
        run.combat.hitEnemy(eggs.getFirst(), 1e9, Combat.Source.MACHINE, 0, 0);
        TestRuns.seconds(run, InputFrame.NONE, 4);
        long mites =
                run.enemies.stream().filter(e -> e.alive() && e.kind == EnemyKind.MITE).count();
        assertEquals(6, mites, "zwei Eier schlüpfen zu je drei Milben");
        assertTrue(run.enemies.stream().noneMatch(e -> e.alive() && e.kind == EnemyKind.EGG));
    }

    @Test
    void inkDarkensTheArenaAndTheWhirlPullsThePlayer() {
        var run = arena();
        var brood = boss(run, EnemyKind.BROOD, 0);
        brood.phase = 1;
        until(run, brood, BroodBrain.INK, Enemy.State.WINDUP);
        assertTrue(run.ink() > .9, "Tintenwolke");
        // Nach dem Tintenstoss zählt der Zyklus weiter; drei plus eins ergibt den Sog.
        brood.attacks = 3;
        until(run, brood, BroodBrain.WHIRL, Enemy.State.STRIKE);
        assertNotEquals(0, run.pullOn(run.player), "Sog zieht zur Brutmutter");
    }

    @Test
    void torpedosSteeredIntoTheCaptainStunHim() {
        var run = arena();
        var captain = boss(run, EnemyKind.CAPTAIN, 0);
        var torpedo =
                run.shoot(
                        Projectile.Kind.TORPEDO,
                        false,
                        1300,
                        captain.centerY(),
                        -400,
                        0,
                        10,
                        16,
                        5);
        torpedo.wreck = true;
        torpedo.age = 1;
        captain.state = Enemy.State.APPROACH;
        double before = captain.health;
        for (int i = 0; i < 120 && torpedo.life > 0; i++)
            run.update(TestRuns.STEP, InputFrame.NONE);
        assertTrue(captain.health < before, "eigener Torpedo trifft den Lotsen");
        assertEquals(Enemy.State.STUNNED, captain.state);
    }

    @Test
    void crossfireComesFromBothWallsOnTwoHeights() {
        var run = arena();
        var captain = boss(run, EnemyKind.CAPTAIN, 0);
        captain.phase = 1;
        until(run, captain, CaptainBrain.CROSSFIRE, Enemy.State.WINDUP);
        assertEquals(4, run.lances.size());
        assertEquals(2, run.lances.stream().mapToDouble(l -> l.y).distinct().count());
        assertTrue(run.lances.stream().anyMatch(l -> l.x < 100));
        assertTrue(run.lances.stream().anyMatch(l -> l.x > run.layout().width() - 100));
    }

    @Test
    void theEmpressRulesTheBridgeFromTheSecondCycle() {
        assertEquals(
                EnemyKind.CAPTAIN,
                new RoomGenerator(5, 0).room(23, 0).waves().getFirst().getFirst().kind());
        var bridge = new RoomGenerator(5, 1).room(23, 0);
        assertEquals(EnemyKind.EMPRESS, bridge.waves().getFirst().getFirst().kind());
        assertTrue(EnemyKind.EMPRESS.boss() && EnemyKind.EMPRESS.flying());
        assertTrue(bridge.fixtures().stream().noneMatch(f -> f.kind() == Fixture.Kind.LASER));
    }

    @Test
    void lanceRowsLeaveAGapAtTheFloorInTheFirstPhase() {
        var run = arena();
        var empress = boss(run, EnemyKind.EMPRESS, 1);
        until(run, empress, EmpressBrain.LANCES, Enemy.State.WINDUP);
        assertFalse(run.lances.isEmpty());
        double standingTop = GameRun.FLOOR - Player.HEIGHT;
        var firstWave = run.lances.stream().filter(l -> l.delay <= 1.0 + 1e-9).toList();
        assertFalse(firstWave.isEmpty());
        for (var lance : firstWave)
            assertTrue(lance.y + 11 < standingTop, "erste Welle verschont die stehende Figur");
    }

    @Test
    void theSunDanceSpinsLiveBeamsAroundTheEmpress() {
        var run = arena();
        var empress = boss(run, EnemyKind.EMPRESS, 5);
        until(run, empress, EmpressBrain.SUN_DANCE, Enemy.State.STRIKE);
        TestRuns.seconds(run, InputFrame.NONE, .6);
        assertEquals(6, run.beams().size());
        assertTrue(run.beams().stream().allMatch(Beam::live));
        double angle =
                Math.atan2(
                        run.beams().getFirst().y2() - run.beams().getFirst().y1(),
                        run.beams().getFirst().x2() - run.beams().getFirst().x1());
        TestRuns.seconds(run, InputFrame.NONE, .5);
        double later =
                Math.atan2(
                        run.beams().getFirst().y2() - run.beams().getFirst().y1(),
                        run.beams().getFirst().x2() - run.beams().getFirst().x1());
        assertNotEquals(angle, later, 1e-3, "die Strahlen drehen sich");
    }

    @Test
    void theEmpressVanishesBrieflyAtEachPhaseChange() {
        var run = arena();
        run.player.invulnerableTime = 1e9;
        var empress = boss(run, EnemyKind.EMPRESS, 0);
        empress.health = empress.maxHealth * .6;
        boolean veiled = false;
        for (int i = 0; i < 120 * 12 && !veiled; i++) {
            run.update(TestRuns.STEP, InputFrame.NONE);
            veiled = empress.untargetable() && empress.pattern == EmpressBrain.ASCEND;
        }
        assertTrue(veiled, "Entrückung beim Phasenwechsel");
        assertEquals(0, run.combat.hitEnemy(empress, 1000, Combat.Source.MACHINE, 0, 0), 1e-9);
    }

    @Test
    void prismBoltsHangInTheAirAndThenSurgeAtThePlayer() {
        var run = arena();
        run.player.invulnerableTime = 1e9;
        var empress = boss(run, EnemyKind.EMPRESS, 0);
        until(run, empress, EmpressBrain.BOLTS, Enemy.State.STRIKE);
        TestRuns.seconds(run, InputFrame.NONE, .5);
        var bolt =
                run.projectiles.stream()
                        .filter(q -> q.kind == Projectile.Kind.PRISM)
                        .findFirst()
                        .orElseThrow();
        double slow = Math.hypot(bolt.vx, bolt.vy);
        TestRuns.seconds(run, InputFrame.NONE, 1);
        assertTrue(Math.hypot(bolt.vx, bolt.vy) > slow * 3, "schnellt auf die Figur zu");
    }

    @Test
    void theEmpressGrowsFuriousIfTheFightDragsOn() {
        var run = arena();
        run.player.invulnerableTime = 1e9;
        var empress = boss(run, EnemyKind.EMPRESS, 0);
        empress.animationTime = EmpressBrain.FURY + 1;
        TestRuns.seconds(run, InputFrame.NONE, 4);
        assertTrue(empress.furious());
    }

    @Test
    void bossesInLaterCyclesUnleashBulletPatterns() {
        var base = RunSetup.standard(7, DiverClass.MECHANIC);
        var run = new GameRun(base);
        for (int cycle = 0; cycle < 1; cycle++) {
            for (int guard = 0; guard < 60 && run.phase() != GameRun.Phase.VICTORY; guard++) {
                TestRuns.clear(run);
                if (run.phase() == GameRun.Phase.ROOM_CLEARED) run.chooseNextRoom(0);
            }
            run.nextCycle();
        }
        assertEquals(1, run.cycle());
        TestRuns.empty(run);
        run.player.health = run.player.maxHealth = 1e6;
        var warden = boss(run, EnemyKind.WARDEN, 1);
        until(run, warden, WardenBrain.RIVETS, Enemy.State.RECOVER);
        assertTrue(
                run.projectiles.stream().filter(q -> q.kind == Projectile.Kind.BOLT).count() >= 8,
                "Nietenring");
    }
}
