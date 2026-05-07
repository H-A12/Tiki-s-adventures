package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
        super("Controles", skin);
        this.skin = skin;
        this.onCloseCallback = onCloseCallback;

        setModal(true);
        setMovable(true);
        pad(20);

        Table tabTable = new Table();
        TextButton keyboardTab = new TextButton("Teclado", skin);
        TextButton controllerTab = new TextButton("Mando", skin);
        TextButton touchpadTab = new TextButton("Touchpad", skin);

        tabTable.add(keyboardTab).padRight(5);
        tabTable.add(controllerTab).padRight(5);
        tabTable.add(touchpadTab);
        add(tabTable).padBottom(15).row();

        contentTable = new Table();
        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setFlickScroll(false);

        add(scrollPane).minSize(500, 280).fillX().expandY().row();

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
                setVisible(false);
                // --- NUEVO: Ejecutamos el callback si existe ---
                if (onCloseCallback != null) {
                    onCloseCallback.run();
                }
            }
        });
        add(closeButton).padTop(15);

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

        TextButton resetBtn = new TextButton("Restablecer a Default", skin);
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                config.resetToDefaults();
                SaveManager.saveProfileData();
                showKeyboardSettings();
            }
        });
        contentTable.add(resetBtn).colspan(4).padTop(20).fillX();
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
