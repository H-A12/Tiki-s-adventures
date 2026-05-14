package com.tikisadventure.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveGenerator {

    private static final int BASE_BUDGET = 40;
    private static final int BUDGET_PER_WAVE = 6;
    private static final int BUDGET_PER_STAGE = 30;
    private static final int MAX_PER_TYPE = 8;
    private static final int BOSS_BUDGET_MULTIPLIER = 3;

    private static JsonValue enemyConfig;

    static {
        try {
            enemyConfig = new JsonReader().parse(
                Gdx.files.internal("data/enemy_config.json")
            ).get("enemies");
        } catch (Exception e) {
            Gdx.app.error("WaveGenerator", "Failed to load enemy_config.json", e);
            enemyConfig = null;
        }
    }

    public static class WaveEntry {
        public String type;
        public int count;

        public WaveEntry(String type, int count) {
            this.type = type;
            this.count = count;
        }
    }

    public List<WaveEntry> generate(int globalWaveCount, String biome, int stage, int totalStages, Random rng, boolean infiniteMode) {
        if (enemyConfig == null) {
            List<WaveEntry> fallback = new ArrayList<>();
            fallback.add(new WaveEntry("slime", 3));
            return fallback;
        }

        List<EligibleEnemy> pool = buildPool(biome, globalWaveCount);

        if (pool.isEmpty()) {
            List<WaveEntry> fallback = new ArrayList<>();
            fallback.add(new WaveEntry("slime", 3));
            return fallback;
        }

        boolean isBossWave = !infiniteMode && isBossStage(stage, totalStages);
        boolean isForestBossWave = isBossWave && "bosque".equals(biome);
        boolean isDesertBossWave = isBossWave && "desierto".equals(biome);

        int budget;
        if (isBossWave) {
            budget = calculateBudget(globalWaveCount) * BOSS_BUDGET_MULTIPLIER;
        } else {
            budget = calculateBudget(globalWaveCount) + stage * 2;
        }

        List<WaveEntry> composition = new ArrayList<>();
        int[] selectedCounts = new int[pool.size()];

        if (isForestBossWave) {
            composition.add(new WaveEntry("forest_boss", 1));
            return composition;
        }

        if (isDesertBossWave) {
            composition.add(new WaveEntry("desert_boss", 1));
            return composition;
        }

        if (isBossWave) {
            for (EligibleEnemy e : pool) {
                if (e.cost > budget) continue;
                int count = Math.min(budget / e.cost, MAX_PER_TYPE * 2);
                if (count > 0) {
                    composition.add(new WaveEntry(e.type, count));
                    budget -= count * e.cost;
                }
            }
            if (!composition.isEmpty()) return composition;
        }

        int cheapestCost = Integer.MAX_VALUE;
        for (EligibleEnemy e : pool) {
            if (e.cost < cheapestCost) cheapestCost = e.cost;
        }

        int attempts = 0;
        while (budget >= cheapestCost && attempts < 50) {
            attempts++;

            int idx = pickWeightedEnemy(pool, selectedCounts, budget, rng);
            if (idx < 0) break;

            EligibleEnemy chosen = pool.get(idx);
            int maxCount = Math.min(budget / chosen.cost, MAX_PER_TYPE);
            if (maxCount <= 0) break;

            int count = maxCount == 1 ? 1 : rng.nextInt(maxCount) + 1;
            count = Math.min(count, maxCount);

            composition.add(new WaveEntry(chosen.type, count));
            selectedCounts[idx] += count;
            budget -= count * chosen.cost;
        }

        if (composition.isEmpty()) {
            composition.add(new WaveEntry("slime", 3));
        }

        return composition;
    }

    private boolean isBossStage(int stage, int totalStages) {
        return stage >= totalStages;
    }

    private int calculateBudget(int globalWaveCount) {
        return BASE_BUDGET + globalWaveCount * BUDGET_PER_WAVE;
    }

    private List<EligibleEnemy> buildPool(String biome, int globalWaveCount) {
        List<EligibleEnemy> pool = new ArrayList<>();
        if (enemyConfig == null) return pool;

        for (JsonValue enemy : enemyConfig) {
            String name = enemy.name();
            JsonValue biomes = enemy.get("biomes");
            if (biomes == null) continue;

            boolean biomeMatch = false;
            for (JsonValue b : biomes) {
                if (b.asString().equals(biome)) {
                    biomeMatch = true;
                    break;
                }
            }
            if (!biomeMatch) continue;

            int unlockWave = enemy.getInt("unlock_wave", 1);
            if (globalWaveCount < unlockWave) continue;

            int cost = enemy.getInt("difficulty_cost", 5);
            pool.add(new EligibleEnemy(name, cost));
        }
        return pool;
    }

    private int pickWeightedEnemy(List<EligibleEnemy> pool, int[] selectedCounts, int budget, Random rng) {
        float totalWeight = 0;
        float[] weights = new float[pool.size()];

        for (int i = 0; i < pool.size(); i++) {
            EligibleEnemy e = pool.get(i);
            if (e.cost > budget) {
                weights[i] = 0;
                continue;
            }
            float varietyBonus = 1.0f + (selectedCounts[i] == 0 ? 3.0f : 0.5f / (selectedCounts[i] + 1));
            float costFactor = 1.0f / e.cost;
            weights[i] = costFactor * varietyBonus;
            totalWeight += weights[i];
        }

        if (totalWeight <= 0) return -1;

        float roll = rng.nextFloat() * totalWeight;
        float cumulative = 0;
        for (int i = 0; i < pool.size(); i++) {
            cumulative += weights[i];
            if (roll <= cumulative) return i;
        }

        for (int i = pool.size() - 1; i >= 0; i--) {
            if (weights[i] > 0) return i;
        }
        return -1;
    }

    private static class EligibleEnemy {
        String type;
        int cost;

        EligibleEnemy(String type, int cost) {
            this.type = type;
            this.cost = cost;
        }
    }
}
