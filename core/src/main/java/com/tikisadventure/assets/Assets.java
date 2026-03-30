package com.tikisadventure.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.Gdx;

public class Assets {
    private static AssetManager manager = new AssetManager();
    private static TextureAtlas atlas;
    private static JsonValue enemyConfigs;

    // Rutas según tu estructura de archivos
    public static final String GAME_ATLAS = "sprites/game_assets.atlas";
    public static final String ENEMY_DATA = "config/enemy_config.json";

    /**
     * Carga inicial del Atlas. Se llama en el inicio de Main o SplashScreen.
     */
    public static void load() {
        manager.load(GAME_ATLAS, TextureAtlas.class);
    }

    /**
     * Se ejecuta en el render loop de la pantalla de carga.
     * Devuelve true cuando todo está listo.
     */
    public static boolean update() {
        boolean finished = manager.update();
        if (finished && atlas == null) {
            atlas = manager.get(GAME_ATLAS, TextureAtlas.class);

            // Carga del JSON de configuración
            try {
                JsonReader reader = new JsonReader();
                enemyConfigs = reader.parse(Gdx.files.internal(ENEMY_DATA));
            } catch (Exception e) {
                Gdx.app.error("Assets", "Error al leer " + ENEMY_DATA);
            }
        }
        return finished;
    }

    /**
     * Obtiene una animación automáticamente buscando secuencias en el Atlas.
     * Si en el Atlas tienes "slime_0", "slime_1", usas getAnimation("slime", 0.1f).
     */
    public static Animation<TextureRegion> getAnimation(String name, float frameDuration) {
        if (atlas == null) return null;
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(name);
        if (regions.size == 0) return null;

        return new Animation<>(frameDuration, regions, Animation.PlayMode.LOOP);
    }

    /**
     * Obtiene una sola imagen del Atlas.
     */
    public static TextureRegion getTexture(String name) {
        if (atlas == null) return null;
        return atlas.findRegion(name);
    }

    /**
     * Retorna los datos de configuración de un enemigo del JSON.
     */
    public static JsonValue getEnemyConfig(String name) {
        return (enemyConfigs != null) ? enemyConfigs.get(name) : null;
    }

    public static float getProgress() {
        return manager.getProgress();
    }

    public static void dispose() {
        manager.dispose();
        if (atlas != null) atlas.dispose();
    }
}
