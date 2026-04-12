package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.components.PositionComponent;
import com.tikisadventure.components.VelocityComponent;

public class MovementSystem {
    private EffectManager effectManager;

    public MovementSystem(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void update(Array<Entity> entities, float delta) {
        for (Entity e : entities) {
            PositionComponent pos = e.getComponent(PositionComponent.class);
            VelocityComponent vel = e.getComponent(VelocityComponent.class);
            if (pos != null && vel != null) {
                // Apply Velocity
                pos.posicion.mulAdd(vel.velocidad, delta);
                
                // Apply Knockback
                if (vel.knockbackVelocity.len() > 0.1f) {
                    pos.posicion.mulAdd(vel.knockbackVelocity, delta);
                    vel.knockbackVelocity.scl(1f - 8f * delta);
                    if (vel.knockbackVelocity.len() < 0.1f) {
                        vel.knockbackVelocity.setZero();
                    }
                }
            }
        }
    }

    public void updateProjectiles(Array<Projectile> projectiles, Array<Entity> enemies, float delta) {
        for (Projectile p : projectiles) {
            p.update(delta);
            for (Component c : p.getComponents()) {
                c.tick(p, delta, enemies);
            }
        }
    }
}
