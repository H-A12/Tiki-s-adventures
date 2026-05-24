package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.components.BoomerangComponent;
import com.tikisadventure.effects.EffectManager;

//Añadir comportamiento de boomerang al proyectil
public class BoomerangModifier implements ProjectileModifier {
    private final float maxDistance;
    private final Weapon weapon;

    public BoomerangModifier(Weapon weapon, float maxDistance) {
        this.weapon = weapon;
        this.maxDistance = maxDistance;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        BoomerangComponent component = new BoomerangComponent(maxDistance, weapon);
        p.addComponent(component);
        component.onAttach(p);
    }
}
