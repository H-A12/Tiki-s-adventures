package com.tikisadventure.core;

import com.badlogic.gdx.Game;
import com.tikisadventure.screens.GameScreen;

public class TikiGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
