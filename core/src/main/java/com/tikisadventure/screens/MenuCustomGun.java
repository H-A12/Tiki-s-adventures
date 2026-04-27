package com.tikisadventure.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.ui.DeleteWeaponUI;

public class MenuCustomGun {

    public static int MAX_CUSTOM_WEAPONS = 10;

    public interface OnCustomWeaponSaved {
        void onSaved();
    }

    public static void mostrar(Stage stage, final Skin skin, final OnCustomWeaponSaved callback) {
        final Dialog dialog = new Dialog("Creador de armas", skin);
        dialog.setModal(true);
        dialog.setMovable(true);

        int randomCustomName = com.badlogic.gdx.math.MathUtils.random(100, 9999);
        final TextField nameField = new TextField("Custom" + randomCustomName, skin);
        final TextField damageField = new TextField("10", skin);
        final TextField cdField = new TextField("0.5", skin);
        final TextField critField = new TextField("0.1", skin);

        final SelectBox<String> typeBox = new SelectBox<>(skin);
        typeBox.setItems("KINETIC", "ENERGY", "FIRE", "POISON");

        // --- Mapeado skins armas ---
        final ObjectMap<String, String> spriteMap = new ObjectMap<>();
        spriteMap.put("Pistola", "weapons_assets/Handgun");
        spriteMap.put("Fusil", "weapons_assets/Machinegun");
        spriteMap.put("Arma laser", "weapons_assets/LaserGun");
        spriteMap.put("Lanzacohetes", "weapons_assets/RocketLauncher");
        spriteMap.put("Lanzasierras", "weapons_assets/SawGun");
        spriteMap.put("Espada", "weapons_assets/Sword");
        spriteMap.put("Escopeta", "weapons_assets/Shotgun");
        spriteMap.put("Extintor", "weapons_assets/Extinguisher");
        spriteMap.put("Pez Globo", "weapons_assets/Pufferfish");
        spriteMap.put("Banana", "weapons_assets/Banana");

        final SelectBox<String> spriteBox = new SelectBox<>(skin);
        spriteBox.setItems("Pistola", "Fusil", "Arma laser", "Lanzacohetes", "Lanzasierras", "Espada", "Escopeta", "Extintor", "Pez Globo", "Banana");
        spriteBox.setSelected("Pistola");

        // --- Mapeado skins balas ---
        final ObjectMap<String, String> projectileMap = new ObjectMap<>();
        projectileMap.put("Bala gris", "particle_assets/GrayBullet");
        projectileMap.put("Bala verde", "particle_assets/GreenBullet");
        projectileMap.put("Bala roja", "particle_assets/RedBullet");
        projectileMap.put("Bala blanca", "particle_assets/WhiteBullet");
        projectileMap.put("Bala amarilla", "particle_assets/YellowBullet");
        projectileMap.put("Bala azul", "particle_assets/BlueBullet");
        projectileMap.put("Laser azul", "particle_assets/BlueLaser");
        projectileMap.put("Casquillo", "particle_assets/BulletCasing");
        projectileMap.put("Sierra", "particle_assets/SawBullet");
        projectileMap.put("Misil", "particle_assets/RocketBullet");
        projectileMap.put("SpikeFish", "particle_assets/SpikeFish");
        projectileMap.put("Banana", "weapons_assets/Banana");
        projectileMap.put("Jalapeño", "particle_assets/Jalapeno");
        projectileMap.put("Piedras", "particle_assets/Ground_pebbles");
        projectileMap.put("Chicle", "particle_assets/MintGum");
        projectileMap.put("Refresco", "particle_assets/ShakedCola");

        final SelectBox<String> projectileBox = new SelectBox<>(skin);
        projectileBox.setItems("Bala gris", "Bala verde", "Bala roja", "Bala blanca", "Bala amarilla", "Bala azul", "Laser azul", "Casquillo", "Sierra", "Misil", "SpikeFish", "Banana", "Jalapeño", "Piedras", "Chicle", "Refresco");
        projectileBox.setSelected("Bala gris");

        // --- Mapeo de comportamiento (Behavior) ---
        final SelectBox<String> behaviorBox = new SelectBox<>(skin);
        behaviorBox.setItems("Normal", "Rebote", "Zigzag", "Perdigones", "Explosiva", "Cadena", "Boomerang", "Triple");
        behaviorBox.setSelected("Normal");

        Table content = dialog.getContentTable();
        content.pad(20);

        // Fila nombre
        content.add(new Label("Nombre:", skin)).right().padRight(10);
        content.add(nameField).width(150).left().colspan(3).row();

        // Fila skins
        content.add(new Label("Skin Arma:", skin)).right().padRight(10).padTop(10);
        content.add(spriteBox).width(130).padTop(10).padRight(20);

        content.add(new Label("Skin Bala:", skin)).right().padRight(10).padTop(10);
        content.add(projectileBox).width(130).padTop(10).row();

        // Fila daño/tipo daño
        content.add(new Label("Damage:", skin)).right().padRight(10).padTop(10);
        content.add(damageField).width(130).padTop(10).padRight(20);

        content.add(new Label("Damage Type:", skin)).right().padRight(10).padTop(10);
        content.add(typeBox).width(130).padTop(10).row();

        // Fila tipo bala
        content.add(new Label("Modificador:", skin)).right().padRight(10).padTop(10);
        content.add(behaviorBox).width(150).padTop(10).left().colspan(3).row();

        // Fila cd (cadencia)/crítico
        content.add(new Label("Cd:", skin)).right().padRight(10).padTop(10);
        content.add(cdField).width(130).padTop(10).padRight(20);

        content.add(new Label("Critico:", skin)).right().padRight(10).padTop(10);
        content.add(critField).width(130).padTop(10).row();

        TextButton btnGuardar = new TextButton("Guardar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        // --- MAGIA UX: EXTRAEMOS EL GUARDADO A UN BLOQUE REUTILIZABLE ---
        final Runnable ejecutarGuardado = new Runnable() {
            @Override
            public void run() {
                GameSession.CustomWeaponConfig conf = new GameSession.CustomWeaponConfig();
                conf.id = "custom_" + System.currentTimeMillis();
                conf.name = nameField.getText();
                conf.damageType = typeBox.getSelected();
                conf.sprite = spriteMap.get(spriteBox.getSelected());
                conf.projectileSprite = projectileMap.get(projectileBox.getSelected());
                conf.bulletBehavior = behaviorBox.getSelected();

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

                GameSession.customWeapons.put(conf.id, conf);
                GameSession.saveCustomWeapons();

                String currentUser = com.tikisadventure.core.SaveManager.getLastUsername();
                if (currentUser != null && !currentUser.isEmpty()) {
                    long coins = com.tikisadventure.core.SaveManager.getProfileData().coins;
                    long score = com.tikisadventure.core.SaveManager.getProfileData().totalScore;
                    new com.tikisadventure.database.progress.ProgressRepository()
                        .actualizarProgreso(currentUser, coins, score, null);
                }

                dialog.hide();
                if (callback != null) callback.onSaved();
            }
        };

        btnGuardar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                // Si hemos alcanzado el límite...
                if (GameSession.customWeapons.size >= MAX_CUSTOM_WEAPONS) {
                    String mensajeAviso = "No puedes tener mas de " + MAX_CUSTOM_WEAPONS + " armas guardadas, elimina una para continuar.";

                    // Abrimos la ventana de borrado y le damos el bloque "ejecutarGuardado"
                    // para que lo lance en cuanto acabe de hacer hueco.
                    new DeleteWeaponUI(skin, stage, mensajeAviso, new Runnable() {
                        @Override
                        public void run() {
                            // Cuando el jugador borre un arma y quede hueco, ¡guardamos la nueva automáticamente!
                            ejecutarGuardado.run();
                        }
                    }).show();

                } else {
                    // Si hay hueco de sobra, guardamos del tirón
                    ejecutarGuardado.run();
                }
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
