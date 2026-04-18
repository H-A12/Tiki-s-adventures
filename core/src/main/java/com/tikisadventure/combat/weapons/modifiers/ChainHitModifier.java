package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.ChainHitComponent;
import com.tikisadventure.effects.EffectManager;

public class ChainHitModifier implements ProjectileModifier {
    private final int maxBounces;
    private final float searchRadius;

    public ChainHitModifier(int maxBounces, float searchRadius) {
        this.maxBounces = maxBounces;
        this.searchRadius = searchRadius;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        ChainHitComponent component = new ChainHitComponent(maxBounces, searchRadius);
        p.addComponent(component);
        component.onAttach(p);
    }
}