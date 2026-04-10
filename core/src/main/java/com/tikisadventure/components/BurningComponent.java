package com.tikisadventure.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.BurningStatus;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class BurningComponent implements Component {
    private final float damagePerTick;
    private final float interval;
    private final float duration;

    public BurningComponent(float damagePerTick, float interval, float duration) {
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}

    @Override
    public void onHit(Entity target) {
        target.getStatusManager().addStatus(new BurningStatus(damagePerTick, interval, duration), target);
    }
}
