package com.tikisadventure.combat.statuses;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.StatusType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class BurningStatus implements StatusEffect {
    private final EffectManager effectManager;
    private final float damagePerTick;
    private final float interval;
    private final float duration;
    private float elapsedTime = 0;
    private float tickTimer = 0;
    private float particleTimer = 0;

    public BurningStatus(EffectManager effectManager, float damagePerTick, float interval, float duration) {
        this.effectManager = effectManager;
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void tick(Entity target, float delta) {
        elapsedTime += delta;
        tickTimer += delta;
        particleTimer += delta;

        if (tickTimer >= interval) {
            target.receiveDamage(damagePerTick, false, DamageType.FIRE);
            tickTimer = 0;
        }

        if (particleTimer >= 0.1f) {
            float dispersion = 0.5f;
            float offsetX = MathUtils.random(-target.getANCHO() / 2f * dispersion, target.getANCHO() / 2f * dispersion);
            float offsetY = MathUtils.random(-target.getALTO() / 2f * dispersion, target.getALTO() / 2f * dispersion);
            Vector2 spawnPos = new Vector2(target.getPosicion()).add(offsetX, offsetY);
            effectManager.spawnSingleParticle(EffectType.FIRE_PARTICLE, spawnPos, new Vector2(0, 8));
            particleTimer = 0;
        }
    }

    @Override
    public boolean isExpired() {
        return elapsedTime >= duration;
    }

    @Override
    public void onApply(Entity target) {}

    @Override
    public void onRemove(Entity target) {}

    @Override
    public StatusType getType() {
        return StatusType.BURNING;
    }
}
