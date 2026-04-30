package com.tikisadventure.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.systems.events.Event;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.EventListener;
import com.tikisadventure.systems.events.HitEvent;
import com.tikisadventure.systems.events.FiredEvent;

public class EffectManager {

    public static class EffectConfig {
        public String tex;
        public float size;
        public float life;
        public boolean physics;
        public boolean fade;
        public float angle;
        public float friction;
        public boolean isSpritesheet = false;
        public boolean attached = false;
        public boolean randomRotation = false;
        public float rotationalVelocity = 0f;
        public int frameCount = 1;
        public Color startColor;
        public Color endColor;
        public TextureRegion region;
        public float grow = 1.0f;
        public float bounce = 0.4f;
        public float floorOffset = 0.3f;
        public float[] ejectSpeed = {3f, 6f};
        public float[] ejectBoost = {2f, 4f};
        public TextureRegion[] precalculatedFrames;
    }

    public static class ExplosionProfile {
        public String smoke;
        public String sparks;
        public String spritesheet = "EXPLOSION_SPRITESHEET";
    }

    private final Array<GenericParticle> activeParticles = new Array<>();
    private final Pool<GenericParticle> particlePool;
    private final ObjectMap<String, EffectConfig> effectConfigs = new ObjectMap<>();
    private final ObjectMap<String, ExplosionProfile> explosionProfiles = new ObjectMap<>();
    private final ObjectMap<String, Float> lastImpactTimes = new ObjectMap<>();
    private static final float IMPACT_COOLDOWN = 0.05f;

    private final Array<DelayedEffect> delayedEffects = new Array<>();

    private static class DelayedEffect {
        String type;
        Vector2 pos;
        Vector2 dir;
        float delay;
        Entity target;
    }

    private final EventListener<HitEvent> hitListener;
    private final EventListener<FiredEvent> firedListener;

    public EffectManager(int maxParticles) {
        particlePool = new Pool<GenericParticle>(maxParticles) {
            @Override
            protected GenericParticle newObject() {
                return new GenericParticle();
            }
        };

        loadConfig();

        hitListener = event -> {
            String key = (int)event.position.x + "," + (int)event.position.y;
            float currentTime = Gdx.graphics.getFrameId() * Gdx.graphics.getDeltaTime();

            if (lastImpactTimes.containsKey(key) && (currentTime - lastImpactTimes.get(key) < IMPACT_COOLDOWN)) {
                return;
            }
            lastImpactTimes.put(key, currentTime);
            spawnEffect("IMPACT_SPRITESHEET", event.position, new Vector2(0, 0), 0f, event.entity);
        };

        firedListener = event -> {
            if (event.effectType != null) {
                spawnEffect(event.effectType, event.position, event.direction, 0f, null);
            }
            if (event.muzzleFlashType != null) {
                spawnEffect(event.muzzleFlashType, event.position, event.direction, 0f, null);
            }
        };

        EventBus.subscribe(HitEvent.class, hitListener);
        EventBus.subscribe(FiredEvent.class, firedListener);
    }

    public void dispose() {
        for (int i = activeParticles.size - 1; i >= 0; i--) {
            particlePool.free(activeParticles.get(i));
        }
        activeParticles.clear();
        delayedEffects.clear();
        EventBus.unsubscribe(HitEvent.class, hitListener);
        EventBus.unsubscribe(FiredEvent.class, firedListener);
    }

    private void loadConfig() {
        JsonReader reader = new JsonReader();
        if (!Gdx.files.internal("data/effects_config.json").exists()) {
            Gdx.app.error("EffectManager", "Archivo no encontrado: data/effects_config.json");
            return;
        }
        JsonValue root = reader.parse(Gdx.files.internal("data/effects_config.json"));

        JsonValue effectsRoot = root.get("effects");
        for (JsonValue configJson : effectsRoot) {
            Gdx.app.log("EffectManager", "Loading effect: " + configJson.name());
            String id = configJson.name();
            EffectConfig config = new EffectConfig();
            config.tex = configJson.getString("tex");
            config.size = configJson.getFloat("size");
            config.life = configJson.getFloat("life");
            config.physics = configJson.getBoolean("physics");
            config.fade = configJson.getBoolean("fade");
            config.angle = configJson.getFloat("angle");
            config.friction = configJson.getFloat("friction");
            config.isSpritesheet = configJson.getBoolean("isSpritesheet", false);
            config.attached = configJson.getBoolean("attached", false);
            config.randomRotation = configJson.getBoolean("randomRotation", false);
            config.rotationalVelocity = configJson.getFloat("rotationalVelocity", 0f);
            config.frameCount = configJson.getInt("frameCount", 1);
            config.grow = configJson.getFloat("grow", 1.0f);
            config.bounce = configJson.getFloat("bounce", 0.4f);
            config.floorOffset = configJson.getFloat("floorOffset", 0.3f);
            JsonValue ejectSpeed = configJson.get("ejectSpeed");
            if (ejectSpeed != null) {
                config.ejectSpeed = new float[]{ejectSpeed.getFloat(0), ejectSpeed.getFloat(1)};
            }
            JsonValue ejectBoost = configJson.get("ejectBoost");
            if (ejectBoost != null) {
                config.ejectBoost = new float[]{ejectBoost.getFloat(0), ejectBoost.getFloat(1)};
            }

            JsonValue startColor = configJson.get("startColor");
            config.startColor = new Color(startColor.getFloat(0), startColor.getFloat(1), startColor.getFloat(2), startColor.getFloat(3));
            JsonValue endColor = configJson.get("endColor");
            config.endColor = new Color(endColor.getFloat(0), endColor.getFloat(1), endColor.getFloat(2), endColor.getFloat(3));

            config.region = Assets.getRegion("shared", config.tex, config.isSpritesheet);

            if (config.isSpritesheet && config.region != null) {
                int frameWidth = config.region.getRegionWidth() / config.frameCount;
                int frameHeight = config.region.getRegionHeight();
                if (frameWidth > 0 && frameHeight > 0) {
                    TextureRegion[][] tmp = config.region.split(frameWidth, frameHeight);
                    config.precalculatedFrames = tmp[0];
                }
            }
            effectConfigs.put(id, config);
        }

        JsonValue profilesRoot = root.get("explosion_profiles");
        if (profilesRoot != null) {
            for (JsonValue profileJson : profilesRoot) {
                ExplosionProfile profile = new ExplosionProfile();
                profile.smoke = profileJson.getString("smoke");
                profile.sparks = profileJson.getString("sparks");
                profile.spritesheet = profileJson.getString("spritesheet", "EXPLOSION_SPRITESHEET");
                explosionProfiles.put(profileJson.name(), profile);
            }
        }
    }

    public ExplosionProfile getExplosionProfile(String name) {
        return explosionProfiles.get(name);
    }

    public void spawnEffect(String type, Vector2 pos, Vector2 direction) {
        spawnEffect(type, pos, direction, 0f, null);
    }

    public void spawnEffect(String type, Vector2 pos, Vector2 direction, float delay, Entity target) {
        if (delay > 0) {
            DelayedEffect de = new DelayedEffect();
            de.type = type;
            de.pos = new Vector2(pos);
            de.dir = new Vector2(direction);
            de.delay = delay;
            de.target = target;
            delayedEffects.add(de);
            return;
        }

        spawnSingleParticle(type, pos, direction, target);
    }

    public void spawnSingleParticle(String type, Vector2 pos, Vector2 direction) {
        spawnSingleParticle(type, pos, direction, null);
    }

    public void spawnSingleParticle(String type, Vector2 pos, Vector2 direction, Entity target) {
        GenericParticle p = particlePool.obtain();
        if (p != null) {
            EffectConfig config = effectConfigs.get(type);
            if (config != null && config.region != null) {
                p.init(pos, direction, config, config.region, target);
                activeParticles.add(p);
            } else {
                particlePool.free(p);
            }
        }
    }

    public void update(float delta) {
        for (int i = delayedEffects.size - 1; i >= 0; i--) {
            DelayedEffect de = delayedEffects.get(i);
            de.delay -= delta;
            if (de.delay <= 0) {
                spawnEffect(de.type, de.pos, de.dir, 0f, de.target);
                delayedEffects.removeIndex(i);
            }
        }

        for (int i = activeParticles.size - 1; i >= 0; i--) {
            GenericParticle p = activeParticles.get(i);
            p.update(delta);
            if (!p.isAlive()) {
                activeParticles.removeIndex(i);
                particlePool.free(p);
            }
        }
    }

    public void render(Batch batch) {
        for (GenericParticle p : activeParticles) {
            p.render(batch);
        }
    }

    public void spawnEffectCustom(EffectConfig config, TextureRegion tex, Vector2 position, Vector2 direction) {
        if (config == null || tex == null) return;
        GenericParticle p = particlePool.obtain();
        p.init(position, direction, config, tex, null);
        activeParticles.add(p);
    }
}
