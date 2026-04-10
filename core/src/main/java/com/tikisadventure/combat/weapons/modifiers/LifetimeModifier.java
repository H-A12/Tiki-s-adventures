package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.effects.EffectManager;

public class LifetimeModifier implements ProjectileModifier {
    private float seconds;
    public LifetimeModifier(float seconds) { this.seconds = seconds; }
    @Override
    public void apply(Projectile p, EffectManager em) { p.setLifetime(seconds); }
}