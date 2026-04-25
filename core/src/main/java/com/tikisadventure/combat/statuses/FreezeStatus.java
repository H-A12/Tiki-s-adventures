package com.tikisadventure.combat.statuses;

import com.badlogic.gdx.graphics.Color;
import com.tikisadventure.combat.StatusType;
import com.tikisadventure.effects.EffectManager;
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
        // Guardamos la velocidad original y congelamos
        originalSpeed = target.getSpeed();
        target.setSpeed(0);

        // Aplicamos el tinte azul helado a la entidad universalmente
        target.setTintColor(new Color(0.5f, 0.8f, 1.0f, 1.0f));
    }

    @Override
    public void tick(Entity target, float delta) {
        timer += delta;
    }

    @Override
    public void onRemove(Entity target) {
        // Restauramos velocidad y quitamos el tinte
        target.setSpeed(originalSpeed);
        target.setTintColor(Color.WHITE);
    }

    @Override
    public boolean isExpired() {
        return timer >= duration;
    }

    @Override
    public StatusType getType() {
        // Asume que tienes FREEZE en tu enum StatusType. Si no, pon null o añádelo.
        return StatusType.FREEZE;
    }

    @Override
    public void refreshDuration() {
        timer = 0; // Si le cae otra granada, reiniciamos el tiempo congelado
    }
}
