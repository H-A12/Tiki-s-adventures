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
        final TextField penetrationField = new TextField("0", skin);

        final SelectBox<String> typeBox = new SelectBox<>(skin);
        typeBox.setItems("KINETIC", "EXPLOSIVE", "ENERGY", "FIRE", "POISON", "ICE");
        typeBox.setMaxListCount(6);

        // --- Mapeado skins armas ---
        final ObjectMap<String, String> spriteMap = new ObjectMap<>();
        spriteMap.put("Pistola", "weapons_assets/Handgun");
        spriteMap.put("Fusil de bolas", "weapons_assets/BallRifle");
        spriteMap.put("Pirocohete", "weapons_assets/RocketLauncher");
        spriteMap.put("Escupepalillos", "weapons_assets/ToothpickShotgun");
        spriteMap.put("Clavolleta", "weapons_assets/NailGun");
        spriteMap.put("Lanzadiscos", "weapons_assets/DiscLauncher");
        spriteMap.put("Lanzapelotas", "weapons_assets/TennisLauncher");
        spriteMap.put("Extintor trucado", "weapons_assets/Extinguisher");
        spriteMap.put("Triturahielo", "weapons_assets/IceGrinder");
        spriteMap.put("Putripez", "weapons_assets/RottenFish");
        spriteMap.put("Banana", "weapons_assets/Banana");
        spriteMap.put("Saxofon", "weapons_assets/Saxophone");
        spriteMap.put("Arma laser", "weapons_assets/LaserGun");
        spriteMap.put("Espada", "weapons_assets/Sword");

        final SelectBox<String> spriteBox = new SelectBox<>(skin);
        spriteBox.setItems(
            "Pistola", "Fusil de bolas", "Pirocohete", "Escupepalillos",
            "Clavolleta", "Lanzadiscos", "Lanzapelotas", "Extintor trucado",
            "Triturahielo", "Putripez", "Banana", "Saxofon", "Arma laser", "Espada"
        );
        spriteBox.setSelected("Pistola");
        spriteBox.setMaxListCount(6);

        // --- Mapeado skins balas ---
        final ObjectMap<String, String> projectileMap = new ObjectMap<>();
        projectileMap.put("Bala gris", "particle_assets/GrayBullet");
        projectileMap.put("Bala verde", "particle_assets/GreenBullet");
        projectileMap.put("Bala roja", "particle_assets/RedBullet");
        projectileMap.put("Bala blanca", "particle_assets/WhiteBullet");
        projectileMap.put("Bala amarilla", "particle_assets/YellowBullet");
        projectileMap.put("Bala azul", "particle_assets/BlueBullet");
        projectileMap.put("Laser azul", "particle_assets/BlueLaser");
        projectileMap.put("Casquillo amarillo", "particle_assets/BulletCasing");
        projectileMap.put("Sierra", "particle_assets/SawBullet");
        projectileMap.put("Casquillo rojo", "particle_assets/ShotgunCasing");
        projectileMap.put("Cortocircuito", "particle_assets/SparkBullet");
        projectileMap.put("Palillo", "particle_assets/ToothpickBullet");
        projectileMap.put("Pelota de tenis", "particle_assets/TennisBullet");
        projectileMap.put("Palomita", "particle_assets/popcorn");
        projectileMap.put("Escarcha", "particle_assets/IceBullet");
        projectileMap.put("Llamarada", "particle_assets/FlameBullet");
        projectileMap.put("Nota musical", "particle_assets/MusicNote");
        projectileMap.put("Disco", "particle_assets/Disc");
        projectileMap.put("Petardo", "particle_assets/RocketBullet");
        projectileMap.put("Pua de pez", "particle_assets/SpikeFish");
        projectileMap.put("Banana", "weapons_assets/Banana");
        projectileMap.put("Piedra", "particle_assets/Ground_pebbles");
        projectileMap.put("Bola de pelo", "particle_assets/TurretBullet");

        final SelectBox<String> projectileBox = new SelectBox<>(skin);
        projectileBox.setItems(
            "Bala gris", "Bala verde", "Bala roja", "Bala blanca",
            "Bala amarilla", "Bala azul", "Laser azul", "Casquillo amarillo",
            "Sierra", "Casquillo rojo", "Cortocircuito", "Palillo",
            "Pelota de tenis", "Palomita", "Escarcha", "Llamarada",
            "Nota musical", "Disco", "Petardo", "Pua de pez",
            "Banana", "Piedra", "Bola de pelo"
        );
        projectileBox.setSelected("Bala gris");
        projectileBox.setMaxListCount(6);

        // --- NUEVO: Efecto de Estado ---
        final SelectBox<String> effectBox = new SelectBox<>(skin);
        effectBox.setItems("Ninguno", "Quemadura", "Veneno", "Congelacion");
        effectBox.setSelected("Ninguno");
        effectBox.setMaxListCount(4);

        // --- Mapeo de comportamiento (Behavior) ---
        final SelectBox<String> behaviorBox = new SelectBox<>(skin);
        behaviorBox.setItems("Normal", "Rebote", "Zigzag", "Perdigones", "Explosiva", "Cadena", "Boomerang", "Triple");
        behaviorBox.setSelected("Normal");
        behaviorBox.setMaxListCount(6);


        // --- LÓGICA DE AUTO-RELLENADO DE PENETRACIÓN ---
        behaviorBox.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                String selected = behaviorBox.getSelected();

                switch (selected) {
                    case "Boomerang":
                        penetrationField.setText("999");
                        break;
                    case "Rebote":
                        penetrationField.setText("5");
                        break;
                    case "Cadena":
                        penetrationField.setText("3");
                        break;
                    case "Explosiva":
                    case "Normal":
                    case "Zigzag":
                    case "Perdigones":
                    case "Triple":
                        penetrationField.setText("0");
                        break;
                }
            }
        });

        Table content = dialog.getContentTable();
        content.pad(20);

        // FILA 1: Nombre y Penetración (Estadísticas de estructura)
        content.add(new Label("Nombre:", skin)).right().padRight(10);
        content.add(nameField).width(120).left();
        content.add(new Label("Penetración:", skin)).right().padRight(10).padLeft(15);
        content.add(penetrationField).width(45).left().row();

        // FILA 2: Skins (Estética)
        content.add(new Label("Skin Arma:", skin)).right().padRight(10).padTop(10);
        content.add(spriteBox).width(130).padTop(10).padRight(20);
        content.add(new Label("Skin Bala:", skin)).right().padRight(10).padTop(10);
        content.add(projectileBox).width(130).padTop(10).row();

        // FILA 3: Daño y Tipo (Poder base)
        content.add(new Label("Daño:", skin)).right().padRight(10).padTop(10);
        content.add(damageField).width(130).padTop(10).padRight(20);
        content.add(new Label("Tipo Daño:", skin)).right().padRight(10).padTop(10);
        content.add(typeBox).width(130).padTop(10).row();

        // FILA 4: Modificadores (Efectos especiales)
        content.add(new Label("Efecto:", skin)).right().padRight(10).padTop(10);
        content.add(effectBox).width(130).padTop(10).padRight(20);
        content.add(new Label("Movimiento:", skin)).right().padRight(10).padTop(10);
        content.add(behaviorBox).width(130).padTop(10).row();

        // FILA 5: Cadencia y Crítico (Rendimiento)
        content.add(new Label("Cd (FPS):", skin)).right().padRight(10).padTop(10);
        content.add(cdField).width(130).padTop(10).padRight(20);
        content.add(new Label("Crítico:", skin)).right().padRight(10).padTop(10);
        content.add(critField).width(130).padTop(10).row();

        TextButton btnGuardar = new TextButton("Guardar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        final Runnable ejecutarGuardado = new Runnable() {
            @Override
            public void run() {
                GameSession.CustomWeaponConfig conf = new GameSession.CustomWeaponConfig();
                conf.id = "custom_" + System.currentTimeMillis();
                conf.name = nameField.getText();
                conf.damageType = typeBox.getSelected();
                conf.sprite = spriteMap.get(spriteBox.getSelected());
                conf.projectileSprite = projectileMap.get(projectileBox.getSelected());

                // GUARDAMOS LOS DOS
                conf.bulletEffect = effectBox.getSelected();
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

                try {
                    conf.penetration = Integer.parseInt(penetrationField.getText());
                } catch (NumberFormatException e) {
                    conf.penetration = 0;
                }

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
                if (GameSession.customWeapons.size >= MAX_CUSTOM_WEAPONS) {
                    String mensajeAviso = "No puedes tener mas de " + MAX_CUSTOM_WEAPONS + " armas guardadas, elimina una para continuar.";
                    new DeleteWeaponUI(skin, stage, mensajeAviso, new Runnable() {
                        @Override
                        public void run() {
                            ejecutarGuardado.run();
                        }
                    }).show();
                } else {
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
