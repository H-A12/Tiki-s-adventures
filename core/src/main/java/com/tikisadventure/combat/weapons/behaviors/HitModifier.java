package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public interface HitModifier {
    void apply(Entity owner, Entity target, EffectManager em, float damage);
}
