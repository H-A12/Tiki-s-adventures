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
        // Inicializamos el Pool para reutilizar objetos y no saturar la memoria
        particlePool = new Pool<GenericParticle>(maxParticles) {
            @Override
            protected GenericParticle newObject() {
                return new GenericParticle();
            }
        };

        // CARGA AUTOMÁTICA de texturas basadas en el Enum
        for (EffectType type : EffectType.values()) {
            try {
                // Nota: En un proyecto real, usa un AssetManager para evitar duplicados de Texture
                Texture tex = new Texture(type.textureName);
                textures.put(type, new TextureRegion(tex));
            } catch (Exception e) {
                System.out.println("Error cargando textura para: " + type + " -> " + type.textureName);
            }
        }
    }

    /**
     * Spawnea un efecto. Si el tipo es EXPLOSION_HUMO, genera la explosión compuesta.
     */
    public void spawnEffect(EffectType type, Vector2 pos, Vector2 direction) {
        if (type == EffectType.EXPLOSION_HUMO) {
            // 1. DESTELLO (FLASH) - Uno solo en el centro, muy rápido
            spawnSingleParticle(EffectType.EXPLOSION_FLASH, pos, new Vector2(0, 0));

            // 2. HUMO - Varias nubes que se expanden en direcciones aleatorias
            for (int i = 0; i < 8; i++) {
                Vector2 randomDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                // Añadimos un pequeño offset a la posición para que no salgan todas del mismo píxel
                Vector2 offsetPos = new Vector2(pos).add(MathUtils.random(-0.2f, 0.2f), MathUtils.random(-0.2f, 0.2f));
                spawnSingleParticle(EffectType.EXPLOSION_HUMO, offsetPos, randomDir.scl(0.5f));
            }

            // 3. CHISPAS - Muchas partículas pequeñas con mucha velocidad y física
            for (int i = 0; i < 15; i++) {
                Vector2 sparkDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                sparkDir.scl(MathUtils.random(3f, 6f)); // Velocidad de salida alta
                spawnSingleParticle(EffectType.EXPLOSION_CHISPA, pos, sparkDir);
            }
            return;
        }

        // Si no es una explosión, spawnea la partícula individual (Casquillos, Trails, etc.)
        spawnSingleParticle(type, pos, direction);
    }

    private void spawnSingleParticle(EffectType type, Vector2 pos, Vector2 direction) {
        GenericParticle p = particlePool.obtain();
        if (p != null) {
            TextureRegion tex = textures.get(type);
            if (tex != null) {
                // Inicializamos la partícula con sus datos y textura
                p.init(pos, direction, type, tex);
                activeParticles.add(p);
            } else {
                // Si no hay textura, devolvemos la partícula al pool para no perderla
                particlePool.free(p);
            }
        }
    }

    public void update(float delta) {
        for (int i = activeParticles.size - 1; i >= 0; i--) {
            GenericParticle p = activeParticles.get(i);
            p.update(delta);

            // Si la partícula ha muerto (lifeTimer <= 0), la devolvemos al pool
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
