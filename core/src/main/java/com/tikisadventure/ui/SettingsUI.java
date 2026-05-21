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
import com.tikisadventure.localization.LanguageManager;
import com.tikisadventure.ui.button.ButtonFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public class SettingsUI extends Window {
    private static final Set<String> MOUSE_ONLY_ACTIONS = new HashSet<String>() {{
        add("manualAim");
        add("ability2");
    }};

    private String getActionDisplayName(String action) {
        switch (action) {
            case "toggleAutoFire": return LanguageManager.t("controls.action.autoFire");
            case "toggleStats": return LanguageManager.t("controls.action.stats");
            default: return LanguageManager.t("controls.action." + action);
        }
    }

    private final Skin skin;
    private Table contentTable;
    private Table tabTable;
    private TextButton navButton;
    private boolean waitingForKey = false;
    private boolean showLanguage;
    private Runnable onCloseCallback;
    private TextButton.TextButtonStyle btnStyle;
    private SelectBox<String> resSelector;
    private SelectBox.SelectBoxStyle smallSelectStyle;
    private ScrollPane scrollPane;
    private Label titleLabel;

    // PestaÃƒÂ±as
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

        btnStyle = ButtonFactory.getTextBtnStyle();

        // --- ESTILO DE DESPLEGABLE ---
        SelectBox.SelectBoxStyle baseStyle = skin.get(SelectBox.SelectBoxStyle.class);
        Label.LabelStyle font13Style = skin.get("font-13", Label.LabelStyle.class);
        if (font13Style == null) font13Style = skin.get(Label.LabelStyle.class);
        smallSelectStyle = new SelectBox.SelectBoxStyle(baseStyle);
        smallSelectStyle.font = font13Style != null ? font13Style.font : null;
        smallSelectStyle.listStyle = new List.ListStyle(baseStyle.listStyle);
        smallSelectStyle.listStyle.font = font13Style != null ? font13Style.font : null;

        if (baseStyle.listStyle.selection != null) {
            Drawable selectionCopy = skin.newDrawable(baseStyle.listStyle.selection);
            selectionCopy.setTopHeight(8f);
            selectionCopy.setBottomHeight(8f);
            selectionCopy.setLeftWidth(5f);
            smallSelectStyle.listStyle.selection = selectionCopy;
        }

        setModal(true);
        setMovable(false);
        pad(78, 75, 75, 75);



        tabTable = new Table();
        keyboardTab = new TextButton(LanguageManager.t("settings.keyboard"), btnStyle);
        controllerTab = new TextButton(LanguageManager.t("settings.controller"), btnStyle);
        touchpadTab = new TextButton(LanguageManager.t("settings.touchpad"), btnStyle);
        ButtonFactory.configure(keyboardTab, () -> showKeyboardSettings());
        ButtonFactory.configure(controllerTab, () -> showControllerSettings());
        ButtonFactory.configure(touchpadTab, () -> showTouchpadSettings());

        tabTable.add(keyboardTab).padRight(10).width(110);
        tabTable.add(controllerTab).padRight(10).width(110);
        tabTable.add(touchpadTab).width(110);
        add(tabTable).colspan(3).center().padTop(4).padBottom(6).row();
        tabTable.setVisible(false);

        titleLabel = new Label("", skin, "font-18");
        titleLabel.setVisible(true);
        add(titleLabel).colspan(3).center().padTop(4).padBottom(10).row();

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

        scrollPane = new ScrollPane(contentTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFlickScroll(false);

        add(scrollPane).colspan(3).expand().fill().padLeft(6).padRight(6).padBottom(16).row();

        navButton = new TextButton("", btnStyle);
        add(navButton).colspan(3).center().padBottom(5).width(180);

        showMainSettings();
    }

    // =========================================================================
    // BLOQUEAMOS LA POSICIÃƒâ€œN PARA QUE MENUSCREEN NO LO HUNDA A LA IZQUIERDA
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

    private boolean focusSet = false;

    @Override
    public void act(float delta) {
        super.act(delta);
        Stage s = getStage();
        if (s != null) {
            if (!focusSet) {
                s.setScrollFocus(scrollPane);
                focusSet = true;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !waitingForKey) {
                if (onCloseCallback != null) onCloseCallback.run();
                showMainSettings();
            }
        }
    }

    private void showMainSettings() {
        contentTable.clear();
        tabTable.setVisible(false);
        titleLabel.setVisible(true);
        titleLabel.setText(LanguageManager.t("settings.title"));
        actualizarColorPestanas(null);
        setSize(580, 580);

        contentTable.add(new Label(LanguageManager.t("settings.volume.music"), skin, "font-14")).left().padLeft(20).padRight(15).padBottom(22);
        final Slider musicSlider = new Slider(0, 1, 0.1f, false, skin);
        musicSlider.setValue(com.tikisadventure.core.SaveManager.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = musicSlider.getValue();
                com.tikisadventure.audio.AudioManager.setMusicVolume(vol);
                com.tikisadventure.core.SaveManager.saveVolume(vol, com.tikisadventure.core.SaveManager.getSFXVolume());
            }
        });
        contentTable.add(musicSlider).width(200).left().padBottom(22);
        contentTable.add().expandX().row();

        contentTable.add(new Label(LanguageManager.t("settings.volume.sfx"), skin, "font-14")).left().padLeft(20).padRight(15).padBottom(22);
        final Slider sfxSlider = new Slider(0, 1, 0.1f, false, skin);
        sfxSlider.setValue(com.tikisadventure.core.SaveManager.getSFXVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = sfxSlider.getValue();
                com.tikisadventure.audio.AudioManager.setSFXVolume(vol);
                com.tikisadventure.core.SaveManager.saveVolume(com.tikisadventure.core.SaveManager.getMusicVolume(), vol);
            }
        });
        contentTable.add(sfxSlider).width(200).left().padBottom(22);
        contentTable.add().expandX().row();

        contentTable.add(new Label(LanguageManager.t("settings.resolution"), skin, "font-14")).left().padLeft(20).padRight(15).padBottom(22);
        resSelector = crearSelectBoxEscalado(smallSelectStyle);
        resSelector.setItems(LanguageManager.t("settings.window"), LanguageManager.t("settings.fullscreen"));

        if (Gdx.graphics.isFullscreen() || Gdx.graphics.getWidth() >= 1920) {
            resSelector.setSelectedIndex(1);
        } else {
            resSelector.setSelectedIndex(0);
        }

        resSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resSelector.hideList();
                int idx = resSelector.getSelectedIndex();

                if (idx == 1) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    SaveManager.saveFullscreen(true);
                } else {
                    Gdx.graphics.setWindowedMode(1280, 720);
                    SaveManager.saveFullscreen(false);
                    SaveManager.saveResolution(1280, 720);
                }
                Stage stage = getStage();
                if (stage != null && stage.getViewport() != null) {
                    stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                }
            }
        });

        contentTable.add(resSelector).width(220).left().padBottom(22);
        contentTable.add().expandX().row();

        if (showLanguage) {
            contentTable.add(new Label(LanguageManager.t("settings.language"), skin, "font-14")).left().padLeft(20).padRight(15).padBottom(22);
            SelectBox<String> langSelector = crearSelectBoxEscalado(smallSelectStyle);
            langSelector.setItems(LanguageManager.t("settings.spanish"), LanguageManager.t("settings.english"));
            langSelector.setSelectedIndex(LanguageManager.getInstance().isEnglish() ? 1 : 0);

            langSelector.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    langSelector.hideList();
                    int idx = langSelector.getSelectedIndex();
                    LanguageManager.getInstance().setLanguage(idx == 1 ? "en" : "es");
                    showMainSettings();
                }
            });

            contentTable.add(langSelector).width(220).left().padBottom(22);
            contentTable.add().expandX().row();
        }

        final CheckBox fpsCheck = new CheckBox(LanguageManager.t("settings.fps"), skin);
        fpsCheck.setChecked(SaveManager.getProfileData().showFps);
        Texture tickTextureFps = new Texture(Gdx.files.internal("sprites/shared/UI_assets/UI_V.png"));
        TextureRegionDrawable tickOnFps = new TextureRegionDrawable(tickTextureFps);
        tickOnFps.setMinWidth(32);
        tickOnFps.setMinHeight(32);
        CheckBox.CheckBoxStyle fpsStyle = new CheckBox.CheckBoxStyle(skin.get(CheckBox.CheckBoxStyle.class));
        fpsStyle.checkboxOn = tickOnFps;
        fpsCheck.setStyle(fpsStyle);
        fpsCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SaveManager.getProfileData().showFps = fpsCheck.isChecked();
                SaveManager.saveProfileData();
            }
        });
        contentTable.add(fpsCheck).colspan(3).padLeft(20).padBottom(4).row();

        TextButton btnControles = ButtonFactory.createTextButton(LanguageManager.t("settings.controls"), () -> showControlsSettings());
        contentTable.add(btnControles).colspan(3).center().width(180).padTop(16).row();

        navButton.setText(LanguageManager.t("settings.back"));
        navButton.clearListeners();
        ButtonFactory.configure(navButton, () -> {
            showMainSettings();
            if (onCloseCallback != null) onCloseCallback.run();
        });
    }

    private void showControlsSettings() {
        setSize(580, 580);
        titleLabel.setVisible(false);
        tabTable.setVisible(true);
        showKeyboardSettings();

        navButton.setText(LanguageManager.t("settings.back"));
        navButton.clearListeners();
        ButtonFactory.configure(navButton, () -> showMainSettings());
    }

    private void showKeyboardSettings() {
        actualizarColorPestanas(keyboardTab);
        contentTable.clear();
        contentTable.add(new Label(LanguageManager.t("settings.controls.general"), skin, "font-14")).colspan(3).padLeft(20).padTop(8).padBottom(8).row();

        InputConfig config = SaveManager.getProfileData().inputConfig;

        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, false);
        }

        contentTable.add(new Label("__________________________", skin)).colspan(3).padLeft(20).pad(6).row();
        contentTable.add(new Label(" ", skin)).colspan(3).padLeft(20).pad(4).row();
        contentTable.add(new Label(LanguageManager.t("settings.controls.mouse"), skin, "font-14")).colspan(3).padLeft(20).padBottom(6).row();

        for (Map.Entry<String, Integer> entry : config.keyboardMapping.entrySet()) {
            if (!MOUSE_ONLY_ACTIONS.contains(entry.getKey())) continue;
            addCellToSettingsTable(entry.getKey(), entry.getValue(), config, true);
        }

        contentTable.add(new Label(LanguageManager.t("settings.controls.cursor"), skin, "font-14")).padLeft(20).padRight(10).left();
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

        TextButton resetBtn = ButtonFactory.createTextButton(LanguageManager.t("settings.controls.reset"), () -> {
            config.resetToDefaults();
            mouseSizeSlider.setValue(1.0f);
            SaveManager.getProfileData().inputConfig.mouseSize = 1.0f;
            Assets.updateCursorScale(1.0f);
            SaveManager.saveProfileData();
            showKeyboardSettings();
        });
        contentTable.add(resetBtn).colspan(3).center().width(240).padTop(12).padBottom(6).padLeft(10).row();
    }

    private void addCellToSettingsTable(final String action, int currentCode, final InputConfig config, final boolean isOnlyMouse) {
        String displayName = getActionDisplayName(action);
        contentTable.add(new Label(displayName, skin, "font-13")).padLeft(20).padRight(10).left();
        boolean isMovement = action.equals("up") || action.equals("down") || action.equals("left") || action.equals("right");
        TextButton btn = new TextButton(getInputName(currentCode, isOnlyMouse || (!isMovement && currentCode >= 0 && currentCode <= 4)), btnStyle);
        ButtonFactory.configure(btn, () -> startWaitingForKey(action, btn, !isMovement, isOnlyMouse));
        contentTable.add(btn).width(150).left().padBottom(6);
        contentTable.add().expandX().row();
    }

    private void startWaitingForKey(String action, TextButton btn, boolean allowMouse, boolean isOnlyMouse) {
        waitingForKey = true;
        btn.setText(isOnlyMouse ? LanguageManager.t("settings.waiting") : LanguageManager.t("settings.press"));
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
                if (waitingForKey) {
                    if (!InputConfig.isValidInput(keycode, false)) {
                        waitingForKey = false;
                        Gdx.input.setInputProcessor(getStage());
                        showKeyboardSettings();
                        TextureRegionDrawable warningBg = new TextureRegionDrawable(
                            new TextureRegion(new Texture(Gdx.files.internal("Menu/MenuSalir.png"))));
                        Dialog warningDialog = new Dialog("", skin);
                        warningDialog.setBackground(warningBg);
                        warningDialog.setModal(true);
                        warningDialog.setMovable(false);
                        Label msgLabel = new Label(
                            LanguageManager.t("settings.controls.invalidKey"), skin, "font-14");
                        msgLabel.setWrap(true);
                        warningDialog.getContentTable().add(msgLabel).width(240);
                        warningDialog.getContentTable().pad(10, 0, 15, 0);
                        TextButton okBtn = new TextButton("OK", btnStyle);
                        ButtonFactory.configure(okBtn, () -> warningDialog.hide());
                        warningDialog.getButtonTable().add(okBtn).size(80, 30);
                        warningDialog.pad(25, 20, 15, 20);
                        warningDialog.pack();
                        warningDialog.show(getStage());
                        warningDialog.setPosition(
                            Math.round((getStage().getWidth() - warningDialog.getWidth()) / 2f),
                            Math.round((getStage().getHeight() - warningDialog.getHeight()) / 2f));
                        return true;
                    }
                    saveMapping(action, keycode, false);
                    return true;
                }
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
        contentTable.add(new Label(LanguageManager.t("settings.coming.soon"), skin, "font-14")).row();
    }

    private void showTouchpadSettings() {
        actualizarColorPestanas(touchpadTab);
        contentTable.clear();
        contentTable.add(new Label(LanguageManager.t("settings.coming.soon"), skin, "font-14")).row();
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
    // HELPER: SelectBox que escala su lista flotante al tamaÃƒÂ±o de la ventana
    // =========================================================================
    private <T> SelectBox<T> crearSelectBoxEscalado(SelectBox.SelectBoxStyle style) {
        return new SelectBox<T>(style) {
            private final com.badlogic.gdx.math.Vector2 tempCoords = new com.badlogic.gdx.math.Vector2();

            @Override
            public void act(float delta) {
                super.act(delta);
                ScrollPane popup = getScrollPane();

                // Solo actuamos si la lista desplegable estÃƒÂ¡ abierta y visible en el Stage
                if (popup != null && popup.getParent() != null) {
                    popup.setTransform(true); // Permitimos que la lista sufra transformaciones (escalado)

                    // Calculamos la posiciÃƒÂ³n del botÃƒÂ³n para saber si la lista se abriÃƒÂ³ hacia abajo o hacia arriba
                    tempCoords.set(0, 0);
                    localToStageCoordinates(tempCoords);

                    if (popup.getY() >= tempCoords.y) {
                        popup.setOrigin(0, 0); // Se abriÃƒÂ³ hacia arriba -> anclamos la escala abajo
                    } else {
                        popup.setOrigin(0, popup.getHeight()); // Se abriÃƒÂ³ hacia abajo -> anclamos la escala arriba
                    }

                    // Forzamos a la lista a tener exactamente la misma escala que tiene la ventana SettingsUI
                    popup.setScale(SettingsUI.this.getScaleX(), SettingsUI.this.getScaleY());
                }
            }
        };
    }
}


