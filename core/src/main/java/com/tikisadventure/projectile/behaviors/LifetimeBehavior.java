package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
// IMPORTANTE: Aquí le decimos dónde está la interfaz
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class LifetimeBehavior implements ProjectileBehavior {
    private float maxLife;

    public LifetimeBehavior(float seconds) {
        this.maxLife = seconds;
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        // p.stateTime se incrementa automáticamente en el update de Projectile
        if (p.stateTime >= maxLife) {
            p.alive = false;
        }
    }
}
