package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Laufbahnwaffen: Tiefenbohrer, Plasmawerfer und Tiefseesense. */
class CareerWeaponTest {

    private static GameRun armed(Weapon weapon) {
        var base = RunSetup.standard(3, DiverClass.MECHANIC);
        var run =
                new GameRun(
                        new RunSetup(
                                3,
                                base.diver(),
                                weapon,
                                base.module(),
                                false,
                                0,
                                base.itemPool(),
                                base.weaponPool(),
                                0,
                                0,
                                0));
        TestRuns.empty(run);
        run.player.invulnerableTime = 1e9;
        return run;
    }

    private static Enemy dummy(GameRun run, double x) {
        var enemy = TestRuns.place(run, EnemyKind.SENTINEL, x);
        enemy.state = Enemy.State.STUNNED;
        enemy.stateTime = 1e9;
        return enemy;
    }

    private static void attack(GameRun run, double seconds) {
        var input = InputFrame.builder().attack().build();
        for (int i = 0; i < seconds * 120; i++) run.update(TestRuns.STEP, input);
    }

    @Test
    void careerWeaponsAreOnlyAvailableThroughTheCareer() {
        for (var weapon : new Weapon[] {Weapon.DRILL, Weapon.PLASMA, Weapon.SCYTHE}) {
            assertTrue(weapon.career());
            assertFalse(weapon.startsUnlocked());
            assertFalse(RunSetup.defaultWeapons().contains(weapon));
        }
        assertFalse(Weapon.WRENCH.career());
    }

    @Test
    void plasmaShotsExplodeOnImpact() {
        var run = armed(Weapon.PLASMA);
        var target = dummy(run, run.player.x + 400);
        var bystander = dummy(run, run.player.x + 440);
        boolean seen = false;
        var input = InputFrame.builder().attack().build();
        for (int i = 0; i < 120 * 2; i++) {
            run.update(TestRuns.STEP, i < 30 ? input : InputFrame.NONE);
            seen |= run.projectiles.stream().anyMatch(q -> q.kind == Projectile.Kind.PLASMA);
        }
        assertTrue(seen, "Plasmakugel fliegt");
        assertTrue(target.health() < target.maxHealth());
        assertTrue(bystander.health() < bystander.maxHealth(), "Explosion trifft Nachbarn");
    }

    @Test
    void theScythesThirdSwingAlsoHitsBehind() {
        var run = armed(Weapon.SCYTHE);
        var behind = dummy(run, run.player.x - 120);
        run.player.facing = 1;
        attack(run, 1.6);
        assertTrue(behind.health() < behind.maxHealth(), "Rundumschwung trifft hinter der Figur");
    }

    @Test
    void theDrillHitsMoreOftenThanTheWrench() {
        var drill = armed(Weapon.DRILL);
        var wrench = armed(Weapon.WRENCH);
        var a = dummy(drill, drill.player.x + 120);
        var b = dummy(wrench, wrench.player.x + 120);
        drill.drainEvents();
        wrench.drainEvents();
        attack(drill, 2);
        attack(wrench, 2);
        long drillHits =
                drill.drainEvents().stream()
                        .filter(
                                e ->
                                        e.type() == GameEvent.Type.HIT
                                                || e.type() == GameEvent.Type.CRIT)
                        .count();
        long wrenchHits =
                wrench.drainEvents().stream()
                        .filter(
                                e ->
                                        e.type() == GameEvent.Type.HIT
                                                || e.type() == GameEvent.Type.CRIT)
                        .count();
        assertTrue(drillHits > wrenchHits * 1.5, drillHits + " gegen " + wrenchHits);
        assertTrue(a.health() < a.maxHealth() && b.health() < b.maxHealth());
    }
}
