package com.tikisadventure.ui.button;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.tikisadventure.ui.FontManager;

public class ButtonFactory {
    private static Texture texBotonText;
    private static Texture texBotonAlargado;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        texBotonText = new Texture(Gdx.files.internal("Menu/BotonText.png"));
        texBotonAlargado = new Texture(Gdx.files.internal("Menu/BotonAlargado.png"));
        initialized = true;
    }

    private static TextButton.TextButtonStyle createTextBtnStyle() {
        init();
        Skin skin = FontManager.getGlobalSkin();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(texBotonText));
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = drawable;
        style.down = drawable;
        style.over = drawable;
        style.font = skin.get("font-14", Label.LabelStyle.class).font;
        style.pressedOffsetX = 0;
        style.pressedOffsetY = 0;
        return style;
    }

    private static TextButton.TextButtonStyle createTabBtnStyle() {
        init();
        Skin skin = FontManager.getGlobalSkin();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(texBotonAlargado));
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = drawable;
        style.down = drawable;
        style.over = drawable;
        style.checked = drawable;
        style.font = skin.get("font-14", Label.LabelStyle.class).font;
        style.pressedOffsetX = 0;
        style.pressedOffsetY = 0;
        return style;
    }

    public static TextButton createTextButton(String text, Runnable action) {
        TextButton button = new TextButton(text, createTextBtnStyle());
        configure(button, action);
        return button;
    }

    public static TextButton createTextButton(String text, int width, int height, Runnable action) {
        TextButton button = createTextButton(text, action);
        button.setSize(width, height);
        return button;
    }

    public static TextButton createTabButton(String text, Runnable action) {
        TextButton button = new TextButton(text, createTabBtnStyle());
        configure(button, action);
        return button;
    }

    public static ImageButton createImageButton(Drawable icon, Runnable action) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = icon;
        style.imageDown = icon;
        style.imageOver = icon;
        ImageButton button = new ImageButton(style);
        configure(button, action);
        return button;
    }

    public static <T extends Button> void configure(T button, Runnable action) {
        button.setTransform(true);
        button.setOrigin(Align.center);
        button.addListener(new StandardButtonListener(action));
    }

    public static TextButton.TextButtonStyle getTextBtnStyle() {
        return createTextBtnStyle();
    }

    public static Texture getBotonTextTex() {
        init();
        return texBotonText;
    }

    public static Texture getBotonAlargadoTex() {
        init();
        return texBotonAlargado;
    }

    public static void dispose() {
        if (texBotonText != null) { texBotonText.dispose(); texBotonText = null; }
        if (texBotonAlargado != null) { texBotonAlargado.dispose(); texBotonAlargado = null; }
        initialized = false;
    }
}
