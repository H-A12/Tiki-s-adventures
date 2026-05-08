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

    private String enemyId = "Desconocido";

    static {
        JsonReader reader = new JsonReader();
        enemyConfig = reader.parse(Gdx.files.internal("data/enemy_config.json")).get("enemies");
    }

    public ConfigurableEnemy(String enemyType, WaveSystem waveSystem) {
        JsonValue config = enemyConfig.get(enemyType);

        if (config == null) {
            System.out.println("¡ALERTA! El JSON no tiene al enemigo: " + enemyType + ". Lo convierto en Slime por seguridad.");
            config = enemyConfig.get("slime");
        }

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

        float w = config.getFloat("width", 1);
        float h = config.getFloat("height", 1);
        this.renderComponent = new RenderComponent(null, w, h);
        setANCHO(w);
        setALTO(h);

        String atlas = "shared";

        try {
            boolean hasMultiSprite = config.has("sprite_idle");

            if (hasMultiSprite) {
                // --- NUEVO SISTEMA MULTI-SPRITE ---
                int frameSize = config.getInt("frame_size", 16);

                String idleStr = config.getString("sprite_idle");
                String walkStr = config.getString("sprite_walk", idleStr);
                String attackStr = config.getString("sprite_attack", idleStr);

                idleAnim = createAnimationFromRegion(atlas, idleStr, frameSize, 0.15f, Animation.PlayMode.LOOP);
                walkAnim = createAnimationFromRegion(atlas, walkStr, frameSize, 0.15f, Animation.PlayMode.LOOP);
                attackAnim = createAnimationFromRegion(atlas, attackStr, frameSize, 0.1f, Animation.PlayMode.NORMAL);
                detectedAnim = idleAnim;

            } else {
                // --- SISTEMA ANTIGUO DE 1 SOLO SPRITE (Slimes, Skeleton) ---
                String region = config.getString("sprite", "enemies_assets/slime");
                spriteTexture = Assets.getRegion(atlas, region);
                int frameSize = 16;
                int frameCount = spriteTexture.getRegionWidth() / frameSize;

                TextureRegion[] allFrames = new TextureRegion[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    allFrames[i] = new TextureRegion(spriteTexture, i * frameSize, 0, frameSize, frameSize);
                }

                boolean hasAnimationConfig = config.has("idle_frames") || config.has("walk_frames");

                if (hasAnimationConfig) {
                    if (config.has("idle_frames")) {
                        JsonValue idleFramesVal = config.get("idle_frames");
                        TextureRegion[] idleFrames = new TextureRegion[idleFramesVal.size];
                        for (int i = 0; i < idleFramesVal.size; i++) {
                            idleFrames[i] = allFrames[idleFramesVal.getInt(i)];
                        }
                        idleAnim = new Animation<>(0.15f, idleFrames);
                        idleAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                    }

                    if (config.has("walk_frames")) {
                        JsonValue walkFramesVal = config.get("walk_frames");
                        TextureRegion[] walkFrames = new TextureRegion[walkFramesVal.size];
                        for (int i = 0; i < walkFramesVal.size; i++) {
                            walkFrames[i] = allFrames[walkFramesVal.getInt(i)];
                        }
                        walkAnim = new Animation<>(0.15f, walkFrames);
                        walkAnim.setPlayMode(Animation.PlayMode.LOOP);
                    }

                    if (config.has("detected_frames")) {
                        JsonValue detectedFramesVal = config.get("detected_frames");
                        TextureRegion[] detectedFrames = new TextureRegion[detectedFramesVal.size];
                        for (int i = 0; i < detectedFramesVal.size; i++) {
                            detectedFrames[i] = allFrames[detectedFramesVal.getInt(i)];
                        }
                        detectedAnim = new Animation<>(0.15f, detectedFrames);
                        detectedAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                    }

                    if (config.has("attack_frames")) {
                        JsonValue attackFramesVal = config.get("attack_frames");
                        TextureRegion[] attackFrames = new TextureRegion[attackFramesVal.size];
                        for (int i = 0; i < attackFramesVal.size; i++) {
                            attackFrames[i] = allFrames[attackFramesVal.getInt(i)];
                        }
                        attackAnim = new Animation<>(0.1f, attackFrames);
                        attackAnim.setPlayMode(Animation.PlayMode.LOOP);
                    }
                } else {
                    if (frameCount > 1) {
                        if (frameCount == 6) {
                            idleAnim = new Animation<>(0.15f, allFrames[0], allFrames[1], allFrames[2], allFrames[3]);
                            idleAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                            walkAnim = new Animation<>(0.15f, allFrames[0], allFrames[1], allFrames[2], allFrames[3]);
                            walkAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                            detectedAnim = new Animation<>(0.1f, allFrames[4]);
                            attackAnim = new Animation<>(0.15f, allFrames[5]);
                        } else {
                            idleAnim = new Animation<>(0.1f, allFrames[0]);
                            walkAnim = new Animation<>(0.15f, allFrames[0]);
                            if (frameCount >= 2) {
                                detectedAnim = new Animation<>(0.1f, allFrames[1]);
                            }
                            if (frameCount >= 3) {
                                TextureRegion[] attackFrames = new TextureRegion[frameCount - 2];
                                for (int i = 2; i < frameCount; i++) {
                                    attackFrames[i - 2] = allFrames[i];
                                }
                                attackAnim = new Animation<>(0.1f, attackFrames);
                                attackAnim.setPlayMode(Animation.PlayMode.LOOP);
                            }
                        }
                    } else {
                        idleAnim = new Animation<>(0.1f, spriteTexture);
                        walkAnim = new Animation<>(0.15f, spriteTexture);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("ConfigurableEnemy", "Error cargando sprite", e);
        }

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

    private Animation<TextureRegion> createAnimationFromRegion(String atlas, String regionName, int frameSize, float frameDuration, Animation.PlayMode playMode) {
        TextureRegion region = Assets.getRegion(atlas, regionName);
        if (region == null) {
            Gdx.app.error("ConfigurableEnemy", "No se encontró el sprite: " + regionName);
            return new Animation<>(frameDuration);
        }
        int frameCount = region.getRegionWidth() / frameSize;
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(region, i * frameSize, 0, frameSize, frameSize);
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(playMode);
        return anim;
    }

    public void setBehavior(EnemyBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public void update(float delta, Entity target) {
        super.update(delta);
        if (!isAlive() || target == null) return;

        if (isFrozen()) return;

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
        boolean isChaserAttacking = behavior instanceof ChaserBehavior && ((ChaserBehavior) behavior).isAttacking();

        float floatOffset = 0;

        if (isPouncingEnemy) {
            floatOffset = ((PouncingBounceBehavior) behavior).getVisualOffsetY();
            PouncingBounceBehavior.PounceState pounceState = ((PouncingBounceBehavior) behavior).getCurrentState();

            if (pounceState == PouncingBounceBehavior.PounceState.TRANSFORMING ||
                pounceState == PouncingBounceBehavior.PounceState.WAITING) {
                // Seguro anti-crasheos
                frame = (detectedAnim != null && detectedAnim.getKeyFrames().length > 0) ? detectedAnim.getKeyFrame(st) : idleAnim.getKeyFrame(st);
            } else if (pounceState == PouncingBounceBehavior.PounceState.POUNCING ||
                pounceState == PouncingBounceBehavior.PounceState.BOUNCING) {
                frame = (attackAnim != null && attackAnim.getKeyFrames().length > 0) ? attackAnim.getKeyFrame(st) : walkAnim.getKeyFrame(st);
            } else {
                frame = idleAnim.getKeyFrame(st);
            }
        } else if (isFiring || isChaserAttacking) {
            // --- FIX DEL CRASHEO: Si no tiene fotogramas de ataque (ej. Slime), usa el de caminar ---
            if (attackAnim != null && attackAnim.getKeyFrames().length > 0) {
                frame = attackAnim.getKeyFrame(st);
            } else {
                frame = walkAnim.getKeyFrame(st);
            }
        } else if (isDetected) {
            if (detectedAnim != null && detectedAnim.getKeyFrames().length > 0) {
                frame = detectedAnim.getKeyFrame(st);
            } else {
                frame = idleAnim.getKeyFrame(st);
            }
        } else {
            if (getEstado() == Estado.walking) {
                frame = walkAnim.getKeyFrame(st);
            } else {
                frame = idleAnim.getKeyFrame(st);
            }
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

    public boolean isInAttackWindup() {
        return behavior != null && behavior.isInWindup();
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

    public String getEnemyId() { return this.enemyId; }
    public void setEnemyId(String id) { this.enemyId = id; }
}
