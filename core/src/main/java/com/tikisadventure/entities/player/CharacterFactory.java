package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.combat.abilities.Ability;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.assets.Assets;
import com.tikisadventure.combat.abilities.Dash;

/**
 * Factoría agnóstica: No conoce Enums, solo lee lo que el JSON le dicta.
 */
public class CharacterFactory {

    /**
     * Crea un perfil de personaje extrayendo los datos de un nodo JSON.
     * @param characterData El nodo "tiki", "moko", etc., del characters_config.json
     */
    public static CharacterProfile create(JsonValue characterData,
                                          Weapon.ProjectileCreator projectileCreator,
                                          EffectManager effectManager) {

        // 1. Extraer los datos técnicos del JSON
        String name = characterData.getString("name", "Héroe Genérico");
        String folder = characterData.getString("sprite_path", "player/tiki") + "/";
        int frameSize = characterData.getInt("frame_size", 16);

        // 2. Carga dinámica de animaciones (Y-Sorting Ready)
        Animation<TextureRegion> idle  = createAnim(folder + "idle",  frameSize, 0.15f);
        Animation<TextureRegion> down  = createAnim(folder + "down",  frameSize, 0.15f);
        Animation<TextureRegion> up    = createAnim(folder + "up",    frameSize, 0.15f);
        Animation<TextureRegion> left  = createAnim(folder + "left",  frameSize, 0.15f);
        Animation<TextureRegion> right = createAnim(folder + "right", frameSize, 0.15f);

        if (idle == null) {
            Gdx.app.error("CharacterFactory", "Error crítico: No se encontraron sprites para " + name);
            return null;
        }

        // 3. Configuración de Habilidades (Hardcoded temporalmente o via Reflection)
        Ability ability1 = new Dash(); // Podrías usar characterData.getString("ability1") después

        // 4. Construcción del Perfil
        CharacterProfile profile = new CharacterProfile(
            name,
            characterData.getFloat("max_health", 100f),
            characterData.getFloat("speed", 5f),
            ability1,
            com.badlogic.gdx.Input.Keys.SPACE,
            null, // Segunda habilidad
            -1,
            idle.getKeyFrame(0)
        );

        profile.idle = idle;
        profile.down = down;
        profile.up = up;
        profile.left = left;
        profile.right = right;

        return profile;
    }

    private static Animation<TextureRegion> createAnim(String path, int frameSize, float frameDuration) {
        try {
            TextureRegion fullRegion = Assets.getTexture(path);
            if (fullRegion == null) return null;

            // Extraemos la textura del Atlas para poder trocear el spritesheet
            Texture tex = fullRegion.getTexture();
            TextureRegion[][] tmp = TextureRegion.split(tex, frameSize, frameSize);

            if (tmp.length > 0 && tmp[0].length > 0) {
                return new Animation<TextureRegion>(frameDuration, tmp[0]);
            }
        } catch (Exception e) {
            Gdx.app.error("CharacterFactory", "Fallo al procesar spritesheet: " + path);
        }
        return null;
    }
}
