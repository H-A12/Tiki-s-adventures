package com.tikisadventure.combat;

import com.tikisadventure.combat.weapons.behaviors.BehaviorRegistry;
import com.tikisadventure.combat.weapons.behaviors.MeleeBehavior;
import com.tikisadventure.combat.weapons.behaviors.ProjectilePatternBehavior;
import com.tikisadventure.combat.weapons.behaviors.ExplosiveModifier;
import com.tikisadventure.combat.weapons.behaviors.LifetimeModifier;
import com.tikisadventure.combat.weapons.behaviors.PenetrationModifier;
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
            
            if (params.get("recoil") != null && !params.get("recoil").isNull()) {
                float recoilForce = params.get("recoil").getFloat("force", 0f);
                float recoilRecovery = params.get("recoil").getFloat("recovery", 8f);
                b.setRecoil(recoilForce, recoilRecovery);
            }
            
            // Check for explosive data in params
            if (params.get("explosive") != null) {
                com.badlogic.gdx.utils.JsonValue exp = params.get("explosive");
                // Note: We need a way to set this on the projectile later in the system
                // But for now, just configure the behavior to know it's explosive
            }

            return b;
        });


        // Register Melee (Simplified for now)
        BehaviorRegistry.register("melee", (params, pc, damage) -> {
            MeleeBehavior b = new MeleeBehavior(
                params.getFloat("range", 1f),
                params.getFloat("arc", 60f),
                params.getFloat("speed", 0.5f),
                params.getFloat("swingRadius", 0.5f),
                params.getFloat("pivotX", 0.5f),
                params.getFloat("pivotY", 0.5f)
            );
            return b;
        });
    }
}
