package com.tikisadventure.combat.weapons.types;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.tikisadventure.core.Assets;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.combat.weapons.behaviors.ProjectileBehavior;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class SimplePistol extends Weapon {

    public SimplePistol(Entity owner, ProjectileCreator factory, EffectManager effectManager) {
        super(owner, new ProjectileBehavior(factory, Assets.getRegion("YellowBullet"), 5f, 8f, 0.15f, null, 0f), effectManager);

        this.sprite = Assets.getRegion("Handgun");

        this.cd = 1f;
        this.shootRange = 12f;
    }
}
