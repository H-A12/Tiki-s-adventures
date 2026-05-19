package com.tikisadventure.core;

import com.badlogic.gdx.Game;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuScreen;

public class TikiGame extends Game {

    @Override
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
    public void dispose() {
        super.dispose();
        AudioManager.dispose();
        Assets.dispose();
    }
}
