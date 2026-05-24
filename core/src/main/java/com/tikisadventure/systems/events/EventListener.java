package com.tikisadventure.systems.events;

//Listener generico para manejar eventos
public interface EventListener<T extends Event> {
    //Manejar evento recibido
    void onEvent(T event);
}
