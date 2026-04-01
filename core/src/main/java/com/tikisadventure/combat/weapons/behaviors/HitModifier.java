package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;

public interface HitModifier {
    void apply(Entity attacker, Entity target, EffectManager em);
}
