package com.tikisadventure.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.tikisadventure.Main;
import com.tikisadventure.assets.Assets;

public class LoadingScreen extends ScreenAdapter {
    private final Main game;

    public LoadingScreen(Main game) {
        this.game = game;
        Assets.load(); // Inicia la carga al entrar en esta pantalla
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla a negro
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar la carga. Si devuelve true, es que ha terminado.
        if (Assets.update()) {
            // Aquí iríamos a GameScreen (o MenuScreen si lo tienes)
            game.setScreen(new GameScreen(game));
        }

        // Opcional: Imprimir progreso en consola
        float progress = Assets.getProgress() * 100;
        System.out.println("Cargando assets... " + progress + "%");
    }
}
