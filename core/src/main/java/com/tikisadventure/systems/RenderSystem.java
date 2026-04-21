package com.tikisadventure.systems;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.base.Entity;

public class RenderSystem {

    public void render(Array<Entity> entities, Batch batch, float delta) {
        for (Entity e : entities) {
            if (e != null && e.isAlive()) {
                e.render(batch, delta);
            }
        }
    }

    public void render(Entity entity, Batch batch, float delta) {
        if (entity != null && entity.isAlive()) {
            entity.render(batch, delta);
        }
    }

    public void renderProjectiles(Array<Projectile> projectiles, Batch batch, float delta) {
        for (Projectile p : projectiles) {
            if (p != null && p.isAlive()) {
                p.render(batch);
            }
        }
    }
}
