package com.tikisadventure.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.math.MathUtils;
import com.tikisadventure.core.Assets;

import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.HitEvent;
import com.tikisadventure.systems.events.FiredEvent;

public class EffectManager {

    private final Array<GenericParticle> activeParticles = new Array<>();
    private final Pool<GenericParticle> particlePool;
    private ObjectMap<EffectType, TextureRegion> textures = new ObjectMap<>();
    
    // Cola de efectos retardados
    private final Array<DelayedEffect> delayedEffects = new Array<>();

    private static class DelayedEffect {
        EffectType type;
        Vector2 pos;
        Vector2 dir;
        float delay;
    }

    public EffectManager(int maxParticles) {
        particlePool = new Pool<GenericParticle>(maxParticles) {
            @Override
            protected GenericParticle newObject() {
                return new GenericParticle();
            }
        };

        for (EffectType type : EffectType.values()) {
            TextureRegion region;
            if (type == EffectType.EXPLOSION_SPRITESHEET || type == EffectType.IMPACT_EFFECT) {
                region = Assets.getRegion("shared", type.textureName, true);
            } else {
                region = Assets.getRegion("shared", type.textureName);
            }
            textures.put(type, region);
        }
        
        // Un solo efecto de impacto para todos los tipos
        EventBus.subscribe(HitEvent.class, event -> {
            spawnEffect(EffectType.IMPACT_EFFECT, event.position, new Vector2(0, 0));
        });
        
        EventBus.subscribe(FiredEvent.class, event -> {
            if (event.effectType != null) {
                spawnEffect(event.effectType, event.position, event.direction);
            }
            if (event.muzzleFlashType != null) {
                spawnEffect(event.muzzleFlashType, event.position, event.direction);
            }
        });
    }

    public void spawnEffect(EffectType type, Vector2 pos, Vector2 direction) {
        spawnEffect(type, pos, direction, 0f);
    }
    
    public void spawnEffect(EffectType type, Vector2 pos, Vector2 direction, float delay) {
        if (delay > 0) {
            DelayedEffect de = new DelayedEffect();
            de.type = type;
            de.pos = new Vector2(pos);
            de.dir = new Vector2(direction);
            de.delay = delay;
            delayedEffects.add(de);
            return;
        }

        if (type == EffectType.EXPLOSION_HUMO) {
            spawnSingleParticle(EffectType.EXPLOSION_FLASH, pos, new Vector2(0, 0));
            for (int i = 0; i < 8; i++) {
                Vector2 randomDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                Vector2 offsetPos = new Vector2(pos).add(MathUtils.random(-0.2f, 0.2f), MathUtils.random(-0.2f, 0.2f));
                spawnSingleParticle(EffectType.EXPLOSION_HUMO, offsetPos, randomDir.scl(0.5f));
            }
            for (int i = 0; i < 15; i++) {
                Vector2 sparkDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                sparkDir.scl(MathUtils.random(3f, 6f));
                spawnSingleParticle(EffectType.EXPLOSION_CHISPA, pos, sparkDir);
            }
            return;
        }

        spawnSingleParticle(type, pos, direction);
    }

    private void spawnSingleParticle(EffectType type, Vector2 pos, Vector2 direction) {
        GenericParticle p = particlePool.obtain();
        if (p != null) {
            TextureRegion tex = textures.get(type);
            Gdx.app.log("EffectManager", "Spawn " + type + " tex=" + (tex != null ? tex.getRegionWidth() : "null"));
            if (tex != null) {
                p.init(pos, direction, type, tex);
                activeParticles.add(p);
            } else {
                particlePool.free(p);
            }
        }
    }

    public void update(float delta) {
        // Procesar efectos retardados
        for (int i = delayedEffects.size - 1; i >= 0; i--) {
            DelayedEffect de = delayedEffects.get(i);
            de.delay -= delta;
            if (de.delay <= 0) {
                spawnEffect(de.type, de.pos, de.dir);
                delayedEffects.removeIndex(i);
            }
        }

        // Procesar partículas
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
}
