package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.Actor;
import ch.zhaw.abyss.domain.Enemy;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.GameEvent;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Hazard;
import ch.zhaw.abyss.domain.Projectile;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Upgrade;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.TextAlignment;

/** Ausschliesslich Darstellung: keine Treffer-, Fortschritts- oder Inventarregeln. */
public final class GameRenderer {
    public static final Color INK = Color.web("#06131c"),
            CYAN = Color.web("#80e0dc"),
            AMBER = Color.web("#eaba72"),
            TEXT = Color.web("#edf0e9");
    private final Canvas canvas;
    private final AssetCatalog assets;
    private final ParticleField particles = new ParticleField();
    private double clock, shake, pulseTime, pulseX, pulseY;
    private String toast = "";
    private double toastTime;

    public GameRenderer(Canvas canvas, AssetCatalog assets) {
        this.canvas = canvas;
        this.assets = assets;
    }

    public void update(double dt) {
        clock += dt;
        shake = Math.max(0, shake - dt * 24);
        pulseTime = Math.max(0, pulseTime - dt);
        toastTime = Math.max(0, toastTime - dt);
        particles.update(dt);
    }

    public void toast(String message) {
        if (!message.isBlank()) {
            toast = message;
            toastTime = 4;
        }
    }

    public void clearEffects() {
        particles.clear();
        shake = 0;
        pulseTime = 0;
    }

    public void event(GameEvent event, Settings settings) {
        particles.event(event, settings.reducedMotion());
        if (!settings.reducedMotion()) {
            if (event.type() == GameEvent.Type.PLAYER_HIT) shake = 6;
            else if (event.type() == GameEvent.Type.ENEMY_DOWN) shake = 3;
        }
        if (event.type() == GameEvent.Type.PULSE && event.amount() > 0) {
            pulseTime = .45;
            pulseX = event.x();
            pulseY = event.y();
        }
        switch (event.type()) {
            case UPGRADE -> toast(event.text() + " installiert");
            case HEAL -> toast("Reserven aufgefüllt");
            case BOSS_PHASE -> toast("DER LOTSE · Notfallprotokoll aktiviert");
            case REINFORCEMENTS -> toast("VERSTÄRKUNG · Nächste Patrouille trifft ein");
            default -> {}
        }
    }

    public void render(GameRun run, Settings settings, boolean title, boolean chrome) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setGlobalAlpha(1);
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(INK);
        g.fillRect(0, 0, 1600, 900);
        if (title || run == null) {
            drawTitle(g, settings, chrome);
            if (chrome) drawToast(g);
            return;
        }
        g.save();
        if (shake > 0 && !settings.reducedMotion())
            g.translate(Math.sin(clock * 143) * shake, Math.cos(clock * 167) * shake * .5);
        var background = assets.room(run.room());
        if (background != null) g.drawImage(background, 0, 0, 1600, 900);
        ambient(g, run.room().sector(), settings);
        drawDoor(g, run);
        for (Hazard hazard : run.hazards())
            drawHazard(g, hazard, run.phase() == GameRun.Phase.RUNNING, settings.reducedMotion());
        if (run.phase() == GameRun.Phase.ROOM_CLEARED) drawReward(g, run);
        for (Enemy enemy : run.enemies()) if (enemy.alive()) drawTelegraph(g, enemy);
        for (Enemy enemy : run.enemies())
            if (enemy.alive()) drawActor(g, enemy, enemy.kind().name().toLowerCase(), run);
        drawActor(g, run.player(), "player", run);
        for (Projectile p : run.projectiles()) drawProjectile(g, p);
        drawPlayerEffects(g, run);
        particles.draw(g, assets);
        if (pulseTime > 0) {
            double r = (1 - pulseTime / .45) * 300;
            g.setStroke(Color.rgb(120, 240, 235, Math.min(.8, pulseTime * 2)));
            g.setLineWidth(4);
            g.strokeOval(pulseX - r, pulseY - r, r * 2, r * 2);
        }
        g.restore();
        vignette(g);
        if (chrome) {
            hud(g, run);
            drawToast(g);
        }
    }

    private void drawToast(GraphicsContext g) {
        if (toastTime > 0) {
            g.setGlobalAlpha(Math.min(1, toastTime));
            g.setFill(Color.rgb(5, 20, 27, .92));
            g.fillRoundRect(465, 157, 670, 48, 8, 8);
            g.setStroke(Color.rgb(128, 224, 220, .3));
            g.strokeRoundRect(465, 157, 670, 48, 8, 8);
            text(g, toast, 800, 188, 22, CYAN, true);
            g.setGlobalAlpha(1);
        }
    }

    private void drawTitle(GraphicsContext g, Settings settings, boolean chrome) {
        var background = assets.image("art/title-submarine.png");
        if (background != null) g.drawImage(background, 0, 0, 1600, 900);
        g.setFill(
                new LinearGradient(
                        0,
                        0,
                        0,
                        1,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(2, 13, 23, .38)),
                        new Stop(.48, Color.TRANSPARENT),
                        new Stop(1, Color.rgb(2, 11, 18, .91))));
        g.fillRect(0, 0, 1600, 900);
        ambient(g, 2, settings);
        if (!chrome) return;
        g.setStroke(Color.rgb(128, 224, 220, .35));
        g.setLineWidth(1);
        g.strokeLine(72, 67, 1528, 67);
        text(g, "TIEFSEEEXPEDITION / HEK–07", 72, 49, 17, CYAN, false);
        text(g, "SIGNAL EMPFANGEN     ·     2 840 M", 1270, 49, 17, Color.web("#94b2bf"), false);
        text(g, "A B Y S S", 72, 238, 158, TEXT, false);
        text(g, "VOM HECK BIS ZUR BRÜCKE.", 79, 291, 31, AMBER, false);
        text(
                g,
                "Ein Boot. Zwölf Räume. Dein nächster Versuch.",
                80,
                333,
                22,
                Color.web("#a1bac4"),
                false);
        g.setStroke(Color.rgb(128, 224, 220, .17));
        g.strokeLine(72, 724, 1528, 724);
        text(g, "DAS BOOT FÄHRT WEITER. DU AUCH.", 72, 700, 18, CYAN, false);
        text(
                g,
                "A / D  Bewegung     ·     LEER  Sprung     ·     J  Angriff     ·     SHIFT "
                        + " Ausweichen",
                72,
                863,
                18,
                Color.web("#95aab3"),
                false);
        text(
                g,
                "K  Modul     ·     E  Interaktion     ·     ESC  Pause",
                1055,
                863,
                18,
                Color.web("#95aab3"),
                false);
    }

    private void ambient(GraphicsContext g, int sector, Settings settings) {
        if (settings.reducedMotion()) return;
        g.setFill(Color.rgb(151, 208, 216, .14));
        for (int i = 0; i < 45; i++) {
            double x = (i * 173.73 + clock * (3 + i % 5)) % 1600;
            double y = 180 + (i * 97.19 + Math.sin(clock * .3 + i) * 10) % 540;
            g.fillOval(x, y, i % 3 + 1, i % 3 + 1);
        }
        double alpha = .025 + .009 * Math.sin(clock * .8);
        g.setFill(
                new RadialGradient(
                        0,
                        0,
                        .5,
                        .5,
                        .5,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(
                                0,
                                Color.rgb(
                                        sector == 0 ? 245 : 65,
                                        200,
                                        sector == 0 ? 110 : 235,
                                        alpha)),
                        new Stop(1, Color.TRANSPARENT)));
        g.fillOval(100, 200, 1400, 600);
    }

    private void drawDoor(GraphicsContext g, GameRun run) {
        boolean open = run.phase() == GameRun.Phase.ROOM_CLEARED;
        Color c = open ? CYAN : Color.web("#b87754");
        g.setStroke(c.deriveColor(0, 1, 1, .7));
        g.setLineWidth(3);
        g.strokeLine(1534, 410, 1534, 612);
        text(g, open ? "SCHOTT FREI" : "VERRIEGELT", 1458, 390, 16, c, true);
        if (open) {
            double offset = Math.sin(clock * 3) * 5;
            g.setFill(CYAN);
            g.fillPolygon(
                    new double[] {1460 + offset, 1480 + offset, 1460 + offset},
                    new double[] {498, 510, 522},
                    3);
            if (run.player().x() > 1300) prompt(g, "E", "ROUTE WÄHLEN", 1430, 445);
        }
    }

    private void drawReward(GraphicsContext g, GameRun run) {
        boolean workshop = run.room().kind() == RoomPlan.Kind.WORKSHOP;
        if (!run.rewardAvailable() && (!workshop || run.repaired())) return;
        double x = 800, y = GameRun.FLOOR;
        g.setFill(Color.rgb(10, 20, 22, .6));
        g.fillOval(x - 60, y - 5, 120, 16);
        g.setFill(
                new LinearGradient(
                        0,
                        0,
                        0,
                        1,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#4d6467")),
                        new Stop(1, Color.web("#172b32"))));
        g.fillRoundRect(x - 38, y - 41, 76, 40, 5, 5);
        g.setStroke(AMBER);
        g.setLineWidth(2);
        g.strokeRoundRect(x - 38, y - 41, 76, 40, 5, 5);
        g.setFill(CYAN);
        g.fillRect(x - 12, y - 32, 24, 5);
        g.setFill(Color.rgb(128, 224, 220, .12 + .06 * Math.sin(clock * 3)));
        g.fillOval(x - 60, y - 110, 120, 125);
        text(
                g,
                workshop
                        ? "WERKSTATT"
                        : run.room().kind() == RoomPlan.Kind.CACHE ? "VORRÄTE" : "MODULFUND",
                x,
                y - 91,
                19,
                CYAN,
                true);
        if (Math.abs(run.player().x() - 800) < 165)
            prompt(
                    g,
                    "E",
                    workshop
                            ? "VERSORGEN"
                            : run.rewardOffers().isEmpty() ? "BERGEN" : "MODUL WÄHLEN",
                    x,
                    y - 130);
    }

    private void drawHazard(
            GraphicsContext g, Hazard hazard, boolean enabled, boolean reducedMotion) {
        double x = hazard.x(), floor = GameRun.FLOOR;
        g.setFill(Color.web("#172a31"));
        g.fillRoundRect(x - 61, floor - 3, 122, 12, 3, 3);
        g.setStroke(Color.web("#63716c"));
        g.setLineWidth(2);
        for (int i = 0; i < 8; i++)
            g.strokeLine(x - 49 + i * 14, floor - 2, x - 49 + i * 14, floor + 6);
        if (!enabled) return;
        if (hazard.warning()) {
            g.setFill(Color.rgb(246, 183, 84, .17));
            g.fillRect(x - 64, floor - 8, 128, 12);
            text(
                    g,
                    hazard.kind() == Hazard.Kind.STEAM ? "HEISSDAMPF" : "ÜBERSPANNUNG",
                    x,
                    floor - 29,
                    15,
                    AMBER,
                    true);
        }
        if (hazard.active()) {
            if (hazard.kind() == Hazard.Kind.STEAM) {
                for (int i = 0; i < 18; i++) {
                    double rise = reducedMotion ? i / 18.0 : (clock * 1.4 + i * .071) % 1;
                    double width = 9 + rise * 39;
                    g.setFill(Color.rgb(166, 211, 216, (1 - rise) * .22));
                    g.fillOval(
                            x + Math.sin(i * 3.7 + clock) * 25 - width / 2,
                            floor - rise * 150 - 15,
                            width,
                            width * 1.5);
                }
                g.setStroke(Color.rgb(245, 210, 150, .4));
                g.strokeLine(x - 48, floor - 5, x + 48, floor - 5);
            } else {
                g.setStroke(CYAN);
                g.setLineWidth(2);
                for (int i = 0; i < 8; i++) {
                    double y1 =
                            floor
                                    - 5
                                    - Math.abs(Math.sin(i * 5.7 + (reducedMotion ? 0 : clock * 25)))
                                            * 25;
                    double y2 =
                            floor
                                    - 5
                                    - Math.abs(
                                                    Math.sin(
                                                            (i + 1) * 5.7
                                                                    + (reducedMotion
                                                                            ? 0
                                                                            : clock * 25)))
                                            * 25;
                    g.strokeLine(x - 56 + i * 14, y1, x - 42 + i * 14, y2);
                }
            }
        }
    }

    private void drawActor(GraphicsContext g, Actor actor, String key, GameRun run) {
        String pose = "idle";
        int frame;
        if (actor.hurtTime() > .05) pose = "hurt";
        else if (key.equals("player")) {
            if (run.player().dashTime() > 0) pose = "dash";
            else if (run.player().attackTime() > 0) pose = "attack";
            else if (Math.abs(actor.vx()) > 1) pose = "walk";
        } else if (actor instanceof Enemy enemy) {
            if (enemy.state() == Enemy.State.STRIKE || enemy.state() == Enemy.State.WINDUP)
                pose = "attack";
            else if (Math.abs(actor.vx()) > 1) pose = "walk";
        }
        int count = assets.frames(key, pose);
        if (pose.equals("attack") && key.equals("player"))
            frame = Math.min(count - 1, (int) ((.29 - run.player().attackTime()) / .29 * count));
        else
            frame =
                    (int)
                                    (actor.animationTime()
                                            * (pose.equals("walk")
                                                    ? 12
                                                    : pose.equals("idle") ? 4 : 12))
                            % Math.max(1, count);
        var sprite = assets.actor(key, pose, Math.max(0, frame));
        double size = key.equals("captain") ? 235 : key.equals("sentinel") ? 177 : 155;
        double anchor = key.equals("drone") ? .68 : .944;
        g.setFill(Color.rgb(0, 0, 0, .40));
        double shadow = key.equals("captain") ? 140 : 65;
        g.fillOval(actor.x() - shadow / 2, GameRun.FLOOR - 7, shadow, 15);
        g.save();
        if (actor == run.player()
                && run.player().invulnerableTime() > .1
                && run.player().dashTime() <= 0)
            g.setGlobalAlpha(Math.sin(clock * 45) > 0 ? .68 : 1);
        g.translate(actor.x(), actor.y());
        g.scale(actor.facing(), 1);
        if (sprite != null) g.drawImage(sprite, -size / 2, -size * anchor, size, size);
        else {
            g.setFill(key.equals("player") ? AMBER : Color.FIREBRICK);
            g.fillRect(-actor.width() / 2, -actor.height(), actor.width(), actor.height());
        }
        g.restore();
        if (actor instanceof Enemy enemy
                && enemy.kind() != EnemyKind.CAPTAIN
                && actor.health() < actor.maxHealth()) {
            double y = actor.y() - actor.height() - 18;
            g.setFill(Color.rgb(0, 0, 0, .7));
            g.fillRoundRect(actor.x() - 30, y, 60, 5, 2, 2);
            g.setFill(Color.web("#d69e66"));
            g.fillRoundRect(actor.x() - 30, y, 60 * actor.health() / actor.maxHealth(), 5, 2, 2);
        }
        if (actor instanceof Enemy enemy && enemy.kind() == EnemyKind.CAPTAIN) {
            g.setStroke(
                    enemy.armored() ? Color.rgb(117, 215, 237, .38) : Color.rgb(255, 205, 111, .8));
            g.setLineWidth(enemy.armored() ? 2 : 4);
            g.strokeOval(enemy.x() - 94, enemy.y() - enemy.height() - 9, 188, enemy.height() + 15);
        }
    }

    private void drawTelegraph(GraphicsContext g, Enemy e) {
        if (e.state() != Enemy.State.WINDUP) return;
        double alpha = .18 + .09 * Math.sin(clock * 26);
        g.setStroke(Color.rgb(255, 126, 71, .8));
        g.setLineWidth(2);
        if (e.kind() == EnemyKind.DRONE
                || e.kind() == EnemyKind.CAPTAIN && e.attackPattern() == 1) {
            g.setLineDashes(6, 8);
            g.strokeLine(e.x(), e.y() - e.height() * .55, e.targetX(), e.targetY());
            g.setLineDashes();
            g.strokeOval(e.targetX() - 17, e.targetY() - 17, 34, 34);
        } else {
            double range =
                    e.kind() == EnemyKind.CAPTAIN
                            ? 330
                            : e.kind() == EnemyKind.SENTINEL ? 180 : 290;
            g.setFill(Color.rgb(255, 111, 61, alpha));
            double x = e.facing() > 0 ? e.x() : e.x() - range;
            if (e.kind() == EnemyKind.CAPTAIN && e.attackPattern() == 0) {
                x = 80;
                range = 1440;
            }
            g.fillRect(x, GameRun.FLOOR - 8, range, 10);
            g.strokeLine(x, GameRun.FLOOR - 7, x + range, GameRun.FLOOR - 7);
        }
        text(g, "!", e.x(), e.y() - e.height() - 25, 33, AMBER, true);
    }

    private void drawProjectile(GraphicsContext g, Projectile p) {
        Color color = p.friendly() ? CYAN : Color.web("#ff9463");
        g.setFill(color.deriveColor(0, 1, 1, .12));
        g.fillOval(p.x() - p.radius() * 2, p.y() - p.radius() * 2, p.radius() * 4, p.radius() * 4);
        g.setStroke(color);
        g.setLineWidth(p.kind() == Projectile.Kind.ARC ? 6 : 3);
        if (p.kind() == Projectile.Kind.SHOCKWAVE) {
            g.strokeArc(p.x() - 23, p.y() - 30, 46, 52, 0, 180, ArcType.OPEN);
            g.strokeLine(p.x(), p.y() - 25, p.x(), p.y() + 10);
        } else {
            g.strokeLine(
                    p.x() - Math.signum(p.vx()) * 24,
                    p.y(),
                    p.x() + Math.signum(p.vx()) * 4,
                    p.y());
            g.setFill(Color.web("#fff1cc"));
            g.fillOval(p.x() - 4, p.y() - 4, 8, 8);
        }
    }

    private void drawPlayerEffects(GraphicsContext g, GameRun run) {
        var p = run.player();
        if (p.shieldTime() > 0) {
            g.setStroke(Color.rgb(130, 235, 236, .65));
            g.setLineWidth(2);
            g.strokeOval(p.x() - 51, p.y() - 130, 102, 142);
        }
        if (p.attackTime() > 0) {
            double alpha = Math.min(.9, p.attackTime() * 4);
            g.setStroke(Color.rgb(255, 209, 139, alpha));
            g.setLineWidth(6);
            g.strokeArc(
                    p.x() - 92,
                    p.y() - 154,
                    184,
                    154,
                    p.facing() > 0 ? -60 : 120,
                    120,
                    ArcType.OPEN);
            g.setStroke(Color.rgb(255, 245, 220, alpha * .8));
            g.setLineWidth(2);
            g.strokeArc(
                    p.x() - 102,
                    p.y() - 164,
                    204,
                    174,
                    p.facing() > 0 ? -60 : 120,
                    120,
                    ArcType.OPEN);
        }
    }

    private void hud(GraphicsContext g, GameRun run) {
        var p = run.player();
        g.setFill(
                new LinearGradient(
                        0,
                        0,
                        0,
                        1,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(3, 14, 22, .96)),
                        new Stop(1, Color.rgb(3, 14, 22, .0))));
        g.fillRect(0, 0, 1600, 155);
        g.setStroke(Color.rgb(142, 187, 199, .25));
        g.setLineWidth(1);
        g.strokeLine(40, 114, 1560, 114);
        text(g, "A B Y S S", 40, 37, 23, CYAN, false);
        text(g, "INTEGRITÄT", 40, 63, 15, Color.web("#9fb5bc"), false);
        bar(
                g,
                40,
                74,
                262,
                11,
                p.health() / p.maxHealth(),
                p.health() / p.maxHealth() < .3 ? Color.web("#fb755c") : AMBER);
        text(
                g,
                Math.round(p.health()) + " / " + Math.round(p.maxHealth()),
                313,
                85,
                17,
                TEXT,
                false);
        text(g, "ENERGIE", 410, 63, 15, Color.web("#9fb5bc"), false);
        bar(g, 410, 74, 185, 7, p.energy() / p.maxEnergy(), CYAN);
        text(g, run.room().sectorName(), 800, 34, 19, CYAN, true);
        text(g, run.room().title(), 800, 77, 36, TEXT, true);
        text(
                g,
                "RAUM " + String.format("%02d", run.room().depth() + 1) + " / 12",
                1310,
                37,
                23,
                TEXT,
                false);
        text(g, "ZYKLUS " + String.format("%02d", run.cycle() + 1), 1439, 76, 17, CYAN, false);
        text(g, p.salvage() + " SCHROTT", 1310, 76, 17, AMBER, false);
        if (p.explorer()) text(g, "ENTDECKER", 40, 133, 15, AMBER, false);
        if (run.phase() == GameRun.Phase.RUNNING && run.room().waveCount() > 1)
            text(
                    g,
                    "PATROUILLE " + (run.wave() + 1) + " / " + run.room().waveCount(),
                    800,
                    132,
                    16,
                    Color.web("#a4bbc6"),
                    true);
        // Permanenter Wegweiser verhindert, dass ein neues Eingabeschema erraten werden muss.
        g.setFill(Color.rgb(3, 14, 22, .82));
        g.fillRect(0, 816, 1600, 84);
        g.setStroke(Color.rgb(142, 187, 199, .22));
        g.strokeLine(40, 816, 1560, 816);
        text(
                g,
                "A / D  BEWEGEN   ·   LEER  SPRINGEN   ·   J / MAUS  ANGREIFEN",
                40,
                855,
                18,
                Color.web("#b0c1c5"),
                false);
        String dash =
                p.dashCooldown() <= 0
                        ? "BEREIT"
                        : String.format(java.util.Locale.ROOT, "%.1f S", p.dashCooldown());
        text(
                g,
                "SHIFT  AUSWEICHEN  /  " + dash,
                690,
                855,
                18,
                p.dashCooldown() <= 0 ? CYAN : Color.web("#8099a2"),
                false);
        String ability =
                p.abilityCooldown() <= 0
                        ? (p.energy() >= p.module().cost() ? "BEREIT" : "ENERGIE FEHLT")
                        : String.format(java.util.Locale.ROOT, "%.1f S", p.abilityCooldown());
        text(
                g,
                "K  " + p.module().title().toUpperCase() + "  /  " + ability,
                1110,
                855,
                18,
                CYAN,
                false);
        text(g, "E  INTERAKTION      ESC  PAUSE", 40, 883, 15, Color.web("#6f8f9d"), false);
        int i = 0;
        for (Upgrade u : Upgrade.values())
            if (p.stacks(u) > 0) {
                text(
                        g,
                        u.title() + " " + p.stacks(u),
                        430 + i * 190,
                        883,
                        15,
                        Color.web("#a9b6ad"),
                        false);
                i++;
            }
        if (run.room().depth() == 0 && run.roomTime() < 12) {
            text(
                    g,
                    "Rote Markierung? Weiche aus. Nach dem Kampf: Modul bergen und zum rechten"
                            + " Schott.",
                    800,
                    748,
                    22,
                    TEXT,
                    true);
        } else if (run.phase() == GameRun.Phase.ROOM_CLEARED) {
            text(
                    g,
                    run.rewardAvailable()
                            ? "RAUM GESICHERT    ·    E am Modulbehälter, danach weiter zum rechten"
                                    + " Schott."
                            : "RAUM GESICHERT    ·    Gehe zum rechten Schott und wähle deine"
                                    + " Route.",
                    800,
                    748,
                    21,
                    CYAN,
                    true);
        }
        for (Enemy enemy : run.enemies())
            if (enemy.kind() == EnemyKind.CAPTAIN) {
                text(
                        g,
                        "DER LOTSE" + (enemy.enraged() ? "  /  NOTFALLPROTOKOLL" : ""),
                        800,
                        237,
                        20,
                        AMBER,
                        true);
                bar(g, 520, 249, 560, 8, enemy.health() / enemy.maxHealth(), Color.web("#da8064"));
                text(
                        g,
                        enemy.armored()
                                ? "PANZERUNG AKTIV · WEICHE DEM ANGRIFF AUS"
                                : "KERN OFFEN · JETZT ANGREIFEN",
                        800,
                        286,
                        17,
                        enemy.armored() ? CYAN : AMBER,
                        true);
            }
    }

    private void vignette(GraphicsContext g) {
        g.setFill(
                new RadialGradient(
                        0,
                        0,
                        .5,
                        .53,
                        .7,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(.30, Color.TRANSPARENT),
                        new Stop(1, Color.rgb(0, 6, 14, .57))));
        g.fillRect(0, 0, 1600, 900);
    }

    private void bar(
            GraphicsContext g, double x, double y, double w, double h, double fill, Color color) {
        g.setFill(Color.rgb(153, 182, 194, .15));
        g.fillRect(x, y, w, h);
        g.setFill(color);
        g.fillRect(x, y, w * Math.max(0, Math.min(1, fill)), h);
    }

    private void prompt(GraphicsContext g, String key, String label, double x, double y) {
        g.setFill(Color.rgb(3, 17, 24, .9));
        g.fillRoundRect(x - 22, y - 27, 44, 38, 5, 5);
        g.setStroke(CYAN);
        g.setLineWidth(1);
        g.strokeRoundRect(x - 22, y - 27, 44, 38, 5, 5);
        text(g, key, x, y, 25, TEXT, true);
        text(g, label, x, y + 34, 17, CYAN, true);
    }

    private void text(
            GraphicsContext g,
            String value,
            double x,
            double y,
            double size,
            Color color,
            boolean center) {
        g.setTextAlign(center ? TextAlignment.CENTER : TextAlignment.LEFT);
        g.setFont(size >= 30 ? assets.display(size) : assets.text(size));
        g.setFill(color);
        g.fillText(value, x, y);
        g.setTextAlign(TextAlignment.LEFT);
    }
}
