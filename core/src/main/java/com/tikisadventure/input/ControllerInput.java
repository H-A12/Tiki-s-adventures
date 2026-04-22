package com.tikisadventure.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

public class ControllerInput implements ControllerListener {
    private final InputHandler handler;

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
        // Mapeo simple de botones
        if (buttonIndex == 0) handler.isAttacking = true; // Button A (ejemplo)
        if (buttonIndex == 1) handler.useAbility1 = true;
        if (buttonIndex == 2) handler.useAbility2 = true;
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        if (buttonIndex == 0) handler.isAttacking = false;
        if (buttonIndex == 1) handler.useAbility1 = false;
        if (buttonIndex == 2) handler.useAbility2 = false;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        // Mapeo básico para joystick izquierdo
        if (Math.abs(value) < 0.2f) value = 0; // Deadzone
        
        if (axisIndex == 0) handler.moveDirection.x = value;
        if (axisIndex == 1) handler.moveDirection.y = -value; // Y axis is inverted
        
        return false;
    }
}
