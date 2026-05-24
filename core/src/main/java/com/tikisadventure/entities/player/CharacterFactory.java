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

//Fábrica singleton que construye CharacterProfile desde JSON (data/player_config.json).
//Crea animaciones, habilidades (Ability) y asigna teclas. Soporta modo dios.
public class CharacterFactory {

    private JsonValue characterData;
    private int defaultFrameSize = 16;
    private float defaultFrameDuration = 0.15f;

    private static CharacterFactory instance;

    //Obtener instancia singleton
    public static CharacterFactory getInstance() {
        if (instance == null) {
            instance = new CharacterFactory();
        }
        return instance;
    }

    //Reiniciar singleton (para recargar configuración)
    public static void resetInstance() {
        instance = null;
    }

    public void setAnimationSettings(int frameSize, float frameDuration) {
        this.defaultFrameSize = frameSize;
        this.defaultFrameDuration = frameDuration;
    }

    //Cargar JSON de configuración de personajes una sola vez
    private void loadConfig() {
        if (characterData == null) {
            try {
                characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
            } catch (Exception e) {
            }
        }
    }

    //Convertir nombre de tecla del JSON a constante de Input.Keys
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
            return Input.Keys.SPACE;
        }
        return key;
    }

    //Construir CharacterProfile: animaciones, habilidades, stats y modo dios
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
            return null;
        }

        int frameSize = characterJson.getInt("frameSize", defaultFrameSize);
        float frameDuration = characterJson.getFloat("frameDuration", defaultFrameDuration);

        String atlasName = characterJson.getString("texturePath").replace(".png", "").toLowerCase();
        String pathPrefix = "player_assets/" + atlasName + "/";

        Animation<TextureRegion> idleAnim  = createAnim(atlasName, pathPrefix + "idle",  frameSize, frameDuration);
        Animation<TextureRegion> downAnim  = createAnim(atlasName, pathPrefix + "down",  frameSize, frameDuration);
        Animation<TextureRegion> upAnim    = createAnim(atlasName, pathPrefix + "up",    frameSize, frameDuration);
        Animation<TextureRegion> leftAnim  = createAnim(atlasName, pathPrefix + "left",  frameSize, frameDuration);
        Animation<TextureRegion> rightAnim = createAnim(atlasName, pathPrefix + "right", frameSize, frameDuration);

        TextureRegion initialFrame = (idleAnim != null) ? idleAnim.getKeyFrame(0) : null;


        JsonValue ab1Json = characterJson.get("ability1");
        JsonValue ab2Json = characterJson.get("ability2");

        Ability ability1 = createAbility(ab1Json, projectileCreator, effectManager);
        int key1 = parseKey(ab1Json != null && ab1Json.has("key") ? ab1Json.getString("key") : "SHIFT_LEFT");
        String currentAb1Id = ab1Json != null && ab1Json.has("id") ? ab1Json.getString("id") : null;

        Ability ability2 = createAbility(ab2Json, projectileCreator, effectManager);
        int key2 = parseKey(ab2Json != null && ab2Json.has("key") ? ab2Json.getString("key") : "MOUSE_RIGHT");
        String currentAb2Id = ab2Json != null && ab2Json.has("id") ? ab2Json.getString("id") : null;

        String startingWeapon = characterJson.getString("startingWeapon", null);

        if (com.tikisadventure.core.GameSession.godMode) {

            if (com.tikisadventure.core.GameSession.godModeAbility1Id != null) {
                currentAb1Id = com.tikisadventure.core.GameSession.godModeAbility1Id;
                ability1 = com.tikisadventure.combat.abilities.AbilityFactory.create(currentAb1Id, projectileCreator, effectManager);
            }

            if (com.tikisadventure.core.GameSession.godModeAbility2Id != null) {
                currentAb2Id = com.tikisadventure.core.GameSession.godModeAbility2Id;
                ability2 = com.tikisadventure.combat.abilities.AbilityFactory.create(currentAb2Id, projectileCreator, effectManager);
            }
        }

        String nameAb1 = "---";
        String nameAb2 = "---";
        try {
            JsonValue abilitiesData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));
            if (currentAb1Id != null && abilitiesData.has(currentAb1Id)) nameAb1 = abilitiesData.get(currentAb1Id).getString("name", "---");
            if (currentAb2Id != null && abilitiesData.has(currentAb2Id)) nameAb2 = abilitiesData.get(currentAb2Id).getString("name", "---");
        } catch (Exception e) {
        }

        float finalHealth = characterJson.getFloat("maxHealth");
        if (com.tikisadventure.core.GameSession.godMode) {
            finalHealth = com.tikisadventure.core.GameSession.godModeHealthValue;
        }

        float finalSpeed = characterJson.getFloat("speed");
        if (com.tikisadventure.core.GameSession.godMode) {
            finalSpeed = com.tikisadventure.core.GameSession.godModeSpeedValue;
        }

        CharacterProfile profile = new CharacterProfile(
            characterJson.getString("name"),
            finalHealth,
            finalSpeed,
            startingWeapon,
            ability1,
            key1,
            ability2,
            key2,
            initialFrame
        );

        profile.ability1Name = nameAb1;
        profile.ability2Name = nameAb2;

        profile.idle = idleAnim;
        profile.down = downAnim;
        profile.up = upAnim;
        profile.left = leftAnim;
        profile.right = rightAnim;

        return profile;
    }

    //Obtener animación idle de un personaje desde su ID
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
                String pathPrefix = "player_assets/" + atlasName + "/";
                return createAnim(atlasName, pathPrefix + "idle", frameSize, frameDuration);
            }
        }
        return null;
    }

    //Crear habilidad desde JSON (por ID o por nombre de clase)
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
            return null;
        }
    }

    //Crear animación dividiendo una tira de sprites en frames individuales
    private Animation<TextureRegion> createAnim(String atlasName, String regionName, int frameSize, float frameDuration) {
        TextureRegion stripRegion = Assets.getRegion(atlasName, regionName);

        if (stripRegion == null) {
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
