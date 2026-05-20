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

    public GlobalStatPowerUp(String powerUpId, String name, String desc, Rarity rarity, StatType stat, float amount) {
        super(powerUpId, name, desc, rarity);
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
                }
                break;

            case LUCK:
                if (player.getLuck() >= Player.MAX_LUCK) {
                    break;
                }
                player.setLuck(player.getLuck() + amount);
                break;

            case KINETIC_DMG:
                if (player.getKineticDamageBonus() >= Player.MAX_DMG_BONUS) {
                    break;
                }
                player.addKineticDamageBonus(amount);
                break;

            case FIRE_DMG:
                if (player.getFireDamageBonus() >= Player.MAX_DMG_BONUS) {
                    break;
                }
                player.addFireDamageBonus(amount);
                break;

            case EXPLOSIVE_DMG:
                if (player.getExplosiveDamageBonus() >= Player.MAX_DMG_BONUS) {
                    break;
                }
                player.addExplosiveDamageBonus(amount);
                break;

            case POISON_DMG:
                if (player.getPoisonDamageBonus() >= Player.MAX_DMG_BONUS) {
                    break;
                }
                player.addPoisonDamageBonus(amount);
                break;

            case CRIT_CHANCE:
                if (player.getCritChanceBonus() >= Player.MAX_CRIT) {
                    break;
                }
                player.addCritChanceBonus(amount);
                break;

            case XP_GAIN_PERCENT:
                if (player.getXpMultiplier() >= Player.MAX_XP_MULTI) {
                    break;
                }
                player.setXpMultiplier(player.getXpMultiplier() + amount);
                break;

            case SPEED:
                if (player.getSpeed() >= player.getProfile().speed * (1f + Player.MAX_SPEED_BONUS)) {
                    break;
                }
                player.addSpeedPercent(amount);
                break;

            case ICE_DMG:
                if (player.getIceDamageBonus() >= Player.MAX_DMG_BONUS) {
                    break;
                }
                player.addIceDamageBonus(amount);
                break;

            case ENERGY_DMG:
                if (player.getEnergyDamageBonus() >= Player.MAX_DMG_BONUS) {
                    break;
                }
                player.addEnergyDamageBonus(amount);
                break;

            case ATTRACTION_RANGE:
                if (player.getAttractionRange() >= Player.MAX_ATTRACTION_RANGE) {
                    break;
                }
                player.addAttractionRange(amount);
                break;

            case LIFE_LEECH:
                if (player.getLifeLeechPercent() >= Player.MAX_LEECH) {
                    break;
                }
                player.addLifeLeechPercent(amount);
                break;

            case LIFE_REGEN:
                if (player.getLifeRegenPercent() >= Player.MAX_REGEN) {
                    break;
                }
                player.addLifeRegenPercent(amount);
                break;

            case EVASION:
                if (player.getEvasionChance() >= Player.MAX_EVASION) {
                    break;
                }
                player.addEvasionChance(amount);
                break;

            case ELEMENTAL_DMG:
                if (player.getKineticDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getExplosiveDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getFireDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getPoisonDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getIceDamageBonus() >= Player.MAX_DMG_BONUS &&
                    player.getEnergyDamageBonus() >= Player.MAX_DMG_BONUS) {
                    break;
                }
                player.addAllDamageBonus(amount);
                break;

            default:
                break;
        }
    }

    public StatType getStatType() {
        return this.stat;
    }
}
