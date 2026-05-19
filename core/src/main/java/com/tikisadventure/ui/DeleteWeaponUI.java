package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.ui.button.ButtonFactory;

public class DeleteWeaponUI extends Window {

    private Table listTable;
    private Skin skin;
    private Stage stage;
    private String customMessage;
    private Runnable onWeaponDeleted;
    private TextButton.TextButtonStyle btnStyle;
    private ScrollPane scrollPane;
    private boolean focusSet = false;

    // Constructor clásico (Para abrir desde el Modo Dios normalmente)
    public DeleteWeaponUI(Skin skin, Stage stage, Runnable onWeaponDeleted) {
        this(skin, stage, null, onWeaponDeleted);
    }

    // Nuevo Constructor que acepta un mensaje de aviso
    public DeleteWeaponUI(Skin skin, Stage stage, String customMessage, Runnable onWeaponDeleted) {
        super("", skin);
        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaArmas.png")));
        setBackground(bgImage.getDrawable());
        this.skin = skin;
        this.stage = stage;
        this.customMessage = customMessage;
        this.onWeaponDeleted = onWeaponDeleted;

        setModal(true);
        setMovable(true);
        setResizable(false);
        pad(25);
        padTop(55);
        padLeft(60);
        padRight(60);

        // --- NUEVO: Añadimos el mensaje de aviso arriba si existe ---
        if (customMessage != null) {
            Label msgLabel = new Label(customMessage, skin);
            msgLabel.setWrap(true);
            msgLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            msgLabel.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
            add(msgLabel).width(320).padBottom(15).row();
        }
        // ------------------------------------------------------------

        // Tabla interna que albergará la lista de armas
        listTable = new Table();
        listTable.top();

        // Creamos el Slider / ScrollPane
        scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Solo scroll vertical

        add(scrollPane).width(340).height(200).row();

        btnStyle = ButtonFactory.getTextBtnStyle();

        TextButton btnCerrar = ButtonFactory.createTextButton("Cerrar", () -> {
            if (getStage() != null) getStage().setScrollFocus(null);
            addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.removeActor()));
        });

        add(btnCerrar).padTop(15).width(140);

        refreshList();
        pack();

        // Centrar en pantalla
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));
    }

    public void show() {
        getColor().a = 0f;
        addAction(Actions.fadeIn(0.2f));
        stage.addActor(this);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!focusSet && getStage() != null) {
            getStage().setScrollFocus(scrollPane);
            focusSet = true;
        }
    }

    private void refreshList() {
        listTable.clearChildren();

        TextureRegion xTex = Assets.getRegion("shared", "UI_assets/UI_X");
        if (xTex == null) xTex = Assets.getRegion("shared", "UI_assets/UI_Crosshair");

        if (GameSession.customWeapons.size == 0) {
            listTable.add(new Label("No tienes armas custom creadas.", skin)).pad(20);
            return;
        }

        for (final GameSession.CustomWeaponConfig conf : GameSession.customWeapons.values()) {
            Button rowButton = new Button(skin);
            Label nameLabel = new Label(conf.name, skin);
            Image imgDelete = new Image(xTex);
            imgDelete.setScaling(com.badlogic.gdx.utils.Scaling.fit);

            ClickListener deleteListener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showConfirmDialog(conf);
                }
            };
            rowButton.addListener(deleteListener);

            rowButton.add(nameLabel).expandX().left().padLeft(15);
            rowButton.add(imgDelete).size(40, 40).right().padRight(15).padTop(5).padBottom(5);
            listTable.add(rowButton).padBottom(8).fillX().expandX().row();
        }
    }

    private void showConfirmDialog(final GameSession.CustomWeaponConfig conf) {
        final Dialog confirm = new Dialog("Aviso", skin);
        confirm.text("¿Seguro que quieres borrar\n" + conf.name + "?");
        confirm.pad(15);

        TextButton btnSi = ButtonFactory.createTextButton("SI", () -> {
            GameSession.customWeapons.remove(conf.id);
            GameSession.saveCustomWeapons();

            String currentUser = com.tikisadventure.core.SaveManager.getLastUsername();
            if (currentUser != null && !currentUser.isEmpty()) {
                long coins = com.tikisadventure.core.SaveManager.getProfileData().coins;
                long score = com.tikisadventure.core.SaveManager.getProfileData().totalScore;
                new com.tikisadventure.database.progress.ProgressRepository()
                    .actualizarProgreso(currentUser, coins, score, null);
            }

            confirm.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.run(new Runnable() {
                @Override
                public void run() {
                    confirm.hide();
                }
            })));

            if (customMessage != null) {
                DeleteWeaponUI.this.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.removeActor()));
            } else {
                refreshList();
            }

            if (onWeaponDeleted != null) onWeaponDeleted.run();
        });

        TextButton btnNo = ButtonFactory.createTextButton("NO", () -> {
            confirm.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.run(new Runnable() {
                @Override
                public void run() {
                    confirm.hide();
                }
            })));
        });

        confirm.getButtonTable().add(btnSi).width(100).pad(10);
        confirm.getButtonTable().add(btnNo).width(100).pad(10);
        confirm.getColor().a = 0f;
        confirm.addAction(Actions.fadeIn(0.2f));
        confirm.show(stage);
    }
}
