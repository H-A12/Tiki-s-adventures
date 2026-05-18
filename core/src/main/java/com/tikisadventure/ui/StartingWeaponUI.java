package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.tikisadventure.audio.AudioUtils;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;

public class StartingWeaponUI {
    private final Stage stage;
    private final Skin uiSkin;
    private final Button btnEquippedWeapon;
    private final Image equippedWeaponImage;
    private final Texture btnWeaponTex;
    private final Texture ventanaWeaponTex;

    public StartingWeaponUI(Stage stage, Skin uiSkin) {
        this.stage = stage;
        this.uiSkin = uiSkin;

        btnWeaponTex = new Texture(Gdx.files.internal("Menu/MenuMapas/BotonGadget.png"));
        ventanaWeaponTex = new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaGadget.png"));

        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(new TextureRegion(btnWeaponTex));

        btnEquippedWeapon = new Button(style);
        equippedWeaponImage = new Image();
        equippedWeaponImage.setScaling(Scaling.fit);
        equippedWeaponImage.setOrigin(15f, 15f);
        btnEquippedWeapon.add(equippedWeaponImage).size(30, 30).center();
        btnEquippedWeapon.setSize(50, 50);
        AudioUtils.addButtonSounds(btnEquippedWeapon);

        updateEquippedWeaponIcon();

        btnEquippedWeapon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarSelectorArmas();
            }
        });
    }

    public Button getButton() {
        return btnEquippedWeapon;
    }

    public void updateEquippedWeaponIcon() {
        String currentId = SaveManager.getEquippedStartingWeapon();
        if (currentId == null || currentId.isEmpty()) {
            currentId = "BallRifle";
        }
        TextureRegion icon = getWeaponIcon(currentId);
        if (icon != null) equippedWeaponImage.setDrawable(new TextureRegionDrawable(icon));
    }

    public void mostrarSelectorArmas() {
        final Window modal = new Window("", uiSkin);
        Image bgImage = new Image(ventanaWeaponTex);
        modal.setBackground(bgImage.getDrawable());
        modal.setModal(true);
        modal.setMovable(false);

        modal.pad(20);

        Table grid = new Table();
        Array<String> availableWeapons = new Array<>();

        JsonValue weaponsData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        JsonValue weapons = weaponsData.get("weapons");

        String selected = SaveManager.getEquippedStartingWeapon();
        if (selected == null) selected = "BallRifle";

        for (JsonValue weaponEntry : weapons) {
            String weaponId = weaponEntry.name;
            if ("PlantillaArma".equals(weaponId) || "EspadaEjemplo".equals(weaponId)) continue;
            availableWeapons.add(weaponId);
        }

        TextureRegion lockRegion = Assets.getRegion("shared", "UI_assets/lock16");

        int col = 0;
        for (final String id : availableWeapons) {
            final boolean unlocked = SaveManager.isWeaponUnlockedOrDefault(id);
            final boolean isSelected = id.equals(selected) && unlocked;

            Button.ButtonStyle btnStyle = new Button.ButtonStyle();
            btnStyle.up = new TextureRegionDrawable(new TextureRegion(btnWeaponTex));
            final Button btn = new Button(btnStyle);
            btn.setChecked(isSelected);

            TextureRegion iconRegion = getWeaponIcon(id);

            if (!unlocked) {
                Image img = new Image(iconRegion);
                img.setScaling(Scaling.fit);
                img.setOrigin(15f, 15f);
                img.setColor(new Color(0.3f, 0.3f, 0.3f, 0.8f));

                Image lockImg = new Image(lockRegion);
                lockImg.setScaling(Scaling.fit);
                lockImg.setOrigin(8f, 8f);

                Stack stack = new Stack();
                stack.add(img);
                stack.add(lockImg);
                btn.add(stack).size(30, 30);

                btn.setColor(new Color(0.3f, 0.3f, 0.3f, 0.7f));
                btn.setDisabled(true);
            } else {
                final Image img = new Image(iconRegion);
                img.setScaling(Scaling.fit);
                img.setOrigin(15f, 15f);
                img.setColor(Color.WHITE);

                btn.add(img).size(30, 30);

                if (isSelected) {
                    btn.setColor(new Color(0.9f, 0.5f, 0.5f, 1f));
                }

                img.addListener(new ClickListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (pointer == -1) {
                            img.clearActions();
                            img.addAction(Actions.parallel(
                                Actions.scaleTo(1.2f, 1.2f, 0.1f, Interpolation.sineOut),
                                Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                            ));
                        }
                        super.enter(event, x, y, pointer, fromActor);
                    }
                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if (pointer == -1) {
                            img.clearActions();
                            img.addAction(Actions.parallel(
                                Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                                Actions.color(Color.WHITE, 0.1f)
                            ));
                        }
                        super.exit(event, x, y, pointer, toActor);
                    }
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        img.clearActions();
                        img.addAction(Actions.parallel(
                            Actions.scaleTo(0.85f, 0.85f, 0.05f, Interpolation.sineOut),
                            Actions.color(new Color(0.5f, 0.5f, 0.5f, 1f), 0.05f)
                        ));
                        return super.touchDown(event, x, y, pointer, button);
                    }
                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        img.clearActions();
                        if (isOver()) {
                            img.addAction(Actions.parallel(
                                Actions.scaleTo(1.2f, 1.2f, 0.1f, Interpolation.sineIn),
                                Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                            ));
                        } else {
                            img.addAction(Actions.parallel(
                                Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                                Actions.color(Color.WHITE, 0.1f)
                            ));
                        }
                        super.touchUp(event, x, y, pointer, button);
                    }
                });

                btn.addListener(new Assets.HoverCursorListener());
                btn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        SaveManager.setEquippedStartingWeapon(id);
                        updateEquippedWeaponIcon();
                        modal.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.removeActor()));
                    }
                });
            }

            grid.add(btn).size(60, 60).pad(10);
            grid.padTop(20);
            col++;
            if (col >= 3) { grid.row(); col = 0; }
        }

        TextButton.TextButtonStyle cerrarStyle = new TextButton.TextButtonStyle();
        cerrarStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        cerrarStyle.font = uiSkin.get("font-14", Label.LabelStyle.class).font;
        cerrarStyle.pressedOffsetX = 0;
        cerrarStyle.pressedOffsetY = 0;
        TextButton btnCerrar = new TextButton("Cerrar", cerrarStyle);
        btnCerrar.addListener(new Assets.HoverCursorListener());
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { modal.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.removeActor())); }
        });

        modal.add(grid).pad(10).row();
        modal.add(btnCerrar).padTop(2).padBottom(25).width(140);
        modal.pack();
        modal.setPosition(Math.round((stage.getWidth() - modal.getWidth()) / 2f), Math.round((stage.getHeight() - modal.getHeight()) / 2f));
        modal.getColor().a = 0f;
        modal.addAction(Actions.fadeIn(0.2f));
        stage.addActor(modal);
    }

    public void updateGodModeAppearance() {
        if (GameSession.godMode) {
            Color dark = new Color(0.25f, 0.25f, 0.25f, 1f);
            btnEquippedWeapon.setColor(dark);
            equippedWeaponImage.setColor(dark);
        } else {
            btnEquippedWeapon.setColor(Color.WHITE);
            equippedWeaponImage.setColor(Color.WHITE);
        }
    }

    private TextureRegion getWeaponIcon(String weaponId) {
        try {
            JsonValue weaponsData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
            JsonValue weapons = weaponsData.get("weapons");
            JsonValue def = weapons.get(weaponId);
            if (def != null && def.has("sprite")) {
                String spriteName = def.getString("sprite");
                TextureRegion region = Assets.getRegion("shared", spriteName);
                if (region != null) return region;
                if (spriteName.startsWith("weapons_assets/")) {
                    region = Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
                    if (region != null) return region;
                }
                if (spriteName.startsWith("particle_assets/")) {
                    region = Assets.getRegion("shared", "weapons_assets/" + spriteName.replace("particle_assets/", ""));
                    if (region != null) return region;
                }
            }
        } catch (Exception e) {
            Gdx.app.error("StartingWeaponUI", "Error icono arma " + weaponId, e);
        }
        return Assets.getRegion("shared", "UI_assets/UI_Crosshair");
    }

    public void dispose() {
        if (btnWeaponTex != null) btnWeaponTex.dispose();
        if (ventanaWeaponTex != null) ventanaWeaponTex.dispose();
    }
}
