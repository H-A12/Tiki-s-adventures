package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.abilities.Ability;
import com.tikisadventure.abilities.DashAbility;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.effects.EffectManager;

public class CharacterFactory {

    public static CharacterProfile create(CharacterType type, Weapon.ProjectileCreator projectileCreator,
                                          EffectManager effectManager) {
        String folder = type.name.toLowerCase() + "/";

        Animation<TextureRegion> idleAnim  = createAnim(folder + "idle.png",  16, 0.15f);
        Animation<TextureRegion> downAnim  = createAnim(folder + "down.png",  16, 0.15f);
        Animation<TextureRegion> upAnim    = createAnim(folder + "up.png",    16, 0.15f);
        Animation<TextureRegion> leftAnim  = createAnim(folder + "left.png",  16, 0.15f);
        Animation<TextureRegion> rightAnim = createAnim(folder + "right.png", 16, 0.15f);

        TextureRegion initialFrame = idleAnim.getKeyFrame(0);

        Ability ability1 = createAbility(type.ability1Class, projectileCreator, effectManager);
        Ability ability2 = createAbility(type.ability2Class, projectileCreator, effectManager);

        CharacterProfile profile = new CharacterProfile(
            type.name,
            type.maxHealth,
            type.speed,
            ability1,
            type.ability1Key,
            ability2,
            type.ability2Key,
            initialFrame
        );

        profile.idle = idleAnim;
        profile.down = downAnim;
        profile.up = upAnim;
        profile.left = leftAnim;
        profile.right = rightAnim;

        return profile;
    }

    private static Ability createAbility(Class<? extends Ability> abilityClass,
                                         Weapon.ProjectileCreator projectileCreator,
                                         EffectManager effectManager) {
        if (abilityClass == null) return null;

        try {
            // Eliminada la lógica de TurretAbility
            if (abilityClass == DashAbility.class) {
                return new DashAbility();
            }

            // Intento genérico para futuras habilidades sin parámetros especiales
            return abilityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Gdx.app.error("CharacterFactory", "Error creando habilidad: " + abilityClass.getName());
            return null;
        }
    }

    private static Animation<TextureRegion> createAnim(String path, int frameSize, float frameDuration) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("CharacterFactory", "Archivo no encontrado: " + path);
            return null; // Evita crash si falta el archivo
        }

        Texture tex = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(tex, frameSize, frameSize);
        return new Animation<TextureRegion>(frameDuration, tmp[0]);
    }
}
