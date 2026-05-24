package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

//Crear una explosión en una posición que daña y empuja enemigos
public class ExplosionEffect implements AbilityEffect {
    private float radius;
    private float damage;
    private float knockback;
    private EffectManager effectManager;
    private DamageType damageType;

    private String explosionProfile;

    public ExplosionEffect(EffectManager effectManager, float radius, float damage, float knockback, String explosionProfile, DamageType damageType) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
        this.explosionProfile = explosionProfile;
        this.damageType = damageType;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        com.tikisadventure.combat.ExplosionUtility.explode(
            owner,
            effectManager,
            targetPosition,
            explosionProfile,
            radius,
            damage,
            knockback,
            damageType,
            enemies
        );
        return true;
    }
}
