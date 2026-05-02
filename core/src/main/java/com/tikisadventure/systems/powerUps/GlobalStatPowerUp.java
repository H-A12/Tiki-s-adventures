package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.entities.player.Player;

// Power ups de estadísticas globales
public class GlobalStatPowerUp extends PowerUp {

    public enum StatType {
        KINETIC_DMG, FIRE_DMG, POISON_DMG, EXPLOSIVE_DMG, ICE_DMG, ENERGY_DMG,
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
                    player.addExtraHealthGained(amount);
                    player.getHealthComponent().maxHealth += amount;
                    player.getHealthComponent().currentHealth += amount;
                }
                break;

            case MAX_HP_PERCENT:
                if (player.getHealthComponent() != null) {
                    float bonusHp = player.getHealthComponent().maxHealth * amount;
                    player.addExtraHealthGained(bonusHp);
                    player.getHealthComponent().maxHealth += bonusHp;
                    player.getHealthComponent().currentHealth += bonusHp;
                    Gdx.app.log("POWER UP", "Vida máxima aumentada en " + bonusHp + " (+" + (amount * 100) + "%). Nuevo máximo: " + player.getHealthComponent().maxHealth);
                }
                break;

            case LUCK:
                player.setLuck(player.getLuck() + amount);
                Gdx.app.log("POWER UP", "Suerte aumentada. Nueva suerte: " + player.getLuck());
                break;

            case KINETIC_DMG:
                player.addKineticDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño cinético aumentado un " + (amount * 100) + "%. Total: " + (player.getKineticDamageBonus() * 100) + "%");
                break;

            case FIRE_DMG:
                player.addFireDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de fuego aumentado un " + (amount * 100) + "%. Total: " + (player.getFireDamageBonus() * 100) + "%");
                break;

            case EXPLOSIVE_DMG:
                player.addExplosiveDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño explosivo aumentado un " + (amount * 100) + "%. Total: " + (player.getExplosiveDamageBonus() * 100) + "%");
                break;

            case POISON_DMG:
                player.addPoisonDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de veneno aumentado un " + (amount * 100) + "%. Total: " + (player.getPoisonDamageBonus() * 100) + "%");
                break;

            case CRIT_CHANCE:
                player.addCritChanceBonus(amount);
                Gdx.app.log("POWER UP", "Probabilidad de crítico aumentada. Total: " + (player.getCritChanceBonus() * 100) + "%");
                break;

            case XP_GAIN_PERCENT:
                player.setXpMultiplier(player.getXpMultiplier() + amount);
                Gdx.app.log("POWER UP", "Ganancia de XP aumentada. Nuevo multiplicador: " + player.getXpMultiplier());
                break;

            case SPEED:
                player.addSpeedPercent(amount);
                Gdx.app.log("POWER UP", "Velocidad aumentada un " + (amount * 100) + "%. Nueva velocidad: " + player.getSpeed());
                break;

            case ICE_DMG:
                player.addIceDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de hielo aumentado un " + (amount * 100) + "%. Total: " + (player.getIceDamageBonus() * 100) + "%");
                break;

            case ENERGY_DMG:
                player.addEnergyDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de energía aumentado un " + (amount * 100) + "%. Total: " + (player.getEnergyDamageBonus() * 100) + "%");
                break;

            default:
                Gdx.app.log("POWER UP", "Se aplicó estadística global: " + stat);
                break;
        }
    }

    public StatType getStatType() {
        return this.stat;
    }
}
