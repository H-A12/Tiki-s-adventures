package com.tikisadventure.components.traits;

import com.badlogic.gdx.math.Vector2;

//Dar la capacidad de recibir empujón a una entidad
public interface Knockbackable {
    Vector2 getKnockbackVelocity();
    void setKnockbackVelocity(Vector2 velocity);
}
