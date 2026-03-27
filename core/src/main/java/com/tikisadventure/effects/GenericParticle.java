package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class GenericParticle implements Poolable {

    // Estado
    private Vector2 position = new Vector2();
    private Vector2 velocity = new Vector2();
    private float rotation;
    private float rotationalVelocity;

    private float lifeTime;
    private float maxLifeTime;
    private boolean isAlive;

    // Configuración visual y física
    private TextureRegion texture;
    private float size;
    private boolean hasPhysics;
    private boolean fadeOut;
    private float groundY;

    // Constantes físicas
    private static final float GRAVITY = -15f;

    public GenericParticle() {
        this.isAlive = false;
    }

    /**
     * Inicializa la partícula usando la configuración del EffectType.
     */
    public void init(Vector2 spawnPos, Vector2 direction, EffectType type, TextureRegion tex) {
        this.position.set(spawnPos);
        this.texture = tex;
        this.size = type.baseSize;
        this.maxLifeTime = type.lifeTime * MathUtils.random(0.8f, 1.2f);
        this.lifeTime = 0;
        this.hasPhysics = type.hasPhysics;
        this.fadeOut = type.fadeOut;
        this.isAlive = true;

        // --- Lógica específica según el tipo ---

        if (type == EffectType.HUELLA_PISADA) {
            // Estática y orientada según el movimiento del personaje
            this.velocity.setZero();
            this.rotation = direction.angleDeg();
            this.rotationalVelocity = 0;
        } else if (type == EffectType.CASQUILLO_PISTOLA || type == EffectType.CASQUILLO_ESCOPETA) {
            // Lógica de expulsión de casquillos con rebote
            this.groundY = spawnPos.y - 0.4f;
            Vector2 ejectionDir = new Vector2(direction).rotateDeg(type.ejectionAngle);
            float speed = MathUtils.random(3f, 6f);
            this.velocity.set(ejectionDir).scl(speed);
            this.velocity.y += MathUtils.random(2f, 4f);
            this.rotation = MathUtils.random(0, 360);
            this.rotationalVelocity = MathUtils.random(-720f, 720f);
        } else if (type == EffectType.CHISPA_IMPACTO) {
            // Salen despedidas hacia afuera
            this.velocity.set(direction).scl(MathUtils.random(5f, 10f));
            this.rotation = MathUtils.random(0, 360);
            this.rotationalVelocity = MathUtils.random(-360f, 360f);
        }
    }

    @Override
    public void reset() {
        isAlive = false;
        position.setZero();
        velocity.setZero();
        texture = null;
    }

    public void update(float delta) {
        if (!isAlive) return;

        lifeTime += delta;
        if (lifeTime >= maxLifeTime) {
            isAlive = false;
            return;
        }

        if (hasPhysics) {
            velocity.y += GRAVITY * delta;
            position.mulAdd(velocity, delta);

            if (position.y <= groundY && velocity.y < 0) {
                position.y = groundY;
                velocity.y = -velocity.y * 0.4f;
                velocity.x *= 0.8f;
                rotationalVelocity *= 0.6f;
                if (Math.abs(velocity.y) < 0.2f) {
                    velocity.setZero();
                    rotationalVelocity = 0;
                }
            }
        } else {
            position.mulAdd(velocity, delta);
        }

        rotation += rotationalVelocity * delta;
    }

    public void render(Batch batch) {
        if (!isAlive || texture == null) return;

        float alpha = 1.0f;
        if (fadeOut) {
            float fadeStart = maxLifeTime * 0.8f;
            if (lifeTime > fadeStart) {
                alpha = 1.0f - ((lifeTime - fadeStart) / (maxLifeTime - fadeStart));
            }
        }

        batch.setColor(1, 1, 1, alpha);
        batch.draw(texture, position.x - size/2, position.y - size/2, size/2, size/2, size, size, 1f, 1f, rotation);
        batch.setColor(1, 1, 1, 1);
    }

    public boolean isAlive() { return isAlive; }
}
