package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.ui.DeleteWeaponUI;

public class MenuGodMode {

    private final Stage stage;
    private final Skin uiSkin;

    private TextButton customGodButton;
    private Dialog customGodDialog;

    private SelectBox<String>[] weaponSelectors;
    private ObjectMap<String, String> weaponNameToIdMap = new ObjectMap<>();

    public MenuGodMode(Stage stage, Skin uiSkin) {
        this.stage = stage;
        this.uiSkin = uiSkin;
        GameSession.loadCustomWeapons();
        crearVentanaModoDios();
    }

    public void inyectarInterfaz(Table tablaDestino) {
        CheckBox godModeCheck = new CheckBox("MODO DIOS", uiSkin);
        godModeCheck.addListener(new Assets.HoverCursorListener());
        godModeCheck.setChecked(GameSession.godMode);

        customGodButton = new TextButton("Parametros", uiSkin);
        customGodButton.setVisible(GameSession.godMode);

        // --- BOTONES CON FONDO GRIS Y SPRITE DENTRO ---
        TextureRegion texCreate = Assets.getRegion("shared", "UI_assets/CreateWeapon");
        TextureRegion texDelete = Assets.getRegion("shared", "UI_assets/DeleteWeapon");

        if (texCreate == null) texCreate = Assets.getRegion("shared", "UI_assets/UI_Crosshair");
        if (texDelete == null) texDelete = Assets.getRegion("shared", "UI_assets/UI_Crosshair");

        // Botón Crear
        final Button btnCrearArma = new Button(uiSkin); // Esto le pone el fondo gris estándar
        Image imgCreate = new Image(texCreate);
        imgCreate.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        btnCrearArma.add(imgCreate).size(28, 28).expand().center(); // Tamaño de la imagen interior

        // Botón Borrar
        final Button btnBorrarArma = new Button(uiSkin); // Fondo gris estándar
        Image imgDelete = new Image(texDelete);
        imgDelete.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        btnBorrarArma.add(imgDelete).size(28, 28).expand().center(); // Tamaño de la imagen interior

        btnCrearArma.setVisible(GameSession.godMode);
        btnBorrarArma.setVisible(GameSession.godMode);

        godModeCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godMode = godModeCheck.isChecked();
                customGodButton.setVisible(GameSession.godMode);
                btnCrearArma.setVisible(GameSession.godMode);
                btnBorrarArma.setVisible(GameSession.godMode);
            }
        });

        customGodButton.addListener(new Assets.HoverCursorListener());
        customGodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.show(stage);
            }
        });

        btnCrearArma.addListener(new Assets.HoverCursorListener());
        btnCrearArma.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MenuCustomGun.mostrar(stage, uiSkin, new MenuCustomGun.OnCustomWeaponSaved() {
                    @Override
                    public void onSaved() {
                        actualizarDesplegablesArmas();
                    }
                });
            }
        });

        btnBorrarArma.addListener(new Assets.HoverCursorListener());
        btnBorrarArma.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new DeleteWeaponUI(uiSkin, stage, new Runnable() {
                    @Override
                    public void run() {
                        actualizarDesplegablesArmas();
                    }
                }).show();
            }
        });

        // Empaquetamos los dos botones cuadrados en una sub-tabla
        Table botonesCustomTable = new Table();
        // Aquí definimos el tamaño del botón exterior (40x40)
        botonesCustomTable.add(btnCrearArma).size(40, 40).padRight(10);
        botonesCustomTable.add(btnBorrarArma).size(40, 40);

        tablaDestino.row();
        tablaDestino.add(godModeCheck).left().bottom().pad(10);
        tablaDestino.row();
        tablaDestino.add(customGodButton).left().padLeft(10);
        tablaDestino.row();
        // Añadimos la sub-tabla
        tablaDestino.add(botonesCustomTable).left().padLeft(10).padTop(5);
    }

    // ... EL RESTO DE LA CLASE SIGUE IGUAL (crearVentanaModoDios y actualizarDesplegablesArmas) ...
    @SuppressWarnings("unchecked")
    private void crearVentanaModoDios() {
        customGodDialog = new Dialog("Parametros", uiSkin);
        customGodDialog.setModal(true);
        customGodDialog.setMovable(false);
        customGodDialog.getContentTable().add().minSize(400, 250);

        TextButton closeButton = new TextButton("X", uiSkin);
        closeButton.addListener(new Assets.HoverCursorListener());
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.hide();
            }
        });
        customGodDialog.getTitleTable().add(closeButton).size(30, 30).padRight(8);


        // --- ARMAS ---
        weaponSelectors = new SelectBox[6];
        Table tablaArmas = new Table();

        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        Array<String> weaponNames = new Array<>();
        weaponNameToIdMap.clear();

        weaponNames.add("- Sin arma -");
        weaponNameToIdMap.put("- Sin arma -", "");

        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            String weaponId = weaponEntry.name;
            if (weaponId.contains("Plantilla")) continue;

            String displayName = weaponEntry.getString("name", weaponId);
            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        for (GameSession.CustomWeaponConfig custom : GameSession.customWeapons.values()) {
            String displayCustomName = custom.name + " [C]";
            weaponNames.add(displayCustomName);
            weaponNameToIdMap.put(displayCustomName, custom.id);
        }

        for (int i = 0; i < 6; i++) {
            weaponSelectors[i] = new SelectBox<>(uiSkin);
            weaponSelectors[i].setMaxListCount(6);
            weaponSelectors[i].setItems(weaponNames);

            if (i == 0) {
                weaponSelectors[i].setSelectedIndex(1);
            } else {
                weaponSelectors[i].setSelectedIndex(0);
            }

            final int index = i;
            weaponSelectors[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String selectedName = weaponSelectors[index].getSelected();
                    String selectedId = weaponNameToIdMap.get(selectedName);
                    GameSession.godModeWeapons[index] = selectedId;
                }
            });

            GameSession.godModeWeapons[i] = weaponNameToIdMap.get(weaponSelectors[i].getSelected());

            tablaArmas.add(new Label("Arma " + (i + 1) + ":", uiSkin)).padRight(5).right();
            tablaArmas.add(weaponSelectors[i]).width(140).padRight(15).padBottom(5);
            if (i % 2 == 1) tablaArmas.row();
        }

        // --- MULTIPLICADOR DE DAÑO ---
        final ObjectMap<String, Float> multiplicadoresMap = new ObjectMap<>();
        multiplicadoresMap.put("x0.25", 0.25f);
        multiplicadoresMap.put("x0.5", 0.5f);
        multiplicadoresMap.put("x1.0 (Normal)", 1.0f);
        multiplicadoresMap.put("x1.5", 1.5f);
        multiplicadoresMap.put("x2.0", 2.0f);
        multiplicadoresMap.put("x3.0", 3.0f);
        multiplicadoresMap.put("x5.0", 5.0f);
        multiplicadoresMap.put("x10.0", 10.0f);

        final SelectBox<String> damageSelector = new SelectBox<>(uiSkin);
        damageSelector.setItems("x0.25", "x0.5", "x1.0 (Normal)", "x1.5", "x2.0", "x3.0", "x5.0", "x10.0");
        damageSelector.setSelected("x1.0 (Normal)");

        damageSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeDamageMultiplier = multiplicadoresMap.get(damageSelector.getSelected());
            }
        });
        GameSession.godModeDamageMultiplier = multiplicadoresMap.get(damageSelector.getSelected());

        // --- VIDA PERSONAJE ---
        final SelectBox<String> healthSelector = new SelectBox<>(uiSkin);
        healthSelector.setItems("1", "25", "50", "100", "200", "500", "1000", "Inmortal");
        healthSelector.setSelected("100");

        healthSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selection = healthSelector.getSelected();
                if (selection.equalsIgnoreCase("Inmortal")) {
                    GameSession.godModeIsImmortal = true;
                    GameSession.godModeHealthValue = 9999f;
                } else {
                    GameSession.godModeIsImmortal = false;
                    GameSession.godModeHealthValue = Float.parseFloat(selection);
                }
            }
        });
        GameSession.godModeHealthValue = 100f;
        GameSession.godModeIsImmortal = false;

        // --- VELOCIDAD DEL PERSONAJE ---
        final SelectBox<String> speedSelector = new SelectBox<>(uiSkin);
        speedSelector.setItems("1", "3", "5", "7", "10", "15", "30");
        speedSelector.setSelected("5");

        speedSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeSpeedValue = Float.parseFloat(speedSelector.getSelected());
            }
        });
        GameSession.godModeSpeedValue = 5.0f;

        // --- GADGETS (Habilidad 2) ---
        JsonValue abilityData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));
        Array<String> gadgetNames = new Array<>();
        final ObjectMap<String, String> gadgetNameToIdMap = new ObjectMap<>();

        for (JsonValue abilityEntry : abilityData) {
            String abilityId = abilityEntry.name;
            if (abilityId.toLowerCase().contains("dash")) continue;

            String displayName = abilityEntry.getString("name", abilityId);
            gadgetNames.add(displayName);
            gadgetNameToIdMap.put(displayName, abilityId);
        }

        final SelectBox<String> gadgetSelector = new SelectBox<>(uiSkin);
        gadgetSelector.setItems(gadgetNames);
        gadgetSelector.setMaxListCount(6);
        gadgetSelector.setHeight(30);

        gadgetSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeAbility2Id = gadgetNameToIdMap.get(gadgetSelector.getSelected());
            }
        });

        GameSession.godModeAbility1Id = null;
        GameSession.godModeAbility2Id = gadgetNameToIdMap.get(gadgetSelector.getSelected());

        // --- MONTAJE VENTANA PARAMETROS ---
        customGodDialog.getContentTable().clear();

        customGodDialog.getContentTable().add(tablaArmas).colspan(2).padBottom(15).row();

        customGodDialog.getContentTable().add(new Label("Gadget:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(gadgetSelector).width(200).padTop(5).padBottom(10).left().row();

        customGodDialog.getContentTable().add(new Label("Damage:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(damageSelector).width(150).padBottom(5).left().row();

        customGodDialog.getContentTable().add(new Label("Vida:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(healthSelector).width(150).padBottom(5).left().row();

        customGodDialog.getContentTable().add(new Label("Velocidad:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(speedSelector).width(150).padBottom(5).left().row();
    }

    @SuppressWarnings("unchecked")
    private void actualizarDesplegablesArmas() {
        if (weaponSelectors == null) return;

        Array<String> weaponNames = new Array<>();
        weaponNameToIdMap.clear();

        weaponNames.add("- Sin arma -");
        weaponNameToIdMap.put("- Sin arma -", "");

        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            String weaponId = weaponEntry.name;
            if (weaponId.contains("Plantilla")) continue;

            String displayName = weaponEntry.getString("name", weaponId);
            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        for (GameSession.CustomWeaponConfig custom : GameSession.customWeapons.values()) {
            String displayCustomName = custom.name + " [C]";
            weaponNames.add(displayCustomName);
            weaponNameToIdMap.put(displayCustomName, custom.id);
        }

        for (int i = 0; i < 6; i++) {
            String seleccionPrevia = weaponSelectors[i].getSelected();
            weaponSelectors[i].setItems(weaponNames);

            if (seleccionPrevia != null && weaponNames.contains(seleccionPrevia, false)) {
                weaponSelectors[i].setSelected(seleccionPrevia);
            } else {
                if (i == 0) weaponSelectors[i].setSelectedIndex(1);
                else weaponSelectors[i].setSelectedIndex(0);
            }
        }
    }
}
