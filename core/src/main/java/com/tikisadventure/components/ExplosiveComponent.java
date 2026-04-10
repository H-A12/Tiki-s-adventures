package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.traits.Killable;
import com.tikisadventure.components.traits.Knockbackable;
import com.tikisadventure.components.traits.PositionProvider;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.combat.DamageType;

public class ExplosiveComponent implements Component {
    private final EffectManager effectManager;
    private final float explosionRadius;
    private final float explosionDamage;
    private final float knockbackForce;
    private boolean hasExploded = false;

    public ExplosiveComponent(EffectManager effectManager, float radius, float damage, float knockback) {
        this.effectManager = effectManager;
        this.explosionRadius = radius;
        this.explosionDamage = damage;
        this.knockbackForce = knockback;
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
        if (!(owner instanceof PositionProvider)) return;

        Vector2 pos = ((PositionProvider) owner).getPosition();

        effectManager.spawnEffect(EffectType.EXPLOSION_SPRITESHEET, pos, new Vector2(0, 0));
        effectManager.spawnEffect(EffectType.EXPLOSION_HUMO, pos, new Vector2(0, 0));

        for (Entity enemy : entities) {
            if (enemy.isAlive()) {
                float distance = pos.dst(enemy.getPosicion());

                if (distance <= explosionRadius) {

                    enemy.receiveDamage(explosionDamage, false, DamageType.EXPLOSIVE);

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
}
