package com.tikisadventure;

import com.badlogic.gdx.Game;
import com.tikisadventure.screens.GameScreen;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
