package com.tikisadventure.systems;

public class ExperienceSystem {

    private int level = 1;
    private int currentXP = 0;
    private int xpToNextLevel = 10;

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

        System.out.println("LEVEL UP! Nivel actual: " + level);
    }

    private int calculateNextLevelXP(int level) {
        return 10 + (level - 1) * 5;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentXP() {
        return currentXP;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }

    public float getXPPercent() {
        return (float) currentXP / xpToNextLevel;
    }
}
