package com.tikisadventure.systems;

import com.tikisadventure.systems.events.OrbCollectedEvent;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.EventListener;

public class ExperienceSystem implements EventListener<OrbCollectedEvent> {

    private int level = 1;
    private int currentXP = 0;
    private int xpToNextLevel = 10;
    private com.tikisadventure.entities.player.Player player;


    public ExperienceSystem(com.tikisadventure.entities.player.Player player) {
        this.player = player;
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
        com.tikisadventure.screens.GameScreen.isGamePaused = true;
        level++;
        xpToNextLevel = calculateNextLevelXP(level);

        if (player == null) return;

        float healthIncreaseFactor = 1.02f;
        float damageIncreaseFactor = 1.05f;

        com.tikisadventure.components.HealthComponent health = player.getHealthComponent();
        if (health != null) {
            health.maxHealth *= healthIncreaseFactor;
            health.currentHealth += health.maxHealth/6; //Curamos un  pequenyo % de la vida maxima al subir de nivel
            if(health.currentHealth > health.maxHealth) health.currentHealth = health.maxHealth;
        }

        com.tikisadventure.combat.weapons.WeaponManager weaponManager = player.getWeaponFactory();
        if (weaponManager != null) {
            for (com.tikisadventure.combat.weapons.Weapon weapon : weaponManager.getWeapons()) {
                float oldDamage = weapon.getDamage();
                weapon.setDamage(oldDamage * damageIncreaseFactor);
            }
        }
    }



    private int calculateNextLevelXP(int level) {
        return 10 + (level - 1) * 15;
    }

    public int getLevel() { return level; }
    public int getCurrentXP() { return currentXP; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public float getXPPercent() { return (float) currentXP / xpToNextLevel; }
}
