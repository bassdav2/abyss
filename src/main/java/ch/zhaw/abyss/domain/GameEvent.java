package ch.zhaw.abyss.domain;

/** Einmalige Ereignisse für Audio/Partikel; die Domäne kennt deren Umsetzung nicht. */
public record GameEvent(Type type, double x, double y, double amount, String text) {
    public enum Type {
        SWING,
        HIT,
        PLAYER_HIT,
        ENEMY_DOWN,
        DASH,
        JUMP,
        PULSE,
        ARC,
        SHIELD,
        SHOT,
        TELEGRAPH,
        ROOM_CLEAR,
        UPGRADE,
        HEAL,
        DEFEAT,
        VICTORY,
        BOSS_PHASE,
        SUPPLY,
        REINFORCEMENTS
    }

    public static GameEvent at(Type type, double x, double y) {
        return new GameEvent(type, x, y, 0, "");
    }
}
