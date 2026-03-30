package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.base.Component;

public class PenetrationComponent implements Component {

    private Vector2 startPos = new Vector2();
    private float maxRange = 25f;
    private int maxPenetrations;
    private Array<Entity> hitEntities = new Array<>();

    public PenetrationComponent(int maxPenetrations) {
        this.maxPenetrations = maxPenetrations;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof HasPosition) || !(owner instanceof HasDirection) ||
            !(owner instanceof HasSpeed) || !(owner instanceof HasRadius) ||
            !(owner instanceof HasDamage) || !(owner instanceof Killable)) {
            return;
        }

        HasPosition posInterface = (HasPosition) owner;
        HasDirection dirInterface = (HasDirection) owner;
        HasSpeed speedInterface = (HasSpeed) owner;
        HasRadius radiusInterface = (HasRadius) owner;
        HasDamage damageInterface = (HasDamage) owner;
        Killable killable = (Killable) owner;

        if (!killable.isAlive()) return;

        if (startPos.isZero()) startPos.set(posInterface.getPosition());

        posInterface.getPosition().mulAdd(dirInterface.getDirection(), speedInterface.getSpeed() * delta);

        if (posInterface.getPosition().dst2(startPos) > maxRange * maxRange) {
            killable.die();
            return;
        }

        for (Entity e : entities) {
            if (!e.isAlive() || hitEntities.contains(e, true)) continue;

            float hitRadius = radiusInterface.getRadius() + e.getHitboxActionTrigger().radius;

            if (posInterface.getPosition().dst2(e.getPosicion()) <= hitRadius * hitRadius) {
                e.receiveDamage(damageInterface.getDamage());
                hitEntities.add(e);
                maxPenetrations--;

                if (maxPenetrations < 0) {
                    killable.die();
                    return;
                }
            }
        }
    }

    @Override
    public void onAttach(Object owner) {
        startPos.setZero();
        hitEntities.clear();
    }
}
