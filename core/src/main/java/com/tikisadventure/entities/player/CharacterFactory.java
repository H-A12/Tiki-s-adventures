package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.entities.abilities.Ability;

public class CharacterFactory {

    public static CharacterProfile create(CharacterType type, Ability ability) {
        // 1. Definimos la carpeta del personaje basada en su nombre (en minúsculas)
        // Ejemplo: "tiki/" o "moko/"
        String folder = type.name.toLowerCase() + "/";

        // 2. Cargamos las animaciones usando la ruta dinámica de la carpeta
        Animation<TextureRegion> idleAnim  = createAnim(folder + "idle.png",  16, 0.15f);
        Animation<TextureRegion> downAnim  = createAnim(folder + "down.png",  16, 0.15f);
        Animation<TextureRegion> upAnim    = createAnim(folder + "up.png",    16, 0.15f);
        Animation<TextureRegion> leftAnim  = createAnim(folder + "left.png",  16, 0.15f);
        Animation<TextureRegion> rightAnim = createAnim(folder + "right.png", 16, 0.15f);

        // 3. Extraemos el frame inicial
        TextureRegion initialFrame = idleAnim.getKeyFrame(0);

        // 4. Creamos el perfil con los datos del Enum y el frame inicial
        CharacterProfile profile = new CharacterProfile(
            type.name,
            type.maxHealth,
            type.speed,
            ability,
            initialFrame
        );

        // 5. Asignamos las animaciones al perfil
        profile.idle = idleAnim;
        profile.down = downAnim;
        profile.up = upAnim;
        profile.left = leftAnim;
        profile.right = rightAnim;

        return profile;
    }

    private static Animation<TextureRegion> createAnim(String path, int frameSize, float frameDuration) {
        // Verificamos si el archivo existe para evitar un crash feo si falta una imagen
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("CharacterFactory", "Archivo no encontrado: " + path);
            // Podrías cargar una textura de error aquí si quisieras
        }

        Texture tex = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(tex, frameSize, frameSize);
        return new Animation<TextureRegion>(frameDuration, tmp[0]);
    }
}
