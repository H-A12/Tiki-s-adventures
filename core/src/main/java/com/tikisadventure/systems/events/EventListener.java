package com.tikisadventure.systems.events;

public interface EventListener<T extends Event> {
    void onEvent(T event);
}
