package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class AnimatedImage extends Image {
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;

    public AnimatedImage(Animation<TextureRegion> animation) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
        setScaling(com.badlogic.gdx.utils.Scaling.fit);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        ((TextureRegionDrawable)getDrawable()).setRegion(animation.getKeyFrame(stateTime, true));
    }
}
