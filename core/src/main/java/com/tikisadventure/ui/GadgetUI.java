package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.SaveManager;

public class GadgetUI {
    private final Stage stage;
    private final Skin uiSkin;
    private final GadgetButton btnEquippedGadget;
    private final Texture ventanaTex;
    private final Texture botonTex;
    private Image equippedGadgetImage;

    public GadgetUI(Stage stage, Skin uiSkin) {
        this.stage = stage;
        this.uiSkin = uiSkin;

        ventanaTex = new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaGadget.png"));
        botonTex = new Texture(Gdx.files.internal("Menu/MenuMapas/BotonGadget.png"));

        equippedGadgetImage = new Image();
        equippedGadgetImage.setScaling(Scaling.fit);
        equippedGadgetImage.setOrigin(Align.center);

        btnEquippedGadget = new GadgetButton(botonTex, equippedGadgetImage, 50);
        btnEquippedGadget.setContentSize(25, 25);

        updateEquippedGadgetIcon();

        btnEquippedGadget.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarSelectorGadgets();
            }
        });
    }

    public GadgetButton getButton() {
        return btnEquippedGadget;
    }

    public void updateEquippedGadgetIcon() {
        String currentId = SaveManager.getEquippedGadget();
        if (currentId == null || currentId.isEmpty()) currentId = "grenade_kinetic";
        TextureRegion icon = getGadgetIcon(currentId);
        if (icon != null) equippedGadgetImage.setDrawable(new TextureRegionDrawable(icon));
    }

    private void mostrarSelectorGadgets() {
        final Window modal = new Window("", uiSkin);
        modal.setBackground(new TextureRegionDrawable(new TextureRegion(ventanaTex)));
        modal.setModal(true);
        modal.setMovable(false);

        Label tituloLabel = new Label("Seleccionar Gadget", uiSkin);
        tituloLabel.setFontScale(1.2f);
        tituloLabel.setColor(Color.WHITE);

        Table grid = new Table();
        Array<String> availableGadgets = new Array<>();
        availableGadgets.add("grenade_kinetic");
        if (SaveManager.isCharacterUnlocked(2)) availableGadgets.add("grenade_explosive");
        if (SaveManager.isCharacterUnlocked(3)) availableGadgets.add("grenade_fire");
        if (SaveManager.isGadgetOwned("grenade_freeze")) availableGadgets.add("grenade_freeze");

        String equipped = SaveManager.getEquippedGadget();
        if (equipped == null || equipped.isEmpty()) equipped = "grenade_kinetic";

        if (SaveManager.isGadgetOwned("grenade_cactus")) availableGadgets.add("grenade_cactus");
        if (SaveManager.isGadgetOwned("grenade_sewer")) availableGadgets.add("grenade_sewer");
        if (SaveManager.isGadgetOwned("grenade_sheel")) availableGadgets.add("grenade_sheel");
        if (SaveManager.isGadgetOwned("grenade_scarecrow")) availableGadgets.add("grenade_scarecrow");
        if (SaveManager.isGadgetOwned("grenade_turret")) availableGadgets.add("grenade_turret");

        int col = 0;
        for (final String id : availableGadgets) {
            final boolean isSelected = id.equals(equipped);

            final Image iconImg = new Image(getGadgetIcon(id));
            iconImg.setScaling(Scaling.fit);
            iconImg.setOrigin(Align.center);
            iconImg.setColor(Color.WHITE);

            final GadgetButton gadgetBtn = new GadgetButton(botonTex, iconImg, 55);
            gadgetBtn.setContentSize(25, 25);
            if (isSelected) {
                gadgetBtn.getBgImage().setColor(new Color(0.9f, 0.5f, 0.5f, 1f));
            }

            gadgetBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SaveManager.setEquippedGadget(id);
                    updateEquippedGadgetIcon();
                    modal.remove();
                }
            });

            grid.add(gadgetBtn).size(55, 55).pad(10);
            col++;
            if (col >= 3) { grid.row(); col = 0; }
        }

        Label cerrarLabel = new Label("Cerrar", uiSkin);
        cerrarLabel.setColor(Color.WHITE);
        cerrarLabel.setAlignment(Align.center);

        final GadgetButton cerrarBtn = new GadgetButton(botonTex, cerrarLabel, 50);
        cerrarBtn.setContentSize(40, 20);
        cerrarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { modal.remove(); }
        });

        modal.add(tituloLabel).padTop(15).row();
        modal.add(grid).pad(30).row();
        modal.add(cerrarBtn).padTop(10).padBottom(15).size(50, 50);
        modal.setSize(300, 300);
        modal.setPosition(Math.round((stage.getWidth() - modal.getWidth()) / 2f), Math.round((stage.getHeight() - modal.getHeight()) / 2f));
        stage.addActor(modal);
    }

    private TextureRegion getGadgetIcon(String gadgetId) {
        try {
            JsonValue abilitiesData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));
            JsonValue def = abilitiesData.get(gadgetId);
            if (def != null && def.has("effects")) {
                JsonValue effects = def.get("effects");
                for (int i = 0; i < effects.size; i++) {
                    JsonValue eff = effects.get(i);
                    if ("THROW".equals(eff.getString("type"))) {
                        String spriteName = eff.get("params").getString("sprite");
                        TextureRegion region = Assets.getRegion("shared", spriteName);
                        if (region != null) return region;
                        if (spriteName.startsWith("weapons_assets/")) {
                            return Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("GadgetUI", "Error icono gadget " + gadgetId, e);
        }
        return Assets.getRegion("shared", "UI_assets/UI_Crosshair");
    }

    public void dispose() {
        if (ventanaTex != null) ventanaTex.dispose();
        if (botonTex != null) botonTex.dispose();
    }

    public static class GadgetButton extends Group {
        private final Image bgImage;
        private final Actor contentActor;
        private Color baseColor = Color.WHITE;

        public GadgetButton(Texture buttonTex, Actor contentActor, int size) {
            this.contentActor = contentActor;
            setSize(size, size);
            setOrigin(Align.center);
            setTouchable(Touchable.enabled);

            bgImage = new Image(buttonTex);
            bgImage.setScaling(Scaling.fit);
            bgImage.setOrigin(Align.center);
            bgImage.setFillParent(true);
            addActor(bgImage);

            addActor(this.contentActor);
            this.contentActor.setSize(size, size);
            this.contentActor.setPosition((getWidth() - this.contentActor.getWidth()) / 2f, (getHeight() - this.contentActor.getHeight()) / 2f);

            addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        baseColor = bgImage.getColor().cpy();
                        clearActions();
                        addAction(Actions.parallel(
                            Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineOut),
                            Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                        ));
                    }
                    super.enter(event, x, y, pointer, fromActor);
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        clearActions();
                        addAction(Actions.sequence(
                            Actions.parallel(
                                Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                                Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                            ),
                            Actions.run(() -> bgImage.setColor(baseColor))
                        ));
                    }
                    super.exit(event, x, y, pointer, toActor);
                }
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    clearActions();
                    addAction(Actions.parallel(
                        Actions.scaleTo(0.9f, 0.9f, 0.05f, Interpolation.sineOut),
                        Actions.color(new Color(0.5f, 0.5f, 0.5f, 1f), 0.05f)
                    ));
                    return super.touchDown(event, x, y, pointer, button);
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    clearActions();
                    if (isOver()) {
                        addAction(Actions.parallel(
                            Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineIn),
                            Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                        ));
                    } else {
                        clearActions();
                        bgImage.setColor(baseColor);
                        setColor(Color.WHITE);
                    }
                    super.touchUp(event, x, y, pointer, button);
                }
            });
        }

        @Override
        public void setColor(Color color) {
            super.setColor(Color.WHITE);
            bgImage.setColor(color);
            baseColor = color.cpy();
        }

        public Color getBaseColor() {
            return baseColor;
        }

        public Image getBgImage() {
            return bgImage;
        }

        public void setContentSize(int w, int h) {
            this.contentActor.setSize(w, h);
            this.contentActor.setPosition((getWidth() - w) / 2f, (getHeight() - h) / 2f);
        }
    }
}
