package com.tikisadventure.systems.events;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.effects.EffectType;

public class FiredEvent implements Event {
    public final Vector2 position;
    public final Vector2 direction;
    public final EffectType effectType;

    public FiredEvent(Vector2 position, Vector2 direction, EffectType effectType) {
        this.position = new Vector2(position);
        this.direction = new Vector2(direction);
        this.effectType = effectType;
    }
}
