package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.BurningComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class BurningEffect implements AbilityEffect {
    private float radius;
    private float damagePerTick;
    private float duration;
    private EffectManager effectManager;

    public BurningEffect(EffectManager effectManager, float radius, float damagePerTick, float duration) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.damagePerTick = damagePerTick;
        this.duration = duration;
    }

    @Override
    public void execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // Find enemies in radius and apply BurningComponent
        for (Entity e : enemies) {
            if (e.getPosition().dst(targetPosition) < radius) {
                e.addComponent(new BurningComponent(effectManager, damagePerTick, 0.5f, duration));
            }
        }
    }
}
