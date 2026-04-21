package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.entities.player.Player;

// Power ups de estadísticas globales
public class GlobalStatPowerUp extends PowerUp {

    public enum StatType {
        KINETIC_DMG, FIRE_DMG, POISON_DMG, EXPLOSIVE_DMG,
        CRIT_CHANCE, LUCK, MAX_HP, MAX_HP_PERCENT, XP_GAIN_PERCENT, SPEED
    }

    private StatType stat;
    private float amount;

    public GlobalStatPowerUp(String name, String desc, Rarity rarity, StatType stat, float amount) {
        super(name, desc, rarity);
        this.stat = stat;
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {

        switch (stat) {
            case MAX_HP:
                if (player.getHealthComponent() != null) {
                    player.getHealthComponent().maxHealth += amount;
                    player.getHealthComponent().currentHealth += amount;
                }
                break;

            case MAX_HP_PERCENT:
                if (player.getHealthComponent() != null) {

                    float bonusHp = player.getHealthComponent().maxHealth * amount;

                    player.getHealthComponent().maxHealth += bonusHp;

                    player.getHealthComponent().currentHealth += bonusHp;

                    Gdx.app.log("POWER UP", "Vida máxima aumentada en " + bonusHp + " (+" + (amount * 100) + "%). Nuevo máximo: " + player.getHealthComponent().maxHealth);
                }
                break;

            case LUCK:
                // Sumamos la suerte a una variable del jugador (tendrás que crear esta variable en Player.java)
                player.setLuck(player.getLuck() + amount);
                Gdx.app.log("POWER UP", "Suerte aumentada. Nueva suerte: " + player.getLuck());
                break;

            case EXPLOSIVE_DMG:
                Gdx.app.log("POWER UP", "Daño explosivo aumentado un " + (amount * 100) + "%");
                break;

            case XP_GAIN_PERCENT:
                player.setXpMultiplier(player.getXpMultiplier() + amount);
                Gdx.app.log("POWER UP", "Ganancia de XP aumentada. Nuevo multiplicador: " + player.getXpMultiplier());
                break;

            case SPEED:
                player.addSpeedPercent(amount);
                Gdx.app.log("POWER UP", "Velocidad aumentada un " + (amount * 100) + "%. Nueva velocidad: " + player.getSpeed());
                break;

            // ... (Añade el resto de casos cuando tengas las variables)
            default:
                Gdx.app.log("POWER UP", "Se aplicó estadística global: " + stat);
                break;
        }
    }

    public StatType getStatType() {
        return this.stat;
    }
}
