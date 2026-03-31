package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.combat.weapons.behaviors.*;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class WeaponFactory {

    private ProjectileCreator projectileCreator;
    private EffectManager effectManager;
    private JsonValue weaponDefs;

    public WeaponFactory(ProjectileCreator projectileCreator, EffectManager effectManager) {
        this.projectileCreator = projectileCreator;
        this.effectManager = effectManager;
        loadConfig();
    }

    private void loadConfig() {
        JsonReader reader = new JsonReader();
        weaponDefs = reader.parse(Gdx.files.internal("data/weapons.json")).get("weapons");
    }

    public Weapon createWeapon(String weaponId, Entity owner) {
        JsonValue weaponJson = weaponDefs.get(weaponId);
        if (weaponJson == null) {
            Gdx.app.error("WeaponFactory", "Arma no encontrada: " + weaponId);
            return null;
        }

        String spriteName = weaponJson.getString("sprite");
        TextureRegion sprite = Assets.getRegion(spriteName);
        if (sprite == null) {
            Gdx.app.error("WeaponFactory", "Sprite no encontrado para: " + weaponId + " : " + spriteName);
        }
        
        float damage = weaponJson.getFloat("damage");
        float cd = weaponJson.getFloat("cd");
        float range = weaponJson.getFloat("range");

        JsonValue behaviorJson = weaponJson.get("behavior");
        String behaviorType = behaviorJson.getString("type");
        JsonValue params = behaviorJson.get("params");

        AttackBehavior behavior = null;
        if ("projectile_pattern".equals(behaviorType)) {
            behavior = new ProjectilePatternBehavior(
                projectileCreator,
                Assets.getRegion(params.getString("projectileTexture")),
                params.getFloat("speed", 5f),
                damage,
                params.getFloat("size", 0.2f),
                params.getInt("count", 1),
                params.getFloat("spread", 0f),
                params.getInt("burstCount", 1),
                params.getFloat("burstInterval", 0f)
            );
        } else if ("swing".equals(behaviorType)) {
            behavior = new SwingBehavior(damage, range, params.getFloat("arc"), params.getFloat("speed"));
        } else if ("rocket".equals(behaviorType)) {
            behavior = new RocketBehavior(projectileCreator, Assets.getRegion(params.getString("projectileTexture")));
        } else if ("grenade".equals(behaviorType)) {
            behavior = new GrenadeBehavior(projectileCreator);
        }

        ConfigurableWeapon weapon = new ConfigurableWeapon(owner, sprite, damage, cd, range, behavior, effectManager);
        Gdx.app.log("WeaponFactory", "Arma creada: " + weaponId);
        return weapon;
    }
}
