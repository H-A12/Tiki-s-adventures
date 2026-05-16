package com.tikisadventure.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.ui.FontManager;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuMapScreen;

public class PauseUI extends Table {

    private final Skin skin;
    private final GameScreen gameScreen;
    private final Game game;
    private final Runnable onResumeCallback;

    private Window pauseWindow;
    private Window confirmWindow;
    private SettingsUI settingsUI;

    private Image blurBackground;
    private boolean transitionStarted = false;

    private Texture texMenuSalir;
    private Texture texBotonText;

    public PauseUI(Skin skin, Game game, GameScreen gameScreen, Runnable onResumeCallback) {
        this.skin = skin;
        this.game = game;
        this.gameScreen = gameScreen;
        this.onResumeCallback = onResumeCallback;

        texMenuSalir = new Texture(Gdx.files.internal("Menu/MenuSalir.png"));
        texBotonText = new Texture(Gdx.files.internal("Menu/BotonText.png"));

        setFillParent(true);
        setTouchable(Touchable.enabled);

        blurBackground = new Image(skin.newDrawable(skin.getDrawable("rect"), new Color(0f, 0f, 0f, 0.7f)));
        blurBackground.setFillParent(true);
        addActor(blurBackground);

        buildPauseWindow();
        buildConfirmWindow();
        buildSettingsWindow();

        addActor(pauseWindow);
        addActor(confirmWindow);
        addActor(settingsUI);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            confirmWindow.setVisible(false);
            settingsUI.setVisible(false);

            pauseWindow.setVisible(true);
        }
    }

    private void buildPauseWindow() {
        pauseWindow = new Window("", skin);
        pauseWindow.setBackground(new TextureRegionDrawable(new TextureRegion(texMenuSalir)));
        pauseWindow.setModal(true);
        pauseWindow.setMovable(false);
        pauseWindow.pad(30);

        Label title = new Label("PAUSA", skin, "font-38");
        pauseWindow.add(title).padTop(25).padBottom(40).row();

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = new TextureRegionDrawable(new TextureRegion(texBotonText));
        btnStyle.font = skin.get("font-14", Label.LabelStyle.class).font;
        btnStyle.pressedOffsetX = 0;
        btnStyle.pressedOffsetY = 0;

        TextButton btnResume = new TextButton("Reanudar", btnStyle);
        TextButton btnSettings = new TextButton("Ajustes", btnStyle);
        TextButton btnExit = new TextButton("Salir", btnStyle);

        btnResume.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (onResumeCallback != null) onResumeCallback.run();
            }
        });

        btnSettings.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                pauseWindow.setVisible(false);
                settingsUI.setVisible(true);
                settingsUI.toFront();
                event.getStage().setKeyboardFocus(settingsUI);
            }
        });

        btnExit.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                pauseWindow.setVisible(false);
                confirmWindow.setVisible(true);
            }
        });

        pauseWindow.add(btnResume).width(200).height(40).padBottom(15).row();
        pauseWindow.add(btnSettings).width(175).height(40).padBottom(15).row();
        pauseWindow.add(btnExit).width(125).height(40).row();

        pauseWindow.pack();
    }

    private void buildSettingsWindow() {
        // CAMBIO: Ahora pedimos la global
        Skin fullSkin = FontManager.getGlobalSkin();

        settingsUI = new SettingsUI(fullSkin, false, new Runnable() {
            @Override
            public void run() {
                settingsUI.setVisible(false);
                pauseWindow.setVisible(true);
            }
        });
        settingsUI.setVisible(false);
    }

    private void buildConfirmWindow() {
        confirmWindow = new Window("", skin);
        confirmWindow.setBackground(new TextureRegionDrawable(new TextureRegion(texMenuSalir)));
        confirmWindow.setModal(true);
        confirmWindow.setMovable(false);
        confirmWindow.pad(30);
        confirmWindow.setVisible(false);

        BitmapFont font18 = FontManager.getFont(18);
        Label.LabelStyle lblStyle = new Label.LabelStyle(font18, Color.WHITE);

        Label lblConfirm = new Label("¿Seguro que quieres salir?", lblStyle);
        Label lblWarning = new Label("Se perderá el progreso actual.", skin, "font-12");
        lblWarning.setColor(Color.RED);

        confirmWindow.add(lblConfirm).colspan(2).padTop(25).padBottom(10).row();
        confirmWindow.add(lblWarning).colspan(2).padBottom(30).row();

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = new TextureRegionDrawable(new TextureRegion(texBotonText));
        btnStyle.font = font18;
        btnStyle.pressedOffsetX = 0;
        btnStyle.pressedOffsetY = 0;

        TextButton btnYes = new TextButton("Sí, salir", btnStyle);
        TextButton btnNo = new TextButton("Cancelar", btnStyle);

        btnYes.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!transitionStarted) {
                    doFadeTransition(new MenuMapScreen(game));
                }
            }
        });

        btnNo.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                confirmWindow.setVisible(false);
                pauseWindow.setVisible(true);
            }
        });

        confirmWindow.add(btnYes).width(180).height(40).padRight(20);
        confirmWindow.add(btnNo).width(180).height(40);

        confirmWindow.pack();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (getStage() != null) {
            float targetScale = com.badlogic.gdx.math.MathUtils.clamp(getStage().getWidth() / 1280f, 0.7f, 1.3f);

            applyScaleToWindow(pauseWindow, targetScale);
            applyScaleToWindow(confirmWindow, targetScale);
            applyScaleToWindow(settingsUI, targetScale);

            if (pauseWindow.isVisible()) {
                pauseWindow.setPosition(
                    Math.round((getStage().getWidth() - pauseWindow.getWidth()) / 2f),
                    Math.round((getStage().getHeight() - pauseWindow.getHeight()) / 2f)
                );
            } else if (confirmWindow.isVisible()) {
                confirmWindow.setPosition(
                    Math.round((getStage().getWidth() - confirmWindow.getWidth()) / 2f),
                    Math.round((getStage().getHeight() - confirmWindow.getHeight()) / 2f)
                );
            } else if (settingsUI.isVisible()) {
                settingsUI.setPosition(
                    Math.round((getStage().getWidth() - settingsUI.getWidth()) / 2f),
                    Math.round((getStage().getHeight() - settingsUI.getHeight()) / 2f)
                );
            }
        }
    }

    private void applyScaleToWindow(Window win, float scale) {
        if (win != null && win.getScaleX() != scale) {
            win.setTransform(true);
            win.setScale(scale);
            win.setOrigin(win.getWidth() / 2f, win.getHeight() / 2f);
        }
    }

    public void dispose() {
        if (texMenuSalir != null) texMenuSalir.dispose();
        if (texBotonText != null) texBotonText.dispose();
    }

    public void sincronizarSelectorResolucion() {
        if (settingsUI != null) settingsUI.sincronizarSelectorResolucion();
    }

    private void doFadeTransition(com.badlogic.gdx.Screen nextScreen) {
        transitionStarted = true;
        Image         blackScreen = new Image(skin.newDrawable(skin.getDrawable("rect"), Color.BLACK));
        blackScreen.setFillParent(true);
        blackScreen.getColor().a = 0f;
        blackScreen.setTouchable(Touchable.disabled);
        getStage().addActor(blackScreen);

        blackScreen.addAction(Actions.sequence(
            Actions.fadeIn(1.0f),
            Actions.run(() -> {
                game.setScreen(nextScreen);
                Gdx.app.postRunnable(() -> gameScreen.dispose());
            })
        ));
    }
}
