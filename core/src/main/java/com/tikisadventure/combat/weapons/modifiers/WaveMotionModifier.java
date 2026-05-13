package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.WaveMotionComponent;
import com.tikisadventure.effects.EffectManager;

public class WaveMotionModifier implements ProjectileModifier {
    private final float amplitude;
    private final float frequency;

    public WaveMotionModifier(float amplitude, float frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        WaveMotionComponent component = new WaveMotionComponent(amplitude, frequency);
        p.addComponent(component);
        component.onAttach(p);
    }
}