package com.tikisadventure.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.ProjectileImpactEvent;

public class MovementComponent implements Component {
    private float maxRange;
    private Vector2 startPos = new Vector2();

    public MovementComponent(float maxRange) {
        this.maxRange = maxRange;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof HasPosition) || 
            !(owner instanceof HasDirection) ||
            !(owner instanceof HasSpeed) ||
            !(owner instanceof Killable)) {
            return;
        }

        HasPosition posInterface = (HasPosition) owner;
        HasDirection dirInterface = (HasDirection) owner;
        HasSpeed speedInterface = (HasSpeed) owner;
        Killable killable = (Killable) owner;
        HasRadius radiusInterface = (owner instanceof HasRadius) ? (HasRadius) owner : null;
        HasDamage damageInterface = (owner instanceof HasDamage) ? (HasDamage) owner : null;
        Sensorable sensorInterface = (owner instanceof Sensorable) ? (Sensorable) owner : null;

        Vector2 pos = posInterface.getPosition();
        Vector2 dir = dirInterface.getDirection();
        float speed = speedInterface.getSpeed();

        if (startPos.isZero()) {
            startPos.set(pos);
        }

        pos.mulAdd(dir, speed * delta);

        if (pos.dst2(startPos) > maxRange * maxRange) {
            killable.die();
            return;
        }

        if (sensorInterface != null && sensorInterface.isSensorMode()) {
            return;
        }

        if (radiusInterface == null || damageInterface == null) {
            return;
        }

        float hitRadius = radiusInterface.getRadius();
        float damage = damageInterface.getDamage();

        for (Entity e : entities) {
            if (!e.isAlive()) continue;

            float enemyRadius = e.getHitboxActionTrigger().radius;
            float totalRadius = hitRadius + enemyRadius;

            if (pos.dst2(e.getPosicion()) <= totalRadius * totalRadius) {
                e.receiveDamage(damage);
                
                if (owner instanceof Projectile) {
                    EventBus.publish(new ProjectileImpactEvent((Projectile) owner, e));
                }
                
                killable.die();
                return;
            }
        }
    }

    @Override
    public void onAttach(Object owner) {
        startPos.setZero();
    }
}
