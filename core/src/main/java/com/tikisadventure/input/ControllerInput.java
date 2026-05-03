package com.tikisadventure.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.ControllerConnectedEvent;

public class ControllerInput implements ControllerListener {
    private final InputHandler handler;
    private boolean h2ButtonHeld = false;

    public ControllerInput(InputHandler handler) {
        this.handler = handler;
        Controllers.addListener(this);
    }
    @Override
    public void connected(Controller controller) {
        EventBus.publish(new ControllerConnectedEvent("Controller Detected"));
    }


    @Override
    public void disconnected(Controller controller) {}

    @Override
    public boolean buttonDown(Controller controller, int buttonIndex) {
        InputHandler.InputState state = handler.controllerState;
        if (buttonIndex == 0) state.isInteracting = true;
        if (buttonIndex == 1) {
            state.useDash = true;
        }
        if (buttonIndex == 3) {
            h2ButtonHeld = true;
            state.isAimingAbility2 = true;
        }
        if (buttonIndex == 4) state.useAbility1 = true;
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        InputHandler.InputState state = handler.controllerState;
        if (buttonIndex == 0) state.isInteracting = false;
        if (buttonIndex == 1) state.useDash = false;
        if (buttonIndex == 3) {
            if (h2ButtonHeld) {
                state.useAbility2 = true;
            }
            h2ButtonHeld = false;
            state.isAimingAbility2 = false;
            state.aimDirectionAbility2.setZero();
            state.aimMagnitudeAbility2 = 0;
        }
        if (buttonIndex == 4) state.useAbility1 = false;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        InputHandler.InputState state = handler.controllerState;
        InputConfig config = SaveManager.getProfileData().inputConfig;

        if (Math.abs(value) < 0.2f) value = 0;

        // Map sticks based on configuration
        int moveBaseAxis = config.movementJoystick * 2;
        int aimBaseAxis = config.aimingJoystick * 2;

        if (axisIndex == moveBaseAxis) state.moveDirection.x = value;
        if (axisIndex == moveBaseAxis + 1) state.moveDirection.y = -value;

        if (h2ButtonHeld) {
            if (axisIndex == aimBaseAxis) state.aimDirectionAbility2.x = value;
            if (axisIndex == aimBaseAxis + 1) state.aimDirectionAbility2.y = -value;

            float magnitude = (float) Math.sqrt(
                state.aimDirectionAbility2.x * state.aimDirectionAbility2.x +
                state.aimDirectionAbility2.y * state.aimDirectionAbility2.y
            );
            state.aimMagnitudeAbility2 = magnitude;
        } else {
            if (axisIndex == aimBaseAxis) state.aimDirection.x = value;
            if (axisIndex == aimBaseAxis + 1) state.aimDirection.y = -value;
        }

        return false;
    }
}