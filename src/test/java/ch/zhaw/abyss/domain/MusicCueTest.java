package ch.zhaw.abyss.domain;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.infrastructure.music.MusicDirector;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

/** Musikregie: welches Stück, wie intensiv, wie gedämpft – abgeleitet aus dem Spielzustand. */
class MusicCueTest {

    @Test
    void menusPlayTheTitleTheme() {
        assertEquals("title", MusicDirector.cue(null, true, 0).song());
        assertEquals("title", MusicDirector.cue(TestRuns.standard(), true, .7).song());
    }

    @Test
    void theSternPlaysItsSectorTrackAndGrowsWithTheSwarm() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var calm = MusicDirector.cue(run, false, 0);
        assertEquals("hold", calm.song());
        assertEquals(.45, calm.intensity(), 1e-9);
        TestRuns.place(run, EnemyKind.SCUTTLER, 900);
        double few = MusicDirector.cue(run, false, 0).intensity();
        for (int i = 0; i < 60; i++) TestRuns.place(run, EnemyKind.SCUTTLER, 700 + i * 5);
        double many = MusicDirector.cue(run, false, 0).intensity();
        assertTrue(few >= .5 && few < many, few + " / " + many);
        assertTrue(many > .9, "sechzig Gegner bringen fast volle Besetzung: " + many);
    }

    @Test
    void aClearedRoomFallsBackToTheQuietLayer() {
        var run = TestRuns.standard();
        TestRuns.clear(run);
        assertEquals(GameRun.Phase.ROOM_CLEARED, run.phase());
        assertEquals(.3, MusicDirector.cue(run, false, 0).intensity(), 1e-9);
    }

    @Test
    void wardensDriveTheBossTrackPhaseByPhase() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var warden = TestRuns.place(run, EnemyKind.WARDEN, 1100);
        var first = MusicDirector.cue(run, false, 0);
        assertEquals("boss", first.song());
        warden.phase = 2;
        var last = MusicDirector.cue(run, false, 0);
        assertTrue(last.intensity() > first.intensity());
        assertTrue(last.intensity() >= .9, "letzte Phase mit voller Wucht");
    }

    @Test
    void theEmpressHasHerOwnThemeAndSpeedsUpInFury() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        var empress = TestRuns.place(run, EnemyKind.EMPRESS, 1100);
        assertEquals("empress", MusicDirector.cue(run, false, 0).song());
        assertEquals(1, MusicDirector.cue(run, false, 0).tempo(), 1e-9);
        empress.furious = true;
        var fury = MusicDirector.cue(run, false, 0);
        assertTrue(fury.tempo() > 1);
        assertEquals(1, fury.intensity(), 1e-9);
    }

    @Test
    void escalatedRoomsSwitchToTheAbyss() {
        var base = TestRuns.standard().checkpoint();
        var deep =
                new RunCheckpoint(
                        base.seed(),
                        1,
                        base.pressure(),
                        base.depth(),
                        base.branch(),
                        base.diver(),
                        base.weapon(),
                        base.weaponLevel(),
                        base.module(),
                        base.explorer(),
                        base.health(),
                        base.energy(),
                        base.salvage(),
                        base.repairKits(),
                        base.items(),
                        base.kills(),
                        base.elapsed(),
                        base.route(),
                        base.cores(),
                        base.reviveUsed(),
                        base.rushStacks(),
                        base.healthPenalty(),
                        base.bonusHealth(),
                        base.level(),
                        base.xp(),
                        base.pendingLevelUps());
        var pool = EnumSet.noneOf(Item.class);
        pool.addAll(Item.lootable());
        var run = GameRun.restore(deep, pool, EnumSet.allOf(Weapon.class));
        assertTrue(run.escalation() > 0);
        TestRuns.empty(run);
        assertEquals("abyss", MusicDirector.cue(run, false, 0).song());
    }

    @Test
    void menusAndLowHealthMuffleTheMusic() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        assertEquals(0, MusicDirector.cue(run, false, 0).muffle(), 1e-9);
        assertEquals(.72, MusicDirector.cue(run, false, .72).muffle(), 1e-9);
        run.player.health = run.player.maxHealth * .1;
        assertTrue(MusicDirector.cue(run, false, 0).muffle() > .3);
    }

    @Test
    void defeatDropsToTheGroundLayerUnderWater() {
        var run = TestRuns.standard();
        TestRuns.empty(run);
        run.player.health = 0;
        run.update(TestRuns.STEP, InputFrame.NONE);
        assertEquals(GameRun.Phase.DEFEAT, run.phase());
        var cue = MusicDirector.cue(run, false, 0);
        assertEquals(0, cue.intensity(), 1e-9);
        assertTrue(cue.muffle() > .8);
    }
}
