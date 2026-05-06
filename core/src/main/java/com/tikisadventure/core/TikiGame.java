package com.tikisadventure.core;

import com.badlogic.gdx.Game;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.screens.MenuScreen;

public class TikiGame extends Game {

    @Override
    public void create() {
        Assets.load();
        Assets.finishLoading();
        Assets.loadCursor();
        Assets.loadHandCursor();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        Assets.dispose();
    }
}
