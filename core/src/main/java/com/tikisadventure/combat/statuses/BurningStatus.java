package com.tikisadventure.combat.statuses;

import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.StatusType;
import com.tikisadventure.entities.base.Entity;

public class BurningStatus implements StatusEffect {
    private final float damagePerTick;
    private final float interval;
    private final float duration;
    private float elapsedTime = 0;
    private float tickTimer = 0;

    public BurningStatus(float damagePerTick, float interval, float duration) {
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void tick(Entity target, float delta) {
        elapsedTime += delta;
        tickTimer += delta;

        if (tickTimer >= interval) {
            target.receiveDamage(damagePerTick, false, DamageType.FIRE);
            tickTimer = 0;
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
