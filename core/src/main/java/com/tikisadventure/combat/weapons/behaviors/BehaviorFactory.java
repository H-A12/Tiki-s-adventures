package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.combat.weapons.ProjectileCreator;

public interface BehaviorFactory {
    AttackBehavior create(JsonValue params, ProjectileCreator pc, float damage);
}
