package com.tikisadventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tikisadventure.assets.Assets;
import com.tikisadventure.screens.GameScreen;

public class Main extends Game {

    private SpriteBatch batch;
    private boolean assetsLoaded = false;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // 1. Iniciamos la carga asíncrona de los assets (Atlas + JSON)
        Assets.load();
    }

    @Override
    public void render() {
        // 2. Verificamos si los assets terminaron de cargar
        if (!assetsLoaded) {
            if (Assets.update()) {
                assetsLoaded = true;
                // 3. Solo cuando Assets.update() es true, cambiamos de pantalla
                setScreen(new GameScreen(this, batch));
            } else {
                // Opcional: Aquí podrías dibujar una barra de carga simple
                // float progress = Assets.getProgress();
                return; // No renderizamos nada más hasta que cargue
            }
        }

        // Delegamos el render a la pantalla activa (GameScreen)
        super.render();
    }

    @Override
    public void dispose() {
        // Limpieza de recursos globales
        batch.dispose();
        Assets.dispose();
        super.dispose();
    }
}
