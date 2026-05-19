package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.entities.player.Player;

// Power ups de estadísticas globales
public class GlobalStatPowerUp extends PowerUp {

    public enum StatType {
        KINETIC_DMG, FIRE_DMG, POISON_DMG, EXPLOSIVE_DMG, ICE_DMG, ENERGY_DMG, ELEMENTAL_DMG,
        CRIT_CHANCE, LUCK, MAX_HP, MAX_HP_PERCENT, XP_GAIN_PERCENT, SPEED,
        ATTRACTION_RANGE, LIFE_LEECH, LIFE_REGEN, EVASION
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
                if (player.getLuck() >= Player.MAX_LUCK) {
                    Gdx.app.log("POWER UP", "Suerte ya al máximo (100%), bonus ignorado");
                    break;
                }
                player.setLuck(player.getLuck() + amount);
                Gdx.app.log("POWER UP", "Suerte aumentada. Nueva suerte: " + player.getLuck());
                break;

            case KINETIC_DMG:
                if (player.getKineticDamageBonus() >= Player.MAX_DMG_BONUS) {
                    Gdx.app.log("POWER UP", "Daño cinético ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addKineticDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño cinético aumentado un " + (amount * 100) + "%. Total: " + (player.getKineticDamageBonus() * 100) + "%");
                break;

            case FIRE_DMG:
                if (player.getFireDamageBonus() >= Player.MAX_DMG_BONUS) {
                    Gdx.app.log("POWER UP", "Daño de fuego ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addFireDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de fuego aumentado un " + (amount * 100) + "%. Total: " + (player.getFireDamageBonus() * 100) + "%");
                break;

            case EXPLOSIVE_DMG:
                if (player.getExplosiveDamageBonus() >= Player.MAX_DMG_BONUS) {
                    Gdx.app.log("POWER UP", "Daño explosivo ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addExplosiveDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño explosivo aumentado un " + (amount * 100) + "%. Total: " + (player.getExplosiveDamageBonus() * 100) + "%");
                break;

            case POISON_DMG:
                if (player.getPoisonDamageBonus() >= Player.MAX_DMG_BONUS) {
                    Gdx.app.log("POWER UP", "Daño de veneno ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addPoisonDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de veneno aumentado un " + (amount * 100) + "%. Total: " + (player.getPoisonDamageBonus() * 100) + "%");
                break;

            case CRIT_CHANCE:
                if (player.getCritChanceBonus() >= Player.MAX_CRIT) {
                    Gdx.app.log("POWER UP", "Prob. crítico ya al máximo (100%), bonus ignorado");
                    break;
                }
                player.addCritChanceBonus(amount);
                Gdx.app.log("POWER UP", "Probabilidad de crítico aumentada. Total: " + (player.getCritChanceBonus() * 100) + "%");
                break;

            case XP_GAIN_PERCENT:
                if (player.getXpMultiplier() >= Player.MAX_XP_MULTI) {
                    Gdx.app.log("POWER UP", "Bonus XP ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.setXpMultiplier(player.getXpMultiplier() + amount);
                Gdx.app.log("POWER UP", "Ganancia de XP aumentada. Nuevo multiplicador: " + player.getXpMultiplier());
                break;

            case SPEED:
                if (player.getSpeed() >= player.getProfile().speed * (1f + Player.MAX_SPEED_BONUS)) {
                    Gdx.app.log("POWER UP", "Velocidad ya al máximo (300%), bonus ignorado");
                    break;
                }
                player.addSpeedPercent(amount);
                Gdx.app.log("POWER UP", "Velocidad aumentada un " + (amount * 100) + "%. Nueva velocidad: " + player.getSpeed());
                break;

            case ICE_DMG:
                if (player.getIceDamageBonus() >= Player.MAX_DMG_BONUS) {
                    Gdx.app.log("POWER UP", "Daño de hielo ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addIceDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de hielo aumentado un " + (amount * 100) + "%. Total: " + (player.getIceDamageBonus() * 100) + "%");
                break;

            case ENERGY_DMG:
                if (player.getEnergyDamageBonus() >= Player.MAX_DMG_BONUS) {
                    Gdx.app.log("POWER UP", "Daño de energía ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addEnergyDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño de energía aumentado un " + (amount * 100) + "%. Total: " + (player.getEnergyDamageBonus() * 100) + "%");
                break;

            case ATTRACTION_RANGE:
                if (player.getAttractionRange() >= Player.MAX_ATTRACTION_RANGE) {
                    Gdx.app.log("POWER UP", "Atracción ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addAttractionRange(amount);
                Gdx.app.log("POWER UP", "Rango de atracción aumentado en " + amount + ". Total: " + player.getAttractionRange());
                break;

            case LIFE_LEECH:
                if (player.getLifeLeechPercent() >= Player.MAX_LEECH) {
                    Gdx.app.log("POWER UP", "Robo de vida ya al máximo (100%), bonus ignorado");
                    break;
                }
                player.addLifeLeechPercent(amount);
                Gdx.app.log("POWER UP", "Robo de vida aumentado un " + (amount * 100) + "%. Total: " + (player.getLifeLeechPercent() * 100) + "%");
                break;

            case LIFE_REGEN:
                if (player.getLifeRegenPercent() >= Player.MAX_REGEN) {
                    Gdx.app.log("POWER UP", "Regeneración ya al máximo (100%), bonus ignorado");
                    break;
                }
                player.addLifeRegenPercent(amount);
                Gdx.app.log("POWER UP", "Regeneración de vida aumentada un " + (amount * 100) + "%/s. Total: " + (player.getLifeRegenPercent() * 100) + "%/s");
                break;

            case EVASION:
                if (player.getEvasionChance() >= Player.MAX_EVASION) {
                    Gdx.app.log("POWER UP", "Evasión ya al máximo (75%), bonus ignorado");
                    break;
                }
                player.addEvasionChance(amount);
                Gdx.app.log("POWER UP", "Evasión aumentada un " + (amount * 100) + "%. Total: " + (player.getEvasionChance() * 100) + "%");
                break;

            case ELEMENTAL_DMG:
                if (player.getKineticDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getExplosiveDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getFireDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getPoisonDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getIceDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getEnergyDamageBonus() >= Player.MAX_DMG_BONUS) {
                    Gdx.app.log("POWER UP", "Todos los daños elementales ya al máximo (999%), bonus ignorado");
                    break;
                }
                player.addAllDamageBonus(amount);
                Gdx.app.log("POWER UP", "Daño Elemental (+ " + (amount * 100) + "% a todos los elementos)");
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
