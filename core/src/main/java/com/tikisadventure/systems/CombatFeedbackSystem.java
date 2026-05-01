package com.tikisadventure.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.effects.FloatingText;
import com.tikisadventure.systems.events.DamageEvent;
import com.tikisadventure.systems.events.EventListener;
import com.tikisadventure.systems.events.EventBus;

public class CombatFeedbackSystem implements EventListener<DamageEvent> {
    private final Pool<FloatingText> pool;
    private final Array<FloatingText> activeTexts;
    private final ObjectMap<DamageType, Color> typeColors = new ObjectMap<>();

    public CombatFeedbackSystem() {
        this.pool = new Pool<FloatingText>() {
            @Override
            protected FloatingText newObject() {
                return new FloatingText();
            }
        };
        this.activeTexts = new Array<>();

        typeColors.put(DamageType.KINETIC, Color.WHITE);
        typeColors.put(DamageType.ENERGY, Color.PINK);
        typeColors.put(DamageType.EXPLOSIVE, Color.ORANGE);
        typeColors.put(DamageType.FIRE, Color.RED);
        typeColors.put(DamageType.POISON, Color.LIME);
        typeColors.put(DamageType.ICE, Color.CYAN);

        EventBus.subscribe(DamageEvent.class, this);
    }

    @Override
    public void onEvent(DamageEvent event) {
        FloatingText ft = pool.obtain();
        Color color = typeColors.get(event.damageType, Color.WHITE);
        ft.init(event.entity.getPosition().x, event.entity.getPosition().y + 1.0f, event.damage, event.isCritical, color);
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
