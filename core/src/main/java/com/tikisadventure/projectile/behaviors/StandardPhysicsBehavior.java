package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class StandardPhysicsBehavior implements ProjectileBehavior {

    private Vector2 startPos = new Vector2();
    private float maxRange = 25f;

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        if (!p.isAlive()) return;

        // 1. INICIALIZAR ORIGEN (Solo el primer frame)
        if (startPos.isZero()) {
            startPos.set(p.getPosition());
        }

        // 2. MOVIMIENTO (Suma posición cada frame)
        p.getPosition().mulAdd(p.getDirection(), p.getSpeed() * delta);

        // 3. SEGURO DE DISTANCIA (Limpia memoria si sale del mapa)
        if (p.getPosition().dst2(startPos) > maxRange * maxRange) {
            p.die();
            return;
        }

        // 4. DETECCIÓN DE COLISIONES Y DAÑO
        // Recorremos los enemigos para ver si la bala toca a alguien
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;

            // Calculamos la distancia necesaria para el impacto (Suma de radios)
            float hitRadius = p.getRadius() + e.getHitboxActionTrigger().radius;

            // Usamos dst2 (distancia al cuadrado) porque es mucho más rápido para la CPU
            if (p.getPosition().dst2(e.getPosicion()) <= hitRadius * hitRadius) {

                // ¡IMPACTO!
                e.receiveDamage(p.getDamage()); // Aplicamos el daño del proyectil
                p.die();                        // La bala desaparece
                return;                         // Salimos para no chocar con más gente este frame
            }
        }
    }
}
