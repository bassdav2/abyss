package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.Enemy;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.Fixture;
import ch.zhaw.abyss.domain.GameEvent;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Hazard;
import ch.zhaw.abyss.domain.Interaction;
import ch.zhaw.abyss.domain.Pickup;
import ch.zhaw.abyss.domain.Projectile;
import ch.zhaw.abyss.domain.RoomCondition;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Status;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.art.PropArt;
import ch.zhaw.abyss.ui.art.RoomArt;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.LightMap;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.pixel.PostProcess;
import ch.zhaw.abyss.ui.pixel.Sprite;

/**
 * Zeichnet einen Tauchgang in die Pixelauflösung 480 x 270: Meer, Raumstreifen, Requisiten,
 * Figuren, Vorwarnungen, Geschosse und Effekte; danach Licht, Bloom, Farbstimmung und HUD. Liest
 * die Domäne ausschliesslich und übersetzt Ereignisse in Effekte.
 */
public final class WorldRenderer {
    /** Bildbreite in Pixeln. */
    public static final int W = 480;

    /** Bildhöhe in Pixeln. */
    public static final int H = 270;

    private static final double PX = RoomArt.PX;

    private final Frame frame = new Frame(W, H);
    private final Frame emissive = new Frame(W, H);
    private final MachinePainter machines = new MachinePainter(frame, emissive);
    private final LightMap lights = new LightMap(W, H, 2);
    private final PostProcess post = new PostProcess(W, H);
    private final PixelFont font;
    private final SpriteBank bank;
    private final Effects fx = new Effects();
    private final Camera camera = new Camera();
    private final Ocean ocean = new Ocean(W, H);
    private final HudRenderer hud;
    private final SubmarineScene submarine;
    private final EventEffects events;
    private final ActorPainter actors;
    private Cosmetics look = Cosmetics.DEFAULT;
    private RoomPlan lastRoom;
    private double time, lowHealthPulse, fadeIn;
    // Stromversorgung im Raum: 0 bei Stromausfall, steigt nach dem Sichern wieder auf 1.
    private double power = 1;
    private final ScreenFeel feel = new ScreenFeel();
    private final java.util.Set<Hazard> detonated =
            java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    /**
     * @param font Pixelschrift
     * @param bank Grafikspeicher
     */
    public WorldRenderer(PixelFont font, SpriteBank bank) {
        this.font = font;
        this.bank = bank;
        this.hud = new HudRenderer(font);
        this.submarine = new SubmarineScene(frame, emissive, lights, post, ocean, font);
        this.events = new EventEffects(fx, camera, hud, feel);
        this.actors = new ActorPainter(frame, emissive, bank, font);
    }

    /**
     * Zeichnet den Titelbildschirm mit dem gleitenden Boot.
     *
     * @param settings Einstellungen
     * @param showTitle Schriftzug zeichnen
     */
    public void renderTitle(Settings settings, boolean showTitle) {
        submarine.title(time, settings.reducedMotion(), showTitle);
    }

    /**
     * Zeichnet den Auftakt vor dem ersten Tauchgang.
     *
     * @param seconds Sekunden seit Beginn der Szene
     * @param settings Einstellungen
     */
    public void renderIntro(double seconds, Settings settings) {
        submarine.intro(seconds, settings.reducedMotion());
    }

    /**
     * Zeichnet die Siegesszene mit dem aufsteigenden Boot.
     *
     * @param seconds Sekunden seit Beginn der Szene
     * @param settings Einstellungen
     */
    public void renderEnding(double seconds, Settings settings) {
        submarine.ending(seconds, settings.reducedMotion());
        hud.drawMessagesOnly(frame);
    }

    /**
     * Zeichnet die Bootskarte mit aktueller Position.
     *
     * @param run Tauchgang
     */
    public void renderMap(GameRun run) {
        submarine.map(time, run.room().depth(), true);
    }

    /**
     * Dunkelt das zuletzt gezeichnete Bild für überlagernde Menüs ab.
     *
     * @param amount Stärke 0 bis 1
     */
    public void dim(double amount) {
        post.flash(frame, Pal.INK, amount);
    }

    /**
     * @return fertiges Bild
     */
    public Frame frame() {
        return frame;
    }

    /**
     * @param worldX Weltposition
     * @return Position im Bild in Pixeln
     */
    public double screenX(double worldX) {
        return worldX * PX - camera.left();
    }

    /**
     * @return Anzeige für Meldungen und Banner
     */
    public HudRenderer hud() {
        return hud;
    }

    /**
     * @param value Aussehen der Spielfigur
     */
    public void look(Cosmetics value) {
        look = value;
    }

    /**
     * Liefert und verbraucht eine angeforderte Trefferpause.
     *
     * @return Pause in Sekunden, während der die Simulation ruht
     */
    public double takeHitStop() {
        double value = feel.hitStop;
        feel.hitStop = 0;
        return value;
    }

    /**
     * @return Zeitfaktor für Zeitlupe, 1 bedeutet normal
     */
    public double timeScale() {
        return feel.slowMotion > 0 ? .35 : 1;
    }

    /** Entfernt alle laufenden Effekte. */
    public void clearEffects() {
        fx.clear();
        feel.clear();
        hud.clear();
    }

    // ---------------------------------------------------------------------------------------------
    // Ereignisse
    // ---------------------------------------------------------------------------------------------

    /**
     * Übersetzt ein Domänenereignis in Effekte.
     *
     * @param e Ereignis
     * @param run laufender Tauchgang
     * @param settings Einstellungen
     */
    public void event(GameEvent e, GameRun run, Settings settings) {
        events.apply(e, run, settings);
    }

    // ---------------------------------------------------------------------------------------------
    // Zeit
    // ---------------------------------------------------------------------------------------------

    /**
     * Lässt Effekte, Kamera und Anzeigen Zeit vergehen.
     *
     * @param dt reale Sekunden
     * @param run laufender Tauchgang oder {@code null}
     * @param settings Einstellungen
     */
    public void update(double dt, GameRun run, Settings settings) {
        time += dt;
        actors.prepare(time, look);
        feel.update(dt);
        fadeIn = Math.max(0, fadeIn - dt);
        fx.budget(settings.reducedMotion() ? .35 : 1);
        fx.update(dt);
        ocean.update(dt);
        hud.update(dt);
        if (run == null) return;
        var room = bank.room(run.room(), run.seed());
        boolean dark =
                run.room().condition() == RoomCondition.BLACKOUT
                        && run.phase() == GameRun.Phase.RUNNING;
        if (run.room() != lastRoom) {
            lastRoom = run.room();
            camera.snap(run.player().x() * PX, room.width(), W);
            fx.clear();
            fadeIn = .45;
            power = dark ? 0 : 1;
        }
        if (!dark && power < 1) {
            if (power == 0) hud.toast("Strom wiederhergestellt", Pal.RUST_6);
            power = Math.min(1, power + dt * .7);
        }
        var p = run.player();
        camera.follow(
                p.x() * PX,
                p.facing(),
                room.width(),
                W,
                dt,
                settings.reducedMotion() ? 0 : settings.screenShake());
        lowHealthPulse += dt * (p.health() < p.maxHealth() * .3 ? 5 : 0);
        if (p.dashTime() > 0) {
            feel.dashGhost -= dt;
            if (feel.dashGhost <= 0) {
                feel.dashGhost = .03;
                fx.ghost(
                        actors.playerSprite(run),
                        (int) Math.round(p.x() * PX),
                        (int) Math.round(p.y() * PX),
                        p.facing() < 0,
                        Pal.TEAL_5,
                        .25);
            }
        }
        ambientParticles(run, dt, room);
    }

    /** Hüllenbruch: Wasser steigt über den Boden und wird nach dem Abdichten abgepumpt. */
    private void drawFlood(GameRun run, int camY) {
        double level = run.breachLevel();
        if (level <= 0) return;
        int floor = RoomArt.FLOOR + camY, depth = (int) Math.round(level * 22);
        for (int x = 0; x < W; x++) {
            int surface = floor - depth + (int) Math.round(Math.sin(x * .12 + time * 2.4) * 1.2);
            frame.fill(x, surface, 1, floor + 4 - surface, 0x5A2E6E8C);
            frame.pixel(x, surface, 0xB0A8D8E8);
            if ((x + (int) (time * 20)) % 17 == 0) emissive.add(x, surface, 0xFF4A8AA8, .4);
        }
    }

    /** Druckleck: Wasserstrahlen von der Decke über den zusätzlichen Dampfaustritten. */
    private void drawLeaks(GameRun run, int camX, int camY) {
        if (run.room().condition() != RoomCondition.LEAK) return;
        int floor = RoomArt.FLOOR + camY;
        for (var h : run.hazards()) {
            if (h.kind() != Hazard.Kind.STEAM) continue;
            int x = px(h.x()) - camX + 16;
            if (x < -16 || x > W + 16) continue;
            for (int y = 22 + camY; y < floor; y++) {
                double phase = (time * 110 + y + x) % 11;
                frame.pixel(x, y, phase < 5 ? 0xB08FD4F0 : 0x605A9AB8);
                frame.pixel(x + 1, y, phase < 7 ? 0x807FC8E8 : 0x405A9AB8);
                if (phase < 2) frame.pixel(x - 1, y, 0x607FC8E8);
                if (phase < 5) emissive.add(x, y, 0xFF3A7A9A, .15);
            }
            frame.fill(x - 1, 20 + camY, 4, 3, 0xFF2A3440);
            for (int dx = -14; dx <= 14; dx++)
                frame.pixel(
                        x + dx, floor, Frame.alpha(0xFF6FB8D8, .55 * (1 - Math.abs(dx) / 15.0)));
        }
    }

    private void ambientParticles(GameRun run, double dt, RoomArt room) {
        var r = fx.random();
        machines.particles(run, fx, dt);
        var p = run.player();
        if (run.room().condition() == RoomCondition.LEAK)
            for (var h : run.hazards())
                if (h.kind() == Hazard.Kind.STEAM && r.nextDouble() < dt * 14)
                    fx.burst(
                            Effects.Kind.GOO,
                            h.x() * PX + 16,
                            RoomArt.FLOOR - 1,
                            2,
                            40,
                            -Math.PI / 2,
                            1.6,
                            0xFF8FD4F0);
        if (run.room().condition() == RoomCondition.LEAK && r.nextDouble() < dt * 7)
            fx.burst(
                    Effects.Kind.GOO,
                    camera.left() + r.nextInt(W),
                    26 + r.nextInt(12),
                    1,
                    8,
                    Math.PI / 2,
                    .2,
                    0xFF7FB8D8);
        if (p.statuses().active(Status.BURN) && r.nextDouble() < dt * 25)
            fx.burst(
                    Effects.Kind.FLAME,
                    p.x() * PX,
                    p.centerY() * PX,
                    1,
                    20,
                    -Math.PI / 2,
                    1,
                    Pal.RUST_6);
        if (p.overdriveTime() > 0 && r.nextDouble() < dt * 20)
            fx.burst(
                    Effects.Kind.SPARK,
                    p.x() * PX,
                    p.centerY() * PX,
                    1,
                    60,
                    r.nextDouble() * 6.3,
                    1,
                    Pal.RUST_6);
        for (var e : run.enemies()) {
            if (!e.alive()) continue;
            double ex = e.x() * PX, ey = e.centerY() * PX;
            if (e.statuses().active(Status.BURN) && r.nextDouble() < dt * 18)
                fx.burst(Effects.Kind.FLAME, ex, ey, 1, 20, -Math.PI / 2, 1, Pal.RUST_6);
            if (e.affix().elite() && r.nextDouble() < dt * 8)
                fx.burst(
                        Effects.Kind.EMBER,
                        ex,
                        ey,
                        1,
                        20,
                        -Math.PI / 2,
                        2,
                        ActorPainter.affixColor(e));
            if (e.kind() == EnemyKind.EEL
                    && e.state() == Enemy.State.HIDDEN
                    && r.nextDouble() < dt * 6)
                fx.burst(
                        Effects.Kind.BUBBLE,
                        ex,
                        RoomArt.FLOOR - 1,
                        1,
                        20,
                        -Math.PI / 2,
                        1,
                        Pal.TEAL_6);
            if (e.kind() == EnemyKind.WELDER && e.state() == Enemy.State.STRIKE) {
                double nozzle = ex + e.facing() * 13;
                for (int i = 0; i < 3; i++)
                    fx.burst(
                            Effects.Kind.FLAME,
                            nozzle,
                            e.y() * PX - 18,
                            1,
                            220,
                            e.facing() > 0 ? 0 : Math.PI,
                            .35,
                            Pal.RUST_6);
            }
            if (e.kind().boss() && e.healthRatio() < .5 && r.nextDouble() < dt * 6)
                fx.burst(
                        Effects.Kind.SMOKE,
                        ex + r.nextInt(20) - 10,
                        e.y() * PX - e.height() * PX,
                        1,
                        15,
                        -Math.PI / 2,
                        1,
                        0xFF2A2828);
        }
        for (var h : run.hazards()) {
            double hx = h.x() * PX;
            if (h.kind() == Hazard.Kind.STEAM
                    && (h.active() || h.warning() && r.nextDouble() < .2)
                    && r.nextDouble() < dt * (h.active() ? 60 : 8))
                fx.burst(
                        Effects.Kind.STEAM,
                        hx + r.nextInt(30) - 15,
                        RoomArt.FLOOR - 2,
                        1,
                        h.active() ? 120 : 30,
                        -Math.PI / 2,
                        .4,
                        0xFFC8D8DC);
            if (h.kind() == Hazard.Kind.FIRE && r.nextDouble() < dt * 30)
                fx.burst(
                        Effects.Kind.FLAME,
                        hx + r.nextInt((int) Math.max(2, h.width() * PX)) - h.width() * PX / 2,
                        RoomArt.FLOOR - 1,
                        1,
                        40,
                        -Math.PI / 2,
                        .6,
                        Pal.RUST_6);
            if (h.kind() == Hazard.Kind.ACID && r.nextDouble() < dt * 10)
                fx.burst(
                        Effects.Kind.BUBBLE,
                        hx + r.nextInt(30) - 15,
                        RoomArt.FLOOR - 1,
                        1,
                        10,
                        -Math.PI / 2,
                        1,
                        Pal.GREEN_4);
        }
        for (var prop : room.props())
            if (prop.kind() == RoomArt.PropKind.STEAM && r.nextDouble() < dt * 2)
                fx.burst(
                        Effects.Kind.STEAM,
                        prop.x() + 2,
                        prop.y(),
                        1,
                        30,
                        -Math.PI / 2,
                        .5,
                        0xFFB0C0C4);
    }

    // ---------------------------------------------------------------------------------------------
    // Zeichnen
    // ---------------------------------------------------------------------------------------------

    /**
     * Zeichnet den Tauchgang samt HUD.
     *
     * @param run laufender Tauchgang
     * @param settings Einstellungen
     * @param showHud HUD anzeigen
     */
    public void render(GameRun run, Settings settings, boolean showHud) {
        var room = bank.room(run.room(), run.seed());
        actors.prepare(time, look);
        boolean calm = settings.reducedMotion();
        int camX = camera.left(), camY = calm ? 0 : camera.offsetY();
        emissive.clear(0);
        ocean.draw(frame, emissive, camX, run.room().sector(), calm);
        frame.copyStrip(room.pixels(), room.width(), camX, camY, RoomArt.HEIGHT);
        drawProps(room, camX, camY, calm);
        fx.drawDecals(frame, camX, camY);
        for (var h : run.hazards()) drawHazard(h, camX, camY);
        drawLeaks(run, camX, camY);
        drawFixtures(run, camX, camY);
        machines.drawBack(run, camX, camY, calm ? 0 : time);
        for (var crate : run.crates()) {
            if (!crate.intact()) continue;
            int shake = crate.hitTime() > 0 ? (int) Math.round(Math.sin(time * 90) * 1.5) : 0;
            var sprite = PropArt.crate(crate.kind());
            frame.draw(sprite, px(crate.x()) - camX + shake, px(crate.y()) + camY, false);
            if (sprite.glow() != null)
                emissive.drawAdd(
                        sprite.glow(),
                        px(crate.x()) - camX + shake,
                        px(crate.y()) + camY,
                        false,
                        .8);
        }
        for (var e : run.enemies()) if (e.alive()) drawTelegraph(e, run, camX, camY);
        for (var e : run.enemies()) if (e.alive()) actors.drawEnemy(e, camX, camY);
        actors.drawPlayer(run, camX, camY);
        for (var q : run.projectiles()) drawProjectile(q, run, camX, camY);
        for (var pickup : run.pickups()) drawPickup(pickup, camX, camY);
        actors.drawSmear(run.player(), camX, camY);
        machines.drawFront(run, camX, camY, time);
        drawFlood(run, camY);
        fx.draw(frame, emissive, camX, camY);

        lightScene(run, room, camX, calm);
        lights.apply(frame, emissive, 0);
        drawForeground(room, camX, camY);
        post.bloom(frame, emissive, .85);
        int sector = run.room().sector();
        int[] grade = grade(sector);
        post.grade(frame, grade[0], grade[1], .8, 0);
        double lowHealth =
                run.player().health() < run.player().maxHealth() * .3
                        ? .35 + .2 * Math.sin(lowHealthPulse)
                        : 0;
        post.vignette(frame, .55, Pal.INK);
        if (lowHealth > 0) post.vignette(frame, lowHealth, Pal.RED_1);
        if (feel.aberration > 0 && !calm) post.aberration(frame, feel.aberration > .12 ? 2 : 1);
        if (feel.flash > 0) post.flash(frame, feel.flashColor, Math.min(.6, feel.flash));
        if (!calm) tilt(run.listTilt());
        if (fadeIn > 0) bulkhead(Math.min(1, fadeIn / .45), calm);
        fx.drawTexts(frame, font, camX, camY);
        drawPrompts(run, camX, camY);
        drawOffscreenMarkers(run, camX);
        if (showHud) hud.draw(frame, run, time);
    }

    private int[] tiltBuffer = new int[0];

    /** Schlagseite: verschiebt die Spalten gegeneinander, das Bild wirkt gekippt. */
    private void tilt(double amount) {
        if (Math.abs(amount) < .02) return;
        int[] px = frame.pixels();
        if (tiltBuffer.length != px.length) tiltBuffer = new int[px.length];
        System.arraycopy(px, 0, tiltBuffer, 0, px.length);
        for (int x = 0; x < W; x++) {
            int offset = (int) Math.round(amount * 6 * (x - W / 2.0) / (W / 2.0));
            for (int y = 0; y < H; y++) {
                int sy = Math.max(0, Math.min(H - 1, y - offset));
                px[y * W + x] = tiltBuffer[sy * W + x];
            }
        }
    }

    /**
     * Raumwechsel: Zwei Schotthälften gleiten auseinander.
     *
     * @param closed 1 geschlossen, 0 offen
     * @param calm ruhige Darstellung ersetzt die Bewegung durch eine Blende
     */
    private void bulkhead(double closed, boolean calm) {
        if (calm) {
            post.flash(frame, Pal.INK, closed);
            return;
        }
        double ease = closed * closed * (3 - 2 * closed);
        int half = (int) Math.round(W / 2.0 * ease);
        if (half <= 0) return;
        for (int side = 0; side < 2; side++) {
            int x0 = side == 0 ? 0 : W - half;
            int edge = side == 0 ? half - 1 : W - half;
            frame.fill(x0, 0, half, H, 0xFF1C2530);
            for (int y = 0; y < H; y += 30) {
                frame.fill(x0, y, half, 1, 0xFF2E3A48);
                frame.fill(x0, y + 29, half, 1, 0xFF10161E);
                for (int x = x0 + 6; x < x0 + half - 3; x += 24) frame.pixel(x, y + 4, 0xFF4E6072);
            }
            // Warnstreifen an der Kante
            for (int y = 0; y < H; y++)
                for (int k = 0; k < 8; k++) {
                    int x = side == 0 ? edge - k : edge + k;
                    boolean stripe = ((y + (side == 0 ? k : -k)) / 6) % 2 == 0;
                    frame.pixel(x, y, stripe ? 0xFFE0A030 : 0xFF14100A);
                }
            frame.fill(side == 0 ? edge : edge - 1, 0, 2, H, Pal.OUTLINE);
        }
    }

    private static int px(double units) {
        return (int) Math.round(units * PX);
    }

    private static int[] grade(int sector) {
        return switch (sector) {
            case 0 -> new int[] {0xFF301808, 0xFFFFC890};
            case 1 -> new int[] {0xFF0A2A1A, 0xFFB0FFD0};
            case 2 -> new int[] {0xFF1A1036, 0xFFE0C8FF};
            default -> new int[] {0xFF0A1A36, 0xFFB8E0FF};
        };
    }

    private void drawProps(RoomArt room, int camX, int camY, boolean calm) {
        double t = calm ? 0 : time;
        for (var prop : room.props()) {
            int x = prop.x() - camX, y = prop.y() + camY;
            if (x + prop.w() < -4 || x > W + 4) continue;
            switch (prop.kind()) {
                case FAN -> {
                    int cx = x + prop.w() / 2, cy = y + prop.h() / 2, r = prop.w() / 2 - 1;
                    for (int b = 0; b < 5; b++) {
                        double a = t * 4 + b * Math.PI * 2 / 5;
                        for (int k = 2; k < r; k++) {
                            double aa = a + k * .04;
                            frame.pixel(
                                    (int) Math.round(cx + Math.cos(aa) * k),
                                    (int) Math.round(cy + Math.sin(aa) * k),
                                    prop.color());
                            frame.pixel(
                                    (int) Math.round(cx + Math.cos(aa + .12) * k),
                                    (int) Math.round(cy + Math.sin(aa + .12) * k),
                                    Pal.shade(prop.color(), .6));
                        }
                    }
                    frame.disc(cx, cy, 2, Pal.STEEL_5);
                }
                case SCREEN -> {
                    for (int yy = 0; yy < prop.h(); yy++)
                        for (int xx = 0; xx < prop.w(); xx++) {
                            boolean on =
                                    ((int) (t * 8) + yy) % 3 != 0
                                            && ((xx * 7 + yy * 3 + (int) (t * 3)) % 11 < 6);
                            int c = on ? prop.color() : Pal.mix(prop.color(), Pal.INK, .8);
                            frame.pixel(x + xx, y + yy, c);
                            if (on) emissive.add(x + xx, y + yy, prop.color(), .35);
                        }
                }
                case BLINK -> {
                    for (int xx = 0; xx < prop.w(); xx += 3) {
                        boolean on = Math.sin(t * 3 + xx * 1.7 + prop.x()) > 0;
                        int c = on ? prop.color() : Pal.mix(prop.color(), Pal.INK, .7);
                        frame.pixel(x + xx, y, c);
                        if (on) emissive.add(x + xx, y, prop.color(), .9);
                    }
                }
                case CORE -> {
                    for (int yy = 0; yy < prop.h(); yy++) {
                        double wave = .6 + .4 * Math.sin(t * 2 - yy * .12);
                        for (int xx = 2; xx < prop.w() - 2; xx++) {
                            double edge = Math.min(xx, prop.w() - xx) / (prop.w() / 2.0);
                            int c = Pal.mix(Pal.GREEN_1, Pal.GREEN_5, wave * edge);
                            frame.pixel(x + xx, y + yy, c);
                            emissive.add(x + xx, y + yy, Pal.GREEN_4, .45 * wave * edge);
                        }
                    }
                }
                case TANK -> {
                    for (int k = 0; k < 5; k++) {
                        double by = y + prop.h() - ((t * 16 + k * 23 + prop.x()) % prop.h());
                        int bx =
                                x
                                        + 4
                                        + (k * 7) % Math.max(1, prop.w() - 8)
                                        + (int) Math.round(Math.sin(t * 3 + k) * 1.5);
                        frame.pixel(bx, (int) by, Pal.mix(prop.color(), Pal.WHITE, .5));
                        emissive.add(bx, (int) by, prop.color(), .4);
                    }
                }
                case BEACON -> {
                    double a = t * 5 + prop.x();
                    boolean facing = Math.cos(a) > 0;
                    int c = facing ? prop.color() : Pal.RED_1;
                    frame.fill(x + 1, y + 1, 3, 3, c);
                    if (facing) emissive.glow(x + 2, y + 2, 6, prop.color(), .8);
                }
                case WATER -> {
                    for (int xx = 0; xx < prop.w(); xx++) {
                        int yy = y + (int) Math.round(Math.sin(t * 2 + (xx + prop.x()) * .3));
                        frame.pixel(x + xx, yy, Pal.mix(prop.color(), Pal.INK, .6));
                        if ((xx + (int) (t * 4)) % 9 == 0)
                            emissive.add(x + xx, yy, prop.color(), .3);
                    }
                }
                case CANDLE -> {
                    boolean flick = Math.sin(t * 13 + prop.x() * 3) > -.3;
                    emissive.add(x, y, prop.color(), flick ? .9 : .5);
                    if (flick) frame.pixel(x, y - 1, Pal.RUST_7);
                }
                case STEAM, DRIP -> {}
            }
        }
    }

    private void drawFixtures(GameRun run, int camX, int camY) {
        var layout = run.layout();
        int rx = px(layout.rewardX()) - camX, floor = RoomArt.FLOOR + camY;
        int frameIndex = (int) (time * 5) % 4;
        switch (run.room().kind()) {
            case MERCHANT -> drawSprite(PropArt.merchant(frameIndex), rx, floor, false, 0, 0);
            case SHRINE ->
                    drawSprite(PropArt.shrine(frameIndex, run.dealTaken()), rx, floor, false, 0, 0);
            case WORKSHOP -> drawSprite(PropArt.workbench(frameIndex), rx, floor, false, 0, 0);
            default -> {
                if (run.phase() == GameRun.Phase.ROOM_CLEARED
                        && run.room().kind() != RoomPlan.Kind.BRIDGE) {
                    boolean open = !run.rewardAvailable();
                    drawSprite(PropArt.pod(open, frameIndex), rx, floor, false, 0, 0);
                    if (!open) {
                        emissive.glow(
                                rx, floor - 16, 18, Pal.TEAL_5, .25 + .1 * Math.sin(time * 4));
                        if (fx.random().nextDouble() < .05)
                            fx.burst(
                                    Effects.Kind.BUBBLE,
                                    px(layout.rewardX()),
                                    RoomArt.FLOOR - 20,
                                    1,
                                    20,
                                    -Math.PI / 2,
                                    1,
                                    Pal.TEAL_6);
                    }
                }
            }
        }
        int doorOpen =
                run.phase() == GameRun.Phase.ROOM_CLEARED
                        ? Math.min(4, (int) (run.roomTime() * 6) % 100)
                        : 0;
        if (run.phase() == GameRun.Phase.ROOM_CLEARED) doorOpen = 4;
        int dx = px(layout.width()) - 15 - camX;
        drawSprite(PropArt.door(doorOpen), dx, floor, false, 0, 0);
        if (run.phase() == GameRun.Phase.ROOM_CLEARED) {
            int ax = dx - 26 + (int) Math.round(Math.sin(time * 5) * 3);
            for (int i = 0; i < 4; i++) {
                frame.pixel(ax + i, floor - 30 - i, Pal.TEAL_5);
                frame.pixel(ax + i, floor - 30 + i, Pal.TEAL_5);
                emissive.add(ax + i, floor - 30 - i, Pal.TEAL_5, .8);
                emissive.add(ax + i, floor - 30 + i, Pal.TEAL_5, .8);
            }
        }
    }

    private void drawSprite(Sprite sprite, int x, int y, boolean flip, int tint, double amount) {
        frame.draw(sprite, x, y, flip, tint, amount, 1);
        if (sprite.glow() != null) emissive.drawAdd(sprite.glow(), x, y, flip, 1);
    }

    private void drawHazard(Hazard h, int camX, int camY) {
        int x = px(h.x()) - camX,
                w = (int) Math.round(h.width() * PX),
                floor = RoomArt.FLOOR + camY;
        int left = x - w / 2;
        switch (h.kind()) {
            case STEAM, ELECTRIC -> {
                frame.fill(left, floor - 1, w, 3, Pal.STEEL_1);
                for (int i = left + 1; i < left + w; i += 3) frame.pixel(i, floor, Pal.STEEL_3);
                int warn = h.kind() == Hazard.Kind.STEAM ? Pal.RUST_5 : Pal.TEAL_5;
                if (h.warning()) {
                    boolean blink = ((int) (time * 10)) % 2 == 0;
                    for (int i = left; i < left + w; i += 2) {
                        frame.pixel(i, floor - 2, blink ? warn : Pal.INK);
                        if (blink) emissive.add(i, floor - 2, warn, .7);
                    }
                }
                if (h.active() && h.kind() == Hazard.Kind.ELECTRIC) {
                    var r = fx.random();
                    for (int k = 0; k < 3; k++) {
                        int x1 = left + r.nextInt(Math.max(1, w)),
                                x2 = left + r.nextInt(Math.max(1, w));
                        int y2 = floor - 4 - r.nextInt(8);
                        emissive.lineAdd(x1, floor - 1, x2, y2, Pal.TEAL_5, 1);
                        frame.line(x1, floor - 1, x2, y2, Pal.TEAL_6);
                    }
                }
            }
            case ACID -> {
                for (int i = left; i < left + w; i++) {
                    int c = ((i + (int) (time * 6)) % 5 == 0) ? Pal.GREEN_5 : Pal.GREEN_3;
                    frame.pixel(i, floor - 1, c);
                    frame.pixel(i, floor, Pal.GREEN_2);
                    emissive.add(i, floor - 1, Pal.GREEN_4, .5);
                }
            }
            case FIRE -> {
                for (int i = left; i < left + w; i += 2) emissive.add(i, floor - 1, Pal.RUST_6, .6);
            }
            case BARRAGE -> {
                if (h.warning()) {
                    boolean blink = ((int) (time * 12)) % 2 == 0;
                    for (int yy = 30; yy < floor; yy += 2) {
                        frame.pixel(left, yy, Frame.alpha(Pal.RED_4, .5));
                        frame.pixel(left + w - 1, yy, Frame.alpha(Pal.RED_4, .5));
                    }
                    for (int i = left; i < left + w; i++) {
                        frame.pixel(i, floor - 1, blink ? Pal.RED_4 : Pal.RED_2);
                        if (blink) emissive.add(i, floor - 1, Pal.RED_4, .8);
                    }
                } else if (h.active() && detonated.add(h)) {
                    fx.explosion(px(h.x()), RoomArt.FLOOR - 8, w * .6, false);
                    fx.bolt(px(h.x()), 0, px(h.x()), RoomArt.FLOOR, .12, Pal.RUST_6);
                    camera.shake(.15);
                }
            }
        }
    }

    private void drawTelegraph(Enemy e, GameRun run, int camX, int camY) {
        var t = e.telegraph();
        if (t == null) return;
        boolean blink = ((int) (time * 14)) % 2 == 0;
        int color = blink ? Pal.RED_4 : Pal.RED_3;
        switch (t.shape()) {
            case AIM -> {
                int x1 = px(t.x1()) - camX,
                        y1 = px(t.y1()) + camY,
                        x2 = px(t.x2()) - camX,
                        y2 = px(t.y2()) + camY;
                double len = Math.hypot(x2 - x1, y2 - y1);
                for (int i = 0; i < len; i += 2) {
                    if ((i / 4) % 2 == 1) continue;
                    int x = (int) Math.round(x1 + (x2 - x1) * i / len),
                            y = (int) Math.round(y1 + (y2 - y1) * i / len);
                    frame.pixel(x, y, Frame.alpha(color, .8));
                    emissive.add(x, y, Pal.RED_4, .5);
                }
                frame.circle(x2, y2, 4, color);
                emissive.add(x2, y2, Pal.RED_4, 1);
            }
            case FLOOR -> {
                int a = px(Math.min(t.x1(), t.x2())) - camX,
                        b = px(Math.max(t.x1(), t.x2())) - camX;
                int y = px(t.y1() > 0 ? t.y1() : GameRun.FLOOR) + camY;
                for (int x = a; x <= b; x++) {
                    boolean stripe = ((x + (int) (time * 20)) / 3) % 2 == 0;
                    frame.pixel(x, y - 1, stripe ? color : Frame.alpha(Pal.RED_2, .6));
                    if (stripe) emissive.add(x, y - 1, Pal.RED_4, .7);
                    if (stripe && blink) frame.pixel(x, y - 2, Frame.alpha(Pal.RED_4, .5));
                }
            }
            case CIRCLE -> {
                int r = (int) Math.round(t.x2() * PX);
                int cx = px(t.x1()) - camX, cy = px(t.y1()) + camY;
                int steps = Math.max(16, r * 5);
                for (int i = 0; i < steps; i += 2) {
                    double ang = i * Math.PI * 2 / steps + time;
                    int x = (int) Math.round(cx + Math.cos(ang) * r),
                            y = (int) Math.round(cy + Math.sin(ang) * r);
                    frame.pixel(x, y, color);
                    emissive.add(x, y, Pal.RED_4, .6);
                }
            }
            case BEAM -> {
                int y = px(t.y1()) + camY;
                double progress = 1 - Math.max(0, e.stateTime()) / 1.1;
                for (int x = 0; x < W; x++) {
                    if (((x + (int) (time * 30)) / 4) % 2 == 0) frame.pixel(x, y, color);
                    emissive.add(x, y, Pal.GREEN_4, .25 + .5 * progress);
                }
                if (progress > .6) {
                    for (int x = 0; x < W; x += 2) emissive.add(x, y - 1, Pal.GREEN_4, .4);
                }
            }
            case COLUMN -> {
                int x = px(t.x1()) - camX, r = (int) Math.round(t.x2() * PX);
                for (int yy = 20; yy < RoomArt.FLOOR; yy += 3) {
                    frame.pixel(x - r, yy + camY, Frame.alpha(Pal.RED_4, .5));
                    frame.pixel(x + r, yy + camY, Frame.alpha(Pal.RED_4, .5));
                }
                for (int xx = x - r; xx <= x + r; xx++) {
                    frame.pixel(xx, RoomArt.FLOOR - 1 + camY, color);
                    emissive.add(xx, RoomArt.FLOOR - 1 + camY, Pal.RED_4, .6);
                }
            }
        }
    }

    private void drawProjectile(Projectile q, GameRun run, int camX, int camY) {
        int x = px(q.x()) - camX, y = px(q.y()) + camY;
        boolean right = q.vx() >= 0;
        switch (q.kind()) {
            case BOLT -> {
                int tx = x - (int) Math.round(q.vx() * .012),
                        ty = y - (int) Math.round(q.vy() * .012);
                frame.line(tx, ty, x, y, Pal.RED_3);
                frame.fill(x - 1, y - 1, 3, 3, Pal.RUST_6);
                frame.pixel(x, y, Pal.WHITE);
                emissive.glow(x, y, 5, Pal.RED_4, .9);
            }
            case DRONE_SHOT -> {
                frame.line(x - (right ? 3 : -3), y, x, y, Pal.TEAL_5);
                emissive.glow(x, y, 3, Pal.TEAL_5, .9);
            }
            case ARC -> {
                var r = fx.random();
                for (int k = 0; k < 4; k++) {
                    int ex = x + r.nextInt(13) - 6, ey = y + r.nextInt(13) - 6;
                    frame.line(x, y, ex, ey, Pal.TEAL_6);
                    emissive.lineAdd(x, y, ex, ey, Pal.TEAL_5, 1);
                }
                frame.disc(x, y, 3, Pal.WHITE);
                emissive.glow(x, y, 9, Pal.TEAL_5, 1);
            }
            case SHOCKWAVE -> {
                int color = q.friendly() ? Pal.TEAL_5 : Pal.RUST_5;
                int floor = RoomArt.FLOOR + camY;
                for (int k = 0; k < 7; k++) {
                    int h = 8 - Math.abs(k - 3) * 2;
                    int xx = x + (right ? -k : k);
                    for (int yy = 0; yy < h; yy++) {
                        frame.pixel(xx, floor - 1 - yy, yy == h - 1 ? Pal.WHITE : color);
                        emissive.add(xx, floor - 1 - yy, color, .8);
                    }
                }
                if (fx.random().nextDouble() < .5)
                    fx.burst(
                            Effects.Kind.DUST,
                            px(q.x()),
                            RoomArt.FLOOR - 1,
                            1,
                            30,
                            -Math.PI / 2,
                            1,
                            Pal.STEEL_6);
            }
            case HARPOON, ENEMY_HARPOON -> {
                var sprite =
                        PropArt.small(q.kind() == Projectile.Kind.HARPOON ? "harpoon" : "eharpoon");
                frame.draw(sprite, x, y, !right);
                if (sprite.glow() != null) emissive.drawAdd(sprite.glow(), x, y, !right, 1);
                for (int k = 6; k < 20; k += 2)
                    frame.pixel(x - (right ? k : -k), y, Frame.alpha(Pal.STEEL_6, .4));
            }
            case TORPEDO -> {
                var sprite = PropArt.small("torpedo");
                double angle = Math.atan2(q.vy(), q.vx());
                frame.draw(sprite, x, y, Math.cos(angle) < 0);
                emissive.drawAdd(sprite.glow(), x, y, Math.cos(angle) < 0, 1);
                if (fx.random().nextDouble() < .6)
                    fx.burst(
                            Effects.Kind.BUBBLE,
                            px(q.x()) - (right ? 5 : -5),
                            px(q.y()),
                            1,
                            20,
                            right ? Math.PI : 0,
                            1,
                            Pal.TEAL_6);
            }
            case MINE -> {
                var sprite = PropArt.small("mine");
                frame.draw(sprite, x, y, false);
                boolean on = q.armed() ? ((int) (time * 10)) % 2 == 0 : ((int) (time * 3)) % 2 == 0;
                frame.pixel(x, y - 2, on ? Pal.RED_4 : Pal.RED_1);
                if (on) emissive.glow(x, y - 2, q.armed() ? 6 : 3, Pal.RED_4, .9);
            }
            case ACID -> {
                frame.disc(x, y, 2, Pal.GREEN_4);
                frame.pixel(x - 1, y - 1, Pal.GREEN_5);
                emissive.glow(x, y, 5, Pal.GREEN_4, .7);
            }
            case CRYO -> {
                var sprite = PropArt.small("cryo");
                frame.draw(sprite, x, y, false);
                emissive.glow(x, y, 5, Pal.TEAL_5, .7);
            }
            case AFTERIMAGE -> {
                var sprite = actors.playerSprite(run);
                double a = Math.max(0, 1 - q.age() / .55);
                frame.silhouette(
                        sprite,
                        x,
                        y + 17,
                        run.player().facing() < 0,
                        Frame.alpha(Pal.VIOLET_4, .5 * a));
                emissive.glow(x, y, 10, Pal.VIOLET_4, .5 * a);
            }
        }
    }

    private void drawPickup(Pickup pickup, int camX, int camY) {
        int x = px(pickup.x()) - camX, y = px(pickup.y()) + camY - 2;
        String name =
                switch (pickup.kind()) {
                    case SCRAP -> ((int) (time * 8 + pickup.id())) % 2 == 0 ? "scrap0" : "scrap1";
                    case HEALTH -> "health";
                    case ENERGY -> "energy";
                    case CORE -> "core";
                };
        var sprite = PropArt.small(name);
        frame.draw(sprite, x, y, false);
        if (sprite.glow() != null) emissive.drawAdd(sprite.glow(), x, y, false, 1);
        if (pickup.kind() == Pickup.Kind.CORE)
            emissive.glow(x, y, 8, Pal.TEAL_5, .5 + .3 * Math.sin(time * 6));
    }

    private void lightScene(GameRun run, RoomArt room, int camX, boolean calm) {
        double ambient =
                room.ambientLevel() + (run.phase() == GameRun.Phase.ROOM_CLEARED ? .06 : 0);
        if (run.room().kind() == RoomPlan.Kind.BOSS || run.room().kind() == RoomPlan.Kind.BRIDGE)
            ambient -= .04;
        double t = calm ? 0 : time;
        boolean alarm =
                run.room().condition() == RoomCondition.ALARM
                        && run.phase() == GameRun.Phase.RUNNING;
        // Beim Wiederherstellen flackert das Licht, bevor es ruhig brennt.
        double lit = power < 1 && power > 0 && !calm && Math.sin(t * 31) < -.3 ? power * .3 : power;
        ambient *= .3 + .7 * lit;
        int ambientColor = room.ambient();
        if (alarm) ambientColor = Pal.mix(ambientColor, 0xFFFF2A1A, .3 + .15 * Math.sin(t * 5));
        lights.ambient(ambientColor, ambient);
        var roomLights = room.lights();
        for (int i = 0; i < roomLights.size(); i++) {
            var l = roomLights.get(i);
            int x = l.x() - camX;
            if (x < -l.radius() || x > W + l.radius()) continue;
            if (lit < 1 && i % 3 == 0)
                lights.point(
                        x,
                        l.y(),
                        l.radius() * .6,
                        0xFFFF3A2A,
                        .45 * (1 - lit) * (.8 + .2 * Math.sin(t * 3 + i)));
            if (alarm && l.y() < 45 && i % 2 == 0) {
                double angle = t * 4.5 + i * 1.3;
                lights.cone(x, l.y() + 3, angle, .34, 170, 0xFFFF3020, .85);
                lights.point(x, l.y() + 3, 16, 0xFFFF3020, .9);
                emissive.glow(x, l.y() + 3, 2, Pal.RED_4, 1);
            }
            double intensity = l.intensity() * lit;
            switch (l.flicker()) {
                case 1 -> intensity *= .88 + .12 * Math.sin(t * 17 + l.x());
                case 2 -> intensity *= .7 + .3 * Math.sin(t * 2.2 + l.x());
                case 3 ->
                        intensity *=
                                (Math.sin(t * 23 + l.x()) > .2 || Math.sin(t * 3.1 + l.x()) > .6)
                                        ? 1
                                        : .15;
                default -> {}
            }
            lights.point(x, l.y(), l.radius(), l.color(), intensity);
            if (l.y() < 45)
                lights.cone(
                        x, l.y(), Math.PI / 2, .6, l.radius() * 1.6, l.color(), intensity * .45);
        }
        for (var w : room.windows()) {
            int x = w.x() - camX;
            if (x + w.w() < -40 || x > W + 40) continue;
            lights.point(
                    x + w.w() / 2.0,
                    w.y() + w.h() / 2.0,
                    Math.max(w.w(), w.h()) * 1.1,
                    0xFF4A9AB8,
                    .35 * (.6 + .4 * lit));
            lights.shaft(
                    x + w.w() * .2 + 8,
                    w.y() + w.h(),
                    w.w() * .6,
                    RoomArt.FLOOR - w.y() - w.h(),
                    0xFF5AA8C8,
                    .16);
        }
        var p = run.player();
        int px = px(p.x()) - camX, py = px(p.y());
        lights.point(px, py - 18, 72, 0xFFFFE0B0, .5);
        double headY = py - 32;
        lights.cone(
                px + p.facing() * 4,
                headY,
                p.facing() > 0 ? .08 : Math.PI - .08,
                .42,
                150,
                0xFFFFE6C0,
                .75);
        if (p.shieldTime() > 0) lights.point(px, py - 17, 40, Pal.TEAL_5, .6);
        for (var e : run.enemies()) {
            if (!e.alive() || e.state() == Enemy.State.HIDDEN) continue;
            int ex = px(e.x()) - camX, ey = px(e.y() - e.height() * .7);
            int color =
                    e.kind() == EnemyKind.JELLY
                            ? Pal.VIOLET_4
                            : e.kind() == EnemyKind.REACTOR
                                    ? Pal.GREEN_4
                                    : e.kind() == EnemyKind.BROOD ? Pal.TEAL_5 : Pal.RED_4;
            double radius = e.kind().boss() ? 70 : e.kind() == EnemyKind.JELLY ? 40 : 22;
            lights.point(ex, ey, radius, color, e.state() == Enemy.State.WINDUP ? .8 : .4);
            if (e.kind() == EnemyKind.SEEKER) {
                // Suchlicht: folgt der Figur, beim Zielen heller und enger
                boolean aiming = e.state() == Enemy.State.WINDUP;
                double angle = Math.atan2(py - 20 - ey, px - ex);
                lights.cone(
                        ex + e.facing() * 3,
                        ey,
                        angle,
                        aiming ? .14 : .24,
                        240,
                        0xFFFFF0C0,
                        aiming ? .95 : .5);
            }
        }
        machines.light(run, lights, camX);
        for (var q : run.projectiles()) {
            int color =
                    switch (q.kind()) {
                        case ARC, DRONE_SHOT, CRYO -> Pal.TEAL_5;
                        case ACID -> Pal.GREEN_4;
                        case AFTERIMAGE -> Pal.VIOLET_4;
                        case SHOCKWAVE -> q.friendly() ? Pal.TEAL_5 : Pal.RUST_5;
                        default -> Pal.RUST_6;
                    };
            lights.point(
                    px(q.x()) - camX,
                    px(q.y()),
                    q.kind() == Projectile.Kind.ARC ? 50 : 24,
                    color,
                    .6);
        }
        for (var h : run.hazards()) {
            if (h.kind() == Hazard.Kind.ELECTRIC && h.active())
                lights.point(
                        px(h.x()) - camX,
                        RoomArt.FLOOR - 6,
                        50,
                        Pal.TEAL_5,
                        .7 + .3 * Math.sin(time * 30));
            if (h.kind() == Hazard.Kind.FIRE || h.kind() == Hazard.Kind.ACID)
                lights.point(
                        px(h.x()) - camX,
                        RoomArt.FLOOR - 6,
                        40,
                        h.kind() == Hazard.Kind.FIRE ? Pal.RUST_5 : Pal.GREEN_4,
                        .5);
        }
        if (run.phase() == GameRun.Phase.ROOM_CLEARED && run.rewardAvailable())
            lights.point(px(run.layout().rewardX()) - camX, RoomArt.FLOOR - 16, 40, Pal.TEAL_5, .5);
        fx.light(lights, camX);
    }

    private void drawForeground(RoomArt room, int camX, int camY) {
        double parallax = 1.35;
        int dark = 0xFF03050A;
        int rim = Pal.mix(room.accent(), dark, .75);
        for (var f : room.foreground()) {
            int x = (int) Math.round(f.x() - camX * parallax);
            if (x < -40 || x > W + 40) continue;
            switch (f.kind()) {
                case 0 -> {
                    int span = 40 + f.size();
                    for (int i = 0; i <= span; i++) {
                        double t = i / (double) span;
                        int y = (int) Math.round(4 + 4 * f.size() * t * (1 - t)) + camY;
                        frame.fill(x + i, y, 1, 3, dark);
                        frame.pixel(x + i, y, rim);
                    }
                }
                case 1 -> {
                    frame.fill(x, 0, 6, H, Frame.alpha(dark, .88));
                    frame.fill(x + 1, 0, 1, H, rim);
                    for (int y = 20; y < H; y += 60) frame.fill(x - 2, y + camY, 10, 3, dark);
                }
                case 2 -> {
                    for (int i = 0; i < 60; i++)
                        frame.fill(x + i, H - 20 - i / 2 + camY, 3, 3, dark);
                }
                case 3 -> {
                    frame.fill(x, H - 34 + camY, 50, 34, dark);
                    frame.fill(x + 6, H - 44 + camY, 16, 10, dark);
                    frame.fill(x, H - 34 + camY, 50, 1, rim);
                }
                default -> {
                    for (int y = 0; y < 40 + f.size() * 2; y += 3) {
                        frame.fill(x, y + camY, 2, 2, dark);
                        frame.pixel(x + 1, y + 1 + camY, dark);
                    }
                }
            }
        }
    }

    private void drawPrompts(GameRun run, int camX, int camY) {
        var interaction = run.interaction();
        if (interaction == Interaction.NONE || interaction == Interaction.LOCKED) return;
        if (interaction == Interaction.CONSOLE) {
            for (var m : run.fixtures())
                if (m.kind() == Fixture.Kind.CONSOLE && Math.abs(m.x() - run.player().x()) < 80) {
                    int bob = (int) Math.round(Math.sin(time * 4) * 1.5);
                    hud.keyPrompt(
                            frame,
                            interaction.key(),
                            m.consoleEffect(),
                            px(m.x()) - camX,
                            RoomArt.FLOOR - 40 + camY + bob);
                }
            return;
        }
        int x, y;
        if (interaction == Interaction.EXIT) {
            x = px(run.layout().width()) - 40 - camX;
            y = RoomArt.FLOOR - 82 + camY;
        } else {
            x = px(run.layout().rewardX()) - camX;
            y =
                    RoomArt.FLOOR
                            - (run.room().kind() == RoomPlan.Kind.SHRINE
                                    ? 70
                                    : run.room().kind() == RoomPlan.Kind.MERCHANT ? 62 : 48)
                            + camY;
        }
        y += (int) Math.round(Math.sin(time * 4) * 1.5);
        hud.keyPrompt(frame, interaction.key(), interaction.label(), x, y);
    }

    private void drawOffscreenMarkers(GameRun run, int camX) {
        for (var e : run.enemies()) {
            if (!e.alive() || e.state() == Enemy.State.HIDDEN) continue;
            int x = px(e.x()) - camX;
            if (x >= -4 && x <= W + 4) continue;
            int y = Math.max(40, Math.min(RoomArt.FLOOR - 6, px(e.centerY())));
            int edge = x < 0 ? 3 : W - 4;
            int dir = x < 0 ? -1 : 1;
            int color = e.kind().boss() ? Pal.RUST_6 : Pal.RED_4;
            for (int i = 0; i < 3; i++) {
                frame.pixel(edge + dir * i, y - (2 - i), color);
                frame.pixel(edge + dir * i, y + (2 - i), color);
            }
            frame.pixel(edge, y, color);
        }
    }
}
