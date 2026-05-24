package com.tikisadventure.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import java.util.HashMap;
import java.util.Map;

//Reproduce música por biomas (bajo, principal, batería) con variaciones
//aleatorias y sistema de pausa (duck). Tiene modos menú, jefe y game over.
//Usa PitchController para cambiar el tono.
public class MusicPlayer {
    private static final float TRACK_DURATION = 30.720f;
    private static final int VARIATIONS = 5;
    private static final String[] BIOMES = {"bosque", "desierto", "castillo"};
    private static final float BASE_VOLUME = 0.8f;

    private Music menuMusic;
    private Music gameOverMusic;
    private Music bossMusic;
    private Map<String, Music> biomeBasses;
    private Map<String, Music[]> biomePrincipals;
    private Map<String, Music[]> biomeDrums;
    private Map<String, Music> biomeDeaths;

    private Map<String, Music> biomePauseBasses;
    private Map<String, Music[]> biomePausePrincipals;
    private Map<String, Music[]> biomePauseDrums;

    private String currentBiome;
    private Music currentBass;
    private Music currentMain1;
    private Music currentMain2;
    private Music currentDrums;
    private Music currentPauseBass;
    private Music currentPauseMain1;
    private Music currentPauseMain2;
    private Music currentPauseDrums;
    private int currentMainIdx1;
    private int currentMainIdx2;
    private int currentDrumsIdx;
    private float playTime;

    private String currentMode = "none";
    private Music currentDeathTrack;
    private float musicVolume = 1.0f;
    private boolean muted = false;
    private float duckCurrent = 0f;
    private float currentPitch = 1f;

    public void setPitch(float pitch) {
        currentPitch = pitch;
        applyPitch();
    }

    private void applyPitch() {
        if (currentMode.equals("biome")) {
            if (currentBass != null) PitchController.setPitch(currentBass, currentPitch);
            if (currentMain1 != null) PitchController.setPitch(currentMain1, currentPitch);
            if (currentMain2 != null) PitchController.setPitch(currentMain2, currentPitch);
            if (currentDrums != null) PitchController.setPitch(currentDrums, currentPitch);
            if (currentPauseBass != null) PitchController.setPitch(currentPauseBass, currentPitch);
            if (currentPauseMain1 != null) PitchController.setPitch(currentPauseMain1, currentPitch);
            if (currentPauseMain2 != null) PitchController.setPitch(currentPauseMain2, currentPitch);
            if (currentPauseDrums != null) PitchController.setPitch(currentPauseDrums, currentPitch);
        } else if (currentMode.equals("menu") && menuMusic != null) {
            PitchController.setPitch(menuMusic, currentPitch);
        } else if (currentMode.equals("game_over") && currentDeathTrack != null) {
            PitchController.setPitch(currentDeathTrack, currentPitch);
        } else if (currentMode.equals("boss") && bossMusic != null) {
            PitchController.setPitch(bossMusic, currentPitch);
        }
    }

    //Cargar todas las pistas de música de todos los biomas
    public void load() {
        biomeBasses = new HashMap<>();
        biomePrincipals = new HashMap<>();
        biomeDrums = new HashMap<>();
        biomeDeaths = new HashMap<>();
        biomePauseBasses = new HashMap<>();
        biomePausePrincipals = new HashMap<>();
        biomePauseDrums = new HashMap<>();
        for (String biome : BIOMES) {
            biomeBasses.put(biome, tryLoad("audio/music/" + biome + "/bajo.ogg"));
            Music[] principals = new Music[VARIATIONS];
            Music[] drums = new Music[VARIATIONS];
            for (int i = 0; i < VARIATIONS; i++) {
                principals[i] = tryLoad("audio/music/" + biome + "/principal_" + (i + 1) + ".ogg");
                drums[i] = tryLoad("audio/music/" + biome + "/bateria_" + (i + 1) + ".ogg");
            }
            biomePrincipals.put(biome, principals);
            biomeDrums.put(biome, drums);
            biomeDeaths.put(biome, tryLoad("audio/music/" + biome + "/muerte.ogg"));

            biomePauseBasses.put(biome, tryLoad("audio/music/" + biome + "/bajo_pausa.ogg"));
            Music[] pPrincipals = new Music[VARIATIONS];
            Music[] pDrums = new Music[VARIATIONS];
            for (int i = 0; i < VARIATIONS; i++) {
                pPrincipals[i] = tryLoad("audio/music/" + biome + "/principal_" + (i + 1) + "_pausa.ogg");
                pDrums[i] = tryLoad("audio/music/" + biome + "/bateria_" + (i + 1) + "_pausa.ogg");
            }
            biomePausePrincipals.put(biome, pPrincipals);
            biomePauseDrums.put(biome, pDrums);
        }
        menuMusic = tryLoad("audio/music/menu.ogg");
        gameOverMusic = tryLoad("audio/music/game_over.ogg");
        bossMusic = tryLoad("audio/music/boss.ogg");
    }

    private Music tryLoad(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) return null;
        return Gdx.audio.newMusic(file);
    }

    //Cambiar a música de un bioma
    public void setBiome(String biome) {
        if (!biomePrincipals.containsKey(biome)) {
            return;
        }
        if (currentMode.equals("biome") && biome.equals(currentBiome)) return;
        stopAll();
        duckCurrent = 0f;
        currentBiome = biome;
        currentMode = "biome";
        pickNext();
        startSet();
    }

    private void pickNext() {
        int nm1, nm2, nd;
        do {
            nm1 = MathUtils.random(VARIATIONS - 1);
            do { nm2 = MathUtils.random(VARIATIONS - 1); } while (nm2 == nm1);
            nd = MathUtils.random(VARIATIONS - 1);
        } while (nm1 == currentMainIdx1 && nm2 == currentMainIdx2 && nd == currentDrumsIdx);
        currentMainIdx1 = nm1;
        currentMainIdx2 = nm2;
        currentDrumsIdx = nd;
    }

    private void startSet() {
        float normalVol = getEffectiveNormalVol();
        float pauseVol = getEffectivePauseVol();
        currentBass = biomeBasses.get(currentBiome);
        currentMain1 = biomePrincipals.get(currentBiome)[currentMainIdx1];
        currentMain2 = biomePrincipals.get(currentBiome)[currentMainIdx2];
        currentDrums = biomeDrums.get(currentBiome)[currentDrumsIdx];
        currentPauseBass = biomePauseBasses.get(currentBiome);
        currentPauseMain1 = biomePausePrincipals.get(currentBiome)[currentMainIdx1];
        currentPauseMain2 = biomePausePrincipals.get(currentBiome)[currentMainIdx2];
        currentPauseDrums = biomePauseDrums.get(currentBiome)[currentDrumsIdx];
        playTime = 0;
        if (currentBass != null) {
            currentBass.setLooping(true);
            currentBass.setVolume(normalVol);
            currentBass.play();
        }
        if (currentPauseBass != null) {
            currentPauseBass.setLooping(true);
            currentPauseBass.setVolume(pauseVol);
            currentPauseBass.play();
        }
        playTrack(currentMain1, false, normalVol);
        playTrack(currentMain2, false, normalVol);
        playTrack(currentDrums, false, normalVol);
        playTrack(currentPauseMain1, false, pauseVol);
        playTrack(currentPauseMain2, false, pauseVol);
        playTrack(currentPauseDrums, false, pauseVol);
        applyPitch();
    }

    private void playTrack(Music track, boolean looping, float vol) {
        if (track == null) return;
        track.setLooping(looping);
        track.setVolume(vol);
        track.play();
    }

    //Cambiar de variación cada 30 segundos
    public void update(float delta) {
        if (!currentMode.equals("biome") || currentBass == null) return;

        playTime += delta;
        if (playTime >= TRACK_DURATION) {
            float normalVol = getEffectiveNormalVol();
            float pauseVol = getEffectivePauseVol();
            stopTrack(currentBass);
            stopTrack(currentMain1);
            stopTrack(currentMain2);
            stopTrack(currentDrums);
            stopTrack(currentPauseBass);
            stopTrack(currentPauseMain1);
            stopTrack(currentPauseMain2);
            stopTrack(currentPauseDrums);
            pickNext();
            currentBass = biomeBasses.get(currentBiome);
            currentMain1 = biomePrincipals.get(currentBiome)[currentMainIdx1];
            currentMain2 = biomePrincipals.get(currentBiome)[currentMainIdx2];
            currentDrums = biomeDrums.get(currentBiome)[currentDrumsIdx];
            currentPauseBass = biomePauseBasses.get(currentBiome);
            currentPauseMain1 = biomePausePrincipals.get(currentBiome)[currentMainIdx1];
            currentPauseMain2 = biomePausePrincipals.get(currentBiome)[currentMainIdx2];
            currentPauseDrums = biomePauseDrums.get(currentBiome)[currentDrumsIdx];
            currentBass.setLooping(true);
            currentBass.setVolume(normalVol);
            currentBass.play();
            playTrack(currentMain1, false, normalVol);
            playTrack(currentMain2, false, normalVol);
            playTrack(currentDrums, false, normalVol);
            if (currentPauseBass != null) {
                currentPauseBass.setLooping(true);
                currentPauseBass.setVolume(pauseVol);
                currentPauseBass.play();
            }
            playTrack(currentPauseMain1, false, pauseVol);
            playTrack(currentPauseMain2, false, pauseVol);
            playTrack(currentPauseDrums, false, pauseVol);
            applyPitch();
            playTime = 0;
        }
    }

    private void stopTrack(Music track) {
        if (track == null) return;
        track.stop();
    }

    //Reproducir música del menú
    public void playMenu() {
        if (currentMode.equals("menu") && menuMusic != null && menuMusic.isPlaying()) {
            applyVolume();
            applyPitch();
            return;
        }
        stopAll();
        duckCurrent = 0f;
        currentMode = "menu";
        if (menuMusic != null) {
            menuMusic.setLooping(true);
            menuMusic.setVolume(muted ? 0 : musicVolume * BASE_VOLUME);
            menuMusic.play();
            applyPitch();
        }
    }

    //Reproducir música de game over
    public void playGameOver(String biome) {
        stopAll();
        duckCurrent = 0f;
        currentMode = "game_over";
        currentDeathTrack = null;
        if (biome != null && biomeDeaths.containsKey(biome)) {
            currentDeathTrack = biomeDeaths.get(biome);
        }
        if (currentDeathTrack == null) currentDeathTrack = gameOverMusic;
        if (currentDeathTrack != null) {
            currentDeathTrack.setLooping(false);
            currentDeathTrack.setVolume(muted ? 0 : musicVolume * BASE_VOLUME);
            currentDeathTrack.play();
            applyPitch();
        }
    }

    //Reproducir música de jefe
    public void playBoss() {
        if (currentMode.equals("boss")) return;
        stopAll();
        duckCurrent = 0f;
        currentMode = "boss";
        if (bossMusic != null) {
            bossMusic.setLooping(true);
            bossMusic.setVolume(muted ? 0 : musicVolume * BASE_VOLUME);
            bossMusic.play();
            applyPitch();
        }
    }

    //Parar música de jefe y volver al bioma
    public void stopBoss() {
        if (!currentMode.equals("boss")) return;
        if (bossMusic != null) bossMusic.stop();
        if (currentBiome != null) setBiome(currentBiome);
        else currentMode = "none";
    }

    //Parar toda la música
    public void stopAll() {
        stopTrack(currentBass); currentBass = null;
        stopTrack(currentMain1); currentMain1 = null;
        stopTrack(currentMain2); currentMain2 = null;
        stopTrack(currentDrums); currentDrums = null;
        stopTrack(currentPauseBass); currentPauseBass = null;
        stopTrack(currentPauseMain1); currentPauseMain1 = null;
        stopTrack(currentPauseMain2); currentPauseMain2 = null;
        stopTrack(currentPauseDrums); currentPauseDrums = null;
        stopTrack(currentDeathTrack); currentDeathTrack = null;
        if (menuMusic != null && menuMusic.isPlaying()) menuMusic.stop();
        if (gameOverMusic != null && gameOverMusic.isPlaying()) gameOverMusic.stop();
        if (bossMusic != null && bossMusic.isPlaying()) bossMusic.stop();
        currentMode = "none";
        duckCurrent = 0f;
        currentPitch = 1f;
    }

    //Bajar volumen al pausar
    public void duckForPause() {
        duckCurrent = 1f;
        applyVolume();
    }

    //Restaurar volumen al reanudar
    public void unduckFromPause() {
        duckCurrent = 0f;
        applyVolume();
    }

    public void setVolume(float vol) {
        musicVolume = vol;
        applyVolume();
    }

    public boolean isMuted() { return muted; }

    public void setMuted(boolean m) {
        muted = m;
        applyVolume();
    }

    private float getEffectiveNormalVol() {
        return muted ? 0 : musicVolume * BASE_VOLUME * (1 - duckCurrent);
    }

    private float getEffectivePauseVol() {
        return muted ? 0 : musicVolume * BASE_VOLUME * duckCurrent;
    }

    private void applyVolume() {
        if (currentMode.equals("biome")) {
            float normalVol = getEffectiveNormalVol();
            float pauseVol = getEffectivePauseVol();
            if (currentBass != null) currentBass.setVolume(normalVol);
            if (currentMain1 != null) currentMain1.setVolume(normalVol);
            if (currentMain2 != null) currentMain2.setVolume(normalVol);
            if (currentDrums != null) currentDrums.setVolume(normalVol);
            if (currentPauseBass != null) currentPauseBass.setVolume(pauseVol);
            if (currentPauseMain1 != null) currentPauseMain1.setVolume(pauseVol);
            if (currentPauseMain2 != null) currentPauseMain2.setVolume(pauseVol);
            if (currentPauseDrums != null) currentPauseDrums.setVolume(pauseVol);
        } else {
            float vol = muted ? 0 : musicVolume * BASE_VOLUME * (1 - duckCurrent);
            if (currentMode.equals("menu") && menuMusic != null) {
                menuMusic.setVolume(vol);
            } else if (currentMode.equals("game_over") && currentDeathTrack != null) {
                currentDeathTrack.setVolume(vol);
            } else if (currentMode.equals("boss") && bossMusic != null) {
                bossMusic.setVolume(vol);
            }
        }
    }

    //Liberar todas las pistas de música
    public void dispose() {
        stopAll();
        for (Music m : biomeBasses.values()) { if (m != null) m.dispose(); }
        for (Music[] arr : biomePrincipals.values()) {
            for (Music m : arr) { if (m != null) m.dispose(); }
        }
        for (Music[] arr : biomeDrums.values()) {
            for (Music m : arr) { if (m != null) m.dispose(); }
        }
        biomeBasses.clear();
        biomePrincipals.clear();
        biomeDrums.clear();
        for (Music m : biomeDeaths.values()) { if (m != null) m.dispose(); }
        biomeDeaths.clear();
        for (Music m : biomePauseBasses.values()) { if (m != null) m.dispose(); }
        biomePauseBasses.clear();
        for (Music[] arr : biomePausePrincipals.values()) {
            for (Music m : arr) { if (m != null) m.dispose(); }
        }
        biomePausePrincipals.clear();
        for (Music[] arr : biomePauseDrums.values()) {
            for (Music m : arr) { if (m != null) m.dispose(); }
        }
        biomePauseDrums.clear();
        if (menuMusic != null) { menuMusic.dispose(); menuMusic = null; }
        if (gameOverMusic != null) { gameOverMusic.dispose(); gameOverMusic = null; }
        if (bossMusic != null) { bossMusic.dispose(); bossMusic = null; }
    }
}
