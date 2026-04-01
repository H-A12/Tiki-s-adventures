package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.combat.weapons.behaviors.AttackBehavior;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class ConfigurableWeapon extends Weapon {

    public ConfigurableWeapon(Entity owner, TextureRegion sprite, float damage, float cd, float range, AttackBehavior behavior, EffectManager em) {
        super(owner, behavior, em);
        this.sprite = sprite;
        this.damage = damage;
        this.cd = cd;
        this.shootRange = range;
        if (behavior != null) {
            behavior.setWeapon(this);
        }
    }
}
