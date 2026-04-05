package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.components.traits.DamageDealer;
import com.tikisadventure.components.traits.Orientable;
import com.tikisadventure.components.traits.Ownable;
import com.tikisadventure.components.traits.PositionProvider;
import com.tikisadventure.components.traits.RadiusProvider;
import com.tikisadventure.components.traits.SpeedProvider;
import com.tikisadventure.components.traits.Killable;
import com.tikisadventure.components.traits.Sensorable;
import com.tikisadventure.components.traits.Timed;
import com.tikisadventure.components.traits.Piercing;
import com.tikisadventure.components.PenetrationComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import java.util.HashMap;
import java.util.Map;

public class Projectile implements PositionProvider, Orientable, SpeedProvider, DamageDealer,
    RadiusProvider, Ownable, Timed, Killable, Sensorable, Piercing {

    private Vector2 position = new Vector2();
    private Vector2 direction = new Vector2();
    private float speed;
    private float damage;
    private float baseRadius;
    private float currentRadius;
    private boolean sensorMode = false;
    private float stateTime = 0;
    private boolean alive = true;
    private Entity owner;
    private TextureRegion sprite;
    private EffectManager effectManager;
    private EffectType trailType;
    private float trailSpacing;
    private Vector2 lastTrailPos = new Vector2();
    private float trailAccumulator = 0f;

    // Explosive Data
    private boolean explosive = false;
    private float explosionRadius;
    private float explosionDamage;
    private float knockbackForce;

    // Lifetime Data
    private float maxLifetime = -1f;

    // Penetration Data
    private int remainingPenetrations = 0;

    private Map<Entity, Float> lastHitTimes = new HashMap<>();

    public Projectile(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float radius,
                      TextureRegion sprite, EffectManager em, EffectType trailType, float trailSpacing) {
        this.owner = owner;
        this.position.set(pos);
        this.lastTrailPos.set(pos);
        this.trailAccumulator = 0f;
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

    public void setExplosive(float radius, float damage, float knockback) {
        this.explosive = true;
        this.explosionRadius = radius;
        this.explosionDamage = damage;
        this.knockbackForce = knockback;
    }
    public boolean isExplosive() { return explosive; }
    public float getExplosionRadius() { return explosionRadius; }
    public float getExplosionDamage() { return explosionDamage; }
    public float getKnockbackForce() { return knockbackForce; }

    public void setLifetime(float seconds) { this.maxLifetime = seconds; }
    public boolean isExpired() { return maxLifetime > 0 && stateTime >= maxLifetime; }

    public void setPenetrations(int count) { this.remainingPenetrations = count; }
    public boolean canPenetrate() { return remainingPenetrations > 0; }
    public void reducePenetration() { remainingPenetrations--; }

    @Override
    public PenetrationComponent getPenetrationComponent() {
        return new PenetrationComponent(remainingPenetrations);
    }

    public boolean canHit(Entity entity) {
        Float lastHit = lastHitTimes.get(entity);
        return lastHit == null || (stateTime - lastHit) > 0.2f;
    }

    public void registerHit(Entity entity) {
        lastHitTimes.put(entity, stateTime);
    }

    public void update(float delta) {
        if (!alive) return;
        stateTime += delta;
        position.mulAdd(direction, speed * delta);

        if (isExpired()) {
            die();
        }

        if (trailType != null && effectManager != null && trailSpacing > 0) {
            float distMoved = position.dst(lastTrailPos);
            trailAccumulator += distMoved;

            if (trailAccumulator >= trailSpacing) {
                int count = (int) (trailAccumulator / trailSpacing);
                Vector2 tempPos = new Vector2();
                for (int i = 1; i <= count; i++) {
                    float t = (i * trailSpacing) / trailAccumulator;
                    tempPos.set(lastTrailPos).lerp(position, t);
                    effectManager.spawnEffect(trailType, tempPos, new Vector2(direction).scl(-1f));
                }
                trailAccumulator %= trailSpacing;
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
        batch.draw(sprite, position.x - width / 2f, position.y - height / 2f, width / 2f, height / 2f, width, height, 1f, 1f, angle);
    }

    @Override public void die() {
        if (alive && explosive) { explode(); }
        this.alive = false;
    }

    private void explode() {
        if (effectManager == null) return;
        effectManager.spawnEffect(EffectType.EXPLOSION_FLASH, position, new Vector2(0, 0));
        // Smoke/Sparks...
    }

    @Override public Vector2 getPosition() { return position; }
    @Override public void setPosition(Vector2 pos) { this.position.set(pos); }
    @Override public Vector2 getDirection() { return direction; }
    @Override public void setDirection(Vector2 dir) { this.direction.set(dir).nor(); }
    @Override public float getSpeed() { return speed; }
    @Override public void setSpeed(float speed) { this.speed = speed; }
    @Override public float getDamage() { return damage; }
    @Override public void setDamage(float damage) { this.damage = damage; }
    @Override public float getRadius() { return currentRadius; }
    @Override public void setRadius(float radius) { this.currentRadius = Math.max(0.01f, radius); }
    @Override public boolean isSensorMode() { return sensorMode; }
    @Override public void setSensorMode(boolean sensorMode) { this.sensorMode = sensorMode; }
    @Override public Entity getOwner() { return owner; }
    @Override public void setOwner(Object owner) { this.owner = (Entity) owner; }
    @Override public float getStateTime() { return stateTime; }
    @Override public void setStateTime(float time) { this.stateTime = time; }
    @Override public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
}
