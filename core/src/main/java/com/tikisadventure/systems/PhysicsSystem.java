package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.floors.FloorManager;
import com.tikisadventure.entities.player.Player;
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
        float push = 6f;
        boolean tookDamage = false;

        for (Entity enemy : enemies) {
            float dist = enemy.getPosition().dst(player.getPosition());
            float minDist = enemy.getHitboxActionTrigger().radius + player.getHitboxActionTrigger().radius;

            if (dist < minDist && dist > 0) {
                // Empuje siempre activo para que no se "peguen"
                tempVec.set(player.getPosition()).sub(enemy.getPosition()).nor();
                float force = (minDist - dist) * push * delta;
                player.getPosition().mulAdd(tempVec, force);

                // Solo aplicamos daño si el cooldown de la pantalla llegó a 0
                if (damageCooldown <= 0) {
                    player.receiveDamage(enemy.getDamage(), false, DamageType.KINETIC); // Default to false
                    tookDamage = true;
                }
            }
        }
        return tookDamage;
    }
}
