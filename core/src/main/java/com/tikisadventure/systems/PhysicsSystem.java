package com.tikisadventure.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.floors.FloorManager;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.combat.DamageType;

public class PhysicsSystem {
    private static class PhysicsConfig {
        public float strength = 3f;
        public float push = 6f;
    }

    private final FloorManager floorManager;
    private final Vector2 tempVec = new Vector2();
    private PhysicsConfig config;

    public PhysicsSystem(FloorManager floorManager) {
        this.floorManager = floorManager;
        loadConfig();
    }

    private void loadConfig() {
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(Gdx.files.internal("data/physics_config.json"));

        JsonValue enemySep = root.get("enemySeparation");
        JsonValue playerCol = root.get("playerCollision");

        config = new PhysicsConfig();
        if (enemySep != null) config.strength = enemySep.getFloat("strength", 3f);
        if (playerCol != null) config.push = playerCol.getFloat("push", 6f);
    }

    public void resolveWallCollision(Entity entity, float halfSize) {
        float x = entity.getPosition().x;
        float y = entity.getPosition().y;

        if (floorManager.isWall(x - halfSize, y)) entity.getPosition().x = (float)Math.floor(x - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x + halfSize, y)) entity.getPosition().x = (float)Math.floor(x + halfSize) - halfSize;
        if (floorManager.isWall(x, y - halfSize)) entity.getPosition().y = (float)Math.floor(y - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x, y + halfSize)) entity.getPosition().y = (float)Math.floor(y + halfSize) - halfSize;
    }

    public void resolveWallCollisionWithBounce(Entity entity, float halfSize) {
        boolean hitWall = false;
        float x = entity.getPosition().x;
        float y = entity.getPosition().y;

        float bounceX = 0;
        float bounceY = 0;

        if (floorManager.isWall(x - halfSize, y)) {
            entity.getPosition().x = (float)Math.floor(x - halfSize) + 1 + halfSize;
            bounceX = 1;
            hitWall = true;
        }
        if (floorManager.isWall(x + halfSize, y)) {
            entity.getPosition().x = (float)Math.floor(x + halfSize) - halfSize;
            bounceX = -1;
            hitWall = true;
        }
        if (floorManager.isWall(x, y - halfSize)) {
            entity.getPosition().y = (float)Math.floor(y - halfSize) + 1 + halfSize;
            bounceY = 1;
            hitWall = true;
        }
        if (floorManager.isWall(x, y + halfSize)) {
            entity.getPosition().y = (float)Math.floor(y + halfSize) - halfSize;
            bounceY = -1;
            hitWall = true;
        }

        if (hitWall && entity instanceof com.tikisadventure.entities.enemies.ConfigurableEnemy) {
            com.tikisadventure.entities.enemies.ConfigurableEnemy configEnemy =
                (com.tikisadventure.entities.enemies.ConfigurableEnemy) entity;
            if (configEnemy.hasPouncingBehavior() && configEnemy.getBehavior() instanceof com.tikisadventure.enemies.behavior.PouncingBounceBehavior) {
                Vector2 bounceDir = new Vector2(bounceX, bounceY);
                if (bounceDir.len() > 0) {
                    ((com.tikisadventure.enemies.behavior.PouncingBounceBehavior) configEnemy.getBehavior()).triggerBounce(bounceDir);
                }
            }
        }
    }

    public void resolveEnemySeparation(Array<Entity> enemies, float delta) {
        for (int i = 0; i < enemies.size; i++) {
            Entity a = enemies.get(i);
            for (int j = i + 1; j < enemies.size; j++) {
                Entity b = enemies.get(j);
                float dist = a.getPosition().dst(b.getPosition());
                float minDist = a.getHitboxActionTrigger().radius + b.getHitboxActionTrigger().radius;
                if (dist < minDist && dist > 0) {
                    tempVec.set(b.getPosition()).sub(a.getPosition()).nor();
                    float force = (minDist - dist) * config.strength * delta;
                    a.getPosition().mulAdd(tempVec, -force);
                    b.getPosition().mulAdd(tempVec, force);
                }
            }
        }
    }

    public boolean resolvePlayerCollision(Player player, Array<Entity> enemies, float delta, float damageCooldown) {
        boolean tookDamage = false;

        for (Entity enemy : enemies) {
            float dist = enemy.getPosition().dst(player.getPosition());
            float minDist = enemy.getHitboxActionTrigger().radius + player.getHitboxActionTrigger().radius;

            if (dist < minDist && dist > 0) {
                tempVec.set(player.getPosition()).sub(enemy.getPosition()).nor();
                float force = (minDist - dist) * config.push * delta;
                player.getPosition().mulAdd(tempVec, force);

                boolean isPouncingBouncing = false;
                if (enemy instanceof com.tikisadventure.entities.enemies.ConfigurableEnemy) {
                    com.tikisadventure.entities.enemies.ConfigurableEnemy configEnemy =
                        (com.tikisadventure.entities.enemies.ConfigurableEnemy) enemy;
                    if (configEnemy.hasPouncingBehavior() && configEnemy.getBehavior() instanceof com.tikisadventure.enemies.behavior.PouncingBounceBehavior) {
                        com.tikisadventure.enemies.behavior.PouncingBounceBehavior pounceBehavior =
                            (com.tikisadventure.enemies.behavior.PouncingBounceBehavior) configEnemy.getBehavior();

                        if (pounceBehavior.getCurrentState() == com.tikisadventure.enemies.behavior.PouncingBounceBehavior.PounceState.BOUNCING ||
                            pounceBehavior.getCurrentState() == com.tikisadventure.enemies.behavior.PouncingBounceBehavior.PounceState.POUNCING) {
                            Vector2 bounceDir = new Vector2(-tempVec.x, -tempVec.y);
                            pounceBehavior.triggerBounce(bounceDir);
                            isPouncingBouncing = true;
                        }
                    }
                }

                // --- MODIFICADO: DAÑO DE COLISIÓN REMOVIDO PARA ENEMIGOS CON ARMAS ---
                // Solo infligiremos daño por choque físico ("body block") si el enemigo NO tiene
                // una animación/lógica de ataque cuerpo a cuerpo (ej. los slimes base).
                if (!isPouncingBouncing && damageCooldown <= 0) {
                    if (!player.isImmune()) {
                        boolean hasMeleeAttack = false;
                        if (enemy instanceof com.tikisadventure.entities.enemies.ConfigurableEnemy) {
                            com.tikisadventure.entities.enemies.ConfigurableEnemy ce = (com.tikisadventure.entities.enemies.ConfigurableEnemy) enemy;
                            // Si es Chaser y tiene un rango de ataque superior a tocarte, asume que usa un arma.
                            if (ce.getBehavior() instanceof com.tikisadventure.enemies.behavior.ChaserBehavior ||
                                ce.getBehavior() instanceof com.tikisadventure.enemies.behavior.BombBehavior) {
                                if (ce.getBehavior().getAttackRange() > 0.5f) {
                                    hasMeleeAttack = true;
                                }
                            }
                        }

                        // Solo choca y daña si es un enemigo tonto sin arma (slime)
                        if (!hasMeleeAttack) {
                            player.receiveDamage(enemy.getDamage(), false, DamageType.KINETIC);
                            tookDamage = true;
                        }
                    }
                }
            }
        }
        return tookDamage;
    }

    public void dispose() {
        config = null;
    }
}
