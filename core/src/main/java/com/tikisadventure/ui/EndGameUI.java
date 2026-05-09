package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuMapScreen;
import com.badlogic.gdx.Game;

public class EndGameUI extends Table {

    private float stateTime = 0f;
    private boolean isAnimatingScore = false;
    private int targetScore = 0;
    private float currentDisplayedScore = 0f;
    private Label scoreLabel;
    private Label titleLabel;
    private TextButton btnRetry;
    private TextButton btnMenu;
    private Game game;
    private GameScreen gameScreen;
    private Skin skin;
    private boolean transitionStarted = false;

    public EndGameUI(Skin skin, int finalScore, Game game, GameScreen gameScreen) {
        this.skin = skin;
        this.targetScore = finalScore;
        this.game = game;
        this.gameScreen = gameScreen;
        setFillParent(true);

        titleLabel = new Label("FIN DE LA PARTIDA", skin);
        titleLabel.setFontScale(2.5f);
        titleLabel.setColor(Color.RED);
        titleLabel.getColor().a = 0f; // Empieza invisible

        scoreLabel = new Label("0", skin);
        scoreLabel.setFontScale(4f);
        scoreLabel.setColor(Color.GOLD);
        scoreLabel.getColor().a = 0f; // Empieza invisible

        btnRetry = new TextButton("Reintentar", skin);
        btnMenu = new TextButton("Menu Principal", skin);

        // Ocultamos los botones en sí
        btnRetry.getColor().a = 0f;
        btnMenu.getColor().a = 0f;

        btnRetry.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if(!transitionStarted) doFadeTransition(new GameScreen(game));
            }
        });
        btnMenu.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if(!transitionStarted) doFadeTransition(new MenuMapScreen(game));
            }
        });

        // Tabla intermedia solo para alinear, sin modificar su Alpha
        Table buttonsRow = new Table();
        buttonsRow.add(btnRetry).width(200).height(60).padRight(20);
        buttonsRow.add(btnMenu).width(200).height(60);

        add(titleLabel).padBottom(20).row();
        add(scoreLabel).padBottom(40).row();
        add(buttonsRow);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float realDelta = Gdx.graphics.getDeltaTime();
        stateTime += realDelta;

        if (stateTime > 2.0f && !isAnimatingScore) {
            isAnimatingScore = true;
            titleLabel.addAction(Actions.fadeIn(0.5f));
            scoreLabel.addAction(Actions.fadeIn(0.5f));
        }

        if (isAnimatingScore) {
            if (currentDisplayedScore < targetScore) {
                currentDisplayedScore += (Math.max(targetScore, 10) / 1.5f) * realDelta;

                if (currentDisplayedScore >= targetScore) {
                    currentDisplayedScore = targetScore;
                    // Los botones se animan a sí mismos
                    btnRetry.addAction(Actions.fadeIn(0.5f));
                    btnMenu.addAction(Actions.fadeIn(0.5f));
                }
                scoreLabel.setText(String.valueOf((int)currentDisplayedScore));
            } else if (targetScore == 0 && btnRetry.getColor().a == 0f) {
                // Caso extremo: Si la puntuación es 0, los botones deben salir inmediatamente
                btnRetry.addAction(Actions.fadeIn(0.5f));
                btnMenu.addAction(Actions.fadeIn(0.5f));
            }
        }
    }

    private void doFadeTransition(com.badlogic.gdx.Screen nextScreen) {
        transitionStarted = true;
        Image blackScreen = new Image(skin.newDrawable("rect", Color.BLACK));
        blackScreen.setFillParent(true);
        blackScreen.getColor().a = 0f;
        blackScreen.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
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
