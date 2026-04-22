package com.tikisadventure.input;

import com.badlogic.gdx.math.Vector2;

public class InputHandler {
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