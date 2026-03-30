package com.tikisadventure.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
    private static AssetManager manager;
    private static TextureAtlas atlas;

    public static void load() {
        manager = new AssetManager();
        // Cargamos el atlas. Necesitas generar sprites.atlas usando TexturePacker
        manager.load("atlas/sprites.atlas", TextureAtlas.class);
    }

    public static void finishLoading() {
        manager.finishLoading();
        atlas = manager.get("atlas/sprites.atlas", TextureAtlas.class);
    }

    public static TextureRegion getRegion(String name) {
        if (atlas == null) return null;
        return atlas.findRegion(name);
    }

    public static void dispose() {
        manager.dispose();
    }
}
