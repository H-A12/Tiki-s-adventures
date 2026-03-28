package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class GrenadeBehavior implements ProjectileBehavior {

    private float fuseTime;
    private float timer = 0;

    // --- PARÁMETROS CONFIGURABLES ---
    private float arcHeight;      // Cuánto "salta" visualmente
    private int bounceCount;      // Cuántos botes da antes de detenerse
    private float friction;       // Multiplicador de velocidad tras rebotar (ej: 0.7f)
    private float randomness;     // Desviación en grados tras cada bote (ej: 25f)

    private int lastBounceIndex = -1;

    public GrenadeBehavior(float fuseTime, float arcHeight, int bounceCount, float friction, float randomness) {
        this.fuseTime = fuseTime;
        this.arcHeight = arcHeight;
        this.bounceCount = bounceCount;
        this.friction = friction;
        this.randomness = randomness;
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        // Al inicio, nos aseguramos de que atraviese enemigos (modo sensor)
        if (timer == 0) {
            p.setSensorMode(true);
        }

        timer += delta;
        float progress = Math.min(1.0f, timer / fuseTime);

        // --- 1. LÓGICA DE REBOTES (FÍSICA) ---
        // Dividimos el tiempo total en segmentos según el número de botes
        int currentBounceIndex = (int) (progress * bounceCount);

        // Si entramos en un nuevo segmento, significa que la granada "ha tocado el suelo"
        if (currentBounceIndex > lastBounceIndex) {
            if (lastBounceIndex != -1) {
                applyBouncePhysics(p);
            }
            lastBounceIndex = currentBounceIndex;
        }

        // --- 2. EFECTO VISUAL DE SALTO (ARCO) ---
        // Usamos la función Seno para crear las parábolas de los saltos
        float bounceCurve = Math.abs(MathUtils.sin(progress * MathUtils.PI * bounceCount));

        // El "dampening" hace que cada salto sea más bajo que el anterior
        float dampening = 1.0f - progress;

        // Modificamos el radio visual (escalado) del proyectil
        p.setRadius(p.getBaseRadius() + (bounceCurve * arcHeight * dampening));

        // --- 3. DETONACIÓN ---
        if (timer >= fuseTime) {
            p.setAlive(false); // Esto dispara el ExplosiveBehavior y ShrapnelBehavior
        }
    }

    /**
     * Aplica fricción y desvía la granada cuando impacta contra el "suelo"
     */
    private void applyBouncePhysics(Projectile p) {
        // Desviación aleatoria para que no sea una línea recta perfecta
        float angleChange = MathUtils.random(-randomness, randomness);
        p.getDirection().rotateDeg(angleChange);

        // Aplicamos la fricción (frenado)
        p.getDirection().scl(friction);
    }
}
