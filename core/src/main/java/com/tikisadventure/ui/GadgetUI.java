package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.SaveManager;

public class GadgetUI {
    private final Stage stage;
    private final Skin uiSkin;
    private final Button btnEquippedGadget;
    private final Image equippedGadgetImage;

    public GadgetUI(Stage stage, Skin uiSkin) {
        this.stage = stage;
        this.uiSkin = uiSkin;

        btnEquippedGadget = new Button(uiSkin);
        equippedGadgetImage = new Image();
        equippedGadgetImage.setScaling(Scaling.fit);
        btnEquippedGadget.add(equippedGadgetImage).size(30, 30).center();
        btnEquippedGadget.setSize(40, 40);

        updateEquippedGadgetIcon();

        btnEquippedGadget.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarSelectorGadgets();
            }
        });
    }

    public Button getButton() {
        return btnEquippedGadget;
    }

    public void updateEquippedGadgetIcon() {
        String currentId = SaveManager.getEquippedGadget();
        if (currentId == null || currentId.isEmpty()) currentId = "grenade_kinetic";
        TextureRegion icon = getGadgetIcon(currentId);
        if (icon != null) equippedGadgetImage.setDrawable(new TextureRegionDrawable(icon));
    }

    private void mostrarSelectorGadgets() {
        final Window modal = new Window("Seleccionar Gadget", uiSkin);
        modal.setModal(true);
        modal.setMovable(false);

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
            Button btn = new Button(uiSkin);
            Image img = new Image(getGadgetIcon(id));
            btn.add(img).size(40, 40);
            if (id.equals(equipped)) btn.setChecked(true);

            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SaveManager.setEquippedGadget(id);
                    updateEquippedGadgetIcon();
                    modal.remove();
                }
            });

            grid.add(btn).size(55, 55).pad(10);
            col++;
            if (col >= 3) { grid.row(); col = 0; }
        }

        TextButton btnCerrar = new TextButton("Cerrar", uiSkin);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { modal.remove(); }
        });

        modal.add(grid).pad(15).row();
        modal.add(btnCerrar).padTop(10).padBottom(10).width(120);
        modal.pack();
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
}
