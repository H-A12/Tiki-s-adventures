package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.entities.player.Player;

// Power ups de estadísticas globales
public class GlobalStatPowerUp extends PowerUp {

    public enum StatType {
        KINETIC_DMG, FIRE_DMG, POISON_DMG, EXPLOSIVE_DMG,
        CRIT_CHANCE, LUCK, MAX_HP, MAX_HP_PERCENT
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

            // ... (Añade el resto de casos cuando tengas las variables)
            default:
                Gdx.app.log("POWER UP", "Se aplicó estadística global: " + stat);
                break;
        }
    }
}
