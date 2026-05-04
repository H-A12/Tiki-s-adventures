package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.components.ChainHitComponent;
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

    // --- AHORA DEVUELVE BOOLEAN PARA SABER SI ACERTÓ O ESQUIVÓ ---
    public boolean processDamage(Entity attacker, Entity target, float quantity, boolean isCritical, DamageType damageType) {
        if (!target.isAlive()) return false;

        // --- LÓGICA DE EVASIÓN ---
        if (target instanceof Player) {
            Player p = (Player) target;
            if (com.badlogic.gdx.math.MathUtils.random() < p.getEvasionChance()) {
                com.tikisadventure.systems.events.EventBus.publish(new com.tikisadventure.systems.events.EvadeEvent(p));
                return false; // Retorna false: ¡Daño evadido!
            }
        }

        HealthComponent health = target.getComponent(HealthComponent.class);
        if (health != null) {

            float effectiveDamage = Math.min(health.currentHealth, quantity);

            health.currentHealth -= quantity;
            EventBus.publish(new DamageEvent(target, quantity, isCritical, damageType));

            // --- LÓGICA DE ROBO DE VIDA (LEACH) ---
            if (attacker instanceof Player) {
                Player player = (Player) attacker;
                float leechPercent = player.getLifeLeechPercent();

                if (leechPercent > 0) {
                    float distance = player.getPosition().dst(target.getPosition());
                    float optimalRange = player.getOptimalLeechRange();

                    if (distance > optimalRange) {
                        float extraDistance = distance - optimalRange;
                        leechPercent -= (extraDistance * player.getLeechFalloffPercent());
                    }

                    if (leechPercent < 0.01f) {
                        leechPercent = 0.01f;
                    }

                    float healAmount = (effectiveDamage * leechPercent) / 3.0f;


                    if (healAmount < 0.0033f && effectiveDamage > 0) {
                        healAmount = 0.0033f;
                    }

                    float vidaAntes = player.getHealthComponent().currentHealth;
                    player.heal(healAmount);

                    float vidaRestaurada = player.getHealthComponent().currentHealth - vidaAntes;
                    player.leechTextAccumulator += vidaRestaurada;

                }
            }
            // ----------------------------------------

            if (health.currentHealth <= 0) {
                health.currentHealth = 0;
                target.die();
            }
        }
        return true; // Retorna true: Daño aplicado correctamente
    }

    public void update(Array<Projectile> projectiles, Array<Entity> enemies, float delta) {
        for (int pi = 0; pi < projectiles.size; pi++) {
            Projectile p = projectiles.get(pi);
            if (!p.isAlive()) continue;

            Vector2 pos = p.getPosition();
            float hitRadius = p.getRadius();

            ChainHitComponent chainHit = p.getComponent(ChainHitComponent.class);

            for (int ei = 0; ei < enemies.size; ei++) {
                Entity e = enemies.get(ei);
                if (!e.isAlive()) continue;

                float enemyRadius = e.getHitboxActionTrigger().radius;
                float totalRadius = hitRadius + enemyRadius;

                if (pos.dst2(e.getPosition()) <= totalRadius * totalRadius) {
                    if (p instanceof com.tikisadventure.combat.abilities.effects.GrenadeProjectile) continue;

                    if (chainHit != null && chainHit.hasHitTarget(e)) {
                        continue;
                    }

                    if (!p.canHit(e)) continue;
                    p.registerHit(e);

                    processDamage(p.getOwner(), e, p.getDamageValue(), p.isCrit(), p.getDamageType());

                    float knockback = p.getImpactKnockback();
                    if (knockback > 0 && e instanceof Knockbackable) {
                        pushDir.set(p.getDirection()).nor().scl(knockback);
                        ((Knockbackable) e).getKnockbackVelocity().add(pushDir);
                    }

                    for (int ci = 0; ci < p.getComponents().size; ci++) {
                        p.getComponents().get(ci).onHit(e);
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

                // --- COMPROBAMOS SI SE ESQUIVÓ ANTES DE APLICAR IMPACTOS ---
                boolean hitLanded = processDamage(p.getOwner(), player, p.getDamageValue(), p.isCrit(), p.getDamageType());

                if (hitLanded) {
                    for (Component c : p.getComponents()) {
                        c.onHit(player);
                    }
                    tookDamage = true;
                }

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
