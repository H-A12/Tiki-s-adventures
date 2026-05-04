package com.tikisadventure.systems.events;

import com.tikisadventure.entities.base.Entity;

public class HealEvent implements Event {
    public enum HealType { REGEN, LEECH, PICKUP }

    public final Entity entity;
    public final float amount;
    public final HealType type;

    public HealEvent(Entity entity, float amount, HealType type) {
        this.entity = entity;
        this.amount = amount;
        this.type = type;
    }
}
