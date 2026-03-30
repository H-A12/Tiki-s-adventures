package com.tikisadventure.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Gestor central de progresión. Carga la configuración desde JSON y escala stats.
 */
public class WaveSystem {

    private int currentWave = 0;
    private int maxWave = 5;
    private float waveMultiplier = 0.1f;
    private float baseDifficulty = 1.0f;

    private JsonValue root;
    private JsonValue enemyConfigs;
    private JsonValue currentWaveSection;

    private float difficultyMultiplier = 1.0f;

    public WaveSystem() {
        this("default");
    }

    public WaveSystem(String waveSection) {
        loadConfig(waveSection);
    }

    private void loadConfig(String waveSection) {
        JsonReader reader = new JsonReader();
        try {
            // Intentamos cargar el archivo desde la carpeta data
            root = reader.parse(Gdx.files.internal("data/enemy_config.json"));

            waveMultiplier = root.getFloat("wave_multiplier", 0.1f);
            baseDifficulty = root.getFloat("base_wave_difficulty", 1.0f);
            enemyConfigs = root.get("enemies");

            loadWaveSection(waveSection);
        } catch (Exception e) {
            Gdx.app.error("WaveSystem", "Error crítico cargando JSON: " + e.getMessage());
            // Creamos un root vacío para evitar NullPointerExceptions posteriores
            root = new JsonValue(JsonValue.ValueType.object);
        }
    }

    public void loadWaveSection(String waveSection) {
        if (root == null || root.size == 0) return;

        String sectionKey = waveSection.startsWith("waves_") ? waveSection : "waves_" + waveSection;
        currentWaveSection = root.get(sectionKey);

        if (currentWaveSection != null) {
            maxWave = currentWaveSection.size;
        } else {
            currentWaveSection = root.get("waves_default");
            maxWave = (currentWaveSection != null) ? currentWaveSection.size : 5;
        }

        reset();
    }

    public void nextWave() {
        if (currentWave < maxWave) {
            currentWave++;
            // Dificultad incremental: Dificultad = Base + (Oleada * Multiplicador)
            difficultyMultiplier = baseDifficulty + (currentWave * waveMultiplier);
        }
    }

    // --- MÉTODOS DE CONSULTA ---

    /**
     * Devuelve el multiplicador actual para que la factoría escale los enemigos.
     * SOLUCIONA EL ERROR: "cannot find symbol: method getDifficultyMultiplier()"
     */
    public float getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public JsonValue getEnemyConfig(String enemyType) {
        if (enemyConfigs == null) return null;
        return enemyConfigs.get(enemyType);
    }

    public float getScaledStat(float baseValue) {
        return baseValue * difficultyMultiplier;
    }

    public int getScaledStatInt(int baseValue) {
        return Math.round(baseValue * difficultyMultiplier);
    }

    public JsonValue getCurrentWaveEnemies() {
        if (currentWaveSection == null || currentWave == 0) return null;
        JsonValue waveData = currentWaveSection.get(String.valueOf(currentWave));
        return (waveData != null) ? waveData.get("enemies") : null;
    }

    public int getCurrentWave() { return currentWave; }
    public boolean hasMoreWaves() { return currentWave < maxWave; }

    public void reset() {
        currentWave = 0;
        difficultyMultiplier = 1.0f;
    }
}
