package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
// ESTOS DOS IMPORTS SON LA CLAVE:
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class StandardPhysics implements ProjectileBehavior {

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        // 1. Movimiento rectilíneo
        p.position.mulAdd(p.direction, p.speed * delta);

        // 2. Detección de colisiones (Tu lógica original de Bullet)
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;

            float hitRadius = p.radius + e.getHitboxActionTrigger().radius;
            if (p.position.dst2(e.getPosicion()) <= hitRadius * hitRadius) {
                e.receiveDamage(p.damage);
                p.alive = false; // La bala estándar muere al chocar
                break;
            }
        }
    }
}
