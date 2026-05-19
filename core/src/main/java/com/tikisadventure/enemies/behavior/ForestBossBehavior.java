package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.entities.base.Entity;

public class ForestBossBehavior implements EnemyBehavior {

    public enum BossState {
        HOVERING, DIVING_START, DIVING_FALL, DIVING_LAND, RISING, DYING
    }

    private BossState state = BossState.HOVERING;
    private float speed;
    private float attackDamage;
    private float attackRange;
    private float attackCooldown;
    private float hoverHeight;
    private float diveSpeed;

    private float stateTimer = 0;
    private float attackCooldownTimer = 0;

    private static final float HOVER_DURATION = 2.5f;
    private static final float DIVE_START_DURATION = 0.5f;
    private static final float DIVE_FALL_DURATION = 1.0f;
    private static final float DIVE_LAND_DURATION = 0.2f;
    private static final float RISE_DURATION = 0.5f;
    private static final float DEATH_DURATION = 1.5f;

    private boolean dying = false;
    private boolean hasDealtLandingDamage = false;

    public ForestBossBehavior(float speed, float attackDamage, float attackRange, float attackCooldown, float hoverHeight, float diveSpeed) {
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.hoverHeight = hoverHeight;
        this.diveSpeed = diveSpeed;
    }

    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (dying) {
            stateTimer += delta;
            return;
        }

        stateTimer += delta;

        switch (state) {
            case HOVERING:
                if (target != null) {
                    float targetY = target.getPosition().y + 2.0f + hoverHeight;
                    float dy = targetY - enemy.getPosition().y;
                    if (Math.abs(dy) > 0.2f) {
                        float ySpeed = Math.min(speed, Math.abs(dy) * 3.0f);
                        enemy.getPosition().y += Math.signum(dy) * ySpeed * delta;
                    }
                    float dx = target.getPosition().x - enemy.getPosition().x;
                    float absDx = Math.abs(dx);
                    if (absDx > 4.0f + 0.3f) {
                        float extra = absDx - 4.0f;
                        float xSpeed = Math.min(speed * 0.8f, extra * 1.5f);
                        enemy.getPosition().x += Math.signum(dx) * xSpeed * delta;
                    } else if (absDx < 1.6f && absDx > 0.3f) {
                        enemy.getPosition().x -= Math.signum(dx) * speed * delta * 0.4f;
                    }
                    enemy.setMirarDerecha(dx >= 0);
                }
                if (stateTimer >= HOVER_DURATION && (target == null || enemy.getPosition().y >= target.getPosition().y)) {
                    state = BossState.DIVING_START;
                    stateTimer = 0;
                }
                break;

            case DIVING_START:
                if (target != null) {
                    enemy.getPosition().y += diveSpeed * delta * 0.4f;
                    float dx = enemy.getPosition().x - target.getPosition().x;
                    if (Math.abs(dx) < 5.0f) {
                        float awayDir = dx >= 0 ? 1 : -1;
                        if (Math.abs(dx) < 0.5f) awayDir = 1;
                        enemy.getPosition().x += awayDir * diveSpeed * delta * 0.4f;
                    }
                }
                if (stateTimer >= DIVE_START_DURATION) {
                    state = BossState.DIVING_FALL;
                    stateTimer = 0;
                    hasDealtLandingDamage = false;
                }
                break;

            case DIVING_FALL:
                if (target != null) {
                    float dx = target.getPosition().x - enemy.getPosition().x;
                    float absDx = Math.abs(dx);
                    if (absDx > 0.3f) {
                        float xSpeed = Math.min(diveSpeed, absDx * 3.0f);
                        enemy.getPosition().x += Math.signum(dx) * xSpeed * delta;
                    }
                    enemy.getPosition().y -= diveSpeed * delta;
                    enemy.setMirarDerecha(dx >= 0);
                    if (!hasDealtLandingDamage) {
                        float dist = enemy.getPosition().dst(target.getPosition());
                        if (dist < attackRange) {
                            target.receiveDamage(attackDamage, false, DamageType.KINETIC);
                            hasDealtLandingDamage = true;
                        }
                    }
                }
                if (stateTimer >= DIVE_FALL_DURATION) {
                    state = BossState.DIVING_LAND;
                    stateTimer = 0;
                }
                break;

            case DIVING_LAND:
                if (!hasDealtLandingDamage && target != null) {
                    float dist = enemy.getPosition().dst(target.getPosition());
                    if (dist < attackRange) {
                        target.receiveDamage(attackDamage, false, DamageType.KINETIC);
                        hasDealtLandingDamage = true;
                    }
                }
                if (stateTimer >= DIVE_LAND_DURATION) {
                    state = BossState.RISING;
                    stateTimer = 0;
                }
                break;

            case RISING:
                if (target != null) {
                    Vector2 dir = new Vector2(target.getPosition()).sub(enemy.getPosition());
                    float dist = dir.len();
                    float riseSpeed = Math.min(speed * 0.6f, dist * 2.0f);
                    if (dist > 0.3f) {
                        dir.nor().scl(riseSpeed * delta);
                        enemy.getPosition().add(dir);
                    }
                    enemy.setMirarDerecha(dir.x >= 0);
                }
                if (stateTimer >= RISE_DURATION) {
                    state = BossState.HOVERING;
                    stateTimer = 0;
                }
                break;

            case DYING:
                break;
        }

        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }
    }

    public float getVisualOffsetY() {
        return 0;
    }

    public BossState getCurrentState() {
        return state;
    }

    public void startDying() {
        dying = true;
        state = BossState.DYING;
        stateTimer = 0;
    }

    public boolean isDying() {
        return dying;
    }

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
    public String getBehaviorType() { return "forest_boss"; }
}
