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
        // Use player velocity for dash direction instead of mouse target position
        com.tikisadventure.components.VelocityComponent velComp = owner.getComponent(com.tikisadventure.components.VelocityComponent.class);
        Vector2 direction = new Vector2(1, 0);
        if (velComp != null && !velComp.velocidad.isZero()) {
            direction = velComp.velocidad.cpy().nor();
        } else {
            // If standing still, dash in facing direction
            direction = owner.getDirection();
        }
        owner.applyDashImpulse(direction.scl(force), duration);
    }
}
