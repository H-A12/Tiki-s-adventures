package com.tikisadventure.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class GenericParticle implements Poolable {

    private static final int EXPLOSION_FRAMES = 14;
    private static final float FRAME_TIME = 0.5f / EXPLOSION_FRAMES;

    private Vector2 position = new Vector2();
    private Vector2 velocity = new Vector2();
    private float rotation;
    private float rotationalVelocity;

    private float lifeTime;
    private float maxLifeTime;
    private boolean isAlive;

    private TextureRegion texture;
    private float size;
    private float currentSize; // Para manejar el escalado (crecimiento)
    private boolean hasPhysics;
    private boolean fadeOut;
    private float groundY;
    private float friction;

    // Colores
    private Color startColor;
    private Color endColor;
    private Color currentColor = new Color(); // Para evitar GC
    
    // Nuevo: Guardamos el tipo para lógica específica en el update
    private EffectType type;
    private int currentFrame;
    private TextureRegion[] spriteFrames;

    private static final float GRAVITY = -15f;

    public GenericParticle() {
        this.isAlive = false;
    }

    public void init(Vector2 spawnPos, Vector2 direction, EffectType type, TextureRegion tex) {
        this.type = type;
        this.position.set(spawnPos);
        this.texture = tex;
        // Añado variación aleatoria de tamaño (entre 80% y 120%)
        this.size = type.baseSize * MathUtils.random(0.8f, 1.2f);
        this.currentSize = size;
        this.maxLifeTime = type.lifeTime * MathUtils.random(0.8f, 1.2f);
        this.lifeTime = 0;
        this.hasPhysics = type.hasPhysics;
        this.fadeOut = type.fadeOut;
        this.isAlive = true;
        this.friction = type.friction;
        this.startColor = type.startColor;
        this.endColor = type.endColor;
        this.currentColor.set(startColor);

        // Extraer frames del spritesheet para explosiones
        if (type == EffectType.EXPLOSION_SPRITESHEET && tex != null) {
            int frameWidth = tex.getRegionWidth() / EXPLOSION_FRAMES;
            int frameHeight = tex.getRegionHeight();
            if (frameWidth > 0 && frameHeight > 0 && tex.getRegionWidth() >= frameWidth) {
                TextureRegion[][] frames = tex.split(frameWidth, frameHeight);
                if (frames != null && frames.length > 0 && frames[0].length > 0) {
                    spriteFrames = frames[0];
                }
            }
            currentFrame = 0;
        } else {
            spriteFrames = null;
            currentFrame = 0;
        }

        // --- LÓGICA POR TIPO ---

        if (type == EffectType.EXPLOSION_FLASH) {
            this.velocity.setZero();
            this.rotation = MathUtils.random(0, 360);
            this.rotationalVelocity = 0;
        } else if (type == EffectType.EXPLOSION_HUMO) {
            // El humo sale despacio y rota lento
            this.velocity.set(direction).scl(MathUtils.random(0.5f, 1.5f));
            this.rotation = MathUtils.random(0, 360);
            this.rotationalVelocity = MathUtils.random(-40f, 40f);
        } else if (type == EffectType.EXPLOSION_CHISPA || type == EffectType.EXPLOSION_SLIME) {
            // Las chispas tienen gravedad y rebotan
            this.groundY = spawnPos.y - MathUtils.random(1f, 3f); // Suelo aleatorio para profundidad
            this.velocity.set(direction);
            this.rotation = MathUtils.random(0, 360);
            this.rotationalVelocity = MathUtils.random(-900f, 900f);
        } else if (type == EffectType.HUELLA_PISADA) {
            this.velocity.setZero();
            this.rotation = direction.angleDeg();
            this.rotationalVelocity = 0;
        } else if (type.name().startsWith("MUZZLE_FLASH")) {
            // Muzzleflash: crece rápido, luego desaparece
            this.velocity.setZero();
            this.rotation = direction.angleDeg();
            this.rotationalVelocity = 0;
        } else if (type.name().startsWith("CASQUILLO")) {
            // Casquillos: expelidos desde el arma con física
            this.groundY = spawnPos.y - 0.3f;
            Vector2 ejectionDir = new Vector2(direction).rotateDeg(type.ejectionAngle);
            this.velocity.set(ejectionDir).scl(MathUtils.random(3f, 6f));
            this.velocity.y += MathUtils.random(2f, 4f);
            this.rotation = MathUtils.random(0, 360);
            this.rotationalVelocity = MathUtils.random(-720f, 720f);
        } else if (type == EffectType.EXPLOSION_SPRITESHEET) {
            // Explosión con spritesheet: без физики, без fade
            this.velocity.setZero();
            this.rotation = direction.angleDeg();
            this.rotationalVelocity = 0;
            this.currentFrame = 0;
        } else {
            // Comportamiento genérico para otros (como Trails)
            this.velocity.set(direction);
            this.rotation = direction.angleDeg();
            this.rotationalVelocity = 0;
        }
    }

    @Override
    public void reset() {
        isAlive = false;
        position.setZero();
        velocity.setZero();
        texture = null;
        type = null;
        currentFrame = 0;
        spriteFrames = null;
    }

    public void update(float delta) {
        if (!isAlive) return;

        lifeTime += delta;
        float progress = MathUtils.clamp(lifeTime / maxLifeTime, 0f, 1f);

        if (lifeTime >= maxLifeTime) {
            // Para animación de spritesheet, esperar hasta que termine el último frame
            if (type == EffectType.EXPLOSION_SPRITESHEET) {
                if (currentFrame >= EXPLOSION_FRAMES - 1) {
                    isAlive = false;
                    return;
                }
            } else {
                isAlive = false;
                return;
            }
        }

        // Interpolación de color
        currentColor.set(startColor).lerp(endColor, progress);

        // --- LÓGICA DE ESCALADO (CRECIMIENTO) ---
        if (type == EffectType.EXPLOSION_HUMO) {
            // El humo crece hasta el doble de su tamaño base
            currentSize = MathUtils.lerp(size, size * 2.5f, progress);
        } else if (type == EffectType.EXPLOSION_FLASH) {
            // El flash crece muy rápido y luego encoge
            currentSize = size * (1.0f + progress);
        } else if (type.name().startsWith("MUZZLE_FLASH")) {
            // Muzzleflash: crece rápidamente y luego encoge
            if (progress < 0.3f) {
                currentSize = MathUtils.lerp(size, size * 1.5f, progress / 0.3f);
            } else {
                currentSize = MathUtils.lerp(size * 1.5f, size * 0.5f, (progress - 0.3f) / 0.7f);
            }
        } else {
            currentSize = size;
        }

        // --- ANIMACIÓN DE SPRITESHEET ---
        if (type == EffectType.EXPLOSION_SPRITESHEET && spriteFrames != null) {
            float frameFloat = lifeTime / FRAME_TIME;
            currentFrame = (int) frameFloat;
            if (currentFrame >= EXPLOSION_FRAMES) {
                currentFrame = EXPLOSION_FRAMES - 1;
            }
        }

        // --- FÍSICA ---
        if (hasPhysics) {
            velocity.y += GRAVITY * delta;
            velocity.scl(friction); // Aplicamos fricción
            position.mulAdd(velocity, delta);

            // Rebote simple
            if (position.y <= groundY && velocity.y < 0) {
                position.y = groundY;
                velocity.y = -velocity.y * 0.4f; // Pierde energía al botar
                velocity.x *= 0.8f;
                rotationalVelocity *= 0.6f;
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
            alpha = 1.0f - (lifeTime / maxLifeTime);
        }

        batch.setColor(currentColor.r, currentColor.g, currentColor.b, alpha);
        
        // Renderizar según tipo
        if (type == EffectType.EXPLOSION_SPRITESHEET && spriteFrames != null) {
            int frameIndex = Math.min(currentFrame, spriteFrames.length - 1);
            TextureRegion frame = spriteFrames[frameIndex];
            // Sin tint para spritesheet de explosión
            batch.setColor(1, 1, 1, 1);
            batch.draw(
                frame,
                position.x - currentSize/2, position.y - currentSize/2,
                currentSize/2, currentSize/2,
                currentSize, currentSize,
                1f, 1f,
                rotation
            );
        } else {
            batch.draw(
                texture,
                position.x - currentSize/2, position.y - currentSize/2,
                currentSize/2, currentSize/2,
                currentSize, currentSize,
                1f, 1f,
                rotation
            );
        }
        batch.setColor(1, 1, 1, 1);
    }

    public boolean isAlive() { return isAlive; }
}
