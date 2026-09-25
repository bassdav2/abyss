package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.domain.Enemy;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Player;
import ch.zhaw.abyss.domain.Status;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.DiverArt;
import ch.zhaw.abyss.ui.art.EnemyArt;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.art.PropArt;
import ch.zhaw.abyss.ui.art.RoomArt;
import ch.zhaw.abyss.ui.pixel.Frame;
import ch.zhaw.abyss.ui.pixel.PixelFont;
import ch.zhaw.abyss.ui.pixel.Sprite;

/**
 * Zeichnet Figuren: Animationswahl aus dem Domänenzustand, Trefferblitze, Elite-Umrisse,
 * Zustandsanzeigen, Lebensbalken, Schild, Begleitdrohne und die Hiebspuren der Waffen.
 */
final class ActorPainter {
    private static final double PX = RoomArt.PX;
    private final Frame frame, emissive;
    private final SpriteBank bank;
    private final PixelFont font;
    private double time;
    private Cosmetics look = Cosmetics.DEFAULT;

    ActorPainter(Frame frame, Frame emissive, SpriteBank bank, PixelFont font) {
        this.frame = frame;
        this.emissive = emissive;
        this.bank = bank;
        this.font = font;
    }

    /**
     * @param time Animationszeit
     * @param look Aussehen der Spielfigur
     */
    void prepare(double time, Cosmetics look) {
        this.time = time;
        this.look = look;
    }

    private static int px(double units) {
        return (int) Math.round(units * PX);
    }

    private void drawSprite(Sprite sprite, int x, int y, boolean flip) {
        frame.draw(sprite, x, y, flip);
        if (sprite.glow() != null) emissive.drawAdd(sprite.glow(), x, y, flip, 1);
    }

    void drawEnemy(Enemy e, int camX, int camY) {
        if (e.state() == Enemy.State.HIDDEN) return;
        var anims = bank.enemy(e.kind());
        EnemyArt.Anim anim;
        switch (e.state()) {
            case WINDUP -> anim = EnemyArt.Anim.WINDUP;
            case STRIKE -> anim = EnemyArt.Anim.STRIKE;
            case RECOVER -> anim = EnemyArt.Anim.RECOVER;
            case STUNNED -> anim = EnemyArt.Anim.HURT;
            default -> anim = Math.abs(e.vx()) > 8 ? EnemyArt.Anim.MOVE : EnemyArt.Anim.IDLE;
        }
        if (e.hurtTime() > .06 && !e.kind().boss()) anim = EnemyArt.Anim.HURT;
        var frames = anims.get(anim);
        int index =
                (int)
                                (e.animationTime()
                                        * (anim == EnemyArt.Anim.MOVE
                                                ? 10
                                                : anim == EnemyArt.Anim.WINDUP ? 12 : 5))
                        % frames.size();
        var sprite = frames.get(index);
        int x = px(e.x()) - camX, y = px(e.y()) + camY;
        boolean flip = e.facing() < 0;
        double alpha = 1;
        if (e.state() == Enemy.State.SPAWNING) {
            double t = Math.max(0, e.stateTime());
            alpha = e.kind().boss() ? 1 : Math.max(.15, 1 - t / .55);
            if (!e.kind().boss())
                frame.silhouette(sprite, x, y, flip, Frame.alpha(Pal.TEAL_5, .5 * (1 - alpha)));
        }
        if (e.affix().elite()) {
            int c = affixColor(e);
            frame.silhouette(sprite, x - 1, y, flip, c);
            frame.silhouette(sprite, x + 1, y, flip, c);
            frame.silhouette(sprite, x, y - 1, flip, c);
            frame.silhouette(sprite, x, y + 1, flip, c);
            emissive.drawAdd(sprite, x, y, flip, .08);
        }
        int tint = 0;
        double tintAmount = 0;
        if (e.hurtTime() > 0) {
            tint = Pal.WHITE;
            tintAmount = e.kind().boss() ? (e.hurtTime() > .08 ? .45 : 0) : .85;
        } else if (e.statuses().active(Status.FREEZE)) {
            tint = 0xFFBFEAFF;
            tintAmount = .6;
        } else if (e.statuses().active(Status.CHILL)) {
            tint = Pal.TEAL_5;
            tintAmount = .3;
        } else if (e.state() == Enemy.State.WINDUP && ((int) (time * 16)) % 2 == 0) {
            tint = Pal.RED_4;
            tintAmount = .25;
        }
        frame.draw(sprite, x, y, flip, tint, tintAmount, alpha);
        if (sprite.glow() != null) emissive.drawAdd(sprite.glow(), x, y, flip, alpha);
        if (e.kind().boss() && !e.armored() && e.state() != Enemy.State.SPAWNING) {
            double pulse = .5 + .5 * Math.sin(time * 10);
            emissive.glow(x, y - px(e.height()) / 2, 14, Pal.RUST_6, .3 * pulse);
        }
        if (e.statuses().active(Status.MARK)) {
            int my = y - px(e.height()) - 8;
            frame.circle(x, my, 3, Pal.RED_4);
            frame.pixel(x, my, Pal.RED_4);
            emissive.add(x, my, Pal.RED_4, 1);
        }
        if (e.eliteShield() > 0) {
            int r = px(Math.max(e.width(), e.height())) / 2 + 2;
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI / 12 + time * 2;
                emissive.add(
                        (int) Math.round(x + Math.cos(a) * r),
                        (int) Math.round(y - px(e.height()) / 2.0 + Math.sin(a) * r),
                        Pal.TEAL_5,
                        .5);
            }
        }
        if (!e.kind().boss() && e.health() < e.maxHealth()) {
            int w = Math.max(10, px(e.width()));
            int by = y - px(e.height()) - 5;
            frame.fill(x - w / 2 - 1, by - 1, w + 2, 4, Pal.OUTLINE);
            frame.fill(x - w / 2, by, w, 2, Pal.RED_1);
            frame.fill(
                    x - w / 2,
                    by,
                    (int) Math.round(w * e.healthRatio()),
                    2,
                    e.affix().elite() ? affixColor(e) : Pal.RED_4);
            if (e.eliteShield() > 0)
                frame.fill(
                        x - w / 2,
                        by - 2,
                        (int) Math.round(w * Math.min(1, e.eliteShield() / (e.maxHealth() * .35))),
                        1,
                        Pal.TEAL_5);
        }
        if (e.state() == Enemy.State.WINDUP) {
            int ey = y - px(e.height()) - 14;
            font.drawOutlined(
                    frame,
                    "!",
                    x - 1,
                    ey,
                    ((int) (time * 12)) % 2 == 0 ? Pal.RUST_6 : Pal.RED_4,
                    Pal.OUTLINE,
                    1);
        }
    }

    static int affixColor(Enemy e) {
        return switch (e.affix()) {
            case ARMORED -> 0xFF8AB0FF;
            case VOLATILE -> Pal.RUST_5;
            case SWIFT -> Pal.GREEN_4;
            case REGENERATING -> Pal.RED_4;
            case SHIELDED -> Pal.TEAL_5;
            case NONE -> Pal.WHITE;
        };
    }

    Sprite playerSprite(GameRun run) {
        var p = run.player();
        var anims = bank.diver(look, p.weapon(), p.diver());
        DiverArt.Anim anim;
        int index = 0;
        if (run.phase() == GameRun.Phase.DEFEAT) anim = DiverArt.Anim.DOWN;
        else if (p.hurtTime() > .08) {
            anim = DiverArt.Anim.HURT;
            index = p.hurtTime() > .17 ? 0 : 1;
        } else if (p.dashTime() > 0) {
            anim = DiverArt.Anim.DASH;
            index = ((int) (time * 20)) % 2;
        } else if (p.swing() != null) {
            var swing = p.swing();
            anim = DiverArt.of(swing.style());
            double t = p.swingTime();
            int count = anims.get(anim).size();
            index =
                    t < swing.windup()
                            ? 0
                            : t < swing.windup() + swing.active() ? 1 : Math.min(count - 1, 2);
        } else if (!p.grounded()) anim = p.vy() < 0 ? DiverArt.Anim.JUMP : DiverArt.Anim.FALL;
        else if (Math.abs(p.vx()) > 20) {
            anim = DiverArt.Anim.RUN;
            index = (int) (p.animationTime() * 14);
        } else {
            anim = DiverArt.Anim.IDLE;
            index = (int) (p.animationTime() * 4);
        }
        var frames = anims.get(anim);
        return frames.get(Math.floorMod(index, frames.size()));
    }

    void drawPlayer(GameRun run, int camX, int camY) {
        var p = run.player();
        var sprite = playerSprite(run);
        int x = px(p.x()) - camX, y = px(p.y()) + camY;
        boolean flip = p.facing() < 0;
        boolean blink =
                p.invulnerableTime() > .1
                        && p.dashTime() <= 0
                        && ((int) (time * 20)) % 2 == 0
                        && run.phase() == GameRun.Phase.RUNNING;
        // Kurzer weisser Blitz, danach rötlich nachglühend: die Figur bleibt lesbar.
        int tint =
                p.hurtTime() > .19
                        ? Pal.WHITE
                        : p.hurtTime() > .08 ? Pal.RED_4 : p.overdriveTime() > 0 ? Pal.RUST_6 : 0;
        double amount =
                p.hurtTime() > .19
                        ? .7
                        : p.hurtTime() > .08 ? .35 : p.overdriveTime() > 0 ? .15 : 0;
        frame.draw(sprite, x, y, flip, tint, amount, blink ? .45 : 1);
        if (sprite.glow() != null) emissive.drawAdd(sprite.glow(), x, y, flip, 1);
        if (p.shieldTime() > 0
                || p.shield() > 0
                        && p.shield() >= p.stats().maxShield() * .99
                        && p.stats().maxShield() > 0
                        && ((int) (time * 2)) % 4 == 0) {
            double strength = p.shieldTime() > 0 ? .9 : .25;
            int cx = x, cy = y - 17;
            for (int i = 0; i < 40; i++) {
                double a = i * Math.PI / 20 + time * 3;
                int bx = (int) Math.round(cx + Math.cos(a) * 15),
                        by = (int) Math.round(cy + Math.sin(a) * 20);
                if ((i + (int) (time * 12)) % 3 == 0) frame.pixel(bx, by, Pal.TEAL_6);
                emissive.add(bx, by, Pal.TEAL_5, strength * .6);
            }
        }
        if (p.droneTime() > 0) {
            int dx = x - p.facing() * 12, dy = y - 44 + (int) Math.round(Math.sin(time * 4) * 2);
            var drone = PropArt.small(((int) (time * 20)) % 2 == 0 ? "drone0" : "drone1");
            drawSprite(drone, dx, dy, flip);
        }
    }

    void drawSmear(Player p, int camX, int camY) {
        var swing = p.swing();
        if (swing == null || swing.ranged()) return;
        double t = p.swingTime();
        if (t < swing.windup() * .7 || t > swing.windup() + swing.active() + .05) return;
        double progress =
                Math.min(1, (t - swing.windup() * .7) / (swing.active() + swing.windup() * .3));
        int color = smearColor(p.weapon());
        int cx = px(p.x()) - camX, cy = px(p.y()) - 20 + camY;
        int dir = p.facing();
        double reach = swing.reach() * p.stats().reach() * PX;
        double from, to;
        switch (swing.style()) {
            case SLASH -> {
                from = -80;
                to = 45;
            }
            case OVERHEAD -> {
                from = -115;
                to = 60;
            }
            case AIR_SLASH -> {
                from = -60;
                to = 115;
            }
            case UPPERCUT -> {
                from = 70;
                to = -95;
            }
            case THRUST -> {
                for (int k = -1; k <= 1; k++) {
                    int yy = cy - 1 + k * 3;
                    int len = (int) (reach * (k == 0 ? 1 : .7) * progress);
                    for (int i = 6; i < len; i++) {
                        int xx = cx + dir * i;
                        double f = i / (double) Math.max(1, len);
                        frame.pixel(xx, yy, f > .8 ? Pal.WHITE : color);
                        emissive.add(xx, yy, color, .8 * f);
                    }
                }
                return;
            }
            case PUNCH -> {
                int fx0 = cx + dir * (int) (reach * .8);
                frame.circle(fx0, cy - 1, 3 + (int) (progress * 3), color);
                emissive.glow(fx0, cy - 1, 6, color, .7);
                return;
            }
            case SLAM -> {
                if (p.slamming()) {
                    for (int k = 0; k < 6; k++) {
                        int yy = cy - 20 - k * 4;
                        frame.pixel(cx - 5, yy, color);
                        frame.pixel(cx + 5, yy, color);
                        emissive.add(cx - 5, yy, color, .6);
                        emissive.add(cx + 5, yy, color, .6);
                    }
                }
                return;
            }
            default -> {
                return;
            }
        }
        double current = from + (to - from) * progress;
        double start = from + (to - from) * Math.max(0, progress - .55);
        for (double a = start; a <= current; a += 2) {
            double f = (a - start) / Math.max(1, current - start);
            double rad = Math.toRadians(a);
            int thickness = (int) Math.round(1 + f * 3);
            for (int k = 0; k < thickness; k++) {
                double r = reach - k * 1.4;
                int xx = cx + (int) Math.round(Math.cos(rad) * r * dir);
                int yy = cy + (int) Math.round(Math.sin(rad) * r * .9);
                int c = k == 0 && f > .6 ? Pal.WHITE : color;
                frame.pixel(xx, yy, c);
                emissive.add(xx, yy, color, .55 + .4 * f);
            }
        }
    }

    private static int smearColor(Weapon weapon) {
        return switch (weapon) {
            case KNIVES -> 0xFFD8F4FF;
            case TORCH_LANCE -> 0xFFFFB45A;
            case ANCHOR -> 0xFFE8E2D0;
            case TESLA -> Pal.TEAL_5;
            default -> 0xFFFFE6A8;
        };
    }
}
