package com.tikisadventure.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.tikisadventure.core.SaveManager;

public class KeyboardInput extends InputAdapter {
    private final InputHandler handler;
    private final Vector2 tmpVector = new Vector2();
    private final Vector3 tmpVector3 = new Vector3();
    private boolean wasAbility2Held = false;
    private OrthographicCamera camera;

    public KeyboardInput(InputHandler handler) {
        this.handler = handler;
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    private boolean isHeld(int code) {
        // Los botones de ratón de libGDX suelen estar en el rango 0-4
        if (code >= 0 && code <= 4) {
            return Gdx.input.isButtonPressed(code);
        }
        // Asumimos que es una tecla
        return Gdx.input.isKeyPressed(code);
    }

    private boolean isJustPressed(int code) {
        return Gdx.input.isKeyJustPressed(code);
    }

    public void update(InputHandler handler) {
        InputConfig config = SaveManager.getProfileData().inputConfig;
        InputHandler.InputState state = handler.keyboardState;
        
        // Mover
        state.moveDirection.setZero();
        if (isHeld(config.keyboardMapping.get("up"))) state.moveDirection.y += 1;
        if (isHeld(config.keyboardMapping.get("down"))) state.moveDirection.y -= 1;
        if (isHeld(config.keyboardMapping.get("left"))) state.moveDirection.x -= 1;
        if (isHeld(config.keyboardMapping.get("right"))) state.moveDirection.x += 1;

        if (!state.moveDirection.isZero()) state.moveDirection.nor();

        // Acciones
        state.isInteracting = isJustPressed(config.keyboardMapping.get("interact"));
        state.useAbility1 = isJustPressed(config.keyboardMapping.get("ability1"));

        // Ability 2 y Apuntado Manual
        boolean isAbility2Held = isHeld(config.keyboardMapping.get("ability2"));

        if (isAbility2Held && camera != null) {
            state.isAimingAbility2 = true;
            tmpVector3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(tmpVector3);
            state.aimTargetAbility2.set(tmpVector3.x, tmpVector3.y);
        } else {
            state.isAimingAbility2 = false;
            state.aimTargetAbility2.setZero();
        }

        if (wasAbility2Held && !isAbility2Held) {
            state.useAbility2 = true;
        }

        wasAbility2Held = isAbility2Held;
    }
}
