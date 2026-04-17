package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class BounceComponent implements Component {
    private int remainingBounces;
    private Projectile projectile;

    public BounceComponent(int maxBounces) {
        this.remainingBounces = maxBounces;
    }

    @Override
    public void onAttach(Object owner) {
        if (owner instanceof Projectile) {
            this.projectile = (Projectile) owner;
        }
    }

    @Override
    public void onHit(Entity target) {
        if (projectile != null && remainingBounces > 0 && projectile.canPenetrate()) {
            Vector2 projPos = projectile.getPosition();
            Vector2 targetPos = target.getPosition();
            
            // Calculate normal vector (normalized vector from target to projectile)
            Vector2 normal = new Vector2(projPos).sub(targetPos).nor();
            
            // Reflect direction: R = I - 2 * (I . N) * N
            Vector2 dir = projectile.getDirection();
            float dot = dir.dot(normal);
            dir.sub(normal.scl(2 * dot));
            projectile.setDirection(dir);
            
            projectile.clearHitTimes();
            remainingBounces--;
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}
