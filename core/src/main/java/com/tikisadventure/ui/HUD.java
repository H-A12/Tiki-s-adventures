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

    private ProgressBar xpBar;
    private ProgressBar ability1Bar;
    private ProgressBar ability2Bar;

    public HUD(Batch batch){

        stage = new Stage(new ScreenViewport(), batch);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.top();
        table.setFillParent(true);

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

        table.add(hpLabel).left().pad(10);
        table.add(levelLabel).center().expandX();
        table.add(fpsLabel).right().pad(10);
        table.add(scoreLabel).center().pad(10);
        table.row();
        table.add(xpBar).colspan(4).expandX().fillX().padLeft(10).padRight(10).padBottom(5);

        // Cooldowns table
        Table cdTable = new Table();
        cdTable.bottom().center();
        cdTable.setFillParent(true);
        cdTable.padBottom(20);
        
        cdTable.add(new Label("Dash", skin)).padRight(5);
        cdTable.add(ability1Bar).width(100).padRight(20);
        cdTable.add(new Label("Grenade", skin)).padRight(5);
        cdTable.add(ability2Bar).width(100);

        stage.addActor(table);
        stage.addActor(cdTable);
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
        stage.act();
        stage.draw();
    }

    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }
}
