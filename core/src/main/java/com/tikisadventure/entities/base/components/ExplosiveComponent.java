package com.tikisadventure.entities.base.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;

public class ExplosiveComponent implements Component {
    private final EffectManager effectManager;
    private final float explosionDamage;
    private final float explosionRadius;
    private final float knockbackForce;
    private final int smokeCount;
    private final int sparkCount;

    private boolean hasExploded = false;

    public ExplosiveComponent(EffectManager effectManager, float damage, float radius,
                            float force, int smokes, int sparks) {
        this.effectManager = effectManager;
        this.explosionDamage = damage;
        this.explosionRadius = radius;
        this.knockbackForce = force;
        this.smokeCount = smokes;
        this.sparkCount = sparks;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (owner instanceof Killable) {
            Killable killable = (Killable) owner;
            if (!killable.isAlive() && !hasExploded) {
                explode(owner, entities);
                hasExploded = true;
            }
        }
    }

    private void explode(Object owner, Array<Entity> entities) {
        if (effectManager == null) return;
        if (!(owner instanceof HasPosition)) return;

        Vector2 pos = ((HasPosition) owner).getPosition();

        effectManager.spawnEffect(EffectType.EXPLOSION_FLASH, pos, new Vector2(0, 0));

        for (int i = 0; i < smokeCount; i++) {
            Vector2 offset = new Vector2(pos).add(
                MathUtils.random(-0.3f, 0.3f),
                MathUtils.random(-0.3f, 0.3f)
            );
            Vector2 smokeDir = new Vector2(
                MathUtils.random(-1f, 1f),
                MathUtils.random(-1f, 1f)
            ).scl(0.5f);
            effectManager.spawnEffect(EffectType.EXPLOSION_HUMO, offset, smokeDir);
        }

        for (int i = 0; i < sparkCount; i++) {
            Vector2 sparkDir = new Vector2(
                MathUtils.random(-1f, 1f),
                MathUtils.random(-1f, 1f)
            ).nor().scl(MathUtils.random(4f, 8f));
            effectManager.spawnEffect(EffectType.EXPLOSION_CHISPA, pos, sparkDir);
        }

        for (Entity enemy : entities) {
            if (enemy.isAlive()) {
                float distance = pos.dst(enemy.getPosicion());

                if (distance <= explosionRadius) {
                    enemy.receiveDamage(explosionDamage);

                    Vector2 pushDir = new Vector2(enemy.getPosicion()).sub(pos).nor();
                    if (pushDir.len() == 0) pushDir.set(1, 0);

                    float intensity = 1.0f - (distance / explosionRadius);
                    float finalForce = knockbackForce * intensity;

                    if (enemy instanceof Knockbackable) {
                        ((Knockbackable) enemy).getKnockbackVelocity()
                            .add(pushDir.nor().scl(finalForce));
                    }
                }
            }
        }
    }

    public void reset() {
        hasExploded = false;
    }
}
