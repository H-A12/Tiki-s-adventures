package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.Entity;

public class TrailComponent implements Component {
    private final EffectManager effectManager;
    private final EffectType trailType;
    private final float trailInterval;
    private final Vector2 lastTrailPos = new Vector2();

    public TrailComponent(EffectManager effectManager, EffectType trailType, float interval) {
        this.effectManager = effectManager;
        this.trailType = trailType;
        this.trailInterval = interval;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (effectManager == null || trailType == null || trailInterval <= 0) return;
        if (!(owner instanceof HasPosition) || !(owner instanceof HasDirection)) return;

        HasPosition hasPosition = (HasPosition) owner;
        Vector2 currentPos = hasPosition.getPosition();

        float distMoved = currentPos.dst(lastTrailPos);

        if (distMoved >= trailInterval) {
            int count = (int) (distMoved / trailInterval);
            Vector2 tempPos = new Vector2();

            for (int i = 0; i < count; i++) {
                float t = (float) i / count;
                tempPos.set(lastTrailPos).lerp(currentPos, t);
                
                HasDirection hasDir = (HasDirection) owner;
                Vector2 dir = hasDir.getDirection();
                effectManager.spawnEffect(trailType, tempPos, new Vector2(dir).scl(-1f));
            }
            lastTrailPos.set(currentPos);
        }
    }

    @Override
    public void onAttach(Object owner) {
        if (owner instanceof HasPosition) {
            lastTrailPos.set(((HasPosition) owner).getPosition());
        }
    }
}
