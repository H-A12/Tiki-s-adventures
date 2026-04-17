package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.traits.Knockbackable;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.events.DamageEvent;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.HitEvent;

public class CombatSystem {
    private final EffectManager effectManager;
    private final Vector2 pushDir = new Vector2();

    public CombatSystem(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void processDamage(Entity target, float quantity, boolean isCritical, DamageType damageType) {
        if (!target.isAlive()) return;
        
        HealthComponent health = target.getComponent(HealthComponent.class);
        if (health != null) {
            health.currentHealth -= quantity;
            EventBus.publish(new DamageEvent(target, quantity, isCritical, damageType));

            if (health.currentHealth <= 0) {
                health.currentHealth = 0;
                target.die();
            }
        }
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

                if (pos.dst2(e.getPosition()) <= totalRadius * totalRadius) {
                    if (p instanceof com.tikisadventure.combat.abilities.effects.GrenadeProjectile) continue;

                    if (!p.canHit(e)) continue;
                    p.registerHit(e);

                    processDamage(e, p.getDamageValue(), p.isCrit(), p.getDamageType());

                    float knockback = p.getImpactKnockback();
                    if (knockback > 0 && e instanceof Knockbackable) {
                        pushDir.set(p.getDirection()).nor().scl(knockback);
                        ((Knockbackable) e).getKnockbackVelocity().add(pushDir);
                    }
                    
                    for (Component c : p.getComponents()) {
                        c.onHit(e);
                    }
                    
                    EventBus.publish(new HitEvent(e, e.getPosition()));

                    if (p.canPenetrate()) {
                        p.reducePenetration();
                    } else {
                        Array<Entity> enemiesCopy = new Array<>(enemies);
                        p.die(enemiesCopy);
                    }
                }
            }
        }
    }

    public boolean checkEnemyProjectileCollisions(Array<Projectile> enemyProjectiles, Player player) {
        if (player == null || !player.isAlive()) return false;
        
        boolean tookDamage = false;
        
        for (Projectile p : enemyProjectiles) {
            if (!p.isAlive()) continue;
            
            Vector2 pos = p.getPosition();
            float hitRadius = p.getRadius();
            float playerRadius = player.getHitboxActionTrigger().radius;
            float totalRadius = hitRadius + playerRadius;
            
            if (pos.dst2(player.getPosition()) <= totalRadius * totalRadius) {
                if (!p.canHit(player)) continue;
                p.registerHit(player);
                
                processDamage(player, p.getDamageValue(), p.isCrit(), p.getDamageType());
                
                for (Component c : p.getComponents()) {
                    c.onHit(player);
                }
                
                tookDamage = true;
                
                if (p.canPenetrate()) {
                    p.reducePenetration();
                } else {
                    p.die();
                }
            }
        }
        
        return tookDamage;
    }
}
