package com.tikisadventure.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.tikisadventure.core.SaveManager;

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
        InputConfig config = SaveManager.getProfileData().inputConfig;
        
        handler.requestFocus(InputHandler.DeviceType.CONTROLLER);
        if (!handler.isDeviceActive(InputHandler.DeviceType.CONTROLLER)) return false;

        if (buttonIndex == config.gamepadMapping.get("interact")) handler.isInteracting = true;
        if (buttonIndex == config.gamepadMapping.get("dash")) {
            handler.useDash = true;
        }
        if (buttonIndex == config.gamepadMapping.get("ability2")) {
            h2ButtonHeld = true;
            handler.isAimingAbility2 = true;
        }
        if (buttonIndex == config.gamepadMapping.get("ability1")) handler.useAbility1 = true;
        if (buttonIndex == config.gamepadMapping.get("toggleAutoFire")) handler.isToggleAutoFireJustPressed = true;
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        InputConfig config = SaveManager.getProfileData().inputConfig;
        if (!handler.isDeviceActive(InputHandler.DeviceType.CONTROLLER)) return false;

        if (buttonIndex == config.gamepadMapping.get("interact")) handler.isInteracting = false;
        if (buttonIndex == config.gamepadMapping.get("dash")) handler.useDash = false;
        if (buttonIndex == config.gamepadMapping.get("ability2")) {
            if (h2ButtonHeld) {
                handler.useAbility2 = true;
            }
            h2ButtonHeld = false;
            handler.isAimingAbility2 = false;
            handler.aimDirectionAbility2.setZero();
            handler.aimMagnitudeAbility2 = 0;
        }
        if (buttonIndex == config.gamepadMapping.get("ability1")) handler.useAbility1 = false;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        if (Math.abs(value) < 0.2f) value = 0;
        if (value != 0) handler.requestFocus(InputHandler.DeviceType.CONTROLLER);
        if (!handler.isDeviceActive(InputHandler.DeviceType.CONTROLLER)) return false;

        if (axisIndex == 0) handler.moveDirection.x = value;
        if (axisIndex == 1) handler.moveDirection.y = -value;

        if (h2ButtonHeld) {
            if (axisIndex == 2) handler.aimDirectionAbility2.x = value;
            if (axisIndex == 3) handler.aimDirectionAbility2.y = -value;

            float magnitude = (float) Math.sqrt(
                handler.aimDirectionAbility2.x * handler.aimDirectionAbility2.x +
                handler.aimDirectionAbility2.y * handler.aimDirectionAbility2.y
            );
            handler.aimMagnitudeAbility2 = magnitude;
        } else {
            if (axisIndex == 2) handler.aimDirection.x = value;
            if (axisIndex == 3) handler.aimDirection.y = -value;
        }

        return false;
    }
}