package com.tikisadventure.input;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class TouchpadInput {
    private Touchpad moveTouchpad;
    private Touchpad aimTouchpad;
    private Button interactButton;
    private Button dashButton;
    private Button ability2Button;
    private boolean h2ButtonHeld = false;
    private InputHandler handler;

    public TouchpadInput(Touchpad moveTouchpad, Touchpad aimTouchpad, Button interactButton, Button dashButton, Button ability2Button) {
        this.moveTouchpad = moveTouchpad;
        this.aimTouchpad = aimTouchpad;
        this.interactButton = interactButton;
        this.dashButton = dashButton;
        this.ability2Button = ability2Button;

        setupListeners();
    }

    private void setupListeners() {
        if (ability2Button != null) {
            ability2Button.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    h2ButtonHeld = true;
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (h2ButtonHeld && handler != null) {
                        handler.touchpadState.useAbility2 = true;
                    }
                    h2ButtonHeld = false;
                    if (handler != null) {
                        handler.touchpadState.isAimingAbility2 = false;
                        handler.touchpadState.aimDirectionAbility2.setZero();
                    }
                }
            });
        }

        if (interactButton != null) {
            interactButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (handler != null) handler.touchpadState.isInteracting = true;
                    return true;
                }
            });
        }

        if (dashButton != null) {
            dashButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (handler != null) handler.touchpadState.useAbility1 = true;
                    return true;
                }
            });
        }
    }

    public void update(InputHandler handler) {
        this.handler = handler;
        InputHandler.InputState state = handler.touchpadState;

        if (moveTouchpad != null && moveTouchpad.isTouched()) {
            state.moveDirection.x = moveTouchpad.getKnobPercentX();
            state.moveDirection.y = moveTouchpad.getKnobPercentY();
        }

        if (aimTouchpad != null && aimTouchpad.isTouched()) {
            if (h2ButtonHeld) {
                state.isAimingAbility2 = true;
                state.aimDirectionAbility2.x = aimTouchpad.getKnobPercentX();
                state.aimDirectionAbility2.y = aimTouchpad.getKnobPercentY();

                float magnitude = (float) Math.sqrt(
                    state.aimDirectionAbility2.x * state.aimDirectionAbility2.x +
                    state.aimDirectionAbility2.y * state.aimDirectionAbility2.y
                );
                state.aimMagnitudeAbility2 = magnitude;
            } else {
                state.aimDirection.x = aimTouchpad.getKnobPercentX();
                state.aimDirection.y = aimTouchpad.getKnobPercentY();
            }
        } else if (h2ButtonHeld) {
            state.isAimingAbility2 = true;
            state.aimDirectionAbility2.setZero();
            state.aimMagnitudeAbility2 = 0;
        }
    }

    public Touchpad getMoveTouchpad() { return moveTouchpad; }
    public Touchpad getAimTouchpad() { return aimTouchpad; }
    public Button getInteractButton() { return interactButton; }
    public Button getDashButton() { return dashButton; }
    public Button getAbility2Button() { return ability2Button; }
}