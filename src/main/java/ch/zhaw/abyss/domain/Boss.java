package ch.zhaw.abyss.domain;

/**
 * Wächter am Ende einer Sektion. Spezialisiert {@link Enemy} durch Vererbung: Statt an vielen
 * Stellen {@code kind.boss()} abzufragen, überschreibt diese Klasse die Reaktionen, in denen sich
 * Wächter unterscheiden (GRASP Polymorphism, Information Expert). Wächter sind standfest, haben
 * drei Phasen mit Schadensgrenze, tragen ausserhalb der Erholung Panzerung und legen ihren Kern
 * frei, wenn Raumtechnik oder ein Torpedo sie trifft.
 */
final class Boss extends Enemy {
    /** Dauer des Auftritts, bevor der Wächter angreift. */
    private static final double ENTRANCE = 2.2;

    Boss(long id, EnemyKind kind, Affix affix, double x, double y, double strength, double delay) {
        super(id, kind, affix, x, y, strength, delay);
        stateTime = ENTRANCE;
    }

    @Override
    boolean staggerable() {
        return false;
    }

    @Override
    boolean stun(double seconds) {
        return false;
    }

    @Override
    double knockbackFactor() {
        return .12;
    }

    @Override
    double pullFactor() {
        return .1;
    }

    /** Ein Treffer kann die Lebenspunkte nicht unter die Grenze der aktuellen Phase drücken. */
    @Override
    double limitDamage(double damage) {
        return Math.min(damage, Math.max(0, health - phaseFloor()));
    }

    /**
     * Untere Lebensgrenze der laufenden Phase. Kein Treffer schlägt tiefer, bevor die nächste Phase
     * begonnen hat: So lässt sich keine Phase überspringen.
     *
     * @return Lebenspunkte, bei denen die aktuelle Phase endet
     */
    double phaseFloor() {
        double share = phase == 0 ? 2 / 3.0 : phase == 1 ? 1 / 3.0 : 0;
        return maxHealth * share * (1 - 1e-9);
    }

    /** Wächter frieren nicht ein, sie werden nur länger verlangsamt. */
    @Override
    void applyStatus(Status status, double seconds) {
        if (status == Status.FREEZE) statuses.apply(Status.CHILL, seconds * 1.5);
        else super.applyStatus(status, seconds);
    }

    @Override
    double torpedoShare() {
        return .045;
    }

    @Override
    boolean expose(State into, double seconds) {
        state = into;
        stateTime = seconds;
        telegraph = null;
        return true;
    }

    @Override
    boolean hasPhases() {
        return true;
    }

    @Override
    double shardReward() {
        return 30;
    }

    @Override
    GameEvent.Type downEvent() {
        return GameEvent.Type.BOSS_DOWN;
    }

    /**
     * Ein besiegter Wächter beendet laufende Horden, lässt Kerne fallen und nimmt seine Brut mit.
     */
    @Override
    void onDefeated(GameRun run) {
        run.cancelHordes();
        run.drop(Pickup.Kind.CORE, kind == EnemyKind.CAPTAIN ? 5 : 3, x, centerY());
        for (var minion : run.enemies)
            if (children.contains(minion.id) && minion.alive()) {
                minion.health = 0;
                run.emit(GameEvent.at(GameEvent.Type.ENEMY_DOWN, minion.x, minion.centerY()));
            }
    }

    @Override
    public boolean armored() {
        return state != State.RECOVER && state != State.STUNNED;
    }

    @Override
    public boolean untargetable() {
        return super.untargetable() || state == State.SPAWNING;
    }
}
