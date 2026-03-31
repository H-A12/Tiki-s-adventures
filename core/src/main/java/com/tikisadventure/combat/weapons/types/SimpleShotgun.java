package com.tikisadventure.combat.weapons.types;

import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.combat.weapons.behaviors.ShotgunBehavior;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class SimpleShotgun extends Weapon {

    public SimpleShotgun(Entity owner, ProjectileCreator factory, EffectManager effectManager) {
        super(owner, new ShotgunBehavior(factory, Assets.getRegion("RedBullet"), 12f, 10f, 0.3f), effectManager);

        this.sprite = Assets.getRegion("Shotgun");

        this.cd = 0.8f;
        this.shootRange = 8f;
    }
}
