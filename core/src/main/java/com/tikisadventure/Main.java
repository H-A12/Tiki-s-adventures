package com.tikisadventure;

import com.badlogic.gdx.Game;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
