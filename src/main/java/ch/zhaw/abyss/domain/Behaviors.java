package ch.zhaw.abyss.domain;

import ch.zhaw.abyss.domain.Enemy.State;

/**
 * Katalog der zustandslosen Verhaltensstrategien regulärer Gegner. Bosse haben wegen ihrer Muster
 * eigene Klassen.
 */
final class Behaviors {
    static final EnemyBehavior SCUTTLER = new Scuttler();
    static final EnemyBehavior DRONE = new Drone();
    static final EnemyBehavior SENTINEL = new Sentinel();
    static final EnemyBehavior BOMBER = new Bomber();
    static final EnemyBehavior WELDER = new Welder();
    static final EnemyBehavior TURRET = new Turret();
    static final EnemyBehavior MINELAYER = new MineLayer();
    static final EnemyBehavior JELLY = new Jelly();
    static final EnemyBehavior EEL = new Eel();
    static final EnemyBehavior SHIELDBEARER = new ShieldBearer();
    static final EnemyBehavior ENFORCER = new Enforcer();
    static final EnemyBehavior SEEKER = new Seeker();
    static final EnemyBehavior SMUGGLER = new Smuggler();
    static final EnemyBehavior WARDEN = new WardenBrain();
    static final EnemyBehavior REACTOR = new ReactorBrain();
    static final EnemyBehavior BROOD = new BroodBrain();
    static final EnemyBehavior CAPTAIN = new CaptainBrain();

    private Behaviors() {}

    /** Vierbeiniger Schrottläufer: Ansturm aus kurzer Distanz, springt Stegen hinterher. */
    static final class Scuttler extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            Brain.close(e, run, 190);
            if (e.grounded
                    && dy(e, run) < -90
                    && Math.abs(dx(e, run)) < 280
                    && run.rng.nextDouble() < dt * 1.6) e.vy = -830;
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 240 && Math.abs(dy(e, run)) < 80 && e.grounded;
        }

        @Override
        double windupTime(Enemy e) {
            return .6;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.FLOOR, e.x, e.y, e.x + e.facing * 270, e.y);
        }

        @Override
        double strikeTime(Enemy e) {
            return .32;
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            e.vx = e.facing * 680 * (e.affix == Affix.SWIFT ? 1.2 : 1);
            melee(e, run, 72, 60, 10);
        }
    }

    /** Schwebende Wachdrohne: hält Abstand und feuert gezielte Energiegeschosse. */
    static final class Drone extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            face(e, run);
            double base =
                    run.player.y < GameRun.FLOOR - 60 ? run.player.y - 110 : GameRun.FLOOR - 112;
            double y = base + Math.sin(run.elapsed() * 2.4 + e.id) * 14;
            double d = Math.abs(dx(e, run));
            double targetX =
                    d > 470 ? run.player.x - e.facing * 400 : d < 250 ? e.x - e.facing * 200 : e.x;
            flyTo(e, targetX, y, speed(e));
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 600;
        }

        @Override
        double windupTime(Enemy e) {
            return .7;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(
                    Telegraph.Shape.AIM, e.x, e.y - e.height * .55, e.targetX, e.targetY);
        }

        @Override
        void strikeStart(Enemy e, GameRun run) {
            shootAt(e, run, 0, 560, 9, e.targetX, e.targetY);
        }

        @Override
        double recoverTime(Enemy e) {
            return .45;
        }

        @Override
        double cooldown(Enemy e) {
            return 1.6;
        }
    }

    /** Schwer gepanzerter Schottwächter mit weitem Überkopfschlag. */
    static final class Sentinel extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            Brain.close(e, run, 125);
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 155 && Math.abs(dy(e, run)) < 70;
        }

        @Override
        double windupTime(Enemy e) {
            return .8;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.FLOOR, e.x, e.y, e.x + e.facing * 190, e.y);
        }

        @Override
        double strikeTime(Enemy e) {
            return .18;
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            melee(e, run, 185, 130, 14);
        }

        @Override
        double recoverTime(Enemy e) {
            return .9;
        }
    }

    /** Rollende Kugelbombe, die sich neben der Figur selbst sprengt. */
    static final class Bomber extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            Brain.close(e, run, 40);
            e.animationTime += dt * 2;
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 100 && Math.abs(dy(e, run)) < 80;
        }

        @Override
        double windupTime(Enemy e) {
            return .6;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.centerY(), 140, 0);
        }

        @Override
        double strikeTime(Enemy e) {
            return .05;
        }

        @Override
        void strikeStart(Enemy e, GameRun run) {
            run.combat.explode(e.x, e.centerY(), 140, 22 * run.enemyDamage(), true, false, null);
            e.health = 0;
        }
    }

    /** Schweissroboter mit kurzem Flammenstrahl, der entzündet. */
    static final class Welder extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            Brain.close(e, run, 150);
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 180 && Math.abs(dy(e, run)) < 70;
        }

        @Override
        double windupTime(Enemy e) {
            return .6;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.FLOOR, e.x, e.y, e.x + e.facing * 215, e.y);
        }

        @Override
        double strikeTime(Enemy e) {
            return 1.0;
        }

        @Override
        void strikeStart(Enemy e, GameRun run) {
            run.emit(GameEvent.at(GameEvent.Type.FLAME, e.x + e.facing * 60, e.y - 50));
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            double left = e.facing > 0 ? e.x + 20 : e.x - 215;
            var flame = new Bounds(left, e.y - 80, 195, 60);
            if (flame.intersects(run.player.bounds())) {
                if (run.combat.hurtPlayer(6 * run.enemyDamage(), e.x, e, false))
                    run.player.statuses.ignite(2.5, 4 * run.enemyDamage());
            }
        }

        @Override
        double recoverTime(Enemy e) {
            return .9;
        }

        @Override
        double cooldown(Enemy e) {
            return 1.3;
        }
    }

    /** Deckenmontierter Geschützturm, feuert Dreiersalven. */
    static final class Turret extends Brain {
        @Override
        void idle(Enemy e, GameRun run, double dt) {
            e.vx = 0;
            e.vy = 0;
        }

        @Override
        void approach(Enemy e, GameRun run, double dt) {
            idle(e, run, dt);
            face(e, run);
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 720;
        }

        @Override
        double windupTime(Enemy e) {
            return .8;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(
                    Telegraph.Shape.AIM, e.x, e.y - e.height * .55, e.targetX, e.targetY);
        }

        @Override
        double strikeTime(Enemy e) {
            return .5;
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            idle(e, run, dt);
            if (e.burst < 3 && e.stateTime <= .5 - e.burst * .15) {
                face(e, run);
                shootAt(e, run, 0, 600, 8, run.player.x, run.player.centerY());
                e.burst++;
            }
        }

        @Override
        double recoverTime(Enemy e) {
            return .3;
        }

        @Override
        double cooldown(Enemy e) {
            return 1.9;
        }
    }

    /** Langsamer Minenleger, der scharfe Minen in die Laufwege wirft. */
    static final class MineLayer extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            keepDistance(e, run, 260, 440);
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 640 && e.grounded;
        }

        @Override
        double windupTime(Enemy e) {
            return .9;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            double surface = run.layout().surfaceBelow(e.targetX, run.player.y - 5);
            return new Telegraph(Telegraph.Shape.CIRCLE, e.targetX, surface - 12, 80, 0);
        }

        @Override
        void strikeStart(Enemy e, GameRun run) {
            double time = .9;
            var mine =
                    run.shoot(
                            Projectile.Kind.MINE,
                            false,
                            e.x,
                            e.y - e.height,
                            (e.targetX - e.x) / time,
                            -680,
                            16 * run.enemyDamage(),
                            14,
                            8);
            mine.gravity = 1500;
            mine.armTime = .7;
            mine.explosionRadius = 110;
            run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, e.y - e.height));
        }

        @Override
        double recoverTime(Enemy e) {
            return .7;
        }

        @Override
        double cooldown(Enemy e) {
            return 2.2;
        }
    }

    /** Leuchtqualle: treibt heran und entlädt einen Stromstoss. */
    static final class Jelly extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            face(e, run);
            double bob = Math.sin(run.elapsed() * 1.7 + e.id) * 20;
            flyTo(e, run.player.x, run.player.y - 40 + bob, speed(e));
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.hypot(dx(e, run), run.player.centerY() - e.centerY()) < 175;
        }

        @Override
        double windupTime(Enemy e) {
            return .8;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.centerY(), 165, 0);
        }

        @Override
        void strikeStart(Enemy e, GameRun run) {
            run.emit(new GameEvent(GameEvent.Type.SHOCK, e.x, e.centerY(), 165, ""));
            if (Math.hypot(dx(e, run), run.player.centerY() - e.centerY()) < 165
                    && run.combat.hurtPlayer(12 * run.enemyDamage(), e.x, e, false))
                run.player.statuses.apply(Status.SHOCK, 1.0);
        }

        @Override
        double strikeTime(Enemy e) {
            return .25;
        }

        @Override
        double recoverTime(Enemy e) {
            return 1.0;
        }

        @Override
        double cooldown(Enemy e) {
            return 1.4;
        }
    }

    /**
     * Sicherheitsautomat des Kommandodecks: hält mittleren Abstand und stösst dann mit dem
     * Schockstab weit nach vorn. Ein Treffer verlangsamt durch Schock.
     */
    static final class Enforcer extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            Brain.close(e, run, 300);
            if (e.grounded
                    && dy(e, run) < -90
                    && Math.abs(dx(e, run)) < 320
                    && run.rng.nextDouble() < dt * 1.2) e.vy = -860;
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 400 && Math.abs(dy(e, run)) < 90 && e.grounded;
        }

        @Override
        double windupTime(Enemy e) {
            return .65;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.FLOOR, e.x, e.y, e.x + e.facing * 420, e.y);
        }

        @Override
        double strikeTime(Enemy e) {
            return .34;
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            e.vx = e.facing * 1100 * (e.affix == Affix.SWIFT ? 1.15 : 1);
            if (melee(e, run, 95, 115, 14)) run.player.statuses.apply(Status.SHOCK, .8);
        }

        @Override
        double recoverTime(Enemy e) {
            return .9;
        }

        @Override
        double cooldown(Enemy e) {
            return 1.5;
        }
    }

    /**
     * Suchlichtsonde: schwebt hoch unter der Decke, erfasst die Figur mit ihrem Scheinwerfer und
     * feuert eine schnelle Dreiersalve entlang der Ziellinie.
     */
    static final class Seeker extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            face(e, run);
            double y = GameRun.FLOOR - 215 + Math.sin(run.elapsed() * 1.7 + e.id) * 18;
            double d = Math.abs(dx(e, run));
            double targetX =
                    d > 520 ? run.player.x - e.facing * 420 : d < 260 ? e.x - e.facing * 220 : e.x;
            flyTo(e, targetX, y, speed(e));
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 650;
        }

        @Override
        double windupTime(Enemy e) {
            return .9;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(
                    Telegraph.Shape.AIM, e.x, e.y - e.height * .55, e.targetX, e.targetY);
        }

        @Override
        double strikeTime(Enemy e) {
            return .42;
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            int due = e.stateTime > .3 ? 1 : e.stateTime > .16 ? 2 : 3;
            while (e.burst < due) {
                shootAt(e, run, (e.burst - 1) * .05, 900, 7, e.targetX, e.targetY);
                e.burst++;
            }
        }

        @Override
        double recoverTime(Enemy e) {
            return .6;
        }

        @Override
        double cooldown(Enemy e) {
            return 2.0;
        }
    }

    /**
     * Schmugglerdrohne mit Beute: greift nie an, hält Abstand und entkommt nach {@value
     * #ESCAPE_TIME} Sekunden durch einen Lüftungsschacht.
     */
    static final class Smuggler extends Brain {
        static final double ESCAPE_TIME = 15;

        @Override
        void approach(Enemy e, GameRun run, double dt) {
            double away = e.x >= run.player.x ? 1 : -1;
            double min = RoomLayout.WALL + 80, max = run.layout().width() - RoomLayout.WALL - 80;
            if (e.x < min + 60) away = 1;
            if (e.x > max - 60) away = -1;
            double targetX =
                    Math.abs(e.x - run.player.x) < 460
                            ? e.x + away * 220
                            : e.x + Math.sin(e.animationTime * 1.3) * 120;
            double y = GameRun.FLOOR - 190 + Math.sin(e.animationTime * 3.1 + e.id) * 40;
            flyTo(e, GameRun.clamp(targetX, min, max), y, speed(e));
            e.facing = e.vx >= 0 ? 1 : -1;
            if (e.animationTime > ESCAPE_TIME && !e.escaped) {
                e.escaped = true;
                run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.y, 0, "ESCAPE"));
            }
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return false;
        }

        @Override
        double windupTime(Enemy e) {
            return 1;
        }
    }

    /** Tiefseeaal: lauert unter den Bodengittern und schnellt heraus. */
    static final class Eel extends Brain {
        @Override
        void spawned(Enemy e, GameRun run) {
            e.state = State.HIDDEN;
            e.stateTime = 1.0;
        }

        @Override
        void approach(Enemy e, GameRun run, double dt) {
            e.state = State.HIDDEN;
            e.stateTime = 1.2;
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return false;
        }

        @Override
        void hidden(Enemy e, GameRun run, double dt) {
            e.stateTime -= dt;
            e.y = GameRun.FLOOR;
            e.vx = 0;
            double d = dx(e, run);
            e.x += Math.signum(d) * Math.min(Math.abs(d), 260 * dt);
            if (e.stateTime <= 0 && (Math.abs(d) < 90 || e.stateTime < -2.5)) beginWindup(e, run);
        }

        @Override
        double windupTime(Enemy e) {
            return .7;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.FLOOR, e.x - 70, GameRun.FLOOR, e.x + 70, 0);
        }

        @Override
        double strikeTime(Enemy e) {
            return 2;
        }

        @Override
        void strikeStart(Enemy e, GameRun run) {
            e.vy = -990;
            e.grounded = false;
            run.emit(GameEvent.at(GameEvent.Type.SPAWN, e.x, GameRun.FLOOR));
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            e.vx = e.facing * 210;
            contact(e, run, 12);
            if (e.grounded && e.stateTime < 1.8) e.stateTime = 0;
        }

        @Override
        double recoverTime(Enemy e) {
            return 1.3;
        }
    }

    /** Schildträger: blockt frontal und rammt; nur von hinten oder nach dem Stoss verwundbar. */
    static final class ShieldBearer extends Brain {
        @Override
        void approach(Enemy e, GameRun run, double dt) {
            Brain.close(e, run, 140);
        }

        @Override
        boolean ready(Enemy e, GameRun run) {
            return Math.abs(dx(e, run)) < 180 && Math.abs(dy(e, run)) < 70;
        }

        @Override
        double windupTime(Enemy e) {
            return .9;
        }

        @Override
        Telegraph telegraph(Enemy e, GameRun run) {
            return new Telegraph(Telegraph.Shape.FLOOR, e.x, e.y, e.x + e.facing * 240, e.y);
        }

        @Override
        double strikeTime(Enemy e) {
            return .3;
        }

        @Override
        void strike(Enemy e, GameRun run, double dt) {
            e.vx = e.facing * 520;
            if (melee(e, run, 95, 115, 13)) run.player.vx += e.facing * 300;
        }

        @Override
        double recoverTime(Enemy e) {
            return 1.1;
        }

        @Override
        double cooldown(Enemy e) {
            return 1.0;
        }
    }
}
