package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;

public interface Knockbackable {
    Vector2 getKnockbackVelocity();
    void setKnockbackVelocity(Vector2 velocity);
}
