package com.tikisadventure.systems.events;

import com.tikisadventure.entities.base.Entity;

public class DamageEvent implements Event {
    public final Entity entity;

    public DamageEvent(Entity entity) {
        this.entity = entity;
    }
}