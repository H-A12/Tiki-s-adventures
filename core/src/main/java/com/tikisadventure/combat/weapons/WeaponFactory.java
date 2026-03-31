package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.combat.weapons.behaviors.AttackBehavior;
import com.tikisadventure.combat.weapons.behaviors.ProjectileBehavior;
import com.tikisadventure.combat.weapons.behaviors.SwingBehavior;
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
        if (weaponJson == null) return null;

        String spriteName = weaponJson.getString("sprite");
        float damage = weaponJson.getFloat("damage");
        float cd = weaponJson.getFloat("cd");
        float range = weaponJson.getFloat("range");

        JsonValue behaviorJson = weaponJson.get("behavior");
        String behaviorType = behaviorJson.getString("type");
        JsonValue params = behaviorJson.get("params");

        AttackBehavior behavior = null;
        if ("projectile".equals(behaviorType)) {
            behavior = new ProjectileBehavior(
                projectileCreator,
                Assets.getRegion(params.getString("projectileTexture")),
                params.getFloat("speed"),
                damage,
                params.getFloat("size"),
                null, 0f
            );
        } else if ("swing".equals(behaviorType)) {
            behavior = new SwingBehavior(damage, range, params.getFloat("arc"), params.getFloat("speed"));
        }

        return new ConfigurableWeapon(owner, Assets.getRegion(spriteName), damage, cd, range, behavior, effectManager);
    }
}
