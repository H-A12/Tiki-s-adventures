package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class LightningTrailComponent implements Component {
    private Projectile projectile;
    private EffectManager effectManager;
    private float lastSpawnTime;
    private static final float SPAWN_INTERVAL = 0.001f;

    public LightningTrailComponent(float amplitude, float frequency, EffectManager effectManager) {
        this.effectManager = effectManager;
        this.lastSpawnTime = 0f;
    }

    @Override
    public void onAttach(Object owner) {
        if (owner instanceof Projectile) {
            this.projectile = (Projectile) owner;
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (projectile == null || effectManager == null) return;

        lastSpawnTime += delta;

        if (lastSpawnTime >= SPAWN_INTERVAL) {
            effectManager.spawnEffect("TRAIL_LIGHTNING", projectile.getPosition(), projectile.getDirection());
            lastSpawnTime = 0f;
        }
    }

    @Override
    public void dispose() {
        effectManager = null;
        projectile = null;
    }
}
