package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;

public class MovementSystem {
    public void update(Array<Projectile> projectiles, float delta) {
        for (Projectile p : projectiles) {
            p.update(delta);
        }
    }
}
