package com.tikisadventure.systems.events;

public class OrbCollectedEvent implements Event {
    public final int xpAmount;

    public OrbCollectedEvent(int xpAmount) {
        this.xpAmount = xpAmount;
    }
}
