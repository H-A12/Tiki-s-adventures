package com.tikisadventure.systems.events;

import com.badlogic.gdx.math.Vector2;

public class FiredEvent implements Event {
    public final Vector2 position;
    public final Vector2 direction;
    public final String effectType;
    public final String muzzleFlashType;

    public FiredEvent(Vector2 position, Vector2 direction, String effectType) {
        this(position, direction, effectType, null);
    }

    public FiredEvent(Vector2 position, Vector2 direction, String effectType, String muzzleFlashType) {
        this.position = new Vector2(position);
        this.direction = new Vector2(direction);
        this.effectType = effectType;
        this.muzzleFlashType = muzzleFlashType;
    }
}
