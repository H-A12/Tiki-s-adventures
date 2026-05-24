package com.tikisadventure.systems.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

//Bus de eventos pub-sub estatico
public class EventBus {
    //Registro de listeners por tipo de evento
    private static final ObjectMap<Class<? extends Event>, Array<EventListener<?>>> listeners = new ObjectMap<>();

    //Suscribir listener a un tipo de evento
    public static <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        if (!listeners.containsKey(eventType)) {
            listeners.put(eventType, new Array<>());
        }
        listeners.get(eventType).add(listener);
    }

    //Desuscribir listener
    public static <T extends Event> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).removeValue(listener, true);
        }
    }

    //Publicar evento a todos los listeners suscritos
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
