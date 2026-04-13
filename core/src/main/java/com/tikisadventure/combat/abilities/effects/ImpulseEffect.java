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
    public void execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        Vector2 direction = targetPosition.cpy().sub(owner.getPosition()).nor();
        if (direction.isZero()) direction.set(1, 0);
        owner.applyDashImpulse(direction.scl(force), duration);
    }
}
