package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class ChainLightning {

    private Array<Vector2> hitPositions;
    private int maxBounces;
    private int currentBounces;
    private float damage;
    private float reducedDamage;
    private float bounceDelay;
    private float currentDelay;
    private boolean active;
    private float lifetime;
    private float maxLifetime = 4.0f;
    private Entity lastHit;
    private Vector2 startPosition;

    public ChainLightning(Vector2 startPos, Entity firstTarget, float damage, int bounces, float bounceDelay) {
        this.hitPositions = new Array<>();
        this.startPosition = startPos.cpy();
        this.maxBounces = bounces;
        this.currentBounces = 0;
        this.damage = damage;
        this.reducedDamage = damage * 0.8f;
        this.bounceDelay = bounceDelay;
        this.currentDelay = 0;
        this.active = true;
        this.lifetime = 0;

        if (firstTarget != null) {
            firstTarget.receiveDamage(damage);
            hitPositions.add(firstTarget.getPosicion().cpy());
            lastHit = firstTarget;
            currentBounces = 1;
        }
    }

    public void update(float delta, Array<Entity> enemies) {
        if (!active) return;

        lifetime += delta;

        if (lifetime >= maxLifetime) {
            active = false;
            return;
        }

        // Si ya llegamos al máximo de rebotes, esperar a que termine el tiempo
        if (currentBounces >= maxBounces) {
            return; // No desactivar, esperar a que pase el tiempo
        }

        if (lastHit != null) {
            currentDelay += delta;

            if (currentDelay >= bounceDelay) {
                currentDelay = 0;

                Entity nextTarget = findNearestEnemy(lastHit.getPosicion(), enemies, lastHit);

                if (nextTarget != null) {
                    nextTarget.receiveDamage(reducedDamage);
                    hitPositions.add(nextTarget.getPosicion().cpy());
                    lastHit = nextTarget;
                    currentBounces++;
                }
                // Si no hay más enemigos, no hacer nada - esperar a que pase el tiempo
            }
        }
    }

    private Entity findNearestEnemy(Vector2 from, Array<Entity> enemies, Entity exclude) {
        Entity closest = null;
        float minDist = Float.MAX_VALUE;

        for (Entity e : enemies) {
            if (!e.isAlive()) continue;
            if (e == exclude) continue;

            float dist = from.dst2(e.getPosicion());
            if (dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }

        return closest;
    }

    public boolean isActive() {
        return active;
    }

    public Array<Vector2> getHitPositions() {
        return hitPositions;
    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public Entity getLastHit() {
        return lastHit;
    }
}
