package com.tikisadventure.systems.events;

import com.badlogic.gdx.math.Vector2;

public class HitEvent implements Event {
    public final Vector2 position;
    public final ImpactType type;

    public HitEvent(Vector2 position, ImpactType type) {
        this.position = new Vector2(position);
        this.type = type;
    }
}
