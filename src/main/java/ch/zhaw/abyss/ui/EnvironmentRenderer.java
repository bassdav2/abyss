package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.GameRun;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;

/** Räumliche Licht- und Vordergrundebenen, unabhängig von Kollisions- und Spielregeln. */
final class EnvironmentRenderer {
    private static final Color WARM = Color.web("#ffd393"),
            COOL = Color.web("#63d8ef"),
            GREEN = Color.web("#9ec97c");
    private double parallax;

    double parallax(GameRun run, boolean reduced) {
        if (reduced) {
            parallax = 0;
            return 0;
        }
        double target = reduced ? 0 : (run.player().x() - 800) * .011;
        parallax += (target - parallax) * .035;
        return parallax;
    }

    void behind(GraphicsContext g, GameRun run, String room, double time, boolean reduced) {
        double t = reduced ? 0 : time;
        Color tone =
                room.equals("hydroponics")
                        ? GREEN
                        : room.equals("torpedo")
                                ? Color.web("#fb7653")
                                : run.room().sector() == 0 ? WARM : COOL;
        g.save();
        // Lichtkegel bleiben hinter den Spielfiguren. Ruhiger Modus erhält statisches Licht.
        for (int i = 0; i < 3; i++) {
            double x = 290 + i * 520 + Math.sin(t * .13 + i) * 8;
            g.setGlobalAlpha(.055 + (reduced ? 0 : .012 * Math.sin(t * .7 + i * 2)));
            g.setFill(
                    new LinearGradient(
                            0,
                            220,
                            0,
                            640,
                            false,
                            CycleMethod.NO_CYCLE,
                            new Stop(0, tone),
                            new Stop(1, Color.TRANSPARENT)));
            g.fillPolygon(
                    new double[] {x - 12, x + 12, x + 135, x - 120},
                    new double[] {225, 225, 620, 620},
                    4);
        }
        g.setGlobalAlpha(1);
        if (room.equals("ballast") || room.equals("observatory") || room.equals("hydroponics")) {
            g.setStroke(Color.rgb(127, 226, 228, .13));
            g.setLineWidth(1);
            for (int i = 0; i < 18; i++) {
                double x = 490 + (i * 67.7 + t * 4) % 625,
                        y = 320 + (i * 41.3 - t * (7 + i % 4) + 10000) % 220;
                double radius = 2 + i % 3;
                g.strokeOval(x, y, radius, radius * 1.3);
            }
        }
        // Persönliche Stirnlampe verankert die Spielfigur in derselben Beleuchtung.
        var p = run.player();
        double x = p.x() + p.facing() * 15, y = p.y() - 119;
        g.setFill(
                new LinearGradient(
                        x,
                        y,
                        x + p.facing() * 330,
                        y,
                        false,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 230, 177, .13)),
                        new Stop(1, Color.TRANSPARENT)));
        g.fillPolygon(
                new double[] {x, x + p.facing() * 350, x + p.facing() * 350},
                new double[] {y, y - 90, y + 125},
                3);
        g.restore();
    }

    void foreground(GraphicsContext g, GameRun run, String room, double time, boolean reduced) {
        double t = reduced ? 0 : time;
        g.save();
        // Schwaden bewegen sich im Vordergrund, unterhalb der Trefferzonen.
        for (int i = 0; i < 4; i++) {
            double x = (i * 487 + t * (9 + i * 3)) % 2100 - 300;
            g.setFill(
                    new RadialGradient(
                            0,
                            0,
                            .5,
                            .5,
                            .5,
                            true,
                            CycleMethod.NO_CYCLE,
                            new Stop(0, Color.rgb(127, 173, 180, .075)),
                            new Stop(1, Color.TRANSPARENT)));
            g.fillOval(x, 583 + Math.sin(t * .25 + i) * 4, 540, 97);
        }
        // Nahe Streben und frei hängende Kabel erzeugen eine zweite Tiefenebene.
        double sway = Math.sin(t * .55) * 3;
        g.setStroke(Color.rgb(2, 10, 14, .82));
        g.setLineWidth(8);
        for (int i = 0; i < 2; i++) {
            double x = 350 + i * 780 - parallax * 1.8;
            g.beginPath();
            g.moveTo(x, 107);
            g.bezierCurveTo(x + 13, 145, x - 10 + sway, 190, x + 9 + sway, 247);
            g.stroke();
            g.setStroke(Color.rgb(91, 117, 119, .35));
            g.setLineWidth(1);
            g.strokeLine(x - 2, 110, x + 7 + sway, 235);
            g.setStroke(Color.rgb(2, 10, 14, .82));
            g.setLineWidth(8);
        }
        for (double x : new double[] {12 - parallax * 1.8, 1585 - parallax * 1.8}) {
            g.setFill(Color.rgb(3, 12, 17, .9));
            g.fillRoundRect(x, 130, 14, 610, 6, 6);
            g.setFill(Color.rgb(71, 94, 96, .4));
            g.fillRect(x + 2, 150, 2, 450);
        }
        // Kondenswasser fällt vereinzelt von beschädigten Leitungen.
        if (!reduced
                && (room.equals("ballast") || room.equals("cooling") || room.equals("medbay"))) {
            g.setStroke(Color.rgb(152, 219, 231, .32));
            g.setLineWidth(1);
            for (int i = 0; i < 7; i++) {
                double y = 200 + (t * (100 + i * 8) + i * 69) % 417, x = 220 + i * 193;
                g.strokeLine(x, y, x, y + 7);
            }
        }
        g.restore();
    }
}
