package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class DamageModifier implements HitModifier {
    private float damage;

    public DamageModifier(float damage) {
        this.damage = damage;
    }

    @Override
    public void apply(Entity attacker, Entity target, EffectManager em) {
        target.receiveDamage(damage);
    }
}
