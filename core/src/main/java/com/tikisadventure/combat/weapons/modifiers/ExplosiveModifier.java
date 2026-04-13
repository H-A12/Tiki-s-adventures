package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.components.ExplosiveComponent;

public class ExplosiveModifier implements ProjectileModifier {
    private float radius, damage, knockback;
    private String profile;
    public ExplosiveModifier(float radius, float damage, float knockback, String profile) {
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
        this.profile = profile;
    }
    @Override
    public void apply(Projectile p, EffectManager em) {
        p.addComponent(new ExplosiveComponent(em, radius, damage, knockback, profile));
    }
}