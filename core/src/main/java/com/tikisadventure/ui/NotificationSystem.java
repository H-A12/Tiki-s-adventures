package com.tikisadventure.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;

public class NotificationSystem {
    public static void showNotification(Stage stage, Skin skin, String message) {
        Window notification = new Window("", skin);
        notification.setModal(false);
        notification.setMovable(false);
        notification.add(new Label(message, skin)).pad(10);
        notification.pack();
        notification.setPosition(stage.getWidth() / 2f, stage.getHeight() * 0.8f, Align.center);
        notification.getColor().a = 0;
        notification.addAction(Actions.sequence(
            Actions.fadeIn(0.5f),
            Actions.delay(2.0f),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));
        stage.addActor(notification);
    }
}
