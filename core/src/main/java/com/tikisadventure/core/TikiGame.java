package com.tikisadventure.core;

import com.badlogic.gdx.Game;
import com.tikisadventure.screens.GameScreen;

public class TikiGame extends Game {

    @Override
    public void create() {
        Assets.load();
        Assets.finishLoading();
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        Assets.dispose();
    }
}
