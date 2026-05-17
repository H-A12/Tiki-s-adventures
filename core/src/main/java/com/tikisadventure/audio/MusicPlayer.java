package com.tikisadventure.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import java.util.HashMap;
import java.util.Map;

public class MusicPlayer {
    private static final float TRACK_DURATION = 30.720f;
    private static final int VARIATIONS = 5;
    private static final String[] BIOMES = {"bosque", "desierto", "castillo"};

    private Music menuMusic;
    private Music gameOverMusic;
    private Music bossMusic;
    private Map<String, Music> biomeBasses;
    private Map<String, Music[]> biomePrincipals;
    private Map<String, Music[]> biomeDrums;
    private Map<String, Music> biomeDeaths;

    private String currentBiome;
    private Music currentBass;
    private Music currentMain1;
    private Music currentMain2;
    private Music currentDrums;
    private int currentMainIdx1;
    private int currentMainIdx2;
    private int currentDrumsIdx;
    private float playTime;

    private String currentMode = "none";
    private Music currentDeathTrack;
    private float musicVolume = 1.0f;
    private boolean muted = false;

    public void load() {
        biomeBasses = new HashMap<>();
        biomePrincipals = new HashMap<>();
        biomeDrums = new HashMap<>();
        biomeDeaths = new HashMap<>();
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
        }
        menuMusic = tryLoad("audio/music/menu.ogg");
        gameOverMusic = tryLoad("audio/music/game_over.ogg");
        bossMusic = tryLoad("audio/music/boss.ogg");
        Gdx.app.log("MusicPlayer", "Loaded all music tracks");
    }

    private Music tryLoad(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) return null;
        return Gdx.audio.newMusic(file);
    }

    public void setBiome(String biome) {
        if (!biomePrincipals.containsKey(biome)) {
            Gdx.app.error("MusicPlayer", "Unknown biome: " + biome);
            return;
        }
        if (currentMode.equals("biome") && biome.equals(currentBiome)) return;
        stopAll();
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
        float vol = muted ? 0 : musicVolume;
        currentBass = biomeBasses.get(currentBiome);
        currentMain1 = biomePrincipals.get(currentBiome)[currentMainIdx1];
        currentMain2 = biomePrincipals.get(currentBiome)[currentMainIdx2];
        currentDrums = biomeDrums.get(currentBiome)[currentDrumsIdx];
        playTime = 0;
        if (currentBass != null) {
            currentBass.setLooping(true);
            currentBass.setVolume(vol);
            currentBass.play();
        }
        playTrack(currentMain1, false, vol);
        playTrack(currentMain2, false, vol);
        playTrack(currentDrums, false, vol);
    }

    private void playTrack(Music track, boolean looping, float vol) {
        if (track == null) return;
        track.setLooping(looping);
        track.setVolume(vol);
        track.play();
    }

    public void update(float delta) {
        if (!currentMode.equals("biome") || currentBass == null) return;

        playTime += delta;
        if (playTime >= TRACK_DURATION) {
            float vol = muted ? 0 : musicVolume;
            stopTrack(currentBass);
            stopTrack(currentMain1);
            stopTrack(currentMain2);
            stopTrack(currentDrums);
            pickNext();
            currentBass = biomeBasses.get(currentBiome);
            currentMain1 = biomePrincipals.get(currentBiome)[currentMainIdx1];
            currentMain2 = biomePrincipals.get(currentBiome)[currentMainIdx2];
            currentDrums = biomeDrums.get(currentBiome)[currentDrumsIdx];
            currentBass.setLooping(true);
            currentBass.setVolume(vol);
            currentBass.play();
            playTrack(currentMain1, false, vol);
            playTrack(currentMain2, false, vol);
            playTrack(currentDrums, false, vol);
            playTime = 0;
        }
    }

    private void stopTrack(Music track) {
        if (track == null) return;
        track.stop();
    }

    public void playMenu() {
        stopAll();
        currentMode = "menu";
        if (menuMusic != null) {
            menuMusic.setLooping(true);
            menuMusic.setVolume(muted ? 0 : musicVolume);
            menuMusic.play();
        }
    }

    public void playGameOver(String biome) {
        stopAll();
        currentMode = "game_over";
        currentDeathTrack = null;
        if (biome != null && biomeDeaths.containsKey(biome)) {
            currentDeathTrack = biomeDeaths.get(biome);
        }
        if (currentDeathTrack == null) currentDeathTrack = gameOverMusic;
        if (currentDeathTrack != null) {
            currentDeathTrack.setLooping(false);
            currentDeathTrack.setVolume(muted ? 0 : musicVolume);
            currentDeathTrack.play();
        }
    }

    public void playBoss() {
        if (currentMode.equals("boss")) return;
        stopAll();
        currentMode = "boss";
        if (bossMusic != null) {
            bossMusic.setLooping(true);
            bossMusic.setVolume(muted ? 0 : musicVolume);
            bossMusic.play();
        }
    }

    public void stopBoss() {
        if (!currentMode.equals("boss")) return;
        if (bossMusic != null) bossMusic.stop();
        if (currentBiome != null) setBiome(currentBiome);
        else currentMode = "none";
    }

    public void stopAll() {
        stopTrack(currentBass); currentBass = null;
        stopTrack(currentMain1); currentMain1 = null;
        stopTrack(currentMain2); currentMain2 = null;
        stopTrack(currentDrums); currentDrums = null;
        stopTrack(currentDeathTrack); currentDeathTrack = null;
        if (menuMusic != null && menuMusic.isPlaying()) menuMusic.stop();
        if (gameOverMusic != null && gameOverMusic.isPlaying()) gameOverMusic.stop();
        if (bossMusic != null && bossMusic.isPlaying()) bossMusic.stop();
        currentMode = "none";
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

    private void applyVolume() {
        float vol = muted ? 0 : musicVolume;
        if (currentMode.equals("biome")) {
            if (currentBass != null) currentBass.setVolume(vol);
            if (currentMain1 != null) currentMain1.setVolume(vol);
            if (currentMain2 != null) currentMain2.setVolume(vol);
            if (currentDrums != null) currentDrums.setVolume(vol);
        } else if (currentMode.equals("menu") && menuMusic != null) {
            menuMusic.setVolume(vol);
        } else if (currentMode.equals("game_over") && currentDeathTrack != null) {
            currentDeathTrack.setVolume(vol);
        } else if (currentMode.equals("boss") && bossMusic != null) {
            bossMusic.setVolume(vol);
        }
    }

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
        if (menuMusic != null) { menuMusic.dispose(); menuMusic = null; }
        if (gameOverMusic != null) { gameOverMusic.dispose(); gameOverMusic = null; }
        if (bossMusic != null) { bossMusic.dispose(); bossMusic = null; }
    }
}
