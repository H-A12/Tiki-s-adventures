package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.weapons.behaviors.*;
import com.tikisadventure.systems.registry.CombatRegistry;

public class WeaponFactory {

    private ProjectileCreator projectileCreator;
    private EffectManager effectManager;
    private JsonValue weaponDefs;

    public WeaponFactory(ProjectileCreator projectileCreator, EffectManager effectManager) {
        this.projectileCreator = projectileCreator;
        this.effectManager = effectManager;
        CombatRegistry.init(projectileCreator, effectManager);
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

        // Lógica de carga de stats
        float damage = weaponJson.getFloat("damage");
        float cd = weaponJson.getFloat("cd");
        float range = weaponJson.getFloat("range");
        
        int price = weaponJson.getInt("price", 0);
        int tier = weaponJson.getInt("tier", 1);
        float critChance = weaponJson.getFloat("critChance", 0.05f);
        float critDamageMult = weaponJson.getFloat("critDamageMult", 1.5f);

        // Crear comportamiento
        AttackBehavior behavior = factory.create(params, projectileCreator, damage);
        
        ConfigurableWeapon weapon = new ConfigurableWeapon(owner, sprite, damage, cd, range, behavior, effectManager);
        
        // Asignar nuevos stats
        weapon.setPrice(price);
        weapon.setTier(tier);
        weapon.setCritChance(critChance);
        weapon.setCritDamageMult(critDamageMult);
        
        JsonValue categories = weaponJson.get("categories");
        if (categories != null && categories.isArray()) {
            for (JsonValue cat : categories) {
                weapon.addCategory(cat.asString());
            }
        }
        
        behavior.setWeapon(weapon);

        JsonValue muzzleFlashJson = weaponJson.get("muzzleFlash");
        if (muzzleFlashJson != null) {
            String muzzleType = muzzleFlashJson.getString("type");
            if (muzzleType != null) {
                try {
                    EffectType muzzleEffectType = EffectType.valueOf(muzzleType);
                    weapon.setMuzzleFlashType(muzzleEffectType);
                } catch (IllegalArgumentException e) {
                    Gdx.app.error("WeaponFactory", "Tipo de muzzleflash no válido: " + muzzleType);
                }
            }
        }

        Gdx.app.log("WeaponFactory", "Arma creada: " + weaponId);
        return weapon;
    }
}
