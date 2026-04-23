package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RangedBehavior implements EnemyBehavior {

    public enum RangedState {
        CHASE,
        ATTACKING
    }

    private float speed;
    private float detectionRange;
    private float attackCooldown;
    private float projectileSpeed;
    private float projectileDamage;
    private float projectileRadius = 0.3f;
    private String projectileSprite;
    private TextureRegion projectileTexture;
    
    private RangedState currentState = RangedState.CHASE;
    private float currentCooldown = 0;
    private boolean isFiring = false;
    private float firingTimer = 0f;
    private boolean isShowingAttackFrame = false;
    
    private EffectManager effectManager;
    private Array<Projectile> enemyProjectiles;

    public RangedBehavior(float speed, float detectionRange, float attackCooldown, 
                    float projectileSpeed, float projectileDamage, String projectileSprite) {
        this.speed = speed;
        this.detectionRange = detectionRange;
        this.attackCooldown = attackCooldown;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.projectileSprite = projectileSprite;
    }

    public void setEffectManager(EffectManager em) { this.effectManager = em; }
    public void setEnemyProjectiles(Array<Projectile> projectiles) { this.enemyProjectiles = projectiles; }
    public void setProjectileRadius(float radius) { this.projectileRadius = radius; }

    @Override
    public void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies) {
        if (enemy == null || target == null || !enemy.isAlive()) return;

        if (isFiring) {
            firingTimer += delta;
            if (firingTimer > 0.3f) {
                isFiring = false;
                isShowingAttackFrame = false;
                firingTimer = 0;
            }
        }
        
        float distToTarget = enemy.getPosition().dst(target.getPosition());
        boolean canSeeTarget = distToTarget <= detectionRange;

        if (canSeeTarget) {
            currentState = RangedState.ATTACKING;
        } else {
            currentState = RangedState.CHASE;
        }

        if (currentState == RangedState.CHASE) {
            updateChase(enemy, target, delta);
        } else {
            updateAttack(enemy, target, delta);
        }
    }

    private void updateChase(Entity enemy, Entity target, float delta) {
        Vector2 direction = new Vector2(
            target.getPosition().x - enemy.getPosition().x,
            target.getPosition().y - enemy.getPosition().y
        ).nor();
        
        enemy.getPosition().mulAdd(direction, speed * delta);
        enemy.setEstado(Entity.Estado.walking);
        enemy.setMirarDerecha(direction.x > 0);
    }

    private void updateAttack(Entity enemy, Entity target, float delta) {
        Vector2 direction = new Vector2(
            target.getPosition().x - enemy.getPosition().x,
            target.getPosition().y - enemy.getPosition().y
        );
        enemy.setMirarDerecha(direction.x > 0);
        enemy.setEstado(Entity.Estado.idle);
        
        if (currentCooldown <= 0) {
            fireProjectile(enemy, direction.nor());
            currentCooldown = attackCooldown;
            isFiring = true;
            firingTimer = 0;
            isShowingAttackFrame = true;
        } else {
            currentCooldown -= delta;
        }
    }

    private void fireProjectile(Entity enemy, Vector2 direction) {
        if (enemyProjectiles == null || projectileTexture == null) return;
        Projectile projectile = new Projectile(enemy, new Vector2(enemy.getPosition()), direction, projectileSpeed, projectileDamage, 0f, 1f, projectileRadius, projectileTexture, effectManager, null, 0);
        projectile.setOwner(enemy);
        enemyProjectiles.add(projectile);
    }

    public void loadProjectileTexture() {
        if (projectileSprite != null) {
            // Support both old format "shared_slime" and new format "folder/sprite"
            if (projectileSprite.contains("/")) {
                projectileTexture = Assets.getRegion("shared", projectileSprite);
            } else {
                String[] parts = projectileSprite.split("_", 2);
                projectileTexture = Assets.getRegion(parts.length > 1 ? parts[0] : "shared", parts.length > 1 ? parts[1] : projectileSprite);
            }
        }
    }

    public boolean isAttacking() { return isShowingAttackFrame; }
    public boolean isFiring() { return isFiring; }
    public boolean isDetected() { return currentState == RangedState.ATTACKING && !isFiring; }
    @Override public float getAttackRange() { return detectionRange; }
    @Override public float getAttackDamage() { return projectileDamage; }
    @Override public float getAttackCooldown() { return attackCooldown; }
    @Override public String getBehaviorType() { return "ranged"; }
}