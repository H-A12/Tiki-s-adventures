package com.tikisadventure.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;

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
        if (Gdx.input.isKeyPressed(Input.Keys.W)) handler.moveDirection.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) handler.moveDirection.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) handler.moveDirection.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) handler.moveDirection.x += 1;

        if (!handler.moveDirection.isZero()) handler.moveDirection.nor();

        handler.isInteracting = Gdx.input.isKeyJustPressed(Input.Keys.E);
        handler.useAbility1 = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        boolean isRightClickHeld = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);

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