package com.tikisadventure.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import java.util.EnumMap;

public class SfxPlayer {
    private EnumMap<AudioType, Sound> sounds;
    private float volume = 1.0f;
    private boolean muted = false;

    public void load() {
        sounds = new EnumMap<>(AudioType.class);
        for (AudioType type : AudioType.values()) {
            String path = "audio/sfx/" + type.name().toLowerCase() + ".ogg";
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                sounds.put(type, Gdx.audio.newSound(file));
                Gdx.app.log("SfxPlayer", "Loaded: " + path);
            } else {
                String wavPath = "audio/sfx/" + type.name().toLowerCase() + ".wav";
                FileHandle wavFile = Gdx.files.internal(wavPath);
                if (wavFile.exists()) {
                    sounds.put(type, Gdx.audio.newSound(wavFile));
                    Gdx.app.log("SfxPlayer", "Loaded: " + wavPath);
                } else {
                    Gdx.app.error("SfxPlayer", "SFX not found: " + path + " or " + wavPath);
                }
            }
        }
        Gdx.app.log("SfxPlayer", "Loaded " + sounds.size() + " SFX");
    }

    public void play(AudioType type) {
        Sound sound = sounds.get(type);
        if (sound != null) {
            Gdx.app.log("SfxPlayer", "Playing: " + type.name());
            sound.play(muted ? 0 : volume);
        } else {
            Gdx.app.log("SfxPlayer", "Sound is null for: " + type.name());
        }
    }

    public long play(AudioType type, float vol) {
        Sound sound = sounds.get(type);
        if (sound != null) {
            return sound.play(muted ? 0 : volume * vol);
        }
        return -1;
    }

    public void setVolume(float vol) {
        this.volume = vol;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void dispose() {
        if (sounds != null) {
            for (Sound s : sounds.values()) {
                if (s != null) s.dispose();
            }
            sounds.clear();
        }
    }
}
