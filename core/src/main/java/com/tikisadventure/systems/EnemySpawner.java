package com.tikisadventure.systems;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.enemies.EnemyFactoryImpl;

/**
 * Gestiona el nacimiento de enemigos tanto por oleadas automáticas como por comandos manuales.
 */
public class EnemySpawner {

    private final Array<Entity> entities;
    private final TiledMapTileLayer collisionLayer;
    private final WaveSystem waveSystem;
    private final EnemyFactoryImpl factory;

    private float spawnTimer = 0;
    private final float BASE_SPAWN_INTERVAL = 1.0f;
    private final float SPAWN_RADIUS = 15f;

    private int enemiesSpawnedThisWave = 0;
    private int totalEnemiesForCurrentWave = 0;
    private int enemiesPerWave = 1;
    private boolean waveSpawningComplete = false;
    private int currentEnemyIndex = 0;

    public EnemySpawner(Array<Entity> entities, TiledMapTileLayer collisionLayer, WaveSystem waveSystem) {
        this.entities = entities;
        this.collisionLayer = collisionLayer;
        this.waveSystem = waveSystem;
        this.factory = new EnemyFactoryImpl();
    }

    /**
     * SPAWN MANUAL: Llama a este método para decidir exactamente qué y dónde spawnear.
     */
    public Entity spawnManual(String enemyType, float x, float y) {
        int currentWave = (waveSystem != null) ? waveSystem.getCurrentWave() : 1;

        // La Factory ahora debe recibir (String type, int wave)
        Entity enemy = factory.create(enemyType, currentWave);

        if (enemy != null) {
            // Clamping para evitar que nazcan en los bordes técnicos del mapa (tiles 0 y 1)
            float clampedX = MathUtils.clamp(x, 2, collisionLayer.getWidth() - 2);
            float clampedY = MathUtils.clamp(y, 2, collisionLayer.getHeight() - 2);

            enemy.getPosicion().set(clampedX, clampedY);
            enemy.actualizarHitboxes(); // Sincronización de hitbox inmediata
            entities.add(enemy);
        }
        return enemy;
    }

    /**
     * SPAWN CERCA: Útil para rodear al jugador con enemigos.
     */
    public Entity spawnNear(String enemyType, Entity target, float radius) {
        float angle = MathUtils.random(0f, 360f);
        float x = target.getPosicion().x + MathUtils.cosDeg(angle) * radius;
        float y = target.getPosicion().y + MathUtils.sinDeg(angle) * radius;
        return spawnManual(enemyType, x, y);
    }

    public void update(float delta, Entity player) {
        if (waveSystem == null || waveSystem.getCurrentWave() == 0 || player == null) return;

        JsonValue waveEnemies = waveSystem.getCurrentWaveEnemies();
        if (waveEnemies == null) return;

        if (totalEnemiesForCurrentWave == 0) {
            initWave(waveEnemies);
        }

        if (waveSpawningComplete) return;

        spawnTimer += delta;
        if (spawnTimer >= calculateSpawnInterval()) {
            spawnTimer = 0;

            for (int i = 0; i < enemiesPerWave; i++) {
                if (enemiesSpawnedThisWave >= totalEnemiesForCurrentWave) {
                    waveSpawningComplete = true;
                    break;
                }

                String enemyType = getNextEnemyType(waveEnemies);
                if (enemyType != null) {
                    // El flujo automático usa la lógica de spawn circular
                    spawnNear(enemyType, player, SPAWN_RADIUS);
                    enemiesSpawnedThisWave++;
                }
            }
            waveSpawningComplete = (enemiesSpawnedThisWave >= totalEnemiesForCurrentWave);
        }
    }

    private void initWave(JsonValue waveEnemies) {
        totalEnemiesForCurrentWave = waveSystem.getTotalEnemiesForCurrentWave();
        enemiesSpawnedThisWave = 0;
        waveSpawningComplete = false;
        currentEnemyIndex = 0;

        // Ajuste de densidad: más enemigos por "tic" en oleadas grandes
        if (totalEnemiesForCurrentWave <= 10) enemiesPerWave = 1;
        else if (totalEnemiesForCurrentWave <= 30) enemiesPerWave = 2;
        else enemiesPerWave = 4;
    }

    private float calculateSpawnInterval() {
        // Reducción del delay según la oleada: más frenético conforme avanzas
        return Math.max(0.25f, BASE_SPAWN_INTERVAL - (waveSystem.getCurrentWave() * 0.07f));
    }

    private String getNextEnemyType(JsonValue waveEnemies) {
        int accumulated = 0;
        for (JsonValue enemyConfig : waveEnemies) {
            String type = enemyConfig.getString("type");
            int count = enemyConfig.getInt("count");

            if (currentEnemyIndex < accumulated + count) {
                currentEnemyIndex++;
                return type;
            }
            accumulated += count;
        }
        return "slime";
    }

    public void resetForNewWave() {
        totalEnemiesForCurrentWave = 0;
        enemiesSpawnedThisWave = 0;
        waveSpawningComplete = false;
        currentEnemyIndex = 0;
        spawnTimer = 0;
    }

    public boolean isWaveSpawningComplete() { return waveSpawningComplete; }
}
