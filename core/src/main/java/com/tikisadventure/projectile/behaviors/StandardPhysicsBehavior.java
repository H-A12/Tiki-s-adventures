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

        // 1. INICIALIZAR ORIGEN
        if (startPos.isZero()) {
            startPos.set(p.getPosition());
        }

        // 2. MOVIMIENTO
        p.getPosition().mulAdd(p.getDirection(), p.getSpeed() * delta);

        // 3. SEGURO DE DISTANCIA
        if (p.getPosition().dst2(startPos) > maxRange * maxRange) {
            p.die();
            return;
        }

        // --- 4. DETECCIÓN DE COLISIONES INTELIGENTE ---

        // SI EL PROYECTIL ES UN SENSOR (Granada volando), NO CHOCA CON ENEMIGOS
        if (p.isSensorMode()) {
            return; // Saltamos la detección de daño para este frame
        }

        // Lógica de daño normal para balas terrestres
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;

            float hitRadius = p.getRadius() + e.getHitboxActionTrigger().radius;

            if (p.getPosition().dst2(e.getPosicion()) <= hitRadius * hitRadius) {
                // ¡IMPACTO!
                e.receiveDamage(p.getDamage());
                p.die(); // Aquí es donde moriría y activaría la explosión
                return;
            }
        }
    }
}
