package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.floors.FloorManager;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.enemies.ConfigurableEnemy;
import com.tikisadventure.enemies.behavior.PouncingBounceBehavior;
import com.tikisadventure.combat.DamageType;

public class PhysicsSystem {
    private final FloorManager floorManager;
    private final Vector2 tempVec = new Vector2(); // Reutilizamos para evitar basura (GC)

    public PhysicsSystem(FloorManager floorManager) {
        this.floorManager = floorManager;
    }

    public void resolveWallCollision(Entity entity, float halfSize) {
        float x = entity.getPosition().x;
        float y = entity.getPosition().y;

        if (floorManager.isWall(x - halfSize, y)) entity.getPosition().x = (float)Math.floor(x - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x + halfSize, y)) entity.getPosition().x = (float)Math.floor(x + halfSize) - halfSize;
        if (floorManager.isWall(x, y - halfSize)) entity.getPosition().y = (float)Math.floor(y - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x, y + halfSize)) entity.getPosition().y = (float)Math.floor(y + halfSize) - halfSize;
    }

    public boolean resolveWallCollisionWithBounce(Entity entity, float halfSize) {
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

        if (hitWall && entity instanceof ConfigurableEnemy) {
            ConfigurableEnemy configEnemy = (ConfigurableEnemy) entity;
            if (configEnemy.hasPouncingBehavior() && configEnemy.getBehavior() instanceof PouncingBounceBehavior) {
                Vector2 bounceDir = new Vector2(bounceX, bounceY);
                if (bounceDir.len() > 0) {
                    ((PouncingBounceBehavior) configEnemy.getBehavior()).triggerBounce(bounceDir);
                }
            }
        }

        return hitWall;
    }

    public void resolveEnemySeparation(Array<Entity> enemies, float delta) {
        float strength = 3f;
        for (int i = 0; i < enemies.size; i++) {
            Entity a = enemies.get(i);
            for (int j = i + 1; j < enemies.size; j++) {
                Entity b = enemies.get(j);
                float dist = a.getPosition().dst(b.getPosition());
                float minDist = a.getHitboxActionTrigger().radius + b.getHitboxActionTrigger().radius;
                if (dist < minDist && dist > 0) {
                    tempVec.set(b.getPosition()).sub(a.getPosition()).nor();
                    float force = (minDist - dist) * strength * delta;
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

                if (enemy instanceof com.tikisadventure.entities.enemies.ConfigurableEnemy) {
                    ConfigurableEnemy configEnemy = (ConfigurableEnemy) enemy;
                    if (configEnemy.hasPouncingBehavior() && configEnemy.getBehavior() instanceof PouncingBounceBehavior) {
                        Vector2 bounceDir = new Vector2(-tempVec.x, -tempVec.y);
                        ((PouncingBounceBehavior) configEnemy.getBehavior()).triggerBounce(bounceDir);
                    } else {
                        float push = 6f;
                        float force = (minDist - dist) * push * delta;
                        player.getPosition().mulAdd(tempVec, force);
                    }
                } else {
                    float push = 6f;
                    float force = (minDist - dist) * push * delta;
                    player.getPosition().mulAdd(tempVec, force);
                }

                if (damageCooldown <= 0) {
                    player.receiveDamage(enemy.getDamage(), false, DamageType.KINETIC);
                    tookDamage = true;
                }
            }
        }
        return tookDamage;
    }
}
