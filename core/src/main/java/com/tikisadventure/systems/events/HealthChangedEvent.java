package com.tikisadventure.systems.events;

import com.tikisadventure.entities.base.Entity;

public class HealthChangedEvent implements Event {
    public final Entity entity;
    public final float oldHealth;
    public final float newHealth;

    public HealthChangedEvent(Entity entity, float oldHealth, float newHealth) {
        this.entity = entity;
        this.oldHealth = oldHealth;
        this.newHealth = newHealth;
    }
}
