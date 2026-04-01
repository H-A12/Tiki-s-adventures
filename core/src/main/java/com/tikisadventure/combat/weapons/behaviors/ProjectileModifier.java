package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;

public interface ProjectileModifier {
    void apply(Projectile p, EffectManager em);
}
