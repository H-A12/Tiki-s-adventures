package com.tikisadventure.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class SettingsUI extends Window {
    private final Skin skin;
    private Table contentTable;

    public SettingsUI(Skin skin) {
        super("Configuración de Controles", skin);
        this.skin = skin;

        setModal(true);
        setMovable(true);
        pad(20);

        // Tabla de tabs
        Table tabTable = new Table();
        TextButton keyboardTab = new TextButton("Teclado", skin);
        TextButton controllerTab = new TextButton("Mando", skin);
        TextButton touchpadTab = new TextButton("Touchpad", skin);

        tabTable.add(keyboardTab);
        tabTable.add(controllerTab);
        tabTable.add(touchpadTab);
        add(tabTable).padBottom(10).row();

        // Contenido
        contentTable = new Table();
        add(contentTable).minSize(300, 200).row();

        // Lógica de Tabs
        keyboardTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showKeyboardSettings();
            }
        });
        controllerTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showControllerSettings();
            }
        });
        touchpadTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTouchpadSettings();
            }
        });

        TextButton closeButton = new TextButton("Cerrar", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        add(closeButton).padTop(10);
        
        showKeyboardSettings(); // Por defecto
        pack();
    }

    private void showKeyboardSettings() {
        contentTable.clear();
        contentTable.add(new Label("Controles de Teclado", skin)).row();
        // Aquí añadiremos la lista de controles (Accion -> Tecla)
        contentTable.add(new Label("W, A, S, D para mover", skin)).row();
    }

    private void showControllerSettings() {
        contentTable.clear();
        contentTable.add(new Label("Controles de Mando", skin)).row();
    }

    private void showTouchpadSettings() {
        contentTable.clear();
        contentTable.add(new Label("Controles de Touchpad", skin)).row();
    }
}
