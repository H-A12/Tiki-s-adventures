package com.tikisadventure.combat.weapons.types;

import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.combat.weapons.behaviors.MachineGunBehavior;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class SimpleMachineGun extends Weapon {

    public SimpleMachineGun(Entity owner, ProjectileCreator factory, EffectManager effectManager) {
        super(owner, new MachineGunBehavior(factory, Assets.getRegion("GreenBullet"), 20f, 4f, 0.19f), effectManager);
        this.sprite = Assets.getRegion("Machinegun");

        this.cd = 0.9f;
        this.shootRange = 14f;
    }
}
