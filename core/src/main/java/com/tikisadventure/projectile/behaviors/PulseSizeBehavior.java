package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class PulseSizeBehavior implements ProjectileBehavior {

    private float baseRadius = 0;
    private float targetRadius;
    private float growthDuration;   // Tiempo total para alcanzar el tamaño objetivo
    private float pulseFrequency;

    private boolean isPermanentChange;

    /**
     * Constructor para un cambio de tamaño progresivo.
     * @param targetRadius El radio final (ej: si bulletSize es 0.15 y quieres el doble, pon 0.30).
     * @param growthDuration Segundos que tarda en llegar al tamaño final.
     */
    public PulseSizeBehavior(float targetRadius, float growthDuration) {
        this.targetRadius = targetRadius;
        this.growthDuration = growthDuration;
        this.isPermanentChange = true;
        this.pulseFrequency = 0f;
    }

    /**
     * Constructor para un palpitado constante.
     * @param maxRadiusScale Multiplicador (ej: 2.0f para que crezca al doble).
     * @param frequency Cuántas veces por segundo completa un ciclo de palpitado.
     */
    public PulseSizeBehavior(float maxRadiusScale, float frequency, boolean pulse) {
        this.targetRadius = maxRadiusScale;
        this.pulseFrequency = frequency;
        this.isPermanentChange = false;
        // growthDuration no se usa en modo pulso, pero lo inicializamos
        this.growthDuration = 1f;
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        if (!p.isAlive()) return;

        if (baseRadius == 0) {
            baseRadius = p.getRadius();
        }

        float newRadius = baseRadius;

        if (isPermanentChange) {
            // Calculamos el progreso (de 0.0 a 1.0) basado en el tiempo de vida y la duración
            // Usamos Math.min para que no siga creciendo infinitamente después de la duración
            float progress = Math.min(p.getStateTime() / growthDuration, 1f);

            // Interpolación lineal entre el radio base y el objetivo
            newRadius = baseRadius + (targetRadius - baseRadius) * progress;

        } else {
            // Modo palpitado (igual que antes)
            float scaleFactor = (MathUtils.sin(p.getStateTime() * pulseFrequency * MathUtils.PI2) + 1f) / 2f;
            newRadius = baseRadius + (targetRadius * baseRadius - baseRadius) * scaleFactor;
        }

        p.setRadius(newRadius);
    }
}
