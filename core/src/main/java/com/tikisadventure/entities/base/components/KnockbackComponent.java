package com.tikisadventure.entities.base.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class KnockbackComponent implements Component {

    private static final float FRICTION = 8f;
    private final Vector2 knockbackVelocity = new Vector2();
    private boolean hasKnockback = false;

    public void applyKnockback(Vector2 direction, float force) {
        knockbackVelocity.set(direction).nor().scl(force);
        hasKnockback = true;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!hasKnockback) return;
        if (!(owner instanceof HasPosition)) return;

        HasPosition posInterface = (HasPosition) owner;

        posInterface.getPosition().mulAdd(knockbackVelocity, delta);

        knockbackVelocity.scl(1f - FRICTION * delta);

        if (knockbackVelocity.len() < 0.1f) {
            knockbackVelocity.setZero();
            hasKnockback = false;
        }
    }

    @Override
    public void onAttach(Object owner) {
        knockbackVelocity.setZero();
        hasKnockback = false;
    }

    public boolean hasKnockback() {
        return hasKnockback;
    }
}
