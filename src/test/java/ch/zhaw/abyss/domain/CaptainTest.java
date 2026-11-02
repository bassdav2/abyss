package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Der Lotse als Ausweichkampf: Deckung, Phasensperre, Blinzeln und lernbare Muster. */
class CaptainTest {

    private static GameRun arena() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.health = run.player.maxHealth = 1e9;
        return run;
    }

    private static Enemy captain(GameRun run) {
        var captain = TestRuns.place(run, EnemyKind.CAPTAIN, 1100);
        captain.maxHealth = captain.health = 100_000;
        captain.actionCooldown = 99;
        return captain;
    }

    private static double hit(GameRun run, Enemy e, double damage) {
        return run.combat.hitEnemy(e, damage, Combat.Source.MELEE, 0, e.x - 50);
    }

    @Test
    void outsideItsWindowsTheGuardSwallowsEvenHugeHits() {
        var run = arena();
        var captain = captain(run);
        run.player.x = 400;
        run.update(TestRuns.STEP, InputFrame.NONE);
        hit(run, captain, 1e9);
        hit(run, captain, 1e9);
        assertTrue(
                captain.health >= captain.maxHealth * (1 - CaptainBrain.CLOSED_CAP - .001),
                "ausserhalb der Pause höchstens ein Hauch Schaden: " + captain.health);
    }

    @Test
    void aPunishWindowLetsAPortionThrough() {
        var run = arena();
        var captain = captain(run);
        run.player.x = 400;
        captain.state = Enemy.State.RECOVER;
        captain.stateTime = 2;
        run.update(TestRuns.STEP, InputFrame.NONE);
        hit(run, captain, 1e9);
        double lost = 1 - captain.health / captain.maxHealth;
        assertTrue(
                lost > CaptainBrain.OPEN_BURST * .9 && lost < CaptainBrain.OPEN_CAP,
                "Fenster: " + lost);
    }

    @Test
    void noPhaseCanBeSkippedAndHeVanishesBetweenThem() {
        var run = arena();
        var captain = captain(run);
        run.player.x = 400;
        captain.guard = captain.maxHealth * 10;
        captain.state = Enemy.State.RECOVER;
        captain.stateTime = 2;
        captain.exposedLast = true;
        hit(run, captain, 1e9);
        assertTrue(captain.health > captain.maxHealth * .66, "nicht über die Phasengrenze");
        assertTrue(captain.alive());
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertEquals(1, captain.phase);
        assertEquals(Enemy.State.HIDDEN, captain.state);
        assertTrue(captain.untargetable(), "beim Phasenwechsel nicht angreifbar");
        assertTrue(
                run.lances.stream().allMatch(l -> Math.abs(Math.sin(l.angle) - 1) < 1e-9),
                "er hinterlässt eine Ankerkette");
        assertFalse(run.lances.isEmpty());
        for (int i = 0; i < 120 * 3; i++) run.update(TestRuns.STEP, InputFrame.NONE);
        assertNotEquals(Enemy.State.HIDDEN, captain.state, "nach kurzer Zeit kehrt er zurück");
    }

    @Test
    void heBlinksAwayFromCloseCombat() {
        var run = arena();
        var captain = captain(run);
        run.player.x = captain.x - 90;
        captain.state = Enemy.State.APPROACH;
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertTrue(Math.abs(captain.x - run.player.x) >= 300, "weggeblinzelt");
        double x = captain.x;
        run.player.x = captain.x + 80;
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertEquals(x, captain.x, 30, "Blinzeln braucht eine Abklingzeit");
    }

    @Test
    void rhythmFireAlternatesWallsAndHeights() {
        var run = arena();
        var captain = captain(run);
        CaptainBrain.rhythm(captain, run);
        var lances = run.lances;
        assertEquals(7, lances.size());
        for (int i = 0; i < lances.size(); i++) {
            var l = lances.get(i);
            assertEquals(i % 2 == 0, l.x < 100, "Wände wechseln sich ab");
            assertEquals(i % 2 == 0 ? CaptainBrain.LOW : CaptainBrain.HIGH, l.y, 1e-9);
        }
        double standingTop = GameRun.FLOOR - Player.HEIGHT;
        assertTrue(CaptainBrain.HIGH + 11 < standingTop, "hohe Lanzen verschonen Stehende");
        assertTrue(CaptainBrain.LOW - 11 > standingTop, "tiefe Lanzen treffen Stehende");
    }

    @Test
    void theAnchorChainLeavesGapsWideEnoughToStandIn() {
        var run = arena();
        var captain = captain(run);
        CaptainBrain.anchors(captain, run, false);
        var xs = run.lances.stream().mapToDouble(l -> l.x).sorted().toArray();
        assertTrue(xs.length > 8);
        for (int i = 1; i < xs.length; i++)
            assertTrue(xs[i] - xs[i - 1] - 22 > 48, "Lücke zwischen zwei Säulen");
    }

    @Test
    void aMonsterBuildStillHasToSurviveAboutAMinute() {
        var run = arena();
        var captain = TestRuns.place(run, EnemyKind.CAPTAIN, 1100);
        captain.state = Enemy.State.SPAWNING;
        captain.stateTime = .1;
        captain.actionCooldown = 1;
        run.player.x = 300;
        double seconds = 0;
        for (int i = 0; i < 120 * 300 && captain.alive(); i++) {
            run.player.invulnerableTime = 1;
            if (!captain.untargetable()) hit(run, captain, 1e9);
            run.update(TestRuns.STEP, InputFrame.NONE);
            seconds += TestRuns.STEP;
        }
        assertFalse(captain.alive());
        assertTrue(seconds > 50, "Lotse fiel nach " + seconds + " s");
        assertTrue(seconds < 120, "Lotse hielt " + seconds + " s");
        assertEquals(2, captain.phase, "alle drei Phasen gesehen");
    }
}
