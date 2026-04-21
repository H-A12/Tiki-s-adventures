package com.tikisadventure.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    private static AssetManager manager;
    private static Map<String, TextureAtlas> atlases = new HashMap<>();
    public static ShaderProgram whiteFlashShader;
    public static TextureRegion[] numberRegions;
    public static TextureRegion trajectoryDot;
    private static Texture trajectoryDotTexture;
    public static Texture trajectoryDotTexture() { return trajectoryDotTexture; }
    public static ShaderProgram outlineShader;

    public static void load() {
        manager = new AssetManager();
        manager.load("atlas/shared.atlas", TextureAtlas.class);
        manager.load("atlas/moko.atlas", TextureAtlas.class);
        manager.load("atlas/tiki.atlas", TextureAtlas.class);
        manager.load("atlas/zuki.atlas", TextureAtlas.class);
        manager.load("sprites/shared/numbers_spritesheet.png", Texture.class);
        manager.load("SkinsMenu/flat/raw/dot.png", Texture.class);

        whiteFlashShader = new ShaderProgram(Gdx.files.internal("shaders/white_flash.vert"), Gdx.files.internal("shaders/white_flash.frag"));
        if (!whiteFlashShader.isCompiled()) {
            Gdx.app.error("Assets", "Error compilando Shader: " + whiteFlashShader.getLog());
        }

        String vertOutline = Gdx.files.internal("shaders/outline.vert").readString();
        String fragOutline = Gdx.files.internal("shaders/outline.frag").readString();
        outlineShader = new ShaderProgram(vertOutline, fragOutline);
        if (!outlineShader.isCompiled()) {
            Gdx.app.error("Shader", "Error compilando outlineShader: " + outlineShader.getLog());
        }
    }

    public static void finishLoading() {
        manager.finishLoading();
        atlases.put("shared", manager.get("atlas/shared.atlas", TextureAtlas.class));
        atlases.put("moko", manager.get("atlas/moko.atlas", TextureAtlas.class));
        atlases.put("tiki", manager.get("atlas/tiki.atlas", TextureAtlas.class));
        atlases.put("zuki", manager.get("atlas/zuki.atlas", TextureAtlas.class));

        Texture numberTex = manager.get("sprites/shared/numbers_spritesheet.png", Texture.class);
        int digitWidth = numberTex.getWidth() / 10;
        int digitHeight = numberTex.getHeight();
        numberRegions = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            numberRegions[i] = new TextureRegion(numberTex, i * digitWidth, 0, digitWidth, digitHeight);
        }

        trajectoryDotTexture = manager.get("SkinsMenu/flat/raw/dot.png", Texture.class);
        trajectoryDot = new TextureRegion(trajectoryDotTexture);
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
        if (whiteFlashShader != null) {
            whiteFlashShader.dispose();
            whiteFlashShader = null;
        }
    }
}
