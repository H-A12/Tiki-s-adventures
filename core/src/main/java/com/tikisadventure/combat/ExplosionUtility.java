package com.tikisadventure.combat;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.traits.Knockbackable;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.DamageType;

public class ExplosionUtility {

    public static void explode(EffectManager effectManager, Vector2 pos, String profileName, float radius, float damage, float knockback, Array<Entity> enemies) {
        spawnVisuals(effectManager, pos, profileName);
        applyCombat(pos, radius, damage, knockback, enemies);
    }

    public static void spawnVisuals(EffectManager effectManager, Vector2 pos, String profileName) {
        if (effectManager == null) return;

        EffectManager.ExplosionProfile profile = effectManager.getExplosionProfile(profileName);
        if (profile == null) profile = effectManager.getExplosionProfile("STANDARD");

        effectManager.spawnEffect("EXPLOSION_SPRITESHEET", pos, new Vector2(0, 0));

        if (profile != null) {
            effectManager.spawnSingleParticle(profile.flash, pos, new Vector2(0, 0));
            
            for (int i = 0; i < 8; i++) {
                Vector2 randomDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                Vector2 offsetPos = new Vector2(pos).add(MathUtils.random(-0.2f, 0.2f), MathUtils.random(-0.2f, 0.2f));
                effectManager.spawnSingleParticle(profile.smoke, offsetPos, randomDir.scl(0.5f));
            }
            for (int i = 0; i < 15; i++) {
                Vector2 sparkDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                sparkDir.scl(MathUtils.random(3f, 6f));
                effectManager.spawnSingleParticle(profile.sparks, pos, sparkDir);
            }
        }
    }

    public static void applyCombat(Vector2 pos, float radius, float damage, float knockback, Array<Entity> enemies) {
        for (Entity enemy : enemies) {
            if (enemy.isAlive()) {
                float distance = pos.dst(enemy.getPosition());

                if (distance <= radius) {
                    enemy.receiveDamage(damage, false, DamageType.EXPLOSIVE);

                    Vector2 pushDir = new Vector2(enemy.getPosition()).sub(pos).nor();
                    if (pushDir.len() == 0) pushDir.set(1, 0);

                    float intensity = 1.0f - (distance / radius);
                    float finalForce = knockback * intensity;

                    if (enemy instanceof Knockbackable) {
                        ((Knockbackable) enemy).getKnockbackVelocity()
                            .add(pushDir.nor().scl(finalForce));
                    }
                }
            }
        }
    }
}
