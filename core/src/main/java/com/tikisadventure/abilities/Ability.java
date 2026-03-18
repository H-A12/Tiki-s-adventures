package com.tikisadventure.abilities;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;

public abstract class Ability {
    protected float cooldown;
    protected float cooldownTimer;
    protected Entity owner;

    public Ability(Entity owner, float cooldown) {
        this.owner = owner;
        this.cooldown = cooldown;
    }

    public void update(float delta) {
        if (cooldownTimer > 0) cooldownTimer -= delta;
    }

    public boolean canUse() {
        return cooldownTimer <= 0;
    }

    public abstract void activate();
}
