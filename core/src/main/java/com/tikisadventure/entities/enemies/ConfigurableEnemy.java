package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.enemies.behavior.ChaserBehavior;
import com.tikisadventure.enemies.behavior.EnemyBehavior;
import com.tikisadventure.enemies.behavior.RangedBehavior;
import com.tikisadventure.enemies.behavior.PouncingBounceBehavior;
import com.tikisadventure.systems.WaveSystem;
import com.tikisadventure.combat.projectiles.Projectile;
import com.badlogic.gdx.utils.Array;

public class ConfigurableEnemy extends Entity {

    private EnemyBehavior behavior;
    private static JsonValue enemyConfig;
    private TextureRegion spriteTexture;
    private Animation<TextureRegion> idleAnim = new Animation<>(0.1f);
    private Animation<TextureRegion> walkAnim = new Animation<>(0.15f);
    private Animation<TextureRegion> attackAnim = new Animation<>(0.1f);
    private Animation<TextureRegion> detectedAnim = new Animation<>(0.1f);
    
    private boolean isRanged = false;
    private Array<Projectile> enemyProjectiles;
    private com.tikisadventure.effects.EffectManager effectManager;
    private boolean alive = true;

    static {
        JsonReader reader = new JsonReader();
        enemyConfig = reader.parse(Gdx.files.internal("data/enemy_config.json")).get("enemies");
    }

    public ConfigurableEnemy(String enemyType, WaveSystem waveSystem) {
        JsonValue config = enemyConfig.get(enemyType);

        if (config == null) {
            config = enemyConfig.get("slime");
        }

        // Cargar stats con escalado por oleada
        float baseHealth = config.getFloat("health", 3);
        float baseSpeed = config.getFloat("speed", 2.5f);
        float baseDamage = config.getFloat("damage", 2);
        float baseExperience = config.getFloat("experience", 5);

        int baseScore = config.getInt("score", 5);
        setScoreValue(baseScore);

        float health = Math.round(baseHealth * waveSystem.getDifficultyMultiplier());
        this.healthComponent = new HealthComponent(health);
        this.velocityComponent.speed = baseSpeed * waveSystem.getDifficultyMultiplier();
        setDamage(Math.round(baseDamage * waveSystem.getDifficultyMultiplier()));
        setExperience(Math.round(baseExperience * waveSystem.getDifficultyMultiplier()));

        // Tamaño
        float w = config.getFloat("width", 1);
        float h = config.getFloat("height", 1);
        this.renderComponent = new RenderComponent(null, w, h);
        setANCHO(w);
        setALTO(h);

        // Cargar sprite desde Atlas
        String spriteRaw = config.getString("sprite", "shared_slime");
        String[] parts = spriteRaw.split("_", 2);
        String atlas = "shared";
        String region = spriteRaw;
        if (parts.length > 1) {
            atlas = parts[0];
            region = parts[1];
        }

        try {
            spriteTexture = Assets.getRegion(atlas, region);
            int frameSize = 16;
            int frameCount = spriteTexture.getRegionWidth() / frameSize;
            
            if (frameCount > 1) {
                TextureRegion[] regions = new TextureRegion[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    regions[i] = new TextureRegion(spriteTexture, i * frameSize, 0, frameSize, frameSize);
                }
                
                if (frameCount == 6) {
                    idleAnim = new Animation<>(0.15f, regions[0], regions[1], regions[2], regions[3]);
                    idleAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                    walkAnim = new Animation<>(0.15f, regions[0], regions[1], regions[2], regions[3]);
                    walkAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                    detectedAnim = new Animation<>(0.1f, regions[4]);
                    attackAnim = new Animation<>(0.15f, regions[5]);
                } else {
                    idleAnim = new Animation<>(0.1f, regions[0]);
                    walkAnim = new Animation<>(0.15f, regions[0]);
                    if (frameCount >= 2) {
                        detectedAnim = new Animation<>(0.1f, regions[1]);
                    }
                    if (frameCount >= 3) {
                        TextureRegion[] attackFrames = new TextureRegion[frameCount - 2];
                        for (int i = 2; i < frameCount; i++) {
                            attackFrames[i - 2] = regions[i];
                        }
                        attackAnim = new Animation<>(0.1f, attackFrames);
                        attackAnim.setPlayMode(Animation.PlayMode.LOOP);
                    }
                }
            } else {
                idleAnim = new Animation<>(0.1f, spriteTexture);
                walkAnim = new Animation<>(0.15f, spriteTexture);
            }
        } catch (Exception e) {
            Gdx.app.error("ConfigurableEnemy", "Error cargando sprite: " + atlas + "/" + region, e);
        }

        // Crear comportamiento
        String behaviorType = config.getString("type", "chaser");
        float attackRange = config.getFloat("attack_range", 1.0f);
        float attackCooldown = config.getFloat("attack_cooldown", 1.0f);

        if ("chaser".equals(behaviorType)) {
            behavior = new ChaserBehavior(getSpeed(), getDamage(), attackRange, attackCooldown);
        } else if ("ranged".equals(behaviorType)) {
            isRanged = true;
            float detectionRange = config.getFloat("detection_range", 6.0f);
            float projectileSpeed = config.getFloat("projectile_speed", 5.0f);
            float projectileRadius = config.getFloat("projectile_radius", 0.3f);
            String projectileSprite = config.getString("projectile_sprite", "YellowBullet");
            
            RangedBehavior rangedBehavior = new RangedBehavior(getSpeed(), detectionRange, attackCooldown,
                    projectileSpeed, getDamage(), projectileSprite);
            rangedBehavior.setProjectileRadius(projectileRadius);
            rangedBehavior.loadProjectileTexture();
            behavior = rangedBehavior;
        } else if ("pouncing".equals(behaviorType)) {
            float transformDistance = config.getFloat("transform_distance", 6.0f);
            float waitDuration = config.getFloat("wait_duration", 1.0f);
            float pounceSpeed = config.getFloat("pounce_speed", 10.0f);
            float bounceForce = config.getFloat("bounce_force", 4.0f);
            float restartDistance = config.getFloat("restart_distance", 4.0f);
            
            behavior = new PouncingBounceBehavior(getSpeed(), getDamage(), transformDistance,
                    waitDuration, pounceSpeed, bounceForce, restartDistance, attackCooldown);
        }

        this.alive = true;
    }

    public void setBehavior(EnemyBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public void update(float delta, Entity target) {
        super.update(delta);
        if (!isAlive() || target == null) return;

        float st = getStateTime();
        st += delta;
        setStateTime(st);
        actualizarHitboxes();

        if (behavior != null) {
            behavior.update(this, target, delta, null);
        }
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float delta) {
        TextureRegion frame;
        float st = getStateTime();
        
        boolean isRangedEnemy = isRanged && behavior instanceof RangedBehavior;
        boolean isFiring = isRangedEnemy && ((RangedBehavior) behavior).isFiring();
        boolean isDetected = isRangedEnemy && !isFiring && ((RangedBehavior) behavior).isDetected();
        
        boolean isPouncingEnemy = !isRanged && behavior instanceof PouncingBounceBehavior;
        float floatOffset = 0;
        if (isPouncingEnemy) {
            floatOffset = ((PouncingBounceBehavior) behavior).getVisualOffsetY();
            PouncingBounceBehavior.PounceState pounceState = ((PouncingBounceBehavior) behavior).getCurrentState();
            
            if (pounceState == PouncingBounceBehavior.PounceState.TRANSFORMING || 
                pounceState == PouncingBounceBehavior.PounceState.WAITING) {
                frame = detectedAnim.getKeyFrame(0);
            } else if (pounceState == PouncingBounceBehavior.PounceState.POUNCING ||
                       pounceState == PouncingBounceBehavior.PounceState.BOUNCING) {
                frame = attackAnim.getKeyFrame(st);
            } else {
                frame = idleAnim.getKeyFrame(0);
            }
        } else if (isFiring) {
            frame = attackAnim.getKeyFrame(st);
        } else if (isDetected) {
            frame = detectedAnim.getKeyFrame(0);
        } else if (getEstado() == Estado.walking) {
            frame = walkAnim.getKeyFrame(st);
        } else {
            frame = idleAnim.getKeyFrame(st);
        }

        if (frame == null) {
            return;
        }

        float x = getPosition().x - getANCHO() / 2;
        float y = getPosition().y - getALTO() / 2 + floatOffset;
        
        if (isMirarDerecha()) {
            batch.draw(frame, x + getANCHO(), y, -getANCHO(), getALTO());
        } else {
            batch.draw(frame, x, y, getANCHO(), getALTO());
        }
    }

    public EnemyBehavior getBehavior() {
        return behavior;
    }

    public boolean canAttack() {
        if (behavior instanceof ChaserBehavior) {
            return ((ChaserBehavior) behavior).canAttack();
        }
        return false;
    }

    public void setEffectManager(com.tikisadventure.effects.EffectManager em) {
        this.effectManager = em;
        if (behavior instanceof RangedBehavior) {
            ((RangedBehavior) behavior).setEffectManager(em);
        }
    }

    public void setEnemyProjectiles(Array<Projectile> projectiles) {
        this.enemyProjectiles = projectiles;
        if (behavior instanceof RangedBehavior) {
            ((RangedBehavior) behavior).setEnemyProjectiles(projectiles);
        }
    }

    public boolean hasPouncingBehavior() {
        return "pouncing".equals(behavior != null ? behavior.getBehaviorType() : null);
    }
}
