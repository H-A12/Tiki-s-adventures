package com.tikisadventure.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

public class ControllerInput implements ControllerListener {
    private final InputHandler handler;
    private boolean h2ButtonHeld = false;

    public ControllerInput(InputHandler handler) {
        this.handler = handler;
        Controllers.addListener(this);
    }

    @Override
    public void connected(Controller controller) {}

    @Override
    public void disconnected(Controller controller) {}

    @Override
    public boolean buttonDown(Controller controller, int buttonIndex) {
        if (buttonIndex == 0) handler.isInteracting = true;
        if (buttonIndex == 1) {
            handler.useDash = true;
        }
        if (buttonIndex == 3) {
            h2ButtonHeld = true;
            handler.isAimingAbility2 = true;
        }
        if (buttonIndex == 4) handler.useAbility1 = true;
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        if (buttonIndex == 0) handler.isInteracting = false;
        if (buttonIndex == 1) handler.useDash = false;
        if (buttonIndex == 3) {
            if (h2ButtonHeld) {
                handler.useAbility2 = true;
            }
            h2ButtonHeld = false;
            handler.isAimingAbility2 = false;
            handler.aimDirectionAbility2.setZero();
        }
        if (buttonIndex == 4) handler.useAbility1 = false;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        if (Math.abs(value) < 0.2f) value = 0;

        if (axisIndex == 0) handler.moveDirection.x = value;
        if (axisIndex == 1) handler.moveDirection.y = -value;

        if (h2ButtonHeld) {
            if (axisIndex == 2) handler.aimDirectionAbility2.x = value;
            if (axisIndex == 3) handler.aimDirectionAbility2.y = -value;
        } else {
            if (axisIndex == 2) handler.aimDirection.x = value;
            if (axisIndex == 3) handler.aimDirection.y = -value;
        }

        return false;
    }
}