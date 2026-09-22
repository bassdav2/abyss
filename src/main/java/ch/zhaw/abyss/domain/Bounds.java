package ch.zhaw.abyss.domain;

public record Bounds(double x, double y, double width, double height) {
    public boolean intersects(Bounds other) {
        return x < other.x + other.width
                && x + width > other.x
                && y < other.y + other.height
                && y + height > other.y;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
