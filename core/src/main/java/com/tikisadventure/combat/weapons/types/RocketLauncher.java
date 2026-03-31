package com.tikisadventure.combat.weapons.types;

import com.tikisadventure.core.Assets;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.combat.weapons.behaviors.RocketBehavior;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class RocketLauncher extends Weapon {

    public RocketLauncher(Entity owner, ProjectileCreator factory, EffectManager effectManager) {
        super(owner, new RocketBehavior(factory, Assets.getRegion("RocketBullet")), effectManager);
        this.sprite = Assets.getRegion("RocketLauncher");

        this.cd = 1f;
        this.shootRange = 12f;
    }
}
