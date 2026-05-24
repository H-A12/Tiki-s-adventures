package com.tikisadventure.core;

import com.badlogic.gdx.Game;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuScreen;

//Punto de entrada del juego. Carga assets, audio y abre el menú principal.
//Usa SaveManager para ajustes guardados y pasa a GameScreen cuando toca.
public class TikiGame extends Game {

    @Override
    //Iniciar juego
    public void create() {
        Assets.load();
        Assets.finishLoading();
        Assets.loadCursor();
        AudioManager.load();
        AudioManager.setMusicVolume(SaveManager.getMusicVolume());
        AudioManager.setSFXVolume(SaveManager.getSFXVolume());
        setScreen(new MenuScreen(this));
    }

    @Override
    //Cerrar juego
    public void dispose() {
        super.dispose();
        AudioManager.dispose();
        Assets.dispose();
    }
}
