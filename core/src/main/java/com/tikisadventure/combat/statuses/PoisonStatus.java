package com.tikisadventure.combat.statuses;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.StatusType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.components.ParticleEmitterComponent;

public class PoisonStatus implements StatusEffect {
    private EffectManager effectManager;
    private final float damagePerTick;
    private final float interval;
    private final float duration;
    private float elapsedTime = 0;
    private float tickTimer = 0;
    private ParticleEmitterComponent emitter;

    public PoisonStatus(EffectManager effectManager, float damagePerTick, float interval, float duration) {
        this.effectManager = effectManager;
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void tick(Entity target, float delta) {
        elapsedTime += delta;
        tickTimer += delta;

        if (tickTimer >= interval) {
            float finalDamage = damagePerTick;

            // --- LÓGICA DE EJECUCIÓN (Más daño cuanta menos vida) ---
            if (target.getHealthComponent() != null && target.getHealthComponent().maxHealth > 0) {
                float hpPercent = target.getHealthComponent().currentHealth / target.getHealthComponent().maxHealth;

                // Si la vida está al 100% (1.0), multiplier es 1x
                // Si la vida está al 10% (0.1), multiplier es 1.9x
                float executionMultiplier = 1.0f + (1.0f - hpPercent);
                finalDamage *= executionMultiplier;
            }

            target.receiveDamage(finalDamage, false, DamageType.POISON);
            tickTimer = 0;
        }
    }

    @Override
    public boolean isExpired() {
        return elapsedTime >= duration;
    }

    @Override
    public void onApply(Entity target) {
        emitter = new ParticleEmitterComponent(effectManager, "POISON_PARTICLE", new Vector2(0,0), 0.2f);
        target.addComponent(emitter);
    }

    @Override
    public void onRemove(Entity target) {
        if (target != null && emitter != null) {
            target.removeComponent(emitter);
        }
    }

    @Override
    public StatusType getType() {
        return StatusType.POISONED;
    }

    @Override
    public void refreshDuration() {
        this.elapsedTime = 0; // Reinicia el tiempo total del veneno
        this.tickTimer = 0;   // Reinicia el contador del siguiente tic de daño
    }

    @Override
    public void dispose() {
        effectManager = null;
    }
}
