package com.tikisadventure.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class GrenadeComponent implements Component {

    private float fuseTime;
    private float timer = 0;
    private float arcHeight;
    private int bounceCount;
    private float friction;
    private float randomness;

    private int lastBounceIndex = -1;
    private float initialRadius = -1;

    public GrenadeComponent(float fuseTime, float arcHeight, int bounceCount,
                            float friction, float randomness) {
        this.fuseTime = fuseTime;
        this.arcHeight = arcHeight;
        this.bounceCount = bounceCount;
        this.friction = friction;
        this.randomness = randomness;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof Sensorable) || !(owner instanceof HasDirection) ||
            !(owner instanceof HasRadius) || !(owner instanceof Killable)) {
            return;
        }

        Sensorable sensorable = (Sensorable) owner;
        HasDirection dirInterface = (HasDirection) owner;
        HasRadius radiusInterface = (HasRadius) owner;
        Killable killable = (Killable) owner;

        if (timer == 0) {
            sensorable.setSensorMode(true);
            initialRadius = radiusInterface.getRadius();
        }

        timer += delta;
        float progress = Math.min(1.0f, timer / fuseTime);

        // Control de rebotes para aplicar fricción y dirección
        int currentBounceIndex = (int) (progress * bounceCount);

        if (currentBounceIndex > lastBounceIndex) {
            if (lastBounceIndex != -1) {
                // Aplicamos el cambio de dirección y frenado solo en el momento del impacto
                float angleChange = MathUtils.random(-randomness, randomness);
                dirInterface.getDirection().rotateDeg(angleChange);
                dirInterface.getDirection().scl(friction);
            }
            lastBounceIndex = currentBounceIndex;
        }

        // --- MEJORA MATEMÁTICA ---
        // 1. Dampening: Reduce la altura de cada rebote conforme pasa el tiempo
        float dampening = 1.0f - progress;

        // 2. Math.abs: Convierte la onda suave en "arcos" que chocan contra el suelo
        float bounceOffset = Math.abs(MathUtils.sin(progress * MathUtils.PI * bounceCount));

        // 3. Variación de tamaño: Simulamos altura (Z) escalando el radio
        float sizeVariation = bounceOffset * arcHeight * dampening;

        // Aseguramos que el radio resultante sea como mínimo el inicial (no encoge)
        float newRadius = initialRadius + sizeVariation;
        newRadius = Math.max(initialRadius, newRadius);

        radiusInterface.setRadius(newRadius);

        // Muerte al agotar el tiempo
        if (timer >= fuseTime) {
            killable.die();
        }
    }

    @Override
    public void onAttach(Object owner) {
        timer = 0;
        lastBounceIndex = -1;
        initialRadius = -1;
    }
}
