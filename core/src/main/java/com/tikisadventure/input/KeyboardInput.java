package com.tikisadventure.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

public class KeyboardInput extends InputAdapter {
    private final InputHandler handler;

    public KeyboardInput(InputHandler handler) {
        this.handler = handler;
    }

    public void update(InputHandler handler) {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) handler.moveDirection.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) handler.moveDirection.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) handler.moveDirection.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) handler.moveDirection.x += 1;
        
        if (!handler.moveDirection.isZero()) handler.moveDirection.nor();
        
        handler.isInteracting = Gdx.input.isKeyJustPressed(Input.Keys.E);
    }
}
