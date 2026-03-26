package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

/**
 * Este comportamiento limita la duración de un proyectil en el tiempo.
 * Útil para que las balas no viajen infinitamente por el mapa.
 */
public class LifetimeBehavior implements ProjectileBehavior {
    private float maxLife;

    public LifetimeBehavior(float seconds) {
        this.maxLife = seconds;
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        // Accedemos al tiempo de vida acumulado mediante el Getter oficial
        if (p.getStateTime() >= maxLife) {
            // Matamos el proyectil limpiamente
            p.die();
        }
    }
}
