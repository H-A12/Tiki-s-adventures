package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class ImpulseEffect implements AbilityEffect {
    private float force;
    private float duration;

    public ImpulseEffect(float force, float duration) {
        this.force = force;
        this.duration = duration;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        Vector2 direction = owner.getInputDirection();

        if (direction.isZero()) {
            direction = owner.getLastMoveDirection();
        }
        if (direction.isZero()) {
            direction = targetPosition.cpy().sub(owner.getPosition());
        }
        if (direction.isZero()) {
            direction.set(0, -1);
        }

        owner.applyDashImpulse(direction.nor().scl(force), duration);
        return true;
    }
}
