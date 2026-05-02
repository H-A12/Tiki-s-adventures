package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.combat.DamageType;

public class ExplosiveModifier implements ProjectileModifier {
    private float radius, damage, knockback;
    private String profile;

    public ExplosiveModifier(float radius, float damage, float knockback, String profile) {
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
        this.profile = profile;
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        float finalDamage = this.damage;

        // Comprobamos si el dueño es el Player para aplicar el escalado
        if (p.getOwner() instanceof Player) {
            Player player = (Player) p.getOwner();
            float bonus = player.getDamageBonusByType(DamageType.EXPLOSIVE);
            finalDamage *= (1f + bonus);
        }

        p.addComponent(new ExplosiveComponent(em, radius, finalDamage, knockback, profile));
    }
}
