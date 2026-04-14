package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.tikisadventure.entities.base.Entity;

public class GenericParticle implements Poolable {

    private Vector2 position = new Vector2();
    private Vector2 velocity = new Vector2();
    private float rotation;
    private float rotationalVelocity;

    private float lifeTime;
    private float maxLifeTime;
    private boolean isAlive;

    private TextureRegion texture;
    private float size;
    private float currentSize;
    private boolean hasPhysics;
    private boolean fadeOut;
    private float groundY;
    private float friction;

    private Color startColor;
    private Color endColor;
    private Color currentColor = new Color();
    
    private int currentFrame;
    private int totalFrames;
    private TextureRegion[] spriteFrames;
    
    private float grow;
    private float bounce;
    private float floorOffset;
    private float[] ejectSpeed;
    private float[] ejectBoost;
    private boolean isSpritesheet;

    private static final float GRAVITY = -15f;

    public GenericParticle() {
        this.isAlive = false;
    }

    private Entity target;
    private Vector2 offsetFromTarget = new Vector2();
    private boolean isAttached;

    public void init(Vector2 spawnPos, Vector2 direction, EffectManager.EffectConfig config, TextureRegion tex, Entity target) {
        this.position.set(spawnPos);
        this.texture = tex;
        this.size = config.size * MathUtils.random(0.8f, 1.2f);
        this.currentSize = size;
        this.maxLifeTime = config.life * MathUtils.random(0.8f, 1.2f);
        this.lifeTime = 0;
        this.hasPhysics = config.physics;
        this.fadeOut = config.fade;
        this.isAlive = true;
        this.friction = config.friction;
        this.startColor = config.startColor;
        this.endColor = config.endColor;
        this.currentColor.set(startColor);
        this.totalFrames = config.frameCount;
        this.grow = config.grow;
        this.bounce = config.bounce;
        this.floorOffset = config.floorOffset;
        this.ejectSpeed = config.ejectSpeed;
        this.ejectBoost = config.ejectBoost;
        this.isSpritesheet = config.isSpritesheet;
        
        this.isAttached = config.attached && target != null && target.isAlive();
        this.target = isAttached ? target : null;
        if (isAttached) {
            this.offsetFromTarget.set(spawnPos).sub(target.getPosition());
        }

        if (config.randomRotation) {
            this.rotation = MathUtils.random(0, 360);
            this.rotationalVelocity = config.rotationalVelocity * MathUtils.randomSign();
        } else {
            this.rotation = direction.angleDeg();
            this.rotationalVelocity = 0;
        }

        if (config.isSpritesheet && tex != null) {
            int frameWidth = tex.getRegionWidth() / totalFrames;
            int frameHeight = tex.getRegionHeight();
            if (frameWidth > 0 && frameHeight > 0) {
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

        // --- LÓGICA DE VELOCIDAD SEGÚN PARÁMETROS ---
        if (hasPhysics && floorOffset > 0 && ejectSpeed != null) {
            this.groundY = spawnPos.y - floorOffset;
            Vector2 ejectionDir = new Vector2(direction).rotateDeg(config.angle);
            this.velocity.set(ejectionDir).scl(MathUtils.random(ejectSpeed[0], ejectSpeed[1]));
            this.velocity.y += MathUtils.random(ejectBoost[0], ejectBoost[1]);
        } else if (hasPhysics) {
            this.groundY = spawnPos.y - floorOffset;
            this.velocity.set(direction);
        } else {
            this.velocity.set(direction);
        }
    }

    @Override
    public void reset() {
        isAlive = false;
        position.setZero();
        velocity.setZero();
        texture = null;
        currentFrame = 0;
        spriteFrames = null;
    }

    public void update(float delta) {
        if (!isAlive) return;

        lifeTime += delta;
        float progress = MathUtils.clamp(lifeTime / maxLifeTime, 0f, 1f);

        if (lifeTime >= maxLifeTime) {
            isAlive = false;
            return;
        }

        currentColor.set(startColor).lerp(endColor, progress);

        // Escalado según grow
        if (grow > 1.0f) {
            currentSize = MathUtils.lerp(size, size * grow, progress);
        } else if (grow < 1.0f && grow > 0f) {
            currentSize = size * (1.0f + progress * (1.0f - grow));
        } else {
            currentSize = size;
        }

        // Animación spritesheet
        if (isSpritesheet && spriteFrames != null) {
            currentFrame = Math.min((int)(progress * totalFrames), totalFrames - 1);
        }

        // Física
        if (isAttached && target != null && target.isAlive()) {
            position.set(target.getPosition()).add(offsetFromTarget);
        } else if (hasPhysics) {
            velocity.y += GRAVITY * delta;
            velocity.scl(friction);
            position.mulAdd(velocity, delta);

            if (position.y <= groundY && velocity.y < 0) {
                position.y = groundY;
                velocity.y = -velocity.y * bounce;
                velocity.x *= bounce;
                rotationalVelocity *= bounce;
            }
        } else {
            position.mulAdd(velocity, delta);
        }
        rotation += rotationalVelocity * delta;
    }

    public void render(Batch batch) {
        if (!isAlive || texture == null) return;
        float alpha = fadeOut ? 1.0f - (lifeTime / maxLifeTime) : 1.0f;
        batch.setColor(currentColor.r, currentColor.g, currentColor.b, alpha);
        
        if (isSpritesheet && spriteFrames != null) {
            TextureRegion frame = spriteFrames[currentFrame];
            batch.draw(frame, position.x - currentSize/2, position.y - currentSize/2, currentSize/2, currentSize/2, currentSize, currentSize, 1f, 1f, rotation);
        } else {
            batch.draw(texture, position.x - currentSize/2, position.y - currentSize/2, currentSize/2, currentSize/2, currentSize, currentSize, 1f, 1f, rotation);
        }
        batch.setColor(1, 1, 1, 1);
    }

    public boolean isAlive() { return isAlive; }
}
