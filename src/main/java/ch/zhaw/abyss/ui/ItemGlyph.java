package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.Upgrade;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Kleine eigens gezeichnete technische Piktogramme; in HUD, Inventar und Bergung identisch. */
final class ItemGlyph {
    private ItemGlyph() {}

    static Color color(Upgrade u) {
        return switch (u.color()) {
            case "red" -> Color.web("#ec967e");
            case "green" -> Color.web("#afc78c");
            case "blue" -> Color.web("#8cb7d8");
            case "amber" -> GameRenderer.AMBER;
            default -> GameRenderer.CYAN;
        };
    }

    static Canvas node(Upgrade u, double size) {
        var c = new Canvas(size, size);
        draw(c.getGraphicsContext2D(), u, 0, 0, size, 1);
        return c;
    }

    static void draw(
            GraphicsContext g, Upgrade u, double x, double y, double size, double opacity) {
        g.save();
        g.translate(x, y);
        g.scale(size / 48, size / 48);
        g.setGlobalAlpha(opacity);
        g.setFill(Color.rgb(5, 19, 27, .88));
        g.fillRoundRect(0, 0, 48, 48, 9, 9);
        g.setStroke(color(u).deriveColor(0, 1, 1, .5));
        g.setLineWidth(1);
        g.strokeRoundRect(.5, .5, 47, 47, 9, 9);
        g.setStroke(color(u));
        g.setFill(color(u));
        g.setLineWidth(2.4);
        switch (u) {
            case SERVO -> {
                g.strokeOval(13, 13, 22, 22);
                g.strokeOval(20, 20, 8, 8);
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    g.strokeLine(
                            24 + 11 * Math.cos(a),
                            24 + 11 * Math.sin(a),
                            24 + 16 * Math.cos(a),
                            24 + 16 * Math.sin(a));
                }
            }
            case PLATING -> {
                g.strokePolygon(
                        new double[] {24, 37, 34, 24, 14, 11},
                        new double[] {9, 14, 30, 39, 30, 14},
                        6);
                g.strokeLine(24, 14, 24, 33);
            }
            case CAPACITOR -> {
                g.strokeRoundRect(13, 12, 22, 26, 3, 3);
                g.fillRect(19, 8, 10, 4);
                bolt(g);
            }
            case MEDICAL -> {
                g.fillRect(20, 10, 8, 28);
                g.fillRect(10, 20, 28, 8);
            }
            case COOLANT -> {
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3;
                    g.strokeLine(24, 24, 24 + 15 * Math.cos(a), 24 + 15 * Math.sin(a));
                }
                g.strokeOval(19, 19, 10, 10);
            }
            case RECOVERY -> {
                g.strokeArc(10, 10, 28, 28, 20, 280, javafx.scene.shape.ArcType.OPEN);
                g.fillPolygon(new double[] {33, 40, 30}, new double[] {12, 20, 21}, 3);
                g.strokeLine(18, 24, 30, 24);
                g.strokeLine(24, 18, 24, 30);
            }
            case LANCE -> {
                g.strokeLine(11, 37, 34, 14);
                g.strokeLine(17, 38, 38, 17);
                g.fillPolygon(new double[] {30, 39, 38}, new double[] {12, 9, 18}, 3);
            }
            case OVERCLOCK -> {
                g.strokeOval(10, 10, 28, 28);
                g.strokeLine(24, 24, 24, 15);
                g.strokeLine(24, 24, 34, 22);
                g.fillRect(19, 6, 10, 3);
            }
            case THRUSTER -> {
                g.strokePolygon(
                        new double[] {14, 19, 32, 35, 29, 18},
                        new double[] {27, 11, 11, 27, 32, 32},
                        6);
                g.strokeLine(19, 35, 16, 41);
                g.strokeLine(25, 35, 25, 43);
                g.strokeLine(31, 35, 34, 41);
            }
            case SIPHON -> {
                g.strokeArc(11, 10, 26, 27, 180, 180, javafx.scene.shape.ArcType.OPEN);
                g.strokeLine(11, 23, 11, 12);
                g.strokeLine(37, 23, 37, 12);
                g.fillRect(8, 9, 7, 8);
                g.fillRect(33, 9, 7, 8);
            }
            case ARC_COIL -> {
                bolt(g);
                g.strokeOval(8, 17, 6, 6);
                g.strokeOval(34, 28, 6, 6);
                g.strokeLine(14, 20, 21, 23);
                g.strokeLine(29, 27, 34, 31);
            }
            case REGEN -> {
                g.strokeOval(15, 15, 18, 18);
                g.strokeLine(24, 19, 24, 29);
                g.strokeLine(19, 24, 29, 24);
                for (int i = 0; i < 3; i++) {
                    double a = i * 2.1;
                    g.fillOval(22 + 16 * Math.cos(a), 22 + 16 * Math.sin(a), 4, 4);
                }
            }
        }
        g.restore();
    }

    private static void bolt(GraphicsContext g) {
        g.strokePolyline(new double[] {28, 19, 26, 20}, new double[] {13, 25, 25, 36}, 4);
    }
}
