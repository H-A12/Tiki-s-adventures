package com.tikisadventure.components;

import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.badlogic.gdx.utils.Array;

public class StatsComponent implements Component {
    public float damage;
    public int experience;
    public int scoreValue;

    public StatsComponent(float damage, int experience, int scoreValue) {
        this.damage = damage;
        this.experience = experience;
        this.scoreValue = scoreValue;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}
