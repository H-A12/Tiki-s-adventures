package com.tikisadventure.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.effects.FloatingText;
import com.tikisadventure.core.Assets;
import com.tikisadventure.systems.events.DamageEvent;
import com.tikisadventure.systems.events.EvadeEvent;
import com.tikisadventure.systems.events.HealEvent;
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

        // --- NUEVO: Escuchador para Evasiones ---
        EventBus.subscribe(EvadeEvent.class, event -> {
            FloatingText ft = pool.obtain();
            // Usamos la imagen dodged, NO fuente, NO gravedad, SI parpadeo (3 blinks), 0.5f velocidad
            // Aumentamos scale a 2.0f para hacerlo más grande
            ft.initImage(event.entity.getPosition().x, event.entity.getPosition().y + 1.0f, Assets.dodgedRegion, Color.WHITE, 7.0f, true, 3, 0.5f);
            activeTexts.add(ft);
        });

        // --- Escuchador para Curaciones ---
        EventBus.subscribe(HealEvent.class, event -> {
            FloatingText ft = pool.obtain();
            String text = "+" + (int)event.amount;
            Color col;
            float scale;

            // Variables para que no se pisen los textos
            float offsetX = 0f;
            float offsetY = 1.0f;

            switch (event.type) {
                case PICKUP:
                    col = Color.GREEN;
                    scale = 1.0f; // Unificado a 1.0f
                    break;
                case REGEN:
                    col = Color.GREEN;
                    scale = 1.0f;
                    break;
                case LEECH:
                    col = new Color(0.6f, 0.0f, 0.0f, 1.0f); // Granate oscuro
                    scale = 1.0f;
                    // Lo movemos un poco a la derecha y un poco más alto
                    offsetX = 0.5f;
                    offsetY = 1.3f;
                    break;
                default:
                    col = Color.GREEN;
                    scale = 1.0f;
            }
            // NO usa fuente (false), NO usa gravedad (false), SI parpadeo (true), 3 blinks, 0.5f speed
            ft.init(event.entity.getPosition().x + offsetX, event.entity.getPosition().y + offsetY, text, false, col, scale, false, false, true, 3, 0.5f);
            activeTexts.add(ft);
        });
    }

    @Override
    public void onEvent(DamageEvent event) {
        FloatingText ft = pool.obtain();
        Color color = typeColors.get(event.damageType, Color.WHITE);
        // Daño normal: tamaño 1.0f, usa texturas (useFont = false), SI usa gravedad (true), NO parpadeo (false), 0 blinks, 1.0f speed
        ft.init(event.entity.getPosition().x, event.entity.getPosition().y + 1.0f, String.valueOf((int)event.damage), event.isCritical, color, 1.0f, false, true, false, 0, 1.0f);
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
