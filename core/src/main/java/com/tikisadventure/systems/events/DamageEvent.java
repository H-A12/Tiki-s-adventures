package com.tikisadventure.systems.events;

import com.tikisadventure.entities.base.Entity;

public class DamageEvent implements Event {
    public final Entity entity;
    public final float damage;
    public final boolean isCritical;

    public DamageEvent(Entity entity, float damage, boolean isCritical) {
        this.entity = entity;
        this.damage = damage;
        this.isCritical = isCritical;
    }
}