package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.entities.base.Entity;

//Comportamiento de enemigo que persigue y ataca cuerpo a cuerpo.
//Usa hitboxes físicas y rango de ataque para decidir cuándo golpear.
public class ChaserBehavior implements EnemyBehavior {

    private float attackRange;
    private float attackDamage;
    private float attackCooldown;
    private float currentCooldown = 0;
    private float speed;

    private boolean isAttacking = false;
    private boolean hasDealtDamage = false;
    private float attackStateTime = 0f;

    private static final float ATTACK_WINDUP = 0.25f;
    private static final float ATTACK_TOTAL_DURATION = 0.6f;

    public ChaserBehavior(float speed, float attackDamage, float attackRange, float attackCooldown) {
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
    }

    //Perseguir al jugador y atacar cuerpo a cuerpo si está en rango
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

        if (currentCooldown > 0) {
            currentCooldown -= delta;
        }

        if (isAttacking) {
            attackStateTime += delta;

            enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
            enemy.setEstado(Entity.Estado.idle);

            if (attackStateTime < 0.2f) {
                Vector2 dir = new Vector2(
                    target.getPosition().x - enemy.getPosition().x,
                    target.getPosition().y - enemy.getPosition().y
                );
                if (dir.len() > 0.1f) {
                    enemy.setMirarDerecha(dir.x >= 0);
                }
            }

            if (!hasDealtDamage && attackStateTime >= ATTACK_WINDUP) {
                float dist = enemy.getPosition().dst(target.getPosition());
                float radioFisico = enemy.getHitboxActionTrigger().radius + target.getHitboxActionTrigger().radius;

                if (dist <= Math.max(attackRange, radioFisico) + 0.8f) {
                    target.receiveDamage(attackDamage, false, DamageType.KINETIC);
                    hasDealtDamage = true;
                }
            }

            if (attackStateTime >= ATTACK_TOTAL_DURATION) {
                isAttacking = false;
                hasDealtDamage = false;
                attackStateTime = 0;
                currentCooldown = attackCooldown;
                enemy.setStateTime(0);
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
            direction.nor();

            float radioFisico = enemy.getHitboxActionTrigger().radius + target.getHitboxActionTrigger().radius;

            if (distance > attackRange && distance > radioFisico + 0.05f) {
                enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.set(direction).scl(enemy.getSpeed());
                enemy.setEstado(Entity.Estado.walking);
            } else {
                enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();

                if (currentCooldown <= 0) {
                    isAttacking = true;
                    hasDealtDamage = false;
                    attackStateTime = 0;
                    enemy.setStateTime(0);
                }
                enemy.setEstado(Entity.Estado.idle);
            }

            enemy.setMirarDerecha(direction.x >= 0);
        } else {
            enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
        }

        enemy.actualizarHitboxes();
    }

    public boolean isAttacking() { return isAttacking; }
    @Override public boolean isInWindup() { return isAttacking && !hasDealtDamage; }
    @Override public float getAttackRange() { return attackRange; }
    @Override public float getAttackDamage() { return attackDamage; }
    @Override public float getAttackCooldown() { return attackCooldown; }
    @Override public String getBehaviorType() { return "chaser"; }
    public float getSpeed() { return speed; }
    public void resetCooldown() { currentCooldown = 0; }
    public boolean canAttack() { return currentCooldown <= 0 && !isAttacking; }
}
