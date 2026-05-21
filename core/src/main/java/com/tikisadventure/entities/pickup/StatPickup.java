package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.audio.AudioType;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.powerUps.GlobalStatPowerUp;

public class StatPickup extends Pickup {
    private GlobalStatPowerUp.StatType statType;
    private float amount;
    private TextureRegion sprite;

    public StatPickup() {
        super();
        setANCHO(0.6f);
        setALTO(0.6f);
    }

    public void init(Vector2 position, GlobalStatPowerUp.StatType statType, float amount) {
        super.init(position);
        this.statType = statType;
        this.amount = amount;
        this.sprite = getSpriteForStat(statType);
    }

    public String getLabelText() {
        String label = getStatLabel(statType);
        String pct = statType == GlobalStatPowerUp.StatType.ATTRACTION_RANGE
            ? "+" + (int)(amount * 100) + "%"
            : "+" + (int)(amount * 100) + "%";
        return pct + " " + label;
    }

    @Override
    protected void onPickup(Entity entity) {
        AudioManager.playSFX(AudioType.STAT_PICKUP);
        if (entity instanceof Player) {
            Player player = (Player) entity;
            switch (statType) {
                case MAX_HP_PERCENT:
                    float bonusHp = player.getHealthComponent().maxHealth * amount;
                    player.addExtraHealthGained(bonusHp);
                    player.getHealthComponent().maxHealth += bonusHp;
                    player.getHealthComponent().currentHealth += bonusHp;
                    break;
                case SPEED:
                    player.addSpeedPercent(amount);
                    break;
                case KINETIC_DMG:
                    player.addKineticDamageBonus(amount);
                    break;
                case EXPLOSIVE_DMG:
                    player.addExplosiveDamageBonus(amount);
                    break;
                case ENERGY_DMG:
                    player.addEnergyDamageBonus(amount);
                    break;
                case FIRE_DMG:
                    player.addFireDamageBonus(amount);
                    break;
                case ICE_DMG:
                    player.addIceDamageBonus(amount);
                    break;
                case POISON_DMG:
                    player.addPoisonDamageBonus(amount);
                    break;
                case CRIT_CHANCE:
                    player.addCritChanceBonus(amount);
                    break;
                case LUCK:
                    player.setLuck(player.getLuck() + amount);
                    break;
                case XP_GAIN_PERCENT:
                    player.setXpMultiplier(player.getXpMultiplier() + amount);
                    break;
                case ATTRACTION_RANGE:
                    player.addAttractionRange(amount);
                    break;
                case LIFE_REGEN:
                    player.addLifeRegenPercent(amount);
                    break;
                case LIFE_LEECH:
                    player.addLifeLeechPercent(amount);
                    break;
                case EVASION:
                    player.addEvasionChance(amount);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (sprite == null || !isAlive()) return;
        float drawSize = getANCHO() * 1.5f;
        batch.draw(sprite,
            positionComponent.posicion.x - drawSize / 2,
            positionComponent.posicion.y - drawSize / 2 + bobOffset,
            drawSize, drawSize);
    }

    @Override
    public void reset() {
        super.reset();
        this.statType = null;
        this.amount = 0;
        this.sprite = null;
    }

    private static TextureRegion getSpriteForStat(GlobalStatPowerUp.StatType type) {
        String path;
        switch (type) {
            case MAX_HP_PERCENT: path = "stats_asset/statLife"; break;
            case SPEED: path = "stats_asset/statSpeed"; break;
            case KINETIC_DMG: path = "stats_asset/statKineticDamage"; break;
            case EXPLOSIVE_DMG: path = "stats_asset/statExplosionDamage"; break;
            case ENERGY_DMG: path = "stats_asset/statEnergyDamage"; break;
            case FIRE_DMG: path = "stats_asset/statFireDamage"; break;
            case ICE_DMG: path = "stats_asset/statIceDamage"; break;
            case POISON_DMG: path = "stats_asset/statPoison"; break;
            case CRIT_CHANCE: path = "stats_asset/statCrit"; break;
            case LUCK: path = "stats_asset/statLuck"; break;
            case XP_GAIN_PERCENT: path = "stats_asset/statXP"; break;
            case ATTRACTION_RANGE: path = "stats_asset/statAtraction"; break;
            case LIFE_REGEN: path = "stats_asset/statRegen"; break;
            case LIFE_LEECH: path = "stats_asset/statLifeLeach"; break;
            case EVASION: path = "stats_asset/statEvasion"; break;
            default: path = "stats_asset/statLife"; break;
        }
        return Assets.getRegion("shared", path);
    }

    private static String getStatLabel(GlobalStatPowerUp.StatType type) {
        switch (type) {
            case MAX_HP_PERCENT: return "Vida Maxima";
            case SPEED: return "Velocidad";
            case KINETIC_DMG: return "Dano Kinetico";
            case EXPLOSIVE_DMG: return "Dano Explosivo";
            case ENERGY_DMG: return "Dano Energia";
            case FIRE_DMG: return "Dano de Fuego";
            case ICE_DMG: return "Dano de Hielo";
            case POISON_DMG: return "Dano Veneno";
            case CRIT_CHANCE: return "Prob. Critica";
            case LUCK: return "Suerte";
            case XP_GAIN_PERCENT: return "Ganancia XP";
            case ATTRACTION_RANGE: return "Atraccion XP";
            case LIFE_REGEN: return "Regeneracion";
            case LIFE_LEECH: return "Robo de Vida";
            case EVASION: return "Evasion";
            default: return "Stat";
        }
    }
}
