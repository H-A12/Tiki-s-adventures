package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import java.util.HashSet;
import java.util.Set;

//Hacer que un proyectil salte al enemigo más cercano al golpear
public class ChainHitComponent implements Component {
    private int remainingBounces;
    private float searchRadius;
    private Projectile projectile;
    private Entity lastHitTarget;
    private Set<Entity> hitTargets = new HashSet<>();
    private Array<Entity> cachedEntities;

    public ChainHitComponent(int maxBounces, float searchRadius) {
        this.remainingBounces = maxBounces;
        this.searchRadius = searchRadius;
    }

    //Comprobar si ya golpeó a este objetivo
    public boolean hasHitTarget(Entity target) {
        return hitTargets.contains(target);
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

        if (hitTargets.contains(target)) {
            return;
        }

        hitTargets.add(target);
        lastHitTarget = target;

        executeChain();
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (projectile == null) return;
        cachedEntities = entities;
    }

    private void executeChain() {
        if (cachedEntities == null) return;

        Entity nearestEnemy = null;
        float minDist = Float.MAX_VALUE;
        Vector2 projPos = projectile.getPosition();

        for (Entity entity : cachedEntities) {
            if (!entity.isAlive()) continue;
            if (hitTargets.contains(entity)) continue;

            float dist = entity.getPosition().dst(projPos);
            if (dist < searchRadius && dist < minDist && entity != projectile.getOwner()) {
                minDist = dist;
                nearestEnemy = entity;
            }
        }

        if (nearestEnemy != null) {
            Vector2 newDirection = new Vector2(nearestEnemy.getPosition()).sub(projPos).nor();
            projectile.setDirection(newDirection);
            projectile.getPosition().mulAdd(newDirection, 0.5f);
            projectile.clearHitTimes();
            remainingBounces--;
        } else {
            projectile.setPenetration(0);
        }
    }
}
