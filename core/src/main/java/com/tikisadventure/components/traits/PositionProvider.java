package com.tikisadventure.components.traits;

import com.badlogic.gdx.math.Vector2;

public interface PositionProvider {
    Vector2 getPosition();
    void setPosition(Vector2 pos);
}
