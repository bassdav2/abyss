package ch.zhaw.abyss.domain;

public final class Enemy extends Actor {
    public enum State {
        APPROACH,
        WINDUP,
        STRIKE,
        RECOVER,
        STUNNED
    }

    final EnemyKind kind;
    State state = State.APPROACH;
    double stateTime, actionCooldown, targetX, targetY;
    int attackPattern, attacks;
    boolean strikeHit, enraged;

    Enemy(long id, EnemyKind kind, double x, double strength, double delay) {
        super(
                id,
                x,
                kind == EnemyKind.DRONE ? GameRun.FLOOR - 72 : GameRun.FLOOR,
                kind.width,
                kind.height,
                kind.health * strength);
        this.kind = kind;
        this.actionCooldown = delay;
        this.facing = -1;
    }

    public EnemyKind kind() {
        return kind;
    }

    public State state() {
        return state;
    }

    public double stateTime() {
        return stateTime;
    }

    public int attackPattern() {
        return attackPattern;
    }

    public double targetX() {
        return targetX;
    }

    public double targetY() {
        return targetY;
    }

    public boolean enraged() {
        return enraged;
    }

    public boolean armored() {
        return kind == EnemyKind.CAPTAIN && state != State.RECOVER;
    }
}
