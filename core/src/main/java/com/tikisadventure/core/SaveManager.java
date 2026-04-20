package com.tikisadventure.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SaveManager {

    // Nombre del archivo de guardado:
    private static final String SAVE_FILE = "profile_data.sav";
    //Clave de seguridad de acceso a los datos del perfil:
    private static final String SECRET_KEY = "T1k1Adv3ntur3K3y";

    private static PlayerData profileData;
    private static final Json json = new Json();

    //Puntos necesarios acumulados para desbloquear cada personaje
    private static int scoreUnlockMoko = 300;  //Cambiable
    private static int scoreUnlockZuki = 1000; //Cambiable
    public static int waveUnlockDesert = 3;  //Cambiable
    public static int waveUnlockCave = 5;   //Cambiable

    //Cargar datos al iniciar el juego
    public static void loadProfileData() {
        FileHandle file = Gdx.files.local(SAVE_FILE);

        if (file.exists()) {
            try {
                String encryptedText = file.readString();
                String decryptedJson = decrypt(encryptedText);
                profileData = json.fromJson(PlayerData.class, decryptedJson);
            } catch (Exception e) {
                Gdx.app.error("SaveManager", "Error cargando/desencriptando el guardado. Creando uno nuevo.", e);
                profileData = new PlayerData();
            }
        } else {
            // Si es la primera vez que juega
            profileData = new PlayerData();
        }
    }

    //Guardar datos:
    public static void saveProfileData() {
        if (profileData == null) return;

        try {
            String jsonString = json.toJson(profileData);
            String encryptedText = encrypt(jsonString);

            FileHandle file = Gdx.files.local(SAVE_FILE);
            file.writeString(encryptedText, false);

            Gdx.app.log("SaveManager", "Partida guardada con éxito.");
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error al guardar la partida.", e);
        }
    }

    //Añadir top 5 de puntuacion del perfil:
    public static void addScoreRankProfileData(int newScore) {
        if (profileData == null) loadProfileData();

        profileData.globalScore += newScore;

        profileData.topScores.add(newScore);
        profileData.topScores.sort();
        profileData.topScores.reverse();

        // Si hay más de 5 borramos los peores
        while (profileData.topScores.size > 5) {
            profileData.topScores.pop();
        }

        saveProfileData(); // Guardamos automáticamente
    }

    //Comprobar si los personajes están desbloqueados
    public static boolean isCharacterUnlocked(int characterIndex) {
        if (profileData == null) loadProfileData();

        switch (characterIndex) {
            case 1: return true; // Personaje 1 siempre desbloqueado
            case 2: return profileData.globalScore >= scoreUnlockMoko;
            case 3: return profileData.globalScore >= scoreUnlockZuki;
            default: return false;
        }
    }

    //Accesos rapidos a los datos
    public static PlayerData getProfileData() {
        if (profileData == null) loadProfileData();
        return profileData;
    }

    //Metodos de encriptacion de datos
    private static String encrypt(String plainText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return String.valueOf(Base64Coder.encode(encryptedBytes));
    }
    private static String decrypt(String encryptedText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64Coder.decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    //Actualizar la oleada máxima alcanzada por el perfil de jugador de cada mapa
    public static void updateMaxWave(String mapName, int reachedWave) {
        if (profileData == null) loadProfileData();

        if ("bosque".equals(mapName) && reachedWave > profileData.maxWaveForest) {
            profileData.maxWaveForest = reachedWave;
        } else if ("desierto".equals(mapName) && reachedWave > profileData.maxWaveDesert) {
            profileData.maxWaveDesert = reachedWave;
        } else if ("cueva".equals(mapName) && reachedWave > profileData.maxWaveCave) {
            profileData.maxWaveCave = reachedWave;
        }
        saveProfileData(); // Guardar los cambios
    }

    //Comprueba si los mapas están desbloqueados en este perfil de jugador
    public static boolean isMapUnlocked(String mapName) {
        if (profileData == null) loadProfileData();

        if ("bosque".equals(mapName)) return true; // Siempre disponible
        if ("desierto".equals(mapName)) return profileData.maxWaveForest >= waveUnlockDesert;
        if ("cueva".equals(mapName)) return profileData.maxWaveDesert >= waveUnlockCave;

        return false;
    }
}
