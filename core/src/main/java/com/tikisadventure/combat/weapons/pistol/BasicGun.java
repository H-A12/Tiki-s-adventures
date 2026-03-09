package com.tikisadventure.combat.weapons.pistol;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.Entity;

public class BasicGun extends Weapon {

    private static Texture texture = new Texture("gun.png");

    public BasicGun(Entity owner) {

        super(owner);

        sprite = new TextureRegion(texture);

        cd = 0.6f;
        damage = 1f;
        bulletSpeed = 8f;
        bulletSize = 0.2f;
        shootRange = 6f;
    }

    @Override
    protected void shoot() {

        System.out.println("Bang!");
    }
}
