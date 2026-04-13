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
import com.tikisadventure.systems.WaveSystem;

public class ConfigurableEnemy extends Entity {

    private EnemyBehavior behavior;
    private static JsonValue enemyConfig;
    private TextureRegion spriteTexture;
    private Animation<TextureRegion> idleAnim = new Animation<>(0.1f);
    private Animation<TextureRegion> walkAnim = new Animation<>(0.15f);

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
        String atlas = (parts.length > 1) ? parts[0] : "shared";
        String region = (parts.length > 1) ? parts[1] : parts[0];

        try {
            spriteTexture = Assets.getRegion(atlas, region);
            int frameSize = 16;
            int frameCount = spriteTexture.getRegionWidth() / frameSize;
            TextureRegion[] regions = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                regions[i] = new TextureRegion(spriteTexture, i * frameSize, 0, frameSize, frameSize);
            }

            idleAnim = new Animation<>(0.1f, regions[0]);
            walkAnim = new Animation<>(0.15f, regions[0], regions[1]);
            walkAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        } catch (Exception e) {
            Gdx.app.error("ConfigurableEnemy", "Error cargando sprite: " + atlas + "/" + region, e);
        }

        // Crear comportamiento
        String behaviorType = config.getString("type", "chaser");
        float attackRange = config.getFloat("attack_range", 1.0f);
        float attackCooldown = config.getFloat("attack_cooldown", 1.0f);

        if ("chaser".equals(behaviorType)) {
            behavior = new ChaserBehavior(getSpeed(), getDamage(), attackRange, attackCooldown);
        }
    }

    public void setBehavior(EnemyBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public void update(float delta, Entity target) {
        super.update(delta);
        if (!isAlive() || target == null) return;

        actualizarHitboxes(); // FIX: Update hitboxes for collision detection

        if (behavior != null) {
            behavior.update(this, target, delta, null);
        }
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float delta) {
        TextureRegion frame;
        if (getEstado() == Estado.walking) {
            frame = walkAnim.getKeyFrame(getStateTime());
        } else {
            frame = idleAnim.getKeyFrame(getStateTime());
        }

        if (frame == null) {
            Gdx.app.log("ConfigurableEnemy", "Frame is null, state: " + getEstado());
            return;
        }

        float x = positionComponent.posicion.x - getANCHO() / 2;
        float y = positionComponent.posicion.y - getALTO() / 2;
        
        Gdx.app.log("ConfigurableEnemy", "Drawing enemy at: " + x + ", " + y + " Size: " + getANCHO() + "x" + getALTO());

        if (isMirarDerecha()) {
            batch.draw(frame, x, y, getANCHO(), getALTO());
        } else {
            batch.draw(frame, x + getANCHO(), y, -getANCHO(), getALTO());
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
}
