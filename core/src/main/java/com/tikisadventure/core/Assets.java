package com.tikisadventure.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
    private static AssetManager manager;
    private static TextureAtlas atlas;

    public static void load() {
        manager = new AssetManager();
        manager.load("atlas/sprites.atlas", TextureAtlas.class);
    }

    public static void finishLoading() {
        manager.finishLoading();
        atlas = manager.get("atlas/sprites.atlas", TextureAtlas.class);
    }

    public static TextureRegion getRegion(String name) {
        if (atlas == null) {
            Gdx.app.error("Assets", "Atlas es nulo!");
            return null;
        }
        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            Gdx.app.error("Assets", "No se encontró la región: " + name);
        }
        return region;
    }

    public static void dispose() {
        manager.dispose();
    }
}
