package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.effects.EffectType;

public class Emitter {
    public EffectType type;
    public Vector2 offset;

    public Emitter(EffectType type, Vector2 offset) {
        this.type = type;
        this.offset = offset;
    }
}
