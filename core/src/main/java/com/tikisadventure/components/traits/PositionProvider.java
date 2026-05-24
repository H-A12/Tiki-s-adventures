package com.tikisadventure.components.traits;

import com.badlogic.gdx.math.Vector2;

//Dar posición a una entidad
public interface PositionProvider {
    Vector2 getPosition();
    void setPosition(Vector2 pos);
}
