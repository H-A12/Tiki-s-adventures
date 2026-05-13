package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class CharacterPreviewActor extends Actor {
    private Animation<TextureRegion> animation;
    private float stateTime = 0;

    public CharacterPreviewActor(Animation<TextureRegion> animation) {
        this.animation = animation;
        setSize(32, 32); // Tamaño relativo en el botón
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (animation != null) {
            TextureRegion frame = animation.getKeyFrame(stateTime, true);
            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
            batch.setColor(1, 1, 1, 1);
        }
    }
}
