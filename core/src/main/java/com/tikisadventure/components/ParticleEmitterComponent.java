package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class ParticleEmitterComponent implements Component {
    private EffectManager effectManager;
    private final String type;
    private final Vector2 offset;
    private final float interval;
    private float timer;

    private final Vector2 tempPos = new Vector2();
    private final Vector2 tempDir = new Vector2(0, 5);

    public ParticleEmitterComponent(EffectManager effectManager, String type, Vector2 offset, float interval) {
        this.effectManager = effectManager;
        this.type = type;
        this.offset = offset;
        this.interval = interval;
        this.timer = 0;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof Entity)) return;
        Entity entity = (Entity) owner;
        timer += delta;
        if (timer >= interval) {
            tempPos.set(entity.getPosition()).add(offset);
            effectManager.spawnSingleParticle(type, tempPos, tempDir, entity);
            timer = 0;
        }
    }

    @Override
    public void dispose() {
        effectManager = null;
    }
}
