package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.BounceComponent;
import com.tikisadventure.effects.EffectManager;

//Añadir rebotes al proyectil
public class BounceModifier implements ProjectileModifier {
    private final int maxBounces;

    public BounceModifier(int maxBounces) {
        this.maxBounces = maxBounces;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        BounceComponent bounceComponent = new BounceComponent(maxBounces);
        p.addComponent(bounceComponent);
        bounceComponent.onAttach(p);
    }
}
