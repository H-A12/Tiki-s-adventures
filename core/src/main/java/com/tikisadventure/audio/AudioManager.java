package com.tikisadventure.audio;

//Punto central de audio. Usa SfxPlayer para efectos y MusicPlayer para
//música. Se comunica con SaveManager para el volumen y con el EventBus
//para reproducir sonidos según lo que pasa en el juego.
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

    //Reproducir efecto de sonido
    public static void playSFX(AudioType type) {
        if (!loaded || sfxPlayer == null) {
            return;
        }
        sfxPlayer.play(type);
    }

    //Reproducir efecto con volumen personalizado
    public static void playSFX(AudioType type, float volume) {
        if (!loaded || sfxPlayer == null) return;
        sfxPlayer.play(type, volume);
    }

    //Cambiar música al bioma actual
    public static void setMusicBiome(String biome) {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.setBiome(biome);
    }

    //Reproducir música del menú
    public static void playMenuMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.playMenu();
    }

    //Reproducir música de game over
    public static void playGameOverMusic(String biome) {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.playGameOver(biome);
    }

    //Reproducir música de jefe
    public static void playBossMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.playBoss();
    }

    //Parar música de jefe
    public static void stopBossMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.stopBoss();
    }

    //Parar toda la música
    public static void stopAllMusic() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.stopAll();
    }

    //Bajar volumen al pausar
    public static void duckForPause() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.duckForPause();
    }

    //Restaurar volumen al reanudar
    public static void unduckFromPause() {
        if (!loaded || musicPlayer == null) return;
        musicPlayer.unduckFromPause();
    }

    //Cambiar volumen de efectos
    public static void setSFXVolume(float vol) {
        if (sfxPlayer != null) sfxPlayer.setVolume(vol);
    }

    //Cambiar volumen de música
    public static void setMusicVolume(float vol) {
        if (musicPlayer != null) musicPlayer.setVolume(vol);
    }

    //Cambiar tono de la música
    public static void setMusicPitch(float pitch) {
        if (musicPlayer != null) musicPlayer.setPitch(pitch);
    }

    //Silenciar todo el audio
    public static void setMuted(boolean muted) {
        if (sfxPlayer != null) sfxPlayer.setMuted(muted);
        if (musicPlayer != null) musicPlayer.setMuted(muted);
    }

    public static boolean isLoaded() { return loaded; }

    //Actualizar música cada frame
    public static void update(float delta) {
        if (musicPlayer != null) musicPlayer.update(delta);
    }

    //Liberar recursos de audio
    public static void dispose() {
        if (sfxPlayer != null) { sfxPlayer.dispose(); sfxPlayer = null; }
        if (musicPlayer != null) { musicPlayer.dispose(); musicPlayer = null; }
        loaded = false;
    }
}
