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
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.audio.AudioType;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;

//Carga texturas, atlas de sprites, shaders y cursores. Proporciona regiones
//de sprite para el juego completo. Tiene un listener para cambiar el cursor al
//pasar el ratón por encima de botones.
public class Assets {
    private static AssetManager manager;
    private static Map<String, TextureAtlas> atlases = new HashMap<>();
    public static ShaderProgram whiteFlashShader;
    public static TextureRegion[] numberRegions;
    public static TextureRegion dodgedRegion;
    public static TextureRegion trajectoryDot;
    private static Texture trajectoryDotTexture;
    public static Texture trajectoryDotTexture() { return trajectoryDotTexture; }
    public static ShaderProgram outlineShader;
    public static Texture whitePixel;
    private static Cursor customCursor;
    private static Cursor handCursor;
    private static Cursor hiddenCursor;
    private static String cursorPath = "sprites/shared/UI_assets/UI_Cursor.png";
    private static String handCursorPath = "sprites/shared/UI_assets/UI_Hand.png";

    //Cargar cursores
    public static void loadCursor() {
        updateCursorScale(1.0f);
        loadHiddenCursor();
    }

    //Cursor invisible
    private static void loadHiddenCursor() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        hiddenCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap.dispose();
    }

    //Ocultar cursor del sistema
    public static void hideSystemCursor() {
        Gdx.graphics.setCursor(hiddenCursor);
    }

    //Escalar cursor según resolución
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

        setDefaultCursor();
    }

    //Cursor de mano
    public static void setHandCursor() {
        Gdx.graphics.setCursor(handCursor);
    }

    //Cursor por defecto
    public static void setDefaultCursor() {
        Gdx.graphics.setCursor(customCursor);
    }

    //Cargar texturas, atlas y shaders
    public static void load() {
        manager = new AssetManager();
        manager.load("atlas/shared.atlas", TextureAtlas.class);
        manager.load("sprites/shared/particle_assets/numbers_spritesheet.png", Texture.class);
        manager.load("SkinsMenu/flat/raw/dot.png", Texture.class);

        whiteFlashShader = new ShaderProgram(Gdx.files.internal("shaders/white_flash.vert"), Gdx.files.internal("shaders/white_flash.frag"));
        if (!whiteFlashShader.isCompiled()) {
        }

        String vertOutline = Gdx.files.internal("shaders/outline.vert").readString();
        String fragOutline = Gdx.files.internal("shaders/outline.frag").readString();
        outlineShader = new ShaderProgram(vertOutline, fragOutline);
        if (!outlineShader.isCompiled()) {
        }
    }

    //Asignar regiones de sprites cargados
    public static void finishLoading() {
        manager.finishLoading();
        atlases.put("shared", manager.get("atlas/shared.atlas", TextureAtlas.class));
        atlases.put("moko", atlases.get("shared"));
        atlases.put("tiki", atlases.get("shared"));
        atlases.put("zuki", atlases.get("shared"));
        atlases.put("tikibot", atlases.get("shared"));

        Texture numberTex = manager.get("sprites/shared/particle_assets/numbers_spritesheet.png", Texture.class);
        int digitWidth = numberTex.getWidth() / 11;
        int digitHeight = numberTex.getHeight();
        numberRegions = new TextureRegion[11];
        for (int i = 0; i < 11; i++) {
            numberRegions[i] = new TextureRegion(numberTex, i * digitWidth, 0, digitWidth, digitHeight);
        }

        dodgedRegion = getRegion("shared", "particle_assets/dodged");

        trajectoryDotTexture = manager.get("SkinsMenu/flat/raw/dot.png", Texture.class);
        trajectoryDot = new TextureRegion(trajectoryDotTexture);

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(1, 1, 1, 1);
        pix.fill();
        whitePixel = new Texture(pix);
        pix.dispose();
    }

    //Obtener región de un atlas
    public static TextureRegion getRegion(String atlasName, String regionName) {
        return getRegion(atlasName, regionName, false);
    }

    public static TextureRegion getRegion(String atlasName, String regionName, boolean getFullRegion) {
        TextureAtlas atlas = atlases.get(atlasName);
        if (atlas == null) {
            return null;
        }

        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) {
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

    //Liberar todos los assets
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
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }

    public static class HoverCursorListener extends com.badlogic.gdx.scenes.scene2d.InputListener {
        @Override
        public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
            Assets.setHandCursor();
            if (pointer == -1) {
                AudioManager.playSFX(AudioType.UI_HOVER);
            }
        }

        @Override
        public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
            Assets.setDefaultCursor();
        }
    }
}
