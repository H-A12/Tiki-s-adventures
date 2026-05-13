package com.tikisadventure.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import java.util.ArrayList;
import java.util.Collections;

public class RandomSpriteComponent implements Component {
    private ArrayList<String> spriteNames;
    private Projectile projectile;

    public RandomSpriteComponent(ArrayList<String> spriteNames) {
        this.spriteNames = spriteNames;
    }

    @Override
    public void onAttach(Object owner) {
        if (owner instanceof Projectile) {
            this.projectile = (Projectile) owner;
            selectRandomSprite();
        }
    }

    private void selectRandomSprite() {
        if (spriteNames == null || spriteNames.isEmpty() || projectile == null) return;
        int index = (int) (Math.random() * spriteNames.size());
        String spriteName = spriteNames.get(index);
        TextureRegion sprite = Assets.getRegion("shared", spriteName);
        if (sprite != null) {
            projectile.setSprite(sprite);
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}