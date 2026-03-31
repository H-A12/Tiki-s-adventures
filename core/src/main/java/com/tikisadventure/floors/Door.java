package com.tikisadventure.floors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;

public class Door {

    public enum State {
        HIDDEN,
        CLOSED,
        OPENING,
        OPEN
    }

    private Vector2 position;
    private State state;
    private TextureRegion closedTexture;
    private TextureRegion openTexture;
    private TextureRegion currentFrame;
    private float activationRadius;
    private float openingTimer;
    private float openingDuration = 0.5f;

    public Door(float x, float y) {
        this.position = new Vector2(x, y);
        this.state = State.HIDDEN;
        this.activationRadius = 2.0f;
        this.openingTimer = 0f;
        
        closedTexture = Assets.getRegion("shared", "door_closed");
        openTexture = Assets.getRegion("shared", "door_open");
        currentFrame = closedTexture;
    }

    public void update(float delta) {
        if (state == State.OPENING) {
            openingTimer += delta;
            if (openingTimer >= openingDuration) {
                state = State.OPEN;
                currentFrame = openTexture;
            }
        }
    }

    public void render(Batch batch) {
        if (state == State.HIDDEN) return;
        
        if (currentFrame != null) {
            float width = 1.5f;
            float height = 1.5f;
            float x = position.x - width / 2;
            float y = position.y - height / 2;
            batch.draw(currentFrame, x, y, width, height);
        }
    }

    public void show() {
        state = State.CLOSED;
        openingTimer = 0f;
        currentFrame = closedTexture;
        Gdx.app.log("Door", "Door shown at position: " + position.x + ", " + position.y);
    }

    public void hide() {
        state = State.HIDDEN;
    }

    public void open() {
        if (state == State.CLOSED) {
            state = State.OPENING;
            openingTimer = 0f;
        }
    }

    public boolean isPlayerNear(Vector2 playerPos) {
        return position.dst(playerPos) <= activationRadius;
    }

    public boolean canInteract() {
        return state == State.CLOSED && isOpen() == false;
    }

    public boolean isOpen() {
        return state == State.OPEN || state == State.OPENING;
    }

    public State getState() {
        return state;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        // Textures are disposed by Assets.dispose()
    }
}
