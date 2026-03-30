package com.tikisadventure.entities.base.components;

import com.badlogic.gdx.math.Vector2;

public interface HasDirection {
    Vector2 getDirection();
    void setDirection(Vector2 dir);
}
