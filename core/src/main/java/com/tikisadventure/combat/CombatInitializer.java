package com.tikisadventure.combat;

import com.tikisadventure.combat.weapons.behaviors.*;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.combat.weapons.ProjectileCreator;

public class CombatInitializer {

    public static void init(ProjectileCreator projectileCreator, EffectManager effectManager) {
        // Register Launchers
        BehaviorRegistry.register("projectile_pattern", (params, pc, damage) -> {
            EffectType firingEffect = null;
            if (params.get("firingEffect") != null && !params.get("firingEffect").isNull()) {
                firingEffect = EffectType.valueOf(params.getString("firingEffect").toUpperCase());
            }
            
            EffectType trailType = null;
            float trailInterval = 0f;
            if (params.get("trail") != null) {
                com.badlogic.gdx.utils.JsonValue trail = params.get("trail");
                if (trail.get("type") != null && !trail.get("type").isNull()) {
                    trailType = EffectType.valueOf(trail.getString("type").toUpperCase());
                }
                trailInterval = trail.getFloat("interval", 0f);
            }

            ProjectilePatternBehavior b = new ProjectilePatternBehavior(
                pc,
                com.tikisadventure.core.Assets.getRegion("shared", params.getString("projectileTexture")),
                params.getFloat("speed", 5f),
                damage,
                params.getFloat("size", 0.2f),
                params.getInt("count", 1),
                params.getFloat("spread", 0f),
                params.getInt("burstCount", 1),
                params.getFloat("burstInterval", 0f),
                firingEffect,
                trailType,
                trailInterval
            );
            // Apply modifiers from JSON if they exist in a "modifiers" array
            if (params.get("modifiers") != null) {
                for (com.badlogic.gdx.utils.JsonValue m : params.get("modifiers")) {
                    String type = m.getString("type");
                    if ("explosive".equals(type)) {
                        b.addModifier(new ExplosiveModifier(m.getFloat("radius", 3.0f), m.getFloat("damage", 10.0f)));
                    } else if ("lifetime".equals(type)) {
                        b.addModifier(new LifetimeModifier(m.getFloat("seconds", 2.0f)));
                    } else if ("penetration".equals(type)) {
                        b.addModifier(new PenetrationModifier(m.getInt("count", 1)));
                    }
                }
            }
            // Apply recoil from JSON if it exists
            if (params.get("recoil") != null) {
                com.badlogic.gdx.utils.JsonValue recoil = params.get("recoil");
                b.setRecoil(recoil.getFloat("force", 0.0f), recoil.getFloat("recovery", 0.0f));
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
                    } else if ("melee_knockback".equals(type)) {
                        b.addModifier(new MeleeKnockbackModifier(m.getFloat("force", 10.0f)));
                    }
                }
            }
            return b;
        });
    }
}
