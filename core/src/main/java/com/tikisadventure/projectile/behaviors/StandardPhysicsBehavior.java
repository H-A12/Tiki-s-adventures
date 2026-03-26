package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class StandardPhysicsBehavior implements ProjectileBehavior {

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        // 1. Si el proyectil ya está muerto (por otro behavior), no hacemos nada
        if (!p.isAlive()) return;

        // 2. Movimiento rectilíneo base
        p.getPosition().mulAdd(p.getDirection(), p.getSpeed() * delta);

        // 3. Detección de colisiones
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;

            float hitRadius = p.getRadius() + e.getHitboxActionTrigger().radius;

            if (p.getPosition().dst2(e.getPosicion()) <= hitRadius * hitRadius) {
                e.receiveDamage(p.getDamage());
                p.die();
                return; // Cambiamos 'break' por 'return' para salir del update por completo
            }
        }
    }
}
