package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.entities.base.Entity;

public class CastleBossBehavior implements EnemyBehavior {

    public enum BossState {
        PATROL, PUNCH, CAST, DYING
    }

    public static class CastleSpell {
        public Vector2 position = new Vector2();
        public boolean active = false;
        public float timer = 0;
        public boolean hasDealtDamage = false;
        public Animation<TextureRegion> animation;
    }

    private static final float FRAME_DURATION = 0.083f;
    private static final float PATROL_Y_SPEED = 2.0f;
    private static final float PUNCH_RANGE = 5.0f;
    private static final float CAST_RANGE = 7.0f;
    private static final float PUNCH_WINDUP = 5f * FRAME_DURATION;
    private static final float PUNCH_TOTAL = 10f * FRAME_DURATION;
    private static final float CAST_SPAWN_TIME = 6f * FRAME_DURATION;
    private static final float CAST_TOTAL = 9f * FRAME_DURATION;
    private static final float SPELL_TOTAL = 16f * FRAME_DURATION;
    private static final float DEATH_DURATION = 10f * FRAME_DURATION;

    private BossState state = BossState.PATROL;
    private float speed;
    private float attackDamage;
    private float attackRange;
    private float attackCooldown;

    private float stateTimer = 0;
    private float attackCooldownTimer = 0;
    private float patrolYDir = 1;

    private boolean dying = false;
    private boolean hasDealtPunchDamage = false;
    private boolean spellSpawned = false;
    private CastleSpell spell = new CastleSpell();

    public CastleBossBehavior(float speed, float attackDamage, float attackRange, float attackCooldown) {
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
    }

    public void setSpellAnimation(Animation<TextureRegion> anim) {
        this.spell.animation = anim;
    }

    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (dying) {
            stateTimer += delta;
            return;
        }

        stateTimer += delta;

        if (attackCooldownTimer > 0) attackCooldownTimer -= delta;

        if (spell.active) {
            spell.timer += delta;
            if (spell.timer >= SPELL_TOTAL) {
                spell.active = false;
            }
        }

        switch (state) {
            case PATROL:
                updatePatrol(enemy, target, delta);
                break;
            case PUNCH:
                updatePunch(enemy, target, delta);
                break;
            case CAST:
                updateCast(enemy, target, delta);
                break;
            case DYING:
                break;
        }
    }

    private void enterState(Entity enemy, BossState newState) {
        state = newState;
        stateTimer = 0;
        enemy.setStateTime(0);
        if (newState == BossState.PUNCH) {
            hasDealtPunchDamage = false;
        }
        if (newState == BossState.CAST) {
            spellSpawned = false;
        }
        if (newState == BossState.PATROL) {
            spell.active = false;
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

        if (dist > CAST_RANGE && attackCooldownTimer <= 0) {
            enterState(enemy, BossState.CAST);
            return;
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

    private void updateCast(Entity enemy, Entity target, float delta) {
        if (target != null) {
            float dx = target.getPosition().x - enemy.getPosition().x;
            if (Math.abs(dx) > 0.3f) {
                enemy.setMirarDerecha(dx < 0);
            }
            if (!spellSpawned && stateTimer >= CAST_SPAWN_TIME) {
                spellSpawned = true;
                spell.active = true;
                spell.timer = 0;
                spell.hasDealtDamage = false;
                spell.position.set(target.getPosition().x, target.getPosition().y + 5f);
            }
        }
        if (stateTimer >= CAST_TOTAL) {
            attackCooldownTimer = attackCooldown;
            enterState(enemy, BossState.PATROL);
        }
    }

    public CastleSpell getActiveSpell() {
        return spell.active ? spell : null;
    }

    public TextureRegion getSpellFrame() {
        if (!spell.active || spell.animation == null) return null;
        return spell.animation.getKeyFrame(spell.timer);
    }

    public BossState getCurrentState() { return state; }

    public void startDying() {
        dying = true;
        spell.active = false;
        state = BossState.DYING;
        stateTimer = 0;
    }

    public boolean isDying() { return dying; }

    public boolean isDeathAnimationComplete() {
        return dying && stateTimer >= DEATH_DURATION;
    }

    public boolean isAttacking() {
        return state == BossState.PUNCH || state == BossState.CAST;
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
