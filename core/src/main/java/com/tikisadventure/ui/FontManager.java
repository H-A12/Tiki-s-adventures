package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import java.util.HashMap;
import java.util.Map;

public class FontManager {
    private static FreeTypeFontGenerator generator;
    private static final Map<String, BitmapFont> cache = new HashMap<>();

    // La Skin global que evitará que las fuentes se borren
    private static Skin globalSkin;

    public static BitmapFont getFont(int size) {
        return getFont(size, 0f);
    }

    public static BitmapFont getFont(int size, float borderWidth) {
        String key = size + "|" + borderWidth;
        if (!cache.containsKey(key)) {
            if (generator == null)
                generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PressStart2P.ttf"));
            FreeTypeFontParameter p = new FreeTypeFontParameter();
            p.size = size;
            p.borderWidth = borderWidth;
            p.borderColor = Color.BLACK;
            p.hinting = FreeTypeFontGenerator.Hinting.None;
            p.gamma = 1.8f;
            cache.put(key, generator.generateFont(p));
        }
        return cache.get(key);
    }

    public static void addLabelStylesToSkin(Skin skin) {
        int[] sizes = {12, 13, 14, 15, 16, 18, 21, 27, 30, 38};
        for (int size : sizes)
            skin.add("font-" + size, new Label.LabelStyle(getFont(size), Color.WHITE));
    }

    // Método público para obtener la skin global
    public static Skin getGlobalSkin() {
        if (globalSkin == null) {
            globalSkin = loadMenuSkin();
        }
        return globalSkin;
    }

    // Ahora es privado para que nadie cree instancias nuevas por accidente
    private static Skin loadMenuSkin() {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("SkinsMenu/flat/skin/skin.atlas"));
        Skin skin = new Skin(atlas);

        BitmapFont font = getFont(15);
        skin.add("default", font, BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);

        String json = Gdx.files.internal("SkinsMenu/flat/skin/skin.json").readString();
        json = json.replaceAll(
            "com\\.badlogic\\.gdx\\.graphics\\.g2d\\.BitmapFont\\s*:\\s*\\{[^}]*\\{[^}]*\\}[^}]*\\}\\s*,?\\s*",
            "");

        FileHandle tmp = Gdx.files.local("cache/skin_temp.json");
        tmp.writeString(json, false);
        skin.load(tmp);
        tmp.delete();

        addLabelStylesToSkin(skin);
        return skin;
    }

    public static void dispose() {
        if (globalSkin != null) {
            globalSkin.dispose();
            globalSkin = null;
        }
        for (BitmapFont font : cache.values())
            font.dispose();
        cache.clear();
        if (generator != null) {
            generator.dispose();
            generator = null;
        }
    }
}
