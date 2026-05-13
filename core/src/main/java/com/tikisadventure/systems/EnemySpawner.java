package com.tikisadventure.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.enemies.ConfigurableEnemy;
import com.tikisadventure.entities.enemies.EnemyFactoryImpl;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.floors.FloorManager;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.combat.projectiles.Projectile;

import java.util.List;

public class EnemySpawner {

    private Array<Entity> enemies;
    private FloorManager floorManager;
    private WaveSystem waveSystem;

    private float spawnTimer = 0;
    private final float BASE_SPAWN_INTERVAL = 1.0f;
    private final float MAX_SPAWN_RADIUS = 15f;
    private final float MIN_SPAWN_RADIUS = 6f;

    private int enemiesSpawnedThisWave = 0;
    private int totalEnemiesForCurrentWave = 0;
    private int enemiesPerWave = 1;
    private boolean waveSpawningComplete = false;
    private int currentEnemyIndex = 0;
    private List<WaveGenerator.WaveEntry> cachedWaveEntries;

    private EffectManager effectManager;
    private Array<Projectile> enemyProjectiles = new Array<>();

    public EnemySpawner(Array<Entity> enemies, FloorManager floorManager, WaveSystem waveSystem, EffectManager effectManager) {
        this.enemies = enemies;
        this.floorManager = floorManager;
        this.waveSystem = waveSystem;
        this.effectManager = effectManager;
    }

    public void update(float delta, Entity player) {
        if (waveSystem == null) return;
        if (waveSystem.getGlobalWaveCount() == 0) return;

        List<WaveGenerator.WaveEntry> waveEntries = waveSystem.getCurrentWaveEnemies();
        if (waveEntries == null) return;

        if (totalEnemiesForCurrentWave == 0) {
            initWave(waveEntries);
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

                String enemyType = getNextEnemyType(waveEntries);
                if (enemyType != null) {
                    spawnEnemy(enemyType, player);
                    enemiesSpawnedThisWave++;
                }
            }

            waveSpawningComplete = enemiesSpawnedThisWave >= totalEnemiesForCurrentWave;
        }
    }

    private void initWave(List<WaveGenerator.WaveEntry> waveEntries) {
        totalEnemiesForCurrentWave = waveSystem.getTotalEnemiesForCurrentWave();
        enemiesSpawnedThisWave = 0;
        waveSpawningComplete = false;
        currentEnemyIndex = 0;
        cachedWaveEntries = waveEntries;

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

    private String getNextEnemyType(List<WaveGenerator.WaveEntry> waveEntries) {
        int accumulated = 0;
        for (WaveGenerator.WaveEntry entry : waveEntries) {
            if (currentEnemyIndex < accumulated + entry.count) {
                currentEnemyIndex++;
                return entry.type;
            }
            accumulated += entry.count;
        }
        return "slime";
    }

    private void spawnEnemy(String enemyType, Entity player) {
        EnemyFactoryImpl factory = new EnemyFactoryImpl(enemyType, waveSystem);
        Entity enemy = factory.create();

        if (enemy instanceof ConfigurableEnemy) {
            ConfigurableEnemy configEnemy = (ConfigurableEnemy) enemy;
            configEnemy.setEffectManager(effectManager);
            configEnemy.setEnemyProjectiles(enemyProjectiles);
            configEnemy.setEnemyId(enemyType);
        }

        applyPlayerStatBoost(enemy, player);

        Vector2 spawnPos = findSpawnPosition(player);
        enemy.getPosition().set(spawnPos.x, spawnPos.y);
        enemies.add(enemy);
    }

    private Vector2 findSpawnPosition(Entity player) {
        for (int attempt = 0; attempt < 30; attempt++) {
            float angle = MathUtils.random(0f, 360f);
            float radius = MathUtils.random(MIN_SPAWN_RADIUS, MAX_SPAWN_RADIUS);
            float x = player.getPosition().x + MathUtils.cosDeg(angle) * radius;
            float y = player.getPosition().y + MathUtils.sinDeg(angle) * radius;

            if (x >= 1 && x <= 48 && y >= 1 && y <= 48 && !floorManager.isWall(x, y)) {
                return new Vector2(x, y);
            }
        }

        Vector2 fallback = floorManager.findValidSpawnPosition(1, 48, 1, 48);
        if (fallback != null) return fallback;

        return new Vector2(10, 10);
    }

    private void applyPlayerStatBoost(Entity enemy, Entity player) {
        if (!(player instanceof Player)) return;

        Player p = (Player) player;

        float totalDamageBonus = p.getKineticDamageBonus() + p.getExplosiveDamageBonus()
                               + p.getFireDamageBonus() + p.getIceDamageBonus()
                               + p.getPoisonDamageBonus() + p.getEnergyDamageBonus();

        float extraHealth = p.getExtraHealthGained();
        float baseSpeed = p.getProfile().speed;
        float speedIncrease = baseSpeed > 0 ? (p.getSpeed() - baseSpeed) / baseSpeed : 0;

        float healthBoost = 1.0f + totalDamageBonus * 0.15f;
        float damageBoost = 1.0f + (extraHealth / 50f) * 0.15f;
        float speedBoost  = 1.0f + Math.max(0, speedIncrease) * 0.1f;

        enemy.setVida_max(Math.round(enemy.getVida_max() * healthBoost));
        enemy.setVida(enemy.getVida_max());
        enemy.setDamage(Math.round(enemy.getDamage() * damageBoost));
        enemy.setSpeed(enemy.getSpeed() * speedBoost);
    }

    public void resetForNewWave() {
        enemiesSpawnedThisWave = 0;
        totalEnemiesForCurrentWave = 0;
        waveSpawningComplete = false;
        currentEnemyIndex = 0;
        spawnTimer = 0;
        cachedWaveEntries = null;
    }

    public boolean isWaveSpawningComplete() {
        return waveSpawningComplete;
    }

    public int getEnemiesRemainingToSpawn() {
        return totalEnemiesForCurrentWave - enemiesSpawnedThisWave;
    }

    public Array<Projectile> getEnemyProjectiles() {
        return enemyProjectiles;
    }
}
