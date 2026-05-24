package com.tikisadventure.audio;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.tikisadventure.core.Assets;

//Método helper para añadir sonido de hover y click a botones de la UI.
//Usa AudioManager y Assets para el cursor.
public class AudioUtils {
    public static void addButtonSounds(Actor actor) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.playSFX(AudioType.UI_CLICK);
            }
        });

        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    AudioManager.playSFX(AudioType.UI_HOVER);
                    Assets.setHandCursor();
                }
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    Assets.setDefaultCursor();
                }
            }
        });
    }
}
