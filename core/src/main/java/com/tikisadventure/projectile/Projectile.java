package com.tikisadventure.projectile;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class Projectile {
    private Vector2 position = new Vector2();
    private Vector2 direction = new Vector2();
    private float speed;
    private float damage;

    // --- ESCALADO Y ESTADO ---
    private float baseRadius;    // Radio original para cálculos de arco
    private float currentRadius; // Radio visual actual
    private boolean sensorMode = false; // Si es TRUE, atraviesa enemigos sin chocar

    private float stateTime = 0;
    private boolean alive = true;

    private Entity owner;
    private TextureRegion sprite;

    private EffectManager effectManager;
    private EffectType trailType;
    private float trailSpacing;
    private Vector2 lastTrailPos = new Vector2();

    private Array<ProjectileBehavior> behaviors = new Array<>();

    public Projectile(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float radius,
                      TextureRegion sprite, EffectManager em, EffectType trailType, float trailSpacing) {
        this.owner = owner;
        this.position.set(pos);
        this.lastTrailPos.set(pos);
        this.direction.set(dir).nor();
        this.speed = speed;
        this.damage = dmg;

        this.baseRadius = radius;
        this.currentRadius = radius;

        this.sprite = sprite;
        this.effectManager = em;
        this.trailType = trailType;
        this.trailSpacing = trailSpacing;
    }

    public void addBehavior(ProjectileBehavior b) {
        behaviors.add(b);
    }

    public void update(float delta, Array<Entity> enemies) {
        if (!alive) return;
        stateTime += delta;

        for (ProjectileBehavior b : behaviors) {
            b.update(this, delta, enemies);
        }

        if (trailType != null && effectManager != null && trailSpacing > 0) {
            float distMoved = position.dst(lastTrailPos);

            if (distMoved >= trailSpacing) {
                int count = (int) (distMoved / trailSpacing);
                Vector2 tempPos = new Vector2();

                for (int i = 0; i < count; i++) {
                    float t = (float) i / count;
                    tempPos.set(lastTrailPos).lerp(position, t);
                    effectManager.spawnEffect(trailType, tempPos, new Vector2(direction).scl(-1f));
                }
                lastTrailPos.set(position);
            }
        }
    }

    public void render(Batch batch) {
        if (!alive || sprite == null) return;

        float angle = direction.angleDeg();
        float width = currentRadius * 2;
        float aspectRatio = (float) sprite.getRegionHeight() / sprite.getRegionWidth();
        float height = width * aspectRatio;

        batch.draw(
            sprite,
            position.x - width / 2f,
            position.y - height / 2f,
            width / 2f,
            height / 2f,
            width,
            height,
            1f, 1f,
            angle
        );
    }

    public void die() {
        this.alive = false;
    }

    // --- GETTERS & SETTERS ---
    public Vector2 getPosition() { return position; }
    public Vector2 getDirection() { return direction; }
    public float getSpeed() { return speed; }
    public float getDamage() { return damage; }

    public float getBaseRadius() { return baseRadius; }
    public float getRadius() { return currentRadius; }
    public void setRadius(float radius) { this.currentRadius = Math.max(0.01f, radius); }

    // Métodos para el modo fantasma
    public boolean isSensorMode() { return sensorMode; }
    public void setSensorMode(boolean sensorMode) { this.sensorMode = sensorMode; }

    public Entity getOwner() { return owner; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public float getStateTime() { return stateTime; }

    public <T extends ProjectileBehavior> T getBehavior(Class<T> type) {
        for (ProjectileBehavior b : behaviors) {
            if (type.isInstance(b)) return type.cast(b);
        }
        return null;
    }
}
