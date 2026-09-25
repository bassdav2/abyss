package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.GameEvent;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.Synergy;
import ch.zhaw.abyss.ui.art.Pal;
import ch.zhaw.abyss.ui.art.RoomArt;

/**
 * Übersetzt Domänenereignisse in sichtbare Rückmeldung: Funken, Trümmer, Explosionen, Ringe,
 * Kamerawackeln, Trefferpausen, Blitze, Meldungen und Banner. Wirkt nie auf die Spielregeln zurück.
 */
final class EventEffects {
    private static final double PX = RoomArt.PX;
    private final Effects fx;
    private final Camera camera;
    private final HudRenderer hud;
    private final ScreenFeel feel;

    EventEffects(Effects fx, Camera camera, HudRenderer hud, ScreenFeel feel) {
        this.fx = fx;
        this.camera = camera;
        this.hud = hud;
        this.feel = feel;
    }

    /**
     * Übersetzt ein Domänenereignis in Effekte.
     *
     * @param e Ereignis
     * @param run laufender Tauchgang
     * @param settings Einstellungen
     */
    void apply(GameEvent e, GameRun run, Settings settings) {
        double x = e.x() * PX, y = e.y() * PX;
        double shake = settings.reducedMotion() ? 0 : settings.screenShake();
        var r = fx.random();
        switch (e.type()) {
            case HIT -> {
                boolean burn = "BURN".equals(e.text());
                if (!burn) {
                    fx.burst(Effects.Kind.SPARK, x, y, 6, 110, direction(run, x), 1.6, Pal.RUST_7);
                    feel.hitStop = Math.max(feel.hitStop, .035);
                    camera.shake(.06 * shake);
                }
                if (settings.damageNumbers() && (e.amount() >= 1 || !burn))
                    fx.text(
                            "" + Math.round(e.amount()),
                            x + r.nextInt(7) - 3,
                            y - 6,
                            .6,
                            burn ? Pal.RUST_5 : Pal.BONE,
                            1);
            }
            case CRIT -> {
                fx.burst(Effects.Kind.SPARK, x, y, 12, 170, direction(run, x), 1.9, Pal.RUST_7);
                fx.burst(Effects.Kind.STAR, x, y, 3, 40, -Math.PI / 2, 2, Pal.RUST_6);
                fx.flash(x, y, 40, Pal.RUST_6, .8, .12);
                feel.hitStop = Math.max(feel.hitStop, .07);
                camera.shake(.14 * shake);
                if (settings.damageNumbers())
                    fx.text(Math.round(e.amount()) + "!", x, y - 8, .8, Pal.RUST_6, 2);
            }
            case PLAYER_HIT -> {
                fx.burst(Effects.Kind.SPARK, x, y, 10, 120, -Math.PI / 2, 3, Pal.RED_4);
                fx.burst(Effects.Kind.DEBRIS, x, y, 4, 90, -Math.PI / 2, 2, Pal.STEEL_4);
                camera.shake(.35 * shake);
                feel.aberration = .25;
                feel.flash = .18;
                feel.flashColor = Pal.RED_3;
                feel.hitStop = Math.max(feel.hitStop, .06);
                if (settings.damageNumbers())
                    fx.text("-" + Math.round(e.amount()), x, y - 10, .8, Pal.RED_4, 1);
            }
            case SHIELD_HIT, BLOCK -> {
                fx.burst(Effects.Kind.SPARK, x, y, 8, 100, -Math.PI / 2, 3, Pal.TEAL_5);
                fx.ring(x, y, 4, 16, .25, Pal.TEAL_5);
                if (e.type() == GameEvent.Type.BLOCK)
                    fx.text("GEBLOCKT", x, y - 12, .6, Pal.TEAL_5, 1);
            }
            case ENEMY_DOWN, ELITE_DOWN -> {
                var kind =
                        EnemyKind.values()[
                                (int)
                                        Math.max(
                                                0,
                                                Math.min(
                                                        EnemyKind.values().length - 1,
                                                        e.amount()))];
                boolean organic =
                        kind == EnemyKind.JELLY || kind == EnemyKind.EEL || kind == EnemyKind.BROOD;
                if (organic) {
                    fx.burst(
                            Effects.Kind.GOO,
                            x,
                            y,
                            22,
                            150,
                            -Math.PI / 2,
                            2.6,
                            kind == EnemyKind.JELLY ? Pal.VIOLET_4 : Pal.TEAL_4);
                    fx.burst(Effects.Kind.BUBBLE, x, y, 14, 50, -Math.PI / 2, 2, Pal.TEAL_6);
                } else {
                    fx.burst(Effects.Kind.DEBRIS, x, y, 16, 170, -Math.PI / 2, 2.6, Pal.STEEL_4);
                    fx.burst(Effects.Kind.DEBRIS, x, y, 8, 150, -Math.PI / 2, 2.6, Pal.RUST_3);
                    fx.burst(Effects.Kind.SMOKE, x, y, 8, 30, -Math.PI / 2, 2, 0xFF3A3434);
                    fx.explosion(x, y, e.type() == GameEvent.Type.ELITE_DOWN ? 18 : 9, false);
                }
                camera.shake((e.type() == GameEvent.Type.ELITE_DOWN ? .3 : .12) * shake);
                feel.hitStop = Math.max(feel.hitStop, .05);
                if (e.type() == GameEvent.Type.ELITE_DOWN)
                    hud.toast("ELITE ZERSTÖRT · DATENKERN", Pal.TEAL_5);
            }
            case BOSS_DOWN -> {
                for (int i = 0; i < 6; i++)
                    fx.explosion(
                            x + r.nextInt(50) - 25,
                            y + r.nextInt(40) - 20,
                            14 + r.nextInt(16),
                            false);
                fx.burst(Effects.Kind.DEBRIS, x, y, 40, 220, -Math.PI / 2, 3, Pal.STEEL_4);
                camera.shake(1.0 * shake);
                feel.slowMotion = 1.4;
                feel.flash = .8;
                feel.flashColor = Pal.WHITE;
                hud.banner(e.text().toUpperCase(), "BEZWUNGEN", Pal.RUST_6, 3);
            }
            case DASH -> {
                feel.dashGhost = 0;
                fx.burst(Effects.Kind.DUST, x, y, 6, 60, Math.PI, 1, Pal.STEEL_6);
            }
            case JUMP -> fx.burst(Effects.Kind.DUST, x, y, 5, 50, -Math.PI / 2, 2.5, Pal.STEEL_6);
            case AIR_JUMP -> {
                fx.ring(x, y, 2, 12, .3, Pal.TEAL_5);
                fx.burst(Effects.Kind.BUBBLE, x, y, 8, 40, Math.PI / 2, 2, Pal.TEAL_6);
            }
            case LAND -> {
                fx.burst(Effects.Kind.DUST, x - 3, y, 4, 60, Math.PI, .6, Pal.STEEL_6);
                fx.burst(Effects.Kind.DUST, x + 3, y, 4, 60, 0, .6, Pal.STEEL_6);
            }
            case PULSE -> {
                double radius = Math.abs(e.amount()) * PX;
                boolean hostile = e.amount() < 0;
                int color = hostile ? Pal.GREEN_4 : Pal.TEAL_5;
                fx.ring(x, y, 4, radius, .45, color);
                fx.ring(x, y, 2, radius * .7, .35, Pal.WHITE);
                fx.flash(x, y, radius * 1.4, color, 1, .3);
                camera.shake(.2 * shake);
            }
            case ARC -> {
                if ("beam".equals(e.text())) {
                    fx.flash(x, y, 200, Pal.GREEN_4, 1.2, .45);
                    camera.shake(.25 * shake);
                } else {
                    fx.burst(Effects.Kind.SPARK, x, y, 10, 120, 0, 6.3, Pal.TEAL_6);
                    fx.flash(x, y, 50, Pal.TEAL_5, .9, .2);
                }
            }
            case CHAIN -> {
                fx.bolt(e.x2() * PX, e.y2() * PX, x, y, .18, Pal.TEAL_5);
                fx.flash(x, y, 36, Pal.TEAL_5, .8, .15);
            }
            case SHIELD -> fx.ring(x, y, 6, 22, .4, Pal.TEAL_5);
            case SHOT, HARPOON, TORPEDO -> {
                fx.burst(Effects.Kind.SPARK, x, y, 4, 80, 0, 6.3, Pal.RUST_6);
                fx.flash(x, y, 30, Pal.RUST_6, .7, .08);
            }
            case EXPLOSION -> {
                double radius = e.amount() * PX;
                boolean acid = "acid".equals(e.text());
                if (acid) {
                    fx.burst(Effects.Kind.GOO, x, y, 10, 80, -Math.PI / 2, 2, Pal.GREEN_4);
                    fx.burst(Effects.Kind.SMOKE, x, y, 5, 20, -Math.PI / 2, 1, Pal.GREEN_2);
                } else {
                    fx.explosion(x, y, radius, false);
                    camera.shake(Math.min(.8, radius / 60) * shake);
                    feel.hitStop = Math.max(feel.hitStop, .04);
                }
            }
            case FREEZE -> {
                double radius = Math.max(10, e.amount() * PX);
                if (e.amount() > 0) fx.explosion(x, y, radius, true);
                else fx.burst(Effects.Kind.ICE, x, y, 6, 60, -Math.PI / 2, 3, Pal.TEAL_6);
            }
            case SONAR -> {
                fx.ring(x, y, 4, 320, 1.2, Pal.TEAL_5);
                fx.ring(x, y, 2, 220, .9, Pal.TEAL_6);
                hud.toast("SONAR · ALLE ZIELE MARKIERT", Pal.TEAL_5);
            }
            case OVERDRIVE -> {
                fx.burst(Effects.Kind.SPARK, x, y, 20, 150, 0, 6.3, Pal.RUST_6);
                fx.flash(x, y, 60, Pal.RUST_6, 1, .3);
                hud.toast("ÜBERLASTUNG", Pal.RUST_6);
            }
            case DRONE -> fx.burst(Effects.Kind.STAR, x, y, 8, 40, 0, 6.3, Pal.TEAL_5);
            case FLAME -> fx.burst(Effects.Kind.FLAME, x, y, 6, 60, 0, 6.3, Pal.RUST_6);
            case SHOCK -> {
                double radius = e.amount() * PX;
                fx.ring(x, y, 4, radius, .3, Pal.TEAL_6);
                for (int i = 0; i < 5; i++) {
                    double a = r.nextDouble() * Math.PI * 2;
                    fx.bolt(
                            x,
                            y,
                            x + Math.cos(a) * radius,
                            y + Math.sin(a) * radius,
                            .2,
                            Pal.TEAL_5);
                }
                fx.flash(x, y, radius * 2, Pal.TEAL_5, 1, .25);
            }
            case SLAM -> {
                double radius = e.amount() * PX;
                fx.burst(Effects.Kind.DUST, x, y, 16, 140, Math.PI, .5, Pal.STEEL_6);
                fx.burst(Effects.Kind.DUST, x, y, 16, 140, 0, .5, Pal.STEEL_6);
                fx.burst(Effects.Kind.DEBRIS, x, y, 6, 120, -Math.PI / 2, 1.5, Pal.STEEL_3);
                fx.ring(
                        x,
                        y - 2,
                        4,
                        radius,
                        .3,
                        "player".equals(e.text()) ? Pal.TEAL_5 : Pal.RUST_5);
                camera.shake(.35 * shake);
            }
            case SPAWN -> {
                fx.burst(Effects.Kind.BUBBLE, x, y - 6, 10, 40, -Math.PI / 2, 2.4, Pal.TEAL_6);
                fx.ring(x, y - 8, 2, 14, .35, Pal.RED_4);
                if ((int) e.amount() == EnemyKind.SMUGGLER.ordinal())
                    hud.toast("SCHMUGGLERDROHNE MIT BEUTE · 15 SEKUNDEN", Pal.RUST_6);
            }
            case ROOM_CLEAR -> {
                hud.banner(
                        "RAUM GESICHERT", "Bergung im Raum · Schott rechts offen", Pal.TEAL_5, 2.4);
                feel.flash = .15;
                feel.flashColor = Pal.TEAL_5;
            }
            case UPGRADE, WEAPON -> {
                double px = run.player().x() * PX, py = run.player().centerY() * PX;
                fx.burst(Effects.Kind.STAR, px, py, 18, 70, 0, 6.3, Pal.RUST_6);
                fx.ring(px, py, 4, 26, .5, Pal.RUST_6);
                hud.toast(
                        e.text()
                                + (e.type() == GameEvent.Type.WEAPON
                                        ? " ausgerüstet"
                                        : " installiert"),
                        Pal.RUST_6);
            }
            case SYNERGY -> {
                double px = run.player().x() * PX, py = run.player().centerY() * PX;
                fx.burst(Effects.Kind.STAR, px, py, 30, 110, 0, 6.3, Pal.VIOLET_4);
                fx.ring(px, py, 6, 46, .7, Pal.VIOLET_4);
                fx.ring(px, py, 2, 30, .5, Pal.WHITE);
                feel.flash = Math.max(feel.flash, .12);
                feel.flashColor = Pal.VIOLET_4;
                String effect = "";
                for (var synergy : Synergy.values())
                    if (synergy.title().equals(e.text())) effect = synergy.effect();
                hud.toast("RESONANZ: " + e.text() + " · " + effect, Pal.VIOLET_4);
            }
            case HEAL -> {
                double px = run.player().x() * PX, py = run.player().centerY() * PX;
                fx.burst(Effects.Kind.PLUS, px, py, 12, 40, -Math.PI / 2, 2, Pal.GREEN_4);
                hud.toast("Reserven aufgefüllt", Pal.GREEN_4);
            }
            case PURCHASE ->
                    fx.burst(
                            Effects.Kind.STAR,
                            run.player().x() * PX,
                            run.player().centerY() * PX,
                            10,
                            50,
                            -Math.PI / 2,
                            2,
                            Pal.RUST_6);
            case CURSE -> {
                double px = run.player().x() * PX, py = run.player().centerY() * PX;
                fx.burst(Effects.Kind.SMOKE, px, py, 16, 40, -Math.PI / 2, 3, Pal.VIOLET_2);
                fx.burst(Effects.Kind.STAR, px, py, 10, 60, 0, 6.3, Pal.VIOLET_4);
                hud.toast("FLUCH ANGENOMMEN · " + e.text(), Pal.VIOLET_4);
            }
            case DEFEAT -> {
                feel.slowMotion = 1.2;
                feel.flash = .5;
                feel.flashColor = Pal.RED_2;
                camera.shake(.6 * shake);
            }
            case VICTORY -> {
                feel.flash = .9;
                feel.flashColor = Pal.WHITE;
            }
            case REVIVE -> {
                fx.ring(x, y, 4, 80, .8, Pal.RUST_6);
                fx.burst(Effects.Kind.STAR, x, y, 30, 120, 0, 6.3, Pal.RUST_6);
                feel.flash = .6;
                feel.flashColor = Pal.RUST_7;
                hud.banner("NOTFALLKAPSEL", "Zweiter Atemzug", Pal.RUST_6, 2);
            }
            case BOSS_INTRO -> hud.bossIntro(e.text(), bossSubtitle(e.text()));
            case BOSS_PHASE -> {
                feel.flash = .35;
                feel.flashColor = Pal.RED_3;
                camera.shake(.5 * shake);
                hud.banner(
                        e.amount() >= 2 ? "LETZTE PHASE" : "NOTFALLPROTOKOLL",
                        e.text(),
                        Pal.RED_4,
                        1.8);
            }
            case SUPPLY -> hud.toast(e.text(), Pal.GREEN_4);
            case CRATE_BREAK -> {
                fx.burst(Effects.Kind.DEBRIS, x, y, 10, 120, -Math.PI / 2, 2.4, Pal.RUST_3);
                fx.burst(Effects.Kind.DUST, x, y, 6, 50, -Math.PI / 2, 2, Pal.STEEL_6);
            }
            case PICKUP -> {
                int color =
                        switch (e.text()) {
                            case "HEALTH" -> Pal.GREEN_4;
                            case "ENERGY" -> Pal.TEAL_5;
                            default -> Pal.RUST_6;
                        };
                fx.burst(Effects.Kind.STAR, x, y, 2, 30, -Math.PI / 2, 2, color);
                hud.pickup(e.text(), e.amount());
            }
            case CORE -> {
                fx.burst(Effects.Kind.STAR, x, y, 10, 60, 0, 6.3, Pal.TEAL_5);
                hud.toast("+" + Math.round(e.amount()) + " DATENKERN", Pal.TEAL_5);
            }
            case REINFORCEMENTS ->
                    hud.banner("VERSTÄRKUNG", "Welle " + Math.round(e.amount()), Pal.RED_4, 1.6);
            case MACHINE -> {
                switch (e.text()) {
                    case "VENT" -> {
                        fx.burst(
                                Effects.Kind.STEAM, x, y - 2, 14, 90, -Math.PI / 2, .8, 0xFFE0F0F8);
                        fx.ring(x, y - 2, 3, 22, .35, Pal.TEAL_6);
                        if (e.amount() > 0) camera.shake(.25 * shake);
                    }
                    case "LASER" -> {
                        fx.burst(Effects.Kind.SPARK, x, y, 10, 120, 0, 6.3, 0xFFFF6080);
                        fx.flash(x, y, 18, 0xFFFF4070, .8, .12);
                    }
                    case "STEAM" -> {
                        for (double sx = 20; sx < e.amount() * PX; sx += 22)
                            fx.burst(
                                    Effects.Kind.STEAM,
                                    sx,
                                    y - 2,
                                    3,
                                    120,
                                    -Math.PI / 2,
                                    .6,
                                    0xFFE8F4F8);
                        camera.shake(.4 * shake);
                        hud.toast("DAMPFVENTIL GEÖFFNET", 0xFFE8F4F8);
                    }
                    case "ESCAPE", "FLEE" -> {
                        fx.burst(Effects.Kind.SMOKE, x, y, 8, 40, -Math.PI / 2, 2, Pal.STEEL_5);
                        if (e.text().equals("ESCAPE"))
                            hud.toast("SCHMUGGLERDROHNE ENTKOMMEN", Pal.STEEL_6);
                    }
                    case "CAUGHT" -> {
                        fx.burst(Effects.Kind.STAR, x, y, 24, 110, 0, 6.3, Pal.RUST_6);
                        fx.ring(x, y, 4, 34, .5, Pal.RUST_6);
                        hud.toast("BEUTE GESICHERT · +1 DATENKERN", Pal.RUST_6);
                    }
                    case "LIST_WARN" -> {
                        hud.toast("SCHLAGSEITE · FESTHALTEN!", Pal.RUST_6);
                        camera.shake(.15 * shake);
                    }
                    case "LIST" -> {
                        camera.shake(.7 * shake);
                        for (int k = 0; k < 14; k++)
                            fx.burst(
                                    Effects.Kind.DUST,
                                    camera.left() + r.nextInt(WorldRenderer.W),
                                    24,
                                    1,
                                    40,
                                    Math.PI / 2,
                                    .6,
                                    Pal.STEEL_6);
                    }
                    case "EXPOSED" -> {
                        fx.ring(x, y, 4, 40, .45, Pal.RUST_6);
                        fx.burst(Effects.Kind.SPARK, x, y, 16, 140, 0, 6.3, Pal.RUST_6);
                        hud.toast("KERN FREIGELEGT · JETZT ANGREIFEN", Pal.RUST_6);
                    }
                    case "SEALED" ->
                            hud.banner("LECK ABGEDICHTET", "Die Pumpen laufen", Pal.TEAL_5, 2.2);
                    case "CONSOLE" -> {
                        int color =
                                switch ((int) e.amount()) {
                                    case 0 -> Pal.RUST_6;
                                    case 1 -> 0xFFE8F4F8;
                                    case 2 -> Pal.TEAL_5;
                                    default -> Pal.RED_4;
                                };
                        fx.ring(x, y - 12, 4, 40, .5, color);
                        feel.flash = Math.max(feel.flash, .15);
                        feel.flashColor = color;
                        switch ((int) e.amount()) {
                            case 0 -> hud.toast("TORPEDO ABGEFEUERT", color);
                            case 2 -> hud.toast("KÄLTEKAMMER GEFLUTET", color);
                            case 3 -> hud.toast("SICHERHEITSNETZ ÜBERLASTET", color);
                            default -> {}
                        }
                    }
                    default -> {}
                }
            }
            case CONDITION -> {
                if ("ALARM".equals(e.text())) {
                    feel.flash = Math.max(feel.flash, .22);
                    feel.flashColor = Pal.RED_3;
                }
            }
            case DOOR -> {
                if (e.amount() >= 0) hud.roomCard(run.room());
            }
            case SWING, TELEGRAPH -> {}
        }
    }

    private double direction(GameRun run, double x) {
        return x >= run.player().x() * PX ? -.3 : Math.PI + .3;
    }

    private static String bossSubtitle(String name) {
        return switch (name) {
            case "Der Schottmeister" -> "Wächter der Hecksektion";
            case "Der Reaktorkern" -> "Herz des Maschinendecks";
            case "Die Brutmutter" -> "Was im Labor entkam";
            default -> "Herr über die Brücke";
        };
    }
}
