package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Gegner mit Art, Elite-Eigenschaft und expliziter Zustandsmaschine. Das Verhalten ist eine
 * zustandslose Strategie ({@link EnemyBehavior}); sämtlicher veränderlicher Zustand liegt hier.
 *
 * <p>Wie ein Gegner auf Treffer, Rückstoss, Betäubung und Raumtechnik reagiert, legen
 * überschreibbare Methoden fest. Wächter spezialisieren sie in {@link Boss} (Polymorphismus statt
 * Typabfragen wie {@code kind.boss()}). Erzeugt werden Gegner über die Fabrikmethode {@link
 * #create}.
 */
public class Enemy extends Actor {
    /** Zustände der Kampf-Zustandsmaschine. */
    public enum State {
        SPAWNING,
        APPROACH,
        WINDUP,
        STRIKE,
        RECOVER,
        STUNNED,
        HIDDEN
    }

    final EnemyKind kind;
    final Affix affix;
    final EnemyBehavior behavior;
    State state = State.SPAWNING;
    double stateTime, actionCooldown, targetX, targetY;
    int pattern, attacks, phase, burst;
    boolean strikeHit, enraged, fired, escaped;
    double knockVx, eliteShield, regenDelay;
    double homeX, homeY;

    /** Freie Zeitgeber und Schrittzähler für die Bossmechaniken. */
    double aux, clock;

    int step;

    /** Panzerschwarm: normale Treffer wirken nur gedämpft. */
    boolean plated;

    /** Entrückt: vorübergehend nicht angreifbar, etwa beim Phasenwechsel der Prismenkaiserin. */
    boolean veiled;

    /** Rasend nach zu langem Kampf: mehr Schaden und schnellere Muster. */
    boolean furious;

    /**
     * Deckung gedeckter Bosse: so viel Schaden darf gerade noch durch. Sie füllt sich langsam und
     * in den Erholungsfenstern schnell; alles darüber prallt ab.
     */
    double guard;

    /** War im letzten Schritt ein Erholungsfenster offen (für den Deckungsstoss beim Öffnen)? */
    boolean exposedLast;

    /** Abklingzeit des Blinzelns, mit dem der Lotse Nahkämpfern ausweicht. */
    double blinkCooldown;

    /** Darstellungsgrösse, etwa für Kolosse. */
    double size = 1;

    Telegraph telegraph;
    final List<Long> children = new ArrayList<>();

    /** Gegner, der diesen herbeigerufen hat, sonst 0. */
    long parentId;

    Enemy(long id, EnemyKind kind, Affix affix, double x, double y, double strength, double delay) {
        super(
                id,
                x,
                y,
                kind.width,
                kind.height,
                kind.health * strength * (affix.elite() ? 1.7 : 1));
        this.kind = kind;
        this.affix = affix;
        this.behavior = kind.behavior();
        this.actionCooldown = delay;
        this.facing = -1;
        this.stateTime = kind.swarm() ? .3 : kind == EnemyKind.EGG ? 3.2 : .55;
        this.homeX = x;
        this.homeY = y;
        if (affix == Affix.SHIELDED) eliteShield = maxHealth * .35;
    }

    /**
     * Fabrikmethode: Wächterarten werden als {@link Boss} erzeugt, alle anderen als {@code Enemy}.
     *
     * @param id eindeutige Nummer
     * @param kind Gegnerart
     * @param affix Elite-Eigenschaft
     * @param x Position
     * @param y Position
     * @param strength Lebensfaktor
     * @param delay Verzögerung bis zum ersten Angriff
     * @return neuer Gegner
     */
    static Enemy create(
            long id,
            EnemyKind kind,
            Affix affix,
            double x,
            double y,
            double strength,
            double delay) {
        return kind.boss()
                ? new Boss(id, kind, affix, x, y, strength, delay)
                : new Enemy(id, kind, affix, x, y, strength, delay);
    }

    @Override
    boolean flying() {
        return kind.flying();
    }

    // --- Reaktionen, die Wächter in Boss überschreiben -----------------------------------------

    /**
     * @return {@code true}, wenn sich der Gegner betäuben, hochschleudern oder verschieben lässt
     */
    boolean staggerable() {
        return true;
    }

    /**
     * Betäubt den Gegner.
     *
     * @param seconds Dauer
     * @return {@code true}, wenn die Betäubung wirkt
     */
    boolean stun(double seconds) {
        state = State.STUNNED;
        stateTime = seconds;
        return true;
    }

    /**
     * @return Anteil des Rückstosses, der beim Gegner ankommt
     */
    double knockbackFactor() {
        return affix.elite() ? .5 : 1;
    }

    /**
     * @return Anteil eines Sogs, der beim Gegner ankommt
     */
    double pullFactor() {
        return affix.elite() ? .6 : 1;
    }

    /**
     * Begrenzt einen Treffer, etwa auf die Grenze der aktuellen Bossphase.
     *
     * @param damage berechneter Schaden
     * @return tatsächlich wirksamer Schaden
     */
    double limitDamage(double damage) {
        return damage;
    }

    /**
     * Wendet einen Zustand wie Brand oder Frost an.
     *
     * @param status Zustand
     * @param seconds Dauer
     */
    void applyStatus(Status status, double seconds) {
        statuses.apply(status, seconds);
    }

    /**
     * @return Anteil der maximalen Lebenspunkte, den ein Torpedotreffer kostet
     */
    double torpedoShare() {
        return .5;
    }

    /**
     * Legt den Kern frei, wenn der Gegner einen hat. Gewöhnliche Gegner haben keinen.
     *
     * @param into Zustand während des Freiliegens
     * @param seconds Dauer
     * @return {@code true}, wenn ein Kern freigelegt wurde
     */
    boolean expose(State into, double seconds) {
        return false;
    }

    /**
     * @return {@code true}, wenn der Gegner Kampfphasen hat
     */
    boolean hasPhases() {
        return false;
    }

    /**
     * @return Wert der Splitter, die der Gegner beim Ende fallen lässt
     */
    double shardReward() {
        if (kind.swarm() || parentId != 0) return 1;
        return 1 + kind.threat() + (affix.elite() ? 8 : 0);
    }

    /**
     * @return Ereignis, das beim Ende des Gegners gemeldet wird
     */
    GameEvent.Type downEvent() {
        return affix.elite() ? GameEvent.Type.ELITE_DOWN : GameEvent.Type.ENEMY_DOWN;
    }

    /**
     * Zusätzliche Folgen, wenn der Gegner besiegt wird. Gewöhnliche Gegner haben keine.
     *
     * @param run laufender Tauchgang
     */
    void onDefeated(GameRun run) {}

    /**
     * @return Gegnerart
     */
    public EnemyKind kind() {
        return kind;
    }

    /**
     * @return Elite-Eigenschaft oder {@link Affix#NONE}
     */
    public Affix affix() {
        return affix;
    }

    /**
     * @return aktueller Zustand
     */
    public State state() {
        return state;
    }

    /**
     * @return verbleibende Zeit im Zustand
     */
    public double stateTime() {
        return stateTime;
    }

    /**
     * @return gewähltes Angriffsmuster
     */
    public int pattern() {
        return pattern;
    }

    /**
     * @return Bossphase ab 0
     */
    public int phase() {
        return phase;
    }

    /**
     * @return Zielpunkt x des aktuellen Angriffs
     */
    public double targetX() {
        return targetX;
    }

    /**
     * @return Zielpunkt y des aktuellen Angriffs
     */
    public double targetY() {
        return targetY;
    }

    /**
     * @return {@code true} nach der Halbzeit eines Bosskampfs
     */
    public boolean enraged() {
        return enraged;
    }

    /**
     * @return aktuelle Vorwarnung oder {@code null}
     */
    public Telegraph telegraph() {
        return state == State.WINDUP ? telegraph : null;
    }

    /**
     * @return {@code true} in einem Panzerschwarm: nur kritische Treffer, Brand und Explosionen
     *     wirken voll
     */
    public boolean plated() {
        return plated;
    }

    /**
     * @return Darstellungsgrösse, 1 für normale Gegner
     */
    public double size() {
        return size;
    }

    /**
     * @return Schrittzähler der laufenden Bossmechanik
     */
    public int step() {
        return step;
    }

    /**
     * @return freier Zeitgeber der laufenden Mechanik
     */
    public double clock() {
        return clock;
    }

    /**
     * @return verbleibender Eliteschild
     */
    public double eliteShield() {
        return eliteShield;
    }

    /**
     * @return {@code true}, wenn der Gegner gerade Panzerung trägt
     */
    public boolean armored() {
        return false;
    }

    /**
     * @return {@code true}, solange der Gegner nicht getroffen werden kann
     */
    public boolean untargetable() {
        return veiled || state == State.HIDDEN;
    }

    /**
     * @return {@code true}, wenn der Gegner nach zu langem Kampf rast
     */
    public boolean furious() {
        return furious;
    }

    /**
     * @return Anteil der verbleibenden Integrität
     */
    public double healthRatio() {
        return maxHealth <= 0 ? 0 : health / maxHealth;
    }

    /**
     * @return {@code true}, solange der Gegner lebt und nicht entkommen ist
     */
    @Override
    public boolean alive() {
        return super.alive() && !escaped;
    }

    /**
     * @return {@code true}, wenn der Gegner den Raum verlassen hat, etwa eine Schmugglerdrohne
     */
    public boolean escaped() {
        return escaped;
    }
}
