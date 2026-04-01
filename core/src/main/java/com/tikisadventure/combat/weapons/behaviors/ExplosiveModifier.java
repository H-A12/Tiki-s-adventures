package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.effects.EffectManager;

public class ExplosiveModifier implements ProjectileModifier {
    private float radius;
    private float damage;

    public ExplosiveModifier(float radius, float damage) {
        this.radius = radius;
        this.damage = damage;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        // Keeping similar parameters as in RocketBehavior
        p.addComponent(new ExplosiveComponent(em, 15.0f, radius, damage, 10, 25));
    }
}
