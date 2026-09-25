package ch.zhaw.abyss.ui;

import static org.junit.jupiter.api.Assertions.*;

import ch.zhaw.abyss.domain.InputFrame;

import javafx.scene.input.KeyCode;

import org.junit.jupiter.api.Test;

/** Übersetzung von Tastatur und Maus in Simulationseingaben. */
class InputControllerTest {
    @Test
    void downPlusJumpAndHeldJumpAreReported() {
        var input = new InputController();
        input.down(KeyCode.S);
        input.down(KeyCode.SPACE);
        var frame = input.frame(100);
        assertTrue(frame.down() && frame.jump() && frame.jumpHeld());
        var held = input.frame(100);
        assertFalse(held.jump());
        assertTrue(held.jumpHeld());
        input.up(KeyCode.SPACE);
        assertFalse(input.frame(100).jumpHeld());
    }

    @Test
    void alternativeDashKeyWorks() {
        var input = new InputController();
        input.down(KeyCode.L);
        assertTrue(input.frame(100).dash());
    }

    @Test
    void jumpDashAndAbilityUseRisingEdgesDespiteKeyboardAutoRepeat() {
        var input = new InputController();
        input.down(KeyCode.SPACE);
        input.down(KeyCode.SHIFT);
        input.down(KeyCode.K);
        var first = input.frame(170);
        assertTrue(first.jump() && first.dash() && first.ability());
        input.down(KeyCode.SPACE);
        input.down(KeyCode.SHIFT);
        input.down(KeyCode.K);
        var repeated = input.frame(170);
        assertFalse(repeated.jump() || repeated.dash() || repeated.ability());
        input.up(KeyCode.SPACE);
        input.down(KeyCode.SPACE);
        assertTrue(input.frame(170).jump());
    }

    @Test
    void heldMeleeAndMovementContinueButModalClearPreventsStuckKeys() {
        var input = new InputController();
        input.down(KeyCode.D);
        input.down(KeyCode.J);
        for (int frame = 0; frame < 100; frame++) assertTrue(input.frame(170).attack());
        input.mouseAttack(true);
        input.mouseAbility(true);
        input.clear();
        assertEquals(InputFrame.NONE, input.frame(170));
    }

    @Test
    void mouseAimsIndependentlyAndSecondaryClickTriggersOnce() {
        var input = new InputController();
        input.pointer(40);
        input.mouseAttack(true);
        input.mouseAbility(true);
        var frame = input.frame(170);
        assertEquals(-1, frame.aimDirection());
        assertTrue(frame.attack() && frame.ability());
        assertFalse(input.frame(170).ability());
        input.pointer(400);
        assertEquals(1, input.frame(170).aimDirection());
        input.mouseAttack(false);
        assertEquals(0, input.frame(170).aimDirection());
    }

    @org.junit.jupiter.api.Test
    void repairKeyUsesRisingEdgeAndIgnoresOsAutoRepeat() {
        var input = new InputController();
        input.down(javafx.scene.input.KeyCode.Q);
        assertTrue(input.frame(0).heal());
        input.down(javafx.scene.input.KeyCode.Q);
        assertFalse(input.frame(0).heal());
        input.up(javafx.scene.input.KeyCode.Q);
        input.down(javafx.scene.input.KeyCode.Q);
        assertTrue(input.frame(0).heal());
    }
}
