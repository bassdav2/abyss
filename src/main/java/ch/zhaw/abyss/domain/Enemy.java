package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Gegner mit Art, Elite-Eigenschaft und expliziter Zustandsmaschine. Das Verhalten ist eine
 * zustandslose Strategie ({@link EnemyBehavior}); sämtlicher veränderlicher Zustand liegt hier.
 */
public final class Enemy extends Actor {
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
        this.stateTime = kind.boss() ? 2.2 : kind.swarm() ? .3 : kind == EnemyKind.EGG ? 3.2 : .55;
        this.homeX = x;
        this.homeY = y;
        if (affix == Affix.SHIELDED) eliteShield = maxHealth * .35;
    }

    @Override
    boolean flying() {
        return kind.flying();
    }

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
        return kind.boss() && state != State.RECOVER && state != State.STUNNED;
    }

    /**
     * @return {@code true}, solange der Gegner nicht getroffen werden kann
     */
    public boolean untargetable() {
        return veiled || state == State.HIDDEN || state == State.SPAWNING && kind.boss();
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
