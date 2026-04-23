package com.tikisadventure.input;

import com.badlogic.gdx.Input;
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
    private boolean wasRightClickHeld = false;
    private OrthographicCamera camera;

    public KeyboardInput(InputHandler handler) {
        this.handler = handler;
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void update(InputHandler handler) {
        InputConfig config = SaveManager.getProfileData().inputConfig;
        
        if (Gdx.input.isKeyPressed(config.keyboardMapping.get("up"))) handler.moveDirection.y += 1;
        if (Gdx.input.isKeyPressed(config.keyboardMapping.get("down"))) handler.moveDirection.y -= 1;
        if (Gdx.input.isKeyPressed(config.keyboardMapping.get("left"))) handler.moveDirection.x -= 1;
        if (Gdx.input.isKeyPressed(config.keyboardMapping.get("right"))) handler.moveDirection.x += 1;

        if (!handler.moveDirection.isZero()) handler.moveDirection.nor();

        handler.isInteracting = Gdx.input.isKeyJustPressed(config.keyboardMapping.get("interact"));
        handler.useAbility1 = Gdx.input.isKeyJustPressed(config.keyboardMapping.get("ability1"));

        // Right click is a special case for mouse buttons. The config might need to handle buttons differently.
        // For now, let's keep it simple and assume the config mapping handles it.
        // If config.keyboardMapping.get("ability2") returns a keycode (not button code), this will break.
        // This is a limitation I'll have to address later.
        
        boolean isRightClickHeld = Gdx.input.isButtonPressed(Input.Buttons.RIGHT); // Keeping this hardcoded for now to avoid crashing

        if (isRightClickHeld && camera != null) {
            handler.isAimingAbility2 = true;
            tmpVector3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(tmpVector3);
            handler.aimTargetAbility2.set(tmpVector3.x, tmpVector3.y);
        } else {
            handler.isAimingAbility2 = false;
            handler.aimTargetAbility2.setZero();
        }

        if (wasRightClickHeld && !isRightClickHeld) {
            handler.useAbility2 = true;
        }

        wasRightClickHeld = isRightClickHeld;
    }
}
