package com.tikisadventure.entities.gadgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.audio.AudioType;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.systems.powerUps.GlobalStatPowerUp;

import java.util.Random;

public class LootBox extends Entity {

    public enum DropType { COINS, HEAL, STAT }

    private static TextureRegion sprite;
    private DropType dropType;
    private int coinAmount;
    private GlobalStatPowerUp.StatType statType;
    private float statAmount;
    private float shakeTimer = 0f;
    private static final float SHAKE_DURATION = 0.3f;
    private static final float SHAKE_INTENSITY = 0.12f;
    private static final float LOOTBOX_HEALTH = 25f;
    private Random rng;

    public LootBox(Vector2 position, Random rng) {
        super();
        this.rng = rng;

        if (sprite == null) {
            sprite = Assets.getRegion("shared", "pickup_assets/lootBox");
        }

        this.healthComponent = new HealthComponent(LOOTBOX_HEALTH);
        this.renderComponent = new RenderComponent(sprite, 1.125f, 1.125f);
        setANCHO(1.125f);
        setALTO(1.125f);
        setPosition(position);
        actualizarHitboxes();

        decideDrop();
    }

    private void decideDrop() {
        float roll = rng.nextFloat();
        if (roll < 0.60f) {
            dropType = DropType.COINS;
            coinAmount = 5 + rng.nextInt(6);
        } else if (roll < 0.85f) {
            dropType = DropType.HEAL;
        } else {
            dropType = DropType.STAT;
            statType = pickRandomStat();
            statAmount = getAmountForStat(statType);
        }
    }

    private GlobalStatPowerUp.StatType pickRandomStat() {
        GlobalStatPowerUp.StatType[] stats = {
            GlobalStatPowerUp.StatType.MAX_HP_PERCENT,
            GlobalStatPowerUp.StatType.SPEED,
            GlobalStatPowerUp.StatType.KINETIC_DMG,
            GlobalStatPowerUp.StatType.EXPLOSIVE_DMG,
            GlobalStatPowerUp.StatType.ENERGY_DMG,
            GlobalStatPowerUp.StatType.FIRE_DMG,
            GlobalStatPowerUp.StatType.ICE_DMG,
            GlobalStatPowerUp.StatType.POISON_DMG,
            GlobalStatPowerUp.StatType.CRIT_CHANCE,
            GlobalStatPowerUp.StatType.LUCK,
            GlobalStatPowerUp.StatType.XP_GAIN_PERCENT,
            GlobalStatPowerUp.StatType.ATTRACTION_RANGE,
            GlobalStatPowerUp.StatType.LIFE_REGEN,
            GlobalStatPowerUp.StatType.LIFE_LEECH,
            GlobalStatPowerUp.StatType.EVASION
        };
        return stats[rng.nextInt(stats.length)];
    }

    private float getAmountForStat(GlobalStatPowerUp.StatType type) {
        switch (type) {
            case CRIT_CHANCE:
            case LUCK:
            case LIFE_REGEN:
                return 0.02f;
            case LIFE_LEECH:
            case EVASION:
                return 0.03f;
            case SPEED:
            case KINETIC_DMG:
            case EXPLOSIVE_DMG:
            case ENERGY_DMG:
            case FIRE_DMG:
            case ICE_DMG:
            case POISON_DMG:
            case MAX_HP_PERCENT:
                return 0.05f;
            case XP_GAIN_PERCENT:
                return 0.08f;
            case ATTRACTION_RANGE:
                return 0.10f;
            default:
                return 0.05f;
        }
    }

    public DropType getDropType() { return dropType; }
    public int getCoinAmount() { return coinAmount; }
    public GlobalStatPowerUp.StatType getStatType() { return statType; }
    public float getStatAmount() { return statAmount; }

    @Override
    public void update(float delta, Entity target) {
        super.update(delta);

        if (damageFlashTimer > 0) {
            shakeTimer = SHAKE_DURATION;
        }
        if (shakeTimer > 0) {
            shakeTimer -= delta;
        }
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (!isAlive() || sprite == null) return;

        float shakeX = 0;
        if (shakeTimer > 0) {
            shakeX = MathUtils.random(-SHAKE_INTENSITY, SHAKE_INTENSITY);
        }

        float drawW = getANCHO() * 1.5f;
        float drawH = getALTO() * 1.5f;
        float x = getPosition().x - drawW / 2 + shakeX;
        float y = getPosition().y - drawH / 2;

        batch.draw(sprite, x, y, drawW, drawH);
    }

    @Override
    public void receiveDamage(float quantity, boolean isCritical, com.tikisadventure.combat.DamageType damageType) {
        super.receiveDamage(quantity, isCritical, damageType);
        shakeTimer = SHAKE_DURATION;
        AudioManager.playSFX(AudioType.LOOTBOX_HIT);
    }

    @Override
    public void die() {
        AudioManager.playSFX(AudioType.LOOTBOX_BREAK);
        super.die();
    }
}
