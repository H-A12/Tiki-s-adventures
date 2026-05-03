package com.tikisadventure.input;

import com.badlogic.gdx.math.Vector2;

public class InputHandler {
    private static InputHandler instance;
    public static InputHandler getInstance() {
        if (instance == null) instance = new InputHandler();
        return instance;
    }

    public enum InputSource { KEYBOARD_MOUSE, TOUCHPAD, CONTROLLER }
    public InputSource lastInputSource = InputSource.KEYBOARD_MOUSE;
    
    public Vector2 moveDirection = new Vector2();
    public Vector2 aimDirection = new Vector2();
    public Vector2 aimDirectionAbility2 = new Vector2();
    public Vector2 aimTargetAbility2 = new Vector2();
    public float aimMagnitudeAbility2 = 0;
    public boolean isAttacking = false;
    public boolean isInteracting = false;
    public boolean useAbility1 = false;
    public boolean useAbility2 = false;
    public boolean useDash = false;
    public boolean isAimingAbility2 = false;

    // Temporary storage for arbitration
    public final InputState keyboardState = new InputState();
    public final InputState touchpadState = new InputState();
    public final InputState controllerState = new InputState();

    public static class InputState {
        public Vector2 moveDirection = new Vector2();
        public Vector2 aimDirection = new Vector2();
        public Vector2 aimDirectionAbility2 = new Vector2();
        public Vector2 aimTargetAbility2 = new Vector2();
        public float aimMagnitudeAbility2 = 0;
        public boolean isAttacking = false;
        public boolean isInteracting = false;
        public boolean useAbility1 = false;
        public boolean useAbility2 = false;
        public boolean useDash = false;
        public boolean isAimingAbility2 = false;

        public void reset() {
            moveDirection.setZero();
            aimDirection.setZero();
            aimDirectionAbility2.setZero();
            aimTargetAbility2.setZero();
            aimMagnitudeAbility2 = 0;
            isAttacking = false;
            isInteracting = false;
            useAbility1 = false;
            useAbility2 = false;
            useDash = false;
            isAimingAbility2 = false;
        }
    }

    public void reset() {
        keyboardState.reset();
        touchpadState.reset();
        controllerState.reset();
        // The main fields are reset after arbitration in update
    }
    
    public void arbitrate() {
        // Priority: KEYBOARD_MOUSE > TOUCHPAD > CONTROLLER
        if (!keyboardState.moveDirection.isZero() || keyboardState.isAttacking || keyboardState.isInteracting || keyboardState.useAbility1 || keyboardState.useAbility2 || keyboardState.useDash || keyboardState.isAimingAbility2) {
            applyState(keyboardState);
            lastInputSource = InputSource.KEYBOARD_MOUSE;
        } else if (!touchpadState.moveDirection.isZero() || touchpadState.isAttacking || touchpadState.isInteracting || touchpadState.useAbility1 || touchpadState.useAbility2 || touchpadState.useDash || touchpadState.isAimingAbility2) {
            applyState(touchpadState);
            lastInputSource = InputSource.TOUCHPAD;
        } else {
            applyState(controllerState);
            lastInputSource = InputSource.CONTROLLER;
        }
    }

    private void applyState(InputState state) {
        moveDirection.set(state.moveDirection);
        aimDirection.set(state.aimDirection);
        aimDirectionAbility2.set(state.aimDirectionAbility2);
        aimTargetAbility2.set(state.aimTargetAbility2);
        aimMagnitudeAbility2 = state.aimMagnitudeAbility2;
        isAttacking = state.isAttacking;
        isInteracting = state.isInteracting;
        useAbility1 = state.useAbility1;
        useAbility2 = state.useAbility2;
        useDash = state.useDash;
        isAimingAbility2 = state.isAimingAbility2;
    }
}
