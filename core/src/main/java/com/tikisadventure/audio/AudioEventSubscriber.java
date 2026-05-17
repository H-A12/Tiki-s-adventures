package com.tikisadventure.audio;

import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.events.DamageEvent;
import com.tikisadventure.systems.events.EntityDiedEvent;
import com.tikisadventure.systems.events.EvadeEvent;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.EventListener;
import com.tikisadventure.systems.events.FiredEvent;
import com.tikisadventure.systems.events.HealEvent;
import com.tikisadventure.systems.events.HitEvent;
import com.tikisadventure.systems.events.OrbCollectedEvent;

public class AudioEventSubscriber {
    private static boolean initialized = false;

    private static EventListener<FiredEvent> firedListener;
    private static EventListener<HitEvent> hitListener;
    private static EventListener<DamageEvent> damageListener;
    private static EventListener<EntityDiedEvent> entityDiedListener;
    private static EventListener<HealEvent> healListener;
    private static EventListener<OrbCollectedEvent> orbListener;
    private static EventListener<EvadeEvent> evadeListener;

    public static void init() {
        if (initialized) return;

        firedListener = event -> AudioManager.playSFX(AudioType.SHOOT);
        EventBus.subscribe(FiredEvent.class, firedListener);

        hitListener = event -> AudioManager.playSFX(AudioType.HIT);
        EventBus.subscribe(HitEvent.class, hitListener);

        damageListener = event -> {
            if (event.entity instanceof Player) {
                AudioManager.playSFX(AudioType.HURT);
            }
        };
        EventBus.subscribe(DamageEvent.class, damageListener);

        entityDiedListener = event -> {
            if (!(event.entity instanceof Player)) {
                AudioManager.playSFX(AudioType.ENEMY_DIE);
            }
        };
        EventBus.subscribe(EntityDiedEvent.class, entityDiedListener);

        healListener = event -> AudioManager.playSFX(AudioType.HEAL);
        EventBus.subscribe(HealEvent.class, healListener);

        orbListener = event -> AudioManager.playSFX(AudioType.XP_PICKUP);
        EventBus.subscribe(OrbCollectedEvent.class, orbListener);

        evadeListener = event -> AudioManager.playSFX(AudioType.DODGE);
        EventBus.subscribe(EvadeEvent.class, evadeListener);

        initialized = true;
        com.badlogic.gdx.Gdx.app.log("AudioEventSubscriber", "Subscribed to EventBus");
    }

    public static void dispose() {
        if (!initialized) return;
        if (firedListener != null) EventBus.unsubscribe(FiredEvent.class, firedListener);
        if (hitListener != null) EventBus.unsubscribe(HitEvent.class, hitListener);
        if (damageListener != null) EventBus.unsubscribe(DamageEvent.class, damageListener);
        if (entityDiedListener != null) EventBus.unsubscribe(EntityDiedEvent.class, entityDiedListener);
        if (healListener != null) EventBus.unsubscribe(HealEvent.class, healListener);
        if (orbListener != null) EventBus.unsubscribe(OrbCollectedEvent.class, orbListener);
        if (evadeListener != null) EventBus.unsubscribe(EvadeEvent.class, evadeListener);
        initialized = false;
    }
}
