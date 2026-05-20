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

    // NUEVO: Preferencias para datos persistentes
    private static com.badlogic.gdx.Preferences preferences;

    //Puntos necesarios acumulados para desbloquear cada personaje
    private static int scoreUnlockMoko = 300;  //Cambiable
    private static int scoreUnlockZuki = 1500; //Cambiable

    //Wave o state a la que llegar para desbloquear cada mapa
    public static int stageUnlockDesert = 2;  //Cambiable
    public static int stageUnlockCastillo = 2;   //Cambiable

    private static PlayerData localProfile;   // Se guarda en el disco
    private static PlayerData sessionProfile; // Vive en la RAM (Supabase)
    public static boolean isGuest = true;     // true = Local, false = Nube
    private static final Json json = new Json();

    //Cargar datos al iniciar el juego
    public static void loadProfileData() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (file.exists()) {
            try {
                String encryptedText = file.readString();
                String decryptedJson = decrypt(encryptedText);
                localProfile = json.fromJson(PlayerData.class, decryptedJson);
                if (localProfile == null) localProfile = new PlayerData();
            } catch (Exception e) {
                localProfile = new PlayerData();
            }
        } else {
            localProfile = new PlayerData();
        }
        localProfile.inputConfig.ensureDefaults();
    }

    public static void saveProfileData() {
        if (localProfile == null) return;
        try {
            // MUY IMPORTANTE: Solo guardamos el localProfile en el disco
            String jsonString = json.toJson(localProfile);
            String encryptedText = encrypt(jsonString);
            FileHandle file = Gdx.files.local(SAVE_FILE);
            file.writeString(encryptedText, false);
        } catch (Exception e) {
        }
    }

    // Accesos rápidos al perfil correcto (Nube o Local)
    public static PlayerData getProfileData() {
        if (localProfile == null) loadProfileData();
        return isGuest ? localProfile : sessionProfile;
    }

    public static PlayerData getLocalProfile() {
        if (localProfile == null) loadProfileData();
        return localProfile;
    }

    //Añadir top 5 de puntuacion del perfil:
    public static void addScoreRankProfileData(int newScore) {
        PlayerData data = getProfileData();
        data.totalScore += newScore;

        data.topScores.add(newScore);
        data.topScores.sort();
        data.topScores.reverse();

        while (data.topScores.size > 5) {
            data.topScores.pop();
        }

        checkAndUnlockCharacters();
        saveProfileData();
    }

    public static void addCoins(int amount) {
        getProfileData().coins += amount;
        saveProfileData();
    }

    public static void subtractCoins(int amount) {
        getProfileData().coins -= amount;
        if (getProfileData().coins < 0) getProfileData().coins = 0;
        saveProfileData();
    }

    public static boolean isWeaponOwned(String weaponId) {
        return getProfileData().ownedWeapons.getOrDefault(weaponId, false);
    }

    public static boolean purchaseWeapon(String weaponId, int price) {
        PlayerData data = getProfileData();
        if (isWeaponOwned(weaponId)) return false;
        if (data.coins < price) return false;

        data.coins -= price;
        data.ownedWeapons.put(weaponId, true);
        saveProfileData();
        return true;
    }

    public static boolean isCharacterUnlocked(int characterIndex) {
        PlayerData data = getProfileData();
        switch (characterIndex) {
            case 1: return true;
            case 2: return data.unlockedMoko || data.totalScore >= scoreUnlockMoko;
            case 3: return data.unlockedZuki || data.totalScore >= scoreUnlockZuki;
            default: return false;
        }
    }

    public static void checkAndUnlockCharacters() {
        PlayerData data = getProfileData();

        boolean mokoDesbloqueadoAhora = false;
        boolean zukiDesbloqueadoAhora = false;

        if (!data.unlockedMoko && data.totalScore >= scoreUnlockMoko) {
            data.unlockedMoko = true;
            mokoDesbloqueadoAhora = true;
        }
        if (!data.unlockedZuki && data.totalScore >= scoreUnlockZuki) {
            data.unlockedZuki = true;
            zukiDesbloqueadoAhora = true;
        }

        if (mokoDesbloqueadoAhora || zukiDesbloqueadoAhora) {
            saveProfileData();

            if (data.playerId != -1) {
                com.tikisadventure.database.progress.ProgressRepository progRepo = new com.tikisadventure.database.progress.ProgressRepository();
                if (mokoDesbloqueadoAhora) progRepo.desbloquearPersonajeBD(data.playerId, 2, null);
                if (zukiDesbloqueadoAhora) progRepo.desbloquearPersonajeBD(data.playerId, 3, null);
            }
        }
    }

    public static void updateMaxWave(String mapName, int reachedWave) {
        PlayerData data = getProfileData();
        if ("bosque".equals(mapName) && reachedWave > data.maxWaveForest) {
            data.maxWaveForest = reachedWave;
        } else if ("desierto".equals(mapName) && reachedWave > data.maxWaveDesert) {
            data.maxWaveDesert = reachedWave;
        } else if ("castillo".equals(mapName) && reachedWave > data.maxWaveCastillo) {
            data.maxWaveCastillo = reachedWave;
        }
        saveProfileData();
    }

    // 1. Revertido a la lógica original para desbloquear mapas según progreso
    public static boolean isMapUnlocked(String mapName) {
        PlayerData data = getProfileData();
        if ("bosque".equals(mapName)) return true;
        if ("desierto".equals(mapName)) return data.unlockedDesert || data.maxStageForest >= stageUnlockDesert;
        if ("castillo".equals(mapName)) return data.unlockedCastillo || data.maxStageDesert >= stageUnlockCastillo;
        return false;
    }

    // 2. NUEVO: Actualiza récord local y comprueba si hay que desbloquear
    public static void updateMaxProgress(String mapName, int reachedStage, int reachedWave) {
        PlayerData data = getProfileData();
        boolean changed = false;

        if ("bosque".equals(mapName)) {
            if (reachedStage > data.maxStageForest || (reachedStage == data.maxStageForest && reachedWave > data.maxWaveForest)) {
                data.maxStageForest = reachedStage;
                data.maxWaveForest = reachedWave;
                changed = true;
            }
        } else if ("desierto".equals(mapName)) {
            if (reachedStage > data.maxStageDesert || (reachedStage == data.maxStageDesert && reachedWave > data.maxWaveDesert)) {
                data.maxStageDesert = reachedStage;
                data.maxWaveDesert = reachedWave;
                changed = true;
            }
        } else if ("castillo".equals(mapName)) {
            if (reachedStage > data.maxStageCastillo || (reachedStage == data.maxStageCastillo && reachedWave > data.maxWaveCastillo)) {
                data.maxStageCastillo = reachedStage;
                data.maxWaveCastillo = reachedWave;
                changed = true;
            }
        }

        if (changed) {
            checkAndUnlockMaps(); // Comprobamos si el nuevo récord desbloquea el siguiente mapa
            saveProfileData();
        }
    }

    // 3. NUEVO: Lógica de desbloqueo de mapas
    private static void checkAndUnlockMaps() {
        PlayerData data = getProfileData();
        boolean desertUnlockedNow = false;
        boolean castilloUnlockedNow = false;

        if (!data.unlockedDesert && data.maxStageForest >= stageUnlockDesert) {
            data.unlockedDesert = true;
            desertUnlockedNow = true;
        }
        if (!data.unlockedCastillo && data.maxStageDesert >= stageUnlockCastillo) {
            data.unlockedCastillo = true;
            castilloUnlockedNow = true;
        }

        if (desertUnlockedNow || castilloUnlockedNow) {
            saveProfileData();
            if (data.playerId != -1) {
                com.tikisadventure.database.progress.ProgressRepository progRepo = new com.tikisadventure.database.progress.ProgressRepository();
                if (desertUnlockedNow) progRepo.desbloquearMapaBD(data.playerId, "desierto", null);
                if (castilloUnlockedNow) progRepo.desbloquearMapaBD(data.playerId, "castillo", null);
            }
        }
    }

    public static void aplicarMapasNube(boolean desert, boolean castillo) {
        if (sessionProfile != null) {
            sessionProfile.unlockedDesert = desert;
            sessionProfile.unlockedCastillo = castillo;
        }
    }

    // --- GESTIÓN DE SESIÓN DE SUPABASE ---

    public static void saveLogin(String username, String password) {
        PlayerData local = getLocalProfile();
        local.lastUsername = username;
        local.lastPassword = password;
        saveProfileData();
    }

    public static void clearLogin() {
        PlayerData local = getLocalProfile();
        local.lastUsername = "";
        local.lastPassword = "";
        local.selectedGadget = "grenade_kinetic";
        local.selectedStartingWeapon = null;

        sessionProfile = null;
        isGuest = true;

        saveProfileData();
    }

    // Usamos explícitamente getLocalProfile() para los datos de acceso
    public static String getLastUsername() {
        return getLocalProfile().lastUsername != null ? getLocalProfile().lastUsername : "";
    }

    public static String getLastPassword() {
        return getLocalProfile().lastPassword != null ? getLocalProfile().lastPassword : "";
    }

    public static void setCoins(int amount) {
        getProfileData().coins = amount;
        saveProfileData();
    }

    public static void setProgresoNube(int cloudCoins, int cloudTotalScore) {
        PlayerData data = getProfileData();
        data.coins = cloudCoins;
        data.totalScore = cloudTotalScore;

        checkAndUnlockCharacters();
        saveProfileData();
    }

    public static void aplicarDatosNube(long id, int coins, int score, boolean moko, boolean zuki) {
        if (localProfile == null) loadProfileData();

        sessionProfile = new PlayerData();
        sessionProfile.playerId = id;
        sessionProfile.coins = coins;
        sessionProfile.totalScore = score;
        sessionProfile.unlockedMoko = moko;
        sessionProfile.unlockedZuki = zuki;

        isGuest = false;
    }

    public static void markLocalAsLinked() {
        getLocalProfile().wasLinkedToCloud = true;
        saveProfileData();
    }

    //Metodos de encriptacion
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

    public static void aplicarArmasNube(com.badlogic.gdx.utils.Array<String> armasDesbloqueadas) {
        if (sessionProfile == null) return; // Por seguridad

        // Limpiamos las que pudiera haber y metemos las de la nube
        sessionProfile.ownedWeapons.clear();
        for (String arma : armasDesbloqueadas) {
            sessionProfile.ownedWeapons.put(arma, true);
        }
    }

    // NUEVA FUNCIONALIDAD: Resolución guardada
    private static final String RESOLUTION_WIDTH_KEY = "resolution_width";
    private static final String RESOLUTION_HEIGHT_KEY = "resolution_height";
    private static final String FULLSCREEN_KEY = "fullscreen";
    private static final String MUSIC_VOLUME_KEY = "music_volume";
    private static final String SFX_VOLUME_KEY = "sfx_volume";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;

    // NUEVO: Inicializar preferencias
    private static void initPreferences() {
        if (preferences == null) {
            preferences = Gdx.app.getPreferences("tikisadventure_settings");
        }
    }

    public static void saveResolution(int width, int height) {
        initPreferences();
        preferences.putInteger(RESOLUTION_WIDTH_KEY, width);
        preferences.putInteger(RESOLUTION_HEIGHT_KEY, height);
        preferences.flush();
    }

    public static void saveFullscreen(boolean fullscreen) {
        initPreferences();
        preferences.putBoolean(FULLSCREEN_KEY, fullscreen);
        preferences.flush();
    }

    public static boolean isFullscreen() {
        initPreferences();
        return preferences.getBoolean(FULLSCREEN_KEY, false);
    }

    public static void saveVolume(float musicVol, float sfxVol) {
        initPreferences();
        preferences.putFloat(MUSIC_VOLUME_KEY, musicVol);
        preferences.putFloat(SFX_VOLUME_KEY, sfxVol);
        preferences.flush();
    }

    public static float getMusicVolume() {
        initPreferences();
        return preferences.getFloat(MUSIC_VOLUME_KEY, 1.0f);
    }

    public static float getSFXVolume() {
        initPreferences();
        return preferences.getFloat(SFX_VOLUME_KEY, 1.0f);
    }

    public static int getResolutionWidth() {
        initPreferences();
        return preferences.getInteger(RESOLUTION_WIDTH_KEY, DEFAULT_WIDTH);
    }

    public static int getResolutionHeight() {
        initPreferences();
        return preferences.getInteger(RESOLUTION_HEIGHT_KEY, DEFAULT_HEIGHT);
    }

    public static boolean isGadgetOwned(String gadgetId) {
        if ("grenade_kinetic".equals(gadgetId)) return true;
        PlayerData data = getProfileData();
        return data.ownedGadgets != null && data.ownedGadgets.get(gadgetId, false);
    }

    public static boolean purchaseGadget(String gadgetId, int price) {
        PlayerData data = getProfileData();
        if (isGadgetOwned(gadgetId)) return false;
        if (data.coins < price) return false;

        data.coins -= price;
        if (data.ownedGadgets == null) data.ownedGadgets = new com.badlogic.gdx.utils.ObjectMap<>();
        data.ownedGadgets.put(gadgetId, true);
        saveProfileData();
        return true;
    }

    public static void setEquippedGadget(String gadgetId) {
        PlayerData data = getProfileData();
        if (data.ownedGadgets == null) {
            data.ownedGadgets = new com.badlogic.gdx.utils.ObjectMap<>();
        }
        data.selectedGadget = gadgetId;

        saveProfileData();
    }

    public static String getEquippedGadget() {
        PlayerData data = getProfileData();
        return data.selectedGadget != null ? data.selectedGadget : "grenade_kinetic";
    }

    public static void aplicarGadgetsNube(com.badlogic.gdx.utils.Array<String> gadgetsDesbloqueados) {
        if (sessionProfile == null) return;
        sessionProfile.ownedGadgets.clear();
        for (String gadget : gadgetsDesbloqueados) {
            sessionProfile.ownedGadgets.put(gadget, true);
        }
    }

    public static boolean isWeaponUnlockedOrDefault(String weaponId) {
        if ("BallRifle".equals(weaponId)) return true;
        if ("FireworkLauncher".equals(weaponId)) return isCharacterUnlocked(2);
        if ("ToothpickShotgun".equals(weaponId)) return isCharacterUnlocked(3);
        return isWeaponOwned(weaponId);
    }

    public static void setEquippedStartingWeapon(String weaponId) {
        PlayerData data = getProfileData();
        data.selectedStartingWeapon = weaponId;

        saveProfileData();
    }

    public static String getEquippedStartingWeapon() {
        PlayerData data = getProfileData();
        return data.selectedStartingWeapon != null && !data.selectedStartingWeapon.isEmpty()
            ? data.selectedStartingWeapon : null;
    }
}
