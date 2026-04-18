package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class ChainHitComponent implements Component {
    private int remainingBounces;
    private float searchRadius;
    private Projectile projectile;
    private Array<Entity> cachedEnemies;
    private boolean pendingChain;
    private Entity lastHitTarget;

    public ChainHitComponent(int maxBounces, float searchRadius) {
        this.remainingBounces = maxBounces;
        this.searchRadius = searchRadius;
    }

    @Override
    public void onAttach(Object owner) {
        if (owner instanceof Projectile) {
            this.projectile = (Projectile) owner;
        }
    }

    @Override
    public void onHit(Entity target) {
        if (projectile == null || remainingBounces <= 0 || !projectile.canPenetrate()) {
            return;
        }
        lastHitTarget = target;
        pendingChain = true;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!pendingChain || projectile == null) return;
        cachedEnemies = entities;
        executeChain();
        pendingChain = false;
    }

    private Entity executeChain() {
        if (cachedEnemies == null) return null;

        Entity nearestEnemy = null;
        float minDist = Float.MAX_VALUE;
        Vector2 projPos = projectile.getPosition();

        for (Entity entity : cachedEnemies) {
            if (!entity.isAlive()) continue;
            if (entity == lastHitTarget) continue;
            float dist = entity.getPosition().dst(projPos);
            if (dist < searchRadius && dist < minDist && entity != projectile.getOwner()) {
                minDist = dist;
                nearestEnemy = entity;
            }
        }

        if (nearestEnemy != null) {
            Vector2 direction = new Vector2(nearestEnemy.getPosition()).sub(projPos).nor();
            projectile.setDirection(direction);
            projectile.clearHitTimes();
            remainingBounces--;
            return nearestEnemy;
        }
        return null;
    }
}