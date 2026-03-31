package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;
import com.tikisadventure.core.Assets;
import com.tikisadventure.combat.abilities.Ability;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.effects.EffectManager;

public class CharacterFactory {

    private static JsonValue characterData;

    private static void loadConfig() {
        if (characterData == null) {
            try {
                characterData = new JsonReader().parse(Gdx.files.internal("player_config.json"));
            } catch (Exception e) {
                Gdx.app.error("CharacterFactory", "Error crítico cargando player_config.json", e);
            }
        }
    }

    private static int parseKey(String keyName) {
        if (keyName == null || keyName.isEmpty()) return Input.Keys.UNKNOWN;
        int key = Input.Keys.valueOf(keyName.toUpperCase());

        if (key == -1) {
            if (keyName.equalsIgnoreCase("SPACE")) return Input.Keys.SPACE;
            if (keyName.equalsIgnoreCase("Q")) return Input.Keys.Q;
            if (keyName.equalsIgnoreCase("E")) return Input.Keys.E;
            if (keyName.equalsIgnoreCase("SHIFT_LEFT")) return Input.Keys.SHIFT_LEFT;
            if (keyName.equalsIgnoreCase("CONTROL_LEFT")) return Input.Keys.CONTROL_LEFT;
            Gdx.app.error("CharacterFactory", "Mapeo de tecla inválido: [" + keyName + "]. Usando SPACE por defecto.");
            return Input.Keys.SPACE;
        }
        return key;
    }

    public static CharacterProfile create(String characterId, ProjectileCreator projectileCreator,
                                          EffectManager effectManager) {
        loadConfig();
        if (characterData == null) return null;

        JsonValue characterJson = null;
        for (JsonValue charEntry : characterData.get("characters")) {
            if (charEntry.getString("id").equals(characterId)) {
                characterJson = charEntry;
                break;
            }
        }

        if (characterJson == null) {
            Gdx.app.error("CharacterFactory", "Personaje no encontrado en el JSON: " + characterId);
            return null;
        }

        String folder = characterJson.getString("name").toLowerCase() + "/";

        Animation<TextureRegion> idleAnim  = createAnim(folder + "idle.png",  16, 0.15f);
        Animation<TextureRegion> downAnim  = createAnim(folder + "down.png",  16, 0.15f);
        Animation<TextureRegion> upAnim    = createAnim(folder + "up.png",    16, 0.15f);
        Animation<TextureRegion> leftAnim  = createAnim(folder + "left.png",  16, 0.15f);
        Animation<TextureRegion> rightAnim = createAnim(folder + "right.png", 16, 0.15f);

        TextureRegion initialFrame = (idleAnim != null) ? idleAnim.getKeyFrame(0) : null;

        JsonValue ab1Json = characterJson.get("ability1");
        JsonValue ab2Json = characterJson.get("ability2");

        Ability ability1 = createAbility(ab1Json, projectileCreator, effectManager);
        int key1 = parseKey(ab1Json != null ? ab1Json.getString("key") : "UNKNOWN");

        Ability ability2 = createAbility(ab2Json, projectileCreator, effectManager);
        int key2 = parseKey(ab2Json != null ? ab2Json.getString("key") : "UNKNOWN");

        CharacterProfile profile = new CharacterProfile(
            characterJson.getString("name"),
            characterJson.getFloat("maxHealth"),
            characterJson.getFloat("speed"),
            ability1,
            key1,
            ability2,
            key2,
            initialFrame
        );

        profile.idle = idleAnim;
        profile.down = downAnim;
        profile.up = upAnim;
        profile.left = leftAnim;
        profile.right = rightAnim;

        return profile;
    }

    private static Ability createAbility(JsonValue abilityJson,
                                         ProjectileCreator projectileCreator,
                                         EffectManager effectManager) {
        if (abilityJson == null) return null;
        String className = abilityJson.getString("class");
        try {
            Class<?> clazz = Class.forName(className);
            return (Ability) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Gdx.app.error("CharacterFactory", "Error instanciando clase de habilidad: " + className);
            return null;
        }
    }

    private static Animation<TextureRegion> createAnim(String path, int frameSize, float frameDuration) {
        String regionName = path.replace(".png", "");
        TextureRegion stripRegion = Assets.getRegion(regionName);

        if (stripRegion == null) {
            Gdx.app.error("CharacterFactory", "Sprite no encontrado en atlas: " + regionName);
            return null;
        }

        int frameCount = stripRegion.getRegionWidth() / frameSize;
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(stripRegion, i * frameSize, 0, frameSize, frameSize);
        }
        return new Animation<TextureRegion>(frameDuration, frames);
    }
}
