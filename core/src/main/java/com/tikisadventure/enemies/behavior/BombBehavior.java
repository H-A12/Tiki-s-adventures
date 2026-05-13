package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.ExplosionUtility;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class BombBehavior implements EnemyBehavior {

    private float speed;
    private float explosionDamage;
    private float explosionRadius;
    private String explosionProfile;
    private EffectManager effectManager;

    private boolean isDetonating = false;
    private boolean hasDealtDamage = false;
    private float detonateTimer = 0f;
    private float triggerRange;
    private static final float DETONATE_WINDUP = 0.4f;

    public BombBehavior(float speed, float explosionDamage, float explosionRadius, float triggerRange, String explosionProfile) {
        this.speed = speed;
        this.explosionDamage = explosionDamage;
        this.explosionRadius = explosionRadius;
        this.triggerRange = triggerRange;
        this.explosionProfile = explosionProfile != null ? explosionProfile : "EXPLOSIVE";
    }

    public void setEffectManager(EffectManager em) {
        this.effectManager = em;
    }

    public boolean isDetonating() {
        return isDetonating;
    }

    @Override
    public boolean isInWindup() {
        return isDetonating && !hasDealtDamage;
    }

    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (enemy == null || target == null || !enemy.isAlive()) return;

        if (target.getHealthComponent() != null && target.getHealthComponent().currentHealth <= 0) {
            if (enemy.getComponent(com.tikisadventure.components.VelocityComponent.class) != null) {
                enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
            }
            enemy.setEstado(Entity.Estado.walking);
            return;
        }

        if (isDetonating) {
            detonateTimer += delta;

            Vector2 dir = new Vector2(
                target.getPosition().x - enemy.getPosition().x,
                target.getPosition().y - enemy.getPosition().y
            );
            if (dir.len() > 0.1f) {
                enemy.setMirarDerecha(dir.x >= 0);
            }

            enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
            enemy.setEstado(Entity.Estado.idle);

            if (!hasDealtDamage && detonateTimer >= DETONATE_WINDUP) {
                hasDealtDamage = true;

                if (effectManager != null) {
                    ExplosionUtility.spawnVisuals(effectManager, enemy.getPosition(), explosionProfile);
                }

                float dist = enemy.getPosition().dst(target.getPosition());
                if (dist <= explosionRadius + 0.5f) {
                    target.receiveDamage(explosionDamage, false, DamageType.KINETIC);
                }

                enemy.setAlive(false);
            }

            enemy.actualizarHitboxes();
            return;
        }

        Vector2 direction = new Vector2(
            target.getPosition().x - enemy.getPosition().x,
            target.getPosition().y - enemy.getPosition().y
        );

        float distance = direction.len();

        if (distance > 0.1f) {
            if (distance <= triggerRange) {
                isDetonating = true;
                hasDealtDamage = false;
                detonateTimer = 0;
                enemy.setStateTime(0);
                enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
                enemy.setEstado(Entity.Estado.idle);
                enemy.setMirarDerecha(direction.x >= 0);
                enemy.actualizarHitboxes();
                return;
            }

            direction.nor();
            enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.set(direction).scl(enemy.getSpeed());
            enemy.setEstado(Entity.Estado.walking);
            enemy.setMirarDerecha(direction.x >= 0);
        } else {
            enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
        }

        enemy.actualizarHitboxes();
    }

    @Override
    public float getAttackRange() { return explosionRadius; }

    @Override
    public float getAttackDamage() { return explosionDamage; }

    @Override
    public float getAttackCooldown() { return 999f; }

    @Override
    public String getBehaviorType() { return "bomb"; }
}
