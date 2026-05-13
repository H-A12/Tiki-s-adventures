package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.abilities.effects.GrenadeProjectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.components.PositionComponent;
import com.tikisadventure.components.VelocityComponent;
import com.tikisadventure.combat.projectiles.ProjectileFactory;

public class MovementSystem {

    private EffectManager effectManager;
    private ProjectileFactory projectileFactory;

    public MovementSystem(EffectManager effectManager, ProjectileFactory projectileFactory) {
        this.effectManager = effectManager;
        this.projectileFactory = projectileFactory;
    }

    public void update(Array<Entity> entities, float delta) {
        for (Entity e : entities) {
            PositionComponent pos = e.getComponent(PositionComponent.class);
            VelocityComponent vel = e.getComponent(VelocityComponent.class);
            if (pos != null && vel != null) {
                //Velocidad
                pos.posicion.mulAdd(vel.velocidad, delta);

                //Knockback
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
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);

            //Actualizar
            p.update(delta, enemies);

            //Tick de componentes
            for (Component c : p.getComponents()) {
                c.tick(p, delta, enemies);
            }

            //Eliminar y reciclar
            if (!p.isAlive()) {
                if (!(p instanceof GrenadeProjectile)) {
                    projectileFactory.freeProjectile(p);
                }
                projectiles.removeIndex(i);
            }
        }
    }
}
