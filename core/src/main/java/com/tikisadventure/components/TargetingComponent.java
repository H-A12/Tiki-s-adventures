package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class TargetingComponent implements Component {
    private Entity target;
    private float range;

    public TargetingComponent(float range) {
        this.range = range;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof HasPosition)) return;

        HasPosition hasPosition = (HasPosition) owner;
        Vector2 ownerPos = hasPosition.getPosition();

        if (target == null || !target.isAlive() || 
            ownerPos.dst2(target.getPosicion()) > range * range) {
            target = findClosestEnemy(ownerPos, entities);
        }
    }

    private Entity findClosestEnemy(Vector2 position, Array<Entity> entities) {
        Entity closest = null;
        float minDistance = Float.MAX_VALUE;

        for (Entity e : entities) {
            if (!e.isAlive()) continue;

            float distanceSq = position.dst2(e.getPosicion());
            if (distanceSq < minDistance && distanceSq <= range * range) {
                minDistance = distanceSq;
                closest = e;
            }
        }

        return closest;
    }

    public Entity getTarget() {
        return target;
    }

    public void clearTarget() {
        target = null;
    }
}
