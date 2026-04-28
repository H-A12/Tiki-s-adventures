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
import com.tikisadventure.combat.weapons.modifiers.RandomSpriteModifier;
import com.tikisadventure.combat.weapons.modifiers.WaveMotionModifier;
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

    private static TextureRegion getWeaponSprite(String spriteName) {
        TextureRegion region = Assets.getRegion("shared", spriteName);
        if (region == null) {
            if (spriteName.startsWith("weapons_assets/")) {
                region = Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
            } else if (spriteName.startsWith("particle_assets/")) {
                String simpleName = spriteName.replace("particle_assets/", "");
                region = Assets.getRegion("shared", "weapons_assets/" + simpleName);
            } else {
                region = Assets.getRegion("shared", "weapons_assets/" + spriteName);
            }
        }
        if (region == null) {
            Gdx.app.error("WeaponFactory", "Sprite no encontrado: " + spriteName);
            return Assets.getRegion("shared", "UI_assets/UI_Crosshair");
        }
        return region;
    }

    public Weapon createWeapon(String weaponId, Entity owner) {

        //CREAR ARMA CUSTOM
        if (com.tikisadventure.core.GameSession.customWeapons.containsKey(weaponId)) {
            com.tikisadventure.core.GameSession.CustomWeaponConfig customConf = com.tikisadventure.core.GameSession.customWeapons.get(weaponId);

            Weapon weapon = new Weapon(owner, projectileCreator, effectManager);
            weapon.setName(customConf.name);

            // Sprite del arma
            String spriteName = customConf.sprite != null ? customConf.sprite : "Machinegun";
            weapon.setSprite(Assets.getRegion("shared", spriteName));

            // Aplicar daño y multiplicador
            float baseDamage = customConf.damage;
            if (com.tikisadventure.core.GameSession.godMode) {
                baseDamage *= com.tikisadventure.core.GameSession.godModeDamageMultiplier;
            }
            weapon.setDamage(baseDamage);

            // Validación del tipo de daño
            String typeStr = customConf.damageType;
            try {
                weapon.setDamageType(DamageType.valueOf(typeStr));
            } catch (Exception e) {
                weapon.setDamageType(DamageType.KINETIC);
                typeStr = "KINETIC";
            }

            //Logica de las balas
            String bulletSkin = customConf.projectileSprite != null ? customConf.projectileSprite : "GrayBullet";
            weapon.setProjectileTexture(Assets.getRegion("shared", bulletSkin));

            // 2. Aplicar mecánicas según DamageType
            if ("FIRE".equals(typeStr)) {
                weapon.setMuzzleFlashType("MUZZLE_FLASH_ORANGE");
                weapon.addModifier(new ProjectileModifier() {
                    @Override
                    public void apply(Projectile p, EffectManager em) {
                        p.addComponent(new BurningComponent(em, 2.0f, 1.0f, 3.0f));
                    }
                });
            } else if ("POISON".equals(typeStr)) {
                weapon.addModifier(new ProjectileModifier() {
                    @Override
                    public void apply(Projectile p, EffectManager em) {
                        p.addComponent(new PoisonComponent(em, 2.0f, 1.0f, 3.0f));
                    }
                });
            } else if ("ENERGY".equals(typeStr)) {
                weapon.setMuzzleFlashType("MUZZLE_FLASH_BLUE");
            } else {
                weapon.setMuzzleFlashType("MUZZLE_FLASH_ORANGE");
            }

            //Logica del tipo de bala
            String behavior = customConf.bulletBehavior != null ? customConf.bulletBehavior : "Normal";

            if ("Rebote".equals(behavior)) {
                weapon.addModifier(new com.tikisadventure.combat.weapons.modifiers.BounceModifier(2));
                weapon.setPenetration(5);

            } else if ("Zigzag".equals(behavior)) {
                weapon.addModifier(new com.tikisadventure.combat.weapons.modifiers.WaveMotionModifier(0.4f, 10f));

            } else if ("Perdigones".equals(behavior)) {
                weapon.setProjectileCount(6);
                weapon.setSpread(15.0f);
                weapon.setImprecision(5.0f);
            } else if ("Triple".equals(behavior)) {
                weapon.setProjectileCount(3);
                weapon.setSpread(30.0f); // Un abanico de 30 grados
                weapon.setFixedSpread(true); // Para que salgan siempre en 3 líneas rectas limpias y no aleatorias
                // ------------------------------------
            } else if ("Explosiva".equals(behavior)) {
                weapon.addModifier(new com.tikisadventure.combat.weapons.modifiers.ExplosiveModifier(2.0f, baseDamage, 10.0f, "STANDARD"));

            } else if ("Cadena".equals(behavior)) {
                weapon.addModifier(new com.tikisadventure.combat.weapons.modifiers.ChainHitModifier(3, 5.0f));
                weapon.setPenetration(3);
            } else if ("Boomerang".equals(behavior)) {
                // Le pasamos la referencia del arma actual y la distancia (12 unidades)
                weapon.addModifier(new com.tikisadventure.combat.weapons.modifiers.BoomerangModifier(weapon, 12.0f));
                weapon.setPenetration(999);
            }

            //Estadisticas base
            weapon.setCooldown(customConf.cd);
            weapon.setCritChance(customConf.critChance);
            weapon.setCritDamageMult(2.0f);
            weapon.setShootRange(15.0f);
            weapon.setBulletSpeed(12.0f);
            weapon.setBulletSize(0.3f);
            weapon.setProjectileLifetime(3.0f);

            if ("Sword".equals(customConf.sprite) || "Espada".equals(customConf.sprite)) {
                weapon.setCategory(WeaponCategory.MELEE);
                weapon.setShootRange(2.5f); // Rango muy corto para el cuerpo a cuerpo
                weapon.setImpactKnockback(20.0f); // Empuje fuerte al golpear
                weapon.setProjectileCount(0);
                weapon.setPivot(0.5f, 0.1f);// Sin balas
            } else {
                weapon.setCategory(WeaponCategory.PISTOL);
                weapon.setShootRange(15.0f); // Rango normal para armas de fuego
                weapon.setBulletSpeed(12.0f);
                weapon.setBulletSize(0.3f);
                weapon.setProjectileLifetime(3.0f);
                weapon.setPivot(0.5f, 0.5f);

                if (!"Perdigones".equals(behavior) && !"Triple".equals(behavior)) {
                    weapon.setProjectileCount(1);
                }
            }

            return weapon;
        }

        JsonValue weaponJson = weaponDefs.get(weaponId);
        if (weaponJson == null) {
            Gdx.app.error("WeaponFactory", "Arma no encontrada: " + weaponId);
            return null;
        }

        String spriteName = weaponJson.getString("sprite");
        TextureRegion sprite = getWeaponSprite(spriteName);

        Weapon weapon = new Weapon(owner, projectileCreator, effectManager);
        weapon.setName(weaponJson.getString("name", weaponId));
        weapon.setSprite(sprite);

        float baseDamage = weaponJson.getFloat("damage");
        if (com.tikisadventure.core.GameSession.godMode) {
            baseDamage *= com.tikisadventure.core.GameSession.godModeDamageMultiplier;
        }
        weapon.setDamage(baseDamage);

        weapon.setDamageType(DamageType.valueOf(weaponJson.getString("damageType", "KINETIC").toUpperCase()));
        weapon.setDamageType(DamageType.valueOf(weaponJson.getString("damageType", "KINETIC").toUpperCase()));
        weapon.setCooldown(weaponJson.getFloat("cd"));
        weapon.setShootRange(weaponJson.getFloat("range"));
        weapon.setPrice(weaponJson.getInt("price", 0));
        weapon.setTier(weaponJson.getInt("tier", 1));
        weapon.setCategory(WeaponCategory.valueOf(weaponJson.getString("category", "PISTOL").toUpperCase()));

        if (weapon.getCategory() == WeaponCategory.MELEE) {
            weapon.setPivot(0.5f, 0.1f);
        } else {
            weapon.setPivot(0.5f, 0.5f);
        }

        weapon.setCritChance(weaponJson.getFloat("critChance", 0.05f));
        weapon.setCritDamageMult(weaponJson.getFloat("critDamageMult", 1.5f));

        weapon.setProjectileTexture(getWeaponSprite(weaponJson.getString("projectileTexture", "bullet")));
        weapon.setBulletSpeed(weaponJson.getFloat("speed", 10.0f));
        weapon.setBulletSize(weaponJson.getFloat("size", 0.2f));
        weapon.setPenetration(weaponJson.getInt("penetration", 0));
        weapon.setImpactKnockback(weaponJson.getFloat("impactKnockback", 0f));
        weapon.setProjectileCount(weaponJson.getInt("count", 1));
        weapon.setSpread(weaponJson.getFloat("spread", 0.0f));
        weapon.setFixedSpread(weaponJson.getBoolean("fixedSpread", false));
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
                } else if (type.equals("waveMotion")) {
                    weapon.addModifier(new WaveMotionModifier(
                        mod.getFloat("amplitude", 0.5f),
                        mod.getFloat("frequency", 5.0f)
                    ));
                } else if (type.equals("randomSprite")) {
                    weapon.addModifier(new RandomSpriteModifier(mod.get("sprites")));
                } else if (type.equals("boomerang")) {
                    weapon.addModifier(new com.tikisadventure.combat.weapons.modifiers.BoomerangModifier(
                        weapon,
                        mod.getFloat("maxDistance", 12.0f)
                    ));
                }

            }
        }

        return weapon;
    }
}
