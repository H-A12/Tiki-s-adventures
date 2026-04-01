package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.components.PenetrationComponent;
import com.tikisadventure.effects.EffectManager;

public class PenetrationModifier implements ProjectileModifier {
    private int penetrations;

    public PenetrationModifier(int penetrations) {
        this.penetrations = penetrations;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        p.addComponent(new PenetrationComponent(penetrations));
    }
}
