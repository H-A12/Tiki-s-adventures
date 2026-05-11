package com.tikisadventure.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuScreen;

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

    public PauseUI(Skin skin, Game game, GameScreen gameScreen, Runnable onResumeCallback) {
        this.skin = skin;
        this.game = game;
        this.gameScreen = gameScreen;
        this.onResumeCallback = onResumeCallback;

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

    // --- NUEVO: Sobreescribimos setVisible para reiniciar el estado ---
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
        pauseWindow.setModal(true);
        pauseWindow.setMovable(false);
        pauseWindow.pad(30);

        Label title = new Label("PAUSA", skin);
        title.setFontScale(2.5f);
        pauseWindow.add(title).padBottom(40).row();

        TextButton btnResume = new TextButton("Reanudar", skin);
        TextButton btnSettings = new TextButton("Ajustes", skin);
        TextButton btnExit = new TextButton("Salir", skin);

        btnResume.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (onResumeCallback != null) onResumeCallback.run();
            }
        });

        btnSettings.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                pauseWindow.setVisible(false);
                settingsUI.setVisible(true);
            }
        });

        btnExit.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                pauseWindow.setVisible(false);
                confirmWindow.setVisible(true);
            }
        });

        pauseWindow.add(btnResume).width(200).height(60).padBottom(20).row();
        pauseWindow.add(btnSettings).width(200).height(60).padBottom(20).row();
        pauseWindow.add(btnExit).width(200).height(60).row();

        pauseWindow.pack();
    }

    private void buildSettingsWindow() {
        Skin fullSkin = new Skin(Gdx.files.internal("uiskin.json"));

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
        confirmWindow.setModal(true);
        confirmWindow.setMovable(false);
        confirmWindow.pad(30);
        confirmWindow.setVisible(false);

        Label lblConfirm = new Label("¿Seguro que quieres salir?", skin);
        Label lblWarning = new Label("Se perderá el progreso actual.", skin);
        lblWarning.setColor(Color.RED);
        lblWarning.setFontScale(0.8f);

        confirmWindow.add(lblConfirm).colspan(2).padBottom(10).row();
        confirmWindow.add(lblWarning).colspan(2).padBottom(30).row();

        TextButton btnYes = new TextButton("Sí, salir", skin);
        TextButton btnNo = new TextButton("Cancelar", skin);

        btnYes.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!transitionStarted) {
                    doFadeTransition(new MenuScreen(game));
                }
            }
        });

        btnNo.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                confirmWindow.setVisible(false);
                pauseWindow.setVisible(true);
            }
        });

        confirmWindow.add(btnYes).width(150).height(50).padRight(20);
        confirmWindow.add(btnNo).width(150).height(50);

        confirmWindow.pack();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (getStage() != null) {
            // 1. Calculamos la escala basada en 1280x720
            float targetScale = com.badlogic.gdx.math.MathUtils.clamp(getStage().getWidth() / 1280f, 0.7f, 1.3f);

            // 2. Aplicamos la escala a las ventanas de la pausa
            applyScaleToWindow(pauseWindow, targetScale);
            applyScaleToWindow(confirmWindow, targetScale);
            applyScaleToWindow(settingsUI, targetScale);

            // 3. Centramos dinámicamente la ventana que esté visible
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

    // --- NUEVO: Método auxiliar para escalar ventanas desde su centro ---
    private void applyScaleToWindow(Window win, float scale) {
        if (win != null && win.getScaleX() != scale) {
            win.setTransform(true);
            win.setScale(scale);
            win.setOrigin(win.getWidth() / 2f, win.getHeight() / 2f);
        }
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
