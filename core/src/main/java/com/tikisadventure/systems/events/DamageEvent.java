package com.tikisadventure.systems.events;

import com.tikisadventure.combat.DamageType;
import com.tikisadventure.entities.base.Entity;

public class DamageEvent implements Event {
    public final Entity entity;
    public final float damage;
    public final boolean isCritical;
    public final DamageType damageType;

    public DamageEvent(Entity entity, float damage, boolean isCritical, DamageType damageType) {
        this.entity = entity;
        this.damage = damage;
        this.isCritical = isCritical;
        this.damageType = damageType;
    }
}