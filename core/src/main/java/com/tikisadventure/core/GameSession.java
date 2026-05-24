package com.tikisadventure.core;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

//Datos de la partida actual: personaje, mapa, modo dios, semilla aleatoria,
//monedas recogidas y armas personalizadas. Se usa desde varias partes del juego
//para saber qué hay seleccionado y en qué estado está la ejecución.
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

    public static long currentSeed = 0L;
    public static int coinsCollectedThisRun = 0;

    public static void generateNewSeed() {
        currentSeed = new java.util.Random().nextLong();
    }

    public static java.util.Random getSeededRandomForStage(int stage) {
        return new java.util.Random(currentSeed + stage);
    }

    //Estructura para armas custom
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
    public static com.badlogic.gdx.utils.ObjectMap<String, CustomWeaponConfig> customWeapons = new com.badlogic.gdx.utils.ObjectMap<>();

    //Guardar armas custom en disco
    public static void saveCustomWeapons() {
        com.badlogic.gdx.utils.Json json = new com.badlogic.gdx.utils.Json();

        json.setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json);
        json.setTypeName(null);

        FileHandle file = Gdx.files.local("Saves/Weapons/custom_weapons.json");

        //Crear carpeta si no existe
        file.parent().mkdirs();

        try {
            file.writeString(json.prettyPrint(customWeapons), false); // prettyPrint lo hace legible
            System.out.println("Armas custom guardadas localmente en Saves/Weapons/");
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unchecked")
    //Cargar armas custom desde disco
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
                customWeapons = new ObjectMap<>();
            }
        } else {
            // Si el archivo no existe, inicializamos el mapa para evitar NullPointerExceptions
            customWeapons = new ObjectMap<>();
            System.out.println("No hay archivo de armas custom. Iniciando mapa vacío.");
        }
    }
}
