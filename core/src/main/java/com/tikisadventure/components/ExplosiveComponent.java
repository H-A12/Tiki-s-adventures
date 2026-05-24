package com.tikisadventure.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.traits.PositionProvider;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.base.Component;

//Explotar al morir el proyectil, dañando enemigos cercanos
public class ExplosiveComponent implements Component {
    private EffectManager effectManager;
    private final float explosionRadius;
    private final float explosionDamage;
    private final float knockbackForce;
    private final String explosionProfile;
    private boolean hasExploded = false;

    public ExplosiveComponent(EffectManager effectManager, float radius, float damage, float knockback, String explosionProfile) {
        this.effectManager = effectManager;
        this.explosionRadius = radius;
        this.explosionDamage = damage;
        this.knockbackForce = knockback;
        this.explosionProfile = explosionProfile;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}

    @Override
    public void onDeath(Object owner, Array<Entity> entities) {
        if (!hasExploded && owner != null && owner instanceof PositionProvider) {
            com.tikisadventure.combat.ExplosionUtility.explode(
                effectManager,
                ((PositionProvider) owner).getPosition(),
                explosionProfile,
                explosionRadius,
                explosionDamage,
                knockbackForce,
                entities
            );
            hasExploded = true;
        }
    }

    @Override
    public void dispose() {
        effectManager = null;
    }
}
