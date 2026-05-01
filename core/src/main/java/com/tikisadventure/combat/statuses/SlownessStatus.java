package com.tikisadventure.combat.statuses;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.StatusType;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.components.ParticleEmitterComponent;

public class SlownessStatus implements StatusEffect {
    private EffectManager effectManager;
    private final float duration;
    private final float speedMult;
    private final float damagePerTick;
    private final float interval;
    private float elapsedTime = 0;
    private float tickTimer = 0;
    private float originalSpeed;
    private ParticleEmitterComponent emitter;

    public SlownessStatus(EffectManager effectManager, float duration, float speedMult, float damagePerTick, float interval) {
        this.effectManager = effectManager;
        this.duration = duration;
        this.speedMult = speedMult;
        this.damagePerTick = damagePerTick;
        this.interval = interval;
    }

    @Override
    public void onApply(Entity target) {
        originalSpeed = target.getSpeed();
        target.setSpeed(originalSpeed * speedMult);

        emitter = new ParticleEmitterComponent(effectManager, "ICE_PARTICLE", new Vector2(0, 0), 0.1f);
        target.addComponent(emitter);
    }

    @Override
    public void tick(Entity target, float delta) {
        elapsedTime += delta;
        tickTimer += delta;

        if (tickTimer >= interval) {
            target.receiveDamage(damagePerTick, false, DamageType.SLOW);
            tickTimer = 0;
        }
    }

    @Override
    public boolean isExpired() {
        return elapsedTime >= duration;
    }

    @Override
    public void onRemove(Entity target) {
        if (target != null) {
            target.setSpeed(originalSpeed);
            if (emitter != null) {
                target.removeComponent(emitter);
            }
        }
    }

    @Override
    public StatusType getType() {
        return StatusType.SLOW;
    }

    @Override
    public void refreshDuration() {
        this.elapsedTime = 0;
        this.tickTimer = 0;
    }

    @Override
    public void dispose() {
        effectManager = null;
    }
}