package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.tikisadventure.core.Assets;

public class VirtualCursorActor extends Image {
    public VirtualCursorActor() {
        super(Assets.getRegion("shared", "UI_assets/UI_Circle"));
        setSize(32, 32); // Adjust size as needed
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }
}
