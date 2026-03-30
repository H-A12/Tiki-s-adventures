package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class CollisionSystem {

    private final Vector2 tempDir = new Vector2();
    private final Array<Entity> enemiesTemp = new Array<>();
    private final Array<Projectile> projectilesTemp = new Array<>();
    private final EffectManager effectManager;

    public CollisionSystem(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void update(Player player, Array<Entity> allEntities, float delta) {
        enemiesTemp.clear();
        projectilesTemp.clear();

        for (Entity e : allEntities) {
            if (!e.isAlive()) continue;
            e.actualizarHitboxes();

            if (e instanceof Projectile) {
                projectilesTemp.add((Projectile) e);
            } else if (e != player) {
                enemiesTemp.add(e);
            }
        }
        player.actualizarHitboxes();

        resolveEnemySeparation(enemiesTemp, delta);
        resolvePlayerEnemyCollision(player, enemiesTemp, delta);
        resolveProjectileCollisions(projectilesTemp, enemiesTemp);
    }

    private void resolveProjectileCollisions(Array<Projectile> projectiles, Array<Entity> enemies) {
        for (Projectile p : projectiles) {
            for (Entity enemy : enemies) {
                // dst2 es más rápido que dst (evita la raíz cuadrada)
                float distSq = p.getPosicion().dst2(enemy.getPosicion());
                // Los proyectiles deben tener getRadius() definido
                float minDist = p.getRadius() + enemy.getHitboxActionTrigger().radius;

                if (distSq < minDist * minDist) {
                    // 1. Lógica de Daño y Feedback
                    if (enemy.receiveDamage(p.getDamage())) {
                        effectManager.startDamageFlash(enemy);

                        // Calculamos dirección para partículas y knockback
                        tempDir.set(enemy.getPosicion()).sub(p.getPosicion()).nor();

                        // 2. Efecto visual de impacto
                        effectManager.spawnEffect(EffectType.EXPLOSION_HUMO, p.getPosicion(), tempDir.cpy().scl(0.5f));

                        // 3. Física de Retroceso
                        enemy.applyKnockback(tempDir, 0.15f);
                    }

                    p.die();
                    break;
                }
            }
        }
    }

    private void resolveEnemySeparation(Array<Entity> enemies, float delta) {
        float strength = 5f;
        for (int i = 0; i < enemies.size; i++) {
            Entity a = enemies.get(i);
            for (int j = i + 1; j < enemies.size; j++) {
                Entity b = enemies.get(j);

                float distSq = a.getPosicion().dst2(b.getPosicion());
                float minDist = a.getHitboxActionTrigger().radius + b.getHitboxActionTrigger().radius;

                if (distSq < minDist * minDist && distSq > 0) {
                    tempDir.set(b.getPosicion()).sub(a.getPosicion()).nor();
                    float overlap = minDist - (float)Math.sqrt(distSq);
                    float force = overlap * strength * delta;

                    a.getPosicion().mulAdd(tempDir, -force);
                    b.getPosicion().mulAdd(tempDir, force);
                }
            }
        }
    }

    private void resolvePlayerEnemyCollision(Player player, Array<Entity> enemies, float delta) {
        for (Entity enemy : enemies) {
            float distSq = enemy.getPosicion().dst2(player.getPosicion());
            float minDist = enemy.getHitboxActionTrigger().radius + player.getHitboxActionTrigger().radius;

            if (distSq < minDist * minDist) {
                tempDir.set(enemy.getPosicion()).sub(player.getPosicion()).nor();

                // Empuje mutuo para que no se traspasen
                enemy.getPosicion().mulAdd(tempDir, 2f * delta);
                player.getPosicion().mulAdd(tempDir, -2f * delta);

                // Daño por contacto (el delta evita que muera en 1 frame)
                if (player.receiveDamage(enemy.getDanyo() * delta)) {
                    effectManager.startDamageFlash(player);
                }
            }
        }
    }
}
