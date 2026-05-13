package com.tikisadventure.systems.events;

import com.tikisadventure.entities.base.Entity;

public class EvadeEvent implements Event {
    public final Entity entity;

    public EvadeEvent(Entity entity) {
        this.entity = entity;
    }
}
