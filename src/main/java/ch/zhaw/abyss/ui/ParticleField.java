package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.GameEvent;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class ParticleField {
    private static final class Particle {
        double x, y, vx, vy, life, maxLife, size;
        Color color;

        Particle(double x, double y, double vx, double vy, double life, double size, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.size = size;
            this.color = color;
        }
    }

    private record Number(double x, double y, double created, String text, Color color) {}

    private final List<Particle> particles = new ArrayList<>();
    private final List<Number> numbers = new ArrayList<>();
    private final Random random = new Random(1729);
    private double clock;

    void event(GameEvent event, boolean reduced) {
        Color color =
                event.type() == GameEvent.Type.PLAYER_HIT
                        ? Color.web("#ff6f55")
                        : event.type() == GameEvent.Type.PULSE
                                        || event.type() == GameEvent.Type.ARC
                                        || event.type() == GameEvent.Type.DASH
                                ? Color.web("#78eeeb")
                                : Color.web("#ffd282");
        int count =
                switch (event.type()) {
                    case HIT -> 13;
                    case SUPPLY -> 18;
                    case ENEMY_DOWN -> 34;
                    case PLAYER_HIT -> 18;
                    case PULSE, ARC -> 32;
                    case DASH -> 14;
                    case JUMP -> 8;
                    default -> 0;
                };
        if (reduced) count /= 3;
        for (int i = 0; i < count && particles.size() < 450; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 60 + random.nextDouble() * 290;
            particles.add(
                    new Particle(
                            event.x(),
                            event.y(),
                            Math.cos(angle) * speed,
                            Math.sin(angle) * speed - 60,
                            .25 + random.nextDouble() * .5,
                            1.5 + random.nextDouble() * 3,
                            color));
        }
        if (event.type() == GameEvent.Type.HIT || event.type() == GameEvent.Type.PLAYER_HIT)
            numbers.add(
                    new Number(
                            event.x(),
                            event.y() - 35,
                            clock,
                            "" + Math.round(event.amount()),
                            color));
    }

    void update(double dt) {
        clock += dt;
        for (var p : particles) {
            p.life -= dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy += dt * 440;
        }
        particles.removeIf(p -> p.life <= 0);
        numbers.removeIf(n -> clock - n.created() > .8);
    }

    void draw(GraphicsContext g, AssetCatalog assets) {
        for (var p : particles) {
            g.setGlobalAlpha(Math.max(0, p.life / p.maxLife));
            g.setFill(p.color);
            g.fillRect(p.x, p.y, p.size * 2, p.size);
        }
        g.setFont(assets.display(24));
        for (var n : numbers) {
            double age = clock - n.created();
            g.setGlobalAlpha(Math.min(1, (1 - age / .8) * 2));
            g.setFill(Color.BLACK);
            g.fillText(n.text(), n.x() + 1, n.y() - age * 54 + 2);
            g.setFill(n.color());
            g.fillText(n.text(), n.x(), n.y() - age * 54);
        }
        g.setGlobalAlpha(1);
    }

    void clear() {
        particles.clear();
        numbers.clear();
    }
}
