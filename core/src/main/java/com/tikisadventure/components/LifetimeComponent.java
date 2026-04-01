package com.tikisadventure.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class LifetimeComponent implements Component {
    private float lifetime;
    private float elapsed;

    public LifetimeComponent(float lifetime) {
        this.lifetime = lifetime;
        this.elapsed = 0;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (owner instanceof Projectile) {
            elapsed += delta;
            if (elapsed >= lifetime) {
                ((Projectile) owner).die();
            }
        }
    }
}
