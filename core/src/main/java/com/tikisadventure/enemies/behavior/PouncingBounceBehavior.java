package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class PouncingBounceBehavior implements EnemyBehavior {

    public enum PounceState {
        APPROACHING,
        TRANSFORMING,
        WAITING,
        POUNCING,
        BOUNCING
    }

    private float approachSpeed;
    private float attackDamage;
    private float transformDistance;
    private float waitDuration;
    private float pounceSpeed;
    private float bounceForce;
    private float restartDistance;
    private float attackCooldown;
    private float currentCooldown = 0;

    private PounceState currentState = PounceState.APPROACHING;
    private float stateTimer = 0;
    private Vector2 bounceDirection = new Vector2();
    private float visualOffsetY = 0;
    private float floatTimer = 0;
    private float stuckTimer = 0;
    private static final float FLOAT_AMPLITUDE = 0.1f;
    private static final float FLOAT_PERIOD = 1.0f;
    private static final float TRANSFORM_DURATION = 0.5f;
    private static final float STUCK_THRESHOLD = 5.0f;

    public PouncingBounceBehavior(float approachSpeed, float attackDamage, float transformDistance,
                                  float waitDuration, float pounceSpeed, float bounceForce,
                                  float restartDistance, float attackCooldown) {
        this.approachSpeed = approachSpeed;
        this.attackDamage = attackDamage;
        this.transformDistance = transformDistance;
        this.waitDuration = waitDuration;
        this.pounceSpeed = pounceSpeed;
        this.bounceForce = bounceForce;
        this.restartDistance = restartDistance;
        this.attackCooldown = attackCooldown;
    }

    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (enemy == null || target == null || !enemy.isAlive()) return;

        float deltaTime = delta;

        if (currentCooldown > 0) {
            currentCooldown -= deltaTime;
        }

        floatTimer += deltaTime;
        visualOffsetY = (float) Math.sin((float) (floatTimer / FLOAT_PERIOD * 2 * Math.PI)) * FLOAT_AMPLITUDE;

        Vector2 toTarget = new Vector2(
            target.getPosicion().x - enemy.getPosicion().x,
            target.getPosicion().y - enemy.getPosicion().y
        );
        float distance = toTarget.len();

        switch (currentState) {
            case APPROACHING:
                stuckTimer += deltaTime;
                if (distance > transformDistance) {
                    if (stuckTimer > STUCK_THRESHOLD) {
                        currentState = PounceState.TRANSFORMING;
                        stateTimer = 0;
                        stuckTimer = 0;
                        enemy.setEstado(Entity.Estado.idle);
                        break;
                    }
                    
                    toTarget.nor();
                    enemy.getPosicion().mulAdd(toTarget, approachSpeed * deltaTime);
                    enemy.setEstado(Entity.Estado.walking);
                } else {
                    currentState = PounceState.TRANSFORMING;
                    stateTimer = 0;
                    stuckTimer = 0;
                    enemy.setEstado(Entity.Estado.idle);
                }
                break;

            case TRANSFORMING:
                stateTimer += deltaTime;
                if (stateTimer >= TRANSFORM_DURATION) {
                    currentState = PounceState.WAITING;
                    stateTimer = 0;
                }
                break;

            case WAITING:
                stateTimer += deltaTime;
                if (stateTimer >= waitDuration) {
                    currentState = PounceState.POUNCING;
                    toTarget.nor();
                    bounceDirection.set(toTarget);
                }
                enemy.setEstado(Entity.Estado.idle);
                break;

            case POUNCING:
                if (distance > 0.1f) {
                    enemy.getPosicion().mulAdd(bounceDirection, pounceSpeed * deltaTime);
                    enemy.setEstado(Entity.Estado.walking);
                } else {
                    currentState = PounceState.BOUNCING;
                    bounceDirection.set(-bounceDirection.x, -bounceDirection.y);
                    stateTimer = 0;
                }
                break;

            case BOUNCING:
                stateTimer += deltaTime;
                enemy.getPosicion().mulAdd(bounceDirection, bounceForce * deltaTime);
                enemy.setEstado(Entity.Estado.walking);
                
                if (stateTimer >= 0.5f) {
                    currentState = PounceState.APPROACHING;
                    stateTimer = 0;
                }
                break;
        }

        if (toTarget.x != 0) {
            enemy.setMirarDerecha(toTarget.x >= 0);
        }

        enemy.actualizarHitboxes();
    }

    public void triggerBounce(Vector2 direction) {
        currentState = PounceState.BOUNCING;
        stateTimer = 0;
        bounceDirection.set(direction).nor();
    }

    public PounceState getCurrentState() {
        return currentState;
    }

    public float getVisualOffsetY() {
        return visualOffsetY;
    }

    @Override
    public float getAttackRange() {
        return 0.5f;
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
        return "pouncing";
    }

    public void reset() {
        currentState = PounceState.APPROACHING;
        stateTimer = 0;
        currentCooldown = 0;
        floatTimer = 0;
        visualOffsetY = 0;
        stuckTimer = 0;
    }

    public boolean canAttack() {
        return currentCooldown <= 0 && (currentState == PounceState.POUNCING);
    }
}
