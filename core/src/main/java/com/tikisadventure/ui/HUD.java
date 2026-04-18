package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikisadventure.systems.ExperienceSystem;

public class HUD {

    private Stage stage;

    private Label fpsLabel;
    private Label hpLabel;
    private Label levelLabel;
    private Label scoreLabel;

    private Label ability1NameLabel;
    private Label ability2NameLabel;

    private ProgressBar xpBar;
    private ProgressBar ability1Bar;
    private ProgressBar ability2Bar;

    private Window levelUpWindow;
    private TextButton okButton;

    public HUD(Batch batch){

        stage = new Stage(new ScreenViewport(), batch);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Layout principal que llena la pantalla
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top(); // Inicialmente alineado arriba

        hpLabel = new Label("HP: 0", skin);
        fpsLabel = new Label("FPS: 0", skin);
        levelLabel = new Label("LVL 1", skin);
        scoreLabel = new Label("Puntos: 0", skin);

        ProgressBar.ProgressBarStyle xpBarStyle = new ProgressBar.ProgressBarStyle();
        xpBarStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
        xpBarStyle.background.setMinHeight(4);
        xpBarStyle.knobBefore = skin.newDrawable("white", Color.CYAN);
        xpBarStyle.knobBefore.setMinHeight(4);
        xpBar = new ProgressBar(0f, 1f, 0.01f, false, xpBarStyle);

        ProgressBar.ProgressBarStyle cdStyle1 = new ProgressBar.ProgressBarStyle();
        cdStyle1.background = skin.newDrawable("white", Color.DARK_GRAY);
        cdStyle1.background.setMinHeight(4);
        cdStyle1.knobBefore = skin.newDrawable("white", Color.YELLOW);
        cdStyle1.knobBefore.setMinHeight(4);
        ability1Bar = new ProgressBar(0f, 1f, 0.01f, false, cdStyle1);

        ProgressBar.ProgressBarStyle cdStyle2 = new ProgressBar.ProgressBarStyle();
        cdStyle2.background = skin.newDrawable("white", Color.DARK_GRAY);
        cdStyle2.background.setMinHeight(4);
        cdStyle2.knobBefore = skin.newDrawable("white", Color.ORANGE);
        cdStyle2.knobBefore.setMinHeight(4);
        ability2Bar = new ProgressBar(0f, 1f, 0.01f, false, cdStyle2);

        mainTable.add(hpLabel).left().pad(10);
        mainTable.add(levelLabel).center().expandX();
        mainTable.add(fpsLabel).right().pad(10);
        mainTable.add(scoreLabel).center().pad(10);
        mainTable.row();
        mainTable.add(xpBar).colspan(4).expandX().fillX().padLeft(10).padRight(10).padBottom(5);
        mainTable.row();

        mainTable.add().expandY();
        mainTable.row();

        ability1NameLabel = new Label("---", skin);
        ability2NameLabel = new Label("---", skin);

        Table cdTable = new Table();

        cdTable.add(ability1Bar).width(150).padRight(40);
        cdTable.add(ability2Bar).width(150);
        cdTable.row().padTop(5);

        cdTable.add(ability1NameLabel).padRight(40).center();
        cdTable.add(ability2NameLabel).center();

        mainTable.add(cdTable).colspan(4).center().bottom().padBottom(20);

        stage.addActor(mainTable);

        levelUpWindow = new Window("Level Up", skin);
        levelUpWindow.setModal(true);
        levelUpWindow.add(new Label("Has subido de nivel!", skin)).pad(20);
        levelUpWindow.row();
        okButton = new TextButton("OK", skin);
        levelUpWindow.add(okButton).pad(10);

        levelUpWindow.pack();
        levelUpWindow.setPosition(stage.getWidth() / 2 - levelUpWindow.getWidth() / 2, stage.getHeight() / 2 - levelUpWindow.getHeight() / 2);
        levelUpWindow.setVisible(false);
        stage.addActor(levelUpWindow);

        okButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                com.tikisadventure.screens.GameScreen.isGamePaused = false;
                levelUpWindow.setVisible(false);
                Gdx.input.setInputProcessor(null);
            }
        });
    }

    public void update(float hp, ExperienceSystem xpSystem, int score, float ab1Cd, float ab2Cd){

        hpLabel.setText("HP: " + (int)hp);
        levelLabel.setText("LVL " + xpSystem.getLevel());
        xpBar.setValue(xpSystem.getXPPercent());
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        scoreLabel.setText("Puntos: " + score);
        ability1Bar.setValue(ab1Cd);
        ability2Bar.setValue(ab2Cd);
    }

    public void render(){
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (!levelUpWindow.isVisible()) {
            return;
        }
        // Comprobamos la tecla Enter
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            com.tikisadventure.screens.GameScreen.isGamePaused = false;
            levelUpWindow.setVisible(false);
            Gdx.input.setInputProcessor(null);
            return; // Salimos
        }

        // Comprobamos el clic del ratón
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            com.badlogic.gdx.math.Vector2 screenCoords = new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY());
            com.badlogic.gdx.math.Vector2 stageCoords = stage.screenToStageCoordinates(screenCoords);
            com.badlogic.gdx.scenes.scene2d.Actor hitActor = stage.hit(stageCoords.x, stageCoords.y, true);

            if (hitActor != null && (hitActor == okButton || hitActor.getParent() == okButton)) {
                com.tikisadventure.screens.GameScreen.isGamePaused = false;
                levelUpWindow.setVisible(false);
                Gdx.input.setInputProcessor(null);
            }
        }
    }

    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    public void showLevelUpWindow() {
        levelUpWindow.setVisible(true);
    }

    public void setAbilityNames(String name1, String name2) {
        ability1NameLabel.setText(name1 != null ? name1 : "---");
        ability2NameLabel.setText(name2 != null ? name2 : "---");
    }
}
