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

    private ProgressBar xpBar;

    public HUD(Batch batch){

        stage = new Stage(new ScreenViewport(), batch);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        hpLabel = new Label("HP: 0", skin);
        fpsLabel = new Label("FPS: 0", skin);
        levelLabel = new Label("LVL 1", skin);


        ProgressBar.ProgressBarStyle xpBarStyle = new ProgressBar.ProgressBarStyle();
        xpBarStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
        xpBarStyle.background.setMinHeight(1);
        xpBarStyle.knobBefore = skin.newDrawable("white", Color.CYAN);
        xpBarStyle.knobBefore.setMinHeight(1);

        xpBar = new ProgressBar(0f, 1f, 0.01f, false, xpBarStyle);

        table.add(hpLabel).left().pad(10);
        table.add(levelLabel).center().expandX();
        table.add(fpsLabel).right().pad(10);

        table.row();

        table.add(xpBar)
            .colspan(3)
            .expandX()
            .fillX().padLeft(10)
            .padRight(10)
            .padBottom(5);

        stage.addActor(table);
    }

    public void update(float hp, ExperienceSystem xpSystem){

        hpLabel.setText("HP: " + (int)hp);

        levelLabel.setText("LVL " + xpSystem.getLevel());

        xpBar.setValue(xpSystem.getXPPercent());

        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    public void render(){
        stage.act();
        stage.draw();
    }

    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }
}
