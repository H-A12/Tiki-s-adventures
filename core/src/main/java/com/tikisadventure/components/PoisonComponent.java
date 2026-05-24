package com.tikisadventure.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.PoisonStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

//Añadir estado de veneno al enemigo al golpear
public class PoisonComponent implements Component {
    private EffectManager effectManager;
    private final float damagePerTick;
    private final float interval;
    private final float duration;

    public PoisonComponent(EffectManager effectManager, float damagePerTick, float interval, float duration) {
        this.effectManager = effectManager;
        this.damagePerTick = damagePerTick;
        this.interval = interval;
        this.duration = duration;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}

    @Override
    public void onHit(Entity target) {
        target.getStatusManager().addStatus(new PoisonStatus(effectManager, damagePerTick, interval, duration), target);
    }

    @Override
    public void dispose() {
        effectManager = null;
    }
}
