package ch.zhaw.abyss.domain;

/**
 * Einmalige Ereignisse für Audio, Partikel und Kamera. Die Domäne kennt deren Umsetzung nicht
 * (Beobachter über eine entnehmbare Ereignisliste).
 *
 * @param type Ereignisart
 * @param x horizontale Weltposition
 * @param y vertikale Weltposition
 * @param amount Zahlwert wie Schaden oder Radius
 * @param text optionaler Text
 * @param x2 zweiter Punkt, etwa Ursprung eines Kettenblitzes
 * @param y2 zweiter Punkt, vertikal
 */
public record GameEvent(
        Type type, double x, double y, double amount, String text, double x2, double y2) {
    /** Ereignisarten. */
    public enum Type {
        SWING,
        HIT,
        CRIT,
        PLAYER_HIT,
        SHIELD_HIT,
        BLOCK,
        ENEMY_DOWN,
        ELITE_DOWN,
        BOSS_DOWN,
        DASH,
        JUMP,
        AIR_JUMP,
        LAND,
        PULSE,
        ARC,
        CHAIN,
        SHIELD,
        SHOT,
        HARPOON,
        TORPEDO,
        EXPLOSION,
        FREEZE,
        SONAR,
        OVERDRIVE,
        DRONE,
        FLAME,
        SHOCK,
        SLAM,
        TELEGRAPH,
        SPAWN,
        ROOM_CLEAR,
        UPGRADE,
        SYNERGY,
        CONDITION,
        MACHINE,
        WEAPON,
        HEAL,
        PURCHASE,
        CURSE,
        DEFEAT,
        VICTORY,
        REVIVE,
        BOSS_INTRO,
        BOSS_PHASE,
        SUPPLY,
        CRATE_BREAK,
        PICKUP,
        CORE,
        REINFORCEMENTS,
        DOOR
    }

    /**
     * Ereignis an einem Punkt.
     *
     * @param type Ereignisart
     * @param x horizontale Position
     * @param y vertikale Position
     * @param amount Zahlwert
     * @param text Text
     */
    public GameEvent(Type type, double x, double y, double amount, String text) {
        this(type, x, y, amount, text, x, y);
    }

    /**
     * @param type Ereignisart
     * @param x horizontale Position
     * @param y vertikale Position
     * @return Ereignis ohne Zahlwert und Text
     */
    public static GameEvent at(Type type, double x, double y) {
        return new GameEvent(type, x, y, 0, "");
    }

    /**
     * Verbindung zweier Punkte, etwa ein Kettenblitz.
     *
     * @param type Ereignisart
     * @param fromX Ursprung x
     * @param fromY Ursprung y
     * @param toX Ziel x
     * @param toY Ziel y
     * @return Ereignis mit beiden Punkten; {@code x/y} ist das Ziel
     */
    public static GameEvent link(Type type, double fromX, double fromY, double toX, double toY) {
        return new GameEvent(type, toX, toY, 0, "", fromX, fromY);
    }
}
