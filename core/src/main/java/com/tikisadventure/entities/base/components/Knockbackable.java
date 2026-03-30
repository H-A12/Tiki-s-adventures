package com.tikisadventure.entities.base.components;

import com.badlogic.gdx.math.Vector2;

public interface Knockbackable {
    /**
     * Aplica un impulso de retroceso en una dirección específica.
     */
    void applyKnockback(Vector2 direction, float force);

    Vector2 getKnockbackVelocity();

    void setKnockbackVelocity(Vector2 velocity);
}
