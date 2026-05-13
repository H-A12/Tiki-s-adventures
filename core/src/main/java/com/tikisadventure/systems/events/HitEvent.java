package com.tikisadventure.systems.events;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.base.Entity;

public class HitEvent implements Event {
    public final Vector2 position;
    public final Entity entity;

    public HitEvent(Entity entity, Vector2 position) {
        this.entity = entity;
        this.position = new Vector2(position);
    }
}
