package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class ChaserBehavior implements EnemyBehavior {

    private float attackRange;
    private float attackDamage;
    private float attackCooldown;
    private float currentCooldown = 0;
    private float speed;

    public ChaserBehavior(float speed, float attackDamage, float attackRange, float attackCooldown) {
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
    }

    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (enemy == null || target == null || !enemy.isAlive()) return;

        // Actualizar cooldown
        if (currentCooldown > 0) {
            currentCooldown -= delta;
        }

        // Calcular dirección hacia el jugador
        Vector2 direction = new Vector2(
            target.getPosition().x - enemy.getPosition().x,
            target.getPosition().y - enemy.getPosition().y
        );

        float distance = direction.len();

        if (distance > 0.1f) {
            direction.nor();

            // Si está fuera del rango de ataque, perseguir
            if (distance > attackRange) {
                // Set velocity instead of modifying position directly
                enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.set(direction).scl(speed);
                enemy.setEstado(Entity.Estado.walking);
            } else {
                // Está en rango, atacar
                enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
                if (currentCooldown <= 0) {
                    currentCooldown = attackCooldown;
                }
                enemy.setEstado(Entity.Estado.idle);
            }

            // Mirar dirección
            enemy.setMirarDerecha(direction.x >= 0);
        } else {
             enemy.getComponent(com.tikisadventure.components.VelocityComponent.class).velocidad.setZero();
        }

        enemy.actualizarHitboxes();
    }

    @Override
    public float getAttackRange() {
        return attackRange;
    }

    @Override
    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    public float getAttackCooldown() {
        return attackCooldown;
    }

    @Override
    public String getBehaviorType() {
        return "chaser";
    }

    public float getSpeed() {
        return speed;
    }

    public void resetCooldown() {
        currentCooldown = 0;
    }

    public boolean canAttack() {
        return currentCooldown <= 0;
    }
}
