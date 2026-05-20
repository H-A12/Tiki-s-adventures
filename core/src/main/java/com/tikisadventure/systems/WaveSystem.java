package com.tikisadventure.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import com.tikisadventure.core.GameSession;

import java.util.List;
import java.util.Random;

public class WaveSystem {

    public static final int WAVES_PER_STAGE = 5;
    public static final float WAVE_DELAY = 3.0f;
    public static final float BOSS_WAVE_DELAY = 8.0f;

    private int currentStage = 1;
    private int waveInStage = 0;
    private int globalWaveCount = 0;
    private int totalStages = 10;
    private int maxWavesForCurrentStage = 10;

    private float baseDifficulty = 1.0f;

    private String biome;
    private WaveGenerator waveGenerator;
    private List<WaveGenerator.WaveEntry> currentWaveEnemies;
    private Random rng;

    private boolean infiniteMode = false;
    private int infiniteWaveCount = 0;
    private boolean waveDelayActive = false;
    private float waveDelayTimer = 0;
    private float currentWaveDelay = WAVE_DELAY;

    public WaveSystem(String biome) {
        this.biome = biome;
        this.waveGenerator = new WaveGenerator();
        this.rng = GameSession.getSeededRandomForStage(1);
        loadStageConfig();
    }

    private void loadStageConfig() {
        try {
            JsonValue root = new JsonReader().parse(
                Gdx.files.internal("data/stage_config.json")
            );
            totalStages = root.getInt("total_stages", 10);
        } catch (Exception e) {
            totalStages = 10;
        }
    }

    public void initStage(int stage, int maxWavesForStage) {
        this.currentStage = stage;
        this.maxWavesForCurrentStage = isBossStage() ? 1 : maxWavesForStage;
        this.waveInStage = 0;
        this.rng = GameSession.getSeededRandomForStage(stage);
    }

    public void nextWave() {
        waveInStage++;
        globalWaveCount++;
        if (infiniteMode) infiniteWaveCount++;
        currentWaveEnemies = waveGenerator.generate(globalWaveCount, biome, currentStage, totalStages, rng, infiniteMode);
    }

    public void startWaveDelay() {
        startWaveDelay(WAVE_DELAY);
    }

    public void startWaveDelay(float delay) {
        waveDelayActive = true;
        waveDelayTimer = 0;
        currentWaveDelay = delay;
    }

    public void update(float delta) {
        if (waveDelayActive) {
            waveDelayTimer += delta;
        }
    }

    public boolean isWaveDelayComplete() {
        return waveDelayActive && waveDelayTimer >= currentWaveDelay;
    }

    public void clearWaveDelay() {
        waveDelayActive = false;
        waveDelayTimer = 0;
    }

    public boolean isBossStage() {
        return currentStage == totalStages;
    }

    public void enterInfiniteMode() {
        infiniteMode = true;
        infiniteWaveCount = 0;
    }

    public boolean isInfiniteMode() {
        return infiniteMode;
    }

    public boolean isWaveComplete(int enemiesRemaining) {
        return enemiesRemaining == 0;
    }

    public float getHealthMultiplier() {
        return 1.0f + 0.15f * (Math.max(1, globalWaveCount) - 1) + 0.15f * infiniteWaveCount;
    }

    public float getDamageMultiplier() {
        return 1.0f + 0.05f * (Math.max(1, globalWaveCount) - 1) + 0.05f * infiniteWaveCount;
    }

    public float getSpeedMultiplier() {
        return 1.0f + 0.01f * (Math.max(1, globalWaveCount) - 1) + 0.01f * infiniteWaveCount;
    }

    public float getExpMultiplier() {
        return 1.0f + 0.08f * (Math.max(1, globalWaveCount) - 1) + 0.08f * infiniteWaveCount;
    }

    public float getDifficultyMultiplier() {
        return 1.0f + 0.1f * (Math.max(1, globalWaveCount) - 1) + 0.1f * infiniteWaveCount;
    }

    public boolean hasMoreWavesInStage() {
        if (infiniteMode) return true;
        return waveInStage < maxWavesForCurrentStage;
    }

    public boolean hasMoreStages() {
        return currentStage < totalStages;
    }

    public int getWaveInStage() {
        return waveInStage;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public int getGlobalWaveCount() {
        return globalWaveCount;
    }

    public List<WaveGenerator.WaveEntry> getCurrentWaveEnemies() {
        return currentWaveEnemies;
    }

    public int getTotalEnemiesForCurrentWave() {
        if (currentWaveEnemies == null) return 0;
        int total = 0;
        for (WaveGenerator.WaveEntry entry : currentWaveEnemies) {
            total += entry.count;
        }
        return total;
    }

    public String[] getEnemyTypesForCurrentWave() {
        if (currentWaveEnemies == null) return new String[0];
        String[] types = new String[currentWaveEnemies.size()];
        for (int i = 0; i < currentWaveEnemies.size(); i++) {
            types[i] = currentWaveEnemies.get(i).type;
        }
        return types;
    }

    public int getCurrentWaveNumber() {
        return globalWaveCount;
    }



    public boolean isWaveDelayActive() {
        return waveDelayActive;
    }

    public String getBiome() {
        return biome;
    }
}
