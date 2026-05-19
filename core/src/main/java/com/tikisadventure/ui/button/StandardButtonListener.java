package com.tikisadventure.ui.button;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.audio.AudioType;
import com.tikisadventure.core.Assets;

public class StandardButtonListener extends ClickListener {
    private final Runnable action;

    public StandardButtonListener(Runnable action) {
        this.action = action;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        super.enter(event, x, y, pointer, fromActor);
        if (pointer == -1) {
            Actor actor = event.getListenerActor();
            actor.setOrigin(Align.center);
            Assets.setHandCursor();
            AudioManager.playSFX(AudioType.UI_HOVER);
            actor.clearActions();
            actor.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
        }
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        super.exit(event, x, y, pointer, toActor);
        if (pointer == -1) {
            Assets.setDefaultCursor();
            Actor actor = event.getListenerActor();
            actor.clearActions();
            actor.addAction(Actions.scaleTo(1f, 1f, 0.1f));
        }
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        Actor actor = event.getListenerActor();
        actor.clearActions();
        actor.addAction(Actions.scaleTo(0.9f, 0.9f, 0.05f));
        return super.touchDown(event, x, y, pointer, button);
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (isOver()) {
            Actor actor = event.getListenerActor();
            actor.clearActions();
            actor.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
        }
        super.touchUp(event, x, y, pointer, button);
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        AudioManager.playSFX(AudioType.UI_CLICK);
        if (action != null) action.run();
    }
}
