package com.tikisadventure.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
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
    private static Cursor customCursor;
    private static Cursor handCursor;
    private static Cursor hiddenCursor;
    private static String cursorPath = "sprites/shared/UI_assets/UI_Cursor.png";
    private static String handCursorPath = "sprites/shared/UI_assets/UI_Hand.png";

    public static void loadCursor() {
        updateCursorScale(1.0f);
        loadHiddenCursor();
    }

    private static void loadHiddenCursor() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent
        pixmap.fill();
        hiddenCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap.dispose();
    }

    public static void hideSystemCursor() {
        Gdx.graphics.setCursor(hiddenCursor);
    }

    public static void updateCursorScale(float scale) {
        if (customCursor != null) customCursor.dispose();
        if (handCursor != null) handCursor.dispose();

        Pixmap originalCursor = new Pixmap(Gdx.files.internal(cursorPath));
        Pixmap originalHand = new Pixmap(Gdx.files.internal(handCursorPath));

        int newWidth = MathUtils.nextPowerOfTwo((int) (originalCursor.getWidth() * scale));
        int newHeight = MathUtils.nextPowerOfTwo((int) (originalCursor.getHeight() * scale));
        Pixmap scaledCursorPix = new Pixmap(newWidth, newHeight, Pixmap.Format.RGBA8888);
        scaledCursorPix.drawPixmap(originalCursor, 0, 0, originalCursor.getWidth(), originalCursor.getHeight(), 0, 0, (int)(originalCursor.getWidth() * scale), (int)(originalCursor.getHeight() * scale));
        customCursor = Gdx.graphics.newCursor(scaledCursorPix, 0, 0);

        int newHandWidth = MathUtils.nextPowerOfTwo((int) (originalHand.getWidth() * scale));
        int newHandHeight = MathUtils.nextPowerOfTwo((int) (originalHand.getHeight() * scale));
        Pixmap scaledHandPix = new Pixmap(newHandWidth, newHandHeight, Pixmap.Format.RGBA8888);
        scaledHandPix.drawPixmap(originalHand, 0, 0, originalHand.getWidth(), originalHand.getHeight(), 0, 0, (int)(originalHand.getWidth() * scale), (int)(originalHand.getHeight() * scale));
        handCursor = Gdx.graphics.newCursor(scaledHandPix, 0, 0);

        originalCursor.dispose();
        originalHand.dispose();
        scaledCursorPix.dispose();
        scaledHandPix.dispose();

        // Aplicar el cursor por defecto (actualiza si ya estaba puesto)
        setDefaultCursor();
    }

    public static void setHandCursor() {
        Gdx.graphics.setCursor(handCursor);
    }

    public static void setDefaultCursor() {
        Gdx.graphics.setCursor(customCursor);
    }

    public static void load() {
        manager = new AssetManager();
        manager.load("atlas/shared.atlas", TextureAtlas.class);
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
        atlases.put("moko", atlases.get("shared"));
        atlases.put("tiki", atlases.get("shared"));
        atlases.put("zuki", atlases.get("shared"));
        atlases.put("tikibot", atlases.get("shared"));

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
        if (customCursor != null) {
            customCursor.dispose();
            customCursor = null;
        }
        if (handCursor != null) {
            handCursor.dispose();
            handCursor = null;
        }
        if (hiddenCursor != null) {
            hiddenCursor.dispose();
            hiddenCursor = null;
        }
    }

    public static class HoverCursorListener extends com.badlogic.gdx.scenes.scene2d.InputListener {
        @Override
        public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
            Assets.setHandCursor();
        }

        @Override
        public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
            Assets.setDefaultCursor();
        }
    }
}
