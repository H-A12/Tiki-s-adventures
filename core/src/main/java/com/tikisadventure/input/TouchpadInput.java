package com.tikisadventure.input;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

//Entrada tactil con touchpads y botones
public class TouchpadInput {
    //Componentes de la interfaz tactil
    private Touchpad moveTouchpad;
    private Touchpad aimTouchpad;
    private Button interactButton;
    private Button dashButton;
    private Button ability2Button;
    private boolean h2ButtonHeld = false;
    private InputHandler handler;

    //Vincular touchpads y botones
    public TouchpadInput(Touchpad moveTouchpad, Touchpad aimTouchpad, Button interactButton, Button dashButton, Button ability2Button) {
        this.moveTouchpad = moveTouchpad;
        this.aimTouchpad = aimTouchpad;
        this.interactButton = interactButton;
        this.dashButton = dashButton;
        this.ability2Button = ability2Button;

        setupListeners();
    }

    //Asignar eventos a los botones
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
                        handler.useAbility2 = true;
                    }
                    h2ButtonHeld = false;
                    if (handler != null) {
                        handler.isAimingAbility2 = false;
                        handler.aimDirectionAbility2.setZero();
                    }
                }
            });
        }

        if (interactButton != null) {
            interactButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (handler != null) handler.isInteracting = true;
                    return true;
                }
            });
        }

        if (dashButton != null) {
            dashButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (handler != null) handler.useAbility1 = true;
                    return true;
                }
            });
        }
    }

    //Leer touchpads y actualizar handler
    public void update(InputHandler handler) {
        this.handler = handler;

        if (moveTouchpad != null && moveTouchpad.isTouched()) {
            handler.moveDirection.x = moveTouchpad.getKnobPercentX();
            handler.moveDirection.y = moveTouchpad.getKnobPercentY();
        }

        if (aimTouchpad != null && aimTouchpad.isTouched()) {
            if (h2ButtonHeld) {
                handler.isAimingAbility2 = true;
                handler.aimDirectionAbility2.x = aimTouchpad.getKnobPercentX();
                handler.aimDirectionAbility2.y = aimTouchpad.getKnobPercentY();

                float magnitude = (float) Math.sqrt(
                    handler.aimDirectionAbility2.x * handler.aimDirectionAbility2.x +
                    handler.aimDirectionAbility2.y * handler.aimDirectionAbility2.y
                );
                handler.aimMagnitudeAbility2 = magnitude;
            } else {
                handler.aimDirection.x = aimTouchpad.getKnobPercentX();
                handler.aimDirection.y = aimTouchpad.getKnobPercentY();
            }
        } else if (h2ButtonHeld) {
            handler.isAimingAbility2 = true;
            handler.aimDirectionAbility2.setZero();
            handler.aimMagnitudeAbility2 = 0;
        }
    }

    public Touchpad getMoveTouchpad() { return moveTouchpad; }
    public Touchpad getAimTouchpad() { return aimTouchpad; }
    public Button getInteractButton() { return interactButton; }
    public Button getDashButton() { return dashButton; }
    public Button getAbility2Button() { return ability2Button; }
}