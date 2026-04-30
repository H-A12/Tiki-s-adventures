package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.PoisonComponent;
import com.tikisadventure.effects.EffectManager;

public class PoisonModifier implements ProjectileModifier {
    private final float damagePerTick;
    private final float interval;
    private final float duration;

    public PoisonModifier(float damagePerTick, float interval, float duration) {
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        p.addComponent(new PoisonComponent(em, damagePerTick, interval, duration));
    }
}
