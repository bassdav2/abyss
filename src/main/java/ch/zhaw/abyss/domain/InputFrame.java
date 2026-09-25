package ch.zhaw.abyss.domain;

/**
 * Eingaben eines Simulationsschritts; unabhängig von JavaFX und Tastencodes. Gedrückt-Felder
 * ({@code jump}, {@code dash}, {@code ability}, {@code heal}) gelten nur für den ersten Schritt
 * nach dem Tastendruck, Gehalten-Felder solange die Taste unten ist.
 *
 * @param left links gehalten
 * @param right rechts gehalten
 * @param down unten gehalten (mit Sprung: vom Steg fallen lassen)
 * @param jump Sprung gedrückt
 * @param jumpHeld Sprung gehalten (höherer Sprung)
 * @param dash Ausweichen gedrückt
 * @param attack Angriff gehalten
 * @param ability aktives Modul gedrückt
 * @param aimDirection Mausblickrichtung -1, 0 oder 1
 * @param heal Reparaturset gedrückt
 */
public record InputFrame(
        boolean left,
        boolean right,
        boolean down,
        boolean jump,
        boolean jumpHeld,
        boolean dash,
        boolean attack,
        boolean ability,
        int aimDirection,
        boolean heal) {
    /** Keine Eingabe. */
    public static final InputFrame NONE = builder().build();

    /** Prüft die Blickrichtung. */
    public InputFrame {
        if (aimDirection < -1 || aimDirection > 1)
            throw new IllegalArgumentException("Ungültige Blickrichtung");
    }

    /**
     * @return leerer Baukasten für lesbare Tests und Bots
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Baukasten für Eingaben. */
    public static final class Builder {
        private boolean left, right, down, jump, jumpHeld, dash, attack, ability, heal;
        private int aim;

        private Builder() {}

        /**
         * @return Baukasten mit gehaltenem Links
         */
        public Builder left() {
            left = true;
            return this;
        }

        /**
         * @return Baukasten mit gehaltenem Rechts
         */
        public Builder right() {
            right = true;
            return this;
        }

        /**
         * @param direction -1 links, 1 rechts, 0 keine Bewegung
         * @return Baukasten
         */
        public Builder move(int direction) {
            left = direction < 0;
            right = direction > 0;
            return this;
        }

        /**
         * @return Baukasten mit gehaltenem Unten
         */
        public Builder down() {
            down = true;
            return this;
        }

        /**
         * @return Baukasten mit gedrücktem und gehaltenem Sprung
         */
        public Builder jump() {
            jump = true;
            jumpHeld = true;
            return this;
        }

        /**
         * @return Baukasten mit gehaltenem Sprung ohne neuen Druck
         */
        public Builder holdJump() {
            jumpHeld = true;
            return this;
        }

        /**
         * @return Baukasten mit Ausweichen
         */
        public Builder dash() {
            dash = true;
            return this;
        }

        /**
         * @return Baukasten mit Angriff
         */
        public Builder attack() {
            attack = true;
            return this;
        }

        /**
         * @return Baukasten mit aktivem Modul
         */
        public Builder ability() {
            ability = true;
            return this;
        }

        /**
         * @return Baukasten mit Reparaturset
         */
        public Builder heal() {
            heal = true;
            return this;
        }

        /**
         * @param direction Mausblickrichtung
         * @return Baukasten
         */
        public Builder aim(int direction) {
            aim = direction;
            return this;
        }

        /**
         * @return unveränderliche Eingabe
         */
        public InputFrame build() {
            return new InputFrame(
                    left, right, down, jump, jumpHeld, dash, attack, ability, aim, heal);
        }
    }
}
