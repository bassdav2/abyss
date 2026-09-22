package ch.zhaw.abyss.domain;

/** Eingaben eines Simulationsschritts; unabhängig von JavaFX und Tastencodes. */
public record InputFrame(
        boolean left,
        boolean right,
        boolean jump,
        boolean dash,
        boolean attack,
        boolean ability,
        int aimDirection,
        boolean heal) {
    public InputFrame(
            boolean left,
            boolean right,
            boolean jump,
            boolean dash,
            boolean attack,
            boolean ability,
            int aimDirection) {
        this(left, right, jump, dash, attack, ability, aimDirection, false);
    }

    public InputFrame(
            boolean left,
            boolean right,
            boolean jump,
            boolean dash,
            boolean attack,
            boolean ability) {
        this(left, right, jump, dash, attack, ability, 0);
    }

    public InputFrame {
        if (aimDirection < -1 || aimDirection > 1)
            throw new IllegalArgumentException("Ungültige Blickrichtung");
    }

    public static final InputFrame NONE = new InputFrame(false, false, false, false, false, false);
}
