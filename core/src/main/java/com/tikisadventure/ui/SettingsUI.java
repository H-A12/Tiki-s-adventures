package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.input.InputConfig;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SettingsUI extends Window {
    private static final Set<String> MOUSE_ONLY_ACTIONS = new HashSet<String>() {{
        add("manualAim");
        add("ability2");
    }};

    private final Skin skin;
    private Table contentTable;
    private Table tabTable;
    private TextButton navButton;
    private ClickListener navListener;
    private boolean waitingForKey = false;
    private boolean showLanguage;
    private Runnable onCloseCallback;
    private TextButton.TextButtonStyle btnStyle;
    private SelectBox<String> resSelector;
    private SelectBox.SelectBoxStyle smallSelectStyle; // Estilo reducido para evitar solapamiento

    public SettingsUI(Skin skin, boolean showLanguage, Runnable onCloseCallback) {
        super("", skin);
        this.skin = skin;
        this.showLanguage = showLanguage;
        this.onCloseCallback = onCloseCallback;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaConfiguracion.png")));
        setBackground(bgImage.getDrawable());

        btnStyle = new TextButton.TextButtonStyle();
        TextureRegionDrawable botonDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        btnStyle.up = botonDrawable;
        btnStyle.down = botonDrawable;
        btnStyle.over = botonDrawable;
        btnStyle.pressedOffsetX = 0;
        btnStyle.pressedOffsetY = 0;
        // Usamos una fuente más pequeña para los botones de ajustes
        btnStyle.font = skin.get("font-14", Label.LabelStyle.class).font;

        // Creamos un estilo de SelectBox con fuente más pequeña para que no se junten las opciones
        SelectBox.SelectBoxStyle baseStyle = skin.get(SelectBox.SelectBoxStyle.class);
        smallSelectStyle = new SelectBox.SelectBoxStyle(baseStyle);
        smallSelectStyle.font = skin.get("font-14", Label.LabelStyle.class).font;
        smallSelectStyle.listStyle = new List.ListStyle(baseStyle.listStyle);
        smallSelectStyle.listStyle.font = skin.get("font-14", Label.LabelStyle.class).font;

        setModal(true);
        setMovable(true);
        pad(35, 30, 30, 30);

        Label titleLabel = new Label("Ajustes", skin, "font-18");
        add(titleLabel).colspan(3).padBottom(6).row();

        tabTable = new Table();
        TextButton keyboardTab = new TextButton("Teclado", btnStyle);
        TextButton controllerTab = new TextButton("Mando", btnStyle);
        TextButton touchpadTab = new TextButton("Touchpad", btnStyle);

        tabTable.add(keyboardTab).padRight(10).width(110);
        tabTable.add(controllerTab).padRight(10).width(110);
        tabTable.add(touchpadTab).width(110);
        add(tabTable).colspan(3).center().padBottom(8).row();
        tabTable.setVisible(false);

        contentTable = new Table();
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();

        Pixmap pmScrollBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollBg.setColor(0.3f, 0.3f, 0.3f, 0.4f);
        pmScrollBg.fill();
        TextureRegionDrawable scrollBg = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollBg)));
        scrollBg.setMinWidth(8);
        pmScrollBg.dispose();

        Pixmap pmScrollKnob = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollKnob.setColor(0.5f, 0.5f, 0.5f, 0.7f);
        pmScrollKnob.fill();
        TextureRegionDrawable scrollKnob = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollKnob)));
        scrollKnob.setMinWidth(8);
        pmScrollKnob.dispose();

        scrollStyle.vScroll = scrollBg;
        scrollStyle.vScrollKnob = scrollKnob;

        ScrollPane scrollPane = new ScrollPane(contentTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFlickScroll(false);

        add(scrollPane).colspan(3).minSize(380, 400).fillX().expandY().padLeft(6).padRight(6).padBottom(8).row();

        keyboardTab.addListener(new Assets.HoverCursorListener());
        keyboardTab.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { showKeyboardSettings(); }
        });

        controllerTab.addListener(new Assets.HoverCursorListener());
        controllerTab.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { showControllerSettings(); }
        });

        touchpadTab.addListener(new Assets.HoverCursorListener());
        touchpadTab.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { showTouchpadSettings(); }
        });

        navButton = new TextButton("", btnStyle);
        navButton.addListener(new Assets.HoverCursorListener());
        add(navButton).colspan(3).center().padTop(4).width(180);

        showMainSettings();
        pack();
    }

    private void showMainSettings() {
        contentTable.clear();
        tabTable.setVisible(false);

        contentTable.add(new Label("Volumen:", skin, "font-14")).left().padLeft(20).padRight(10).padBottom(10);
        final Slider volumeSlider = new Slider(0, 1, 0.1f, false, skin);
        volumeSlider.setValue(0.5f);
        contentTable.add(volumeSlider).fillX().colspan(2).padRight(16).padBottom(10).row();

        contentTable.add(new Label("Pantalla:", skin, "font-14")).left().padLeft(20).padRight(10).padBottom(10);
        resSelector = new SelectBox<>(smallSelectStyle);
        resSelector.setItems("800x480", "1280x720", "1920x1080");
        int currentWidth = Gdx.graphics.getWidth();
        if (currentWidth >= 1920) resSelector.setSelectedIndex(2);
        else if (currentWidth >= 1280) resSelector.setSelectedIndex(1);
        else resSelector.setSelectedIndex(0);
        resSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resSelector.hideList();
                String[] partes = resSelector.getSelected().split("x");
                int ancho = Integer.parseInt(partes[0]);
                int alto = Integer.parseInt(partes[1]);
                SaveManager.saveResolution(ancho, alto);
                Gdx.graphics.setWindowedMode(ancho, alto);
                if (getStage() != null && getStage().getViewport() != null) {
                    getStage().getViewport().update(ancho, alto, true);
                }
            }
        });
        contentTable.add(resSelector).fillX().colspan(2).padRight(16).padBottom(10).row();

        if (showLanguage) {
            contentTable.add(new Label("Idioma:", skin, "font-14")).left().padLeft(20).padRight(10).padBottom(10);
            SelectBox<String> langSelector = new SelectBox<>(smallSelectStyle);
            langSelector.setItems("Espa\u00F1ol", "Ingl\u00E9s");
            langSelector.setSelectedIndex(0);
            contentTable.add(langSelector).fillX().colspan(2).padRight(16).padBottom(10).row();
        }

        TextButton btnControles = new TextButton("Controles", btnStyle);
        btnControles.addListener(new Assets.HoverCursorListener());
        btnControles.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                showControlsSettings();
            }
        });
        contentTable.add(btnControles).colspan(3).center().width(180).padTop(24).padBottom(4).row();

        contentTable.add().colspan(3).height(40).row();

        navButton.setText("Volver");
        if (navListener != null) navButton.removeListener(navListener);
        navListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onCloseCallback != null) onCloseCallback.run();
            }
        };
        navButton.addListener(navListener);
    }

    private void showControlsSettings() {
        tabTable.setVisible(true);
        showKeyboardSettings();

        navButton.setText("Volver a ajustes");
        if (navListener != null) navButton.removeListener(navListener);
        navListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMainSettings();
            }
        };
        navButton.addListener(navListener);
    }

    private void showKeyboardSettings() {
        contentTable.clear();
        contentTable.add(new Label("Controles Generales", skin, "font-14")).colspan(2).padLeft(20).padBottom(12).row();

        InputConfig config = SaveManager.getProfileData().inputConfig;

        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, false);
            contentTable.row().padBottom(10);
        }

        contentTable.add(new Label("__________________________", skin)).colspan(2).padLeft(20).pad(10).row();
        contentTable.add(new Label("Acciones de Raton", skin, "font-14")).colspan(2).padLeft(20).padBottom(12).row();

        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (!MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, true);
            contentTable.row().padBottom(10);
        }

        contentTable.add(new Label("Tama\u00F1o Cursor:", skin, "font-14")).padLeft(20).padRight(10).left();
        final Slider mouseSizeSlider = new Slider(0.5f, 1.5f, 0.1f, false, skin);
        mouseSizeSlider.setValue(SaveManager.getProfileData().inputConfig.mouseSize);
        mouseSizeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float newSize = mouseSizeSlider.getValue();
                SaveManager.getProfileData().inputConfig.mouseSize = newSize;
                Assets.updateCursorScale(newSize);
            }
        });
        contentTable.add(mouseSizeSlider).width(130).colspan(1).padRight(16).padTop(10).row();

        TextButton resetBtn = new TextButton("Restablecer a Default", btnStyle);
        resetBtn.addListener(new Assets.HoverCursorListener());
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                config.resetToDefaults();
                mouseSizeSlider.setValue(1.0f);
                SaveManager.getProfileData().inputConfig.mouseSize = 1.0f;
                Assets.updateCursorScale(1.0f);
                SaveManager.saveProfileData();
                showKeyboardSettings();
            }
        });
        contentTable.add(resetBtn).colspan(2).center().width(240).padTop(24);
        contentTable.row().padBottom(12);
        contentTable.add().colspan(2);
    }

    private void addCellToSettingsTable(final String action, int currentCode, final InputConfig config, final boolean isOnlyMouse) {
        contentTable.add(new Label(action, skin, "font-14")).padLeft(20).padRight(10).left();
        boolean isMovement = action.equals("up") || action.equals("down") || action.equals("left") || action.equals("right");
        TextButton btn = new TextButton(getInputName(currentCode, isOnlyMouse || (!isMovement && currentCode >= 0 && currentCode <= 4)), btnStyle);
        btn.addListener(new Assets.HoverCursorListener());
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startWaitingForKey(action, btn, !isMovement, isOnlyMouse);
            }
        });
        contentTable.add(btn).width(150).padRight(20);
    }

    private void startWaitingForKey(String action, TextButton btn, boolean allowMouse, boolean isOnlyMouse) {
        waitingForKey = true;
        btn.setText(isOnlyMouse ? "Esperando..." : "Presiona...");
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (isOnlyMouse) return false;
                if (waitingForKey) { saveMapping(action, keycode, false); return true; }
                return false;
            }
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (waitingForKey && allowMouse && (!isOnlyMouse || InputConfig.isValidInput(button, true))) {
                    saveMapping(action, button, true); return true;
                }
                return false;
            }
        });
    }

    private void saveMapping(String action, int code, boolean isButton) {
        if (!InputConfig.isValidInput(code, isButton)) {
            waitingForKey = false; Gdx.input.setInputProcessor(getStage()); showKeyboardSettings(); return;
        }
        InputConfig config = SaveManager.getProfileData().inputConfig;
        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (!entry.getKey().equals(action) && entry.getValue() == code) {
                config.keyboardMapping.put(entry.getKey(), config.keyboardMapping.get(action));
                break;
            }
        }
        config.keyboardMapping.put(action, code);
        SaveManager.saveProfileData();
        waitingForKey = false;
        Gdx.input.setInputProcessor(getStage());
        showKeyboardSettings();
    }

    private void showControllerSettings() {
        contentTable.clear(); contentTable.add(new Label("Controles de Mando (Pr\u00F3ximamente)", skin, "font-14")).row();
    }

    private void showTouchpadSettings() {
        contentTable.clear(); contentTable.add(new Label("Controles de Touchpad (Pr\u00F3ximamente)", skin, "font-14")).row();
    }

    public void sincronizarSelectorResolucion() {
        if (resSelector == null) return;
        if (Gdx.graphics.isFullscreen()) {
            int w = Gdx.graphics.getWidth();
            if (w >= 1920) resSelector.setSelectedIndex(2);
            else if (w >= 1280) resSelector.setSelectedIndex(1);
            else resSelector.setSelectedIndex(0);
        } else {
            int w = Gdx.graphics.getWidth();
            if (w >= 1920) resSelector.setSelectedIndex(2);
            else if (w >= 1280) resSelector.setSelectedIndex(1);
            else resSelector.setSelectedIndex(0);
        }
    }

    private String getInputName(int code, boolean isButton) {
        if (isButton) {
            switch (code) {
                case Input.Buttons.LEFT: return "Left Click";
                case Input.Buttons.RIGHT: return "Right Click";
                case Input.Buttons.MIDDLE: return "Middle Click";
                case Input.Buttons.BACK: return "Back";
                case Input.Buttons.FORWARD: return "Forward";
                default: return "Button " + code;
            }
        }
        return Input.Keys.toString(code);
    }
}
