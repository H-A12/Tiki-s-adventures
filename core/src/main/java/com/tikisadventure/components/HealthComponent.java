package com.tikisadventure.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class HealthComponent implements Component {
    public float currentHealth;
    public float maxHealth;

    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        // This component is data-only for now, but adheres to the current interface
    }
}
