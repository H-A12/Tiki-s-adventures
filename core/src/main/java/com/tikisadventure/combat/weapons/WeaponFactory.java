package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.WeaponCategory;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.modifiers.BounceModifier;
import com.tikisadventure.combat.weapons.modifiers.ChainHitModifier;
import com.tikisadventure.combat.weapons.modifiers.ExplosiveModifier;
import com.tikisadventure.components.BurningComponent;
import com.tikisadventure.components.PoisonComponent;
import com.tikisadventure.combat.weapons.Emitter;

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
        weaponDefs = reader.parse(Gdx.files.internal("data/weapons_config.json")).get("weapons");
    }

    public Weapon createWeapon(String weaponId, Entity owner) {
        JsonValue weaponJson = weaponDefs.get(weaponId);
        if (weaponJson == null) {
            Gdx.app.error("WeaponFactory", "Arma no encontrada: " + weaponId);
            return null;
        }

        String spriteName = weaponJson.getString("sprite");
        TextureRegion sprite = Assets.getRegion("shared", spriteName);

        Weapon weapon = new Weapon(owner, projectileCreator, effectManager);
        weapon.setSprite(sprite);
        weapon.setDamage(weaponJson.getFloat("damage"));
        weapon.setDamageType(DamageType.valueOf(weaponJson.getString("damageType", "KINETIC").toUpperCase()));
        weapon.setCooldown(weaponJson.getFloat("cd"));
        weapon.setShootRange(weaponJson.getFloat("range"));
        weapon.setPrice(weaponJson.getInt("price", 0));
        weapon.setTier(weaponJson.getInt("tier", 1));
        weapon.setCategory(WeaponCategory.valueOf(weaponJson.getString("category", "PISTOL").toUpperCase()));
        weapon.setCritChance(weaponJson.getFloat("critChance", 0.05f));
        weapon.setCritDamageMult(weaponJson.getFloat("critDamageMult", 1.5f));

        weapon.setProjectileTexture(Assets.getRegion("shared", weaponJson.getString("projectileTexture", "bullet")));
        weapon.setBulletSpeed(weaponJson.getFloat("speed", 10.0f));
        weapon.setBulletSize(weaponJson.getFloat("size", 0.2f));
        weapon.setPenetration(weaponJson.getInt("penetration", 0));
        weapon.setImpactKnockback(weaponJson.getFloat("impactKnockback", 0f));
        weapon.setProjectileCount(weaponJson.getInt("count", 1));
        weapon.setSpread(weaponJson.getFloat("spread", 0.0f));
        weapon.setSpreadDelay(weaponJson.getFloat("spreadDelay", 0.0f));
        weapon.setGrowthRate(weaponJson.getFloat("growthRate", 0.0f));
        weapon.setMaxRadius(weaponJson.getFloat("maxRadius", Float.MAX_VALUE));
        weapon.setRotationSpeed(weaponJson.getFloat("rotationSpeed", 0.0f));
        weapon.setImprecision(weaponJson.getFloat("imprecision", 0.0f));
        weapon.setProjectileLifetime(weaponJson.getFloat("lifetime", 2.0f));
        weapon.setSpawnOffset(new Vector2(weaponJson.getFloat("spawnOffsetX", 0), weaponJson.getFloat("spawnOffsetY", 0)));
        weapon.setMuzzleFlashOffset(new Vector2(weaponJson.getFloat("muzzleFlashOffsetX", 0), weaponJson.getFloat("muzzleFlashOffsetY", 0)));
        
        JsonValue muzzleFlashJson = weaponJson.get("muzzleFlash");
        if (muzzleFlashJson != null) {
            weapon.setMuzzleFlashType(muzzleFlashJson.getString("type"));
        }

        JsonValue trailJson = weaponJson.get("trail");
        if (trailJson != null) {
            weapon.setTrail(
                trailJson.getString("type"),
                trailJson.getFloat("interval", 0.05f)
            );
        }

        weapon.setRecoil(weaponJson.getFloat("recoilForce", 0f), weaponJson.getFloat("recoilRecovery", 8f));

        JsonValue emittersJson = weaponJson.get("emitters");
        if (emittersJson != null && emittersJson.isArray()) {
            for (JsonValue emitterJson : emittersJson) {
                String type = emitterJson.getString("type");
                Vector2 offset = new Vector2(emitterJson.get("offset").getFloat("x", 0f), emitterJson.get("offset").getFloat("y", 0f));
                weapon.addEmitter(new Emitter(type, offset));
            }
        }

        JsonValue modifiers = weaponJson.get("modifiers");
        if (modifiers != null && modifiers.isArray()) {
            for (JsonValue mod : modifiers) {
                String type = mod.getString("type");
                if (type.equals("explosive")) {
                    weapon.addModifier(new ExplosiveModifier(
                        mod.getFloat("radius"),
                        mod.getFloat("damage"),
                        mod.getFloat("knockback", 0f),
                        mod.getString("profile", "STANDARD")
                    ));
                } else if (type.equals("burning")) {
                    weapon.addModifier(new ProjectileModifier() {
                        @Override
                        public void apply(Projectile p, EffectManager em) {
                            p.addComponent(new BurningComponent(
                                em,
                                mod.getFloat("damage"),
                                mod.getFloat("interval"),
                                mod.getFloat("duration")
                            ));
                        }
                    });
                } else if (type.equals("poison")) {
                    weapon.addModifier(new ProjectileModifier() {
                        @Override
                        public void apply(Projectile p, EffectManager em) {
                            p.addComponent(new PoisonComponent(
                                em,
                                mod.getFloat("damage"),
                                mod.getFloat("interval"),
                                mod.getFloat("duration")
                            ));
                        }
                    });
                } else if (type.equals("bounce")) {
                    weapon.addModifier(new BounceModifier(mod.getInt("maxBounces", 1)));
                } else if (type.equals("chainHit")) {
                    weapon.addModifier(new ChainHitModifier(
                        mod.getInt("maxBounces", 1),
                        mod.getFloat("searchRadius", 5.0f)
                    ));
                }
            }
        }

        return weapon;
    }
}