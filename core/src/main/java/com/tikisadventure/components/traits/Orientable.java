package com.tikisadventure.components.traits;

import com.badlogic.gdx.math.Vector2;

//Dar dirección a una entidad
public interface Orientable {
    Vector2 getDirection();
    void setDirection(Vector2 dir);
}
