package com.tikisadventure.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.effects.FloatingText;
import com.tikisadventure.systems.events.DamageEvent;
import com.tikisadventure.systems.events.EventListener;
import com.tikisadventure.systems.events.EventBus;

public class CombatFeedbackSystem implements EventListener<DamageEvent> {
    private final Pool<FloatingText> pool;
    private final Array<FloatingText> activeTexts;

    public CombatFeedbackSystem() {
        this.pool = new Pool<FloatingText>() {
            @Override
            protected FloatingText newObject() {
                return new FloatingText();
            }
        };
        this.activeTexts = new Array<>();
        
        EventBus.subscribe(DamageEvent.class, this);
    }

    @Override
    public void onEvent(DamageEvent event) {
        FloatingText ft = pool.obtain();
        ft.init(event.entity.getPosicion().x, event.entity.getPosicion().y + 1.0f, event.damage, event.isCritical);
        activeTexts.add(ft);
    }

    public void update(float delta) {
        for (int i = activeTexts.size - 1; i >= 0; i--) {
            FloatingText ft = activeTexts.get(i);
            ft.update(delta);
            if (!ft.active) {
                pool.free(ft);
                activeTexts.removeIndex(i);
            }
        }
    }

    public void render(Batch batch) {
        for (FloatingText ft : activeTexts) {
            ft.render(batch);
        }
    }

    public void dispose() {
        EventBus.unsubscribe(DamageEvent.class, this);
    }
}