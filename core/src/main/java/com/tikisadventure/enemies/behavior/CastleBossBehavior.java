package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.entities.base.Entity;

//Comportamiento del jefe del castillo: vuela y carga contra el jugador.
//Alterna entre estados FLIGHT (volar) y ATTACK (cargar).
public class CastleBossBehavior implements EnemyBehavior {

    public enum BossState {
        FLIGHT, ATTACK, DYING
    }

    private BossState state = BossState.FLIGHT;
    private float speed;
    private float attackDamage;
    private float attackRange;
    private float attackCooldown;
    private float chargeSpeed;

    private float stateTimer = 0;
    private float attackCooldownTimer = 0;

    private static final float FLIGHT_DURATION = 2.0f;
    private static final float ATTACK_DURATION = 0.8f;
    private static final float DEATH_DURATION = 1.5f;
    private static final float FLIGHT_Y_SPEED = 2.0f;
    private static final float PREFERRED_DISTANCE = 5.0f;

    private boolean dying = false;
    private boolean hasDealtAttackDamage = false;

    public CastleBossBehavior(float speed, float attackDamage, float attackRange, float attackCooldown, float chargeSpeed) {
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.chargeSpeed = chargeSpeed;
    }

    //Actualizar según el estado actual: FLIGHT, ATTACK o DYING
    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (dying) {
            stateTimer += delta;
            return;
        }

        stateTimer += delta;

        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }

        switch (state) {
            case FLIGHT:
                updateFlight(enemy, target, delta);
                break;
            case ATTACK:
                updateAttack(enemy, target, delta);
                break;
            case DYING:
                break;
        }
    }

    private void enterState(Entity enemy, BossState newState) {
        state = newState;
        stateTimer = 0;
        enemy.setStateTime(0);
        if (newState == BossState.ATTACK) {
            hasDealtAttackDamage = false;
        }
    }

    private void updateFlight(Entity enemy, Entity target, float delta) {
        if (target == null) return;

        float dx = target.getPosition().x - enemy.getPosition().x;
        float dy = target.getPosition().y - enemy.getPosition().y;
        float dist = enemy.getPosition().dst(target.getPosition());

        float moveX;
        if (dist > PREFERRED_DISTANCE + 1.0f) {
            moveX = Math.signum(dx) * speed * delta * 0.7f;
        } else if (dist < PREFERRED_DISTANCE - 1.0f) {
            moveX = -Math.signum(dx) * speed * delta * 0.5f;
        } else {
            moveX = 0;
        }
        enemy.getPosition().x += moveX;

        float targetY = target.getPosition().y + 1.5f;
        float yDiff = targetY - enemy.getPosition().y;
        if (Math.abs(yDiff) > 0.3f) {
            float ySpeed = Math.min(FLIGHT_Y_SPEED, Math.abs(yDiff) * 2.0f);
            enemy.getPosition().y += Math.signum(yDiff) * ySpeed * delta;
        }

        if (Math.abs(dx) > 0.3f) {
            enemy.setMirarDerecha(dx < 0);
        }

        if (stateTimer >= FLIGHT_DURATION && attackCooldownTimer <= 0) {
            enterState(enemy, BossState.ATTACK);
        }
    }

    private void updateAttack(Entity enemy, Entity target, float delta) {
        if (target != null) {
            float dx = target.getPosition().x - enemy.getPosition().x;
            if (Math.abs(dx) > 0.3f) {
                enemy.setMirarDerecha(dx < 0);
            }

            if (stateTimer < 0.25f) {
                Vector2 dir = new Vector2(target.getPosition()).sub(enemy.getPosition()).nor();
                enemy.getPosition().mulAdd(dir, chargeSpeed * delta);
            } else {
                Vector2 dir = new Vector2(enemy.getPosition()).sub(target.getPosition()).nor();
                enemy.getPosition().mulAdd(dir, chargeSpeed * delta * 0.3f);
            }

            if (!hasDealtAttackDamage && stateTimer >= 0.2f) {
                float dist = enemy.getPosition().dst(target.getPosition());
                if (dist < attackRange + 1.0f) {
                    target.receiveDamage(attackDamage, false, DamageType.KINETIC);
                    hasDealtAttackDamage = true;
                }
            }
        }

        if (stateTimer >= ATTACK_DURATION) {
            attackCooldownTimer = attackCooldown;
            enterState(enemy, BossState.FLIGHT);
        }
    }

    public BossState getCurrentState() {
        return state;
    }

    //Iniciar animación de muerte
    public void startDying() {
        dying = true;
        state = BossState.DYING;
        stateTimer = 0;
    }

    public boolean isDying() {
        return dying;
    }

    //Comprobar si la animación de muerte ha terminado
    public boolean isDeathAnimationComplete() {
        return dying && stateTimer >= DEATH_DURATION;
    }

    @Override
    public float getAttackRange() { return attackRange; }

    @Override
    public float getAttackDamage() { return attackDamage; }

    @Override
    public float getAttackCooldown() { return attackCooldown; }

    @Override
    public String getBehaviorType() { return "castle_boss"; }
}
