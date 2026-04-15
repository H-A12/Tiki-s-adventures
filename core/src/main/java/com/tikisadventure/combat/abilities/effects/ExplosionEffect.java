package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class ExplosionEffect implements AbilityEffect {
    private float radius;
    private float damage;
    private float knockback;
    private EffectManager effectManager;

    private String explosionProfile;

    public ExplosionEffect(EffectManager effectManager, float radius, float damage, float knockback, String explosionProfile) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
        this.explosionProfile = explosionProfile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        com.tikisadventure.combat.ExplosionUtility.explode(
            effectManager,
            targetPosition,
            explosionProfile,
            radius,
            damage,
            knockback,
            enemies
        );
        return true;
    }
}
