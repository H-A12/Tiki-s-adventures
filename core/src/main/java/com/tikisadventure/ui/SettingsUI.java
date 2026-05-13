package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.input.InputConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SettingsUI extends Window {
    private static final Set<String> MOUSE_ONLY_ACTIONS = new HashSet<String>() {{
        add("manualAim");
        add("ability2");
    }};

    private static final Map<String, String> ACTION_NAMES = new HashMap<>();
    static {
        ACTION_NAMES.put("up", "Arriba");
        ACTION_NAMES.put("down", "Abajo");
        ACTION_NAMES.put("left", "Izquierda");
        ACTION_NAMES.put("right", "Derecha");
        ACTION_NAMES.put("interact", "Interactuar");
        ACTION_NAMES.put("ability1", "Habilidad 1");
        ACTION_NAMES.put("ability2", "Habilidad 2");
        ACTION_NAMES.put("manualAim", "Apuntado manual");
        ACTION_NAMES.put("toggleAutoFire", "Auto-disparo");
    }

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
    private SelectBox.SelectBoxStyle smallSelectStyle;

    // Pestañas
    private TextButton keyboardTab;
    private TextButton controllerTab;
    private TextButton touchpadTab;

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
        btnStyle.font = skin.get("font-14", Label.LabelStyle.class).font;

        // --- ESTILO DE DESPLEGABLE ---
        SelectBox.SelectBoxStyle baseStyle = skin.get(SelectBox.SelectBoxStyle.class);
        smallSelectStyle = new SelectBox.SelectBoxStyle(baseStyle);
        smallSelectStyle.font = skin.get("font-13", Label.LabelStyle.class).font;
        smallSelectStyle.listStyle = new List.ListStyle(baseStyle.listStyle);
        smallSelectStyle.listStyle.font = skin.get("font-13", Label.LabelStyle.class).font;

        if (baseStyle.listStyle.selection != null) {
            Drawable selectionCopy = skin.newDrawable(baseStyle.listStyle.selection);
            selectionCopy.setTopHeight(8f);
            selectionCopy.setBottomHeight(8f);
            selectionCopy.setLeftWidth(5f);
            smallSelectStyle.listStyle.selection = selectionCopy;
        }

        setModal(true);
        setMovable(true);
        pad(38, 35, 35, 35);

        setSize(520, 500);



        tabTable = new Table();
        keyboardTab = new TextButton("Teclado", btnStyle);
        controllerTab = new TextButton("Mando", btnStyle);
        touchpadTab = new TextButton("Touchpad", btnStyle);

        tabTable.add(keyboardTab).padRight(10).width(110);
        tabTable.add(controllerTab).padRight(10).width(110);
        tabTable.add(touchpadTab).width(110);
        add(tabTable).colspan(3).center().padBottom(6).row();
        tabTable.setVisible(false);

        contentTable = new Table();
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();

        Pixmap pmScrollBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollBg.setColor(0f, 0f, 0f, 0f);
        pmScrollBg.fill();
        TextureRegionDrawable scrollBg = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollBg)));
        scrollBg.setMinWidth(6);
        pmScrollBg.dispose();

        Pixmap pmScrollKnob = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollKnob.setColor(1f, 1f, 1f, 0.9f);
        pmScrollKnob.fill();
        TextureRegionDrawable scrollKnob = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollKnob)));
        scrollKnob.setMinWidth(10);
        pmScrollKnob.dispose();

        scrollStyle.vScroll = scrollBg;
        scrollStyle.vScrollKnob = scrollKnob;

        ScrollPane scrollPane = new ScrollPane(contentTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFlickScroll(false);

        add(scrollPane).colspan(3).expand().fill().padLeft(6).padRight(6).padBottom(8).row();

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
    }

    // =========================================================================
    // BLOQUEAMOS LA POSICIÓN PARA QUE MENUSCREEN NO LO HUNDA A LA IZQUIERDA
    // =========================================================================
    @Override
    public void setOrigin(int alignment) {
        super.setOrigin(Align.center);
    }

    @Override
    public void setPosition(float x, float y) {
        if (getStage() != null) {
            float w = getStage().getViewport().getWorldWidth();
            float h = getStage().getViewport().getWorldHeight();
            super.setPosition(Math.round((w - getWidth()) / 2f), Math.round((h - getHeight()) / 2f));
        } else {
            super.setPosition(x, y);
        }
    }

    @Override
    public void setPosition(float x, float y, int alignment) {
        if (getStage() != null) {
            float w = getStage().getViewport().getWorldWidth();
            float h = getStage().getViewport().getWorldHeight();
            super.setPosition(w / 2f, h / 2f, Align.center);
        } else {
            super.setPosition(x, y, alignment);
        }
    }
    // =========================================================================

    private void actualizarColorPestanas(TextButton botonActivo) {
        keyboardTab.setColor(Color.WHITE);
        controllerTab.setColor(Color.WHITE);
        touchpadTab.setColor(Color.WHITE);
        if (botonActivo != null) {
            botonActivo.setColor(Color.GRAY);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getStage() != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !waitingForKey) {
                if (onCloseCallback != null) onCloseCallback.run();
                showMainSettings();
            }
        }
    }

    private void showMainSettings() {
        contentTable.clear();
        tabTable.setVisible(false);
        actualizarColorPestanas(null);

        contentTable.add(new Label("Ajustes", skin, "font-18")).colspan(3).padBottom(20).row();

        contentTable.add(new Label("Volumen:", skin, "font-14")).left().padLeft(20).padRight(15).padBottom(18);
        final Slider volumeSlider = new Slider(0, 1, 0.1f, false, skin);
        volumeSlider.setValue(0.5f);
        contentTable.add(volumeSlider).width(200).left().padBottom(18);
        contentTable.add().expandX().row();

        contentTable.add(new Label("Resolución:", skin, "font-14")).left().padLeft(20).padRight(15).padBottom(18);
        resSelector = crearSelectBoxEscalado(smallSelectStyle);
        resSelector.setItems("Ventana", "Pantalla completa");

        if (Gdx.graphics.isFullscreen() || Gdx.graphics.getWidth() >= 1920) {
            resSelector.setSelectedIndex(1);
        } else {
            resSelector.setSelectedIndex(0);
        }

        resSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resSelector.hideList();
                String selected = resSelector.getSelected();

                if (selected.equals("Pantalla completa")) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    SaveManager.saveFullscreen(true);
                } else {
                    Gdx.graphics.setWindowedMode(1280, 720);
                    SaveManager.saveFullscreen(false);
                    SaveManager.saveResolution(1280, 720);
                }

                if (getStage() != null && getStage().getViewport() != null) {
                    getStage().getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                }
            }
        });

        contentTable.add(resSelector).width(220).left().padBottom(18);
        contentTable.add().expandX().row();

        if (showLanguage) {
            contentTable.add(new Label("Idioma:", skin, "font-14")).left().padLeft(20).padRight(15).padBottom(18);
            SelectBox<String> langSelector = crearSelectBoxEscalado(smallSelectStyle);
            langSelector.setItems("Español", "Inglés");
            langSelector.setSelectedIndex(0);

            contentTable.add(langSelector).width(220).left().padBottom(18);
            contentTable.add().expandX().row();
        }

        final CheckBox fpsCheck = new CheckBox(" Mostrar FPS en partida", skin);
        fpsCheck.setChecked(SaveManager.getProfileData().showFps);
        fpsCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SaveManager.getProfileData().showFps = fpsCheck.isChecked();
                SaveManager.saveProfileData();
            }
        });
        contentTable.add(fpsCheck).colspan(3).padLeft(20).padBottom(18).row();

        TextButton btnControles = new TextButton("Controles", btnStyle);
        btnControles.addListener(new Assets.HoverCursorListener());
        btnControles.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                showControlsSettings();
            }
        });
        contentTable.add(btnControles).colspan(3).center().width(180).padTop(10).row();

        navButton.setText("Volver");
        if (navListener != null) navButton.removeListener(navListener);
        navListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMainSettings();
                if (onCloseCallback != null) onCloseCallback.run();
            }
        };
        navButton.addListener(navListener);
    }

    private void showControlsSettings() {
        tabTable.setVisible(true);
        showKeyboardSettings();

        navButton.setText("Volver");
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
        actualizarColorPestanas(keyboardTab);
        contentTable.clear();
        contentTable.add(new Label("Controles Generales", skin, "font-14")).colspan(3).padLeft(20).padBottom(8).row();

        InputConfig config = SaveManager.getProfileData().inputConfig;

        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, false);
        }

        contentTable.add(new Label("__________________________", skin)).colspan(3).padLeft(20).pad(6).row();
        contentTable.add(new Label(" ", skin)).colspan(3).padLeft(20).pad(4).row();
        contentTable.add(new Label("Acciones de Ratón", skin, "font-14")).colspan(3).padLeft(20).padBottom(6).row();

        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (!MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, true);
        }

        contentTable.add(new Label("Tamaño Cursor:", skin, "font-14")).padLeft(20).padRight(10).left();
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
        contentTable.add(mouseSizeSlider).width(150).left().padTop(10);
        contentTable.add().expandX().row();

        TextButton resetBtn = new TextButton("Restablecer", btnStyle);
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
        contentTable.add(resetBtn).colspan(3).center().width(240).padTop(12).padBottom(6).row();
    }

    private void addCellToSettingsTable(final String action, int currentCode, final InputConfig config, final boolean isOnlyMouse) {
        String displayName = ACTION_NAMES.getOrDefault(action, action);
        contentTable.add(new Label(displayName, skin, "font-14")).padLeft(20).padRight(10).left();
        boolean isMovement = action.equals("up") || action.equals("down") || action.equals("left") || action.equals("right");
        TextButton btn = new TextButton(getInputName(currentCode, isOnlyMouse || (!isMovement && currentCode >= 0 && currentCode <= 4)), btnStyle);
        btn.addListener(new Assets.HoverCursorListener());
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startWaitingForKey(action, btn, !isMovement, isOnlyMouse);
            }
        });
        contentTable.add(btn).width(150).left().padBottom(6);
        contentTable.add().expandX().row();
    }

    private void startWaitingForKey(String action, TextButton btn, boolean allowMouse, boolean isOnlyMouse) {
        waitingForKey = true;
        btn.setText(isOnlyMouse ? "Esperando..." : "Presiona...");
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    waitingForKey = false;
                    Gdx.input.setInputProcessor(getStage());
                    showKeyboardSettings();
                    return true;
                }
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
        actualizarColorPestanas(controllerTab);
        contentTable.clear();
        contentTable.add(new Label("(Próximamente)", skin, "font-14")).row();
    }

    private void showTouchpadSettings() {
        actualizarColorPestanas(touchpadTab);
        contentTable.clear();
        contentTable.add(new Label("(Próximamente)", skin, "font-14")).row();
    }

    public void sincronizarSelectorResolucion() {
        if (resSelector == null) return;
        if (Gdx.graphics.isFullscreen() || Gdx.graphics.getWidth() >= 1920) {
            resSelector.setSelectedIndex(1);
        } else {
            resSelector.setSelectedIndex(0);
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

    // =========================================================================
    // HELPER: SelectBox que escala su lista flotante al tamaño de la ventana
    // =========================================================================
    private <T> SelectBox<T> crearSelectBoxEscalado(SelectBox.SelectBoxStyle style) {
        return new SelectBox<T>(style) {
            private final com.badlogic.gdx.math.Vector2 tempCoords = new com.badlogic.gdx.math.Vector2();

            @Override
            public void act(float delta) {
                super.act(delta);
                ScrollPane popup = getScrollPane();

                // Solo actuamos si la lista desplegable está abierta y visible en el Stage
                if (popup != null && popup.getParent() != null) {
                    popup.setTransform(true); // Permitimos que la lista sufra transformaciones (escalado)

                    // Calculamos la posición del botón para saber si la lista se abrió hacia abajo o hacia arriba
                    tempCoords.set(0, 0);
                    localToStageCoordinates(tempCoords);

                    if (popup.getY() >= tempCoords.y) {
                        popup.setOrigin(0, 0); // Se abrió hacia arriba -> anclamos la escala abajo
                    } else {
                        popup.setOrigin(0, popup.getHeight()); // Se abrió hacia abajo -> anclamos la escala arriba
                    }

                    // Forzamos a la lista a tener exactamente la misma escala que tiene la ventana SettingsUI
                    popup.setScale(SettingsUI.this.getScaleX(), SettingsUI.this.getScaleY());
                }
            }
        };
    }
}
