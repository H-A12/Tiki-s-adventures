package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.effects.EffectManager;

public class ExplosiveModifier implements ProjectileModifier {
    private float radius;
    private float damage;
    private float knockback;

    public ExplosiveModifier(float radius, float damage, float knockback) {
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        p.addComponent(new ExplosiveComponent(em, radius, damage, knockback));
    }
}
