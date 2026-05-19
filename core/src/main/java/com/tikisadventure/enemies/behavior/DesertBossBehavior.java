package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public class DesertBossBehavior implements EnemyBehavior {

    public enum BossState {
        PATROL, DASH_APPROACH, DASH_RETREAT, PUNCH, SHOOT, DYING
    }

    public static final float SHOOT_FRAME_DURATION = 0.08f;
    private static final float SHOOT_FIRE_TIME = 9f * SHOOT_FRAME_DURATION;
    private static final float SHOOT_TOTAL_DURATION = 21f * SHOOT_FRAME_DURATION;

    private static final float PATROL_Y_SPEED = 3.0f;
    private static final float APPROACH_DISTANCE = 6.0f;
    private static final float PUNCH_RANGE = 2.5f;
    private static final float SHOOT_ALIGN_THRESHOLD = 2.0f;
    private static final float DASH_DURATION = 0.35f;
    private static final float PUNCH_WINDUP = 0.25f;
    private static final float PUNCH_TOTAL = 0.55f;
    private static final float DODGE_COOLDOWN = 2.5f;
    private static final float DAMAGE_WINDOW_DURATION = 2.0f;
    private static final float DAMAGE_THRESHOLD = 500f;
    private static final float DEATH_DURATION = 1.5f;

    public static final float BEAM_DURATION = 1.0f;
    private static final float BEAM_SHOOT_PHASE = 0.33f;
    private static final float BEAM_FADE1_PHASE = 0.66f;
    public static final float BEAM_RADIUS = 0.7f;
    public static final float BEAM_LENGTH = 50f;
    public static final float BEAM_HEIGHT = 0.8f;

    public static class LaserBeam {
        public Vector2 position = new Vector2();
        public boolean active = false;
        public float timer = 0;
        public boolean facingRight = true;
    }

    private BossState state = BossState.PATROL;
    private float speed;
    private float attackDamage;
    private float attackRange;
    private float attackCooldown;
    private float dashSpeed;
    private float projectileDamage;

    private float stateTimer = 0;
    private float attackCooldownTimer = 0;
    private float dodgeCooldownTimer = 0;
    private float patrolYDir = 1;
    private float damageTakenWindow = 0;
    private float damageWindowTimer = 0;

    private boolean dying = false;
    private boolean hasDealtPunchDamage = false;
    private boolean beamFired = false;
    private LaserBeam beam = new LaserBeam();

    private TextureRegion laserShootTex;
    private TextureRegion laserFade1Tex;
    private TextureRegion laserFade2Tex;
    private EffectManager effectManager;
    private Array<Projectile> enemyProjectiles;

    public DesertBossBehavior(float speed, float attackDamage, float attackRange,
                              float attackCooldown, float dashSpeed,
                              float projectileSpeed, float projectileDamage,
                              float projectileRadius) {
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.dashSpeed = dashSpeed;
        this.projectileDamage = projectileDamage;
    }

    public void setLaserTextures(TextureRegion shoot, TextureRegion fade1, TextureRegion fade2) {
        this.laserShootTex = shoot;
        this.laserFade1Tex = fade1;
        this.laserFade2Tex = fade2;
    }

    public void setEffectManager(EffectManager em) { this.effectManager = em; }
    public void setEnemyProjectiles(Array<Projectile> projectiles) { this.enemyProjectiles = projectiles; }

    public void receiveDamageNotice(float amount) {
        damageTakenWindow += amount;
    }

    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (dying) {
            stateTimer += delta;
            return;
        }

        stateTimer += delta;

        if (attackCooldownTimer > 0) attackCooldownTimer -= delta;
        if (dodgeCooldownTimer > 0) dodgeCooldownTimer -= delta;

        damageWindowTimer += delta;
        if (damageWindowTimer >= DAMAGE_WINDOW_DURATION) {
            damageTakenWindow = 0;
            damageWindowTimer = 0;
        }

        if (damageTakenWindow >= DAMAGE_THRESHOLD && state != BossState.DYING && state != BossState.DASH_RETREAT) {
            damageTakenWindow = 0;
            damageWindowTimer = 0;
            enterState(enemy, BossState.DASH_RETREAT);
            return;
        }

        if (beam.active) {
            beam.timer += delta;
            if (beam.timer >= BEAM_DURATION) {
                beam.active = false;
            }
        }

        switch (state) {
            case PATROL:
                updatePatrol(enemy, target, delta);
                break;
            case DASH_APPROACH:
                updateDashApproach(enemy, target, delta);
                break;
            case DASH_RETREAT:
                updateDashRetreat(enemy, target, delta);
                break;
            case PUNCH:
                updatePunch(enemy, target, delta);
                break;
            case SHOOT:
                updateShoot(enemy, target, delta);
                break;
            case DYING:
                break;
        }
    }

    private void enterState(Entity enemy, BossState newState) {
        if (state == BossState.SHOOT && newState != BossState.SHOOT && newState != BossState.DYING) {
            beam.active = false;
        }
        state = newState;
        stateTimer = 0;
        enemy.setStateTime(0);
        if (newState == BossState.PUNCH) {
            hasDealtPunchDamage = false;
        }
        if (newState == BossState.SHOOT) {
            beamFired = false;
            beam.active = false;
        }
    }

    private void updatePatrol(Entity enemy, Entity target, float delta) {
        if (target == null) return;

        float dx = target.getPosition().x - enemy.getPosition().x;
        float dy = target.getPosition().y - enemy.getPosition().y;
        float dist = enemy.getPosition().dst(target.getPosition());

        float moveX = Math.signum(dx) * speed * delta * 0.7f;
        enemy.getPosition().x += moveX;

        patrolYDir = MathUtils.random() < 0.005f ? -patrolYDir : patrolYDir;
        float yMove = patrolYDir * PATROL_Y_SPEED * delta;
        enemy.getPosition().y += yMove;

        if (Math.abs(dx) > 0.3f) {
            enemy.setMirarDerecha(dx < 0);
        }

        if (dist < PUNCH_RANGE && attackCooldownTimer <= 0) {
            enterState(enemy, BossState.PUNCH);
            return;
        }

        if (Math.abs(dy) < SHOOT_ALIGN_THRESHOLD && attackCooldownTimer <= 0 && MathUtils.random() < 0.01f) {
            enterState(enemy, BossState.SHOOT);
            return;
        }

        if (dist > APPROACH_DISTANCE && attackCooldownTimer <= 0) {
            enterState(enemy, BossState.DASH_APPROACH);
            return;
        }
    }

    private void updateDashApproach(Entity enemy, Entity target, float delta) {
        if (target != null) {
            Vector2 dir = new Vector2(target.getPosition()).sub(enemy.getPosition()).nor();
            enemy.getPosition().mulAdd(dir, dashSpeed * delta);
            if (Math.abs(dir.x) > 0.3f) {
                enemy.setMirarDerecha(dir.x < 0);
            }
        }
        if (stateTimer >= DASH_DURATION) {
            enterState(enemy, BossState.PATROL);
        }
    }

    private void updateDashRetreat(Entity enemy, Entity target, float delta) {
        if (target != null) {
            Vector2 away = new Vector2(enemy.getPosition()).sub(target.getPosition()).nor();
            enemy.getPosition().mulAdd(away, dashSpeed * delta);
            if (Math.abs(away.x) > 0.3f) {
                enemy.setMirarDerecha(away.x < 0);
            }
        }
        if (stateTimer >= DASH_DURATION * 0.8f) {
            enterState(enemy, BossState.PATROL);
        }
    }

    private void updatePunch(Entity enemy, Entity target, float delta) {
        if (target != null) {
            float dx = target.getPosition().x - enemy.getPosition().x;
            if (Math.abs(dx) > 0.3f) {
                enemy.setMirarDerecha(dx < 0);
            }
        }
        if (stateTimer >= PUNCH_WINDUP && !hasDealtPunchDamage && target != null) {
            float dist = enemy.getPosition().dst(target.getPosition());
            if (dist <= attackRange + 1.5f) {
                target.receiveDamage(attackDamage, false, DamageType.KINETIC);
                hasDealtPunchDamage = true;
            }
        }
        if (stateTimer >= PUNCH_TOTAL) {
            attackCooldownTimer = attackCooldown;
            enterState(enemy, BossState.PATROL);
        }
    }

    private void updateShoot(Entity enemy, Entity target, float delta) {
        float dx = 0;
        if (target != null) {
            dx = target.getPosition().x - enemy.getPosition().x;
            if (Math.abs(dx) > 0.3f) {
                enemy.setMirarDerecha(dx < 0);
            }
        }

        if (!beamFired && stateTimer >= SHOOT_FIRE_TIME) {
            beamFired = true;
            beam.active = true;
            beam.timer = 0;
            beam.position.set(enemy.getPosition());
            beam.facingRight = !enemy.isMirarDerecha();
        }

        if (stateTimer >= SHOOT_TOTAL_DURATION) {
            beam.active = false;
            attackCooldownTimer = attackCooldown;
            enterState(enemy, BossState.PATROL);
        }
    }

    public TextureRegion getBeamTexture() {
        if (!beam.active) return null;
        float t = beam.timer;
        if (t < BEAM_SHOOT_PHASE) return laserShootTex;
        if (t < BEAM_FADE1_PHASE) return laserFade1Tex;
        return laserFade2Tex;
    }

    public LaserBeam getActiveBeam() {
        return beam.active ? beam : null;
    }

    public BossState getCurrentState() { return state; }

    public void startDying() {
        dying = true;
        beam.active = false;
        state = BossState.DYING;
        stateTimer = 0;
    }

    public boolean isDying() { return dying; }

    public boolean isDeathAnimationComplete() {
        return dying && stateTimer >= DEATH_DURATION;
    }

    public boolean isAttacking() {
        return state == BossState.PUNCH || state == BossState.SHOOT;
    }

    public boolean isShooting() {
        return state == BossState.SHOOT;
    }

    @Override
    public float getAttackRange() { return attackRange; }

    @Override
    public float getAttackDamage() { return attackDamage; }

    @Override
    public float getAttackCooldown() { return attackCooldown; }

    @Override
    public String getBehaviorType() { return "desert_boss"; }
}
