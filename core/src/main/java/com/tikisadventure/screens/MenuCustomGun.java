package com.tikisadventure.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.core.GameSession;

public class MenuCustomGun {

    public interface OnCustomWeaponSaved {
        void onSaved();
    }

    public static void mostrar(Stage stage, Skin skin, final OnCustomWeaponSaved callback) {
        final Dialog dialog = new Dialog("Creador de armas", skin);
        dialog.setModal(true);
        dialog.setMovable(true);

        // Campos de texto para los valores numéricos y nombre
        final TextField nameField = new TextField("Custom", skin);
        final TextField damageField = new TextField("10", skin);
        final TextField cdField = new TextField("0.5", skin);
        final TextField critField = new TextField("0.1", skin);

        // Desplegable Tipo Daño
        final SelectBox<String> typeBox = new SelectBox<>(skin);
        typeBox.setItems("KINETIC", "ENERGY", "FIRE", "POISON");

        // --- NUEVO: Diccionario y Desplegable de Sprites ---
        final ObjectMap<String, String> spriteMap = new ObjectMap<>();
        spriteMap.put("Pistola", "Handgun");
        spriteMap.put("Fusil", "Machinegun");
        spriteMap.put("Arma laser", "LaserGun");
        spriteMap.put("Lanzacohetes", "RocketLauncher");
        spriteMap.put("Lanzasierras", "SawGun");
        spriteMap.put("Espada", "Sword");
        spriteMap.put("Escopeta", "Shotgun");

        final SelectBox<String> spriteBox = new SelectBox<>(skin);
        // ¡¡¡TIENEN QUE COINCIDIR!!!! exÁctamente con las claves del diccionario de arriba
        spriteBox.setItems("Pistola", "Fusil", "Arma laser", "Lanzacohetes", "Lanzasierras", "Espada", "Escopeta");
        spriteBox.setSelected("Pistola"); // Valor por defecto

        Table content = dialog.getContentTable();
        content.pad(20);

        content.add(new Label("Nombre:", skin)).right().padRight(10);
        content.add(nameField).width(150).row();

        // --- NUEVA FILA EN LA UI ---
        content.add(new Label("Skin:", skin)).right().padRight(10).padTop(5);
        content.add(spriteBox).width(150).padTop(5).row();

        content.add(new Label("Damage:", skin)).right().padRight(10).padTop(5);
        content.add(damageField).width(150).padTop(5).row();

        content.add(new Label("Cadencia:", skin)).right().padRight(10).padTop(5);
        content.add(cdField).width(150).padTop(5).row();

        content.add(new Label("Damage type:", skin)).right().padRight(10).padTop(5);
        content.add(typeBox).width(150).padTop(5).row();

        content.add(new Label("Critico:", skin)).right().padRight(10).padTop(5);
        content.add(critField).width(150).padTop(5).row();

        TextButton btnGuardar = new TextButton("Guardar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        btnGuardar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSession.CustomWeaponConfig conf = new GameSession.CustomWeaponConfig();
                conf.id = "custom_" + System.currentTimeMillis();
                conf.name = nameField.getText();
                conf.damageType = typeBox.getSelected();

                // --- NUEVO: Guardar el nombre del archivo sprite ---
                conf.sprite = spriteMap.get(spriteBox.getSelected());

                // Validación segura de números
                try { conf.damage = Float.parseFloat(damageField.getText()); }
                catch (NumberFormatException e) { conf.damage = 10f; }

                try { conf.cd = Float.parseFloat(cdField.getText()); }
                catch (NumberFormatException e) { conf.cd = 0.5f; }

                try {
                    float crit = Float.parseFloat(critField.getText());
                    if (crit > 1.0f) crit = 1.0f;
                    if (crit < 0.0f) crit = 0.0f;
                    conf.critChance = crit;
                } catch (NumberFormatException e) { conf.critChance = 0.05f; }

                // Guardar en la caché de la sesión
                GameSession.customWeapons.put(conf.id, conf);
                GameSession.saveCustomWeapons();

                dialog.hide();
                if (callback != null) callback.onSaved();
            }
        });

        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(btnGuardar).pad(10);
        dialog.getButtonTable().add(btnCancelar).pad(10);

        dialog.pack();
        dialog.show(stage);
    }
}
