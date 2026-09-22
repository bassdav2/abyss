package ch.zhaw.abyss.domain;

/** Weltkoordinaten: x ist die Mitte, y die Position der Füße. */
public abstract class Actor {
    final long id;
    double x, y, vx, vy;
    double health, maxHealth;
    int facing = 1;
    double hurtTime, animationTime;
    final double width, height;

    protected Actor(long id, double x, double y, double width, double height, double health) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = health;
        this.maxHealth = health;
    }

    public long id() {
        return id;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double vx() {
        return vx;
    }

    public double health() {
        return health;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public int facing() {
        return facing;
    }

    public double hurtTime() {
        return hurtTime;
    }

    public double animationTime() {
        return animationTime;
    }

    public boolean alive() {
        return health > 0;
    }

    public Bounds bounds() {
        return new Bounds(x - width / 2, y - height, width, height);
    }
}
