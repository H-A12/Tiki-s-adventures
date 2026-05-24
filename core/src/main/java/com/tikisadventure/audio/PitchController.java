package com.tikisadventure.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

//Cambia el tono (pitch) de la música usando OpenAL por reflection.
//Busca el sourceID interno de la Music de LibGDX y llama a alSourcef.
public class PitchController {
    private static Field sourceIdField;
    private static Method alSourcefMethod;
    private static Integer alPitchConstant;
    private static boolean failed = false;

    public static void setPitch(Music music, float pitch) {
        if (failed) return;
        try {
            if (sourceIdField == null) initReflection(music);
            int sourceID = sourceIdField.getInt(music);
            alSourcefMethod.invoke(null, sourceID, alPitchConstant, pitch);
        } catch (Exception e) {
            failed = true;
        }
    }

    private static void initReflection(Music music) throws Exception {
        Class<?> clazz = music.getClass();
        while (clazz != null) {
            try {
                sourceIdField = clazz.getDeclaredField("sourceID");
                sourceIdField.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (sourceIdField == null) throw new NoSuchFieldException("sourceID no encontrado en " + music.getClass().getName());

        Class<?> al10 = Class.forName("org.lwjgl.openal.AL10");
        alSourcefMethod = al10.getMethod("alSourcef", int.class, int.class, float.class);
        alPitchConstant = al10.getField("AL_PITCH").getInt(null);
    }
}
