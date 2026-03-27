package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.math.MathUtils;

public class EffectManager {

    private final Array<GenericParticle> activeParticles = new Array<>();
    private final Pool<GenericParticle> particlePool;
    private ObjectMap<EffectType, TextureRegion> textures = new ObjectMap<>();

    public EffectManager(int maxParticles) {
        particlePool = new Pool<GenericParticle>(maxParticles) {
            @Override
            protected GenericParticle newObject() {
                return new GenericParticle();
            }
        };

        // CARGA AUTOMÁTICA: Recorre el Enum y carga las texturas definidas allí
        for (EffectType type : EffectType.values()) {
            try {
                Texture tex = new Texture(type.textureName);
                textures.put(type, new TextureRegion(tex));
            } catch (Exception e) {
                System.out.println("Error cargando textura para: " + type + " -> " + type.textureName);
            }
        }
    }

    public void spawnEffect(EffectType type, Vector2 pos, Vector2 direction) {
        if (type == EffectType.EXPLOSION_HUMO) {
            for (int i = 0; i < 15; i++) {
                Vector2 randomDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                spawnSingleParticle(type, pos, randomDir);
            }
            return;
        }
        spawnSingleParticle(type, pos, direction);
    }

    private void spawnSingleParticle(EffectType type, Vector2 pos, Vector2 direction) {
        GenericParticle p = particlePool.obtain();
        if (p != null) {
            TextureRegion tex = textures.get(type);
            if (tex != null) {
                p.init(pos, direction, type, tex);
                activeParticles.add(p);
            } else {
                particlePool.free(p);
            }
        }
    }

    public void update(float delta) {
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
