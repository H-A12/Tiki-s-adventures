package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class BoomerangBehavior implements ProjectileBehavior {

    private float returnDelay;     // Tiempo que tarda en empezar a volver
    private float rotationSpeed;   // Qué tan rápido gira (curva del boomerang)
    private boolean isReturning = false;
    private Vector2 tempDir = new Vector2();

    /**
     * @param returnDelay Segundos antes de que la bala intente volver.
     * @param rotationSpeed Velocidad de giro (ej: 5f para un giro suave, 15f para uno cerrado).
     */
    public BoomerangBehavior(float returnDelay, float rotationSpeed) {
        this.returnDelay = returnDelay;
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        if (!p.isAlive()) return;

        // 1. Verificamos si es hora de activar el retorno
        if (p.getStateTime() >= returnDelay) {
            isReturning = true;
        }

        if (isReturning && p.getOwner() != null) {
            // 2. Calculamos la dirección hacia el dueño (el Jugador)
            tempDir.set(p.getOwner().getPosicion())
                .sub(p.getPosition())
                .nor();

            // 3. Interpolamos la dirección actual hacia la dirección del dueño
            // Esto crea una curva suave en lugar de un giro brusco de 180 grados
            p.getDirection().lerp(tempDir, rotationSpeed * delta).nor();

            // 4. Autodestrucción al llegar al dueño (opcional)
            if (p.getPosition().dst2(p.getOwner().getPosicion()) < 0.5f) {
                p.die();
            }
        }
    }
}
