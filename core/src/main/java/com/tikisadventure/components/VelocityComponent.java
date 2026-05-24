package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

//Guardar la velocidad de movimiento y knockback de una entidad
public class VelocityComponent implements Component {
    public final Vector2 velocidad = new Vector2();
    public final Vector2 knockbackVelocity = new Vector2();
    public float speed;

    public VelocityComponent(float speed) {
        this.speed = speed;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}
