package com.tikisadventure.combat.statuses;

import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.StatusType;

//Interfaz para los estados que se aplican a entidades (quemadura, veneno, etc.)
public interface StatusEffect {
    void tick(Entity target, float delta);
    boolean isExpired();
    void onApply(Entity target);
    void onRemove(Entity target);
    StatusType getType();

    void refreshDuration();

    default void dispose() {}
}
