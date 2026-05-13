package com.tikisadventure.systems.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class EventBus {
    private static final ObjectMap<Class<? extends Event>, Array<EventListener<?>>> listeners = new ObjectMap<>();

    public static <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        if (!listeners.containsKey(eventType)) {
            listeners.put(eventType, new Array<>());
        }
        listeners.get(eventType).add(listener);
    }

    public static <T extends Event> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).removeValue(listener, true);
        }
    }

    public static <T extends Event> void publish(T event) {
        Array<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                @SuppressWarnings("unchecked")
                EventListener<T> typedListener = (EventListener<T>) listener;
                typedListener.onEvent(event);
            }
        }
    }
}
