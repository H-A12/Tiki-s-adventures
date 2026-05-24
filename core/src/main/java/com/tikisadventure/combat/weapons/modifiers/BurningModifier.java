package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.BurningComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.combat.DamageType;

//Añadir efecto de quemadura al proyectil
public class BurningModifier implements ProjectileModifier {
    private final float damagePerTick;
    private final float interval;
    private final float duration;

    public BurningModifier(float damagePerTick, float interval, float duration) {
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        float finalDamage = this.damagePerTick;

        if (p.getOwner() instanceof Player) {
            Player player = (Player) p.getOwner();
            float bonus = player.getDamageBonusByType(DamageType.FIRE);
            finalDamage *= (1f + bonus);
        }

        p.addComponent(new BurningComponent(em, finalDamage, interval, duration));
    }
}
