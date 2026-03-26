package com.tikisadventure.projectile.behaviors;

// IMPORTANTE: Estos 3 son de LibGDX
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

// IMPORTANTE: Estos son de tu propio proyecto
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class WaveMovement implements ProjectileBehavior {
    private float amplitude = 0.4f;
    private float frequency = 12f;

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        // 1. Movimiento hacia adelante (Física básica)
        p.position.mulAdd(p.direction, p.speed * delta);

        // 2. Cálculo del Zig-Zag (Ondulatorio)
        // Creamos un vector perpendicular a la dirección del proyectil
        Vector2 perp = new Vector2(-p.direction.y, p.direction.x);

        // Calculamos el desplazamiento usando la función Seno
        float offset = (float) Math.sin(p.stateTime * frequency) * amplitude;

        // Aplicamos el desplazamiento lateral a la posición
        p.position.mulAdd(perp, offset);

        // 3. Colisión de "Lanzallamas" (Atraviesa enemigos)
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;

            float hitRadius = p.radius + e.getHitboxActionTrigger().radius;
            if (p.position.dst2(e.getPosicion()) <= hitRadius * hitRadius) {
                // Aplicamos daño constante por frame (quemadura)
                e.receiveDamage(p.damage * delta * 60);
                // NOTA: No ponemos p.alive = false porque el fuego debe atravesar
            }
        }
    }
}
