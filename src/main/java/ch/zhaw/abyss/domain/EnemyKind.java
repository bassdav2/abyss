package ch.zhaw.abyss.domain;

public enum EnemyKind {
    SCUTTLER("Schrottläufer", 44, 70, 78, 150),
    DRONE("Wachdrohne", 34, 70, 72, 100),
    SENTINEL("Schottwächter", 88, 84, 125, 95),
    WARDEN("Der Schottmeister", 520, 142, 164, 120),
    REACTOR("Der Reaktorkern", 670, 170, 178, 72),
    CAPTAIN("Der Lotse", 1050, 160, 184, 105);
    final double health, width, height, speed;
    private final String title;

    EnemyKind(String title, double health, double width, double height, double speed) {
        this.title = title;
        this.health = health;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public boolean boss() {
        return this == WARDEN || this == REACTOR || this == CAPTAIN;
    }

    public String title() {
        return title;
    }
}
