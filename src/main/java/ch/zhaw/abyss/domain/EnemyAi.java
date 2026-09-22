package ch.zhaw.abyss.domain;

/**
 * Kleine explizite Zustandsmaschine statt Engine-Abhängigkeit oder impliziter Animationstreffer.
 */
final class EnemyAi {
    private EnemyAi() {}

    static void update(Enemy enemy, GameRun run, double dt) {
        enemy.hurtTime = Math.max(0, enemy.hurtTime - dt);
        enemy.actionCooldown = Math.max(0, enemy.actionCooldown - dt);
        enemy.animationTime += dt;
        enemy.vx = 0;
        if (enemy.kind == EnemyKind.DRONE)
            enemy.y = GameRun.FLOOR - 78 + Math.sin(run.elapsed() * 2.4 + enemy.id) * 12;
        if (enemy.kind == EnemyKind.CAPTAIN
                && !enemy.enraged
                && enemy.health < enemy.maxHealth * .5) {
            enemy.enraged = true;
            run.events.add(GameEvent.at(GameEvent.Type.BOSS_PHASE, enemy.x, enemy.y));
        }
        if (enemy.state == Enemy.State.STUNNED || enemy.state == Enemy.State.RECOVER) {
            enemy.stateTime -= dt;
            if (enemy.stateTime <= 0) enemy.state = Enemy.State.APPROACH;
            return;
        }
        if (enemy.state == Enemy.State.WINDUP) {
            enemy.stateTime -= dt;
            if (enemy.stateTime <= 0) beginStrike(enemy, run);
            return;
        }
        if (enemy.state == Enemy.State.STRIKE) {
            strike(enemy, run, dt);
            return;
        }

        double distance = Math.abs(run.player.x - enemy.x);
        enemy.facing = run.player.x >= enemy.x ? 1 : -1;
        double range =
                switch (enemy.kind) {
                    case DRONE -> 550;
                    case SCUTTLER -> 250;
                    case SENTINEL -> 140;
                    case CAPTAIN -> 440;
                };
        if (distance > range) {
            enemy.vx = enemy.facing * enemy.kind.speed;
            enemy.x += enemy.vx * dt;
        } else if (enemy.kind == EnemyKind.DRONE && distance < 220) {
            enemy.vx = -enemy.facing * 75;
            enemy.x += enemy.vx * dt;
        }
        enemy.x = GameRun.clamp(enemy.x, 90, GameRun.WIDTH - 90);
        if (distance <= range + 20 && enemy.actionCooldown <= 0) {
            enemy.state = Enemy.State.WINDUP;
            enemy.stateTime =
                    switch (enemy.kind) {
                        case DRONE -> .7;
                        case SCUTTLER -> .65;
                        case SENTINEL -> .8;
                        case CAPTAIN -> enemy.enraged ? .8 : 1.0;
                    };
            enemy.targetX = run.player.x;
            enemy.targetY = run.player.y - 65;
            enemy.strikeHit = false;
            enemy.attackPattern = enemy.kind == EnemyKind.CAPTAIN ? enemy.attacks % 3 : 0;
            run.events.add(GameEvent.at(GameEvent.Type.TELEGRAPH, enemy.x, enemy.y));
        }
    }

    private static void beginStrike(Enemy enemy, GameRun run) {
        enemy.state = Enemy.State.STRIKE;
        enemy.stateTime =
                enemy.kind == EnemyKind.SCUTTLER
                                || enemy.kind == EnemyKind.CAPTAIN && enemy.attackPattern == 2
                        ? .34
                        : .16;
        double damage = run.enemyDamage();
        if (enemy.kind == EnemyKind.DRONE) {
            shootAt(enemy, run, 0, 610, 9 * damage);
        } else if (enemy.kind == EnemyKind.CAPTAIN) {
            if (enemy.attackPattern == 0) {
                for (int sign : new int[] {-1, 1})
                    run.addProjectile(
                            Projectile.Kind.SHOCKWAVE,
                            false,
                            enemy.x + sign * 70,
                            GameRun.FLOOR - 18,
                            sign * (enemy.enraged ? 520 : 430),
                            0,
                            17 * damage,
                            22,
                            4);
                run.events.add(new GameEvent(GameEvent.Type.PULSE, enemy.x, enemy.y, -1, ""));
            } else if (enemy.attackPattern == 1) {
                for (int i = -1; i <= 1; i++) shootAt(enemy, run, i * .16, 490, 12 * damage);
            }
        }
    }

    private static void shootAt(
            Enemy enemy, GameRun run, double offset, double speed, double damage) {
        double y = enemy.y - enemy.height * .55;
        double angle = Math.atan2(enemy.targetY - y, enemy.targetX - enemy.x) + offset;
        run.addProjectile(
                Projectile.Kind.BOLT,
                false,
                enemy.x + enemy.facing * 32,
                y,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                damage,
                9,
                4);
        run.events.add(GameEvent.at(GameEvent.Type.SHOT, enemy.x, y));
    }

    private static void strike(Enemy enemy, GameRun run, double dt) {
        double reach =
                enemy.kind == EnemyKind.SENTINEL ? 175 : enemy.kind == EnemyKind.CAPTAIN ? 135 : 78;
        boolean charging =
                enemy.kind == EnemyKind.SCUTTLER
                        || enemy.kind == EnemyKind.CAPTAIN && enemy.attackPattern == 2;
        if (charging) {
            enemy.vx = enemy.facing * (enemy.kind == EnemyKind.CAPTAIN ? 620 : 680);
            enemy.x = GameRun.clamp(enemy.x + enemy.vx * dt, 90, GameRun.WIDTH - 90);
        }
        boolean melee =
                enemy.kind == EnemyKind.SCUTTLER || enemy.kind == EnemyKind.SENTINEL || charging;
        double delta = run.player.x - enemy.x;
        if (melee
                && !enemy.strikeHit
                && Math.abs(delta) < reach
                && delta * enemy.facing > -50
                && run.player.y > GameRun.FLOOR - (enemy.kind == EnemyKind.SENTINEL ? 125 : 75)) {
            run.damagePlayer(
                    (enemy.kind == EnemyKind.CAPTAIN
                                    ? 20
                                    : enemy.kind == EnemyKind.SENTINEL ? 14 : 10)
                            * run.enemyDamage(),
                    enemy.x);
            enemy.strikeHit = true;
        }
        enemy.stateTime -= dt;
        if (enemy.stateTime <= 0) {
            enemy.state = Enemy.State.RECOVER;
            enemy.stateTime = enemy.kind == EnemyKind.CAPTAIN ? (enemy.enraged ? 1.45 : 1.9) : .8;
            enemy.actionCooldown = enemy.kind == EnemyKind.DRONE ? 1.7 : .9;
            enemy.attacks++;
        }
    }
}
