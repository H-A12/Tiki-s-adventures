package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class MovementSystem {
    private EffectManager effectManager;

    public MovementSystem(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void update(Array<Projectile> projectiles, Array<Entity> enemies, float delta) {
        for (Projectile p : projectiles) {
            p.update(delta);
            for (Component c : p.getComponents()) {
                c.tick(p, delta, enemies);
            }
        }
    }
}
