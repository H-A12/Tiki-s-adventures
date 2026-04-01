package com.tikisadventure.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class PenetrationComponent implements Component {
    private int remainingPenetrations;

    public PenetrationComponent(int maxPenetrations) {
        this.remainingPenetrations = maxPenetrations;
    }

    public boolean canPenetrate() {
        return remainingPenetrations > 0;
    }

    public void reducePenetration() {
        remainingPenetrations--;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}
