package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

/** Bewegung, Sprünge, Laufstege und Ausweichen der spielenden Figur. */
class MovementTest {
    private static final InputFrame RIGHT = InputFrame.builder().right().build();

    @Test
    void walkingIsBoundedByTheBulkheads() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        TestRuns.seconds(run, InputFrame.builder().left().build(), 2);
        assertEquals(RoomLayout.WALL + run.player.width / 2, run.player.x, 1e-6);
        TestRuns.seconds(run, RIGHT, 12);
        assertEquals(
                run.layout().width() - RoomLayout.WALL - run.player.width / 2, run.player.x, 1e-6);
    }

    @Test
    void heldJumpReachesHigherThanATappedJump() {
        double held = apex(true), tapped = apex(false);
        assertTrue(held > tapped + 40, "gehaltener Sprung: " + held + ", kurzer Sprung: " + tapped);
        assertTrue(held > 150, "Laufstege auf 150 Einheiten Höhe müssen erreichbar sein");
    }

    private static double apex(boolean hold) {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        double highest = run.player.y;
        run.update(TestRuns.STEP, InputFrame.builder().jump().build());
        for (int i = 0; i < 120; i++) {
            run.update(
                    TestRuns.STEP,
                    hold ? InputFrame.builder().holdJump().build() : InputFrame.NONE);
            highest = Math.min(highest, run.player.y);
        }
        return GameRun.FLOOR - highest;
    }

    @Test
    void landingOnAPlatformAndDroppingThroughIt() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var platform = new Platform(100, 470, 400);
        var layout =
                new RoomLayout(run.layout().width(), List.of(platform), run.layout().rewardX());
        run.player.x = 300;
        run.player.y = 400;
        run.player.vy = 0;
        run.player.grounded = false;
        for (int i = 0; i < 60; i++) Physics.move(run.player, layout, TestRuns.STEP);
        assertEquals(470, run.player.y, 1e-6);
        assertTrue(run.player.grounded);
        assertSame(platform, run.player.platform);
        run.player.dropTime = .28;
        for (int i = 0; i < 60; i++) Physics.move(run.player, layout, TestRuns.STEP);
        assertEquals(GameRun.FLOOR, run.player.y, 1e-6);
    }

    @Test
    void platformsCanBeJumpedThroughFromBelow() {
        var layout = new RoomLayout(1600, List.of(new Platform(100, 470, 400)), 800);
        var run = TestRuns.standard();
        run.player.x = 300;
        run.player.y = GameRun.FLOOR;
        run.player.vy = -900;
        run.player.grounded = false;
        double highest = run.player.y;
        for (int i = 0; i < 40; i++) {
            Physics.move(run.player, layout, TestRuns.STEP);
            highest = Math.min(highest, run.player.y);
        }
        assertTrue(highest < 470, "springt durch den Steg nach oben");
    }

    @Test
    void airJumpRequiresTheCompressedAirItem() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.update(TestRuns.STEP, InputFrame.builder().jump().build());
        TestRuns.tick(run, InputFrame.builder().holdJump().build(), 30);
        double before = run.player.vy;
        run.update(TestRuns.STEP, InputFrame.builder().jump().build());
        assertTrue(run.player.vy >= before, "ohne Düse kein zweiter Sprung");
        run.player.install(Item.JETPACK);
        run.update(TestRuns.STEP, InputFrame.builder().jump().build());
        assertTrue(run.player.vy < -700, "mit Düse ein Luftsprung");
    }

    @Test
    void dashGrantsInvulnerabilityAndHasACooldown() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.update(TestRuns.STEP, InputFrame.builder().right().dash().build());
        assertTrue(run.player.dashTime() > 0);
        assertTrue(run.player.invulnerableTime() > .2);
        double health = run.player.health;
        assertFalse(run.combat.hurtPlayer(50, run.player.x + 30, null, true));
        assertEquals(health, run.player.health);
        TestRuns.seconds(run, InputFrame.NONE, .3);
        double cooldown = run.player.dashCooldown();
        assertTrue(cooldown > 0);
        run.update(TestRuns.STEP, InputFrame.builder().dash().build());
        assertEquals(0, run.player.dashTime());
    }

    @Test
    void coolantShortensDashCooldown() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.update(TestRuns.STEP, InputFrame.builder().dash().build());
        double normal = run.player.dashCooldown();
        var cooled = TestRuns.standard();
        TestRuns.empty(cooled);
        cooled.player.install(Item.COOLANT);
        cooled.update(TestRuns.STEP, InputFrame.builder().dash().build());
        assertTrue(cooled.player.dashCooldown() < normal);
    }

    @Test
    void enemiesDropThroughPlatformsToReachAPlayerBelow() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var sentinel = TestRuns.place(run, EnemyKind.SENTINEL, 900);
        sentinel.y = 470;
        sentinel.platform = new Platform(800, 470, 300);
        sentinel.grounded = true;
        run.player.x = 900;
        Behaviors.SENTINEL.update(sentinel, run, TestRuns.STEP);
        assertTrue(sentinel.dropTime > 0);
    }
}
