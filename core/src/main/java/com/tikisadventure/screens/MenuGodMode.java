package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.core.GameSession;

public class MenuGodMode {

    private final Stage stage;
    private final Skin uiSkin;

    private TextButton customGodButton;
    private Dialog customGodDialog;

    public MenuGodMode(Stage stage, Skin uiSkin) {
        this.stage = stage;
        this.uiSkin = uiSkin;
        crearVentanaModoDios();
    }

    public void inyectarInterfaz(Table tablaDestino) {
        CheckBox godModeCheck = new CheckBox("MODO DIOS", uiSkin);
        godModeCheck.setChecked(GameSession.godMode); // Recordar el estado si vuelves al menú

        customGodButton = new TextButton("Parametros", uiSkin);
        customGodButton.setVisible(GameSession.godMode); // Solo visible si está activado

        godModeCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godMode = godModeCheck.isChecked();
                customGodButton.setVisible(GameSession.godMode);
                System.out.println("Modo Dios activado: " + GameSession.godMode);
            }
        });

        customGodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.show(stage);
            }
        });

        // Elementos a la tabla que nos pasa la pantalla padre MenuMapScreen.java
        tablaDestino.row();
        tablaDestino.add(godModeCheck).left().bottom().pad(10);
        tablaDestino.row();
        tablaDestino.add(customGodButton).left().pad(10);
    }

    private void crearVentanaModoDios() {
        customGodDialog = new Dialog("Parametros", uiSkin);
        customGodDialog.setModal(true);
        customGodDialog.setMovable(false);
        customGodDialog.getContentTable().add().minSize(400, 250);

        TextButton closeButton = new TextButton("X", uiSkin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.hide();
            }
        });
        customGodDialog.getTitleTable().add(closeButton).size(30, 30).padRight(8);

        //Buscamos las armas en el json
        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        Array<String> weaponNames = new Array<>();
        final ObjectMap<String, String> weaponNameToIdMap = new ObjectMap<>();

        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            String weaponId = weaponEntry.name;

            if (weaponId.contains("Plantilla")) {
                continue;
            }
            String displayName = weaponEntry.getString("name", weaponId);
            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        final SelectBox<String> weaponSelector = new SelectBox<>(uiSkin);
        weaponSelector.setItems(weaponNames);

        weaponSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeWeaponId = weaponNameToIdMap.get(weaponSelector.getSelected());
            }
        });
        GameSession.godModeWeaponId = weaponNameToIdMap.get(weaponSelector.getSelected());

        //Buscamos las habilidades en el json
        JsonValue abilityData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));
        Array<String> abilityNames = new Array<>();
        final ObjectMap<String, String> abilityNameToIdMap = new ObjectMap<>();

        for (JsonValue abilityEntry : abilityData) {
            String abilityId = abilityEntry.name;
            String displayName = abilityEntry.getString("name", abilityId);
            abilityNames.add(displayName);
            abilityNameToIdMap.put(displayName, abilityId);
        }

        final SelectBox<String> ability1Selector = new SelectBox<>(uiSkin);
        ability1Selector.setItems(abilityNames);

        final SelectBox<String> ability2Selector = new SelectBox<>(uiSkin);
        ability2Selector.setItems(abilityNames);

        ability1Selector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeAbility1Id = abilityNameToIdMap.get(ability1Selector.getSelected());
            }
        });

        ability2Selector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeAbility2Id = abilityNameToIdMap.get(ability2Selector.getSelected());
            }
        });

        GameSession.godModeAbility1Id = abilityNameToIdMap.get(ability1Selector.getSelected());
        GameSession.godModeAbility2Id = abilityNameToIdMap.get(ability2Selector.getSelected());

        //Montaje de ventana de parametros del modo dios
        customGodDialog.getContentTable().clear();
        customGodDialog.getContentTable().add(new Label("Arma inicial:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(weaponSelector).width(250).row();
        customGodDialog.getContentTable().add(new Label("Habilidad 1:", uiSkin)).padRight(10).padTop(10).right();
        customGodDialog.getContentTable().add(ability1Selector).width(250).padTop(10).row();
        customGodDialog.getContentTable().add(new Label("Habilidad 2:", uiSkin)).padRight(10).padTop(10).right();
        customGodDialog.getContentTable().add(ability2Selector).width(250).padTop(10).row();
    }
}
