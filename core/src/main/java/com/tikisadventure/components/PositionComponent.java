package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class PositionComponent implements Component {
    public final Vector2 posicion = new Vector2();

    public PositionComponent(float x, float y) {
        posicion.set(x, y);
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}
