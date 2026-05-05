package com.tikisadventure.input;

import com.badlogic.gdx.math.Vector2;

public class InputHandler {
    public enum DeviceType { KEYBOARD, CONTROLLER, TOUCHPAD }
    public DeviceType activeDevice = DeviceType.KEYBOARD;
    private float lockTimer = 0f;
    private static final float LOCK_DURATION = 0.5f;

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
    public boolean isToggleAutoFireJustPressed = false;

    public void requestFocus(DeviceType device) {
        if (lockTimer <= 0) {
            activeDevice = device;
            lockTimer = LOCK_DURATION;
        }
    }

    public boolean isDeviceActive(DeviceType device) {
        return activeDevice == device;
    }

    public void update(float delta) {
        if (lockTimer > 0) lockTimer -= delta;
    }

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
        isToggleAutoFireJustPressed = false;
    }
}
