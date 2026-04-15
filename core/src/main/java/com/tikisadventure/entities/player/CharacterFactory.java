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

    private JsonValue characterData;
    private int defaultFrameSize = 16;
    private float defaultFrameDuration = 0.15f;

    private static CharacterFactory instance;

    public static CharacterFactory getInstance() {
        if (instance == null) {
            instance = new CharacterFactory();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void setAnimationSettings(int frameSize, float frameDuration) {
        this.defaultFrameSize = frameSize;
        this.defaultFrameDuration = frameDuration;
    }

    private void loadConfig() {
        if (characterData == null) {
            try {
                characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
            } catch (Exception e) {
                Gdx.app.error("CharacterFactory", "Error crítico cargando data/player_config.json", e);
            }
        }
    }

    private int parseKey(String keyName) {
        if (keyName == null || keyName.isEmpty()) return Input.Keys.UNKNOWN;

        if (keyName.equalsIgnoreCase("MOUSE_LEFT")) return Input.Buttons.LEFT;
        if (keyName.equalsIgnoreCase("MOUSE_RIGHT")) return Input.Buttons.RIGHT;
        if (keyName.equalsIgnoreCase("MOUSE_MIDDLE")) return Input.Buttons.MIDDLE;

        int key = Input.Keys.valueOf(keyName.toUpperCase());

        if (key == -1) {
            if (keyName.equalsIgnoreCase("SPACE")) return Input.Keys.SPACE;
            if (keyName.equalsIgnoreCase("Q")) return Input.Keys.Q;
            if (keyName.equalsIgnoreCase("E")) return Input.Keys.E;
            if (keyName.equalsIgnoreCase("SHIFT_LEFT")) return Input.Keys.SHIFT_LEFT;
            if (keyName.equalsIgnoreCase("SHIFT")) return Input.Keys.SHIFT_LEFT;
            if (keyName.equalsIgnoreCase("CONTROL_LEFT")) return Input.Keys.CONTROL_LEFT;
            Gdx.app.error("CharacterFactory", "Mapeo de tecla inválido: [" + keyName + "]. Usando SPACE por defecto.");
            return Input.Keys.SPACE;
        }
        return key;
    }

    public CharacterProfile create(String characterId, ProjectileCreator projectileCreator,
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

        int frameSize = characterJson.getInt("frameSize", defaultFrameSize);
        float frameDuration = characterJson.getFloat("frameDuration", defaultFrameDuration);

        String atlasName = characterJson.getString("texturePath").replace(".png", "").toLowerCase();
        Gdx.app.log("CharacterFactory", "Cargando animaciones para: " + atlasName);

        Animation<TextureRegion> idleAnim  = createAnim(atlasName, "idle",  frameSize, frameDuration);
        Animation<TextureRegion> downAnim  = createAnim(atlasName, "down",  frameSize, frameDuration);
        Animation<TextureRegion> upAnim    = createAnim(atlasName, "up",    frameSize, frameDuration);
        Animation<TextureRegion> leftAnim  = createAnim(atlasName, "left",  frameSize, frameDuration);
        Animation<TextureRegion> rightAnim = createAnim(atlasName, "right", frameSize, frameDuration);

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

    public static Animation<TextureRegion> getCharacterIdleAnimation(String characterId) {
        return getInstance().getCharacterIdleAnimationInternal(characterId);
    }

    private Animation<TextureRegion> getCharacterIdleAnimationInternal(String characterId) {
        loadConfig();
        if (characterData == null) return null;

        for (JsonValue charEntry : characterData.get("characters")) {
            if (charEntry.getString("id").equals(characterId)) {
                int frameSize = charEntry.getInt("frameSize", defaultFrameSize);
                float frameDuration = charEntry.getFloat("frameDuration", defaultFrameDuration);
                String atlasName = charEntry.getString("texturePath").replace(".png", "").toLowerCase();
                return createAnim(atlasName, "idle", frameSize, frameDuration);
            }
        }
        return null;
    }

    private Ability createAbility(JsonValue abilityJson,
                                         ProjectileCreator projectileCreator,
                                         EffectManager effectManager) {
        if (abilityJson == null) return null;

        if (abilityJson.has("id")) {
            return com.tikisadventure.combat.abilities.AbilityFactory.create(abilityJson.getString("id"), projectileCreator, effectManager);
        }

        String className = abilityJson.getString("class");
        try {
            Class<?> clazz = Class.forName(className);
            return (Ability) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Gdx.app.error("CharacterFactory", "Error instanciando clase de habilidad: " + className);
            return null;
        }
    }

    private Animation<TextureRegion> createAnim(String atlasName, String regionName, int frameSize, float frameDuration) {
        TextureRegion stripRegion = Assets.getRegion(atlasName, regionName);

        if (stripRegion == null) {
            Gdx.app.error("CharacterFactory", "Sprite no encontrado en atlas " + atlasName + ": " + regionName);
            return null;
        }

        Gdx.app.log("CharacterFactory", "Cargada region: " + regionName + " | Tamaño: " + stripRegion.getRegionWidth() + "x" + stripRegion.getRegionHeight());

        int frameCount = stripRegion.getRegionWidth() / frameSize;
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(stripRegion, i * frameSize, 0, frameSize, frameSize);
        }
        return new Animation<TextureRegion>(frameDuration, frames);
    }
}
