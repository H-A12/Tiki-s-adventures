package com.tikisadventure.combat.weapons.behaviors;

import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.badlogic.gdx.math.Vector2;

public interface AttackBehavior {
    void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em);
    void update(float delta);
    void setWeapon(Weapon weapon);
}
