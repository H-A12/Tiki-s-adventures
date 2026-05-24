package com.tikisadventure.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.SlownessStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

//Ralentizar al enemigo y hacerle daño por tiempo al golpear
public class SlownessComponent implements Component {
    private EffectManager effectManager;
    private final float speedMult;
    private final float damagePerTick;
    private final float interval;
    private final float duration;

    public SlownessComponent(EffectManager effectManager, float speedMult, float damagePerTick, float interval, float duration) {
        this.effectManager = effectManager;
        this.speedMult = speedMult;
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}

    @Override
    public void onHit(Entity target) {
        target.getStatusManager().addStatus(new SlownessStatus(effectManager, duration, speedMult, damagePerTick, interval), target);
    }

    @Override
    public void dispose() {
        effectManager = null;
    }
}