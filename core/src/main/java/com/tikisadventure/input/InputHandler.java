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
    
    // Acciones como estado (pueden ser mantenidas)
    public boolean isAttacking = false;
    public boolean isAimingAbility2 = false;
    
    // Acciones como eventos (Just Pressed)
    public boolean isInteractingJustPressed = false;
    public boolean useAbility1JustPressed = false;
    public boolean useAbility2JustPressed = false;
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
        
        // No reseteamos los estados "just pressed" aquí porque el jugador 
        // debe procesarlos en su ciclo de update antes de que se limpien
        // O mejor: los reseteamos al final de cada frame del juego.
        isInteractingJustPressed = false;
        useAbility1JustPressed = false;
        useAbility2JustPressed = false;
        isToggleAutoFireJustPressed = false;
    }
}
