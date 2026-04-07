package com.tikisadventure.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    private static AssetManager manager;
    private static Map<String, TextureAtlas> atlases = new HashMap<>();

    public static void load() {
        manager = new AssetManager();
        manager.load("atlas/shared.atlas", TextureAtlas.class);
        manager.load("atlas/moko.atlas", TextureAtlas.class);
        manager.load("atlas/tiki.atlas", TextureAtlas.class);
        manager.load("atlas/zuki.atlas", TextureAtlas.class);
    }

    public static void finishLoading() {
        manager.finishLoading();
        atlases.put("shared", manager.get("atlas/shared.atlas", TextureAtlas.class));
        atlases.put("moko", manager.get("atlas/moko.atlas", TextureAtlas.class));
        atlases.put("tiki", manager.get("atlas/tiki.atlas", TextureAtlas.class));
        atlases.put("zuki", manager.get("atlas/zuki.atlas", TextureAtlas.class));
    }

    public static TextureRegion getRegion(String atlasName, String regionName) {
        return getRegion(atlasName, regionName, false);
    }

    public static TextureRegion getRegion(String atlasName, String regionName, boolean getFullRegion) {
        TextureAtlas atlas = atlases.get(atlasName);
        if (atlas == null) {
            Gdx.app.error("Assets", "Atlas no encontrado: " + atlasName);
            return null;
        }
        
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) {
            Gdx.app.error("Assets", "No se encontró la región: " + regionName + " en el atlas: " + atlasName);
            return null;
        }
        
        if (getFullRegion && region.getRegionWidth() < 64) {
            for (TextureAtlas.AtlasRegion ar : atlas.getRegions()) {
                if (ar.name.equals(regionName)) {
                    return new TextureRegion(ar.getTexture(), ar.packedWidth, ar.packedHeight);
                }
            }
        }
        
        return region;
    }

    public static void dispose() {
        manager.dispose();
    }
}
