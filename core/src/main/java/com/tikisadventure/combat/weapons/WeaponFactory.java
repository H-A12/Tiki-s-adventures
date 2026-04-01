package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.weapons.behaviors.*;
import com.tikisadventure.combat.CombatInitializer;

public class WeaponFactory {

    private ProjectileCreator projectileCreator;
    private EffectManager effectManager;
    private JsonValue weaponDefs;

    public WeaponFactory(ProjectileCreator projectileCreator, EffectManager effectManager) {
        this.projectileCreator = projectileCreator;
        this.effectManager = effectManager;
        CombatInitializer.init(projectileCreator, effectManager);
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
        TextureRegion sprite = Assets.getRegion("shared", spriteName);
        if (sprite == null) {
            Gdx.app.error("WeaponFactory", "Sprite no encontrado para: " + weaponId + " : " + spriteName);
        }
        
        float damage = weaponJson.getFloat("damage");
        float cd = weaponJson.getFloat("cd");
        float range = weaponJson.getFloat("range");

        JsonValue behaviorJson = weaponJson.get("behavior");
        String behaviorType = behaviorJson.getString("type");
        JsonValue params = behaviorJson.get("params");

        BehaviorFactory factory = BehaviorRegistry.get(behaviorType);
        if (factory == null) {
            Gdx.app.error("WeaponFactory", "Comportamiento no encontrado: " + behaviorType);
            return null;
        }
        
        AttackBehavior behavior = factory.create(params, projectileCreator, damage);
        ConfigurableWeapon weapon = new ConfigurableWeapon(owner, sprite, damage, cd, range, behavior, effectManager);
        behavior.setWeapon(weapon);
        
        Gdx.app.log("WeaponFactory", "Arma creada: " + weaponId);
        return weapon;
    }
}
