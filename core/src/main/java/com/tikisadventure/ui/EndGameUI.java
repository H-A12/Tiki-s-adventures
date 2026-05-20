package com.tikisadventure.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.localization.LanguageManager;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuMapScreen;
import com.tikisadventure.ui.button.ButtonFactory;

public class EndGameUI extends Table {

    private float stateTime = 0f;
    private boolean isAnimatingScore = false;
    private boolean scoreFinished = false;
    private boolean isAnimatingCoins = false;
    private boolean coinsFinished = false;
    private int targetScore = 0;
    private int targetCoins = 0;
    private float currentDisplayedScore = 0f;
    private float currentDisplayedCoins = 0f;
    private Label scoreLabel;
    private Label coinsLabel;
    private Label titleLabel;
    private Image coinImage;
    private Table coinsRow;
    private Cell leftSpacer;
    private TextButton btnRetry;
    private TextButton btnMenu;
    private Game game;
    private GameScreen gameScreen;
    private Skin skin;
    private boolean transitionStarted = false;
    private boolean showCoins;
    private float btnAppearTimer = -1f;

    public EndGameUI(Skin skin, int finalScore, int coinsEarned, Game game, GameScreen gameScreen) {
        this.skin = skin;
        this.targetScore = finalScore;
        this.targetCoins = coinsEarned;
        this.game = game;
        this.gameScreen = gameScreen;
        setFillParent(true);

        this.showCoins = !GameSession.godMode;

        Label.LabelStyle titleStyle = new Label.LabelStyle(FontManager.getFont(38, 3f), Color.RED);
        Label.LabelStyle scoreStyle = new Label.LabelStyle(FontManager.getFont(60, 3f), Color.WHITE);
        Label.LabelStyle coinStyle = new Label.LabelStyle(FontManager.getFont(52, 3f), Color.YELLOW);

        titleLabel = new Label(LanguageManager.t("endgame.title"), titleStyle);
        titleLabel.getColor().a = 0f;

        scoreLabel = new Label("0", scoreStyle);
        scoreLabel.getColor().a = 0f;

        if (showCoins) {
            coinImage = new Image(Assets.getRegion("shared", "UI_assets/coin"));
            coinImage.getColor().a = 0f;

            coinsLabel = new Label("0", coinStyle);
            coinsLabel.getColor().a = 0f;

            coinsRow = new Table();
            leftSpacer = coinsRow.add();
            coinsRow.add(coinsLabel).padRight(10);
            coinsRow.add(coinImage).size(64, 64).padBottom(12);
        }

        btnRetry = ButtonFactory.createTextButton(LanguageManager.t("endgame.retry"), () -> {
            if(!transitionStarted) doFadeTransition(new GameScreen(game));
        });
        btnMenu = ButtonFactory.createTextButton(LanguageManager.t("endgame.main.menu"), () -> {
            if(!transitionStarted) doFadeTransition(new MenuMapScreen(game));
        });

        btnRetry.getColor().a = 0f;
        btnMenu.getColor().a = 0f;

        Table buttonsRow = new Table();
        buttonsRow.add(btnRetry).width(280).height(50).padRight(20);
        buttonsRow.add(btnMenu).width(280).height(50);

        add(titleLabel).padBottom(20).row();
        add(scoreLabel).padBottom(10).row();
        if (showCoins) {
            add(coinsRow).padBottom(40).row();
        }
        add(buttonsRow);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float realDelta = Gdx.graphics.getDeltaTime();
        stateTime += realDelta;

        // --- Fase 1: esperar 2 segundos, luego mostrar título y score ---
        if (stateTime > 2.0f && !isAnimatingScore && !scoreFinished) {
            isAnimatingScore = true;
            titleLabel.addAction(Actions.fadeIn(0.5f));
            scoreLabel.addAction(Actions.fadeIn(0.5f));
        }

        // --- Fase 2: animar el contador de score ---
        if (isAnimatingScore && !scoreFinished) {
            if (currentDisplayedScore < targetScore) {
                currentDisplayedScore += (Math.max(targetScore, 10) / 1.5f) * realDelta;
                updateScoreColor(currentDisplayedScore);
                if (currentDisplayedScore >= targetScore) {
                    currentDisplayedScore = targetScore;
                    scoreFinished = true;
                    isAnimatingScore = false;
                    if (showCoins) {
                        coinsLabel.addAction(Actions.fadeIn(0.4f));
                        isAnimatingCoins = true;
                    } else {
                        btnAppearTimer = 0f;
                    }
                }
                scoreLabel.setText(String.valueOf((int)currentDisplayedScore));
            } else if (targetScore == 0 && currentDisplayedScore == 0) {
                scoreFinished = true;
                isAnimatingScore = false;
                if (showCoins) {
                    coinsLabel.addAction(Actions.fadeIn(0.4f));
                    isAnimatingCoins = true;
                } else {
                    btnAppearTimer = 0f;
                }
                scoreLabel.setText("0");
                updateScoreColor(0);
            }
        }

        // Control manual de alpha de botones (fuera del sistema de Actions para evitar clearActions)
        if (btnAppearTimer >= 0f) {
            btnAppearTimer += realDelta;
            float alpha = Math.min(1f, btnAppearTimer / 0.5f);
            btnRetry.getColor().a = alpha;
            btnMenu.getColor().a = alpha;
        }

        // Rainbow eterno si la puntuación final es >= 25000
        if (scoreFinished && targetScore >= 25000) {
            updateScoreColor(targetScore);
        }

        // --- Fase 3: animar el contador de monedas ---
        if (showCoins && isAnimatingCoins && !coinsFinished) {
            if (currentDisplayedCoins < targetCoins) {
                currentDisplayedCoins += (Math.max(targetCoins, 10) / 1.5f) * realDelta;
                if (currentDisplayedCoins >= targetCoins) {
                    currentDisplayedCoins = targetCoins;
                    coinsFinished = true;
                    isAnimatingCoins = false;
                    coinImage.addAction(Actions.fadeIn(0.3f));
                    coinImage.addAction(Actions.forever(
                        Actions.sequence(
                    Actions.moveBy(0, 4, 0.6f),
                            Actions.moveBy(0, -4, 0.6f)
                        )
                    ));
                    btnAppearTimer = 0f;
                }
                coinsLabel.setText(String.valueOf((int)currentDisplayedCoins));
                leftSpacer.width(String.valueOf((int)currentDisplayedCoins).length() * 10f);
                coinsRow.invalidate();
            } else if (targetCoins == 0) {
                coinsFinished = true;
                isAnimatingCoins = false;
                coinImage.addAction(Actions.fadeIn(0.3f));
                coinImage.addAction(Actions.forever(
                    Actions.sequence(
                        Actions.moveBy(0, 4, 0.6f),
                        Actions.moveBy(0, -4, 0.6f)
                    )
                ));
                btnAppearTimer = 0f;
                coinsLabel.setText("0");
                leftSpacer.width(1f * 10f);
                coinsRow.invalidate();
            }
        }
    }

    private void updateScoreColor(float score) {
        if (score <= 0) {
            scoreLabel.setColor(Color.WHITE);
            return;
        }

        float hue, saturation;

        if (score < 10000) {
            float t = score / 10000f;
            hue = t * 270f;
            saturation = t;
        } else if (score < 25000) {
            hue = 270f;
            saturation = 1f;
        } else {
            hue = (stateTime * 60f) % 360f;
            saturation = 1f;
        }

        scoreLabel.setColor(hsvToRgb(hue, saturation, 1f));
    }

    private Color hsvToRgb(float h, float s, float v) {
        float r, g, b;
        int i = (int)(h / 60f) % 6;
        float f = (h / 60f) - (int)(h / 60f);
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        return new Color(r, g, b, 1f);
    }

    public void dispose() {
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
                AudioManager.stopAllMusic();
                AudioManager.playMenuMusic();
                game.setScreen(nextScreen);
                Gdx.app.postRunnable(() -> gameScreen.dispose());
            })
        ));
    }
}
