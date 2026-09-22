package ch.zhaw.abyss.qa;

import ch.zhaw.abyss.domain.*;

import java.util.Comparator;

/**
 * Automatischer Testspieler. Verwendet nur die öffentlichen Spieleingaben und Belohnungsaktionen.
 */
public final class CampaignPilot {
    private CampaignPilot() {}

    public static InputFrame input(GameRun run) {
        Player player = run.player();
        Enemy enemy =
                run.enemies().stream()
                        .filter(Enemy::alive)
                        .min(Comparator.comparingDouble(e -> Math.abs(e.x() - player.x())))
                        .orElse(null);
        if (enemy == null) return InputFrame.NONE;
        double dx = enemy.x() - player.x();
        boolean approach = Math.abs(dx) > 110;
        boolean turn = Math.signum(dx) != player.facing();
        boolean left = dx < 0 && (approach || turn), right = dx > 0 && (approach || turn);
        boolean threatened =
                run.enemies().stream()
                        .anyMatch(
                                e ->
                                        e.alive()
                                                && e.state() == Enemy.State.WINDUP
                                                && e.stateTime() < .23
                                                && (Math.abs(e.x() - player.x()) < 330
                                                        || e.kind().boss()));
        boolean bolt =
                run.projectiles().stream()
                        .anyMatch(q -> !q.friendly() && Math.abs(q.x() - player.x()) < 150);
        boolean hazard =
                run.hazards().stream()
                        .anyMatch(h -> h.warning() && Math.abs(h.x() - player.x()) < 110);
        boolean jump = (threatened || bolt || hazard) && player.grounded();
        boolean dash = threatened && player.dashCooldown() <= 0 && !enemy.kind().boss();
        double range = player.module() == ActiveModule.ARC ? 900 : 270;
        boolean ability =
                Math.abs(dx) < range
                        && player.abilityCooldown() <= 0
                        && player.energy() >= player.module().cost();
        return new InputFrame(
                left, right, jump, dash, Math.abs(dx) < 170, ability, dx < 0 ? -1 : 1);
    }

    public static boolean advance(GameRun run, int preferredBranch) {
        if (run.phase() != GameRun.Phase.ROOM_CLEARED) return false;
        run.repair();
        if (run.rewardAvailable()) {
            if (run.rewardOffers().isEmpty()) run.claimSupplies();
            else
                run.claimReward(
                        run.rewardOffers().stream()
                                .max(Comparator.comparingInt(u -> priority(run.player(), u)))
                                .orElseThrow());
        }
        return run.chooseNextRoom(Math.min(preferredBranch, run.nextRooms().size() - 1));
    }

    private static int priority(Player player, Upgrade upgrade) {
        return switch (upgrade) {
            case MEDICAL -> player.health() / player.maxHealth() < .65 ? 100 : 65;
            case RECOVERY -> player.stacks(Upgrade.RECOVERY) < 2 ? 95 : 60;
            case SERVO -> 85;
            case PLATING -> 80;
            case CAPACITOR -> 70;
            case COOLANT -> 55;
            case REGEN -> player.stacks(Upgrade.REGEN) < 2 ? 92 : 60;
            case LANCE -> 62;
            case OVERCLOCK -> 78;
            case THRUSTER -> 40;
            case SIPHON -> 68;
            case ARC_COIL -> 75;
        };
    }
}
