package com.tikisadventure.input;

import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;

public class TouchpadInput {
    private final Touchpad touchpad;

    public TouchpadInput(Touchpad touchpad) {
        this.touchpad = touchpad;
    }

    public void update(InputHandler handler) {
        if (touchpad.isTouched()) {
            handler.moveDirection.x = touchpad.getKnobPercentX();
            handler.moveDirection.y = touchpad.getKnobPercentY();
        }
    }
}
