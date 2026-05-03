package com.tikisadventure.systems.events;

public class ControllerConnectedEvent implements Event {
    public final String message;

    public ControllerConnectedEvent(String message) {
        this.message = message;
    }
}
