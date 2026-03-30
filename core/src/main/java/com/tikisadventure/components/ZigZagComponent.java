package com.tikisadventure.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class ZigZagComponent implements Component {

    private float amplitude = 40f;
    private float frequency = 40f;
    private final Vector2 sideStep = new Vector2();

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof HasDirection) || !(owner instanceof HasPosition) ||
            !(owner instanceof Timed)) {
            return;
        }

        HasDirection dirInterface = (HasDirection) owner;
        HasPosition posInterface = (HasPosition) owner;
        Timed timed = (Timed) owner;

        sideStep.set(-dirInterface.getDirection().y, dirInterface.getDirection().x);

        float lateralSpeed = MathUtils.cos(timed.getStateTime() * frequency) * amplitude;

        posInterface.getPosition().mulAdd(sideStep, lateralSpeed * delta);
    }
}
