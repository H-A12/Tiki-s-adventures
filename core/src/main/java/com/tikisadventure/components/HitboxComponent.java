package com.tikisadventure.components;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class HitboxComponent implements Component {
    public final Circle eventTrigger = new Circle();
    public final Circle actionTrigger = new Circle();

    public HitboxComponent(float eventRadius, float actionRadius) {
        eventTrigger.radius = eventRadius;
        actionTrigger.radius = actionRadius;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (owner instanceof Entity) {
            Entity entity = (Entity) owner;
            eventTrigger.setPosition(entity.getPosicion().x, entity.getPosicion().y);
            actionTrigger.setPosition(entity.getPosicion().x, entity.getPosicion().y);
        }
    }
}
