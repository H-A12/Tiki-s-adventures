package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
    private boolean waitingForKey = false;
    private Runnable onCloseCallback; // <-- NUEVO: Callback para avisar a PauseUI

    public SettingsUI(Skin skin, Runnable onCloseCallback) { // <-- NUEVO: Parámetro extra
        super("", skin);
        this.skin = skin;
        this.onCloseCallback = onCloseCallback;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaConfiguracion.png")));
        setBackground(bgImage.getDrawable());

        TextureRegionDrawable botonDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        TextButton.TextButtonStyle btnStyle = skin.get(TextButton.TextButtonStyle.class);
        btnStyle.up = botonDrawable;
        btnStyle.down = botonDrawable;
        btnStyle.over = botonDrawable;
        btnStyle.pressedOffsetX = 0;
        btnStyle.pressedOffsetY = 0;
        btnStyle.font = skin.getFont("default-font");

        setModal(true);
        setMovable(true);
        pad(30, 25, 25, 25);

        Label titleLabel = new Label("Controles", skin);
        titleLabel.setFontScale(1.2f);
        add(titleLabel).colspan(3).padBottom(6).row();

        Table tabTable = new Table();
        TextButton keyboardTab = new TextButton("Teclado", skin);
        TextButton controllerTab = new TextButton("Mando", skin);
        TextButton touchpadTab = new TextButton("Touchpad", skin);

        tabTable.add(keyboardTab).padRight(18);
        tabTable.add(controllerTab).padRight(18);
        tabTable.add(touchpadTab);
        add(tabTable).colspan(3).center().padBottom(8).row();

        contentTable = new Table();
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        ScrollPane scrollPane = new ScrollPane(contentTable, scrollStyle);
        scrollPane.setFadeScrollBars(true);
        scrollPane.setFlickScroll(false);

        add(scrollPane).colspan(3).minSize(500, 260).fillX().expandY().row();

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

        TextButton closeButton = new TextButton("Cerrar", skin);
        closeButton.addListener(new Assets.HoverCursorListener());
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (waitingForKey) return;
                SaveManager.saveProfileData();
                addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.3f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.visible(false),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                        if (onCloseCallback != null) {
                            onCloseCallback.run();
                        }
                    })
                ));
            }
        });
        add(closeButton).colspan(3).center().padTop(8);

        showKeyboardSettings();
        pack();
    }

    private void showKeyboardSettings() {
        contentTable.clear();
        contentTable.add(new Label("Controles Generales", skin)).colspan(4).padBottom(10).row();

        InputConfig config = SaveManager.getProfileData().inputConfig;

        int count = 0;
        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;

            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, false);
            count++;
            if (count % 2 == 0) contentTable.row().padBottom(5);
        }
        if (count % 2 != 0) contentTable.row().padBottom(5);

        contentTable.add(new Label("__________________________", skin)).colspan(4).pad(15).row();
        contentTable.add(new Label("Acciones de Raton", skin)).colspan(4).padBottom(10).row();

        count = 0;
        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (!MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;

            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, true);
            count++;
            if (count % 2 == 0) contentTable.row().padBottom(5);
        }
        if (count % 2 != 0) contentTable.row().padBottom(5);

        contentTable.add(new Label("Tamaño Cursor:", skin)).padRight(10).left();
        Slider mouseSizeSlider = new Slider(0.5f, 2.0f, 0.1f, false, skin);
        mouseSizeSlider.setValue(SaveManager.getProfileData().inputConfig.mouseSize);
        mouseSizeSlider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float newSize = mouseSizeSlider.getValue();
                SaveManager.getProfileData().inputConfig.mouseSize = newSize;
                Assets.updateCursorScale(newSize);
            }
        });
        contentTable.add(mouseSizeSlider).width(110).colspan(3).padTop(10).row();

        TextButton resetBtn = new TextButton("Restablecer a Default", skin);
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                config.resetToDefaults();
                SaveManager.saveProfileData();
                showKeyboardSettings();
            }
        });
        contentTable.add(resetBtn).colspan(4).padTop(20).center();
    }

    private void addCellToSettingsTable(final String action, int currentCode, final InputConfig config, final boolean isOnlyMouse) {
        contentTable.add(new Label(action, skin)).padRight(10).left();
        boolean isMovement = action.equals("up") || action.equals("down") || action.equals("left") || action.equals("right");
        TextButton btn = new TextButton(getInputName(currentCode, isOnlyMouse || (!isMovement && currentCode >= 0 && currentCode <= 4)), skin);
        btn.addListener(new Assets.HoverCursorListener());
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startWaitingForKey(action, btn, !isMovement, isOnlyMouse);
            }
        });
        contentTable.add(btn).width(110).padRight(20);
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
        contentTable.clear(); contentTable.add(new Label("Controles de Mando (Próximamente)", skin)).row();
    }

    private void showTouchpadSettings() {
        contentTable.clear(); contentTable.add(new Label("Controles de Touchpad (Próximamente)", skin)).row();
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

    // --- NUEVO: Auto-escalado dinámico ---
    @Override
    public void act(float delta) {
        super.act(delta);

        if (getStage() != null) {
            // Calculamos la escala tomando 1280 como la resolución base (escala 1.0)
            float targetScale = com.badlogic.gdx.math.MathUtils.clamp(getStage().getWidth() / 1280f, 0.7f, 1.3f);

            if (getScaleX() != targetScale) {
                setTransform(true);
                setScale(targetScale);
                // Establecemos el origen en el centro exacto para que el escalado no la descentre
                setOrigin(getWidth() / 2f, getHeight() / 2f);
            }
        }
    }
}
