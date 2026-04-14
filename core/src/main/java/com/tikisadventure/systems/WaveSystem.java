package com.tikisadventure.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class WaveSystem {

    private int currentWave = 0;
    private int maxWave = 5;
    private float waveMultiplier = 0.1f;
    private float baseDifficulty = 1.0f;

    private JsonValue enemyConfig;
    private JsonValue currentWaveSection;

    private float difficultyMultiplier = 1.0f;

    public WaveSystem() {
        loadConfig("waves_default");
    }

    public WaveSystem(String waveSection) {
        loadConfig(waveSection);
    }

    private void loadConfig(String waveSection) {
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(Gdx.files.internal("data/enemy_config.json"));

        waveMultiplier = root.getFloat("wave_multiplier", 0.1f);
        baseDifficulty = root.getFloat("base_wave_difficulty", 1.0f);
        enemyConfig = root.get("enemies");
        
        loadWaveSection(waveSection);
    }

    public void loadWaveSection(String waveSection) {
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(Gdx.files.internal("data/enemy_config.json"));
        
        String sectionKey = waveSection.startsWith("waves_") ? waveSection : "waves_" + waveSection;
        currentWaveSection = root.get(sectionKey);
        
        if (currentWaveSection != null) {
            maxWave = currentWaveSection.size;
        } else {
            currentWaveSection = root.get("waves_default");
            maxWave = currentWaveSection != null ? currentWaveSection.size : 5;
        }
        
        reset();
    }

    public void nextWave() {
        if (currentWave < maxWave) {
            currentWave++;
            difficultyMultiplier = baseDifficulty + (currentWave * waveMultiplier);
        }
    }

    public boolean isWaveComplete(int enemiesRemaining) {
        return enemiesRemaining == 0;
    }

    public float getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void reset() {
        currentWave = 1;
        updateDifficultyMultiplier();
    }

    public JsonValue getEnemyConfig(String enemyType) {
        if (enemyConfig == null) return null;
        return enemyConfig.get(enemyType);
    }

    public JsonValue getCurrentWaveConfig() {
        if (currentWaveSection == null || currentWave == 0) return null;
        String waveKey = String.valueOf(currentWave);
        return currentWaveSection.get(waveKey);
    }

    public float getScaledStat(float baseValue) {
        return baseValue * difficultyMultiplier;
    }

    public int getScaledStatInt(int baseValue) {
        return Math.round(baseValue * difficultyMultiplier);
    }

    public boolean hasMoreWaves() {
        return currentWave < maxWave;
    }

    public JsonValue getCurrentWaveEnemies() {
        if (currentWaveSection == null || currentWave == 0) return null;
        String waveKey = String.valueOf(currentWave);
        JsonValue waveConfig = currentWaveSection.get(waveKey);
        if (waveConfig == null) return null;
        return waveConfig.get("enemies");
    }

    public int getTotalEnemiesForCurrentWave() {
        JsonValue enemies = getCurrentWaveEnemies();
        if (enemies == null) return 0;
        
        int total = 0;
        for (JsonValue enemy : enemies) {
            total += enemy.getInt("count", 0);
        }
        return total;
    }

    public String[] getEnemyTypesForCurrentWave() {
        JsonValue enemies = getCurrentWaveEnemies();
        if (enemies == null) return new String[0];
        
        String[] types = new String[enemies.size];
        for (int i = 0; i < enemies.size; i++) {
            types[i] = enemies.get(i).getString("type");
        }
        return types;
    }

    public void setWave(int waveNumber) {
        if (waveNumber > 0 && waveNumber <= maxWave) {
            currentWave = waveNumber;
            updateDifficultyMultiplier();
        }
    }

    private void updateDifficultyMultiplier() {
        difficultyMultiplier = baseDifficulty + (waveMultiplier * (currentWave - 1));
    }
}
