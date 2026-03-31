package com.tikisadventure.systems;

import com.tikisadventure.systems.events.OrbCollectedEvent;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.EventListener;

public class ExperienceSystem implements EventListener<OrbCollectedEvent> {

    private int level = 1;
    private int currentXP = 0;
    private int xpToNextLevel = 10;

    public ExperienceSystem() {
        EventBus.subscribe(OrbCollectedEvent.class, this);
    }

    @Override
    public void onEvent(OrbCollectedEvent event) {
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
        xpToNextLevel = calculateNextLevelXP(level);
    }

    private int calculateNextLevelXP(int level) {
        return 10 + (level - 1) * 5;
    }

    public int getLevel() { return level; }
    public int getCurrentXP() { return currentXP; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public float getXPPercent() { return (float) currentXP / xpToNextLevel; }
}
