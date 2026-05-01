package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.LightningTrailComponent;
import com.tikisadventure.effects.EffectManager;

public class LightningTrailModifier implements ProjectileModifier {
    private final float amplitude;
    private final float frequency;

    public LightningTrailModifier(float amplitude, float frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        LightningTrailComponent component = new LightningTrailComponent(amplitude, frequency, em);
        p.addComponent(component);
        component.onAttach(p);
    }
}