package com.tikisadventure.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.enemies.EnemyFactoryImpl;
import com.tikisadventure.floors.FloorManager;

public class EnemySpawner {

    private Array<Entity> enemies;
    private FloorManager floorManager;
    private WaveSystem waveSystem;

    private float spawnTimer = 0;
    private final float BASE_SPAWN_INTERVAL = 1.0f;
    private final float SPAWN_RADIUS = 10f;

    private int enemiesSpawnedThisWave = 0;
    private int totalEnemiesForCurrentWave = 0;
    private int enemiesPerWave = 1;
    private boolean waveSpawningComplete = false;
    private int currentEnemyIndex = 0;

    public EnemySpawner(Array<Entity> enemies, FloorManager floorManager, WaveSystem waveSystem) {
        this.enemies = enemies;
        this.floorManager = floorManager;
        this.waveSystem = waveSystem;
    }

    public void update(float delta, Entity player) {
        if (waveSystem == null) return;
        if (waveSystem.getCurrentWave() == 0) return;

        JsonValue waveEnemies = waveSystem.getCurrentWaveEnemies();
        if (waveEnemies == null) return;

        if (totalEnemiesForCurrentWave == 0) {
            initWave(waveEnemies);
        }

        if (waveSpawningComplete) return;

        spawnTimer += delta;
        float currentInterval = calculateSpawnInterval();

        if (spawnTimer >= currentInterval) {
            spawnTimer = 0;

            if (enemiesSpawnedThisWave >= totalEnemiesForCurrentWave) {
                waveSpawningComplete = true;
                return;
            }

            for (int batch = 0; batch < enemiesPerWave; batch++) {
                if (enemiesSpawnedThisWave >= totalEnemiesForCurrentWave) break;

                String enemyType = getNextEnemyType(waveEnemies);
                if (enemyType != null) {
                    spawnEnemy(enemyType, player);
                    enemiesSpawnedThisWave++;
                }
            }

            waveSpawningComplete = enemiesSpawnedThisWave >= totalEnemiesForCurrentWave;
        }
    }

    private void initWave(JsonValue waveEnemies) {
        totalEnemiesForCurrentWave = waveSystem.getTotalEnemiesForCurrentWave();
        enemiesSpawnedThisWave = 0;
        waveSpawningComplete = false;
        currentEnemyIndex = 0;

        if (totalEnemiesForCurrentWave <= 5) {
            enemiesPerWave = 1;
        } else if (totalEnemiesForCurrentWave <= 10) {
            enemiesPerWave = 2;
        } else if (totalEnemiesForCurrentWave <= 20) {
            enemiesPerWave = 3;
        } else {
            enemiesPerWave = 4;
        }
    }

    private float calculateSpawnInterval() {
        if (totalEnemiesForCurrentWave <= 5) {
            return BASE_SPAWN_INTERVAL;
        } else {
            return BASE_SPAWN_INTERVAL * 0.7f;
        }
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

    private void spawnEnemy(String enemyType, Entity player) {
        EnemyFactoryImpl factory = new EnemyFactoryImpl(enemyType, waveSystem);
        Entity enemy = factory.create();

        float angle = MathUtils.random(0f, 360f);
        float x = player.getPosicion().x + MathUtils.cosDeg(angle) * SPAWN_RADIUS;
        float y = player.getPosicion().y + MathUtils.sinDeg(angle) * SPAWN_RADIUS;

        x = MathUtils.clamp(x, 3, 17);
        y = MathUtils.clamp(y, 3, 17);

        enemy.getPosicion().set(x, y);
        enemies.add(enemy);
    }

    public void resetForNewWave() {
        enemiesSpawnedThisWave = 0;
        totalEnemiesForCurrentWave = 0;
        waveSpawningComplete = false;
        currentEnemyIndex = 0;
        spawnTimer = 0;
    }

    public boolean isWaveSpawningComplete() {
        return waveSpawningComplete;
    }

    public int getEnemiesRemainingToSpawn() {
        return totalEnemiesForCurrentWave - enemiesSpawnedThisWave;
    }
}
