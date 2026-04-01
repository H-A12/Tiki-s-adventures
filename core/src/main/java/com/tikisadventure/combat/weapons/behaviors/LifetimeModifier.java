package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;

public class LifetimeModifier implements ProjectileModifier {
    private float lifetime;

    public LifetimeModifier(float lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        p.setLifetime(lifetime);
    }
}
