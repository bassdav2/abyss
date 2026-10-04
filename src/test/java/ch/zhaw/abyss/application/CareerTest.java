package ch.zhaw.abyss.application;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.application.SkillTree.SkillNode;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.MetaBonus;
import ch.zhaw.abyss.domain.Rarity;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.domain.StatSheet;
import ch.zhaw.abyss.domain.Weapon;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Laufbahn: Ränge, Punkte, Skill-Bäume, Waffenmeisterschaft und Startvorteile. */
class CareerTest {

    private static SkillNode node(String id) {
        return SkillNode.find(id).orElseThrow();
    }

    private static long xpForRank(int rank) {
        long sum = 0;
        for (int r = 0; r < rank; r++) sum += Career.xpForRank(r);
        return sum;
    }

    private static Profile with(Profile profile, Career career) {
        var edit = profile.edit();
        edit.career = career;
        return edit.build();
    }

    @Test
    void ranksGrowWithExperienceAndGetSlowerLater() {
        assertEquals(0, Career.rankFor(0));
        assertEquals(0, Career.rankFor(399));
        assertEquals(1, Career.rankFor(400));
        assertEquals(10, Career.rankFor(xpForRank(10)));
        assertEquals(9, Career.rankFor(xpForRank(10) - 1));
        assertTrue(Career.xpForRank(20) > Career.xpForRank(2) * 3);
        assertEquals(.5, Career.rankProgress(200), 1e-9);
    }

    @Test
    void catalogIsConsistent() {
        var ids = new HashSet<String>();
        for (var node : SkillNode.catalog()) {
            assertTrue(ids.add(node.id()), "eindeutige Kennung " + node.id());
            for (String required : node.requires()) {
                var before = node(required);
                assertEquals(node.tree(), before.tree(), "Voraussetzung im selben Baum");
                assertTrue(before.column() < node.column(), "Äste wachsen nach rechts");
            }
        }
        for (var tree : SkillTree.values()) assertTrue(tree.nodes().size() >= 8, tree.name());
        for (var weapon : Weapon.values())
            if (weapon.career())
                assertTrue(
                        SkillNode.catalog().stream().anyMatch(n -> n.weapon() == weapon),
                        "Laufbahnwaffe hat einen Knoten: " + weapon);
    }

    @Test
    void treesOpenThroughProgress() {
        var fresh = Profile.fresh();
        assertTrue(SkillTree.DEPTH.open(fresh));
        assertFalse(SkillTree.ARSENAL.open(fresh));
        assertFalse(SkillTree.ABYSS.open(fresh));
        assertTrue(SkillTree.MECHANIC.open(fresh), "Startklasse hat ihren Baum");
        assertFalse(SkillTree.TITAN.open(fresh), "gesperrte Klasse, gesperrter Baum");
        var nodes = Set.of("d.dmg1", "d.dmg2", "d.xp1", "d.magnet", "d.hp1");
        var veteran = with(fresh, new Career(xpForRank(20), Map.of(), Map.of(), nodes));
        assertTrue(SkillTree.ARSENAL.open(veteran));
        var edit = veteran.edit();
        edit.wins = 1;
        assertTrue(SkillTree.ABYSS.open(edit.build()));
    }

    @Test
    void accountTreesSharePointsAndClassTreesUseTheirOwnRank() {
        var career =
                new Career(
                        xpForRank(3),
                        Map.of(DiverClass.MECHANIC, xpForRank(2)),
                        Map.of(),
                        Set.of());
        var profile = with(Profile.fresh(), career);
        assertEquals(3, career.points(SkillTree.DEPTH));
        assertEquals(3, career.points(SkillTree.ABYSS), "Kontobäume teilen sich die Punkte");
        assertEquals(2, career.points(SkillTree.MECHANIC));
        assertEquals(0, career.points(SkillTree.TITAN));
        assertFalse(career.canBuy(node("d.dmg2"), profile), "Voraussetzung fehlt");
        assertTrue(career.canBuy(node("d.dmg1"), profile));
        career = career.with(node("d.dmg1"));
        assertEquals(2, career.points(SkillTree.DEPTH));
        assertEquals(2, career.points(SkillTree.MECHANIC), "Klassenpunkte unberührt");
        profile = with(profile, career);
        assertTrue(career.canBuy(node("d.dmg2"), profile));
        assertFalse(career.canBuy(node("d.dmg1"), profile), "nicht doppelt");
        assertFalse(career.canBuy(node("a.forge1"), profile), "Arsenal noch geschlossen");
    }

    @Test
    void classNodesOnlyStrengthenTheirOwnClass() {
        var career = new Career(0, Map.of(), Map.of(), Set.of("m.3", "d.dmg1", "d.hp1"));
        assertEquals(1.18, career.bonus(DiverClass.MECHANIC).damage(), 1e-9);
        assertEquals(1.08, career.bonus(DiverClass.TITAN).damage(), 1e-9);
        assertEquals(15, career.bonus(DiverClass.TITAN).health(), 1e-9);
    }

    @Test
    void capstonesUnlockCareerWeaponsThatTheArchiveDoesNotSell() {
        var profile = Profile.fresh();
        assertFalse(profile.owns(Weapon.DRILL));
        assertTrue(
                Unlock.catalog().stream().noneMatch(u -> u.id().equals(Unlock.of(Weapon.DRILL))));
        profile = with(profile, new Career(0, Map.of(), Map.of(), Set.of("m.cap")));
        assertTrue(profile.owns(Weapon.DRILL));
        assertTrue(profile.weaponPool(DiverClass.MECHANIC).contains(Weapon.DRILL));
    }

    @Test
    void killsWithAWeaponRaiseItsMasteryAndDamage() {
        var career = Career.NEW.gain(0, DiverClass.MECHANIC, Map.of(Weapon.WRENCH, 450));
        assertEquals(2, career.mastery(Weapon.WRENCH));
        assertEquals(0, career.mastery(Weapon.KNIVES));
        var faster =
                new Career(0, Map.of(), Map.of(), Set.of("a.master"))
                        .gain(0, DiverClass.MECHANIC, Map.of(Weapon.WRENCH, 450));
        assertTrue(faster.weaponXp().get(Weapon.WRENCH) > career.weaponXp().get(Weapon.WRENCH));
        var items = new EnumMap<Item, Integer>(Item.class);
        var plain = StatSheet.compute(DiverClass.MECHANIC, Weapon.WRENCH, 0, items, 0);
        var mastered =
                StatSheet.compute(
                        DiverClass.MECHANIC,
                        Weapon.WRENCH,
                        0,
                        items,
                        0,
                        career.bonus(DiverClass.MECHANIC));
        assertEquals(plain.damage() * 1.08, mastered.damage(), 1e-9);
    }

    @Test
    void startBonusesShapeTheFirstRoom() {
        var meta =
                new MetaBonus(
                        1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 1, 2, 2, 1, 1, Map.of());
        var run = new GameRun(RunSetup.standard(9, DiverClass.MECHANIC).withMeta(meta));
        assertEquals(2, run.player().weaponLevel());
        assertEquals(2, run.pendingLevelUps());
        assertFalse(run.levelOffers().isEmpty());
        assertTrue(
                run.player().items().keySet().stream().anyMatch(i -> i.rarity() == Rarity.RARE),
                "seltenes Startmodul");
        assertEquals(2, run.checkpoint().weaponLevel(), "Sicherung enthält die Startvorteile");
        assertEquals(1, run.orbitals().length / 2, "Kreiselmesser aus der Laufbahn");
    }

    @Test
    void endgameKillCountsAreCreditedWithDiminishingReturns() {
        assertEquals(300, GameService.killCredit(300));
        assertEquals(500, GameService.killCredit(500));
        long huge = GameService.killCredit(80_000);
        assertTrue(huge > 4000 && huge < 8000, "zehntausende Abschüsse: viel, aber begrenzt");
        long previous = 0;
        for (int kills = 0; kills < 20_000; kills += 250) {
            assertTrue(GameService.killCredit(kills) >= previous);
            previous = GameService.killCredit(kills);
        }
    }

    @Test
    void finishedRunsGrantCareerExperienceAndNodesCanBeBought() {
        var repository = new GameServiceTest.MemoryRepository();
        var service = new GameService(repository);
        var run = service.start(Loadout.DEFAULT, 77);
        for (int tick = 0; tick < 120 * 600 && run.phase() == GameRun.Phase.RUNNING; tick++) {
            run.update(1.0 / 120, ch.zhaw.abyss.domain.InputFrame.NONE);
        }
        assertEquals(GameRun.Phase.DEFEAT, run.phase(), "ohne Eingaben endet der Tauchgang");
        service.recordOutcome();
        var career = service.profile().career();
        assertTrue(career.xp() > 0, "Tiefe zählt auch bei einer Niederlage");
        assertEquals(career.xp(), career.xp(DiverClass.MECHANIC));
        assertEquals(career.xp(), service.lastCareer().xp());
        var boosted = service.profile().edit();
        boosted.career = new Career(xpForRank(4), Map.of(), Map.of(), Set.of());
        repository.profile = boosted.build();
        var fresh = new GameService(repository);
        assertTrue(fresh.purchase(node("d.dmg1")));
        assertTrue(fresh.profile().career().nodes().contains("d.dmg1"));
        assertFalse(fresh.purchase(node("d.dmg1")));
        assertEquals(1.08, fresh.profile().meta(DiverClass.MECHANIC).damage(), 1e-9);
    }
}
