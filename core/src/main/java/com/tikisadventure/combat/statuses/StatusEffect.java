package com.tikisadventure.combat.statuses;

import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.StatusType;

public interface StatusEffect {
    void tick(Entity target, float delta);
    boolean isExpired();
    void onApply(Entity target);
    void onRemove(Entity target);
    StatusType getType();

    // Obliga a todos los estados a tener una forma de reiniciar su tiempo
    void refreshDuration();

    default void dispose() {}
}
