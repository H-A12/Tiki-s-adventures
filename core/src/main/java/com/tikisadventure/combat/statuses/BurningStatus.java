package com.tikisadventure.combat.statuses;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.StatusType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.components.ParticleEmitterComponent;

public class BurningStatus implements StatusEffect {
    private EffectManager effectManager;
    private final float duration;
    private final float damagePerTick;
    private final float interval;
    private final DamageType damageType;
    private final Player owner;
    private float elapsedTime = 0;
    private float tickTimer = 0;
    private ParticleEmitterComponent emitter;

    public BurningStatus(EffectManager effectManager, float duration, float damagePerTick, float interval, DamageType damageType, Player owner) {
        this.effectManager = effectManager;
        this.duration = duration;
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.damageType = damageType;
        this.owner = owner;
    }

    // Constructor alternativo para componentes que ya calculan el bonus previamente
    public BurningStatus(EffectManager effectManager, float duration, float damagePerTick, float interval) {
        this(effectManager, duration, damagePerTick, interval, DamageType.FIRE, null);
    }

    @Override
    public void onApply(Entity target) {
        emitter = new ParticleEmitterComponent(effectManager, "FIRE_PARTICLE", new Vector2(0,0), 0.1f);
        target.addComponent(emitter);
    }

    @Override
    public void tick(Entity target, float delta) {
        elapsedTime += delta;
        tickTimer += delta;

        if (tickTimer >= interval) {
            target.receiveDamage(damagePerTick, false, damageType);
            tickTimer = 0;
        }
    }

    @Override
    public boolean isExpired() {
        return elapsedTime >= duration;
    }

    @Override
    public void onRemove(Entity target) {
        if (target != null && emitter != null) {
            target.removeComponent(emitter);
        }
    }

    @Override
    public StatusType getType() {
        return StatusType.BURNING;
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
