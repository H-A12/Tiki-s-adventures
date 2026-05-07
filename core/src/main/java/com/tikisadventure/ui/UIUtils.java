package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tikisadventure.core.Assets;

public class UIUtils {

    public static void configureButton(final Button btn, final Runnable accion) {
        btn.clearListeners();
        btn.addListener(new Assets.HoverCursorListener());

        btn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !btn.isDisabled()) {
                    btn.clearActions();
                    btn.addAction(Actions.parallel(
                        Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineOut),
                        Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                    ));
                }
                super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !btn.isDisabled()) {
                    btn.clearActions();
                    btn.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                        Actions.color(Color.WHITE, 0.1f)
                    ));
                }
                super.exit(event, x, y, pointer, toActor);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!btn.isDisabled()) {
                    btn.clearActions();
                    btn.addAction(Actions.parallel(
                        Actions.scaleTo(0.9f, 0.9f, 0.05f, Interpolation.sineOut),
                        Actions.color(new Color(0.5f, 0.5f, 0.5f, 1f), 0.05f)
                    ));
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!btn.isDisabled()) {
                    btn.clearActions();
                    if (isOver()) {
                        btn.addAction(Actions.parallel(
                            Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineIn),
                            Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                        ));
                    } else {
                        btn.addAction(Actions.parallel(
                            Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                            Actions.color(Color.WHITE, 0.1f)
                        ));
                    }
                }
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btn.isDisabled()) accion.run();
            }
        });
    }

    public static void configureActorAnimated(final Actor actor, final Runnable accion) {
        actor.addListener(new Assets.HoverCursorListener());
        
        actor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) { // Removed isDisabled check for general Actors
                    actor.addAction(Actions.parallel(
                        Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineOut),
                        Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                    ));
                }
                super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    actor.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                        Actions.color(Color.WHITE, 0.1f)
                    ));
                }
                super.exit(event, x, y, pointer, toActor);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                actor.addAction(Actions.parallel(
                    Actions.scaleTo(0.9f, 0.9f, 0.05f, Interpolation.sineOut),
                    Actions.color(new Color(0.5f, 0.5f, 0.5f, 1f), 0.05f)
                ));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (isOver()) {
                    actor.addAction(Actions.parallel(
                        Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineIn),
                        Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                    ));
                } else {
                    actor.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                        Actions.color(Color.WHITE, 0.1f)
                    ));
                }
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                accion.run();
            }
        });
    }
}
