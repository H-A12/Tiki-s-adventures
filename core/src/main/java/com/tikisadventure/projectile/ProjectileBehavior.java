package com.tikisadventure.projectile;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public interface ProjectileBehavior {
    void update(Projectile p, float delta, Array<Entity> enemies);
}
