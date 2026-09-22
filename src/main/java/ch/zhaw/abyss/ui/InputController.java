package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.InputFrame;

import javafx.scene.input.KeyCode;

import java.util.EnumSet;

final class InputController {
    private final EnumSet<KeyCode> held = EnumSet.noneOf(KeyCode.class);
    private final EnumSet<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);
    private boolean mouseAttack, mouseAbility;
    private double pointerX = 800;

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

    void pointer(double x) {
        pointerX = x;
    }

    InputFrame frame(double playerX) {
        var result =
                new InputFrame(
                        held.contains(KeyCode.A) || held.contains(KeyCode.LEFT),
                        held.contains(KeyCode.D) || held.contains(KeyCode.RIGHT),
                        pressed.contains(KeyCode.SPACE)
                                || pressed.contains(KeyCode.W)
                                || pressed.contains(KeyCode.UP),
                        pressed.contains(KeyCode.SHIFT),
                        held.contains(KeyCode.J) || mouseAttack,
                        pressed.contains(KeyCode.K) || mouseAbility,
                        mouseAttack || mouseAbility ? (pointerX >= playerX ? 1 : -1) : 0);
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
