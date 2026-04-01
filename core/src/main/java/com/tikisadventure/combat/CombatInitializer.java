package com.tikisadventure.combat;

import com.tikisadventure.combat.weapons.behaviors.*;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.combat.weapons.ProjectileCreator;

public class CombatInitializer {

    public static void init(ProjectileCreator projectileCreator, EffectManager effectManager) {
        // Register Launchers
        BehaviorRegistry.register("projectile_pattern", (params, pc, damage) -> {
            ProjectilePatternBehavior b = new ProjectilePatternBehavior(
                pc,
                com.tikisadventure.core.Assets.getRegion("shared", params.getString("projectileTexture")),
                params.getFloat("speed", 5f),
                damage,
                params.getFloat("size", 0.2f),
                params.getInt("count", 1),
                params.getFloat("spread", 0f),
                params.getInt("burstCount", 1),
                params.getFloat("burstInterval", 0f)
            );
            // Apply modifiers from JSON if they exist in a "modifiers" array
            if (params.get("modifiers") != null) {
                for (com.badlogic.gdx.utils.JsonValue m : params.get("modifiers")) {
                    String type = m.getString("type");
                    if ("explosive".equals(type)) {
                        b.addModifier(new ExplosiveModifier(m.getFloat("radius", 3.0f), m.getFloat("damage", 10.0f)));
                    }
                }
            }
            return b;
        });

        // Register Melee
        BehaviorRegistry.register("melee", (params, pc, damage) -> {
            MeleeBehavior b = new MeleeBehavior(
                params.getFloat("range", 1f),
                params.getFloat("arc", 60f),
                params.getFloat("speed", 0.5f),
                params.getFloat("swingRadius", 0.5f),
                params.getFloat("pivotX", 0.5f),
                params.getFloat("pivotY", 0.5f)
            );
            // Register HitModifiers if they exist
             if (params.get("hitModifiers") != null) {
                for (com.badlogic.gdx.utils.JsonValue m : params.get("hitModifiers")) {
                    String type = m.getString("type");
                    if ("basic_damage".equals(type)) {
                        b.addModifier(new DamageModifier(damage));
                    }
                }
            }
            return b;
        });
    }
}
