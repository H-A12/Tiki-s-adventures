package com.tikisadventure.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tikisadventure.entities.player.Tiki;
import com.tikisadventure.systems.ExperienceSystem;

public class HUD {

    private Stage stage;
    private Label fpsLabel;
    private Label hpLabel;
    private Label xpLabel;

    public HUD(Batch batch){

        stage = new Stage(new ScreenViewport(), batch);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        hpLabel = new Label("HP: 0", skin);
        xpLabel = new Label("LVL 1 XP: 0/10", skin);
        fpsLabel = new Label("FPS: 0", skin);

        table.add(hpLabel).left().pad(10);
        table.add(xpLabel).center().pad(10).expandX();
        table.add(fpsLabel).right().pad(10);

        stage.addActor(table);
    }

    public void update(float hp, ExperienceSystem experienceSystem){

        hpLabel.setText("HP: " + (int)hp);

        xpLabel.setText(
            "LVL " + experienceSystem.getLevel() +
                " XP: " + experienceSystem.getCurrentXP() +
                "/" + experienceSystem.getXpToNextLevel()
        );

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
