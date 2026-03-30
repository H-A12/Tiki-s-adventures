package com.tikisadventure.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikisadventure.systems.ExperienceSystem;
import com.tikisadventure.systems.WaveSystem; // <--- Añadido

public class HUD {

    private Stage stage;

    private Label fpsLabel;
    private Label hpLabel;
    private Label levelLabel;
    private Label waveLabel; // <--- Para mostrar la oleada actual

    private ProgressBar xpBar;

    public HUD(Batch batch) {
        stage = new Stage(new ScreenViewport(), batch);
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        // Inicializar labels
        hpLabel = new Label("HP: 0", skin);
        fpsLabel = new Label("FPS: 0", skin);
        levelLabel = new Label("LVL 1", skin);
        waveLabel = new Label("WAVE 1", skin);
        waveLabel.setColor(Color.GOLD); // Un toque distintivo para la oleada

        // Estilo de la barra de XP (Cian/Azul)
        ProgressBar.ProgressBarStyle xpBarStyle = new ProgressBar.ProgressBarStyle();
        xpBarStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
        xpBarStyle.background.setMinHeight(10); // Un poco más alta para que se vea bien
        xpBarStyle.knobBefore = skin.newDrawable("white", Color.CYAN);
        xpBarStyle.knobBefore.setMinHeight(10);

        xpBar = new ProgressBar(0f, 1f, 0.01f, false, xpBarStyle);

        // Fila 1: Stats principales
        table.add(hpLabel).left().pad(10);
        table.add(levelLabel).center().expandX();
        table.add(fpsLabel).right().pad(10);

        table.row();

        // Fila 2: Información de oleada
        table.add(waveLabel).colspan(3).center().padTop(5);

        table.row();

        // Fila 3: Barra de experiencia que ocupa todo el ancho
        table.add(xpBar)
            .colspan(3)
            .expandX()
            .fillX()
            .pad(10, 20, 5, 20); // Márgenes laterales para que no toque los bordes

        stage.addActor(table);
    }

    /**
     * Ahora el update recibe el WaveSystem para sincronizar la progresión
     */
    public void update(float hp, ExperienceSystem xpSystem, WaveSystem waveSystem) {
        hpLabel.setText("HP: " + (int)hp);

        // Color dinámico para la vida (Feedback visual)
        if (hp < 30) hpLabel.setColor(Color.RED);
        else hpLabel.setColor(Color.WHITE);

        levelLabel.setText("LVL " + xpSystem.getLevel());
        waveLabel.setText("WAVE: " + waveSystem.getCurrentWave());

        xpBar.setValue(xpSystem.getXPPercent());
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

        stage.act();
    }

    public void render() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
