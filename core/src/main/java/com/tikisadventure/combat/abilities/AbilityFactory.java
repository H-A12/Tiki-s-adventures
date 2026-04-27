package com.tikisadventure.combat.abilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.combat.abilities.effects.*;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.combat.weapons.ProjectileCreator;

public class AbilityFactory {
    private JsonValue abilityDefinitions;

    private static AbilityFactory instance;

    public static AbilityFactory getInstance() {
        if (instance == null) {
            instance = new AbilityFactory();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private void loadConfig() {
        if (abilityDefinitions == null) {
            abilityDefinitions = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));
        }
    }

    public static Ability create(String id, ProjectileCreator projectileCreator, EffectManager effectManager) {
        return getInstance().createInternal(id, projectileCreator, effectManager);
    }

    private Ability createInternal(String id, ProjectileCreator projectileCreator, EffectManager effectManager) {
        loadConfig();
        JsonValue def = abilityDefinitions.get(id);
        if (def == null) return null;

        String name = def.getString("name");
        float cooldown = def.getFloat("cooldown");
        float maxRange = def.getFloat("maxRange", 10.0f);
        JsonValue effectsJson = def.get("effects");
        Array<AbilityEffect> effects = new Array<>();

        for (int i = 0; i < effectsJson.size; i++) {
            JsonValue effJson = effectsJson.get(i);
            String type = effJson.getString("type");
            JsonValue params = effJson.get("params");

            if ("THROW".equals(type)) {
                Array<AbilityEffect> nextEffects = new Array<>();
                for (int j = i + 1; j < effectsJson.size; j++) {
                    nextEffects.add(createEffect(effectsJson.get(j), projectileCreator, effectManager));
                }
                effects.add(new ThrowEffect(
                    effectManager,
                    params.getString("sprite"),
                    params.getFloat("speed", 5.0f),
                    params.getFloat("lifetime", 1.0f),
                    params.getString("trailType", "TRAIL_LASER"),
                    params.getFloat("trailSpacing", 0.1f),
                    nextEffects));
                break;
            } else {
                effects.add(createEffect(effJson, projectileCreator, effectManager));
            }
        }

        return new GenericAbility(name, cooldown, maxRange, effects);
    }

    private static AbilityEffect createEffect(JsonValue json, ProjectileCreator pc, EffectManager em) {
        String type = json.getString("type");
        JsonValue params = json.get("params");

        switch (type) {
            case "IMPULSE":
                return new ImpulseEffect(params.getFloat("force"), params.getFloat("duration"));
            case "SPAWN_PROJECTILES":
                return new SpawnProjectilesEffect(
                        em,
                        DamageType.valueOf(params.getString("damageType")),
                        params.getInt("count"),
                        params.getFloat("damage"),
                        params.getString("sprite", "bullet"),
                        params.getString("profile", "STANDARD"),
                        params.getFloat("scale", 1.0f),
                        params.getFloat("rotationSpeed", 0f));
            case "EXPLOSION":
                return new ExplosionEffect(em, params.getFloat("radius"), params.getFloat("damage"), params.getFloat("knockback"), params.getString("profile", "STANDARD"));
            case "BURNING":
                return new BurningEffect(em, params.getFloat("radius"), params.getFloat("damagePerTick"), params.getFloat("duration"), params.getString("profile", "FIRE"));
            case "FREEZE":
                return new FreezeEffect(em, params.getFloat("radius"), params.getFloat("duration"), params.getString("profile", "FREEZE"));
            default:
                return null;
        }
    }
}
