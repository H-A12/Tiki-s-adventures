package com.tikisadventure.audio;

public class AudioManager {
    private static SfxPlayer sfxPlayer;
    private static MusicPlayer musicPlayer;
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        sfxPlayer = new SfxPlayer();
        sfxPlayer.load();
        musicPlayer = new MusicPlayer();
        musicPlayer.load();
        loaded = true;
    }

    public static void playSFX(AudioType type) {
        if (!loaded || sfxPlayer == null) {
            return;
        }
        sfxPlayer.play(type);
    }

    public static void playSFX(AudioType type, float volume) {
        if (!loaded || sfxPlayer == null) return;
        sfxPlayer.play(type, volume);
    }

    public static void setMusicBiome(String biome) {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.setBiome(biome);
    }

    public static void playMenuMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.playMenu();
    }

    public static void playGameOverMusic(String biome) {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.playGameOver(biome);
    }

    public static void playBossMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.playBoss();
    }

    public static void stopBossMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.stopBoss();
    }

    public static void stopAllMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.stopAll();
    }

    public static void duckForPause() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.duckForPause();
    }

    public static void unduckFromPause() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.unduckFromPause();
    }

    public static void setSFXVolume(float vol) {
        if (sfxPlayer != null) sfxPlayer.setVolume(vol);
    }

    public static void setMusicVolume(float vol) {
        if (musicPlayer != null) musicPlayer.setVolume(vol);
    }

    public static void setMusicPitch(float pitch) {
        if (musicPlayer != null) musicPlayer.setPitch(pitch);
    }

    public static void setMuted(boolean muted) {
        if (sfxPlayer != null) sfxPlayer.setMuted(muted);
        if (musicPlayer != null) musicPlayer.setMuted(muted);
    }

    public static boolean isLoaded() { return loaded; }

    public static void update(float delta) {
        if (musicPlayer != null) musicPlayer.update(delta);
    }

    public static void dispose() {
        if (sfxPlayer != null) { sfxPlayer.dispose(); sfxPlayer = null; }
        if (musicPlayer != null) { musicPlayer.dispose(); musicPlayer = null; }
        loaded = false;
    }
}
