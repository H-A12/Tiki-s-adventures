package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Disposable; // <--- Importante
import com.tikisadventure.assets.Assets;
import com.tikisadventure.entities.base.Entity;

public class EffectManager implements Disposable { // <--- Implementamos la interfaz

    private final Array<GenericParticle> activeParticles = new Array<>();
    private final Pool<GenericParticle> particlePool;

    // Para el parpadeo de daño (Damage Flash)
    private final Array<Entity> flashingEntities = new Array<>();
    private final Array<Float> flashTimers = new Array<>();

    public EffectManager(int maxParticles) {
        particlePool = new Pool<GenericParticle>(maxParticles) {
            @Override
            protected GenericParticle newObject() {
                return new GenericParticle();
            }
        };
    }

    private void spawnSingleParticle(EffectType type, Vector2 pos, Vector2 direction) {
        GenericParticle p = particlePool.obtain();
        TextureRegion tex = Assets.getTexture(type.textureName);

        if (p != null && tex != null) {
            p.init(pos, direction, type, tex);
            activeParticles.add(p);
        } else if (p != null) {
            particlePool.free(p);
        }
    }

    public void startDamageFlash(Entity entity) {
        if (!flashingEntities.contains(entity, true)) {
            flashingEntities.add(entity);
            flashTimers.add(0.12f);
        }
    }

    public boolean isEntityFlashing(Entity entity) {
        return flashingEntities.contains(entity, true);
    }

    public void update(float delta) {
        // 1. Actualizar Partículas
        for (int i = activeParticles.size - 1; i >= 0; i--) {
            GenericParticle p = activeParticles.get(i);
            p.update(delta);
            if (!p.isAlive()) {
                activeParticles.removeIndex(i);
                particlePool.free(p);
            }
        }

        // 2. Actualizar Timers de Flash
        for (int i = flashTimers.size - 1; i >= 0; i--) {
            float time = flashTimers.get(i) - delta;
            if (time <= 0) {
                flashTimers.removeIndex(i);
                flashingEntities.removeIndex(i);
            } else {
                flashTimers.set(i, time);
            }
        }
    }

    public void spawnEffect(EffectType type, Vector2 pos, Vector2 direction) {
        spawnSingleParticle(type, pos, direction);
    }

    public void render(Batch batch) {
        for (GenericParticle p : activeParticles) {
            p.render(batch);
        }
    }

    /**
     * MÉTODO AÑADIDO: Limpia los recursos y ayuda al recolector de basura.
     * Ahora GameScreen podrá llamar a effectManager.dispose() sin error.
     */
    @Override
    public void dispose() {
        activeParticles.clear();
        particlePool.clear();
        flashingEntities.clear();
        flashTimers.clear();
    }
}
