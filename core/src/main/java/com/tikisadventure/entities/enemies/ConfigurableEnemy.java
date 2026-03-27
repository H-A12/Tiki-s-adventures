package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.enemies.behavior.ChaserBehavior;
import com.tikisadventure.enemies.behavior.EnemyBehavior;
import com.tikisadventure.systems.WaveSystem;

public class ConfigurableEnemy extends Entity {

    private EnemyBehavior behavior;
    private static JsonValue enemyConfig;
    private Texture spriteTexture;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;

    static {
        JsonReader reader = new JsonReader();
        enemyConfig = reader.parse(Gdx.files.internal("enemy_config.json")).get("enemies");
    }

    public ConfigurableEnemy(String enemyType, WaveSystem waveSystem) {
        JsonValue config = enemyConfig.get(enemyType);
        
        if (config == null) {
            // Fallback a slime por defecto
            config = enemyConfig.get("slime");
        }

        // Cargar stats con escalado por oleada
        float baseHealth = config.getFloat("health", 3);
        float baseSpeed = config.getFloat("speed", 2.5f);
        float baseDamage = config.getFloat("damage", 2);
        float baseExperience = config.getFloat("experience", 5);

        this.vida = Math.round(baseHealth * waveSystem.getDifficultyMultiplier());
        this.vida_max = this.vida;
        this.speed = baseSpeed * waveSystem.getDifficultyMultiplier();
        this.danyo = Math.round(baseDamage * waveSystem.getDifficultyMultiplier());
        this.experience = Math.round(baseExperience * waveSystem.getDifficultyMultiplier());

        // Tamaño
        this.ANCHO = config.getFloat("width", 1);
        this.ALTO = config.getFloat("height", 1);

        // Cargar sprite
        String spriteName = config.getString("sprite", "slime.png");
        try {
            spriteTexture = new Texture(spriteName);
            TextureRegion[] regions = TextureRegion.split(spriteTexture, 16, 16)[0];
            
            idleAnim = new Animation<>(0.1f, regions[0]);
            walkAnim = new Animation<>(0.15f, regions[0], regions[1]);
            walkAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        } catch (Exception e) {
            // Si no encuentra el sprite, usar uno por defecto
        }

        // Crear comportamiento
        String behaviorType = config.getString("type", "chaser");
        float attackRange = config.getFloat("attack_range", 1.0f);
        float attackCooldown = config.getFloat("attack_cooldown", 1.0f);

        if ("chaser".equals(behaviorType)) {
            behavior = new ChaserBehavior(speed, danyo, attackRange, attackCooldown);
        }

        this.alive = true;
    }

    public void setBehavior(EnemyBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public void update(float delta, Entity target) {
        if (!alive || target == null) return;

        stateTime += delta;

        if (behavior != null) {
            behavior.update(this, target, delta, null);
        }
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.Batch batch, float delta) {
        if (!alive) return;

        TextureRegion frame;
        if (estado == Entity.Estado.walking) {
            frame = walkAnim.getKeyFrame(stateTime);
        } else {
            frame = idleAnim.getKeyFrame(stateTime);
        }

        float x = posicion.x - ANCHO / 2;
        float y = posicion.y - ALTO / 2;

        if (mirarDerecha) {
            batch.draw(frame, x, y, ANCHO, ALTO);
        } else {
            batch.draw(frame, x + ANCHO, y, -ANCHO, ALTO);
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
