package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.tikisadventure.core.SaveManager;
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

    public SettingsUI(Skin skin) {
        super("Configuración de Controles", skin);
        this.skin = skin;

        setModal(true);
        setMovable(true);
        pad(20);

        // Tabla de tabs
        Table tabTable = new Table();
        TextButton keyboardTab = new TextButton("Teclado", skin);
        TextButton controllerTab = new TextButton("Mando", skin);
        TextButton touchpadTab = new TextButton("Touchpad", skin);

        tabTable.add(keyboardTab);
        tabTable.add(controllerTab);
        tabTable.add(touchpadTab);
        add(tabTable).padBottom(10).row();

        // Contenido
        contentTable = new Table();
        add(contentTable).minSize(350, 250).row();

        // Lógica de Tabs
        keyboardTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { showKeyboardSettings(); }
        });
        controllerTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { showControllerSettings(); }
        });
        touchpadTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { showTouchpadSettings(); }
        });

        TextButton closeButton = new TextButton("Cerrar", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (waitingForKey) return;
                setVisible(false);
            }
        });
        add(closeButton).padTop(10);
        
        showKeyboardSettings();
        pack();
    }

    private String getInputName(int code, boolean isButton) {
        if (isButton) {
            switch (code) {
                case Input.Buttons.LEFT: return "Left Click";
                case Input.Buttons.RIGHT: return "Right Click";
                case Input.Buttons.MIDDLE: return "Middle Click";
                case Input.Buttons.BACK: return "Back Button";
                case Input.Buttons.FORWARD: return "Forward Button";
                default: return "Button " + code;
            }
        }
        return getFriendlyKeyName(code);
    }

    private String getFriendlyKeyName(int code) {
        switch (code) {
            case Input.Keys.ANY_KEY: return "Any Key";
            case Input.Keys.BACKSPACE: return "Backspace";
            case Input.Keys.TAB: return "Tab";
            case Input.Keys.ENTER: return "Enter";
            case Input.Keys.ESCAPE: return "Escape";
            case Input.Keys.SPACE: return "Space";
            case Input.Keys.PAGE_UP: return "Page Up";
            case Input.Keys.PAGE_DOWN: return "Page Down";
            case Input.Keys.END: return "End";
            case Input.Keys.HOME: return "Home";
            case Input.Keys.INSERT: return "Insert";
            case Input.Keys.UP: return "Up";
            case Input.Keys.DOWN: return "Down";
            case Input.Keys.LEFT: return "Left";
            case Input.Keys.RIGHT: return "Right";
            case Input.Keys.NUM_1: return "Num 1";
            case Input.Keys.NUM_2: return "Num 2";
            case Input.Keys.NUM_3: return "Num 3";
            case Input.Keys.NUM_4: return "Num 4";
            case Input.Keys.NUM_5: return "Num 5";
            case Input.Keys.NUM_6: return "Num 6";
            case Input.Keys.NUM_7: return "Num 7";
            case Input.Keys.NUM_8: return "Num 8";
            case Input.Keys.NUM_9: return "Num 9";
            case Input.Keys.SEMICOLON: return ";";
            case Input.Keys.COMMA: return ",";
            case Input.Keys.PERIOD: return ".";
            case Input.Keys.SLASH: return "/";
            case Input.Keys.BACKSLASH: return "\\";
            case Input.Keys.MINUS: return "-";
            case Input.Keys.EQUALS: return "=";
            case Input.Keys.APOSTROPHE: return "'";
            case Input.Keys.GRAVE: return "`";
            case Input.Keys.SHIFT_LEFT: return "Left Shift";
            case Input.Keys.SHIFT_RIGHT: return "Right Shift";
            case Input.Keys.CONTROL_LEFT: return "Left Ctrl";
            case Input.Keys.CONTROL_RIGHT: return "Right Ctrl";
            case Input.Keys.ALT_LEFT: return "Left Alt";
            case Input.Keys.ALT_RIGHT: return "Right Alt";
            case Input.Keys.CAPS_LOCK: return "Caps Lock";
            case Input.Keys.F1: return "F1";
            case Input.Keys.F2: return "F2";
            case Input.Keys.F3: return "F3";
            case Input.Keys.F4: return "F4";
            case Input.Keys.F5: return "F5";
            case Input.Keys.F6: return "F6";
            case Input.Keys.F7: return "F7";
            case Input.Keys.F8: return "F8";
            case Input.Keys.F9: return "F9";
            case Input.Keys.F10: return "F10";
            case Input.Keys.F11: return "F11";
            case Input.Keys.F12: return "F12";
            case Input.Keys.NUM: return "Num Lock";
            case Input.Keys.SCROLL_LOCK: return "Scroll Lock";
            default: return Input.Keys.toString(code);
        }
    }

    private void showKeyboardSettings() {
        contentTable.clear();
        contentTable.add(new Label("Controles Generales", skin)).colspan(2).padBottom(10).row();
        
        InputConfig config = SaveManager.getProfileData().inputConfig;
        
        // Primero: Controles que permiten teclado
        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            
            addRowToSettingsTable(entry.getKey(), entry.getValue(), config, false);
        }

        // Divisor para controles de solo ratón
        contentTable.add(new Label("__________________________", skin)).colspan(2).pad(15).row();
        contentTable.add(new Label("Acciones de Ratón", skin)).colspan(2).padBottom(10).row();

        // Segundo: Controles de solo ratón
        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (!MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            
            addRowToSettingsTable(entry.getKey(), entry.getValue(), config, true);
        }

        TextButton resetBtn = new TextButton("Restablecer a Default", skin);
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                config.resetToDefaults();
                SaveManager.saveProfileData();
                showKeyboardSettings();
            }
        });
        contentTable.add(resetBtn).colspan(2).padTop(20).fillX();
    }

    private void addRowToSettingsTable(final String action, int currentCode, final InputConfig config, final boolean isOnlyMouse) {
        contentTable.add(new Label(action, skin)).padRight(10).left();
        
        boolean isMovement = action.equals("up") || action.equals("down") || 
                             action.equals("left") || action.equals("right");

        TextButton btn = new TextButton(getInputName(currentCode, isOnlyMouse || (!isMovement && currentCode >= 0 && currentCode <= 4)), skin);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startWaitingForKey(action, btn, !isMovement, isOnlyMouse);
            }
        });
        
        contentTable.add(btn).fillX();
        contentTable.row();
    }

    private void startWaitingForKey(String action, TextButton btn, boolean allowMouse, boolean isOnlyMouse) {
        waitingForKey = true;
        btn.setText(isOnlyMouse ? "Presiona botón de ratón..." : "Presiona...");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (isOnlyMouse) {
                    return false;
                }
                if (waitingForKey) {
                    saveMapping(action, keycode, false);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (waitingForKey && allowMouse && (!isOnlyMouse || InputConfig.isValidInput(button, true))) {
                    saveMapping(action, button, true);
                    return true;
                }
                return false;
            }
        });
    }

    private void saveMapping(String action, int code, boolean isButton) {
        if (!InputConfig.isValidInput(code, isButton)) {
            waitingForKey = false;
            Gdx.input.setInputProcessor(getStage());
            showKeyboardSettings();
            return;
        }

        InputConfig config = SaveManager.getProfileData().inputConfig;
        
        // Intercambio automático (Swap)
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
        contentTable.clear();
        contentTable.add(new Label("Controles de Mando", skin)).colspan(2).padBottom(10).row();
        
        InputConfig config = SaveManager.getProfileData().inputConfig;
        
        for (Map.Entry<String, Integer> entry : config.gamepadMapping.entrySet()) {
            addRowToGamepadSettingsTable(entry.getKey(), entry.getValue(), config);
        }

        TextButton resetBtn = new TextButton("Restablecer a Default", skin);
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                config.resetToDefaults();
                SaveManager.saveProfileData();
                showControllerSettings();
            }
        });
        contentTable.add(resetBtn).colspan(2).padTop(20).fillX();
    }

    private void addRowToGamepadSettingsTable(final String action, int currentButton, final InputConfig config) {
        contentTable.add(new Label(action, skin)).padRight(10).left();
        
        TextButton btn = new TextButton("Botón " + currentButton, skin);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startWaitingForButton(action, btn, config);
            }
        });
        
        contentTable.add(btn).fillX();
        contentTable.row();
    }

    private void startWaitingForButton(final String action, final TextButton btn, final InputConfig config) {
        waitingForKey = true;
        btn.setText("Presiona botón mando...");

        Controllers.addListener(new ControllerAdapter() {
            @Override
            public boolean buttonDown(Controller controller, int buttonIndex) {
                if (waitingForKey) {
                    config.gamepadMapping.put(action, buttonIndex);
                    SaveManager.saveProfileData();
                    waitingForKey = false;
                    Controllers.removeListener(this);
                    
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            showControllerSettings();
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }

    private void showTouchpadSettings() {
        contentTable.clear();
        contentTable.add(new Label("Controles de Touchpad (Próximamente)", skin)).row();
    }
}
