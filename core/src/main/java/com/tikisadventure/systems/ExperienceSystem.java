package com.tikisadventure.systems;

import com.tikisadventure.systems.events.OrbCollectedEvent;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.EventListener;

public class ExperienceSystem implements EventListener<OrbCollectedEvent> {

    private int level = 1;
    private int currentXP = 0;
    private int xpToNextLevel = 10;
    private com.tikisadventure.entities.player.Player player;
    private int levelsPending = 0;


    public ExperienceSystem(com.tikisadventure.entities.player.Player player) {
        this.player = player;
        EventBus.subscribe(OrbCollectedEvent.class, this);
    }


    @Override
    public void onEvent(OrbCollectedEvent event) {
        if (player == null || !player.isAlive()) return;
        addXP(event.xpAmount);
    }

    public void addXP(int amount) {
        currentXP += amount;
        while (currentXP >= xpToNextLevel) {
            currentXP -= xpToNextLevel;
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        levelsPending++;
        xpToNextLevel = calculateNextLevelXP(level);
    }


    private int calculateNextLevelXP(int level) {
        return 10 + (level - 1) * 15;
    }

    public int getLevel() { return level; }
    public int getCurrentXP() { return currentXP; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public float getXPPercent() { return (float) currentXP / xpToNextLevel; }


    public void dispose() {
        com.tikisadventure.systems.events.EventBus.unsubscribe(OrbCollectedEvent.class, this);
        this.player = null; // Limpieza

    }

    public int getLevelsPending() { return levelsPending; }
    public void consumeLevel() { if (levelsPending > 0) levelsPending--; }
}
