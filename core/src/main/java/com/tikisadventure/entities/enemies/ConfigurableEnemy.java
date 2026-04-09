package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.enemies.behavior.ChaserBehavior;
import com.tikisadventure.enemies.behavior.EnemyBehavior;
import com.tikisadventure.enemies.behavior.PouncingBounceBehavior;
import com.tikisadventure.systems.WaveSystem;

public class ConfigurableEnemy extends Entity {

    private EnemyBehavior behavior;
    private static JsonValue enemyConfig;
    private TextureRegion spriteTexture;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> transformAnim;
    private Animation<TextureRegion> attackAnim;
    private boolean hasPouncingBehavior = false;
    private boolean invertRenderDirection = false;

    static {
        JsonReader reader = new JsonReader();
        enemyConfig = reader.parse(Gdx.files.internal("enemy_config.json")).get("enemies");
    }

    public ConfigurableEnemy(String enemyType, WaveSystem waveSystem) {
        JsonValue config = enemyConfig.get(enemyType);
        if (config == null) config = enemyConfig.get("slime");

        this.vida = Math.round(config.getFloat("health", 3) * waveSystem.getDifficultyMultiplier());
        this.vida_max = this.vida;
        this.speed = config.getFloat("speed", 2.5f) * waveSystem.getDifficultyMultiplier();
        this.danyo = Math.round(config.getFloat("damage", 2) * waveSystem.getDifficultyMultiplier());
        this.experience = Math.round(config.getFloat("experience", 5) * waveSystem.getDifficultyMultiplier());
        this.ANCHO = config.getFloat("width", 1);
        this.ALTO = config.getFloat("height", 1);

        String spriteRaw = config.getString("sprite", "shared_slime");
        String[] parts = spriteRaw.split("_", 2);
        String atlas = (parts.length > 1) ? parts[0] : "shared";
        String region = (parts.length > 1) ? parts[1] : parts[0];

        try {
            spriteTexture = Assets.getRegion(atlas, region);
            Gdx.app.log("ConfigurableEnemy", "Cargando sprite: " + atlas + "/" + region + " -> " + (spriteTexture != null ? "OK" : "NULL"));
            if (spriteTexture != null) {
                int frameSize = 16;
                int frameCount = spriteTexture.getRegionWidth() / frameSize;
                Gdx.app.log("ConfigurableEnemy", "Frames detectados: " + frameCount + ", ancho sprite: " + spriteTexture.getRegionWidth());
                TextureRegion[] regions = new TextureRegion[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    regions[i] = new TextureRegion(spriteTexture, i * frameSize, 0, frameSize, frameSize);
                }

                if (regions.length > 0) {
                    idleAnim = new Animation<>(0.1f, regions[0]);
                    walkAnim = new Animation<>(0.15f, regions[0], regions.length > 1 ? regions[1] : regions[0]);
                    walkAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                }

                if (regions.length >= 10) {
                    transformAnim = new Animation<>(0.1f, regions[1]);
                    TextureRegion[] attackFrames = new TextureRegion[8];
                    System.arraycopy(regions, 2, attackFrames, 0, 8);
                    attackAnim = new Animation<>(0.1f, attackFrames);
                    attackAnim.setPlayMode(Animation.PlayMode.LOOP);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("ConfigurableEnemy", "Error loading sprite: " + atlas + "/" + region, e);
        }

        String behaviorType = config.getString("type", "chaser");
        float attackRange = config.getFloat("attack_range", 1.0f);
        float attackCooldown = config.getFloat("attack_cooldown", 1.0f);
        invertRenderDirection = config.getBoolean("flip_sprite", false);

        if ("pouncing".equals(behaviorType)) {
            hasPouncingBehavior = true;
            behavior = new PouncingBounceBehavior(speed, danyo, config.getFloat("transform_distance", 6),
                config.getFloat("wait_duration", 1), config.getFloat("pounce_speed", 10),
                config.getFloat("bounce_force", 4), config.getFloat("restart_distance", 4), attackCooldown);
        } else {
            behavior = new ChaserBehavior(speed, danyo, attackRange, attackCooldown);
        }

        this.alive = true;
    }

    @Override
    public void update(float delta, Entity target) {
        if (!alive || target == null) return;
        stateTime += delta;
        actualizarHitboxes();
        if (behavior != null) behavior.update(this, target, delta, null);
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.Batch batch, float delta) {
        if (!alive) return;

        TextureRegion frame = idleAnim != null ? idleAnim.getKeyFrame(stateTime) : null;

        if (hasPouncingBehavior && behavior instanceof PouncingBounceBehavior) {
            PouncingBounceBehavior pouncing = (PouncingBounceBehavior) behavior;
            PouncingBounceBehavior.PounceState state = pouncing.getCurrentState();

            switch (state) {
                case TRANSFORMING:
                case WAITING:
                    if (transformAnim != null) frame = transformAnim.getKeyFrame(stateTime);
                    break;
                case POUNCING:
                case BOUNCING:
                    if (attackAnim != null) frame = attackAnim.getKeyFrame(stateTime);
                    break;
                default:
                    if (idleAnim != null) frame = idleAnim.getKeyFrame(stateTime);
                    break;
            }
        } else if (estado == Estado.walking && walkAnim != null) {
            frame = walkAnim.getKeyFrame(stateTime);
        }

        if (frame == null) return;

        float x = posicion.x - ANCHO / 2;
        float y = posicion.y - ALTO / 2;

        if (hasPouncingBehavior && behavior instanceof PouncingBounceBehavior) {
            y += ((PouncingBounceBehavior) behavior).getVisualOffsetY();
        }

        boolean shouldFlip = invertRenderDirection ? !mirarDerecha : mirarDerecha;
        if (shouldFlip) batch.draw(frame, x, y, ANCHO, ALTO);
        else batch.draw(frame, x + ANCHO, y, -ANCHO, ALTO);
    }

    public boolean hasPouncingBehavior() { return hasPouncingBehavior; }
    public EnemyBehavior getBehavior() { return behavior; }

    public void triggerBounce(com.badlogic.gdx.math.Vector2 dir) {
        if (behavior instanceof PouncingBounceBehavior) {
            ((PouncingBounceBehavior) behavior).triggerBounce(dir);
        }
    }
}
