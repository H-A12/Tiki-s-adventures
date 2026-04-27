package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;

public class DeleteWeaponUI extends Window {

    private Table listTable;
    private Skin skin;
    private Stage stage;
    private Runnable onWeaponDeleted;

    public DeleteWeaponUI(Skin skin, Stage stage, Runnable onWeaponDeleted) {
        super("Borrar Armas Custom", skin);
        this.skin = skin;
        this.stage = stage;
        this.onWeaponDeleted = onWeaponDeleted;

        setModal(true);
        setMovable(true);
        setResizable(false);
        pad(15);
        padTop(35);

        // Tabla interna que albergará la lista de armas
        listTable = new Table();
        listTable.top();

        // Creamos el Slider / ScrollPane
        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Solo scroll vertical

        add(scrollPane).width(350).height(200).row();

        TextButton btnCerrar = new TextButton("Cerrar", skin);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });

        add(btnCerrar).padTop(15).width(120);

        refreshList();
        pack();

        // Centrar en pantalla
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));
    }

    public void show() {
        stage.addActor(this);
    }

    private void refreshList() {
        listTable.clearChildren();

        // Cargamos la X roja
        TextureRegion xTex = Assets.getRegion("shared", "UI_assets/UI_X");
        if (xTex == null) xTex = Assets.getRegion("shared", "UI_assets/UI_Crosshair");

        // Si no hay armas creadas
        if (GameSession.customWeapons.size == 0) {
            listTable.add(new Label("No tienes armas custom creadas.", skin)).pad(20);
            return;
        }

        // Iterar por cada arma guardada
        for (final GameSession.CustomWeaponConfig conf : GameSession.customWeapons.values()) {

            // --- NUEVO: Convertimos toda la fila en un botón ---
            Button rowButton = new Button(skin);

            // Nombre del arma
            Label nameLabel = new Label(conf.name, skin);

            // Imagen de la X (ya no es un ImageButton, solo la imagen escalada)
            Image imgDelete = new Image(xTex);
            imgDelete.setScaling(com.badlogic.gdx.utils.Scaling.fit);

            // Al hacer clic en CUALQUIER PARTE de la fila, sale el aviso
            ClickListener deleteListener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showConfirmDialog(conf);
                }
            };
            rowButton.addListener(deleteListener);

            // Añadimos los elementos al botón/fila
            rowButton.add(nameLabel).expandX().left().padLeft(15);

            // --- NUEVO: Aumentamos el tamaño de la X a 40x40 ---
            rowButton.add(imgDelete).size(40, 40).right().padRight(15).padTop(5).padBottom(5);

            // Añadimos el botón a la lista general con un poco de separación
            listTable.add(rowButton).padBottom(8).fillX().expandX().row();
        }
    }

    private void showConfirmDialog(final GameSession.CustomWeaponConfig conf) {
        final Dialog confirm = new Dialog("Aviso", skin);
        confirm.text("¿Seguro que quieres borrar\n" + conf.name + "?");
        confirm.pad(20);

        TextButton btnSi = new TextButton("SI", skin);
        btnSi.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Borramos de la RAM y guardamos en el disco
                GameSession.customWeapons.remove(conf.id);
                GameSession.saveCustomWeapons();

                // Refrescamos la lista visual
                refreshList();

                // Avisamos a MenuGodMode para que quite el arma de los desplegables
                if (onWeaponDeleted != null) onWeaponDeleted.run();

                confirm.hide();
            }
        });

        TextButton btnNo = new TextButton("NO", skin);
        btnNo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirm.hide();
            }
        });

        confirm.getButtonTable().add(btnSi).width(80).pad(10);
        confirm.getButtonTable().add(btnNo).width(80).pad(10);
        confirm.show(stage);
    }
}
