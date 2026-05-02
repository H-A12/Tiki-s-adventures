package com.tikisadventure.core;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class GameSession {
    public static String selectedCharacterId = "TIKI";
    public static String selectedMapName = "bosque";

    public static boolean godMode = false;

    public static String[] godModeWeapons = new String[6];

    public static String godModeAbility1Id = null;
    public static String godModeAbility2Id = null;

    public static float godModeDamageMultiplier = 1.0f;

    public static float godModeHealthValue = 100f;
    public static boolean godModeIsImmortal = false;

    public static float godModeSpeedValue = 5.0f;

    //ESTRUCTURA PARA ARMAS CUSTOM
    public static class CustomWeaponConfig {
        public String id;
        public String name;
        public float damage;
        public float cd;
        public String damageType;
        public float critChance;
        public String sprite;
        public String projectileSprite;
        public String bulletBehavior;
        public String bulletEffect;
        public int penetration;
    }
    // Aquí guardaremos las armas creadas en caliente
    public static com.badlogic.gdx.utils.ObjectMap<String, CustomWeaponConfig> customWeapons = new com.badlogic.gdx.utils.ObjectMap<>();

    //Metodos de guardado/cargado de armas custom
    public static void saveCustomWeapons() {
        com.badlogic.gdx.utils.Json json = new com.badlogic.gdx.utils.Json();

        json.setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json);
        json.setTypeName(null);

        FileHandle file = Gdx.files.local("Saves/Weapons/custom_weapons.json");

        // --- ¡LA LÍNEA MÁGICA! ---
        // Esto le dice a LibGDX: "Si no existen las carpetas Saves/Weapons, créalas"
        file.parent().mkdirs();

        try {
            file.writeString(json.prettyPrint(customWeapons), false); // prettyPrint lo hace legible
            System.out.println("Armas custom guardadas localmente en Saves/Weapons/");
        } catch (Exception e) {
            Gdx.app.error("GameSession", "Error al escribir el archivo de armas custom", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadCustomWeapons() {
        FileHandle file = Gdx.files.local("Saves/Weapons/custom_weapons.json");
        if (file.exists()) {
            com.badlogic.gdx.utils.Json json = new com.badlogic.gdx.utils.Json();

            json.setTypeName(null);

            try {
                customWeapons = json.fromJson(ObjectMap.class, CustomWeaponConfig.class, file.readString());

                if (customWeapons == null) {
                    customWeapons = new ObjectMap<>();
                }

                System.out.println("Armas custom cargadas: " + customWeapons.size);
            } catch (Exception e) {
                Gdx.app.error("GameSession", "Error al cargar las armas custom", e);
                customWeapons = new ObjectMap<>(); // Si falla, evitamos crasheos
            }
        } else {
            // Si el archivo no existe, inicializamos el mapa para evitar NullPointerExceptions
            customWeapons = new ObjectMap<>();
            System.out.println("No hay archivo de armas custom. Iniciando mapa vacío.");
        }
    }
}
