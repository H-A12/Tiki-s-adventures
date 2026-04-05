package com.tikisadventure.components.traits;

import com.badlogic.gdx.math.Vector2;

public interface Orientable {
    Vector2 getDirection();
    void setDirection(Vector2 dir);
}
