package com.tikisadventure.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.Gdx;
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
        if (!handler.isDeviceActive(InputHandler.DeviceType.CONTROLLER)) {
            return false;
        }

        java.util.Map<String, Integer> map = config.gamepadButtonMapping;
        
        if (map.containsKey("interact") && buttonIndex == map.get("interact")) handler.isInteractingJustPressed = true;
        if (map.containsKey("ability2") && buttonIndex == map.get("ability2")) {
            h2ButtonHeld = true;
            handler.isAimingAbility2 = true;
        }
        if (map.containsKey("ability1") && buttonIndex == map.get("ability1")) handler.useAbility1JustPressed = true;
        if (map.containsKey("toggleAutoFire") && buttonIndex == map.get("toggleAutoFire")) handler.isToggleAutoFireJustPressed = true;
        
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        InputConfig config = SaveManager.getProfileData().inputConfig;
        if (!handler.isDeviceActive(InputHandler.DeviceType.CONTROLLER)) return false;

        java.util.Map<String, Integer> map = config.gamepadButtonMapping;

        if (map.containsKey("ability2") && buttonIndex == map.get("ability2")) {
            if (h2ButtonHeld) {
                handler.useAbility2JustPressed = true;
            }
            h2ButtonHeld = false;
            handler.isAimingAbility2 = false;
            handler.aimDirectionAbility2.setZero();
            handler.aimMagnitudeAbility2 = 0;
        }
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        if (Math.abs(value) < 0.2f) value = 0;
        
        InputConfig config = SaveManager.getProfileData().inputConfig;
        
        if (value != 0) {
            handler.requestFocus(InputHandler.DeviceType.CONTROLLER);
        }

        if (!handler.isDeviceActive(InputHandler.DeviceType.CONTROLLER)) return false;

        java.util.Map<String, Integer> axes = config.gamepadAxisMapping;
        
        int moveXIdx = axes.getOrDefault("moveX", 0);
        int moveYIdx = axes.getOrDefault("moveY", 1);
        int aimXIdx  = axes.getOrDefault("aimX", 2);
        int aimYIdx  = axes.getOrDefault("aimY", 3);

        if (axisIndex == moveXIdx) handler.moveDirection.x = value;
        if (axisIndex == moveYIdx) handler.moveDirection.y = -value;

        if (h2ButtonHeld) {
            if (axisIndex == aimXIdx) handler.aimDirectionAbility2.x = value;
            if (axisIndex == aimYIdx) handler.aimDirectionAbility2.y = -value;

            float magnitude = (float) Math.sqrt(
                handler.aimDirectionAbility2.x * handler.aimDirectionAbility2.x +
                handler.aimDirectionAbility2.y * handler.aimDirectionAbility2.y
            );
            handler.aimMagnitudeAbility2 = magnitude;
        } else {
            if (axisIndex == aimXIdx) handler.aimDirection.x = value;
            if (axisIndex == aimYIdx) handler.aimDirection.y = -value;
        }

        return false;
    }
}
