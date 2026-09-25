package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.domain.Enemy;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Hazard;
import ch.zhaw.abyss.domain.InputFrame;
import ch.zhaw.abyss.domain.Interaction;
import ch.zhaw.abyss.domain.Item;
import ch.zhaw.abyss.domain.Offer;
import ch.zhaw.abyss.domain.Projectile;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.ShrineDeal;
import ch.zhaw.abyss.domain.Telegraph;
import ch.zhaw.abyss.domain.Weapon;

import java.util.Comparator;

/**
 * Automatischer Testspieler. Verwendet nur öffentliche Eingaben und Anwendungsaktionen: laufen,
 * springen, ausweichen, angreifen, Modul, Reparaturset, Bergung und Routenwahl. Dient
 * Kampagnensimulationen, Render-Probeläufen und Demoaufnahmen.
 */
public final class CampaignPilot {
    private static final double FLOOR = GameRun.FLOOR;

    private CampaignPilot() {}

    /**
     * Entscheidet die Eingabe für den nächsten Schritt.
     *
     * @param run Tauchgang
     * @return Eingabe
     */
    public static InputFrame input(GameRun run) {
        var p = run.player();
        var b = InputFrame.builder();
        if (p.vy() < 0) b.holdJump();
        if (p.health() < p.maxHealth() * .4) b.heal();
        Enemy target =
                run.enemies().stream()
                        .filter(e -> e.alive() && !e.untargetable())
                        .min(
                                Comparator.comparingDouble(
                                        e ->
                                                Math.abs(e.x() - p.x())
                                                        + Math.abs(e.y() - p.y()) * .6))
                        .orElse(null);
        if (target == null) {
            if (run.phase() == GameRun.Phase.RUNNING
                    && run.enemies().stream().anyMatch(Enemy::alive))
                b.move(p.x() < run.layout().width() / 2 ? 1 : -1);
            return b.build();
        }
        boolean ranged = p.weapon() == Weapon.HARPOON;
        double reach = ranged ? 700 : p.attackReach() * .85;
        double dx = target.x() - p.x();
        double dy = target.y() - p.y();
        int toward = dx >= 0 ? 1 : -1;
        boolean flying = target.kind().flying();
        if (ranged) {
            if (Math.abs(dx) > 520) b.move(toward);
            else if (Math.abs(dx) < 200) b.move(-toward);
        } else if (Math.abs(dx) > reach * .7) b.move(toward);
        boolean verticalOk = Math.abs(target.centerY() - p.centerY()) < (flying ? 110 : 90);
        if (Math.abs(dx) < reach && verticalOk) {
            b.attack();
            b.aim(toward);
        }
        if (p.grounded() && target.centerY() < p.y() - 150 && Math.abs(dx) < 260) b.jump();
        if (p.grounded() && dy > 60 && target.grounded() && Math.abs(dx) < 400) {
            b.down();
            b.jump();
        }
        if (threatened(run)) {
            if (p.dashCooldown() <= 0) {
                b.dash();
                b.move(-toward);
            } else if (p.grounded()) b.jump();
        }
        if (lowDanger(run) && p.grounded()) b.jump();
        if (highBeam(run)) b.move(toward);
        if (p.abilityCooldown() <= 0
                && p.energy() >= p.module().cost()
                && Math.abs(dx) < (ranged ? 700 : 360)) b.ability();
        int dodge = machineDanger(run);
        if (dodge != 0) {
            b.move(dodge);
            if (p.dashCooldown() <= 0) b.dash();
        }
        if (run.interaction() == Interaction.CONSOLE && run.enemies().size() >= 2) run.useConsole();
        return b.build();
    }

    /** Weicht Pressen und Lasern aus, die gleich auslösen oder gerade wirken. */
    private static int machineDanger(GameRun run) {
        var p = run.player();
        for (var m : run.fixtures()) {
            if (!m.warning() && !m.active()) continue;
            double reach =
                    switch (m.kind()) {
                        case PRESS -> m.width() / 2 + 40;
                        case LASER -> 60;
                        default -> -1;
                    };
            if (reach > 0 && Math.abs(p.x() - m.x()) < reach) return p.x() < m.x() ? -1 : 1;
        }
        return 0;
    }

    private static boolean threatened(GameRun run) {
        var p = run.player();
        for (var e : run.enemies()) {
            if (!e.alive() || e.state() != Enemy.State.WINDUP || e.stateTime() > .2) continue;
            var t = e.telegraph();
            if (t == null) continue;
            if (t.shape() == Telegraph.Shape.CIRCLE
                    && Math.hypot(t.x1() - p.x(), t.y1() - p.centerY()) < t.x2() + 30) return true;
            if (t.shape() == Telegraph.Shape.FLOOR
                    && t.y1() > 0
                    && p.x() > Math.min(t.x1(), t.x2()) - 40
                    && p.x() < Math.max(t.x1(), t.x2()) + 40
                    && Math.abs(e.x() - p.x()) < 700) return true;
            if (t.shape() == Telegraph.Shape.AIM && Math.abs(e.x() - p.x()) < 260) return true;
        }
        for (var q : run.projectiles())
            if (!q.friendly()
                    && q.kind() != Projectile.Kind.SHOCKWAVE
                    && q.kind() != Projectile.Kind.MINE
                    && Math.abs(q.x() - p.x()) < 110
                    && Math.abs(q.y() - p.centerY()) < 90
                    && Math.signum(p.x() - q.x()) == Math.signum(q.vx())) return true;
        return false;
    }

    private static boolean lowDanger(GameRun run) {
        var p = run.player();
        for (var q : run.projectiles())
            if (!q.friendly()
                    && q.kind() == Projectile.Kind.SHOCKWAVE
                    && Math.abs(q.x() - p.x()) < 150
                    && Math.signum(p.x() - q.x()) == Math.signum(q.vx())) return true;
        for (var h : run.hazards())
            if ((h.warning() || h.active())
                    && (h.kind() == Hazard.Kind.ELECTRIC || h.kind() == Hazard.Kind.ACID)
                    && Math.abs(h.x() - p.x()) < h.width() / 2 + 40) return true;
        for (var e : run.enemies()) {
            var t = e.telegraph();
            if (t != null
                    && t.shape() == Telegraph.Shape.BEAM
                    && t.y1() > FLOOR - 100
                    && e.stateTime() < .25) return true;
            if (t != null
                    && t.shape() == Telegraph.Shape.FLOOR
                    && t.y1() == FLOOR
                    && Math.abs(t.x2() - t.x1()) > 800
                    && e.stateTime() < .1) return true;
        }
        return false;
    }

    private static boolean highBeam(GameRun run) {
        for (var e : run.enemies()) {
            var t = e.telegraph();
            if (t != null && t.shape() == Telegraph.Shape.BEAM && t.y1() < FLOOR - 100) return true;
        }
        return false;
    }

    /**
     * Nimmt Belohnungen und wählt den nächsten Raum.
     *
     * @param run Tauchgang
     * @param preferredBranch bevorzugter Abzweig
     * @return {@code true}, wenn der Raum gewechselt wurde
     */
    public static boolean advance(GameRun run, int preferredBranch) {
        if (run.phase() != GameRun.Phase.ROOM_CLEARED) return false;
        var p = run.player();
        run.repair();
        switch (run.room().kind()) {
            case SHRINE -> {
                if (run.deals().contains(ShrineDeal.SCRAP_FOR_HEALTH) && p.salvage() >= 40)
                    run.acceptDeal(ShrineDeal.SCRAP_FOR_HEALTH);
                else if (run.deals().contains(ShrineDeal.BLOOD_FOR_POWER)
                        && p.health() > p.maxHealth() * .8)
                    run.acceptDeal(ShrineDeal.BLOOD_FOR_POWER);
            }
            case MERCHANT, WORKSHOP -> {
                for (int round = 0; round < 6; round++) {
                    var best =
                            run.offers().stream()
                                    .filter(o -> o.price() <= p.salvage())
                                    .filter(o -> useful(run, o))
                                    .max(Comparator.comparingInt(o -> score(run, o)))
                                    .orElse(null);
                    if (best == null || !run.take(best)) break;
                }
            }
            default -> {
                if (run.rewardAvailable() && !run.offers().isEmpty())
                    run.offers().stream()
                            .max(Comparator.comparingInt(o -> score(run, o)))
                            .ifPresent(run::take);
            }
        }
        var choices = run.nextRooms();
        int branch = Math.min(preferredBranch, choices.size() - 1);
        if (choices.size() > 1 && p.health() < p.maxHealth() * .45) {
            for (int i = 0; i < choices.size(); i++)
                if (choices.get(i).kind() == RoomPlan.Kind.CACHE
                        || choices.get(i).kind() == RoomPlan.Kind.MERCHANT) branch = i;
            if (choices.get(branch).kind() == RoomPlan.Kind.ELITE) branch = 0;
        }
        return run.chooseNextRoom(branch);
    }

    private static boolean useful(GameRun run, Offer offer) {
        var p = run.player();
        return switch (offer.type()) {
            case REPAIR_KIT -> p.repairKits() < p.stats().maxRepairKits();
            case HEAL -> p.health() < p.maxHealth() * .7;
            default -> true;
        };
    }

    private static int score(GameRun run, Offer offer) {
        var p = run.player();
        return switch (offer.type()) {
            case ITEM -> priority(offer.item()) + offer.item().rarity().ordinal() * 15;
            case WEAPON ->
                    offer.weapon() == Weapon.KNIVES || offer.weapon() == Weapon.HARPOON ? 20 : 55;
            case WEAPON_UPGRADE -> 95;
            case REPAIR_KIT -> p.repairKits() == 0 ? 90 : 40;
            case HEAL -> p.health() < p.maxHealth() * .5 ? 85 : 10;
            case SUPPLIES -> 100;
        };
    }

    private static int priority(Item item) {
        return switch (item) {
            case SERVO, PLATING, MEDICAL, NANITES, SECOND_HEART, BARRIER -> 90;
            case RECOVERY, REGEN, OVERCLOCK, LENS, SHIELD_CELL, ADRENALINE -> 75;
            case ARC_COIL, CHAIN_REACTION, CRIT_DAMAGE, AMBUSH, LEVIATHAN_TOOTH -> 65;
            case GLASS_HULL, GREED, FEVER -> 0;
            default -> 45;
        };
    }
}
