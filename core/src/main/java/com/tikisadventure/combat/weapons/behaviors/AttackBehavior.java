package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;

public interface AttackBehavior {
    void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em);
    void update(float delta);
}
