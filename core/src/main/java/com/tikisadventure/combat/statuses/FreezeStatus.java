package com.tikisadventure.combat.statuses;

import com.tikisadventure.combat.StatusType;
import com.tikisadventure.entities.base.Entity;

public class FreezeStatus implements StatusEffect {
    private float duration;
    private float timer;
    private float originalSpeed;

    public FreezeStatus(float duration) {
        this.duration = duration;
        this.timer = 0;
    }

    @Override
    public void onApply(Entity target) {
        originalSpeed = target.getSpeed();
        target.setSpeed(0);

        // Frenado en seco: Matamos la inercia
        target.getVelocity().setZero();
        target.getKnockbackVelocity().setZero();

        // ¡CONGELAMOS LA ENTIDAD!
        target.setFrozen(true);
    }

    @Override
    public void tick(Entity target, float delta) {
        timer += delta;
    }

    @Override
    public void onRemove(Entity target) {
        target.setSpeed(originalSpeed);
        target.setFrozen(false);
    }

    @Override
    public boolean isExpired() {
        return timer >= duration;
    }

    @Override
    public StatusType getType() {
        return StatusType.FREEZE;
    }

    @Override
    public void refreshDuration() {
        timer = 0;
    }
}
