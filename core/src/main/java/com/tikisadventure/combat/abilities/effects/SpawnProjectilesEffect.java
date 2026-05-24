package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.core.Assets;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

//Disparar varios proyectiles en círculo desde una posición
public class SpawnProjectilesEffect implements AbilityEffect {
    private DamageType damageType;
    private int count;
    private float damage;
    private EffectManager effectManager;
    private String spriteName;
    private String explosionProfile;
    private float scale;
    private float rotationSpeed;
    private float lifetime;
    private float growthRate;
    private float maxSize;

    public SpawnProjectilesEffect(EffectManager effectManager, DamageType damageType, int count, float damage, String spriteName, String explosionProfile, float scale, float rotationSpeed, float lifetime, float growthRate, float maxSize) {
        this.effectManager = effectManager;
        this.damageType = damageType;
        this.count = count;
        this.damage = damage;
        this.spriteName = spriteName;
        this.explosionProfile = explosionProfile;
        this.scale = scale;
        this.rotationSpeed = rotationSpeed;
        this.lifetime = lifetime;
        this.growthRate = growthRate;
        this.maxSize = maxSize;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        com.tikisadventure.combat.ExplosionUtility.explode(
            effectManager, targetPosition, explosionProfile, 0f, 0f, 0f, enemies
        );

        float bonus = owner.getDamageBonusByType(damageType);
        float finalDamage = damage * (1f + bonus);

        Vector2 origin = targetPosition;
        for (int i = 0; i < count; i++) {
            float baseAngle = (i * 360f / count);
            float randomOffset = MathUtils.random(-15f, 15f);
            float angle = baseAngle + randomOffset;
            Vector2 bulletDir = new Vector2(1, 0).setAngleDeg(angle);

            Projectile p = new Projectile(owner, origin, bulletDir, 10f, finalDamage, 0f, 1f, 0.5f * scale,
                                          Assets.getRegion("shared", spriteName), effectManager, null, 0f);
            p.setLifetime(lifetime);
            p.setDamageType(damageType);
            p.setRotationSpeed(rotationSpeed);
            p.setGrowthRate(growthRate);
            p.setMaxRadius(maxSize);
            owner.addProjectile(p);
        }
        return true;
    }
}
