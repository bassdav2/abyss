package ch.zhaw.abyss.domain;

import ch.zhaw.abyss.domain.Enemy.State;

/**
 * Der Lotse auf der Brücke: ein Kampf ums Ausweichen. Er trägt Deckung und blockt fast alles; nur
 * in den Pausen nach seinen Mustern dringt Schaden durch, und auch dann nur ein Stück. Wer ihm zu
 * nahe kommt, dem blinzelt er davon. Seine Muster lassen sich lernen:
 *
 * <ul>
 *   <li>Taktfeuer: Lanzen von beiden Wänden im Rhythmus, tief (springen) oder hoch (unten bleiben).
 *   <li>Ankerkette: eine Lanzenwelle von der Decke; zwischen zwei Säulen stehen oder
 *       hindurchdashen.
 *   <li>Zielstrahlen: Harpunen von oben, die dorthin zielen, wo du gerade stehst.
 *   <li>Enterangriff: Er erscheint hinter dir und schlägt nach einer kurzen Warnung zu.
 *   <li>Sonarringe: Ringe aus Geschossen mit einer Lücke.
 *   <li>dazu Harpune, Ansturm, Bodenwelle, Kreuzfeuer, Sperrfeuer, Drohnen, Enterkommando und
 *       Torpedos, die ihn selbst betäuben, wenn man sie in ihn lenkt.
 * </ul>
 *
 * Beim Phasenwechsel verschwindet er und lässt eine Ankerkette los, bevor er zurückkehrt.
 */
final class CaptainBrain extends Brain {
    static final int HARPOON = 0, CHARGE = 1, WAVE = 2, DRONES = 3, BARRAGE = 4, TORPEDO = 5;
    static final int CROSSFIRE = 6, BOARDING = 7, RHYTHM = 8, ANCHORS = 9, BEAMS = 10;
    static final int BLINK = 11, RINGS = 12;
    private static final int[][] CYCLES = {
        {HARPOON, RHYTHM, CHARGE, TORPEDO, BEAMS, WAVE},
        {CROSSFIRE, ANCHORS, BLINK, TORPEDO, RINGS, RHYTHM, DRONES, HARPOON},
        {BARRAGE, BEAMS, ANCHORS, BLINK, RHYTHM, BOARDING, RINGS, TORPEDO, CROSSFIRE}
    };

    /** Rhythmus des Taktfeuers: t = tief (springen), h = hoch (unten bleiben). */
    private static final String[] RHYTHMS = {"thththt", "tthththt", "thhttththt"};

    /** Höhe der tiefen und der hohen Lanzen. */
    static final double LOW = GameRun.FLOOR - 32, HIGH = GameRun.FLOOR - 150;

    /** Deckung als Anteil der Lebenspunkte: Stoss beim Öffnen, Füllrate, Obergrenze. */
    static final double OPEN_BURST = .03, STUN_BURST = .05, OPEN_RATE = .018, OPEN_CAP = .06;

    static final double CLOSED_RATE = .004, CLOSED_CAP = .012;

    // --- Deckung und Blinzeln ------------------------------------------------------------------

    @Override
    void tick(Enemy e, GameRun run, double dt) {
        e.blinkCooldown = Math.max(0, e.blinkCooldown - dt);
        boolean open = e.state == State.RECOVER || e.state == State.STUNNED;
        double easy = run.player.explorer ? 1.6 : 1;
        if (open && !e.exposedLast)
            e.guard =
                    Math.max(
                            e.guard,
                            e.maxHealth * (e.state == State.STUNNED ? STUN_BURST : OPEN_BURST));
        if (!open && e.exposedLast) e.guard = Math.min(e.guard, e.maxHealth * CLOSED_CAP);
        e.exposedLast = open;
        double cap = (open ? OPEN_CAP : CLOSED_CAP) * e.maxHealth;
        double rate = (open ? OPEN_RATE : CLOSED_RATE) * easy * e.maxHealth;
        if (e.guard < cap) e.guard = Math.min(cap, e.guard + rate * dt);
    }

    @Override
    void spawned(Enemy e, GameRun run) {
        e.guard = e.maxHealth * CLOSED_CAP;
    }

    @Override
    void approach(Enemy e, GameRun run, double dt) {
        keepDistance(e, run, 300, 430);
        // Nahkämpfern blinzelt er davon: Schaden gibt es in seinen Pausen, nicht im Gedränge.
        if (e.blinkCooldown <= 0 && Math.abs(dx(e, run)) < 150) blinkAway(e, run);
    }

    private static void blinkAway(Enemy e, GameRun run) {
        double width = run.layout().width();
        double side = run.player.x < width / 2 ? 1 : -1;
        double x = GameRun.clamp(run.player.x + side * 480, 160, width - 160);
        if (Math.abs(x - run.player.x) < 300)
            x = GameRun.clamp(run.player.x - side * 480, 160, width - 160);
        teleport(e, run, x);
        e.blinkCooldown = 2.4 - .5 * e.phase;
        face(e, run);
    }

    private static void teleport(Enemy e, GameRun run, double x) {
        run.emit(new GameEvent(GameEvent.Type.SPAWN, e.x, e.y, e.kind.ordinal(), "blink"));
        e.x = x;
        e.vx = 0;
        run.emit(new GameEvent(GameEvent.Type.SPAWN, e.x, e.y, e.kind.ordinal(), "blink"));
    }

    // --- Phasenwechsel -------------------------------------------------------------------------

    @Override
    void phaseChanged(Enemy e, GameRun run) {
        e.state = State.HIDDEN;
        e.stateTime = 2.4;
        e.telegraph = null;
        anchors(e, run, true);
        if (e.phase == 2) run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.y, 0, "ORDER"));
    }

    @Override
    void hidden(Enemy e, GameRun run, double dt) {
        e.stateTime -= dt;
        if (e.stateTime > 0) return;
        double width = run.layout().width();
        teleport(e, run, run.player.x < width / 2 ? width - 260 : 260);
        face(e, run);
        e.state = State.APPROACH;
        e.actionCooldown = .4;
    }

    // --- Muster --------------------------------------------------------------------------------

    @Override
    boolean ready(Enemy e, GameRun run) {
        return true;
    }

    @Override
    void choose(Enemy e, GameRun run) {
        var cycle = CYCLES[e.phase];
        e.pattern = cycle[e.attacks % cycle.length];
        switch (e.pattern) {
            case BARRAGE -> barrage(e, run);
            case CROSSFIRE -> crossfire(run);
            case BLINK -> {
                // Erscheint hinter der Figur, mit Blick auf sie.
                double behind = run.player.x - run.player.facing * 130;
                double width = run.layout().width();
                if (behind < 140 || behind > width - 140)
                    behind = run.player.x + run.player.facing * 130;
                teleport(e, run, GameRun.clamp(behind, 140, width - 140));
                face(e, run);
            }
            default -> {}
        }
    }

    /**
     * Sperrfeuer: In der letzten Phase zwei versetzte Reihen im Schachbrettmuster. Wer nach der
     * ersten Reihe in deren Einschlagstellen wechselt, steht sicher.
     */
    private static void barrage(Enemy e, GameRun run) {
        double width = run.layout().width();
        if (e.phase < 2) {
            for (int k = -2; k <= 2; k++) {
                double x = GameRun.clamp(run.player.x + k * 190, 90, width - 90);
                run.addHazard(
                        new Hazard(x, 110, Hazard.Kind.BARRAGE, -1.05 - Math.abs(k) * .08, 1.6));
            }
            return;
        }
        double offset = run.rng.nextInt(2) * 160;
        for (int round = 0; round < 2; round++)
            for (double x = 100 + offset + round * 160; x < width - 60; x += 320)
                run.addHazard(
                        new Hazard(x, 140, Hazard.Kind.BARRAGE, -1.05 - round * .85, 1.6 + round));
    }

    /**
     * Kreuzfeuer: Harpunengeschütze an beiden Wänden zielen zuerst auf Kopfhöhe der Figur, dann in
     * Sprunghöhe. Erst ducken bleiben, dann springen.
     */
    private static void crossfire(GameRun run) {
        double width = run.layout().width();
        double low = Math.min(GameRun.FLOOR - 60, run.player.centerY());
        double high = GameRun.FLOOR - 210;
        for (int side : new int[] {-1, 1}) {
            double x = side < 0 ? 30 : width - 30;
            double angle = side < 0 ? 0 : Math.PI;
            run.lance(steel(run, x, low, angle, 1500, 14, 1.0));
            run.lance(steel(run, x, high, angle, 1500, 14, 1.9));
        }
    }

    private static Lance steel(
            GameRun run,
            double x,
            double y,
            double angle,
            double speed,
            double damage,
            double warning) {
        return new Lance(
                x, y, angle, speed, damage * run.enemyDamage(), warning, Lance.Style.STEEL, 0);
    }

    /** Taktfeuer: Lanzen von wechselnden Wänden im Takt, tief zum Springen, hoch zum Bleiben. */
    static void rhythm(Enemy e, GameRun run) {
        double width = run.layout().width();
        String beats = RHYTHMS[Math.min(e.phase, RHYTHMS.length - 1)];
        double spacing = rhythmSpacing(e);
        for (int i = 0; i < beats.length(); i++) {
            boolean left = i % 2 == 0;
            double y = beats.charAt(i) == 't' ? LOW : HIGH;
            run.lance(
                    steel(
                            run,
                            left ? 30 : width - 30,
                            y,
                            left ? 0 : Math.PI,
                            1350,
                            11,
                            .9 + i * spacing));
        }
    }

    private static double rhythmSpacing(Enemy e) {
        return .62 - .06 * e.phase;
    }

    /**
     * Ankerkette: Lanzen fallen als Welle von der Decke. Zwischen zwei Säulen bleibt eine Lücke,
     * alternativ trägt das Ausweichen hindurch. In der letzten Phase kommen zwei Wellen von beiden
     * Seiten.
     */
    static void anchors(Enemy e, GameRun run, boolean transition) {
        double width = run.layout().width();
        boolean fromLeft = e.x < width / 2;
        int columns = (int) ((width - 180) / 120) + 1;
        for (int k = 0; k < columns; k++) {
            double x = fromLeft ? 90 + k * 120 : width - 90 - k * 120;
            run.lance(steel(run, x, 70, Math.PI / 2, 1400, 13, 1.0 + k * .07));
        }
        if (e.phase >= 2 && !transition)
            for (int k = 0; k < columns; k++) {
                double x = fromLeft ? width - 150 - k * 120 : 150 + k * 120;
                run.lance(steel(run, x, 70, Math.PI / 2, 1400, 13, 1.6 + k * .07));
            }
    }

    private static double anchorTime(Enemy e, GameRun run) {
        int columns = (int) ((run.layout().width() - 180) / 120) + 1;
        return 1.2 + columns * .07 + (e.phase >= 2 ? .6 : 0);
    }

    /** Zielstrahlen: Harpunen von der Decke auf die Stelle, an der die Figur gerade steht. */
    static void beams(Enemy e, GameRun run) {
        double width = run.layout().width();
        int count = e.phase >= 2 ? 5 : 3;
        double tx = run.player.x, ty = run.player.centerY();
        for (int i = 0; i < count; i++) {
            double ox = GameRun.clamp(tx + (i - (count - 1) / 2.0) * 330, 60, width - 60);
            double oy = 90 + (i % 2) * 40;
            double angle = Math.atan2(ty - oy, tx - ox);
            run.lance(steel(run, ox, oy, angle, 1800, 13, 1.0 + i * .22));
        }
    }

    /** Sonarringe: Geschossringe mit einer Lücke, unterschiedlich schnell. */
    static void rings(Enemy e, GameRun run) {
        int count = e.phase >= 2 ? 18 : 14;
        int rings = e.phase >= 2 ? 3 : 2;
        double y = e.y - e.height * .55;
        for (int ring = 0; ring < rings; ring++) {
            int gap = run.rng.nextInt(count);
            for (int i = 0; i < count; i++) {
                int distance = Math.floorMod(i - gap, count);
                if (distance <= 1 || distance == count - 1) continue;
                double angle = (i + ring * .5) / count * Math.PI * 2;
                double speed = 210 + ring * 80;
                run.shoot(
                        Projectile.Kind.BOLT,
                        false,
                        e.x,
                        y,
                        Math.cos(angle) * speed,
                        Math.sin(angle) * speed,
                        9 * run.enemyDamage(),
                        9,
                        4.5);
            }
        }
        run.emit(GameEvent.at(GameEvent.Type.SHOT, e.x, y));
    }

    @Override
    double windupTime(Enemy e) {
        return switch (e.pattern) {
            case HARPOON -> e.enraged ? .6 : .75;
            case CHARGE -> .9;
            case WAVE -> .8;
            case DRONES, BOARDING -> .7;
            case TORPEDO -> .8;
            case CROSSFIRE, RINGS -> .7;
            case RHYTHM, ANCHORS -> .6;
            case BEAMS -> .5;
            case BLINK -> .6 - .06 * e.phase;
            default -> 1.0;
        };
    }

    @Override
    Telegraph telegraph(Enemy e, GameRun run) {
        double width = run.layout().width();
        return switch (e.pattern) {
            case HARPOON, TORPEDO ->
                    new Telegraph(
                            Telegraph.Shape.AIM, e.x, e.y - e.height * .6, e.targetX, e.targetY);
            case CHARGE ->
                    new Telegraph(
                            Telegraph.Shape.FLOOR, e.x, e.y, e.facing > 0 ? width - 50 : 50, e.y);
            case WAVE -> new Telegraph(Telegraph.Shape.FLOOR, 60, GameRun.FLOOR, width - 60, 0);
            case BLINK -> new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.y - e.height * .5, 175, 0);
            case RINGS -> new Telegraph(Telegraph.Shape.CIRCLE, e.x, e.y - e.height * .55, 90, 0);
            default -> null;
        };
    }

    @Override
    double strikeTime(Enemy e) {
        return switch (e.pattern) {
            case CHARGE -> 3;
            case RHYTHM -> .9 + RHYTHMS[Math.min(e.phase, 2)].length() * rhythmSpacing(e);
            case BEAMS -> e.phase >= 2 ? 2.1 : 1.6;
            case RINGS -> 1.0;
            default -> .2;
        };
    }

    @Override
    void strikeStart(Enemy e, GameRun run) {
        switch (e.pattern) {
            case HARPOON -> {
                double y = e.y - e.height * .6;
                double angle = Math.atan2(e.targetY - y, e.targetX - e.x);
                int shots = 1 + 2 * e.phase;
                for (int i = 0; i < shots; i++)
                    run.shoot(
                            Projectile.Kind.ENEMY_HARPOON,
                            false,
                            e.x + e.facing * 40,
                            y,
                            Math.cos(angle + (i - (shots - 1) / 2.0) * .18) * 1150,
                            Math.sin(angle + (i - (shots - 1) / 2.0) * .18) * 1150,
                            16 * run.enemyDamage(),
                            12,
                            3);
                run.emit(GameEvent.at(GameEvent.Type.HARPOON, e.x, y));
            }
            case WAVE -> {
                run.shockwaves(e.x, 17 * run.enemyDamage(), 500);
                run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 140, ""));
            }
            case DRONES -> {
                if (minions(e, run) < 3) {
                    run.summon(EnemyKind.DRONE, e.x - 200, GameRun.FLOOR - 220, e);
                    run.summon(EnemyKind.DRONE, e.x + 200, GameRun.FLOOR - 220, e);
                }
            }
            case BOARDING -> {
                if (minions(e, run) < 3) {
                    run.summon(EnemyKind.ENFORCER, 140, GameRun.FLOOR, e);
                    run.summon(EnemyKind.ENFORCER, run.layout().width() - 140, GameRun.FLOOR, e);
                    run.emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.y, 0, "BOARDING"));
                }
            }
            case TORPEDO -> torpedoes(e, run);
            case RHYTHM -> rhythm(e, run);
            case ANCHORS -> {
                anchors(e, run, false);
                e.stateTime = anchorTime(e, run);
            }
            case BEAMS -> beams(e, run);
            case RINGS -> rings(e, run);
            case BLINK -> {
                melee(e, run, 190, 170, 22);
                run.emit(new GameEvent(GameEvent.Type.SLAM, e.x, e.y, 90, ""));
            }
            default -> {}
        }
    }

    /** Torpedofächer: langsame, zielsuchende Torpedos, die auch den Lotsen selbst treffen. */
    private static void torpedoes(Enemy e, GameRun run) {
        int count = 2 + e.phase;
        double y = e.y - e.height * .6;
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 + (i - (count - 1) / 2.0) * .55;
            var torpedo =
                    run.shoot(
                            Projectile.Kind.TORPEDO,
                            false,
                            e.x,
                            y,
                            Math.cos(angle) * 300 + e.facing * 80,
                            Math.sin(angle) * 300,
                            15 * run.enemyDamage(),
                            16,
                            7);
            torpedo.homing = true;
            torpedo.steerStart = .35;
            torpedo.explosionRadius = 110;
            torpedo.wreck = true;
            torpedo.owner = e.id;
        }
        run.emit(GameEvent.at(GameEvent.Type.TORPEDO, e.x, y));
    }

    @Override
    void strike(Enemy e, GameRun run, double dt) {
        if (e.pattern != CHARGE) return;
        e.vx = e.facing * 740;
        melee(e, run, 90, 150, 20);
        if (atWall(e, run) || (e.x - e.targetX) * e.facing > 350) e.stateTime = 0;
    }

    @Override
    double recoverTime(Enemy e) {
        // Die Pausen sind die Fenster für Schaden; nach schweren Mustern dauern sie länger.
        return switch (e.pattern) {
            case HARPOON -> .9;
            case CHARGE -> 1.8;
            case WAVE -> 1.4;
            case DRONES, BOARDING -> 1.0;
            case TORPEDO -> 1.6;
            case CROSSFIRE, BARRAGE -> 1.6;
            case RHYTHM -> 2.4;
            case ANCHORS -> 2.2;
            case BEAMS -> 1.8;
            case BLINK -> 1.6;
            case RINGS -> 2.0;
            default -> .9;
        };
    }

    @Override
    double cooldown(Enemy e) {
        return .25;
    }
}
