package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.InputFrame;

import javafx.scene.input.KeyCode;

import java.util.EnumSet;

/**
 * Übersetzt Tastatur und Maus in {@link InputFrame}-Werte. Gedrückt-Ereignisse gelten genau einen
 * Simulationsschritt, damit ein Tastendruck nicht mehrfach auslöst.
 */
final class InputController {
    private final EnumSet<KeyCode> held = EnumSet.noneOf(KeyCode.class);
    private final EnumSet<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);
    private boolean mouseAttack, mouseAbility;
    private double pointerX = -1;

    void down(KeyCode key) {
        if (held.add(key)) pressed.add(key);
    }

    void up(KeyCode key) {
        held.remove(key);
    }

    void mouseAttack(boolean value) {
        mouseAttack = value;
    }

    void mouseAbility(boolean value) {
        mouseAbility = value;
    }

    /**
     * @param x Mausposition in Bildpixeln
     */
    void pointer(double x) {
        pointerX = x;
    }

    /**
     * Erzeugt die Eingabe eines Schritts.
     *
     * @param playerScreenX Position der Spielfigur in Bildpixeln für die Mausrichtung
     * @return Eingabe
     */
    InputFrame frame(double playerScreenX) {
        boolean mouse = mouseAttack || mouseAbility;
        var result =
                new InputFrame(
                        held.contains(KeyCode.A) || held.contains(KeyCode.LEFT),
                        held.contains(KeyCode.D) || held.contains(KeyCode.RIGHT),
                        held.contains(KeyCode.S) || held.contains(KeyCode.DOWN),
                        pressed.contains(KeyCode.SPACE)
                                || pressed.contains(KeyCode.W)
                                || pressed.contains(KeyCode.UP),
                        held.contains(KeyCode.SPACE)
                                || held.contains(KeyCode.W)
                                || held.contains(KeyCode.UP),
                        pressed.contains(KeyCode.SHIFT) || pressed.contains(KeyCode.L),
                        held.contains(KeyCode.J) || mouseAttack,
                        pressed.contains(KeyCode.K) || mouseAbility,
                        mouse && pointerX >= 0 ? (pointerX >= playerScreenX ? 1 : -1) : 0,
                        pressed.contains(KeyCode.Q));
        pressed.clear();
        mouseAbility = false;
        return result;
    }

    void clear() {
        held.clear();
        pressed.clear();
        mouseAttack = false;
        mouseAbility = false;
    }
}
