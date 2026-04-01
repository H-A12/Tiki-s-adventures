package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class MeleeKnockbackModifier implements HitModifier {
    private float force;

    public MeleeKnockbackModifier(float force) {
        this.force = force;
    }

    @Override
    public void apply(Entity attacker, Entity target, EffectManager em, float attackAngle) {
        float rad = (float) Math.toRadians(attackAngle);
        float x = (float) Math.cos(rad) * force;
        float y = (float) Math.sin(rad) * force;
        target.setKnockbackVelocity(new Vector2(x, y));
    }
}
