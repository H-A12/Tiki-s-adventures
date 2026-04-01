package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.components.Knockbackable;
import com.tikisadventure.components.Killable;

public class CombatSystem {
    private final EffectManager effectManager;

    public CombatSystem(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void update(Array<Projectile> projectiles, Array<Entity> enemies, float delta) {
        for (Projectile p : projectiles) {
            if (!p.isAlive()) continue;

            Vector2 pos = p.getPosition();
            float hitRadius = p.getRadius();

            for (Entity e : enemies) {
                if (!e.isAlive()) continue;

                float enemyRadius = e.getHitboxActionTrigger().radius;
                float totalRadius = hitRadius + enemyRadius;

                if (pos.dst2(e.getPosicion()) <= totalRadius * totalRadius) {
                    if (!p.canHit(e)) continue;
                    p.registerHit(e);

                    e.receiveDamage(p.getDamage());
                    
                    if (p.canPenetrate()) {
                        p.reducePenetration();
                    } else {
                        p.die();
                        if (p.isExplosive()) {
                            performExplosion(p, enemies);
                        }
                        return;
                    }
                }
            }
        }
    }

    private void performExplosion(Projectile p, Array<Entity> enemies) {
        Vector2 pos = p.getPosition();
        float radius = p.getExplosionRadius();
        float damage = p.getExplosionDamage();
        float knockback = p.getKnockbackForce();

        for (Entity enemy : enemies) {
            if (enemy.isAlive()) {
                float distance = pos.dst(enemy.getPosicion());
                if (distance <= radius) {
                    enemy.receiveDamage(damage);
                    Vector2 pushDir = new Vector2(enemy.getPosicion()).sub(pos).nor();
                    if (pushDir.len() == 0) pushDir.set(1, 0);
                    float intensity = 1.0f - (distance / radius);
                    float finalForce = knockback * intensity;
                    if (enemy instanceof Knockbackable) {
                        ((Knockbackable) enemy).getKnockbackVelocity().add(pushDir.nor().scl(finalForce));
                    }
                }
            }
        }
    }
}
