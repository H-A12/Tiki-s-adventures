package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class PenetrationPhysicsBehavior implements ProjectileBehavior {

    private Vector2 startPos = new Vector2();
    private float maxRange = 25f;

    private int maxPenetrations; // Cuántos enemigos puede atravesar
    private Array<Entity> hitEntities = new Array<>(); // Para no dañar al mismo 2 veces

    public PenetrationPhysicsBehavior(int maxPenetrations) {
        this.maxPenetrations = maxPenetrations;
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        if (!p.isAlive()) return;

        if (startPos.isZero()) startPos.set(p.getPosition());

        // 1. MOVIMIENTO
        p.getPosition().mulAdd(p.getDirection(), p.getSpeed() * delta);

        // 2. SEGURO DE DISTANCIA
        if (p.getPosition().dst2(startPos) > maxRange * maxRange) {
            p.die();
            return;
        }

        // 3. COLISIÓN CON PENETRACIÓN
        for (Entity e : enemies) {
            // CONDICIÓN CRÍTICA: No golpear enemigos muertos NI a los que ya golpeamos
            if (!e.isAlive() || hitEntities.contains(e, true)) continue;

            float hitRadius = p.getRadius() + e.getHitboxActionTrigger().radius;

            if (p.getPosition().dst2(e.getPosicion()) <= hitRadius * hitRadius) {

                // Aplicamos daño
                e.receiveDamage(p.getDamage());

                // Registramos que ya lo golpeamos para no hacerle daño infinito cada frame
                hitEntities.add(e);

                // Restamos penetración
                maxPenetrations--;

                // Si ya no puede atravesar más, la bala muere
                if (maxPenetrations < 0) {
                    p.die();
                    return;
                }
            }
        }
    }
}
