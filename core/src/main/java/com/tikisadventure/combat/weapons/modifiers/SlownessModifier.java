package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.SlownessComponent;
import com.tikisadventure.effects.EffectManager;

public class SlownessModifier implements ProjectileModifier {
    private final float speedMult;
    private final float damagePerTick;
    private final float interval;
    private final float duration;

    public SlownessModifier(float speedMult, float damagePerTick, float interval, float duration) {
        this.speedMult = speedMult;
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        p.addComponent(new SlownessComponent(em, speedMult, damagePerTick, interval, duration));
    }
}