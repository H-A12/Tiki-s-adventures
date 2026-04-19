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
        godModeCheck.setChecked(GameSession.godMode); // Recordar el estado si vuelves al menú

        customGodButton = new TextButton("Parametros", uiSkin);
        customGodButton.setVisible(GameSession.godMode); // Solo visible si está activado

        // Declaramos el botón como final para que el listener pueda acceder a él
        final TextButton btnCrearArma = new TextButton("Crear Arma", uiSkin);
        btnCrearArma.setVisible(GameSession.godMode);

        godModeCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godMode = godModeCheck.isChecked();
                customGodButton.setVisible(GameSession.godMode);

                // --- ¡AQUÍ ESTABA EL ERROR! Faltaba esta línea: ---
                btnCrearArma.setVisible(GameSession.godMode);

                System.out.println("Modo Dios activado: " + GameSession.godMode);
            }
        });

        customGodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.show(stage);
            }
        });

        btnCrearArma.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MenuCustomGun.mostrar(stage, uiSkin, new MenuCustomGun.OnCustomWeaponSaved() {
                    @Override
                    public void onSaved() {
                        // Cuando cerramos la ventana de forja, actualizamos los desplegables
                        actualizarDesplegablesArmas();
                    }
                });
            }
        });

        // Elementos a la tabla que nos pasa la pantalla padre MenuMapScreen.java
        tablaDestino.row();
        tablaDestino.add(godModeCheck).left().bottom().pad(10);
        tablaDestino.row();
        tablaDestino.add(customGodButton).left().padLeft(10);
        tablaDestino.row();
        // Reducimos el pad superior para que quede pegadito a "Parametros"
        tablaDestino.add(btnCrearArma).left().padLeft(10).padTop(5);
    }

    @SuppressWarnings("unchecked")
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


        //ARMAS ---------------------------------------------------------------------------------

        // --- 1. Armas (Array de 6) ---
        weaponSelectors = new SelectBox[6];
        Table tablaArmas = new Table();

        // 1. Leemos el JSON y preparamos las opciones
        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        Array<String> weaponNames = new Array<>();
        weaponNameToIdMap.clear();

        //Opcion sin arma
        weaponNames.add("- Sin arma -");
        weaponNameToIdMap.put("- Sin arma -", ""); // <-- CORREGIDO: Ahora los espacios coinciden

        // Recorremos las armas del JSON
        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            String weaponId = weaponEntry.name;
            // Filtramos las plantillas
            if (weaponId.contains("Plantilla")) continue;

            String displayName = weaponEntry.getString("name", weaponId);
            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        // --- ¡NUEVO! LEEMOS LAS ARMAS CUSTOM CARGADAS DE LA SESIÓN ---
        for (GameSession.CustomWeaponConfig custom : GameSession.customWeapons.values()) {
            String displayCustomName = custom.name + " (Custom)";
            weaponNames.add(displayCustomName);
            weaponNameToIdMap.put(displayCustomName, custom.id);
        }

        // 2. Configuramos los 6 desplegables
        for (int i = 0; i < 6; i++) {
            weaponSelectors[i] = new SelectBox<>(uiSkin);
            weaponSelectors[i].setItems(weaponNames);

            // Por defecto, solo la primera lleva arma, el resto vacio
            if (i == 0) {
                weaponSelectors[i].setSelectedIndex(1);
            } else {
                weaponSelectors[i].setSelectedIndex(0);
            }

            final int index = i;
            weaponSelectors[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Guardado
                    String selectedName = weaponSelectors[index].getSelected();
                    String selectedId = weaponNameToIdMap.get(selectedName);
                    GameSession.godModeWeapons[index] = selectedId;
                }
            });

            // Guardar estado inicial en la sesión
            GameSession.godModeWeapons[i] = weaponNameToIdMap.get(weaponSelectors[i].getSelected());

            // Añadirlos a la tabla en 2 columnas
            tablaArmas.add(new Label("Arma " + (i + 1) + ":", uiSkin)).padRight(5).right();
            tablaArmas.add(weaponSelectors[i]).width(140).padRight(15).padBottom(5);
            if (i % 2 == 1) tablaArmas.row(); // Cada 2 armas, salto de línea
        }

        //MULTIPLICADOR DE DAÑO ---------------------------------------------------------------------------

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
        damageSelector.setSelected("x1.0 (Normal)"); // Por defecto no alteramos el daño

        damageSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeDamageMultiplier = multiplicadoresMap.get(damageSelector.getSelected());
            }
        });
        GameSession.godModeDamageMultiplier = multiplicadoresMap.get(damageSelector.getSelected());

        //VIDA PERSONAJE ---------------------------------------------------------------------------------------
        final SelectBox<String> healthSelector = new SelectBox<>(uiSkin);
        healthSelector.setItems("1", "25", "50", "100", "200", "500", "1000", "Inmortal");
        healthSelector.setSelected("100"); // Valor inicial equilibrado

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
        //Estandar
        GameSession.godModeHealthValue = 100f;
        GameSession.godModeIsImmortal = false;


        //VELOCIDAD DEL PERSONAJE ------------------------------------------------------------------------------
        final SelectBox<String> speedSelector = new SelectBox<>(uiSkin);
        speedSelector.setItems("1", "3", "5", "7", "10", "15", "30");
        speedSelector.setSelected("5"); // Seleccionamos 5 por defecto

        speedSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeSpeedValue = Float.parseFloat(speedSelector.getSelected());
            }
        });
        //Estandar
        GameSession.godModeSpeedValue = 5.0f;

        //HABILIDADES ----------------------------------------------------------------------------------------

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
        ability1Selector.setItems(abilityNames); // Lista completa

        final SelectBox<String> ability2Selector = new SelectBox<>(uiSkin);
        ability2Selector.setItems(abilityNames); // Lista completa

        for (int i = 0; i < abilityNames.size; i++) {
            String nombreHabilidad = abilityNames.get(i).toLowerCase();
            if (!nombreHabilidad.contains("dash") && i != ability1Selector.getSelectedIndex()) {
                ability2Selector.setSelectedIndex(i);
                break; // En cuanto encontramos una, paramos de buscar
            }
        }

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

        //MONTAJE VENTANA PARAMETROS ---------------------------------------------------------------------
        customGodDialog.getContentTable().clear();

        //Cuadricula de armas
        customGodDialog.getContentTable().add(tablaArmas).colspan(2).padBottom(15).row();

        //Cuadriculas de habilidades
        customGodDialog.getContentTable().add(new Label("Habilidad 1:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(ability1Selector).width(200).padTop(5).row();
        customGodDialog.getContentTable().add(new Label("Habilidad 2:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(ability2Selector).width(200).padTop(5).row();

        //Cuadriculas de daño
        customGodDialog.getContentTable().add(new Label("Damage:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(damageSelector).width(150).padBottom(5).left().row();

        //Cuadriculas de vida
        customGodDialog.getContentTable().add(new Label("Vida:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(healthSelector).width(150).padBottom(5).left().row();

        //Cuadriculas de velocidad
        customGodDialog.getContentTable().add(new Label("Velocidad:", uiSkin)).padRight(10).right();
        customGodDialog.getContentTable().add(speedSelector).width(150).padBottom(5).left().row();
    }

    @SuppressWarnings("unchecked")
    private void actualizarDesplegablesArmas() {
        if (weaponSelectors == null) return; // Por seguridad

        Array<String> weaponNames = new Array<>();
        weaponNameToIdMap.clear();

        weaponNames.add("- Sin arma -");
        weaponNameToIdMap.put("- Sin arma -", "");

        // Leer JSON
        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            String weaponId = weaponEntry.name;
            if (weaponId.contains("Plantilla")) continue;

            String displayName = weaponEntry.getString("name", weaponId);
            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        // Leer Armas Creadas en Caliente
        for (GameSession.CustomWeaponConfig custom : GameSession.customWeapons.values()) {
            String displayCustomName = custom.name + " (Custom)";
            weaponNames.add(displayCustomName);
            weaponNameToIdMap.put(displayCustomName, custom.id);
        }

        // Refrescar los 6 SelectBox
        for (int i = 0; i < 6; i++) {
            String seleccionPrevia = weaponSelectors[i].getSelected();
            weaponSelectors[i].setItems(weaponNames);

            // Intentar mantener la selección anterior si existe
            if (seleccionPrevia != null && weaponNames.contains(seleccionPrevia, false)) {
                weaponSelectors[i].setSelected(seleccionPrevia);
            } else {
                // Configuración por defecto si está limpio
                if (i == 0) weaponSelectors[i].setSelectedIndex(1); // Primer arma real
                else weaponSelectors[i].setSelectedIndex(0); // Sin arma
            }
        }
    }
}
