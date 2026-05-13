package com.tikisadventure.systems.events;

import com.tikisadventure.entities.base.Entity;

public class EntityDiedEvent implements Event {
    public final Entity entity;

    public EntityDiedEvent(Entity entity) {
        this.entity = entity;
    }
}
