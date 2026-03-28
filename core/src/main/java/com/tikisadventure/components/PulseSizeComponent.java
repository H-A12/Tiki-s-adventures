package com.tikisadventure.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class PulseSizeComponent implements Component {

    private float baseRadius = 0;
    private float targetRadius;
    private float growthDuration;
    private float pulseFrequency;
    private boolean isPermanentChange;

    public PulseSizeComponent(float targetRadius, float growthDuration) {
        this.targetRadius = targetRadius;
        this.growthDuration = growthDuration;
        this.isPermanentChange = true;
        this.pulseFrequency = 0f;
    }

    public PulseSizeComponent(float maxRadiusScale, float frequency, boolean pulse) {
        this.targetRadius = maxRadiusScale;
        this.pulseFrequency = frequency;
        this.isPermanentChange = false;
        this.growthDuration = 1f;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof HasRadius) || !(owner instanceof Timed)) {
            return;
        }

        HasRadius radiusInterface = (HasRadius) owner;
        Timed timed = (Timed) owner;

        if (baseRadius == 0) {
            baseRadius = radiusInterface.getRadius();
        }

        float newRadius = baseRadius;

        if (isPermanentChange) {
            float progress = Math.min(timed.getStateTime() / growthDuration, 1f);
            newRadius = baseRadius + (targetRadius - baseRadius) * progress;
        } else {
            float scaleFactor = (MathUtils.sin(timed.getStateTime() * pulseFrequency * MathUtils.PI2) + 1f) / 2f;
            newRadius = baseRadius + (targetRadius * baseRadius - baseRadius) * scaleFactor;
        }

        radiusInterface.setRadius(newRadius);
    }

    @Override
    public void onAttach(Object owner) {
        baseRadius = 0;
    }
}
